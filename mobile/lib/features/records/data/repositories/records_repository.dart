import 'package:dio/dio.dart';
import 'package:mplatform_mobile/features/records/domain/models/classification_node_model.dart';
import 'package:mplatform_mobile/features/records/domain/models/domain_model.dart';
import 'package:mplatform_mobile/features/records/domain/models/field_definition.dart';
import 'package:mplatform_mobile/features/records/domain/models/record_item.dart';
import 'package:mplatform_mobile/features/records/domain/models/records_page_response.dart';

class RecordsRepository {
  final Dio _dio;

  RecordsRepository(this._dio);

  Future<List<DomainModel>> getDomains() async {
    final response = await _dio.get('/api/domains');
    final data = response.data;
    List<dynamic> list;
    if (data is Map && data.containsKey('content')) {
      list = data['content'] as List<dynamic>;
    } else if (data is Map && data.containsKey('data')) {
      list = data['data'] as List<dynamic>;
    } else if (data is List) {
      list = data;
    } else {
      list = [];
    }
    return list.map((e) => DomainModel.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<List<FieldDefinition>> getFieldDefinitions(String domainId) async {
    final response = await _dio.get('/api/domains/$domainId/fields');
    final list = response.data as List<dynamic>;
    return list.map((e) => FieldDefinition.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<List<ClassificationNodeModel>> getClassificationTree(String domainId) async {
    final response = await _dio.get('/api/domains/$domainId/nodes/tree');
    final list = response.data as List<dynamic>;
    return list.map((e) => ClassificationNodeModel.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<RecordsPageResponse> getRecords({
    required String domainId,
    String? nodeId,
    int page = 0,
    int size = 20,
    String? searchQuery,
    List<String>? searchFields,
    Map<String, dynamic>? filters,
  }) async {
    final queryParams = <String, dynamic>{
      'page': page,
      'size': size,
      if (searchQuery != null && searchQuery.trim().isNotEmpty) ...{
        if (searchFields != null && searchFields.isNotEmpty) ...{
          'search_multi_keys': searchFields.join(','),
          'search_multi_val': searchQuery.trim(),
        } else ...{
          'search_name': searchQuery.trim(),
          'search_q': searchQuery.trim(),
        }
      },
      if (filters != null) ...filters,
    };

    final String url = nodeId != null 
        ? '/api/nodes/$nodeId/records' 
        : '/api/records/domain/$domainId';

    final response = await _dio.get(url, queryParameters: queryParams);
    return RecordsPageResponse.fromJson(response.data as Map<String, dynamic>);
  }

  Future<RecordItem> getRecordDetail(String recordId) async {
    final response = await _dio.get('/api/records/$recordId');
    return RecordItem.fromJson(response.data as Map<String, dynamic>);
  }

  Future<Map<String, String>> decryptRecordFields({
    required String recordId,
    required List<String> fieldKeys,
    required String accessReason,
  }) async {
    final response = await _dio.post(
      '/api/sensitive-data/record/$recordId/decrypt',
      data: {
        'fieldKeys': fieldKeys,
        'accessReason': accessReason,
      },
    );
    final map = response.data as Map<String, dynamic>;
    return map.map((key, value) => MapEntry(key, value?.toString() ?? ''));
  }

  Future<List<Map<String, dynamic>>> getRecordHistory(String recordId) async {
    try {
      final response = await _dio.get('/api/records/$recordId/history');
      final list = response.data as List<dynamic>;
      return list.cast<Map<String, dynamic>>();
    } catch (e) {
      print('Failed to load record history: $e');
      return [];
    }
  }

  Future<Map<String, String>> decryptHistoryFields({
    required String historyId,
    required List<String> fieldKeys,
    required String accessReason,
  }) async {
    final response = await _dio.post(
      '/api/sensitive-data/history/$historyId/decrypt',
      data: {
        'fieldKeys': fieldKeys,
        'accessReason': accessReason,
      },
    );
    final map = response.data as Map<String, dynamic>;
    return map.map((key, value) => MapEntry(key, value?.toString() ?? ''));
  }
}
