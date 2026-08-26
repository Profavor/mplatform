<template>
  <va-card outlined class="specialized-widget stock-widget">
    <va-card-title class="widget-header">
      <div class="header-left">
        <va-icon name="candlestick_chart" color="danger" size="small" class="mr-2" />
        <span class="widget-title">{{ $t('specialized_widget_stock') }}</span>
      </div>
      <va-badge
        v-if="marketType"
        :text="marketType"
        color="danger"
        size="small"
      />
    </va-card-title>
    <va-card-content class="widget-content">
      <div class="stock-title-row">
        <span class="stock-name">{{ stockName }}</span>
        <span v-if="tickerCode" class="ticker-badge">{{ tickerCode }}</span>
        <va-chip v-if="isinCode" size="small" preset="outline" color="secondary">
          {{ isinCode }}
        </va-chip>
      </div>

      <div class="sector-text" v-if="industrySector">
        <va-icon name="category" size="14px" color="secondary" class="mr-1" />
        <span>{{ industrySector }}</span>
      </div>

      <div class="stock-metrics-grid">
        <div class="metric-box">
          <span class="metric-label">{{ $t('par_value') }}</span>
          <span class="metric-val">{{ formattedParValue }}</span>
        </div>
        <div class="metric-box">
          <span class="metric-label">{{ $t('listed_shares') }}</span>
          <span class="metric-val">{{ formattedListedShares }}</span>
        </div>
        <div class="metric-box capital-box" v-if="estimatedCapital !== null">
          <span class="metric-label">{{ $t('market_cap_est') }}</span>
          <span class="metric-val capital">{{ formattedEstimatedCapital }}</span>
        </div>
      </div>
    </va-card-content>
  </va-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  recordData: Record<string, any>
  domain?: Record<string, any>
}>()

const tickerCode = computed(() => props.recordData?.ticker_code || '')
const isinCode = computed(() => props.recordData?.isin_code || '')
const stockName = computed(() => props.recordData?.stock_name || '-')
const marketType = computed(() => props.recordData?.market_type || '')
const industrySector = computed(() => props.recordData?.industry_sector || '')
const parValue = computed(() => Number(props.recordData?.par_value) || 0)
const listedShares = computed(() => Number(props.recordData?.listed_shares) || 0)
const currency = computed(() => props.recordData?.currency || 'KRW')

const formatNumber = (val: number) => {
  return new Intl.NumberFormat('ko-KR').format(val)
}

const formattedParValue = computed(() => {
  return `${formatNumber(parValue.value)} ${currency.value}`
})

const formattedListedShares = computed(() => {
  return `${formatNumber(listedShares.value)} 주`
})

const estimatedCapital = computed(() => {
  if (parValue.value <= 0 || listedShares.value <= 0) return null
  return parValue.value * listedShares.value
})

const formattedEstimatedCapital = computed(() => {
  if (!estimatedCapital.value) return '-'
  return `${formatNumber(estimatedCapital.value)} ${currency.value}`
})
</script>

<style scoped>
.specialized-widget {
  border-radius: 10px;
  background: var(--va-background-primary);
  margin-bottom: 1rem;
}
.widget-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid var(--va-background-border);
}
.header-left {
  display: flex;
  align-items: center;
}
.widget-title {
  font-weight: 700;
  font-size: 0.95rem;
  color: var(--va-text-primary);
}
.widget-content {
  padding: 1rem;
}
.stock-title-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.35rem;
  flex-wrap: wrap;
}
.stock-name {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--va-text-primary);
}
.ticker-badge {
  font-size: 0.8rem;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  font-family: monospace;
}
.sector-text {
  display: flex;
  align-items: center;
  font-size: 0.85rem;
  color: var(--va-text-secondary);
  margin-bottom: 1rem;
}
.stock-metrics-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.75rem;
}
.metric-box {
  background: var(--va-background-element);
  padding: 0.65rem 0.75rem;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
}
.metric-label {
  font-size: 0.75rem;
  color: var(--va-text-secondary);
  margin-bottom: 0.2rem;
}
.metric-val {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--va-text-primary);
}
.metric-val.capital {
  color: #ef4444;
}
</style>
