<template>
  <va-modal
    v-model="show"
    :title="$t('pipeline_self_healing')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🤖 {{ $t('pipeline_self_healing_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="healingReport" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Summary Banner -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center;">
            <span style="font-weight: 700; font-size: 0.85rem;">{{ healingReport.summary }}</span>
            <va-badge
              :text="'자율 복구율: ' + healingReport.healingSuccessRate + '% (' + healingReport.autoHealedCount + '/' + healingReport.totalIncidents + ')'"
              color="success"
              size="small"
            />
          </div>

          <!-- Healing Actions Table -->
          <div style="max-height: 270px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.5rem 0.75rem;">파이프라인 채널</th>
                  <th style="padding: 0.5rem 0.75rem; width: 140px;">{{ $t('healing_strategy') }}</th>
                  <th style="padding: 0.5rem 0.75rem;">{{ $t('diagnosed_cause') }}</th>
                  <th style="padding: 0.5rem 0.75rem; text-align: right; width: 90px;">{{ $t('recovered_records') }}</th>
                  <th style="padding: 0.5rem 0.75rem; text-align: center; width: 90px;">조치</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(item, idx) in healingReport.actions"
                  :key="idx"
                  style="border-bottom: 1px solid var(--va-background-border);"
                >
                  <td style="padding: 0.5rem 0.75rem; font-weight: 700;">
                    <div>{{ item.pipelineChannel }}</div>
                    <span style="font-size: 0.72rem; color: var(--va-text-secondary); font-family: monospace;">{{ item.actionId }} ({{ item.errorType }})</span>
                  </td>
                  <td style="padding: 0.5rem 0.75rem;">
                    <va-badge :text="item.healingStrategy" color="info" size="small" />
                  </td>
                  <td style="padding: 0.5rem 0.75rem; font-size: 0.75rem; color: var(--va-text-primary);">
                    {{ item.diagnosedCause }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: right; font-weight: 700; color: var(--va-success);">
                    +{{ item.recoveredCount }}건
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: center;">
                    <va-button
                      size="small"
                      preset="secondary"
                      color="primary"
                      icon="auto_fix_high"
                      :loading="triggering === item.pipelineChannel"
                      @click="trigger(item.pipelineChannel)"
                    >
                      복구
                    </va-button>
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
  </va-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'

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

const healingReport = ref<any>(null)
const loading = ref(false)
const triggering = ref<string | null>(null)

const loadHealing = async () => {
  loading.value = true
  try {
    const res = await customFetch('/api/system/pipeline-healing')
    const payload = res?.activeIncidents ? res : res?.data?.value
    if (payload) {
      healingReport.value = payload
    }
  } catch (e: any) {
    console.error('Failed to load pipeline self-healing report', e)
  } finally {
    loading.value = false
  }
}

const trigger = async (channel: string) => {
  triggering.value = channel
  try {
    const res = await customFetch('/api/system/pipeline-healing/trigger', {
      method: 'POST',
      body: { pipelineChannel: channel }
    })
    if (res) {
      alert(t('healing_triggered'))
      loadHealing()
    }
  } catch (e: any) {
    console.error('Failed to trigger healing', e)
  } finally {
    triggering.value = null
  }
}

watch(() => props.modelValue, (val) => {
  if (val) loadHealing()
}, { immediate: true })
</script>
