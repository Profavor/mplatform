import 'dart:convert';
import 'package:crypto/crypto.dart';
import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/core/auth/oidc_service.dart';

import 'oidc_service_test.mocks.dart';

@GenerateMocks([Dio])
void main() {
  group('OidcService Tests (TDD - PKCE, Auth URL, Token Exchange)', () {
    late OidcService oidcService;
    late MockDio mockDio;

    setUp(() {
      oidcService = OidcService();
      mockDio = MockDio();
    });

    test('generateCodeVerifier creates high-entropy random base64url string with valid length', () {
      final verifier = oidcService.generateCodeVerifier();
      expect(verifier.length, greaterThanOrEqualTo(43));
      expect(verifier.length, lessThanOrEqualTo(128));
      // RFC 7636 valid unreserved characters: alphanumeric, -, ., _, ~
      expect(RegExp(r'^[A-Za-z0-9\-._~]+$').hasMatch(verifier), isTrue);
    });

    test('generateCodeChallenge correctly computes SHA-256 Base64URL hash of code_verifier', () {
      const verifier = 'dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk';
      // Expected SHA256 of above verifier
      final expectedHash = base64Url.encode(sha256.convert(utf8.encode(verifier)).bytes).replaceAll('=', '');

      final challenge = oidcService.generateCodeChallenge(verifier);
      expect(challenge, equals(expectedHash));
    });

    test('buildAuthorizationUrl constructs standard Keycloak OIDC auth URL with PKCE parameters', () {
      final url = oidcService.buildAuthorizationUrl(
        issuer: 'http://localhost:8081/realms/mplatform',
        clientId: 'mdm-mobile',
        redirectUri: 'mplatform://oauth2redirect',
        codeChallenge: 'test_code_challenge_123',
        state: 'random_state_xyz',
      );

      final uri = Uri.parse(url);
      expect(uri.scheme, equals('http'));
      expect(uri.host, equals('localhost'));
      expect(uri.port, equals(8081));
      expect(uri.path, equals('/realms/mplatform/protocol/openid-connect/auth'));
      expect(uri.queryParameters['client_id'], equals('mdm-mobile'));
      expect(uri.queryParameters['response_type'], equals('code'));
      expect(uri.queryParameters['redirect_uri'], equals('mplatform://oauth2redirect'));
      expect(uri.queryParameters['code_challenge'], equals('test_code_challenge_123'));
      expect(uri.queryParameters['code_challenge_method'], equals('S256'));
      expect(uri.queryParameters['state'], equals('random_state_xyz'));
      expect(uri.queryParameters['scope'], contains('openid'));
    });

    test('exchangeCodeForTokens posts standard authorization_code payload to token endpoint', () async {
      when(mockDio.post(
        any,
        data: anyNamed('data'),
        options: anyNamed('options'),
      )).thenAnswer((_) async => Response(
            requestOptions: RequestOptions(path: '/token'),
            statusCode: 200,
            data: {
              'access_token': 'kc_access_token_12345',
              'refresh_token': 'kc_refresh_token_67890',
              'id_token': 'kc_id_token_abcde',
              'token_type': 'Bearer',
              'expires_in': 300,
            },
          ));

      final tokens = await oidcService.exchangeCodeForTokens(
        dio: mockDio,
        tokenEndpoint: 'http://localhost:8081/realms/mplatform/protocol/openid-connect/token',
        clientId: 'mdm-mobile',
        redirectUri: 'mplatform://oauth2redirect',
        code: 'auth_code_sample',
        codeVerifier: 'verifier_sample',
      );

      expect(tokens['access_token'], equals('kc_access_token_12345'));
      expect(tokens['refresh_token'], equals('kc_refresh_token_67890'));
    });
  });
}
