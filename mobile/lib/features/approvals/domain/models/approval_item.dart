import 'dart:convert';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'approval_item.freezed.dart';

@freezed
class ApprovalItem with _$ApprovalItem {
  const factory ApprovalItem({
    required String approvalId,
    required String targetType,
    required String targetId,
    required String requester,
    required String status,
    String? requestReason,
    String? requestDate,
    String? reviewedDate,
    @Default({}) Map<String, dynamic> payload,
    @Default([]) List<dynamic> steps,
    String? domainId,
  }) = _ApprovalItem;

  factory ApprovalItem.fromJson(Map<String, dynamic> json) {
    final requestMap = json['approvalRequest'] is Map<String, dynamic>
        ? json['approvalRequest'] as Map<String, dynamic>
        : json;

    final id = (json['approvalId'] ?? json['id'] ?? requestMap['id'])?.toString() ?? 'unknown-id';
    final type = (json['targetType'] ?? requestMap['targetType'])?.toString() ?? 'RECORD';
    final targetId = (json['targetId'] ?? requestMap['targetId'])?.toString() ?? '';
    
    var requesterVal = json['requester'] ?? requestMap['requesterUsername'] ?? requestMap['requesterName'] ?? requestMap['requesterId'] ?? requestMap['requester'];
    if (requesterVal is Map) {
      requesterVal = requesterVal['username'] ?? requesterVal['name'] ?? requesterVal['id'];
    }
    final requesterStr = requesterVal?.toString() ?? 'unknown_user';

    final statusStr = (json['status'] ?? requestMap['status'])?.toString() ?? 'PENDING';
    String? reason = (json['reason'] ?? requestMap['reason'] ?? json['requestReason'] ?? requestMap['requestReason'] ?? json['comment'])?.toString();
    final date = (json['requestDate'] ?? requestMap['createdAt'] ?? json['createdAt'])?.toString();
    final reviewed = (json['reviewedDate'] ?? json['updatedAt'])?.toString();
    
    final payloadRaw = json['changes'] ?? json['payload'] ?? requestMap['changes'] ?? requestMap['payload'];
    Map<String, dynamic> payloadMap = {};
    if (payloadRaw is Map<String, dynamic>) {
      payloadMap = payloadRaw;
    } else if (payloadRaw is String) {
      try {
        payloadMap = jsonDecode(payloadRaw) as Map<String, dynamic>;
      } catch (_) {}
    }

    final stepsRaw = json['steps'] ?? requestMap['steps'];
    final stepsList = stepsRaw is List ? stepsRaw : [];
    
    if ((reason == null || reason.isEmpty) && stepsList.isNotEmpty) {
      for (var step in stepsList) {
        if (step is Map && step['stepOrder'] == 0 && step['comment'] != null) {
          reason = step['comment'].toString();
          break;
        }
      }
    }

    final nodeMap = json['classificationNode'] ?? requestMap['classificationNode'];
    final dId = (nodeMap is Map) ? (nodeMap['domainId'] ?? requestMap['domainId'])?.toString() : requestMap['domainId']?.toString();

    return ApprovalItem(
      approvalId: id,
      targetType: type,
      targetId: targetId,
      requester: requesterStr,
      status: statusStr,
      requestReason: reason,
      requestDate: date,
      reviewedDate: reviewed,
      payload: payloadMap,
      steps: stepsList,
      domainId: dId,
    );
  }
}
