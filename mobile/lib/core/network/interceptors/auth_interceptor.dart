import 'dart:ui';
import 'package:dio/dio.dart';
import 'package:mplatform_mobile/core/storage/storage_service.dart';

class AuthInterceptor extends Interceptor {
  final StorageService _storageService;
  final Dio _dio;
  final VoidCallback? onAuthenticationExpired;

  AuthInterceptor(this._storageService, this._dio, {this.onAuthenticationExpired});

  @override
  Future<void> onRequest(RequestOptions options, RequestInterceptorHandler handler) async {
    final token = await _storageService.getAccessToken();
    if (token != null && token.isNotEmpty) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    super.onRequest(options, handler);
  }

  @override
  Future<void> onError(DioException err, ErrorInterceptorHandler handler) async {
    if (err.response?.statusCode == 401 && !err.requestOptions.path.contains('/api/auth/refresh')) {
      final refreshToken = await _storageService.getRefreshToken();
      if (refreshToken != null && refreshToken.isNotEmpty) {
        try {
          // Token renewal request to Spring Boot backend
          final refreshResponse = await _dio.post(
            '/api/auth/refresh',
            data: {'refreshToken': refreshToken},
          );

          if (refreshResponse.statusCode == 200 && refreshResponse.data != null) {
            final newAccessToken = (refreshResponse.data['token'] ?? refreshResponse.data['accessToken']) as String?;
            final newRefreshToken = refreshResponse.data['refreshToken'] as String?;

            if (newAccessToken != null && newRefreshToken != null) {
              await _storageService.saveTokens(
                accessToken: newAccessToken,
                refreshToken: newRefreshToken,
              );

              // Retry failed request with newly issued access token
              err.requestOptions.headers['Authorization'] = 'Bearer $newAccessToken';
              final retryResponse = await _dio.fetch(err.requestOptions);
              return handler.resolve(retryResponse);
            }
          }
          // If response format is invalid, clear and expire
          await _storageService.deleteTokens();
          onAuthenticationExpired?.call();
        } catch (e) {
          // If token refresh fails (expired refresh token), clear tokens and trigger logout redirect
          await _storageService.deleteTokens();
          onAuthenticationExpired?.call();
        }
      } else {
        await _storageService.deleteTokens();
        onAuthenticationExpired?.call();
      }
    }
    super.onError(err, handler);
  }
}
