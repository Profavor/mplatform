<template>
  <div class="dashboard-container">
    <!-- Header Banner -->
    <div class="dashboard-header-card">
      <div class="header-content">
        <div class="header-title-group">
          <div class="header-icon-wrapper">
            <va-icon name="dashboard" size="2rem" color="primary" />
          </div>
          <div>
            <h1 class="header-title">{{ t('dashboard') }}</h1>
            <p class="header-subtitle">{{ t('dashboard_subtitle') }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 4 Core KPI Metric Cards -->
    <div class="kpi-grid">
      <!-- Total Domains -->
      <div class="kpi-card">
        <div class="kpi-card-header">
          <span class="kpi-title">{{ t('total_domains') }}</span>
          <div class="kpi-icon-pill blue-pill">
            <va-icon name="domain" size="medium" />
          </div>
        </div>
        <div class="metric-body">
          <div class="metric-value blue-text">
            {{ stats?.totalDomains?.toLocaleString() ?? 0 }}
          </div>
          <div class="metric-subtext">{{ t('registered_domains') }}</div>
        </div>
      </div>

      <!-- Pending Approvals -->
      <div class="kpi-card">
        <div class="kpi-card-header">
          <span class="kpi-title">{{ t('pending_approvals') }}</span>
          <div class="kpi-icon-pill red-pill">
            <va-icon name="pending_actions" size="medium" />
          </div>
        </div>
        <div class="metric-body">
          <div class="metric-value red-text">
            {{ stats?.pendingApprovals?.toLocaleString() ?? 0 }}
          </div>
          <div class="metric-subtext" :class="{ 'has-pending': (stats?.pendingApprovals || 0) > 0 }">
            {{ (stats?.pendingApprovals || 0) > 0 ? t('action_required') : t('all_tasks_cleared') }}
          </div>
        </div>
      </div>

      <!-- Active Records -->
      <div class="kpi-card">
        <div class="kpi-card-header">
          <span class="kpi-title">{{ t('active_records') }}</span>
          <div class="kpi-icon-pill green-pill">
            <va-icon name="inventory_2" size="medium" />
          </div>
        </div>
        <div class="metric-body">
          <div class="metric-value green-text">
            {{ stats?.activeRecords?.toLocaleString() ?? 0 }}
          </div>
          <div class="metric-subtext">{{ t('managed_master_records') }}</div>
        </div>
      </div>

      <!-- Pending Match Candidates -->
      <div class="kpi-card">
        <div class="kpi-card-header">
          <span class="kpi-title">{{ t('pending_match_candidates') }}</span>
          <div class="kpi-icon-pill purple-pill">
            <va-icon name="fact_check" size="medium" />
          </div>
        </div>
        <div class="metric-body">
          <div class="metric-value purple-text">
            {{ stats?.pendingMatches?.toLocaleString() ?? 0 }}
          </div>
          <div class="metric-subtext">{{ t('potential_duplicates') }}</div>
        </div>
      </div>
    </div>

    <!-- Real Analytics & Distribution Charts Section -->
    <div class="content-grid">
      <!-- 7-Day Approval Requests Trend Chart -->
      <va-card class="section-card chart-card">
        <va-card-title class="card-header-title">
          <va-icon name="show_chart" size="small" color="primary" />
          {{ t('approval_trend_title') }}
        </va-card-title>
        <va-card-content>
          <ClientOnly>
            <v-chart style="height: 330px; width: 100%;" :option="trendChartOption" autoresize />
          </ClientOnly>
        </va-card-content>
      </va-card>

      <!-- Master Record Distribution Donut Chart -->
      <va-card class="section-card chart-card">
        <va-card-title class="card-header-title">
          <va-icon name="pie_chart" size="small" color="info" />
          {{ t('domain_distribution_title') }}
        </va-card-title>
        <va-card-content>
          <ClientOnly>
            <v-chart style="height: 330px; width: 100%;" :option="distributionChartOption" autoresize />
          </ClientOnly>
        </va-card-content>
      </va-card>
    </div>

    <!-- Bottom Section: My To-Do List & Governance/DQ Health -->
    <div class="bottom-grid">
      <!-- My To-Do List -->
      <va-card class="section-card todo-card">
        <va-card-title class="card-header-title">
          <va-icon name="task" size="small" color="warning" />
          {{ t('my_to_do_list') }}
        </va-card-title>
        <va-card-content>
          <div v-if="!todos || todos.length === 0" class="empty-todo-state">
            <va-icon name="check_circle_outline" size="2.5rem" color="success" />
            <p>{{ t('no_pending_tasks_you') }}</p>
          </div>
          <div v-else class="todo-list">
            <div v-for="todo in todos" :key="todo.id" class="todo-item-card">
              <div class="todo-item-main">
                <div class="todo-badges">
                  <va-badge :text="getStepTypeLabel(todo.stepType)" :color="todo.stepType === 'CONSENSUS' ? 'warning' : 'danger'" class="badge-bold" />
                  <va-badge :text="getActionTypeLabel(todo.approvalRequest?.changes)" color="info" outline class="badge-bold" />
                </div>

                <div class="todo-details">
                  <div v-if="todo.approvalRequest?.classificationNode" class="todo-node-info">
                    <span><strong>{{ t('domain') }}:</strong> {{ todo.approvalRequest.classificationNode.domainName?.[currentLocale] || todo.approvalRequest.classificationNode.domainName?.['en'] || 'Unknown' }}</span>
                    <span><strong>{{ t('classification') }}:</strong> {{ todo.approvalRequest.classificationNode.name?.[currentLocale] || todo.approvalRequest.classificationNode.name?.['en'] || 'Unknown' }}</span>
                  </div>
                  <div class="todo-requester">
                    <strong>{{ t('requester') }}:</strong> {{ todo.approvalRequest?.requesterName || getUserName(todo.approvalRequest?.requesterId) }}
                  </div>
                  <div class="todo-date">
                    <strong>{{ t('date') }}:</strong> {{ formatDate(todo.approvalRequest?.createdAt) }}
                  </div>
                </div>
              </div>

              <!-- Display info snippet -->
              <div class="todo-info-box">
                <div v-if="displayInfo[todo.id]?.displayId || displayInfo[todo.id]?.displayName" class="info-snippet">
                  <div v-if="displayInfo[todo.id]?.displayId" class="info-id">
                    {{ displayInfo[todo.id].idField?.name?.[currentLocale] || displayInfo[todo.id].idField?.name?.ko || displayInfo[todo.id].idField?.name?.en || 'ID' }}: {{ displayInfo[todo.id].displayId }}
                  </div>
                  <div v-if="displayInfo[todo.id]?.displayName" class="info-name">
                    {{ displayInfo[todo.id].nameField?.name?.[currentLocale] || displayInfo[todo.id].nameField?.name?.ko || displayInfo[todo.id].nameField?.name?.en || 'Name' }}: {{ displayInfo[todo.id].displayName }}
                  </div>
                </div>
                <div v-else class="info-snippet-fallback">
                  <em>{{ t('waiting_for_field_data') }}</em>
                </div>
              </div>

              <div class="todo-action">
                <va-button size="small" color="primary" class="review-btn" @click="goToApprovals(todo)">
                  {{ t('review') }}
                </va-button>
              </div>
            </div>
          </div>
        </va-card-content>
      </va-card>

      <!-- Governance & Data Quality Status Card -->
      <va-card class="section-card governance-card">
        <va-card-title class="card-header-title">
          <va-icon name="health_and_safety" size="small" color="success" />
          {{ t('governance_health_title') }}
        </va-card-title>
        <va-card-content>
          <div class="health-indicators-grid">
            <!-- Open DQ Violations -->
            <div class="health-item-card">
              <div class="health-item-header">
                <va-icon name="warning_amber" color="danger" size="1.5rem" />
                <span class="health-item-title">{{ t('open_dq_violations') }}</span>
              </div>
              <div class="health-item-body">
                <span class="health-value text-danger">{{ stats?.openDqViolations ?? 0 }}</span>
                <va-button size="small" color="danger" preset="secondary" icon="arrow_forward" @click="router.push('/dq-dashboard')">
                  {{ t('go_to_dq_dashboard') }}
                </va-button>
              </div>
            </div>

            <!-- Pending Match Candidates -->
            <div class="health-item-card">
              <div class="health-item-header">
                <va-icon name="find_in_page" color="warning" size="1.5rem" />
                <span class="health-item-title">{{ t('pending_match_candidates') }}</span>
              </div>
              <div class="health-item-body">
                <span class="health-value text-warning">{{ stats?.pendingMatches ?? 0 }}</span>
                <va-button size="small" color="warning" preset="secondary" icon="arrow_forward" @click="router.push('/admin/match-review')">
                  {{ t('go_to_match_review') }}
                </va-button>
              </div>
            </div>

            <!-- Approval Success Rate -->
            <div class="health-item-card">
              <div class="health-item-header">
                <va-icon name="verified" color="primary" size="1.5rem" />
                <span class="health-item-title">{{ t('approval_success_rate') }}</span>
              </div>
              <div class="health-item-body">
                <span class="health-value text-primary">
                  {{ getApprovalRate() }}%
                </span>
                <span class="health-subtext">
                  {{ t('approval_stats_summary', { approved: stats?.approvedApprovals ?? 0, rejected: stats?.rejectedApprovals ?? 0 }) }}
                </span>
              </div>
            </div>
          </div>
        </va-card-content>
      </va-card>
    </div>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
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
    if (typeof name === 'string' && name.startsWith('{')) {
      try {
        const parsed = JSON.parse(name)
        name = parsed[currentLocale.value] || parsed['ko'] || parsed['en'] || name
      } catch(e) {}
    }
    return {
      name: name || 'Domain',
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
