<template>
  <div class="dq-dashboard-container">
    <!-- Header Banner -->
    <div class="dashboard-header-card">
      <div class="header-content">
        <div class="header-title-group">
          <div class="header-icon-wrapper">
            <va-icon name="analytics" size="2rem" color="primary" />
          </div>
          <div>
            <h1 class="header-title">{{ $t('dq_dashboard_title') || 'Data Quality Dashboard' }}</h1>
            <p class="header-subtitle">{{ $t('dq_dashboard_subtitle') || 'Real-time Master Data Governance & Integrity Monitoring' }}</p>
          </div>
        </div>

        <div class="header-actions">
          <va-select
            v-model="selectedDomainId"
            :options="domains"
            label="Select Domain"
            text-by="label"
            value-by="value"
            class="domain-select"
            placeholder="도메인을 선택하세요"
          />
          <va-button
            v-if="selectedDomainId"
            icon="autorenew"
            color="primary"
            class="scan-btn"
            :loading="scanning"
            @click="triggerScan"
          >
            {{ $t('run_dq_scan') || 'Run DQ Scan' }}
          </va-button>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <va-progress-circle indeterminate size="3.5rem" color="primary" />
      <span class="loading-text">{{ $t('loading_dq_metrics') || 'Loading Quality Metrics...' }}</span>
    </div>

    <!-- Score & Stats -->
    <template v-else-if="scoreData">
      <!-- 4 Core Metric KPI Cards -->
      <div class="kpi-grid">
        <!-- DQ Score Card -->
        <div class="kpi-card score-card" :class="getScoreClass(scoreData.score)">
          <div class="kpi-card-header">
            <span class="kpi-title">{{ $t('dq_score_title') || 'Data Quality Score' }}</span>
            <va-badge :text="getGradeLabel(scoreData.score)" :color="getScoreColor(scoreData.score)" />
          </div>
          <div class="score-body">
            <va-progress-circle
              :model-value="scoreData.score"
              :color="getScoreColor(scoreData.score)"
              size="7.5rem"
              :thickness="0.16"
            >
              <div class="score-gauge-inner">
                <span class="score-number" :style="{ color: `var(--va-${getScoreColor(scoreData.score)})` }">
                  {{ scoreData.score }}<span class="score-percent">%</span>
                </span>
              </div>
            </va-progress-circle>
          </div>
        </div>

        <!-- Total Records Card -->
        <div class="kpi-card metric-card records-kpi">
          <div class="kpi-card-header">
            <span class="kpi-title">Total Records</span>
            <div class="kpi-icon-pill blue-pill">
              <va-icon name="dataset" size="medium" />
            </div>
          </div>
          <div class="metric-body">
            <div class="metric-value blue-text">
              {{ scoreData.totalRecords?.toLocaleString() ?? 0 }}
            </div>
            <div class="metric-subtext">Monitored Entities in Domain</div>
          </div>
        </div>

        <!-- Total Violations Card -->
        <div class="kpi-card metric-card violations-kpi">
          <div class="kpi-card-header">
            <span class="kpi-title">Total Violations</span>
            <div class="kpi-icon-pill red-pill">
              <va-icon name="warning" size="medium" />
            </div>
          </div>
          <div class="metric-body">
            <div class="metric-value red-text">
              {{ scoreData.totalViolations?.toLocaleString() ?? 0 }}
            </div>
            <div class="metric-subtext" :class="{ 'has-violations': scoreData.totalViolations > 0 }">
              {{ scoreData.totalViolations > 0 ? '⚠️ Action Required' : '✅ All Records Passed' }}
            </div>
          </div>
        </div>

        <!-- Active Rules Card -->
        <div class="kpi-card metric-card rules-kpi">
          <div class="kpi-card-header">
            <span class="kpi-title">Active DQ Rules</span>
            <div class="kpi-icon-pill gold-pill">
              <va-icon name="verified_user" size="medium" />
            </div>
          </div>
          <div class="metric-body">
            <div class="metric-value gold-text">
              {{ ruleCount }}
            </div>
            <div class="metric-subtext">Automated Inspection Rules</div>
          </div>
        </div>
      </div>

      <!-- DQ Score Trend Card -->
      <va-card class="trend-card mb-6">
        <va-card-title class="card-header-title flex items-center justify-between">
          <div class="flex items-center gap-2">
            <va-icon name="show_chart" size="small" color="primary" />
            <span>DQ 품질 점수 트렌드 히스토리 (DQ Score Trend)</span>
          </div>
          <div class="flex items-center gap-2">
            <va-button-toggle
              v-model="trendPeriod"
              preset="secondary"
              border-color="primary"
              size="small"
              :options="[
                { label: '최근 7일', value: 7 },
                { label: '최근 30일', value: 30 },
                { label: '최근 90일', value: 90 },
                { label: '전체 (Recent)', value: 0 }
              ]"
              @update:modelValue="onTrendPeriodChange"
            />
            <span class="text-xs text-gray-400 font-normal">조회 {{ recentSnapshots.length }}건</span>
          </div>
        </va-card-title>
        <va-card-content>
          <div v-if="recentSnapshots.length === 0" class="text-sm text-gray-400 py-6 text-center">
            아직 기록된 DQ 스냅샷이 없습니다. "Run DQ Scan" 버튼을 클릭해 스캔을 실행하세요.
          </div>
          <div v-else class="space-y-3">
            <!-- Trend Bar Sparkline / Points -->
            <div class="flex items-end gap-2 h-28 pt-4 pb-1 px-2 border-b border-gray-100 dark:border-gray-800 overflow-x-auto">
              <div
                v-for="(snap, idx) in recentSnapshots"
                :key="snap.id || idx"
                class="flex-1 min-w-[36px] flex flex-col items-center gap-1 group relative cursor-pointer"
              >
                <!-- Tooltip -->
                <div class="absolute bottom-full mb-2 hidden group-hover:flex flex-col items-center bg-gray-900 text-white text-xs rounded px-2 py-1 z-20 whitespace-nowrap shadow-lg">
                  <span class="font-bold">{{ snap.score }}점</span>
                  <span class="text-[10px] text-gray-300">{{ formatDate(snap.recordedAt) }} ({{ snap.scanType }})</span>
                  <span class="text-[10px] text-gray-400">위반 {{ snap.totalViolations }}건 / 전체 {{ snap.totalRecords }}행</span>
                </div>
                <!-- Bar height based on score % -->
                <span class="text-[10px] font-semibold text-gray-500 group-hover:text-blue-600 dark:text-gray-400">{{ Math.round(snap.score) }}%</span>
                <div class="w-full bg-gray-100 dark:bg-gray-800 rounded-t h-20 flex items-end overflow-hidden">
                  <div
                    class="w-full rounded-t transition-all duration-300"
                    :class="snap.score >= 90 ? 'bg-emerald-500' : snap.score >= 70 ? 'bg-amber-500' : 'bg-rose-500'"
                    :style="{ height: `${Math.max(snap.score, 5)}%` }"
                  ></div>
                </div>
                <span class="text-[9px] text-gray-400 font-mono truncate w-full text-center">
                  {{ formatDateShort(snap.recordedAt) }}
                </span>
              </div>
            </div>
          </div>
        </va-card-content>
      </va-card>

      <!-- Breakdown Analytics Section -->
      <div class="analytics-grid">
        <!-- Violations by Severity -->
        <va-card class="analytics-card">
          <va-card-title class="card-header-title">
            <va-icon name="pie_chart" size="small" color="primary" />
            Violations by Severity
          </va-card-title>
          <va-card-content>
            <div v-if="Object.keys(scoreData.violationsBySeverity || {}).length === 0" class="empty-state">
              <span class="empty-icon">🎉</span>
              <p>No violations detected! Perfect data quality.</p>
            </div>
            <div v-else class="severity-list">
              <div
                v-for="(count, severity) in scoreData.violationsBySeverity"
                :key="severity"
                class="severity-item"
                :class="{ active: filterSeverity === severity }"
                @click="filterSeverity = filterSeverity === severity ? '' : severity"
              >
                <div class="severity-item-header">
                  <va-badge
                    :text="severity"
                    :color="severity === 'ERROR' ? 'danger' : 'warning'"
                    class="severity-badge"
                  />
                  <span class="severity-count">{{ count }}건 ({{ getPercentage(count, scoreData.totalViolations) }}%)</span>
                </div>
                <va-progress-bar
                  :model-value="scoreData.totalViolations > 0 ? (count / scoreData.totalViolations * 100) : 0"
                  :color="severity === 'ERROR' ? 'danger' : 'warning'"
                  class="progress-bar-styled"
                />
              </div>
            </div>
          </va-card-content>
        </va-card>

        <!-- Violations by Field -->
        <va-card class="analytics-card">
          <va-card-title class="card-header-title">
            <va-icon name="bar_chart" size="small" color="primary" />
            Violations by Field
          </va-card-title>
          <va-card-content>
            <div v-if="Object.keys(scoreData.violationsByField || {}).length === 0" class="empty-state">
              <span class="empty-icon">🎉</span>
              <p>No field violations detected!</p>
            </div>
            <div v-else class="field-list">
              <div
                v-for="(count, fieldKey, index) in sortedFieldViolations"
                :key="fieldKey"
                class="field-item"
                :class="{ active: filterFieldKey === fieldKey }"
                @click="filterFieldKey = filterFieldKey === fieldKey ? '' : fieldKey"
              >
                <div class="field-info">
                  <span class="field-rank">#{{ index + 1 }}</span>
                  <span class="field-name">{{ getFieldDisplayName(fieldKey) }}</span>
                  <span class="field-count-badge">{{ count }}</span>
                </div>
                <va-progress-bar
                  :model-value="maxFieldViolation > 0 ? (count / maxFieldViolation * 100) : 0"
                  color="danger"
                  class="progress-bar-styled"
                />
              </div>
            </div>
          </va-card-content>
        </va-card>
      </div>

      <!-- Violation Details Table Section -->
      <va-card class="table-card">
        <va-card-title class="table-card-title">
          <div class="table-title-group">
            <div class="title-icon-box">
              <va-icon name="report_problem" color="danger" size="medium" />
            </div>
            <div>
              <div class="table-main-title">DQ 위반 상세 레코드 목록</div>
              <div class="table-sub-title">Detailed Violation Records List</div>
            </div>
          </div>

          <!-- Filters Bar -->
          <div class="filters-toolbar">
            <va-select
              v-model="filterSeverity"
              :options="severityOptions"
              text-by="label"
              value-by="value"
              placeholder="심각도 전체"
              class="filter-select"
              clearable
            />
            <va-select
              v-model="filterFieldKey"
              :options="availableFieldFilterOptions"
              text-by="label"
              value-by="value"
              placeholder="필드 전체"
              class="filter-select-wide"
              clearable
            />
            <va-button
              v-if="filterSeverity || filterFieldKey"
              size="small"
              preset="secondary"
              icon="clear"
              @click="resetViolationFilters"
            >
              초기화
            </va-button>
          </div>
        </va-card-title>

        <va-card-content class="table-content">
          <div v-if="loadingViolations" class="table-loading">
            <va-progress-circle indeterminate size="2rem" />
            <span>위반 레코드 조회 중...</span>
          </div>

          <div v-else-if="!violationList || violationList.length === 0" class="empty-state">
            <span class="empty-icon">🎉</span>
            <p>선택한 조건의 품질 위반 레코드가 없습니다.</p>
          </div>

          <template v-else>
            <div class="table-responsive">
              <table class="custom-dq-table">
                <thead>
                  <tr>
                    <th>레코드 식별자</th>
                    <th>분류 노드</th>
                    <th>위반 필드</th>
                    <th>심각도</th>
                    <th>위반 사유</th>
                    <th>입력된 값</th>
                    <th>검증 시각</th>
                    <th class="text-center">이동</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="v in violationList" :key="v.id" class="table-row">
                    <td>
                      <div class="record-id-cell">
                        <va-icon name="folder" size="small" color="primary" />
                        <span>{{ v.recordIdentifier || v.recordId }}</span>
                      </div>
                    </td>
                    <td>
                      <span class="node-cell">{{ getNodeDisplayName(v.nodeName) }}</span>
                    </td>
                    <td>
                      <va-chip size="small" preset="outline" class="field-chip">
                        {{ getFieldDisplayName(v.fieldKey) }}
                      </va-chip>
                    </td>
                    <td>
                      <va-badge
                        :text="v.severity"
                        :color="v.severity === 'ERROR' ? 'danger' : 'warning'"
                        class="severity-badge-table"
                      />
                    </td>
                    <td>
                      <span class="violation-msg">{{ getViolationMessage(v.message) }}</span>
                    </td>
                    <td>
                      <code class="actual-value-code">
                        {{ v.actualValue || '(빈 값)' }}
                      </code>
                    </td>
                    <td>
                      <span class="date-cell">{{ formatDate(v.checkedAt) }}</span>
                    </td>
                    <td class="text-center">
                      <va-button
                        size="small"
                        color="primary"
                        icon="launch"
                        class="goto-btn"
                        @click="goToRecord(v.recordId)"
                      >
                        상세
                      </va-button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Custom Pagination Toolbar -->
            <div class="pagination-toolbar">
              <span class="pagination-info">
                총 <strong>{{ totalViolationsCount.toLocaleString() }}</strong>건 중
                <strong>{{ (violationPage * violationSize) + 1 }}</strong> -
                <strong>{{ Math.min((violationPage + 1) * violationSize, totalViolationsCount) }}</strong>건 표시
              </span>

              <div class="pagination-controls">
                <va-button
                  size="small"
                  preset="secondary"
                  icon="chevron_left"
                  :disabled="violationPage === 0"
                  @click="violationPage--"
                />
                <span class="page-badge">
                  {{ violationPage + 1 }} / {{ totalViolationPages || 1 }}
                </span>
                <va-button
                  size="small"
                  preset="secondary"
                  icon="chevron_right"
                  :disabled="violationPage + 1 >= totalViolationPages"
                  @click="violationPage++"
                />
              </div>
            </div>
          </template>
        </va-card-content>
      </va-card>
    </template>

    <div v-else-if="selectedDomainId" class="select-domain-prompt">
      <va-icon name="touch_app" size="3rem" color="primary" />
      <p>조회할 도메인을 선택하면 Data Quality 지표가 표시됩니다.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'

const domains = ref([])
const selectedDomainId = ref(null)
const scoreData = ref(null)
const ruleCount = ref(0)
const loading = ref(false)
const scanning = ref(false)

const fieldMap = ref({})
const recentSnapshots = ref([])

// Violation Details State
const violationList = ref([])
const violationPage = ref(0)
const violationSize = ref(10)
const totalViolationsCount = ref(0)
const totalViolationPages = ref(0)
const loadingViolations = ref(false)
const filterSeverity = ref('')
const filterFieldKey = ref('')

const severityOptions = [
  { label: '전체 (All)', value: '' },
  { label: 'ERROR', value: 'ERROR' },
  { label: 'WARNING', value: 'WARNING' }
]

const { locale } = useI18n()
const token = useCookie('auth_token')

const getHeaders = () => {
  return token.value ? { Authorization: `Bearer ${token.value}` } : {}
}

async function fetchDomainFields(domainId) {
  try {
    const headers = getHeaders()
    const fields = await $fetch(`/api/domains/${domainId}/fields`, { headers })
    const map = {}
    for (const f of (fields || [])) {
      let nameObj = f.name
      if (typeof nameObj === 'string') {
        try { nameObj = JSON.parse(nameObj) } catch (e) {}
      }
      map[f.key] = nameObj
    }
    fieldMap.value = map
  } catch (e) {
    console.error('Failed to fetch domain fields:', e)
    fieldMap.value = {}
  }
}

function getFieldDisplayName(fieldKey) {
  const nameObj = fieldMap.value[fieldKey]
  if (!nameObj) return fieldKey

  const koName = typeof nameObj === 'object' ? nameObj.ko : null
  const enName = typeof nameObj === 'object' ? nameObj.en : null

  const primaryName = locale.value === 'ko'
    ? (koName || enName)
    : (enName || koName)

  if (primaryName && primaryName !== fieldKey) {
    return `${primaryName} (${fieldKey})`
  }
  return fieldKey
}

function getNodeDisplayName(nodeName) {
  if (!nodeName) return '-'
  if (typeof nodeName === 'string') return nodeName
  return nodeName[locale.value] || nodeName.ko || nodeName.en || Object.values(nodeName)[0] || '-'
}

function getViolationMessage(msgMap) {
  if (!msgMap) return '품질 검증 규칙 위반'
  if (typeof msgMap === 'string') return msgMap
  return msgMap[locale.value] || msgMap.ko || msgMap.en || Object.values(msgMap)[0] || '품질 검증 규칙 위반'
}

function formatDate(dtStr) {
  if (!dtStr) return '-'
  try {
    const d = new Date(dtStr)
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  } catch (e) {
    return dtStr
  }
}

function formatDateShort(dtStr) {
  if (!dtStr) return ''
  try {
    const d = new Date(dtStr)
    return `${String(d.getMonth() + 1).padStart(2, '0')}/${String(d.getDate()).padStart(2, '0')}`
  } catch (e) {
    return ''
  }
}

function getPercentage(count, total) {
  if (!total || total === 0) return 0
  return Math.round((count / total) * 100)
}

const availableFieldFilterOptions = computed(() => {
  const keys = Object.keys(scoreData.value?.violationsByField || {})
  return [
    { label: '전체 필드 (All)', value: '' },
    ...keys.map(k => ({
      label: getFieldDisplayName(k),
      value: k
    }))
  ]
})

function resetViolationFilters() {
  filterSeverity.value = ''
  filterFieldKey.value = ''
  violationPage.value = 0
}

async function fetchViolations() {
  const val = selectedDomainId.value
  const domainId = typeof val === 'object' && val !== null ? (val.value || val.id) : val
  if (!domainId) {
    violationList.value = []
    return
  }

  loadingViolations.value = true
  try {
    const headers = getHeaders()
    let url = `/api/domains/${domainId}/dq-violations?page=${violationPage.value}&size=${violationSize.value}`
    if (filterSeverity.value) url += `&severity=${encodeURIComponent(filterSeverity.value)}`
    if (filterFieldKey.value) url += `&fieldKey=${encodeURIComponent(filterFieldKey.value)}`

    const res = await $fetch(url, { headers })
    violationList.value = res.content || []
    totalViolationsCount.value = res.totalElements || 0
    totalViolationPages.value = res.totalPages || 0
  } catch (e) {
    console.error('Failed to fetch violations:', e)
    violationList.value = []
  } finally {
    loadingViolations.value = false
  }
}

const trendPeriod = ref(30)

async function fetchRecentSnapshots(domainId) {
  try {
    const headers = getHeaders()
    let url = `/api/domains/${domainId}/dq-score/recent`

    if (trendPeriod.value > 0) {
      const now = new Date()
      const fromDate = new Date(now.getTime() - trendPeriod.value * 24 * 60 * 60 * 1000)
      url = `/api/domains/${domainId}/dq-score/trend?from=${fromDate.toISOString()}&to=${now.toISOString()}`
    }

    const list = await $fetch(url, { headers })
    recentSnapshots.value = list || []
  } catch (e) {
    console.error('Failed to fetch DQ score trend snapshots:', e)
    recentSnapshots.value = []
  }
}

function onTrendPeriodChange() {
  const val = selectedDomainId.value
  const domainId = typeof val === 'object' && val !== null ? (val.value || val.id) : val
  if (domainId) {
    fetchRecentSnapshots(domainId)
  }
}

async function triggerScan() {
  const val = selectedDomainId.value
  const domainId = typeof val === 'object' && val !== null ? (val.value || val.id) : val
  if (!domainId) return

  scanning.value = true
  try {
    const headers = getHeaders()
    const score = await $fetch(`/api/domains/${domainId}/dq-scan`, { method: 'POST', headers })
    scoreData.value = score
    violationPage.value = 0
    await Promise.all([fetchViolations(), fetchRecentSnapshots(domainId)])
  } catch (e) {
    console.error('DQ scan error:', e)
  } finally {
    scanning.value = false
  }
}

const router = useRouter()
function goToRecord(recordId) {
  if (recordId) {
    router.push(`/records?recordId=${recordId}`)
  }
}

onMounted(async () => {
  try {
    const res = await $fetch('/api/domains', { headers: getHeaders() })
    domains.value = (res || []).map(d => {
      let nameObj = d.name
      if (typeof nameObj === 'string') {
        try { nameObj = JSON.parse(nameObj) } catch (e) {}
      }
      const labelText = typeof nameObj === 'object' && nameObj !== null
        ? (nameObj[locale.value] || nameObj.ko || nameObj.en || d.code || 'Unknown')
        : (nameObj || d.code || 'Unknown')

      return {
        label: labelText,
        value: d.id
      }
    })
  } catch (e) {
    console.error('Failed to fetch domains:', e)
  }
})

watch(selectedDomainId, async (val) => {
  if (!val) {
    scoreData.value = null
    fieldMap.value = {}
    violationList.value = []
    return
  }
  const domainId = typeof val === 'object' && val !== null ? (val.value || val.id) : val
  if (!domainId) return

  loading.value = true
  violationPage.value = 0
  filterSeverity.value = ''
  filterFieldKey.value = ''

  try {
    const headers = getHeaders()
    await fetchDomainFields(domainId)
    const [score, rules] = await Promise.all([
      $fetch(`/api/domains/${domainId}/dq-score`, { headers }),
      $fetch(`/api/domains/${domainId}/dq-rules-count`, { headers }).catch((e) => {
        console.error('Failed to fetch dq-rules-count:', e)
        return { count: 0 }
      })
    ])
    scoreData.value = score
    ruleCount.value = rules?.count ?? 0
    await Promise.all([fetchViolations(), fetchRecentSnapshots(domainId)])
  } catch (e) {
    console.error('Failed to fetch DQ score:', e)
    scoreData.value = null
  } finally {
    loading.value = false
  }
})

watch([violationPage, filterSeverity, filterFieldKey], () => {
  fetchViolations()
})

const sortedFieldViolations = computed(() => {
  if (!scoreData.value?.violationsByField) return {}
  const entries = Object.entries(scoreData.value.violationsByField)
  entries.sort((a, b) => b[1] - a[1])
  return Object.fromEntries(entries)
})

const maxFieldViolation = computed(() => {
  if (!scoreData.value?.violationsByField) return 0
  return Math.max(...Object.values(scoreData.value.violationsByField), 1)
})

function getScoreColor(score) {
  if (score >= 90) return 'success'
  if (score >= 70) return 'warning'
  return 'danger'
}

function getGradeLabel(score) {
  if (score >= 90) return 'Grade A (Excellent)'
  if (score >= 70) return 'Grade B (Good)'
  return 'Grade C (Critical)'
}

function getScoreClass(score) {
  if (score >= 90) return 'grade-a'
  if (score >= 70) return 'grade-b'
  return 'grade-c'
}
</script>

<style scoped>
.dq-dashboard-container {
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.domain-select {
  width: 320px;
}

.scan-btn {
  height: 40px;
  font-weight: 700;
  border-radius: 10px;
  padding: 0 1.25rem;
  box-shadow: 0 4px 14px rgba(44, 130, 224, 0.35);
  transition: all 0.2s ease;
}

.scan-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(44, 130, 224, 0.45);
}

/* Loading State */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 1rem;
  gap: 1rem;
}

.loading-text {
  font-size: 1rem;
  font-weight: 600;
  color: var(--va-text-secondary);
}

/* KPI Grid - Full 4 Columns */
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

@media (max-width: 640px) {
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
.gold-pill { background: rgba(245, 158, 11, 0.12); color: #f59e0b; }

.score-body {
  display: flex;
  justify-content: center;
  padding: 0.5rem 0;
}

.score-gauge-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.score-number {
  font-size: 3.2rem;
  font-weight: 900;
  letter-spacing: -1.5px;
  line-height: 1;
}

.score-percent {
  font-size: 1.5rem;
  font-weight: 700;
  margin-left: 2px;
}

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
.gold-text { color: #f59e0b; }

.metric-subtext {
  font-size: 0.92rem;
  color: var(--va-text-secondary);
  font-weight: 600;
  text-align: center;
}

.metric-subtext.has-violations {
  color: #e4233c;
  font-weight: 700;
}

/* Analytics Breakdown Section */
.analytics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  margin-bottom: 1.75rem;
}

@media (max-width: 900px) {
  .analytics-grid { grid-template-columns: 1fr; }
}

.analytics-card {
  border-radius: 16px;
  border: 1px solid var(--va-background-element, #e2e8f0);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.card-header-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.05rem;
  font-weight: 700;
  padding: 1.25rem 1.5rem 0.5rem 1.5rem;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2.5rem 1rem;
  color: var(--va-text-secondary);
  text-align: center;
}

.empty-icon {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
}

.severity-list, .field-list {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  padding: 0.5rem 0;
}

.severity-item, .field-item {
  padding: 0.75rem 1rem;
  background: var(--va-background-element, #f8fafc);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 2px solid transparent;
}

.severity-item:hover, .field-item:hover {
  background: rgba(44, 130, 224, 0.05);
  transform: translateX(4px);
}

.severity-item.active, .field-item.active {
  border-color: #2c82e0;
  background: rgba(44, 130, 224, 0.08);
}

.severity-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.severity-badge {
  font-weight: 700;
}

.severity-count {
  font-weight: 700;
  font-size: 0.9rem;
  color: var(--va-text-primary);
}

.field-info {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin-bottom: 0.5rem;
}

.field-rank {
  font-size: 0.75rem;
  font-weight: 800;
  color: #2c82e0;
  background: rgba(44, 130, 224, 0.12);
  padding: 2px 6px;
  border-radius: 6px;
}

.field-name {
  flex: 1;
  font-weight: 600;
  font-size: 0.88rem;
  color: var(--va-text-primary);
}

.field-count-badge {
  font-weight: 800;
  font-size: 0.85rem;
  color: #e4233c;
  background: rgba(228, 35, 60, 0.1);
  padding: 2px 8px;
  border-radius: 12px;
}

.progress-bar-styled {
  height: 8px;
  border-radius: 4px;
}

/* Violation Details Table Section */
.table-card {
  border-radius: 16px;
  border: 1px solid var(--va-background-element, #e2e8f0);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.table-card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--va-background-element, #e2e8f0);
}

.table-title-group {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.title-icon-box {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba(228, 35, 60, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.table-main-title {
  font-size: 1.15rem;
  font-weight: 800;
  color: var(--va-text-primary);
  line-height: 1.2;
}

.table-sub-title {
  font-size: 0.8rem;
  color: var(--va-text-secondary);
}

.filters-toolbar {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.filter-select { width: 150px; }
.filter-select-wide { width: 220px; }

.table-content {
  padding: 1rem 1.5rem 1.5rem 1.5rem;
}

.table-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 3rem;
  color: var(--va-text-secondary);
  font-weight: 600;
}

.table-responsive {
  overflow-x: auto;
  border-radius: 10px;
  border: 1px solid var(--va-background-element, #e2e8f0);
}

.custom-dq-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.88rem;
}

.custom-dq-table th {
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

.custom-dq-table td {
  padding: 0.85rem 1rem;
  border-bottom: 1px solid var(--va-background-element, #f1f5f9);
  vertical-align: middle;
}

.table-row:hover {
  background: rgba(44, 130, 224, 0.03);
}

.record-id-cell {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 700;
  color: #2c82e0;
}

.node-cell {
  font-weight: 600;
  color: var(--va-text-primary);
}

.field-chip {
  font-weight: 600;
}

.severity-badge-table {
  font-weight: 700;
  padding: 4px 8px;
}

.violation-msg {
  color: #e4233c;
  font-weight: 600;
}

.actual-value-code {
  background: var(--va-background-element, #f1f5f9);
  padding: 3px 8px;
  border-radius: 6px;
  font-family: monospace;
  font-size: 0.82rem;
  color: var(--va-text-primary);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.date-cell {
  font-size: 0.82rem;
  color: var(--va-text-secondary);
  white-space: nowrap;
}

.goto-btn {
  font-weight: 700;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.goto-btn:hover {
  transform: scale(1.05);
}

.pagination-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1.25rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.pagination-info {
  font-size: 0.85rem;
  color: var(--va-text-secondary);
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.page-badge {
  font-weight: 700;
  font-size: 0.88rem;
  padding: 0 0.5rem;
  color: var(--va-text-primary);
}

.select-domain-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 5rem 1rem;
  gap: 1rem;
  color: var(--va-text-secondary);
  font-size: 1.1rem;
  font-weight: 600;
  text-align: center;
}
</style>
