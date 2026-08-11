import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/features/chat/data/repositories/chat_repository.dart';
import 'package:mplatform_mobile/features/chat/data/services/chat_websocket_service.dart';
import 'package:mplatform_mobile/features/chat/domain/models/chat_message_model.dart';
import 'package:mplatform_mobile/features/chat/domain/models/chat_room_model.dart';
import 'package:mplatform_mobile/features/chat/presentation/providers/chat_provider.dart';

import 'chat_controller_test.mocks.dart';

@GenerateMocks([ChatRepository, ChatWebSocketService])
void main() {
  group('ChatController Tests (TDD - Real-time Messaging & Explicit Deletion)', () {
    late MockChatRepository mockRepository;
    late MockChatWebSocketService mockWsService;
    late ChatController controller;

    setUp(() {
      mockRepository = MockChatRepository();
      mockWsService = MockChatWebSocketService();
      when(mockWsService.messageStream).thenAnswer((_) => const Stream<ChatMessageModel>.empty());
      when(mockWsService.notificationStream).thenAnswer((_) => const Stream<Map<String, dynamic>>.empty());
      when(mockWsService.roomReadStream).thenAnswer((_) => const Stream<String>.empty());
      when(mockWsService.presenceStream).thenAnswer((_) => const Stream<Map<String, dynamic>>.empty());
      
      when(mockRepository.markRoomAsRead(any)).thenAnswer((_) async => true);
      when(mockRepository.fetchTotalUnreadCount()).thenAnswer((_) async => 0);
      when(mockRepository.getChatRooms()).thenAnswer((_) async => []);
      when(mockRepository.getOnlineUsers()).thenAnswer((_) async => []);

      controller = ChatController(mockRepository, mockWsService);
    });

    test('loadRooms populates available chat rooms in state', () async {
      const room = ChatRoomModel(roomId: 'r1', title: 'Data QA Chat', unreadCount: 1);
      when(mockRepository.getChatRooms()).thenAnswer((_) async => [room]);

      await controller.loadRooms();

      expect(controller.state.rooms.length, equals(1));
      expect(controller.state.rooms.first.title, equals('Data QA Chat'));
      expect(controller.state.isLoadingRooms, isFalse);
    });

    test('selectRoom loads messages and updates selectedRoomId', () async {
      const msg = ChatMessageModel(messageId: 'm1', roomId: 'r1', senderUsername: 'user1', senderName: 'User One', content: 'Hello');
      when(mockRepository.getMessages('r1')).thenAnswer((_) async => [msg]);

      await controller.selectRoom('r1');

      expect(controller.state.selectedRoomId, equals('r1'));
      expect(controller.state.activeMessages.length, equals(1));
      expect(controller.state.activeMessages.first.content, equals('Hello'));
    });

    test('deleteMessage explicitly removes individual record from state without truncate or clearing all', () async {
      const msg1 = ChatMessageModel(messageId: 'm1', roomId: 'r1', senderUsername: 'u1', senderName: 'U 1', content: 'Msg 1');
      const msg2 = ChatMessageModel(messageId: 'm2', roomId: 'r1', senderUsername: 'u1', senderName: 'U 1', content: 'Msg 2');
      
      // Setup initial messages in state
      controller.state = controller.state.copyWith(activeMessages: [msg1, msg2]);

      when(mockRepository.deleteMessage('m1')).thenAnswer((_) async => true);

      final result = await controller.deleteMessage('m1');

      expect(result, isTrue);
      // Verify msg2 remains unaffected! No Truncate Table or entire wipe occurred!
      expect(controller.state.activeMessages.length, equals(1));
      expect(controller.state.activeMessages.first.messageId, equals('m2'));
    });
  });
}
