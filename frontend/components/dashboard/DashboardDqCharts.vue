<template>
  <div class="content-grid" style="margin-bottom: 1.75rem;">
    <!-- DQ Violation Trend Chart -->
    <va-card class="section-card chart-card">
      <va-card-title class="card-header-title">
        <va-icon name="trending_up" size="small" color="danger" />
        {{ t('dq_violation_trend', '최근 7일간 품질 오류 추이') }}
      </va-card-title>
      <va-card-content>
        <ClientOnly>
          <v-chart style="height: 330px; width: 100%;" :option="dqTrendChartOption" autoresize />
        </ClientOnly>
      </va-card-content>
    </va-card>

    <!-- DQ Severity Distribution Chart -->
    <va-card class="section-card chart-card">
      <va-card-title class="card-header-title">
        <va-icon name="warning" size="small" color="warning" />
        {{ t('dq_severity_distribution', '품질 오류 심각도별 분포') }}
      </va-card-title>
      <va-card-content>
        <ClientOnly>
          <v-chart style="height: 330px; width: 100%;" :option="dqSeverityChartOption" autoresize />
        </ClientOnly>
      </va-card-content>
    </va-card>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

defineProps<{
  dqTrendChartOption: any
  dqSeverityChartOption: any
}>()
</script>

<style scoped>
.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
  gap: 1.25rem;
}

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
</style>
