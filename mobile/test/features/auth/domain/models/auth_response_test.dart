import 'package:flutter_test/flutter_test.dart';
import 'package:mplatform_mobile/features/auth/domain/models/auth_response.dart';
import 'package:mplatform_mobile/features/auth/domain/models/user_model.dart';

void main() {
  group('AuthResponse & UserModel Parsing Tests (TDD - Spring Boot Flat Response Compatibility)', () {
    test('parses Spring Boot flat JSON with token (instead of accessToken) and UUID string id', () {
      final springBootJson = {
        'token': 'jwt_token_from_spring',
        'refreshToken': 'refresh_token_from_spring',
        'username': 'tester',
        'role': 'ROLE_USER',
        'uuid': '340a0917-af0b-4d13-a1ce-479d4b2e2ca7',
        'id': '340a0917-af0b-4d13-a1ce-479d4b2e2ca7',
        'timezone': 'Asia/Seoul',
        'serverOffset': '+09:00',
        'permissions': ['admin:read'],
        'mustChangePassword': false,
      };

      final authResponse = AuthResponse.fromJson(springBootJson);

      expect(authResponse.accessToken, equals('jwt_token_from_spring'));
      expect(authResponse.refreshToken, equals('refresh_token_from_spring'));
      expect(authResponse.user.id, equals('340a0917-af0b-4d13-a1ce-479d4b2e2ca7'));
      expect(authResponse.user.username, equals('tester'));
      expect(authResponse.user.role, equals('ROLE_USER'));
      expect(authResponse.user.timezone, equals('Asia/Seoul'));
    });

    test('parses numeric id gracefully into string representation', () {
      final jsonWithNumericId = {
        'accessToken': 'token_123',
        'refreshToken': 'refresh_123',
        'user': {
          'id': 1004,
          'username': 'numeric_user',
          'name': 'Numeric User',
          'role': 'ADMIN',
        }
      };

      final authResponse = AuthResponse.fromJson(jsonWithNumericId);

      expect(authResponse.accessToken, equals('token_123'));
      expect(authResponse.user.id, equals('1004'));
      expect(authResponse.user.name, equals('Numeric User'));
    });
  });
}
