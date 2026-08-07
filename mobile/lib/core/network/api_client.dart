import 'package:dio/dio.dart';
import 'package:mplatform_mobile/core/network/interceptors/auth_interceptor.dart';
import 'package:mplatform_mobile/core/network/interceptors/timezone_interceptor.dart';

class ApiClient {
  final Dio dio;

  ApiClient({
    required AuthInterceptor authInterceptor,
    required TimezoneInterceptor timezoneInterceptor,
    String baseUrl = 'http://localhost:8080',
  }) : dio = Dio(
          BaseOptions(
            baseUrl: baseUrl,
            connectTimeout: const Duration(seconds: 15),
            receiveTimeout: const Duration(seconds: 15),
            headers: {'Content-Type': 'application/json'},
          ),
        ) {
    dio.interceptors.addAll([
      authInterceptor,
      timezoneInterceptor,
      LogInterceptor(requestBody: true, responseBody: true, error: true),
    ]);
  }
}
