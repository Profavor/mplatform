import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/features/inbox/data/repositories/inbox_repository.dart';
import 'package:mplatform_mobile/features/inbox/domain/models/inbox_message_model.dart';
import 'package:mplatform_mobile/features/inbox/presentation/providers/inbox_state.dart';

class InboxController extends StateNotifier<InboxState> {
  final InboxRepository _repository;

  InboxController(this._repository) : super(const InboxState());

  Future<void> init() async {
    await Future.wait([
      loadMessages(refresh: true),
      refreshCounts(),
    ]);
  }

  Future<void> selectFolder(String folder) async {
    if (state.currentFolder == folder) return;
    state = state.copyWith(
      currentFolder: folder,
      page: 0,
      messages: [],
      hasMore: true,
      errorMessage: null,
    );
    await loadMessages(refresh: true);
  }

  Future<void> search(String keyword) async {
    state = state.copyWith(
      keyword: keyword.isEmpty ? null : keyword,
      page: 0,
      messages: [],
      hasMore: true,
      errorMessage: null,
    );
    await loadMessages(refresh: true);
  }

  Future<void> loadMessages({bool refresh = false}) async {
    if (state.isLoading) return;
    if (!refresh && !state.hasMore) return;

    final targetPage = refresh ? 0 : state.page + 1;
    state = state.copyWith(isLoading: true, errorMessage: null);

    try {
      final fetched = await _repository.getMessages(
        folder: state.currentFolder,
        page: targetPage,
        size: 20,
        keyword: state.keyword,
      );

      final updatedList = refresh ? fetched : [...state.messages, ...fetched];
      state = state.copyWith(
        messages: updatedList,
        page: targetPage,
        hasMore: fetched.length >= 20,
        isLoading: false,
      );
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        errorMessage: e.toString(),
      );
    }
  }

  Future<void> refreshCounts() async {
    try {
      final counts = await _repository.getFolderCounts();
      final unread = await _repository.getUnreadCount();
      state = state.copyWith(
        folderCounts: counts,
        unreadTotal: unread,
      );
    } catch (_) {}
  }

  Future<bool> toggleStar(String messageId) async {
    final originalMessages = state.messages;
    final targetIndex = originalMessages.indexWhere((m) => m.id == messageId);
    if (targetIndex == -1) return false;

    final target = originalMessages[targetIndex];
    final updatedTarget = target.copyWith(isStarred: !target.isStarred);
    final updatedList = List<InboxMessageModel>.from(originalMessages);
    updatedList[targetIndex] = updatedTarget;

    state = state.copyWith(messages: updatedList);

    try {
      await _repository.toggleStar(messageId);
      return true;
    } catch (e) {
      // Rollback
      state = state.copyWith(messages: originalMessages);
      return false;
    }
  }

  Future<bool> toggleRead(String messageId, bool isRead) async {
    final originalMessages = state.messages;
    final targetIndex = originalMessages.indexWhere((m) => m.id == messageId);
    if (targetIndex == -1) return false;

    final target = originalMessages[targetIndex];
    final updatedTarget = target.copyWith(isRead: isRead);
    final updatedList = List<InboxMessageModel>.from(originalMessages);
    updatedList[targetIndex] = updatedTarget;

    state = state.copyWith(messages: updatedList);

    try {
      await _repository.toggleRead(messageId, isRead);
      refreshCounts();
      return true;
    } catch (e) {
      state = state.copyWith(messages: originalMessages);
      return false;
    }
  }

  Future<bool> moveToFolder(String messageId, String folder) async {
    try {
      await _repository.moveToFolder(messageId, folder);
      state = state.copyWith(
        messages: state.messages.where((m) => m.id != messageId).toList(),
      );
      refreshCounts();
      return true;
    } catch (e) {
      state = state.copyWith(errorMessage: e.toString());
      return false;
    }
  }

  Future<bool> deleteMessage(String messageId, {bool permanent = false}) async {
    try {
      await _repository.deleteMessage(messageId, permanent: permanent);
      state = state.copyWith(
        messages: state.messages.where((m) => m.id != messageId).toList(),
      );
      refreshCounts();
      return true;
    } catch (e) {
      state = state.copyWith(errorMessage: e.toString());
      return false;
    }
  }

  Future<Map<String, dynamic>> recallMessage(String messageId) async {
    try {
      final res = await _repository.recallMessage(messageId);
      loadMessages(refresh: true);
      return res;
    } catch (e) {
      return {'error': e.toString()};
    }
  }
}

final inboxControllerProvider = StateNotifierProvider<InboxController, InboxState>((ref) {
  final repository = ref.watch(inboxRepositoryProvider);
  return InboxController(repository);
});
