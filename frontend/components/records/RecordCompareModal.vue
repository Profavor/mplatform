<template>
  <AppModal
    v-model="modalVisible"
    :title="t('compare_records_title')"
    icon="compare_arrows"
    size="large"
    hide-default-actions
    class="record-compare-modal"
  >
    <div style="display: flex; flex-direction: column; gap: 1rem; padding: 0.5rem 0;">

      <!-- Controls Header Toolbar -->
      <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.5rem 0.75rem; background: var(--va-background-element); border-radius: 8px; border: 1px solid var(--va-background-border); flex-wrap: wrap; gap: 0.5rem;">
        <div style="display: flex; align-items: center; gap: 0.75rem;">
          <div style="display: flex; align-items: center; gap: 0.4rem;">
            <va-icon name="scale" color="primary" />
            <span style="font-weight: 700; font-size: 0.9rem;">
              {{ t('comparing_count', { count: records.length }) }}
            </span>
          </div>

          <!-- Baseline Record Selector -->
          <div style="display: flex; align-items: center; gap: 0.35rem; margin-left: 0.5rem;">
            <span style="font-size: 0.78rem; font-weight: 700; color: var(--va-text-secondary); white-space: nowrap;">
              {{ t('baseline_record') }}
            </span>
            <va-select
              v-model="baselineRecordIndex"
              :options="baselineRecordOptions"
              value-by="value"
              text-by="text"
              dense
              style="width: 320px; min-width: 220px;"
            />
          </div>
        </div>

        <div style="display: flex; align-items: center; gap: 1rem;">
          <va-switch
            v-model="onlyDifferences"
            size="small"
            color="warning"
            :label="t('only_differences')"
          />
          <va-badge
            :text="`${diffFieldsCount} ${t('diff_count_suffix')}`"
            color="warning"
            style="font-size: 0.8rem; font-weight: 700;"
          />
        </div>
      </div>

      <!-- Compare Matrix Table Wrapper -->
      <div style="overflow: auto; flex: 1; border: 1px solid var(--va-background-border); border-radius: 8px;">
        <table class="compare-matrix-table" style="width: 100%; border-collapse: collapse; font-size: 0.875rem;">
          
          <!-- Sticky Header: Record Header Cards -->
          <thead>
            <tr style="background: var(--va-background-element); position: sticky; top: 0; z-index: 2; border-bottom: 2px solid var(--va-background-border);">
              <th style="width: 180px; min-width: 160px; padding: 0.75rem; text-align: left; background: var(--va-background-element); border-right: 1px solid var(--va-background-border);">
                <div style="font-weight: 700; color: var(--va-text-secondary); text-transform: uppercase; font-size: 0.75rem;">
                  {{ t('field_name') }}
                </div>
              </th>
              
              <th
                v-for="(rec, index) in records"
                :key="getRecordId(rec, index)"
                :style="{
                  padding: '0.75rem',
                  textAlign: 'left',
                  minWidth: '220px',
                  borderRight: '1px solid var(--va-background-border)',
                  background: index === baselineRecordIndex ? 'rgba(var(--va-primary-rgb, 59, 130, 246), 0.08)' : 'var(--va-background-element)'
                }"
              >
                <div style="display: flex; flex-direction: column; gap: 0.35rem;">
                  <div style="display: flex; align-items: center; justify-content: space-between; gap: 0.5rem;">
                    <span style="font-weight: 800; font-size: 0.95rem; color: var(--va-primary);">
                      {{ getRecordName(rec) }}
                    </span>
                    <va-chip size="small" color="primary" square outline style="font-weight: 700; font-family: monospace;">
                      {{ getRecordId(rec, index) }}
                    </va-chip>
                  </div>
                  
                  <div style="display: flex; align-items: center; justify-content: space-between; gap: 0.5rem; margin-top: 0.15rem;">
                    <span style="font-size: 0.75rem; color: var(--va-text-secondary);">
                      Record #{{ index + 1 }}
                    </span>
                    <va-chip
                      v-if="index === baselineRecordIndex"
                      size="small"
                      color="primary"
                      style="font-size: 11px; font-weight: 700;"
                    >
                      ⭐ {{ t('baseline_badge') }}
                    </va-chip>
                    <va-button
                      v-else
                      size="small"
                      preset="plain"
                      color="primary"
                      style="font-size: 11px; padding: 0;"
                      @click="baselineRecordIndex = index"
                    >
                      {{ t('set_as_baseline') }}
                    </va-button>
                  </div>
                </div>
              </th>
            </tr>
          </thead>

          <!-- Table Body: Matrix Rows -->
          <tbody>
            <tr
              v-for="field in displayedFields"
              :key="field.id || field.key"
              :class="{ 'diff-row': isFieldDifferent(field) }"
              style="border-bottom: 1px solid var(--va-background-border); transition: background-color 0.15s ease;"
            >
              <!-- Field Title Cell -->
              <td style="padding: 0.65rem 0.75rem; font-weight: 600; background: var(--va-background-element); border-right: 1px solid var(--va-background-border);">
                <div style="display: flex; align-items: center; justify-content: space-between; gap: 0.5rem;">
                  <span>{{ getTranslatedName(field.name) }}</span>
                  <va-chip
                    v-if="isFieldDifferent(field)"
                    size="small"
                    color="warning"
                    square
                    style="font-size: 10px; font-weight: 800; height: 18px; padding: 0 4px;"
                  >
                    DIFF
                  </va-chip>
                </div>
              </td>

              <!-- Value Cells for Each Record -->
              <td
                v-for="(rec, index) in records"
                :key="getRecordId(rec, index)"
                :class="{
                  'baseline-cell': index === baselineRecordIndex,
                  'diff-cell': isCellDifferentFromBaseline(field, index)
                }"
                :style="{
                  padding: '0.65rem 0.75rem',
                  borderRight: '1px solid var(--va-background-border)',
                  verticalAlign: 'top',
                  background: index === baselineRecordIndex ? 'rgba(var(--va-primary-rgb, 59, 130, 246), 0.03)' : undefined
                }"
              >
                <div style="word-break: break-all; line-height: 1.4;">
                  {{ formatFieldValue(rec, field) }}
                </div>
              </td>
            </tr>

            <tr v-if="displayedFields.length === 0">
              <td :colspan="records.length + 1" style="text-align: center; padding: 2rem; color: var(--va-text-secondary);">
                {{ t('no_differences_found') }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Footer Actions -->
    <template #footer>
      <div style="display: flex; justify-content: space-between; width: 100%; align-items: center;">
        <va-button
          color="success"
          icon="file_download"
          preset="secondary"
          @click="exportToExcel"
        >
          {{ t('export_excel') }}
        </va-button>
        <va-button color="secondary" @click="handleClose">
          {{ t('close') }}
        </va-button>
      </div>
    </template>
  </AppModal>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import AppModal from '~/components/common/AppModal.vue'

let t = (key) => key
let i18nLocale = ref('ko')
try {
  const i18n = useI18n()
  if (i18n) {
    t = i18n.t
    i18nLocale = i18n.locale
  }
} catch (e) {}

const props = defineProps({
  show: { type: Boolean, default: false },
  records: { type: Array, default: () => [] },
  fields: { type: Array, default: () => [] }
})

const emit = defineEmits(['close', 'update:show'])

const isEn = computed(() => (i18nLocale?.value || 'ko') === 'en')
const onlyDifferences = ref(false)
const baselineRecordIndex = ref(0)

watch(() => props.records, (newRecs) => {
  if (!newRecs || baselineRecordIndex.value >= newRecs.length) {
    baselineRecordIndex.value = 0
  }
}, { immediate: true })

const baselineRecordOptions = computed(() => {
  if (!props.records) return []
  return props.records.map((rec, idx) => ({
    value: idx,
    text: `Record #${idx + 1}: ${getRecordName(rec)}`
  }))
})

const modalVisible = computed({
  get: () => props.show,
  set: (val) => {
    emit('update:show', val)
    if (!val) emit('close')
  }
})

const handleClose = () => {
  emit('close')
}

const getTranslatedName = (nameObj) => {
  if (!nameObj) return ''
  if (typeof nameObj === 'object') {
    return nameObj[i18nLocale?.value || 'ko'] || nameObj.ko || nameObj.en || ''
  }
  return String(nameObj)
}

const getRawFieldValue = (record, field) => {
  if (!record || !field) return ''
  const k = field.key
  let val = record[k]
  if (val === undefined && record.data) {
    val = record.data[k]
  }
  if (val === undefined && record.data && typeof record.data === 'object') {
    const lowerK = String(k).toLowerCase()
    const foundKey = Object.keys(record.data).find(key => key.toLowerCase() === lowerK)
    if (foundKey) val = record.data[foundKey]
  }
  return val
}

const NUMERIC_TYPES = ['NUMBER', 'DECIMAL', 'FLOAT', 'INTEGER']

const isNumericField = (field) => {
  if (!field) return false
  if (field.type && NUMERIC_TYPES.includes(String(field.type).toUpperCase())) return true
  return false
}

const formatFieldValue = (record, field, { raw = false } = {}) => {
  const rawVal = getRawFieldValue(record, field)
  if (rawVal === undefined || rawVal === null || rawVal === '') return '-'

  if (typeof rawVal === 'object') {
    if (rawVal.ko || rawVal.en) {
      return rawVal[i18nLocale?.value || 'ko'] || rawVal.ko || rawVal.en || ''
    }
    return JSON.stringify(rawVal)
  }

  // Numeric formatting (UI only — raw=false)
  if (!raw && isNumericField(field)) {
    const num = Number(rawVal)
    if (!isNaN(num)) {
      // Preserve decimals if present
      const hasDecimal = String(rawVal).includes('.')
      return hasDecimal
        ? num.toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 10 })
        : num.toLocaleString()
    }
  }

  return String(rawVal)
}

const isUuid = (val) => {
  if (!val || typeof val !== 'string') return false
  return /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(val)
}

const getRecordId = (rec, idx) => {
  if (!rec) return `REC-${idx + 1}`

  if (props.fields && props.fields.length) {
    const idField = props.fields.find(f => f.isIdentifier || f.isIdAttribute || f.isPrimary || f.isUnique)
    if (idField) {
      const val = getRawFieldValue(rec, idField)
      if (val !== undefined && val !== null && val !== '' && !isUuid(String(val))) {
        return String(val)
      }
    }

    for (const f of props.fields) {
      const fName = getTranslatedName(f.name).toLowerCase()
      const k = String(f.key || '').toLowerCase()
      if (
        fName.includes('사번') || fName.includes('코드') || fName.includes('번호') || fName.includes('id') ||
        k.includes('empno') || k.includes('sabun') || k.includes('code') || k.includes('number') || k.includes('id')
      ) {
        const val = getRawFieldValue(rec, f)
        if (val !== undefined && val !== null && val !== '' && !isUuid(String(val))) {
          return String(val)
        }
      }
    }
  }

  const dataObj = rec.data && typeof rec.data === 'object' ? rec.data : rec
  const candidateKeys = ['empNo', 'employeeNo', 'sabun', 'code', 'idAttribute', 'id_attribute', 'idNo', 'number']
  for (const k of candidateKeys) {
    const val = dataObj[k] || rec[k]
    if (val !== undefined && val !== null && val !== '' && !isUuid(String(val))) {
      return String(val)
    }
  }

  if (dataObj && typeof dataObj === 'object') {
    for (const [k, val] of Object.entries(dataObj)) {
      const lowerK = k.toLowerCase()
      if (
        (lowerK.includes('code') || lowerK.includes('id') || lowerK.includes('no') || lowerK.includes('num')) &&
        val !== undefined && val !== null && val !== '' && !isUuid(String(val))
      ) {
        return String(val)
      }
    }
  }

  return `REC-${idx + 1}`
}

const getRecordName = (rec) => {
  if (!rec) return 'Record'
  let nameStr = ''
  if (rec.name) {
    nameStr = typeof rec.name === 'object' ? (rec.name[i18nLocale?.value || 'ko'] || rec.name.ko || rec.name.en) : String(rec.name)
  } else if (rec.data && rec.data.name) {
    nameStr = typeof rec.data.name === 'object' ? (rec.data.name[i18nLocale?.value || 'ko'] || rec.data.name.ko || rec.data.name.en) : String(rec.data.name)
  }
  return nameStr || getRecordId(rec, 0)
}

const isFieldDifferent = (field) => {
  if (!props.records || props.records.length < 2) return false
  const baselineRec = props.records[baselineRecordIndex.value] || props.records[0]
  const baseVal = String(formatFieldValue(baselineRec, field, { raw: true })).trim()
  for (let i = 0; i < props.records.length; i++) {
    if (i === baselineRecordIndex.value) continue
    const currVal = String(formatFieldValue(props.records[i], field, { raw: true })).trim()
    if (baseVal !== currVal) return true
  }
  return false
}

const isCellDifferentFromBaseline = (field, recordIndex) => {
  if (recordIndex === baselineRecordIndex.value) return false
  if (!props.records || props.records.length < 2) return false
  const baselineRec = props.records[baselineRecordIndex.value] || props.records[0]
  const baseVal = String(formatFieldValue(baselineRec, field, { raw: true })).trim()
  const currVal = String(formatFieldValue(props.records[recordIndex], field, { raw: true })).trim()
  return baseVal !== currVal
}

const diffFieldsCount = computed(() => {
  if (!props.fields) return 0
  return props.fields.filter(f => isFieldDifferent(f)).length
})

const displayedFields = computed(() => {
  if (!props.fields) return []
  if (onlyDifferences.value) {
    return props.fields.filter(f => isFieldDifferent(f))
  }
  return props.fields
})

const buildExcelRows = () => {
  const locale = i18nLocale?.value || 'ko'
  const isKo = locale !== 'en'

  // Header row
  const header = [isKo ? '필드' : 'Field']
  props.records.forEach((rec, idx) => {
    const baseline = idx === baselineRecordIndex.value ? (isKo ? ' [기준]' : ' [Baseline]') : ''
    header.push(`Record #${idx + 1}: ${getRecordId(rec, idx)}${baseline}`)
  })

  // Data rows - all fields (not just displayed), use raw values for correct comparison
  const dataRows = (props.fields || []).map(field => {
    const fieldName = getTranslatedName(field.name) || field.key
    const hasDiff = isFieldDifferent(field)
    const row = [hasDiff ? `★ ${fieldName}` : fieldName]
    props.records.forEach(rec => {
      row.push(formatFieldValue(rec, field, { raw: true }))
    })
    return row
  })

  return [header, ...dataRows]
}

const exportToExcel = async () => {
  const locale = i18nLocale?.value || 'ko'
  const isKo = locale !== 'en'

  // Dynamic import to avoid SSR issues
  const ExcelJS = (await import('exceljs')).default

  const wb = new ExcelJS.Workbook()
  wb.creator = 'Record Compare'
  wb.created = new Date()

  const sheetName = isKo ? '레코드 비교' : 'Record Compare'
  const ws = wb.addWorksheet(sheetName)

  // ── Column definitions ──────────────────────────────────────
  const colCount = (props.fields?.length ? props.records.length + 1 : 1)
  ws.columns = Array.from({ length: colCount }, (_, i) => ({
    width: i === 0 ? 26 : 22
  }))

  // ── Header row ──────────────────────────────────────────────
  const headerValues = [isKo ? '필드' : 'Field']
  props.records.forEach((rec, idx) => {
    const baseline = idx === baselineRecordIndex.value
      ? (isKo ? ' [기준]' : ' [Baseline]')
      : ''
    headerValues.push(`Record #${idx + 1}: ${getRecordId(rec, idx)}${baseline}`)
  })
  const headerRow = ws.addRow(headerValues)
  headerRow.height = 22
  headerRow.eachCell(cell => {
    cell.font = { bold: true, color: { argb: 'FFFFFFFF' }, size: 11 }
    cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FF1E3A5F' } }
    cell.alignment = { vertical: 'middle', horizontal: 'center', wrapText: false }
    cell.border = {
      top: { style: 'thin', color: { argb: 'FF9CB4CC' } },
      left: { style: 'thin', color: { argb: 'FF9CB4CC' } },
      bottom: { style: 'thin', color: { argb: 'FF9CB4CC' } },
      right: { style: 'thin', color: { argb: 'FF9CB4CC' } }
    }
  })

  // ── Data rows ───────────────────────────────────────────────
  ;(props.fields || []).forEach(field => {
    const hasDiff = isFieldDifferent(field)
    const fieldName = getTranslatedName(field.name) || field.key
    const isNum = isNumericField(field)
    const rowValues = [hasDiff ? `★ ${fieldName}` : fieldName]
    props.records.forEach(rec => {
      const rawStr = formatFieldValue(rec, field, { raw: true })
      if (isNum && rawStr !== '-') {
        const n = Number(rawStr)
        rowValues.push(isNaN(n) ? rawStr : n)
      } else {
        rowValues.push(rawStr)
      }
    })

    const dataRow = ws.addRow(rowValues)
    dataRow.height = 18

    dataRow.eachCell({ includeEmpty: true }, (cell, colNumber) => {
      // Numeric format for data columns
      if (isNum && colNumber > 1 && typeof cell.value === 'number') {
        const isDecimal = NUMERIC_TYPES.some(t =>
          (field.type || '').toUpperCase() === t && (t === 'DECIMAL' || t === 'FLOAT')
        )
        cell.numFmt = isDecimal ? '#,##0.##' : '#,##0'
        cell.alignment = { vertical: 'middle', horizontal: 'right', wrapText: false }
      } else {
        cell.alignment = { vertical: 'middle', wrapText: false }
      }

      if (hasDiff) {
        cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFFFF3CD' } }
        cell.font = colNumber === 1
          ? { bold: true, color: { argb: 'FF92400E' }, size: 10 }
          : { color: { argb: 'FF92400E' }, size: 10 }
      } else {
        cell.font = { size: 10 }
      }
      cell.border = {
        top: { style: 'hair', color: { argb: 'FFD1D5DB' } },
        left: { style: 'hair', color: { argb: 'FFD1D5DB' } },
        bottom: { style: 'hair', color: { argb: 'FFD1D5DB' } },
        right: { style: 'hair', color: { argb: 'FFD1D5DB' } }
      }
    })
  })

  // ── Freeze header row ────────────────────────────────────────
  ws.views = [{ state: 'frozen', xSplit: 0, ySplit: 1 }]

  // ── Download ─────────────────────────────────────────────────
  const buffer = await wb.xlsx.writeBuffer()
  const blob = new Blob([buffer], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  const now = new Date().toISOString().slice(0, 10)
  link.href = url
  link.download = `${isKo ? '레코드비교' : 'RecordCompare'}_${now}.xlsx`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.diff-row {
  background-color: rgba(234, 179, 8, 0.08) !important;
}

:deep(.va-modal--dark) .diff-row,
.diff-row.dark {
  background-color: rgba(234, 179, 8, 0.15) !important;
}

.diff-cell {
  color: #b45309;
  font-weight: 600;
}

:deep(.va-modal--dark) .diff-cell {
  color: #fbbf24;
}

.compare-matrix-table tr:hover {
  background-color: rgba(0, 0, 0, 0.02);
}
</style>
