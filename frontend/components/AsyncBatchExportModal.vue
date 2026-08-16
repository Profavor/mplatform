<template>
  <va-modal
    v-model="show"
    :title="$t('async_export_title')"
    hide-default-actions
  >
    <div style="padding: 0.5rem; display: flex; flex-direction: column; gap: 1.25rem;">
      <div v-if="!taskInfo" style="display: flex; flex-direction: column; gap: 1rem;">
        <p style="font-size: 0.9rem; color: var(--va-text-secondary); margin: 0;">
          {{ $t('async_export_desc') }}
        </p>
        <va-button color="primary" icon="download" @click="startExport">
          {{ $t('start_export') }}
        </va-button>
      </div>

      <div v-else style="display: flex; flex-direction: column; gap: 1rem;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-weight: 600; font-size: 0.95rem;">{{ $t('export_progress') }}</span>
          <va-badge :text="taskStatusText" :color="taskStatus === 'COMPLETED' ? 'success' : (taskStatus === 'FAILED' ? 'danger' : 'primary')" />
        </div>

        <va-progress-bar :model-value="taskInfo.progressPercent" color="primary" animated />

        <div style="font-size: 0.85rem; color: var(--va-text-secondary); text-align: center;">
          {{ taskInfo.processedCount?.toLocaleString() || 0 }} / {{ taskInfo.totalCount?.toLocaleString() || 0 }} {{ $t('records_count_suffix') }} ({{ taskInfo.progressPercent || 0 }}%)
        </div>

        <div v-if="taskStatus === 'COMPLETED'" style="margin-top: 0.5rem; text-align: center;">
          <va-button color="success" icon="file_download" :loading="downloading" @click="downloadFile">
            {{ $t('download_file') }}
          </va-button>
        </div>
      </div>

      <div style="display: flex; justify-content: flex-end; margin-top: 1rem;">
        <va-button preset="secondary" @click="show = false">{{ $t('close') }}</va-button>
      </div>
    </div>
  </va-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'

const props = defineProps<{
  modelValue: boolean
  domainId: string | null
  gridApi?: any
  domainReferences?: Record<string, any>
  fields?: Array<any>
}>()

const emit = defineEmits(['update:modelValue'])

const { t, locale } = useI18n()
const { customFetch } = useCustomFetch()

const show = ref(props.modelValue)
const taskInfo = ref<any>(null)
let pollTimer: any = null

const getFieldByKey = (key: string) => {
  if (!props.fields || !key) return null
  const cleanKey = key.startsWith('data.') ? key.substring(5) : key
  return props.fields.find(f => f.key === cleanKey || String(f.key).toLowerCase() === String(cleanKey).toLowerCase())
}

const getSubColumnInfo = (fieldKey: string, colKey: string) => {
  const f = getFieldByKey(fieldKey)
  if (!f || !f.options) return null
  try {
    const parsed = typeof f.options === 'string' ? JSON.parse(f.options) : f.options
    const subCols = Array.isArray(parsed) ? parsed : (parsed.tableSchema?.columns || parsed.tableColumns || parsed.columns || [])
    return subCols.find((c: any) => c.key === colKey || String(c.key).toLowerCase() === String(colKey).toLowerCase())
  } catch (e) {
    return null
  }
}

const getSubColLabel = (fieldKey: string, colKey: string): string => {
  const subCol = getSubColumnInfo(fieldKey, colKey)
  if (subCol && subCol.name) {
    if (typeof subCol.name === 'object') {
      return subCol.name[locale.value] || subCol.name.ko || subCol.name.en || colKey
    }
    return String(subCol.name)
  }
  return colKey
}

const formatSubColValue = (fieldKey: string, colKey: string, rawVal: any): string => {
  if (rawVal === null || rawVal === undefined || rawVal === '') return ''
  const subCol = getSubColumnInfo(fieldKey, colKey)
  if (subCol && (subCol.options || subCol.optionsStr)) {
    let opts: any[] = []
    try {
      const rawOpts = subCol.options || subCol.optionsStr
      opts = typeof rawOpts === 'string' ? JSON.parse(rawOpts) : rawOpts
    } catch(e) {}
    if (Array.isArray(opts)) {
      const opt = opts.find(o => String(o.value) === String(rawVal) || String(o.key) === String(rawVal) || String(o.code) === String(rawVal))
      if (opt) {
        if (typeof opt.label === 'object') return opt.label[locale.value] || opt.label.ko || opt.label.en || rawVal
        if (typeof opt.name === 'object') return opt.name[locale.value] || opt.name.ko || opt.name.en || rawVal
        return opt.label || opt.name || rawVal
      }
    }
  }
  if (typeof rawVal === 'object' && rawVal !== null) {
    return rawVal[locale.value] || rawVal.ko || rawVal.en || JSON.stringify(rawVal)
  }
  return String(rawVal)
}

const extractFileName = (input: any): string => {
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
      if (Array.isArray(parsed) && parsed.length > 0) {
        return parsed.map((item: any) => extractFileName(item)).filter(Boolean).join(', ')
      }
      if (typeof parsed === 'object' && (parsed.name || parsed.originalName)) {
        return parsed.name || parsed.originalName
      }
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

const getDomainRefDisplayName = (fieldKey: string, val: any): string => {
  if (!val) return ''
  const valStr = String(val).trim()
  const refInfo = props.domainReferences?.[fieldKey] || Object.values(props.domainReferences || {}).find((r: any) => r.records?.some((rec: any) => String(rec.id) === valStr || String(rec.code) === valStr))

  if (refInfo && refInfo.records) {
    const recList = Array.isArray(refInfo.records) ? refInfo.records : (refInfo.records?.content || [])
    const record = recList.find((r: any) => String(r.id) === valStr || String(r.code) === valStr)
    if (record) {
      const data = typeof record.data === 'string' ? JSON.parse(record.data) : (record.data || record)
      const idFieldId = refInfo.domainInfo?.identifierFieldId
      const dFieldId = refInfo.domainInfo?.displayNameFieldId || idFieldId
      const idF = refInfo.fields?.find((x: any) => x.id === idFieldId || x.key === idFieldId)
      const nameF = refInfo.fields?.find((x: any) => x.id === dFieldId || x.key === dFieldId)

      const extractVal = (d: any, key: any) => {
        if (!d || !key) return null
        const v = d[key]
        if (v && typeof v === 'object') return v[locale.value] || v.ko || v.en || JSON.stringify(v)
        return v ? String(v) : null
      }

      const idStr = extractVal(data, idF?.key) || record.code || data.code || data.EP_NO || data.EMP_NO
      const nameStr = extractVal(data, nameF?.key) || data.name || data.EP_NAME || data.EMP_NAME || data.title

      if (idStr && nameStr && idStr !== nameStr) return `[${idStr}] ${nameStr}`
      if (nameStr) return nameStr
      if (idStr) return `[${idStr}]`
    }
  }

  // Raw UUID fallback pattern
  if (/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(valStr)) {
    return `REC-${valStr.substring(0, 8)}`
  }

  return valStr
}

const formatExportValue = (fieldKey: string, val: any): any => {
  if (val === undefined || val === null || val === '') return ''

  // 1. Array handling (JSON Subtable or Files)
  if (Array.isArray(val)) {
    if (val.length === 0) return ''
    if (typeof val[0] === 'object' && val[0] !== null) {
      // Subtable array: convert to clean numbered multi-line text with multilingual column labels
      return val.map((row: any, idx: number) => {
        const pairs = Object.entries(row)
          .filter(([k, v]) => !k.startsWith('_idx_') && v !== null && v !== undefined && v !== '')
          .map(([k, v]) => `${getSubColLabel(fieldKey, k)}: ${formatSubColValue(fieldKey, k, v)}`)
        return `${idx + 1}. ${pairs.join(', ')}`
      }).join('\n')
    } else {
      // String/File array
      return val.map((item: any) => extractFileName(item)).join(', ')
    }
  }

  // 2. Multilingual or Object handling
  if (typeof val === 'object' && val !== null) {
    if ('ko' in val || 'en' in val) {
      const ko = val.ko || ''
      const en = val.en || ''
      return ko && en && ko !== en ? `${ko} (${en})` : ko || en || ''
    }
    const pairs = Object.entries(val)
      .filter(([k, v]) => !k.startsWith('_idx_') && v !== null && v !== undefined && v !== '')
      .map(([k, v]) => `${getSubColLabel(fieldKey, k)}: ${formatSubColValue(fieldKey, k, v)}`)
    return pairs.join(', ')
  }

  // 3. String value inspection
  if (typeof val === 'string') {
    const trimmed = val.trim()
    if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
      try {
        const parsed = JSON.parse(trimmed)
        if (Array.isArray(parsed)) return formatExportValue(fieldKey, parsed)
      } catch (e) {}
    }
    if (trimmed.startsWith('{') && trimmed.endsWith('}')) {
      try {
        const parsed = JSON.parse(trimmed)
        if (typeof parsed === 'object') return formatExportValue(fieldKey, parsed)
      } catch (e) {}
    }

    // Domain reference or UUID resolution
    const actualKey = fieldKey.startsWith('data.') ? fieldKey.substring(5) : fieldKey
    const resolvedRef = getDomainRefDisplayName(actualKey, trimmed)
    if (resolvedRef !== trimmed) return resolvedRef

    // File URL resolution
    const resolvedFile = extractFileName(trimmed)
    if (resolvedFile !== trimmed) return resolvedFile
  }

  return val
}

const taskStatus = computed(() => {
  if (!taskInfo.value) return ''
  const st = taskInfo.value.status
  if (typeof st === 'object' && st !== null) {
    return String(st.value || 'PROCESSING')
  }
  return String(st || 'PROCESSING')
})

const taskStatusText = computed(() => {
  if (taskStatus.value === 'COMPLETED') return t('status_completed', '완료')
  if (taskStatus.value === 'FAILED') return t('status_failed', '실패')
  return t('status_processing', '진행 중')
})

watch(() => props.modelValue, (val) => {
  show.value = val
  if (!val) {
    stopPolling()
    taskInfo.value = null
  }
})

watch(show, (val) => {
  emit('update:modelValue', val)
})

/**
 * Extract visible columns from AG-Grid API in display order.
 * This ensures the Excel columns match exactly what the user sees.
 */
const extractGridColumns = (): Array<{ field: string; headerName: string }> => {
  const api = props.gridApi
  if (!api) return []

  const allColumns = api.getAllDisplayedColumns?.() || api.getColumns?.() || []
  const columns: Array<{ field: string; headerName: string }> = []

  for (const col of allColumns) {
    const colDef = col.getColDef?.() || {}
    const colId = col.getColId?.() || colDef.colId || colDef.field || ''
    const field = colDef.field || colId
    const headerName = colDef.headerName || field

    // Skip AG-Grid internal columns (selection, checkbox, etc.)
    if (colId.startsWith('ag-Grid-')) continue
    if (colDef.checkboxSelection && !colDef.field) continue
    if (!field) continue

    columns.push({ field, headerName })
  }

  return columns
}

/**
 * Extract all cached rows from AG-Grid's infinite row model.
 * Uses forEachNode to iterate over all rows currently loaded in the grid.
 */
const extractGridRows = (columns: Array<{ field: string; headerName: string }>): Array<Record<string, any>> => {
  const api = props.gridApi
  if (!api) return []

  const rows: Array<Record<string, any>> = []

  api.forEachNode((node: any) => {
    if (!node.data) return
    const rowObj: Record<string, any> = {}

    for (const col of columns) {
      const field = col.field
      // Use AG-Grid's built-in getValue for proper valueGetter support
      const column = api.getColumn?.(field)
      let value: any = undefined

      if (column) {
        try {
          value = api.getValue(column, node)
        } catch {
          // Fallback to direct data access
        }
      }

      // If getValue didn't work, fallback to direct data access
      if (value === undefined || value === null) {
        if (field.startsWith('data.')) {
          const subKey = field.substring(5)
          value = node.data?.data?.[subKey] ?? node.data?.[subKey] ?? ''
        } else {
          value = node.data?.[field] ?? ''
        }
      }

      rowObj[field] = formatExportValue(field, value)
    }

    rows.push(rowObj)
  })

  return rows
}

const startExport = async () => {
  try {
    const columns = extractGridColumns()
    const rows = extractGridRows(columns)

    const payload = {
      domainId: props.domainId,
      format: 'EXCEL',
      columns,
      records: rows
    }

    const res = await customFetch(`/api/batch/export/async?format=EXCEL`, {
      method: 'POST',
      body: payload
    })
    taskInfo.value = res
    startPolling(res.taskId)
  } catch (e) {
    console.error('Failed to start export:', e)
  }
}

const startPolling = (taskId: string) => {
  stopPolling()
  pollTimer = setInterval(async () => {
    try {
      const res = await customFetch(`/api/batch/tasks/${taskId}`)
      taskInfo.value = res
      const st = typeof res?.status === 'object' ? res?.status?.value : res?.status
      if (st === 'COMPLETED' || st === 'FAILED') {
        stopPolling()
      }
    } catch (e) {
      stopPolling()
    }
  }, 300)
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

const downloading = ref(false)

const downloadFile = async () => {
  if (!taskInfo.value?.downloadUrl) return
  downloading.value = true
  try {
    const rawData: any = await customFetch(taskInfo.value.downloadUrl, {
      responseType: 'blob'
    })
    const blob = rawData instanceof Blob 
      ? rawData 
      : new Blob([rawData], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `export_master_data_${taskInfo.value.taskId ? taskInfo.value.taskId.substring(0, 8) : 'export'}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(url)
  } catch (e) {
    console.error('Failed to download file:', e)
  } finally {
    downloading.value = false
  }
}

onUnmounted(() => {
  stopPolling()
})
</script>
