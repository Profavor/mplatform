<template>
  <AppModal
    v-model="show"
    :title="$t('freshness_heatmap')"
    icon="local_fire_department"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🌡️ {{ $t('freshness_heatmap_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="heatmapData" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Summary Banner -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center;">
            <span style="font-weight: 700; font-size: 0.85rem;">{{ heatmapData.summary }}</span>
            <va-badge
              :text="'종합 신선도 ' + heatmapData.overallFreshnessScore + '점 (노후화 ' + heatmapData.staleCount + '건)'"
              color="success"
              size="small"
            />
          </div>

          <!-- Heatmap Domain Cards Grid -->
          <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 0.75rem;">
            <div
              v-for="(dom, idx) in heatmapData.domains"
              :key="idx"
              style="padding: 0.75rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-card); display: flex; flex-direction: column; gap: 0.3rem;"
            >
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span style="font-weight: 700; font-size: 0.85rem;">{{ dom.domainName }}</span>
                <va-badge :text="dom.freshnessScore + '점'" color="success" size="small" />
              </div>
              <div style="font-size: 0.75rem; color: var(--va-text-secondary); margin-top: 0.2rem;">
                최종 갱신: <strong style="color: var(--va-text-primary);">{{ dom.lastUpdatedTime }}</strong>
              </div>
              <div style="font-size: 0.72rem; color: var(--va-text-secondary);">
                SLA 기준: {{ dom.freshnessSlaMinutes }}분 이내 (지연: {{ dom.delayMinutes }}분)
              </div>
            </div>
          </div>

          <!-- Detailed Table -->
          <div style="max-height: 220px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.5rem 0.75rem;">도메인 코드 / 명칭</th>
                  <th style="padding: 0.5rem 0.75rem;">{{ $t('last_updated_time') }}</th>
                  <th style="padding: 0.5rem 0.75rem; text-align: right;">{{ $t('delay_minutes') }}</th>
                  <th style="padding: 0.5rem 0.75rem; text-align: right;">{{ $t('freshness_score') }}</th>
                  <th style="padding: 0.5rem 0.75rem; text-align: center; width: 90px;">상태</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(dom, idx) in heatmapData.domains"
                  :key="idx"
                  style="border-bottom: 1px solid var(--va-background-border);"
                >
                  <td style="padding: 0.5rem 0.75rem; font-weight: 700;">
                    <div>{{ dom.domainName }}</div>
                    <span style="font-size: 0.72rem; color: var(--va-text-secondary); font-family: monospace;">{{ dom.domainCode }}</span>
                  </td>
                  <td style="padding: 0.5rem 0.75rem; font-size: 0.78rem;">
                    {{ dom.lastUpdatedTime }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: right; font-weight: 600;">
                    {{ dom.delayMinutes }}분 (SLA: {{ dom.freshnessSlaMinutes }}분)
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: right; font-weight: 700; color: var(--va-success);">
                    {{ dom.freshnessScore }}점
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: center;">
                    <va-badge
                      :text="dom.status === 'FRESH' ? $t('fresh_status') : dom.status"
                      :color="dom.status === 'FRESH' ? 'success' : 'warning'"
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

const heatmapData = ref<any>(null)
const loading = ref(false)

const loadHeatmap = async () => {
  loading.value = true
  try {
    const res = await customFetch('/api/system/freshness-heatmap')
    const payload = res?.heatmapEntries ? res : res?.data?.value
    if (payload) {
      heatmapData.value = payload
    }
  } catch (e: any) {
    console.error('Failed to load freshness heatmap', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) loadHeatmap()
}, { immediate: true })
</script>
