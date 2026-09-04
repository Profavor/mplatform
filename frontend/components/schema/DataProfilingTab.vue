<template>
  <div class="profiling-container" style="padding: 1rem 0;">
    <va-inner-loading :loading="loading">
      <!-- Header Controls -->
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem; flex-wrap: wrap; gap: 0.75rem; background: var(--va-background-element); padding: 1rem; border-radius: 8px; border: 1px solid var(--va-background-border);">
        <div>
          <h4 style="margin: 0; font-size: 1.05rem; font-weight: 700; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
            <va-icon name="insights" color="primary" />
            {{ $t('profiling_title') }}
          </h4>
          <p style="margin: 0.25rem 0 0 0; font-size: 0.82rem; color: var(--va-text-secondary);">
            {{ $t('profiling_desc') }}
          </p>
        </div>
        <div style="display: flex; align-items: center; gap: 0.75rem;">
          <span v-if="report?.scannedAt" style="font-size: 0.8rem; color: var(--va-text-secondary);">
            분석 일시: {{ formatWithTimezone(report.scannedAt) }}
          </span>
          <va-button
            preset="primary"
            icon="refresh"
            size="small"
            :loading="loading"
            @click="runScan"
          >
            {{ $t('run_scan') }}
          </va-button>
        </div>
      </div>

      <!-- Outliers Alert Box if any -->
      <va-alert
        v-if="report?.outliers && report.outliers.length > 0"
        color="warning"
        outline
        style="margin-bottom: 1.25rem;"
      >
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.5rem;">
          <span style="font-weight: 700; font-size: 0.9rem;">
            ⚠️ {{ $t('outliers_found', { count: report.outliers.length }) }}
          </span>
          <span v-if="report.outliers.length > 50" style="font-size: 0.8rem; color: var(--va-text-secondary);">
            (상위 50건 미리보기)
          </span>
        </div>
        <div style="margin-top: 0.5rem; max-height: 180px; overflow-y: auto;">
          <table style="width: 100%; border-collapse: collapse; font-size: 0.8rem;">
            <thead>
              <tr style="text-align: left; border-bottom: 1px solid var(--va-background-border);">
                <th style="padding: 0.3rem 0.5rem;">필드</th>
                <th style="padding: 0.3rem 0.5rem;">이상치 값</th>
                <th style="padding: 0.3rem 0.5rem;">탐지 사유</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(out, idx) in previewOutliers" :key="idx" style="border-bottom: 1px solid var(--va-background-border);">
                <td style="padding: 0.3rem 0.5rem; font-weight: 600;">{{ out.fieldKey }}</td>
                <td style="padding: 0.3rem 0.5rem; color: var(--va-danger); font-weight: bold;">{{ out.value }}</td>
                <td style="padding: 0.3rem 0.5rem;">{{ out.reason }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </va-alert>

      <!-- Field Profiles Grid -->
      <div v-if="report?.fieldProfiles && report.fieldProfiles.length > 0" class="stats-grid">
        <va-card v-for="fp in report.fieldProfiles" :key="fp.fieldKey" class="stat-card">
          <va-card-title class="stat-header" style="padding-bottom: 0.5rem;">
            <div style="display: flex; justify-content: space-between; width: 100%; align-items: center;">
              <span style="font-weight: 700; font-size: 0.95rem;">
                {{ fp.fieldName }} <span style="font-size: 0.8rem; font-weight: normal; color: var(--va-text-secondary);">({{ fp.fieldKey }})</span>
              </span>
              <va-badge :text="fp.fieldType" color="info" size="small" />
            </div>
          </va-card-title>
          <va-card-content style="font-size: 0.85rem; display: flex; flex-direction: column; gap: 0.5rem;">
            <!-- Null Rate -->
            <div>
              <div style="display: flex; justify-content: space-between; margin-bottom: 2px;">
                <span style="color: var(--va-text-secondary);">{{ $t('null_rate') }}:</span>
                <span :style="{ color: fp.nullRate > 20 ? 'var(--va-danger)' : 'var(--va-text-primary)', fontWeight: 'bold' }">
                  {{ fp.nullRate }}% ({{ fp.nullCount }}건)
                </span>
              </div>
              <va-progress-bar
                :model-value="fp.nullRate"
                :color="fp.nullRate > 30 ? 'danger' : (fp.nullRate > 10 ? 'warning' : 'success')"
                size="small"
              />
            </div>

            <!-- Uniqueness -->
            <div style="display: flex; justify-content: space-between;">
              <span style="color: var(--va-text-secondary);">{{ $t('uniqueness') }}:</span>
              <span style="font-weight: bold; color: var(--va-primary);">
                {{ fp.uniquenessRatio }}% ({{ fp.distinctCount }} 고유값)
              </span>
            </div>

            <!-- Numeric range if available -->
            <div v-if="fp.minValue !== null && fp.maxValue !== null" style="background: var(--va-background-secondary); padding: 0.5rem; border-radius: 6px; font-size: 0.8rem; margin-top: 0.25rem;">
              <div style="display: flex; justify-content: space-between; margin-bottom: 2px;">
                <span>범위 (Min ~ Max):</span>
                <b>{{ fp.minValue }} ~ {{ fp.maxValue }}</b>
              </div>
              <div style="display: flex; justify-content: space-between; margin-bottom: 2px;">
                <span>평균 (Avg):</span>
                <b>{{ fp.avgValue }}</b>
              </div>
              <div v-if="fp.iqrLowerBound !== null" style="display: flex; justify-content: space-between; color: var(--va-text-secondary);">
                <span>IQR 정상한계:</span>
                <span>{{ fp.iqrLowerBound }} ~ {{ fp.iqrUpperBound }}</span>
              </div>
            </div>
          </va-card-content>
        </va-card>
      </div>
      <div v-else-if="!loading" style="text-align: center; padding: 3rem; color: var(--va-text-secondary);">
        <va-icon name="analytics" size="large" style="margin-bottom: 0.5rem; opacity: 0.5;" />
        <p>{{ $t('no_data_available', '프로파일링 가능한 데이터가 없습니다.') }}</p>
      </div>
    </va-inner-loading>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'vuestic-ui'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { formatWithTimezone } from '~/composables/useTimezoneDate'

const props = defineProps<{
  domainId?: string | null
}>()

const { t } = useI18n()
const toast = useToast()

const report = ref<any>(null)
const loading = ref(false)

const previewOutliers = computed(() => {
  if (!report.value?.outliers || !Array.isArray(report.value.outliers)) return []
  return report.value.outliers.slice(0, 50)
})

const fetchProfiling = async () => {
  if (!props.domainId) return
  loading.value = true
  try {
    const res: any = await useCustomFetch(`/domains/${props.domainId}/profiling/report`)
    const rawData = res?.data?.value ?? res
    if (rawData) {
      report.value = rawData
    }
  } catch (e: any) {
    console.error('Failed to fetch profiling data', e)
  } finally {
    loading.value = false
  }
}

const runScan = async () => {
  if (!props.domainId) return
  loading.value = true
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/profiling/scan`, {
      method: 'POST'
    })
    if (res.data?.value) {
      report.value = res.data.value
      toast.init({
        message: '데이터 프로파일링 및 이상치 분석이 완료되었습니다.',
        color: 'success'
      })
    }
  } catch (e: any) {
    toast.init({
      message: '프로파일링 스캔 실패: ' + (e.message || ''),
      color: 'danger'
    })
  } finally {
    loading.value = false
  }
}

watch(() => props.domainId, (val) => {
  if (val) fetchProfiling()
}, { immediate: true })

defineExpose({
  previewOutliers,
  report,
  fetchProfiling
})
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}
.stat-card {
  border: 1px solid var(--va-background-border);
  border-radius: 8px;
  background: var(--va-background-element);
}
</style>
