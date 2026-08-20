import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/auth/oidc_service.dart';
import 'package:mplatform_mobile/core/auth/web_redirect.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/features/auth/presentation/providers/auth_provider.dart';
import 'package:url_launcher/url_launcher.dart';

class LoginScreen extends ConsumerStatefulWidget {
  final VoidCallback? onLoginSuccess;

  const LoginScreen({super.key, this.onLoginSuccess});

  @override
  ConsumerState<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends ConsumerState<LoginScreen> {
  static final Set<String> _processedOidcCodes = {};
  bool _isSsoLoading = false;
  bool _isProcessingOidc = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _checkOidcCallback();
    });
  }

  Future<void> _checkOidcCallback() async {
    if (!kIsWeb || _isProcessingOidc) return;

    final code = Uri.base.queryParameters['code'];
    if (code == null || code.isEmpty) return;

    if (_processedOidcCodes.contains(code)) {
      print('[OIDC UI] Code $code already processed, skipping.');
      return;
    }

    _processedOidcCodes.add(code);
    _isProcessingOidc = true;

    if (mounted) {
      setState(() => _isSsoLoading = true);
    }

    try {
      final storage = ref.read(storageServiceProvider);
      final codeVerifier = getWebVerifier() ?? await storage.getOidcVerifier();
      final oidcConfig = ref.read(oidcConfigProvider);
      print('[OIDC UI] Processing code: $code, codeVerifier found: ${codeVerifier != null}');

      if (codeVerifier != null && codeVerifier.isNotEmpty) {
        final tokenEndpoint = '${oidcConfig.issuer.replaceAll(RegExp(r'/+$'), '')}/protocol/openid-connect/token';
        final success = await ref.read(authControllerProvider.notifier).loginWithOidc(
              authCode: code,
              codeVerifier: codeVerifier,
              tokenEndpoint: tokenEndpoint,
              clientId: oidcConfig.clientId,
              redirectUri: oidcConfig.redirectUri,
            );

        deleteWebVerifier();
        await storage.deleteOidcVerifier();
        clearUrlParams();
        print('[OIDC UI] loginWithOidc result: $success');

        if (success) {
          if (mounted) {
            print('[OIDC UI] Triggering onLoginSuccess navigation');
            widget.onLoginSuccess?.call();
          }
        } else {
          _processedOidcCodes.remove(code);
          if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text(AppLocalizations.of(context)!.loginSsoError)),
            );
          }
        }
      } else {
        print('[OIDC UI Error] codeVerifier was null or empty!');
        _processedOidcCodes.remove(code);
      }
    } catch (e, st) {
      print('[OIDC Callback Exception]: $e');
      print('[OIDC Callback StackTrace]: $st');
      _processedOidcCodes.remove(code);
    } finally {
      _isProcessingOidc = false;
      if (mounted) {
        setState(() => _isSsoLoading = false);
      }
    }
  }

  Future<void> _handleSsoLogin() async {
    final l10n = AppLocalizations.of(context)!;
    final oidcService = ref.read(oidcServiceProvider);
    final oidcConfig = ref.read(oidcConfigProvider);
    final storage = ref.read(storageServiceProvider);

    setState(() => _isSsoLoading = true);

    try {
      final codeVerifier = oidcService.generateCodeVerifier();
      final codeChallenge = oidcService.generateCodeChallenge(codeVerifier);

      saveWebVerifier(codeVerifier);
      await storage.saveOidcVerifier(codeVerifier);

      final authUrl = oidcService.buildAuthorizationUrl(
        issuer: oidcConfig.issuer,
        clientId: oidcConfig.clientId,
        redirectUri: oidcConfig.redirectUri,
        codeChallenge: codeChallenge,
        scope: oidcConfig.scope,
      );

      await redirectToUrl(authUrl);
    } catch (e) {
      debugPrint('[OIDC Login Error]: $e');
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(l10n.loginSsoError)),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _isSsoLoading = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final theme = Theme.of(context);

    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 28.0, vertical: 24.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // App Logo and Header
                Center(
                  child: Container(
                    padding: const EdgeInsets.all(20),
                    decoration: BoxDecoration(
                      color: Colors.deepPurple.shade50,
                      shape: BoxShape.circle,
                      boxShadow: [
                        BoxShadow(
                          color: Colors.deepPurple.withOpacity(0.12),
                          blurRadius: 16,
                          offset: const Offset(0, 4),
                        ),
                      ],
                    ),
                    child: const Icon(Icons.hub, size: 56, color: Colors.deepPurple),
                  ),
                ),
                const SizedBox(height: 24),
                Text(
                  'Domain System',
                  textAlign: TextAlign.center,
                  style: theme.textTheme.headlineSmall?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: const Color(0xFF1E293B),
                    letterSpacing: -0.5,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  l10n.loginTitleSub,
                  textAlign: TextAlign.center,
                  style: theme.textTheme.bodyMedium?.copyWith(
                    color: const Color(0xFF64748B),
                    height: 1.4,
                  ),
                ),
                const SizedBox(height: 48),

                // Keycloak OIDC SSO Single Luxury Card
                Card(
                  elevation: 2,
                  shadowColor: Colors.black.withOpacity(0.06),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(16),
                    side: BorderSide(color: Colors.grey.shade200),
                  ),
                  child: Padding(
                    padding: const EdgeInsets.all(24.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        SizedBox(
                          height: 52,
                          child: ElevatedButton(
                            onPressed: _isSsoLoading ? null : _handleSsoLogin,
                            style: ElevatedButton.styleFrom(
                              backgroundColor: Colors.deepPurple,
                              foregroundColor: Colors.white,
                              elevation: 2,
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                              textStyle: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                            ),
                            child: Row(
                              mainAxisAlignment: MainAxisAlignment.center,
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                if (_isSsoLoading) ...[
                                  const SizedBox(
                                    width: 20,
                                    height: 20,
                                    child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2.5),
                                  ),
                                  const SizedBox(width: 12),
                                ] else ...[
                                  const Icon(Icons.lock_open_rounded, size: 22),
                                  const SizedBox(width: 8),
                                ],
                                Text(l10n.loginWithKeycloak),
                              ],
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 48),

                // Footer Copyright
                Text(
                  '© 2026 Domain System. All rights reserved.',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 12,
                    color: Colors.grey[400],
                    letterSpacing: 0.2,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
