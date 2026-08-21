import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_widget_from_html/flutter_widget_from_html.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/core/utils/date_helper.dart';
import 'package:mplatform_mobile/core/utils/html_helper.dart';
import 'package:mplatform_mobile/core/utils/uuid_formatter.dart';
import 'package:mplatform_mobile/features/auth/presentation/providers/auth_provider.dart';
import 'package:mplatform_mobile/features/inbox/data/repositories/inbox_repository.dart';
import 'package:mplatform_mobile/features/inbox/domain/models/inbox_message_model.dart';
import 'package:mplatform_mobile/features/inbox/presentation/providers/inbox_provider.dart';
import 'package:mplatform_mobile/features/inbox/presentation/screens/inbox_compose_screen.dart';

class InboxDetailScreen extends ConsumerStatefulWidget {
  final String messageId;

  const InboxDetailScreen({
    super.key,
    required this.messageId,
  });

  @override
  ConsumerState<InboxDetailScreen> createState() => _InboxDetailScreenState();
}

class _InboxDetailScreenState extends ConsumerState<InboxDetailScreen> {
  InboxMessageModel? _message;
  bool _isLoading = true;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _loadMessage();
  }

  Future<void> _loadMessage() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final repo = ref.read(inboxRepositoryProvider);
      final msg = await repo.getMessage(widget.messageId);
      if (mounted) {
        setState(() {
          _message = msg;
          _isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _errorMessage = e.toString();
          _isLoading = false;
        });
      }
    }
  }

  Future<void> _handleRecall() async {
    final l10n = AppLocalizations.of(context)!;
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(l10n.recallMessage),
        content: Text(l10n.recallConfirm),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: Text(l10n.cancel),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            onPressed: () => Navigator.pop(ctx, true),
            child: Text(l10n.confirm),
          ),
        ],
      ),
    );

    if (confirmed == true) {
      final notifier = ref.read(inboxControllerProvider.notifier);
      final res = await notifier.recallMessage(widget.messageId);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              res.containsKey('error')
                  ? '${l10n.error}: ${res['error']}'
                  : l10n.recallSuccess,
            ),
          ),
        );
        _loadMessage();
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final currentUser = ref.watch(authControllerProvider).valueOrNull;

    if (_isLoading) {
      return Scaffold(
        appBar: AppBar(title: Text(l10n.inboxTitle)),
        body: const Center(child: CircularProgressIndicator()),
      );
    }

    if (_errorMessage != null || _message == null) {
      return Scaffold(
        appBar: AppBar(title: Text(l10n.inboxTitle)),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(_errorMessage ?? l10n.noMessageSelected),
              const SizedBox(height: 12),
              ElevatedButton(
                onPressed: _loadMessage,
                child: Text(l10n.refresh),
              ),
            ],
          ),
        ),
      );
    }

    final msg = _message!;
    final formattedId = UuidFormatter.format(msg.id, prefix: 'INB');
    final formattedDate = msg.createdAt != null
        ? DateHelper.formatDateTime(msg.createdAt!)
        : '';
    final isSender = currentUser != null &&
        (currentUser.id == msg.senderId || currentUser.username == msg.senderName);

    return Scaffold(
      appBar: AppBar(
        title: Text(formattedId),
        actions: [
          IconButton(
            icon: Icon(
              msg.isStarred ? Icons.star : Icons.star_border,
              color: msg.isStarred ? Colors.amber : Colors.white,
            ),
            onPressed: () async {
              await ref.read(inboxControllerProvider.notifier).toggleStar(msg.id);
              _loadMessage();
            },
          ),
          IconButton(
            icon: const Icon(Icons.delete_outline),
            onPressed: () async {
              await ref.read(inboxControllerProvider.notifier).deleteMessage(msg.id);
              if (mounted) Navigator.pop(context);
            },
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Subject
            Text(
              msg.subject.isNotEmpty ? msg.subject : l10n.noSubject,
              style: const TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 12),
            // Header Info Card
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.grey.withOpacity(0.08),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Text(
                        '${l10n.sender}: ',
                        style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                      ),
                      Text(
                        msg.senderName,
                        style: const TextStyle(fontSize: 13),
                      ),
                      const Spacer(),
                      Text(
                        formattedDate,
                        style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                      ),
                    ],
                  ),
                  if (msg.toRecipients.isNotEmpty) ...[
                    const SizedBox(height: 6),
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          '${l10n.recipientTo}: ',
                          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                        ),
                        Expanded(
                          child: Text(
                            msg.toRecipients.map((r) => r.name).join(', '),
                            style: const TextStyle(fontSize: 13),
                          ),
                        ),
                      ],
                    ),
                  ],
                  if (msg.ccRecipients.isNotEmpty) ...[
                    const SizedBox(height: 6),
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          '${l10n.recipientCc}: ',
                          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                        ),
                        Expanded(
                          child: Text(
                            msg.ccRecipients.map((r) => r.name).join(', '),
                            style: const TextStyle(fontSize: 13),
                          ),
                        ),
                      ],
                    ),
                  ],
                ],
              ),
            ),
            const SizedBox(height: 20),
            // Body Content (HTML / RichText 렌더링 지원 & UUID -> username 자동 치환)
            () {
              final users = ref.watch(userListProvider).valueOrNull ?? [];
              final displayBody = HtmlHelper.replaceUserUuids(msg.body, users);
              if (HtmlHelper.isHtml(displayBody)) {
                return HtmlWidget(
                  displayBody,
                  textStyle: const TextStyle(fontSize: 15, height: 1.5, color: Colors.black87),
                  customStylesBuilder: (element) {
                    if (element.localName == 'blockquote') {
                      return {
                        'margin': '8px 0',
                        'padding': '8px 12px',
                        'border-left': '3px solid #ccc',
                        'background-color': '#f9f9f9',
                        'color': '#555555',
                      };
                    }
                    if (element.localName == 'hr') {
                      return {
                        'margin': '12px 0',
                        'border-top': '1px solid #e0e0e0',
                      };
                    }
                    return null;
                  },
                );
              } else {
                return Text(
                  displayBody,
                  style: const TextStyle(fontSize: 15, height: 1.5),
                );
              }
            }(),
            const SizedBox(height: 24),
            // Attachments Section
            if (msg.attachments.isNotEmpty) ...[
              const Divider(),
              const SizedBox(height: 8),
              Row(
                children: [
                  const Icon(Icons.attach_file, size: 18),
                  const SizedBox(width: 6),
                  Text(
                    '${l10n.attachments} (${msg.attachments.length})',
                    style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              ...msg.attachments.map(
                (att) => ListTile(
                  dense: true,
                  contentPadding: EdgeInsets.zero,
                  leading: const Icon(Icons.insert_drive_file, color: Colors.deepPurple),
                  title: Text(att.fileName, style: const TextStyle(fontSize: 13)),
                  subtitle: Text('${(att.fileSize / 1024).toStringAsFixed(1)} KB'),
                  trailing: IconButton(
                    icon: const Icon(Icons.download, size: 20),
                    onPressed: () {
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(content: Text('${l10n.downloadAttachment}: ${att.fileName}')),
                      );
                    },
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
      bottomNavigationBar: SafeArea(
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          decoration: BoxDecoration(
            color: Theme.of(context).cardColor,
            boxShadow: [
              BoxShadow(
                color: Colors.black.withOpacity(0.05),
                blurRadius: 4,
                offset: const Offset(0, -2),
              ),
            ],
          ),
          child: Row(
            children: [
              // Reply
              Expanded(
                child: OutlinedButton.icon(
                  icon: const Icon(Icons.reply, size: 18),
                  label: Text(l10n.reply),
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => InboxComposeScreen(
                          replyToMessage: msg,
                          isReplyAll: false,
                        ),
                      ),
                    );
                  },
                ),
              ),
              const SizedBox(width: 8),
              // Forward
              Expanded(
                child: OutlinedButton.icon(
                  icon: const Icon(Icons.forward, size: 18),
                  label: Text(l10n.forward),
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => InboxComposeScreen(
                          forwardMessage: msg,
                        ),
                      ),
                    );
                  },
                ),
              ),
              if (isSender) ...[
                const SizedBox(width: 8),
                IconButton(
                  icon: const Icon(Icons.undo, color: Colors.orange),
                  tooltip: l10n.recallMessage,
                  onPressed: _handleRecall,
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}
