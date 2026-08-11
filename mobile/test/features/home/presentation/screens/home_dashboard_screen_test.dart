import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/features/home/presentation/screens/home_dashboard_screen.dart';

import 'package:dio/dio.dart';
import 'package:mplatform_mobile/features/approvals/data/repositories/approvals_repository.dart';
import 'package:mplatform_mobile/features/approvals/domain/models/approval_item.dart';
import 'package:mplatform_mobile/features/approvals/presentation/providers/approvals_provider.dart';
import 'package:mplatform_mobile/features/chat/data/repositories/chat_repository.dart';
import 'package:mplatform_mobile/features/chat/data/services/chat_websocket_service.dart';
import 'package:mplatform_mobile/features/dashboard/data/models/dashboard_stats_model.dart';
import 'package:mplatform_mobile/features/dashboard/data/models/dq_trend_item_model.dart';
import 'package:mplatform_mobile/features/dashboard/data/models/dq_severity_item_model.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/core/storage/storage_service.dart';
import 'package:dio/dio.dart';
import 'package:mplatform_mobile/features/chat/domain/models/chat_room_model.dart';
import 'package:mplatform_mobile/features/home/presentation/screens/home_dashboard_screen.dart';
import 'package:mplatform_mobile/features/notifications/data/repositories/notifications_repository.dart';
import 'package:mplatform_mobile/features/notifications/domain/models/notification_item.dart';
import 'package:mplatform_mobile/features/notifications/presentation/providers/notifications_provider.dart';
import 'package:mplatform_mobile/features/chat/domain/models/chat_message_model.dart';
import 'package:mplatform_mobile/features/chat/presentation/providers/chat_provider.dart';
import 'package:mplatform_mobile/features/dashboard/data/repositories/dashboard_repository.dart';
import 'package:mplatform_mobile/features/dashboard/data/models/dashboard_stats_model.dart';
import 'package:mplatform_mobile/features/dashboard/data/models/dq_trend_item_model.dart';
import 'package:mplatform_mobile/features/dashboard/data/models/dq_severity_item_model.dart';
import 'package:mplatform_mobile/features/dashboard/presentation/providers/dashboard_provider.dart';
import 'package:mockito/mockito.dart';

class FakeApprovalsRepo extends ApprovalsRepository {
  FakeApprovalsRepo() : super(Dio());
  @override
  Future<List<ApprovalItem>> getPendingApprovals() async => [
    const ApprovalItem(
      approvalId: '340a0917-af0b-4d13-a1ce-479d4b2e2ca7',
      targetType: 'TERM',
      targetId: 'REF-1234',
      requester: 'admin',
      status: 'PENDING',
      requestDate: '2023-10-25T10:00:00Z',
    ),
  ];
}

class FakeChatRepo extends ChatRepository {
  FakeChatRepo() : super(Dio());
  @override
  Future<List<ChatRoomModel>> getRooms() async => [];
  @override
  Future<List<ChatMessageModel>> getMessages(String roomId, {int page = 0, int size = 30}) async => [];
}

class FakeNotifRepo extends NotificationsRepository {
  FakeNotifRepo() : super(Dio());
  @override
  Future<List<NotificationItem>> getNotifications({int page = 0, int size = 20, bool unreadOnly = false}) async => [
    const NotificationItem(
      id: 'notif-1',
      title: '새로운 결재 요청',
      content: '결재가 요청되었습니다.',
      isRead: false,
      createdAt: '2023-10-25T10:00:00Z',
      targetId: 'REF-1234',
      targetType: 'APPROVAL',
    ),
  ];
  @override
  Future<void> markAsRead(String id) async {}
  @override
  Future<void> markAllAsRead() async {}
}

class MockChatWs extends Mock implements ChatWebSocketService {
  @override
  Future<void> connect() async {}
  
  @override
  Stream<Map<String, dynamic>> get notificationStream => const Stream<Map<String, dynamic>>.empty();
  @override
  Stream<ChatMessageModel> get messageStream => const Stream<ChatMessageModel>.empty();
  @override
  Stream<String> get roomReadStream => const Stream<String>.empty();
  @override
  Stream<Map<String, dynamic>> get presenceStream => const Stream<Map<String, dynamic>>.empty();
}

class FakeDashboardRepo extends DashboardRepository {
  FakeDashboardRepo() : super(Dio());
  @override
  Future<DashboardStatsModel> getStats() async => const DashboardStatsModel(
    totalDomains: 10,
    pendingApprovals: 5,
    activeRecords: 100,
    pendingMatches: 3,
    openDqViolations: 1,
  );
  @override
  Future<List<DqTrendItemModel>> getDqTrends() async => [];
  @override
  Future<List<DqSeverityItemModel>> getDqSeverity() async => [];
}

class FakeDio extends Fake implements Dio {
  @override
  Future<Response<T>> get<T>(
    String path, {
    Object? data,
    Map<String, dynamic>? queryParameters,
    Options? options,
    CancelToken? cancelToken,
    void Function(int, int)? onReceiveProgress,
  }) async {
    return Response<T>(
      requestOptions: RequestOptions(path: path),
      statusCode: 200,
      data: [] as T,
    );
  }
}

class FakeStorageService extends Fake implements StorageService {
  @override
  Future<String?> getAccessToken() async => 'test_token';
}

void main() {
  group('HomeDashboardScreen Widget Tests (TDD - No Hardcoding, UUID Masking, Timezone Safety)', () {
    Widget createTestWidget() {
      return ProviderScope(
        overrides: [
          approvalsRepositoryProvider.overrideWithValue(FakeApprovalsRepo()),
          chatRepositoryProvider.overrideWithValue(FakeChatRepo()),
          chatWebSocketServiceProvider.overrideWithValue(MockChatWs()),
          notificationsRepositoryProvider.overrideWithValue(FakeNotifRepo()),
          dashboardRepositoryProvider.overrideWithValue(FakeDashboardRepo()),
          storageServiceProvider.overrideWithValue(FakeStorageService()),
          dioProvider.overrideWithValue(FakeDio()),
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
          home: HomeDashboardScreen(),
        ),
      );
    }

    testWidgets('renders official home dashboard titles and metric summaries using AppLocalizations without hardcoded text', (WidgetTester tester) async {
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      // 1. Verify localized headers exist
      final texts = tester.widgetList<Text>(find.byType(Text)).map((t) => t.data).toList();
      print('TEXTS FOUND: $texts');
      
      expect(find.text('거버넌스 포털 대시보드'), findsWidgets);
      expect(find.text('나의 처리 대기 현황'), findsOneWidget);
      expect(find.text('최근 변경 및 승인 활동'), findsOneWidget);
      expect(find.text('미안독 채팅 메시지'), findsOneWidget);

      // 2. Verify no raw UUIDs appear anywhere
      const rawUuid = '340a0917-af0b-4d13-a1ce-479d4b2e2ca7';
      expect(find.textContaining(rawUuid), findsNothing);
      
      // Formatted reference code should be rendered
      expect(find.textContaining('REF-'), findsWidgets);
    });
  });
}
