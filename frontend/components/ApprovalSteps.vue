<template>
  <div v-if="request?.steps && request.steps.length > 0">
    <div v-for="group in groupedSteps" :key="group.order" style="margin-bottom: 0.25rem;">
      <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
        <div v-for="step in group.steps" :key="step.id" style="flex: 1; min-width: 200px; background: var(--va-background-element); padding: 0.5rem; border-radius: 4px; font-size: 0.85rem; border: 1px solid var(--va-background-border);">
          <div style="display: flex; justify-content: space-between; margin-bottom: 4px; align-items: center;">
            <span style="font-weight: bold; color: var(--va-primary); display: flex; align-items: center;">
              <span style="display:inline-flex; align-items:center; justify-content:center; width:20px; height:20px; background-color:var(--va-primary); color:white; border-radius:50%; font-size:0.75rem; margin-right:6px; font-weight:bold;">{{ step.stepOrder }}</span>
              {{ step.stepType === 'CONSENSUS' ? '합의' : (step.stepType === 'DRAFT' ? '기안' : '결재') }} - {{ step.assigneeName || getUserName(step.assigneeId) }}
            </span>
            <va-badge :text="step.stepType === 'DRAFT' ? '기안완료' : step.status" :color="step.stepType === 'DRAFT' ? 'info' : (step.status === 'APPROVED' ? 'success' : (step.status === 'REJECTED' ? 'danger' : 'warning'))" size="small" />
          </div>
          <div v-if="step.status === 'APPROVED' || step.status === 'REJECTED' || step.stepType === 'DRAFT'" style="font-size: 0.75rem; color: var(--va-text-secondary); margin-bottom: 4px; text-align: right;">
            {{ new Date(step.updatedAt).toLocaleString() }} 처리됨
          </div>
          <div v-if="step.comment" style="color: var(--va-text-primary); background: var(--va-background-secondary); padding: 4px 8px; border-radius: 4px; border-left: 3px solid var(--va-primary); font-style: italic;">
            "{{ step.comment }}"
          </div>
          <div v-else style="color: var(--va-text-secondary); font-style: italic;">
            의견 없음
          </div>
        </div>
      </div>
    </div>
    
    <div v-if="observersList.length > 0" style="margin-top: 1rem; padding-top: 1rem; border-top: 1px dashed #ccc;">
      <div style="font-weight: 600; font-size: 0.9rem; margin-bottom: 0.5rem; color: #555;">참조자 목록</div>
      <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
        <va-badge v-for="obsId in observersList" :key="obsId" color="info" preset="secondary">{{ getUserName(obsId) }}</va-badge>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  request: { type: Object, required: true }
})

const getUserName = (id, fallbackName) => {
  return fallbackName || id || ''
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
