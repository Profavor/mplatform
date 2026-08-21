import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/core/utils/uuid_formatter.dart';
import 'package:mplatform_mobile/features/inbox/data/repositories/inbox_repository.dart';
import 'package:mplatform_mobile/features/inbox/domain/models/inbox_message_model.dart';
import 'package:mplatform_mobile/features/inbox/presentation/providers/inbox_provider.dart';

class InboxComposeScreen extends ConsumerStatefulWidget {
  final InboxMessageModel? replyToMessage;
  final bool isReplyAll;
  final InboxMessageModel? forwardMessage;
  final String? relatedApprovalId;
  final String? defaultRecipientUserId;
  final String? defaultRecipientName;

  const InboxComposeScreen({
    super.key,
    this.replyToMessage,
    this.isReplyAll = false,
    this.forwardMessage,
    this.relatedApprovalId,
    this.defaultRecipientUserId,
    this.defaultRecipientName,
  });

  @override
  ConsumerState<InboxComposeScreen> createState() => _InboxComposeScreenState();
}

class _InboxComposeScreenState extends ConsumerState<InboxComposeScreen> {
  final TextEditingController _recipientsController = TextEditingController();
  final TextEditingController _ccController = TextEditingController();
  final TextEditingController _subjectController = TextEditingController();
  final TextEditingController _bodyController = TextEditingController();

  String _importance = 'NORMAL';
  bool _isSending = false;

  @override
  void initState() {
    super.initState();

    if (widget.replyToMessage != null) {
      final replyMsg = widget.replyToMessage!;
      _recipientsController.text = replyMsg.senderId;
      if (widget.isReplyAll) {
        final ccIds = replyMsg.toRecipients.map((r) => r.userId).where((id) => id != replyMsg.senderId).join(', ');
        _ccController.text = ccIds;
      }
      _subjectController.text = replyMsg.subject.startsWith('Re:')
          ? replyMsg.subject
          : 'Re: ${replyMsg.subject}';
      _bodyController.text = '\n\n--- Original Message ---\n${replyMsg.body}';
    } else if (widget.forwardMessage != null) {
      final fwdMsg = widget.forwardMessage!;
      _subjectController.text = fwdMsg.subject.startsWith('Fwd:')
          ? fwdMsg.subject
          : 'Fwd: ${fwdMsg.subject}';
      _bodyController.text = '\n\n--- Forwarded Message ---\nFrom: ${fwdMsg.senderName}\nSubject: ${fwdMsg.subject}\n\n${fwdMsg.body}';
    } else if (widget.defaultRecipientUserId != null) {
      _recipientsController.text = widget.defaultRecipientUserId!;
    }

    if (widget.relatedApprovalId != null) {
      final formattedApprovalId = UuidFormatter.format(widget.relatedApprovalId!, prefix: 'APR');
      if (_subjectController.text.isEmpty) {
        _subjectController.text = '[$formattedApprovalId] 결재 관련 메모';
      }
    }
  }

  @override
  void dispose() {
    _recipientsController.dispose();
    _ccController.dispose();
    _subjectController.dispose();
    _bodyController.dispose();
    super.dispose();
  }

  Future<void> _handleSend({bool isDraft = false}) async {
    final l10n = AppLocalizations.of(context);
    final recipients = _recipientsController.text
        .split(',')
        .map((s) => s.trim())
        .where((s) => s.isNotEmpty)
        .toList();
    final ccList = _ccController.text
        .split(',')
        .map((s) => s.trim())
        .where((s) => s.isNotEmpty)
        .toList();

    if (!isDraft && recipients.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(l10n.recipientRequired)),
      );
      return;
    }

    final subject = _subjectController.text.trim();
    if (!isDraft && subject.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(l10n.subjectRequired)),
      );
      return;
    }

    setState(() {
      _isSending = true;
    });

    final payload = {
      'recipientUserIds': recipients,
      'ccUserIds': ccList,
      'subject': subject.isNotEmpty ? subject : l10n.noSubject,
      'body': _bodyController.text,
      'importance': _importance,
      'isDraft': isDraft,
      if (widget.relatedApprovalId != null) 'relatedApprovalId': widget.relatedApprovalId,
      if (widget.replyToMessage != null) 'parentMessageId': widget.replyToMessage!.id,
    };

    try {
      final repo = ref.read(inboxRepositoryProvider);
      if (isDraft) {
        await repo.saveDraft(payload);
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(l10n.draftSaved)),
          );
        }
      } else {
        await repo.sendMessage(payload);
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(l10n.messageSent)),
          );
        }
      }

      ref.read(inboxControllerProvider.notifier).init();
      if (mounted) Navigator.pop(context);
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(isDraft ? l10n.draftFailed : l10n.sendFailed),
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() {
          _isSending = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.composeTitle),
        actions: [
          IconButton(
            icon: const Icon(Icons.save_outlined),
            tooltip: l10n.saveDraft,
            onPressed: _isSending ? null : () => _handleSend(isDraft: true),
          ),
          IconButton(
            icon: const Icon(Icons.send),
            tooltip: l10n.send,
            onPressed: _isSending ? null : () => _handleSend(isDraft: false),
          ),
        ],
      ),
      body: _isSending
          ? const Center(child: CircularProgressIndicator())
          : SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Recipients
                  TextField(
                    controller: _recipientsController,
                    decoration: InputDecoration(
                      labelText: l10n.recipientTo,
                      hintText: l10n.searchUsers,
                      prefixIcon: const Icon(Icons.person_outline),
                      border: const OutlineInputBorder(),
                    ),
                  ),
                  const SizedBox(height: 12),
                  // CC
                  TextField(
                    controller: _ccController,
                    decoration: InputDecoration(
                      labelText: l10n.recipientCc,
                      hintText: l10n.searchUsers,
                      prefixIcon: const Icon(Icons.people_outline),
                      border: const OutlineInputBorder(),
                    ),
                  ),
                  const SizedBox(height: 12),
                  // Importance Selector
                  Row(
                    children: [
                      Text(
                        '${l10n.importance}: ',
                        style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 13),
                      ),
                      const SizedBox(width: 8),
                      ChoiceChip(
                        label: Text(l10n.importanceNormal),
                        selected: _importance == 'NORMAL',
                        onSelected: (val) {
                          if (val) setState(() => _importance = 'NORMAL');
                        },
                      ),
                      const SizedBox(width: 6),
                      ChoiceChip(
                        label: Text(l10n.importanceHigh),
                        selected: _importance == 'HIGH',
                        selectedColor: Colors.orange.withOpacity(0.3),
                        onSelected: (val) {
                          if (val) setState(() => _importance = 'HIGH');
                        },
                      ),
                      const SizedBox(width: 6),
                      ChoiceChip(
                        label: Text(l10n.importanceUrgent),
                        selected: _importance == 'URGENT',
                        selectedColor: Colors.red.withOpacity(0.3),
                        onSelected: (val) {
                          if (val) setState(() => _importance = 'URGENT');
                        },
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  // Subject
                  TextField(
                    controller: _subjectController,
                    decoration: InputDecoration(
                      labelText: l10n.subject,
                      hintText: l10n.subjectPlaceholder,
                      border: const OutlineInputBorder(),
                    ),
                  ),
                  const SizedBox(height: 12),
                  // Body
                  TextField(
                    controller: _bodyController,
                    maxLines: 12,
                    decoration: InputDecoration(
                      hintText: l10n.bodyPlaceholder,
                      border: const OutlineInputBorder(),
                      alignLabelWithHint: true,
                    ),
                  ),
                ],
              ),
            ),
    );
  }
}
