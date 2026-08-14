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
                backgroundColor: step.completed ? 'var(--va-primary)' : (step.active ? 'var(--va-warning)' : 'var(--va-background-border)'),
                color: step.completed || step.active ? '#ffffff' : 'var(--va-text-secondary)',
                boxShadow: step.active ? '0 0 0 4px rgba(235, 130, 60, 0.25)' : 'none'
              }"
            >
              <va-icon v-if="step.completed" name="check" size="small" color="#fff" />
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
            :style="{ backgroundColor: idx < currentStepIndex - 0.5 ? 'var(--va-primary)' : 'var(--va-background-border)' }"
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
  
  // 1. Draft step
  steps.push({
    title: t('draft'),
    subtitle: props.request.requesterName || props.request.requesterUsername || '',
    completed: true,
    active: false
  })

  const reqSteps = props.request.steps || []
  reqSteps.forEach((s) => {
    const isCompleted = s.status === 'APPROVED' || s.status === 'REJECTED'
    const isActive = props.request.status === 'PENDING' && s.stepOrder === props.request.currentStepOrder
    steps.push({
      title: s.stepType === 'CONSENSUS' ? t('consensus') : t('approval'),
      subtitle: s.assigneeName || s.assigneeUser?.username || s.assigneeId || '',
      completed: isCompleted,
      active: isActive
    })
  })

  return steps
})

const currentStepIndex = computed(() => {
  if (!props.request) return 0
  if (props.request.status === 'APPROVED' || props.request.status === 'REJECTED') {
    return (props.request.steps?.length || 0) + 1
  }
  return props.request.currentStepOrder || 1
})
</script>
