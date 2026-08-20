import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/features/inbox/data/repositories/inbox_repository.dart';
import 'package:mplatform_mobile/features/inbox/domain/models/inbox_folder_count_model.dart';
import 'package:mplatform_mobile/features/inbox/domain/models/inbox_message_model.dart';
import 'package:mplatform_mobile/features/inbox/presentation/providers/inbox_provider.dart';

import 'inbox_controller_test.mocks.dart';

@GenerateMocks([InboxRepository])
void main() {
  group('InboxController Tests (TDD - State Management & Optimistic Updates)', () {
    late MockInboxRepository mockRepository;
    late InboxController controller;

    final sampleMessage = InboxMessageModel(
      id: 'inb-test-1111',
      senderId: 'user01',
      senderName: '홍길동',
      subject: '테스트 공지사항',
      body: '공지 내용입니다.',
      isRead: false,
      isStarred: false,
      folder: 'INBOX',
    );

    setUp(() {
      mockRepository = MockInboxRepository();
      controller = InboxController(mockRepository);
    });

    test('init loads messages and counts properly', () async {
      when(mockRepository.getMessages(folder: 'INBOX', page: 0, size: 20, keyword: null))
          .thenAnswer((_) async => [sampleMessage]);
      when(mockRepository.getFolderCounts())
          .thenAnswer((_) async => [InboxFolderCountModel(folder: 'INBOX', total: 1, unread: 1)]);
      when(mockRepository.getUnreadCount())
          .thenAnswer((_) async => 1);

      await controller.init();

      expect(controller.state.messages.length, equals(1));
      expect(controller.state.messages.first.id, equals('inb-test-1111'));
      expect(controller.state.folderCounts.length, equals(1));
      expect(controller.state.unreadTotal, equals(1));
      expect(controller.state.isLoading, isFalse);
    });

    test('selectFolder updates currentFolder and reloads list', () async {
      when(mockRepository.getMessages(folder: 'SENT', page: 0, size: 20, keyword: null))
          .thenAnswer((_) async => []);

      await controller.selectFolder('SENT');

      expect(controller.state.currentFolder, equals('SENT'));
      expect(controller.state.messages, isEmpty);
      verify(mockRepository.getMessages(folder: 'SENT', page: 0, size: 20, keyword: null)).called(1);
    });

    test('toggleStar optimistically updates isStarred in state', () async {
      when(mockRepository.getMessages(folder: 'INBOX', page: 0, size: 20, keyword: null))
          .thenAnswer((_) async => [sampleMessage]);
      when(mockRepository.getFolderCounts()).thenAnswer((_) async => []);
      when(mockRepository.getUnreadCount()).thenAnswer((_) async => 0);
      when(mockRepository.toggleStar('inb-test-1111')).thenAnswer((_) async {});

      await controller.init();
      expect(controller.state.messages.first.isStarred, isFalse);

      final success = await controller.toggleStar('inb-test-1111');

      expect(success, isTrue);
      expect(controller.state.messages.first.isStarred, isTrue);
      verify(mockRepository.toggleStar('inb-test-1111')).called(1);
    });

    test('deleteMessage removes item from state list', () async {
      when(mockRepository.getMessages(folder: 'INBOX', page: 0, size: 20, keyword: null))
          .thenAnswer((_) async => [sampleMessage]);
      when(mockRepository.getFolderCounts()).thenAnswer((_) async => []);
      when(mockRepository.getUnreadCount()).thenAnswer((_) async => 0);
      when(mockRepository.deleteMessage('inb-test-1111', permanent: false)).thenAnswer((_) async {});

      await controller.init();
      expect(controller.state.messages.length, equals(1));

      final deleted = await controller.deleteMessage('inb-test-1111');

      expect(deleted, isTrue);
      expect(controller.state.messages, isEmpty);
    });
  });
}
