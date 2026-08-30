<template>
  <AppModal
    :model-value="modelValue"
    :title="t('workflowDetails')"
    icon="verified_user"
    size="large"
    hide-default-actions
    @update:model-value="val => emit('update:modelValue', val)"
  >
    <div v-if="selectedFlow" style="display: flex; flex-direction: column; gap: 1rem; padding: 0.5rem 0;">
      
      <div style="display: flex; justify-content: space-between; margin-bottom: 1rem;">
        <div style="font-weight: bold; font-size: 1.1rem;">
          {{ t('requestType') }}: {{ formatTargetType(selectedFlow.targetType) }}
        </div>
        <div>
          <va-badge
            :text="getStatusText(selectedFlow.status)"
            :color="selectedFlow.status === 'PENDING' ? 'warning' : (selectedFlow.status === 'APPROVED' ? 'success' : 'danger')"
          />
        </div>
      </div>

      <div style="font-size: 0.9rem; color: #555; margin-bottom: 1.5rem;">
        <strong>{{ t('requester') }}:</strong> {{ getRequesterName(selectedFlow) }} <br/>
        <strong>{{ t('createdAt') }}:</strong> {{ formatDate(selectedFlow.createdAt) }}
      </div>

      <!-- Pipeline Visualizer -->
      <div style="display: flex; align-items: flex-start; justify-content: space-between; background: var(--va-background-element); padding: 1.5rem 1rem; border-radius: 8px; min-height: 120px; overflow-x: auto;">
        <!-- Steps -->
        <template v-for="(step, idx) in selectedFlow.steps" :key="step.id">
          <div style="text-align: center; flex: 1; position: relative; display: flex; flex-direction: column; align-items: center; min-width: 90px;">
            <va-icon 
              :name="step.status === 'SUBMITTED' ? 'send' : (step.status === 'APPROVED' ? 'check_circle' : (step.status === 'REJECTED' ? 'cancel' : 'radio_button_unchecked'))" 
              :color="step.status === 'SUBMITTED' ? 'primary' : (step.status === 'APPROVED' ? 'success' : (step.status === 'REJECTED' ? 'danger' : (step.status === 'PENDING' ? 'warning' : 'secondary')))" 
              size="large" 
            />
            <div style="font-size: 0.8rem; margin-top: 0.5rem; font-weight: bold; white-space: nowrap;">
              {{ step.status === 'SUBMITTED' ? t('draft') : (step.stepType === 'CONSENSUS' ? t('consensus') : t('approval')) }}
            </div>
            <div style="font-size: 0.75rem; color: var(--va-text-secondary); margin-top: 0.2rem; min-height: 1.1rem; white-space: nowrap;">
              {{ getStatusText(step.status) }}
            </div>
            <div style="font-size: 0.75rem; color: var(--va-text-primary); margin-top: 0.2rem; min-height: 1.1rem; font-weight: bold; white-space: nowrap;" :title="step.assigneeId">
              {{ formatStepAssignee(step, selectedFlow) }}
            </div>
            <div style="font-size: 0.7rem; color: var(--va-text-secondary); margin-top: 0.1rem; min-height: 1rem; white-space: nowrap;">
              <span v-if="step.updatedAt && step.status !== 'PENDING'">{{ formatShortDate(step.updatedAt) }}</span>
              <span v-else-if="step.status === 'SUBMITTED'">{{ formatShortDate(step.createdAt || selectedFlow.createdAt) }}</span>
              <span v-else>&nbsp;</span>
            </div>
            
            <div v-if="step.status === 'PENDING'" style="margin-top: 0.5rem; display: flex; gap: 0.2rem; justify-content: center;">
              <va-button
                size="small"
                preset="secondary"
                color="success"
                icon="check"
                :title="t('proxyApprove')"
                @click="emit('proxy-approve', step.id)"
              />
              <va-button
                size="small"
                preset="secondary"
                color="danger"
                icon="close"
                :title="t('proxyReject')"
                @click="emit('proxy-reject', step.id)"
              />
            </div>
          </div>
          
          <div v-if="idx < selectedFlow.steps.length - 1" style="flex: 1; height: 2px; background: var(--va-background-border); margin: 20px 10px 0 10px; min-width: 30px;"></div>
        </template>
      </div>
      
      <va-accordion style="margin-top: 1rem;" :multiple="true">
        <va-collapse :header="t('viewDataChanges')">
          <div style="padding: 1rem; background: var(--va-background-primary); border: 1px solid var(--va-background-border); border-radius: 4px;">
            <ApprovalDetailsViewer :request="selectedFlow" />
          </div>
        </va-collapse>
      </va-accordion>
      
      <div style="display: flex; justify-content: flex-end; margin-top: 0.5rem;">
        <va-button preset="secondary" @click="emit('update:modelValue', false)">
          {{ t('close') }}
        </va-button>
      </div>
    </div>
  </AppModal>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import ApprovalDetailsViewer from '~/components/ApprovalDetailsViewer.vue'
import AppModal from '~/components/common/AppModal.vue'
import { useCodeStore } from '~/stores/useCodeStore'
import { useApprovalEnricher } from '~/composables/useApprovalEnricher'

const { t, te } = useI18n()
const codeStore = useCodeStore()
const { getRequestTypeLabel } = useApprovalEnricher()

const props = defineProps<{
  modelValue: boolean
  selectedFlow: any
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'proxy-approve', stepId: string): void
  (e: 'proxy-reject', stepId: string): void
}>()

const getStatusText = (status: any) => {
  if (!status) return ''
  const codeName = codeStore.getCodeName('APPROVAL_STATUS', status, null)
  if (codeName && codeName !== status) return codeName
  const key = 'status_' + String(status).toLowerCase()
  if (te && te(key)) return t(key)
  return status
}

const formatTargetType = (type: string) => {
  return getRequestTypeLabel(type)
}

const getRequesterName = (flow: any) => {
  if (!flow) return ''
  return flow.requesterName || flow.requesterUsername || flow.requesterId || '-'
}

const formatDate = (dateStr: any) => {
  if (!dateStr) return '-'
  try {
    const d = new Date(dateStr)
    return d.toLocaleString()
  } catch (e) {
    return dateStr
  }
}

const formatShortDate = (dateStr: any) => {
  if (!dateStr) return ''
  try {
    const d = new Date(dateStr)
    return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
  } catch (e) {
    return dateStr
  }
}

const formatStepAssignee = (s: any, req: any) => {
  if (!s) return ''
  if (s.stepType === 'DRAFT' || s.status === 'SUBMITTED') {
    return s.assigneeName || req?.requesterName || req?.requesterUsername || s.assigneeId || '-'
  }
  if (s.assigneeRole && s.assigneeRole !== 'null') {
    return `${t('label_role', '역할')}: ${s.assigneeRole}`
  }
  if (s.assigneeName) return s.assigneeName
  if (s.assigneeId) return s.assigneeId
  return t('unassigned', '승인자 미지정')
}
</script>
