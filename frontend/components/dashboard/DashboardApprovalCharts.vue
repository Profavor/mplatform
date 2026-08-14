<template>
  <div class="content-grid">
    <!-- 7-Day Approval Requests Trend Chart -->
    <va-card class="section-card chart-card">
      <va-card-title class="card-header-title">
        <va-icon name="show_chart" size="small" color="primary" />
        {{ t('approval_trend_title', '최근 7일간 결재 요청 추이') }}
      </va-card-title>
      <va-card-content>
        <ClientOnly>
          <v-chart style="height: 330px; width: 100%;" :option="trendChartOption" autoresize />
        </ClientOnly>
      </va-card-content>
    </va-card>

    <!-- Master Record Distribution Donut Chart -->
    <va-card class="section-card chart-card">
      <va-card-title class="card-header-title">
        <va-icon name="pie_chart" size="small" color="info" />
        {{ t('domain_distribution_title', '도메인별 마스터 데이터 분포') }}
      </va-card-title>
      <va-card-content>
        <ClientOnly>
          <v-chart style="height: 330px; width: 100%;" :option="distributionChartOption" autoresize />
        </ClientOnly>
      </va-card-content>
    </va-card>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

defineProps<{
  trendChartOption: any
  distributionChartOption: any
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
