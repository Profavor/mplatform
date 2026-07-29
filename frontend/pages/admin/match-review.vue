<template>
  <div class="page-container p-6 bg-slate-50 min-h-screen flex flex-col gap-5">
    <!-- Page Header -->
    <div class="flex flex-wrap justify-between items-center bg-white p-5 rounded-xl border border-slate-200 shadow-sm gap-4">
      <div>
        <h1 class="text-2xl font-bold text-slate-800 flex items-center gap-2">
          <va-icon name="fact_check" color="primary" size="28px" />
          {{ $t('match_review.title') || '매칭 후보 검토' }}
        </h1>
        <p class="text-xs text-slate-500 mt-1">
          유사도가 높은 매칭 후보 데이터를 검토하여 마스터 데이터 병합 승인 또는 반결 처리를 진행합니다.
        </p>
      </div>

      <div class="flex items-center gap-3">
        <div class="flex items-center gap-2">
          <span class="text-xs font-semibold text-slate-600 whitespace-nowrap">대상 도메인:</span>
          <va-select
            v-model="selectedDomain"
            :options="domainOptions"
            value-by="value"
            :placeholder="$t('match_review.domain_select') || '도메인 선택'"
            class="w-56"
          />
        </div>
        <va-button color="primary" preset="secondary" icon="refresh" @click="refreshGrid">
          {{ $t('match_review.refresh') || '새로고침' }}
        </va-button>
      </div>
    </div>

    <!-- Status Filters & Batch Action Controls -->
    <va-card flat class="border border-slate-200 shadow-sm rounded-xl">
      <va-card-content class="p-4 flex flex-wrap justify-between items-center gap-4">
        <div class="flex items-center gap-3">
          <span class="text-xs font-semibold text-slate-500 uppercase tracking-wider">검토 상태:</span>
          <va-button-toggle
            v-model="statusFilter"
            :options="statusOptions"
            preset="secondary"
            color="primary"
            size="small"
            @update:modelValue="onStatusFilterChanged"
          />
        </div>

        <div class="flex items-center gap-2" v-if="hasWritePermission">
          <va-button
            color="success"
            icon="check_circle"
            size="small"
            :disabled="selectedRows.length === 0"
            @click="batchConfirm"
          >
            {{ $t('match_review.batch_confirm') || '일괄 승인' }} ({{ selectedRows.length }})
          </va-button>
          <va-button
            color="danger"
            preset="secondary"
            icon="cancel"
            size="small"
            :disabled="selectedRows.length === 0"
            @click="batchReject"
          >
            {{ $t('match_review.batch_reject') || '일괄 거절' }} ({{ selectedRows.length }})
          </va-button>
        </div>
      </va-card-content>
    </va-card>

    <!-- Main Content Layout (Grid + Detail Sidebar) -->
    <div class="flex gap-5 items-start">
      <!-- Grid Section -->
      <va-card class="flex-1 shadow-sm border border-slate-200 rounded-xl overflow-hidden">
        <va-card-content class="p-0" style="height: 600px;">
          <client-only>
            <ag-grid-vue
              v-if="isMounted"
              style="width: 100%; height: 100%;"
              :theme="gridTheme"
              :columnDefs="columnDefs"
              :defaultColDef="defaultColDef"
              :autoSizeStrategy="autoSizeStrategy"
              rowModelType="infinite"
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
      <va-card v-if="selectedCandidate" class="w-96 shadow-md border border-slate-200 rounded-xl overflow-hidden glassmorphism flex flex-col" style="height: 600px;">
        <va-card-title class="bg-slate-100 border-b border-slate-200 py-3 px-4 flex justify-between items-center">
          <span class="font-bold text-slate-700 text-sm flex items-center gap-1.5">
            <va-icon name="info" size="18px" color="primary" />
            {{ $t('match_review.field_details') || '후보 상세 비교' }}
          </span>
          <va-badge
            :text="(selectedCandidate.score * 100).toFixed(1) + '%'"
            :color="getScoreColor(selectedCandidate.score)"
          />
        </va-card-title>

        <va-card-content class="flex-1 overflow-y-auto p-4 space-y-4">
          <!-- Existing Record Data -->
          <div class="bg-white p-3 rounded-lg border border-slate-200 shadow-2xs">
            <h4 class="text-xs font-bold text-slate-500 uppercase tracking-wider mb-2 flex items-center gap-1">
              <va-icon name="inventory_2" size="14px" color="gray" />
              {{ $t('match_review.existing_record') || '기존 마스터 레코드' }}
            </h4>
            <div class="space-y-1 text-xs text-slate-600 break-all">
              <div v-for="(val, key) in parseRecordData(selectedCandidate.existingRecord)" :key="'ex-'+key" class="flex justify-between py-1 border-b border-slate-100 last:border-0">
                <span class="font-medium text-slate-700">{{ key }}:</span>
                <span class="text-slate-900 font-mono">{{ formatValue(val) }}</span>
              </div>
            </div>
          </div>

          <!-- Incoming Record Data -->
          <div class="bg-white p-3 rounded-lg border border-slate-200 shadow-2xs">
            <h4 class="text-xs font-bold text-slate-500 uppercase tracking-wider mb-2 flex items-center gap-1">
              <va-icon name="move_to_inbox" size="14px" color="info" />
              {{ $t('match_review.incoming_data') || '신규 유입 레코드' }}
            </h4>
            <div class="space-y-1 text-xs text-slate-600 break-all">
              <div v-for="(val, key) in parseRecordData(selectedCandidate.incomingData)" :key="'in-'+key" class="flex justify-between py-1 border-b border-slate-100 last:border-0">
                <span class="font-medium text-slate-700">{{ key }}:</span>
                <span class="text-slate-900 font-mono">{{ formatValue(val) }}</span>
              </div>
            </div>
          </div>
        </va-card-content>

        <div class="p-4 bg-slate-50 border-t border-slate-200 flex justify-end gap-2" v-if="hasWritePermission && selectedCandidate.status === 'PENDING_REVIEW'">
          <va-button preset="secondary" color="danger" size="small" @click="rejectSingle(selectedCandidate)">
            {{ $t('match_review.reject_new') || '거절' }}
          </va-button>
          <va-button color="success" size="small" icon="merge_type" @click="openMergeModal(selectedCandidate)">
            {{ $t('match_review.confirm_merge') || '병합 검토' }}
          </va-button>
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
  minWidth: 120
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
    width: 140
  },
  { field: 'source', headerName: t('merge.source') || '소스 시스템', width: 140 },
  { 
    field: 'score', 
    headerName: t('match_review.similarity_score') || '유사도 점수', 
    width: 180,
    cellRenderer: (params: any) => {
      if (params.value == null) return '-'
      const pct = (params.value * 100).toFixed(1)
      const colorClass = params.value >= 0.9 ? 'text-emerald-600' : (params.value >= 0.8 ? 'text-amber-600' : 'text-rose-600')
      const bgClass = params.value >= 0.9 ? 'bg-emerald-500' : (params.value >= 0.8 ? 'bg-amber-500' : 'bg-rose-500')
      return `<div class="flex items-center h-full gap-2 px-1">
                <div class="w-full bg-slate-200 rounded-full h-2 overflow-hidden">
                  <div class="h-2 rounded-full ${bgClass}" style="width: ${pct}%"></div>
                </div>
                <span class="text-xs font-bold ${colorClass} w-12 text-right">${pct}%</span>
              </div>`
    }
  },
  { 
    field: 'status', 
    headerName: t('match_review.status_filter') || '상태',
    width: 140,
    cellRenderer: (params: any) => {
      let color = 'gray'
      let text = params.value
      if (params.value === 'PENDING_REVIEW') { color = 'warning'; text = t('match_review.status_pending') || '검토 대기' }
      else if (params.value === 'CONFIRMED_MERGE') { color = 'success'; text = t('match_review.status_confirmed') || '병합 완료' }
      else if (params.value === 'REJECTED') { color = 'danger'; text = t('match_review.status_rejected') || '거절됨' }
      
      return `<span style="padding: 3px 10px; border-radius: 12px; font-size: 0.75rem; font-weight: bold; background-color: var(--va-${color}); color: white;">${text}</span>`
    }
  },
  { 
    field: 'createdAt', 
    headerName: t('schema_history.changed_at') || '생성 시각',
    width: 180,
    valueFormatter: (params: any) => params.value ? formatWithTimezone(params.value) : '-'
  }
])

const createDatasource = () => ({
  getRows: async (params: any) => {
    if (!selectedDomain.value) {
      params.successCallback([], 0)
      return
    }

    const startRow = params.startRow || 0
    const endRow = params.endRow || 20
    const size = Math.max(endRow - startRow, 1)
    const page = Math.floor(startRow / size)

    try {
      let url = `/api/domains/${selectedDomain.value}/match-candidates?page=${page}&size=${size}`
      if (statusFilter.value && statusFilter.value !== 'ALL') {
        url += `&status=${statusFilter.value}`
      }

      const res = await customFetch(url)
      const content = res?.content || res?.data || (Array.isArray(res) ? res : [])
      const totalElements = res?.totalElements !== undefined ? res.totalElements : content.length
      
      params.successCallback(content, totalElements)
    } catch (e) {
      console.error('Failed to get rows', e)
      params.failCallback()
    }
  }
})

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
    const list = res?.content || res?.data || (Array.isArray(res) ? res : [])
    domainOptions.value = list.map((d: any) => ({
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
  if (selectedDomain.value) {
    gridApi.setGridOption('datasource', createDatasource())
  }
}

const refreshGrid = () => {
  if (gridApi && selectedDomain.value) {
    gridApi.setGridOption('datasource', createDatasource())
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

const parseRecordData = (data: any) => {
  if (!data) return {}
  if (typeof data === 'string') {
    try { return JSON.parse(data) } catch (e) { return { value: data } }
  }
  return data
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
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
}
</style>
