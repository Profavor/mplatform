<template>
  <va-card outlined class="specialized-widget vendor-widget">
    <va-card-title class="widget-header">
      <div class="header-left">
        <va-icon name="corporate_fare" color="warning" size="small" class="mr-2" />
        <span class="widget-title">{{ $t('specialized_widget_vendor') }}</span>
      </div>
      <span v-if="vendorCode" class="vendor-code-badge">{{ vendorCode }}</span>
    </va-card-title>
    <va-card-content class="widget-content">
      <div class="vendor-main-row">
        <div class="vendor-name-group">
          <span class="vendor-name">{{ vendorName }}</span>
          <div class="biz-info-chips">
            <va-chip size="small" preset="outline" color="warning" icon="badge">
              {{ $t('biz_no_formatted') }}: {{ formattedBizNo }}
            </va-chip>
            <va-chip v-if="ceoName" size="small" preset="outline" color="secondary" icon="person">
              {{ $t('ceo_name') }}: {{ ceoName }}
            </va-chip>
          </div>
        </div>
      </div>

      <div class="credit-section" v-if="creditLimit">
        <div class="credit-header">
          <span class="credit-title">{{ $t('credit_limit_label') }}</span>
          <span class="credit-amount">{{ formattedCreditLimit }}</span>
        </div>
        <va-progress-bar :model-value="100" color="warning" size="small" class="credit-progress" />
      </div>

      <div class="vendor-footer" v-if="paymentTerms">
        <span class="meta-label">{{ $t('payment_terms') }}:</span>
        <va-badge :text="paymentTerms" color="info" size="small" />
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

const vendorCode = computed(() => props.recordData?.vendor_code || '')
const vendorName = computed(() => props.recordData?.vendor_name || '-')
const rawBizNo = computed(() => String(props.recordData?.biz_reg_no || ''))
const ceoName = computed(() => props.recordData?.ceo_name || '')
const creditLimit = computed(() => props.recordData?.credit_limit)
const paymentTerms = computed(() => props.recordData?.payment_terms || '')

const formattedBizNo = computed(() => {
  const digits = rawBizNo.value.replace(/\D/g, '')
  if (digits.length === 10) {
    return `${digits.slice(0, 3)}-${digits.slice(3, 5)}-${digits.slice(5)}`
  }
  return rawBizNo.value || '-'
})

const formattedCreditLimit = computed(() => {
  const val = Number(creditLimit.value)
  if (isNaN(val)) return '-'
  return new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW' }).format(val)
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
.vendor-code-badge {
  font-size: 0.75rem;
  padding: 2px 6px;
  border-radius: 4px;
  background: var(--va-background-element);
  color: var(--va-text-secondary);
  font-family: monospace;
}
.widget-content {
  padding: 1rem;
}
.vendor-name {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--va-text-primary);
  display: block;
  margin-bottom: 0.5rem;
}
.biz-info-chips {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}
.credit-section {
  margin-top: 1rem;
  padding: 0.75rem;
  border-radius: 8px;
  background: var(--va-background-element);
}
.credit-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.35rem;
}
.credit-title {
  font-size: 0.8rem;
  color: var(--va-text-secondary);
}
.credit-amount {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--va-text-primary);
}
.credit-progress {
  border-radius: 4px;
}
.vendor-footer {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.75rem;
  font-size: 0.85rem;
}
.meta-label {
  color: var(--va-text-secondary);
}
</style>
