import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/core/utils/date_helper.dart';
import 'package:mplatform_mobile/core/utils/l10n_helper.dart';
import 'package:mplatform_mobile/core/utils/uuid_formatter.dart';
import 'package:mplatform_mobile/features/approvals/domain/models/approval_item.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/features/approvals/presentation/providers/approvals_provider.dart';
import 'package:mplatform_mobile/features/approvals/presentation/screens/approval_detail_screen.dart';

class ApprovalsListScreen extends ConsumerStatefulWidget {
  const ApprovalsListScreen({super.key});

  @override
  ConsumerState<ApprovalsListScreen> createState() => _ApprovalsListScreenState();
}

class _ApprovalsListScreenState extends ConsumerState<ApprovalsListScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) {
        ref.read(approvalsControllerProvider.notifier).loadApprovals();
      }
    });
  }

  void _confirmApprove(BuildContext context, ApprovalItem item, AppLocalizations l10n) {
    showDialog(
      context: context,
      builder: (ctx) {
        final commentCtrl = TextEditingController();
        return AlertDialog(
          title: Text(l10n.approve),
          content: TextField(
            controller: commentCtrl,
            decoration: InputDecoration(hintText: l10n.requestInfo),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(ctx),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () async {
                Navigator.pop(ctx);
                await ref.read(approvalsControllerProvider.notifier).approve(
                      item.approvalId,
                      comment: commentCtrl.text.trim(),
                    );
              },
              style: ElevatedButton.styleFrom(backgroundColor: Colors.teal, foregroundColor: Colors.white),
              child: Text(l10n.approve),
            ),
          ],
        );
      },
    );
  }

  void _confirmReject(BuildContext context, ApprovalItem item, AppLocalizations l10n) {
    showDialog(
      context: context,
      builder: (ctx) {
        final reasonCtrl = TextEditingController();
        return AlertDialog(
          title: Text(l10n.reject),
          content: TextField(
            controller: reasonCtrl,
            decoration: InputDecoration(hintText: l10n.requestInfo),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(ctx),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () async {
                Navigator.pop(ctx);
                await ref.read(approvalsControllerProvider.notifier).reject(
                      item.approvalId,
                      reason: reasonCtrl.text.trim(),
                    );
              },
              style: ElevatedButton.styleFrom(backgroundColor: Colors.redAccent, foregroundColor: Colors.white),
              child: Text(l10n.reject),
            ),
          ],
        );
      },
    );
  }

  Widget _buildApprovalCard(BuildContext context, ApprovalItem item, bool isPending, AppLocalizations l10n) {
    // 규칙: 무의미한 raw UUID 노출 금지. 식별 코드로 치환 표기 (예: APP-340a0917)
    final displayId = UuidFormatter.format(item.approvalId, prefix: 'APP');
    final targetDisplayId = UuidFormatter.format(item.targetId, prefix: 'TGT');
    final offset = DateHelper.getTimezoneOffset(ref.read(sharedPreferencesProvider).getString('user_personal_timezone') ?? 'Asia/Seoul');

    Color badgeColor;
    String statusText = item.status;
    if (item.status == 'APPROVED') {
      badgeColor = Colors.teal;
      statusText = l10n.statusApproved;
    } else if (item.status == 'REJECTED') {
      badgeColor = Colors.redAccent;
      statusText = l10n.statusRejected;
    } else if (item.status == 'CANCELLED') {
      badgeColor = Colors.red.shade700;
      statusText = '상신취소';
    } else {
      badgeColor = Colors.amber.shade800;
      statusText = l10n.pendingApproval;
    }

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
      child: InkWell(
        onTap: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => ApprovalDetailScreen(item: item),
            ),
          );
        },
        borderRadius: BorderRadius.circular(10),
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Expanded(
                    child: Wrap(
                      spacing: 6,
                      runSpacing: 4,
                      crossAxisAlignment: WrapCrossAlignment.center,
                      children: [
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                          decoration: BoxDecoration(
                            color: Colors.indigo[50],
                            borderRadius: BorderRadius.circular(6),
                            border: Border.all(color: Colors.indigo.shade200),
                          ),
                          child: Text(
                            displayId,
                            style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.indigo, fontSize: 13),
                          ),
                        ),
                        if (item.domainName != null && item.domainName!.isNotEmpty)
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 3),
                            decoration: BoxDecoration(
                              color: Colors.blue.shade50,
                              borderRadius: BorderRadius.circular(4),
                              border: Border.all(color: Colors.blue.shade200),
                            ),
                            child: Text(
                              L10nHelper.parseLocalizedMap(item.domainName, context),
                              style: TextStyle(fontSize: 11, fontWeight: FontWeight.w600, color: Colors.blue.shade800),
                            ),
                          ),
                        if (item.idAttribute != null && item.idAttribute!.isNotEmpty)
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 3),
                            decoration: BoxDecoration(
                              color: Colors.amber.shade50,
                              borderRadius: BorderRadius.circular(4),
                              border: Border.all(color: Colors.amber.shade300),
                            ),
                            child: Text(
                              item.idAttribute!,
                              style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: Colors.amber.shade900),
                            ),
                          ),
                        if (item.nameAttribute != null && item.nameAttribute!.isNotEmpty)
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 3),
                            decoration: BoxDecoration(
                              color: Colors.teal.shade50,
                              borderRadius: BorderRadius.circular(4),
                              border: Border.all(color: Colors.teal.shade200),
                            ),
                            child: Text(
                              L10nHelper.parseLocalizedMap(item.nameAttribute, context),
                              style: TextStyle(fontSize: 11, fontWeight: FontWeight.w600, color: Colors.teal.shade800),
                            ),
                          ),
                      ],
                    ),
                  ),
                  const SizedBox(width: 8),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: badgeColor.withOpacity(0.15),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Text(
                      statusText,
                      style: TextStyle(fontWeight: FontWeight.bold, color: badgeColor, fontSize: 12),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Row(
                children: [
                  Icon(Icons.person, size: 16, color: Colors.grey[600]),
                  const SizedBox(width: 6),
                  Text(
                    '${l10n.requester}: ',
                    style: TextStyle(color: Colors.grey[700], fontSize: 13, fontWeight: FontWeight.w500),
                  ),
                  Text(
                    item.requester,
                    style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 13, color: Colors.black87),
                  ),
                ],
              ),
              const SizedBox(height: 4),
              Row(
                children: [
                  Icon(Icons.track_changes, size: 16, color: Colors.grey[600]),
                  const SizedBox(width: 6),
                  Text(
                    'Target: $targetDisplayId (${item.targetType})',
                    style: TextStyle(color: Colors.grey[800], fontSize: 13),
                  ),
                ],
              ),
              if (item.requestDate != null) ...[
                const SizedBox(height: 6),
                Row(
                  children: [
                    Icon(Icons.access_time, size: 16, color: Colors.grey[600]),
                    const SizedBox(width: 6),
                    Text(
                      '${l10n.requestDate}: ${DateHelper.formatWithOffset(item.requestDate, offset, pattern: 'yyyy-MM-dd HH:mm')}',
                      style: TextStyle(color: Colors.grey[600], fontSize: 12),
                    ),
                  ],
                ),
              ],
              if (isPending && item.status == 'PENDING') ...[
                const Divider(height: 24),
                Row(
                  mainAxisAlignment: MainAxisAlignment.end,
                  children: [
                    OutlinedButton.icon(
                      onPressed: () => _confirmReject(context, item, l10n),
                      icon: const Icon(Icons.close, size: 16, color: Colors.redAccent),
                      label: Text(l10n.reject, style: const TextStyle(color: Colors.redAccent)),
                      style: OutlinedButton.styleFrom(
                        side: const BorderSide(color: Colors.redAccent),
                        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                      ),
                    ),
                    const SizedBox(width: 12),
                    ElevatedButton.icon(
                      onPressed: () => _confirmApprove(context, item, l10n),
                      icon: const Icon(Icons.check, size: 16, color: Colors.white),
                      label: Text(l10n.approve),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.teal,
                        foregroundColor: Colors.white,
                        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                      ),
                    ),
                  ],
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final state = ref.watch(approvalsControllerProvider);

    return DefaultTabController(
      length: 2,
      child: Scaffold(
        appBar: AppBar(
          title: Text(l10n.approvalsTitle),
          backgroundColor: Colors.indigo,
          foregroundColor: Colors.white,
          actions: [
            IconButton(
              icon: const Icon(Icons.refresh),
              tooltip: l10n.refresh,
              onPressed: () {
                ref.read(approvalsControllerProvider.notifier).loadApprovals();
              },
            ),
          ],
          bottom: TabBar(
            labelColor: Colors.white,
            unselectedLabelColor: Colors.indigo[200],
            indicatorColor: Colors.white,
            tabs: [
              Tab(text: l10n.pendingApprovals),
              Tab(text: l10n.mySubmittedRequests),
            ],
          ),
        ),
        body: state.isLoading
            ? const Center(child: CircularProgressIndicator())
            : TabBarView(
                children: [
                  // Tab 1: Pending Approvals List
                  RefreshIndicator(
                    onRefresh: () async {
                      ref.read(approvalsControllerProvider.notifier).loadApprovals();
                    },
                    child: state.pendingItems.isEmpty
                        ? CustomScrollView(
                            physics: const AlwaysScrollableScrollPhysics(),
                            slivers: [
                              SliverFillRemaining(
                                child: Center(child: Text(l10n.noPendingRequests, style: TextStyle(color: Colors.grey[600], fontSize: 15))),
                              ),
                            ],
                          )
                        : ListView.builder(
                            physics: const AlwaysScrollableScrollPhysics(),
                            padding: const EdgeInsets.all(12),
                            itemCount: state.pendingItems.length,
                            itemBuilder: (ctx, index) => _buildApprovalCard(ctx, state.pendingItems[index], true, l10n),
                          ),
                  ),
                  // Tab 2: My Submitted Requests
                  RefreshIndicator(
                    onRefresh: () async {
                      ref.read(approvalsControllerProvider.notifier).loadApprovals();
                    },
                    child: state.submittedItems.isEmpty
                        ? CustomScrollView(
                            physics: const AlwaysScrollableScrollPhysics(),
                            slivers: [
                              SliverFillRemaining(
                                child: Center(child: Text(l10n.noRequestsSubmittedYet, style: TextStyle(color: Colors.grey[600], fontSize: 15))),
                              ),
                            ],
                          )
                        : ListView.builder(
                            physics: const AlwaysScrollableScrollPhysics(),
                            padding: const EdgeInsets.all(12),
                            itemCount: state.submittedItems.length,
                            itemBuilder: (ctx, index) => _buildApprovalCard(ctx, state.submittedItems[index], false, l10n),
                          ),
                  ),
                ],
              ),
      ),
    );
  }
}
