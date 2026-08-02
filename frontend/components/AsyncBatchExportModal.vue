<template>
  <va-modal
    v-model="show"
    :title="$t('async_export_title') || '대용량 마스터 데이터 비동기 내보내기'"
    hide-default-actions
  >
    <div style="padding: 0.5rem; display: flex; flex-direction: column; gap: 1.25rem;">
      <div v-if="!taskInfo" style="display: flex; flex-direction: column; gap: 1rem;">
        <p style="font-size: 0.9rem; color: var(--va-text-secondary); margin: 0;">
          {{ $t('async_export_desc') || '선택한 도메인의 전체 마스터 데이터를 백그라운드 비동기 작업으로 Excel 파일로 내보냅니다.' }}
        </p>
        <va-button color="primary" icon="download" @click="startExport">
          {{ $t('start_export') || '내보내기 시작' }}
        </va-button>
      </div>

      <div v-else style="display: flex; flex-direction: column; gap: 1rem;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-weight: 600; font-size: 0.95rem;">{{ $t('export_progress') || '진행 상태' }}</span>
          <va-badge :text="taskInfo.status" :color="taskInfo.status === 'COMPLETED' ? 'success' : 'primary'" />
        </div>

        <va-progress-bar :model-value="taskInfo.progressPercent" color="primary" animated />

        <div style="font-size: 0.85rem; color: var(--va-text-secondary); text-align: center;">
          {{ taskInfo.processedCount.toLocaleString() }} / {{ taskInfo.totalCount.toLocaleString() }} {{ $t('records_count_suffix') || 'records' }} ({{ taskInfo.progressPercent }}%)
        </div>

        <div v-if="taskInfo.status === 'COMPLETED'" style="margin-top: 0.5rem; text-align: center;">
          <va-button color="success" icon="file_download" :loading="downloading" @click="downloadFile">
            {{ $t('download_file') || '완료된 파일 다운로드' }}
          </va-button>
        </div>
      </div>

      <div style="display: flex; justify-content: flex-end; margin-top: 1rem;">
        <va-button preset="secondary" @click="show = false">{{ $t('close') || '닫기' }}</va-button>
      </div>
    </div>
  </va-modal>
</template>

<script setup lang="ts">
import { ref, watch, onUnmounted } from 'vue'
import { useCustomFetch } from '~/composables/useCustomFetch'

const props = defineProps<{
  modelValue: boolean
  domainId: string | null
  gridApi?: any
}>()

const emit = defineEmits(['update:modelValue'])

const { customFetch } = useCustomFetch()

const show = ref(props.modelValue)
const taskInfo = ref<any>(null)
let pollTimer: any = null

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

      // Format multilingual objects for Excel display
      if (value && typeof value === 'object' && !Array.isArray(value)) {
        if ('ko' in value || 'en' in value) {
          const ko = value.ko || ''
          const en = value.en || ''
          value = ko && en ? `${ko} (${en})` : ko || en
        } else {
          value = JSON.stringify(value)
        }
      }

      rowObj[field] = value ?? ''
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
      if (res.status === 'COMPLETED' || res.status === 'FAILED') {
        stopPolling()
      }
    } catch (e) {
      stopPolling()
    }
  }, 1000)
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
    const blob: Blob = await customFetch(taskInfo.value.downloadUrl, {
      responseType: 'blob'
    })
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
