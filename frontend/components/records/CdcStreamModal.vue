<template>
  <va-modal
    v-model="show"
    :title="modalTitle"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        ⚡ {{ $t('cdc_stream_desc') }}
        <span v-if="recordCodeFormatted" style="margin-left: 0.5rem; font-weight: 700; color: var(--va-primary);">
          ({{ $t('selected_record_target', { code: recordCodeFormatted }) }})
        </span>
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
          <va-button preset="secondary" size="small" icon="refresh" :loading="loading" @click="loadStream">
            {{ $t('refresh') }}
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
          <div style="max-height: 340px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px; padding: 0.75rem; background: var(--va-background-element); display: flex; flex-direction: column; gap: 0.75rem;">
            <div v-if="selectedEvent" style="display: flex; flex-direction: column; gap: 0.75rem;">
              <!-- Header & View Mode Switcher -->
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <div style="font-weight: 700; font-size: 0.85rem; color: var(--va-primary); display: flex; align-items: center; gap: 0.5rem;">
                  <span>🔍 {{ selectedEvent.eventId }}</span>
                  <va-badge
                    :text="getOperationLabel(selectedEvent.operation)"
                    :color="selectedEvent.operation === 'c' ? 'success' : (selectedEvent.operation === 'u' ? 'info' : 'danger')"
                    size="small"
                  />
                </div>
                <div style="display: flex; gap: 0.3rem;">
                  <va-button
                    size="small"
                    :preset="viewMode === 'table' ? 'primary' : 'secondary'"
                    @click="viewMode = 'table'"
                  >
                    {{ $t('table_view') }}
                  </va-button>
                  <va-button
                    size="small"
                    :preset="viewMode === 'json' ? 'primary' : 'secondary'"
                    @click="viewMode = 'json'"
                  >
                    {{ $t('json_view') }}
                  </va-button>
                </div>
              </div>

              <!-- Table View: Friendly Field Diff -->
              <div v-if="viewMode === 'table'">
                <table v-if="diffItems.length > 0" style="width: 100%; border-collapse: collapse; font-size: 0.8rem; background: var(--va-background-card); border-radius: 6px; overflow: hidden;">
                  <thead>
                    <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border); text-align: left;">
                      <th style="padding: 0.5rem 0.6rem; font-weight: 600; width: 30%;">{{ $t('diff_field_name') }}</th>
                      <th style="padding: 0.5rem 0.6rem; font-weight: 600; width: 35%;">{{ $t('diff_before') }}</th>
                      <th style="padding: 0.5rem 0.6rem; font-weight: 600; width: 35%;">{{ $t('diff_after') }}</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr
                      v-for="item in diffItems"
                      :key="item.key"
                      style="border-bottom: 1px solid var(--va-background-border); transition: background 0.15s;"
                      :style="{ background: item.isChanged ? 'rgba(33, 150, 243, 0.06)' : 'transparent' }"
                    >
                      <td style="padding: 0.5rem 0.6rem; font-weight: 600; color: var(--va-text-primary);">
                        {{ item.label }}
                        <span v-if="item.key !== item.label" style="font-size: 0.7rem; color: var(--va-text-secondary); margin-left: 0.2rem; font-family: monospace;">({{ item.key }})</span>
                      </td>
                      <td style="padding: 0.5rem 0.6rem; color: var(--va-text-secondary); word-break: break-all;">
                        <span :style="{ textDecoration: item.isChanged && item.before !== '-' ? 'line-through' : 'none', color: item.isChanged ? 'var(--va-danger)' : 'inherit' }">
                          {{ item.before }}
                        </span>
                      </td>
                      <td style="padding: 0.5rem 0.6rem; font-weight: 500; word-break: break-all;" :style="{ color: item.isChanged ? 'var(--va-primary)' : 'inherit' }">
                        {{ item.after }}
                      </td>
                    </tr>
                  </tbody>
                </table>
                <div v-else style="padding: 1.5rem; text-align: center; color: var(--va-text-secondary); font-size: 0.8rem;">
                  {{ $t('no_diff_data') }}
                </div>
              </div>

              <!-- JSON View: Developer raw payloads -->
              <div v-else style="display: flex; flex-direction: column; gap: 0.5rem; font-size: 0.78rem;">
                <div style="font-weight: 600; font-size: 0.75rem; color: var(--va-text-secondary);">{{ $t('before_payload') }}:</div>
                <pre style="margin: 0; padding: 0.5rem; background: var(--va-background-card); border-radius: 4px; font-size: 0.72rem; overflow-x: auto; border: 1px solid var(--va-background-border);">{{ selectedEvent.beforePayload ? JSON.stringify(selectedEvent.beforePayload, null, 2) : 'null (INSERT)' }}</pre>

                <div style="font-weight: 600; font-size: 0.75rem; color: var(--va-text-secondary); margin-top: 0.3rem;">{{ $t('after_payload') }}:</div>
                <pre style="margin: 0; padding: 0.5rem; background: var(--va-background-card); border-radius: 4px; font-size: 0.72rem; overflow-x: auto; border: 1px solid var(--va-background-border);">{{ selectedEvent.afterPayload ? JSON.stringify(selectedEvent.afterPayload, null, 2) : 'null (DELETE)' }}</pre>
              </div>
            </div>
            <div v-else style="display: flex; justify-content: center; align-items: center; height: 100%; min-height: 180px; color: var(--va-text-secondary); font-size: 0.8rem;">
              {{ $t('select_cdc_event_guide') }}
            </div>
          </div>
        </div>
        <div v-else-if="!loading" style="padding: 2rem; text-align: center; color: var(--va-text-secondary); font-size: 0.85rem;">
          {{ $t('no_cdc_events_in_domain') }}
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
import { useCookie } from '#app'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { useTimezoneDate } from '~/composables/useTimezoneDate'
import { formatMultilingual } from '~/composables/useMultilingual'

const props = defineProps<{
  modelValue: boolean
  domainId: string
  recordId?: string | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()
const { formatWithTimezone } = useTimezoneDate()
const localeCookie = useCookie<string>('locale', { default: () => 'ko' })

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const recordCodeFormatted = computed(() => {
  if (!props.recordId) return null
  if (props.recordId.startsWith('REC-')) return props.recordId
  return `REC-${props.recordId.substring(0, 8)}`
})

const modalTitle = computed(() => {
  const base = t('cdc_stream')
  if (recordCodeFormatted.value) {
    return `${base} [${recordCodeFormatted.value}]`
  }
  return base
})

const streamData = ref<any>(null)
const selectedEvent = ref<any>(null)
const loading = ref(false)
const viewMode = ref<'table' | 'json'>('table')
const fieldDefinitions = ref<any[]>([])

const formatTime = (ts: string) => {
  if (!ts) return '-'
  return formatWithTimezone(ts, 'YYYY-MM-DD HH:mm:ss')
}

const getOperationLabel = (op: string) => {
  if (op === 'c') return t('record_create')
  if (op === 'u') return t('record_update')
  if (op === 'd') return t('record_delete')
  return op ? op.toUpperCase() : ''
}

const getFieldLabel = (key: string): string => {
  const fd = fieldDefinitions.value.find(f => f.key === key)
  if (!fd) return key
  if (typeof fd.name === 'object' && fd.name !== null) {
    const loc = localeCookie.value || 'ko'
    return fd.name[loc] || fd.name.ko || fd.name.en || fd.key || key
  }
  return fd.name || fd.key || key
}

const formatValue = (val: any): string => {
  if (val === undefined || val === null || val === '') return '-'
  if (typeof val === 'object') {
    return formatMultilingual(val) || JSON.stringify(val)
  }
  return String(val)
}

const diffItems = computed(() => {
  if (!selectedEvent.value) return []
  const before = selectedEvent.value.beforePayload || {}
  const after = selectedEvent.value.afterPayload || {}

  const allKeys = Array.from(new Set([...Object.keys(before), ...Object.keys(after)]))
  if (allKeys.length === 0) return []

  return allKeys.map(k => {
    const bVal = before[k]
    const aVal = after[k]
    const bFormatted = formatValue(bVal)
    const aFormatted = formatValue(aVal)
    const isChanged = bFormatted !== aFormatted

    return {
      key: k,
      label: getFieldLabel(k),
      before: bFormatted,
      after: aFormatted,
      isChanged
    }
  })
})

const loadFields = async () => {
  if (!props.domainId) return
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/fields`)
    if (Array.isArray(res)) {
      fieldDefinitions.value = res
    } else if (res?.data?.value && Array.isArray(res.data.value)) {
      fieldDefinitions.value = res.data.value
    }
  } catch (e) {
    console.error('Failed to load domain field definitions', e)
  }
}

const loadStream = async () => {
  if (!props.domainId) return
  loading.value = true
  try {
    let url = `/domains/${props.domainId}/cdc/stream`
    if (props.recordId) {
      url += `?recordId=${props.recordId}`
    }
    const res = await useCustomFetch(url)
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

watch(() => props.modelValue, (val) => {
  if (val) {
    selectedEvent.value = null
    loadFields()
    loadStream()
  }
}, { immediate: true })
</script>
