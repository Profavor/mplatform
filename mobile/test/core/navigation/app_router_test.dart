import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/core/navigation/app_router.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/features/approvals/data/repositories/approvals_repository.dart';
import 'package:mplatform_mobile/features/approvals/presentation/providers/approvals_provider.dart';
import 'package:mplatform_mobile/features/auth/data/repositories/auth_repository.dart';
import 'package:mplatform_mobile/features/auth/domain/models/user_model.dart';
import 'package:mplatform_mobile/features/auth/presentation/providers/auth_provider.dart';
import 'package:mplatform_mobile/features/auth/presentation/screens/login_screen.dart';
import 'package:mplatform_mobile/features/chat/data/repositories/chat_repository.dart';
import 'package:mplatform_mobile/features/chat/data/services/chat_websocket_service.dart';
import 'package:mplatform_mobile/features/chat/presentation/providers/chat_provider.dart';
import 'package:mplatform_mobile/features/chat/domain/models/chat_message_model.dart';
import 'package:dio/dio.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:mplatform_mobile/features/navigation/presentation/screens/main_navigation_screen.dart';
import 'package:mplatform_mobile/features/notifications/data/repositories/notifications_repository.dart';
import 'package:mplatform_mobile/features/notifications/domain/models/notification_item.dart';
import 'package:mplatform_mobile/features/notifications/presentation/providers/notifications_provider.dart';
import 'package:mplatform_mobile/features/records/data/repositories/records_repository.dart';
import 'package:mplatform_mobile/features/records/presentation/providers/records_provider.dart';
import 'package:mplatform_mobile/features/dashboard/data/repositories/dashboard_repository.dart';
import 'package:mplatform_mobile/features/dashboard/presentation/providers/dashboard_provider.dart';
import 'package:mplatform_mobile/features/dashboard/data/models/dashboard_stats_model.dart';
import 'package:mplatform_mobile/features/dashboard/data/models/dq_trend_item_model.dart';
import 'package:mplatform_mobile/features/dashboard/data/models/dq_severity_item_model.dart';
import 'package:mplatform_mobile/features/inbox/data/repositories/inbox_repository.dart';
import 'package:mplatform_mobile/features/inbox/domain/models/inbox_folder_count_model.dart';
import 'package:mplatform_mobile/features/inbox/domain/models/inbox_message_model.dart';
import 'package:mplatform_mobile/features/inbox/presentation/providers/inbox_provider.dart';

import 'app_router_test.mocks.dart';

class _FakeAuthController extends AuthController {
  _FakeAuthController(super.repo, AsyncValue<UserModel?> initialState) {
    state = initialState;
  }
  @override
  Future<void> checkAuthStatus() async {
    // No-op for testing to preserve injected state
  }
}

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

@GenerateMocks([AuthRepository, RecordsRepository, ApprovalsRepository, ChatRepository, ChatWebSocketService])
void main() {
  group('AppRouter Routing Tests (TDD - Unauthenticated Access Protection)', () {
    late MockAuthRepository mockAuthRepo;
    late MockRecordsRepository mockRecordsRepo;
    late MockApprovalsRepository mockApprovalsRepo;
    late MockChatRepository mockChatRepo;
    late MockChatWebSocketService mockChatWs;
    late SharedPreferences prefs;

    setUp(() async {
      SharedPreferences.setMockInitialValues({});
      prefs = await SharedPreferences.getInstance();

      mockAuthRepo = MockAuthRepository();
      mockRecordsRepo = MockRecordsRepository();
      mockApprovalsRepo = MockApprovalsRepository();
      mockChatRepo = MockChatRepository();
      mockChatWs = MockChatWebSocketService();

      when(mockRecordsRepo.getDomains()).thenAnswer((_) async => []);
      when(mockApprovalsRepo.getPendingApprovals()).thenAnswer((_) async => []);
      when(mockApprovalsRepo.getMySubmittedApprovals()).thenAnswer((_) async => []);
      when(mockChatRepo.getChatRooms()).thenAnswer((_) async => []);
      when(mockChatWs.messageStream).thenAnswer((_) => const Stream<ChatMessageModel>.empty());
      when(mockChatWs.notificationStream).thenAnswer((_) => const Stream<Map<String, dynamic>>.empty());
      when(mockChatWs.roomReadStream).thenAnswer((_) => const Stream<String>.empty());
      when(mockChatWs.presenceStream).thenAnswer((_) => const Stream<Map<String, dynamic>>.empty());
    });

    testWidgets('unauthenticated user (AsyncValue.data(null)) routes to LoginScreen instead of MainNavigationScreen', (tester) async {
      final authCtrl = _FakeAuthController(mockAuthRepo, const AsyncValue.data(null));

      await tester.pumpWidget(ProviderScope(
        overrides: [
          sharedPreferencesProvider.overrideWithValue(prefs),
          authRepositoryProvider.overrideWithValue(mockAuthRepo),
          authControllerProvider.overrideWith((ref) => authCtrl),
          recordsRepositoryProvider.overrideWithValue(mockRecordsRepo),
          approvalsRepositoryProvider.overrideWithValue(mockApprovalsRepo),
          chatRepositoryProvider.overrideWithValue(mockChatRepo),
          chatWebSocketServiceProvider.overrideWithValue(mockChatWs),
          notificationsRepositoryProvider.overrideWithValue(FakeNotificationsRepository()),
          dashboardRepositoryProvider.overrideWithValue(FakeDashboardRepository()),
          inboxRepositoryProvider.overrideWithValue(FakeInboxRepository()),
        ],
        child: Consumer(builder: (context, ref, _) {
          final router = ref.watch(appRouterProvider);
          return MaterialApp.router(
            routerConfig: router,
            localizationsDelegates: const [
              AppLocalizations.delegate,
              GlobalMaterialLocalizations.delegate,
              GlobalWidgetsLocalizations.delegate,
              GlobalCupertinoLocalizations.delegate,
            ],
            supportedLocales: const [Locale('ko')],
          );
        }),
      ));
      await tester.pumpAndSettle();

      expect(find.byType(LoginScreen), findsOneWidget);
      expect(find.byType(MainNavigationScreen), findsNothing);
    });

    testWidgets('authenticated user routes directly to MainNavigationScreen', (tester) async {
      const dummyUser = UserModel(id: '1', username: 'tester', name: '테스터', role: 'ROLE_USER');
      final authCtrl = _FakeAuthController(mockAuthRepo, const AsyncValue.data(dummyUser));

      await tester.pumpWidget(ProviderScope(
        overrides: [
          sharedPreferencesProvider.overrideWithValue(prefs),
          authRepositoryProvider.overrideWithValue(mockAuthRepo),
          authControllerProvider.overrideWith((ref) => authCtrl),
          recordsRepositoryProvider.overrideWithValue(mockRecordsRepo),
          approvalsRepositoryProvider.overrideWithValue(mockApprovalsRepo),
          chatRepositoryProvider.overrideWithValue(mockChatRepo),
          chatWebSocketServiceProvider.overrideWithValue(mockChatWs),
          notificationsRepositoryProvider.overrideWithValue(FakeNotificationsRepository()),
          dashboardRepositoryProvider.overrideWithValue(FakeDashboardRepository()),
          inboxRepositoryProvider.overrideWithValue(FakeInboxRepository()),
        ],
        child: Consumer(builder: (context, ref, _) {
          final router = ref.watch(appRouterProvider);
          return MaterialApp.router(
            routerConfig: router,
            localizationsDelegates: const [
              AppLocalizations.delegate,
              GlobalMaterialLocalizations.delegate,
              GlobalWidgetsLocalizations.delegate,
              GlobalCupertinoLocalizations.delegate,
            ],
            supportedLocales: const [Locale('ko')],
          );
        }),
      ));
      await tester.pumpAndSettle();

      expect(find.byType(MainNavigationScreen), findsOneWidget);
      expect(find.byType(LoginScreen), findsNothing);
    });
  });
}
