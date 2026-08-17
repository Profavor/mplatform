<template>
  <AppModal
    v-model="show"
    :title="$t('autonomous_cleansing')"
    icon="auto_fix_high"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🧠 {{ $t('autonomous_cleansing_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="proposalData" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Summary Banner -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center;">
            <span style="font-weight: 700; font-size: 0.85rem;">{{ proposalData.summary }}</span>
            <va-button color="success" size="small" :loading="applying" @click="applyAll">
              ⚡ {{ $t('apply_cleansing') }}
            </va-button>
          </div>

          <!-- Anomalies Table -->
          <div style="max-height: 270px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.5rem 0.75rem;">식별 코드 / 필드</th>
                  <th style="padding: 0.5rem 0.75rem;">{{ $t('anomaly_value') }}</th>
                  <th style="padding: 0.5rem 0.75rem; color: var(--va-primary);">{{ $t('recommended_value') }}</th>
                  <th style="padding: 0.5rem 0.75rem;">보정 전략 및 근거</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(item, idx) in proposalData.items"
                  :key="idx"
                  style="border-bottom: 1px solid var(--va-background-border);"
                >
                  <td style="padding: 0.5rem 0.75rem; font-weight: 700;">
                    <div>{{ item.recordCode }}</div>
                    <span style="font-size: 0.75rem; color: var(--va-text-secondary);">{{ item.fieldName }}</span>
                  </td>
                  <td style="padding: 0.5rem 0.75rem; color: var(--va-danger); text-decoration: line-through;">
                    {{ item.currentValue }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem; font-weight: 700; color: var(--va-primary);">
                    {{ item.suggestedValue }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem; font-size: 0.78rem;">
                    <div style="font-weight: 600;">{{ item.strategy }}</div>
                    <span style="color: var(--va-text-secondary);">{{ item.reason }}</span>
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
  domainId: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const proposalData = ref<any>(null)
const loading = ref(false)
const applying = ref(false)

const loadProposals = async () => {
  if (!props.domainId) return
  loading.value = true
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/dq/cleansing-proposals`)
    if (res.data?.value) {
      proposalData.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to load cleansing proposals', e)
  } finally {
    loading.value = false
  }
}

const applyAll = async () => {
  if (!props.domainId || !proposalData.value?.items) return
  applying.value = true
  try {
    const recordCodes = proposalData.value.items.map((i: any) => i.recordCode)
    const res = await useCustomFetch(`/domains/${props.domainId}/dq/cleansing-proposals/apply`, {
      method: 'POST',
      body: { recordCodes }
    })
    if (res.data?.value) {
      alert(t('cleansing_success'))
      show.value = false
    }
  } catch (e: any) {
    console.error('Failed to apply cleansing', e)
  } finally {
    applying.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) loadProposals()
})
</script>
