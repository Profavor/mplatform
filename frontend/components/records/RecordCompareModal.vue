<template>
  <va-modal
    v-model="modalVisible"
    :title="t('compare_records_title') || (isEn ? 'Compare Selected Records' : '선택 레코드 비교')"
    size="large"
    hide-default-actions
    class="record-compare-modal"
  >
    <div style="display: flex; flex-direction: column; gap: 1rem; max-height: 75vh;">

      <!-- Controls Header Toolbar -->
      <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.5rem 0.75rem; background: var(--va-background-element); border-radius: 8px; border: 1px solid var(--va-background-border);">
        <div style="display: flex; align-items: center; gap: 0.5rem;">
          <va-icon name="scale" color="primary" />
          <span style="font-weight: 700; font-size: 0.9rem;">
            {{ t('comparing_count') || (isEn ? `Comparing ${records.length} records` : `${records.length}개 레코드 비교 중`) }}
          </span>
        </div>

        <div style="display: flex; align-items: center; gap: 1rem;">
          <va-switch
            v-model="onlyDifferences"
            size="small"
            color="warning"
            :label="t('only_differences') || (isEn ? 'Show Only Differences' : '차이점만 보기')"
          />
          <va-badge
            :text="`${diffFieldsCount} ${t('diff_count_suffix') || (isEn ? 'Diffs' : '개 항목 다름')}`"
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
                  {{ t('field_name') || (isEn ? 'Attributes / Fields' : '속성 / 필드명') }}
                </div>
              </th>
              
              <th
                v-for="(rec, index) in records"
                :key="getRecordId(rec, index)"
                style="padding: 0.75rem; text-align: left; min-width: 220px; border-right: 1px solid var(--va-background-border); background: var(--va-background-element);"
              >
                <div style="display: flex; flex-direction: column; gap: 0.35rem;">
                  <div style="display: flex; align-items: center; justify-content: space-between;">
                    <span style="font-weight: 800; font-size: 0.95rem; color: var(--va-primary);">
                      {{ getRecordName(rec) }}
                    </span>
                    <va-chip size="small" color="primary" square outline style="font-weight: 700; font-family: monospace;">
                      {{ getRecordId(rec, index) }}
                    </va-chip>
                  </div>
                  <div style="font-size: 0.75rem; color: var(--va-text-secondary);">
                    Record #{{ index + 1 }}
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
                :class="{ 'diff-cell': isFieldDifferent(field) }"
                style="padding: 0.65rem 0.75rem; border-right: 1px solid var(--va-background-border); vertical-align: top;"
              >
                <div style="word-break: break-all; line-height: 1.4;">
                  {{ formatFieldValue(rec, field) }}
                </div>
              </td>
            </tr>

            <tr v-if="displayedFields.length === 0">
              <td :colspan="records.length + 1" style="text-align: center; padding: 2rem; color: var(--va-text-secondary);">
                {{ t('no_differences_found') || (isEn ? 'No differences found between selected records.' : '선택된 레코드 간 차이점이 없습니다.') }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Footer Actions -->
    <template #footer>
      <div style="display: flex; justify-content: flex-end; width: 100%;">
        <va-button color="secondary" @click="handleClose">
          {{ t('close') || (isEn ? 'Close' : '닫기') }}
        </va-button>
      </div>
    </template>
  </va-modal>
</template>

<script setup>
import { ref, computed } from 'vue'

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

const formatFieldValue = (record, field) => {
  const raw = getRawFieldValue(record, field)
  if (raw === undefined || raw === null || raw === '') return '-'
  
  if (typeof raw === 'object') {
    if (raw.ko || raw.en) {
      return raw[i18nLocale?.value || 'ko'] || raw.ko || raw.en || ''
    }
    return JSON.stringify(raw)
  }
  return String(raw)
}

const getRecordId = (rec, idx) => {
  if (!rec) return `REC-${idx}`
  let idVal = rec.empNo || rec.employeeNo || rec.code || rec.id || ''
  if (!idVal && rec.data) {
    idVal = rec.data.empNo || rec.data.code || rec.data.id || ''
  }
  return idVal ? String(idVal) : `REC-${idx + 1}`
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
  const firstVal = String(formatFieldValue(props.records[0], field)).trim()
  for (let i = 1; i < props.records.length; i++) {
    const currVal = String(formatFieldValue(props.records[i], field)).trim()
    if (firstVal !== currVal) return true
  }
  return false
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
