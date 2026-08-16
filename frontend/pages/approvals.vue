<template>
  <div style="display: flex; flex-direction: column; gap: 1.25rem; padding-bottom: 2rem;">
    <!-- Top Action Bar -->
    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); padding: 1rem 1.25rem; border-radius: 12px; border: 1px solid var(--va-background-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04);">
      <div style="display: flex; align-items: center; gap: 0.75rem;">
        <va-icon name="assignment_turned_in" size="large" color="primary" />
        <div>
          <h2 style="font-weight: 700; font-size: 1.35rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
            {{ pageTitle }}
            <va-badge :text="t('approval_inbox')" color="primary" size="small" />
          </h2>
          <span style="font-size: 0.85rem; color: var(--va-text-secondary);">
            {{ t('subtitle') }}
          </span>
        </div>
      </div>

      <div style="display: flex; gap: 0.75rem; align-items: center;">
        <va-button preset="outline" color="primary" icon="how_to_reg" size="small" @click="showDelegationModal = true">
          {{ t('approval_delegation') }}
        </va-button>
      </div>
    </div>
    
    <!-- Card 1: Pending Approvals -->
    <va-card style="border-radius: 12px; border: 1px solid var(--va-background-border); overflow: hidden;">
      <div style="padding: 0.85rem 1.25rem; border-bottom: 1px solid var(--va-background-border); display: flex; justify-content: space-between; align-items: center; background: var(--va-background-element);">
        <div style="display: flex; align-items: center; gap: 0.6rem;">
          <va-icon name="how_to_reg" color="primary" />
          <span style="font-size: 1.05rem; font-weight: 700; color: var(--va-text-primary); font-family: 'Pretendard', 'Inter', sans-serif;">
            {{ t('pending_approvals') }}
          </span>
          <va-chip size="small" color="warning" style="font-weight: 600;">{{ t('pending_count', { count: pendingSteps.length }) }}</va-chip>
        </div>
        <div style="display: flex; gap: 0.5rem; align-items: center;">
          <va-button size="small" preset="primary" icon="check_circle" @click="bulkApprove" :disabled="!pendingSelectedRows.length">
            {{ t('bulk_approve', { count: pendingSelectedRows.length }) }}
          </va-button>
          <va-button size="small" preset="primary" color="danger" icon="cancel" @click="bulkReject" :disabled="!pendingSelectedRows.length">
            {{ t('bulk_reject', { count: pendingSelectedRows.length }) }}
          </va-button>
          <va-button preset="plain" color="secondary" size="small" icon="refresh" @click="refreshPendingRequests">{{ t('refresh') }}</va-button>
        </div>
      </div>

      <div :class="{ 'ag-theme-quartz-dark': isDark }" style="height: 320px; width: 100%;">
        <AgGridVue
          key="pending-grid"
          id="pending-grid"
          style="width: 100%; height: 100%;"
          :theme="gridTheme"
          :autoSizeStrategy="autoSizeStrategy"
          :gridOptions="pendingGridOptions"
          @grid-ready="onPendingGridReady"
          :overlayNoRowsTemplate="`<span style='padding: 8px 16px; border: 1px solid var(--va-background-border); background: var(--va-background-element); color: var(--va-text-secondary); border-radius: 6px; font-size: 0.85rem;'>${t('noRequests')}</span>`"
        />
      </div>
    </va-card>

    <!-- Action Modal for Pending Step -->
    <ApprovalsApprovalActionModal
      v-model="showActionModal"
      :selected-pending-step="selectedPendingStep"
      :comment-data="commentData"
      :is-dark="isDark"
      @single-action="handleSingleAction"
    />

    <!-- Card 2: My Submitted Requests -->
    <va-card style="border-radius: 12px; border: 1px solid var(--va-background-border); overflow: hidden;">
      <div style="padding: 0.85rem 1.25rem; border-bottom: 1px solid var(--va-background-border); display: flex; justify-content: space-between; align-items: center; background: var(--va-background-element);">
        <div style="display: flex; align-items: center; gap: 0.6rem;">
          <va-icon name="outbox" color="primary" />
          <span style="font-size: 1.05rem; font-weight: 700; color: var(--va-text-primary); font-family: 'Pretendard', 'Inter', sans-serif;">
            {{ t('mySubmitted') }}
          </span>
          <va-chip size="small" color="primary" style="font-weight: 600;">{{ t('item_count', { count: myRequests.length }) }}</va-chip>
        </div>
        <div style="display: flex; gap: 0.5rem; align-items: center;">
          <va-button preset="plain" color="secondary" size="small" icon="refresh" @click="refreshMyRequests">{{ t('refresh') }}</va-button>
        </div>
      </div>

      <div :class="{ 'ag-theme-quartz-dark': isDark }" style="height: 320px; width: 100%;">
        <AgGridVue
          key="my-requests-grid"
          id="my-requests-grid"
          style="width: 100%; height: 100%;"
          :theme="gridTheme"
          :autoSizeStrategy="autoSizeStrategy"
          :gridOptions="myRequestsGridOptions"
          @grid-ready="onMyRequestsGridReady"
          :overlayNoRowsTemplate="`<span style='padding: 8px 16px; border: 1px solid var(--va-background-border); background: var(--va-background-element); color: var(--va-text-secondary); border-radius: 6px; font-size: 0.85rem;'>${t('noSubmitted')}</span>`"
        />
      </div>
    </va-card>

    <ApprovalsApprovalDetailsModal
      v-model="showDetailsModal"
      :selected-request="selectedRequest"
    />

    <ApprovalDelegationModal
      v-model="showDelegationModal"
    />
  </div>
</template>

<script setup>
import { useColors } from 'vuestic-ui'
import { useI18n } from 'vue-i18n'
import { usePageTitle } from '~/composables/usePageTitle'
import { useApprovalEnricher } from '~/composables/useApprovalEnricher'
import { usePermission } from '~/composables/usePermission'
import { useUserStore } from '~/stores/useUserStore'
import { useRoleStore } from '~/stores/useRoleStore'
import ApprovalDetailsViewer from '~/components/ApprovalDetailsViewer.vue'
import ApprovalDelegationModal from '~/components/approvals/ApprovalDelegationModal.vue'

const { pageTitle } = usePageTitle('approvals_title', '결재 및 승인 관리')

const showDelegationModal = ref(false)

const { hasPermission } = usePermission()

const colors = useColors()
const isDark = computed(() => colors?.currentPresetName?.value === 'dark')

const messages = {
  ko: {
    title: '내 결재 요청 및 승인함',
    pending_approvals: '승인 대기 결재 목록',
    subtitle: '본인이 제출한 결재 요청 목록 및 승인 대기 중인 결재 건을 확인하고 일괄 처리합니다.',
    refresh: '새로고침',
    allDone: '모든 결재/합의가 완료되었습니다.',
    noRequests: '현재 대기 중인 요청이 없습니다.',
    consensus: '합의',
    finalApproval: '최종 결재',
    step: '단계',
    requestedBy: '요청자',
    requestedData: '요청 데이터:',
    noParsable: '파싱 가능한 데이터가 없습니다.',
    addComment: '의견 추가 (선택)...',
    approve: '승인',
    reject: '반려',
    mySubmitted: '내가 올린 결재 상신 내역',
    noSubmitted: '상신한 요청이 없습니다.',
    request: '요청',
    created: '생성일',
    details: '상세 내용',
    action_title: '결재 처리',
    review: '심사하기',
    view_approval_history: '결재 이력 보기',
    approval_history: '결재 이력',
    bulk_approve: '선택 승인 ({count})',
    bulk_reject: '선택 반려 ({count})',
    bulk_approve_confirm: '선택한 {count}건을 일괄 승인하시겠습니까?',
    bulk_reject_confirm: '선택한 {count}건을 일괄 반려하시겠습니까?',
    bulk_approve_loading: '일괄 승인 처리 중입니다...',
    bulk_reject_loading: '일괄 반려 처리 중입니다...',
    processing: '처리 중입니다...',
    approval_line: '결재라인',
    target_type: '대상 타입',
    step_type: '단계 타입',
    status: '상태',
    action: '액션',
    status_draft: '상신완료',
    status_approved: '승인',
    status_rejected: '반려',
    status_pending: '진행중',
    status_waiting: '대기',
    no_approval_line: '결재라인이 없습니다.',
    id: 'ID',
    record_create: '신규 생성',
    record_update: '데이터 수정',
    record_delete: '데이터 삭제',
    domain_record_create: '도메인 데이터 생성',
    other_request: '기타 요청',
    draft: '기안',
    approval: '결재',
    processed: '처리됨',
    no_comment: '의견 없음',
    observers: '통보자(참조)',
    close: '닫기',
    general: '일반',
    fields: '필드',
    summary: '요약',
    approval_line_status: '결재선 현황',
    systemApplied: '시스템 반영',
    systemComplete: '완료',
    systemCancelled: '취소됨',
    stepScheduled: '예정',
    colDomain: '도메인',
    colClassification: '분류',
    colIdAttr: 'ID 속성',
    colNameAttr: '이름 속성',
    colSummary: '요약',
    action_approve: '승인',
    action_reject: '반려',
    action_processing: '{action} 처리 중입니다...',
    action_success: '결재가 성공적으로 {action} 되었습니다.'
  },
  en: {
    title: 'My Pending Approvals',
    pending_approvals: 'Pending Approvals',
    subtitle: 'View and batch process your submitted approval requests and pending approval items.',
    refresh: 'Refresh',
    allDone: 'All approvals/consensus are completed.',
    noRequests: 'There are no pending requests.',
    consensus: 'Consensus',
    finalApproval: 'Final Approval',
    step: 'Step',
    requestedBy: 'Requested by',
    requestedData: 'Requested Data:',
    noParsable: 'No parsable data provided.',
    addComment: 'Add a comment (optional)...',
    approve: 'Approve',
    reject: 'Reject',
    mySubmitted: 'My Submitted Requests',
    noSubmitted: 'You have not submitted any requests.',
    request: 'Request',
    created: 'Created',
    details: 'Details',
    action_title: 'Process Request',
    review: 'Review',
    view_approval_history: 'View History',
    approval_history: 'History',
    bulk_approve: 'Approve Selected ({count})',
    bulk_reject: 'Reject Selected ({count})',
    bulk_approve_confirm: 'Are you sure you want to approve the selected {count} requests?',
    bulk_reject_confirm: 'Are you sure you want to reject the selected {count} requests?',
    bulk_approve_loading: 'Processing bulk approval...',
    bulk_reject_loading: 'Processing bulk rejection...',
    processing: 'Processing...',
    approval_line: 'Approval Line',
    target_type: 'Target Type',
    step_type: 'Step Type',
    status: 'Status',
    action: 'Action',
    status_draft: 'Drafted',
    status_approved: 'Approved',
    status_rejected: 'Rejected',
    status_pending: 'Pending',
    status_waiting: 'Waiting',
    no_approval_line: 'No Approval Line',
    id: 'ID',
    record_create: 'New Record',
    record_update: 'Data Update',
    record_delete: 'Data Delete',
    domain_record_create: 'Domain Record Create',
    other_request: 'Other Request',
    draft: 'Draft',
    approval: 'Approval',
    processed: 'Processed',
    no_comment: 'No comment',
    observers: 'Observers (CC)',
    close: 'Close',
    general: 'General',
    fields: 'Fields',
    summary: 'Summary',
    approval_line_status: 'Approval Line Status',
    systemApplied: 'System Reflect',
    systemComplete: 'Complete',
    systemCancelled: 'Cancelled',
    stepScheduled: 'Scheduled',
    colDomain: 'Domain',
    colClassification: 'Classification',
    colIdAttr: 'ID Value',
    colNameAttr: 'Name Value',
    colSummary: 'Summary',
    action_approve: 'Approved',
    action_reject: 'Rejected',
    action_processing: 'Processing {action}...',
    action_success: 'Request has been successfully {action}.'
  }
}
const { t, locale } = useI18n({ messages, useScope: 'local', inheritLocale: true })

import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useCookie } from '#app'
import { useToast } from 'vuestic-ui'
import { AgGridVue } from 'ag-grid-vue3'
import { formatApprovalCode } from '../utils/formatters'


const route = useRoute()
const { loadMetadata, enrichRequest } = useApprovalEnricher()
const { formatMultilingual } = useMultilingual()

const { gridTheme, autoSizeStrategy } = useAgGridTheme()


const { init } = useToast()
const pendingSteps = ref([])
const showActionModal = ref(false)
const selectedPendingStep = ref(null)
const pendingSelectedRows = ref([])

const getPendingColumnDefs = () => [
  { colId: 'p_checkbox', headerName: '', field: 'checkbox', width: 50, suppressSizeToFit: true },
  { colId: 'p_targetType', field: 'approvalRequest.targetType', headerName: t('target_type'), width: 130, minWidth: 120 },
  { colId: 'p_domainName', field: 'approvalRequest.domainName', headerName: t('colDomain'), width: 140, valueFormatter: params => formatMultilingual(params.value) },
  { colId: 'p_classificationName', field: 'approvalRequest.classificationName', headerName: t('colClassification'), width: 150, valueFormatter: params => formatMultilingual(params.value) },
  { colId: 'p_idAttribute', field: 'approvalRequest.idAttribute', headerName: t('colIdAttr'), width: 150, valueFormatter: params => formatMultilingual(params.value) },
  { colId: 'p_nameAttribute', field: 'approvalRequest.nameAttribute', headerName: t('colNameAttr'), width: 180, valueFormatter: params => formatMultilingual(params.value) },
  { colId: 'p_summary', field: 'approvalRequest.summary', headerName: t('colSummary'), flex: 1, minWidth: 200, tooltipField: 'approvalRequest.summary' },
  { 
    colId: 'p_steps',
    field: 'approvalRequest.steps', 
    headerName: t('approval_line'), 
    flex: 3, 
    minWidth: 300,
    valueFormatter: params => getApprovalLineString(params.value)
  },
  { colId: 'p_createdAt', field: 'createdAt', headerName: t('created'), width: 180, minWidth: 150, valueFormatter: params => params.value ? formatDate(params.value) : '' },
  { colId: 'p_stepType', field: 'stepType', headerName: t('step_type'), width: 120, minWidth: 100 },
  {
    colId: 'p_action',
    headerName: t('action'),
    field: 'id',
    width: 120,
    minWidth: 100,
    suppressSizeToFit: true,
    cellRenderer: (params) => {
      const eDiv = document.createElement('div')
      const eButton = document.createElement('button')
      eButton.innerHTML = t('review')
      eButton.style.padding = '4px 12px'
      eButton.style.cursor = 'pointer'
      eButton.style.background = '#154ec1'
      eButton.style.color = 'white'
      eButton.style.border = 'none'
      eButton.style.borderRadius = '4px'
      eButton.onclick = () => {
        console.log("Review button clicked!", params.data)
        try {
          selectedPendingStep.value = params.data
          showActionModal.value = true
        } catch (err) {
          console.error("Error opening modal:", err)
        }
      }
      eDiv.appendChild(eButton)
      return eDiv
    }
  }
]

const pendingColumnDefs = ref(getPendingColumnDefs())

const { showLoading, hideLoading } = useLoading()
const pendingGridApi = ref(null)
const myRequestsGridApi = ref(null)



const checkQueryForModal = async () => {
  const reqId = route.query.requestId || route.query.id
  if (!reqId || typeof reqId !== 'string') return
  try {
    const fullReq = await $fetch(`/api/approval-requests/${reqId}`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    if (!fullReq) return
    
    const enriched = await enrichRequest(fullReq)
    const userObj = userCookie.value ? (typeof userCookie.value === 'object' ? userCookie.value : JSON.parse(userCookie.value)) : null
    const myUserRoles = Array.isArray(userObj?.roles) ? userObj.roles : (userObj?.role ? [userObj.role] : [])
    
    const currentPendingStep = (enriched.steps || []).find(s => {
      if (s.status !== 'PENDING') return false
      if (myUuid.value && s.assigneeId && String(s.assigneeId) === String(myUuid.value)) return true
      if (hasPermission('admin:write')) return true // Admin can proxy approve any pending step
      if (s.assigneeRole) {
        const stepRole = String(s.assigneeRole).toUpperCase()
        if (myUserRoles.some(r => String(r).toUpperCase() === stepRole)) return true
      }
      return false
    })

    if (currentPendingStep && enriched.status === 'PENDING') {
      currentPendingStep.approvalRequest = enriched
      selectedPendingStep.value = currentPendingStep
      showActionModal.value = true
    } else {
      selectedRequest.value = enriched
      showDetailsModal.value = true
    }
  } catch (e) {
    console.error('Failed to open requested modal from URL query:', e)
  }
}

const handleApprovalUpdated = () => {
  loadRequests()
}

onMounted(() => {
  checkQueryForModal()
  if (process.client) {
    window.addEventListener('approval-updated', handleApprovalUpdated)
  }
})

onUnmounted(() => {
  if (process.client) {
    window.removeEventListener('approval-updated', handleApprovalUpdated)
  }
})

watch(() => route.query.requestId, () => {
  checkQueryForModal()
})

const onPendingGridReady = (params) => {
  pendingGridApi.value = params.api
  refreshPendingRequests()
}

const onMyRequestsGridReady = (params) => {
  myRequestsGridApi.value = params.api
  refreshMyRequests()
}

const pendingGridOptions = ref({
  rowModelType: 'infinite',
  cacheBlockSize: 100,
  columnDefs: getPendingColumnDefs(),
  rowSelection: { mode: 'multiRow', checkboxes: true, headerCheckbox: false },
  enableCellTextSelection: true,
  ensureDomOrder: true,
  onSelectionChanged: (event) => {
    pendingSelectedRows.value = event.api.getSelectedRows()
  }
})

const bulkApprove = async () => {
  if (!pendingSelectedRows.value.length) return
  showLoading(t('bulk_approve_loading'))
  try {
    await Promise.all(pendingSelectedRows.value.map(row => handleAction(row.id, 'approve', true)))
    pendingSelectedRows.value = []
    await loadRequests()
  } finally {
    hideLoading()
  }
}

const bulkReject = async () => {
  if (!pendingSelectedRows.value.length) return
  showLoading(t('bulk_reject_loading'))
  try {
    await Promise.all(pendingSelectedRows.value.map(row => handleAction(row.id, 'reject', true)))
    pendingSelectedRows.value = []
    await loadRequests()
  } finally {
    hideLoading()
  }
}

const handleSingleAction = async (stepId, action) => {
  try {
    await handleAction(stepId, action)
  } catch (e) {
    console.error('Error handling single approval action:', e)
  } finally {
    showActionModal.value = false
  }
}
const myRequests = ref([])
const commentData = ref({})
const fieldNameMap = ref({})
const userList = ref([])
const currentLocale = useCookie('locale', { default: () => 'ko' })
const token = useCookie('auth_token')
const userCookie = useCookie('user_data')



const currentUser = computed(() => {
  if (userCookie.value) {
    return typeof userCookie.value === 'string' ? JSON.parse(userCookie.value) : userCookie.value
  }
  return null
})

const domainRefDisplayMap = ref({})
const showDetailsModal = ref(false)
const selectedRequest = ref(null)

const getMyRequestsColumnDefs = () => [
  { colId: 'm_id', field: 'id', headerName: t('id'), width: 110, minWidth: 90, valueFormatter: params => formatApprovalCode(params.value) },
  { colId: 'm_targetType', field: 'targetType', headerName: t('target_type'), width: 130, minWidth: 120 },

  { colId: 'm_domainName', field: 'domainName', headerName: t('colDomain'), width: 140, valueFormatter: params => formatMultilingual(params.value) },
  { colId: 'm_classificationName', field: 'classificationName', headerName: t('colClassification'), width: 150, valueFormatter: params => formatMultilingual(params.value) },
  { colId: 'm_idAttribute', field: 'idAttribute', headerName: t('colIdAttr'), width: 150, valueFormatter: params => formatMultilingual(params.value) },
  { colId: 'm_nameAttribute', field: 'nameAttribute', headerName: t('colNameAttr'), width: 180, valueFormatter: params => formatMultilingual(params.value) },
  { colId: 'm_summary', field: 'summary', headerName: t('colSummary'), flex: 1, minWidth: 200, tooltipField: 'summary' },
  { 
    colId: 'm_steps',
    field: 'steps', 
    headerName: t('approval_line'), 
    flex: 3, 
    minWidth: 300,
    valueFormatter: params => getApprovalLineString(params.value)
  },
  { colId: 'm_createdAt', field: 'createdAt', headerName: t('created'), width: 180, minWidth: 150, valueFormatter: params => params.value ? formatDate(params.value) : '' },
  { colId: 'm_status', field: 'status', headerName: t('status'), width: 110, minWidth: 90, 
    cellRenderer: params => {
      if (!params || !params.value) return '';
      const color = params.value === 'PENDING' ? '#fbbf24' : (params.value === 'APPROVED' ? '#22c55e' : '#ef4444');
      const span = document.createElement('span');
      span.style.color = 'white';
      span.style.backgroundColor = color;
      span.style.padding = '2px 8px';
      span.style.borderRadius = '4px';
      span.style.fontSize = '0.8rem';
      span.style.fontWeight = 'bold';
      span.innerText = params.value;
      return span;
    }
  },
  {
    colId: 'm_history',
    headerName: t('approval_history'),
    field: 'id',
    width: 130,
    minWidth: 110,
    suppressSizeToFit: true,
    cellRenderer: (params) => {
      const eDiv = document.createElement('div')
      const eButton = document.createElement('button')
      eButton.innerHTML = t('view_approval_history')
      eButton.style.padding = '4px 12px'
      eButton.style.cursor = 'pointer'
      eButton.style.background = '#eef2f5'
      eButton.style.color = '#154ec1'
      eButton.style.border = '1px solid #154ec1'
      eButton.style.borderRadius = '4px'
      eButton.onclick = () => {
        selectedRequest.value = params.data
        showDetailsModal.value = true
      }
      eDiv.appendChild(eButton)
      return eDiv
    }
  }
]

const myRequestsColumnDefs = ref(getMyRequestsColumnDefs())

watch(locale, () => {
  if (pendingGridApi.value) {
    pendingGridApi.value.setGridOption('columnDefs', getPendingColumnDefs())
    pendingGridApi.value.setGridOption('datasource', createPendingDatasource())
  }
  if (myRequestsGridApi.value) {
    myRequestsGridApi.value.setGridOption('columnDefs', getMyRequestsColumnDefs())
    myRequestsGridApi.value.setGridOption('datasource', createMyRequestsDatasource())
  }
})

const myRequestsGridOptions = ref({
  rowModelType: 'infinite',
  cacheBlockSize: 100,
  columnDefs: getMyRequestsColumnDefs(),
  rowSelection: { mode: 'singleRow' },
  enableCellTextSelection: true,
  ensureDomOrder: true
})

const fetchDomainRefName = async (uuid, targetDomainId) => {
  if (!uuid || domainRefDisplayMap.value[uuid]) return;
  domainRefDisplayMap.value[uuid] = 'Loading...'; // Placeholder
  try {
    const rec = await $fetch(`/api/records/${uuid}`, { headers: { Authorization: `Bearer ${token.value}` } }).catch(() => null);
    
    if (!rec) {
      // Maybe it's a user UUID somehow?
      const uname = getUserName(uuid);
      domainRefDisplayMap.value[uuid] = (uname && uname !== uuid) ? uname : uuid;
      return;
    }
    
    let tDomainId = targetDomainId;
    if (!tDomainId && rec.node) {
      tDomainId = rec.node.domain?.id || rec.node.domainId;
    }
    if (!tDomainId) tDomainId = rec.domainId;
    
    if (!tDomainId) {
      domainRefDisplayMap.value[uuid] = uuid;
      return;
    }

    const domains = await $fetch('/api/domains', { headers: { Authorization: `Bearer ${token.value}` } })
    const tDomain = domains.find(d => d.id === tDomainId)
    if (!tDomain) {
      domainRefDisplayMap.value[uuid] = uuid;
      return;
    }
    const dFieldId = tDomain.displayNameFieldId || tDomain.identifierFieldId
    const tFields = await $fetch(`/api/domains/${tDomainId}/fields`, { headers: { Authorization: `Bearer ${token.value}` } })
    let f = tFields.find(x => x.id === dFieldId);
    if (!f) {
      f = tFields.find(x => {
        const n = JSON.stringify(x.name).toLowerCase();
        return n.includes('name') || n.includes('\uC774\uB984') || n.includes('\uC0AC\uC6D0\uBA85') || n.includes('title') || n.includes('\uC81C\uBAA9');
      });
      if (!f) f = tFields.find(x => x.type === 'TEXT');
    }
    
    if (f && rec.data) {
      const dataObj = typeof rec.data === 'string' ? JSON.parse(rec.data) : rec.data;
      const rawVal = dataObj[f.key];
      if (rawVal) {
        let displayStr = rawVal;
        if (typeof rawVal === 'string') {
          try {
            const parsed = JSON.parse(rawVal);
            if (parsed && typeof parsed === 'object') {
              displayStr = parsed[currentLocale.value] || parsed.ko || parsed.en || rawVal;
            }
          } catch(e) {}
        } else if (typeof rawVal === 'object') {
          displayStr = rawVal[currentLocale.value] || rawVal.ko || rawVal.en || JSON.stringify(rawVal);
        }
        domainRefDisplayMap.value[uuid] = displayStr;
      } else {
        domainRefDisplayMap.value[uuid] = uuid;
      }
    } else {
      domainRefDisplayMap.value[uuid] = uuid;
    }
  } catch (e) {
    const uname = getUserName(uuid);
    domainRefDisplayMap.value[uuid] = (uname && uname !== uuid) ? uname : uuid;
  }
}

const parseJwtUserId = (tStr) => {
  if (!tStr) return null
  try {
    const base64Url = tStr.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''))
    const parsed = JSON.parse(jsonPayload)
    return parsed.userId || parsed.uuid || parsed.username || parsed.sub || null
  } catch {
    return null
  }
}

const myUuid = computed(() => {
  const u = currentUser.value
  let uid = u?.id || u?.userId || u?.uuid || u?.username
  if (!uid || uid === 'test-admin-uuid' || uid === '935102dc-bccf-47e0-8d65-0d883186472f') {
     uid = parseJwtUserId(token.value) || ''
  }
  return uid
})

const getParsedChanges = (changesString) => {
  if (!changesString) return null
  if (typeof changesString === 'object') return changesString
  try {
    let parsed = JSON.parse(changesString)
    if (typeof parsed === 'string') parsed = JSON.parse(parsed)
    if (typeof parsed === 'string') parsed = JSON.parse(parsed)
    if (Object.keys(parsed || {}).length === 0) return null
    return parsed
  } catch (e) {
    console.error('Failed to parse changes:', e, changesString)
    return null
  }
}

const parseDate = (dateString) => {
  if (!dateString) return null
  let str = String(dateString).trim()
  if (/^\d+$/.test(str)) {
    return new Date(parseInt(str, 10))
  }
  if (!str.endsWith('Z') && !str.includes('+') && !/[-+]\d{2}:\d{2}$/.test(str)) {
    if (str.includes(' ') && !str.includes('T')) {
      str = str.replace(' ', 'T')
    }
    const serverOffset = useCookie('server_offset', { default: () => '+09:00' }).value
    str += serverOffset
  }
  const d = new Date(str)
  return isNaN(d.getTime()) ? new Date(dateString) : d
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = parseDate(dateString)
  if (!date) return ''
  const tz = useCookie('timezone', { default: () => 'Asia/Seoul' }).value
  const formatted = date.toLocaleString(undefined, { timeZone: tz })
  return formatted.replace(/\s*(GMT|UTC|KST|PST|EST|CET)[-+0-9:]*/gi, '').trim()
}

const getFilesList = (v) => {
  if (!v) return []
  if (typeof v === 'string') {
    if (v.startsWith('[')) {
      try {
        const arr = JSON.parse(v)
        if (Array.isArray(arr)) {
          return arr
        }
      } catch (e) {
        // ignore
      }
    }
    return [v]
  }
  return []
}

const getFileName = (url) => {
  if (!url) return ''
  if (typeof url !== 'string') return 'Unknown File'
  try {
    if (url.includes('?name=')) {
      const qs = url.split('?name=')[1]
      return decodeURIComponent(qs.split('&')[0])
    }
    const parts = url.split('/')
    let name = parts[parts.length - 1]
    if (name.includes('?')) {
      name = name.split('?')[0]
    }
    return decodeURIComponent(name)
  } catch (e) {
    return url
  }
}

const getFieldName = (key) => {
  const f = fieldNameMap.value[key]
  if (!f) return key;
  const nameObj = f.name
  if (!nameObj) return key;
  if (typeof nameObj === 'object') {
    return nameObj[currentLocale.value] || nameObj.ko || nameObj.en || key;
  }
  return nameObj;
}

const getGroupedChangesList = (changesString, targetType) => {
  const parsed = getParsedChanges(changesString)
  if (!parsed) return []
  
  const map = new Map()
  
  let keysToProcess = []
  if (targetType === 'RECORD_UPDATE') {
    const beforeKeys = Object.keys(parsed.before || {})
    const afterKeys = Object.keys(parsed.after || {})
    keysToProcess = [...new Set([...beforeKeys, ...afterKeys])]
  } else {
    keysToProcess = Object.keys(parsed)
  }
  
  keysToProcess.forEach(key => {
    let valBefore = null
    let valAfter = null
    if (targetType === 'RECORD_UPDATE') {
      valBefore = (parsed.before || {})[key]
      valAfter = (parsed.after || {})[key]
    } else {
      valAfter = parsed[key]
    }
    
    const f = fieldNameMap.value[key] || { name: key, fieldGroup: null }
    
    const parseName = (nameObj) => {
      if (!nameObj) return null;
      if (typeof nameObj === 'string') { try { return JSON.parse(nameObj) } catch(e){ return null } }
      return nameObj
    }
    const translate = (nameObj, defaultKo, defaultEn) => {
      const p = parseName(nameObj)
      if (!p) return currentLocale.value === 'ko' ? defaultKo : defaultEn
      return p[currentLocale.value] || p.ko || p.en || (currentLocale.value === 'ko' ? defaultKo : defaultEn)
    }
    
    const sObj = f.fieldGroup?.sector
    const gObj = f.fieldGroup

    const sName = translate(sObj?.name, t('general'), 'General')
    const sKey = sObj?.id || 'default'
    const sOrder = sObj?.sortOrder || 0
    
    const gName = translate(gObj?.name, t('fields'), 'Fields')
    const gKey = gObj?.id || 'default'
    const gOrder = gObj?.sortOrder || 0
    
    if (!map.has(sKey)) {
      map.set(sKey, { key: sKey, label: sName, order: sOrder, groups: new Map() })
    }
    const sectorObj = map.get(sKey)
    
    if (!sectorObj.groups.has(gKey)) {
      sectorObj.groups.set(gKey, { key: gKey, label: gName, order: gOrder, fields: [] })
    }
    
    let displayValBefore = valBefore;
    let displayValAfter = valAfter;
    
    if (f.type === 'DOMAIN_REFERENCE') {
      let tDomainId = null
      try { tDomainId = JSON.parse(f.options || '{}').targetDomainId } catch(e){}
      if (targetType === 'RECORD_UPDATE') {
        if (valBefore && !domainRefDisplayMap.value[valBefore]) fetchDomainRefName(valBefore, tDomainId);
        if (valAfter && !domainRefDisplayMap.value[valAfter]) fetchDomainRefName(valAfter, tDomainId);
        displayValBefore = domainRefDisplayMap.value[valBefore] || valBefore;
        displayValAfter = domainRefDisplayMap.value[valAfter] || valAfter;
      } else {
        if (valAfter && !domainRefDisplayMap.value[valAfter]) fetchDomainRefName(valAfter, tDomainId);
        displayValAfter = domainRefDisplayMap.value[valAfter] || valAfter;
      }
    } else if (f.type === 'MULTILINGUAL') {
      try {
        const parseMultilingual = (val) => {
          if (!val) return val;
          let obj = val;
          if (typeof val === 'string') {
            try {
              obj = JSON.parse(val);
            } catch (e) {
              return val;
            }
          }
          if (typeof obj === 'object') {
            const isEmpty = Object.values(obj).every(v => !v || String(v).trim() === '');
            if (isEmpty) return '-';
            const koStr = obj.ko ? `[KR] ${obj.ko}` : '';
            const enStr = obj.en ? `[EN] ${obj.en}` : '';
            if (koStr && enStr) return `${koStr} / ${enStr}`;
            return koStr || enStr || JSON.stringify(obj);
          }
          return val;
        };

        if (targetType === 'RECORD_UPDATE') {
          displayValBefore = parseMultilingual(valBefore);
          displayValAfter = parseMultilingual(valAfter);
        } else {
          displayValAfter = parseMultilingual(valAfter);
        }
      } catch (e) {}
    } else if (['SELECT', 'MULTI_SELECT'].includes(f.type) && f.options) {
      try {
        const opts = JSON.parse(f.options);
        const mapVal = (v) => {
          if (!v) return v;
          const found = opts.find(o => o.key === v);
          if (found && found.label) {
            return found.label[currentLocale.value] || found.label.ko || found.label.en || v;
          }
          return v;
        };
        
        if (targetType === 'RECORD_UPDATE') {
          if (Array.isArray(valBefore)) displayValBefore = valBefore.map(mapVal).join(', ');
          else displayValBefore = mapVal(valBefore);
          
          if (Array.isArray(valAfter)) displayValAfter = valAfter.map(mapVal).join(', ');
          else displayValAfter = mapVal(valAfter);
        } else {
          if (Array.isArray(valAfter)) displayValAfter = valAfter.map(mapVal).join(', ');
          else displayValAfter = mapVal(valAfter);
        }
      } catch(e) {}
    }
    
    if (typeof displayValBefore === 'string') {
      const uName = getUserName(displayValBefore);
      if (uName && uName !== displayValBefore) displayValBefore = uName;
    }
    if (typeof displayValAfter === 'string') {
      const uName = getUserName(displayValAfter);
      if (uName && uName !== displayValAfter) displayValAfter = uName;
    }
    
    // Add thousand separator formatting for numeric fields
    if (f && ['NUMBER', 'INTEGER', 'DECIMAL', 'CALCULATED'].includes(f.type)) {
      if (displayValBefore !== null && displayValBefore !== undefined && displayValBefore !== '' && displayValBefore !== '-') {
        const numBefore = Number(displayValBefore);
        if (!isNaN(numBefore)) {
          displayValBefore = numBefore.toLocaleString('ko-KR');
          if (f.unit) displayValBefore += ` ${f.unit}`;
        }
      }
      if (displayValAfter !== null && displayValAfter !== undefined && displayValAfter !== '' && displayValAfter !== '-') {
        const numAfter = Number(displayValAfter);
        if (!isNaN(numAfter)) {
          displayValAfter = numAfter.toLocaleString('ko-KR');
          if (f.unit) displayValAfter += ` ${f.unit}`;
        }
      }
    }

    let finalVal = null;
    if (targetType === 'RECORD_UPDATE') {
      const vBefore = displayValBefore || '-';
      const vAfter = displayValAfter || '-';
      finalVal = {
        before: vBefore,
        after: vAfter,
        isChanged: String(vBefore) !== String(vAfter)
      }
    } else {
      finalVal = displayValAfter || '-'
    }
    
    sectorObj.groups.get(gKey).fields.push({ key, label: translate(f.name, key, key), val: finalVal, type: f.type })
  })
  
  const sectorsArray = Array.from(map.values())
  let sectors = sectorsArray
  
  if (targetType === 'RECORD_UPDATE') {
    sectors.forEach(s => {
      s.groups.forEach(g => {
        g.fields = g.fields.filter(f => f.val && f.val.isChanged)
      })
      Array.from(s.groups.keys()).forEach(k => {
        if (s.groups.get(k).fields.length === 0) {
          s.groups.delete(k)
        }
      })
    })
    sectors = sectors.filter(s => s.groups.size > 0)
  }

  sectors.sort((a, b) => a.order - b.order)
  
  return sectors.map(s => {
    const groups = Array.from(s.groups.values())
    groups.sort((a, b) => a.order - b.order)
    return {
      key: s.key,
      label: s.label,
      groups: groups
    }
  })
}

const getGroupedSteps = (request) => {
  if (!request) return []
  const steps = request.steps || []
  
  const map = new Map()

  const hasDraftStep = steps.some(s => s.stepOrder === 0 || s.stepType === 'DRAFT')
  
  if (!hasDraftStep && request.requesterId) {
    map.set(0, {
      order: 0,
      steps: [{
        id: 'draft-step-' + request.id,
        stepType: 'DRAFT',
        stepOrder: 0,
        assigneeId: request.requesterId,
        status: 'SUBMITTED',
        updatedAt: request.createdAt,
        comment: '' 
      }]
    })
  }

  if (steps.length > 0) {
    steps.forEach(s => {
      if (!map.has(s.stepOrder)) {
        map.set(s.stepOrder, { order: s.stepOrder, steps: [] })
      }
      map.get(s.stepOrder).steps.push(s)
    })
  }
  
  const result = Array.from(map.values())
  result.sort((a, b) => a.order - b.order)
  return result
}

const getObserversList = (obsString) => {
  if (!obsString) return []
  try {
    const parsed = JSON.parse(obsString)
    return Array.isArray(parsed) ? parsed : []
  } catch (e) {
    return []
  }
}

const getApprovalLineString = (steps) => {
  if (!steps || steps.length === 0) return t('no_approval_line');
  const sortedSteps = [...steps].sort((a, b) => a.stepOrder - b.stepOrder);
  const stepStrings = sortedSteps.map(s => {
    const name = s.assigneeName || getUserName(s.assigneeId);
    let statusText = '';
    if (s.stepType === 'DRAFT') statusText = t('draft');
    else if (s.status === 'APPROVED') statusText = t('status_approved');
    else if (s.status === 'REJECTED') statusText = t('status_rejected');
    else if (s.status === 'PENDING') statusText = t('status_pending');
    else statusText = t('status_waiting');
    return `${name}(${statusText})`;
  });
  return stepStrings.join(' ➔ ');
}
const formatStepDate = (dateString) => {
  if (!dateString) return '';
  const date = parseDate(dateString);
  if (!date) return '';
  const tz = useCookie('timezone', { default: () => 'Asia/Seoul' }).value;
  try {
    const formatter = new Intl.DateTimeFormat('ko-KR', {
      timeZone: tz,
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    });
    const parts = formatter.formatToParts(date);
    const getPart = (type) => parts.find(p => p.type === type).value;

    const tzFormatter = new Intl.DateTimeFormat('en-US', {
      timeZone: tz,
      year: 'numeric', month: 'numeric', day: 'numeric',
      hour: 'numeric', minute: 'numeric', hour12: false
    });
    const tzParts = tzFormatter.formatToParts(date);
    const getTzVal = (type) => parseInt(tzParts.find(p => p.type === type).value, 10);

    const utcFormatter = new Intl.DateTimeFormat('en-US', {
      timeZone: 'UTC',
      year: 'numeric', month: 'numeric', day: 'numeric',
      hour: 'numeric', minute: 'numeric', hour12: false
    });
    const utcParts = utcFormatter.formatToParts(date);
    const getUtcVal = (type) => parseInt(utcParts.find(p => p.type === type).value, 10);

    const tzDate = new Date(Date.UTC(getTzVal('year'), getTzVal('month') - 1, getTzVal('day'), getTzVal('hour'), getTzVal('minute')));
    const utcDate = new Date(Date.UTC(getUtcVal('year'), getUtcVal('month') - 1, getUtcVal('day'), getUtcVal('hour'), getUtcVal('minute')));

    const diffMs = tzDate.getTime() - utcDate.getTime();
    const diffHours = diffMs / (1000 * 60 * 60);
    const sign = diffHours >= 0 ? '+' : '-';
    const absHours = Math.abs(diffHours);
    const hours = Math.floor(absHours);
    const offsetStr = `GMT${sign}${hours}`;

    return `${getPart('month')}/${getPart('day')} ${getPart('hour')}:${getPart('minute')}:${getPart('second')}`;
  } catch (e) {
    const pad = (n) => n.toString().padStart(2, '0');
    const month = pad(date.getMonth() + 1);
    const day = pad(date.getDate());
    const hours = pad(date.getHours());
    const minutes = pad(date.getMinutes());
    const seconds = pad(date.getSeconds());
    return `${month}/${day} ${hours}:${minutes}:${seconds}`;
  }
}

const getStepperSteps = (req) => {
  if (!req || !req.steps || req.steps.length === 0) return [];
  const sortedSteps = [...req.steps].sort((a, b) => a.stepOrder - b.stepOrder);
  const result = sortedSteps.map(s => {
    const name = s.assigneeName || getUserName(s.assigneeId);
    let statusText = '';
    if (s.stepType === 'DRAFT') statusText = t('status_draft');
    else if (s.status === 'APPROVED') statusText = t('status_approved');
    else if (s.status === 'REJECTED') statusText = t('status_rejected');
    else if (s.status === 'PENDING') statusText = t('status_pending');
    else statusText = t('status_waiting');
    
    // DRAFT, APPROVED, REJECTED 완료 시점에 날짜 표시
    const isCompleted = s.stepType === 'DRAFT' || s.status === 'APPROVED' || s.status === 'REJECTED';
    const processedDate = isCompleted ? formatStepDate(s.updatedAt) : '';

    return {
      stepOrder: s.stepOrder,
      name: name,
      statusText: statusText,
      hasError: s.status === 'REJECTED',
      isPending: s.status === 'PENDING',
      processedDate: processedDate
    };
  });

  const isAllApproved = req.status === 'APPROVED';
  const isRejected = req.status === 'REJECTED';
  const isFinalized = isAllApproved || isRejected;

  result.push({
    stepOrder: result.length > 0 ? result[result.length - 1].stepOrder + 1 : 1,
    name: t('systemApplied'),
    statusText: isAllApproved ? t('systemComplete') : (isRejected ? t('systemCancelled') : t('stepScheduled')),
    hasError: false,
    isPending: false,
    processedDate: isFinalized ? formatStepDate(req.updatedAt) : ''
  });

  return result;
}

const userStore = useUserStore()
const roleStore = useRoleStore()

const getRequesterName = (req) => {
  if (!req) return '알 수 없음';
  return userStore.getUserName(req.requesterId, req.requesterName || req.requesterUsername);
}

const getClassificationName = (node, field) => {
  if (!node || !node[field]) return '분류 미지정';
  const nameObj = node[field];
  if (typeof nameObj === 'string') return nameObj;
  return nameObj[currentLocale.value] || nameObj['ko'] || nameObj['en'] || '분류 미지정';
}

const getRequestTypeLabel = (type) => {
  if (!type) return t('other_request');
  const i18nKey = `target_type_${type}`;
  const translated = t(i18nKey);
  if (translated && translated !== i18nKey) return translated;
  if (type === 'RECORD_CREATE') return t('record_create');
  if (type === 'RECORD_UPDATE') return t('record_update');
  if (type === 'RECORD_DELETE') return t('record_delete');
  if (type === 'DOMAIN_RECORD_CREATE') return t('domain_record_create');
  return type || t('other_request');
}

const getRequestTypeColor = (type) => {
  if (type === 'RECORD_CREATE' || type === 'DOMAIN_RECORD_CREATE') return 'success';
  if (type === 'RECORD_UPDATE') return 'warning';
  if (type === 'RECORD_DELETE') return 'danger';
  return 'primary';
}

const getCurrentStepIndex = (req) => {
  if (!req || !req.steps || req.steps.length === 0) return 0;
  if (req.status === 'APPROVED') return req.steps.length + 1;
  const sortedSteps = [...req.steps].sort((a, b) => a.stepOrder - b.stepOrder);
  let currentIndex = sortedSteps.findIndex(s => s.status === 'PENDING');
  if (currentIndex === -1) {
    currentIndex = sortedSteps.findIndex(s => s.status === 'REJECTED');
    if (currentIndex === -1) {
      currentIndex = sortedSteps.length; // All approved
    }
  }
  return currentIndex;
}


const createPendingDatasource = () => {
  return {
    getRows: async (params) => {
      const size = params.endRow - params.startRow;
      const page = Math.floor(params.startRow / size);
      
      try {
        const assigneeParam = myUuid.value ? `&assigneeId=${encodeURIComponent(myUuid.value)}` : '';
        const pageData = await $fetch(`/api/approval-requests/todos?page=${page}&size=${size}${assigneeParam}`, {
          headers: { Authorization: `Bearer ${token.value}` }
        });
        
        if (pageData && pageData.content) {
          for (const step of pageData.content) {
            if (step.approvalRequest && !step.approvalRequest.steps) {
              try {
                const fullReq = await $fetch(`/api/approval-requests/${step.approvalRequest.id}`, { headers: { Authorization: `Bearer ${token.value}` } });
                step.approvalRequest.steps = fullReq.steps;
                step.approvalRequest.observerIds = fullReq.observerIds;
              } catch (e) {}
            }
          }
          
          pendingSteps.value = pageData.content;
          for (const step of pageData.content) {
             if (step.approvalRequest) {
               step.approvalRequest = await enrichRequest(step.approvalRequest)
             }
             // Load field names if necessary for DetailsViewer, even though we enrich rows
             if (['RECORD', 'RECORD_UPDATE', 'RECORD_DELETE'].includes(step.approvalRequest?.targetType) && step.approvalRequest.targetId) {
                 const tId = step.approvalRequest.targetId;
                 const nId = step.approvalRequest.classificationNode?.id || step.approvalRequest.classificationNodeId;
                 await loadFieldNamesForRecord(tId, nId);
             }
          }
        }
        
        params.successCallback(pageData?.content || [], pageData?.totalElements || 0);
      } catch (e) {
        console.error('Error fetching pending steps:', e);
        params.failCallback();
      }
    }
  };
};

const createMyRequestsDatasource = () => {
  return {
    getRows: async (params) => {
      const size = params.endRow - params.startRow;
      const page = Math.floor(params.startRow / size);
      
      try {
        const requesterParam = myUuid.value ? `&requesterId=${encodeURIComponent(myUuid.value)}` : '';
        const pageData = await $fetch(`/api/approval-requests/my-requests?page=${page}&size=${size}${requesterParam}`, {
          headers: { Authorization: `Bearer ${token.value}` }
        });
        
        if (pageData && pageData.content) {
          myRequests.value = pageData.content;
          pageData.content = await Promise.all(pageData.content.map(req => enrichRequest(req)));
          for (const req of pageData.content) {
            if (['RECORD', 'RECORD_UPDATE', 'RECORD_DELETE'].includes(req.targetType) && req.targetId) {
              const nId = req.classificationNode?.id || req.classificationNodeId;
              await loadFieldNamesForRecord(req.targetId, nId);
            }
          }
        }
        
        params.successCallback(pageData?.content || [], pageData?.totalElements || 0);
      } catch (e) {
        console.error('Failed to load my requests:', e);
        params.failCallback();
      }
    }
  };
};

const refreshPendingRequests = async () => {
  if (pendingGridApi.value) {
    pendingGridApi.value.setGridOption('datasource', createPendingDatasource());
  }
}

const refreshMyRequests = async () => {
  if (myRequestsGridApi.value) {
    myRequestsGridApi.value.setGridOption('datasource', createMyRequestsDatasource());
  }
}

const loadRequests = async () => {
  await Promise.all([
    refreshPendingRequests(),
    refreshMyRequests()
  ])
}
const handleAction = async (stepId, action, isBulk = false) => {
  const comment = commentData.value[stepId] || ''
  const actionLabel = action === 'approve' ? t('action_approve') : t('action_reject')
  
  showLoading(t('action_processing', { action: actionLabel }))
  try {
    let url = `/api/approval-requests/steps/${stepId}/${action}?approverId=${myUuid.value}`
    if (hasPermission('admin:write')) {
      const step = pendingSelectedRows.value.find(r => r.id === stepId) || selectedPendingStep.value;
      if (step) {
        const isAssignee = step.assigneeId === myUuid.value;
        const userObj = userCookie.value ? (typeof userCookie.value === 'object' ? userCookie.value : JSON.parse(userCookie.value)) : null;
        const myUserRoles = Array.isArray(userObj?.roles) ? userObj.roles : (userObj?.role ? [userObj.role] : []);
        const hasRole = step.assigneeRole && myUserRoles.some(r => String(r).toUpperCase() === String(step.assigneeRole).toUpperCase());
        
        if (!isAssignee && !hasRole) {
          url = `/api/approval-requests/steps/${stepId}/admin-${action}?adminId=${myUuid.value}`
        }
      }
    }

    await $fetch(url, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` },
      body: { comment }
    })
    init({ message: t('action_success', { action: actionLabel }), color: 'success' })
    if (!isBulk) {

      await loadRequests()
    }
  } catch (error) {
    console.error(`Failed to ${action}:`, error)
    const status = error.response?.status
    const errMsg = error.response?._data?.message || error.message
    if (status === 409 || status === 412 || (errMsg && (errMsg.includes('Optimistic') || errMsg.includes('concurrent')))) {
      init({ message: '다른 사용자가 이미 결재 상태를 변경하였습니다. 최신 정보를 불러옵니다.', color: 'warning' })
    } else {
      init({ message: `결재 ${actionName} 실패: ${errMsg}`, color: 'danger' })
    }
    await loadRequests()
  } finally {
    hideLoading()
  }

}

const loadedEffectiveNodes = new Set();
const loadFieldNamesForRecord = async (targetId, nodeId = null) => {
  try {
    let effectiveNodeId = nodeId;
    if (!effectiveNodeId && targetId) {
      const record = await $fetch(`/api/records/${targetId}`, { headers: { Authorization: `Bearer ${token.value}` } }).catch(() => null);
      effectiveNodeId = record?.node?.id || record?.nodeId;
    }
    if (effectiveNodeId && !loadedEffectiveNodes.has(effectiveNodeId)) {
      loadedEffectiveNodes.add(effectiveNodeId);
      const fields = await $fetch(`/api/nodes/${effectiveNodeId}/fields/effective`, { headers: { Authorization: `Bearer ${token.value}` } }).catch(() => []);
      if (fields && fields.length > 0) {
        fields.forEach(f => {
          fieldNameMap.value[f.key] = f
        })
      }
    }
  } catch (e) {
    // Silent catch for non-existent or deleted records
  }
}

const getUserName = (uuid, nameFallback) => {
  return userStore.getUserName(uuid, nameFallback)
}

onMounted(async () => {
  await Promise.all([
    loadMetadata(),
    loadRequests(),
    userStore.fetchUserMap(),
    roleStore.dispatch('fetchRoles')
  ])
  if (route?.query?.openModalId) {
    const stepToOpen = pendingSteps.value.find(s => String(s.id) === String(route.query.openModalId))
    if (stepToOpen) {
      selectedPendingStep.value = stepToOpen
      showActionModal.value = true
    }
  }
  if (process.client) {
    window.addEventListener('approval-updated', loadRequests)
  }
})

onUnmounted(() => {
  if (process.client) {
    window.removeEventListener('approval-updated', loadRequests)
  }
})
</script>

<style scoped>
.approval-card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.approval-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.custom-scrollbar {
  max-height: 40vh;
  overflow-y: auto;
}
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: #f1f1f1; 
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #c1c1c1; 
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8; 
}

@keyframes step-flash {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.15); }
}

@keyframes step-flash {
  0% { box-shadow: 0 0 0 0 rgba(255, 212, 58, 0.6); transform: scale(1); }
  70% { box-shadow: 0 0 0 10px rgba(255, 212, 58, 0); transform: scale(1.05); }
  100% { box-shadow: 0 0 0 0 rgba(255, 212, 58, 0); transform: scale(1); }
}

.step-flash {
  animation: step-flash 1.5s infinite;
}
</style>
