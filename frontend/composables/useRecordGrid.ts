import { ref } from 'vue'

export function useRecordGrid() {
  const gridApi = ref<any>(null)
  const selectedRecordRows = ref<any[]>([])
  const selectedRecordId = ref<string | null>(null)
  const columnDefs = ref<any[]>([])

  const onGridReady = (params: any) => {
    gridApi.value = params.api
  }

  const onSelectionChanged = (event: any) => {
    if (!event || !event.api) return
    const rows = event.api.getSelectedRows() || []
    selectedRecordRows.value = rows
    if (rows.length > 0) {
      selectedRecordId.value = rows[0].id || rows[0].recordId || null
    } else {
      selectedRecordId.value = null
    }
  }

  const clearSelection = () => {
    selectedRecordRows.value = []
    selectedRecordId.value = null
    if (gridApi.value) {
      gridApi.value.deselectAll()
    }
  }

  const generateColumnDefs = (fields: any[], locale: string = 'ko') => {
    if (!fields || !Array.isArray(fields)) return []

    const defs = fields.map(field => {
      let headerName = ''
      if (typeof field.name === 'object' && field.name !== null) {
        headerName = field.name[locale] || field.name.ko || field.name.en || field.key || ''
      } else {
        headerName = field.name || field.key || ''
      }

      return {
        field: `data.${field.key}`,
        headerName,
        width: field.gridWidth || 150,
        sortable: true,
        filter: true,
        resizable: true
      }
    })

    columnDefs.value = defs
    return defs
  }

  return {
    gridApi,
    selectedRecordRows,
    selectedRecordId,
    columnDefs,
    onGridReady,
    onSelectionChanged,
    clearSelection,
    generateColumnDefs
  }
}
