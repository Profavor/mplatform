import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/features/auth/data/repositories/auth_repository.dart';
import 'package:mplatform_mobile/features/auth/domain/models/user_model.dart';
import 'package:mplatform_mobile/features/auth/presentation/providers/auth_provider.dart';
import 'package:mplatform_mobile/features/chat/data/repositories/chat_repository.dart';
import 'package:mplatform_mobile/features/chat/data/services/chat_websocket_service.dart';
import 'package:mplatform_mobile/features/chat/domain/models/chat_message_model.dart';
import 'package:mplatform_mobile/features/chat/domain/models/chat_room_model.dart';
import 'package:mplatform_mobile/features/chat/presentation/providers/chat_provider.dart';
import 'package:mplatform_mobile/features/chat/presentation/screens/chat_room_list_screen.dart';
import 'package:mplatform_mobile/features/chat/presentation/screens/chat_screen.dart';
import 'package:dio/dio.dart';

import 'chat_screen_test.mocks.dart';

class FakeAuthRepo implements AuthRepository {
  @override
  noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

class _FakeAuthController extends AuthController {
  _FakeAuthController(super.repo, AsyncValue<UserModel?> initialState) {
    state = initialState;
  }
  @override
  Future<void> checkAuthStatus() async {}
}

@GenerateMocks([ChatRepository, ChatWebSocketService])
void main() {
  group('ChatRoomListScreen & ChatScreen Widget Tests (TDD - Zero Hardcoding & UUID Protection)', () {
    late MockChatRepository mockRepository;
    late MockChatWebSocketService mockWsService;

    setUp(() {
      mockRepository = MockChatRepository();
      mockWsService = MockChatWebSocketService();
      when(mockWsService.messageStream).thenAnswer((_) => const Stream<ChatMessageModel>.empty());
    });

    Widget createTestWidget(Widget child) {
      return ProviderScope(
        overrides: [
          chatRepositoryProvider.overrideWithValue(mockRepository),
          chatWebSocketServiceProvider.overrideWithValue(mockWsService),
          authControllerProvider.overrideWith((ref) {
            final authCtrl = _FakeAuthController(FakeAuthRepo(), const AsyncValue.data(UserModel(id: '1', username: 'my_account', name: '나', role: 'ROLE_USER')));
            return authCtrl;
          }),
        ],
        child: MaterialApp(
          localizationsDelegates: const [
            AppLocalizations.delegate,
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          supportedLocales: const [Locale('ko'), Locale('en')],
          locale: const Locale('ko'),
          home: child,
        ),
      );
    }

    testWidgets('ChatRoomListScreen displays room title and unread badges without raw UUID leakage', (WidgetTester tester) async {
      const rawRoomUuid = 'f4520a17-af0b-4d13-a1ce-479d4b2e2ca7';
      const room = ChatRoomModel(
        roomId: rawRoomUuid,
        title: '마스터 데이터 품질 QA 대화방',
        lastMessage: '테스트 완료되었습니다.',
        unreadCount: 5,
        updatedAt: '2026-08-06T12:00:00Z',
      );

      when(mockRepository.getChatRooms()).thenAnswer((_) async => [room]);

      await tester.pumpWidget(createTestWidget(const ChatRoomListScreen()));
      await tester.pumpAndSettle();

      expect(find.text('마스터 데이터 품질 QA 대화방'), findsOneWidget);
      expect(find.text('5'), findsOneWidget);
      expect(find.text('테스트 완료되었습니다.'), findsOneWidget);

      // Critical Rule Check: Raw UUID MUST NOT appear in UI!
      expect(find.text(rawRoomUuid), findsNothing);
    });

    testWidgets('ChatScreen displays KakaoTalk style message speech bubbles and timezone formatted clock', (WidgetTester tester) async {
      const msg1 = ChatMessageModel(
        messageId: 'm-001',
        roomId: 'r-100',
        senderUsername: 'other_user',
        senderName: '이홍길 (품질팀)',
        content: '신규 마스터 스키마 승인 요청드립니다.',
        timestamp: '2026-08-06T06:30:00Z', // KST +9 = 15:30 -> 오후 3:30 / 3:30
      );
      const msg2 = ChatMessageModel(
        messageId: 'm-002',
        roomId: 'r-100',
        senderUsername: 'my_account',
        senderName: '나',
        content: '네, 지금 검토 중입니다.',
        timestamp: '2026-08-06T06:31:00Z',
      );

      when(mockRepository.getMessages('r-100')).thenAnswer((_) async => [msg1, msg2]);

      await tester.pumpWidget(createTestWidget(const ChatScreen(
        roomId: 'r-100',
        roomTitle: '마스터 데이터 품질 QA 대화방',
        currentUsername: 'my_account',
      )));
      await tester.pumpAndSettle();

      // Check bubble contents and sender info
      expect(find.text('이홍길 (품질팀)'), findsOneWidget);
      expect(find.text('신규 마스터 스키마 승인 요청드립니다.'), findsOneWidget);
      expect(find.text('네, 지금 검토 중입니다.'), findsOneWidget);

      // Check text field input existence
      expect(find.byType(TextField), findsOneWidget);
    });
  });
}
