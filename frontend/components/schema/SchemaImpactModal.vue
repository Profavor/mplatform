<template>
  <AppModal
    v-model="show"
    :title="$t('schema_simulation')"
    icon="warning"
    size="medium"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="warning" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🛡️ {{ $t('schema_simulation_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="simulationResult" style="display: flex; flex-direction: column; gap: 1.25rem;">
          <!-- Score Banner -->
          <div style="padding: 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center;">
            <div style="display: flex; flex-direction: column; gap: 0.25rem;">
              <span style="font-size: 0.85rem; color: var(--va-text-secondary);">{{ $t('safety_score') }}</span>
              <span style="font-size: 1.8rem; font-weight: 800;" :style="{ color: getScoreColor(simulationResult.safetyScore) }">
                {{ simulationResult.safetyScore }} / 100
              </span>
            </div>
            <va-badge
              :text="simulationResult.riskLevel"
              :color="getRiskColor(simulationResult.riskLevel)"
              size="large"
            />
          </div>

          <!-- Impact Metrics Grid -->
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem;">
            <div style="padding: 0.75rem; border-radius: 6px; border: 1px solid var(--va-background-border); background: var(--va-background-element);">
              <div style="font-size: 0.75rem; color: var(--va-text-secondary); margin-bottom: 0.2rem;">{{ $t('affected_records') }}</div>
              <div style="font-size: 1.1rem; font-weight: bold; color: var(--va-text-primary);">
                {{ simulationResult.populatedRecordCount }} / {{ simulationResult.totalRecordCount }}건
              </div>
            </div>

            <div style="padding: 0.75rem; border-radius: 6px; border: 1px solid var(--va-background-border); background: var(--va-background-element);">
              <div style="font-size: 0.75rem; color: var(--va-text-secondary); margin-bottom: 0.2rem;">{{ $t('affected_channels') }}</div>
              <div style="font-size: 1.1rem; font-weight: bold; color: var(--va-primary);">
                {{ simulationResult.affectedChannels?.length || 0 }}개 채널
              </div>
            </div>
          </div>

          <!-- Recommendations -->
          <div v-if="simulationResult.recommendations?.length > 0">
            <div style="font-weight: 700; font-size: 0.88rem; margin-bottom: 0.4rem;">
              💡 {{ $t('simulation_recommendations') }}:
            </div>
            <ul style="margin: 0; padding-left: 1.25rem; font-size: 0.82rem; color: var(--va-danger); display: flex; flex-direction: column; gap: 0.35rem;">
              <li v-for="(rec, idx) in simulationResult.recommendations" :key="idx">
                {{ rec }}
              </li>
            </ul>
          </div>
        </div>
      </va-inner-loading>

      <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem;">
        <va-button preset="secondary" @click="show = false">
          {{ $t('cancel') }}
        </va-button>
        <va-button
          color="danger"
          :disabled="loading"
          @click="confirmProceed"
        >
          {{ $t('confirm') }}
        </va-button>
      </div>
    </div>
  </AppModal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'
import AppModal from '~/components/common/AppModal.vue'

const props = defineProps<{
  modelValue: boolean
  domainId?: string
  fieldKey?: string
  action?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'proceed'): void
}>()

const { t } = useI18n()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const simulationResult = ref<any>(null)
const loading = ref(false)

const getScoreColor = (score: number) => {
  if (score >= 90) return 'var(--va-success)'
  if (score >= 70) return 'var(--va-info)'
  if (score >= 50) return 'var(--va-warning)'
  return 'var(--va-danger)'
}

const getRiskColor = (risk: string) => {
  switch (risk) {
    case 'SAFE': return 'success'
    case 'LOW': return 'info'
    case 'MEDIUM': return 'warning'
    case 'HIGH':
    case 'CRITICAL': return 'danger'
    default: return 'secondary'
  }
}

const runSimulation = async () => {
  if (!props.domainId || !props.fieldKey) return
  loading.value = true
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/schema/simulate-impact`, {
      method: 'POST',
      body: {
        fieldKey: props.fieldKey,
        action: props.action || 'DELETE'
      }
    })
    if (res.data?.value) {
      simulationResult.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to run schema simulation', e)
  } finally {
    loading.value = false
  }
}

const confirmProceed = () => {
  emit('proceed')
  show.value = false
}

watch(() => props.modelValue, (val) => {
  if (val) runSimulation()
})
</script>
