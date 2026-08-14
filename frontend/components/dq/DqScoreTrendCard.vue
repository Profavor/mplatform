<template>
  <va-card style="border-radius: 12px; border: 1px solid var(--va-background-border); overflow: hidden; background: var(--va-background-primary);">
    <div style="padding: 0.85rem 1.25rem; border-bottom: 1px solid var(--va-background-border); display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); flex-wrap: wrap; gap: 0.75rem;">
      <div style="display: flex; align-items: center; gap: 0.6rem;">
        <va-icon name="show_chart" color="primary" />
        <span style="font-size: 1.05rem; font-weight: 700; color: var(--va-text-primary); font-family: 'Pretendard', 'Inter', sans-serif;">
          {{ t('dq_dashboard.score_trend_title') }}
        </span>
        <va-chip v-if="recentSnapshots.length > 0" size="small" color="primary" style="font-weight: 600;">
          {{ t('dq_dashboard.snapshot_count', { count: recentSnapshots.length }) }}
        </va-chip>
      </div>

      <div style="display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
        <div style="display: flex; background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 2px;">
          <button
            v-for="opt in [
              { label: t('dq_dashboard.recent_7_days'), value: 7 },
              { label: t('dq_dashboard.recent_30_days'), value: 30 },
              { label: t('dq_dashboard.recent_90_days'), value: 90 },
              { label: t('dq_dashboard.recent_all'), value: 0 }
            ]"
            :key="opt.value"
            type="button"
            @click="emit('update:trendPeriod', opt.value); emit('period-change')"
            :style="{
              padding: '4px 10px',
              fontSize: '0.78rem',
              fontWeight: trendPeriod === opt.value ? '700' : '500',
              borderRadius: '6px',
              border: 'none',
              cursor: 'pointer',
              transition: 'all 0.2s ease',
              background: trendPeriod === opt.value ? 'var(--va-primary)' : 'transparent',
              color: trendPeriod === opt.value ? '#ffffff' : 'var(--va-text-secondary)'
            }"
          >
            {{ opt.label }}
          </button>
        </div>
        <va-button
          size="small"
          color="primary"
          icon="bolt"
          :loading="scanning"
          @click="onTriggerScan"
        >
          {{ t('dq_dashboard.run_scan') }}
        </va-button>
      </div>
    </div>

    <va-card-content style="padding: 1.25rem; background: var(--va-background-primary);">
      <!-- Empty State -->
      <div v-if="recentSnapshots.length === 0" style="padding: 2.5rem 1rem; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 0.75rem; text-align: center;">
        <div style="width: 54px; height: 54px; border-radius: 16px; background: rgba(25, 118, 210, 0.12); display: flex; align-items: center; justify-content: center; border: 1px solid rgba(25, 118, 210, 0.2);">
          <va-icon name="insights" size="large" color="primary" />
        </div>
        <div style="font-size: 1.05rem; font-weight: 700; color: var(--va-text-primary); font-family: 'Pretendard', 'Inter', sans-serif;">
          {{ t('dq_dashboard.no_snapshots') }}

        </div>
        <div style="font-size: 0.85rem; color: var(--va-text-secondary); max-width: 440px; line-height: 1.5;">
          {{ t('dq_dashboard.no_snapshots_desc') }}
        </div>
        <va-button size="small" color="primary" icon="bolt" :loading="scanning" @click="onTriggerScan" style="margin-top: 0.5rem;">
          {{ t('dq_dashboard.start_scan_now') }}
        </va-button>
      </div>

      <!-- Trend Data State -->
      <div v-else style="display: flex; flex-direction: column; gap: 1.25rem;">
        <!-- Summary Stats Pill Banner -->
        <div style="display: flex; gap: 1.5rem; padding: 0.75rem 1.25rem; background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 10px; align-items: center; flex-wrap: wrap;">
          <div style="display: flex; align-items: center; gap: 0.5rem;">
            <span style="font-size: 0.8rem; color: var(--va-text-secondary); font-weight: 600;">{{ t('dq_dashboard.avg_score') }}</span>
            <span style="font-size: 1rem; font-weight: 800; color: var(--va-primary);">{{ avgTrendScore }}점</span>
          </div>
          <div style="height: 14px; width: 1px; background: var(--va-background-border);"></div>
          <div style="display: flex; align-items: center; gap: 0.5rem;">
            <span style="font-size: 0.8rem; color: var(--va-text-secondary); font-weight: 600;">{{ t('dq_dashboard.max_score') }}</span>
            <span style="font-size: 1rem; font-weight: 800; color: var(--va-success);">{{ maxTrendScore }}점</span>
          </div>
          <div style="height: 14px; width: 1px; background: var(--va-background-border);"></div>
          <div style="display: flex; align-items: center; gap: 0.5rem;">
            <span style="font-size: 0.8rem; color: var(--va-text-secondary); font-weight: 600;">{{ t('dq_dashboard.latest_snapshot') }}</span>
            <span style="font-size: 0.85rem; font-weight: 700; color: var(--va-text-primary); font-family: monospace;">
              {{ formatDateTime(recentSnapshots[0]?.recordedAt) }}
            </span>
          </div>
        </div>


        <!-- Sparkline Bars -->
        <div style="display: flex; align-items: flex-end; gap: 0.6rem; height: 130px; padding: 0.75rem 0.5rem 0.5rem 0.5rem; border-bottom: 1px solid var(--va-background-border); overflow-x: auto;">
          <div
            v-for="(snap, idx) in recentSnapshots"
            :key="snap.id || idx"
            style="flex: 1; min-width: 42px; display: flex; flex-direction: column; align-items: center; gap: 0.35rem; position: relative;"
            class="group cursor-pointer"
          >
            <!-- Modern Glassmorphism Tooltip -->
            <div 
              class="hidden group-hover:flex" 
              style="position: absolute; bottom: 100%; margin-bottom: 8px; flex-direction: column; align-items: center; background: rgba(15, 23, 42, 0.95); backdrop-filter: blur(8px); border: 1px solid rgba(255,255,255,0.15); color: #ffffff; padding: 6px 10px; border-radius: 8px; z-index: 30; white-space: nowrap; box-shadow: 0 10px 25px rgba(0,0,0,0.5);"
            >
              <span style="font-weight: 800; font-size: 0.85rem; color: var(--va-primary);">{{ snap.score }}점</span>
              <span style="font-size: 0.72rem; color: #94a3b8; font-family: monospace;">{{ formatDateTime(snap.recordedAt) }} ({{ snap.scanType }})</span>
              <span style="font-size: 0.72rem; color: #cbd5e1;">{{ t('dq_dashboard.tooltip_info', { violations: snap.totalViolations, total: snap.totalRecords }) }}</span>
            </div>

            <!-- Score Percent text -->
            <span style="font-size: 0.75rem; font-weight: 700; color: var(--va-text-secondary);" class="group-hover:text-primary">
              {{ Math.round(snap.score) }}%
            </span>

            <!-- Bar Background Track -->
            <div style="width: 100%; background: var(--va-background-element); border-radius: 6px 6px 0 0; height: 80px; display: flex; align-items: flex-end; overflow: hidden; border: 1px solid var(--va-background-border);">
              <div
                style="width: 100%; border-radius: 4px 4px 0 0; transition: all 0.3s ease;"
                :style="{
                  height: `${Math.max(snap.score, 6)}%`,
                  background: snap.score >= 90 ? 'linear-gradient(180deg, #34d399 0%, #059669 100%)' : (snap.score >= 70 ? 'linear-gradient(180deg, #fbbf24 0%, #d97706 100%)' : 'linear-gradient(180deg, #f87171 0%, #dc2626 100%)')
                }"
              ></div>
            </div>

            <!-- Date Label -->
            <span style="font-size: 0.7rem; color: var(--va-text-secondary); font-family: monospace; text-align: center; width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
              {{ formatDateShort(snap.recordedAt) }}
            </span>
          </div>
        </div>
      </div>
    </va-card-content>
  </va-card>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { formatWithTimezone } from '~/composables/useTimezoneDate'

const { t } = useI18n()

const props = defineProps<{
  recentSnapshots: any[]
  trendPeriod: number
  scanning: boolean
  avgTrendScore: string | number
  maxTrendScore: string | number
}>()

const emit = defineEmits<{
  (e: 'update:trendPeriod', val: number): void
  (e: 'period-change'): void
  (e: 'trigger-scan'): void
}>()

const onTriggerScan = () => {
  emit('trigger-scan')
}

const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return '-'
  try {
    return formatWithTimezone(dateStr, 'YYYY-MM-DD HH:mm')
  } catch (e) {
    return dateStr
  }
}

const formatDateShort = (dateStr?: string) => {
  if (!dateStr) return ''
  try {
    return formatWithTimezone(dateStr, 'MM/DD')
  } catch (e) {
    return ''
  }
}

defineExpose({
  onTriggerScan
})
</script>
