<template>
  <va-modal
    v-model="show"
    size="large"
    hide-default-actions
    :title="`📗 ${$t('excel_spreadsheet_viewer_title') || 'MS Excel 스프레드시트 데이터 뷰어'}`"
  >
    <div
      style="padding: 0.5rem; display: flex; flex-direction: column; gap: 0.75rem; max-height: 78vh; min-height: 480px; width: 100%; user-select: none;"
      @mouseup="onMouseUp"
    >
      <!-- Excel Green Header Toolbar -->
      <div style="display: flex; justify-content: space-between; align-items: center; background: linear-gradient(135deg, #107c41, #1f9a55); padding: 0.6rem 1rem; border-radius: 8px; color: white; flex-wrap: wrap; gap: 0.75rem; box-shadow: 0 4px 12px rgba(16, 124, 65, 0.25);">
        <!-- Active Cell Address Box -->
        <div style="display: flex; align-items: center; gap: 0.85rem; flex-wrap: wrap;">
          <div style="font-weight: 700; font-size: 0.95rem; display: flex; align-items: center; gap: 6px;">
            <span>📗 Excel Sheet</span>
            <va-badge color="#0b582e" text-color="#ffffff" size="small">{{ rows.length }} Rows</va-badge>
          </div>

          <!-- Active Cell Address Bar -->
          <div v-if="selectionSummary" style="display: flex; align-items: center; gap: 6px; background: rgba(255,255,255,0.2); padding: 3px 10px; border-radius: 4px; font-size: 0.85rem; border: 1px solid rgba(255,255,255,0.3);">
            <span style="font-weight: 800; color: #ffeb3b;">{{ selectionSummary.addressRange }}</span>
            <span style="max-width: 200px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; opacity: 0.95;">
              {{ selectionSummary.label }}
            </span>
            <va-button
              size="small"
              color="#ffffff"
              preset="plain"
              icon="content_copy"
              title="선택 셀/영역 데이터 복사"
              style="padding: 0 4px; color: white;"
              @click="copySelectedCells"
            />
          </div>
        </div>

        <!-- Right Action Buttons -->
        <div style="display: flex; gap: 0.5rem; align-items: center;">
          <va-button
            size="small"
            color="#ffffff"
            style="color: #107c41; font-weight: 700; background: white;"
            icon="content_copy"
            @click="copyTableData"
          >
            {{ $t('copy_table_excel') || '📋 엑셀 표 데이터 전체 복사' }}
          </va-button>

          <va-input
            v-model="searchQuery"
            class="excel-search-input"
            :placeholder="$t('search_in_table') || '시트 내 데이터 검색...'"
            size="small"
            clearable
            style="width: 190px; background: white; border-radius: 4px;"
          >
            <template #prependInner>
              <va-icon name="search" size="small" color="#107c41" />
            </template>
          </va-input>
        </div>
      </div>

      <!-- Excel Spreadsheet Table Grid -->
      <div
        class="excel-table-container"
        style="flex: 1; overflow: auto; border: 1px solid #d4d4d4; border-radius: 6px; background: #ffffff; user-select: none;"
      >
        <table style="width: 100%; border-collapse: collapse; font-size: 0.83rem; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; text-align: left; min-width: 600px; color: #222222; user-select: none;">
          <thead>
            <!-- Excel Alphabet Column Header Row A, B, C... -->
            <tr style="background: #5c6bc0; border-bottom: 2px solid #3f51b5; position: sticky; top: 0; z-index: 10; color: #ffffff;">
              <!-- Row Index Header -->
              <th style="width: 46px; padding: 0.4rem; text-align: center; border-right: 1px solid #3f51b5; background: #3f51b5; color: #ffffff; font-weight: 700; font-size: 0.75rem;">
                #
              </th>
              <th
                v-for="(h, hIdx) in headers"
                :key="hIdx"
                style="padding: 0.55rem 0.8rem; border-right: 1px solid #3f51b5; font-weight: 700; color: #ffffff; white-space: nowrap; background: #5c6bc0;"
              >
                {{ formatHeader(h) }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(row, rIdx) in filteredRows"
              :key="rIdx"
              style="border-bottom: 1px solid #9e9e9e; transition: background 0.15s;"
            >
              <!-- Row Number Index 1, 2, 3... -->
              <td style="padding: 0.45rem; text-align: center; border-right: 1px solid #9e9e9e; background: #e0e0e0; color: #424242; font-weight: 700; font-size: 0.75rem;">
                {{ rIdx + 1 }}
              </td>

              <!-- Data Cells -->
              <td
                v-for="(val, cIdx) in row"
                :key="cIdx"
                :class="{
                  'excel-cell-selected': isCellSelected(rIdx, cIdx),
                  'cell-highlight': searchQuery && String(val || '').toLowerCase().includes(searchQuery.toLowerCase())
                }"
                style="padding: 0.5rem 0.8rem; border-right: 1px solid #9e9e9e; background: #f5f5f5; color: #111111; max-width: 320px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; cursor: pointer; user-select: none;"
                @mousedown="onCellMouseDown(rIdx, cIdx, $event)"
                @mouseenter="onCellMouseEnter(rIdx, cIdx)"
                @dblclick="copyCellValue(val, getCellAddress(rIdx, cIdx))"
              >
                {{ formatValue(val) }}
              </td>
            </tr>

            <tr v-if="filteredRows.length === 0">
              <td :colspan="headers.length + 1" style="text-align: center; padding: 3rem; color: #888888;">
                {{ $t('no_table_data') || '표시할 데이터가 없습니다.' }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Footer / Toast Status & Tips -->
      <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 0.25rem;">
        <div style="font-size: 0.8rem; color: var(--va-text-secondary); display: flex; align-items: center; gap: 0.5rem;">
          <span>📗 {{ $t('excel_modal_tip') || '마우스 드래그, Shift, Ctrl 키를 이용해 여러 셀을 멀티 선택할 수 있으며 Ctrl+C 로 엑셀에 바로 붙여넣기 할 수 있습니다.' }}</span>
          <va-badge v-if="copyToast" color="success" size="small">{{ copyToastMessage }}</va-badge>
        </div>
      </div>
    </div>
  </va-modal>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'

interface CellPos {
  rIdx: number
  cIdx: number
}

const props = defineProps<{
  modelValue: boolean
  headers: string[]
  rows: string[][]
  rawContent?: string
}>()

const emit = defineEmits(['update:modelValue'])
const { t, te, locale } = useI18n()

const show = ref(props.modelValue)
const searchQuery = ref('')
const copyToast = ref(false)
const copyToastMessage = ref('')

const formatHeader = (h: string): string => {
  if (!h) return ''
  const trimmed = h.trim()
  if (trimmed === '변경일' || trimmed === '수정일' || trimmed === 'updatedAt' || trimmed === 'updated_at') {
    return te('updated_at') ? t('updated_at') : (locale.value === 'en' ? 'Updated At' : '변경일')
  }
  if (te(trimmed)) return t(trimmed)
  return trimmed
}

// Multi-cell selection state
const isDragging = ref(false)
const anchorCell = ref<CellPos | null>(null)
const selectedCellKeys = ref<Set<string>>(new Set())

watch(() => props.modelValue, (val) => {
  show.value = val
  if (val) {
    clearSelection()
  }
})

watch(show, (val) => {
  emit('update:modelValue', val)
})

const clearSelection = () => {
  anchorCell.value = null
  selectedCellKeys.value.clear()
}

// Convert Column Index into Excel Alphabet Name (0 -> A, 1 -> B, ...)
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

const makeCellKey = (rIdx: number, cIdx: number): string => `${rIdx},${cIdx}`

const isCellSelected = (rIdx: number, cIdx: number): boolean => {
  return selectedCellKeys.value.has(makeCellKey(rIdx, cIdx))
}

const selectRange = (start: CellPos, end: CellPos, keepExisting = false) => {
  if (!keepExisting) {
    selectedCellKeys.value.clear()
  }
  const minR = Math.min(start.rIdx, end.rIdx)
  const maxR = Math.max(start.rIdx, end.rIdx)
  const minC = Math.min(start.cIdx, end.cIdx)
  const maxC = Math.max(start.cIdx, end.cIdx)

  for (let r = minR; r <= maxR; r++) {
    for (let c = minC; c <= maxC; c++) {
      selectedCellKeys.value.add(makeCellKey(r, c))
    }
  }
}

const onCellMouseDown = (rIdx: number, cIdx: number, e: MouseEvent) => {
  isDragging.value = true

  if (e.ctrlKey || e.metaKey) {
    // Ctrl+Click: Toggle individual cell
    const key = makeCellKey(rIdx, cIdx)
    if (selectedCellKeys.value.has(key)) {
      selectedCellKeys.value.delete(key)
    } else {
      selectedCellKeys.value.add(key)
      anchorCell.value = { rIdx, cIdx }
    }
  } else if (e.shiftKey && anchorCell.value) {
    // Shift+Click: Select rectangle range from anchor to current
    selectRange(anchorCell.value, { rIdx, cIdx }, false)
  } else {
    // Single Click / Start Drag
    anchorCell.value = { rIdx, cIdx }
    selectRange({ rIdx, cIdx }, { rIdx, cIdx }, false)
  }
}

const onCellMouseEnter = (rIdx: number, cIdx: number) => {
  if (isDragging.value && anchorCell.value) {
    selectRange(anchorCell.value, { rIdx, cIdx }, false)
  }
}

const onMouseUp = () => {
  isDragging.value = false
}

// Format Value according to AGENTS.md rule
const formatValue = (val: string): string => {
  if (!val) return ''
  const uuidRegex = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/
  if (uuidRegex.test(String(val).trim())) {
    return 'REC-' + String(val).trim().substring(0, 8)
  }
  return String(val)
}

const filteredRows = computed(() => {
  if (!searchQuery.value) return props.rows
  const q = searchQuery.value.toLowerCase()
  return props.rows.filter(row => {
    return row.some(val => String(val || '').toLowerCase().includes(q))
  })
})

const getSelectedCellsList = () => {
  const result: Array<{ rIdx: number; cIdx: number; address: string; value: string }> = []
  const currentRows = filteredRows.value

  selectedCellKeys.value.forEach(key => {
    const [rStr, cStr] = key.split(',')
    const rIdx = parseInt(rStr, 10)
    const cIdx = parseInt(cStr, 10)
    if (rIdx >= 0 && rIdx < currentRows.length && cIdx >= 0 && cIdx < (currentRows[rIdx]?.length || 0)) {
      result.push({
        rIdx,
        cIdx,
        address: getCellAddress(rIdx, cIdx),
        value: currentRows[rIdx][cIdx] || ''
      })
    }
  })

  // Sort by row then col
  result.sort((a, b) => (a.rIdx - b.rIdx) || (a.cIdx - b.cIdx))
  return result
}

const selectionSummary = computed(() => {
  const list = getSelectedCellsList()
  if (list.length === 0) return null

  if (list.length === 1) {
    const single = list[0]
    return {
      addressRange: single.address,
      label: `"${formatValue(single.value)}"`
    }
  }

  const minR = Math.min(...list.map(item => item.rIdx))
  const maxR = Math.max(...list.map(item => item.rIdx))
  const minC = Math.min(...list.map(item => item.cIdx))
  const maxC = Math.max(...list.map(item => item.cIdx))

  const firstAddr = getCellAddress(minR, minC)
  const lastAddr = getCellAddress(maxR, maxC)
  const rangeStr = firstAddr === lastAddr ? firstAddr : `${firstAddr}:${lastAddr}`

  return {
    addressRange: rangeStr,
    label: `(${t('excel_cells_selected', { count: list.length }) || list.length + ' cells selected'})`
  }
})

const clearTextSelection = () => {
  if (process.client && window.getSelection) {
    const sel = window.getSelection()
    if (sel) {
      sel.removeAllRanges()
    }
  }
}

const copyCellValue = async (val: string, address?: string) => {
  try {
    const textToCopy = formatValue(val)
    await navigator.clipboard.writeText(textToCopy)
    copyToastMessage.value = t('excel_cell_copied', { address: address || '', value: textToCopy })
    copyToast.value = true
    clearTextSelection()
    setTimeout(() => { copyToast.value = false }, 2500)
  } catch (e) {
    console.error('Failed to copy cell:', e)
  }
}

const copySelectedCells = async () => {
  const list = getSelectedCellsList()
  if (list.length === 0) return

  if (list.length === 1) {
    await copyCellValue(list[0].value, list[0].address)
    return
  }

  const minR = Math.min(...list.map(item => item.rIdx))
  const maxR = Math.max(...list.map(item => item.rIdx))
  const minC = Math.min(...list.map(item => item.cIdx))
  const maxC = Math.max(...list.map(item => item.cIdx))

  const matrix: string[][] = []
  for (let r = minR; r <= maxR; r++) {
    const rowVals: string[] = []
    for (let c = minC; c <= maxC; c++) {
      const key = makeCellKey(r, c)
      if (selectedCellKeys.value.has(key) && filteredRows.value[r] && filteredRows.value[r][c] !== undefined) {
        rowVals.push(formatValue(filteredRows.value[r][c]))
      } else {
        rowVals.push('')
      }
    }
    matrix.push(rowVals)
  }

  const tsv = matrix.map(row => row.join('\t')).join('\n')

  try {
    await navigator.clipboard.writeText(tsv)
    copyToastMessage.value = t('excel_range_copied', { count: list.length })
    copyToast.value = true
    clearTextSelection()
    setTimeout(() => { copyToast.value = false }, 3000)
  } catch (e) {
    console.error('Failed to copy selected cells:', e)
  }
}

const copyTableData = async () => {
  if (props.headers.length === 0) return
  let tsv = props.headers.map(h => formatHeader(h)).join('\t') + '\n'
  props.rows.forEach(row => {
    tsv += row.map(v => formatValue(v)).join('\t') + '\n'
  })

  try {
    await navigator.clipboard.writeText(tsv)
    copyToastMessage.value = t('excel_table_copied', { rows: props.rows.length })
    copyToast.value = true
    clearTextSelection()
    setTimeout(() => { copyToast.value = false }, 3500)
  } catch (e) {
    console.error('Failed to copy table data:', e)
  }
}

const handleKeyDown = (e: KeyboardEvent) => {
  if (!show.value) return
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'c') {
    if (selectedCellKeys.value.size > 0) {
      copySelectedCells()
    } else {
      setTimeout(() => { clearTextSelection() }, 50)
    }
  }
}

onMounted(() => {
  if (process.client) {
    window.addEventListener('keydown', handleKeyDown)
    window.addEventListener('mouseup', onMouseUp)
  }
})

onUnmounted(() => {
  if (process.client) {
    window.removeEventListener('keydown', handleKeyDown)
    window.removeEventListener('mouseup', onMouseUp)
  }
})
</script>

<style scoped>
.excel-table-container,
.excel-table-container table,
.excel-table-container td,
.excel-table-container th {
  user-select: none !important;
  -webkit-user-select: none !important;
}

.excel-cell-selected {
  background: #e8f5e9 !important;
  outline: 2px solid #107c41 !important;
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
