<template>
  <div class="kpi-grid">
    <!-- Total Domains -->
    <div class="kpi-card">
      <div class="kpi-card-header">
        <span class="kpi-title">{{ t('total_domains', '총 도메인 수') }}</span>
        <div class="kpi-icon-pill blue-pill">
          <va-icon name="domain" size="medium" />
        </div>
      </div>
      <div class="metric-body">
        <div class="metric-value blue-text">
          {{ stats?.totalDomains?.toLocaleString() ?? 0 }}
        </div>
        <div class="metric-subtext">{{ t('registered_domains', '등록된 기준 도메인') }}</div>
      </div>
    </div>

    <!-- Pending Approvals -->
    <div class="kpi-card">
      <div class="kpi-card-header">
        <span class="kpi-title">{{ t('pending_approvals', '미결 결재') }}</span>
        <div class="kpi-icon-pill red-pill">
          <va-icon name="pending_actions" size="medium" />
        </div>
      </div>
      <div class="metric-body">
        <div class="metric-value red-text">
          {{ stats?.pendingApprovals?.toLocaleString() ?? 0 }}
        </div>
        <div class="metric-subtext" :class="{ 'has-pending': (stats?.pendingApprovals || 0) > 0 }">
          {{ (stats?.pendingApprovals || 0) > 0 ? t('action_required', '승인 조치 필요') : t('all_tasks_cleared', '모든 결재 완료') }}
        </div>
      </div>
    </div>

    <!-- Active Records -->
    <div class="kpi-card">
      <div class="kpi-card-header">
        <span class="kpi-title">{{ t('active_records', '활성 마스터 레코드') }}</span>
        <div class="kpi-icon-pill green-pill">
          <va-icon name="inventory_2" size="medium" />
        </div>
      </div>
      <div class="metric-body">
        <div class="metric-value green-text">
          {{ stats?.activeRecords?.toLocaleString() ?? 0 }}
        </div>
        <div class="metric-subtext">{{ t('managed_master_records', '관리 중인 마스터 데이터') }}</div>
      </div>
    </div>

    <!-- Pending Match Candidates -->
    <div class="kpi-card">
      <div class="kpi-card-header">
        <span class="kpi-title">{{ t('pending_match_candidates', '매칭 검토 대기') }}</span>
        <div class="kpi-icon-pill purple-pill">
          <va-icon name="fact_check" size="medium" />
        </div>
      </div>
      <div class="metric-body">
        <div class="metric-value purple-text">
          {{ stats?.pendingMatches?.toLocaleString() ?? 0 }}
        </div>
        <div class="metric-subtext">{{ t('potential_duplicates', '중복 의심 레코드') }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

defineProps<{
  stats?: {
    totalDomains?: number
    pendingApprovals?: number
    activeRecords?: number
    pendingMatches?: number
  } | null
}>()
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
  min-height: 140px;
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
.green-text { color: #10b981; }
.purple-text { color: #8b5cf6; }

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
.green-pill { background: rgba(16, 185, 129, 0.1); color: #10b981; }
.purple-pill { background: rgba(139, 92, 246, 0.1); color: #8b5cf6; }

.has-pending {
  color: var(--va-danger);
  font-weight: 600;
}
</style>
