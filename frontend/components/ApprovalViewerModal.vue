<template>
  <AppModal
    :model-value="modelValue"
    v-model:fullscreen="isFullscreenModal"
    size="large"
    hide-default-actions
    without-transitions
    @update:model-value="val => emit('update:modelValue', val)"
  >
    <template #header>
      <div v-if="request" style="display: flex; flex-direction: column; gap: 0.5rem; width: 100%;">
        <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 0.5rem;">
          <div style="display: flex; align-items: center; gap: 0.65rem;">
            <h3 style="margin: 0; font-size: 1.25rem; font-weight: 800; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
              <va-icon :name="request?.isIntegration ? 'sync' : 'rate_review'" color="primary" />
              {{ request?.isIntegration ? (t('integration_history', '연계 이력')) : (t('approval_history', '결재 내역')) }}
            </h3>
            <va-badge 
              v-if="request?.targetType"
              :text="getRequestTypeLabel(request.targetType)" 
              :color="getRequestTypeColor(request.targetType)" 
            />
          </div>

          <div style="display: flex; align-items: center; gap: 0.75rem; margin-left: auto;">
            <div style="font-size: 0.85rem; color: var(--va-text-secondary); display: flex; align-items: center; gap: 0.75rem;">
              <span>
                <va-icon :name="request?.isIntegration ? 'settings' : 'person'" size="small" style="margin-right: 2px;" />
                {{ request?.isIntegration ? (t('source_system', '연계 주체') || '연계 주체') : (t('requester', '기안자')) }}: <strong>{{ request?.requesterName || request?.requesterId || (request?.isIntegration ? (request?.sourceSystem || 'SYSTEM') : 'Unknown') }}</strong>
              </span>
              <span>
                <va-icon name="schedule" size="small" style="margin-right: 2px;" />
                {{ formatDateTime(request?.createdAt) }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </template>

    <div :style="{ maxHeight: isFullscreenModal ? 'calc(100vh - 150px)' : '75vh', overflowY: 'auto', padding: '0.5rem 0 0 0' }">
      <ApprovalDetailsViewer 
        v-if="request" 
        :request="request" 
        :node-id="nodeId || request?.nodeId || request?.classificationNode?.id" 
        @close="onClose" 
      />
    </div>
  </AppModal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import ApprovalDetailsViewer from '~/components/ApprovalDetailsViewer.vue'
import ModalControls from '~/components/common/ModalControls.vue'
import AppModal from '~/components/common/AppModal.vue'
import { formatWithTimezone } from '~/composables/useTimezoneDate'
import { useApprovalEnricher } from '~/composables/useApprovalEnricher'

const { t } = useI18n()
const { getRequestTypeLabel, getRequestTypeColor } = useApprovalEnricher()
const isFullscreenModal = ref(false)

const props = defineProps<{
  modelValue: boolean
  request: any
  nodeId?: string
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
