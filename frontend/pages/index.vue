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

    <!-- 3 Core KPI Metric Cards -->
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
          <div class="metric-subtext">Managed Master Records</div>
        </div>
      </div>
    </div>

    <!-- Analytics & To-Do Section -->
    <div class="content-grid">
      <!-- Record Creation Trends Chart -->
      <va-card class="section-card chart-card">
        <va-card-title class="card-header-title">
          <va-icon name="show_chart" size="small" color="primary" />
          {{ t('record_creation_trends') }}
        </va-card-title>
        <va-card-content>
          <ClientOnly>
            <v-chart style="height: 350px; width: 100%;" :option="chartOption" autoresize />
          </ClientOnly>
        </va-card-content>
      </va-card>

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
                  <va-badge :text="todo.stepType" :color="todo.stepType === 'CONSENSUS' ? 'warning' : 'danger'" class="badge-bold" />
                  <va-badge :text="getActionTypeLabel(todo.approvalRequest?.changes)" color="info" outline class="badge-bold" />
                </div>

                <div class="todo-details">
                  <div v-if="todo.approvalRequest?.classificationNode" class="todo-node-info">
                    <span><strong>{{ t('domain') }}:</strong> {{ todo.approvalRequest.classificationNode.domainName?.[currentLocale] || todo.approvalRequest.classificationNode.domainName?.['en'] || 'Unknown' }}</span>
                    <span><strong>{{ t('classification') }}:</strong> {{ todo.approvalRequest.classificationNode.name?.[currentLocale] || todo.approvalRequest.classificationNode.name?.['en'] || 'Unknown' }}</span>
                  </div>
                  <div class="todo-requester">
                    <strong>{{ t('requester') }}:</strong> {{ getUserName(todo.approvalRequest?.requesterId) }}
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
    </div>

    <!-- My Submitted Requests Table -->
    <va-card class="section-card requests-table-card">
      <va-card-title class="card-header-title">
        <va-icon name="send" size="small" color="primary" />
        {{ t('my_submitted_requests') }}
      </va-card-title>
      <va-card-content>
        <div v-if="!myRequests || myRequests.length === 0" class="empty-state">
          <va-icon name="inbox" size="2.5rem" color="secondary" />
          <p>{{ t('no_requests_submitted_yet') }}</p>
        </div>
        <div v-else class="table-responsive">
          <table class="custom-dashboard-table">
            <thead>
              <tr>
                <th>{{ t('req_id') }}</th>
                <th>{{ t('status') }}</th>
                <th>{{ t('date') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="req in myRequests" :key="req.id" class="table-row">
                <td class="req-id-cell">
                  <va-icon name="receipt_long" size="small" color="primary" />
                  <span>{{ req.id.substring(0, 8) }}...</span>
                </td>
                <td>
                  <va-badge
                    :text="req.status"
                    :color="req.status === 'PENDING' ? 'warning' : (req.status === 'APPROVED' ? 'success' : 'danger')"
                    class="badge-bold"
                  />
                </td>
                <td class="date-cell">{{ formatDate(req.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </va-card-content>
    </va-card>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const stats = ref(null)
const todos = ref([])
const myRequests = ref([])
const userList = ref([])
const domainList = ref([])
const displayInfo = ref({})

const tokenCookie = useCookie('auth_token')
const userCookie = useCookie('user_data')
const localeCookie = useCookie('locale')
const currentLocale = computed(() => localeCookie.value || 'ko')

const currentUser = computed(() => {
  if (userCookie.value) {
    return typeof userCookie.value === 'string' ? JSON.parse(userCookie.value) : userCookie.value
  }
  return null
})

onMounted(async () => {
  try {
    const headers = { Authorization: `Bearer ${tokenCookie.value}` }
    const myUuid = currentUser.value?.uuid
    
    try {
      const uRes = await fetch('/api/auth/users', { headers })
      if (uRes.ok) userList.value = await uRes.json()
    } catch(e) {}

    try {
      domainList.value = await $fetch('/api/domains', { headers })
    } catch(e) {}

    stats.value = await $fetch('/api/dashboard/stats', { headers })
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

      const reqRes = await $fetch(`/api/approval-requests/my-requests?requesterId=${myUuid}`, { headers })
      myRequests.value = Array.isArray(reqRes) ? reqRes : (reqRes?.content || [])
    }
  } catch (e) {
    console.error('Error fetching dashboard data:', e)
  }
})

const goToApprovals = (todo) => {
  router.push(`/approvals?openModalId=${todo.id}`)
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

const getUserName = (uuid) => {
  if (!uuid) return ''
  const u = userList.value.find(user => user.uuid === uuid)
  return u ? u.username : uuid
}

const chartOption = ref({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
  },
  yAxis: { type: 'value' },
  series: [
    {
      data: [820, 932, 901, 934, 1290, 1330, 1320],
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

/* KPI Grid */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.25rem;
  margin-bottom: 1.75rem;
  width: 100%;
}

@media (max-width: 900px) {
  .kpi-grid {
    grid-template-columns: 1fr;
  }
}

.kpi-card {
  background: var(--va-background-card, #ffffff);
  border: 1px solid var(--va-background-element, #e2e8f0);
  border-radius: 18px;
  padding: 1.5rem 1.75rem;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 185px;
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
  font-size: 0.95rem;
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

.metric-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 0.35rem;
  padding: 0.5rem 0;
  flex: 1;
}

.metric-value {
  font-size: 3.6rem;
  font-weight: 900;
  letter-spacing: -1.5px;
  line-height: 1;
  text-align: center;
}

.blue-text { color: #2c82e0; }
.red-text { color: #e4233c; }
.green-text { color: #10b981; }

.metric-subtext {
  font-size: 0.92rem;
  color: var(--va-text-secondary);
  font-weight: 600;
  text-align: center;
}

.metric-subtext.has-pending {
  color: #e4233c;
  font-weight: 700;
}

/* Content Grid Section */
.content-grid {
  display: grid;
  grid-template-columns: 2fr 1.2fr;
  gap: 1.5rem;
  margin-bottom: 1.75rem;
  width: 100%;
}

@media (max-width: 1100px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

.section-card {
  border-radius: 16px;
  border: 1px solid var(--va-background-element, #e2e8f0);
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
.empty-todo-state, .empty-state {
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
  max-height: 400px;
  overflow-y: auto;
}

.todo-item-card {
  border: 1px solid var(--va-background-element, #e2e8f0);
  background: var(--va-background-element, #f8fafc);
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
  background: var(--va-background-card, #ffffff);
  border: 1px dashed var(--va-background-element, #cbd5e1);
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

/* Table Styling */
.table-responsive {
  overflow-x: auto;
  border-radius: 10px;
  border: 1px solid var(--va-background-element, #e2e8f0);
}

.custom-dashboard-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.88rem;
}

.custom-dashboard-table th {
  background: var(--va-background-element, #f8fafc);
  color: var(--va-text-secondary);
  font-weight: 700;
  text-transform: uppercase;
  font-size: 0.75rem;
  letter-spacing: 0.5px;
  padding: 0.9rem 1rem;
  text-align: left;
  border-bottom: 2px solid var(--va-background-element, #e2e8f0);
}

.custom-dashboard-table td {
  padding: 0.85rem 1rem;
  border-bottom: 1px solid var(--va-background-element, #f1f5f9);
  vertical-align: middle;
}

.table-row:hover {
  background: rgba(44, 130, 224, 0.03);
}

.req-id-cell {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 700;
  color: #2c82e0;
}

.date-cell {
  font-size: 0.82rem;
  color: var(--va-text-secondary);
}
</style>
