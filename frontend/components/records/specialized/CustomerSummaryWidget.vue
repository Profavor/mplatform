<template>
  <va-card outlined class="specialized-widget customer-widget">
    <va-card-title class="widget-header">
      <div class="header-left">
        <va-icon name="person_pin" color="primary" size="small" class="mr-2" />
        <span class="widget-title">{{ $t('specialized_widget_customer') }}</span>
      </div>
      <va-badge
        v-if="customerType"
        :text="customerType === 'CORPORATE' ? $t('category_vendor') : (customerType === 'INDIVIDUAL' ? $t('category_customer') : customerType)"
        :color="customerType === 'CORPORATE' ? 'warning' : 'primary'"
        size="small"
      />
    </va-card-title>
    <va-card-content class="widget-content">
      <div class="profile-row">
        <va-avatar
          icon="account_circle"
          color="primary"
          size="large"
          class="profile-avatar"
        />
        <div class="profile-info">
          <div class="customer-name-row">
            <span class="customer-name">{{ customerName }}</span>
            <span v-if="customerNo" class="customer-code-badge">{{ customerNo }}</span>
          </div>
          <div class="contact-chips">
            <va-chip
              v-if="contactEmail"
              size="small"
              preset="outline"
              icon="email"
              class="contact-chip"
              :href="`mailto:${contactEmail}`"
            >
              {{ contactEmail }}
            </va-chip>
            <va-chip
              v-if="contactPhone"
              size="small"
              preset="outline"
              icon="phone"
              class="contact-chip"
              :href="`tel:${contactPhone}`"
            >
              {{ contactPhone }}
            </va-chip>
          </div>
        </div>
      </div>

      <div class="meta-footer">
        <div class="meta-item">
          <span class="meta-label">{{ $t('status') }}:</span>
          <va-badge
            :text="status === 'ACTIVE' ? $t('active_status') : (status === 'DORMANT' ? $t('inactive_status') : status)"
            :color="statusColor"
            size="small"
          />
        </div>
        <div class="meta-item" v-if="registrationDate">
          <span class="meta-label">{{ $t('registration_date') }}:</span>
          <span class="meta-value">{{ formattedRegDate }}</span>
        </div>
      </div>
    </va-card-content>
  </va-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatWithTimezone } from '~/composables/useTimezoneDate'

const props = defineProps<{
  recordData: Record<string, any>
  domain?: Record<string, any>
}>()

const customerNo = computed(() => props.recordData?.customer_no || '')
const customerName = computed(() => props.recordData?.customer_name || '-')
const customerType = computed(() => props.recordData?.customer_type || '')
const contactEmail = computed(() => props.recordData?.contact_email || '')
const contactPhone = computed(() => props.recordData?.contact_phone || '')
const registrationDate = computed(() => props.recordData?.registration_date || '')
const status = computed(() => props.recordData?.status || 'ACTIVE')

const statusColor = computed(() => {
  if (status.value === 'ACTIVE') return 'success'
  if (status.value === 'DORMANT') return 'warning'
  return 'secondary'
})

const formattedRegDate = computed(() => {
  return formatWithTimezone(registrationDate.value)
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
.profile-row {
  display: flex;
  align-items: center;
  gap: 1rem;
}
.customer-name-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.35rem;
}
.customer-name {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--va-text-primary);
}
.customer-code-badge {
  font-size: 0.75rem;
  padding: 2px 6px;
  border-radius: 4px;
  background: var(--va-background-element);
  color: var(--va-text-secondary);
  font-family: monospace;
}
.contact-chips {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}
.meta-footer {
  display: flex;
  gap: 1.5rem;
  margin-top: 1rem;
  padding-top: 0.75rem;
  border-top: 1px dashed var(--va-background-border);
  font-size: 0.85rem;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.meta-label {
  color: var(--va-text-secondary);
}
.meta-value {
  color: var(--va-text-primary);
  font-weight: 500;
}
</style>
