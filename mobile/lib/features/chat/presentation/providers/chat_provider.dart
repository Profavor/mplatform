import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/features/chat/data/repositories/chat_repository.dart';
import 'package:mplatform_mobile/features/chat/data/services/chat_websocket_service.dart';
import 'package:mplatform_mobile/features/chat/domain/models/chat_message_model.dart';
import 'package:mplatform_mobile/features/chat/presentation/providers/chat_state.dart';
import 'package:mplatform_mobile/features/auth/presentation/providers/auth_provider.dart';

final chatRepositoryProvider = Provider<ChatRepository>((ref) {
  final dio = ref.watch(dioProvider);
  return ChatRepository(dio);
});

final chatWebSocketServiceProvider = Provider<ChatWebSocketService>((ref) {
  final storageService = ref.watch(storageServiceProvider);
  final dio = ref.watch(dioProvider);
  final wsBaseUrl = dio.options.baseUrl.replaceFirst(RegExp(r'^http'), 'ws');
  final service = ChatWebSocketService(storageService, wsBaseUrl);
  ref.onDispose(() => service.dispose());
  return service;
});

class ChatController extends StateNotifier<ChatState> {
  final ChatRepository _repository;
  final ChatWebSocketService _wsService;
  final void Function()? onForceLogout;

  ChatController(this._repository, this._wsService, {this.onForceLogout}) : super(const ChatState()) {
    // STOMP 연결은 ChatController 생성 시점 (로그인 완료 후 채팅 화면 진입 시)에만 수행
    _wsService.connect();

    _wsService.notificationStream.listen((event) {
      if (event['eventType'] == 'FORCE_LOGOUT') {
        onForceLogout?.call();
      }
    });

    _wsService.messageStream.listen((newMessage) async {
      if (newMessage.roomId == state.selectedRoomId) {
        final existsIndex = state.activeMessages.indexWhere((m) => m.messageId == newMessage.messageId);
        if (existsIndex >= 0) {
          // If exists, replace it to update unreadCount, etc.
          final updated = List<ChatMessageModel>.from(state.activeMessages);
          updated[existsIndex] = newMessage;
          state = state.copyWith(activeMessages: updated);
        } else {
          // Append new message
          state = state.copyWith(
            activeMessages: [...state.activeMessages, newMessage],
          );
        }
        await _repository.markRoomAsRead(newMessage.roomId);
      }
      // #2: 방 목록 실시간 갱신 — 새 메시지 수신 시 방 목록과 unread count 갱신
      await loadRooms();
      await fetchTotalUnreadCount();
    });

    _wsService.roomReadStream.listen((roomId) async {
      if (roomId == state.selectedRoomId) {
        // 상대방이 방을 읽었을 때 메시지를 새로고침하여 unreadCount 갱신
        try {
          final messages = await _repository.getMessages(roomId);
          state = state.copyWith(activeMessages: messages);
        } catch (_) {}
      }
      await loadRooms();
      await fetchTotalUnreadCount();
    });

    _wsService.presenceStream.listen((event) {
      final username = event['username'] as String?;
      if (username == null) return;
      final status = event['status'] as String?;
      final currentSet = Set<String>.from(state.onlineUserIds);
      if (status == 'ONLINE') {
        currentSet.add(username);
      } else if (status == 'OFFLINE') {
        currentSet.remove(username);
      }
      state = state.copyWith(onlineUserIds: currentSet);
    });
  }

  Future<void> loadRooms() async {
    state = state.copyWith(isLoadingRooms: true, errorMessage: null);
    try {
      final rooms = await _repository.getChatRooms();
      try {
        final onlineUsers = await _repository.getOnlineUsers();
        state = state.copyWith(onlineUserIds: Set<String>.from(onlineUsers));
      } catch (e) {
        // ignore presence fetch failure
      }
      state = state.copyWith(rooms: rooms, isLoadingRooms: false);
    } catch (e) {
      state = state.copyWith(isLoadingRooms: false, errorMessage: e.toString());
    }
  }

  Future<void> fetchTotalUnreadCount() async {
    try {
      final count = await _repository.fetchTotalUnreadCount();
      state = state.copyWith(totalUnreadCount: count);
    } catch (e) {
      // ignore
    }
  }

  Future<void> selectRoom(String roomId) async {
    _wsService.subscribeToRoom(roomId);
    state = state.copyWith(
      selectedRoomId: roomId,
      isLoadingMessages: true,
      activeMessages: [],
    );
    try {
      final messages = await _repository.getMessages(roomId);
      print('MESSAGES LOADED: ${messages.length}');
      state = state.copyWith(activeMessages: messages, isLoadingMessages: false);
      await _repository.markRoomAsRead(roomId);
      await fetchTotalUnreadCount();
      await loadRooms();
    } catch (e, stack) {
      print('selectRoom Error: $e\n$stack');
      state = state.copyWith(isLoadingMessages: false, errorMessage: e.toString());
    }
  }

  void clearSelectedRoom() {
    _wsService.unsubscribeFromRoom();
    state = state.copyWith(selectedRoomId: null);
  }

  Future<void> sendMessage(String roomId, {
    required String content,
    String? attachmentUrl,
    String messageType = 'TEXT',
    String? fileUrl,
    String? fileName,
    int? fileSize,
    String? url,
    String? originalName,
  }) async {
    if (content.trim().isEmpty && attachmentUrl == null && fileUrl == null && url == null) return;
    state = state.copyWith(isSending: true);
    try {
      final sentMsg = await _repository.sendMessage(
        roomId,
        content: content.trim(),
        attachmentUrl: attachmentUrl,
        messageType: messageType,
        fileUrl: fileUrl,
        fileName: fileName,
        fileSize: fileSize,
        url: url,
        originalName: originalName,
      );
      state = state.copyWith(
        activeMessages: [...state.activeMessages, sentMsg],
        isSending: false,
      );
    } catch (e) {
      state = state.copyWith(isSending: false, errorMessage: e.toString());
    }
  }

  /// 파일 업로드 후 메시지 전송
  Future<void> uploadAndSendFile(String roomId, List<int> fileBytes, String fileName, bool isImage) async {
    state = state.copyWith(isSending: true);
    try {
      final uploadResult = await _repository.uploadFile(fileBytes, fileName);
      final type = isImage ? 'IMAGE' : 'FILE';
      
      final url = uploadResult['url'] as String?;
      final originalName = uploadResult['originalName'] as String? ?? fileName;
      
      await sendMessage(
        roomId,
        content: originalName,
        messageType: type,
        fileUrl: url,
        fileName: originalName,
        url: url,
        originalName: originalName,
      );
    } catch (e) {
      state = state.copyWith(isSending: false, errorMessage: e.toString());
    }
  }

  /// 메시지 전달 (Forward)
  Future<bool> forwardMessage(ChatMessageModel message, String targetUserId, String myUserId) async {
    try {
      // 1:1 방 생성 (이미 존재하면 기존 방 반환)
      final room = await _repository.createRoom(
        title: '',
        participants: [targetUserId, myUserId],
      );
      // 전달 메시지 전송
      final forwardContent = '[전달된 메시지]\n👤 작성자: ${message.senderName}\n💬 내용: ${message.content}';
      await _repository.sendMessage(
        room.roomId,
        content: forwardContent,
        messageType: message.messageType,
        fileUrl: message.attachmentUrl,
        fileName: message.fileName,
        fileSize: message.fileSize,
      );
      return true;
    } catch (e) {
      return false;
    }
  }

  /// 번역
  Future<String> translateMessage(String text) async {
    return _repository.translateMessage(text);
  }

  // 규칙: DB 및 레코드 조작 시 Truncate 없이 문제 레코드만 개별 삭제
  Future<bool> deleteMessage(String messageId) async {
    try {
      final success = await _repository.deleteMessage(messageId);
      if (success) {
        state = state.copyWith(
          activeMessages: state.activeMessages.where((m) => m.messageId != messageId).toList(),
        );
        return true;
      }
      return false;
    } catch (e) {
      return false;
    }
  }

  Future<bool> createRoom(String title, List<String> participants) async {
    try {
      final newRoom = await _repository.createRoom(title: title, participants: participants);
      state = state.copyWith(
        rooms: [newRoom, ...state.rooms],
      );
      return true;
    } catch (e) {
      state = state.copyWith(errorMessage: e.toString());
      return false;
    }
  }

  Future<bool> leaveRoom(String roomId) async {
    try {
      final success = await _repository.leaveRoom(roomId);
      if (success) {
        state = state.copyWith(
          rooms: state.rooms.where((r) => r.roomId != roomId).toList(),
          selectedRoomId: state.selectedRoomId == roomId ? null : state.selectedRoomId,
        );
      }
      return success;
    } catch (e) {
      return false;
    }
  }

  Future<bool> deleteRoom(String roomId) async {
    try {
      final success = await _repository.deleteRoom(roomId);
      if (success) {
        state = state.copyWith(
          rooms: state.rooms.where((r) => r.roomId != roomId).toList(),
          selectedRoomId: state.selectedRoomId == roomId ? null : state.selectedRoomId,
        );
      }
      return success;
    } catch (e) {
      return false;
    }
  }

  Future<bool> delegateCreator(String roomId, String newCreatorId) async {
    try {
      final success = await _repository.delegateCreator(roomId, newCreatorId);
      if (success) {
        state = state.copyWith(
          rooms: state.rooms.map((r) {
            if (r.roomId == roomId) {
              return r.copyWith(createdBy: newCreatorId);
            }
            return r;
          }).toList(),
        );
      }
      return success;
    } catch (e) {
      return false;
    }
  }

  Future<bool> inviteMembers(String roomId, List<String> userIds, {int pastMessageHours = 0}) async {
    try {
      final success = await _repository.inviteMembers(roomId, userIds, pastMessageHours: pastMessageHours);
      return success;
    } catch (e) {
      return false;
    }
  }

  Future<bool> kickMember(String roomId, String targetUserId) async {
    try {
      final success = await _repository.kickMember(roomId, targetUserId);
      return success;
    } catch (e) {
      return false;
    }
  }
}

final chatControllerProvider = StateNotifierProvider<ChatController, ChatState>((ref) {
  final repo = ref.watch(chatRepositoryProvider);
  final ws = ref.watch(chatWebSocketServiceProvider);
  return ChatController(
    repo,
    ws,
    onForceLogout: () {
      ref.read(authControllerProvider.notifier).forceUnauthenticated();
    },
  );
});
