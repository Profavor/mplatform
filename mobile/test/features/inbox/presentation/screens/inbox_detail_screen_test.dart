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
import 'package:mplatform_mobile/features/inbox/data/repositories/inbox_repository.dart';
import 'package:mplatform_mobile/features/inbox/domain/models/inbox_attachment_model.dart';
import 'package:mplatform_mobile/features/inbox/domain/models/inbox_message_model.dart';
import 'package:mplatform_mobile/features/inbox/domain/models/inbox_recipient_model.dart';
import 'package:mplatform_mobile/features/inbox/presentation/screens/inbox_detail_screen.dart';

import 'inbox_detail_screen_test.mocks.dart';

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

@GenerateMocks([InboxRepository])
void main() {
  group('InboxDetailScreen Widget Tests (TDD - Zero Hardcoding & UUID Formatting)', () {
    late MockInboxRepository mockRepository;

    final sampleMessage = InboxMessageModel(
      id: '340a0917-af0b-4d13-a1ce-479d4b2e2ca7',
      senderId: 'user01',
      senderName: '이홍길',
      subject: '상세 결재 연계 메모',
      body: '상세 본문 내용입니다. 결재 요청건을 확인해주세요.',
      importance: 'HIGH',
      isRead: true,
      isStarred: false,
      folder: 'INBOX',
      createdAt: '2026-08-20T10:00:00Z',
      toRecipients: const [
        InboxRecipientModel(userId: 'user02', name: '김승인', recipientType: 'TO'),
      ],
      attachments: const [
        InboxAttachmentModel(id: 'att-1111', fileName: 'specification.pdf', fileSize: 204800),
      ],
    );

    setUp(() {
      mockRepository = MockInboxRepository();
      when(mockRepository.getMessage('340a0917-af0b-4d13-a1ce-479d4b2e2ca7'))
          .thenAnswer((_) async => sampleMessage);
    });

    Widget createWidgetUnderTest() {
      final fakeRepo = FakeAuthRepo();
      return ProviderScope(
        overrides: [
          inboxRepositoryProvider.overrideWithValue(mockRepository),
          authControllerProvider.overrideWith((ref) => _FakeAuthController(
                fakeRepo,
                const AsyncValue.data(UserModel(
                  id: 'current-user-id',
                  username: 'kim_admin',
                  role: 'ROLE_ADMIN',
                )),
              )),
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
          home: InboxDetailScreen(messageId: '340a0917-af0b-4d13-a1ce-479d4b2e2ca7'),
        ),
      );
    }

    testWidgets('renders message subject, body, sender, formatted UUID, and attachment', (tester) async {
      await tester.pumpWidget(createWidgetUnderTest());
      await tester.pumpAndSettle();

      // Subject & Body
      expect(find.text('상세 결재 연계 메모'), findsOneWidget);
      expect(find.textContaining('상세 본문 내용입니다'), findsWidgets);

      // Formatted UUID in AppBar
      expect(find.text('INB-340a0917'), findsOneWidget);
      expect(find.text('340a0917-af0b-4d13-a1ce-479d4b2e2ca7'), findsNothing);

      // Recipient & Attachment
      expect(find.text('김승인'), findsOneWidget);
      expect(find.text('specification.pdf'), findsOneWidget);
    });
  });
}
