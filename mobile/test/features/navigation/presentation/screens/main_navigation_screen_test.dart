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
import 'package:mplatform_mobile/features/chat/domain/models/chat_message_model.dart';
import 'package:mplatform_mobile/features/navigation/presentation/screens/main_navigation_screen.dart';
import 'package:mplatform_mobile/features/notifications/data/repositories/notifications_repository.dart';
import 'package:mplatform_mobile/features/notifications/domain/models/notification_item.dart';
import 'package:mplatform_mobile/features/notifications/presentation/providers/notifications_provider.dart';
import 'package:mplatform_mobile/features/records/data/repositories/records_repository.dart';
import 'package:mplatform_mobile/features/records/presentation/providers/records_provider.dart';
import 'package:mplatform_mobile/features/dashboard/data/repositories/dashboard_repository.dart';
import 'package:mplatform_mobile/features/dashboard/data/models/dashboard_stats_model.dart';
import 'package:mplatform_mobile/features/dashboard/data/models/dq_trend_item_model.dart';
import 'package:mplatform_mobile/features/dashboard/data/models/dq_severity_item_model.dart';

import 'package:mplatform_mobile/features/inbox/data/repositories/inbox_repository.dart';
import 'package:mplatform_mobile/features/inbox/domain/models/inbox_folder_count_model.dart';
import 'package:mplatform_mobile/features/inbox/domain/models/inbox_message_model.dart';

import 'main_navigation_screen_test.mocks.dart';

class FakeInboxRepository implements InboxRepository {
  @override
  Future<List<InboxMessageModel>> getMessages({String folder = 'INBOX', int page = 0, int size = 20, String? keyword}) async => [];
  @override
  Future<List<InboxFolderCountModel>> getFolderCounts() async => [];
  @override
  Future<int> getUnreadCount() async => 0;
  @override
  noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

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
class FakeDashboardRepository extends DashboardRepository {
  FakeDashboardRepository() : super(Dio());
  @override
  Future<DashboardStatsModel> getStats() async => const DashboardStatsModel(totalDomains: 0, activeRecords: 0, pendingApprovals: 0, openDqViolations: 0);
  @override
  Future<List<DqTrendItemModel>> getDqTrends() async => [];
  @override
  Future<List<DqSeverityItemModel>> getDqSeverity() async => [];
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
      
      when(mockChatWs.messageStream).thenAnswer((_) => const Stream<ChatMessageModel>.empty());
      when(mockChatWs.notificationStream).thenAnswer((_) => const Stream<Map<String, dynamic>>.empty());
      when(mockChatWs.roomReadStream).thenAnswer((_) => const Stream<String>.empty());
      when(mockChatWs.presenceStream).thenAnswer((_) => const Stream<Map<String, dynamic>>.empty());
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
          dashboardRepositoryProvider.overrideWithValue(FakeDashboardRepository()),
          inboxRepositoryProvider.overrideWithValue(FakeInboxRepository()),
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
