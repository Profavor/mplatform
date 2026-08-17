<template>
  <AppModal
    v-model="show"
    :title="$t('channel_metrics')"
    icon="analytics"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <!-- Header Summary Card -->
      <va-card flat bordered style="padding: 1.25rem; background: var(--va-background-element); border-radius: 10px;">
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
          <div>
            <div style="display: flex; align-items: center; gap: 0.5rem;">
              <span style="font-weight: 700; font-size: 1.2rem; color: var(--va-text-primary);">
                {{ metrics?.channelName || channelName || '-' }}
              </span>
              <va-badge
                :text="getHealthLabel(metrics?.healthStatus)"
                :color="getHealthColor(metrics?.healthStatus)"
                size="small"
              />
            </div>
            <div style="font-size: 0.85rem; color: var(--va-text-secondary); margin-top: 0.25rem;">
              타입: {{ metrics?.channelType || '-' }} · 24시간 성공률: <b>{{ metrics?.successRate ?? 100 }}%</b>
            </div>
          </div>

          <div style="display: flex; align-items: center; gap: 0.75rem;">
            <va-button
              preset="outline"
              color="primary"
              icon="network_ping"
              :loading="pinging"
              @click="runPing"
            >
              {{ pinging ? $t('ping_testing') : $t('ping_test') }}
            </va-button>
          </div>
        </div>

        <!-- Ping Result Banner if available -->
        <div v-if="metrics?.lastPingMessage" style="margin-top: 0.75rem; padding: 0.5rem 0.75rem; background: rgba(46, 125, 50, 0.08); border-radius: 6px; font-size: 0.85rem; color: var(--va-success); display: flex; align-items: center; gap: 0.5rem;">
          <va-icon name="check_circle" size="small" color="success" />
          <span>{{ metrics.lastPingMessage }} (지연 시간: <b>{{ metrics.lastPingLatencyMs }}ms</b>)</span>
        </div>
      </va-card>

      <!-- 4 Stats Cards -->
      <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 0.75rem;">
        <va-card flat bordered style="padding: 0.75rem; text-align: center;">
          <div style="font-size: 0.8rem; color: var(--va-text-secondary);">{{ $t('total_requests_24h') }}</div>
          <div style="font-size: 1.4rem; font-weight: 700; color: var(--va-primary); margin-top: 0.25rem;">
            {{ metrics?.totalRequests || 0 }}
          </div>
        </va-card>

        <va-card flat bordered style="padding: 0.75rem; text-align: center;">
          <div style="font-size: 0.8rem; color: var(--va-text-secondary);">{{ $t('success_count') }}</div>
          <div style="font-size: 1.4rem; font-weight: 700; color: var(--va-success); margin-top: 0.25rem;">
            {{ metrics?.successCount || 0 }}
          </div>
        </va-card>

        <va-card flat bordered style="padding: 0.75rem; text-align: center;">
          <div style="font-size: 0.8rem; color: var(--va-text-secondary);">{{ $t('fail_count') }}</div>
          <div style="font-size: 1.4rem; font-weight: 700; color: var(--va-warning); margin-top: 0.25rem;">
            {{ metrics?.failCount || 0 }}
          </div>
        </va-card>

        <va-card flat bordered style="padding: 0.75rem; text-align: center;">
          <div style="font-size: 0.8rem; color: var(--va-text-secondary);">{{ $t('dlq_count') }}</div>
          <div style="font-size: 1.4rem; font-weight: 700; color: var(--va-danger); margin-top: 0.25rem;">
            {{ metrics?.dlqCount || 0 }}
          </div>
        </va-card>
      </div>

      <!-- 24h Hourly Trend Table -->
      <div style="display: flex; flex-direction: column; gap: 0.5rem;">
        <div style="font-weight: 600; font-size: 0.9rem; color: var(--va-text-primary);">
          📊 {{ $t('throughput_24h') }}
        </div>
        <div style="max-height: 220px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
          <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: center;">
            <thead>
              <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                <th style="padding: 0.5rem;">{{ $t('time_slot') }}</th>
                <th style="padding: 0.5rem; color: var(--va-success);">{{ $t('success_count') }}</th>
                <th style="padding: 0.5rem; color: var(--va-warning);">{{ $t('fail_count') }}</th>
                <th style="padding: 0.5rem; color: var(--va-danger);">{{ $t('dlq_count') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="h in metrics?.hourlyStats || []"
                :key="h.timeSlot"
                style="border-bottom: 1px solid var(--va-background-border);"
              >
                <td style="padding: 0.4rem; font-weight: 600;">{{ h.timeSlot }}</td>
                <td style="padding: 0.4rem; color: var(--va-success);">{{ h.successCount }}</td>
                <td style="padding: 0.4rem; color: var(--va-warning);">{{ h.failCount }}</td>
                <td style="padding: 0.4rem; color: var(--va-danger);">{{ h.dlqCount }}</td>
              </tr>
              <tr v-if="!metrics?.hourlyStats?.length">
                <td colspan="4" style="padding: 1rem; color: var(--va-text-secondary);">데이터 없음</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

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
import { useToast } from 'vuestic-ui'
import { useCustomFetch } from '~/composables/useCustomFetch'
import AppModal from '~/components/common/AppModal.vue'

const props = defineProps<{
  modelValue: boolean
  channelId?: string
  channelName?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()
const toast = useToast()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const pinging = ref(false)
const metrics = ref<any>(null)

const getHealthLabel = (status: string) => {
  if (status === 'HEALTHY') return t('health_healthy')
  if (status === 'DEGRADED') return t('health_degraded')
  if (status === 'UNHEALTHY') return t('health_unhealthy')
  return status || t('health_healthy')
}

const getHealthColor = (status: string) => {
  if (status === 'HEALTHY') return 'success'
  if (status === 'DEGRADED') return 'warning'
  if (status === 'UNHEALTHY') return 'danger'
  return 'primary'
}

const fetchMetrics = async () => {
  if (!props.channelId) return
  loading.value = true
  try {
    const res = await useCustomFetch(`/admin/integration/channels/${props.channelId}/metrics`)
    if (res.data?.value) {
      metrics.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to fetch metrics', e)
  } finally {
    loading.value = false
  }
}

const runPing = async () => {
  if (!props.channelId) return
  pinging.value = true
  try {
    const res = await useCustomFetch(`/admin/integration/channels/${props.channelId}/ping`, {
      method: 'POST'
    })
    if (res.data?.value) {
      metrics.value = res.data.value
      toast.init({
        message: t('ping_success', { ms: res.data.value.lastPingLatencyMs }),
        color: 'success'
      })
    }
  } catch (e: any) {
    toast.init({
      message: 'Ping 테스트 실패: ' + (e.message || ''),
      color: 'danger'
    })
  } finally {
    pinging.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val && props.channelId) {
    fetchMetrics()
  }
})
</script>
