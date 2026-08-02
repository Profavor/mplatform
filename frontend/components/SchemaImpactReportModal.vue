<template>
  <va-modal
    v-model="show"
    :title="$t('schema_impact_title') || '스키마 변경 영향도 사전 분석 보고서'"
    size="large"
    hide-default-actions
  >
    <div style="padding: 0.5rem; display: flex; flex-direction: column; gap: 1.25rem;">
      <div v-if="loading" style="display: flex; justify-content: center; align-items: center; padding: 2rem;">
        <va-progress-circle indeterminate size="large" />
      </div>

      <template v-else-if="reportData">
        <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-element); padding: 1rem; border-radius: 8px;">
          <div>
            <div style="font-size: 0.85rem; color: var(--va-text-secondary);">{{ $t('risk_level') || '위험도 등급' }}</div>
            <div style="font-weight: 700; font-size: 1.2rem; margin-top: 0.25rem;">
              <va-badge :text="reportData.riskLevel" :color="getRiskColor(reportData.riskLevel)" size="large" />
            </div>
          </div>
          <div style="text-align: right;">
            <div style="font-size: 0.85rem; color: var(--va-text-secondary);">{{ $t('affected_records') || '영향 받는 레코드' }}</div>
            <div style="font-weight: 700; font-size: 1.2rem; color: var(--va-primary);">
              {{ reportData.totalAffectedRecords.toLocaleString() }} 건
            </div>
          </div>
        </div>

        <!-- Warning list -->
        <va-card v-if="reportData.warnings.length > 0" color="warning" outline>
          <va-card-content style="padding: 1rem;">
            <div style="font-weight: 700; margin-bottom: 0.5rem; display: flex; align-items: center; gap: 0.5rem;">
              <va-icon name="warning" color="warning" /> {{ $t('impact_warnings') || '경고 및 주의사항' }}
            </div>
            <ul style="margin: 0; padding-left: 1.25rem; font-size: 0.9rem;">
              <li v-for="(warn, idx) in reportData.warnings" :key="idx" style="margin-bottom: 0.25rem;">
                {{ warn }}
              </li>
            </ul>
          </va-card-content>
        </va-card>

        <!-- Affected Channels -->
        <div v-if="reportData.affectedIntegrationChannels.length > 0">
          <span style="font-weight: 600; font-size: 0.9rem;">{{ $t('affected_channels') || '영향 받는 연동 채널' }}:</span>
          <div style="display: flex; gap: 0.5rem; flex-wrap: wrap; margin-top: 0.5rem;">
            <va-chip v-for="ch in reportData.affectedIntegrationChannels" :key="ch" color="info" outline size="small">
              {{ ch }}
            </va-chip>
          </div>
        </div>
      </template>

      <div style="display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1rem;">
        <va-button preset="secondary" @click="show = false">{{ $t('cancel') || '취소' }}</va-button>
        <va-button color="danger" @click="confirmChange">{{ $t('proceed_anyway') || '위험 감수 후 변경 적용' }}</va-button>
      </div>
    </div>
  </va-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useCustomFetch } from '~/composables/useCustomFetch'

const props = defineProps<{
  modelValue: boolean
  domainId: string | null
  changeRequest: any
}>()

const emit = defineEmits(['update:modelValue', 'confirm'])

const { customFetch } = useCustomFetch()

const show = ref(props.modelValue)
const loading = ref(false)
const reportData = ref<any>(null)

watch(() => props.modelValue, (val) => {
  show.value = val
  if (val && props.domainId) {
    runImpactAnalysis()
  }
})

watch(show, (val) => {
  emit('update:modelValue', val)
})

const runImpactAnalysis = async () => {
  if (!props.domainId) return
  loading.value = true
  try {
    const res = await customFetch(`/api/domains/${props.domainId}/impact-analysis`, {
      method: 'POST',
      body: props.changeRequest
    })
    reportData.value = res
  } catch (e) {
    console.error('Impact analysis failed:', e)
  } finally {
    loading.value = false
  }
}

const getRiskColor = (level: string) => {
  switch (level) {
    case 'CRITICAL': return 'danger'
    case 'HIGH': return 'warning'
    case 'MEDIUM': return 'info'
    default: return 'success'
  }
}

const confirmChange = () => {
  show.value = false
  emit('confirm')
}
</script>
