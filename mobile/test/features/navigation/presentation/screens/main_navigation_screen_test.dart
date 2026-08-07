import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/features/approvals/data/repositories/approvals_repository.dart';
import 'package:mplatform_mobile/features/approvals/presentation/providers/approvals_provider.dart';
import 'package:mplatform_mobile/features/auth/data/repositories/auth_repository.dart';
import 'package:mplatform_mobile/features/auth/domain/models/user_model.dart';
import 'package:mplatform_mobile/features/auth/presentation/providers/auth_provider.dart';
import 'package:mplatform_mobile/features/chat/data/repositories/chat_repository.dart';
import 'package:mplatform_mobile/features/chat/data/services/chat_websocket_service.dart';
import 'package:mplatform_mobile/features/chat/presentation/providers/chat_provider.dart';
import 'package:mplatform_mobile/features/navigation/presentation/screens/main_navigation_screen.dart';
import 'package:mplatform_mobile/features/notifications/data/repositories/notifications_repository.dart';
import 'package:mplatform_mobile/features/notifications/domain/models/notification_item.dart';
import 'package:mplatform_mobile/features/notifications/presentation/providers/notifications_provider.dart';
import 'package:mplatform_mobile/features/records/data/repositories/records_repository.dart';
import 'package:mplatform_mobile/features/records/presentation/providers/records_provider.dart';

import 'main_navigation_screen_test.mocks.dart';

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
class FakeNotificationsRepository extends NotificationsRepository {
  FakeNotificationsRepository() : super(Dio());
  @override
  Future<List<NotificationItem>> getNotifications({int page = 0, int size = 20}) async => [];
}

@GenerateMocks([RecordsRepository, ApprovalsRepository, ChatRepository, ChatWebSocketService])
void main() {
  group('MainNavigationScreen Shell Widget Tests (TDD - Official 5-Tab Architecture)', () {
    late MockRecordsRepository mockRecordsRepo;
    late MockApprovalsRepository mockApprovalsRepo;
    late MockChatRepository mockChatRepo;
    late MockChatWebSocketService mockChatWs;

    setUp(() {
      mockRecordsRepo = MockRecordsRepository();
      mockApprovalsRepo = MockApprovalsRepository();
      mockChatRepo = MockChatRepository();
      mockChatWs = MockChatWebSocketService();
      
      when(mockChatWs.messageStream).thenAnswer((_) => const Stream.empty());
      when(mockRecordsRepo.getDomains()).thenAnswer((_) async => []);
      when(mockApprovalsRepo.getPendingApprovals()).thenAnswer((_) async => []);
      when(mockApprovalsRepo.getMySubmittedApprovals()).thenAnswer((_) async => []);
      when(mockChatRepo.getChatRooms()).thenAnswer((_) async => []);
    });

    Widget createTestWidget() {
      return ProviderScope(
        overrides: [
          recordsRepositoryProvider.overrideWithValue(mockRecordsRepo),
          approvalsRepositoryProvider.overrideWithValue(mockApprovalsRepo),
          chatRepositoryProvider.overrideWithValue(mockChatRepo),
          chatWebSocketServiceProvider.overrideWithValue(mockChatWs),
          notificationsRepositoryProvider.overrideWithValue(FakeNotificationsRepository()),
          authControllerProvider.overrideWith((ref) => _FakeAuthController(FakeAuthRepo(), const AsyncValue.data(UserModel(id: '1', username: 'my_account', name: '나', role: 'ROLE_USER')))),
        ],
        child: const MaterialApp(
          localizationsDelegates: [
            AppLocalizations.delegate,
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          supportedLocales: [Locale('ko'), Locale('en')],
          locale: Locale('ko'),
          home: MainNavigationScreen(),
        ),
      );
    }

    testWidgets('renders bottom navigation bar with 5 official localized tabs and switches cleanly between them', (WidgetTester tester) async {
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      // 1. Verify 5 official localized tabs exist [레코드, 홈, 승인, 알림, 채팅] without hardcoding in UI
      expect(find.byType(BottomNavigationBar), findsOneWidget);
      expect(find.text('레코드'), findsWidgets);
      expect(find.text('홈'), findsWidgets);
      expect(find.text('승인'), findsWidgets);
      expect(find.text('알림'), findsWidgets);
      expect(find.text('채팅'), findsWidgets);

      // 2. Tap Home Tab
      await tester.tap(find.descendant(of: find.byType(BottomNavigationBar), matching: find.text('홈')));
      await tester.pumpAndSettle();
      expect(find.text('거버넌스 포털 대시보드'), findsWidgets);

      // 3. Tap Notifications Tab
      await tester.tap(find.descendant(of: find.byType(BottomNavigationBar), matching: find.text('알림')));
      await tester.pumpAndSettle();
      expect(find.text('시스템 알림 센터'), findsWidgets);
    });
  });
}
