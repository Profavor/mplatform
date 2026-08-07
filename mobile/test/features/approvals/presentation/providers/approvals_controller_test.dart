import 'package:flutter_test/flutter_test.dart';
import 'package:mockito/annotations.dart';
import 'package:mockito/mockito.dart';
import 'package:mplatform_mobile/features/approvals/data/repositories/approvals_repository.dart';
import 'package:mplatform_mobile/features/approvals/domain/models/approval_item.dart';
import 'package:mplatform_mobile/features/approvals/presentation/providers/approvals_provider.dart';

import 'approvals_controller_test.mocks.dart';

@GenerateMocks([ApprovalsRepository])
void main() {
  group('ApprovalsController Tests (TDD - State Transitions)', () {
    late MockApprovalsRepository mockRepository;
    late ApprovalsController controller;

    setUp(() {
      mockRepository = MockApprovalsRepository();
      controller = ApprovalsController(mockRepository);
    });

    test('loadApprovals populates pending and submitted workflow lists', () async {
      const pendingItem = ApprovalItem(approvalId: 'app-1', targetType: 'RECORD', targetId: 'rec-1', requester: 'userA', status: 'PENDING');
      const submittedItem = ApprovalItem(approvalId: 'app-2', targetType: 'RECORD_UPDATE', targetId: 'rec-2', requester: 'me', status: 'APPROVED');

      when(mockRepository.getPendingApprovals()).thenAnswer((_) async => [pendingItem]);
      when(mockRepository.getMySubmittedApprovals()).thenAnswer((_) async => [submittedItem]);

      await controller.loadApprovals();

      expect(controller.state.pendingItems.length, equals(1));
      expect(controller.state.submittedItems.length, equals(1));
      expect(controller.state.isLoading, isFalse);
    });

    test('approve succeeds and triggers reload of approval lists', () async {
      when(mockRepository.approveRequest('app-1', comment: 'Looks good')).thenAnswer((_) async => true);
      when(mockRepository.getPendingApprovals()).thenAnswer((_) async => []);
      when(mockRepository.getMySubmittedApprovals()).thenAnswer((_) async => []);

      final success = await controller.approve('app-1', comment: 'Looks good');

      expect(success, isTrue);
      verify(mockRepository.approveRequest('app-1', comment: 'Looks good')).called(1);
      verify(mockRepository.getPendingApprovals()).called(1);
    });
  });
}
