<template>
  <AppModal
    v-model="show"
    :title="$t('compliance_report')"
    icon="verified"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="info" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🛡️ {{ $t('compliance_report_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <!-- Record Overview Header -->
        <div v-if="report" style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-element); padding: 0.85rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); flex-wrap: wrap; gap: 0.5rem;">
          <div>
            <div style="display: flex; align-items: center; gap: 0.5rem;">
              <span style="font-weight: 800; font-size: 1.05rem; color: var(--va-primary);">
                {{ report.recordCode }}
              </span>
              <va-badge :text="`v${report.currentVersion}`" color="info" size="small" />
              <va-badge :text="report.currentStatus" :color="report.currentStatus === 'ACTIVE' ? 'success' : 'warning'" size="small" />
            </div>
            <div style="font-size: 0.82rem; color: var(--va-text-secondary); margin-top: 0.25rem;">
              🏢 {{ report.domainName }} / 📁 {{ report.nodeName }}
            </div>
          </div>
          <div style="font-size: 0.8rem; color: var(--va-text-secondary); text-align: right;">
            <div>등록: {{ formatWithTimezone(report.createdAt) }}</div>
            <div v-if="report.createdBy">등록자: {{ report.createdBy }}</div>
          </div>
        </div>

        <!-- Lifecycle Events Timeline -->
        <div v-if="report?.lifecycleEvents && report.lifecycleEvents.length > 0" style="margin-top: 0.75rem;">
          <div style="font-weight: 700; font-size: 0.95rem; margin-bottom: 0.75rem; color: var(--va-text-primary);">
            📋 {{ $t('audit_timeline') }} ({{ report.lifecycleEvents.length }}개 이벤트):
          </div>

          <div style="display: flex; flex-direction: column; gap: 0.75rem; max-height: 380px; overflow-y: auto; padding-right: 0.25rem;">
            <div
              v-for="(event, idx) in report.lifecycleEvents"
              :key="idx"
              style="display: flex; gap: 0.75rem; background: var(--va-background-element); padding: 0.75rem 1rem; border-radius: 8px; border-left: 4px solid; border: 1px solid var(--va-background-border);"
              :style="{ borderLeftColor: getEventColor(event.eventType), borderLeftWidth: '4px' }"
            >
              <div style="display: flex; flex-direction: column; align-items: center; min-width: 85px;">
                <va-badge
                  :text="event.eventType"
                  :color="getEventBadgeColor(event.eventType)"
                  size="small"
                  style="font-size: 0.7rem;"
                />
                <span style="font-size: 0.72rem; color: var(--va-text-secondary); margin-top: 0.35rem; text-align: center;">
                  {{ formatWithTimezone(event.timestamp) }}
                </span>
              </div>

              <div style="flex: 1;">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <span style="font-weight: 700; font-size: 0.88rem; color: var(--va-text-primary);">
                    {{ event.summary }}
                  </span>
                  <span style="font-size: 0.78rem; color: var(--va-text-secondary);">
                    👤 {{ event.actorName || event.actorId }}
                  </span>
                </div>
                <div v-if="event.detail" style="font-size: 0.8rem; color: var(--va-text-secondary); margin-top: 0.25rem; background: var(--va-background-secondary); padding: 0.35rem 0.6rem; border-radius: 4px;">
                  {{ event.detail }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </va-inner-loading>

      <div style="display: flex; justify-content: flex-end; margin-top: 0.5rem;">
        <va-button preset="secondary" @click="show = false">
          {{ $t('close') }}
        </va-button>
      </div>
    </div>
  </AppModal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { formatWithTimezone } from '~/composables/useTimezoneDate'
import AppModal from '~/components/common/AppModal.vue'

const props = defineProps<{
  modelValue: boolean
  recordId?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const report = ref<any>(null)
const loading = ref(false)

const getEventColor = (type: string) => {
  switch (type) {
    case 'CREATION': return '#2c82e0'
    case 'UPDATE': return '#00a8a8'
    case 'APPROVAL_REQUEST': return '#f59e0b'
    case 'APPROVAL_APPROVED': return '#10b981'
    case 'APPROVAL_REJECTED': return '#ef4444'
    case 'SENSITIVE_VIEW': return '#8b5cf6'
    case 'ROLLBACK': return '#ec4899'
    case 'RECLASSIFICATION': return '#6366f1'
    default: return '#6b7280'
  }
}

const getEventBadgeColor = (type: string) => {
  switch (type) {
    case 'CREATION': return 'primary'
    case 'UPDATE': return 'info'
    case 'APPROVAL_REQUEST': return 'warning'
    case 'APPROVAL_APPROVED': return 'success'
    case 'APPROVAL_REJECTED': return 'danger'
    case 'SENSITIVE_VIEW': return 'secondary'
    case 'ROLLBACK': return 'warning'
    default: return 'primary'
  }
}

const fetchReport = async () => {
  if (!props.recordId) return
  loading.value = true
  try {
    const res = await useCustomFetch(`/compliance/records/${props.recordId}/report`)
    if (res.data?.value) {
      report.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to fetch compliance report', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.recordId, (val) => {
  if (val && props.modelValue) fetchReport()
})

watch(() => props.modelValue, (val) => {
  if (val && props.recordId) fetchReport()
})
</script>
