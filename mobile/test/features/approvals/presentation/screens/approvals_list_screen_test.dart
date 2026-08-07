import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/features/approvals/data/repositories/approvals_repository.dart';
import 'package:mplatform_mobile/features/approvals/domain/models/approval_item.dart';
import 'package:mplatform_mobile/features/approvals/presentation/providers/approvals_provider.dart';
import 'package:mplatform_mobile/features/approvals/presentation/screens/approvals_list_screen.dart';

import 'approvals_list_screen_test.mocks.dart';

@GenerateMocks([ApprovalsRepository])
void main() {
  group('ApprovalsListScreen Widget Tests (TDD - Zero Hardcoding & UUID Protection)', () {
    late MockApprovalsRepository mockRepository;

    setUp(() {
      mockRepository = MockApprovalsRepository();
    });

    Widget createTestWidget() {
      return ProviderScope(
        overrides: [
          approvalsRepositoryProvider.overrideWithValue(mockRepository),
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
          home: ApprovalsListScreen(),
        ),
      );
    }

    testWidgets('renders localized tabs, formats raw UUIDs into short identification codes, and shows action buttons', (WidgetTester tester) async {
      const rawUuid = '8911b324-af0b-4d13-a1ce-479d4b2e2ca7';
      const rawTargetId = '340a0917-af0b-4d13-a1ce-479d4b2e2ca7';

      const pendingItem = ApprovalItem(
        approvalId: rawUuid,
        targetType: 'RECORD_CREATE',
        targetId: rawTargetId,
        requester: 'kim_developer',
        status: 'PENDING',
        requestDate: '2026-08-06T10:00:00Z',
      );

      when(mockRepository.getPendingApprovals()).thenAnswer((_) async => [pendingItem]);
      when(mockRepository.getMySubmittedApprovals()).thenAnswer((_) async => []);

      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      // 1. Check localization titles (Zero Hardcoding)
      expect(find.text('결재 & 승인 관리'), findsOneWidget);
      expect(find.text('결재 대기 중'), findsOneWidget);
      expect(find.text('내가 상신한 결재 내역'), findsOneWidget);

      // 2. Critical Rule Check: Raw UUIDs MUST NOT appear in UI!
      expect(find.text(rawUuid), findsNothing);
      expect(find.text(rawTargetId), findsNothing);

      // Formatted short codes MUST exist!
      expect(find.text('APP-8911b324'), findsOneWidget);
      expect(find.textContaining('TGT-340a0917'), findsOneWidget);
      expect(find.text('kim_developer'), findsOneWidget);

      // 3. Action buttons (승인/반려) must be visible on pending cards
      expect(find.text('승인'), findsWidgets);
      expect(find.text('반려'), findsWidgets);
    });
  });
}
