<template>
  <div style="display: flex; flex-direction: column; gap: 1.25rem; padding-bottom: 2rem;">
    <!-- Top Action Bar -->
    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); padding: 1rem 1.25rem; border-radius: 12px; border: 1px solid var(--va-background-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04); flex-wrap: wrap; gap: 0.75rem;">
      <div style="display: flex; align-items: center; gap: 0.75rem;">
        <va-icon name="terminal" size="large" color="primary" />
        <div>
          <h2 style="font-weight: 700; font-size: 1.35rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
            {{ pageTitle }}
            <va-badge text="Audit & Logs" color="primary" size="small" />
          </h2>
          <span style="font-size: 0.85rem; color: var(--va-text-secondary);">
            {{ $t('system_logs_desc') }}
          </span>
        </div>
      </div>

      <!-- Governance & Orchestration Action Group -->
      <div style="display: flex; align-items: center; gap: 0.4rem; flex-wrap: wrap;">
        <va-button 
          v-for="feature in governanceFeatures" 
          :key="feature.featureNo"
          :color="feature.colorTheme.replace('-outline', '')" 
          size="small" 
          :outline="feature.colorTheme.includes('-outline')"
          :icon="feature.iconName" 
          @click="openFeatureModal(feature.featureNameKey)">
          {{ $t(feature.featureNameKey) }}
        </va-button>
      </div>
    </div>

    <div class="menu-logs-container">
      <!-- Tab Navigation -->
      <va-tabs v-model="activeTab" class="mb-4" style="border-bottom: 1px solid var(--va-background-border);">
      <template #tabs>
        <va-tab name="access">Menu Access Logs</va-tab>
        <va-tab name="login">Login Logs</va-tab>
        <va-tab name="error">Error Logs</va-tab>
        <va-tab name="integration">Integration Logs</va-tab>
        <va-tab name="sensitive">{{ $t('sensitive_access_logs') }}</va-tab>
      </template>
    </va-tabs>

    <!-- 1. Menu Access Logs Tab -->
    <div v-if="activeTab === 'access'">
      <va-card class="mb-4">
        <va-card-title>
          <div class="flex justify-between items-center w-full">
            <h2 style="text-transform: none; font-size: 1.2rem; margin: 0; color: var(--va-dark);">{{ $t('menu_access_statistics') }}</h2>
            <va-button-toggle
              v-model="accessChartPeriod"
              preset="secondary"
              border-color="primary"
              :options="[
                { label: 'Daily', value: 'daily' },
                { label: 'Monthly', value: 'monthly' },
                { label: 'Yearly', value: 'yearly' }
              ]"
              @update:model-value="updateChart"
            />
          </div>
        </va-card-title>
        <va-card-content>
          <div style="height: 300px; width: 100%;">
            <client-only>
              <v-chart v-if="isMounted" :option="chartOption" autoresize />
            </client-only>
          </div>
        </va-card-content>
      </va-card>

      <va-card>
        <div style="background-color: var(--va-background-element); padding: 0.6rem 1rem; border-top-left-radius: 8px; border-top-right-radius: 8px; display: flex; justify-content: flex-end; align-items: center;">
          <va-button preset="plain" color="secondary" size="small" icon="refresh" @click="refreshGrid">{{ $t('refresh') }}</va-button>
        </div>
        <va-card-content>
          <div :class="{ 'ag-theme-quartz-dark': isDark }" style="height: 500px; width: 100%; min-height: 300px;">
            <client-only>
              <ag-grid-vue
                v-if="isMounted"
                style="width: 100%; height: 100%;"
                :theme="gridTheme"
                :columnDefs="columnDefs"
                :rowModelType="'serverSide'"
                :serverSideDatasource="datasource"
                :pagination="true"
                :paginationPageSize="20"
                :cacheBlockSize="20"
                @grid-ready="onGridReady"
              >
              </ag-grid-vue>
            </client-only>
          </div>
        </va-card-content>
      </va-card>
    </div>

    <!-- 2. Login Logs Tab -->
    <div v-if="activeTab === 'login'">
      <va-card class="mb-4">
        <va-card-title>
          <div class="flex justify-between items-center w-full">
            <h2 style="text-transform: none; font-size: 1.2rem; margin: 0; color: var(--va-dark);">Login Frequency Statistics</h2>
            <va-button-toggle
              v-model="loginChartPeriod"
              preset="secondary"
              border-color="primary"
              :options="[
                { label: 'Daily', value: 'daily' },
                { label: 'Monthly', value: 'monthly' },
                { label: 'Yearly', value: 'yearly' }
              ]"
              @update:model-value="updateLoginChart"
            />
          </div>
        </va-card-title>
        <va-card-content>
          <div style="height: 300px; width: 100%;">
            <client-only>
              <v-chart v-if="isMounted" :option="loginChartOption" autoresize />
            </client-only>
          </div>
        </va-card-content>
      </va-card>

      <va-card>
        <div style="background-color: var(--va-background-element); padding: 0.6rem 1rem; border-top-left-radius: 8px; border-top-right-radius: 8px; display: flex; justify-content: flex-end; align-items: center;">
          <va-button preset="plain" color="secondary" size="small" icon="refresh" @click="refreshLoginGrid">{{ $t('refresh') }}</va-button>
        </div>
        <va-card-content>
          <div :class="{ 'ag-theme-quartz-dark': isDark }" style="height: 500px; width: 100%; min-height: 300px;">
            <client-only>
              <ag-grid-vue
                v-if="isMounted"
                style="width: 100%; height: 100%;"
                :theme="gridTheme"
                :columnDefs="loginColumnDefs"
                :rowModelType="'serverSide'"
                :serverSideDatasource="loginDatasource"
                :pagination="true"
                :paginationPageSize="20"
                :cacheBlockSize="20"
                @grid-ready="onLoginGridReady"
              >
              </ag-grid-vue>
            </client-only>
          </div>
        </va-card-content>
      </va-card>
    </div>

    <!-- 3. Error Logs Tab -->
    <div v-if="activeTab === 'error'">
      <va-card>
        <div style="background-color: var(--va-background-element); padding: 0.6rem 1rem; border-top-left-radius: 8px; border-top-right-radius: 8px; display: flex; justify-content: flex-end; align-items: center;">
          <va-button preset="plain" color="secondary" size="small" icon="refresh" @click="refreshErrorGrid">{{ $t('refresh') }}</va-button>
        </div>
        <va-card-content>
          <div class="mb-2" style="font-size: 0.85rem; color: var(--va-text-secondary);">
            * Double click on any row to view full stack trace details.
          </div>
          <div :class="{ 'ag-theme-quartz-dark': isDark }" style="height: 600px; width: 100%; min-height: 300px;">
            <client-only>
              <ag-grid-vue
                v-if="isMounted"
                style="width: 100%; height: 100%;"
                :theme="gridTheme"
                :columnDefs="errorColumnDefs"
                :rowModelType="'serverSide'"
                :serverSideDatasource="errorDatasource"
                :pagination="true"
                :paginationPageSize="20"
                :cacheBlockSize="20"
                @grid-ready="onErrorGridReady"
                @row-double-clicked="onRowDoubleClicked"
              >
              </ag-grid-vue>
            </client-only>
          </div>
        </va-card-content>
      </va-card>
    </div>

    <!-- 4. Integration Logs Tab -->
    <div v-if="activeTab === 'integration'">
      <va-card>
        <!-- 프리미엄 헤더 영역 -->
        <div class="integration-header">
          <div class="integration-header-left">
            <div class="integration-header-icon">
              <va-icon name="hub" size="22px" color="primary" />
            </div>
            <div>
              <div class="integration-header-title">Integration Monitoring Logs</div>
              <div class="integration-header-sub">채널별 인테그레이션 이력 및 DLQ 재시도 현황</div>
            </div>
          </div>

          <!-- 필터 컨트롤 그룹 -->
          <div class="integration-toolbar">
            <!-- 채널 필터 -->
            <div class="toolbar-group">
              <span class="toolbar-label">
                <va-icon name="cable" size="14px" />
                채널
              </span>
              <va-select
                v-model="selectedChannelId"
                :options="channelOptions"
                value-by="id"
                text-by="name"
                placeholder="All Channels"
                clearable
                size="small"
                class="channel-select"
                @update:modelValue="fetchIntegrationLogs(1)"
              />
            </div>

            <!-- 구분선 -->
            <div class="toolbar-divider" />

            <!-- DLQ 토글 그룹 -->
            <div class="toolbar-group dlq-group" :class="{ 'dlq-active': isDlqOnly }">
              <va-icon name="warning_amber" size="16px" :color="isDlqOnly ? 'danger' : 'secondary'" />
              <span class="toolbar-label" :style="isDlqOnly ? 'color: var(--va-danger);' : ''">DLQ Only</span>
              <va-switch
                v-model="isDlqOnly"
                color="danger"
                size="small"
                @update:modelValue="fetchIntegrationLogs(1)"
              />
            </div>

            <!-- DLQ 전체 재시도 버튼 (조건부) -->
            <transition name="slide-fade">
              <va-button
                v-if="isDlqOnly"
                color="danger"
                icon="replay"
                size="small"
                :loading="isBulkRetrying"
                class="retry-all-btn"
                @click="retryAllDlq"
              >
                Retry All
              </va-button>
            </transition>

            <!-- 구분선 -->
            <div class="toolbar-divider" />

            <!-- 새로고침 -->
            <va-button
              preset="secondary"
              icon="refresh"
              size="small"
              class="refresh-btn"
              title="새로고침"
              @click="fetchIntegrationLogs(integrationCurrentPage)"
            />
          </div>
        </div>

        <va-card-content style="padding: 0;">
          <div :class="{ 'ag-theme-quartz-dark': isDark }" style="height: 560px; width: 100%; min-height: 300px;">
            <client-only>
              <ag-grid-vue
                v-if="isMounted"
                style="width: 100%; height: 100%;"
                :theme="gridTheme"
                :columnDefs="integrationColumnDefs"
                :rowModelType="'serverSide'"
                :serverSideDatasource="integrationDatasource"
                :pagination="true"
                :paginationPageSize="20"
                :cacheBlockSize="20"
                @grid-ready="onIntegrationGridReady"
                @row-double-clicked="onIntegrationRowDoubleClicked"
              />
            </client-only>
          </div>
        </va-card-content>
      </va-card>

      <!-- Integration Details Modal (Decoupled Component) -->
      <IntegrationLogDetailModal
        v-model="showIntegrationDetailsModal"
        :log="selectedIntegrationLog"
        :has-permission="hasPermission"
        @retry="retryIntegrationLog"
      />
    </div>

    <!-- Stack Trace Detail Modal -->
    <AppModal v-model="showErrorModal" title="Error Stack Trace Detail" icon="bug_report" size="large" hide-default-actions>
      <div v-if="selectedError" style="padding: 0.5rem 0; box-sizing: border-box; overflow: hidden;">
        <div class="mb-4">
          <strong>Request URI: </strong> <code>{{ selectedError.requestUri }}</code>
        </div>
        <div class="mb-4">
          <strong>Error Message: </strong> <span class="text-danger">{{ selectedError.errorMessage }}</span>
        </div>
        <div class="mb-4">
          <strong>Logged At: </strong> {{ formatWithTimezone(selectedError.loggedAt) }} (User: {{ selectedError.userId }})
        </div>
        <div>
          <strong>Stack Trace:</strong>
          <pre class="stack-trace-view">{{ selectedError.stackTrace }}</pre>
        </div>
        <div class="d-flex justify-end mt-4">
          <va-button preset="secondary" @click="showErrorModal = false">Close</va-button>
        </div>
      </div>
    </AppModal>
    <!-- 5. Sensitive Access Logs Tab -->
    <div v-if="activeTab === 'sensitive'">
      <!-- Decryption Logs Statistics Charts -->
      <div style="display: flex; gap: 1rem; margin-bottom: 1rem;">
        <va-card style="flex: 1;">
          <va-card-title>{{ $t('Decryption Trend (Last 7 Days)') }}</va-card-title>
          <va-card-content>
            <v-chart class="chart" :option="decryptionDailyChartOption" autoresize style="height: 300px; width: 100%;" />
          </va-card-content>
        </va-card>
        <va-card style="flex: 1;">
          <va-card-title>{{ $t('Type Ratios') }}</va-card-title>
          <va-card-content>
            <v-chart class="chart" :option="decryptionTypeChartOption" autoresize style="height: 300px; width: 100%;" />
          </va-card-content>
        </va-card>
        <va-card style="flex: 1;">
          <va-card-title>{{ $t('Top Users') }}</va-card-title>
          <va-card-content>
            <v-chart class="chart" :option="decryptionUserChartOption" autoresize style="height: 300px; width: 100%;" />
          </va-card-content>
        </va-card>
      </div>

      <va-card>
        <div style="background-color: var(--va-background-element); padding: 0.6rem 1rem; border-top-left-radius: 8px; border-top-right-radius: 8px; display: flex; justify-content: flex-end; align-items: center;">
          <va-button preset="plain" color="secondary" size="small" icon="refresh" @click="fetchSensitiveAccessLogs(1)">{{ $t('refresh') }}</va-button>
        </div>
        <va-card-content>
          <div style="height: 500px; width: 100%; min-height: 300px;">
              <ag-grid-vue
              style="width: 100%; height: 100%;"
              :class="{ 'ag-theme-quartz-dark': isDark }"
              :theme="gridTheme"
              :columnDefs="sensitiveLogColDefs"
              :rowData="sensitiveLogs"
              :loading="sensitiveLogLoading"
              :pagination="true"
              :paginationPageSize="20"
              @grid-ready="onSensitiveGridReady"
            />
          </div>
        </va-card-content>
      </va-card>
    </div>
  </div>

  <!-- Governance & System Orchestration Modals -->
  <GovernanceCopilotModal v-model="showCopilot" />
  <PipelineSelfHealingModal v-model="showSelfHealing" />
  <DataFreshnessHeatmapModal v-model="showFreshness" />
  <MultiRegionConflictModal v-model="showMultiRegion" />
  <MasterOrchestratorModal v-model="showMasterOrchestrator" />
  <MultiTenantRouterModal v-model="showMultiTenant" />
  <DataSlaContractModal v-model="showDataSla" />
  <GovernanceMaturityModal v-model="showMaturity" />
  <VolumeAnomalyRadarModal v-model="showVolumeRadar" />
  <RegulatoryComplianceModal v-model="showRegulatory" />
  <ColdStorageArchiveModal v-model="showColdStorage" />
  </div>
</template>

<script setup>
import { ref, watch, onMounted, computed } from 'vue'
import { AgGridVue } from 'ag-grid-vue3'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, GridComponent, LegendComponent } from 'echarts/components'
import { useI18n } from 'vue-i18n'
import { useToast } from 'vuestic-ui'
import { usePageTitle } from '~/composables/usePageTitle'
import { useAgGridTheme } from '~/composables/useAgGridTheme'
import { usePermission } from '~/composables/usePermission'
import { formatWithTimezone } from '~/composables/useTimezoneDate'
import { formatEntityId } from '~/utils/formatters'
import { getMultilingualText } from '~/utils/multilingual'
import { useCodeStore } from '~/stores/useCodeStore'
import IntegrationLogDetailModal from '~/components/admin/IntegrationLogDetailModal.vue'
import MasterOrchestratorModal from '~/components/admin/MasterOrchestratorModal.vue'
import MultiTenantRouterModal from '~/components/admin/MultiTenantRouterModal.vue'
import DataSlaContractModal from '~/components/admin/DataSlaContractModal.vue'
import GovernanceMaturityModal from '~/components/admin/GovernanceMaturityModal.vue'
import VolumeAnomalyRadarModal from '~/components/admin/VolumeAnomalyRadarModal.vue'
import RegulatoryComplianceModal from '~/components/admin/RegulatoryComplianceModal.vue'
import ColdStorageArchiveModal from '~/components/admin/ColdStorageArchiveModal.vue'
import GovernanceCopilotModal from '~/components/admin/GovernanceCopilotModal.vue'
import PipelineSelfHealingModal from '~/components/admin/PipelineSelfHealingModal.vue'
import DataFreshnessHeatmapModal from '~/components/admin/DataFreshnessHeatmapModal.vue'
import MultiRegionConflictModal from '~/components/admin/MultiRegionConflictModal.vue'
import AppModal from '~/components/common/AppModal.vue'

const { pageTitle } = usePageTitle('system_logs_title', '시스템 로그 및 연계 관제')

const showCopilot = ref(false)
const showSelfHealing = ref(false)
const showFreshness = ref(false)
const showMultiRegion = ref(false)
const showMasterOrchestrator = ref(false)
const showMultiTenant = ref(false)
const showDataSla = ref(false)
const showMaturity = ref(false)
const showVolumeRadar = ref(false)
const showRegulatory = ref(false)
const showColdStorage = ref(false)

const governanceFeatures = ref([])

const openFeatureModal = (featureKey) => {
  switch (featureKey) {
    case 'governance_copilot': showCopilot.value = true; break;
    case 'pipeline_self_healing': showSelfHealing.value = true; break;
    case 'freshness_heatmap': showFreshness.value = true; break;
    case 'multi_region_conflict': showMultiRegion.value = true; break;
    case 'master_orchestrator': showMasterOrchestrator.value = true; break;
    case 'multi_tenant': showMultiTenant.value = true; break;
    case 'data_sla': showDataSla.value = true; break;
    case 'governance_maturity': showMaturity.value = true; break;
    case 'volume_radar': showVolumeRadar.value = true; break;
    case 'regulatory_compliance': showRegulatory.value = true; break;
    case 'cold_storage': showColdStorage.value = true; break;
  }
}

const { hasPermission } = usePermission()

if (process.client) {
  use([CanvasRenderer, BarChart, LineChart, PieChart, TitleComponent, TooltipComponent, GridComponent, LegendComponent])
}

const { t, locale } = useI18n()
const { gridTheme, autoSizeStrategy, isDark } = useAgGridTheme()
const token = useCookie('auth_token')
const { init } = useToast()
const activeTab = ref('access')
const isMounted = ref(false)
const codeStore = useCodeStore()

const fetchGovernanceCoreFeatures = async () => {
  try {
    const res = await $fetch('/api/system/master-orchestrator', {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    const payload = res?.modules ? res : res?.data?.value
    if (payload && payload.modules) {
      // Sort by featureNo (or any preferred order) and filter core features
      governanceFeatures.value = payload.modules.filter(m => m.governanceCore).sort((a, b) => a.featureNo - b.featureNo)
    }
  } catch (err) {
    console.error('Failed to load core governance features', err)
  }
}

onMounted(() => {
  fetchGovernanceCoreFeatures()
})

const sensitiveLogs = ref([])
const sensitiveLogLoading = ref(false)

const sensitiveGridApi = ref(null)

const onSensitiveGridReady = (params) => {
  sensitiveGridApi.value = params.api
}

const sensitiveLogColDefs = computed(() => [
  {
    headerValueGetter: () => t('access_log_time'),
    field: 'accessedAt',
    valueFormatter: params => formatWithTimezone(params.value),
    sortable: true,
    width: 170
  },
  {
    headerValueGetter: () => t('access_log_viewer'),
    field: 'userDisplayName',
    valueGetter: params => params.data?.userDisplayName || params.data?.userId || '',
    width: 140
  },
  {
    headerValueGetter: () => t('access_log_target_type'),
    field: 'targetType',
    valueFormatter: params => {
      if (!params.value) return '-'
      return codeStore.getCodeName('TARGET_TYPE', params.value)
    },
    width: 140
  },
  {
    headerValueGetter: () => t('domain_name'),
    field: 'domainName',
    valueGetter: params => params.data?.domainName || '-',
    width: 130
  },
  {
    headerValueGetter: () => t('classification_name'),
    field: 'classificationName',
    valueGetter: params => params.data?.classificationName || '-',
    width: 120
  },
  {
    headerValueGetter: () => t('id_attribute'),
    field: 'idAttribute',
    valueGetter: params => params.data?.idAttribute || '-',
    width: 130
  },
  {
    headerValueGetter: () => t('name_attribute'),
    field: 'nameAttribute',
    valueGetter: params => params.data?.nameAttribute || '-',
    width: 130
  },
  {
    headerValueGetter: () => t('access_log_fields'),
    field: 'formattedFieldLabels',
    valueGetter: params => params.data?.formattedFieldLabels || params.data?.fieldKeys || '-',
    width: 180
  },
  {
    headerValueGetter: () => t('access_log_reason'),
    field: 'accessReason',
    valueGetter: params => params.data?.accessReason || '-',
    width: 220
  },
  {
    headerValueGetter: () => t('access_log_ip'),
    field: 'ipAddress',
    valueFormatter: params => {
      const val = params.value
      if (val === '::1' || val === '0:0:0:0:0:0:0:1') return '127.0.0.1'
      return val || '-'
    },
    width: 140
  }
])

const decryptionDailyChartOption = ref({})
const decryptionTypeChartOption = ref({})
const decryptionUserChartOption = ref({})

const fetchDecryptionStats = async () => {
  try {
    const res = await $fetch('/api/sensitive-data/statistics', {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    const stats = res || {}

    // 1. Daily Trends
    const dates = Object.keys(stats.dailyTrends || {}).sort()
    const counts = dates.map(date => stats.dailyTrends[date])

    decryptionDailyChartOption.value = {
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: dates },
      yAxis: { type: 'value' },
      series: [{ data: counts, type: 'bar', smooth: true, itemStyle: { color: '#5470C6' } }]
    }

    // 2. Type Ratios
    const typeKeys = Object.keys(stats.targetTypeRatios || {})
    const typeData = typeKeys.map(key => ({ name: t(key), value: stats.targetTypeRatios[key] }))
    
    decryptionTypeChartOption.value = {
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{
        name: 'Type',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
        labelLine: { show: false },
        data: typeData
      }]
    }

    // 3. Top Users
    const userKeys = Object.keys(stats.topUsers || {})
    const userCounts = userKeys.map(key => stats.topUsers[key])
    
    decryptionUserChartOption.value = {
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: userKeys, inverse: true },
      series: [{
        type: 'bar',
        data: userCounts,
        itemStyle: { color: '#91CC75' }
      }]
    }
  } catch (err) {
    console.error('Failed to fetch decryption stats:', err)
  }
}

const fetchSensitiveAccessLogs = async (page = 1) => {
  sensitiveLogLoading.value = true
  fetchDecryptionStats()
  try {
    const res = await $fetch(`/api/sensitive-data/access-logs?page=${page - 1}&size=100`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    sensitiveLogs.value = res?.content || []
  } catch (e) {
    console.error('Failed to fetch sensitive access logs:', e)
  } finally {
    sensitiveLogLoading.value = false
  }
}

watch(activeTab, (newTab) => {
  if (newTab === 'sensitive' && sensitiveLogs.value.length === 0) {
    fetchSensitiveAccessLogs(1)
  }
})

watch(locale, () => {
  // Update header text immediately without full unmount
  setTimeout(() => {
    if (gridApi.value) {
      gridApi.value.refreshHeader()
      gridApi.value.refreshServerSide({ purge: true })
    }
    // Refresh Server-Side grids
    if (typeof loginGridApi !== 'undefined' && loginGridApi.value) loginGridApi.value.refreshServerSide({ purge: true })
    if (typeof errorGridApi !== 'undefined' && errorGridApi.value) errorGridApi.value.refreshServerSide({ purge: true })
    if (typeof integrationGridApi !== 'undefined' && integrationGridApi.value) integrationGridApi.value.refreshServerSide({ purge: true })

    if (typeof sensitiveGridApi !== 'undefined' && sensitiveGridApi.value) {
      sensitiveGridApi.value.refreshHeader()
    }
  }, 50)

  // Refetch sensitive logs data dynamically
  if (activeTab.value === 'sensitive') {
    fetchSensitiveAccessLogs(1)
  } else {
    // Clear data so it fetches anew when the tab is visited next time
    sensitiveLogs.value = []
  }
})

const { fetchMenuTree } = useMenu()
const dbMenuMap = ref(new Map())
let isMapLoaded = false

const loadDbMenuMap = async (force = false) => {
  if (isMapLoaded && !force) return
  try {
    const tree = await fetchMenuTree(force)
    const newMap = new Map()
    const traverse = (nodes) => {
      if (!Array.isArray(nodes)) return
      nodes.forEach(node => {
        if (node && node.path) {
          const clean = node.path.toLowerCase().replace(/\/+$/, '')
          newMap.set(clean, node.name)
        }
        if (node.children && node.children.length > 0) {
          traverse(node.children)
        }
      })
    }
    traverse(tree)
    dbMenuMap.value = newMap
    isMapLoaded = true
  } catch (e) {
    console.error('Failed to load menu tree for chart lookup:', e)
  }
}

// Multilingual Menu Translation Helper (Pure DB dynamic lookup - NO hardcoding)
const getMenuName = (path) => {
  if (!path) return ''
  const cleanPath = path.toLowerCase().replace(/\/+$/, '')
  
  if (dbMenuMap.value.has(cleanPath)) {
    const rawName = dbMenuMap.value.get(cleanPath)
    const text = getMultilingualText(rawName, locale.value)
    if (text) return text
  }

  // Exact path or prefix match against DB menu map
  for (const [menuPath, rawName] of dbMenuMap.value.entries()) {
    if (menuPath && (cleanPath === menuPath || cleanPath.startsWith(menuPath) || menuPath.startsWith(cleanPath))) {
      const text = getMultilingualText(rawName, locale.value)
      if (text) return text
    }
  }

  return path
}

// ----------------------------------------------------
// 1. Menu Access Logs Data & Setup
// ----------------------------------------------------
const gridApi = ref(null)
const accessChartPeriod = ref('daily')

const columnDefs = ref([
  { field: 'id', headerName: 'ID', width: 120, valueFormatter: (params) => formatEntityId(params.value, 'LOG') },
  { field: 'userId', headerName: 'User ID', flex: 1, valueFormatter: (params) => formatEntityId(params.value, 'USR') },
  { 
    field: 'menuName', 
    headerName: 'Menu Name', 
    flex: 1.5,
    valueGetter: (params) => {
      if (!params.data) return ''
      return getMenuName(params.data.menuPath)
    }
  },
  { field: 'menuPath', headerName: 'Accessed Path', flex: 1.5 },
  { field: 'userAgent', headerName: 'User Agent', flex: 2 },
  { field: 'clientIp', headerName: 'Client IP', flex: 1 },
  { 
    field: 'accessedAt', 
    headerName: 'Accessed At', 
    flex: 1.2,
    valueFormatter: (params) => formatWithTimezone(params.value)
  }
])

const chartOption = ref({
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: [
    {
      type: 'category',
      data: [],
      axisTick: { alignWithLabel: true },
      axisLabel: {
        interval: 0,
        rotate: 0
      }
    }
  ],
  yAxis: [
    { type: 'value' }
  ],
  series: [
    {
      name: 'Access Count',
      type: 'bar',
      barWidth: '60%',
      data: []
    }
  ]
})

const onGridReady = (params) => {
  gridApi.value = params.api
}

const datasource = {
  getRows: async (params) => {
    try {
      const page = Math.floor(params.request.startRow / 20)
      
      const response = await $fetch('/api/menus/logs', {
        headers: token.value ? { Authorization: `Bearer ${token.value}` } : {},
        params: {
          page: page,
          size: 20,
          sort: 'accessedAt,desc'
        }
      })
      
      let lastRow = -1
      if (response.content.length < 20) {
        lastRow = params.request.startRow + response.content.length
      } else if (response.totalElements) {
        lastRow = response.totalElements
      }
      
      params.success({ rowData: response.content, rowCount: lastRow })
      updateChart()
      
    } catch (error) {
      console.error('Failed to fetch menu logs:', error)
      params.fail()
    }
  }
}

const updateChart = async () => {
  try {
     await loadDbMenuMap()
     fetchDecryptionStats()
     const response = await $fetch('/api/menus/logs', {
        headers: token.value ? { Authorization: `Bearer ${token.value}` } : {},
        params: { page: 0, size: 200, sort: 'accessedAt,desc' }
     })
     
     if (response.content.length === 0) return
     
     // Get latest log's date components for filtering reference
     const latestLogDate = response.content[0].accessedAt
     const targetDay = latestLogDate.substring(0, 10)
     const targetMonth = latestLogDate.substring(0, 7)
     const targetYear = latestLogDate.substring(0, 4)
     
     const pathCounts = {}
     response.content.forEach(log => {
       if (log.accessedAt && log.menuPath) {
         const logDay = log.accessedAt.substring(0, 10)
         const logMonth = log.accessedAt.substring(0, 7)
         const logYear = log.accessedAt.substring(0, 4)
         
         // Filter based on selected period mode
         if (accessChartPeriod.value === 'daily' && logDay !== targetDay) return
         if (accessChartPeriod.value === 'monthly' && logMonth !== targetMonth) return
         if (accessChartPeriod.value === 'yearly' && logYear !== targetYear) return
         
         const translatedName = getMenuName(log.menuPath)
         pathCounts[translatedName] = (pathCounts[translatedName] || 0) + 1
       }
     })
     
     const sortedPaths = Object.keys(pathCounts).sort((a,b) => pathCounts[b] - pathCounts[a]).slice(0, 10)
     const counts = sortedPaths.map(p => pathCounts[p])
     
     chartOption.value.xAxis[0].data = sortedPaths
     chartOption.value.series[0].data = counts
     chartOption.value.series[0].name = t('Access Count')
  } catch (error) {
     console.error('Failed to update chart:', error)
  }
}

const refreshGrid = () => {
  if (gridApi.value) {
    gridApi.value.refreshServerSide()
  }
  updateChart()
}

// ----------------------------------------------------
// 2. User Login Logs Data & Setup
// ----------------------------------------------------
const loginGridApi = ref(null)
const loginChartPeriod = ref('daily')

const loginColumnDefs = ref([
  { field: 'id', headerName: 'ID', width: 120, valueFormatter: (params) => formatEntityId(params.value, 'LOG') },
  { field: 'userId', headerName: 'User UUID', flex: 1.5, valueFormatter: (params) => formatEntityId(params.value, 'USR') },
  { field: 'username', headerName: 'Username', flex: 1 },
  { field: 'userAgent', headerName: 'User Agent', flex: 2 },
  { field: 'clientIp', headerName: 'Client IP', flex: 1 },
  { 
    field: 'loginAt', 
    headerName: 'Login At', 
    flex: 1,
    valueFormatter: (params) => formatWithTimezone(params.value)
  }
])

const loginChartOption = ref({
  tooltip: {
    trigger: 'axis'
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: [
    {
      type: 'category',
      data: [],
      axisTick: { alignWithLabel: true }
    }
  ],
  yAxis: [
    { type: 'value' }
  ],
  series: [
    {
      name: 'Login Count',
      type: 'bar',
      barWidth: '60%',
      data: []
    }
  ]
})

const onLoginGridReady = (params) => {
  loginGridApi.value = params.api
}

const loginDatasource = {
  getRows: async (params) => {
    try {
      const page = Math.floor(params.request.startRow / 20)
      
      const response = await $fetch('/api/auth/login-logs', {
        headers: token.value ? { Authorization: `Bearer ${token.value}` } : {},
        params: {
          page: page,
          size: 20
        }
      })
      
      let lastRow = -1
      if (response.content.length < 20) {
        lastRow = params.request.startRow + response.content.length
      } else if (response.totalElements) {
        lastRow = response.totalElements
      }
      
      params.success({ rowData: response.content, rowCount: lastRow })
      updateLoginChart()
    } catch (error) {
      console.error('Failed to fetch login logs:', error)
      params.fail()
    }
  }
}

const updateLoginChart = async () => {
  try {
     const response = await $fetch('/api/auth/login-logs', {
        headers: token.value ? { Authorization: `Bearer ${token.value}` } : {},
        params: { page: 0, size: 200 }
     })
     
     const dateCounts = {}
     response.content.forEach(log => {
       if (log.loginAt) {
         let key = log.loginAt.substring(0, 10) // default daily
         if (loginChartPeriod.value === 'monthly') {
           key = log.loginAt.substring(0, 7)
         } else if (loginChartPeriod.value === 'yearly') {
           key = log.loginAt.substring(0, 4)
         }
         dateCounts[key] = (dateCounts[key] || 0) + 1
       }
     })
     
     const sortedKeys = Object.keys(dateCounts).sort()
     const counts = sortedKeys.map(k => dateCounts[k])
     
     loginChartOption.value.xAxis[0].data = sortedKeys
     loginChartOption.value.series[0].data = counts
     loginChartOption.value.series[0].name = t('Login Count')
  } catch (error) {
     console.error('Failed to update login chart:', error)
  }
}

const refreshLoginGrid = () => {
  if (loginGridApi.value) {
    loginGridApi.value.refreshServerSide()
  }
  updateLoginChart()
}

// ----------------------------------------------------
// 3. System Error Logs Data & Setup
// ----------------------------------------------------
const errorGridApi = ref(null)
const showErrorModal = ref(false)
const selectedError = ref(null)

const errorColumnDefs = ref([
  { field: 'id', headerName: 'ID', width: 100 },
  { field: 'userId', headerName: 'User ID', flex: 1 },
  { field: 'requestUri', headerName: 'Request URI', flex: 1.5 },
  { field: 'errorMessage', headerName: 'Error Message', flex: 3 },
  { 
    field: 'loggedAt', 
    headerName: 'Logged At', 
    flex: 1.2,
    valueFormatter: (params) => {
      if (!params.value) return ''
      return new Date(params.value).toLocaleString(locale.value === 'ko' ? 'ko-KR' : 'en-US')
    }
  }
])

const onErrorGridReady = (params) => {
  errorGridApi.value = params.api
}

const errorDatasource = {
  getRows: async (params) => {
    try {
      const page = Math.floor(params.request.startRow / 20)
      
      const response = await $fetch('/api/admin/error-logs', {
        headers: token.value ? { Authorization: `Bearer ${token.value}` } : {},
        params: {
          page: page,
          size: 20
        }
      })
      
      let lastRow = -1
      if (response.content.length < 20) {
        lastRow = params.request.startRow + response.content.length
      } else if (response.totalElements) {
        lastRow = response.totalElements
      }
      
      params.success({ rowData: response.content, rowCount: lastRow })
    } catch (error) {
      console.error('Failed to fetch error logs:', error)
      params.fail()
    }
  }
}

const refreshErrorGrid = () => {
  if (errorGridApi.value) {
    errorGridApi.value.refreshServerSide()
  }
}

const onRowDoubleClicked = (event) => {
  if (event.data) {
    selectedError.value = event.data
    showErrorModal.value = true
  }
}

// ----------------------------------------------------
// 4. Integration Logs Data & Setup (AG-Grid Server-Side)
// ----------------------------------------------------
const integrationGridApi = ref(null)
const channelOptions = ref([])
const selectedChannelId = ref(null)
const isDlqOnly = ref(false)
const isBulkRetrying = ref(false)
const showIntegrationDetailsModal = ref(false)
const selectedIntegrationLog = ref(null)
const rawChannels = ref([])

// 더블클릭 → 상세 모달
const onIntegrationRowDoubleClicked = (event) => {
  if (event.data) {
    selectedIntegrationLog.value = event.data
    showIntegrationDetailsModal.value = true
  }
}

const viewIntegrationDetails = (log) => {
  selectedIntegrationLog.value = log
  showIntegrationDetailsModal.value = true
}

const onIntegrationGridReady = (params) => {
  integrationGridApi.value = params.api
}

// AG-Grid 컬럼 정의
const integrationColumnDefs = ref([
  {
    headerName: 'Channel',
    field: 'channelName',
    flex: 1.2,
    valueGetter: (params) => {
      if (!params.data) return ''
      const channel = rawChannels.value.find(c => c.id === params.data.channelId)
      return channel ? getMultilingualText(channel.name, locale.value) : 'Unknown'
    }
  },
  {
    headerName: 'Direction',
    field: 'direction',
    width: 110,
    valueGetter: (params) => {
      if (!params.data) return ''
      const channel = rawChannels.value.find(c => c.id === params.data.channelId)
      return channel?.direction || params.data.direction || 'OUTBOUND'
    },
    cellRenderer: (params) => {
      if (!params.value) return ''
      const isInbound = params.value === 'INBOUND'
      const color = isInbound ? '#f59e0b' : '#3b82f6'
      const label = isInbound ? 'Inbound' : 'Outbound'
      return `<span style="display:inline-flex;align-items:center;gap:4px;padding:2px 8px;border-radius:12px;font-size:0.75rem;font-weight:600;background:${color}22;color:${color};border:1px solid ${color}44">${label}</span>`
    }
  },
  { headerName: 'Event Type', field: 'eventType', flex: 1 },
  {
    headerName: 'Status',
    field: 'status',
    width: 110,
    cellRenderer: (params) => {
      if (!params.value) return ''
      const isSuccess = params.value === 'SUCCESS'
      const color = isSuccess ? '#22c55e' : '#ef4444'
      return `<span style="display:inline-flex;align-items:center;gap:4px;padding:2px 8px;border-radius:12px;font-size:0.75rem;font-weight:600;background:${color}22;color:${color};border:1px solid ${color}44">${params.value}</span>`
    }
  },
  { headerName: 'Retry', field: 'retryCount', width: 80 },
  {
    headerName: 'Logged At',
    field: 'createdAt',
    flex: 1.2,
    valueFormatter: (params) => {
      if (!params.value) return ''
      return new Date(params.value).toLocaleString(locale.value === 'ko' ? 'ko-KR' : 'en-US')
    }
  },
  {
    headerName: 'Details',
    field: 'id',
    width: 90,
    sortable: false,
    cellRenderer: () => `<button style="background:none;border:none;cursor:pointer;color:var(--va-primary)" title="상세보기"><span class="material-icons" style="font-size:18px;vertical-align:middle">visibility</span></button>`,
    onCellClicked: (params) => {
      if (params.data) viewIntegrationDetails(params.data)
    }
  }
])

// AG-Grid 서버 사이드 데이터소스
const integrationDatasource = {
  getRows: async (params) => {
    try {
      const page = Math.floor(params.request.startRow / 20)
      const query = new URLSearchParams({ page: String(page), size: '20' })
      if (selectedChannelId.value) query.append('channelId', selectedChannelId.value)

      const endpoint = isDlqOnly.value
        ? `/api/admin/integration/logs/dead-letter?${query}`
        : `/api/admin/integration/logs?${query}`

      const data = await $fetch(endpoint, {
        headers: { Authorization: `Bearer ${token.value}` }
      })

      const content = data.content || data || []
      let lastRow = -1
      if (content.length < 20) {
        lastRow = params.request.startRow + content.length
      } else if (data.totalElements) {
        lastRow = data.totalElements
      }

      params.success({ rowData: content, rowCount: lastRow })
    } catch (e) {
      console.error('Failed to load integration logs:', e)
      params.fail()
    }
  }
}

// 필터 변경 시 그리드 새로고침
const fetchIntegrationLogs = (_page) => {
  if (integrationGridApi.value) {
    integrationGridApi.value.refreshServerSide({ purge: true })
  }
}

const retryAllDlq = async () => {
  isBulkRetrying.value = true
  try {
    const count = await $fetch('/api/admin/integration/logs/dead-letter/retry-all', {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    init({ message: `DLQ 총 ${count || 0}건의 재시도가 상신되었습니다.`, color: 'success' })
    fetchIntegrationLogs()
  } catch (e) {
    init({ message: 'DLQ 일괄 재시도 실패', color: 'danger' })
  } finally {
    isBulkRetrying.value = false
  }
}

const retryIntegrationLog = async (logId) => {
  try {
    await $fetch(`/api/admin/integration/logs/${logId}/retry`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    init({ message: '재전송 요청이 완료되었습니다.', color: 'success' })
    showIntegrationDetailsModal.value = false
    fetchIntegrationLogs()
  } catch (e) {
    init({ message: '재전송 요청 중 오류가 발생했습니다.', color: 'danger' })
  }
}

const fetchChannels = async () => {
  try {
    const data = await $fetch('/api/admin/integration/channels', {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    rawChannels.value = data
    channelOptions.value = data.map(c => ({
      id: c.id,
      name: getMultilingualText(c.name, locale.value)
    }))
  } catch (e) {
    console.error('Failed to load channels:', e)
  }
}

const formatJson = (str) => {
  if (!str) return 'N/A'
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

const copySuccess = ref('')

const copyPayload = async (payloadStr, type = 'original') => {
  if (!payloadStr) return
  try {
    const textToCopy = formatJson(payloadStr)
    await navigator.clipboard.writeText(textToCopy)
    copySuccess.value = type
    setTimeout(() => { copySuccess.value = '' }, 2000)
    init({ message: '클립보드에 복사되었습니다.', color: 'success' })
  } catch (err) {
    console.error('Failed to copy payload:', err)
  }
}

// Watch language change to dynamically translate chart labels
watch(locale, () => {
  updateChart()
  updateLoginChart()
  if (gridApi.value) {
    gridApi.value.refreshCells({ force: true })
  }
  if (rawChannels.value && rawChannels.value.length > 0) {
    channelOptions.value = rawChannels.value.map(c => ({
      id: c.id,
      name: getMultilingualText(c.name, locale.value)
    }))
  }
  fetchIntegrationLogs()
})

onMounted(async () => {
  isMounted.value = true
  await codeStore.loadGroup('TARGET_TYPE')
  await loadDbMenuMap()
  updateChart()
  updateLoginChart()
  await fetchChannels()
  fetchIntegrationLogs(1)
})
</script>

<style scoped>
.menu-logs-container {
  padding: 20px;
}
.stack-trace-view {
  background-color: #1f2937;
  color: #f3f4f6;
  padding: 1.5rem;
  border-radius: 8px;
  max-height: 400px;
  overflow-y: auto;
  font-family: 'Courier New', Courier, monospace;
  font-size: 0.85rem;
  white-space: pre-wrap;
}

/* Premium Integration Detail Modal Styles */
.integration-modal-container {
  display: flex;
  flex-direction: column;
  background: var(--va-background-element, #ffffff);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.modal-header-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.75rem;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.08) 0%, rgba(147, 51, 234, 0.08) 100%);
  border-bottom: 1px solid var(--va-background-border, #e5e7eb);
}

.modal-title-text {
  font-size: 1.15rem;
  font-weight: 700;
  margin: 0;
  color: var(--va-text-primary, #111827);
  letter-spacing: -0.01em;
}

.modal-close-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 6px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--va-text-secondary, #6b7280);
  transition: all 0.2s ease;
}

.modal-close-btn:hover {
  background: rgba(0, 0, 0, 0.06);
  color: var(--va-text-primary, #111827);
}

.modal-body-content {
  padding: 1.5rem 1.75rem;
  max-height: 75vh;
  overflow-y: auto;
}

/* Metrics Grid Cards */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.metric-card {
  padding: 1rem;
  border-radius: 10px;
  background: var(--va-card-background, rgba(249, 250, 251, 0.7));
  border: 1px solid var(--va-background-border, #e5e7eb);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.metric-label {
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--va-text-secondary, #6b7280);
}

.metric-value {
  font-size: 0.95rem;
  font-weight: 600;
}

.status-indicator-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.metric-card.status-success .status-indicator-dot {
  background-color: #10b981;
  box-shadow: 0 0 8px rgba(16, 185, 129, 0.6);
}

.metric-card.status-fail .status-indicator-dot {
  background-color: #ef4444;
  box-shadow: 0 0 8px rgba(239, 68, 68, 0.6);
}

.retry-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
  background: rgba(245, 158, 11, 0.15);
  color: #b45309;
  font-weight: 600;
}

.date-text {
  font-size: 0.85rem;
  color: var(--va-text-primary);
}

/* Error Panel */
.error-panel {
  border-radius: 8px;
  border: 1px solid rgba(239, 68, 68, 0.3);
  background: rgba(254, 242, 242, 0.6);
  overflow: hidden;
}

.error-panel-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 1rem;
  background: rgba(254, 226, 226, 0.8);
  font-size: 0.85rem;
  font-weight: 700;
  color: #991b1b;
}

.error-message-body {
  padding: 0.85rem 1rem;
  font-family: 'Fira Code', 'Courier New', monospace;
  font-size: 0.85rem;
  color: #7f1d1d;
  white-space: pre-wrap;
}

.stack-trace-terminal {
  padding: 1rem;
  background: #0f172a;
  color: #f87171;
  font-family: 'Fira Code', 'Courier New', monospace;
  font-size: 0.8rem;
  max-height: 200px;
  overflow-y: auto;
  white-space: pre-wrap;
}

/* Mac IDE Terminal Viewers */
.terminal-card {
  border-radius: 10px;
  overflow: hidden;
  background: #0d1117;
  border: 1px solid #30363d;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.terminal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 1rem;
  background: #161b22;
  border-bottom: 1px solid #30363d;
}

.terminal-dots {
  display: flex;
  gap: 6px;
}

.dot {
  width: 11px;
  height: 11px;
  border-radius: 50%;
}

.dot-red { background-color: #ff5f56; }
.dot-yellow { background-color: #ffbd2e; }
.dot-green { background-color: #27c93f; }

.terminal-title {
  font-size: 0.8rem;
  font-family: 'Fira Code', 'JetBrains Mono', monospace;
  color: #8b949e;
  font-weight: 600;
}

.copy-btn {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: #c9d1d9;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 0.75rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s ease;
}

.copy-btn:hover {
  background: rgba(255, 255, 255, 0.18);
  color: #ffffff;
}

.terminal-body {
  padding: 1.25rem;
  max-height: 280px;
  overflow-y: auto;
}

.terminal-body pre {
  margin: 0;
}

.terminal-body code {
  font-family: 'Fira Code', 'Cascadia Code', 'JetBrains Mono', monospace;
  font-size: 0.85rem;
  color: #58a6ff;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}

/* Modal Footer */
.modal-footer-bar {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding: 1rem 1.75rem;
  background: var(--va-background-element, rgba(249, 250, 251, 0.5));
  border-top: 1px solid var(--va-background-border, #e5e7eb);
}

/* ───── Integration Logs 프리미엄 헤더 ───── */
.integration-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 1rem;
  padding: 1rem 1.25rem 0.85rem;
  border-bottom: 1px solid var(--va-background-border);
}

.integration-header-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.integration-header-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba(21, 78, 193, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.integration-header-title {
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--va-text-primary);
  line-height: 1.2;
}

.integration-header-sub {
  font-size: 0.78rem;
  color: var(--va-text-secondary);
  margin-top: 2px;
}

/* ── 툴바 컨트롤 그룹 ── */
.integration-toolbar {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: var(--va-background-element);
  border: 1px solid var(--va-background-border);
  border-radius: 10px;
  padding: 0.4rem 0.75rem;
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: 0.45rem;
}

.toolbar-label {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--va-text-secondary);
  white-space: nowrap;
  transition: color 0.2s;
}

.channel-select {
  width: 200px;
}

.toolbar-divider {
  width: 1px;
  height: 22px;
  background: var(--va-background-border);
  margin: 0 0.25rem;
}

/* DLQ 활성화 상태 시각 피드백 */
.dlq-group {
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
  transition: background 0.25s, box-shadow 0.25s;
}

.dlq-group.dlq-active {
  background: rgba(var(--va-danger-rgb, 229, 57, 53), 0.08);
  box-shadow: 0 0 0 1px rgba(var(--va-danger-rgb, 229, 57, 53), 0.25);
}

.retry-all-btn {
  animation: pop-in 0.2s ease;
}

.refresh-btn {
  transition: transform 0.2s ease;
}

.refresh-btn:hover {
  transform: rotate(45deg);
}

/* 슬라이드 인/아웃 애니메이션 */
.slide-fade-enter-active {
  transition: all 0.2s ease;
}
.slide-fade-leave-active {
  transition: all 0.15s ease;
}
.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(-6px);
}

@keyframes pop-in {
  from { transform: scale(0.85); opacity: 0; }
  to   { transform: scale(1);    opacity: 1; }
}
</style>
