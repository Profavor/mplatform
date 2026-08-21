import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/features/inbox/domain/models/inbox_folder_count_model.dart';
import 'package:mplatform_mobile/features/inbox/domain/models/inbox_message_model.dart';

abstract class InboxRepository {
  Future<List<InboxMessageModel>> getMessages({
    String folder = 'INBOX',
    int page = 0,
    int size = 20,
    String? keyword,
  });

  Future<InboxMessageModel> getMessage(String id);

  Future<InboxMessageModel> sendMessage(Map<String, dynamic> request);

  Future<InboxMessageModel> saveDraft(Map<String, dynamic> request);

  Future<InboxMessageModel> updateDraft(String id, Map<String, dynamic> request);

  Future<InboxMessageModel> reply(String id, Map<String, dynamic> request);

  Future<InboxMessageModel> replyAll(String id, Map<String, dynamic> request);

  Future<InboxMessageModel> forward(String id, Map<String, dynamic> request);

  Future<void> toggleRead(String id, bool isRead);

  Future<void> toggleStar(String id);

  Future<void> moveToFolder(String id, String folder);

  Future<void> deleteMessage(String id, {bool permanent = false});

  Future<void> bulkAction(String action, List<String> messageIds);

  Future<Map<String, dynamic>> recallMessage(String id);

  Future<List<InboxFolderCountModel>> getFolderCounts();

  Future<int> getUnreadCount();
}

class InboxRepositoryImpl implements InboxRepository {
  final Dio _dio;

  InboxRepositoryImpl(this._dio);

  @override
  Future<List<InboxMessageModel>> getMessages({
    String folder = 'INBOX',
    int page = 0,
    int size = 20,
    String? keyword,
  }) async {
    final queryParams = <String, dynamic>{
      'folder': folder,
      'page': page,
      'size': size,
    };
    if (keyword != null && keyword.trim().isNotEmpty) {
      queryParams['keyword'] = keyword.trim();
    }

    final response = await _dio.get('/api/inbox/messages', queryParameters: queryParams);
    final data = response.data;
    List<dynamic> list = [];
    if (data is Map && data.containsKey('content') && data['content'] is List) {
      list = data['content'] as List<dynamic>;
    } else if (data is List) {
      list = data;
    }

    return list.map((e) => InboxMessageModel.fromJson(e as Map<String, dynamic>)).toList();
  }

  @override
  Future<InboxMessageModel> getMessage(String id) async {
    final response = await _dio.get('/api/inbox/messages/$id');
    return InboxMessageModel.fromJson(response.data as Map<String, dynamic>);
  }

  @override
  Future<InboxMessageModel> sendMessage(Map<String, dynamic> request) async {
    final response = await _dio.post('/api/inbox/messages', data: request);
    return InboxMessageModel.fromJson(response.data as Map<String, dynamic>);
  }

  @override
  Future<InboxMessageModel> saveDraft(Map<String, dynamic> request) async {
    final payload = Map<String, dynamic>.from(request);
    payload['isDraft'] = true;
    payload['draft'] = true;
    final response = await _dio.post('/api/inbox/messages', data: payload);
    return InboxMessageModel.fromJson(response.data as Map<String, dynamic>);
  }

  @override
  Future<InboxMessageModel> updateDraft(String id, Map<String, dynamic> request) async {
    final response = await _dio.put('/api/inbox/messages/$id', data: request);
    return InboxMessageModel.fromJson(response.data as Map<String, dynamic>);
  }

  @override
  Future<InboxMessageModel> reply(String id, Map<String, dynamic> request) async {
    final response = await _dio.post('/api/inbox/messages/$id/reply', data: request);
    return InboxMessageModel.fromJson(response.data as Map<String, dynamic>);
  }

  @override
  Future<InboxMessageModel> replyAll(String id, Map<String, dynamic> request) async {
    final response = await _dio.post('/api/inbox/messages/$id/reply-all', data: request);
    return InboxMessageModel.fromJson(response.data as Map<String, dynamic>);
  }

  @override
  Future<InboxMessageModel> forward(String id, Map<String, dynamic> request) async {
    final response = await _dio.post('/api/inbox/messages/$id/forward', data: request);
    return InboxMessageModel.fromJson(response.data as Map<String, dynamic>);
  }

  @override
  Future<void> toggleRead(String id, bool isRead) async {
    await _dio.patch('/api/inbox/messages/$id/read', data: {'isRead': isRead});
  }

  @override
  Future<void> toggleStar(String id) async {
    await _dio.patch('/api/inbox/messages/$id/star');
  }

  @override
  Future<void> moveToFolder(String id, String folder) async {
    await _dio.patch('/api/inbox/messages/$id/folder', data: {'folder': folder});
  }

  @override
  Future<void> deleteMessage(String id, {bool permanent = false}) async {
    await _dio.delete('/api/inbox/messages/$id', queryParameters: {'permanent': permanent});
  }

  @override
  Future<void> bulkAction(String action, List<String> messageIds) async {
    await _dio.post('/api/inbox/messages/bulk-action', data: {
      'action': action,
      'messageIds': messageIds,
    });
  }

  @override
  Future<Map<String, dynamic>> recallMessage(String id) async {
    final response = await _dio.post('/api/inbox/messages/$id/recall');
    return (response.data as Map<String, dynamic>?) ?? {};
  }

  @override
  Future<List<InboxFolderCountModel>> getFolderCounts() async {
    final response = await _dio.get('/api/inbox/folder-counts');
    final data = response.data;
    if (data is List) {
      return data.map((e) => InboxFolderCountModel.fromJson(e as Map<String, dynamic>)).toList();
    }
    return [];
  }

  @override
  Future<int> getUnreadCount() async {
    final response = await _dio.get('/api/inbox/unread-count');
    final data = response.data;
    if (data is Map && data.containsKey('unreadCount')) {
      return ((data['unreadCount'] ?? 0) as num).toInt();
    }
    return 0;
  }
}

final inboxRepositoryProvider = Provider<InboxRepository>((ref) {
  final dio = ref.watch(dioProvider);
  return InboxRepositoryImpl(dio);
});
