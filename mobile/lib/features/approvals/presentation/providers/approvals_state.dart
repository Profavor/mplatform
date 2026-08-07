import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:mplatform_mobile/features/approvals/domain/models/approval_item.dart';

part 'approvals_state.freezed.dart';

@freezed
class ApprovalsState with _$ApprovalsState {
  const factory ApprovalsState({
    @Default([]) List<ApprovalItem> pendingItems,
    @Default([]) List<ApprovalItem> submittedItems,
    @Default(false) bool isLoading,
    String? errorMessage,
  }) = _ApprovalsState;
}
