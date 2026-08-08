import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mplatform_mobile/core/l10n/generated/app_localizations.dart';
import 'package:mplatform_mobile/core/utils/date_helper.dart';
import 'package:mplatform_mobile/core/utils/uuid_formatter.dart';
import 'package:mplatform_mobile/features/approvals/presentation/providers/approvals_provider.dart';
import 'package:mplatform_mobile/features/chat/presentation/providers/chat_provider.dart';
import 'package:mplatform_mobile/features/notifications/domain/models/notification_item.dart';
import 'package:mplatform_mobile/features/notifications/presentation/providers/notifications_provider.dart';
import 'package:mplatform_mobile/core/providers/locale_provider.dart';
import 'package:mplatform_mobile/features/dashboard/presentation/providers/dashboard_provider.dart';
import 'package:fl_chart/fl_chart.dart';

class HomeDashboardScreen extends ConsumerStatefulWidget {
  const HomeDashboardScreen({super.key});

  @override
  ConsumerState<HomeDashboardScreen> createState() => _HomeDashboardScreenState();
}

class _HomeDashboardScreenState extends ConsumerState<HomeDashboardScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) {
        ref.read(approvalsControllerProvider.notifier).loadApprovals();
        ref.read(chatControllerProvider.notifier).loadRooms();
        ref.read(notificationsControllerProvider.notifier).fetchNotifications(refresh: true);
        ref.read(dashboardProvider.notifier).fetchDashboardData();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context)!;
    
    // 규칙: 하드코딩 절대 금지. 실제 Riverpod Provider 상태 및 API 응답을 기반으로 동적 계산 및 표출
    final approvalsState = ref.watch(approvalsControllerProvider);
    final chatState = ref.watch(chatControllerProvider);
    final notificationsState = ref.watch(notificationsControllerProvider);
    final dashboardState = ref.watch(dashboardProvider);

    final int pendingApprovalsCount = approvalsState.pendingItems.length;
    final int unreadMessagesCount = chatState.rooms.fold<int>(0, (sum, room) => sum + room.unreadCount);
    final int openDqViolationsCount = dashboardState.stats?.openDqViolations ?? 0;
    
    final List<NotificationItem> recentActivities = notificationsState.notifications.take(5).toList();

    final bool isLoading = approvalsState.isLoading || chatState.isLoadingRooms || notificationsState.isLoading || dashboardState.isLoading;

    return Scaffold(
      appBar: AppBar(
        title: Text(l10n.homeWelcomeTitle),
        elevation: 2,
        backgroundColor: Colors.indigo[800],
        foregroundColor: Colors.white,
        actions: [
          IconButton(
            icon: const Icon(Icons.language),
            tooltip: 'Toggle Language',
            onPressed: () {
              final current = ref.read(localeProvider);
              if (current.languageCode == 'ko') {
                ref.read(localeProvider.notifier).state = const Locale('en');
              } else {
                ref.read(localeProvider.notifier).state = const Locale('ko');
              }
            },
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          await ref.read(approvalsControllerProvider.notifier).loadApprovals();
          await ref.read(chatControllerProvider.notifier).loadRooms();
          await ref.read(notificationsControllerProvider.notifier).fetchNotifications(refresh: true);
          await ref.read(dashboardProvider.notifier).fetchDashboardData();
        },
        child: SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // 1. Top Banner Card (Quality Status & Greeting)
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    colors: [Colors.indigo.shade800, Colors.deepPurple.shade600],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                  borderRadius: BorderRadius.circular(16),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.indigo.withOpacity(0.3),
                      blurRadius: 10,
                      offset: const Offset(0, 4),
                    ),
                  ],
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      l10n.homeWelcomeTitle,
                      style: const TextStyle(
                        fontSize: 22,
                        fontWeight: FontWeight.bold,
                        color: Colors.white,
                      ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      l10n.navTabRecords,
                      style: TextStyle(
                        fontSize: 14,
                        color: Colors.white.withOpacity(0.9),
                      ),
                    ),
                    const SizedBox(height: 16),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.2),
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const Icon(Icons.check_circle_outline, color: Colors.amberAccent, size: 18),
                          const SizedBox(width: 6),
                          Text(
                            l10n.navTabApprovals,
                            style: const TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.w600,
                              fontSize: 13,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 24),

              // 2. Dynamic Metric Summary Row (No Hardcoding!)
              if (isLoading && pendingApprovalsCount == 0 && unreadMessagesCount == 0)
                const Padding(
                  padding: EdgeInsets.symmetric(vertical: 24),
                  child: Center(child: CircularProgressIndicator()),
                )
              else
                Row(
                  children: [
                    Expanded(
                      child: _buildMetricCard(
                        context,
                        title: l10n.homeTodoTitle,
                        count: pendingApprovalsCount.toString(),
                        icon: Icons.assignment_turned_in,
                        color: Colors.amber.shade800,
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: _buildMetricCard(
                        context,
                        title: l10n.homeUnreadMessages,
                        count: unreadMessagesCount.toString(),
                        icon: Icons.chat_bubble,
                        color: Colors.teal.shade700,
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: _buildMetricCard(
                        context,
                        title: l10n.openDqViolations,
                        count: openDqViolationsCount.toString(),
                        icon: Icons.warning_amber,
                        color: Colors.red.shade700,
                      ),
                    ),
                  ],
                ),
              const SizedBox(height: 28),
              
              // 2-B. Dashboard DQ Charts
              if (!isLoading && (dashboardState.dqTrends.isNotEmpty || dashboardState.dqSeverity.isNotEmpty))
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      l10n.dqDashboardTitle,
                      style: const TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                        color: Colors.black87,
                      ),
                    ),
                    const SizedBox(height: 12),
                    _buildDqTrendChart(dashboardState),
                    const SizedBox(height: 16),
                    _buildDqSeverityChart(dashboardState),
                    const SizedBox(height: 28),
                  ],
                ),

              // 3. Recent Activity Section (No Hardcoded Mock Data!)
              Text(
                l10n.homeRecentActivity,
                style: const TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  color: Colors.black87,
                ),
              ),
              const SizedBox(height: 12),

              if (recentActivities.isEmpty && !isLoading)
                Card(
                  elevation: 1,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                  child: Container(
                    width: double.infinity,
                    padding: const EdgeInsets.symmetric(vertical: 32, horizontal: 16),
                    alignment: Alignment.center,
                    child: Text(
                      l10n.homeNoActivity,
                      style: TextStyle(color: Colors.grey[600], fontSize: 15),
                    ),
                  ),
                )
              else
                ListView.separated(
                  shrinkWrap: true,
                  physics: const NeverScrollableScrollPhysics(),
                  itemCount: recentActivities.length,
                  separatorBuilder: (context, index) => const Divider(height: 1),
                  itemBuilder: (context, index) {
                    final NotificationItem activity = recentActivities[index];
                    // 규칙: 무의미한 raw UUID는 절대 화면에 띄우지 않고 식별 코드로 변환 표출
                    final String displayCode = UuidFormatter.format(activity.targetId, prefix: 'REF');

                    return Card(
                      elevation: 1,
                      margin: const EdgeInsets.only(bottom: 8),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                      child: ListTile(
                        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                        leading: CircleAvatar(
                          backgroundColor: Colors.indigo.shade50,
                          child: Icon(Icons.history, color: Colors.indigo.shade700),
                        ),
                        title: Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Expanded(
                              child: Text(
                                activity.title,
                                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                              ),
                            ),
                            Text(
                              DateHelper.formatWithOffset(activity.createdAt, 9, pattern: 'MM-dd HH:mm'),
                              style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                            ),
                          ],
                        ),
                        subtitle: Padding(
                          padding: const EdgeInsets.only(top: 6.0),
                          child: Row(
                            children: [
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                                decoration: BoxDecoration(
                                  color: Colors.grey.shade200,
                                  borderRadius: BorderRadius.circular(4),
                                ),
                                child: Text(
                                  displayCode,
                                  style: const TextStyle(
                                    fontSize: 11,
                                    fontWeight: FontWeight.bold,
                                    color: Colors.black87,
                                  ),
                                ),
                              ),
                              const SizedBox(width: 8),
                              Expanded(
                                child: Text(
                                  activity.content,
                                  style: TextStyle(color: Colors.grey[700], fontSize: 13),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                    );
                  },
                ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildMetricCard(
    BuildContext context, {
    required String title,
    required String count,
    required IconData icon,
    required Color color,
  }) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: Colors.grey.shade200),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.04),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, color: color, size: 28),
          const SizedBox(height: 12),
          Text(
            count,
            style: const TextStyle(
              fontSize: 26,
              fontWeight: FontWeight.w800,
              color: Colors.black87,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            title,
            style: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w600,
              color: Colors.grey[600],
            ),
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
          ),
        ],
      ),
    );
  }

  Widget _buildDqTrendChart(DashboardState state) {
    if (state.dqTrends.isEmpty) return const SizedBox.shrink();
    
    final maxY = state.dqTrends.map((e) => e.count).fold<int>(0, (p, c) => c > p ? c : p).toDouble() + 5;
    
    return Container(
      height: 240,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: Colors.grey.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.trending_up, color: Colors.red.shade700, size: 20),
              const SizedBox(width: 8),
              Text(
                'DQ Trend (7 Days)',
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
              ),
            ],
          ),
          const SizedBox(height: 16),
          Expanded(
            child: LineChart(
              LineChartData(
                gridData: FlGridData(show: true, drawVerticalLine: false),
                titlesData: FlTitlesData(
                  bottomTitles: AxisTitles(
                    sideTitles: SideTitles(
                      showTitles: true,
                      getTitlesWidget: (value, meta) {
                        if (value.toInt() >= 0 && value.toInt() < state.dqTrends.length) {
                          final dateStr = state.dqTrends[value.toInt()].date;
                          final display = dateStr.length > 5 ? dateStr.substring(5) : dateStr;
                          return Padding(
                            padding: const EdgeInsets.only(top: 8.0),
                            child: Text(display, style: const TextStyle(fontSize: 10)),
                          );
                        }
                        return const SizedBox.shrink();
                      },
                      reservedSize: 22,
                    ),
                  ),
                  leftTitles: AxisTitles(
                    sideTitles: SideTitles(showTitles: true, reservedSize: 28),
                  ),
                  topTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
                  rightTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
                ),
                borderData: FlBorderData(show: false),
                minX: 0,
                maxX: (state.dqTrends.length - 1).toDouble(),
                minY: 0,
                maxY: maxY,
                lineBarsData: [
                  LineChartBarData(
                    spots: List.generate(
                      state.dqTrends.length,
                      (index) => FlSpot(index.toDouble(), state.dqTrends[index].count.toDouble()),
                    ),
                    isCurved: true,
                    color: Colors.red.shade600,
                    barWidth: 3,
                    isStrokeCapRound: true,
                    dotData: const FlDotData(show: true),
                    belowBarData: BarAreaData(
                      show: true,
                      color: Colors.red.shade100.withOpacity(0.5),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDqSeverityChart(DashboardState state) {
    if (state.dqSeverity.isEmpty) return const SizedBox.shrink();
    
    final maxY = state.dqSeverity.map((e) => e.count).fold<int>(0, (p, c) => c > p ? c : p).toDouble() + 5;
    
    return Container(
      height: 240,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: Colors.grey.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.warning, color: Colors.amber.shade800, size: 20),
              const SizedBox(width: 8),
              Text(
                AppLocalizations.of(context)!.dqSeverity,
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
              ),
            ],
          ),
          const SizedBox(height: 16),
          Expanded(
            child: BarChart(
              BarChartData(
                alignment: BarChartAlignment.spaceAround,
                maxY: maxY,
                barTouchData: BarTouchData(enabled: true),
                titlesData: FlTitlesData(
                  bottomTitles: AxisTitles(
                    sideTitles: SideTitles(
                      showTitles: true,
                      getTitlesWidget: (value, meta) {
                        if (value.toInt() >= 0 && value.toInt() < state.dqSeverity.length) {
                          return Padding(
                            padding: const EdgeInsets.only(top: 8.0),
                            child: Text(
                              state.dqSeverity[value.toInt()].severity,
                              style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold),
                            ),
                          );
                        }
                        return const SizedBox.shrink();
                      },
                      reservedSize: 28,
                    ),
                  ),
                  leftTitles: AxisTitles(
                    sideTitles: SideTitles(showTitles: true, reservedSize: 28),
                  ),
                  topTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
                  rightTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
                ),
                gridData: FlGridData(show: true, drawVerticalLine: false),
                borderData: FlBorderData(show: false),
                barGroups: List.generate(
                  state.dqSeverity.length,
                  (index) {
                    final sev = state.dqSeverity[index].severity;
                    Color barColor = Colors.blue;
                    if (sev == 'HIGH' || sev == 'CRITICAL') barColor = Colors.red.shade600;
                    if (sev == 'MEDIUM') barColor = Colors.amber.shade600;
                    return BarChartGroupData(
                      x: index,
                      barRods: [
                        BarChartRodData(
                          toY: state.dqSeverity[index].count.toDouble(),
                          color: barColor,
                          width: 20,
                          borderRadius: const BorderRadius.vertical(top: Radius.circular(4)),
                        ),
                      ],
                    );
                  },
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
