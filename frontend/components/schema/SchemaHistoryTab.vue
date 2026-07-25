<template>
  <div class="schema-history-tab flex flex-col h-full min-h-[400px]">
    <div class="flex justify-between items-center mb-4">
      <h3 class="text-lg font-semibold">{{ $t('schema_history.title') }}</h3>
      <va-button preset="secondary" icon="refresh" @click="fetchHistory">
        {{ $t('match_review.refresh') }}
      </va-button>
    </div>

    <va-data-table
      :items="historyList"
      :columns="columns"
      :loading="isLoading"
      striped
      hoverable
      class="flex-1"
    >
      <template #cell(targetType)="{ rowData }">
        <va-badge :text="$t('schema_history.' + rowData.targetType.toLowerCase())" :color="getTypeColor(rowData.targetType)" />
      </template>

      <template #cell(action)="{ rowData }">
        <va-badge :text="$t('schema_history.' + rowData.action.toLowerCase())" :color="getActionColor(rowData.action)" />
      </template>
      
      <template #cell(changedAt)="{ rowData }">
        {{ formatWithTimezone(rowData.changedAt) }}
      </template>

      <template #cell(actions)="{ rowData, isExpanded, toggleRow }">
        <va-button
          preset="plain"
          size="small"
          :icon="isExpanded ? 'expand_less' : 'expand_more'"
          @click="toggleRow"
        >
          {{ isExpanded ? $t('vuestic.close') : $t('schema_history.view_changes') || 'View Changes' }}
        </va-button>
      </template>

      <template #expandableRow="{ rowData }">
        <div class="p-4 bg-gray-50 border-t flex gap-4">
          <div class="flex-1" v-if="rowData.beforeSnapshot">
            <h5 class="font-bold mb-2 text-red-600">{{ $t('schema_history.before') }}</h5>
            <pre class="bg-gray-100 p-2 rounded text-xs overflow-x-auto">{{ formatJson(rowData.beforeSnapshot) }}</pre>
          </div>
          <div class="flex-1" v-if="rowData.afterSnapshot">
            <h5 class="font-bold mb-2 text-green-600">{{ $t('schema_history.after') }}</h5>
            <pre class="bg-gray-100 p-2 rounded text-xs overflow-x-auto">{{ formatJson(rowData.afterSnapshot) }}</pre>
          </div>
          <div v-if="!rowData.beforeSnapshot && !rowData.afterSnapshot" class="text-gray-500 italic">
            {{ $t('schema_history.no_history') }}
          </div>
        </div>
      </template>
    </va-data-table>

    <div class="flex justify-center mt-4">
      <va-pagination
        v-if="totalPages > 1"
        v-model="currentPage"
        :pages="totalPages"
        @update:modelValue="fetchHistory"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'vuestic-ui'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { useTimezoneDate } from '~/composables/useTimezoneDate'

const props = defineProps<{
  domainId: string | null
}>()

const { t } = useI18n()
const { init } = useToast()
const { customFetch } = useCustomFetch()
const { formatWithTimezone } = useTimezoneDate()

const isLoading = ref(false)
const historyList = ref<any[]>([])
const currentPage = ref(1)
const totalPages = ref(1)
const size = 20

const columns = computed(() => [
  { key: 'targetType', label: t('schema_history.target_type') },
  { key: 'action', label: t('schema_history.action') },
  { key: 'changedBy', label: t('schema_history.changed_by') },
  { key: 'changedAt', label: t('schema_history.changed_at') },
  { key: 'actions', label: ' ' } // Expander
])

const getTypeColor = (type: string) => {
  if (type === 'FIELD') return 'info'
  if (type === 'NODE') return 'warning'
  if (type === 'DOMAIN') return 'primary'
  if (type === 'GROUP') return 'secondary'
  return 'gray'
}

const getActionColor = (action: string) => {
  if (action === 'CREATE') return 'success'
  if (action === 'UPDATE') return 'warning'
  if (action === 'DELETE') return 'danger'
  return 'gray'
}

const formatJson = (str: string) => {
  if (!str) return ''
  try {
    const obj = typeof str === 'string' ? JSON.parse(str) : str
    return JSON.stringify(obj, null, 2)
  } catch {
    return str
  }
}

const fetchHistory = async () => {
  if (!props.domainId) return
  isLoading.value = true
  try {
    const res = await customFetch(`/api/domains/${props.domainId}/schema-history?page=${currentPage.value - 1}&size=${size}`)
    historyList.value = res.content || []
    totalPages.value = res.totalPages || 1
  } catch (e) {
    console.error(e)
    init({ message: '이력 조회 실패', color: 'danger' })
  } finally {
    isLoading.value = false
  }
}

watch(() => props.domainId, () => {
  currentPage.value = 1
  fetchHistory()
}, { immediate: true })

</script>
