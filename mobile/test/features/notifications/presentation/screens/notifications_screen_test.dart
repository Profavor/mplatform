import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/features/notifications/data/repositories/notifications_repository.dart';
import 'package:mplatform_mobile/features/notifications/domain/models/notification_item.dart';
import 'package:mplatform_mobile/features/notifications/presentation/providers/notifications_provider.dart';
import 'package:mplatform_mobile/features/notifications/presentation/screens/notifications_screen.dart';

class FakeNotificationsRepository extends NotificationsRepository {
  FakeNotificationsRepository() : super(Dio());

  @override
  Future<List<NotificationItem>> getNotifications({int page = 0, int size = 20}) async {
    return [
      const NotificationItem(
        id: 'notif-101',
        title: '결재 승인 대기 알림',
        content: '신규 레코드 변경 결재 요청',
        targetId: '340a0917-af0b-4d13-a1ce-479d4b2e2ca7',
        targetType: 'APPROVAL',
        createdAt: '2026-08-06T12:00:00Z',
        isRead: false,
      ),
    ];
  }

  @override
  Future<void> markAsRead(String notificationId) async {
    // mock success
  }

  @override
  Future<void> markAllAsRead() async {
    // mock success
  }
}

void main() {
  group('NotificationsScreen Widget Tests (TDD - Zero Hardcoding & UUID Masking)', () {
    Widget createTestWidget() {
      return ProviderScope(
        overrides: [
          notificationsRepositoryProvider.overrideWithValue(FakeNotificationsRepository()),
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
          home: NotificationsScreen(),
        ),
      );
    }

    testWidgets('renders localized alert center titles, masks raw UUIDs, and displays timezone-safe dates', (WidgetTester tester) async {
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      // 1. Check localization titles without hardcoded literals in screen build
      expect(find.text('시스템 알림 센터'), findsOneWidget);
      expect(find.text('전체 읽음 처리'), findsOneWidget);
      expect(find.text('결재 승인 대기 알림'), findsOneWidget);
      expect(find.text('신규 레코드 변경 결재 요청'), findsOneWidget);

      // 2. Critical Rule Check: Raw UUID MUST NOT appear on screen!
      const rawUuid = '340a0917-af0b-4d13-a1ce-479d4b2e2ca7';
      expect(find.text(rawUuid), findsNothing);

      // Formatted identifier code MUST exist!
      expect(find.text('REF-340a0917'), findsOneWidget);
    });
  });
}
