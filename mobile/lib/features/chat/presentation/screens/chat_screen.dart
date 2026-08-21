import 'dart:typed_data';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:file_saver/file_saver.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:pasteboard/pasteboard.dart';
import 'package:video_player/video_player.dart';
import 'package:file_picker/file_picker.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/core/utils/date_helper.dart';
import 'package:mplatform_mobile/core/utils/uuid_formatter.dart';
import 'package:mplatform_mobile/features/auth/domain/models/user_model.dart';
import 'package:mplatform_mobile/features/auth/presentation/providers/auth_provider.dart';
import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:mplatform_mobile/features/chat/domain/models/chat_message_model.dart';
import 'package:mplatform_mobile/features/chat/domain/models/chat_room_model.dart';
import 'package:mplatform_mobile/features/chat/presentation/providers/chat_provider.dart';
import 'package:mplatform_mobile/core/widgets/authenticated_image.dart';
import 'package:mplatform_mobile/core/widgets/image_viewer_dialog.dart';
import 'package:mplatform_mobile/core/utils/web_video_player_stub.dart' if (dart.library.html) 'package:mplatform_mobile/core/utils/web_video_player.dart';

class ChatScreen extends ConsumerStatefulWidget {
  final String roomId;
  final String roomTitle;
  final String currentUsername;

  const ChatScreen({
    super.key,
    required this.roomId,
    required this.roomTitle,
    required this.currentUsername,
  });

  @override
  ConsumerState<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends ConsumerState<ChatScreen> {
  final TextEditingController _messageController = TextEditingController();
  final ScrollController _scrollController = ScrollController();
  final Map<String, GlobalKey> _dateKeys = {};

  static const List<String> _quickEmojis = ['👍', '❤️', '😂', '🎉', '🔥', '✅', '🙏'];
  
  List<UserModel> _allUsers = [];

  @override
  void initState() {
    super.initState();
    _loadAllUsers();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) {
        ref.read(chatControllerProvider.notifier).selectRoom(widget.roomId);
      }
    });
  }

  Future<void> _loadAllUsers() async {
    try {
      final authRepo = ref.read(authRepositoryProvider);
      final users = await authRepo.getUsers();
      if (mounted) {
        setState(() {
          _allUsers = users;
        });
      }
    } catch (e) {
      // ignore
    }
  }

  @override
  void dispose() {
    _messageController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  bool _isMyMessage(ChatMessageModel msg) {
    final user = ref.read(authControllerProvider).value;
    if (user == null) return msg.senderUsername == widget.currentUsername;
    final su = msg.senderUsername.toString();
    return su == user.username || su == user.id || msg.senderName == user.username;
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 200),
          curve: Curves.easeOut,
        );
      }
    });
  }

  void _sendMessage() {
    final text = _messageController.text.trim();
    if (text.isNotEmpty) {
      ref.read(chatControllerProvider.notifier).sendMessage(widget.roomId, content: text);
      _messageController.clear();
      _scrollToBottom();
    }
  }

  void _sendEmoji(String emoji) {
    ref.read(chatControllerProvider.notifier).sendMessage(
      widget.roomId,
      content: emoji,
      messageType: 'EMOJI',
    );
    _scrollToBottom();
  }

  Future<void> _pickAndSendFile() async {
    try {
      final result = await FilePicker.pickFiles(withData: true);
      if (result != null && result.files.isNotEmpty) {
        final file = result.files.first;
        if (file.bytes != null) {
          final isImage = file.extension != null &&
              ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'].contains(file.extension!.toLowerCase());
          await ref.read(chatControllerProvider.notifier).uploadAndSendFile(
            widget.roomId,
            file.bytes!,
            file.name,
            isImage,
          );
          _scrollToBottom();
        }
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('파일 업로드 실패: $e'), backgroundColor: Colors.redAccent),
        );
      }
    }
  }

  Future<void> _handlePaste() async {
    try {
      final imageBytes = await Pasteboard.image;
      final clipboardData = await Clipboard.getData(Clipboard.kTextPlain);
      final textData = clipboardData?.text ?? '';

      final isTable = _parseTableContent(textData).isTable;

      if (imageBytes != null && isTable) {
        if (mounted) {
          showDialog(
            context: context,
            builder: (ctx) => AlertDialog(
              title: const Text('붙여넣기 옵션'),
              content: const Text('복사하신 내용에 표(텍스트)와 이미지가 모두 포함되어 있습니다. 어떤 형식으로 전송하시겠습니까?'),
              actions: [
                TextButton(
                  onPressed: () {
                    Navigator.pop(ctx);
                    final cursor = _messageController.selection.baseOffset;
                    if (cursor >= 0) {
                      final text = _messageController.text;
                      _messageController.text = text.replaceRange(cursor, cursor, textData);
                      _messageController.selection = TextSelection.collapsed(offset: cursor + textData.length);
                    } else {
                      _messageController.text += textData;
                    }
                  },
                  child: const Text('표(텍스트)로 붙여넣기'),
                ),
                TextButton(
                  onPressed: () async {
                    Navigator.pop(ctx);
                    await ref.read(chatControllerProvider.notifier).uploadAndSendFile(
                      widget.roomId,
                      imageBytes,
                      'pasted_image.png',
                      true,
                    );
                    _scrollToBottom();
                  },
                  child: const Text('이미지로 바로 전송'),
                ),
              ],
            ),
          );
        }
        return;
      }

      if (imageBytes != null) {
        await ref.read(chatControllerProvider.notifier).uploadAndSendFile(
          widget.roomId,
          imageBytes,
          'pasted_image.png',
          true,
        );
        _scrollToBottom();
        return;
      }

      if (textData.isNotEmpty) {
        final cursor = _messageController.selection.baseOffset;
        if (cursor >= 0) {
          final text = _messageController.text;
          _messageController.text = text.replaceRange(cursor, cursor, textData);
          _messageController.selection = TextSelection.collapsed(offset: cursor + textData.length);
        } else {
          _messageController.text += textData;
        }
      }
    } catch (e) {
      debugPrint('Paste error: $e');
    }
  }

  /// 날짜가 바뀌었는지 확인
  bool _shouldShowDateSeparator(List<ChatMessageModel> messages, int index) {
    if (index == 0) return true;
    final prev = messages[index - 1].timestamp;
    final curr = messages[index].timestamp;
    if (prev == null || curr == null) return false;
    return _getDateKey(prev) != _getDateKey(curr);
  }

  String _getDateKey(String? timeStr) {
    if (timeStr == null) return '';
    try {
      final date = DateTime.parse(timeStr);
      return '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
    } catch (e) {
      return '';
    }
  }

  String _formatDateSeparator(String? timeStr) {
    if (timeStr == null) return '';
    try {
      final date = DateTime.parse(timeStr);
      const days = ['일', '월', '화', '수', '목', '금', '토'];
      return '${date.year}년 ${date.month}월 ${date.day}일 ${days[date.weekday % 7]}요일';
    } catch (e) {
      return '';
    }
  }

  void _jumpToDate(String dateKey) {
    final key = _dateKeys[dateKey];
    if (key != null && key.currentContext != null) {
      Scrollable.ensureVisible(
        key.currentContext!,
        duration: const Duration(milliseconds: 350),
        curve: Curves.easeInOut,
        alignment: 0.08,
      );
    }
  }

  void _showDateBookmarkModal(BuildContext context, List<ChatMessageModel> messages) {
    final l10n = AppLocalizations.of(context);
    final Map<String, String> dateLabels = {};
    final Map<String, int> dateMsgCounts = {};

    for (int i = 0; i < messages.length; i++) {
      final m = messages[i];
      final dateKey = _getDateKey(m.timestamp);
      if (dateKey.isNotEmpty) {
        if (!dateLabels.containsKey(dateKey)) {
          dateLabels[dateKey] = _formatDateSeparator(m.timestamp);
        }
        dateMsgCounts[dateKey] = (dateMsgCounts[dateKey] ?? 0) + 1;
      }
    }

    final dateKeysList = dateLabels.keys.toList();

    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      isScrollControlled: true,
      builder: (ctx) {
        return Material(
          color: Colors.transparent,
          child: Container(
            decoration: const BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
            ),
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
            constraints: BoxConstraints(
              maxHeight: MediaQuery.of(context).size.height * 0.65,
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Center(
                child: Container(
                  width: 40,
                  height: 4,
                  decoration: BoxDecoration(
                    color: Colors.grey[300],
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
              ),
              const SizedBox(height: 14),
              Row(
                children: [
                  const Icon(Icons.calendar_month, color: Colors.deepPurple, size: 22),
                  const SizedBox(width: 8),
                  Text(
                    l10n.messengerCalendarTitle,
                    style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Colors.black87),
                  ),
                  const Spacer(),
                  IconButton(
                    icon: const Icon(Icons.close, size: 20),
                    onPressed: () => Navigator.pop(ctx),
                  ),
                ],
              ),
              const Divider(),
              const SizedBox(height: 6),
              if (dateKeysList.isEmpty)
                Padding(
                  padding: const EdgeInsets.symmetric(vertical: 24),
                  child: Center(
                    child: Text(l10n.searchNoData, style: TextStyle(color: Colors.grey[600])),
                  ),
                )
              else
                Flexible(
                  child: ListView.separated(
                    shrinkWrap: true,
                    itemCount: dateKeysList.length,
                    separatorBuilder: (_, __) => const Divider(height: 1),
                    itemBuilder: (context, index) {
                      final k = dateKeysList[index];
                      final label = dateLabels[k] ?? k;
                      final count = dateMsgCounts[k] ?? 0;

                      return ListTile(
                        contentPadding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                        leading: CircleAvatar(
                          radius: 16,
                          backgroundColor: Colors.deepPurple.shade50,
                          child: const Icon(Icons.bookmark, size: 18, color: Colors.deepPurple),
                        ),
                        title: Text(
                          label,
                          style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14, color: Colors.black87),
                        ),
                        trailing: Container(
                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                          decoration: BoxDecoration(
                            color: Colors.grey[100],
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(color: Colors.grey.shade300),
                          ),
                          child: Text(
                            '$count',
                            style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: Colors.grey[700]),
                          ),
                        ),
                        onTap: () {
                          Navigator.pop(ctx);
                          _jumpToDate(k);
                        },
                      );
                    },
                  ),
                ),
              const SizedBox(height: 12),
            ],
          ),
        ),
        );
      },
    );
  }

  String _getSystemMessageText(ChatMessageModel msg, AppLocalizations l10n) {
    if (msg.messageType == 'JOIN' || msg.messageType == 'LEAVE') {
      String displayContent = msg.content;
      if (msg.content.contains('-') && msg.content.length > 20) {
        try {
          final user = _allUsers.firstWhere((u) => u.id == msg.content);
          displayContent = user.username;
        } catch (e) {
          displayContent = UuidFormatter.format(msg.content, prefix: 'USER');
        }
      }
      
      if (msg.messageType == 'JOIN') {
        return l10n.systemJoin(displayContent);
      }
      return l10n.systemLeave(displayContent);
    }
    return msg.content;
  }

  String _formatFileSize(int? bytes) {
    if (bytes == null || bytes == 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    final i = (bytes > 0) ? (bytes.toDouble().toString().length > 3 ? 1 : 0) : 0;
    if (bytes < k) return '$bytes B';
    if (bytes < k * k) return '${(bytes / k).toStringAsFixed(1)} KB';
    if (bytes < k * k * k) return '${(bytes / (k * k)).toStringAsFixed(1)} MB';
    return '${(bytes / (k * k * k)).toStringAsFixed(1)} GB';
  }

  IconData _getFileIcon(String? fileName) {
    if (fileName == null) return Icons.insert_drive_file;
    final lower = fileName.toLowerCase();
    if (lower.endsWith('.pdf')) return Icons.picture_as_pdf;
    if (lower.endsWith('.xls') || lower.endsWith('.xlsx') || lower.endsWith('.csv')) return Icons.table_chart;
    if (lower.endsWith('.doc') || lower.endsWith('.docx') || lower.endsWith('.txt')) return Icons.description;
    if (lower.endsWith('.zip') || lower.endsWith('.rar') || lower.endsWith('.7z')) return Icons.folder_zip;
    return Icons.insert_drive_file;
  }

  void _showContextMenu(BuildContext context, ChatMessageModel msg, AppLocalizations l10n) {
    final isMe = _isMyMessage(msg);
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(16))),
      builder: (ctx) => Material(
        color: Colors.transparent,
        child: SafeArea(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 40, height: 4,
                margin: const EdgeInsets.symmetric(vertical: 12),
                decoration: BoxDecoration(color: Colors.grey[300], borderRadius: BorderRadius.circular(2)),
              ),
              ListTile(
                leading: const Icon(Icons.copy, color: Colors.blueGrey),
                title: const Text('복사'),
                onTap: () {
                  Navigator.pop(ctx);
                  String copyText = msg.content;
                  if (msg.messageType == 'IMAGE') {
                    copyText = msg.attachmentUrl ?? msg.content;
                  } else if (msg.messageType == 'FILE') {
                    copyText = '${msg.fileName ?? ''}\n${msg.attachmentUrl ?? ''}'.trim();
                  }
                  
                  Clipboard.setData(ClipboardData(text: copyText));
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('클립보드에 복사됨'), duration: Duration(seconds: 1)),
                  );
                },
              ),
              ListTile(
                leading: const Icon(Icons.shortcut, color: Colors.indigo),
                title: const Text('전달'),
                onTap: () {
                  Navigator.pop(ctx);
                  _showForwardDialog(context, msg);
                },
              ),
              if (msg.messageType == 'TEXT' || msg.messageType == 'EMOJI')
                ListTile(
                  leading: const Icon(Icons.translate, color: Colors.teal),
                  title: const Text('번역'),
                  onTap: () {
                    Navigator.pop(ctx);
                    _showTranslation(context, msg);
                  },
                ),
              if (isMe)
                ListTile(
                  leading: const Icon(Icons.delete, color: Colors.red),
                  title: const Text('삭제', style: TextStyle(color: Colors.red)),
                  onTap: () {
                    Navigator.pop(ctx);
                    _confirmDeleteMessage(context, msg, l10n);
                  },
                ),
              const SizedBox(height: 8),
            ],
          ),
        ),
      ),
    );
  }

  void _confirmDeleteMessage(BuildContext context, ChatMessageModel message, AppLocalizations l10n) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(l10n.confirmDelete),
        content: Text(message.content, maxLines: 3, overflow: TextOverflow.ellipsis),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: Text(l10n.cancel),
          ),
          ElevatedButton(
            onPressed: () async {
              Navigator.pop(ctx);
              await ref.read(chatControllerProvider.notifier).deleteMessage(message.messageId);
            },
            style: ElevatedButton.styleFrom(backgroundColor: Colors.redAccent, foregroundColor: Colors.white),
            child: const Text('삭제'),
          ),
        ],
      ),
    );
  }

  Future<void> _showForwardDialog(BuildContext context, ChatMessageModel message) async {
    final authRepo = ref.read(authRepositoryProvider);
    final authState = ref.read(authControllerProvider);
    List<UserModel> users = [];
    try {
      users = await authRepo.getUsers();
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('사용자 목록 로드 실패')));
      }
      return;
    }

    if (!context.mounted) return;
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('메시지 전달'),
        content: SizedBox(
          width: double.maxFinite,
          height: 300,
          child: ListView.builder(
            itemCount: users.length,
            itemBuilder: (context, index) {
              final u = users[index];
              if (u.username == widget.currentUsername) return const SizedBox.shrink();
              return ListTile(
                leading: CircleAvatar(child: Text(u.username[0].toUpperCase())),
                title: Text(u.username),
                subtitle: Text(u.role),
                onTap: () async {
                  Navigator.pop(ctx);
                  final myId = authState.value?.id ?? '';
                  final success = await ref.read(chatControllerProvider.notifier)
                      .forwardMessage(message, u.id, myId);
                  if (context.mounted) {
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text(success ? '전달 완료' : '전달 실패')),
                    );
                  }
                },
              );
            },
          ),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('취소')),
        ],
      ),
    );
  }

  Future<void> _showTranslation(BuildContext context, ChatMessageModel msg) async {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (ctx) => const AlertDialog(
        content: Row(
          children: [
            CircularProgressIndicator(),
            SizedBox(width: 16),
            Text('번역 중...'),
          ],
        ),
      ),
    );

    final translated = await ref.read(chatControllerProvider.notifier).translateMessage(msg.content);

    if (context.mounted) {
      Navigator.pop(context); // close loading dialog
      showDialog(
        context: context,
        builder: (ctx) => AlertDialog(
          title: const Row(
            children: [
              Icon(Icons.translate, color: Colors.teal, size: 20),
              SizedBox(width: 8),
              Text('번역 결과', style: TextStyle(fontSize: 16)),
            ],
          ),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: Colors.grey[100],
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(msg.content, style: const TextStyle(fontSize: 13, color: Colors.black54)),
              ),
              const SizedBox(height: 12),
              const Divider(),
              const SizedBox(height: 8),
              Text(translated, style: const TextStyle(fontSize: 15, fontWeight: FontWeight.w500)),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () {
                Clipboard.setData(ClipboardData(text: translated));
                Navigator.pop(ctx);
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('번역 결과 복사됨'), duration: Duration(seconds: 1)),
                );
              },
              child: const Text('복사'),
            ),
            TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('닫기')),
          ],
        ),
      );
    }
  }

  Future<void> _showMembersDialog(BuildContext context, ChatRoomModel room, bool isCreator) async {
    final repo = ref.read(chatRepositoryProvider);
    List<dynamic> members = [];
    bool isLoading = true;

    if (context.mounted) {
      showDialog(
        context: context,
        builder: (ctx) => StatefulBuilder(
          builder: (context, setState) {
            if (isLoading) {
              repo.getRoomMembers(room.roomId).then((data) {
                if (context.mounted) {
                  setState(() {
                    members = data;
                    isLoading = false;
                  });
                }
              }).catchError((e) {
                if (context.mounted) {
                  setState(() { isLoading = false; });
                }
              });
            }

            return AlertDialog(
              title: Text('대화방 참여자 (${members.length})'),
              content: SizedBox(
                width: double.maxFinite,
                child: isLoading
                    ? const Center(child: CircularProgressIndicator())
                    : ListView.builder(
                        shrinkWrap: true,
                        itemCount: members.length,
                        itemBuilder: (context, index) {
                          final m = members[index];
                          final bool isMe = m['username'] == widget.currentUsername;
                          final bool isRoomCreator = room.createdBy == m['username'] || room.createdBy == m['userId'];

                          final chatState = ref.watch(chatControllerProvider);
                          final isOnline = isMe ||
                                           m['status'] == 'ONLINE' ||
                                           m['status'] == 'ACTIVE' ||
                                           m['online'] == true ||
                                           chatState.onlineUserIds.contains(m['username']) ||
                                           chatState.onlineUserIds.contains(m['userId']) ||
                                           chatState.onlineUserIds.contains(m['id']);

                          return ListTile(
                            leading: CircleAvatar(
                              child: Text((m['username'] as String? ?? 'U')[0].toUpperCase()),
                            ),
                            title: Row(
                              children: [
                                Container(
                                  width: 10, height: 10,
                                  decoration: BoxDecoration(
                                    shape: BoxShape.circle,
                                    color: isOnline ? Colors.green : Colors.grey,
                                  ),
                                ),
                                const SizedBox(width: 8),
                                Text(m['username'] ?? ''),
                                if (isMe) ...[
                                  const SizedBox(width: 4),
                                  const Badge(label: Text('나'), backgroundColor: Colors.green),
                                ],
                                if (isRoomCreator) ...[
                                  const SizedBox(width: 4),
                                  const Badge(label: Text('방장'), backgroundColor: Colors.orange),
                                ],
                              ],
                            ),
                            subtitle: Text(m['role'] ?? 'USER'),
                            trailing: (isCreator && !isMe)
                                ? PopupMenuButton<String>(
                                    icon: const Icon(Icons.more_vert),
                                    onSelected: (val) async {
                                      if (val == 'delegate') {
                                        Navigator.pop(ctx);
                                        await ref.read(chatControllerProvider.notifier).delegateCreator(room.roomId, m['userId']);
                                      } else if (val == 'kick') {
                                        Navigator.pop(ctx);
                                        await ref.read(chatControllerProvider.notifier).kickMember(room.roomId, m['userId']);
                                      }
                                    },
                                    itemBuilder: (context) => [
                                      const PopupMenuItem(value: 'delegate', child: Text('방장 위임')),
                                      const PopupMenuItem(value: 'kick', child: Text('강퇴 (추방)', style: TextStyle(color: Colors.red))),
                                    ],
                                  )
                                : null,
                          );
                        },
                      ),
              ),
              actions: [
                if (isCreator)
                  TextButton(
                    onPressed: () {
                      Navigator.pop(ctx);
                      _showInviteDialog(context, room, members);
                    },
                    child: const Text('초대하기'),
                  ),
                TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('닫기')),
              ],
            );
          },
        ),
      );
    }
  }

  Future<void> _showInviteDialog(BuildContext context, ChatRoomModel room, List<dynamic> currentMembers) async {
    final authRepo = ref.read(authRepositoryProvider);
    List<UserModel> users = [];
    try {
      users = await authRepo.getUsers();
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('사용자 목록을 불러오지 못했습니다.')));
      }
      return;
    }

    final currentMemberIds = currentMembers.map((m) => m['userId']).toSet();
    final candidates = users.where((u) => !currentMemberIds.contains(u.id)).toList();
    Set<String> selectedUserIds = {};
    int pastMessageHours = 0;

    if (context.mounted) {
      showDialog(
        context: context,
        builder: (ctx) => StatefulBuilder(
          builder: (context, setState) {
            return AlertDialog(
              title: const Text('사용자 초대'),
              content: SizedBox(
                width: double.maxFinite,
                child: candidates.isEmpty
                    ? const Text('초대할 수 있는 사용자가 없습니다.')
                    : Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Flexible(
                            child: ListView.builder(
                              shrinkWrap: true,
                              itemCount: candidates.length,
                              itemBuilder: (context, index) {
                                final u = candidates[index];
                                final isSelected = selectedUserIds.contains(u.id);
                                return CheckboxListTile(
                                  title: Text(u.username),
                                  subtitle: Text(u.role),
                                  value: isSelected,
                                  onChanged: (val) {
                                    setState(() {
                                      if (val == true) {
                                        selectedUserIds.add(u.id);
                                      } else {
                                        selectedUserIds.remove(u.id);
                                      }
                                    });
                                  },
                                );
                              },
                            ),
                          ),
                          const SizedBox(height: 16),
                          const Align(
                            alignment: Alignment.centerLeft,
                            child: Text('기존 메시지 공개 설정', style: TextStyle(fontSize: 13, fontWeight: FontWeight.bold)),
                          ),
                          DropdownButton<int>(
                            isExpanded: true,
                            value: pastMessageHours,
                            items: const [
                              DropdownMenuItem(value: 0, child: Text('안 보임 (기본)')),
                              DropdownMenuItem(value: 1, child: Text('최근 1시간')),
                              DropdownMenuItem(value: 24, child: Text('최근 24시간')),
                              DropdownMenuItem(value: 48, child: Text('최근 48시간')),
                            ],
                            onChanged: (val) {
                              if (val != null) {
                                setState(() { pastMessageHours = val; });
                              }
                            },
                          ),
                        ],
                      ),
              ),
              actions: [
                TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('취소')),
                ElevatedButton(
                  onPressed: selectedUserIds.isEmpty
                      ? null
                      : () async {
                          Navigator.pop(ctx);
                          final success = await ref.read(chatControllerProvider.notifier).inviteMembers(room.roomId, selectedUserIds.toList(), pastMessageHours: pastMessageHours);
                          if (context.mounted) {
                            ScaffoldMessenger.of(context).showSnackBar(
                              SnackBar(content: Text(success ? '초대 성공' : '초대 실패')),
                            );
                          }
                        },
                  child: const Text('초대'),
                ),
              ],
            );
          },
        ),
      );
    }
  }

  Future<void> _downloadFile(String urlPath, String? fileName) async {
    if (urlPath.isEmpty) return;
    try {
      final dio = ref.read(dioProvider);
      final baseUrl = dio.options.baseUrl;
      
      String finalUrl = urlPath;
      if (!urlPath.startsWith('http://') && !urlPath.startsWith('https://')) {
        finalUrl = '$baseUrl$urlPath';
      }
      
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('파일 다운로드를 시작합니다...')),
        );
      }

      final response = await dio.get<List<int>>(
        finalUrl,
        options: Options(responseType: ResponseType.bytes),
      );

      if (response.data != null) {
        final bytes = Uint8List.fromList(response.data!);
        final ext = fileName != null && fileName.contains('.') ? fileName.split('.').last : '';
        final name = fileName != null && fileName.contains('.') ? fileName.substring(0, fileName.lastIndexOf('.')) : (fileName ?? 'download');
        
        await FileSaver.instance.saveFile(
          name: name,
          bytes: bytes,
          fileExtension: ext,
          mimeType: MimeType.other,
        );
        
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('파일 다운로드가 완료되었습니다.')),
          );
        }
      } else {
        throw Exception('데이터를 받을 수 없습니다.');
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('파일 다운로드 중 오류가 발생했습니다.')),
        );
      }
    }
  }

  /// 메시지 버블 위젯 (messageType 분기)
  Widget _buildMessageBubble(ChatMessageModel msg, bool isMe) {
    final baseUrl = ref.read(dioProvider).options.baseUrl;
    String getFullUrl(String? url) {
      if (url == null || url.isEmpty) return '';
      if (url.startsWith('http://') || url.startsWith('https://')) return url;
      return '$baseUrl$url';
    }

    final bubbleColor = isMe ? const Color(0xFFFFEB33) : Colors.white;
    final borderRadius = BorderRadius.only(
      topLeft: const Radius.circular(14),
      topRight: const Radius.circular(14),
      bottomLeft: isMe ? const Radius.circular(14) : const Radius.circular(0),
      bottomRight: isMe ? const Radius.circular(0) : const Radius.circular(14),
    );

    switch (msg.messageType) {
      case 'IMAGE':
        final imageUrl = getFullUrl(msg.attachmentUrl ?? msg.content);
        return Container(
          decoration: BoxDecoration(borderRadius: borderRadius, boxShadow: [
            BoxShadow(color: Colors.black.withOpacity(0.08), blurRadius: 3, offset: const Offset(0, 1)),
          ]),
          child: ClipRRect(
            borderRadius: borderRadius,
            child: GestureDetector(
              onTap: () => _showImagePreview(context, imageUrl),
              child: AuthenticatedImage(
                url: imageUrl,
                width: 200,
                height: 150,
                fit: BoxFit.cover,
                borderRadius: BorderRadius.circular(8),
              ),
            ),
          ),
        );

      case 'FILE':
        final fileUrl = msg.attachmentUrl ?? msg.content;
        final rawUrl = getFullUrl(fileUrl);
        
        if (_isVideoFile(msg.fileName)) {
          return Container(
            margin: const EdgeInsets.symmetric(vertical: 2),
            decoration: BoxDecoration(borderRadius: borderRadius, boxShadow: [
              BoxShadow(color: Colors.black.withOpacity(0.08), blurRadius: 3, offset: const Offset(0, 1)),
            ]),
            child: ClipRRect(
              borderRadius: borderRadius,
              child: _VideoPlayerBubble(key: ValueKey(rawUrl), url: rawUrl, width: 250, height: 200),
            ),
          );
        }

        return GestureDetector(
          onTap: () => _downloadFile(fileUrl, msg.fileName),
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
            decoration: BoxDecoration(
              color: bubbleColor,
              borderRadius: borderRadius,
              boxShadow: [
                BoxShadow(color: Colors.black.withOpacity(0.08), blurRadius: 3, offset: const Offset(0, 1)),
              ],
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(_getFileIcon(msg.fileName), size: 28, color: const Color(0xFF3B5998)),
                const SizedBox(width: 10),
                Flexible(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        msg.fileName ?? '첨부파일',
                        style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 14),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                      const SizedBox(height: 2),
                      Text(
                        _formatFileSize(msg.fileSize),
                        style: TextStyle(fontSize: 11, color: Colors.grey[600]),
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: 8),
                Icon(Icons.download, size: 22, color: Colors.grey[600]),
              ],
            ),
          ),
        );

      case 'EMOJI':
        return Container(
          padding: const EdgeInsets.all(4),
          child: Text(msg.content, style: const TextStyle(fontSize: 36)),
        );

      case 'TEXT':
      default:
        final parsedTable = _parseTableContent(msg.content);
        if (parsedTable.isTable) {
          return _buildTableWidget(parsedTable, isMe);
        }

        return Container(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
          decoration: BoxDecoration(
            color: bubbleColor,
            borderRadius: borderRadius,
            boxShadow: [
              BoxShadow(color: Colors.black.withOpacity(0.08), blurRadius: 3, offset: const Offset(0, 1)),
            ],
          ),
          child: Text(
            msg.content,
            style: const TextStyle(fontSize: 15, color: Colors.black87, height: 1.3),
          ),
        );
    }
  }

  void _showImagePreview(BuildContext context, String imageUrl) {
    ImageViewerDialog.show(
      context,
      imageUrl: imageUrl,
      title: '이미지 미리보기',
    );
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(chatControllerProvider);
    final l10n = AppLocalizations.of(context);
    final authState = ref.watch(authControllerProvider);

    const backgroundColor = Color(0xFFB2C7D9);

    // 메시지가 업데이트될 때 스크롤
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients && state.activeMessages.isNotEmpty) {
        _scrollController.jumpTo(_scrollController.position.maxScrollExtent);
      }
    });

    return Scaffold(
      backgroundColor: backgroundColor,
      appBar: AppBar(
        title: Text(widget.roomTitle, style: const TextStyle(fontSize: 17, fontWeight: FontWeight.bold)),
        backgroundColor: const Color(0xFF3B5998),
        foregroundColor: Colors.white,
        elevation: 1,
        actions: [
          Builder(builder: (context) {
            final room = state.rooms.cast<ChatRoomModel?>().firstWhere((r) => r?.roomId == widget.roomId, orElse: () => null);
            if (room == null) return const SizedBox.shrink();

            final isCreator = room.createdBy == widget.currentUsername || room.createdBy == authState.value?.id;

            return Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                IconButton(
                  icon: const Icon(Icons.group),
                  onPressed: () => _showMembersDialog(context, room, isCreator),
                ),
                PopupMenuButton<String>(
                  icon: const Icon(Icons.settings),
                  onSelected: (value) async {
                    if (value == 'leave') {
                      final success = await ref.read(chatControllerProvider.notifier).leaveRoom(widget.roomId);
                      if (success && context.mounted) {
                        Navigator.pop(context);
                      }
                    } else if (value == 'delete') {
                      showDialog(
                        context: context,
                        builder: (ctx) => AlertDialog(
                          title: const Text('대화방 삭제'),
                          content: const Text('이 대화방과 모든 메시지를 영구적으로 삭제하시겠습니까?'),
                          actions: [
                            TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('취소')),
                            ElevatedButton(
                              onPressed: () async {
                                Navigator.pop(ctx);
                                final success = await ref.read(chatControllerProvider.notifier).deleteRoom(widget.roomId);
                                if (success && context.mounted) {
                                  Navigator.pop(context);
                                }
                              },
                              style: ElevatedButton.styleFrom(backgroundColor: Colors.redAccent, foregroundColor: Colors.white),
                              child: const Text('삭제'),
                            ),
                          ],
                        ),
                      );
                    }
                  },
                  itemBuilder: (BuildContext context) => <PopupMenuEntry<String>>[
                    const PopupMenuItem<String>(value: 'leave', child: Text('방 나가기')),
                    if (isCreator)
                      const PopupMenuItem<String>(
                        value: 'delete',
                        child: Text('대화방 삭제', style: TextStyle(color: Colors.red)),
                      ),
                  ],
                ),
              ],
            );
          }),
        ],
      ),
      body: Column(
        children: [
          Expanded(
            child: state.isLoadingMessages
                ? const Center(child: CircularProgressIndicator())
                : ListView.builder(
                    controller: _scrollController,
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 16),
                    itemCount: state.activeMessages.length,
                    itemBuilder: (context, index) {
                      final ChatMessageModel msg = state.activeMessages[index];
                      final bool isMe = _isMyMessage(msg);

                      // 규칙: 날짜/시간 필수 쿠키/설정 오프셋 적용 (KST 기준 +9)
                      final String timeText = msg.timestamp != null
                          ? DateHelper.formatWithOffset(msg.timestamp, 9, pattern: 'a h:mm')
                          : '';

                      // 규칙: 무의미한 Raw UUID 화면 노출 금지
                      final displaySender = msg.senderName.contains('-') && msg.senderName.length > 20
                          ? UuidFormatter.format(msg.senderName, prefix: 'USER')
                          : msg.senderName;

                      return Column(
                        children: [
                          // #3: 날짜 구분선 (탭 시 날짜 북마크 이동 모달)
                          if (_shouldShowDateSeparator(state.activeMessages, index))
                            Padding(
                              padding: const EdgeInsets.symmetric(vertical: 12),
                              child: Row(
                                children: [
                                  Expanded(child: Divider(color: Colors.grey[400], thickness: 0.5)),
                                  Material(
                                    color: Colors.transparent,
                                    child: InkWell(
                                      onTap: () => _showDateBookmarkModal(context, state.activeMessages),
                                      borderRadius: BorderRadius.circular(20),
                                      child: Container(
                                        key: _dateKeys.putIfAbsent(_getDateKey(msg.timestamp), () => GlobalKey()),
                                        margin: const EdgeInsets.symmetric(horizontal: 12),
                                        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 5),
                                        decoration: BoxDecoration(
                                          color: Colors.white.withOpacity(0.9),
                                          borderRadius: BorderRadius.circular(20),
                                          border: Border.all(color: Colors.grey.shade300, width: 0.8),
                                          boxShadow: [
                                            BoxShadow(
                                              color: Colors.black.withOpacity(0.04),
                                              blurRadius: 4,
                                              offset: const Offset(0, 1),
                                            ),
                                          ],
                                        ),
                                        child: Row(
                                          mainAxisSize: MainAxisSize.min,
                                          children: [
                                            Text(
                                              '📅 ${_formatDateSeparator(msg.timestamp)}',
                                              style: TextStyle(fontSize: 12, color: Colors.grey[800], fontWeight: FontWeight.w600),
                                            ),
                                            const SizedBox(width: 4),
                                            Icon(Icons.unfold_more, size: 14, color: Colors.grey[600]),
                                          ],
                                        ),
                                      ),
                                    ),
                                  ),
                                  Expanded(child: Divider(color: Colors.grey[400], thickness: 0.5)),
                                ],
                              ),
                            ),

                          // 시스템 메시지 (입장/퇴장 등)
                          if (['SYSTEM', 'LEAVE', 'JOIN', 'INFO'].contains(msg.messageType))
                            Padding(
                              padding: const EdgeInsets.symmetric(vertical: 8),
                              child: Center(
                                child: Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
                                  decoration: BoxDecoration(
                                    color: Colors.grey[300],
                                    borderRadius: BorderRadius.circular(16),
                                  ),
                                  child: Text(
                                    _getSystemMessageText(msg, l10n),
                                    style: TextStyle(fontSize: 12, color: Colors.grey[700], fontWeight: FontWeight.w600),
                                    textAlign: TextAlign.center,
                                  ),
                                ),
                              ),
                            )
                          else
                            // 일반 메시지 버블
                            GestureDetector(
                              onLongPress: () => _showContextMenu(context, msg, l10n),
                              child: Padding(
                                padding: const EdgeInsets.only(bottom: 12),
                                child: Column(
                                  crossAxisAlignment: isMe ? CrossAxisAlignment.end : CrossAxisAlignment.start,
                                  children: [
                                    if (!isMe) ...[
                                      Text(
                                        displaySender,
                                        style: const TextStyle(fontSize: 12, color: Colors.black87, fontWeight: FontWeight.w600),
                                      ),
                                      const SizedBox(height: 4),
                                    ],
                                    Row(
                                      mainAxisAlignment: isMe ? MainAxisAlignment.end : MainAxisAlignment.start,
                                      crossAxisAlignment: CrossAxisAlignment.end,
                                      children: [
                                        if (isMe) ...[
                                          // unread count
                                          Column(
                                            crossAxisAlignment: CrossAxisAlignment.end,
                                            mainAxisSize: MainAxisSize.min,
                                            children: [
                                              if (msg.unreadCount > 0)
                                                Padding(
                                                  padding: const EdgeInsets.only(bottom: 2),
                                                  child: Text(
                                                    '${msg.unreadCount}',
                                                    style: const TextStyle(fontSize: 11, color: Color(0xFFF59E0B), fontWeight: FontWeight.w800),
                                                  ),
                                                ),
                                              Text(timeText, style: const TextStyle(fontSize: 10, color: Colors.black54)),
                                            ],
                                          ),
                                          const SizedBox(width: 6),
                                        ],
                                        Flexible(child: _buildMessageBubble(msg, isMe)),
                                        if (!isMe) ...[
                                          const SizedBox(width: 6),
                                          Column(
                                            crossAxisAlignment: CrossAxisAlignment.start,
                                            mainAxisSize: MainAxisSize.min,
                                            children: [
                                              if (msg.unreadCount > 0)
                                                Padding(
                                                  padding: const EdgeInsets.only(bottom: 2),
                                                  child: Text(
                                                    '${msg.unreadCount}',
                                                    style: const TextStyle(fontSize: 11, color: Color(0xFFF59E0B), fontWeight: FontWeight.w800),
                                                  ),
                                                ),
                                              Text(timeText, style: const TextStyle(fontSize: 10, color: Colors.black54)),
                                            ],
                                          ),
                                        ],
                                      ],
                                    ),
                                  ],
                                ),
                              ),
                            ),
                        ],
                      );
                    },
                  ),
          ),

          // #4: 이모지 빠른 전송 바 + 입력 영역
          Container(
            color: Colors.white,
            child: Column(
              children: [
                // Quick Emoji Bar
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                  decoration: BoxDecoration(
                    border: Border(top: BorderSide(color: Colors.grey[200]!)),
                  ),
                  child: Row(
                    children: _quickEmojis.map((emoji) => Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 4),
                      child: GestureDetector(
                        onTap: () => _sendEmoji(emoji),
                        child: Text(emoji, style: const TextStyle(fontSize: 22)),
                      ),
                    )).toList(),
                  ),
                ),
                // Input Bar
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 6),
                  child: Row(
                    children: [
                      IconButton(
                        icon: const Icon(Icons.attach_file, color: Color(0xFF3B5998), size: 26),
                        onPressed: _pickAndSendFile,
                      ),
                      Expanded(
                        child: Actions(
                          actions: {
                            PasteTextIntent: CallbackAction<PasteTextIntent>(
                              onInvoke: (PasteTextIntent intent) async {
                                await _handlePaste();
                                return null;
                              },
                            ),
                          },
                          child: TextField(
                            controller: _messageController,
                            minLines: 1,
                            maxLines: 4,
                            decoration: InputDecoration(
                              hintText: l10n.requestInfo,
                              filled: true,
                              fillColor: Colors.grey[100],
                              contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                              border: OutlineInputBorder(borderRadius: BorderRadius.circular(20), borderSide: BorderSide.none),
                            ),
                            onSubmitted: (_) => _sendMessage(),
                          ),
                        ),
                      ),
                      const SizedBox(width: 4),
                      IconButton(
                        icon: Icon(
                          Icons.send_rounded,
                          color: state.isSending ? Colors.grey : const Color(0xFF3B5998),
                          size: 28,
                        ),
                        onPressed: state.isSending ? null : _sendMessage,
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

bool _isVideoFile(String? fileName) {
  if (fileName == null) return false;
  final ext = fileName.toLowerCase();
  return ext.endsWith('.mp4') || ext.endsWith('.mov') || ext.endsWith('.webm') || ext.endsWith('.ogg') || ext.endsWith('.mkv');
}

class _VideoPlayerBubble extends ConsumerStatefulWidget {
  final String url;
  final double width;
  final double height;
  
  const _VideoPlayerBubble({required this.url, required this.width, required this.height, Key? key}) : super(key: key);

  @override
  ConsumerState<_VideoPlayerBubble> createState() => _VideoPlayerBubbleState();
}

class _VideoPlayerBubbleState extends ConsumerState<_VideoPlayerBubble> {
  VideoPlayerController? _controller;
  bool _initialized = false;
  bool _error = false;
  String _urlWithToken = '';

  @override
  void initState() {
    super.initState();
    _initializeVideo();
  }
  
  Future<void> _initializeVideo() async {
    try {
      final token = await ref.read(storageServiceProvider).getAccessToken() ?? '';
      final separator = widget.url.contains('?') ? '&' : '?';
      final urlWithToken = '${widget.url}$separator' 'token=$token';
      
      if (mounted) {
        setState(() {
          _urlWithToken = urlWithToken;
        });
      }

      if (!kIsWeb) {
        _controller = VideoPlayerController.networkUrl(
          Uri.parse(urlWithToken),
          httpHeaders: {'Authorization': 'Bearer $token'},
        );
        await _controller!.initialize();
        if (mounted) {
          setState(() {
            _initialized = true;
          });
        }
      } else {
        if (mounted) {
          setState(() {
            _initialized = true;
          });
        }
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _error = true;
        });
      }
    }
  }

  @override
  void dispose() {
    _controller?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (kIsWeb && _urlWithToken.isNotEmpty) {
      return buildWebVideoPlayer(_urlWithToken, widget.width, widget.height);
    }

    if (_error) {
      return Container(
        width: widget.width,
        height: widget.height,
        color: Colors.black87,
        child: const Center(child: Icon(Icons.error_outline, color: Colors.white70, size: 36)),
      );
    }
    
    if (!_initialized || _controller == null) {
      return Container(
        width: widget.width,
        height: widget.height,
        color: Colors.black87,
        child: const Center(child: CircularProgressIndicator(color: Colors.white54)),
      );
    }

    return Container(
      width: widget.width,
      height: widget.height,
      color: Colors.black,
      child: Stack(
        alignment: Alignment.center,
        children: [
          AspectRatio(
            aspectRatio: _controller!.value.aspectRatio,
            child: VideoPlayer(_controller!),
          ),
          IconButton(
            icon: Icon(
              _controller!.value.isPlaying ? Icons.pause_circle_filled : Icons.play_circle_fill,
              color: Colors.white.withOpacity(0.8),
              size: 50,
            ),
            onPressed: () {
              setState(() {
                _controller!.value.isPlaying ? _controller!.pause() : _controller!.play();
              });
            },
          ),
          Positioned(
            bottom: 0,
            left: 0,
            right: 0,
            child: VideoProgressIndicator(
              _controller!,
              allowScrubbing: true,
              colors: const VideoProgressColors(
                playedColor: Colors.blueAccent,
                backgroundColor: Colors.white24,
                bufferedColor: Colors.white54,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class ParsedTable {
  final bool isTable;
  final List<String> headers;
  final List<List<String>> rows;
  ParsedTable(this.isTable, this.headers, this.rows);
}

ParsedTable _parseTableContent(String content) {
  if (content.isEmpty) return ParsedTable(false, [], []);
  
  final lines = content.trim().split('\n').where((l) => l.trim().isNotEmpty).toList();
  if (lines.isEmpty) return ParsedTable(false, [], []);
  
  // Case 1: Markdown table
  if (lines[0].contains('|') && lines.length >= 2) {
    List<String> cleanLine(String l) {
      final parts = l.split('|').map((s) => s.trim()).toList();
      if (parts.isNotEmpty && parts.first.isEmpty) parts.removeAt(0);
      if (parts.isNotEmpty && parts.last.isEmpty) parts.removeLast();
      return parts;
    }
    
    final headers = cleanLine(lines[0]);
    var dataLines = lines.sublist(1);
    if (dataLines.isNotEmpty && dataLines[0].contains('---')) {
      dataLines = dataLines.sublist(1);
    }
    final rows = dataLines.map((l) => cleanLine(l)).toList();
    if (headers.isNotEmpty && rows.isNotEmpty) {
      return ParsedTable(true, headers, rows);
    }
  }
  
  // Case 2: TSV (Excel copy paste)
  if (lines[0].contains('\t') && lines.length >= 1) {
    final headers = lines[0].split('\t').map((s) => s.trim()).toList();
    final rows = lines.sublist(1).map((l) => l.split('\t').map((s) => s.trim()).toList()).toList();
    if (headers.length >= 2 || rows.isNotEmpty) {
      return ParsedTable(true, headers, rows);
    }
  }
  
  // Case 3: CSV
  if (lines[0].contains(',') && lines.length >= 2) {
    final headers = lines[0].split(',').map((s) => s.trim()).toList();
    if (headers.length >= 2) {
      final rows = lines.sublist(1).map((l) => l.split(',').map((s) => s.trim()).toList()).toList();
      return ParsedTable(true, headers, rows);
    }
  }
  
  return ParsedTable(false, [], []);
}

String _generateTsv(ParsedTable table) {
  final sb = StringBuffer();
  sb.writeln(table.headers.join('\t'));
  for (final row in table.rows) {
    sb.writeln(row.join('\t'));
  }
  return sb.toString();
}

Widget _buildTableWidget(ParsedTable table, bool isMe) {
  final headerColor = isMe ? const Color(0xFF3B5998) : const Color(0xFFE5E7EB);
  final headerTextColor = isMe ? Colors.white : Colors.black87;
  final rowBgColor = isMe ? const Color(0xFFE8EAF6) : Colors.white;
  
  return Builder(
    builder: (context) {
      return Container(
        margin: const EdgeInsets.symmetric(vertical: 4),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: Colors.grey.withOpacity(0.3)),
          color: rowBgColor,
        ),
        clipBehavior: Clip.antiAlias,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Top toolbar (Copy Data button)
            Container(
              color: headerColor,
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text('표 데이터', style: TextStyle(color: headerTextColor, fontWeight: FontWeight.bold, fontSize: 12)),
                  const SizedBox(width: 16),
                  Row(
                    children: [
                      InkWell(
                        onTap: () {
                          _showTablePreview(context, table);
                        },
                        child: Row(
                          children: [
                            Icon(Icons.zoom_in, size: 14, color: headerTextColor),
                            const SizedBox(width: 4),
                            Text('크게 보기', style: TextStyle(color: headerTextColor, fontSize: 12)),
                          ],
                        ),
                      ),
                      const SizedBox(width: 12),
                      InkWell(
                        onTap: () {
                          Clipboard.setData(ClipboardData(text: _generateTsv(table)));
                          ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('표 데이터가 복사되었습니다.')));
                        },
                        child: Row(
                          children: [
                            Icon(Icons.copy, size: 14, color: headerTextColor),
                            const SizedBox(width: 4),
                            Text('복사', style: TextStyle(color: headerTextColor, fontSize: 12)),
                          ],
                        ),
                      ),
                    ],
                  )
                ],
              ),
            ),
            SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: DataTable(
                headingRowColor: WidgetStateProperty.all(headerColor.withOpacity(0.8)),
                dataRowColor: WidgetStateProperty.all(rowBgColor),
                headingTextStyle: TextStyle(fontWeight: FontWeight.bold, color: headerTextColor, fontSize: 13),
                dataTextStyle: const TextStyle(color: Colors.black87, fontSize: 12),
                columnSpacing: 16,
                headingRowHeight: 36,
                dataRowMinHeight: 32,
                dataRowMaxHeight: 32,
                columns: table.headers.map((h) => DataColumn(label: Text(h))).toList(),
                rows: table.rows.map((r) {
                  return DataRow(
                    cells: List.generate(table.headers.length, (idx) {
                      final cellStr = idx < r.length ? r[idx] : '';
                      return DataCell(Text(cellStr));
                    }),
                  );
                }).toList(),
              ),
            ),
          ],
        ),
      );
    }
  );
}

void _showTablePreview(BuildContext context, ParsedTable table) {
  showDialog(
    context: context,
    builder: (ctx) {
      return Dialog(
        insetPadding: const EdgeInsets.all(24),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        child: Container(
          padding: const EdgeInsets.all(16),
          width: double.maxFinite,
          constraints: BoxConstraints(maxHeight: MediaQuery.of(context).size.height * 0.8),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text('표 데이터 크게 보기', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                  IconButton(
                    icon: const Icon(Icons.close),
                    onPressed: () => Navigator.pop(ctx),
                  ),
                ],
              ),
              const Divider(),
              Expanded(
                child: SingleChildScrollView(
                  scrollDirection: Axis.vertical,
                  child: SingleChildScrollView(
                    scrollDirection: Axis.horizontal,
                    child: DataTable(
                      headingRowColor: WidgetStateProperty.all(Colors.grey[200]),
                      headingTextStyle: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black, fontSize: 14),
                      dataTextStyle: const TextStyle(color: Colors.black87, fontSize: 13),
                      columnSpacing: 24,
                      columns: table.headers.map((h) => DataColumn(label: Text(h))).toList(),
                      rows: table.rows.map((r) {
                        return DataRow(
                          cells: List.generate(table.headers.length, (idx) {
                            final cellStr = idx < r.length ? r[idx] : '';
                            return DataCell(Text(cellStr));
                          }),
                        );
                      }).toList(),
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 16),
              Align(
                alignment: Alignment.centerRight,
                child: ElevatedButton.icon(
                  onPressed: () {
                    Clipboard.setData(ClipboardData(text: _generateTsv(table)));
                    ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('표 데이터가 복사되었습니다.')));
                  },
                  icon: const Icon(Icons.copy, size: 16),
                  label: const Text('복사하기'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF3B5998),
                    foregroundColor: Colors.white,
                  ),
                ),
              )
            ],
          ),
        ),
      );
    },
  );
}

