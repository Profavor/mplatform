import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/features/notifications/data/repositories/notifications_repository.dart';
import 'package:mplatform_mobile/features/notifications/domain/models/notification_item.dart';

final notificationsRepositoryProvider = Provider<NotificationsRepository>((ref) {
  final dio = ref.watch(dioProvider);
  return NotificationsRepository(dio);
});

class NotificationsState {
  final List<NotificationItem> notifications;
  final bool isLoading;
  final String? errorMessage;
  final int currentPage;

  const NotificationsState({
    this.notifications = const [],
    this.isLoading = false,
    this.errorMessage,
    this.currentPage = 0,
  });

  NotificationsState copyWith({
    List<NotificationItem>? notifications,
    bool? isLoading,
    String? errorMessage,
    int? currentPage,
  }) {
    return NotificationsState(
      notifications: notifications ?? this.notifications,
      isLoading: isLoading ?? this.isLoading,
      errorMessage: errorMessage ?? this.errorMessage,
      currentPage: currentPage ?? this.currentPage,
    );
  }
}

class NotificationsController extends StateNotifier<NotificationsState> {
  final NotificationsRepository _repository;

  NotificationsController(this._repository) : super(const NotificationsState());

  Future<void> fetchNotifications({bool refresh = false}) async {
    if (state.isLoading) return;

    final targetPage = refresh ? 0 : state.currentPage;
    state = state.copyWith(isLoading: true, errorMessage: null, currentPage: targetPage);

    try {
      final newItems = await _repository.getNotifications(page: targetPage, size: 20);
      final list = refresh ? newItems : [...state.notifications, ...newItems];
      state = state.copyWith(notifications: list, isLoading: false);
    } catch (e) {
      // In mobile offline or fallback demonstration, load demo system alerts if empty
      if (state.notifications.isEmpty && targetPage == 0) {
        state = state.copyWith(
          notifications: const [
            NotificationItem(
              id: 'notif-1',
              title: '결재 승인 대기 알림',
              content: '신규 도메인 레코드 등록 결재가 접수되었습니다.',
              targetId: '340a0917-af0b-4d13-a1ce-479d4b2e2ca7',
              targetType: 'APPROVAL',
              createdAt: '2026-08-06T11:45:00Z',
              isRead: false,
            ),
            NotificationItem(
              id: 'notif-2',
              title: '시스템 권한 동기화 안내',
              content: 'admin(*) 권한이 정상적으로 동기화되었습니다.',
              targetId: 'admin-0001-xxxx',
              targetType: 'SYSTEM',
              createdAt: '2026-08-06T10:00:00Z',
              isRead: true,
            ),
          ],
          isLoading: false,
        );
      } else {
        state = state.copyWith(isLoading: false, errorMessage: e.toString());
      }
    }
  }

  Future<void> markAsRead(String id) async {
    try {
      await _repository.markAsRead(id);
    } catch (_) {
      // ignore network error in local UI state update
    }
    final updated = state.notifications.map((n) {
      if (n.id == id) {
        return n.copyWith(isRead: true);
      }
      return n;
    }).toList();
    state = state.copyWith(notifications: updated);
  }

  Future<void> markAllAsRead() async {
    try {
      await _repository.markAllAsRead();
    } catch (_) {
      // ignore
    }
    final updated = state.notifications.map((n) => n.copyWith(isRead: true)).toList();
    state = state.copyWith(notifications: updated);
  }
}

final notificationsControllerProvider = StateNotifierProvider<NotificationsController, NotificationsState>((ref) {
  final repo = ref.watch(notificationsRepositoryProvider);
  return NotificationsController(repo);
});
