import 'dart:ui';
import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:mplatform_mobile/core/network/api_client.dart';
import 'package:mplatform_mobile/core/network/interceptors/auth_interceptor.dart';
import 'package:mplatform_mobile/core/network/interceptors/timezone_interceptor.dart';
import 'package:mplatform_mobile/core/storage/storage_service.dart';
import 'package:mplatform_mobile/core/storage/storage_service_impl.dart';
import 'package:shared_preferences/shared_preferences.dart';

final sharedPreferencesProvider = Provider<SharedPreferences>((ref) {
  throw UnimplementedError('SharedPreferences must be overridden in main() initialization');
});

final secureStorageProvider = Provider<FlutterSecureStorage>((ref) {
  return const FlutterSecureStorage();
});

final storageServiceProvider = Provider<StorageService>((ref) {
  final secureStorage = ref.watch(secureStorageProvider);
  final prefs = ref.watch(sharedPreferencesProvider);
  return StorageServiceImpl(secureStorage, prefs);
});

final timezoneInterceptorProvider = Provider<TimezoneInterceptor>((ref) {
  final storageService = ref.watch(storageServiceProvider);
  return TimezoneInterceptor(storageService);
});

final tokenRefreshDioProvider = Provider<Dio>((ref) {
  return Dio(BaseOptions(
    baseUrl: 'http://localhost:8080',
    connectTimeout: const Duration(seconds: 15),
    receiveTimeout: const Duration(seconds: 15),
    headers: {'Content-Type': 'application/json'},
  ));
});

final onAuthExpiredProvider = StateProvider<VoidCallback?>((ref) => null);

final authInterceptorProvider = Provider<AuthInterceptor>((ref) {
  final storageService = ref.watch(storageServiceProvider);
  final refreshDio = ref.watch(tokenRefreshDioProvider);
  return AuthInterceptor(
    storageService,
    refreshDio,
    onAuthenticationExpired: () {
      ref.read(onAuthExpiredProvider)?.call();
    },
  );
});

final apiClientProvider = Provider<ApiClient>((ref) {
  final authInterceptor = ref.watch(authInterceptorProvider);
  final timezoneInterceptor = ref.watch(timezoneInterceptorProvider);
  return ApiClient(
    authInterceptor: authInterceptor,
    timezoneInterceptor: timezoneInterceptor,
  );
});

final dioProvider = Provider<Dio>((ref) {
  return ref.watch(apiClientProvider).dio;
});
