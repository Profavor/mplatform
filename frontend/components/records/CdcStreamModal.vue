<template>
  <va-modal
    v-model="show"
    :title="$t('cdc_stream')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        ⚡ {{ $t('cdc_stream_desc') }}
      </va-alert>

      <!-- Stream Metrics Banner -->
      <div v-if="streamData" style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center;">
        <div style="display: flex; gap: 1.5rem;">
          <div style="font-size: 0.85rem;">
            <span style="color: var(--va-text-secondary);">{{ $t('active_offset') }}:</span>
            <span style="font-weight: 700; margin-left: 0.3rem;">{{ streamData.activeOffset }}</span>
          </div>
          <div style="font-size: 0.85rem;">
            <span style="color: var(--va-text-secondary);">{{ $t('events_per_sec') }}:</span>
            <span style="font-weight: 700; margin-left: 0.3rem; color: var(--va-primary);">{{ streamData.eventsPerSecond }} eps</span>
          </div>
        </div>
        <div style="display: flex; gap: 0.5rem;">
          <va-button size="small" color="success" :loading="simulating" @click="simulateEvent">
            + {{ $t('simulate_change') }}
          </va-button>
          <va-button preset="secondary" size="small" @click="loadStream">
            새로고침
          </va-button>
        </div>
      </div>

      <!-- Live Stream Events & Inspection -->
      <va-inner-loading :loading="loading">
        <div v-if="streamData?.events?.length > 0" style="display: grid; grid-template-columns: 1.1fr 1fr; gap: 1rem;">
          <!-- Event List -->
          <div style="max-height: 280px; overflow-y: auto; display: flex; flex-direction: column; gap: 0.5rem; border: 1px solid var(--va-background-border); border-radius: 8px; padding: 0.5rem;">
            <div
              v-for="evt in streamData.events"
              :key="evt.eventId"
              style="padding: 0.5rem 0.75rem; border-radius: 6px; border: 1px solid var(--va-background-border); cursor: pointer; display: flex; justify-content: space-between; align-items: center; transition: all 0.2s;"
              :style="{ background: selectedEvent?.eventId === evt.eventId ? 'rgba(33, 150, 243, 0.1)' : 'var(--va-background-card)' }"
              @click="selectedEvent = evt"
            >
              <div style="display: flex; flex-direction: column; gap: 0.2rem;">
                <div style="font-weight: 700; font-size: 0.82rem; display: flex; align-items: center; gap: 0.4rem;">
                  <va-badge
                    :text="evt.operation.toUpperCase()"
                    :color="evt.operation === 'c' ? 'success' : (evt.operation === 'u' ? 'info' : 'danger')"
                    size="small"
                  />
                  <span>{{ evt.recordCode }}</span>
                </div>
                <span style="font-size: 0.72rem; color: var(--va-text-secondary); font-family: monospace;">{{ evt.eventId }}</span>
              </div>
              <span style="font-size: 0.75rem; color: var(--va-text-secondary);">{{ formatTime(evt.timestamp) }}</span>
            </div>
          </div>

          <!-- Payload Inspector -->
          <div style="max-height: 280px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px; padding: 0.75rem; background: var(--va-background-element); display: flex; flex-direction: column; gap: 0.5rem;">
            <div v-if="selectedEvent" style="display: flex; flex-direction: column; gap: 0.5rem; font-size: 0.8rem;">
              <div style="font-weight: 700; color: var(--va-primary);">[CDC Payload Inspector] {{ selectedEvent.eventId }}</div>
              
              <div style="font-weight: 600; font-size: 0.75rem; color: var(--va-text-secondary);">{{ $t('before_payload') }}:</div>
              <pre style="margin: 0; padding: 0.4rem; background: var(--va-background-card); border-radius: 4px; font-size: 0.72rem; overflow-x: auto;">{{ JSON.stringify(selectedEvent.beforePayload, null, 2) || 'null (INSERT)' }}</pre>

              <div style="font-weight: 600; font-size: 0.75rem; color: var(--va-text-secondary); margin-top: 0.3rem;">{{ $t('after_payload') }}:</div>
              <pre style="margin: 0; padding: 0.4rem; background: var(--va-background-card); border-radius: 4px; font-size: 0.72rem; overflow-x: auto;">{{ JSON.stringify(selectedEvent.afterPayload, null, 2) || 'null (DELETE)' }}</pre>
            </div>
            <div v-else style="display: flex; justify-content: center; align-items: center; height: 100%; color: var(--va-text-secondary); font-size: 0.8rem;">
              좌측 목록에서 CDC 이벤트를 선택하세요.
            </div>
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
import { useTimezoneDate } from '~/composables/useTimezoneDate'

const props = defineProps<{
  modelValue: boolean
  domainId: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()
const { formatWithTimezone } = useTimezoneDate()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const streamData = ref<any>(null)
const selectedEvent = ref<any>(null)
const loading = ref(false)
const simulating = ref(false)

const formatTime = (ts: string) => {
  if (!ts) return '-'
  return formatWithTimezone(ts, 'HH:mm:ss')
}

const loadStream = async () => {
  if (!props.domainId) return
  loading.value = true
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/cdc/stream`)
    if (res.data?.value) {
      streamData.value = res.data.value
      if (res.data.value.events?.length > 0 && !selectedEvent.value) {
        selectedEvent.value = res.data.value.events[0]
      }
    }
  } catch (e: any) {
    console.error('Failed to load CDC stream', e)
  } finally {
    loading.value = false
  }
}

const simulateEvent = async () => {
  if (!props.domainId) return
  simulating.value = true
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/cdc/simulate`, {
      method: 'POST',
      body: {
        operation: 'u',
        recordCode: 'REC-003',
        payload: { status: 'UPDATED', updatedAt: new Date().toISOString() }
      }
    })
    if (res.data?.value) {
      await loadStream()
    }
  } catch (e: any) {
    console.error('Failed to simulate CDC event', e)
  } finally {
    simulating.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    selectedEvent.value = null
    loadStream()
  }
})
</script>
