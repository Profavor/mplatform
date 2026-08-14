<template>
  <div class="kpi-grid">
    <!-- DQ Score Card -->
    <div class="kpi-card score-card" :class="getScoreClass(scoreData.score)">
      <div class="kpi-card-header">
        <span class="kpi-title">{{ t('dq_score_title', '데이터 품질 점수') }}</span>
        <va-badge :text="getGradeLabel(scoreData.score)" :color="getScoreColor(scoreData.score)" />
      </div>
      <div class="score-body">
        <va-progress-circle
          :model-value="scoreData.score"
          :color="getScoreColor(scoreData.score)"
          size="7.5rem"
          :thickness="0.16"
        >
          <div class="score-gauge-inner">
            <span class="score-number" :style="{ color: `var(--va-${getScoreColor(scoreData.score)})` }">
              {{ scoreData.score }}<span class="score-percent">%</span>
            </span>
          </div>
        </va-progress-circle>
      </div>
    </div>

    <!-- Total Records Card -->
    <div class="kpi-card metric-card records-kpi">
      <div class="kpi-card-header">
        <span class="kpi-title">{{ t('dq_dashboard.total_records', '총 진단 레코드') }}</span>
        <div class="kpi-icon-pill blue-pill">
          <va-icon name="dataset" size="medium" />
        </div>
      </div>
      <div class="metric-body">
        <div class="metric-value blue-text">
          {{ scoreData.totalRecords?.toLocaleString() ?? 0 }}
        </div>
        <div class="metric-subtext">{{ t('dq_dashboard.total_records_sub', '도메인 내 전체 마스터 레코드') }}</div>
      </div>
    </div>

    <!-- Total Violations Card -->
    <div class="kpi-card metric-card violations-kpi">
      <div class="kpi-card-header">
        <span class="kpi-title">{{ t('dq_dashboard.total_violations', '총 품질 오류/위반') }}</span>
        <div class="kpi-icon-pill red-pill">
          <va-icon name="warning" size="medium" />
        </div>
      </div>
      <div class="metric-body">
        <div class="metric-value red-text">
          {{ scoreData.totalViolations?.toLocaleString() ?? 0 }}
        </div>
        <div class="metric-subtext" :class="{ 'has-violations': (scoreData.totalViolations || 0) > 0 }">
          {{ (scoreData.totalViolations || 0) > 0 ? t('dq_dashboard.action_required', '개선 조치 필요') : t('dq_dashboard.all_passed', '모든 검칙 통과') }}
        </div>
      </div>
    </div>

    <!-- Active Rules Card -->
    <div class="kpi-card metric-card rules-kpi">
      <div class="kpi-card-header">
        <span class="kpi-title">{{ t('dq_dashboard.active_dq_rules', '활성 품질 검칙') }}</span>
        <div class="kpi-icon-pill gold-pill">
          <va-icon name="verified_user" size="medium" />
        </div>
      </div>
      <div class="metric-body">
        <div class="metric-value gold-text">
          {{ ruleCount }}
        </div>
        <div class="metric-subtext">{{ t('dq_dashboard.active_rules_sub', '적용 중인 검증 룰') }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

defineProps<{
  scoreData: {
    score: number
    totalRecords?: number
    totalViolations?: number
  }
  ruleCount: number
}>()

const getScoreColor = (score: number) => {
  if (score >= 90) return 'success'
  if (score >= 80) return 'primary'
  if (score >= 70) return 'warning'
  return 'danger'
}

const getScoreClass = (score: number) => {
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 70) return 'score-warning'
  return 'score-critical'
}

const getGradeLabel = (score: number) => {
  if (score >= 90) return 'Grade A (우수)'
  if (score >= 80) return 'Grade B (양호)'
  if (score >= 70) return 'Grade C (보통)'
  return 'Grade D (주의)'
}

defineExpose({
  getScoreColor,
  getScoreClass,
  getGradeLabel
})
</script>

<style scoped>
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1.25rem;
}

.kpi-card {
  background: var(--va-background-primary);
  border: 1px solid var(--va-background-border);
  border-radius: 14px;
  padding: 1.25rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.03);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 160px;
}

.kpi-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.kpi-title {
  font-size: 0.92rem;
  font-weight: 700;
  color: var(--va-text-secondary);
}

.score-card {
  align-items: center;
}

.score-body {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.5rem 0;
}

.score-gauge-inner {
  display: flex;
  align-items: baseline;
  justify-content: center;
}

.score-number {
  font-size: 1.85rem;
  font-weight: 800;
  font-family: 'Pretendard', 'Inter', sans-serif;
  letter-spacing: -0.5px;
}

.score-percent {
  font-size: 1rem;
  font-weight: 600;
}

.metric-body {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.metric-value {
  font-size: 2rem;
  font-weight: 800;
  line-height: 1.2;
  font-family: 'Pretendard', 'Inter', sans-serif;
}

.metric-subtext {
  font-size: 0.78rem;
  color: var(--va-text-secondary);
}

.blue-text { color: var(--va-primary); }
.red-text { color: var(--va-danger); }
.gold-text { color: #f59e0b; }

.kpi-icon-pill {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.blue-pill { background: rgba(37, 99, 235, 0.1); color: var(--va-primary); }
.red-pill { background: rgba(239, 68, 68, 0.1); color: var(--va-danger); }
.gold-pill { background: rgba(245, 158, 11, 0.1); color: #f59e0b; }

.has-violations {
  color: var(--va-danger);
  font-weight: 600;
}
</style>
