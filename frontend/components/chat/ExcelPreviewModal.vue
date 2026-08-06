<template>
  <va-modal
    v-model="show"
    size="large"
    hide-default-actions
    :title="`📊 ${$t('excel_viewer_title')} - ${fileName}`"
  >
    <div style="padding: 0.5rem; display: flex; flex-direction: column; gap: 1rem; max-height: 75vh; min-height: 480px; width: 100%;">
      <!-- Loading State -->
      <div v-if="loading" style="display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 4rem; gap: 1rem;">
        <va-progress-circle indeterminate size="large" />
        <span style="font-size: 0.9rem; color: var(--va-text-secondary);">{{ $t('excel_loading') }}</span>
      </div>

      <template v-else>
        <!-- Toolbar & Sheet Tabs -->
        <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-element); padding: 0.75rem 1rem; border-radius: 8px; flex-wrap: wrap; gap: 0.75rem;">
          <!-- Sheet Tabs -->
          <div style="display: flex; gap: 0.5rem; overflow-x: auto; max-width: 60%;">
            <va-button
              v-for="(sheet, sIdx) in sheetNames"
              :key="sheet"
              :preset="activeSheetIndex === sIdx ? 'primary' : 'secondary'"
              size="small"
              style="font-weight: 600;"
              @click="activeSheetIndex = sIdx"
            >
              📄 {{ sheet }}
            </va-button>
          </div>

          <!-- Active Cell Info & Copy Button -->
          <div style="display: flex; align-items: center; gap: 0.75rem;">
            <div v-if="selectedCell" style="display: flex; align-items: center; gap: 0.5rem; background: var(--va-background-secondary); padding: 0.35rem 0.75rem; border-radius: 6px; border: 1px solid var(--va-background-border); font-size: 0.85rem;">
              <span style="font-weight: 700; color: var(--va-primary);">{{ selectedCell.address }}</span>
              <span style="color: var(--va-text-secondary); max-width: 150px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                "{{ selectedCell.value }}"
              </span>
              <va-button
                size="small"
                color="success"
                icon="content_copy"
                preset="secondary"
                @click="copySelectedCell"
              >
                {{ $t('copy_cell') }}
              </va-button>
            </div>

            <!-- Search input -->
            <va-input
              v-model="searchQuery"
              class="excel-search-input"
              :placeholder="$t('search_in_table')"
              size="small"
              clearable
              style="width: 180px;"
            >
              <template #prependInner>
                <va-icon name="search" size="small" />
              </template>
            </va-input>
          </div>
        </div>

        <!-- Excel Spreadsheet Grid View -->
        <div style="flex: 1; overflow: auto; border: 1px solid var(--va-background-border); border-radius: 8px; position: relative;">
          <table style="width: 100%; border-collapse: collapse; font-size: 0.85rem; font-family: sans-serif; min-width: 600px;">
            <thead>
              <tr style="background: var(--va-background-element); border-bottom: 2px solid var(--va-background-border); position: sticky; top: 0; z-index: 10;">
                <!-- Row Number Column Header -->
                <th style="width: 48px; padding: 0.5rem; text-align: center; border-right: 1px solid var(--va-background-border); color: var(--va-text-secondary); font-weight: 700;">
                  #
                </th>
                <th
                  v-for="(colHeader, cIdx) in currentHeaders"
                  :key="cIdx"
                  style="padding: 0.6rem 0.8rem; border-right: 1px solid var(--va-background-border); text-align: left; font-weight: 700; color: var(--va-text-primary); white-space: nowrap;"
                >
                  {{ colHeader }}
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(row, rIdx) in filteredRows"
                :key="rIdx"
                style="border-bottom: 1px solid var(--va-background-border); transition: background 0.15s;"
              >
                <!-- Row Number Index -->
                <td style="padding: 0.4rem 0.5rem; text-align: center; border-right: 1px solid var(--va-background-border); background: var(--va-background-element); color: var(--va-text-secondary); font-weight: 600; font-size: 0.78rem;">
                  {{ rIdx + 1 }}
                </td>

                <!-- Cell Data Columns -->
                <td
                  v-for="(colHeader, cIdx) in currentHeaders"
                  :key="cIdx"
                  :class="{
                    'cell-selected': selectedCell && selectedCell.rIdx === rIdx && selectedCell.cIdx === cIdx,
                    'cell-highlight': searchQuery && String(row[colHeader] || '').toLowerCase().includes(searchQuery.toLowerCase())
                  }"
                  style="padding: 0.5rem 0.8rem; border-right: 1px solid var(--va-background-border); cursor: pointer; user-select: text; max-width: 260px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;"
                  @click="selectCell(rIdx, cIdx, colHeader, row[colHeader])"
                  @dblclick="copyCellValue(row[colHeader], getCellAddress(rIdx, cIdx))"
                >
                  {{ row[colHeader] ?? '' }}
                </td>
              </tr>

              <tr v-if="filteredRows.length === 0">
                <td :colspan="currentHeaders.length + 1" style="text-align: center; padding: 3rem; color: var(--va-text-secondary);">
                  {{ $t('no_excel_data') }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Footer / Copy Toast Status & Export Toolbar -->
        <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 0.5rem; flex-wrap: wrap; gap: 0.5rem;">
          <div style="font-size: 0.8rem; color: var(--va-text-secondary); display: flex; align-items: center; gap: 0.5rem;">
            <span>💡 {{ $t('excel_copy_tip') }}</span>
            <va-badge v-if="copyToast" color="success" size="small">{{ copyToastMessage }}</va-badge>
          </div>

          <div style="display: flex; gap: 0.5rem; align-items: center;">
            <va-button
              size="small"
              color="primary"
              outline
              icon="content_copy"
              @click="copyTableAsTSV"
            >
              {{ $t('copy_as_excel_text') }}
            </va-button>
            <va-button
              size="small"
              color="info"
              outline
              icon="table_rows"
              @click="copyTableAsMarkdown"
            >
              {{ $t('copy_as_markdown') }}
            </va-button>
          </div>
        </div>
      </template>
    </div>
  </va-modal>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted, onUnmounted } from 'vue'
import ExcelJS from 'exceljs'
import { useCustomFetch } from '~/composables/useCustomFetch'

const props = defineProps<{
  modelValue: boolean
  fileUrl: string | null
  fileName?: string
}>()

const emit = defineEmits(['update:modelValue'])
const { customFetch } = useCustomFetch()

const show = ref(props.modelValue)
const loading = ref(false)

const workbookData = ref<Record<string, any[]>>({})
const sheetNames = ref<string[]>([])
const activeSheetIndex = ref(0)

const searchQuery = ref('')
const selectedCell = ref<{ rIdx: number; cIdx: number; address: string; value: any } | null>(null)

const copyToast = ref(false)
const copyToastMessage = ref('')

watch(() => props.modelValue, (val) => {
  show.value = val
  if (val && props.fileUrl) {
    loadAndParseExcel()
  }
})

watch(show, (val) => {
  emit('update:modelValue', val)
})

const getColumnName = (colIndex: number): string => {
  let name = ''
  let num = colIndex
  while (num >= 0) {
    name = String.fromCharCode((num % 26) + 65) + name
    num = Math.floor(num / 26) - 1
  }
  return name
}

const getCellAddress = (rIdx: number, cIdx: number): string => {
  return `${getColumnName(cIdx)}${rIdx + 1}`
}

const currentSheetData = computed(() => {
  const currentSheetName = sheetNames.value[activeSheetIndex.value]
  if (!currentSheetName) return []
  return workbookData.value[currentSheetName] || []
})

const currentHeaders = computed(() => {
  const data = currentSheetData.value
  if (!data || data.length === 0) return []
  const keysSet = new Set<string>()
  for (const row of data) {
    if (row && typeof row === 'object') {
      Object.keys(row).forEach(k => keysSet.add(k))
    }
  }
  return Array.from(keysSet)
})

const filteredRows = computed(() => {
  const data = currentSheetData.value
  if (!searchQuery.value) return data
  const q = searchQuery.value.toLowerCase()
  return data.filter(row => {
    return Object.values(row).some(v => String(v || '').toLowerCase().includes(q))
  })
})

const selectCell = (rIdx: number, cIdx: number, colHeader: string, val: any) => {
  const address = getCellAddress(rIdx, cIdx)
  selectedCell.value = {
    rIdx,
    cIdx,
    address,
    value: val ?? ''
  }
}

const copyCellValue = async (val: any, address?: string) => {
  const textToCopy = String(val ?? '')
  try {
    await navigator.clipboard.writeText(textToCopy)
    copyToastMessage.value = `📋 ${address ? '[' + address + '] ' : ''}셀 값이 클립보드에 복사되었습니다!`
    copyToast.value = true
    setTimeout(() => {
      copyToast.value = false
    }, 2500)
  } catch (e) {
    console.error('Failed to copy cell:', e)
  }
}

const copySelectedCell = () => {
  if (selectedCell.value) {
    copyCellValue(selectedCell.value.value, selectedCell.value.address)
  }
}

const copyTableAsTSV = async () => {
  const headers = currentHeaders.value
  const rows = filteredRows.value
  if (headers.length === 0) return

  let tsv = headers.join('\t') + '\n'
  rows.forEach(row => {
    const line = headers.map(h => String(row[h] ?? '')).join('\t')
    tsv += line + '\n'
  })

  try {
    await navigator.clipboard.writeText(tsv)
    copyToastMessage.value = `📋 시트 데이터 ${rows.length}행이 엑셀 표(TSV) 텍스트로 클립보드에 복사되었습니다! (엑셀에 Ctrl+V 시 표 구조 100% 복원)`
    copyToast.value = true
    setTimeout(() => { copyToast.value = false }, 3500)
  } catch (e) {
    console.error('Failed to copy table:', e)
  }
}

const copyTableAsMarkdown = async () => {
  const headers = currentHeaders.value
  const rows = filteredRows.value
  if (headers.length === 0) return

  let md = '| ' + headers.join(' | ') + ' |\n'
  md += '| ' + headers.map(() => '---').join(' | ') + ' |\n'
  rows.forEach(row => {
    const line = '| ' + headers.map(h => String(row[h] ?? '')).join(' | ') + ' |\n'
    md += line
  })

  try {
    await navigator.clipboard.writeText(md)
    copyToastMessage.value = `📝 시트 데이터 ${rows.length}행이 마크다운 표로 클립보드에 복사되었습니다!`
    copyToast.value = true
    setTimeout(() => { copyToast.value = false }, 3500)
  } catch (e) {
    console.error('Failed to copy markdown:', e)
  }
}

const handleKeyDown = (e: KeyboardEvent) => {
  if (!show.value || !selectedCell.value) return
  if ((e.ctrlKey || e.metaKey) && e.key === 'c') {
    copySelectedCell()
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
})

const loadAndParseExcel = async () => {
  if (!props.fileUrl) return
  loading.value = true
  selectedCell.value = null
  try {
    const blob: Blob = await customFetch(props.fileUrl, {
      responseType: 'blob'
    })

    const arrayBuffer = await blob.arrayBuffer()
    const wb = new ExcelJS.Workbook()
    await wb.xlsx.load(arrayBuffer)

    const names: string[] = []
    const wbMap: Record<string, any[]> = {}

    wb.worksheets.forEach(sheet => {
      const sName = sheet.name
      names.push(sName)

      const json: any[] = []
      const headers: string[] = []
      const firstRow = sheet.getRow(1)
      firstRow.eachCell({ includeEmpty: false }, (cell, colNumber) => {
        headers[colNumber] = cell.value ? String(cell.value) : `Column ${colNumber}`
      })

      for (let i = 2; i <= sheet.rowCount; i++) {
        const row = sheet.getRow(i)
        if (!row.hasValues) continue
        const rowObj: Record<string, any> = {}
        headers.forEach((headerName, colIdx) => {
          if (!headerName) return
          const cellVal = row.getCell(colIdx).value
          rowObj[headerName] = cellVal !== null && cellVal !== undefined ? cellVal : ''
        })
        json.push(rowObj)
      }
      wbMap[sName] = json
    })

    sheetNames.value = names
    workbookData.value = wbMap
    activeSheetIndex.value = 0
  } catch (e) {
    console.error('Failed to parse excel file:', e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.cell-selected {
  background: rgba(44, 130, 224, 0.18) !important;
  outline: 2px solid var(--va-primary) !important;
  outline-offset: -2px;
  font-weight: 600;
}

.cell-highlight {
  background: rgba(230, 162, 60, 0.25) !important;
}

.excel-search-input :deep(input),
.excel-search-input :deep(.va-input-wrapper__field),
.excel-search-input :deep(.va-input-wrapper__text) {
  color: #111111 !important;
}

.excel-search-input :deep(input::placeholder) {
  color: #666666 !important;
  opacity: 1 !important;
}
</style>
