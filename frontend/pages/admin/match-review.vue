<template>
  <div style="padding: 1.5rem; display: flex; flex-direction: column; gap: 1.25rem; height: calc(100vh - 80px); overflow: hidden;">

    <!-- 상단 타이틀 바 -->
    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); padding: 1rem 1.25rem; border-radius: 12px; border: 1px solid var(--va-background-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04);">
      <div style="display: flex; align-items: center; gap: 0.75rem;">
        <va-icon name="fact_check" size="large" color="primary" />
        <div>
          <h2 style="font-weight: 700; font-size: 1.35rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
            {{ $t('match_review.title') || '매칭 후보 검토' }}
            <va-badge text="Queue" color="primary" size="small" />
          </h2>
          <span style="font-size: 0.85rem; color: var(--va-text-secondary);">
            유사도가 높은 매칭 후보를 검토하여 마스터 데이터 병합 승인 또는 거절 처리를 진행합니다.
          </span>
        </div>
      </div>

      <div style="display: flex; gap: 0.75rem; align-items: center;">
        <va-select
          v-model="selectedDomain"
          :options="domainOptions"
          value-by="value"
          :placeholder="$t('match_review.domain_select') || '도메인 선택'"
          style="width: 220px;"
          dense
        />
        <va-button preset="outline" color="primary" icon="refresh" size="small" @click="refreshGrid">
          {{ $t('match_review.refresh') || '새로고침' }}
        </va-button>
      </div>
    </div>

    <!-- 필터 & 배치 액션 바 -->
    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); padding: 0.75rem 1.25rem; border-radius: 10px; border: 1px solid var(--va-background-border);">
      <!-- 상태 필터 탭 -->
      <div style="display: flex; align-items: center; gap: 0.75rem;">
        <span style="font-size: 0.75rem; font-weight: 700; color: var(--va-text-secondary); text-transform: uppercase; letter-spacing: 0.05em; white-space: nowrap;">
          상태 필터
        </span>
        <div style="display: flex; gap: 0.25rem; background: var(--va-background-element); padding: 3px; border-radius: 8px; border: 1px solid var(--va-background-border);">
          <va-button
            v-for="opt in statusOptions"
            :key="opt.value"
            :preset="statusFilter === opt.value ? 'primary' : 'plain'"
            :color="statusFilter === opt.value ? 'primary' : 'secondary'"
            size="small"
            style="font-weight: 600;"
            @click="statusFilter = opt.value; onStatusFilterChanged()"
          >
            {{ opt.label }}
          </va-button>
        </div>
      </div>

      <!-- 배치 액션 (행 선택 시) -->
      <div v-if="hasWritePermission" style="display: flex; align-items: center; gap: 0.5rem;">
        <template v-if="selectedRows.length > 0">
          <span style="font-size: 0.78rem; font-weight: 700; color: var(--va-primary); margin-right: 0.25rem;">
            {{ selectedRows.length }}건 선택됨
          </span>
          <va-button color="success" icon="check_circle" size="small" @click="batchConfirm">
            {{ $t('match_review.batch_confirm') || '일괄 승인' }}
          </va-button>
          <va-button color="danger" preset="secondary" icon="cancel" size="small" @click="batchReject">
            {{ $t('match_review.batch_reject') || '일괄 거절' }}
          </va-button>
        </template>
        <span v-else style="font-size: 0.75rem; color: var(--va-text-secondary); display: flex; align-items: center; gap: 4px;">
          <va-icon name="info" size="14px" />
          행을 선택하면 일괄 처리 가능
        </span>
      </div>
    </div>

    <!-- 그리드 + 사이드바 레이아웃 -->
    <div style="display: flex; gap: 1rem; flex: 1; min-height: 0; overflow: hidden;">

      <!-- AG-Grid -->
      <va-card style="flex: 1; display: flex; flex-direction: column; min-height: 0; overflow: hidden; padding: 0.5rem;">
        <div style="width: 100%; height: 100%; flex: 1;">
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
              :rowSelection="{ mode: 'multiRow', checkboxes: true, headerCheckbox: false }"
              :pagination="true"
              :paginationPageSize="20"
              :paginationPageSizeSelector="[10, 20, 50]"
              @grid-ready="onGridReady"
              @selection-changed="onSelectionChanged"
              @row-clicked="onRowClicked"
            />
          </client-only>
        </div>
      </va-card>

      <!-- 상세 비교 사이드바 -->
      <transition name="panel-slide">
        <va-card v-if="selectedCandidate" style="width: 340px; flex-shrink: 0; display: flex; flex-direction: column; overflow: hidden; padding: 0;">

          <!-- 사이드바 헤더 -->
          <div style="display: flex; align-items: center; justify-content: space-between; padding: 0.85rem 1rem; border-bottom: 1px solid var(--va-background-border);">
            <div style="display: flex; align-items: center; gap: 0.5rem; font-size: 0.88rem; font-weight: 700; color: var(--va-text-primary);">
              <va-icon name="compare_arrows" size="18px" color="primary" />
              후보 상세 비교
            </div>
            <va-badge
              :text="(selectedCandidate.score * 100).toFixed(1) + '%'"
              :color="getScoreColor(selectedCandidate.score)"
            />
          </div>

          <!-- 스크롤 영역 -->
          <div style="flex: 1; overflow-y: auto; padding: 0.75rem; display: flex; flex-direction: column; gap: 0.75rem;">

            <!-- 기존 레코드 -->
            <div style="border-radius: 8px; overflow: hidden; border: 1px solid var(--va-background-border);">
              <div style="display: flex; align-items: center; gap: 6px; padding: 0.45rem 0.75rem; font-size: 0.72rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; background: var(--va-background-element); color: var(--va-text-secondary); border-bottom: 1px solid var(--va-background-border);">
                <va-icon name="inventory_2" size="13px" color="primary" />
                {{ $t('match_review.existing_record') || '기존 마스터 레코드' }}
              </div>
              <div>
                <div
                  v-for="(val, key) in parseRecordData(selectedCandidate.existingRecord)"
                  :key="'ex-'+key"
                  style="display: flex; justify-content: space-between; align-items: baseline; padding: 0.35rem 0.75rem; font-size: 0.75rem; border-bottom: 1px solid var(--va-background-border); gap: 0.5rem;"
                >
                  <span style="font-weight: 600; color: var(--va-text-secondary); white-space: nowrap; flex-shrink: 0;">{{ key }}</span>
                  <span style="font-family: 'Fira Code', monospace; font-size: 0.72rem; color: var(--va-text-primary); word-break: break-all; text-align: right;">{{ formatValue(val) }}</span>
                </div>
              </div>
            </div>

            <!-- 신규 유입 레코드 -->
            <div style="border-radius: 8px; overflow: hidden; border: 1px solid var(--va-background-border);">
              <div style="display: flex; align-items: center; gap: 6px; padding: 0.45rem 0.75rem; font-size: 0.72rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; background: var(--va-background-element); color: var(--va-text-secondary); border-bottom: 1px solid var(--va-background-border);">
                <va-icon name="move_to_inbox" size="13px" color="info" />
                {{ $t('match_review.incoming_data') || '신규 유입 레코드' }}
              </div>
              <div>
                <div
                  v-for="(val, key) in parseRecordData(selectedCandidate.incomingData)"
                  :key="'in-'+key"
                  style="display: flex; justify-content: space-between; align-items: baseline; padding: 0.35rem 0.75rem; font-size: 0.75rem; border-bottom: 1px solid var(--va-background-border); gap: 0.5rem;"
                >
                  <span style="font-weight: 600; color: var(--va-text-secondary); white-space: nowrap; flex-shrink: 0;">{{ key }}</span>
                  <span style="font-family: 'Fira Code', monospace; font-size: 0.72rem; color: var(--va-text-primary); word-break: break-all; text-align: right;">{{ formatValue(val) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 사이드바 액션 버튼 -->
          <div v-if="hasWritePermission && selectedCandidate.status === 'PENDING_REVIEW'"
               style="display: flex; gap: 0.5rem; padding: 0.75rem; border-top: 1px solid var(--va-background-border);">
            <va-button preset="secondary" color="danger" style="flex: 1;" @click="rejectSingle(selectedCandidate)">
              <va-icon name="cancel" size="15px" />
              {{ $t('match_review.reject_new') || '거절' }}
            </va-button>
            <va-button color="success" style="flex: 1;" @click="openMergeModal(selectedCandidate)">
              <va-icon name="merge_type" size="15px" />
              {{ $t('match_review.confirm_merge') || '병합 검토' }}
            </va-button>
          </div>
        </va-card>
      </transition>
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
const domainStore = useDomain()
const selectedDomain = ref<string>('')
const domainOptions = computed(() => domainStore.domainOptions.value)
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

const fetchDomains = async () => {
  try {
    await domainStore.fetchDomains()
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
.panel-slide-enter-active { transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1); }
.panel-slide-leave-active  { transition: all 0.18s ease; }
.panel-slide-enter-from, .panel-slide-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>
