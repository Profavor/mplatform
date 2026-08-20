import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/core/storage/storage_service.dart';
import 'package:mplatform_mobile/features/auth/data/repositories/auth_repository.dart';

import 'auth_repository_test.mocks.dart';

@GenerateMocks([Dio, StorageService])
void main() {
  group('AuthRepository Tests (TDD)', () {
    late MockDio mockDio;
    late MockStorageService mockStorageService;
    late AuthRepository repository;

    setUp(() {
      mockDio = MockDio();
      mockStorageService = MockStorageService();
      repository = AuthRepository(mockDio, mockStorageService);
    });

    test('login calls POST /api/auth/login, saves tokens, and returns AuthResponse', () async {
      final mockResponseData = {
        'accessToken': 'new_access_token',
        'refreshToken': 'new_refresh_token',
        'user': {
          'id': 1,
          'username': 'admin',
          'name': 'Administrator',
          'role': 'ADMIN',
          'department': 'IT',
          'email': 'admin@example.com'
        }
      };

      when(mockDio.post(
        '/api/auth/login',
        data: anyNamed('data'),
        options: anyNamed('options'),
        cancelToken: anyNamed('cancelToken'),
        onSendProgress: anyNamed('onSendProgress'),
        onReceiveProgress: anyNamed('onReceiveProgress'),
      )).thenAnswer((_) async => Response(
            requestOptions: RequestOptions(path: '/api/auth/login'),
            statusCode: 200,
            data: mockResponseData,
          ));

      when(mockStorageService.saveTokens(
        accessToken: anyNamed('accessToken'),
        refreshToken: anyNamed('refreshToken'),
      )).thenAnswer((_) async => {});

      final result = await repository.login(username: 'admin', password: 'password');

      expect(result.accessToken, equals('new_access_token'));
      expect(result.user.username, equals('admin'));
      verify(mockStorageService.saveTokens(
        accessToken: 'new_access_token',
        refreshToken: 'new_refresh_token',
      )).called(1);
    });

    test('loginWithOidc exchanges auth code, saves tokens, and fetches current user', () async {
      when(mockStorageService.saveTokens(
        accessToken: anyNamed('accessToken'),
        refreshToken: anyNamed('refreshToken'),
      )).thenAnswer((_) async => {});

      when(mockStorageService.getAccessToken()).thenAnswer((_) async => 'exchanged_token_123');

      when(mockDio.post(
        any,
        data: anyNamed('data'),
        options: anyNamed('options'),
      )).thenAnswer((_) async => Response(
            requestOptions: RequestOptions(path: '/token'),
            statusCode: 200,
            data: {
              'access_token': 'exchanged_token_123',
              'refresh_token': 'refresh_token_456',
            },
          ));

      when(mockDio.get('/api/auth/me', options: anyNamed('options'))).thenAnswer((_) async => Response(
            requestOptions: RequestOptions(path: '/api/auth/me'),
            statusCode: 200,
            data: {
              'id': 'user-oidc-1',
              'username': 'kc_user',
              'name': 'Keycloak User',
              'role': 'ROLE_USER',
              'department': 'R&D',
            },
          ));

      final user = await repository.loginWithOidc(
        authCode: 'mock_code',
        codeVerifier: 'mock_verifier',
      );

      expect(user.username, equals('kc_user'));
      expect(user.name, equals('Keycloak User'));
      verify(mockStorageService.saveTokens(
        accessToken: 'exchanged_token_123',
        refreshToken: 'refresh_token_456',
      )).called(1);
    });

    test('logout clears secure tokens even if server call fails', () async {
      when(mockDio.post('/api/auth/logout', data: anyNamed('data'), options: anyNamed('options'), cancelToken: anyNamed('cancelToken'), onSendProgress: anyNamed('onSendProgress'), onReceiveProgress: anyNamed('onReceiveProgress'))).thenThrow(DioException(requestOptions: RequestOptions(path: '/api/auth/logout')));
      when(mockStorageService.deleteTokens()).thenAnswer((_) async => {});

      await repository.logout();

      verify(mockStorageService.deleteTokens()).called(1);
    });
  });
}
