import 'package:flutter/material.dart';
import 'dart:convert';
import 'package:mplatform_mobile/features/auth/domain/models/user_model.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';

class UserSelectionDialog extends StatefulWidget {
  final List<UserModel> users;
  final Set<String> initialSelectedIds;
  final AppLocalizations l10n;
  final String currentUsername;

  const UserSelectionDialog({
    super.key,
    required this.users,
    required this.initialSelectedIds,
    required this.l10n,
    required this.currentUsername,
  });

  @override
  State<UserSelectionDialog> createState() => _UserSelectionDialogState();
}

class _UserSelectionDialogState extends State<UserSelectionDialog> {
  final TextEditingController _searchController = TextEditingController();
  late Set<String> _selectedIds;
  late List<UserModel> _filteredUsers;

  @override
  void initState() {
    super.initState();
    _selectedIds = Set.from(widget.initialSelectedIds);
    _filteredUsers = List.from(widget.users);
    _searchController.addListener(_onSearchChanged);
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  void _onSearchChanged() {
    final query = _searchController.text.toLowerCase();
    setState(() {
      _filteredUsers = widget.users.where((user) {
        final usernameMatch = user.username.toLowerCase().contains(query);
        final roleMatch = user.role.toLowerCase().contains(query);
        final org = _getLocalizedText(user.orgName).toLowerCase();
        final dept = _getLocalizedText(user.deptName).toLowerCase();
        return usernameMatch || roleMatch || org.contains(query) || dept.contains(query);
      }).toList();
    });
  }

  String _getLocalizedText(String? jsonString) {
    if (jsonString == null || jsonString.isEmpty) return '';
    try {
      final decoded = jsonDecode(jsonString);
      if (decoded is Map) {
        // default to ko, then en
        return decoded['ko']?.toString() ?? decoded['en']?.toString() ?? jsonString;
      }
    } catch (_) {}
    return jsonString;
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text(widget.l10n.chatSearchSelectUser, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18)),
      content: SizedBox(
        width: double.maxFinite,
        height: MediaQuery.of(context).size.height * 0.6,
        child: Column(
          children: [
            TextField(
              controller: _searchController,
              decoration: InputDecoration(
                hintText: widget.l10n.chatSearchUserHint,
                prefixIcon: const Icon(Icons.search),
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
                contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
              ),
            ),
            const SizedBox(height: 12),
            Expanded(
              child: ListView.builder(
                itemCount: _filteredUsers.length,
                itemBuilder: (context, index) {
                  final user = _filteredUsers[index];
                  final isMe = user.username == widget.currentUsername;
                  final org = _getLocalizedText(user.orgName);
                  final dept = _getLocalizedText(user.deptName);
                  final subtitle = [dept, org].where((e) => e.isNotEmpty).join(' / ');

                  return CheckboxListTile(
                    value: _selectedIds.contains(user.id),
                    title: Row(
                      children: [
                        const Icon(Icons.person, size: 16, color: Colors.grey),
                        const SizedBox(width: 4),
                        Text(isMe ? widget.l10n.chatUserMe(user.username) : user.username, style: const TextStyle(fontWeight: FontWeight.bold)),
                      ],
                    ),
                    subtitle: subtitle.isNotEmpty ? Text(subtitle, style: const TextStyle(fontSize: 12, color: Colors.grey)) : null,
                    secondary: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                      decoration: BoxDecoration(
                        color: Colors.blue.withOpacity(0.1),
                        border: Border.all(color: Colors.blue.withOpacity(0.2)),
                        borderRadius: BorderRadius.circular(4),
                      ),
                      child: Text(user.role, style: const TextStyle(fontSize: 10, color: Colors.blue, fontWeight: FontWeight.bold)),
                    ),
                    onChanged: (bool? checked) {
                      setState(() {
                        if (checked == true) {
                          _selectedIds.add(user.id);
                        } else {
                          _selectedIds.remove(user.id);
                        }
                      });
                    },
                  );
                },
              ),
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
          onPressed: () => Navigator.pop(context, _selectedIds),
          style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF192A56)),
          child: Text(widget.l10n.chatConfirmBtn, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
        ),
      ],
    );
  }
}
