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
import 'package:mplatform_mobile/features/chat/domain/models/chat_room_model.dart';
import 'package:mplatform_mobile/features/chat/presentation/providers/chat_provider.dart';
import 'package:mplatform_mobile/features/notifications/data/repositories/notifications_repository.dart';
import 'package:mplatform_mobile/features/notifications/domain/models/notification_item.dart';
import 'package:mplatform_mobile/features/notifications/presentation/providers/notifications_provider.dart';

class FakeApprovalsRepo extends ApprovalsRepository {
  FakeApprovalsRepo() : super(Dio());
  @override
  Future<List<ApprovalItem>> getPendingApprovals() async => [];
}
class FakeChatRepo extends ChatRepository {
  FakeChatRepo() : super(Dio());
  @override
  Future<List<ChatRoomModel>> getChatRooms() async => [];
}
class FakeNotifRepo extends NotificationsRepository {
  FakeNotifRepo() : super(Dio());
  @override
  Future<List<NotificationItem>> getNotifications({int page = 0, int size = 20}) async => [
    const NotificationItem(id: 'n1', title: 'Test Activity', content: 'Test Content', targetId: '340a0917-af0b-4d13-a1ce-479d4b2e2ca7', targetType: 'APPROVAL', createdAt: '2026-08-06T12:00:00Z', isRead: false),
  ];
}
class FakeChatWs extends ChatWebSocketService {}

void main() {
  group('HomeDashboardScreen Widget Tests (TDD - No Hardcoding, UUID Masking, Timezone Safety)', () {
    Widget createTestWidget() {
      return ProviderScope(
        overrides: [
          approvalsRepositoryProvider.overrideWithValue(FakeApprovalsRepo()),
          chatRepositoryProvider.overrideWithValue(FakeChatRepo()),
          chatWebSocketServiceProvider.overrideWithValue(FakeChatWs()),
          notificationsRepositoryProvider.overrideWithValue(FakeNotifRepo()),
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
      expect(find.text('거버넌스 포털 대시보드'), findsWidgets);
      expect(find.text('나의 처리 대기 현황'), findsOneWidget);
      expect(find.text('최근 변경 및 승인 활동'), findsOneWidget);
      expect(find.text('미안독 채팅 메시지'), findsOneWidget);

      // 2. Verify no raw UUIDs appear anywhere
      const rawUuid = '340a0917-af0b-4d13-a1ce-479d4b2e2ca7';
      expect(find.text(rawUuid), findsNothing);
      // Formatted reference code should be rendered
      expect(find.textContaining('REF-'), findsWidgets);
    });
  });
}
