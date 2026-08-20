import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:mplatform_mobile/features/inbox/domain/models/inbox_folder_count_model.dart';
import 'package:mplatform_mobile/features/inbox/domain/models/inbox_message_model.dart';

part 'inbox_state.freezed.dart';

@freezed
class InboxState with _$InboxState {
  const factory InboxState({
    @Default('INBOX') String currentFolder,
    @Default([]) List<InboxMessageModel> messages,
    @Default([]) List<InboxFolderCountModel> folderCounts,
    @Default(0) int unreadTotal,
    @Default(false) bool isLoading,
    @Default(true) bool hasMore,
    @Default(0) int page,
    String? keyword,
    String? errorMessage,
  }) = _InboxState;
}
