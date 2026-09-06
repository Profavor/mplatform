<template>
  <div class="dq-benchmark-matrix-container">
    <!-- 4 Summary KPI Cards -->
    <div class="benchmark-kpi-grid">
      <!-- Monitored Domains -->
      <va-card class="kpi-card">
        <va-card-content class="kpi-content">
          <div class="kpi-header">
            <span class="kpi-label">{{ t('benchmark.kpi_monitored_domains') }}</span>
            <div class="kpi-icon-pill blue">
              <va-icon name="domain" size="small" />
            </div>
          </div>
          <div class="kpi-value text-blue">
            {{ benchmarkData?.totalMonitoredDomains ?? 0 }}
          </div>
          <div class="kpi-subtext">
            {{ benchmarkData?.items?.length ?? 0 }} {{ t('benchmark.kpi_monitored_domains') }}
          </div>
        </va-card-content>
      </va-card>

      <!-- Average Score -->
      <va-card class="kpi-card">
        <va-card-content class="kpi-content">
          <div class="kpi-header">
            <span class="kpi-label">{{ t('benchmark.kpi_average_score') }}</span>
            <div class="kpi-icon-pill green">
              <va-icon name="speed" size="small" />
            </div>
          </div>
          <div class="kpi-value text-green">
            {{ benchmarkData?.averageScore ?? 0 }}<span class="unit">%</span>
          </div>
          <div class="kpi-subtext">
            <va-badge
              :text="getGradeBadge(benchmarkData?.averageScore)"
              :color="getGradeColor(benchmarkData?.averageScore)"
              size="small"
            />
          </div>
        </va-card-content>
      </va-card>

      <!-- Total Violations -->
      <va-card class="kpi-card">
        <va-card-content class="kpi-content">
          <div class="kpi-header">
            <span class="kpi-label">{{ t('benchmark.kpi_total_violations') }}</span>
            <div class="kpi-icon-pill amber">
              <va-icon name="warning" size="small" />
            </div>
          </div>
          <div class="kpi-value text-amber">
            {{ (benchmarkData?.totalViolations ?? 0).toLocaleString() }}
          </div>
          <div class="kpi-subtext text-muted">
            {{ t('benchmark.kpi_total_violations') }}
          </div>
        </va-card-content>
      </va-card>

      <!-- High Risk Domains -->
      <va-card class="kpi-card">
        <va-card-content class="kpi-content">
          <div class="kpi-header">
            <span class="kpi-label">{{ t('benchmark.kpi_high_risk_domains') }}</span>
            <div class="kpi-icon-pill red">
              <va-icon name="error_outline" size="small" />
            </div>
          </div>
          <div class="kpi-value text-red">
            {{ benchmarkData?.highRiskDomainCount ?? 0 }}
          </div>
          <div class="kpi-subtext" :class="{ 'text-red': (benchmarkData?.highRiskDomainCount ?? 0) > 0 }">
            {{ (benchmarkData?.highRiskDomainCount ?? 0) > 0 ? t('benchmark.risk_high_risk') : t('benchmark.risk_healthy') }}
          </div>
        </va-card-content>
      </va-card>
    </div>

    <!-- Benchmark Matrix Table Card -->
    <va-card class="matrix-card">
      <div class="matrix-card-header">
        <div class="title-group">
          <va-icon name="view_list" color="primary" />
          <div>
            <h3 class="card-title">{{ t('benchmark.matrix_title') }}</h3>
            <p class="card-desc">{{ t('benchmark.matrix_desc') }}</p>
          </div>
        </div>
        <va-button
          preset="outline"
          color="primary"
          icon="refresh"
          size="small"
          :loading="loading"
          @click="emit('refresh')"
        >
          {{ t('refresh') }}
        </va-button>
      </div>

      <va-card-content class="matrix-card-content">
        <div v-if="!benchmarkData?.items || benchmarkData.items.length === 0" class="empty-state">
          <va-icon name="inbox" size="3rem" color="secondary" />
          <p class="empty-text">{{ t('benchmark.no_benchmark_data') }}</p>
        </div>

        <div v-else class="table-responsive">
          <table class="benchmark-table">
            <thead>
              <tr>
                <th class="text-left">{{ t('benchmark.col_domain') }}</th>
                <th class="text-center">{{ t('benchmark.col_score') }}</th>
                <th class="text-center">{{ t('benchmark.col_grade') }}</th>
                <th class="text-center">{{ t('benchmark.col_risk') }}</th>
                <th class="text-right">{{ t('benchmark.col_records') }}</th>
                <th class="text-right">{{ t('benchmark.col_rules') }}</th>
                <th class="text-right">{{ t('benchmark.col_violations') }}</th>
                <th class="text-right">{{ t('benchmark.col_severity_error') }}</th>
                <th class="text-right">{{ t('benchmark.col_severity_warn') }}</th>
                <th class="text-center">{{ t('benchmark.col_trend') }}</th>
                <th class="text-center">{{ t('details') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="item in benchmarkData.items"
                :key="item.domainId"
                class="benchmark-row"
                @click="emit('select-domain', item.domainId)"
              >
                <!-- Domain Info -->
                <td class="domain-col">
                  <div class="domain-info-cell">
                    <span class="domain-name">{{ getLocalizedName(item.domainName, item.domainCode) }}</span>
                    <span class="domain-code">{{ item.domainCode }}</span>
                  </div>
                </td>

                <!-- Score with Mini Bar -->
                <td class="text-center score-col">
                  <div class="score-cell-wrap">
                    <span class="score-text" :class="getScoreTextColorClass(item.score)">
                      {{ item.score }}%
                    </span>
                    <div class="score-track">
                      <div
                        class="score-fill"
                        :style="{ width: `${item.score}%`, backgroundColor: getGradeColor(item.score) }"
                      ></div>
                    </div>
                  </div>
                </td>

                <!-- Grade -->
                <td class="text-center">
                  <span class="grade-badge" :style="{ borderColor: getGradeColor(item.score), color: getGradeColor(item.score) }">
                    {{ item.grade }}
                  </span>
                </td>

                <!-- Risk Level -->
                <td class="text-center">
                  <va-badge
                    :text="getRiskLevelLabel(item.riskLevel)"
                    :color="getRiskBadgeColor(item.riskLevel)"
                    size="small"
                  />
                </td>

                <!-- Records -->
                <td class="text-right font-mono">
                  {{ item.totalRecords?.toLocaleString() ?? 0 }}
                </td>

                <!-- Active Rules -->
                <td class="text-right font-mono">
                  {{ item.ruleCount ?? 0 }}
                </td>

                <!-- Total Violations -->
                <td class="text-right font-mono" :class="{ 'text-danger font-bold': item.totalViolations > 0 }">
                  {{ item.totalViolations?.toLocaleString() ?? 0 }}
                </td>

                <!-- Errors -->
                <td class="text-right font-mono text-danger">
                  {{ item.errorCount?.toLocaleString() ?? 0 }}
                </td>

                <!-- Warnings -->
                <td class="text-right font-mono text-warning">
                  {{ item.warningCount?.toLocaleString() ?? 0 }}
                </td>

                <!-- Trend Delta -->
                <td class="text-center">
                  <span
                    class="trend-delta-badge"
                    :class="{
                      'delta-up': item.trendDelta > 0,
                      'delta-down': item.trendDelta < 0,
                      'delta-flat': item.trendDelta === 0
                    }"
                  >
                    <va-icon
                      :name="item.trendDelta > 0 ? 'trending_up' : (item.trendDelta < 0 ? 'trending_down' : 'trending_flat')"
                      size="14px"
                    />
                    {{ item.trendDelta > 0 ? `+${item.trendDelta}` : item.trendDelta }}%
                  </span>
                </td>

                <!-- Action Button -->
                <td class="text-center" @click.stop>
                  <va-button
                    preset="secondary"
                    size="small"
                    icon="arrow_forward"
                    @click="emit('select-domain', item.domainId)"
                  >
                    {{ t('benchmark.action_go_detail') }}
                  </va-button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </va-card-content>
    </va-card>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

interface BenchmarkItem {
  domainId: string
  domainCode: string
  domainName: Record<string, string>
  score: number
  grade: string
  totalRecords: number
  totalViolations: number
  errorCount: number
  warningCount: number
  ruleCount: number
  riskLevel: string
  trendDelta: number
  dimensionScores?: Record<string, number>
}

interface OverviewResponse {
  averageScore: number
  totalMonitoredDomains: number
  highestDomain: BenchmarkItem | null
  lowestDomain: BenchmarkItem | null
  totalViolations: number
  highRiskDomainCount: number
  items: BenchmarkItem[]
}

const props = defineProps<{
  benchmarkData: OverviewResponse | null
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'select-domain', domainId: string): void
  (e: 'refresh'): void
}>()

const { t, locale } = useI18n()

function getLocalizedName(domainName?: Record<string, string>, fallback = ''): string {
  if (!domainName) return fallback
  return domainName[locale.value] || domainName['ko'] || domainName['en'] || fallback
}

function getGradeBadge(score = 0): string {
  if (score >= 90) return 'A'
  if (score >= 80) return 'B'
  if (score >= 70) return 'C'
  return 'D'
}

function getGradeColor(score = 0): string {
  if (score >= 90) return '#10b981'
  if (score >= 80) return '#3b82f6'
  if (score >= 70) return '#f59e0b'
  return '#ef4444'
}

function getScoreTextColorClass(score = 0): string {
  if (score >= 90) return 'text-success'
  if (score >= 80) return 'text-primary'
  if (score >= 70) return 'text-warning'
  return 'text-danger'
}

function getRiskLevelLabel(riskLevel: string): string {
  if (riskLevel === 'HEALTHY') return t('benchmark.risk_healthy')
  if (riskLevel === 'WARNING') return t('benchmark.risk_warning')
  return t('benchmark.risk_high_risk')
}

function getRiskBadgeColor(riskLevel: string): string {
  if (riskLevel === 'HEALTHY') return 'success'
  if (riskLevel === 'WARNING') return 'warning'
  return 'danger'
}
</script>

<style scoped>
.dq-benchmark-matrix-container {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.benchmark-kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
}

.kpi-card {
  border-radius: 12px;
  border: 1px solid var(--va-background-border);
  background: var(--va-background-primary);
}

.kpi-content {
  padding: 1rem 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.kpi-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.kpi-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--va-text-secondary);
}

.kpi-icon-pill {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.kpi-icon-pill.blue { background: rgba(59, 130, 246, 0.12); color: #3b82f6; }
.kpi-icon-pill.green { background: rgba(16, 185, 129, 0.12); color: #10b981; }
.kpi-icon-pill.amber { background: rgba(245, 158, 11, 0.12); color: #f59e0b; }
.kpi-icon-pill.red { background: rgba(239, 68, 68, 0.12); color: #ef4444; }

.kpi-value {
  font-size: 1.8rem;
  font-weight: 800;
  font-family: 'Pretendard', 'Inter', sans-serif;
  line-height: 1.2;
}

.kpi-value .unit {
  font-size: 1.1rem;
  font-weight: 600;
  margin-left: 2px;
}

.text-blue { color: #3b82f6; }
.text-green { color: #10b981; }
.text-amber { color: #f59e0b; }
.text-red { color: #ef4444; }

.kpi-subtext {
  font-size: 0.78rem;
  color: var(--va-text-secondary);
}

.matrix-card {
  border-radius: 12px;
  border: 1px solid var(--va-background-border);
  background: var(--va-background-primary);
  overflow: hidden;
}

.matrix-card-header {
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--va-background-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.title-group {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.card-title {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--va-text-primary);
}

.card-desc {
  margin: 0;
  font-size: 0.8rem;
  color: var(--va-text-secondary);
}

.matrix-card-content {
  padding: 0;
}

.table-responsive {
  width: 100%;
  overflow-x: auto;
}

.benchmark-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.85rem;
}

.benchmark-table th {
  padding: 0.75rem 1rem;
  background: var(--va-background-element);
  color: var(--va-text-secondary);
  font-weight: 600;
  border-bottom: 1px solid var(--va-background-border);
  white-space: nowrap;
}

.benchmark-table td {
  padding: 0.85rem 1rem;
  border-bottom: 1px solid var(--va-background-border);
  color: var(--va-text-primary);
  vertical-align: middle;
}

.benchmark-row {
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.benchmark-row:hover {
  background-color: rgba(var(--va-primary-rgb, 59, 130, 246), 0.05);
}

.domain-info-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.domain-name {
  font-weight: 700;
  color: var(--va-text-primary);
}

.domain-code {
  font-size: 0.75rem;
  font-family: monospace;
  color: var(--va-text-secondary);
}

.score-cell-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  min-width: 80px;
}

.score-text {
  font-weight: 800;
  font-size: 0.9rem;
}

.score-track {
  width: 100%;
  height: 4px;
  background: var(--va-background-element);
  border-radius: 2px;
  overflow: hidden;
}

.score-fill {
  height: 100%;
  border-radius: 2px;
}

.grade-badge {
  display: inline-block;
  width: 26px;
  height: 26px;
  line-height: 24px;
  text-align: center;
  font-weight: 800;
  border: 1.5px solid;
  border-radius: 6px;
  font-size: 0.85rem;
}

.font-mono {
  font-family: monospace;
}

.font-bold {
  font-weight: 700;
}

.text-danger {
  color: #ef4444;
}

.text-warning {
  color: #f59e0b;
}

.text-success {
  color: #10b981;
}

.text-primary {
  color: #3b82f6;
}

.trend-delta-badge {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 700;
  font-family: monospace;
}

.delta-up {
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
}

.delta-down {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.delta-flat {
  color: var(--va-text-secondary);
  background: var(--va-background-element);
}

.empty-state {
  padding: 3rem 1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  text-align: center;
}

.empty-text {
  font-size: 0.9rem;
  color: var(--va-text-secondary);
}
</style>
