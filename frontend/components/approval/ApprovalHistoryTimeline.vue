<template>
  <div v-if="request">
    <!-- Stepper Line -->
    <div style="background-color: var(--va-background-secondary); border-radius: 6px; padding: 1.25rem 1rem; margin-bottom: 1.5rem;">
      <div style="font-weight: 600; font-size: 0.95rem; margin-bottom: 1rem; color: var(--va-text-primary);">{{ t('approvalProgress') }}</div>
      <div v-if="stepperSteps && stepperSteps.length > 0" style="display: flex; align-items: center; width: 100%; overflow-x: auto; padding: 0.5rem 0;">
        <template v-for="(step, idx) in stepperSteps" :key="idx">
          <!-- Step Icon/Node -->
          <div style="display: flex; flex-direction: column; align-items: center; min-width: 80px; position: relative;">
            <div 
              style="width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 0.9rem; margin-bottom: 0.5rem; transition: all 0.3s ease;"
              :style="{
                backgroundColor: step.completed ? 'var(--va-primary)' : (step.rejected ? 'var(--va-danger)' : (step.cancelled ? '#94a3b8' : (step.active ? 'var(--va-warning)' : 'var(--va-background-border)'))),
                color: step.completed || step.rejected || step.cancelled || step.active ? '#ffffff' : 'var(--va-text-secondary)',
                boxShadow: step.active ? '0 0 0 4px rgba(235, 130, 60, 0.25)' : 'none'
              }"
            >
              <va-icon v-if="step.completed" name="check" size="small" color="#fff" />
              <va-icon v-else-if="step.rejected" name="close" size="small" color="#fff" />
              <va-icon v-else-if="step.cancelled" name="cancel" size="small" color="#fff" />
              <span v-else>{{ idx + 1 }}</span>
            </div>
            <div style="font-size: 0.8rem; font-weight: 600; text-align: center; color: var(--va-text-primary); white-space: nowrap;">
              {{ step.title }}
            </div>
            <div v-if="step.subtitle" style="font-size: 0.7rem; color: var(--va-text-secondary); text-align: center; white-space: nowrap;">
              {{ step.subtitle }}
            </div>
          </div>
          <!-- Connecting Line -->
          <div 
            v-if="idx < stepperSteps.length - 1" 
            style="flex-grow: 1; min-width: 40px; height: 2px; margin: 0 1rem;"
            :style="{ 
              backgroundColor: stepperSteps[idx + 1]?.completed ? 'var(--va-primary)' : (stepperSteps[idx + 1]?.rejected ? 'var(--va-danger)' : (stepperSteps[idx + 1]?.cancelled ? '#cbd5e1' : (stepperSteps[idx + 1]?.active ? 'var(--va-warning)' : 'var(--va-background-border)')))
            }"
          />
        </template>
      </div>
      <div v-else style="font-weight: bold; color: var(--va-primary);">{{ t('noApprovalLine') }}</div>
    </div>

    <!-- Approval Line Status Component -->
    <div v-if="request?.steps && request.steps.length > 0" style="margin-bottom: 1.5rem; padding-top: 0.5rem; border-top: 1px solid var(--va-background-border);">
      <div style="font-weight: 600; font-size: 0.95rem; margin-bottom: 0.5rem; color: var(--va-text-primary);">{{ t('approvalLineStatus') }}</div>
      <ApprovalSteps :request="request" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import ApprovalSteps from '../ApprovalSteps.vue'

const props = defineProps({
  request: { type: Object, required: true }
})

const { t } = useI18n()

const stepperSteps = computed(() => {
  if (!props.request) return []
  const steps = []
  
  const reqSteps = props.request.steps || []
  
  // 1. Draft step: extract drafter name and do NOT duplicate with steps list
  const draftStep = reqSteps.find(s => s.stepOrder === 0 || s.stepType === 'DRAFT')
  const requesterName = props.request.requesterName || props.request.requesterUsername || (draftStep ? (draftStep.assigneeName || draftStep.assigneeId) : '')

  steps.push({
    order: 0,
    title: t('draft'),
    subtitle: requesterName,
    status: 'COMPLETED',
    completed: true,
    rejected: false,
    cancelled: false,
    active: false
  })

  // 2. Only actual approval steps (stepOrder > 0 and stepType !== 'DRAFT')
  const approvalSteps = reqSteps.filter(s => s.stepOrder > 0 && s.stepType !== 'DRAFT')
  approvalSteps.sort((a, b) => (a.stepOrder || 0) - (b.stepOrder || 0))

  const isRequestCancelled = props.request.status === 'CANCELLED'

  approvalSteps.forEach((s) => {
    const isStepApproved = s.status === 'APPROVED'
    const isStepRejected = s.status === 'REJECTED'
    const isStepCancelled = s.status === 'CANCELLED' || (isRequestCancelled && s.status !== 'APPROVED')
    const isStepActive = props.request.status === 'PENDING' && s.stepOrder === props.request.currentStepOrder && s.status === 'PENDING'

    steps.push({
      order: s.stepOrder,
      stepId: s.id,
      title: s.stepType === 'CONSENSUS' ? t('consensus') : t('approval'),
      subtitle: s.assigneeName || s.assigneeUser?.username || s.assigneeId || '',
      status: s.status,
      completed: isStepApproved,
      rejected: isStepRejected,
      cancelled: isStepCancelled,
      active: isStepActive
    })
  })

  return steps
})
</script>
