import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/core/network/interceptors/auth_interceptor.dart';
import 'package:mplatform_mobile/core/storage/storage_service.dart';

import 'auth_interceptor_test.mocks.dart';

class _FakeErrorInterceptorHandler extends ErrorInterceptorHandler {
  @override
  void next(DioException err) {}
  @override
  void reject(DioException err, [bool? ignore = false]) {}
  @override
  void resolve(Response response) {}
}

@GenerateMocks([StorageService, Dio])
void main() {
  group('AuthInterceptor Tests (TDD - Refresh Token & Expiry Protection)', () {
    late MockStorageService mockStorageService;
    late MockDio mockDio;
    late AuthInterceptor interceptor;
    bool expiredCallbackCalled = false;

    setUp(() {
      mockStorageService = MockStorageService();
      mockDio = MockDio();
      expiredCallbackCalled = false;
      interceptor = AuthInterceptor(
        mockStorageService,
        mockDio,
        onAuthenticationExpired: () {
          expiredCallbackCalled = true;
        },
      );
    });

    test('injects Authorization Bearer header when token exists in storage', () async {
      when(mockStorageService.getAccessToken()).thenAnswer((_) async => 'valid_jwt_token_123');

      final options = RequestOptions(path: '/api/records');
      final handler = RequestInterceptorHandler();

      await interceptor.onRequest(options, handler);

      expect(options.headers['Authorization'], equals('Bearer valid_jwt_token_123'));
      verify(mockStorageService.getAccessToken()).called(1);
    });

    test('does not inject Authorization header when token is null', () async {
      when(mockStorageService.getAccessToken()).thenAnswer((_) async => null);

      final options = RequestOptions(path: '/api/auth/login');
      final handler = RequestInterceptorHandler();

      await interceptor.onRequest(options, handler);

      expect(options.headers.containsKey('Authorization'), isFalse);
    });

    test('reissues access token using Spring Boot "token" field on 401 Unauthorized error and retries request', () async {
      when(mockStorageService.getRefreshToken()).thenAnswer((_) async => 'valid_refresh_token');
      when(mockStorageService.saveTokens(
        accessToken: anyNamed('accessToken'),
        refreshToken: anyNamed('refreshToken'),
      )).thenAnswer((_) async {});

      final failedRequest = RequestOptions(path: '/api/domains', headers: {'Authorization': 'Bearer old_expired_token'});
      final err = DioException(
        requestOptions: failedRequest,
        response: Response(requestOptions: failedRequest, statusCode: 401),
      );

      when(mockDio.post('/api/auth/refresh', data: {'refreshToken': 'valid_refresh_token'})).thenAnswer(
        (_) async => Response(
          requestOptions: RequestOptions(path: '/api/auth/refresh'),
          statusCode: 200,
          data: {
            'token': 'new_reissued_access_token',
            'refreshToken': 'new_reissued_refresh_token',
          },
        ),
      );

      when(mockDio.fetch(any)).thenAnswer(
        (invocation) async => Response(
          requestOptions: invocation.positionalArguments.first as RequestOptions,
          statusCode: 200,
          data: {'status': 'success'},
        ),
      );

      final handler = _FakeErrorInterceptorHandler();
      await interceptor.onError(err, handler);

      verify(mockStorageService.saveTokens(
        accessToken: 'new_reissued_access_token',
        refreshToken: 'new_reissued_refresh_token',
      )).called(1);
      expect(expiredCallbackCalled, isFalse);
    });

    test('calls onAuthenticationExpired and clears storage when refresh token is null or invalid during 401', () async {
      when(mockStorageService.getRefreshToken()).thenAnswer((_) async => null);
      when(mockStorageService.deleteTokens()).thenAnswer((_) async {});

      final failedRequest = RequestOptions(path: '/api/domains');
      final err = DioException(
        requestOptions: failedRequest,
        response: Response(requestOptions: failedRequest, statusCode: 401),
      );

      final handler = _FakeErrorInterceptorHandler();
      await interceptor.onError(err, handler);

      verify(mockStorageService.deleteTokens()).called(1);
      expect(expiredCallbackCalled, isTrue);
    });

    test('calls onAuthenticationExpired when refresh API returns exception or non-200', () async {
      when(mockStorageService.getRefreshToken()).thenAnswer((_) async => 'invalid_refresh_token');
      when(mockStorageService.deleteTokens()).thenAnswer((_) async {});

      when(mockDio.post('/api/auth/refresh', data: {'refreshToken': 'invalid_refresh_token'})).thenThrow(
        DioException(requestOptions: RequestOptions(path: '/api/auth/refresh')),
      );

      final failedRequest = RequestOptions(path: '/api/domains');
      final err = DioException(
        requestOptions: failedRequest,
        response: Response(requestOptions: failedRequest, statusCode: 401),
      );

      final handler = _FakeErrorInterceptorHandler();
      await interceptor.onError(err, handler);

      verify(mockStorageService.deleteTokens()).called(1);
      expect(expiredCallbackCalled, isTrue);
    });
  });
}
