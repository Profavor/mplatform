import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'package:mplatform_mobile/core/network/interceptors/auth_interceptor.dart';
import 'package:mplatform_mobile/core/network/interceptors/timezone_interceptor.dart';

import 'dart:io' show Platform;

String getDefaultBaseUrl() {
  const envApiBase = String.fromEnvironment('API_BASE_URL');
  if (envApiBase.isNotEmpty) {
    return envApiBase;
  }
  try {
    if (Platform.isAndroid) {
      return 'http://10.0.2.2:8080/api';
    }
  } catch (_) {}
  return '/api';
}

class ApiClient {
  final Dio dio;

  ApiClient({
    required AuthInterceptor authInterceptor,
    required TimezoneInterceptor timezoneInterceptor,
    String? baseUrl,
  }) : dio = Dio(
          BaseOptions(
            baseUrl: baseUrl ?? getDefaultBaseUrl(),
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
