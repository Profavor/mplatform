import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/features/dashboard/data/models/dashboard_stats_model.dart';
import 'package:mplatform_mobile/features/dashboard/data/models/dq_trend_item_model.dart';
import 'package:mplatform_mobile/features/dashboard/data/models/dq_severity_item_model.dart';
import 'package:mplatform_mobile/features/dashboard/data/repositories/dashboard_repository.dart';

part 'dashboard_provider.freezed.dart';

@freezed
class DashboardState with _$DashboardState {
  const factory DashboardState({
    @Default(false) bool isLoading,
    String? errorMessage,
    DashboardStatsModel? stats,
    @Default([]) List<DqTrendItemModel> dqTrends,
    @Default([]) List<DqSeverityItemModel> dqSeverity,
  }) = _DashboardState;
}

class DashboardNotifier extends StateNotifier<DashboardState> {
  final DashboardRepository _repository;

  DashboardNotifier(this._repository) : super(const DashboardState());

  Future<void> fetchDashboardData() async {
    state = state.copyWith(isLoading: true, errorMessage: null);
    try {
      final stats = await _repository.getStats();
      final trends = await _repository.getDqTrends();
      final severity = await _repository.getDqSeverity();

      state = state.copyWith(
        isLoading: false,
        stats: stats,
        dqTrends: trends,
        dqSeverity: severity,
      );
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        errorMessage: e.toString(),
      );
    }
  }
}

final dashboardProvider = StateNotifierProvider<DashboardNotifier, DashboardState>((ref) {
  final repository = ref.watch(dashboardRepositoryProvider);
  return DashboardNotifier(repository);
});
