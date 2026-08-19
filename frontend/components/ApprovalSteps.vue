<template>
  <div v-if="request?.steps && request.steps.length > 0">
    <div v-for="group in groupedSteps" :key="group.order" style="margin-bottom: 0.25rem;">
      <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
        <div v-for="step in group.steps" :key="step.id" style="flex: 1; min-width: 200px; background: var(--va-background-element); padding: 0.5rem; border-radius: 4px; font-size: 0.85rem; border: 1px solid var(--va-background-border);">
          <div style="display: flex; justify-content: space-between; margin-bottom: 4px; align-items: center;">
            <span style="font-weight: bold; color: var(--va-primary); display: flex; align-items: center; gap: 4px; flex-wrap: wrap;">
              <span style="display:inline-flex; align-items:center; justify-content:center; width:20px; height:20px; background-color:var(--va-primary); color:white; border-radius:50%; font-size:0.75rem; font-weight:bold;">{{ step.stepOrder }}</span>
              <span>{{ step.stepType === 'CONSENSUS' ? t('consensus') : (step.stepType === 'DRAFT' ? t('draft') : t('step_approval')) }} - {{ step.assigneeName || getUserName(step.assigneeId) }}</span>
              <va-badge v-if="step.isEscalated" color="danger" size="small" :text="t('sla_escalated_badge', { name: step.escalatedFromUserId || 'Old' })" />
            </span>
            <va-badge :text="step.stepType === 'DRAFT' ? t('draft_completed') : (step.status === 'CANCELLED' ? t('status_cancelled') : step.status)" :color="step.stepType === 'DRAFT' ? 'info' : (step.status === 'APPROVED' ? 'success' : (step.status === 'REJECTED' ? 'danger' : (step.status === 'CANCELLED' ? 'secondary' : 'warning')))" size="small" />
          </div>
          <div v-if="step.status === 'PENDING' && step.slaDueAt" style="font-size: 0.72rem; color: var(--va-warning); margin-bottom: 4px; display: flex; align-items: center; gap: 2px;">
            <va-icon name="timer" size="small" color="warning" />
            <span>{{ t('sla_due', { time: formatWithTimezone(step.slaDueAt) }) }}</span>
          </div>
          <div v-if="step.status === 'APPROVED' || step.status === 'REJECTED' || step.status === 'CANCELLED' || step.stepType === 'DRAFT'" style="font-size: 0.75rem; color: var(--va-text-secondary); margin-bottom: 4px; text-align: right;">
            {{ formatWithTimezone(step.updatedAt) }} {{ t('processed') }}
          </div>
          <div v-if="step.comment" :style="{ color: step.status === 'CANCELLED' ? 'var(--va-danger)' : 'var(--va-text-primary)', background: step.status === 'CANCELLED' ? 'rgba(239, 68, 68, 0.08)' : 'var(--va-background-secondary)', padding: '4px 8px', borderRadius: '4px', borderLeft: step.status === 'CANCELLED' ? '3px solid var(--va-danger)' : '3px solid var(--va-primary)', fontStyle: 'italic' }">
            "{{ step.comment }}" <span v-if="step.status === 'CANCELLED'">({{ t('cancel_reason') }})</span>
          </div>
          <div v-else-if="step.status === 'CANCELLED' && request.reason" style="color: var(--va-danger); background: rgba(239, 68, 68, 0.08); padding: 4px 8px; border-radius: 4px; border-left: 3px solid var(--va-danger); font-style: italic;">
            "{{ request.reason }}" ({{ t('cancel_reason') }})
          </div>
          <div v-else style="color: var(--va-text-secondary); font-style: italic;">
            {{ t('no_comment') }}
          </div>
        </div>
      </div>
    </div>
    
    <div v-if="observersList.length > 0" style="margin-top: 1rem; padding-top: 1rem; border-top: 1px dashed #ccc;">
      <div style="font-weight: 600; font-size: 0.9rem; margin-bottom: 0.5rem; color: #555;">{{ t('observers_list') }}</div>
      <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
        <va-badge v-for="obsId in observersList" :key="obsId" color="info" preset="secondary">{{ getUserName(obsId) }}</va-badge>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { formatWithTimezone } from '~/composables/useTimezoneDate'
import { formatUserCode } from '~/utils/formatters'

const { t } = useI18n()

const props = defineProps({
  request: { type: Object, required: true }
})

const getUserName = (id, fallbackName) => {
  return fallbackName || (id ? formatUserCode(id) : '')
}

const groupedSteps = computed(() => {
  if (!props.request || !props.request.steps) return []
  const map = new Map()
  props.request.steps.forEach(s => {
    if (!map.has(s.stepOrder)) map.set(s.stepOrder, [])
    map.get(s.stepOrder).push(s)
  })
  return Array.from(map.keys()).sort((a,b)=>a-b).map(order => ({
    order, steps: map.get(order)
  }))
})

const observersList = computed(() => {
  if (!props.request || !props.request.observerIds) return []
  try {
    const parsed = JSON.parse(props.request.observerIds)
    return Array.isArray(parsed) ? parsed : []
  } catch(e) {
    return []
  }
})
</script>
