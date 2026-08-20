import 'package:flutter/material.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/core/utils/date_helper.dart';
import 'package:mplatform_mobile/features/approvals/domain/models/approval_item.dart';

class ApprovalTimelineStepperWidget extends StatelessWidget {
  final ApprovalItem item;
  final int offsetHours;

  const ApprovalTimelineStepperWidget({
    super.key,
    required this.item,
    required this.offsetHours,
  });

  Color _getStatusColor(String status) {
    switch (status.toUpperCase()) {
      case 'APPROVED':
      case 'COMPLETED':
        return Colors.blue.shade700;
      case 'REJECTED':
        return Colors.red.shade600;
      case 'CANCELLED':
        return Colors.grey.shade500;
      case 'PENDING':
      default:
        return Colors.orange.shade700;
    }
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    final reqDateStr = item.requestDate != null
        ? DateHelper.formatWithOffset(item.requestDate, offsetHours, pattern: 'yyyy. MM. dd. HH:mm:ss')
        : '';
    final reviewedDateStr = item.reviewedDate != null
        ? DateHelper.formatWithOffset(item.reviewedDate, offsetHours, pattern: 'yyyy. MM. dd. HH:mm:ss')
        : '';

    final steps = item.steps;
    final isCancelled = item.status == 'CANCELLED';

    // Extract reviewer name if available
    String reviewerName = '';
    if (steps.isNotEmpty) {
      final reviewStep = steps.firstWhere(
        (s) => s is Map && (s['stepOrder'] != 0 && s['stepType'] != 'DRAFT'),
        orElse: () => null,
      );
      if (reviewStep is Map) {
        reviewerName = (reviewStep['assigneeName'] ?? reviewStep['assigneeUser']?['username'] ?? reviewStep['assigneeId'] ?? '').toString();
      }
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // 1. 결재 진행 단계 (Horizontal Stepper Card)
        Card(
          elevation: 1,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
            side: BorderSide(color: Colors.grey.shade200),
          ),
          color: Colors.white,
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  l10n.approvalHistory,
                  style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14, color: Colors.black87),
                ),
                const SizedBox(height: 16),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    // Node 0: 기안
                    _buildStepperNode(
                      title: l10n.approvalDraft,
                      subtitle: item.requester,
                      timeStr: reqDateStr,
                      isCompleted: true,
                      isActive: false,
                      isCancelled: false,
                      color: Colors.blue.shade800,
                    ),
                    Expanded(
                      child: _buildConnectingLine(isCompleted: item.status != 'PENDING'),
                    ),
                    // Node 1: 결재 / 시스템 승인
                    _buildStepperNode(
                      title: l10n.approval,
                      subtitle: isCancelled
                          ? (reviewerName.isNotEmpty ? reviewerName : '상신취소')
                          : (item.status == 'APPROVED'
                              ? l10n.statusApproved
                              : (item.status == 'REJECTED' ? l10n.statusRejected : l10n.statusPending)),
                      timeStr: item.status != 'PENDING' ? reviewedDateStr : '',
                      isCompleted: item.status == 'APPROVED',
                      isActive: item.status == 'PENDING',
                      isCancelled: isCancelled,
                      color: _getStatusColor(item.status),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 16),

        // 2. 결재선 현황 (Detailed Approval Line List)
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.only(left: 4.0, bottom: 8.0),
              child: Text(
                l10n.approvalHistory,
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14, color: Colors.black87),
              ),
            ),
            if (steps.isNotEmpty)
              ...List.generate(steps.length, (index) {
                final step = steps[index];
                String stepName = '${l10n.approvalDraft} - ${item.requester}';
                String stepStatus = '상신완료';
                String? stepComment;
                String? stepDate = reqDateStr;
                int stepOrder = index;
                bool isStepCancelled = false;

                if (step is Map) {
                  stepOrder = step['stepOrder'] is int ? step['stepOrder'] : index;
                  final assignee = step['assigneeName'] ?? step['assigneeUser']?['username'] ?? step['assigneeId'] ?? item.requester;
                  final type = step['stepType']?.toString();
                  final status = step['status']?.toString() ?? 'PENDING';

                  if (stepOrder == 0 || type == 'DRAFT') {
                    stepName = '${l10n.approvalDraft} - $assignee';
                    stepStatus = '상신완료';
                    stepDate = reqDateStr;
                    stepComment = step['comment']?.toString() ?? item.requestReason;
                  } else {
                    stepName = '${l10n.approval} - $assignee';
                    if (status == 'CANCELLED' || isCancelled) {
                      stepStatus = '상신취소';
                      isStepCancelled = true;
                      stepComment = step['comment']?.toString() ?? item.requestReason;
                    } else if (status == 'PENDING') {
                      stepStatus = l10n.statusPending;
                      stepComment = step['comment']?.toString();
                    } else {
                      stepStatus = status == 'APPROVED'
                          ? l10n.statusApproved
                          : (status == 'REJECTED' ? l10n.statusRejected : l10n.statusPending);
                      stepComment = step['comment']?.toString();
                    }
                    stepDate = reviewedDateStr.isNotEmpty ? reviewedDateStr : '';
                  }
                }

                return _buildApprovalStepCard(
                  order: stepOrder,
                  title: stepName,
                  statusText: stepStatus,
                  dateText: stepDate != null && stepDate.isNotEmpty ? '$stepDate ${l10n.processedStatus}' : '',
                  comment: stepComment,
                  isCancelled: isStepCancelled,
                );
              })
            else ...[
              // Fallback Step Cards if steps array is empty
              _buildApprovalStepCard(
                order: 0,
                title: '${l10n.approvalDraft} - ${item.requester}',
                statusText: '상신완료',
                dateText: reqDateStr.isNotEmpty ? '$reqDateStr ${l10n.processedStatus}' : '',
                comment: (item.requestReason != null && item.requestReason!.isNotEmpty)
                    ? item.requestReason!
                    : null,
                isCancelled: false,
              ),
              if (isCancelled)
                _buildApprovalStepCard(
                  order: 1,
                  title: '${l10n.approval} - ${reviewerName.isNotEmpty ? reviewerName : "시스템"}',
                  statusText: '상신취소',
                  dateText: reviewedDateStr.isNotEmpty ? '$reviewedDateStr ${l10n.processedStatus}' : '',
                  comment: item.requestReason != null && item.requestReason!.isNotEmpty
                      ? item.requestReason!
                      : '상신 취소됨',
                  isCancelled: true,
                ),
            ],
          ],
        ),
      ],
    );
  }

  Widget _buildStepperNode({
    required String title,
    required String subtitle,
    required String timeStr,
    required bool isCompleted,
    required bool isActive,
    required bool isCancelled,
    required Color color,
  }) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: 36,
          height: 36,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: isCancelled
                ? Colors.grey.shade400
                : (isCompleted ? color : (isActive ? Colors.orange.shade700 : Colors.grey.shade300)),
          ),
          child: Icon(
            isCancelled
                ? Icons.cancel
                : (isCompleted ? Icons.check : (isActive ? Icons.hourglass_top : Icons.person)),
            color: Colors.white,
            size: 18,
          ),
        ),
        const SizedBox(height: 6),
        Text(
          title,
          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 12, color: Colors.black87),
        ),
        Text(
          subtitle,
          style: TextStyle(fontSize: 11, color: Colors.grey.shade600),
        ),
        if (timeStr.isNotEmpty) ...[
          const SizedBox(height: 2),
          Text(
            timeStr,
            style: TextStyle(fontSize: 10, color: Colors.grey.shade500),
          ),
        ],
      ],
    );
  }

  Widget _buildConnectingLine({required bool isCompleted}) {
    return Container(
      width: 60,
      height: 2,
      margin: const EdgeInsets.symmetric(horizontal: 8, vertical: 18),
      color: isCompleted ? Colors.blue.shade700 : Colors.grey.shade300,
    );
  }

  Widget _buildApprovalStepCard({
    required int order,
    required String title,
    required String statusText,
    required String dateText,
    required String? comment,
    required bool isCancelled,
  }) {
    Color badgeColor = Colors.blue.shade600;
    if (statusText == '검토 대기' || statusText.contains('대기') || statusText.contains('Pending')) {
      badgeColor = Colors.orange.shade700;
    } else if (statusText == '상신취소' || isCancelled) {
      badgeColor = Colors.grey.shade600;
    } else if (statusText == '반려' || statusText.contains('Rejected')) {
      badgeColor = Colors.red.shade600;
    } else if (statusText == '승인' || statusText == '상신완료' || statusText.contains('Approved')) {
      badgeColor = Colors.blue.shade600;
    }

    final hasComment = comment != null && comment.trim().isNotEmpty;

    return Container(
      width: double.infinity,
      margin: const EdgeInsets.only(bottom: 10),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: isCancelled ? Colors.red.shade200 : Colors.grey.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  CircleAvatar(
                    radius: 10,
                    backgroundColor: isCancelled ? Colors.grey.shade500 : (statusText == '검토 대기' ? Colors.orange.shade700 : Colors.blue.shade800),
                    child: Text(
                      '$order',
                      style: const TextStyle(color: Colors.white, fontSize: 10, fontWeight: FontWeight.bold),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Text(
                    title,
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      color: isCancelled ? Colors.grey.shade800 : (statusText == '검토 대기' ? Colors.orange.shade900 : Colors.blue.shade800),
                      fontSize: 13,
                    ),
                  ),
                ],
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: badgeColor,
                  borderRadius: BorderRadius.circular(4),
                ),
                child: Text(
                  statusText,
                  style: const TextStyle(color: Colors.white, fontSize: 10.5, fontWeight: FontWeight.bold),
                ),
              ),
            ],
          ),
          if (dateText.isNotEmpty) ...[
            const SizedBox(height: 4),
            Align(
              alignment: Alignment.centerRight,
              child: Text(dateText, style: const TextStyle(fontSize: 11, color: Colors.black54)),
            ),
          ],
          if (hasComment) ...[
            const SizedBox(height: 10),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                border: Border(left: BorderSide(color: isCancelled ? Colors.red.shade700 : Colors.blue.shade800, width: 3)),
                color: isCancelled ? Colors.red.shade50 : Colors.grey.shade50,
                borderRadius: const BorderRadius.horizontal(right: Radius.circular(4)),
              ),
              child: Text(
                isCancelled ? '"$comment" (상신 취소 사유)' : '"$comment"',
                style: TextStyle(
                  fontStyle: FontStyle.italic,
                  color: isCancelled ? Colors.red.shade900 : Colors.black87,
                  fontSize: 12.5,
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }
}
