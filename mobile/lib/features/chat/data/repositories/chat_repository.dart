import 'package:dio/dio.dart';
import 'package:mplatform_mobile/features/chat/domain/models/chat_message_model.dart';
import 'package:mplatform_mobile/features/chat/domain/models/chat_room_model.dart';

class ChatRepository {
  final Dio _dio;

  ChatRepository(this._dio);

  Future<List<ChatRoomModel>> getChatRooms() async {
    final response = await _dio.get('/api/chat/rooms');
    final list = response.data as List<dynamic>;
    return list.map((e) => ChatRoomModel.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<List<String>> getOnlineUsers() async {
    final response = await _dio.get('/api/chat/presence');
    final list = response.data as List<dynamic>;
    return list.map((e) => e.toString()).toList();
  }

  Future<List<dynamic>> getRoomMembers(String roomId) async {
    final response = await _dio.get('/api/chat/rooms/$roomId/members');
    return response.data as List<dynamic>;
  }

  Future<List<ChatMessageModel>> getMessages(String roomId, {int page = 0, int size = 30}) async {
    final response = await _dio.get(
      '/api/chat/rooms/$roomId/messages',
      queryParameters: {'page': page, 'size': size},
    );
    final data = response.data;
    if (data is Map<String, dynamic> && data.containsKey('content')) {
      final list = data['content'] as List<dynamic>;
      return list.map((e) => ChatMessageModel.fromJson(e as Map<String, dynamic>)).toList();
    }
    final list = data as List<dynamic>;
    return list.map((e) => ChatMessageModel.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<ChatMessageModel> sendMessage(String roomId, {
    required String content,
    String? attachmentUrl,
    String messageType = 'TEXT',
    String? fileUrl,
    String? fileName,
    int? fileSize,
  }) async {
    final response = await _dio.post(
      '/api/chat/rooms/$roomId/messages',
      data: {
        'content': content,
        'messageType': messageType,
        if (fileUrl != null) 'fileUrl': fileUrl,
        if (fileName != null) 'fileName': fileName,
        if (fileSize != null) 'fileSize': fileSize,
        if (attachmentUrl != null) 'attachmentUrl': attachmentUrl,
      },
    );
    return ChatMessageModel.fromJson(response.data as Map<String, dynamic>);
  }

  Future<ChatRoomModel> createRoom({required String title, required List<String> participants}) async {
    final response = await _dio.post(
      '/api/chat/rooms',
      data: {
        'roomName': title,
        'memberUserIds': participants,
      },
    );
    return ChatRoomModel.fromJson(response.data as Map<String, dynamic>);
  }

  Future<bool> markRoomAsRead(String roomId) async {
    try {
      final response = await _dio.post('/api/chat/rooms/$roomId/read');
      return response.statusCode == 200 || response.statusCode == 204;
    } catch (e) {
      return false;
    }
  }

  Future<int> fetchTotalUnreadCount() async {
    try {
      final response = await _dio.get('/api/chat/unread-count');
      return int.tryParse(response.data.toString()) ?? 0;
    } catch (e) {
      return 0;
    }
  }

  /// 파일 업로드 → { fileUrl, fileName, fileSize } 반환
  Future<Map<String, dynamic>> uploadFile(List<int> fileBytes, String fileName) async {
    final formData = FormData.fromMap({
      'file': MultipartFile.fromBytes(fileBytes, filename: fileName),
    });
    final response = await _dio.post('/api/chat/upload', data: formData);
    return response.data as Map<String, dynamic>;
  }

  /// 번역 API 호출
  Future<String> translateMessage(String text) async {
    try {
      final response = await _dio.post('/api/chat/translate', data: {'text': text});
      final data = response.data as Map<String, dynamic>;
      return data['translated']?.toString() ?? text;
    } catch (e) {
      return text;
    }
  }

  // 규칙: DB 및 레코드 삭제 작업 시 절대로 전체 삭제나 truncate를 수행하지 않고 문제의 레코드를 개별 삭제(Delete)
  Future<bool> deleteMessage(String messageId) async {
    final response = await _dio.delete('/api/chat/messages/$messageId');
    return response.statusCode == 200 || response.statusCode == 204;
  }

  Future<bool> leaveRoom(String roomId) async {
    final response = await _dio.delete('/api/chat/rooms/$roomId/members');
    return response.statusCode == 200 || response.statusCode == 204;
  }

  Future<bool> deleteRoom(String roomId) async {
    final response = await _dio.delete('/api/chat/rooms/$roomId');
    return response.statusCode == 200 || response.statusCode == 204;
  }

  Future<bool> delegateCreator(String roomId, String newCreatorId) async {
    final response = await _dio.put(
      '/api/chat/rooms/$roomId/creator',
      data: {'newCreatorId': newCreatorId},
    );
    return response.statusCode == 200 || response.statusCode == 204;
  }

  Future<bool> inviteMembers(String roomId, List<String> userIds, {int pastMessageHours = 0}) async {
    final response = await _dio.post(
      '/api/chat/rooms/$roomId/members',
      data: {
        'userIds': userIds,
        'pastMessageHours': pastMessageHours,
      },
    );
    return response.statusCode == 200 || response.statusCode == 204;
  }

  Future<bool> kickMember(String roomId, String targetUserId) async {
    final response = await _dio.delete('/api/chat/rooms/$roomId/members/$targetUserId');
    return response.statusCode == 200 || response.statusCode == 204;
  }
}
