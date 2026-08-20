import 'package:flutter_test/flutter_test.dart';
import 'package:mplatform_mobile/core/config/app_config.dart';

void main() {
  group('AppConfig Tests (TDD - Zero Hardcoding Dynamic Origin & ConfigMap)', () {
    test('dynamicDefault returns dynamic endpoints without hardcoded custom domains', () {
      final config = AppConfig.dynamicDefault();

      expect(config.issuer, contains('/realms/mplatform'));
      expect(config.clientId, equals('mdm-mobile'));
      expect(config.redirectUri, isNotEmpty);
    });

    test('fromJson parses ConfigMap payload dynamically', () {
      final jsonMap = {
        'issuer': 'http://custom-domain.com/auth/realms/mplatform',
        'apiBaseUrl': 'http://custom-domain.com/api',
        'clientId': 'mdm-mobile',
        'redirectUri': 'http://custom-domain.com/mobile',
      };

      final config = AppConfig.fromJson(jsonMap);

      expect(config.issuer, equals('http://custom-domain.com/auth/realms/mplatform'));
      expect(config.apiBaseUrl, equals('http://custom-domain.com/api'));
      expect(config.clientId, equals('mdm-mobile'));
      expect(config.redirectUri, equals('http://custom-domain.com/mobile'));
    });
  });
}
