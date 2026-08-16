<template>
  <div style="display: flex; flex-direction: column; gap: 1.25rem; padding-bottom: 2rem;">
    <!-- Top Action Bar -->
    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); padding: 1rem 1.25rem; border-radius: 12px; border: 1px solid var(--va-background-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04);">
      <div style="display: flex; align-items: center; gap: 0.75rem;">
        <va-icon name="hub" size="large" color="primary" />
        <div>
          <h2 style="font-weight: 700; font-size: 1.35rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
            {{ pageTitle }}
            <va-badge text="Integration" color="primary" size="small" />
          </h2>
          <span style="font-size: 0.85rem; color: var(--va-text-secondary);">
            {{ $t('integration.channels.desc') }}
          </span>
        </div>
      </div>

      <div style="display: flex; gap: 0.75rem; align-items: center;">
        <va-button color="primary" icon="add" size="small" @click="openCreateModal">
          {{ $t('integration.channels.add') }}
        </va-button>
        <va-button preset="outline" color="primary" icon="refresh" size="small" @click="fetchChannels">
          {{ $t('refresh') }}
        </va-button>
      </div>
    </div>

    <!-- Channels Table Card -->
    <va-card style="flex: 1; display: flex; flex-direction: column; overflow: hidden; border-radius: 12px; border: 1px solid var(--va-background-border); margin-bottom: 1.25rem;">
      <va-card-title class="flex justify-between items-center" style="padding: 1rem 1.25rem;">
        <div class="flex items-center gap-2 font-bold text-lg">
          <va-icon name="hub" color="primary" />
          <span style="color: var(--va-text-primary);">{{ $t('integration.channels.title') }}</span>
          <va-chip size="small" color="primary">{{ channels.length }}개 항목</va-chip>
        </div>
      </va-card-title>

      <va-card-content style="padding: 0 1.25rem 1.25rem 1.25rem;">
        <div :class="{ 'ag-theme-quartz-dark': isDark }" style="width: 100%; min-height: 320px;">
          <AgGridVue
            style="width: 100%; height: 350px;"
            :theme="gridTheme"
            :column-defs="channelColumnDefs"
            :row-data="channels"
            :default-col-def="{ sortable: true, resizable: true }"
            :animate-rows="true"
            :pagination="true"
            :pagination-page-size="10"
            :pagination-page-size-selector="[5, 10, 20, 50]"
            :row-height="54"
            :header-height="46"
            :suppress-cell-focus="true"
          />
        </div>
      </va-card-content>
    </va-card>

    <!-- Recent Integration Logs Dashboard -->
    <va-card style="flex: 1; display: flex; flex-direction: column; overflow: hidden; border-radius: 12px; border: 1px solid var(--va-background-border);">
      <va-card-title class="flex justify-between items-center" style="padding: 1rem 1.25rem;">
        <div class="flex items-center gap-2 font-bold text-lg">
          <va-icon name="monitor_heart" color="primary" />
          <span style="color: var(--va-text-primary);">최근 연동 로그 모니터링 (Recent Integration Logs)</span>
        </div>
        <va-button preset="secondary" icon="refresh" size="small" @click="fetchRecentLogs">
          {{ $t('refresh') }}
        </va-button>
      </va-card-title>

      <va-card-content style="padding: 0 1.25rem 1.25rem 1.25rem;">
        <div :class="{ 'ag-theme-quartz-dark': isDark }" style="width: 100%; min-height: 280px;">
          <AgGridVue
            style="width: 100%; height: 300px;"
            :theme="gridTheme"
            :column-defs="recentLogColumnDefs"
            :row-data="recentLogs"
            :default-col-def="{ sortable: true, resizable: true }"
            :animate-rows="true"
            :pagination="true"
            :pagination-page-size="10"
            :pagination-page-size-selector="[5, 10, 20, 50]"
            :row-height="50"
            :header-height="46"
            :suppress-cell-focus="true"
          />
        </div>
      </va-card-content>
    </va-card>

    <!-- Create/Edit Modal (Decoupled Component) -->
    <ChannelConfigModal
      v-model="showModal"
      :is-edit="isEdit"
      :form-data="formData"
      :ui-config="uiConfig"
      :channel-name-ko="channelNameKo"
      :channel-name-en="channelNameEn"
      :direction-options="directionOptions"
      :type-options="typeOptions"
      :auth-type-options="authTypeOptions"
      :method-options="methodOptions"
      :domains="domains"
      :nodes="nodes"
      :selected-domain-id="selectedDomainId"
      :raw-fields="rawFields"
      :ui-mapping-root-path="uiMappingRootPath"
      :ui-mappings="uiMappings"
      :mapping-column-defs="mappingColumnDefs"
      :grid-theme="gridTheme"
      :is-dark="isDark"
      :is-testing="isTesting"
      :webhook-url="getWebhookUrl()"
      :sample-json-payload="sampleJsonPayload"
      @update:channel-name-ko="val => channelNameKo = val"
      @update:channel-name-en="val => channelNameEn = val"
      @update:selected-domain-id="onDomainSelected"
      @update:ui-mapping-root-path="val => uiMappingRootPath = val"
      @direction-changed="onDirectionChanged"
      @generate-token="generateSecretToken"
      @copy-webhook-url="copyWebhookUrl"
      @copy-auth-header="copyAuthHeaderValue"
      @copy-curl="copyCurlSample"
      @copy-json-payload="copySampleJsonPayload"
      @test-connection="testConnection"
      @add-ws-header="addWsHeader"
      @remove-ws-header="removeWsHeader"
      @auto-generate-mappings="autoGenerateMappings"
      @add-mapping="addMapping"
      @mapping-grid-ready="onMappingGridReady"
      @mapping-cell-changed="onMappingCellValueChanged"
      @submit="submitForm"
    />

    <!-- Channel Realtime Metrics & Health Modal -->
    <ChannelMetricsModal
      v-model="showMetricsModal"
      :channel-id="selectedMetricsChannel?.id"
      :channel-name="parseI18nName(selectedMetricsChannel?.name)"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useCookie } from '#app'
import { useToast } from 'vuestic-ui'
import { AgGridVue } from 'ag-grid-vue3'
import { useI18n } from 'vue-i18n'
import { usePageTitle } from '~/composables/usePageTitle'
import { useAgGridTheme } from '~/composables/useAgGridTheme'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { useCodeStore } from '~/stores/useCodeStore'
import ChannelConfigModal from '~/components/admin/ChannelConfigModal.vue'
import ChannelMetricsModal from '~/components/integration/ChannelMetricsModal.vue'

const { pageTitle } = usePageTitle('integration.channels.title', '외부 연동 채널 관리')
const { t, locale } = useI18n()
const { gridTheme, isDark } = useAgGridTheme()
const { init } = useToast()
const { customFetch } = useCustomFetch()
const token = useCookie('auth_token')
const codeStore = useCodeStore()

const channelNameKo = ref('')
const channelNameEn = ref('')

const extractNameParts = (rawName) => {
  if (!rawName) return { ko: '', en: '' }
  try {
    const parsed = typeof rawName === 'object' ? rawName : (String(rawName).trim().startsWith('{') ? JSON.parse(rawName) : null)
    if (parsed && typeof parsed === 'object') {
      return { ko: parsed.ko || '', en: parsed.en || '' }
    }
  } catch (e) {}
  const str = String(rawName).trim()
  return { ko: str, en: str }
}
const channels = ref([])
const rawDomains = ref([])
const rawNodes = ref([])
const rawFields = ref([])
const selectedDomainId = ref(null)

const domains = computed(() => {
  return rawDomains.value.map(d => ({
    ...d,
    name: parseI18nName(d.name)
  }))
})

const nodes = computed(() => {
  const flatNodes = []
  const flatten = (items, prefix = '') => {
    items.forEach(item => {
      flatNodes.push({ id: item.id, name: prefix + parseI18nName(item.name) })
      if (item.children && item.children.length > 0) {
        flatten(item.children, prefix + '-- ')
      }
    })
  }
  flatten(rawNodes.value)
  return flatNodes
})

const domainFields = computed(() => {
  return rawFields.value.map(f => ({
    code: f.key,
    name: `${parseI18nName(f.name)} (${f.key})`
  }))
})

const isLoading = ref(false)
const isTesting = ref(false)
const showModal = ref(false)
const showMetricsModal = ref(false)
const selectedMetricsChannel = ref(null)
const activeModalTab = ref('basic')
const isEdit = ref(false)
const form = ref(null)

const openMetricsModal = (channel) => {
  selectedMetricsChannel.value = channel
  showMetricsModal.value = true
}

const directionOptions = computed(() => codeStore.getDropdownOptions('INTEGRATION_DIRECTION'))
const typeOptions = computed(() => codeStore.getDropdownOptions('INTEGRATION_TYPE'))
const methodOptions = computed(() => codeStore.getDropdownOptions('HTTP_METHOD'))

const authTypeOptions = computed(() => [
  { value: 'BEARER_TOKEN', text: t('integration.channels.auth_bearer') },
  { value: 'API_KEY', text: t('integration.channels.auth_api_key') },
  { value: 'NONE', text: t('integration.channels.auth_none') }
])

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

const columns = computed(() => [
  { key: 'name', label: t('integration.channels.name'), sortable: true },
  { key: 'direction', label: t('integration.channels.direction'), sortable: true },
  { key: 'type', label: t('integration.channels.type'), sortable: true },
  { key: 'isActive', label: t('integration.channels.status'), sortable: true },
  { key: 'createdAt', label: t('integration.channels.created_at'), sortable: true },
  { key: 'actions', label: t('integration.channels.management'), width: '100px' }
])

// Base Entity Model
const initialForm = {
  name: '',
  direction: 'OUTBOUND',
  type: 'WEB_SERVICE',
  nodeId: null,
  configJson: '{}',
  mappingConfigJson: '{}',
  isActive: true,
  requiresApproval: false
}

const getWebhookUrl = () => {
  const config = useRuntimeConfig()
  const apiBaseUrl = config.public.apiBaseUrl || 'http://localhost:8080'
  const channelIdStr = editingId || '{channelId}'
  return `${apiBaseUrl}/api/integration/inbound/${channelIdStr}`
}

const copyWebhookUrl = () => {
  const url = getWebhookUrl()
  if (navigator.clipboard) {
    navigator.clipboard.writeText(url)
    init({ message: t('integration.channels.copied'), color: 'success' })
  }
}

const syncUiMappingsFromGrid = () => {
  if (mappingGridApi) {
    try {
      mappingGridApi.stopEditing(false)
    } catch (e) {}

    const rowData = []
    mappingGridApi.forEachNode(node => {
      if (node.data) rowData.push({ ...node.data })
    })
    uiMappings.value = rowData
  }
}

const sampleJsonPayload = computed(() => {
  const sampleObj = {}
  const mappings = uiMappings.value || []

  // 1. Root Key 추출
  let rootKey = null
  if (uiMappingRootPath.value) {
    const rp = String(uiMappingRootPath.value).trim()
    const bracketMatches = [...rp.matchAll(/\[['"](.+?)['"]\]/g)]
    const dotMatch = rp.match(/\.([a-zA-Z0-9_]+)$/)
    if (bracketMatches.length > 0) {
      rootKey = bracketMatches[bracketMatches.length - 1][1]
    } else if (dotMatch) {
      rootKey = dotMatch[1]
    } else if (/^[a-zA-Z0-9_]+$/.test(rp) && rp !== 'payload' && rp !== '#this') {
      rootKey = rp
    }
  }

  // 2. 매핑 항목이 전혀 없으면 순수 빈 객체/배열 반환 (하드코딩 0%)
  if (mappings.length === 0) {
    if (rootKey) {
      return JSON.stringify({ [rootKey]: [] }, null, 2)
    }
    return JSON.stringify({}, null, 2)
  }

  // 3. 복합 SpEL Map 표현식 (예: {'ko': #this['emp_kor_name'], 'en': #this['emp_eng_name']}) 내 모든 참조 소스 필드 파싱
  mappings.forEach((m, idx) => {
    const keysFound = []

    if (m.sourceExpression) {
      const expr = String(m.sourceExpression).trim()

      const matches = [...expr.matchAll(/(?:#this|payload|[a-zA-Z0-9_]+)?\[['"]([^'"]+)['"]\]|\.([a-zA-Z0-9_]+)/g)]

      matches.forEach(match => {
        const cand = (match[1] || match[2] || '').trim()
        if (
          cand &&
          cand !== rootKey &&
          cand !== 'payload' &&
          cand !== '#this' &&
          cand !== 'ko' &&
          cand !== 'en' &&
          !keysFound.includes(cand)
        ) {
          keysFound.push(cand)
        }
      })

      if (keysFound.length === 0 && /^[a-zA-Z0-9_]+$/.test(expr) && expr !== 'payload' && expr !== '#this' && expr !== rootKey) {
        keysFound.push(expr)
      }
    }

    if (keysFound.length === 0) {
      let fallbackKey = m.selectedField || m.targetField
      if (!fallbackKey || String(fallbackKey).trim() === '') {
        fallbackKey = `field_${idx + 1}`
      }
      keysFound.push(String(fallbackKey).trim())
    }

    // 추출된 모든 참조 필드별로 DB 도메인 속성 다국어 이름(name) 매핑
    keysFound.forEach(key => {
      let dummyVal = null
      const matchedField = rawFields.value?.find(f => f.key === key || f.key === m.selectedField || f.key === m.targetField)
      if (matchedField && matchedField.name) {
        dummyVal = parseI18nName(matchedField.name)
      }

      if (!dummyVal) {
        dummyVal = key
      }

      // 한글/영문 필드 구분 접미사 자동 추가 (예: "이름 (한글)", "이름 (영문)")
      const currentLang = (locale?.value || 'ko').toLowerCase().startsWith('en') ? 'en' : 'ko'
      const lowerKey = key.toLowerCase()
      if (lowerKey.includes('kor') || lowerKey.includes('ko_name') || lowerKey.includes('_ko')) {
        const suffix = currentLang === 'en' ? ' (Korean)' : ' (한글)'
        if (!dummyVal.includes(suffix) && !dummyVal.includes('한글')) {
          dummyVal += suffix
        }
      } else if (lowerKey.includes('eng') || lowerKey.includes('en_name') || lowerKey.includes('_en')) {
        const suffix = currentLang === 'en' ? ' (English)' : ' (영문)'
        if (!dummyVal.includes(suffix) && !dummyVal.includes('영문')) {
          dummyVal += suffix
        }
      }

      sampleObj[key] = dummyVal
    })
  })

  let finalPayload
  if (rootKey) {
    finalPayload = {
      [rootKey]: [sampleObj]
    }
  } else {
    finalPayload = sampleObj
  }

  return JSON.stringify(finalPayload, null, 2)
})

const copySampleJsonPayload = () => {
  if (navigator.clipboard && sampleJsonPayload.value) {
    navigator.clipboard.writeText(sampleJsonPayload.value)
    init({ message: t('integration.channels.json_copied'), color: 'success' })
  }
}

const copyCurlSample = () => {
  const url = getWebhookUrl()
  let authHeaderStr = ''
  if (uiConfig.value.inboundAuthType === 'BEARER_TOKEN') {
    authHeaderStr = `  -H "Authorization: Bearer ${uiConfig.value.inboundSecretToken || 'secretToken'}" \\\n`
  } else if (uiConfig.value.inboundAuthType === 'API_KEY') {
    authHeaderStr = `  -H "X-API-KEY: ${uiConfig.value.inboundSecretToken || 'secretToken'}" \\\n`
  }

  const curlCmd = `curl -X POST "${url}" \\\n  -H "Content-Type: application/json" \\\n${authHeaderStr}  -d '${sampleJsonPayload.value}'`

  if (navigator.clipboard) {
    navigator.clipboard.writeText(curlCmd)
    init({ message: t('integration.channels.curl_copied'), color: 'success' })
  }
}

const copyAuthHeaderValue = () => {
  let headerValue = ''
  if (uiConfig.value.inboundAuthType === 'BEARER_TOKEN') {
    headerValue = `Bearer ${uiConfig.value.inboundSecretToken || 'secretToken'}`
  } else if (uiConfig.value.inboundAuthType === 'API_KEY') {
    headerValue = uiConfig.value.inboundSecretToken || 'secretToken'
  }

  if (navigator.clipboard && headerValue) {
    navigator.clipboard.writeText(headerValue)
    init({ message: t('integration.channels.header_value_copied'), color: 'success' })
  }
}

const formData = ref({ ...initialForm })
let editingId = null

// UI Bindings
const uiConfig = ref({
  wsUrl: '', wsMethod: 'POST', wsHeaders: [],
  jdbcUrl: '', jdbcUser: '', jdbcPassword: '', jdbcTable: '',
  mqBroker: '', mqTopic: '',
  inboundAuthType: 'BEARER_TOKEN', inboundSecretToken: ''
})
const uiMappings = ref([])
const uiMappingRootPath = ref('')

const generateSecretToken = () => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let tokenStr = 'sec_'
  for (let i = 0; i < 24; i++) {
    tokenStr += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  uiConfig.value.inboundSecretToken = tokenStr
}

const addWsHeader = () => {
  uiConfig.value.wsHeaders.push({ key: '', value: '' })
}
const removeWsHeader = (index) => {
  uiConfig.value.wsHeaders.splice(index, 1)
}

const onDirectionChanged = (newDir) => {
  if (newDir === 'INBOUND') {
    formData.value.type = 'WEB_SERVICE'
  }
}

const testConnection = async () => {
  if (formData.value.direction === 'INBOUND') return

  // Config Validation before testing
  if (formData.value.type === 'WEB_SERVICE' && !uiConfig.value.wsUrl) return init({ message: t('integration.channels.err_ws_url_required'), color: 'warning' })
  if (formData.value.type === 'JDBC' && !uiConfig.value.jdbcUrl) return init({ message: t('integration.channels.err_db_url_required'), color: 'warning' })
  if (formData.value.type === 'MESSAGE_QUEUE' && !uiConfig.value.mqBroker) return init({ message: t('integration.channels.err_mq_broker_required'), color: 'warning' })

  // Temporarily serialize ui config to test payload
  let config = {}
  if (formData.value.type === 'WEB_SERVICE') {
    config = { url: uiConfig.value.wsUrl, method: uiConfig.value.wsMethod, headers: uiConfig.value.wsHeaders.filter(h => h.key && h.value) }
  } else if (formData.value.type === 'JDBC') {
    config = { url: uiConfig.value.jdbcUrl, user: uiConfig.value.jdbcUser, password: uiConfig.value.jdbcPassword, table: uiConfig.value.jdbcTable }
  } else if (formData.value.type === 'MESSAGE_QUEUE') {
    config = { broker: uiConfig.value.mqBroker, topic: uiConfig.value.mqTopic }
  }

  isTesting.value = true
  try {
    const res = await $fetch('/api/admin/integration/channels/test-connection', {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` },
      body: {
        type: formData.value.type,
        configJson: JSON.stringify(config)
      }
    })
    
    if (res.success) {
      init({ message: res.message, color: 'success' })
    } else {
      init({ message: res.message, color: 'danger', duration: 5000 })
    }
  } catch (e) {
    console.error('Test connection error:', e)
    init({ message: t('integration.channels.err_test_connection'), color: 'danger' })
  } finally {
    isTesting.value = false
  }
}

const parseI18nName = (nameObj) => {
  if (!nameObj) return ''
  const currentLang = (locale?.value || 'ko').toLowerCase().startsWith('en') ? 'en' : 'ko'
  try {
    const parsed = typeof nameObj === 'object' ? nameObj : (String(nameObj).trim().startsWith('{') ? JSON.parse(nameObj) : null)
    if (parsed && typeof parsed === 'object') {
      const val = currentLang === 'en' ? (parsed.en || parsed.ko) : (parsed.ko || parsed.en)
      if (val) return String(val)
    }
  } catch (e) {}
  return String(nameObj).trim()
}

const fetchDomains = async () => {
  try {
    const data = await $fetch('/api/domains', {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    rawDomains.value = data || []
  } catch (e) { console.error('Failed to load domains', e) }
}

const fetchNodesAndFields = async (domainId) => {
  if (!domainId) {
    rawNodes.value = []
    rawFields.value = []
    return
  }
  try {
    const [nodesRes, fieldsRes] = await Promise.all([
      $fetch(`/api/domains/${domainId}/nodes/tree`, { headers: { Authorization: `Bearer ${token.value}` } }),
      $fetch(`/api/domains/${domainId}/fields`, { headers: { Authorization: `Bearer ${token.value}` } })
    ])
    rawNodes.value = nodesRes || []
    rawFields.value = fieldsRes || []
  } catch (e) {
    console.error('Failed to load nodes/fields', e)
  }
}

const onDomainSelected = (domainId) => {
  formData.value.nodeId = null
  fetchNodesAndFields(domainId)
}

// AG-Grid Settings
let mappingGridApi = null
const onMappingGridReady = (params) => {
  mappingGridApi = params.api
  setTimeout(() => {
    if (params.api && !params.api.isDestroyed()) {
      try {
        const gridDiv = params.api.getGridOption ? params.api.getGridOption('eGridDiv') : null
        const width = gridDiv ? gridDiv.clientWidth : 0
        if (width > 0) {
          params.api.sizeColumnsToFit()
        }
      } catch (e) {}
    }
  }, 250)
}

const validateSpelExpression = (expr) => {
  if (!expr) return true
  let openBrackets = 0
  let openParens = 0
  let inSingleQuote = false
  let inDoubleQuote = false
  
  for (let i = 0; i < expr.length; i++) {
    const c = expr[i]
    if (c === "'" && !inDoubleQuote) inSingleQuote = !inSingleQuote
    else if (c === '"' && !inSingleQuote) inDoubleQuote = !inDoubleQuote
    else if (!inSingleQuote && !inDoubleQuote) {
      if (c === '[') openBrackets++
      else if (c === ']') openBrackets--
      else if (c === '(') openParens++
      else if (c === ')') openParens--
      
      if (openBrackets < 0 || openParens < 0) return false
    }
  }
  return openBrackets === 0 && openParens === 0 && !inSingleQuote && !inDoubleQuote
}

const mappingColumnDefs = computed(() => {
  const isInbound = formData.value.direction === 'INBOUND'
  return [
    { 
      field: 'targetField', 
      headerName: isInbound ? t('integration.channels.target_field_inbound') : t('integration.channels.target_field'), 
      flex: 1, 
      minWidth: 220, 
      editable: true 
    },
    { 
      field: 'selectedField', 
      headerName: isInbound ? t('integration.channels.domain_field') : t('integration.channels.domain_field'), 
      flex: 1, 
      minWidth: 160,
      editable: true,
      cellEditor: 'agSelectCellEditor',
      cellEditorParams: () => {
        return {
          values: domainFields.value.map(f => f.code)
        }
      },
      valueFormatter: (params) => {
        if (!params.value) return ''
        const field = domainFields.value.find(f => f.code === params.value)
        return field ? field.name : params.value
      }
    },
    { 
      field: 'sourceExpression', 
    headerName: t('integration.channels.source_expr'), 
    flex: 3, 
    minWidth: 250, 
    editable: true,
    cellStyle: (params) => {
      if (!validateSpelExpression(params.value)) {
        return { backgroundColor: '#ffebee', color: 'red', border: '1px solid red' }
      }
      return { backgroundColor: 'transparent', color: 'inherit', border: '1px solid transparent' }
    }
  },
  {
    headerName: t('integration.channels.management'),
    width: 120,
    suppressSizeToFit: true,
    cellStyle: { textAlign: 'center' },
    cellRenderer: (params) => {
      const eDiv = document.createElement('div')
      eDiv.innerHTML = `<button type="button" style="color: red; cursor: pointer; border: none; background: none; font-weight: bold;">${t('delete')}</button>`
      const btn = eDiv.querySelector('button')
      btn.addEventListener('click', (e) => {
        e.preventDefault()
        e.stopPropagation()
        params.api.applyTransaction({ remove: [params.node.data] })
        syncUiMappingsFromGrid()
      })
      return eDiv
    }
  }
  ]
})

const onMappingCellValueChanged = (event) => {
  if (event.column.colId === 'selectedField' && event.newValue) {
    if (formData.value.direction === 'INBOUND') {
      // Inbound: 외부(소스) -> 내부(타겟). 도메인 필드를 선택하면 타겟 필드(내부 필드)로 자동 지정
      event.node.setDataValue('targetField', event.newValue)
    }
    event.node.setDataValue('sourceExpression', `payload['${event.newValue}']`)
  }
  syncUiMappingsFromGrid()
}

const autoGenerateMappings = () => {
  if (!rawFields.value || rawFields.value.length === 0) {
    init({ message: '매핑할 도메인 필드가 없습니다. 상단 기본 정보 탭에서 도메인을 먼저 선택해 주세요.', color: 'warning' })
    return
  }

  const newRows = rawFields.value.map(f => ({
    targetField: f.key,
    selectedField: f.key,
    sourceExpression: `payload['${f.key}']`
  }))

  uiMappings.value = newRows
  if (mappingGridApi) {
    mappingGridApi.setGridOption('rowData', newRows)
  }
  syncUiMappingsFromGrid()
  init({ message: `${newRows.length}개 도메인 필드 매핑이 자동 세팅되었습니다.`, color: 'success' })
}

const addMapping = () => {
  if (mappingGridApi) {
    mappingGridApi.applyTransaction({ add: [{ targetField: '', selectedField: null, sourceExpression: '' }] })
  } else {
    uiMappings.value.push({ targetField: '', selectedField: null, sourceExpression: '' })
  }
  syncUiMappingsFromGrid()
}

const serializeUiData = () => {
  let config = {}
  if (formData.value.type === 'WEB_SERVICE') {
    config = { 
        url: uiConfig.value.wsUrl, 
        method: uiConfig.value.wsMethod,
        headers: uiConfig.value.wsHeaders.filter(h => h.key && h.value) 
      }
  } else if (formData.value.type === 'JDBC') {
    config = { url: uiConfig.value.jdbcUrl, user: uiConfig.value.jdbcUser, password: uiConfig.value.jdbcPassword, table: uiConfig.value.jdbcTable }
  } else if (formData.value.type === 'MESSAGE_QUEUE') {
    config = { broker: uiConfig.value.mqBroker, topic: uiConfig.value.mqTopic }
  }
  
  if (formData.value.direction === 'INBOUND') {
    config.authType = uiConfig.value.inboundAuthType || 'BEARER_TOKEN'
    config.secretToken = uiConfig.value.inboundSecretToken || ''
  }

  // UI 복원을 위해 domainId 저장
  if (selectedDomainId.value) {
    config.domainId = selectedDomainId.value
  }

  formData.value.configJson = JSON.stringify(config)

  // Extract from AG-Grid
  if (mappingGridApi) {
    const rowData = []
    mappingGridApi.forEachNode(node => rowData.push(node.data))
    uiMappings.value = rowData
  }

  if (uiMappings.value.length > 0) {
    const mappingsToSave = uiMappings.value.map(m => ({
      targetField: m.targetField,
      sourceExpression: m.sourceExpression
    }))
    const mappingObj = { mappings: mappingsToSave }
    if (uiMappingRootPath.value) {
      mappingObj.rootPath = uiMappingRootPath.value
    }
    formData.value.mappingConfigJson = JSON.stringify(mappingObj)
  } else {
    formData.value.mappingConfigJson = JSON.stringify({})
  }
}

const deserializeUiData = (row) => {
  uiConfig.value = {
    wsUrl: '', wsMethod: 'POST', wsHeaders: [],
    jdbcUrl: '', jdbcUser: '', jdbcPassword: '', jdbcTable: '',
    mqBroker: '', mqTopic: '',
    inboundAuthType: 'BEARER_TOKEN', inboundSecretToken: ''
  }
  uiMappings.value = []
  uiMappingRootPath.value = ''
  selectedDomainId.value = null
  
  if (!row) return

  try {
    const config = JSON.parse(row.configJson || '{}')
    
    // 복원된 domainId가 있으면 도메인 필드와 노드 목록 불러오기
    if (config.domainId) {
      selectedDomainId.value = config.domainId
      fetchNodesAndFields(config.domainId)
    }

    if (row.direction === 'INBOUND') {
      uiConfig.value.inboundAuthType = config.authType || 'BEARER_TOKEN'
      uiConfig.value.inboundSecretToken = config.secretToken || ''
    }

    if (row.type === 'WEB_SERVICE') {
      uiConfig.value.wsUrl = config.url || ''
      uiConfig.value.wsMethod = config.method || 'POST'
      uiConfig.value.wsHeaders = config.headers || []
    } else if (row.type === 'JDBC') {
      uiConfig.value.jdbcUrl = config.url || ''
      uiConfig.value.jdbcUser = config.user || ''
      uiConfig.value.jdbcPassword = config.password || ''
      uiConfig.value.jdbcTable = config.table || ''
    } else if (row.type === 'MESSAGE_QUEUE') {
      uiConfig.value.mqBroker = config.broker || ''
      uiConfig.value.mqTopic = config.topic || ''
    }
  } catch (e) { console.error('Failed to parse configJson', e) }

  try {
    const mapping = JSON.parse(row.mappingConfigJson || '{}')
    uiMappingRootPath.value = mapping.rootPath || ''
    
    if (mapping && mapping.mappings && Array.isArray(mapping.mappings)) {
      uiMappings.value = mapping.mappings.map(m => {
        // Try to guess the selected field from sourceExpression
        let selectedField = null
        const match = m.sourceExpression?.match(/(?:payload|#this)\['(.+)'\]/)
        if (match) selectedField = match[1]
        
        return {
          targetField: m.targetField,
          sourceExpression: m.sourceExpression,
          selectedField
        }
      })
    }
    // Refresh grid if open
    if (mappingGridApi) {
      mappingGridApi.setGridOption('rowData', uiMappings.value)
    }
  } catch (e) { console.error('Failed to parse mappingConfigJson', e) }
}


// Recent Logs Logic
const recentLogs = ref([])
const isLogsLoading = ref(false)
const logColumns = [
  { key: 'direction', label: '방향' },
  { key: 'channelName', label: '채널명' },
  { key: 'eventType', label: '이벤트 타입' },
  { key: 'status', label: '상태' },
  { key: 'createdAt', label: '발생 일시' }
]

const getChannelNameById = (id) => {
  const ch = channels.value.find(c => c.id === id)
  if (ch) return parseI18nName(ch.name)
  return id
}

// AG-Grid Column Definitions for Channels
const channelColumnDefs = computed(() => [
  {
    field: 'name',
    headerName: t('integration.channels.channel_name'),
    flex: 1.2,
    cellRenderer: (params) => {
      const div = document.createElement('div')
      div.style.cssText = 'display: flex; align-items: center; height: 100%; font-weight: 700; color: var(--va-text-primary); font-family: inherit;'
      div.textContent = parseI18nName(params.value) || '-'
      return div
    }
  },
  {
    field: 'channelCode',
    headerName: t('integration.channels.channel_code'),
    width: 140,
    cellRenderer: (params) => {
      const div = document.createElement('div')
      div.style.cssText = 'display: flex; align-items: center; height: 100%;'
      const chip = document.createElement('span')
      chip.style.cssText = 'padding: 2px 8px; background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 4px; font-family: monospace; font-size: 0.8rem; font-weight: 600;'
      chip.textContent = params.value || '-'
      div.appendChild(chip)
      return div
    }
  },
  {
    field: 'direction',
    headerName: t('integration.channels.direction'),
    width: 130,
    cellRenderer: (params) => {
      const div = document.createElement('div')
      div.style.cssText = 'display: flex; align-items: center; height: 100%;'
      const isInbound = params.value === 'INBOUND'
      const pill = document.createElement('span')
      pill.style.cssText = `padding: 2px 8px; border-radius: 12px; font-weight: 700; font-size: 0.75rem; font-family: inherit; ${
        isInbound
          ? 'background: rgba(237, 108, 2, 0.12); color: var(--va-warning); border: 1px solid rgba(237, 108, 2, 0.3);'
          : 'background: rgba(25, 118, 210, 0.12); color: var(--va-primary); border: 1px solid rgba(25, 118, 210, 0.3);'
      }`
      pill.textContent = isInbound ? (t('integration.channels.inbound')) : (t('integration.channels.outbound'))
      div.appendChild(pill)
      return div
    }
  },
  {
    field: 'isActive',
    headerName: t('integration.channels.status'),
    width: 120,
    cellRenderer: (params) => {
      const div = document.createElement('div')
      div.style.cssText = 'display: flex; align-items: center; height: 100%;'
      const isActive = Boolean(params.value)
      const pill = document.createElement('span')
      pill.style.cssText = `padding: 2px 8px; border-radius: 12px; font-weight: 700; font-size: 0.75rem; font-family: inherit; ${
        isActive
          ? 'background: rgba(46, 125, 50, 0.12); color: var(--va-success); border: 1px solid rgba(46, 125, 50, 0.3);'
          : 'background: rgba(229, 57, 53, 0.12); color: var(--va-danger); border: 1px solid rgba(229, 57, 53, 0.3);'
      }`
      pill.textContent = isActive ? 'Active' : 'Inactive'
      div.appendChild(pill)
      return div
    }
  },
  {
    field: 'createdAt',
    headerName: t('createdAt'),
    width: 170,
    cellRenderer: (params) => {
      const div = document.createElement('div')
      div.style.cssText = 'display: flex; align-items: center; height: 100%; font-size: 0.85rem; color: var(--va-text-secondary);'
      div.textContent = formatDate(params.value)
      return div
    }
  },
  {
    field: 'actions',
    headerName: t('actions'),
    width: 140,
    sortable: false,
    cellRenderer: (params) => {
      const div = document.createElement('div')
      div.style.cssText = 'display: flex; align-items: center; justify-content: center; gap: 0.35rem; height: 100%;'

      const metricsBtn = document.createElement('button')
      metricsBtn.style.cssText = 'border: none; background: rgba(76, 175, 80, 0.12); color: var(--va-success); border-radius: 6px; padding: 4px 8px; cursor: pointer; display: flex; align-items: center; font-weight: 600; font-size: 0.78rem;'
      metricsBtn.title = t('channel_metrics')
      metricsBtn.innerHTML = `<span class="material-icons" style="font-size: 16px;">analytics</span>`
      metricsBtn.addEventListener('click', () => openMetricsModal(params.data))

      const editBtn = document.createElement('button')
      editBtn.style.cssText = 'border: none; background: rgba(25, 118, 210, 0.1); color: var(--va-primary); border-radius: 6px; padding: 4px 8px; cursor: pointer; display: flex; align-items: center; font-weight: 600; font-size: 0.78rem;'
      editBtn.innerHTML = `<span class="material-icons" style="font-size: 16px;">edit</span>`
      editBtn.addEventListener('click', () => openEditModal(params.data))

      const deleteBtn = document.createElement('button')
      deleteBtn.style.cssText = 'border: none; background: rgba(229, 57, 53, 0.1); color: var(--va-danger); border-radius: 6px; padding: 4px 8px; cursor: pointer; display: flex; align-items: center; font-weight: 600; font-size: 0.78rem;'
      deleteBtn.innerHTML = `<span class="material-icons" style="font-size: 16px;">delete</span>`
      deleteBtn.addEventListener('click', () => confirmDelete(params.data.id))

      div.appendChild(metricsBtn)
      div.appendChild(editBtn)
      div.appendChild(deleteBtn)
      return div
    }
  }
])

// AG-Grid Column Definitions for Recent Logs
const recentLogColumnDefs = computed(() => [
  {
    field: 'channelId',
    headerName: t('integration.channels.channel_name'),
    flex: 1.2,
    cellRenderer: (params) => {
      const div = document.createElement('div')
      div.style.cssText = 'display: flex; align-items: center; height: 100%; font-weight: 600; font-family: inherit;'
      div.textContent = getChannelNameById(params.value)
      return div
    }
  },
  {
    field: 'direction',
    headerName: t('integration.channels.direction'),
    width: 130,
    cellRenderer: (params) => {
      const div = document.createElement('div')
      div.style.cssText = 'display: flex; align-items: center; height: 100%;'
      const isInbound = params.value === 'INBOUND'
      const pill = document.createElement('span')
      pill.style.cssText = `padding: 2px 8px; border-radius: 12px; font-weight: 700; font-size: 0.75rem; font-family: inherit; ${
        isInbound
          ? 'background: rgba(237, 108, 2, 0.12); color: var(--va-warning); border: 1px solid rgba(237, 108, 2, 0.3);'
          : 'background: rgba(25, 118, 210, 0.12); color: var(--va-primary); border: 1px solid rgba(25, 118, 210, 0.3);'
      }`
      pill.textContent = isInbound ? 'Inbound' : 'Outbound'
      div.appendChild(pill)
      return div
    }
  },
  {
    field: 'status',
    headerName: t('integration.channels.status'),
    width: 120,
    cellRenderer: (params) => {
      const div = document.createElement('div')
      div.style.cssText = 'display: flex; align-items: center; height: 100%;'
      const isSuccess = params.value === 'SUCCESS'
      const pill = document.createElement('span')
      pill.style.cssText = `padding: 2px 8px; border-radius: 12px; font-weight: 700; font-size: 0.75rem; font-family: inherit; ${
        isSuccess
          ? 'background: rgba(46, 125, 50, 0.12); color: var(--va-success); border: 1px solid rgba(46, 125, 50, 0.3);'
          : 'background: rgba(229, 57, 53, 0.12); color: var(--va-danger); border: 1px solid rgba(229, 57, 53, 0.3);'
      }`
      pill.textContent = params.value || 'SUCCESS'
      div.appendChild(pill)
      return div
    }
  },
  {
    field: 'createdAt',
    headerName: t('createdAt'),
    flex: 1,
    cellRenderer: (params) => {
      const div = document.createElement('div')
      div.style.cssText = 'display: flex; align-items: center; height: 100%; font-size: 0.85rem; color: var(--va-text-secondary);'
      div.textContent = formatDate(params.value)
      return div
    }
  }
])

const fetchRecentLogs = async () => {
  isLogsLoading.value = true
  try {
    const res = await customFetch('/api/admin/integration/logs?page=0&size=10&sortField=createdAt&sortOrder=DESC')
    recentLogs.value = res?.content || []
  } catch (error) {
    console.error('Failed to fetch recent logs', error)
  } finally {
    isLogsLoading.value = false
  }
}

const fetchChannels = async () => {
  isLoading.value = true
  try {
    const data = await $fetch('/api/admin/integration/channels', {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    channels.value = data
  } catch (e) {
    console.error('Failed to fetch channels:', e)
  } finally {
    isLoading.value = false
  }
}

const openCreateModal = () => {
  isEdit.value = false
  editingId = null
  formData.value = { ...initialForm }
  channelNameKo.value = ''
  channelNameEn.value = ''
  activeModalTab.value = 'basic'
  deserializeUiData(formData.value)
  showModal.value = true
}

const openEditModal = (row) => {
  isEdit.value = true
  editingId = row.id
  formData.value = { ...row }
  const parts = extractNameParts(row.name)
  channelNameKo.value = parts.ko
  channelNameEn.value = parts.en
  activeModalTab.value = 'basic'

  if (!formData.value.direction) {
    formData.value.direction = 'OUTBOUND'
  }
  deserializeUiData(row)
  showModal.value = true
}

const submitForm = async () => {
  if (!channelNameKo.value && !channelNameEn.value) {
    init({ message: '채널명을 입력해 주세요.', color: 'warning' })
    return
  }

  formData.value.name = JSON.stringify({
    ko: channelNameKo.value || channelNameEn.value,
    en: channelNameEn.value || channelNameKo.value
  })

  if (!form.value.validate()) return

  if (formData.value.direction === 'INBOUND') {
    if (!selectedDomainId.value) {
      init({ message: t('integration.channels.domain_required_for_inbound'), color: 'danger' })
      return
    }
    if (!formData.value.nodeId) {
      init({ message: t('integration.channels.node_required_for_inbound'), color: 'danger' })
      return
    }
  }

  serializeUiData()

  try {
    if (isEdit.value) {
      await $fetch(`/api/admin/integration/channels/${editingId}`, {
        method: 'PUT',
        headers: { Authorization: `Bearer ${token.value}` },
        body: formData.value
      })
    } else {
      await $fetch('/api/admin/integration/channels', {
        method: 'POST',
        headers: { Authorization: `Bearer ${token.value}` },
        body: formData.value
      })
    }
    showModal.value = false
    fetchChannels()
  } catch (e) {
    console.error('Failed to save channel:', e)
  }
}

const confirmDelete = async (id) => {
  try {
    await $fetch(`/api/admin/integration/channels/${id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    fetchChannels()
  } catch (e) {
    console.error('Failed to delete channel:', e)
  }
}

onMounted(async () => {
  await codeStore.preloadGroups(['INTEGRATION_DIRECTION', 'INTEGRATION_TYPE', 'HTTP_METHOD'])
  fetchChannels()
  fetchRecentLogs()
  fetchDomains()
})
</script>

<style scoped>
.page-container {
  padding: 1rem;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}
.page-title {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}
.action-buttons {
  display: flex;
  gap: 0.25rem;
}
</style>
