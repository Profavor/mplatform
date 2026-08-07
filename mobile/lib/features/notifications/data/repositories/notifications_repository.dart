import 'package:dio/dio.dart';
import 'package:mplatform_mobile/features/notifications/domain/models/notification_item.dart';

class NotificationsRepository {
  final Dio _dio;

  NotificationsRepository(this._dio);

  Future<List<NotificationItem>> getNotifications({int page = 0, int size = 20}) async {
    final response = await _dio.get(
      '/api/notifications',
      queryParameters: {'page': page, 'size': size},
    );

    final data = response.data;
    List<dynamic> list;
    if (data is Map && data.containsKey('content')) {
      list = data['content'] as List<dynamic>;
    } else if (data is List) {
      list = data;
    } else {
      list = [];
    }

    return list.map((e) => NotificationItem.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<void> markAsRead(String notificationId) async {
    await _dio.post('/api/notifications/$notificationId/read');
  }

  Future<void> markAllAsRead() async {
    await _dio.post('/api/notifications/read-all');
  }
}
