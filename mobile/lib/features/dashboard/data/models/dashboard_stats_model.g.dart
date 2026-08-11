// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dashboard_stats_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$DashboardStatsModelImpl _$$DashboardStatsModelImplFromJson(
  Map<String, dynamic> json,
) => _$DashboardStatsModelImpl(
  totalDomains: (json['totalDomains'] as num?)?.toInt() ?? 0,
  pendingApprovals: (json['pendingApprovals'] as num?)?.toInt() ?? 0,
  activeRecords: (json['activeRecords'] as num?)?.toInt() ?? 0,
  pendingMatches: (json['pendingMatches'] as num?)?.toInt() ?? 0,
  openDqViolations: (json['openDqViolations'] as num?)?.toInt() ?? 0,
  approvedApprovals: (json['approvedApprovals'] as num?)?.toInt() ?? 0,
  rejectedApprovals: (json['rejectedApprovals'] as num?)?.toInt() ?? 0,
);

Map<String, dynamic> _$$DashboardStatsModelImplToJson(
  _$DashboardStatsModelImpl instance,
) => <String, dynamic>{
  'totalDomains': instance.totalDomains,
  'pendingApprovals': instance.pendingApprovals,
  'activeRecords': instance.activeRecords,
  'pendingMatches': instance.pendingMatches,
  'openDqViolations': instance.openDqViolations,
  'approvedApprovals': instance.approvedApprovals,
  'rejectedApprovals': instance.rejectedApprovals,
};
