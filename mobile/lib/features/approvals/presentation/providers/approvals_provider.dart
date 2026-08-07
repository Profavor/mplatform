import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/features/approvals/data/repositories/approvals_repository.dart';
import 'package:mplatform_mobile/features/approvals/presentation/providers/approvals_state.dart';

final approvalsRepositoryProvider = Provider<ApprovalsRepository>((ref) {
  final dio = ref.watch(dioProvider);
  return ApprovalsRepository(dio);
});

class ApprovalsController extends StateNotifier<ApprovalsState> {
  final ApprovalsRepository _repository;

  ApprovalsController(this._repository) : super(const ApprovalsState());

  Future<void> loadApprovals() async {
    state = state.copyWith(isLoading: true, errorMessage: null);
    try {
      final pending = await _repository.getPendingApprovals();
      final submitted = await _repository.getMySubmittedApprovals();
      state = state.copyWith(
        pendingItems: pending,
        submittedItems: submitted,
        isLoading: false,
      );
    } catch (e) {
      state = state.copyWith(isLoading: false, errorMessage: e.toString());
    }
  }

  Future<bool> approve(String approvalId, {String? comment}) async {
    state = state.copyWith(isLoading: true, errorMessage: null);
    try {
      final success = await _repository.approveRequest(approvalId, comment: comment);
      if (success) {
        await loadApprovals();
        return true;
      }
      state = state.copyWith(isLoading: false, errorMessage: 'Approval failed');
      return false;
    } catch (e) {
      state = state.copyWith(isLoading: false, errorMessage: e.toString());
      return false;
    }
  }

  Future<bool> reject(String approvalId, {String? reason}) async {
    state = state.copyWith(isLoading: true, errorMessage: null);
    try {
      final success = await _repository.rejectRequest(approvalId, reason: reason);
      if (success) {
        await loadApprovals();
        return true;
      }
      state = state.copyWith(isLoading: false, errorMessage: 'Rejection failed');
      return false;
    } catch (e) {
      state = state.copyWith(isLoading: false, errorMessage: e.toString());
      return false;
    }
  }
}

final approvalsControllerProvider = StateNotifierProvider<ApprovalsController, ApprovalsState>((ref) {
  final repo = ref.watch(approvalsRepositoryProvider);
  return ApprovalsController(repo);
});
