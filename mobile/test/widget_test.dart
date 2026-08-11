import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/features/approvals/data/repositories/approvals_repository.dart';
import 'package:mplatform_mobile/features/approvals/presentation/providers/approvals_provider.dart';
import 'package:mplatform_mobile/features/auth/data/repositories/auth_repository.dart';
import 'package:mplatform_mobile/features/auth/presentation/providers/auth_provider.dart';
import 'package:mplatform_mobile/features/chat/data/repositories/chat_repository.dart';
import 'package:mplatform_mobile/features/chat/data/services/chat_websocket_service.dart';
import 'package:mplatform_mobile/features/chat/presentation/providers/chat_provider.dart';
import 'package:mplatform_mobile/features/records/data/repositories/records_repository.dart';
import 'package:mplatform_mobile/features/records/presentation/providers/records_provider.dart';
import 'package:mplatform_mobile/main.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'widget_test.mocks.dart';

@GenerateMocks([AuthRepository, RecordsRepository, ApprovalsRepository, ChatRepository, ChatWebSocketService])
void main() {
  testWidgets('App initialization test with MPlatformMobileApp and complete mock provider overrides without network side-effects', (WidgetTester tester) async {
    SharedPreferences.setMockInitialValues({});
    final prefs = await SharedPreferences.getInstance();
    
    final mockAuthRepo = MockAuthRepository();
    final mockRecordsRepo = MockRecordsRepository();
    final mockApprovalsRepo = MockApprovalsRepository();
    final mockChatRepo = MockChatRepository();
    final mockChatWs = MockChatWebSocketService();

    when(mockAuthRepo.getCurrentUser()).thenAnswer((_) async => null);
    when(mockRecordsRepo.getDomains()).thenAnswer((_) async => []);
    when(mockApprovalsRepo.getPendingApprovals()).thenAnswer((_) async => []);
    when(mockApprovalsRepo.getMySubmittedApprovals()).thenAnswer((_) async => []);
    when(mockChatRepo.getChatRooms()).thenAnswer((_) async => []);
    when(mockChatWs.messageStream).thenAnswer((_) => const Stream.empty());
    when(mockChatWs.notificationStream).thenAnswer((_) => const Stream.empty());
    when(mockChatWs.roomReadStream).thenAnswer((_) => const Stream.empty());

    await tester.pumpWidget(ProviderScope(
      overrides: [
        sharedPreferencesProvider.overrideWithValue(prefs),
        authRepositoryProvider.overrideWithValue(mockAuthRepo),
        authControllerProvider.overrideWith((ref) => AuthController(mockAuthRepo)),
        recordsRepositoryProvider.overrideWithValue(mockRecordsRepo),
        approvalsRepositoryProvider.overrideWithValue(mockApprovalsRepo),
        chatRepositoryProvider.overrideWithValue(mockChatRepo),
        chatWebSocketServiceProvider.overrideWithValue(mockChatWs),
      ],
      child: const MPlatformMobileApp(),
    ));
    await tester.pump();

    expect(find.byType(MPlatformMobileApp), findsOneWidget);
  });
}
