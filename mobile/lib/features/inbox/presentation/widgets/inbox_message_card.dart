import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/core/utils/date_helper.dart';
import 'package:mplatform_mobile/core/utils/html_helper.dart';
import 'package:mplatform_mobile/core/utils/uuid_formatter.dart';
import 'package:mplatform_mobile/features/auth/presentation/providers/auth_provider.dart';
import 'package:mplatform_mobile/features/inbox/domain/models/inbox_message_model.dart';
import 'package:mplatform_mobile/features/inbox/presentation/providers/inbox_provider.dart';

class InboxMessageCard extends ConsumerWidget {
  final InboxMessageModel message;
  final VoidCallback? onTap;

  const InboxMessageCard({
    super.key,
    required this.message,
    this.onTap,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final theme = Theme.of(context);
    final isUnread = !message.isRead;
    final users = ref.watch(userListProvider).valueOrNull ?? [];

    // UUID 마스킹
    final formattedId = UuidFormatter.format(message.id, prefix: 'INB');
    final formattedDate = message.createdAt != null
        ? DateHelper.formatDateTime(message.createdAt!)
        : '';

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      elevation: isUnread ? 2 : 0.5,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(10),
        side: isUnread
            ? BorderSide(color: theme.colorScheme.primary.withOpacity(0.3), width: 1.2)
            : BorderSide(color: Colors.grey.withOpacity(0.2)),
      ),
      color: isUnread
          ? theme.colorScheme.primaryContainer.withOpacity(0.08)
          : theme.cardColor,
      child: InkWell(
        borderRadius: BorderRadius.circular(10),
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Header: Sender & ID & Star
              Row(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  // Unread dot
                  if (isUnread)
                    Container(
                      width: 8,
                      height: 8,
                      margin: const EdgeInsets.only(right: 8),
                      decoration: BoxDecoration(
                        color: theme.colorScheme.primary,
                        shape: BoxShape.circle,
                      ),
                    ),
                  // Sender name
                  Expanded(
                    child: Text(
                      message.senderName.isNotEmpty ? message.senderName : l10n.sender,
                      style: TextStyle(
                        fontWeight: isUnread ? FontWeight.bold : FontWeight.w600,
                        fontSize: 14,
                        color: theme.textTheme.bodyLarge?.color,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  // ID Badge
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                    margin: const EdgeInsets.only(right: 6),
                    decoration: BoxDecoration(
                      color: Colors.grey.withOpacity(0.15),
                      borderRadius: BorderRadius.circular(4),
                    ),
                    child: Text(
                      formattedId,
                      style: TextStyle(
                        fontSize: 11,
                        color: Colors.grey[700],
                        fontFamily: 'monospace',
                      ),
                    ),
                  ),
                  // Date
                  Text(
                    formattedDate,
                    style: TextStyle(
                      fontSize: 11,
                      color: Colors.grey[600],
                    ),
                  ),
                  const SizedBox(width: 4),
                  // Star button
                  IconButton(
                    iconSize: 20,
                    padding: EdgeInsets.zero,
                    constraints: const BoxConstraints(),
                    icon: Icon(
                      message.isStarred ? Icons.star : Icons.star_border,
                      color: message.isStarred ? Colors.amber[700] : Colors.grey[400],
                    ),
                    onPressed: () {
                      ref.read(inboxControllerProvider.notifier).toggleStar(message.id);
                    },
                  ),
                ],
              ),
              const SizedBox(height: 6),
              // Subject & Badges
              Row(
                children: [
                  // Importance badge
                  if (message.importance == 'HIGH' || message.importance == 'URGENT')
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                      margin: const EdgeInsets.only(right: 6),
                      decoration: BoxDecoration(
                        color: message.importance == 'URGENT'
                            ? Colors.red.withOpacity(0.15)
                            : Colors.orange.withOpacity(0.15),
                        borderRadius: BorderRadius.circular(4),
                      ),
                      child: Text(
                        message.importance == 'URGENT'
                            ? l10n.importanceUrgent
                            : l10n.importanceHigh,
                        style: TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.bold,
                          color: message.importance == 'URGENT' ? Colors.red : Colors.deepOrange,
                        ),
                      ),
                    ),
                  // Subject
                  Expanded(
                    child: Text(
                      message.subject.isNotEmpty ? message.subject : l10n.noSubject,
                      style: TextStyle(
                        fontWeight: isUnread ? FontWeight.w700 : FontWeight.w500,
                        fontSize: 14,
                        color: isUnread
                            ? theme.textTheme.titleMedium?.color
                            : theme.textTheme.bodyMedium?.color,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  if (message.hasAttachments || message.attachmentCount > 0)
                    Padding(
                      padding: const EdgeInsets.only(left: 4),
                      child: Icon(Icons.attach_file, size: 16, color: Colors.grey[600]),
                    ),
                ],
              ),
              const SizedBox(height: 4),
              // Body snippet (HTML 태그 제거, UUID 치환 및 엔티티 디코딩된 플레인 텍스트)
              Text(
                HtmlHelper.toPlainText(HtmlHelper.replaceUserUuids(message.body, users)),
                style: TextStyle(
                  fontSize: 12,
                  color: Colors.grey[600],
                  height: 1.3,
                ),
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
