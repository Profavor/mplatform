<template>
  <va-card class="section-card governance-card">
    <va-card-title class="card-header-title">
      <va-icon name="health_and_safety" size="small" color="success" />
      {{ t('governance_health_title', '거버넌스 및 데이터 건전성 현황') }}
    </va-card-title>
    <va-card-content>
      <div class="health-indicators-grid">
        <!-- Open DQ Violations -->
        <div class="health-item-card">
          <div class="health-item-header">
            <va-icon name="warning_amber" color="danger" size="1.5rem" />
            <span class="health-item-title">{{ t('open_dq_violations', '미결 품질 검칙 오류') }}</span>
          </div>
          <div class="health-item-body">
            <span class="health-value text-danger">{{ stats?.openDqViolations ?? 0 }}</span>
            <va-button size="small" color="danger" preset="secondary" icon="arrow_forward" @click="navigateTo('/dq-dashboard')">
              {{ t('go_to_dq_dashboard', 'DQ 대시보드 바로가기') }}
            </va-button>
          </div>
        </div>

        <!-- Pending Match Candidates -->
        <div class="health-item-card">
          <div class="health-item-header">
            <va-icon name="find_in_page" color="warning" size="1.5rem" />
            <span class="health-item-title">{{ t('pending_match_candidates', '매칭 검토 대기') }}</span>
          </div>
          <div class="health-item-body">
            <span class="health-value text-warning">{{ stats?.pendingMatches ?? 0 }}</span>
            <va-button size="small" color="warning" preset="secondary" icon="arrow_forward" @click="navigateTo('/admin/match-review')">
              {{ t('go_to_match_review', '매칭 검토 바로가기') }}
            </va-button>
          </div>
        </div>

        <!-- Approval Success Rate -->
        <div class="health-item-card">
          <div class="health-item-header">
            <va-icon name="verified" color="primary" size="1.5rem" />
            <span class="health-item-title">{{ t('approval_success_rate', '결재 승인율') }}</span>
          </div>
          <div class="health-item-body">
            <span class="health-value text-primary">
              {{ getApprovalRate() }}%
            </span>
            <span class="health-subtext">
              {{ t('approval_stats_summary', { approved: stats?.approvedApprovals ?? 0, rejected: stats?.rejectedApprovals ?? 0 }) }}
            </span>
          </div>
        </div>
      </div>
    </va-card-content>
  </va-card>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

const { t } = useI18n()
const router = useRouter()

const props = defineProps<{
  stats?: {
    openDqViolations?: number
    pendingMatches?: number
    approvedApprovals?: number
    rejectedApprovals?: number
  } | null
}>()

const getApprovalRate = () => {
  const approved = props.stats?.approvedApprovals ?? 0
  const rejected = props.stats?.rejectedApprovals ?? 0
  const total = approved + rejected
  if (total === 0) return 0
  return Math.round((approved / total) * 100)
}

const navigateTo = (path: string) => {
  if (router) {
    router.push(path)
  }
}

defineExpose({
  getApprovalRate
})
</script>

<style scoped>
.section-card {
  border-radius: 12px;
  border: 1px solid var(--va-background-border);
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
  background: var(--va-background-primary);
}

.card-header-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--va-text-primary);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem 1.25rem 0.5rem 1.25rem;
}

.health-indicators-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1.25rem;
  padding: 0.5rem 0;
}

.health-item-card {
  background: var(--va-background-element);
  border: 1px solid var(--va-background-border);
  border-radius: 10px;
  padding: 1rem 1.25rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 0.75rem;
}

.health-item-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.health-item-title {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--va-text-secondary);
}

.health-item-body {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.health-value {
  font-size: 1.6rem;
  font-weight: 800;
  line-height: 1;
}

.text-danger { color: var(--va-danger); }
.text-warning { color: #f59e0b; }
.text-primary { color: var(--va-primary); }

.health-subtext {
  font-size: 0.72rem;
  color: var(--va-text-secondary);
  max-width: 140px;
  text-align: right;
  line-height: 1.3;
}
</style>
