<template>
  <va-card outlined class="specialized-widget employee-widget">
    <va-card-title class="widget-header">
      <div class="header-left">
        <va-icon name="badge" color="primary" size="small" class="mr-2" />
        <span class="widget-title">{{ $t('specialized_widget_employee') }}</span>
      </div>
      <va-badge
        v-if="employmentType"
        :text="employmentType"
        color="info"
        size="small"
      />
    </va-card-title>
    <va-card-content class="widget-content">
      <div class="employee-header-row">
        <va-avatar
          icon="person"
          color="primary"
          size="large"
          class="emp-avatar"
        />
        <div class="emp-main-info">
          <div class="emp-name-row">
            <span class="emp-name">{{ employeeName }}</span>
            <span v-if="position" class="emp-position">{{ position }}</span>
            <span v-if="employeeNo" class="emp-no-badge">{{ employeeNo }}</span>
          </div>
          <div class="dept-row" v-if="department">
            <va-icon name="apartment" size="16px" color="secondary" class="mr-1" />
            <span class="dept-name">{{ department }}</span>
          </div>
        </div>
      </div>

      <div class="emp-details-grid">
        <div class="detail-item" v-if="yearsOfService !== null">
          <span class="detail-label">{{ $t('years_of_service') }}</span>
          <span class="detail-value highlight">{{ yearsOfService }}{{ $t('years_suffix') }}</span>
        </div>
        <div class="detail-item" v-if="hireDate">
          <span class="detail-label">{{ $t('hire_date') }}</span>
          <span class="detail-value">{{ formattedHireDate }}</span>
        </div>
        <div class="detail-item" v-if="workEmail">
          <span class="detail-label">{{ $t('contact_email') }}</span>
          <a :href="`mailto:${workEmail}`" class="detail-link">{{ workEmail }}</a>
        </div>
        <div class="detail-item" v-if="mobilePhone">
          <span class="detail-label">{{ $t('contact_phone') }}</span>
          <a :href="`tel:${mobilePhone}`" class="detail-link">{{ mobilePhone }}</a>
        </div>
      </div>
    </va-card-content>
  </va-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { parseDate, formatWithTimezone } from '~/composables/useTimezoneDate'

const props = defineProps<{
  recordData: Record<string, any>
  domain?: Record<string, any>
}>()

const employeeNo = computed(() => props.recordData?.employee_no || '')
const employeeName = computed(() => props.recordData?.employee_name || '-')
const department = computed(() => props.recordData?.department || '')
const position = computed(() => props.recordData?.position || '')
const employmentType = computed(() => props.recordData?.employment_type || '')
const hireDate = computed(() => props.recordData?.hire_date || '')
const workEmail = computed(() => props.recordData?.work_email || '')
const mobilePhone = computed(() => props.recordData?.mobile_phone || '')

const formattedHireDate = computed(() => formatWithTimezone(hireDate.value))

const yearsOfService = computed(() => {
  const d = parseDate(hireDate.value)
  if (!d) return null
  const now = new Date()
  const diffYears = now.getFullYear() - d.getFullYear()
  return Math.max(0, diffYears)
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
.employee-header-row {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}
.emp-name-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}
.emp-name {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--va-text-primary);
}
.emp-position {
  font-size: 0.9rem;
  color: var(--va-text-secondary);
}
.emp-no-badge {
  font-size: 0.75rem;
  padding: 2px 6px;
  border-radius: 4px;
  background: var(--va-background-element);
  color: var(--va-text-secondary);
  font-family: monospace;
}
.dept-row {
  display: flex;
  align-items: center;
  font-size: 0.85rem;
  color: var(--va-text-secondary);
  margin-top: 0.25rem;
}
.emp-details-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.75rem;
  background: var(--va-background-element);
  padding: 0.75rem 1rem;
  border-radius: 8px;
}
.detail-item {
  display: flex;
  flex-direction: column;
}
.detail-label {
  font-size: 0.75rem;
  color: var(--va-text-secondary);
  margin-bottom: 0.15rem;
}
.detail-value {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--va-text-primary);
}
.detail-value.highlight {
  color: var(--va-primary);
  font-weight: 700;
}
.detail-link {
  font-size: 0.85rem;
  color: var(--va-primary);
  text-decoration: none;
}
.detail-link:hover {
  text-decoration: underline;
}
</style>
