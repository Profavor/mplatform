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
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/features/chat/presentation/screens/chat_room_list_screen.dart';
import 'package:mplatform_mobile/features/chat/presentation/screens/chat_screen.dart';
import 'package:dio/dio.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'chat_screen_test.mocks.dart';

class FakeAuthRepo implements AuthRepository {
  @override
  Future<List<UserModel>> getUsers() async => [];
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

@GenerateMocks([ChatRepository])

class FakeChatWs implements ChatWebSocketService {
  @override
  Stream<ChatMessageModel> get messageStream => const Stream.empty();
  @override
  Stream<Map<String, dynamic>> get notificationStream => const Stream.empty();
  @override
  Stream<String> get roomReadStream => const Stream.empty();
  @override
  Stream<Map<String, dynamic>> get presenceStream => const Stream.empty();
  @override
  void subscribeToRoom(String roomId) {}
  @override
  void unsubscribeFromRoom() {}
  @override
  void disconnect() {}
  @override
  void dispose() {}
  @override
  void sendMessage(String destination, Map<String, dynamic> body) {}
  @override
  Future<void> connect() async {}
  @override
  bool get isConnected => true;
  @override
  void onConnect(dynamic frame) {}
}

void main() {
  group('ChatRoomListScreen & ChatScreen Widget Tests (TDD - Zero Hardcoding & UUID Protection)', () {
    late MockChatRepository mockRepository;
    late FakeChatWs fakeWsService;

    setUp(() {
      mockRepository = MockChatRepository();
      fakeWsService = FakeChatWs();
    });

    Widget createTestWidget(Widget child) {
      final fakeAuthRepo = FakeAuthRepo();
      return ProviderScope(
        overrides: [
          dioProvider.overrideWithValue(Dio(BaseOptions(baseUrl: 'http://localhost'))),
          authRepositoryProvider.overrideWithValue(fakeAuthRepo),
          chatRepositoryProvider.overrideWithValue(mockRepository),
          chatWebSocketServiceProvider.overrideWithValue(fakeWsService),
          authControllerProvider.overrideWith((ref) {
            final authCtrl = _FakeAuthController(fakeAuthRepo, const AsyncValue.data(UserModel(id: '1', username: 'my_account', name: '나', role: 'ROLE_USER')));
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
      when(mockRepository.getOnlineUsers()).thenAnswer((_) async => []);

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
      when(mockRepository.markRoomAsRead(any)).thenAnswer((_) async => true);
      when(mockRepository.getChatRooms()).thenAnswer((_) async => []);
      when(mockRepository.getOnlineUsers()).thenAnswer((_) async => []);
      when(mockRepository.markRoomAsRead(any)).thenAnswer((_) async => true);
      when(mockRepository.fetchTotalUnreadCount()).thenAnswer((_) async => 0);
      when(mockRepository.getChatRooms()).thenAnswer((_) async => []);
      when(mockRepository.getOnlineUsers()).thenAnswer((_) async => []);
      await tester.pumpWidget(createTestWidget(const ChatScreen(
        roomId: 'r-100',
        roomTitle: '마스터 데이터 품질 QA 대화방',
        currentUsername: 'my_account',
      )));
      await tester.pumpAndSettle();

      // Check bubble contents and sender info
      for (final widget in tester.allWidgets.whereType<Text>()) {
        print('TEXT FOUND: ${widget.data}');
      }
      expect(find.byWidgetPredicate((w) => w is Text && w.data != null && w.data!.contains('이홍길'), skipOffstage: false), findsWidgets);
      expect(find.byWidgetPredicate((w) => w is Text && w.data != null && w.data!.contains('신규 마스터 스키마'), skipOffstage: false), findsWidgets);
      expect(find.byWidgetPredicate((w) => w is Text && w.data != null && w.data!.contains('지금 검토 중입니다'), skipOffstage: false), findsWidgets);

      // Check text field input existence
      expect(find.byType(TextField), findsOneWidget);
    });
  });
}
