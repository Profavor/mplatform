import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/providers/core_providers.dart';
import 'package:mplatform_mobile/features/dashboard/data/models/dashboard_stats_model.dart';
import 'package:mplatform_mobile/features/dashboard/data/models/dq_trend_item_model.dart';
import 'package:mplatform_mobile/features/dashboard/data/models/dq_severity_item_model.dart';

final dashboardRepositoryProvider = Provider<DashboardRepository>((ref) {
  final dio = ref.watch(dioProvider);
  return DashboardRepository(dio);
});

class DashboardRepository {
  final Dio _dio;

  DashboardRepository(this._dio);

  Future<DashboardStatsModel> getStats() async {
    final response = await _dio.get('/api/dashboard/stats');
    return DashboardStatsModel.fromJson(response.data);
  }

  Future<List<DqTrendItemModel>> getDqTrends() async {
    final response = await _dio.get('/api/dashboard/dq-trends');
    return (response.data as List)
        .map((e) => DqTrendItemModel.fromJson(e))
        .toList();
  }

  Future<List<DqSeverityItemModel>> getDqSeverity() async {
    final response = await _dio.get('/api/dashboard/dq-severity');
    return (response.data as List)
        .map((e) => DqSeverityItemModel.fromJson(e))
        .toList();
  }
}
