<template>
  <va-modal
    v-model="show"
    :title="$t('data_asset_valuation')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        💎 {{ $t('data_asset_valuation_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="valuationData" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Summary Banner -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center;">
            <div style="display: flex; flex-direction: column; gap: 0.2rem;">
              <span style="font-weight: 700; font-size: 0.9rem;">
                {{ valuationData.summary }}
              </span>
            </div>
            <div style="display: flex; gap: 0.5rem; align-items: center;">
              <span style="font-size: 0.8rem; color: var(--va-text-secondary);">{{ $t('average_quality_score') }}:</span>
              <va-badge :text="`${valuationData.averageQualityScore}%`" color="success" size="small" />
            </div>
          </div>

          <!-- Domains Valuation Table -->
          <div style="max-height: 280px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.5rem 0.75rem;">도메인 명칭</th>
                  <th style="padding: 0.5rem 0.75rem; width: 80px; text-align: right;">레코드 수</th>
                  <th style="padding: 0.5rem 0.75rem; width: 80px; text-align: right;">연계 채널</th>
                  <th style="padding: 0.5rem 0.75rem; width: 80px; text-align: center;">{{ $t('asset_rating') }}</th>
                  <th style="padding: 0.5rem 0.75rem; text-align: right;">{{ $t('estimated_value') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="d in valuationData.domainValuations"
                  :key="d.domainId"
                  style="border-bottom: 1px solid var(--va-background-border);"
                >
                  <td style="padding: 0.5rem 0.75rem; font-weight: 700;">{{ d.domainName }}</td>
                  <td style="padding: 0.5rem 0.75rem; text-align: right; color: var(--va-text-secondary);">
                    {{ d.recordCount.toLocaleString() }}건
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: right; color: var(--va-text-secondary);">
                    {{ d.connectedChannelCount }}개
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: center;">
                    <va-badge
                      :text="d.assetRating"
                      :color="d.assetRating.startsWith('A') ? 'success' : 'warning'"
                      size="small"
                    />
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: right; font-weight: 700; color: var(--va-primary);">
                    약 {{ d.estimatedAssetValueWon.toLocaleString() }}원
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

const valuationData = ref<any>(null)
const loading = ref(false)

const loadValuation = async () => {
  loading.value = true
  try {
    const res = await useCustomFetch('/catalog/valuation')
    if (res.data?.value) {
      valuationData.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to load asset valuation', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) loadValuation()
})
</script>
