<template>
  <div class="executive-panel">
    <!-- Strategy Header & KPI Badges -->
    <div class="panel-header">
      <div class="panel-title-group">
        <va-icon name="auto_awesome" color="warning" size="20px" />
        <span class="panel-title-text">
          {{ t('survivorship.guide_title', '생존 규칙(Survivorship) 전략 가이드') }}
        </span>
      </div>

      <div v-if="selectedDomainId" class="panel-kpi-group">
        <div class="kpi-chip">
          <va-icon name="rule" size="16px" color="primary" />
          <span class="kpi-chip-label">{{ t('survivorship.kpi_rules', '등록 규칙:') }}</span>
          <span class="kpi-chip-value primary-val">{{ rulesCount }}</span>
        </div>

        <div class="kpi-chip">
          <va-icon name="category" size="16px" color="info" />
          <span class="kpi-chip-label">{{ t('survivorship.kpi_fields', '도메인 필드:') }}</span>
          <span class="kpi-chip-value info-val">{{ domainFieldsCount }}</span>
        </div>

        <div class="kpi-chip">
          <va-icon name="verified" size="16px" color="success" />
          <span class="kpi-chip-label">{{ t('survivorship.kpi_domain', '선택 도메인:') }}</span>
          <span class="kpi-chip-value success-val">{{ currentDomainName }}</span>
        </div>
      </div>
    </div>

    <!-- Strategy Cards Grid -->
    <div class="strategy-cards-grid">
      <div
        v-for="opt in strategyOptions"
        :key="opt.value"
        class="strategy-card"
        :class="'border-' + getStrategyColor(opt.value)"
      >
        <div class="strategy-icon-box" :class="'bg-' + getStrategyColor(opt.value) + '-subtle'">
          <va-icon :name="getStrategyIcon(opt.value)" :color="getStrategyColor(opt.value)" size="20px" />
        </div>
        <div class="strategy-body">
          <div class="strategy-title">
            <span>{{ opt.value }}</span>
            <span class="badge-tag" :class="'tag-' + getStrategyColor(opt.value)">{{ opt.text }}</span>
          </div>
          <p class="strategy-desc">
            {{ getStrategyDesc(opt.value) }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

defineProps<{
  selectedDomainId: string | null
  rulesCount: number
  domainFieldsCount: number
  currentDomainName: string
  strategyOptions: Array<{ value: string; text: string }>
}>()

const getStrategyColor = (strategy: string) => {
  if (strategy === 'SOURCE_PRIORITY') return 'primary'
  if (strategy === 'MOST_RECENT') return 'warning'
  if (strategy === 'MOST_COMPLETE') return 'success'
  return 'info'
}

const getStrategyIcon = (strategy: string) => {
  if (strategy === 'SOURCE_PRIORITY') return 'hub'
  if (strategy === 'MOST_RECENT') return 'history'
  if (strategy === 'MOST_COMPLETE') return 'verified'
  return 'rule'
}

const getStrategyDesc = (strategy: string) => {
  if (strategy === 'SOURCE_PRIORITY') {
    return t('survivorship.strategy_desc_source_priority')
  }
  if (strategy === 'MOST_RECENT') {
    return t('survivorship.strategy_desc_most_recent')
  }
  if (strategy === 'MOST_COMPLETE') {
    return t('survivorship.strategy_desc_most_complete')
  }
  return ''
}
</script>

<style scoped>
.executive-panel {
  background: var(--va-background-primary);
  border: 1px solid var(--va-background-border);
  border-radius: 14px;
  padding: 1.25rem;
  box-shadow: 0 4px 16px rgba(0,0,0,0.03);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.panel-title-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.panel-title-text {
  font-weight: 700;
  font-size: 1rem;
  color: var(--va-text-primary);
}

.panel-kpi-group {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.kpi-chip {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  background: var(--va-background-element);
  border: 1px solid var(--va-background-border);
  padding: 0.3rem 0.65rem;
  border-radius: 8px;
  font-size: 0.78rem;
}

.kpi-chip-label {
  color: var(--va-text-secondary);
  font-weight: 600;
}

.kpi-chip-value {
  font-weight: 800;
}

.primary-val { color: var(--va-primary); }
.info-val { color: var(--va-info); }
.success-val { color: var(--va-success); }

.strategy-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1rem;
}

.strategy-card {
  display: flex;
  gap: 0.85rem;
  padding: 0.9rem;
  border-radius: 10px;
  background: var(--va-background-element);
  border-left: 4px solid;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.strategy-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.border-primary { border-left-color: var(--va-primary); }
.border-warning { border-left-color: #f59e0b; }
.border-success { border-left-color: #10b981; }
.border-info { border-left-color: #3b82f6; }

.strategy-icon-box {
  width: 38px;
  height: 38px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.bg-primary-subtle { background: rgba(37, 99, 235, 0.1); }
.bg-warning-subtle { background: rgba(245, 158, 11, 0.1); }
.bg-success-subtle { background: rgba(16, 185, 129, 0.1); }
.bg-info-subtle { background: rgba(59, 130, 246, 0.1); }

.strategy-body {
  flex: 1;
  min-width: 0;
}

.strategy-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--va-text-primary);
  margin-bottom: 0.25rem;
}

.badge-tag {
  font-size: 0.7rem;
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 600;
}

.tag-primary { background: rgba(37, 99, 235, 0.15); color: var(--va-primary); }
.tag-warning { background: rgba(245, 158, 11, 0.15); color: #d97706; }
.tag-success { background: rgba(16, 185, 129, 0.15); color: #059669; }
.tag-info { background: rgba(59, 130, 246, 0.15); color: #2563eb; }

.strategy-desc {
  font-size: 0.78rem;
  color: var(--va-text-secondary);
  line-height: 1.35;
  margin: 0;
}
</style>
