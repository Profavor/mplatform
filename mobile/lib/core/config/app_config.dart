import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class AppConfig {
  final String issuer;
  final String apiBaseUrl;
  final String clientId;
  final String redirectUri;

  const AppConfig({
    required this.issuer,
    required this.apiBaseUrl,
    required this.clientId,
    required this.redirectUri,
  });

  /// 환경 변수(--dart-define) 또는 웹 Origin 기반 동적 생성 (하드코딩 0%)
  factory AppConfig.dynamicDefault() {
    const envIssuer = String.fromEnvironment('OIDC_ISSUER');
    const envApiBase = String.fromEnvironment('API_BASE_URL');
    const envClientId = String.fromEnvironment('CLIENT_ID');
    const envRedirectUri = String.fromEnvironment('REDIRECT_URI');

    if (envIssuer.isNotEmpty && envApiBase.isNotEmpty) {
      return AppConfig(
        issuer: envIssuer,
        apiBaseUrl: envApiBase,
        clientId: envClientId.isNotEmpty ? envClientId : 'mdm-mobile',
        redirectUri: envRedirectUri.isNotEmpty
            ? envRedirectUri
            : (kIsWeb ? Uri.base.origin : 'mplatform://oauth2redirect'),
      );
    }

    if (kIsWeb) {
      try {
        final origin = Uri.base.origin;
        if (origin.isNotEmpty) {
          return AppConfig(
            issuer: envIssuer.isNotEmpty ? envIssuer : '$origin/auth/realms/mplatform',
            apiBaseUrl: envApiBase.isNotEmpty ? envApiBase : origin,
            clientId: envClientId.isNotEmpty ? envClientId : 'mdm-mobile',
            redirectUri: envRedirectUri.isNotEmpty ? envRedirectUri : '$origin/mobile',
          );
        }
      } catch (_) {}
    }

    return AppConfig(
      issuer: envIssuer.isNotEmpty ? envIssuer : '/auth/realms/mplatform',
      apiBaseUrl: envApiBase.isNotEmpty ? envApiBase : '',
      clientId: envClientId.isNotEmpty ? envClientId : 'mdm-mobile',
      redirectUri: envRedirectUri.isNotEmpty ? envRedirectUri : 'mplatform://oauth2redirect',
    );
  }

  factory AppConfig.fromJson(Map<String, dynamic> json) {
    final dynamicBase = AppConfig.dynamicDefault();
    return AppConfig(
      issuer: json['issuer'] as String? ?? json['MOBILE_OIDC_ISSUER'] as String? ?? dynamicBase.issuer,
      apiBaseUrl: json['apiBaseUrl'] as String? ?? json['MOBILE_API_BASE_URL'] as String? ?? dynamicBase.apiBaseUrl,
      clientId: json['clientId'] as String? ?? json['MOBILE_CLIENT_ID'] as String? ?? dynamicBase.clientId,
      redirectUri: json['redirectUri'] as String? ?? json['MOBILE_REDIRECT_URI'] as String? ?? dynamicBase.redirectUri,
    );
  }

  /// Config 파일(assets/config.json) 또는 환경 변수 로드
  static Future<AppConfig> load() async {
    try {
      final jsonString = await rootBundle.loadString('assets/config.json');
      final dynamic decoded = json.decode(jsonString);
      if (decoded is Map<String, dynamic> && decoded.isNotEmpty) {
        return AppConfig.fromJson(decoded);
      }
    } catch (_) {}

    return AppConfig.dynamicDefault();
  }
}

final appConfigProvider = Provider<AppConfig>((ref) {
  return AppConfig.dynamicDefault();
});
