import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/features/inbox/data/repositories/inbox_repository.dart';
import 'package:mplatform_mobile/features/inbox/domain/models/inbox_folder_count_model.dart';
import 'package:mplatform_mobile/features/inbox/domain/models/inbox_message_model.dart';
import 'package:mplatform_mobile/features/inbox/presentation/screens/inbox_screen.dart';

import 'inbox_screen_test.mocks.dart';

@GenerateMocks([InboxRepository])
void main() {
  group('InboxScreen Widget Tests (TDD - Zero Hardcoding & UUID Protection)', () {
    late MockInboxRepository mockRepository;

    final sampleMessage = InboxMessageModel(
      id: '340a0917-af0b-4d13-a1ce-479d4b2e2ca7',
      senderId: 'user01',
      senderName: '이홍길',
      subject: '신규 마스터 스키마 결재 통지',
      body: '승인 요청 건이 정상 처리되었습니다.',
      importance: 'HIGH',
      isRead: false,
      isStarred: true,
      folder: 'INBOX',
      createdAt: '2026-08-20T10:00:00Z',
    );

    setUp(() {
      mockRepository = MockInboxRepository();
      when(mockRepository.getMessages(
        folder: anyNamed('folder'),
        page: anyNamed('page'),
        size: anyNamed('size'),
        keyword: anyNamed('keyword'),
      )).thenAnswer((_) async => [sampleMessage]);

      when(mockRepository.getFolderCounts()).thenAnswer((_) async => [
            InboxFolderCountModel(folder: 'INBOX', total: 1, unread: 1),
            InboxFolderCountModel(folder: 'SENT', total: 0, unread: 0),
          ]);

      when(mockRepository.getUnreadCount()).thenAnswer((_) async => 1);
    });

    Widget createWidgetUnderTest() {
      return ProviderScope(
        overrides: [
          inboxRepositoryProvider.overrideWithValue(mockRepository),
        ],
        child: const MaterialApp(
          localizationsDelegates: [
            AppLocalizations.delegate,
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          supportedLocales: AppLocalizations.supportedLocales,
          locale: Locale('ko'),
          home: InboxScreen(),
        ),
      );
    }

    testWidgets('renders folder selector and message card with formatted UUID and sender name', (tester) async {
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.pumpAndSettle();

      // Verify AppBar Title is 사내 편지함 (never 스키마 변경 이력)
      expect(find.text('사내 편지함'), findsOneWidget);
      expect(find.text('스키마 변경 이력'), findsNothing);

      // Verify Sender Name & Subject
      expect(find.text('이홍길'), findsOneWidget);
      expect(find.text('신규 마스터 스키마 결재 통지'), findsOneWidget);

      // Verify Raw UUID is NOT leaked, formatted UUID is shown
      expect(find.text('340a0917-af0b-4d13-a1ce-479d4b2e2ca7'), findsNothing);
      expect(find.text('INB-340a0917'), findsOneWidget);

      // Verify FloatingActionButton (새 메시지 작성)
      expect(find.byType(FloatingActionButton), findsOneWidget);
    });
  });
}
