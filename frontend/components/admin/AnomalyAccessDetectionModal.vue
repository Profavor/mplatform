<template>
  <AppModal
    v-model="show"
    :title="$t('anomaly_detection')"
    icon="security"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="danger" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🚨 {{ $t('anomaly_detection_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="securityData" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Threat Score Banner -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center;">
            <div style="display: flex; flex-direction: column; gap: 0.2rem;">
              <span style="font-weight: 700; font-size: 0.9rem;">
                {{ securityData.summary }}
              </span>
            </div>
            <div style="display: flex; gap: 0.4rem; align-items: center;">
              <span style="font-size: 0.8rem; color: var(--va-text-secondary);">{{ $t('threat_score') }}:</span>
              <va-badge
                :text="`${securityData.threatLevelScore}점`"
                :color="securityData.threatLevelScore > 50 ? 'danger' : 'success'"
                size="small"
              />
            </div>
          </div>

          <!-- Security Events Table -->
          <div style="max-height: 280px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.5rem 0.75rem; width: 80px;">이벤트</th>
                  <th style="padding: 0.5rem 0.75rem; width: 100px;">행위자</th>
                  <th style="padding: 0.5rem 0.75rem; width: 80px; text-align: center;">{{ $t('threat_level') }}</th>
                  <th style="padding: 0.5rem 0.75rem;">이상 징후 상세</th>
                  <th style="padding: 0.5rem 0.75rem; width: 100px; text-align: center;">조치</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="e in securityData.events"
                  :key="e.eventId"
                  style="border-bottom: 1px solid var(--va-background-border);"
                >
                  <td style="padding: 0.5rem 0.75rem; font-weight: 700;">{{ e.eventId }}</td>
                  <td style="padding: 0.5rem 0.75rem; color: var(--va-primary); font-weight: 600;">
                    {{ e.username }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: center;">
                    <va-badge
                      :text="e.threatLevel"
                      :color="e.threatLevel === 'CRITICAL' ? 'danger' : 'warning'"
                      size="small"
                    />
                  </td>
                  <td style="padding: 0.5rem 0.75rem; color: var(--va-text-secondary); font-size: 0.78rem;">
                    <div>{{ e.actionType }}</div>
                    <div style="font-size: 0.72rem; color: var(--va-text-primary);">{{ e.details }} (IP: {{ e.sourceIp }})</div>
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: center;">
                    <va-badge v-if="e.blocked" :text="$t('blocked_badge')" color="secondary" size="small" />
                    <va-button
                      v-else
                      color="danger"
                      size="small"
                      @click="blockUser(e.userId)"
                    >
                      {{ $t('block_actor') }}
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

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const securityData = ref<any>(null)
const loading = ref(false)

const loadEvents = async () => {
  loading.value = true
  try {
    const res = await useCustomFetch('/security/anomaly-detection')
    if (res.data?.value) {
      securityData.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to load anomaly detection events', e)
  } finally {
    loading.value = false
  }
}

const blockUser = async (userId: string) => {
  try {
    const res = await useCustomFetch('/security/anomaly-detection/block', {
      method: 'POST',
      body: { userId, reason: '비정상 대량 유출 이상 징후 탐지' }
    })
    if (res.data?.value) {
      alert('해당 의심 행위자가 즉시 차단되었습니다.')
      await loadEvents()
    }
  } catch (e: any) {
    console.error('Failed to block user', e)
  }
}

watch(() => props.modelValue, (val) => {
  if (val) loadEvents()
})
</script>
