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
    String? nodeId,
    String? domainName,
    String? idAttribute,
    String? nameAttribute,
    String? classificationPath,
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
      payloadMap = Map<String, dynamic>.from(payloadRaw);
    } else if (payloadRaw is String) {
      try {
        final decoded = jsonDecode(payloadRaw);
        if (decoded is Map<String, dynamic>) {
          payloadMap = decoded;
        }
      } catch (_) {}
    }

    // Parse nested before/after if they are encoded as JSON strings
    if (payloadMap.containsKey('before') && payloadMap['before'] is String) {
      try {
        payloadMap['before'] = jsonDecode(payloadMap['before']);
      } catch (_) {}
    }
    if (payloadMap.containsKey('after') && payloadMap['after'] is String) {
      try {
        payloadMap['after'] = jsonDecode(payloadMap['after']);
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
    String? dId = (nodeMap is Map) ? (nodeMap['domainId'] ?? requestMap['domainId'])?.toString() : requestMap['domainId']?.toString();
    String? nId = (nodeMap is Map) ? nodeMap['id']?.toString() : (json['nodeId'] ?? requestMap['nodeId'])?.toString();
    String? classPath;
    String? domainNameParsed;

    if (nodeMap is Map) {
      final domainObj = nodeMap['domain'];
      final rawDomainName = (domainObj is Map ? domainObj['name'] : null) ??
          nodeMap['domainName'] ??
          requestMap['domainName'] ??
          json['domainName'] ??
          (requestMap['domain'] is Map ? requestMap['domain']['name'] : null) ??
          (json['domain'] is Map ? json['domain']['name'] : null);
      final rawNodeName = nodeMap['name'] ?? nodeMap['nodeName'] ?? requestMap['nodeName'] ?? json['nodeName'];

      final domainName = rawDomainName is Map ? jsonEncode(rawDomainName) : rawDomainName?.toString();
      final nodeName = rawNodeName is Map ? jsonEncode(rawNodeName) : rawNodeName?.toString();
      domainNameParsed = domainName;

      if (domainObj is Map && domainObj['id'] != null) {
        dId = domainObj['id'].toString();
      } else if (nodeMap['domainId'] != null) {
        dId = nodeMap['domainId'].toString();
      }

      if (domainName != null && domainName.isNotEmpty && nodeName != null && nodeName.isNotEmpty) {
        classPath = '$domainName > $nodeName';
      } else {
        classPath = nodeName ?? domainName;
      }
    } else {
      final rawDomainName = requestMap['domainName'] ?? json['domainName'] ?? (requestMap['domain'] is Map ? requestMap['domain']['name'] : null);
      final rawNodeName = requestMap['nodeName'] ?? json['nodeName'];
      final domainName = rawDomainName is Map ? jsonEncode(rawDomainName) : rawDomainName?.toString();
      final nodeName = rawNodeName is Map ? jsonEncode(rawNodeName) : rawNodeName?.toString();
      domainNameParsed = domainName;

      if (domainName != null && domainName.isNotEmpty && nodeName != null && nodeName.isNotEmpty) {
        classPath = '$domainName > $nodeName';
      } else {
        classPath = nodeName ?? domainName;
      }
    }

    // Extract idAttribute & nameAttribute
    final afterData = payloadMap['after'] is Map ? payloadMap['after'] as Map : (payloadMap['data'] is Map ? payloadMap['data'] as Map : payloadMap);
    final beforeData = payloadMap['before'] is Map ? payloadMap['before'] as Map : {};

    String? idAttr = (json['idAttribute'] ?? requestMap['idAttribute'])?.toString();
    String? nameAttr = (json['nameAttribute'] ?? requestMap['nameAttribute'])?.toString();

    if (idAttr == null || idAttr.isEmpty) {
      for (final key in ['idAttribute', 'identifier', 'id_attribute', 'CODE', 'ID', 'EMP_NO', 'EMP_ID', 'USER_ID', 'CUSTOMER_ID']) {
        if (afterData.containsKey(key) && afterData[key] != null) {
          idAttr = afterData[key].toString();
          break;
        }
        if (beforeData.containsKey(key) && beforeData[key] != null) {
          idAttr = beforeData[key].toString();
          break;
        }
      }
    }

    if (nameAttr == null || nameAttr.isEmpty) {
      for (final key in ['nameAttribute', 'displayName', 'name_attribute', 'NAME', 'TITLE', 'EMP_NAME', 'USER_NAME', 'CUSTOMER_NAME']) {
        if (afterData.containsKey(key) && afterData[key] != null) {
          nameAttr = afterData[key] is Map ? jsonEncode(afterData[key]) : afterData[key].toString();
          break;
        }
        if (beforeData.containsKey(key) && beforeData[key] != null) {
          nameAttr = beforeData[key] is Map ? jsonEncode(beforeData[key]) : beforeData[key].toString();
          break;
        }
      }
    }

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
      nodeId: nId,
      domainName: domainNameParsed,
      idAttribute: idAttr,
      nameAttribute: nameAttr,
      classificationPath: classPath,
    );
  }
}
