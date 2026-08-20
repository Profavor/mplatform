import 'dart:convert';
import 'dart:math';
import 'package:crypto/crypto.dart';
import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/config/app_config.dart';

class OidcConfig {
  final String issuer;
  final String clientId;
  final String redirectUri;
  final String scope;

  OidcConfig({
    required this.issuer,
    this.clientId = 'mdm-mobile',
    required this.redirectUri,
    this.scope = 'openid profile email',
  });
}

final oidcConfigProvider = Provider<OidcConfig>((ref) {
  final appConfig = ref.watch(appConfigProvider);
  return OidcConfig(
    issuer: appConfig.issuer,
    clientId: appConfig.clientId,
    redirectUri: appConfig.redirectUri,
  );
});

final oidcServiceProvider = Provider<OidcService>((ref) {
  return OidcService();
});

class OidcService {
  /// Generates a cryptographic random PKCE Code Verifier (RFC 7636 Section 4.1, Alphanumeric only)
  String generateCodeVerifier([int length = 64]) {
    const charset = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    final random = Random.secure();
    return List.generate(length, (_) => charset[random.nextInt(charset.length)]).join();
  }

  /// Computes the S256 Code Challenge from the Code Verifier (RFC 7636 Section 4.2)
  String generateCodeChallenge(String codeVerifier) {
    final bytes = ascii.encode(codeVerifier);
    final digest = sha256.convert(bytes);
    return base64Url.encode(digest.bytes).replaceAll('=', '').replaceAll('+', '-').replaceAll('/', '_');
  }

  /// Constructs the standard Keycloak OIDC Authorization URL
  String buildAuthorizationUrl({
    required String issuer,
    required String clientId,
    required String redirectUri,
    required String codeChallenge,
    String? state,
    String scope = 'openid profile email',
  }) {
    final authEndpoint = '${issuer.replaceAll(RegExp(r'/+$'), '')}/protocol/openid-connect/auth';
    final uri = Uri.parse(authEndpoint).replace(queryParameters: {
      'client_id': clientId,
      'response_type': 'code',
      'redirect_uri': redirectUri,
      'code_challenge': codeChallenge,
      'code_challenge_method': 'S256',
      'scope': scope,
      if (state != null) 'state': state,
    });
    return uri.toString();
  }

  /// Exchanges the Authorization Code for Access & Refresh Tokens
  Future<Map<String, dynamic>> exchangeCodeForTokens({
    required Dio dio,
    required String tokenEndpoint,
    required String clientId,
    required String redirectUri,
    required String code,
    required String codeVerifier,
    String? clientSecret,
  }) async {
    final body = {
      'grant_type': 'authorization_code',
      'client_id': clientId,
      'redirect_uri': redirectUri,
      'code': code,
      'code_verifier': codeVerifier,
      if (clientSecret != null) 'client_secret': clientSecret,
    };

    final response = await dio.post(
      tokenEndpoint,
      data: body,
      options: Options(
        contentType: Headers.formUrlEncodedContentType,
      ),
    );

    if (response.data is Map<String, dynamic>) {
      return response.data as Map<String, dynamic>;
    } else if (response.data is String) {
      return jsonDecode(response.data as String) as Map<String, dynamic>;
    }
    return jsonDecode(jsonEncode(response.data)) as Map<String, dynamic>;
  }
}
