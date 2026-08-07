import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/core/network/interceptors/timezone_interceptor.dart';
import 'package:mplatform_mobile/core/storage/storage_service.dart';

import 'timezone_interceptor_test.mocks.dart';

@GenerateMocks([StorageService])
void main() {
  group('TimezoneInterceptor Tests (TDD)', () {
    late MockStorageService mockStorageService;
    late TimezoneInterceptor interceptor;

    setUp(() {
      mockStorageService = MockStorageService();
      interceptor = TimezoneInterceptor(mockStorageService);
    });

    test('injects X-Timezone header into outgoing HTTP requests', () async {
      when(mockStorageService.getTimezone()).thenAnswer((_) async => 'Asia/Seoul');

      final options = RequestOptions(path: '/api/records');
      final handler = RequestInterceptorHandler();

      await interceptor.onRequest(options, handler);

      expect(options.headers['X-Timezone'], equals('Asia/Seoul'));
      verify(mockStorageService.getTimezone()).called(1);
    });

    test('uses Asia/Seoul as fallback when timezone is empty', () async {
      when(mockStorageService.getTimezone()).thenAnswer((_) async => '');

      final options = RequestOptions(path: '/api/records');
      final handler = RequestInterceptorHandler();

      await interceptor.onRequest(options, handler);

      expect(options.headers['X-Timezone'], equals('Asia/Seoul'));
    });
  });
}
