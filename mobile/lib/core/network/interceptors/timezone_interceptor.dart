import 'package:dio/dio.dart';
import 'package:mplatform_mobile/core/storage/storage_service.dart';

class TimezoneInterceptor extends Interceptor {
  final StorageService _storageService;

  TimezoneInterceptor(this._storageService);

  @override
  Future<void> onRequest(RequestOptions options, RequestInterceptorHandler handler) async {
    final tz = await _storageService.getTimezone();
    final headerValue = (tz.trim().isEmpty) ? 'Asia/Seoul' : tz.trim();
    options.headers['X-Timezone'] = headerValue;
    super.onRequest(options, handler);
  }
}
