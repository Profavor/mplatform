<template>
  <AppModal
    v-model="show"
    :title="$t('volume_radar')"
    icon="radar"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        📡 {{ $t('volume_radar_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="radarData" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Status Banner -->
          <div
            style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); display: flex; justify-content: space-between; align-items: center;"
            :style="{ background: radarData.status === 'SPIKE_DETECTED' ? 'rgba(235, 59, 90, 0.1)' : 'rgba(32, 191, 107, 0.1)' }"
          >
            <div style="display: flex; flex-direction: column; gap: 0.2rem;">
              <span style="font-weight: 700; font-size: 0.85rem;">{{ radarData.recommendation }}</span>
              <span style="font-size: 0.75rem; color: var(--va-text-secondary);">
                {{ $t('current_throughput') }}: <strong>{{ radarData.currentThroughput }} req/min</strong> (기준치: {{ radarData.baselineThroughput }})
              </span>
            </div>
            <va-badge
              :text="radarData.status === 'SPIKE_DETECTED' ? $t('spike_alert') : $t('normal_traffic')"
              :color="radarData.status === 'SPIKE_DETECTED' ? 'danger' : 'success'"
              size="small"
            />
          </div>

          <!-- Volume Time Series Table -->
          <div style="max-height: 260px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.5rem 0.75rem;">시간대</th>
                  <th style="padding: 0.5rem 0.75rem; text-align: right;">생성</th>
                  <th style="padding: 0.5rem 0.75rem; text-align: right;">수정</th>
                  <th style="padding: 0.5rem 0.75rem; text-align: right;">삭제</th>
                  <th style="padding: 0.5rem 0.75rem; text-align: right;">API 호출</th>
                  <th style="padding: 0.5rem 0.75rem; width: 90px; text-align: center;">Z-Score</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(p, idx) in radarData.history"
                  :key="idx"
                  style="border-bottom: 1px solid var(--va-background-border);"
                  :style="{ background: p.isSpike ? 'rgba(235, 59, 90, 0.08)' : 'transparent' }"
                >
                  <td style="padding: 0.5rem 0.75rem; font-weight: 700;">{{ p.timeBucket }}</td>
                  <td style="padding: 0.5rem 0.75rem; text-align: right;">{{ p.createCount }}</td>
                  <td style="padding: 0.5rem 0.75rem; text-align: right;">{{ p.updateCount }}</td>
                  <td style="padding: 0.5rem 0.75rem; text-align: right;">{{ p.deleteCount }}</td>
                  <td style="padding: 0.5rem 0.75rem; text-align: right; font-weight: 700;">{{ p.apiCallCount }}</td>
                  <td style="padding: 0.5rem 0.75rem; text-align: center;">
                    <va-badge
                      :text="p.zScore + (p.isSpike ? ' (Spike)' : '')"
                      :color="p.isSpike ? 'danger' : 'info'"
                      size="small"
                    />
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

const radarData = ref<any>(null)
const loading = ref(false)

const loadRadar = async () => {
  loading.value = true
  try {
    const res = await customFetch('/api/system/volume-radar')
    const payload = res?.spikes ? res : res?.data?.value
    if (payload) {
      radarData.value = payload
    }
  } catch (e: any) {
    console.error('Failed to load volume radar data', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) loadRadar()
}, { immediate: true })
</script>
