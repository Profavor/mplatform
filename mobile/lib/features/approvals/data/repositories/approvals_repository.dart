import 'package:dio/dio.dart';
import 'package:mplatform_mobile/features/approvals/domain/models/approval_item.dart';

class ApprovalsRepository {
  final Dio _dio;

  ApprovalsRepository(this._dio);

  Future<List<ApprovalItem>> getPendingApprovals() async {
    final response = await _dio.get('/api/approval-requests/todos');
    final data = response.data;
    final list = (data is Map ? data['content'] : data) as List<dynamic>? ?? [];
    return list.map((e) => ApprovalItem.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<ApprovalItem?> getApprovalDetail(String id) async {
    try {
      final response = await _dio.get('/api/approval-requests/$id');
      if (response.statusCode == 200 && response.data != null) {
        return ApprovalItem.fromJson(response.data as Map<String, dynamic>);
      }
    } catch (e) {
      // Ignore or log error
    }
    return null;
  }

  Future<List<ApprovalItem>> getMySubmittedApprovals() async {
    final response = await _dio.get('/api/approval-requests/my-requests');
    final data = response.data;
    final list = (data is Map ? data['content'] : data) as List<dynamic>? ?? [];
    return list.map((e) => ApprovalItem.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<bool> approveRequest(String approvalId, {String? comment}) async {
    final response = await _dio.post(
      '/api/approval-requests/steps/$approvalId/approve',
      data: {'comment': comment ?? ''},
    );
    return response.statusCode == 200;
  }

  Future<bool> rejectRequest(String approvalId, {String? reason}) async {
    final response = await _dio.post(
      '/api/approval-requests/steps/$approvalId/reject',
      data: {'comment': reason ?? ''},
    );
    return response.statusCode == 200;
  }

  Future<bool> cancelApprovalRequest(String requestId, {String? reason}) async {
    final response = await _dio.post(
      '/api/approval-requests/$requestId/cancel',
      queryParameters: reason != null && reason.isNotEmpty ? {'reason': reason} : null,
    );
    return response.statusCode == 200;
  }
}
