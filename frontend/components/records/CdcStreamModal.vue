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
        <div v-if="streamData?.events?.length > 0" style="display: grid; grid-template-columns: 1fr 1.3fr; gap: 1rem;">
          <!-- Event List -->
          <div style="max-height: 380px; overflow-y: auto; display: flex; flex-direction: column; gap: 0.5rem; border: 1px solid var(--va-background-border); border-radius: 8px; padding: 0.5rem;">
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
                  <span>{{ formatRecordIdentifier(evt.recordCode, evt.recordId) }}</span>
                </div>
                <span style="font-size: 0.72rem; color: var(--va-text-secondary); font-family: monospace;">{{ evt.eventId }}</span>
              </div>
              <span style="font-size: 0.75rem; color: var(--va-text-secondary);">{{ formatTime(evt.timestamp) }}</span>
            </div>
          </div>

          <!-- Payload Inspector -->
          <div style="max-height: 380px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px; padding: 0.75rem; background: var(--va-background-element); display: flex; flex-direction: column; gap: 0.75rem;">
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

              <!-- Table View: Friendly 3-Column Field Diff -->
              <div v-if="viewMode === 'table'">
                <table v-if="diffItems.length > 0" style="width: 100%; border-collapse: collapse; font-size: 0.8rem; background: var(--va-background-card); border-radius: 6px; overflow: hidden; border: 1px solid var(--va-background-border);">
                  <thead>
                    <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border); text-align: left;">
                      <th style="padding: 0.5rem 0.6rem; font-weight: 600; width: 28%; color: var(--va-text-secondary);">{{ $t('property_field_name') || '속성 / 필드명' }}</th>
                      <th style="padding: 0.5rem 0.6rem; font-weight: 600; width: 36%; color: #ef4444;">{{ $t('previous_value') || '변경 전 (Previous Value)' }}</th>
                      <th style="padding: 0.5rem 0.6rem; font-weight: 600; width: 36%; color: #22c55e;">{{ $t('new_value') || '변경 후 (New Value)' }}</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr
                      v-for="item in diffItems"
                      :key="item.key"
                      style="border-bottom: 1px solid var(--va-background-border); transition: background 0.15s;"
                      :style="{ background: item.isChanged ? 'rgba(33, 150, 243, 0.04)' : 'transparent' }"
                    >
                      <!-- Field Name -->
                      <td style="padding: 0.5rem 0.6rem; font-weight: 600; color: var(--va-text-primary); vertical-align: top;">
                        {{ item.label }}
                      </td>

                      <!-- Previous Value -->
                      <td style="padding: 0.5rem 0.6rem; color: #b91c1c; background: rgba(239, 68, 68, 0.04); vertical-align: top; word-break: break-all;">
                        <!-- If FILE Type -->
                        <template v-if="getFieldByKey(item.key)?.type === 'FILE'">
                          <div v-if="getFilesList(item.rawBefore).length > 0" style="display: flex; flex-direction: column; gap: 4px;">
                            <a
                              v-for="(fUrl, idx) in getFilesList(item.rawBefore)"
                              :key="idx"
                              href="#"
                              @click.prevent="downloadFileWithAuth(fUrl.url || fUrl, fUrl.name || extractFilename(fUrl.url || fUrl))"
                              style="color: #b91c1c; text-decoration: underline; font-weight: 600; font-size: 0.78rem; display: inline-flex; align-items: center; gap: 4px;"
                            >
                              📎 {{ fUrl.name || extractFilename(fUrl.url || fUrl) }}
                            </a>
                          </div>
                          <span v-else>{{ $t('none') || '-' }}</span>
                        </template>

                        <!-- If JSON Type -->
                        <template v-else-if="getFieldByKey(item.key)?.type === 'JSON' && getTableRows(item.rawBefore).length > 0">
                          <div style="border: 1px solid rgba(239, 68, 68, 0.3); border-radius: 6px; overflow: hidden; background: var(--va-background-element);">
                            <table style="width: 100%; border-collapse: collapse; font-size: 0.75rem;">
                              <thead>
                                <tr style="background: var(--va-background-secondary); border-bottom: 1px solid var(--va-background-border);">
                                  <th style="padding: 0.25rem 0.35rem; width: 25px; text-align: center; color: var(--va-text-secondary);">#</th>
                                  <th v-for="col in getTableColumnsForField(getFieldByKey(item.key), item.rawBefore)" :key="col.key" style="padding: 0.25rem 0.4rem; text-align: left; color: var(--va-text-primary); font-weight: 600;">
                                    {{ getColLabel(col) }}
                                  </th>
                                </tr>
                              </thead>
                              <tbody>
                                <tr v-for="(row, rIdx) in getTableRows(item.rawBefore)" :key="rIdx" style="border-bottom: 1px solid var(--va-background-border);">
                                  <td style="padding: 0.25rem 0.35rem; text-align: center; color: var(--va-text-secondary);">{{ rIdx + 1 }}</td>
                                  <td v-for="col in getTableColumnsForField(getFieldByKey(item.key), item.rawBefore)" :key="col.key" style="padding: 0.25rem 0.4rem; color: #b91c1c;">
                                    {{ formatTableCell(row[col.key], col) }}
                                  </td>
                                </tr>
                              </tbody>
                            </table>
                          </div>
                        </template>

                        <!-- Other Field -->
                        <template v-else>
                          <span :style="{ textDecoration: item.isChanged && item.before !== '-' ? 'line-through' : 'none' }">
                            {{ item.before }}
                          </span>
                        </template>
                      </td>

                      <!-- New Value -->
                      <td style="padding: 0.5rem 0.6rem; color: #15803d; background: rgba(34, 197, 94, 0.04); font-weight: 600; vertical-align: top; word-break: break-all;">
                        <!-- If FILE Type -->
                        <template v-if="getFieldByKey(item.key)?.type === 'FILE'">
                          <div v-if="getFilesList(item.rawAfter).length > 0" style="display: flex; flex-direction: column; gap: 4px;">
                            <a
                              v-for="(fUrl, idx) in getFilesList(item.rawAfter)"
                              :key="idx"
                              href="#"
                              @click.prevent="downloadFileWithAuth(fUrl.url || fUrl, fUrl.name || extractFilename(fUrl.url || fUrl))"
                              style="color: #15803d; text-decoration: underline; font-weight: 600; font-size: 0.78rem; display: inline-flex; align-items: center; gap: 4px;"
                            >
                              📎 {{ fUrl.name || extractFilename(fUrl.url || fUrl) }}
                            </a>
                          </div>
                          <span v-else>{{ $t('none') || '-' }}</span>
                        </template>

                        <!-- If JSON Type -->
                        <template v-else-if="getFieldByKey(item.key)?.type === 'JSON' && getTableRows(item.rawAfter).length > 0">
                          <div style="border: 1px solid rgba(34, 197, 94, 0.3); border-radius: 6px; overflow: hidden; background: var(--va-background-element);">
                            <table style="width: 100%; border-collapse: collapse; font-size: 0.75rem;">
                              <thead>
                                <tr style="background: var(--va-background-secondary); border-bottom: 1px solid var(--va-background-border);">
                                  <th style="padding: 0.25rem 0.35rem; width: 25px; text-align: center; color: var(--va-text-secondary);">#</th>
                                  <th v-for="col in getTableColumnsForField(getFieldByKey(item.key), item.rawAfter)" :key="col.key" style="padding: 0.25rem 0.4rem; text-align: left; color: var(--va-text-primary); font-weight: 600;">
                                    {{ getColLabel(col) }}
                                  </th>
                                </tr>
                              </thead>
                              <tbody>
                                <tr v-for="(row, rIdx) in getTableRows(item.rawAfter)" :key="rIdx" style="border-bottom: 1px solid var(--va-background-border);">
                                  <td style="padding: 0.25rem 0.35rem; text-align: center; color: var(--va-text-secondary);">{{ rIdx + 1 }}</td>
                                  <td v-for="col in getTableColumnsForField(getFieldByKey(item.key), item.rawAfter)" :key="col.key" style="padding: 0.25rem 0.4rem; color: #15803d;">
                                    {{ formatTableCell(row[col.key], col) }}
                                  </td>
                                </tr>
                              </tbody>
                            </table>
                          </div>
                        </template>

                        <!-- Other Field -->
                        <template v-else>
                          {{ item.after }}
                        </template>
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
import { useFileDownloader } from '~/composables/useFileDownloader'

const props = defineProps<{
  modelValue: boolean
  domainId: string
  recordId?: string | null
  domainReferences?: Record<string, any>
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()
const { formatWithTimezone } = useTimezoneDate()
const { downloadFileWithAuth } = useFileDownloader()
const localeCookie = useCookie<string>('locale', { default: () => 'ko' })
const domainRefCache = ref<Record<string, string>>({})

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

const formatRecordIdentifier = (code: string | null, id: string | null) => {
  if (code) return code
  if (id) {
    if (id.startsWith('REC-')) return id
    return `REC-${id.substring(0, 8)}`
  }
  return '-'
}

const getOperationLabel = (op: string) => {
  if (op === 'c') return t('record_create')
  if (op === 'u') return t('record_update')
  if (op === 'd') return t('record_delete')
  return op ? op.toUpperCase() : ''
}

const getFieldByKey = (key: string) => {
  return fieldDefinitions.value.find(f => f.key === key || (f.key && f.key.toLowerCase() === key.toLowerCase()) || String(f.id) === String(key))
}

const getFieldLabel = (key: string): string => {
  const fd = getFieldByKey(key)
  if (!fd) return key
  if (typeof fd.name === 'object' && fd.name !== null) {
    const loc = localeCookie.value || 'ko'
    return fd.name[loc] || fd.name.ko || fd.name.en || fd.key || key
  }
  return fd.name || fd.key || key
}

const getFilesList = (v: any): any[] => {
  if (!v) return []
  if (Array.isArray(v)) return v.filter(Boolean)
  if (typeof v === 'string') {
    const trimmed = v.trim()
    if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
      try {
        const parsed = JSON.parse(trimmed)
        return Array.isArray(parsed) ? parsed.filter(Boolean) : []
      } catch (e) {
        return [v]
      }
    }
    return [v]
  }
  if (typeof v === 'object' && v !== null) return [v]
  return []
}

const extractFilename = (input: any): string => {
  if (!input) return ''
  if (typeof input === 'object') {
    if (input.name && input.name !== 'Download') return input.name
    if (input.originalName) return input.originalName
    if (input.url) input = input.url
    else return ''
  }
  let str = String(input).trim()
  if (!str || str === '-' || str === '[]' || str === '{}' || str === 'null' || str === 'undefined') return ''
  try {
    if (str.startsWith('{') || str.startsWith('[')) {
      const parsed = JSON.parse(str)
      if (Array.isArray(parsed) && parsed.length > 0) return extractFilename(parsed[0])
      if (typeof parsed === 'object' && (parsed.name || parsed.originalName)) return parsed.name || parsed.originalName
    }
  } catch (e) {}
  try {
    if (str.includes('?name=')) return decodeURIComponent(str.split('?name=')[1].split('&')[0])
    if (str.includes('?filename=')) return decodeURIComponent(str.split('?filename=')[1].split('&')[0])
    const fname = decodeURIComponent(str.split('/').pop()?.split('?')[0] || '')
    if (fname && fname !== '-' && fname !== 'null') return fname
  } catch (e) {}
  return str
}

const getTableRows = (val: any): any[] => {
  if (!val) return []
  if (Array.isArray(val)) return val
  if (typeof val === 'string') {
    try {
      const parsed = JSON.parse(val)
      return Array.isArray(parsed) ? parsed : []
    } catch (e) {
      return []
    }
  }
  return []
}

const getTableColumnsForField = (field: any, val: any) => {
  if (field && field.options) {
    try {
      const opts = typeof field.options === 'string' ? JSON.parse(field.options) : field.options
      if (opts?.tableSchema?.columns?.length > 0) return opts.tableSchema.columns
      if (opts?.columns?.length > 0) return opts.columns
    } catch (e) {}
  }
  const rows = getTableRows(val)
  if (rows.length > 0 && typeof rows[0] === 'object' && rows[0] !== null) {
    return Object.keys(rows[0]).map(k => ({ key: k, name: k, type: 'TEXT' }))
  }
  return []
}

const getColLabel = (col: any) => {
  if (!col) return ''
  if (typeof col.name === 'object' && col.name !== null) {
    const loc = localeCookie.value || 'ko'
    return col.name[loc] || col.name.ko || col.name.en || col.key
  }
  return col.name || col.key
}

const formatTableCell = (val: any, col: any) => {
  if (val === null || val === undefined || val === '') return '-'
  if (col && col.type === 'SELECT' && col.options) {
    let opts = []
    if (typeof col.options === 'string') {
      try { opts = JSON.parse(col.options) } catch (e) {}
    } else if (Array.isArray(col.options)) {
      opts = col.options
    }
    const found = opts.find((o: any) => (o.value || o.key || o.code) === val)
    if (found) {
      if (typeof found.label === 'object') {
        const loc = localeCookie.value || 'ko'
        return found.label[loc] || found.label.ko || found.label.en || found.label
      }
      return found.label || found.name || val
    }
  }
  if (typeof val === 'object') {
    const loc = localeCookie.value || 'ko'
    return val[loc] || val.ko || val.en || JSON.stringify(val)
  }
  return String(val)
}

const formatValue = (val: any, fieldKey?: string): string => {
  if (val === undefined || val === null || val === '') return '-'
  
  const f = fieldKey ? getFieldByKey(fieldKey) : null
  if (f && f.type === 'DOMAIN_REFERENCE' && props.domainReferences) {
    const refData = props.domainReferences[fieldKey || ''] || props.domainReferences[f.key]
    if (refData && refData.records) {
      const rec = refData.records.find((r: any) => String(r.id) === String(val) || String(r.code) === String(val))
      if (rec) {
        const idStr = rec.code || (rec.data && (rec.data.code || rec.data.EP_NO || rec.data.empNo)) || rec.id?.substring(0, 8)
        const nameStr = (rec.data && (rec.data.name || rec.data.EP_NAME || rec.data.empName || rec.data.title)) || ''
        return nameStr ? `[${idStr}] ${nameStr}` : `REC-${idStr}`
      }
    }
  }

  // Raw UUID string pattern
  if (typeof val === 'string' && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(val)) {
    return `REC-${val.substring(0, 8)}`
  }

  let obj = val
  if (typeof val === 'string' && val.trim().startsWith('{') && val.trim().endsWith('}')) {
    try { obj = JSON.parse(val) } catch (e) {}
  }

  if (typeof obj === 'object' && obj !== null) {
    if ('ko' in obj || 'en' in obj) {
      const loc = localeCookie.value === 'en' ? 'en' : 'ko'
      const primary = obj[loc] || obj.ko || obj.en
      const secondary = loc === 'ko' ? obj.en : obj.ko
      if (primary && secondary && primary !== secondary) {
        return `${primary} (${secondary})`
      }
      return primary || secondary || '-'
    }
    return JSON.stringify(obj)
  }

  return String(val)
}

const diffItems = computed(() => {
  if (!selectedEvent.value) return []
  const before = selectedEvent.value.beforePayload || {}
  const after = selectedEvent.value.afterPayload || {}

  const allKeys = Array.from(new Set([...Object.keys(before), ...Object.keys(after)]))
    .filter(k => !k.startsWith('_idx_') && k !== 'id' && k !== 'domainId' && k !== 'nodeId' && k !== 'createdAt' && k !== 'updatedAt')

  if (allKeys.length === 0) return []

  return allKeys.map(k => {
    const bVal = before[k]
    const aVal = after[k]
    const bFormatted = formatValue(bVal, k)
    const aFormatted = formatValue(aVal, k)
    const isChanged = bFormatted !== aFormatted || JSON.stringify(bVal) !== JSON.stringify(aVal)

    return {
      key: k,
      label: getFieldLabel(k),
      before: bFormatted,
      after: aFormatted,
      rawBefore: bVal,
      rawAfter: aVal,
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
