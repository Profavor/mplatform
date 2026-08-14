<template>
  <div style="display: flex; flex-direction: column; gap: 1.25rem; padding-bottom: 2rem;">
    <!-- Top Action Bar -->
    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); padding: 1rem 1.25rem; border-radius: 12px; border: 1px solid var(--va-background-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04);">
      <div style="display: flex; align-items: center; gap: 0.75rem;">
        <va-icon name="dashboard" size="large" color="primary" />
        <div>
          <h2 style="font-weight: 700; font-size: 1.35rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
            {{ pageTitle }}
            <va-badge text="Overview" color="primary" size="small" />
          </h2>
          <span style="font-size: 0.85rem; color: var(--va-text-secondary);">
            {{ t('dashboard_subtitle') }}
          </span>
        </div>
      </div>
    </div>

    <!-- 4 Core KPI Metric Cards (Decoupled Component) -->
    <DashboardKpiCards :stats="stats" />

    <!-- Real Analytics & Distribution Charts Section (Decoupled Component) -->
    <DashboardApprovalCharts
      :trend-chart-option="trendChartOption"
      :distribution-chart-option="distributionChartOption"
    />

    <!-- Data Quality Analytics Grid (Decoupled Component) -->
    <DashboardDqCharts
      :dq-trend-chart-option="dqTrendChartOption"
      :dq-severity-chart-option="dqSeverityChartOption"
    />

    <!-- Bottom Section: My To-Do List & Governance/DQ Health -->
    <div class="bottom-grid">
      <!-- My To-Do List (Decoupled Component) -->
      <DashboardTodoList
        :todos="todos"
        :display-info="displayInfo"
        :current-locale="currentLocale"
        @review="goToApprovals"
      />

      <!-- Governance & Data Quality Status Card (Decoupled Component) -->
      <DashboardGovernanceCard :stats="stats" />
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import { usePageTitle } from '~/composables/usePageTitle'
import DashboardKpiCards from '~/components/dashboard/DashboardKpiCards.vue'
import DashboardGovernanceCard from '~/components/dashboard/DashboardGovernanceCard.vue'
import DashboardTodoList from '~/components/dashboard/DashboardTodoList.vue'
import DashboardApprovalCharts from '~/components/dashboard/DashboardApprovalCharts.vue'
import DashboardDqCharts from '~/components/dashboard/DashboardDqCharts.vue'
const { t } = useI18n()
const { pageTitle } = usePageTitle('dashboard', '홈')
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useColors } from 'vuestic-ui'

const router = useRouter()
const stats = ref(null)
const todos = ref([])
const domainList = ref([])
const displayInfo = ref({})
const rawTrends = ref([])
const rawDistribution = ref([])
const rawDqTrends = ref([])
const rawDqSeverity = ref([])

const tokenCookie = useCookie('auth_token')
const userCookie = useCookie('user_data')
const localeCookie = useCookie('locale')
const currentLocale = computed(() => localeCookie.value || 'ko')

const { currentPresetName } = useColors()
const isDark = computed(() => currentPresetName.value === 'dark')

const currentUser = computed(() => {
  if (userCookie.value) {
    return typeof userCookie.value === 'string' ? JSON.parse(userCookie.value) : userCookie.value
  }
  return null
})

const loadDashboardTodos = async () => {
  const headers = { Authorization: `Bearer ${tokenCookie.value}` }
  const myUuid = currentUser.value?.uuid
  if (myUuid) {
    try {
      const todoRes = await $fetch(`/api/approval-requests/todos?assigneeId=${myUuid}`, { headers })
      todos.value = Array.isArray(todoRes) ? todoRes : (todoRes?.content || [])
    } catch(e) {}
  }
}

onMounted(async () => {
  if (process.client) {
    window.addEventListener('approval-updated', loadDashboardTodos)
  }
  try {
    const headers = { Authorization: `Bearer ${tokenCookie.value}` }
    const myUuid = currentUser.value?.uuid

    try {
      domainList.value = await $fetch('/api/domains', { headers })
    } catch(e) {}

    stats.value = await $fetch('/api/dashboard/stats', { headers })

    try {
      rawTrends.value = await $fetch('/api/dashboard/trends', { headers })
    } catch(e) {}

    try {
      rawDistribution.value = await $fetch('/api/dashboard/domain-distribution', { headers })
    } catch(e) {}

    try {
      rawDqTrends.value = await $fetch('/api/dashboard/dq-trends', { headers })
    } catch(e) {}

    try {
      rawDqSeverity.value = await $fetch('/api/dashboard/dq-severity', { headers })
    } catch(e) {}

    if (myUuid) {
      const todoRes = await $fetch(`/api/approval-requests/todos?assigneeId=${myUuid}`, { headers })
      todos.value = Array.isArray(todoRes) ? todoRes : (todoRes?.content || [])
      
      const nodeFieldCache = {}
      const fetchFieldsForNode = async (nodeId) => {
        if (nodeFieldCache[nodeId]) return nodeFieldCache[nodeId]
        try {
          const fields = await $fetch(`/api/nodes/${nodeId}/fields/effective`, { headers })
          nodeFieldCache[nodeId] = fields
          return fields
        } catch(e) {
          return []
        }
      }

      if (Array.isArray(todos.value)) {
        for (const todo of todos.value) {
          if (todo?.approvalRequest && todo.approvalRequest.targetType === 'RECORD') {
            const domainId = todo.approvalRequest.classificationNode?.domainId
            const domain = (domainList.value || []).find(d => d.id === domainId)
            if (domain && todo.approvalRequest.classificationNode?.id) {
              const fields = await fetchFieldsForNode(todo.approvalRequest.classificationNode.id)
              let idField = fields.find(f => f.id === domain.identifierFieldId)
              let nameField = fields.find(f => f.id === domain.displayNameFieldId)
              
              if (!idField && fields.length > 0) idField = fields[0]
              if (!nameField && fields.length > 1) nameField = fields[1]

              let payload = {}
              try {
                let parsed = todo.approvalRequest.changes
                if (typeof parsed === 'string') parsed = JSON.parse(parsed)
                if (typeof parsed === 'string') parsed = JSON.parse(parsed)
                payload = parsed?.after || parsed || {}
              } catch(e) {}
              
              displayInfo.value[todo.id] = {
                displayId: idField ? payload[idField.key] : null,
                displayName: nameField ? payload[nameField.key] : null,
                idField: idField ? { ...idField, name: typeof idField.name === 'string' ? JSON.parse(idField.name || '{}') : idField.name } : null,
                nameField: nameField ? { ...nameField, name: typeof nameField.name === 'string' ? JSON.parse(nameField.name || '{}') : nameField.name } : null
              }
            }
          }
        }
      }
    }
  } catch (e) {
    console.error('Error fetching dashboard data:', e)
  }
})

const getApprovalRate = () => {
  const approved = stats.value?.approvedApprovals || 0
  const rejected = stats.value?.rejectedApprovals || 0
  const total = approved + rejected
  if (total === 0) return '100.0'
  return ((approved / total) * 100).toFixed(1)
}

const goToApprovals = (todo) => {
  router.push(`/approvals?openModalId=${todo.id}`)
}

const getStepTypeLabel = (stepType) => {
  if (stepType === 'CONSENSUS') return t('step_consensus')
  if (stepType === 'APPROVAL') return t('step_approval')
  return stepType || ''
}

const getActionTypeLabel = (changes) => {
  if (!changes) return t('create')
  try {
    const deepParse = (val) => {
      try {
        if (typeof val === 'string') return deepParse(JSON.parse(val))
        return val
      } catch(e) { return val }
    }
    const parsed = deepParse(changes)
    if (parsed && typeof parsed === 'object' && ('before' in parsed || 'after' in parsed)) {
      return t('update')
    }
  } catch(e) {}
  return t('create')
}

const getUserName = (uuid, nameFallback) => {
  return nameFallback || uuid || ''
}

// 7-Day Approval Trend Line Chart (ECharts)
const trendChartOption = computed(() => {
  const textColor = isDark.value ? '#cbd5e1' : '#475569'
  const splitLineColor = isDark.value ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.08)'

  const dates = (rawTrends.value || []).map(t => t.date?.substring(5) || t.date)
  const counts = (rawTrends.value || []).map(t => t.count || 0)

  return {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates.length ? dates : ['D-6', 'D-5', 'D-4', 'D-3', 'D-2', 'D-1', t('today')],
      axisLabel: { color: textColor },
      axisLine: { lineStyle: { color: splitLineColor } }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: textColor },
      splitLine: { lineStyle: { color: splitLineColor } }
    },
    series: [
      {
        name: t('pending_approvals'),
        data: counts.length ? counts : [0, 0, 0, 0, 0, 0, 0],
        type: 'line',
        smooth: true,
        color: '#2c82e0',
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [{
              offset: 0, color: 'rgba(44, 130, 224, 0.4)'
            }, {
              offset: 1, color: 'rgba(44, 130, 224, 0)'
            }]
          }
        }
      }
    ]
  }
})

// Master Record Distribution Donut Chart (ECharts)
const distributionChartOption = computed(() => {
  const textColor = isDark.value ? '#cbd5e1' : '#475569'

  const chartData = (rawDistribution.value || []).map(d => {
    let name = d.domainName
    if (typeof name === 'object' && name !== null) {
      name = name[currentLocale.value] || name['ko'] || name['en'] || Object.values(name)[0] || ''
    } else if (typeof name === 'string') {
      if (name.startsWith('{')) {
        try {
          const parsed = JSON.parse(name)
          name = parsed[currentLocale.value] || parsed['ko'] || parsed['en'] || name
        } catch(e) {}
      }
    }
    return {
      name: String(name || 'Domain'),
      value: d.recordCount || 0
    }
  })

  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: '0%',
      left: 'center',
      textStyle: { color: textColor }
    },
    series: [
      {
        name: t('domain_distribution_title'),
        type: 'pie',
        radius: ['42%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: isDark.value ? '#1e293b' : '#ffffff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        data: chartData.length ? chartData : [{ name: t('no_domain_records'), value: 0 }]
      }
    ]
  }
})

// DQ Violation Trend Line Chart
const dqTrendChartOption = computed(() => {
  const textColor = isDark.value ? '#cbd5e1' : '#475569'
  const splitLineColor = isDark.value ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.08)'

  const dates = (rawDqTrends.value || []).map(t => t.date?.substring(5) || t.date)
  const counts = (rawDqTrends.value || []).map(t => t.count || 0)

  return {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates.length ? dates : ['D-6', 'D-5', 'D-4', 'D-3', 'D-2', 'D-1', t('today') || 'Today'],
      axisLabel: { color: textColor },
      axisLine: { lineStyle: { color: splitLineColor } }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: textColor },
      splitLine: { lineStyle: { color: splitLineColor } }
    },
    series: [
      {
        name: t('open_dq_violations') || 'DQ Violations',
        data: counts.length ? counts : [0, 0, 0, 0, 0, 0, 0],
        type: 'line',
        smooth: true,
        color: '#e4233c',
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [{
              offset: 0, color: 'rgba(228, 35, 60, 0.4)'
            }, {
              offset: 1, color: 'rgba(228, 35, 60, 0)'
            }]
          }
        }
      }
    ]
  }
})

// DQ Severity Distribution Bar Chart
const dqSeverityChartOption = computed(() => {
  const textColor = isDark.value ? '#cbd5e1' : '#475569'
  const splitLineColor = isDark.value ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.08)'

  const dataMap = { 'HIGH': 0, 'MEDIUM': 0, 'LOW': 0 }
  ;(rawDqSeverity.value || []).forEach(item => {
    if (item.severity) dataMap[item.severity] = item.count
  })

  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['HIGH', 'MEDIUM', 'LOW'],
      axisLabel: { color: textColor },
      axisLine: { lineStyle: { color: splitLineColor } }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: textColor },
      splitLine: { lineStyle: { color: splitLineColor } }
    },
    series: [
      {
        name: t('dq_severity_distribution') || 'Severity',
        type: 'bar',
        barWidth: '50%',
        data: [
          { value: dataMap['HIGH'], itemStyle: { color: '#e4233c' } },
          { value: dataMap['MEDIUM'], itemStyle: { color: '#f59e0b' } },
          { value: dataMap['LOW'], itemStyle: { color: '#3b82f6' } }
        ]
      }
    ]
  }
})

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
onUnmounted(() => {
  if (process.client) {
    window.removeEventListener('approval-updated', loadDashboardTodos)
  }
})
</script>

<style scoped>
.dashboard-container {
  padding: 1.5rem 2rem;
  width: 100%;
  box-sizing: border-box;
  font-family: var(--va-font-family, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif);
}

/* Header Banner Styling */
.dashboard-header-card {
  background: linear-gradient(135deg, rgba(44, 130, 224, 0.08) 0%, rgba(108, 92, 231, 0.05) 100%);
  border: 1px solid rgba(44, 130, 224, 0.2);
  border-radius: 16px;
  padding: 1.5rem 1.75rem;
  margin-bottom: 1.75rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
  width: 100%;
  box-sizing: border-box;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1.25rem;
}

.header-title-group {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.header-icon-wrapper {
  width: 52px;
  height: 52px;
  background: linear-gradient(135deg, #2c82e0 0%, #1565c0 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(44, 130, 224, 0.3);
}

.header-title {
  font-size: 1.6rem;
  font-weight: 800;
  color: var(--va-text-primary);
  margin: 0;
  letter-spacing: -0.5px;
}

.header-subtitle {
  font-size: 0.9rem;
  color: var(--va-text-secondary);
  margin: 0.2rem 0 0 0;
}

/* KPI Grid (4 Cards) */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.25rem;
  margin-bottom: 1.75rem;
  width: 100%;
}

@media (max-width: 1200px) {
  .kpi-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .kpi-grid {
    grid-template-columns: 1fr;
  }
}

.kpi-card {
  background: var(--va-background-secondary);
  border: 1px solid var(--va-background-border);
  border-radius: 18px;
  padding: 1.25rem 1.5rem;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 165px;
}

.kpi-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.08);
}

.kpi-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.kpi-title {
  font-size: 0.9rem;
  font-weight: 800;
  color: var(--va-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.kpi-icon-pill {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.blue-pill { background: rgba(44, 130, 224, 0.12); color: #2c82e0; }
.red-pill { background: rgba(228, 35, 60, 0.12); color: #e4233c; }
.green-pill { background: rgba(16, 185, 129, 0.12); color: #10b981; }
.purple-pill { background: rgba(139, 92, 246, 0.12); color: #8b5cf6; }

.metric-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 0.35rem;
  padding: 0.25rem 0;
  flex: 1;
}

.metric-value {
  font-size: 3.2rem;
  font-weight: 900;
  letter-spacing: -1.5px;
  line-height: 1;
  text-align: center;
}

.blue-text { color: #2c82e0; }
.red-text { color: #e4233c; }
.green-text { color: #10b981; }
.purple-text { color: #8b5cf6; }

.metric-subtext {
  font-size: 0.88rem;
  color: var(--va-text-secondary);
  font-weight: 600;
  text-align: center;
}

.metric-subtext.has-pending {
  color: #e4233c;
  font-weight: 700;
}

/* Content Grid Section (Charts) */
.content-grid {
  display: grid;
  grid-template-columns: 1.8fr 1.2fr;
  gap: 1.5rem;
  margin-bottom: 1.75rem;
  width: 100%;
}

@media (max-width: 1100px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

/* Bottom Grid (To-Do & Governance Status) */
.bottom-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 1.5rem;
  width: 100%;
}

@media (max-width: 1100px) {
  .bottom-grid {
    grid-template-columns: 1fr;
  }
}

.section-card {
  border-radius: 16px;
  border: 1px solid var(--va-background-border);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.card-header-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.1rem;
  font-weight: 800;
  padding: 1.25rem 1.5rem 0.5rem 1.5rem;
}

/* Todo List Styling */
.empty-todo-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 3rem 1rem;
  color: var(--va-text-secondary);
  text-align: center;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-height: 380px;
  overflow-y: auto;
}

.todo-item-card {
  border: 1px solid var(--va-background-border);
  background: var(--va-background-element);
  border-radius: 12px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  transition: all 0.2s ease;
}

.todo-item-card:hover {
  background: rgba(44, 130, 224, 0.04);
  border-color: rgba(44, 130, 224, 0.3);
}

.todo-item-main {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.todo-badges {
  display: flex;
  gap: 0.5rem;
}

.badge-bold {
  font-weight: 700;
}

.todo-details {
  font-size: 0.85rem;
  color: var(--va-text-secondary);
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.todo-node-info {
  display: flex;
  gap: 1rem;
  color: var(--va-text-primary);
}

.todo-info-box {
  background: var(--va-background-secondary);
  border: 1px dashed var(--va-background-border);
  border-radius: 8px;
  padding: 0.6rem 0.8rem;
  font-size: 0.85rem;
}

.info-id {
  color: #2c82e0;
  font-weight: 700;
}

.info-name {
  color: var(--va-text-primary);
  font-weight: 600;
}

.info-snippet-fallback {
  color: var(--va-text-secondary);
  font-size: 0.8rem;
}

.todo-action {
  display: flex;
  justify-content: flex-end;
}

.review-btn {
  font-weight: 700;
  border-radius: 8px;
}

/* Health Indicators Styling */
.health-indicators-grid {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  padding: 0.5rem 0;
}

.health-item-card {
  border: 1px solid var(--va-background-border);
  background: var(--va-background-element);
  border-radius: 14px;
  padding: 1.1rem 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.health-item-header {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.health-item-title {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--va-text-primary);
}

.health-item-body {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.health-value {
  font-size: 2rem;
  font-weight: 800;
}

.health-subtext {
  font-size: 0.85rem;
  color: var(--va-text-secondary);
  font-weight: 600;
}

.text-danger { color: #e4233c; }
.text-warning { color: #f59e0b; }
.text-primary { color: #2c82e0; }
</style>
