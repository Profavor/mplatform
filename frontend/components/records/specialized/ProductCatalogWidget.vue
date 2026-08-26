<template>
  <va-card outlined class="specialized-widget product-widget">
    <va-card-title class="widget-header">
      <div class="header-left">
        <va-icon name="shopping_bag" color="success" size="small" class="mr-2" />
        <span class="widget-title">{{ $t('specialized_widget_product') }}</span>
      </div>
      <va-badge
        :text="isActive ? $t('active_status') : $t('inactive_status')"
        :color="isActive ? 'success' : 'secondary'"
        size="small"
      />
    </va-card-title>
    <va-card-content class="widget-content">
      <div class="product-main-info">
        <div class="product-title-row">
          <span class="product-name">{{ productName }}</span>
          <span v-if="skuCode" class="sku-badge">{{ skuCode }}</span>
        </div>
        <div class="brand-barcode-row">
          <va-chip v-if="brand" size="small" preset="outline" color="primary">
            {{ brand }}
          </va-chip>
          <va-chip v-if="barcode" size="small" preset="outline" color="secondary" icon="qr_code_2">
            {{ barcode }}
          </va-chip>
        </div>
      </div>

      <!-- Pricing & Margin Grid -->
      <div class="price-margin-grid">
        <div class="price-box">
          <span class="price-box-label">{{ $t('cost_price') }}</span>
          <span class="price-box-value">{{ formattedCostPrice }}</span>
        </div>
        <div class="price-box">
          <span class="price-box-label">{{ $t('retail_price') }}</span>
          <span class="price-box-value retail">{{ formattedRetailPrice }}</span>
        </div>
        <div class="price-box margin-box" v-if="marginRate !== null">
          <span class="price-box-label">{{ $t('margin_rate') }}</span>
          <span class="price-box-value margin" :class="{ positive: marginRate > 0 }">
            {{ marginRate }}%
          </span>
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

const skuCode = computed(() => props.recordData?.sku_code || '')
const productName = computed(() => props.recordData?.product_name || '-')
const barcode = computed(() => props.recordData?.barcode || '')
const brand = computed(() => props.recordData?.brand || '')
const retailPrice = computed(() => Number(props.recordData?.retail_price) || 0)
const costPrice = computed(() => Number(props.recordData?.cost_price) || 0)
const isActive = computed(() => props.recordData?.is_active !== false)

const formatKRW = (val: number) => {
  return new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW' }).format(val)
}

const formattedRetailPrice = computed(() => formatKRW(retailPrice.value))
const formattedCostPrice = computed(() => formatKRW(costPrice.value))

const marginRate = computed(() => {
  if (retailPrice.value <= 0) return null
  const profit = retailPrice.value - costPrice.value
  return Math.round((profit / retailPrice.value) * 1000) / 10
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
.product-title-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.35rem;
}
.product-name {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--va-text-primary);
}
.sku-badge {
  font-size: 0.75rem;
  padding: 2px 6px;
  border-radius: 4px;
  background: var(--va-background-element);
  color: var(--va-text-secondary);
  font-family: monospace;
}
.brand-barcode-row {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}
.price-margin-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.75rem;
}
.price-box {
  background: var(--va-background-element);
  padding: 0.65rem 0.75rem;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
}
.price-box-label {
  font-size: 0.75rem;
  color: var(--va-text-secondary);
  margin-bottom: 0.2rem;
}
.price-box-value {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--va-text-primary);
}
.price-box-value.retail {
  color: var(--va-primary);
}
.price-box-value.margin.positive {
  color: #10b981;
}
</style>
