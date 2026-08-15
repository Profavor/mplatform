<template>
  <va-modal
    v-model="show"
    :title="$t('data_sla')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🤝 {{ $t('data_sla_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="slaData" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Summary Banner -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center;">
            <span style="font-weight: 700; font-size: 0.85rem;">{{ slaData.summary }}</span>
            <va-badge
              :text="'SLA 준수율: ' + slaData.overallComplianceRate + '% (' + slaData.compliantCount + '/' + slaData.totalContracts + ')'"
              color="success"
              size="small"
            />
          </div>

          <!-- SLA Table -->
          <div style="max-height: 270px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.5rem 0.75rem;">{{ $t('sla_contract_name') }}</th>
                  <th style="padding: 0.5rem 0.75rem; text-align: right;">{{ $t('latency_sla') }}</th>
                  <th style="padding: 0.5rem 0.75rem; text-align: right;">{{ $t('availability_sla') }}</th>
                  <th style="padding: 0.5rem 0.75rem; text-align: right;">{{ $t('quality_sla') }}</th>
                  <th style="padding: 0.5rem 0.75rem; text-align: center; width: 100px;">상태</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(item, idx) in slaData.contracts"
                  :key="idx"
                  style="border-bottom: 1px solid var(--va-background-border);"
                >
                  <td style="padding: 0.5rem 0.75rem; font-weight: 700;">
                    <div>{{ item.contractName }}</div>
                    <span style="font-size: 0.72rem; color: var(--va-text-secondary);">{{ item.targetChannelOrDomain }} ({{ item.slaId }})</span>
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: right;">
                    <div style="font-weight: 700; color: var(--va-success);">{{ item.currentLatencyMs }}ms</div>
                    <span style="font-size: 0.72rem; color: var(--va-text-secondary);">목표: &lt;{{ item.latencyThresholdMs }}ms</span>
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: right;">
                    <div style="font-weight: 700; color: var(--va-success);">{{ item.currentAvailabilityPercent }}%</div>
                    <span style="font-size: 0.72rem; color: var(--va-text-secondary);">목표: {{ item.availabilityTargetPercent }}%</span>
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: right; font-weight: 700; color: var(--va-primary);">
                    {{ item.qualityCompliancePercent }}%
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: center;">
                    <va-badge
                      :text="item.status === 'MEETING_SLA' ? $t('meeting_sla') : item.status"
                      :color="item.status === 'MEETING_SLA' ? 'success' : 'warning'"
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

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const slaData = ref<any>(null)
const loading = ref(false)

const loadSla = async () => {
  loading.value = true
  try {
    const res = await useCustomFetch('/system/sla-contracts')
    if (res.data?.value) {
      slaData.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to load SLA contracts', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) loadSla()
})
</script>
