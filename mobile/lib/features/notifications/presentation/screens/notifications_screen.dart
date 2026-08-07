import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/core/utils/date_helper.dart';
import 'package:mplatform_mobile/core/utils/uuid_formatter.dart';
import 'package:mplatform_mobile/features/notifications/domain/models/notification_item.dart';
import 'package:mplatform_mobile/features/notifications/presentation/providers/notifications_provider.dart';

class NotificationsScreen extends ConsumerStatefulWidget {
  const NotificationsScreen({super.key});

  @override
  ConsumerState<NotificationsScreen> createState() => _NotificationsScreenState();
}

class _NotificationsScreenState extends ConsumerState<NotificationsScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) {
        ref.read(notificationsControllerProvider.notifier).fetchNotifications(refresh: true);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final state = ref.watch(notificationsControllerProvider);

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.notificationsTitle),
        elevation: 2,
        backgroundColor: Colors.deepPurple,
        foregroundColor: Colors.white,
        actions: [
          TextButton.icon(
            onPressed: state.notifications.isEmpty
                ? null
                : () {
                    ref.read(notificationsControllerProvider.notifier).markAllAsRead();
                  },
            icon: const Icon(Icons.done_all, color: Colors.white, size: 18),
            label: Text(
              l10n.notificationsMarkAllRead,
              style: const TextStyle(color: Colors.white, fontSize: 13),
            ),
          ),
        ],
      ),
      body: state.isLoading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: () async {
                ref.read(notificationsControllerProvider.notifier).fetchNotifications(refresh: true);
              },
              child: state.notifications.isEmpty
                  ? CustomScrollView(
                      physics: const AlwaysScrollableScrollPhysics(),
                      slivers: [
                        SliverFillRemaining(
                          child: Center(
                            child: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Icon(Icons.notifications_off_outlined, size: 64, color: Colors.grey[400]),
                                const SizedBox(height: 16),
                                Text(l10n.notificationsEmpty, style: TextStyle(color: Colors.grey[600], fontSize: 15)),
                              ],
                            ),
                          ),
                        ),
                      ],
                    )
                  : ListView.builder(
                      physics: const AlwaysScrollableScrollPhysics(),
                      padding: const EdgeInsets.all(12),
                  itemCount: state.notifications.length,
                  itemBuilder: (context, index) {
                    final NotificationItem item = state.notifications[index];
                    // 규칙: 무의미한 raw UUID 표출 금지 -> 식별 코드(REF-xxxx)로 치환
                    final String displayCode = UuidFormatter.format(item.targetId, prefix: 'REF');
                    final String formattedDate = DateHelper.formatWithOffset(item.createdAt, 9, pattern: 'yyyy-MM-dd HH:mm');

                    return Card(
                      margin: const EdgeInsets.only(bottom: 12),
                      elevation: item.isRead ? 1 : 3,
                      color: item.isRead ? Colors.grey[100] : Colors.white,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                        side: item.isRead
                            ? BorderSide.none
                            : BorderSide(color: Colors.deepPurple.shade300, width: 1.5),
                      ),
                      child: InkWell(
                        borderRadius: BorderRadius.circular(12),
                        onTap: () {
                          if (!item.isRead) {
                            ref.read(notificationsControllerProvider.notifier).markAsRead(item.id);
                          }
                        },
                        child: Padding(
                          padding: const EdgeInsets.all(16.0),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: [
                                  Container(
                                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                    decoration: BoxDecoration(
                                      color: item.targetType == 'APPROVAL'
                                          ? Colors.orange.withAlpha(40)
                                          : Colors.blue.withAlpha(40),
                                      borderRadius: BorderRadius.circular(6),
                                    ),
                                    child: Text(
                                      item.targetType == 'APPROVAL' ? l10n.approval : l10n.notification,
                                      style: TextStyle(
                                        fontSize: 12,
                                        fontWeight: FontWeight.bold,
                                        color: item.targetType == 'APPROVAL' ? Colors.orange[900] : Colors.blue[900],
                                      ),
                                    ),
                                  ),
                                  Text(
                                    formattedDate,
                                    style: TextStyle(fontSize: 11, color: Colors.grey[500]),
                                  ),
                                ],
                              ),
                              const SizedBox(height: 10),
                              Text(
                                item.title,
                                style: TextStyle(
                                  fontSize: 16,
                                  fontWeight: item.isRead ? FontWeight.w600 : FontWeight.bold,
                                  color: Colors.black87,
                                ),
                              ),
                              const SizedBox(height: 6),
                              Text(
                                item.content,
                                style: TextStyle(fontSize: 14, color: Colors.grey[700]),
                              ),
                              const SizedBox(height: 12),
                              Row(
                                mainAxisAlignment: MainAxisAlignment.end,
                                children: [
                                  Text(
                                    displayCode,
                                    style: TextStyle(fontSize: 11, color: Colors.deepPurple[600], fontWeight: FontWeight.w600),
                                  ),
                                ],
                              ),
                            ],
                          ),
                        ),
                      ),
                    );
                  },
                ),
            ),
    );
  }
}
