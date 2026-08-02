<template>
  <div class="admin-container" style="display: flex; flex-direction: column; gap: 1.25rem; height: calc(100vh - 120px); padding-bottom: 2rem;">
    <!-- Top Action Bar -->
    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); padding: 1rem 1.25rem; border-radius: 12px; border: 1px solid var(--va-background-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04);">
      <div style="display: flex; align-items: center; gap: 0.75rem;">
        <va-icon name="fact_check" size="large" color="primary" />
        <div>
          <h2 style="font-weight: 700; font-size: 1.35rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
            {{ pageTitle }}
            <va-badge text="Workflow" color="primary" size="small" />
          </h2>
          <span style="font-size: 0.85rem; color: var(--va-text-secondary);">
            {{ t('subtitle') || '모든 워크플로우 결재 건의 진행 상태, 단계별 승인 현황 및 이력을 통합 관리합니다.' }}
          </span>
        </div>
      </div>

    </div>
    
    <va-card style="flex: 1; display: flex; flex-direction: column; min-height: 0;">
      <div style="padding: 0.75rem 1rem; border-bottom: 1px solid var(--va-background-border); display: flex; justify-content: flex-end; align-items: center; background: var(--va-background-element);">
        <va-button preset="plain" color="secondary" size="small" icon="refresh" @click="refreshGrid">{{ t('refresh') || '새로고침' }}</va-button>
      </div>
      
      <va-card-content style="flex: 1; display: flex; flex-direction: column; padding: 0; min-height: 0;">
        <div :class="{ 'ag-theme-quartz-dark': isDark }" style="flex: 1; width: 100%; height: 100%;">
          <ag-grid-vue
            style="width: 100%; height: 100%;"
            :theme="gridTheme"
            :autoSizeStrategy="autoSizeStrategy"
            :columnDefs="columnDefs"
            :defaultColDef="defaultColDef"
            rowModelType="infinite"
            :cacheBlockSize="20"
            :rowSelection="{ mode: 'singleRow', headerCheckbox: false }"
            :pagination="true"
            :paginationPageSize="20"
            :paginationPageSizeSelector="[10, 20, 50]"
            @grid-ready="onGridReady"
          />
        </div>
      </va-card-content>
    </va-card>

    <!-- Details Modal -->
    <va-modal v-model="showDetailsModal" :title="t('workflowDetails')" size="large" hide-default-actions>
      <div v-if="selectedFlow" style="display: flex; flex-direction: column; gap: 1rem; max-height: 80vh; overflow-y: auto;">
        
        <div style="display: flex; justify-content: space-between; margin-bottom: 1rem;">
          <div style="font-weight: bold; font-size: 1.1rem;">
            {{ t('requestType') }}: {{ formatTargetType(selectedFlow.targetType) }}
          </div>
          <div>
            <va-badge :text="getStatusText(selectedFlow.status)" :color="selectedFlow.status === 'PENDING' ? 'warning' : (selectedFlow.status === 'APPROVED' ? 'success' : 'danger')" />
          </div>
        </div>

        <div style="font-size: 0.9rem; color: #555; margin-bottom: 1.5rem;">
          <strong>{{ t('requester') }}:</strong> {{ getRequesterName(selectedFlow) }} <br/>
          <strong>{{ t('createdAt') }}:</strong> {{ formatDate(selectedFlow.createdAt) }}
        </div>

        <!-- Pipeline Visualizer -->
        <div style="display: flex; align-items: flex-start; justify-content: space-between; background: var(--va-background-element); padding: 1.5rem 1rem; border-radius: 8px; min-height: 120px; overflow-x: auto;">
          <!-- Steps -->
          <template v-for="(step, idx) in selectedFlow.steps" :key="step.id">
            <div style="text-align: center; flex: 1; position: relative; display: flex; flex-direction: column; align-items: center; min-width: 90px;">
              <va-icon 
                :name="step.status === 'SUBMITTED' ? 'send' : (step.status === 'APPROVED' ? 'check_circle' : (step.status === 'REJECTED' ? 'cancel' : 'radio_button_unchecked'))" 
                :color="step.status === 'SUBMITTED' ? 'primary' : (step.status === 'APPROVED' ? 'success' : (step.status === 'REJECTED' ? 'danger' : (step.status === 'PENDING' ? 'warning' : 'secondary')))" 
                size="large" 
              />
              <div style="font-size: 0.8rem; margin-top: 0.5rem; font-weight: bold; white-space: nowrap;">
                {{ step.status === 'SUBMITTED' ? t('draft') : (step.stepType === 'CONSENSUS' ? t('consensus') : t('approval')) }}
              </div>
              <div style="font-size: 0.75rem; color: var(--va-text-secondary); margin-top: 0.2rem; min-height: 1.1rem; white-space: nowrap;">
                {{ getStatusText(step.status) }}
              </div>
              <div style="font-size: 0.75rem; color: var(--va-text-primary); margin-top: 0.2rem; min-height: 1.1rem; font-weight: bold; white-space: nowrap;" :title="step.assigneeId">
                {{ formatStepAssignee(step, selectedFlow) }}
              </div>
              <div style="font-size: 0.7rem; color: var(--va-text-secondary); margin-top: 0.1rem; min-height: 1rem; white-space: nowrap;">
                <span v-if="step.updatedAt && step.status !== 'PENDING'">{{ formatShortDate(step.updatedAt) }}</span>
                <span v-else-if="step.status === 'SUBMITTED'">{{ formatShortDate(step.createdAt || selectedFlow.createdAt) }}</span>
                <span v-else>&nbsp;</span>
              </div>
              
              <div v-if="step.status === 'PENDING'" style="margin-top: 0.5rem; display: flex; gap: 0.2rem; justify-content: center;">
                <va-button size="small" preset="secondary" color="success" icon="check" @click="proxyApprove(step.id)" :title="t('proxyApprove')"></va-button>
                <va-button size="small" preset="secondary" color="danger" icon="close" @click="proxyReject(step.id)" :title="t('proxyReject')"></va-button>
              </div>
            </div>
            
            <div v-if="idx < selectedFlow.steps.length - 1" style="flex: 1; height: 2px; background: var(--va-background-border); margin: 20px 10px 0 10px; min-width: 30px;"></div>
          </template>
        </div>
        
        <va-accordion style="margin-top: 1rem;" :multiple="true">
          <va-collapse :header="t('viewDataChanges')">
            <div style="padding: 1rem; background: var(--va-background-primary); border: 1px solid var(--va-background-border); border-radius: 4px;">
              <ApprovalDetailsViewer :request="selectedFlow" />
            </div>
          </va-collapse>
        </va-accordion>
        
      </div>
    </va-modal>

  </div>
</template>

<script setup>
import { ref, onMounted, computed, h } from 'vue'
import { usePageTitle } from '~/composables/usePageTitle'

const { pageTitle } = usePageTitle('approval_monitor_title', '결재 진행 모니터링')
import { useCookie } from '#app'
import { AgGridVue } from 'ag-grid-vue3'
import { useI18n } from 'vue-i18n'
import { useModal } from 'vuestic-ui'
import ApprovalDetailsViewer from '~/components/ApprovalDetailsViewer.vue'
import { useAgGridTheme } from '~/composables/useAgGridTheme'
import { useApprovalEnricher } from '~/composables/useApprovalEnricher'
import { useUserStore } from '~/stores/useUserStore'
import { useRoleStore } from '~/stores/useRoleStore'

const messages = {
  ko: {
    title: '관리자 결재 모니터링',
    subtitle: '전체 결재 진행 현황',
    refresh: '새로고침',
    workflowDetails: '결재 상세 정보',
    requestType: '요청 종류',
    requester: '기안자',
    createdAt: '상신일시',
    draft: '기안',
    consensus: '합의',
    approval: '승인',
    proxyApprove: '관리자 대리 승인',
    proxyReject: '관리자 대리 반려',
    proxyApproveConfirm: '이 단계를 대리 승인하시겠습니까?',
    proxyRejectConfirm: '이 단계를 대리 반려하시겠습니까?',
    proxyApproveFail: '대리 승인 실패: ',
    proxyRejectFail: '대리 반려 실패: ',
    viewDataChanges: '데이터 변경 상세 내역 (Diff)',
    colTargetType: '대상 타입',
    colRequester: '기안자',
    colCreatedAt: '상신일시',
    colStatus: '상태',
    colAction: '액션',
    btnDetails: '상세보기',
    colDomain: '도메인',
    colClassification: '분류',
    colIdAttr: 'ID 속성',
    colNameAttr: '이름 속성',
    colSummary: '요약',
    targetRecordCreate: '데이터 생성',
    targetRecordUpdate: '데이터 수정',
    targetRecordDelete: '데이터 삭제',
    targetBulkUpload: '대량 업로드',
    status_submitted: '상신',
    status_pending: '진행중',
    status_approved: '승인',
    status_rejected: '반려',
    label_role: '역할',
    label_drafter: '기안자',
    unassigned: '승인자 미지정'
  },
  en: {
    title: 'Admin Process Monitor',
    subtitle: 'All Ongoing Workflows',
    refresh: 'Refresh',
    workflowDetails: 'Workflow Details',
    requestType: 'Request',
    requester: 'Requester',
    createdAt: 'Created At',
    draft: 'Draft',
    consensus: 'Consensus',
    approval: 'Approval',
    proxyApprove: 'Proxy Approve',
    proxyReject: 'Proxy Reject',
    proxyApproveConfirm: 'Are you sure you want to proxy approve this step?',
    proxyRejectConfirm: 'Are you sure you want to proxy reject this step?',
    proxyApproveFail: 'Failed to proxy approve: ',
    proxyRejectFail: 'Failed to proxy reject: ',
    viewDataChanges: 'View Data Changes (Details)',
    colTargetType: 'Target Type',
    colRequester: 'Requester',
    colCreatedAt: 'Created At',
    colStatus: 'Status',
    colAction: 'Action',
    btnDetails: 'Details',
    colDomain: 'Domain',
    colClassification: 'Classification',
    colIdAttr: 'ID Value',
    colNameAttr: 'Name Value',
    colSummary: 'Summary',
    targetRecordCreate: 'Record Creation',
    targetRecordUpdate: 'Record Update',
    targetRecordDelete: 'Record Deletion',
    targetBulkUpload: 'Bulk Upload',
    status_submitted: 'SUBMITTED',
    status_pending: 'PENDING',
    status_approved: 'APPROVED',
    status_rejected: 'REJECTED',
    label_role: 'Role',
    label_drafter: 'Drafter',
    unassigned: 'Unassigned'
  }
}

const { t, te, locale } = useI18n({ messages, useScope: 'local', inheritLocale: true })

const getStatusText = (status) => {
  if (!status) return '';
  const key = 'status_' + String(status).toLowerCase();
  if (te(key)) return t(key);
  return status;
}

const { confirm } = useModal()

const vaAlert = (message) => {
  confirm({
    title: 'Notification',
    message: message,
    okText: 'OK',
    cancelText: ''
  })
}

const { gridTheme, autoSizeStrategy, isDark } = useAgGridTheme()

const { loadMetadata, enrichRequest, domains, nodes } = useApprovalEnricher()

const domainFilterValues = computed(() => {
  if (!domains || !domains.value) return [];
  const currentLocale = locale?.value || 'ko';
  return Object.values(domains.value).map(name => {
    if (!name) return '';
    if (typeof name === 'string') return name;
    return name[currentLocale] || name.ko || name.en || JSON.stringify(name);
  });
})

const classificationFilterValues = computed(() => {
  if (!nodes || !nodes.value) return [];
  const currentLocale = locale?.value || 'ko';
  return Object.values(nodes.value).map(name => {
    if (!name) return '';
    if (typeof name === 'string') return name;
    return name[currentLocale] || name.ko || name.en || JSON.stringify(name);
  });
})

const token = useCookie('auth_token')
const userData = useCookie('user_data')
const gridApi = ref(null)

const showDetailsModal = ref(false)
const selectedFlow = ref(null)

const userStore = useUserStore()
const roleStore = useRoleStore()

const parseI18nVal = (val) => {
  return userStore.parseI18nVal(val)
}

const getUserName = (id, fallbackName) => {
  return userStore.getUserName(id, fallbackName)
}

const getRequesterName = (flow) => {
  if (!flow) return ''
  return userStore.getUserName(flow.requesterId, flow.requesterName || flow.requesterUsername)
}

const formatRoleName = (roleCode) => {
  if (!roleCode) return ''
  const parsedCode = parseI18nVal(roleCode)
  const storeDisp = roleStore.getRoleDisplayName(parsedCode)
  if (storeDisp && storeDisp !== parsedCode) return parseI18nVal(storeDisp)
  
  const cleanCode = parsedCode.replace(/^ROLE_/, '')
  const storeCleanDisp = roleStore.getRoleDisplayName(cleanCode)
  if (storeCleanDisp && storeCleanDisp !== cleanCode) return parseI18nVal(storeCleanDisp)

  return parsedCode
}

const formatStepAssignee = (s, req) => {
  if (!s) return ''
  if (s.stepType === 'DRAFT' || s.status === 'SUBMITTED') {
    const nameCandidate = s.assigneeName || req?.requesterName || req?.requesterUsername
    return getUserName(s.assigneeId, nameCandidate)
  }
  if (s.assigneeRole && s.assigneeRole !== 'null') {
    return (t('label_role') || '역할') + ': ' + formatRoleName(s.assigneeRole)
  }
  if (s.assigneeName) {
    let nameStr = String(s.assigneeName)
    const roleKoPrefixes = ['역할: ', '역할:', 'Role: ', 'Role:']
    for (const prefix of roleKoPrefixes) {
      if (nameStr.startsWith(prefix)) {
        const rawRole = nameStr.substring(prefix.length).trim()
        return (t('label_role') || '역할') + ': ' + formatRoleName(rawRole)
      }
    }
    return parseI18nVal(nameStr)
  }
  return getUserName(s.assigneeId) || t('unassigned') || '승인자 미지정'
}

const formatTargetType = (type) => {
  if (type === 'RECORD_CREATE' || type === 'RECORD') return t('targetRecordCreate')
  if (type === 'RECORD_UPDATE') return t('targetRecordUpdate')
  if (type === 'RECORD_DELETE') return t('targetRecordDelete')
  if (type === 'BULK_UPLOAD') return t('targetBulkUpload')
  return type
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
  const targetLocale = locale.value === 'ko' ? 'ko-KR' : 'en-US'
  const formatted = date.toLocaleString(targetLocale, { timeZone: tz })
  return formatted.replace(/\s*(GMT|UTC|KST|PST|EST|CET)[-+0-9:]*/gi, '').trim()
}

const parseLocalizedValue = (val) => {
  return formatMultilingual(val)
}

const formatShortDate = (dateString) => {
  if (!dateString) return ''
  const date = parseDate(dateString)
  if (!date) return ''
  const yy = String(date.getFullYear()).slice(-2)
  const MM = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  return `${yy}.${MM}.${dd} ${hh}:${mm}`
}


const openDetails = (flow) => {
  selectedFlow.value = flow
  showDetailsModal.value = true
}

// Action cell renderer
const actionCellRenderer = {
  setup(props) {
    const onClick = () => {
      openDetails(props.params.data)
    }
    return () => h('button', { 
      onClick, 
      style: {
        background: 'var(--va-primary)', color: 'white', border: 'none', borderRadius: '4px', padding: '4px 12px', cursor: 'pointer', fontSize: '0.8rem', fontWeight: 'bold'
      }
    }, t('btnDetails'))
  }
}

// Computed columnDefs to react to locale changes
const columnDefs = computed(() => [
  { 
    headerName: t('colTargetType'), 
    field: 'targetType', 
    width: 130,
    valueFormatter: (params) => formatTargetType(params.value),
    filter: 'agSetColumnFilter',
    filterParams: {
      values: ['RECORD_CREATE', 'RECORD_UPDATE', 'RECORD_DELETE', 'BULK_UPLOAD'],
      valueFormatter: (params) => formatTargetType(params.value)
    }
  },
  { 
    headerName: t('colDomain'), 
    field: 'domainName', 
    width: 140,
    filter: 'agSetColumnFilter',
    filterParams: {
      values: domainFilterValues.value
    }
  },
  { 
    headerName: t('colClassification'), 
    field: 'classificationName', 
    width: 150,
    filter: 'agSetColumnFilter',
    filterParams: {
      values: classificationFilterValues.value
    }
  },
  { 
    headerName: t('colIdAttr'), 
    field: 'idAttribute', 
    width: 150,
    filter: 'agTextColumnFilter',
    sortable: false
  },
  { 
    headerName: t('colNameAttr'), 
    field: 'nameAttribute', 
    width: 180,
    valueFormatter: (params) => parseLocalizedValue(params.value),
    filter: 'agTextColumnFilter',
    sortable: false
  },
  { 
    headerName: t('colSummary'), 
    field: 'summary', 
    flex: 1,
    minWidth: 200,
    tooltipField: 'summary',
    filter: 'agTextColumnFilter',
    sortable: false
  },
  { 
    headerName: t('colRequester'), 
    field: 'requesterId', 
    width: 120,
    valueGetter: (params) => getRequesterName(params.data),
    filter: 'agTextColumnFilter'
  },
  { 
    headerName: t('colCreatedAt'), 
    field: 'createdAt', 
    width: 150,
    valueFormatter: (params) => formatDate(params.value),
    filter: 'agDateColumnFilter',
    sort: 'desc'
  },
  { 
    headerName: t('colStatus'), 
    field: 'status', 
    width: 100,
    valueFormatter: (params) => getStatusText(params.value),
    filter: 'agSetColumnFilter',
    filterParams: {
      values: ['SUBMITTED', 'PENDING', 'APPROVED', 'REJECTED'],
      valueFormatter: (params) => {
        return getStatusText(params.value);
      }
    },
    cellStyle: (params) => {
      if (params.value === 'PENDING') return { color: 'orange', fontWeight: 'bold' }
      if (params.value === 'APPROVED') return { color: 'green', fontWeight: 'bold' }
      if (params.value === 'REJECTED') return { color: 'red', fontWeight: 'bold' }
      return {}
    }
  },
  {
    headerName: t('colAction'),
    width: 100,
    cellRenderer: actionCellRenderer
  }
])

const searchQuery = ref('')
let searchTimeout = null

const defaultColDef = ref({
  sortable: true,
  filter: true,
  resizable: true,
  tooltipComponentParams: { color: '#ececec' }
})

const createDatasource = () => {
  return {
    getRows: async (params) => {
      const size = params.endRow - params.startRow;
      const page = Math.floor(params.startRow / size);
      
      try {
        const querySearch = searchQuery.value ? `&search=${encodeURIComponent(searchQuery.value)}` : '';
        let filterModelParam = '';
        if (params.filterModel && Object.keys(params.filterModel).length > 0) {
          const fm = JSON.parse(JSON.stringify(params.filterModel));
          if (fm.status) {
            let selectedValues = [];
            if (fm.status.filterType === 'set' && fm.status.values) {
              selectedValues = fm.status.values;
            } else if (fm.status.filter && typeof fm.status.filter === 'string') {
              selectedValues = [fm.status.filter];
            }
            const mapped = selectedValues.map(v => {
              const val = String(v).trim();
              if (val === '상신' || val === 'status_submitted') return 'SUBMITTED';
              if (val === '진행중' || val === 'status_pending') return 'PENDING';
              if (val === '승인' || val === 'status_approved') return 'APPROVED';
              if (val === '반려' || val === 'status_rejected') return 'REJECTED';
              return val;
            });
            if (fm.status.filterType === 'set') {
              fm.status.values = mapped;
            } else {
              fm.status.filter = mapped[0];
            }
          }
          filterModelParam = `&filterModel=${encodeURIComponent(JSON.stringify(fm))}`;
        }
        
        let sortQuery = '';
        if (params.sortModel && params.sortModel.length > 0) {
          const sm = params.sortModel[0];
          sortQuery = `&sort=${sm.colId},${sm.sort}`;
        }
        
        const pageData = await $fetch(`/api/approval-requests/all?page=${page}&size=${size}${querySearch}${filterModelParam}${sortQuery}`, {
          headers: { Authorization: `Bearer ${token.value}` }
        });
        
        // Enrich data
        const enrichedContent = await Promise.all(pageData.content.map(req => enrichRequest(req)))
        
        params.successCallback(enrichedContent, pageData.totalElements);
      } catch (e) {
        console.error('Failed to load workflows:', e);
        params.failCallback();
      }
    }
  };
};

const onGridReady = (params) => {
  gridApi.value = params.api
  gridApi.value.setGridOption('datasource', createDatasource())
}

const refreshGrid = () => {
  if (gridApi.value) {
    gridApi.value.setGridOption('datasource', createDatasource())
  }
}

const getCurrentUserObj = () => {
  const cookieVal = userData.value
  if (!cookieVal) return null
  if (typeof cookieVal === 'object') return cookieVal
  if (typeof cookieVal === 'string') {
    try { return JSON.parse(cookieVal) } catch (e) { return null }
  }
  return null
}

const proxyApprove = async (stepId) => {
  const isConfirmed = await confirm({
    title: t('proxyApprove'),
    message: t('proxyApproveConfirm'),
    cancelText: 'Cancel',
    okText: 'OK'
  })
  if (!isConfirmed) return
  
  try {
    const userObj = getCurrentUserObj()
    const adminId = userObj?.id || userObj?.uuid || userObj?.username
    const headers = { Authorization: `Bearer ${token.value}` }
    const url = adminId 
      ? `/api/approval-requests/steps/${stepId}/admin-approve?adminId=${encodeURIComponent(adminId)}`
      : `/api/approval-requests/steps/${stepId}/admin-approve`
    const updatedFlow = await $fetch(url, {
      method: 'POST',
      headers,
      body: { comment: 'Approved by Administrator' }
    })
    
    // Update the selectedFlow in the modal
    if (selectedFlow.value && selectedFlow.value.id === updatedFlow.id) {
      selectedFlow.value = updatedFlow
    }
    
    refreshGrid()
  } catch (e) {
    vaAlert(t('proxyApproveFail') + (e.data?.message || e.message))
  }
}

const proxyReject = async (stepId) => {
  const isConfirmed = await confirm({
    title: t('proxyReject'),
    message: t('proxyRejectConfirm'),
    cancelText: 'Cancel',
    okText: 'OK'
  })
  if (!isConfirmed) return
  
  try {
    const userObj = getCurrentUserObj()
    const adminId = userObj?.id || userObj?.uuid || userObj?.username
    const headers = { Authorization: `Bearer ${token.value}` }
    const url = adminId 
      ? `/api/approval-requests/steps/${stepId}/admin-reject?adminId=${encodeURIComponent(adminId)}`
      : `/api/approval-requests/steps/${stepId}/admin-reject`
    const updatedFlow = await $fetch(url, {
      method: 'POST',
      headers,
      body: { comment: 'Rejected by Administrator' }
    })
    
    if (selectedFlow.value && selectedFlow.value.id === updatedFlow.id) {
      selectedFlow.value = updatedFlow
    }
    
    refreshGrid()
  } catch (e) {
    vaAlert(t('proxyRejectFail') + (e.data?.message || e.message))
  }
}

const handleApprovalUpdated = () => {
  refreshGrid()
}

onMounted(async () => {
  await Promise.all([
    loadMetadata(),
    userStore.fetchUserMap(),
    roleStore.dispatch('fetchRoles')
  ])
  if (process.client) {
    window.addEventListener('approval-updated', handleApprovalUpdated)
  }
})

onUnmounted(() => {
  if (process.client) {
    window.removeEventListener('approval-updated', handleApprovalUpdated)
  }
})
</script>

