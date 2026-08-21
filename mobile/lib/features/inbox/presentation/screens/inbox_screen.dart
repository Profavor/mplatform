import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/features/inbox/presentation/providers/inbox_provider.dart';
import 'package:mplatform_mobile/features/inbox/presentation/screens/inbox_compose_screen.dart';
import 'package:mplatform_mobile/features/inbox/presentation/screens/inbox_detail_screen.dart';
import 'package:mplatform_mobile/features/inbox/presentation/widgets/inbox_folder_selector.dart';
import 'package:mplatform_mobile/features/inbox/presentation/widgets/inbox_message_card.dart';

class InboxScreen extends ConsumerStatefulWidget {
  const InboxScreen({super.key});

  @override
  ConsumerState<InboxScreen> createState() => _InboxScreenState();
}

class _InboxScreenState extends ConsumerState<InboxScreen> {
  final TextEditingController _searchController = TextEditingController();
  final ScrollController _scrollController = ScrollController();
  bool _isSearching = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      ref.read(inboxControllerProvider.notifier).init();
    });

    _scrollController.addListener(() {
      if (_scrollController.position.pixels >=
          _scrollController.position.maxScrollExtent - 200) {
        ref.read(inboxControllerProvider.notifier).loadMessages();
      }
    });
  }

  @override
  void dispose() {
    _searchController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final state = ref.watch(inboxControllerProvider);
    final notifier = ref.read(inboxControllerProvider.notifier);

    return Scaffold(
      appBar: AppBar(
        title: _isSearching
            ? TextField(
                controller: _searchController,
                autofocus: true,
                decoration: InputDecoration(
                  hintText: l10n.searchPlaceholder,
                  border: InputBorder.none,
                  hintStyle: const TextStyle(color: Colors.white70),
                ),
                style: const TextStyle(color: Colors.white),
                onSubmitted: (query) {
                  notifier.search(query);
                },
              )
            : Text(l10n.inboxTitle),
        actions: [
          IconButton(
            icon: Icon(_isSearching ? Icons.close : Icons.search),
            onPressed: () {
              setState(() {
                if (_isSearching) {
                  _searchController.clear();
                  notifier.search('');
                  _isSearching = false;
                } else {
                  _isSearching = true;
                }
              });
            },
          ),
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () {
              notifier.init();
            },
          ),
        ],
      ),
      body: Column(
        children: [
          const InboxFolderSelector(),
          const Divider(height: 1),
          Expanded(
            child: RefreshIndicator(
              onRefresh: () => notifier.init(),
              child: state.messages.isEmpty && !state.isLoading
                  ? Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.mail_outline, size: 64, color: Colors.grey[400]),
                          const SizedBox(height: 12),
                          Text(
                            l10n.noMessages,
                            style: TextStyle(
                              fontSize: 16,
                              color: Colors.grey[600],
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                        ],
                      ),
                    )
                  : ListView.builder(
                      controller: _scrollController,
                      physics: const AlwaysScrollableScrollPhysics(),
                      itemCount: state.messages.length + (state.isLoading ? 1 : 0),
                      itemBuilder: (context, index) {
                        if (index == state.messages.length) {
                          return const Padding(
                            padding: EdgeInsets.symmetric(vertical: 16),
                            child: Center(child: CircularProgressIndicator()),
                          );
                        }

                        final message = state.messages[index];

                        return Dismissible(
                          key: Key('inbox_msg_${message.id}'),
                          direction: DismissDirection.endToStart,
                          background: Container(
                            alignment: Alignment.centerRight,
                            padding: const EdgeInsets.only(right: 20),
                            color: Colors.red[400],
                            child: const Icon(Icons.delete, color: Colors.white),
                          ),
                          onDismissed: (_) {
                            notifier.deleteMessage(message.id);
                          },
                          child: InboxMessageCard(
                            message: message,
                            onTap: () {
                              notifier.toggleRead(message.id, true);
                              Navigator.push(
                                context,
                                MaterialPageRoute(
                                  builder: (_) => InboxDetailScreen(messageId: message.id),
                                ),
                              );
                            },
                          ),
                        );
                      },
                    ),
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (_) => const InboxComposeScreen(),
            ),
          );
        },
        icon: const Icon(Icons.edit),
        label: Text(l10n.compose),
      ),
    );
  }
}
