import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/features/inbox/domain/models/inbox_folder_count_model.dart';
import 'package:mplatform_mobile/features/inbox/presentation/providers/inbox_provider.dart';

class InboxFolderSelector extends ConsumerWidget {
  const InboxFolderSelector({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final state = ref.watch(inboxControllerProvider);
    final notifier = ref.read(inboxControllerProvider.notifier);

    final folders = [
      {'key': 'INBOX', 'label': l10n.folderInbox, 'icon': Icons.inbox},
      {'key': 'SENT', 'label': l10n.folderSent, 'icon': Icons.send},
      {'key': 'STARRED', 'label': l10n.folderStarred, 'icon': Icons.star},
      {'key': 'DRAFT', 'label': l10n.folderDraft, 'icon': Icons.drafts},
      {'key': 'ARCHIVE', 'label': l10n.folderArchive, 'icon': Icons.archive},
      {'key': 'TRASH', 'label': l10n.folderTrash, 'icon': Icons.delete_outline},
    ];

    return Container(
      height: 48,
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 12),
        itemCount: folders.length,
        separatorBuilder: (_, __) => const SizedBox(width: 8),
        itemBuilder: (context, index) {
          final item = folders[index];
          final folderKey = item['key'] as String;
          final isSelected = state.currentFolder == folderKey;

          // Find folder count if exists
          final countModel = state.folderCounts.firstWhere(
            (fc) => fc.folder.toUpperCase() == folderKey,
            orElse: () => InboxFolderCountModel(folder: '', total: 0, unread: 0),
          );

          return FilterChip(
            selected: isSelected,
            showCheckmark: false,
            avatar: Icon(
              item['icon'] as IconData,
              size: 16,
              color: isSelected ? Colors.white : Colors.grey[700],
            ),
            label: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(item['label'] as String),
                if (countModel.unread > 0 && folderKey == 'INBOX') ...[
                  const SizedBox(width: 4),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 1),
                    decoration: BoxDecoration(
                      color: isSelected ? Colors.white : Colors.red,
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: Text(
                      '${countModel.unread}',
                      style: TextStyle(
                        fontSize: 10,
                        fontWeight: FontWeight.bold,
                        color: isSelected ? Colors.deepPurple : Colors.white,
                      ),
                    ),
                  ),
                ],
              ],
            ),
            selectedColor: Theme.of(context).colorScheme.primary,
            labelStyle: TextStyle(
              color: isSelected ? Colors.white : Colors.black87,
              fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
              fontSize: 12,
            ),
            onSelected: (_) {
              notifier.selectFolder(folderKey);
            },
          );
        },
      ),
    );
  }
}
