<template>
  <AppModal
    v-model="show"
    :title="$t('governance_maturity')"
    icon="workspace_premium"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🏆 {{ $t('governance_maturity_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="maturityReport" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Overall Level Banner -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center;">
            <div style="display: flex; flex-direction: column; gap: 0.2rem;">
              <span style="font-weight: 700; font-size: 0.88rem;">{{ maturityReport.summary }}</span>
              <span style="font-size: 0.75rem; color: var(--va-text-secondary);">
                {{ $t('overall_maturity_level') }}: <strong style="color: var(--va-primary);">{{ maturityReport.overallLevel }}</strong>
              </span>
            </div>
            <va-badge
              :text="'종합 ' + maturityReport.overallScore + '점'"
              color="success"
              size="large"
            />
          </div>

          <!-- 4 KPI Metrics Grid -->
          <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 0.75rem;">
            <div style="padding: 0.5rem 0.75rem; border-radius: 6px; border: 1px solid var(--va-background-border); background: var(--va-background-card); text-align: center;">
              <div style="font-size: 0.75rem; color: var(--va-text-secondary);">{{ $t('completeness_kpi') }}</div>
              <div style="font-weight: 700; font-size: 1rem; color: var(--va-success); margin-top: 0.2rem;">{{ maturityReport.kpiSummary.completeness }}%</div>
            </div>
            <div style="padding: 0.5rem 0.75rem; border-radius: 6px; border: 1px solid var(--va-background-border); background: var(--va-background-card); text-align: center;">
              <div style="font-size: 0.75rem; color: var(--va-text-secondary);">{{ $t('timeliness_kpi') }}</div>
              <div style="font-weight: 700; font-size: 1rem; color: var(--va-success); margin-top: 0.2rem;">{{ maturityReport.kpiSummary.timeliness }}%</div>
            </div>
            <div style="padding: 0.5rem 0.75rem; border-radius: 6px; border: 1px solid var(--va-background-border); background: var(--va-background-card); text-align: center;">
              <div style="font-size: 0.75rem; color: var(--va-text-secondary);">{{ $t('consistency_kpi') }}</div>
              <div style="font-weight: 700; font-size: 1rem; color: var(--va-success); margin-top: 0.2rem;">{{ maturityReport.kpiSummary.consistency }}%</div>
            </div>
            <div style="padding: 0.5rem 0.75rem; border-radius: 6px; border: 1px solid var(--va-background-border); background: var(--va-background-card); text-align: center;">
              <div style="font-size: 0.75rem; color: var(--va-text-secondary);">{{ $t('validity_kpi') }}</div>
              <div style="font-weight: 700; font-size: 1rem; color: var(--va-success); margin-top: 0.2rem;">{{ maturityReport.kpiSummary.validity }}%</div>
            </div>
          </div>

          <!-- 5 Dimensions Table -->
          <div style="max-height: 240px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.5rem 0.75rem;">{{ $t('maturity_dimensions') }}</th>
                  <th style="padding: 0.5rem 0.75rem; width: 130px;">성숙도 레벨</th>
                  <th style="padding: 0.5rem 0.75rem;">주요 강점 및 도약 로드맵</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(dim, idx) in maturityReport.dimensions"
                  :key="idx"
                  style="border-bottom: 1px solid var(--va-background-border);"
                >
                  <td style="padding: 0.5rem 0.75rem; font-weight: 700;">
                    <div>{{ dim.dimensionName }}</div>
                    <span style="font-size: 0.72rem; color: var(--va-primary);">점수: {{ dim.currentScore }}점</span>
                  </td>
                  <td style="padding: 0.5rem 0.75rem;">
                    <va-badge :text="dim.level" color="info" size="small" />
                  </td>
                  <td style="padding: 0.5rem 0.75rem; font-size: 0.75rem;">
                    <div style="color: var(--va-text-primary);">✅ {{ dim.strengths }}</div>
                    <div style="color: var(--va-text-secondary); margin-top: 0.2rem;">🚀 {{ dim.gapAndRoadmap }}</div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </va-inner-loading>

      <div style="display: flex; justify-content: flex-end; margin-top: 0.5rem;">
        <va-button preset="secondary" @click="show = false">
          {{ $t('close') }}
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
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()
const { customFetch } = useCustomFetch()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const maturityReport = ref<any>(null)
const loading = ref(false)

const loadMaturity = async () => {
  loading.value = true
  try {
    const res = await customFetch('/api/governance/maturity-evaluation')
    const payload = res?.dimensions ? res : res?.data?.value
    if (payload) {
      maturityReport.value = payload
    }
  } catch (e: any) {
    console.error('Failed to load governance maturity', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) loadMaturity()
}, { immediate: true })
</script>
