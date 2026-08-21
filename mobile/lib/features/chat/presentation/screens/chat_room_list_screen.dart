import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/core/utils/date_helper.dart';
import 'package:mplatform_mobile/core/utils/uuid_formatter.dart';
import 'package:mplatform_mobile/features/auth/presentation/providers/auth_provider.dart';
import 'package:mplatform_mobile/features/auth/domain/models/user_model.dart';
import 'package:mplatform_mobile/features/chat/domain/models/chat_room_model.dart';
import 'package:mplatform_mobile/features/chat/presentation/providers/chat_provider.dart';
import 'package:mplatform_mobile/features/chat/presentation/screens/chat_screen.dart';
import 'package:mplatform_mobile/features/chat/presentation/widgets/user_selection_dialog.dart';

class ChatRoomListScreen extends ConsumerStatefulWidget {
  const ChatRoomListScreen({super.key});

  @override
  ConsumerState<ChatRoomListScreen> createState() => _ChatRoomListScreenState();
}

class _ChatRoomListScreenState extends ConsumerState<ChatRoomListScreen> {
  final TextEditingController _roomTitleController = TextEditingController();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) {
        ref.read(chatControllerProvider.notifier).loadRooms();
        ref.read(chatControllerProvider.notifier).fetchTotalUnreadCount();
      }
    });
  }

  @override
  void dispose() {
    _roomTitleController.dispose();
    super.dispose();
  }

  void _showCreateRoomDialog(BuildContext context, AppLocalizations l10n, String currentUsername) {
    _roomTitleController.clear();
    showDialog(
      context: context,
      builder: (ctx) => _CreateRoomDialog(
        titleController: _roomTitleController,
        l10n: l10n,
        currentUsername: currentUsername,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final state = ref.watch(chatControllerProvider);
    final authState = ref.watch(authControllerProvider);
    final String currentUsername = authState.value?.username ?? 'my_account';

    return Scaffold(
      appBar: AppBar(
        title: Row(
          children: [
            Text(l10n.chatTitle),
            if (state.totalUnreadCount > 0) ...[
              const SizedBox(width: 8),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 7, vertical: 2),
                decoration: BoxDecoration(
                  color: Colors.redAccent,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Text(
                  state.totalUnreadCount > 99 ? '99+' : '${state.totalUnreadCount}',
                  style: const TextStyle(color: Colors.white, fontSize: 11, fontWeight: FontWeight.bold),
                ),
              ),
            ],
          ],
        ),
        backgroundColor: const Color(0xFF3B5998),
        foregroundColor: Colors.white,
        elevation: 1,
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _showCreateRoomDialog(context, l10n, currentUsername),
        backgroundColor: const Color(0xFF3B5998),
        foregroundColor: Colors.white,
        icon: const Icon(Icons.add_comment),
        label: Text(l10n.chatCreateRoom, style: const TextStyle(fontWeight: FontWeight.bold)),
      ),
      body: state.isLoadingRooms
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: () async {
                ref.read(chatControllerProvider.notifier).loadRooms();
                ref.read(chatControllerProvider.notifier).fetchTotalUnreadCount();
              },
              child: state.rooms.isEmpty
                  ? CustomScrollView(
                      physics: const AlwaysScrollableScrollPhysics(),
                      slivers: [
                        SliverFillRemaining(
                          child: Center(
                            child: Text(
                              l10n.chatEmptyRooms,
                              style: TextStyle(color: Colors.grey[600], fontSize: 16),
                            ),
                          ),
                        ),
                      ],
                    )
                  : ListView.separated(
                      physics: const AlwaysScrollableScrollPhysics(),
                      itemCount: state.rooms.length,
                      separatorBuilder: (ctx, index) => const Divider(height: 1, indent: 72),
                  itemBuilder: (context, index) {
                    final ChatRoomModel room = state.rooms[index];
                    // 규칙: 무의미한 raw UUID 노출 금지. ROOM-xxxxxx 형태로 식별 코드 치환 또는 사용자/시스템 명칭 표출
                    final String displayCode = UuidFormatter.format(room.roomId, prefix: 'ROOM');
                    final String timeText = room.updatedAt != null
                        ? DateHelper.formatWithOffset(room.updatedAt, 9, pattern: 'a h:mm')
                        : '';

                    return ListTile(
                      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                      leading: CircleAvatar(
                        radius: 26,
                        backgroundColor: Colors.indigo[100],
                        child: const Icon(Icons.forum_rounded, color: Color(0xFF3B5998), size: 28),
                      ),
                      title: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Expanded(
                            child: Text(
                              room.title.isNotEmpty ? room.title : displayCode,
                              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                          ),
                          Text(
                            timeText,
                            style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                          ),
                        ],
                      ),
                      subtitle: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Expanded(
                            child: Padding(
                              padding: const EdgeInsets.only(top: 4.0),
                              child: Text(
                                room.lastMessage ?? displayCode,
                                style: TextStyle(color: Colors.grey[700], fontSize: 13),
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                              ),
                            ),
                          ),
                          if (room.unreadCount > 0)
                            Container(
                              margin: const EdgeInsets.only(left: 8),
                              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                              decoration: BoxDecoration(
                                color: Colors.redAccent,
                                borderRadius: BorderRadius.circular(12),
                              ),
                              child: Text(
                                '${room.unreadCount}',
                                style: const TextStyle(color: Colors.white, fontSize: 11, fontWeight: FontWeight.bold),
                              ),
                            ),
                        ],
                      ),
                      onTap: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (_) => ChatScreen(
                              roomId: room.roomId,
                              roomTitle: room.title.isNotEmpty ? room.title : displayCode,
                              currentUsername: currentUsername,
                            ),
                          ),
                        );
                      },
                    );
                  },
                ),
            ),
    );
  }
}

class _CreateRoomDialog extends ConsumerStatefulWidget {
  final TextEditingController titleController;
  final AppLocalizations l10n;
  final String currentUsername;

  const _CreateRoomDialog({
    required this.titleController,
    required this.l10n,
    required this.currentUsername,
  });

  @override
  ConsumerState<_CreateRoomDialog> createState() => _CreateRoomDialogState();
}

class _CreateRoomDialogState extends ConsumerState<_CreateRoomDialog> {
  List<UserModel> _users = [];
  final Set<String> _selectedUserIds = {};
  bool _isLoading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadUsers();
  }

  Future<void> _loadUsers() async {
    try {
      final repo = ref.read(authRepositoryProvider);
      final users = await repo.getUsers();
      if (mounted) {
        setState(() {
          _users = users;
          _isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _error = 'Failed to load users: $e';
          _isLoading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text(widget.l10n.chatCreateRoom, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
      content: SizedBox(
        width: double.maxFinite,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: widget.titleController,
              decoration: InputDecoration(
                hintText: widget.l10n.chatRoomTitlePlaceholder,
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
                contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
              ),
              autofocus: true,
            ),
            const SizedBox(height: 16),
            Align(
              alignment: Alignment.centerLeft,
              child: Text(
                widget.l10n.chatSelectMembers,
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
              ),
            ),
            const SizedBox(height: 8),
            if (_isLoading)
              const Padding(
                padding: EdgeInsets.all(16.0),
                child: Center(child: CircularProgressIndicator()),
              )
            else if (_error != null)
              Padding(
                padding: const EdgeInsets.all(16.0),
                child: Text(_error!, style: const TextStyle(color: Colors.red)),
              )
            else
              Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Wrap(
                    spacing: 12.0,
                    runSpacing: 8.0,
                    crossAxisAlignment: WrapCrossAlignment.center,
                    children: [
                      OutlinedButton.icon(
                        onPressed: () async {
                          final selectedIds = await showDialog<Set<String>>(
                            context: context,
                            builder: (context) => UserSelectionDialog(
                              users: _users,
                              initialSelectedIds: _selectedUserIds,
                              l10n: widget.l10n,
                              currentUsername: widget.currentUsername,
                            ),
                          );
                          if (selectedIds != null) {
                            setState(() {
                              _selectedUserIds.clear();
                              _selectedUserIds.addAll(selectedIds);
                            });
                          }
                        },
                        icon: const Icon(Icons.search, size: 18),
                        label: Text(widget.l10n.chatSearchSelectUser),
                        style: OutlinedButton.styleFrom(
                          foregroundColor: const Color(0xFF192A56),
                          side: const BorderSide(color: Color(0xFF192A56)),
                        ),
                      ),
                      if (_selectedUserIds.isNotEmpty)
                        Wrap(
                          spacing: 8.0,
                          runSpacing: 4.0,
                          children: _selectedUserIds.map((id) {
                            final user = _users.firstWhere((u) => u.id == id, orElse: () => _users.first);
                            final isMe = user.username == widget.currentUsername;
                            return Chip(
                              label: Text(isMe ? widget.l10n.chatUserMe(user.username) : user.username, style: const TextStyle(fontSize: 12)),
                              deleteIcon: const Icon(Icons.close, size: 14),
                              onDeleted: () {
                                setState(() {
                                  _selectedUserIds.remove(id);
                                });
                              },
                            );
                          }).toList(),
                        ),
                      if (_selectedUserIds.isEmpty)
                        Text(widget.l10n.chatNoUserSelected, style: const TextStyle(color: Colors.grey, fontSize: 13)),
                    ],
                  ),
                ],
              ),
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: Text(widget.l10n.cancel, style: TextStyle(color: Colors.grey[600])),
        ),
        ElevatedButton(
          onPressed: _selectedUserIds.isEmpty
              ? null
              : () async {
                  final title = widget.titleController.text.trim();
                  // 제목이 비어있어도 대화방이 생성될 수 있게 허용 (이름 자동생성 등 백엔드 처리)
                  final success = await ref.read(chatControllerProvider.notifier).createRoom(title, _selectedUserIds.toList());
                  if (context.mounted) {
                    Navigator.pop(context);
                    if (!success) {
                      final error = ref.read(chatControllerProvider).errorMessage;
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(
                          content: Text(error ?? widget.l10n.chatCreateRoomFailed),
                          backgroundColor: Colors.redAccent,
                        ),
                      );
                    }
                  }
                },
          style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF3B5998), foregroundColor: Colors.white),
          child: Text(widget.l10n.confirm),
        ),
      ],
    );
  }
}

