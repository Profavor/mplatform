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
            {{ t('subtitle') }}
          </span>
        </div>
      </div>

    </div>
    
    <va-card style="flex: 1; display: flex; flex-direction: column; min-height: 0;">
      <div style="padding: 0.75rem 1rem; border-bottom: 1px solid var(--va-background-border); display: flex; justify-content: flex-end; align-items: center; background: var(--va-background-element);">
        <va-button preset="plain" color="secondary" size="small" icon="refresh" @click="refreshGrid">{{ t('refresh') }}</va-button>
      </div>
      
      <va-card-content style="flex: 1; display: flex; flex-direction: column; padding: 0; min-height: 0;">
        <div :class="{ 'ag-theme-quartz-dark': isDark }" style="flex: 1; width: 100%; height: 100%; min-height: 400px;">
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

    <!-- Details Modal (Decoupled Component) -->
    <ApprovalMonitorDetailModal
      v-model="showDetailsModal"
      :selected-flow="selectedFlow"
      @proxy-approve="proxyApprove"
      @proxy-reject="proxyReject"
    />

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, h } from 'vue'
import { useCookie } from '#app'
import { AgGridVue } from 'ag-grid-vue3'
import { useI18n } from 'vue-i18n'
import { useModal } from 'vuestic-ui'
import ApprovalMonitorDetailModal from '~/components/admin/ApprovalMonitorDetailModal.vue'
import { usePageTitle } from '~/composables/usePageTitle'
import { useAgGridTheme } from '~/composables/useAgGridTheme'
import { useApprovalEnricher } from '~/composables/useApprovalEnricher'
import { useMultilingual } from '~/composables/useMultilingual'
import { useUserStore } from '~/stores/useUserStore'
import { useRoleStore } from '~/stores/useRoleStore'
import { useCodeStore } from '~/stores/useCodeStore'

const { t, te, locale } = useI18n()
const { pageTitle } = usePageTitle('approval_monitor_title', '결재 진행 모니터링')

const codeStore = useCodeStore()
codeStore.preloadGroups(['TARGET_TYPE', 'APPROVAL_STATUS']).catch(console.error)

const getStatusText = (status) => {
  if (!status) return ''
  const codeName = codeStore.getCodeName('APPROVAL_STATUS', status, null)
  if (codeName && codeName !== status) return codeName
  const key = 'status_' + String(status).toLowerCase()
  if (te(key)) return t(key)
  return status
}

const { confirm } = useModal()

const vaAlert = (message) => {
  confirm({
    title: t('notification', '알림'),
    message: message,
    okText: t('confirm', '확인'),
    cancelText: ''
  })
}

const { gridTheme, autoSizeStrategy, isDark } = useAgGridTheme()
const { formatMultilingual } = useMultilingual()
const { loadMetadata, enrichRequest, domains, nodes, getRequestTypeLabel, createTargetTypeBadgeElement, formatTargetInfo } = useApprovalEnricher()

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
    return (t('label_role')) + ': ' + formatRoleName(s.assigneeRole)
  }
  if (s.assigneeName) {
    let nameStr = String(s.assigneeName)
    const roleKoPrefixes = ['역할: ', '역할:', 'Role: ', 'Role:']
    for (const prefix of roleKoPrefixes) {
      if (nameStr.startsWith(prefix)) {
        const rawRole = nameStr.substring(prefix.length).trim()
        return (t('label_role')) + ': ' + formatRoleName(rawRole)
      }
    }
    return parseI18nVal(nameStr)
  }
  return getUserName(s.assigneeId) || t('unassigned')
}

const formatTargetType = (type) => getRequestTypeLabel(type)

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
    width: 140,
    cellRenderer: (params) => {
      if (!params || !params.value) return ''
      return createTargetTypeBadgeElement(params.value, isDark.value)
    },
    valueFormatter: (params) => formatTargetType(params.value),
    filter: 'agSetColumnFilter',
    filterParams: {
      values: ['RECORD_CREATE', 'RECORD_UPDATE', 'RECORD_DELETE', 'BULK_UPLOAD', 'SCHEMA', 'MEMO'],
      valueFormatter: (params) => formatTargetType(params.value)
    }
  },
  { 
    headerName: t('colDomain') || '도메인 / 대상', 
    field: 'domainName', 
    width: 170,
    valueGetter: (params) => formatTargetInfo(params.data),
    filter: 'agTextColumnFilter'
  },
  { 
    headerName: t('colSummary'), 
    field: 'summary', 
    flex: 1,
    minWidth: 220,
    tooltipField: 'summary',
    filter: 'agTextColumnFilter',
    sortable: false
  },
  { 
    headerName: t('colRequester'), 
    field: 'requesterId', 
    width: 130,
    valueGetter: (params) => getRequesterName(params.data),
    filter: 'agTextColumnFilter'
  },
  { 
    headerName: t('colCreatedAt'), 
    field: 'createdAt', 
    width: 160,
    valueFormatter: (params) => formatDate(params.value),
    filter: 'agDateColumnFilter',
    sort: 'desc'
  },
  { 
    headerName: t('colStatus'), 
    field: 'status', 
    width: 110,
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
    cancelText: t('cancel'),
    okText: t('confirm')
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
    cancelText: t('cancel'),
    okText: t('confirm')
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

