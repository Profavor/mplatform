import 'package:freezed_annotation/freezed_annotation.dart';

part 'dashboard_stats_model.freezed.dart';
part 'dashboard_stats_model.g.dart';

@freezed
class DashboardStatsModel with _$DashboardStatsModel {
  const factory DashboardStatsModel({
    @Default(0) int totalDomains,
    @Default(0) int pendingApprovals,
    @Default(0) int activeRecords,
    @Default(0) int pendingMatches,
    @Default(0) int openDqViolations,
    @Default(0) int approvedApprovals,
    @Default(0) int rejectedApprovals,
  }) = _DashboardStatsModel;

  factory DashboardStatsModel.fromJson(Map<String, dynamic> json) =>
      _$DashboardStatsModelFromJson(json);
}
