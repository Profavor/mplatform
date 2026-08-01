<template>
  <div style="display: flex; flex-direction: column; gap: 1.25rem; padding-bottom: 2rem;">
    <!-- Top Action Bar -->
    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); padding: 1rem 1.25rem; border-radius: 12px; border: 1px solid var(--va-background-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04);">
      <div style="display: flex; align-items: center; gap: 0.75rem;">
        <va-icon name="hub" size="large" color="primary" />
        <div>
          <h2 style="font-weight: 700; font-size: 1.35rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
            {{ $t('integration.channels.title') || '연계 채널 관리' }}
            <va-badge text="Integration" color="primary" size="small" />
          </h2>
          <span style="font-size: 0.85rem; color: var(--va-text-secondary);">
            {{ $t('integration.channels.desc') || '외부 시스템과의 데이터 연동 파이프라인 및 인터페이스 채널 설정을 관리합니다.' }}
          </span>
        </div>
      </div>

      <div style="display: flex; gap: 0.75rem; align-items: center;">
        <va-button color="primary" icon="add" size="small" @click="openCreateModal">
          {{ $t('integration.channels.add') || '신규 채널 등록' }}
        </va-button>
        <va-button preset="outline" color="primary" icon="refresh" size="small" @click="fetchChannels">
          {{ $t('refresh') || '새로고침' }}
        </va-button>
      </div>
    </div>

    <!-- Channels Table -->
    <va-card class="mb-4">
      <va-card-content>
        <va-data-table
          :items="channels"
          :columns="columns"
          :loading="isLoading"
          striped
        >
          <template #cell(name)="{ rowData }">
            <span style="font-weight: 700; color: var(--va-text-primary);">
              {{ parseI18nName(rowData.name) }}
            </span>
          </template>
          <template #cell(direction)="{ rowData }">
            <va-badge
              :text="rowData.direction === 'INBOUND' ? $t('integration.channels.inbound') : $t('integration.channels.outbound')"
              :color="rowData.direction === 'INBOUND' ? 'warning' : 'info'"
            />
          </template>
          <template #cell(isActive)="{ rowData }">
            <va-badge
              :text="rowData.isActive ? 'Active' : 'Inactive'"
              :color="rowData.isActive ? 'success' : 'danger'"
            />
          </template>
          <template #cell(createdAt)="{ rowData }">
            {{ formatDate(rowData.createdAt) }}
          </template>
          <template #cell(actions)="{ rowData }">
            <div class="action-buttons">
              <va-button preset="plain" color="primary" @click="openEditModal(rowData)">
                <va-icon name="edit" />
              </va-button>
              <va-button preset="plain" color="danger" @click="confirmDelete(rowData.id)">
                <va-icon name="delete" />
              </va-button>
            </div>
          </template>
        </va-data-table>
      </va-card-content>
    </va-card>

    <!-- Recent Integration Logs Dashboard -->
    <va-card class="mb-4">
      <va-card-title>
        <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
          <span style="font-size: 1.1rem; color: var(--va-text-primary);">
            <va-icon name="monitor_heart" class="mr-2" color="primary" />
            최근 연동 로그 모니터링 (Recent Integration Logs)
          </span>
          <va-button preset="secondary" icon="refresh" size="small" @click="fetchRecentLogs">새로고침</va-button>
        </div>
      </va-card-title>
      <va-card-content>
        <va-data-table
          :items="recentLogs"
          :columns="logColumns"
          :loading="isLogsLoading"
          striped
          hoverable
        >
          <template #cell(direction)="{ rowData }">
            <va-badge
              :text="rowData.direction === 'INBOUND' ? 'Inbound' : 'Outbound'"
              :color="rowData.direction === 'INBOUND' ? 'warning' : 'info'"
            />
          </template>
          <template #cell(status)="{ rowData }">
            <va-badge
              :text="rowData.status"
              :color="rowData.status === 'SUCCESS' ? 'success' : 'danger'"
            />
          </template>
          <template #cell(channelName)="{ rowData }">
            {{ getChannelNameById(rowData.channelId) }}
          </template>
          <template #cell(createdAt)="{ rowData }">
            {{ formatDate(rowData.createdAt) }}
          </template>
        </va-data-table>
      </va-card-content>
    </va-card>

    <!-- Create/Edit Modal (Premium Standardized Design) -->
    <va-modal
      v-model="showModal"
      :title="isEdit ? ($t('integration.channels.edit') || '연계 채널 정보 수정') : ($t('integration.channels.add') || '신규 연계 채널 등록')"
      size="large"
      hide-default-actions
    >
      <div style="min-width: 750px; max-width: 900px; padding: 0.5rem 0.25rem;">
        <!-- Navigation Tabs -->
        <va-tabs v-model="activeModalTab" class="mb-4" style="border-bottom: 1px solid var(--va-background-border);">
          <template #tabs>
            <va-tab name="basic" style="font-weight: 700;">
              <va-icon name="tune" class="mr-2" /> {{ $t('integration.channels.basic_config') || '기본 정보 & 연동 설정' }}
            </va-tab>
            <va-tab name="mapping" style="font-weight: 700;">
              <va-icon name="swap_horiz" class="mr-2" /> {{ $t('integration.channels.field_mapping') || '데이터 필드 매핑' }}
            </va-tab>
          </template>
        </va-tabs>

        <va-form ref="form" @submit.prevent="submitForm">
          <!-- TAB 1: Basic Config -->
          <div v-show="activeModalTab === 'basic'" style="display: flex; flex-direction: column; gap: 1.25rem;">
            <!-- Multilingual Channel Name -->
            <MultilingualInput
              v-model:ko="channelNameKo"
              v-model:en="channelNameEn"
              :label="$t('integration.channels.name') || '채널명'"
              required
            />

            <!-- Direction & Type & Active -->
            <div style="display: grid; grid-template-columns: 1fr 1fr 130px; gap: 1rem; align-items: flex-end;">
              <va-select
                v-model="formData.direction"
                :options="directionOptions"
                value-by="value"
                text-by="text"
                :label="$t('integration.channels.direction')"
                @update:modelValue="onDirectionChanged"
                required
              />
              <va-select
                v-model="formData.type"
                :options="['WEB_SERVICE', 'JDBC', 'MESSAGE_QUEUE']"
                :label="$t('integration.channels.type')"
                :disabled="formData.direction === 'INBOUND'"
                required
              />
              <div style="padding-bottom: 0.5rem;">
                <va-checkbox v-model="formData.isActive" :label="$t('integration.channels.is_active') || '활성화'" />
              </div>
            </div>

            <!-- INBOUND Auth & Webhook Info -->
            <template v-if="formData.direction === 'INBOUND'">
              <div style="background: var(--va-background-element); border-radius: 12px; padding: 1.25rem; border: 1px solid var(--va-background-border); display: flex; flex-direction: column; gap: 1rem;">
                <div style="font-weight: 700; font-size: 0.95rem; color: var(--va-primary); display: flex; align-items: center; gap: 0.4rem;">
                  <va-icon name="security" size="small" color="primary" />
                  {{ $t('integration.channels.auth_type') || '수신(INBOUND) 인증 및 보안 설정' }}
                </div>
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                  <va-select
                    v-model="uiConfig.inboundAuthType"
                    :options="authTypeOptions"
                    value-by="value"
                    text-by="text"
                    :label="$t('integration.channels.auth_type')"
                  />
                  <div v-if="uiConfig.inboundAuthType !== 'NONE'" style="display: flex; gap: 0.5rem; align-items: flex-end;">
                    <va-input v-model="uiConfig.inboundSecretToken" :label="$t('integration.channels.secret_token')" placeholder="sec_token_..." style="flex: 1;" required />
                    <va-button preset="secondary" color="primary" icon="autorenew" @click="generateSecretToken" style="white-space: nowrap;">
                      {{ $t('integration.channels.generate_token') || '토큰 생성' }}
                    </va-button>
                  </div>
                </div>
                <div>
                  <va-checkbox v-model="formData.requiresApproval" :label="`${$t('integration.channels.requires_approval') || 'Requires Approval'}`" />
                </div>
              </div>

              <!-- Webhook Guide Card (Premium Modern Light Theme) -->
              <div style="background: linear-gradient(135deg, rgba(238,242,255,0.8), rgba(243,244,256,0.5)); border-radius: 12px; padding: 1.25rem; border: 1px solid rgba(199,210,254,0.8); display: flex; flex-direction: column; gap: 0.75rem;">
                <div style="font-weight: 700; font-size: 0.9rem; color: #4338ca; display: flex; align-items: center; justify-content: space-between;">
                  <span style="display: flex; align-items: center; gap: 0.4rem;">
                    <va-icon name="link" size="small" color="#4338ca" /> {{ $t('integration.channels.webhook_url') || '수신 Webhook URL 가이드' }}
                  </span>
                  <va-button size="small" color="primary" icon="content_copy" @click="copyWebhookUrl">
                    {{ $t('integration.channels.webhook_copy') || 'URL 복사' }}
                  </va-button>
                </div>
                <div style="font-size: 0.83rem; color: #374151; line-height: 1.4;">
                  {{ $t('integration.channels.inbound_notice') || '외부 시스템에서 아래 Webhook URL로 JSON Payload를 POST 요청하면 설정된 매핑 규칙에 따라 데이터가 연동 처리됩니다.' }}
                </div>
                <va-input :model-value="getWebhookUrl()" readonly style="font-family: monospace; font-size: 0.85rem;" />
                <div v-if="uiConfig.inboundAuthType !== 'NONE'" style="font-size: 0.8rem; background: rgba(255,255,255,0.95); padding: 0.6rem 0.85rem; border-radius: 8px; border: 1px solid #c7d2fe; color: #1e1b4b; display: flex; justify-content: space-between; align-items: center; gap: 0.5rem; flex-wrap: wrap;">
                  <div style="display: flex; align-items: center; gap: 0.6rem;">
                    <va-chip size="small" color="primary" outline style="font-weight: 700;">
                      Header
                    </va-chip>
                    <div style="display: flex; align-items: center; gap: 0.4rem; font-family: monospace;">
                      <span style="font-weight: 700; color: #3730a3;">
                        {{ uiConfig.inboundAuthType === 'BEARER_TOKEN' ? 'Authorization' : 'X-API-KEY' }}:
                      </span>
                      <code style="color: #4338ca; font-weight: bold; background: #eef2ff; padding: 2px 6px; border-radius: 4px;">
                        {{ uiConfig.inboundAuthType === 'BEARER_TOKEN' ? `Bearer ${uiConfig.inboundSecretToken || 'secretToken'}` : (uiConfig.inboundSecretToken || 'secretToken') }}
                      </code>
                    </div>
                  </div>
                  <va-button size="small" preset="secondary" color="primary" icon="content_copy" @click="copyAuthHeaderValue">
                    {{ $t('integration.channels.copy_value') || '값 복사' }}
                  </va-button>
                </div>

                <!-- Real-time JSON Payload Sample Box -->
                <div style="font-size: 0.8rem; background: rgba(255,255,255,0.95); padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid #c7d2fe; color: #1e1b4b; display: flex; flex-direction: column; gap: 0.6rem;">
                  <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.5rem;">
                    <strong style="display: flex; align-items: center; gap: 0.35rem; color: #3730a3;">
                      <va-icon name="code" size="small" color="#4338ca" />
                      {{ $t('integration.channels.sample_payload_title') || '요청 JSON Payload 샘플 (실시간 매핑 반영)' }}
                    </strong>
                    <div style="display: flex; gap: 0.4rem;">
                      <va-button size="small" preset="secondary" color="primary" icon="terminal" @click="copyCurlSample">
                        {{ $t('integration.channels.copy_curl') || 'cURL 복사' }}
                      </va-button>
                      <va-button size="small" preset="secondary" color="primary" icon="content_copy" @click="copySampleJsonPayload">
                        {{ $t('integration.channels.copy_json') || 'JSON 복사' }}
                      </va-button>
                    </div>
                  </div>
                  <pre style="margin: 0; font-family: 'Fira Code', 'Consolas', 'Courier New', monospace; font-size: 0.82rem; background: #0f172a; color: #38bdf8; padding: 0.75rem 1rem; border-radius: 6px; overflow-x: auto; max-height: 220px; line-height: 1.45; border: 1px solid #1e293b;">{{ sampleJsonPayload }}</pre>
                  <div style="font-size: 0.75rem; color: #64748b; display: flex; align-items: center; gap: 0.3rem;">
                    <va-icon name="info" size="extra-small" color="#64748b" />
                    <span>{{ $t('integration.channels.sample_payload_notice') || '매핑 탭에서 구성한 소스 표현식과 Root Path 정보가 실시간으로 반영된 요청 Payload 예시입니다.' }}</span>
                  </div>
                </div>
              </div>
            </template>

            <!-- OUTBOUND Detailed Config -->
            <template v-else-if="formData.direction === 'OUTBOUND'">
              <div style="background: var(--va-background-element); border-radius: 12px; padding: 1.25rem; border: 1px solid var(--va-background-border); display: flex; flex-direction: column; gap: 1rem;">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <div style="font-weight: 700; font-size: 0.95rem; color: var(--va-primary); display: flex; align-items: center; gap: 0.4rem;">
                    <va-icon name="settings_remote" size="small" color="primary" />
                    {{ $t('integration.channels.detail_config') || '송신(OUTBOUND) 연결 정보' }}
                  </div>
                  <va-button size="small" preset="secondary" color="info" icon="cloud_done" @click="testConnection" :loading="isTesting">
                    {{ $t('integration.channels.test_connection') || '연결 테스트' }}
                  </va-button>
                </div>

                <template v-if="formData.type === 'WEB_SERVICE'">
                  <va-input v-model="uiConfig.wsUrl" :label="$t('integration.channels.ws_url')" placeholder="http://api.example.com/webhook" required class="w-full" />
                  <va-select v-model="uiConfig.wsMethod" :options="['POST', 'PUT', 'GET']" :label="$t('integration.channels.ws_method')" class="w-full" />
                  <div style="margin-top: 0.5rem;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                      <span style="font-size: 0.85rem; font-weight: 700;">HTTP Headers</span>
                      <va-button size="small" preset="secondary" icon="add" @click="addWsHeader">{{ $t('integration.channels.add_header') }}</va-button>
                    </div>
                    <div v-if="uiConfig.wsHeaders.length === 0" style="font-size: 0.8rem; color: #888;">{{ $t('integration.channels.no_headers') }}</div>
                    <div v-for="(header, index) in uiConfig.wsHeaders" :key="index" style="display: flex; gap: 0.5rem; margin-bottom: 0.5rem; align-items: center;">
                      <va-input v-model="header.key" placeholder="Header Name" style="flex: 1;" required />
                      <va-input v-model="header.value" placeholder="Header Value" style="flex: 1;" required />
                      <va-button preset="plain" color="danger" icon="remove_circle" @click="removeWsHeader(index)" />
                    </div>
                  </div>
                </template>
                <template v-else-if="formData.type === 'JDBC'">
                  <va-input v-model="uiConfig.jdbcUrl" :label="$t('integration.channels.db_url')" placeholder="jdbc:mysql://localhost:3306/db" required class="w-full" />
                  <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                    <va-input v-model="uiConfig.jdbcUser" :label="$t('integration.channels.db_user')" />
                    <va-input v-model="uiConfig.jdbcPassword" type="password" :label="$t('integration.channels.db_password')" />
                  </div>
                  <va-input v-model="uiConfig.jdbcTable" :label="$t('integration.channels.db_table')" placeholder="integration_data" required class="w-full" />
                </template>
                <template v-else-if="formData.type === 'MESSAGE_QUEUE'">
                  <va-input v-model="uiConfig.mqBroker" :label="$t('integration.channels.mq_broker')" placeholder="kafka://localhost:9092" required class="w-full" />
                  <va-input v-model="uiConfig.mqTopic" :label="$t('integration.channels.mq_topic')" placeholder="events.data.changed" required class="w-full" />
                </template>
              </div>
            </template>

            <!-- Domain & Node Target Selection -->
            <div style="background: var(--va-background-element); border-radius: 12px; padding: 1.25rem; border: 1px solid var(--va-background-border); display: flex; flex-direction: column; gap: 1rem;">
              <div style="font-weight: 700; font-size: 0.95rem; color: var(--va-primary); display: flex; align-items: center; gap: 0.4rem;">
                <va-icon name="account_tree" size="small" color="primary" />
                {{ $t('integration.channels.select_domain_node') || '연계 대상 도메인 & 노드' }}
              </div>
              <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                <va-select
                  v-model="selectedDomainId"
                  :options="domains"
                  value-by="id"
                  text-by="name"
                  :label="$t('integration.channels.select_domain') + (formData.direction === 'INBOUND' ? ' *' : '')"
                  :rules="[v => formData.direction !== 'INBOUND' || !!v || $t('integration.channels.domain_required_for_inbound')]"
                  @update:modelValue="onDomainSelected"
                  clearable
                />
                <va-select
                  v-model="formData.nodeId"
                  :options="nodes"
                  value-by="id"
                  text-by="name"
                  :label="$t('integration.channels.select_node') + (formData.direction === 'INBOUND' ? ' *' : '')"
                  :rules="[v => formData.direction !== 'INBOUND' || !!v || $t('integration.channels.node_required_for_inbound')]"
                  :disabled="!selectedDomainId"
                  clearable
                />
              </div>
            </div>
          </div>

          <!-- TAB 2: Field Mapping -->
          <div v-show="activeModalTab === 'mapping'" style="display: flex; flex-direction: column; gap: 1rem;">
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <div>
                <h4 style="margin: 0; font-weight: 700; color: var(--va-text-primary);">
                  {{ $t('integration.channels.field_mapping') || '데이터 필드 매핑' }}
                </h4>
                <p style="margin: 0.25rem 0 0 0; font-size: 0.82rem; color: var(--va-text-secondary);">
                  * {{ formData.direction === 'INBOUND' ? $t('integration.channels.mapping_desc_inbound') : $t('integration.channels.mapping_desc') }}
                </p>
              </div>
              <va-button size="small" color="primary" icon="add" @click="addMapping">
                + {{ $t('integration.channels.add_field') || '필드 추가' }}
              </va-button>
            </div>

            <va-input 
              v-if="formData.direction === 'INBOUND'" 
              v-model="uiMappingRootPath" 
              :label="$t('integration.channels.mapping_root_path')" 
              :placeholder="$t('integration.channels.mapping_root_path_placeholder')" 
              clearable 
            />

            <div style="height: 360px; width: 100%; border-radius: 8px; overflow: hidden; border: 1px solid var(--va-background-border);">
              <client-only>
                <ag-grid-vue
                  v-if="showModal"
                  style="width: 100%; height: 100%;"
                  :theme="gridTheme"
                  :columnDefs="mappingColumnDefs"
                  :rowData="uiMappings"
                  :autoSizeStrategy="{ type: 'fitGridWidth' }"
                  @grid-ready="onMappingGridReady"
                  @cell-value-changed="onMappingCellValueChanged"
                >
                </ag-grid-vue>
              </client-only>
            </div>
          </div>
        </va-form>
      </div>

      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 0.75rem; width: 100%; padding-top: 1rem; border-top: 1px solid var(--va-background-border);">
          <va-button preset="secondary" color="secondary" @click="showModal = false">
            {{ $t('close') || '취소' }}
          </va-button>
          <va-button color="primary" icon="save" @click="submitForm">
            {{ $t('save') || '저장' }}
          </va-button>
        </div>
      </template>
    </va-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useCookie } from '#app'
import { useToast } from 'vuestic-ui'
import { AgGridVue } from 'ag-grid-vue3'
import { useAgGridTheme } from '~/composables/useAgGridTheme'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'

const { t, locale } = useI18n()
const { gridTheme } = useAgGridTheme()
const { init } = useToast()
const { customFetch } = useCustomFetch()
const token = useCookie('auth_token')

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
const activeModalTab = ref('basic')
const isEdit = ref(false)
const form = ref(null)

const directionOptions = computed(() => [
  { value: 'OUTBOUND', text: t('integration.channels.outbound') },
  { value: 'INBOUND', text: t('integration.channels.inbound') }
])

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
    init({ message: t('integration.channels.json_copied') || '샘플 JSON Payload가 클립보드에 복사되었습니다.', color: 'success' })
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
    init({ message: t('integration.channels.curl_copied') || 'cURL 호출 샘플이 클립보드에 복사되었습니다.', color: 'success' })
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
    init({ message: t('integration.channels.header_value_copied') || '헤더 값(Bearer 토큰)이 클립보드에 복사되었습니다.', color: 'success' })
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
        params.api.sizeColumnsToFit()
      } catch (e) {}
    }
  }, 100)
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
      if (!event.node.data.sourceExpression) {
        event.node.setDataValue('sourceExpression', `payload['${event.newValue}']`)
      }
    } else {
      // Outbound: 내부(소스) -> 외부(타겟). 도메인 필드를 선택하면 소스 표현식으로 자동 지정
      event.node.setDataValue('sourceExpression', `payload['${event.newValue}']`)
    }
  }
  syncUiMappingsFromGrid()
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

onMounted(() => {
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
