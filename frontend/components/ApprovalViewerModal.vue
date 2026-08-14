<template>
  <va-modal
    :model-value="modelValue"
    size="large"
    close-button
    hide-default-actions
    @update:model-value="val => emit('update:modelValue', val)"
  >
    <template #header>
      <div v-if="request" style="display: flex; flex-direction: column; gap: 0.5rem; width: 100%; padding-right: 2.5rem;">
        <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 0.5rem;">
          <div style="display: flex; align-items: center; gap: 0.65rem;">
            <h3 style="margin: 0; font-size: 1.25rem; font-weight: 800; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
              <va-icon name="rate_review" color="primary" />
              {{ t('approval_history', '결재 내역') }}
            </h3>
            <va-badge 
              v-if="request?.targetType"
              :text="request.targetType.startsWith('SCHEMA_') ? (t('schema_change', '스키마 변경')) : request.targetType" 
              color="primary" 
            />
          </div>

          <div style="font-size: 0.85rem; color: var(--va-text-secondary); display: flex; align-items: center; gap: 0.75rem;">
            <span>
              <va-icon name="person" size="small" style="margin-right: 2px;" />
              {{ t('requester', '기안자') }}: <strong>{{ request?.requesterName || request?.requesterId || 'Unknown' }}</strong>
            </span>
            <span>
              <va-icon name="schedule" size="small" style="margin-right: 2px;" />
              {{ formatDateTime(request?.createdAt) }}
            </span>
          </div>
        </div>
      </div>
    </template>

    <div style="padding: 1rem 0 0 0;">
      <ApprovalDetailsViewer
        v-if="request"
        :request="request"
        @close="onClose"
      />
    </div>
  </va-modal>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import ApprovalDetailsViewer from '~/components/ApprovalDetailsViewer.vue'
import { formatWithTimezone } from '~/composables/useTimezoneDate'

const { t } = useI18n()

const props = defineProps<{
  modelValue: boolean
  request: any
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
}>()

const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return ''
  try {
    return formatWithTimezone(dateStr, 'YYYY-MM-DD HH:mm:ss')
  } catch (e) {
    return new Date(dateStr).toLocaleString()
  }
}

const onClose = () => {
  emit('update:modelValue', false)
}

defineExpose({
  onClose
})
</script>
