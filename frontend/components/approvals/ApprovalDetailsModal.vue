<template>
  <AppModal
    :model-value="modelValue"
    v-model:fullscreen="isFullscreenModal"
    size="large"
    hide-default-actions
    without-transitions
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <template #header>
      <div v-if="selectedRequest" style="display: flex; flex-direction: column; gap: 0.5rem; width: 100%;">
        <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 0.5rem;">
          <div style="display: flex; align-items: center; gap: 0.65rem;">
            <h3 style="margin: 0; font-size: 1.25rem; font-weight: 800; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
              <va-icon name="verified_user" color="primary" />
              {{ t('details') }}
            </h3>
            <va-badge :text="getRequestTypeLabel(selectedRequest.targetType)" :color="getRequestTypeColor(selectedRequest.targetType)" />
            <va-badge :text="selectedRequest.status" :color="selectedRequest.status === 'PENDING' ? 'warning' : (selectedRequest.status === 'APPROVED' ? 'success' : 'danger')" />
          </div>

          <div style="display: flex; align-items: center; gap: 0.75rem; margin-left: auto;">
            <div style="font-size: 0.85rem; color: var(--va-text-secondary); display: flex; align-items: center; gap: 0.75rem;">
              <span v-if="selectedRequest">
                <va-icon name="person" size="small" style="margin-right: 2px;" />
                {{ t('requester') }}: <strong>{{ getRequesterName(selectedRequest) }}</strong>
              </span>
              <span>
                <va-icon name="schedule" size="small" style="margin-right: 2px;" />
                {{ formatDate(selectedRequest.createdAt) }}
              </span>
            </div>
          </div>
        </div>

        <div v-if="selectedRequest.classificationNode" style="display: flex; align-items: center; margin-top: 0.25rem;">
          <span style="font-size: 0.88rem; font-weight: 700; display: inline-flex; align-items: center; padding: 3px 12px; background: rgba(37, 99, 235, 0.08); border-radius: 16px; border: 1px solid rgba(37, 99, 235, 0.15);">
            <span style="color: var(--va-primary);">
              {{ getClassificationName(selectedRequest.classificationNode, 'domainName') }}
            </span>
            <va-icon name="chevron_right" size="small" style="margin: 0 4px; color: var(--va-primary); font-size: 1rem;" />
            <span style="color: var(--va-text-primary);">
              {{ getClassificationName(selectedRequest.classificationNode, 'name') }}
            </span>
          </span>
        </div>
      </div>
    </template>

    <div v-if="selectedRequest" :style="{ padding: '1rem 0 0 0', maxHeight: isFullscreenModal ? 'calc(100vh - 160px)' : '75vh', overflowY: 'auto' }">
      <!-- Shared Approval Details Viewer (Collapsible requestedData Accordion & Approval Steps Timeline) -->
      <ApprovalDetailsViewer v-if="selectedRequest" :request="selectedRequest" />
    </div>

    <template #footer>
      <va-button preset="secondary" @click="$emit('update:modelValue', false)">{{ t('close') }}</va-button>
    </template>
  </AppModal>
</template>

<script setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useApprovalEnricher } from '~/composables/useApprovalEnricher'
import ApprovalDetailsViewer from '~/components/ApprovalDetailsViewer.vue'
import ModalControls from '~/components/common/ModalControls.vue'
import AppModal from '~/components/common/AppModal.vue'

const { t } = useI18n()
const { getRequestTypeLabel, getRequestTypeColor, getRequesterName, formatDate, getClassificationName } = useApprovalEnricher()

const isFullscreenModal = ref(false)

defineProps({
  modelValue: { type: Boolean, default: false },
  selectedRequest: { type: Object, default: null }
})

defineEmits(['update:modelValue'])
</script>
