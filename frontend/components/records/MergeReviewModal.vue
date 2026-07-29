<template>
  <va-modal
    :modelValue="show"
    @update:modelValue="$emit('close')"
    size="large"
    hide-default-actions
    no-outside-dismiss
    class="merge-review-modal"
  >
    <template #header>
      <div class="modal-header bg-gradient-to-r from-blue-500 to-indigo-600 text-white p-4 rounded-t-lg flex justify-between items-center">
        <h2 class="text-xl font-bold m-0">{{ $t('merge.title') }}</h2>
        <va-button preset="plain" icon="close" color="white" @click="$emit('close')" />
      </div>
    </template>

    <div class="modal-body p-4 max-h-[70vh] overflow-y-auto">
      <div class="flex gap-4 mb-4">
        <va-button-toggle
          v-model="mode"
          :options="modeOptions"
          preset="primary"
          color="primary"
        />
      </div>

      <div class="flex gap-6 flex-col md:flex-row">
        <!-- Field Comparison -->
        <div class="flex-1">
          <h3 class="text-lg font-semibold mb-2">{{ $t('merge.field_comparison') }}</h3>
          <va-data-table
            :items="comparisonItems"
            :columns="columns"
            striped
            hoverable
          >
            <!-- Field Name Column -->
            <template #cell(fieldName)="{ rowData }">
              <span class="font-medium">{{ rowData.fieldName }}</span>
            </template>
            
            <!-- Existing Value Column -->
            <template #cell(existingValue)="{ rowData }">
              <div class="flex items-center gap-2">
                <va-radio
                  v-if="mode === 'manual'"
                  v-model="rowData.selectedValue"
                  option="existing"
                  :label="formatValue(rowData.existingValue)"
                  @update:modelValue="updatePreview"
                />
                <span v-else>{{ formatValue(rowData.existingValue) }}</span>
              </div>
            </template>

            <!-- Incoming Value Column -->
            <template #cell(incomingValue)="{ rowData }">
              <div class="flex items-center gap-2">
                <va-radio
                  v-if="mode === 'manual'"
                  v-model="rowData.selectedValue"
                  option="incoming"
                  :label="formatValue(rowData.incomingValue)"
                  @update:modelValue="updatePreview"
                />
                <span v-else>{{ formatValue(rowData.incomingValue) }}</span>
              </div>
            </template>
          </va-data-table>
        </div>

        <!-- Result Preview -->
        <div class="w-full md:w-1/3">
          <va-card class="preview-card bg-gray-50 glassmorphism">
            <va-card-title>{{ $t('merge.preview') }}</va-card-title>
            <va-card-content>
              <div v-for="(val, key) in previewRecord" :key="key" class="mb-2 border-b pb-1 last:border-0">
                <div class="text-sm text-gray-500">{{ key }}</div>
                <div class="font-medium break-all">{{ formatValue(val) }}</div>
              </div>
            </va-card-content>
          </va-card>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="flex justify-end gap-3 p-4 border-t">
        <va-button preset="secondary" color="gray" @click="$emit('close')">
          {{ $t('merge.cancel') }}
        </va-button>
        <va-button color="primary" @click="executeMerge" :loading="isSubmitting">
          {{ $t('merge.execute_merge') }}
        </va-button>
      </div>
    </template>
  </va-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'vuestic-ui'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { useTimezoneDate } from '~/composables/useTimezoneDate'

const props = defineProps<{
  show: boolean
  existingRecord: any
  incomingData: any
  candidateId?: string
  domainId?: string
}>()

const emit = defineEmits(['close', 'merged'])
const { t } = useI18n()
const { init } = useToast()
const { customFetch } = useCustomFetch()
const { formatWithTimezone } = useTimezoneDate()

const isSubmitting = ref(false)
const mode = ref('auto')
const previewRecord = ref<any>({})
const comparisonItems = ref<any[]>([])
const survivorshipRules = ref<any[]>([])

const modeOptions = computed(() => [
  { label: t('merge.auto_survivorship'), value: 'auto' },
  { label: t('merge.manual_select'), value: 'manual' }
])

const columns = computed(() => [
  { key: 'fieldName', label: t('merge.field_name'), sortable: true },
  { key: 'existingValue', label: t('match_review.existing_record') },
  { key: 'incomingValue', label: t('match_review.incoming_data') }
])

const formatValue = (val: any) => {
  if (val === null || val === undefined) return '-'
  if (typeof val === 'string' && /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/.test(val)) {
    return formatWithTimezone(val)
  }
  if (typeof val === 'object') return JSON.stringify(val)
  return String(val)
}

const getRecordData = (recordObj: any) => {
  if (!recordObj) return {}
  if (typeof recordObj.data === 'string') {
    try { return JSON.parse(recordObj.data) } catch (e) { return {} }
  }
  if (typeof recordObj.data === 'object' && recordObj.data !== null) {
    return recordObj.data
  }
  return recordObj
}

const buildComparisonItems = () => {
  const existingMap = getRecordData(props.existingRecord)
  const incomingMap = getRecordData(props.incomingData)
  const allKeys = new Set([...Object.keys(existingMap), ...Object.keys(incomingMap)])
  
  allKeys.delete('id')
  allKeys.delete('createdAt')
  allKeys.delete('updatedAt')

  const items = []
  for (const key of allKeys) {
    items.push({
      fieldName: key,
      existingValue: existingMap[key],
      incomingValue: incomingMap[key],
      selectedValue: 'existing'
    })
  }
  comparisonItems.value = items
}

const fetchSurvivorshipRules = async () => {
  if (!props.domainId) return
  try {
    const res = await customFetch(`/api/records/domains/${props.domainId}/survivorship-rules`)
    survivorshipRules.value = res || []
  } catch (e) {
    survivorshipRules.value = []
  }
}

const updatePreview = async () => {
  const existingMap = getRecordData(props.existingRecord)
  const incomingMap = getRecordData(props.incomingData)

  if (mode.value === 'auto') {
    const preview: any = { ...existingMap }
    
    // Apply local survivorship rules preview
    for (const item of comparisonItems.value) {
      const fieldKey = item.fieldName
      const rule = survivorshipRules.value.find((r: any) => r.fieldKey === fieldKey)
      
      if (rule) {
        if (rule.strategy === 'MOST_COMPLETE') {
          const exStr = item.existingValue != null ? String(item.existingValue) : ''
          const inStr = item.incomingValue != null ? String(item.incomingValue) : ''
          if (inStr.length > exStr.length) {
            preview[fieldKey] = item.incomingValue
          }
        } else if (rule.strategy === 'MOST_RECENT') {
          preview[fieldKey] = item.incomingValue !== undefined ? item.incomingValue : item.existingValue
        }
      }
    }
    previewRecord.value = preview
  } else {
    const newRecord: any = { ...existingMap }
    comparisonItems.value.forEach(item => {
      newRecord[item.fieldName] = item.selectedValue === 'incoming' 
        ? item.incomingValue 
        : item.existingValue
    })
    previewRecord.value = newRecord
  }
}

const executeMerge = async () => {
  isSubmitting.value = true
  const survivorId = props.existingRecord?.id || props.existingRecord?.survivorRecordId
  const mergedId = props.incomingData?.id || props.candidateId

  if (!survivorId || !mergedId) {
    init({ message: t('merge.merge_fail'), color: 'danger' })
    isSubmitting.value = false
    return
  }

  try {
    if (mode.value === 'auto') {
      await customFetch('/api/records/merge/auto', {
        method: 'POST',
        body: {
          survivorRecordId: survivorId,
          mergedRecordIds: [mergedId]
        }
      })
    } else {
      const fieldResolutions: Record<string, string> = {}
      comparisonItems.value.forEach(item => {
        fieldResolutions[item.fieldName] = item.selectedValue === 'incoming' ? mergedId : survivorId
      })
      
      await customFetch('/api/records/merge', {
        method: 'POST',
        body: {
          survivorRecordId: survivorId,
          mergedRecordIds: [mergedId],
          fieldResolutions
        }
      })
    }
    
    init({ message: t('merge.merge_success'), color: 'success' })
    emit('merged')
    emit('close')
  } catch (e) {
    init({ message: t('merge.merge_fail'), color: 'danger' })
  } finally {
    isSubmitting.value = false
  }
}

watch(() => mode.value, () => {
  updatePreview()
})

watch(() => props.show, async (val) => {
  if (val) {
    buildComparisonItems()
    await fetchSurvivorshipRules()
    mode.value = 'auto'
    updatePreview()
  }
})

onMounted(async () => {
  if (props.show) {
    buildComparisonItems()
    await fetchSurvivorshipRules()
    updatePreview()
  }
})
</script>

<style scoped>
.glassmorphism {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}
.modal-header {
  margin: -1.25rem -1.25rem 1rem -1.25rem;
  border-radius: 0.5rem 0.5rem 0 0;
}
</style>
