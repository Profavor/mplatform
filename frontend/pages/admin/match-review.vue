<template>
  <div class="page-container p-4 flex flex-col h-full bg-gray-50">
    <div class="page-header flex justify-between items-center mb-6">
      <h1 class="page-title text-2xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-indigo-600">
        {{ $t('match_review.title') || '매칭 후보 검토' }}
      </h1>
      <div class="flex gap-3">
        <va-select
          v-model="selectedDomain"
          :options="domainOptions"
          value-by="value"
          :placeholder="$t('match_review.domain_select') || '도메인 선택'"
          class="w-48"
        />
        <va-button color="primary" icon="refresh" @click="refreshGrid">{{ $t('match_review.refresh') || '새로고침' }}</va-button>
      </div>
    </div>

    <!-- Status Filters & Batch Actions -->
    <div class="flex justify-between items-center mb-4">
      <div class="flex gap-2">
        <va-button-toggle
          v-model="statusFilter"
          :options="statusOptions"
          preset="secondary"
          color="primary"
          @update:modelValue="onStatusFilterChanged"
        />
      </div>
      <div class="flex gap-2" v-if="hasWritePermission">
        <va-button color="success" icon="check_circle" :disabled="selectedRows.length === 0" @click="batchConfirm">
          {{ $t('match_review.batch_confirm') || '일괄 승인' }} ({{ selectedRows.length }})
        </va-button>
        <va-button color="danger" icon="cancel" :disabled="selectedRows.length === 0" @click="batchReject">
          {{ $t('match_review.batch_reject') || '일괄 거절' }} ({{ selectedRows.length }})
        </va-button>
      </div>
    </div>

    <div class="flex-1 flex gap-4 min-h-0">
      <!-- Grid Section -->
      <va-card class="flex-1 flex flex-col shadow-md border border-gray-200">
        <va-card-content class="flex-1 p-0 flex flex-col h-full">
          <client-only>
            <ag-grid-vue
              v-if="isMounted"
              style="width: 100%; height: 100%;"
              :theme="gridTheme"
              :columnDefs="columnDefs"
              :defaultColDef="defaultColDef"
              :autoSizeStrategy="autoSizeStrategy"
              rowModelType="serverSide"
              :serverSideDatasource="serverSideDatasource"
              :cacheBlockSize="20"
              :rowSelection="{ mode: 'multiRow' }"
              :pagination="true"
              :paginationPageSize="20"
              :paginationPageSizeSelector="[10, 20, 50]"
              @grid-ready="onGridReady"
              @selection-changed="onSelectionChanged"
              @row-clicked="onRowClicked"
            />
          </client-only>
        </va-card-content>
      </va-card>

      <!-- Detail Sidebar -->
      <va-card v-if="selectedCandidate" class="w-96 shadow-md border border-gray-200 flex flex-col glassmorphism">
        <va-card-title class="bg-gradient-to-r from-gray-100 to-gray-50 border-b pb-3">
          <div class="flex justify-between items-center w-full">
            <span>{{ $t('match_review.field_details') || '후보 상세 내용' }}</span>
            <va-badge :text="(selectedCandidate.score * 100).toFixed(1) + '%'" :color="getScoreColor(selectedCandidate.score)" />
          </div>
        </va-card-title>
        <va-card-content class="flex-1 overflow-y-auto p-4">
          <div class="mb-4">
            <h4 class="font-semibold text-gray-700 mb-2">{{ $t('match_review.existing_record') || '기존 레코드' }}</h4>
            <div class="bg-white p-3 rounded border text-sm text-gray-600 break-all">
              <div v-for="(val, key) in selectedCandidate.existingRecord" :key="'ex-'+key" class="mb-1">
                <span class="font-medium text-gray-800">{{ key }}:</span> {{ formatValue(val) }}
              </div>
            </div>
          </div>
          <div class="mb-4">
            <h4 class="font-semibold text-gray-700 mb-2">{{ $t('match_review.incoming_data') || '유입 레코드' }}</h4>
            <div class="bg-white p-3 rounded border text-sm text-gray-600 break-all">
              <div v-for="(val, key) in selectedCandidate.incomingData" :key="'in-'+key" class="mb-1">
                <span class="font-medium text-gray-800">{{ key }}:</span> {{ formatValue(val) }}
              </div>
            </div>
          </div>
        </va-card-content>
        <div class="p-4 border-t flex justify-end gap-2 bg-gray-50" v-if="hasWritePermission && selectedCandidate.status === 'PENDING_REVIEW'">
          <va-button preset="secondary" color="danger" @click="rejectSingle(selectedCandidate)">{{ $t('match_review.reject_new') || '거절' }}</va-button>
          <va-button color="success" @click="openMergeModal(selectedCandidate)">{{ $t('match_review.confirm_merge') || '병합 검토' }}</va-button>
        </div>
      </va-card>
    </div>

    <!-- Merge Review Modal -->
    <MergeReviewModal
      v-if="isMergeModalOpen"
      :show="isMergeModalOpen"
      :existingRecord="selectedCandidate?.existingRecord"
      :incomingData="selectedCandidate?.incomingData"
      :candidateId="selectedCandidate?.id"
      :domainId="selectedDomain"
      @close="isMergeModalOpen = false"
      @merged="onMergeSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'vuestic-ui'
import { AgGridVue } from 'ag-grid-vue3'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { useTimezoneDate } from '~/composables/useTimezoneDate'
import { usePermission } from '~/composables/usePermission'
import { useAgGridTheme } from '~/composables/useAgGridTheme'
import MergeReviewModal from '~/components/records/MergeReviewModal.vue'

const { t } = useI18n()
const { init } = useToast()
const { customFetch } = useCustomFetch()
const { formatWithTimezone } = useTimezoneDate()
const { hasPermission } = usePermission()
const { gridTheme, autoSizeStrategy } = useAgGridTheme()

const isMounted = ref(false)
const selectedDomain = ref<string>('')
const domainOptions = ref<{label: string, value: string}[]>([])
const statusFilter = ref('PENDING_REVIEW')
const selectedRows = ref<any[]>([])
const selectedCandidate = ref<any>(null)
const isMergeModalOpen = ref(false)

const hasWritePermission = computed(() => {
  return hasPermission('domain:write') || hasPermission('domain:*')
})

const statusOptions = computed(() => [
  { label: t('match_review.status_pending') || '검토 대기', value: 'PENDING_REVIEW' },
  { label: t('match_review.status_confirmed') || '병합 완료', value: 'CONFIRMED_MERGE' },
  { label: t('match_review.status_rejected') || '거절됨', value: 'REJECTED' },
  { label: '전체', value: 'ALL' }
])

let gridApi: any = null

const defaultColDef = {
  sortable: true,
  resizable: true,
  minWidth: 100
}

const getScoreColor = (score: number) => {
  if (score >= 0.9) return 'success'
  if (score >= 0.8) return 'warning'
  return 'danger'
}

const columnDefs = computed(() => [
  { 
    headerName: 'ID', 
    field: 'id', 
    checkboxSelection: true, 
    headerCheckboxSelection: true,
    width: 150
  },
  { field: 'source', headerName: t('merge.source') || '소스 시스템', width: 150 },
  { 
    field: 'score', 
    headerName: t('match_review.similarity_score') || '유사도 점수', 
    width: 200,
    cellRenderer: (params: any) => {
      if (params.value == null) return ''
      const pct = (params.value * 100).toFixed(1)
      const colorClass = params.value >= 0.9 ? 'text-green-600' : (params.value >= 0.8 ? 'text-yellow-600' : 'text-red-600')
      return `<div class="flex items-center h-full gap-2">
                <div class="w-full bg-gray-200 rounded h-2">
                  <div class="h-2 rounded ${params.value >= 0.9 ? 'bg-green-500' : (params.value >= 0.8 ? 'bg-yellow-500' : 'bg-red-500')}" style="width: ${pct}%"></div>
                </div>
                <span class="text-xs font-bold ${colorClass}">${pct}%</span>
              </div>`
    }
  },
  { 
    field: 'status', 
    headerName: t('match_review.status_filter') || '상태',
    width: 150,
    cellRenderer: (params: any) => {
      let color = 'gray'
      let text = params.value
      if (params.value === 'PENDING_REVIEW') { color = 'warning'; text = t('match_review.status_pending') || '검토 대기' }
      else if (params.value === 'CONFIRMED_MERGE') { color = 'success'; text = t('match_review.status_confirmed') || '병합 완료' }
      else if (params.value === 'REJECTED') { color = 'danger'; text = t('match_review.status_rejected') || '거절됨' }
      
      return `<span style="padding: 2px 8px; border-radius: 12px; font-size: 0.75rem; font-weight: bold; background-color: var(--va-${color}); color: white;">${text}</span>`
    }
  },
  { 
    field: 'createdAt', 
    headerName: t('schema_history.changed_at') || '생성 시각',
    valueFormatter: (params: any) => params.value ? formatWithTimezone(params.value) : '-'
  }
])

const serverSideDatasource = {
  getRows: async (params: any) => {
    if (!selectedDomain.value) {
      params.success({ rowData: [], rowCount: 0 })
      return
    }

    const page = params.request.startRow / (params.request.endRow - params.request.startRow)
    const size = params.request.endRow - params.request.startRow

    try {
      let url = `/api/domains/${selectedDomain.value}/match-candidates?page=${page}&size=${size}`
      if (statusFilter.value !== 'ALL') {
        url += `&status=${statusFilter.value}`
      }

      const res = await customFetch(url)
      const content = res.content || res.data || []
      const totalElements = res.totalElements !== undefined ? res.totalElements : (res.total || content.length)
      
      params.success({ rowData: content, rowCount: totalElements })
    } catch (e) {
      console.error(e)
      params.fail()
    }
  }
}

const getDomainName = (d: any) => {
  if (!d) return ''
  if (typeof d.name === 'object') {
    return d.name.ko || d.name.en || d.id
  }
  return d.name || d.id
}

const fetchDomains = async () => {
  try {
    const res = await customFetch('/api/domains')
    domainOptions.value = (res.content || res || []).map((d: any) => ({
      label: getDomainName(d),
      value: d.id
    }))
    if (domainOptions.value.length > 0 && !selectedDomain.value) {
      selectedDomain.value = domainOptions.value[0].value
    }
  } catch (e) {
    console.error('Failed to fetch domains', e)
  }
}

const onGridReady = (params: any) => {
  gridApi = params.api
}

const refreshGrid = () => {
  if (gridApi) {
    gridApi.refreshServerSide()
  }
  selectedRows.value = []
  selectedCandidate.value = null
}

const onStatusFilterChanged = () => {
  refreshGrid()
}

const onSelectionChanged = () => {
  if (gridApi) {
    selectedRows.value = gridApi.getSelectedRows()
  }
}

const onRowClicked = (event: any) => {
  selectedCandidate.value = event.data
}

const formatValue = (val: any) => {
  if (val === null || val === undefined) return '-'
  if (typeof val === 'string' && /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/.test(val)) {
    return formatWithTimezone(val)
  }
  if (typeof val === 'object') return JSON.stringify(val)
  return String(val)
}

const openMergeModal = (candidate: any) => {
  selectedCandidate.value = candidate
  isMergeModalOpen.value = true
}

const onMergeSuccess = () => {
  refreshGrid()
}

const rejectSingle = async (candidate: any) => {
  try {
    await customFetch(`/api/match-candidates/${candidate.id}/reject`, {
      method: 'POST'
    })
    init({ message: t('match_review.reject_success') || '거절 처리되었습니다.', color: 'success' })
    refreshGrid()
  } catch (e) {
    init({ message: t('match_review.reject_fail') || '거절 처리에 실패했습니다.', color: 'danger' })
  }
}

const batchConfirm = async () => {
  const ids = selectedRows.value.map(r => r.id)
  try {
    await customFetch(`/api/match-candidates/batch/confirm`, {
      method: 'POST',
      body: { ids, domainId: selectedDomain.value }
    })
    init({ message: t('match_review.confirm_success') || '승인 처리되었습니다.', color: 'success' })
    refreshGrid()
  } catch (e) {
    init({ message: t('match_review.confirm_fail') || '승인 처리에 실패했습니다.', color: 'danger' })
  }
}

const batchReject = async () => {
  const ids = selectedRows.value.map(r => r.id)
  try {
    await customFetch(`/api/match-candidates/batch/reject`, {
      method: 'POST',
      body: { ids }
    })
    init({ message: t('match_review.reject_success') || '거절 처리되었습니다.', color: 'success' })
    refreshGrid()
  } catch (e) {
    init({ message: t('match_review.reject_fail') || '거절 처리에 실패했습니다.', color: 'danger' })
  }
}

watch(selectedDomain, () => {
  refreshGrid()
})

onMounted(() => {
  isMounted.value = true
  fetchDomains()
})
</script>

<style scoped>
.glassmorphism {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
}
</style>
