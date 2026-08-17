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
      <div v-if="selectedPendingStep?.approvalRequest" style="display: flex; flex-direction: column; gap: 0.5rem; width: 100%;">
        <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 0.5rem;">
          <div style="display: flex; align-items: center; gap: 0.65rem;">
            <h3 style="margin: 0; font-size: 1.25rem; font-weight: 800; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
              <va-icon name="rate_review" color="primary" />
              {{ $t('approval_review') }}
            </h3>
            <va-badge :text="getRequestTypeLabel(selectedPendingStep.approvalRequest?.targetType)" :color="getRequestTypeColor(selectedPendingStep.approvalRequest?.targetType)" />
          </div>

          <div style="display: flex; align-items: center; gap: 0.75rem; margin-left: auto;">
            <div style="font-size: 0.85rem; color: var(--va-text-secondary); display: flex; align-items: center; gap: 0.75rem;">
              <span v-if="selectedPendingStep.approvalRequest">
                <va-icon name="person" size="small" style="margin-right: 2px;" />
                {{ t('requester') }}: <strong>{{ getRequesterName(selectedPendingStep.approvalRequest) }}</strong>
              </span>
              <span>
                <va-icon name="schedule" size="small" style="margin-right: 2px;" />
                {{ formatDate(selectedPendingStep.approvalRequest?.createdAt) }}
              </span>
            </div>
          </div>
        </div>

        <div v-if="selectedPendingStep.approvalRequest?.classificationNode" style="display: flex; align-items: center; margin-top: 0.25rem;">
          <span style="font-size: 0.88rem; font-weight: 700; display: inline-flex; align-items: center; padding: 3px 12px; background: rgba(37, 99, 235, 0.08); border-radius: 16px; border: 1px solid rgba(37, 99, 235, 0.15);">
            <span style="color: var(--va-primary);">
              {{ getClassificationName(selectedPendingStep.approvalRequest.classificationNode, 'domainName') }}
            </span>
            <va-icon name="chevron_right" size="small" style="margin: 0 4px; color: var(--va-primary); font-size: 1rem;" />
            <span style="color: var(--va-text-primary);">
              {{ getClassificationName(selectedPendingStep.approvalRequest.classificationNode, 'name') }}
            </span>
          </span>
        </div>
      </div>
    </template>

    <div v-if="selectedPendingStep" :style="{ padding: '1rem 0 0 0', maxHeight: isFullscreenModal ? 'calc(100vh - 160px)' : '75vh', overflowY: 'auto' }">
      <!-- Shared Approval Details Viewer (Collapsible requestedData Accordion) -->
      <ApprovalDetailsViewer v-if="selectedPendingStep.approvalRequest" :request="selectedPendingStep.approvalRequest" />

      <div style="width: 100%; margin-top: 1rem; margin-bottom: 1rem; display: block;">
        <textarea
          v-model="commentData[selectedPendingStep.id]" 
          :placeholder="t('addComment')" 
          style="width: 100%; box-sizing: border-box; background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 0.75rem 1rem; color: var(--va-text-primary); resize: vertical; min-height: 80px; font-family: inherit; font-size: 0.9rem;"
        ></textarea>
      </div>

      <!-- Actions -->
      <div style="display: flex; gap: 1rem;">
        <va-button color="success" icon="check" style="flex: 1;" @click="$emit('single-action', selectedPendingStep.id, 'approve')" :outline="isDark">{{ t('approve') }}</va-button>
        <va-button color="danger" icon="close" preset="secondary" style="flex: 1;" @click="$emit('single-action', selectedPendingStep.id, 'reject')">{{ t('reject') }}</va-button>
      </div>
    </div>
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
  selectedPendingStep: { type: Object, default: null },
  commentData: { type: Object, required: true },
  isDark: { type: Boolean, default: false }
})

defineEmits(['update:modelValue', 'single-action'])
</script>
