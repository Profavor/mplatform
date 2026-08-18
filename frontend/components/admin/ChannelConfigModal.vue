<template>
  <AppModal
    :model-value="modelValue"
    :title="isEdit ? (t('integration.channels.edit', '연동 채널 수정')) : (t('integration.channels.add', '신규 연동 채널 등록'))"
    icon="hub"
    size="large"
    hide-default-actions
    @update:model-value="val => emit('update:modelValue', val)"
  >
    <div style="padding: 0.5rem 0.25rem;">
      <!-- Navigation Tabs -->
      <va-tabs v-model="activeModalTab" class="mb-4" style="border-bottom: 1px solid var(--va-background-border);">
        <template #tabs>
          <va-tab name="basic" style="font-weight: 700;">
            <va-icon name="tune" class="mr-2" /> {{ t('integration.channels.basic_config', '기본 설정') }}
          </va-tab>
          <va-tab name="mapping" style="font-weight: 700;">
            <va-icon name="swap_horiz" class="mr-2" /> {{ t('integration.channels.field_mapping', '필드 매핑') }}
          </va-tab>
        </template>
      </va-tabs>

      <va-form ref="form" @submit.prevent="onSubmit">
        <!-- TAB 1: Basic Config -->
        <div v-show="activeModalTab === 'basic'" style="display: flex; flex-direction: column; gap: 1.25rem;">
          <!-- Multilingual Channel Name -->
          <MultilingualInput
            :ko="channelNameKo"
            :en="channelNameEn"
            @update:ko="val => emit('update:channelNameKo', val)"
            @update:en="val => emit('update:channelNameEn', val)"
            :label="t('integration.channels.name', '채널명')"
            required
          />

          <!-- Direction & Type & Active -->
          <div style="display: grid; grid-template-columns: 1fr 1fr 130px; gap: 1rem; align-items: flex-end;">
            <va-select
              v-model="formData.direction"
              :options="directionOptions"
              value-by="value"
              text-by="text"
              :label="t('integration.channels.direction', '연동 방향')"
              @update:model-value="emit('direction-changed')"
              required
            />
            <va-select
              v-model="formData.type"
              :options="typeOptions"
              value-by="value"
              text-by="text"
              :label="t('integration.channels.type', '연동 방식')"
              :disabled="formData.direction === 'INBOUND'"
              required
            />
            <div style="padding-bottom: 0.5rem;">
              <va-checkbox v-model="formData.isActive" :label="t('integration.channels.is_active', '활성화')" />
            </div>
          </div>

          <!-- INBOUND Auth & Webhook Info -->
          <template v-if="formData.direction === 'INBOUND'">
            <div style="background: var(--va-background-element); border-radius: 12px; padding: 1.25rem; border: 1px solid var(--va-background-border); display: flex; flex-direction: column; gap: 1rem;">
              <div style="font-weight: 700; font-size: 0.95rem; color: var(--va-primary); display: flex; align-items: center; gap: 0.4rem;">
                <va-icon name="security" size="small" color="primary" />
                {{ t('integration.channels.auth_type', '인바운드 인증 방식') }}
              </div>
              <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                <va-select
                  v-model="uiConfig.inboundAuthType"
                  :options="authTypeOptions"
                  value-by="value"
                  text-by="text"
                  :label="t('integration.channels.auth_type', '인증 유형')"
                />
                <div v-if="uiConfig.inboundAuthType !== 'NONE'" style="display: flex; gap: 0.5rem; align-items: flex-end;">
                  <va-input v-model="uiConfig.inboundSecretToken" :label="t('integration.channels.secret_token', '비밀 토큰')" placeholder="sec_token_..." style="flex: 1;" required />
                  <va-button preset="secondary" color="primary" icon="autorenew" @click="emit('generate-token')" style="white-space: nowrap;">
                    {{ t('integration.channels.generate_token', '토큰 생성') }}
                  </va-button>
                </div>
              </div>
              <div>
                <va-checkbox v-model="formData.requiresApproval" :label="`${t('integration.channels.requires_approval', '결재 승인 후 반영')}`" />
              </div>
            </div>

            <!-- Webhook Guide Card (Dynamic Theme Compatible) -->
            <div style="background: var(--va-background-element); border-radius: 12px; padding: 1.25rem; border: 1px solid var(--va-background-border); display: flex; flex-direction: column; gap: 0.75rem;">
              <div style="font-weight: 700; font-size: 0.9rem; color: var(--va-primary); display: flex; align-items: center; justify-content: space-between;">
                <span style="display: flex; align-items: center; gap: 0.4rem;">
                  <va-icon name="link" size="small" color="primary" /> {{ t('integration.channels.webhook_url', '웹훅 URL') }}
                </span>
                <va-button size="small" color="primary" icon="content_copy" @click="emit('copy-webhook-url')">
                  {{ t('integration.channels.webhook_copy', 'URL 복사') }}
                </va-button>
              </div>
              <div style="font-size: 0.83rem; color: var(--va-text-secondary); line-height: 1.4;">
                {{ t('integration.channels.inbound_notice', '외부 시스템에서 위 웹훅 엔드포인트로 JSON 데이터를 POST 전송할 수 있습니다.') }}
              </div>
              <va-input :model-value="webhookUrl" readonly style="font-family: monospace; font-size: 0.85rem;" />
              <div v-if="uiConfig.inboundAuthType !== 'NONE'" style="font-size: 0.8rem; background: var(--va-background-primary); padding: 0.6rem 0.85rem; border-radius: 8px; border: 1px solid var(--va-background-border); color: var(--va-text-primary); display: flex; justify-content: space-between; align-items: center; gap: 0.5rem; flex-wrap: wrap;">
                <div style="display: flex; align-items: center; gap: 0.6rem;">
                  <va-chip size="small" color="primary" outline style="font-weight: 700;">
                    Header
                  </va-chip>
                  <div style="display: flex; align-items: center; gap: 0.4rem; font-family: monospace;">
                    <span style="font-weight: 700; color: var(--va-text-primary);">
                      {{ uiConfig.inboundAuthType === 'BEARER_TOKEN' ? 'Authorization' : 'X-API-KEY' }}:
                    </span>
                    <code style="color: var(--va-primary); font-weight: bold; background: var(--va-background-element); border: 1px solid var(--va-background-border); padding: 2px 6px; border-radius: 4px;">
                      {{ uiConfig.inboundAuthType === 'BEARER_TOKEN' ? `Bearer ${uiConfig.inboundSecretToken || 'secretToken'}` : (uiConfig.inboundSecretToken || 'secretToken') }}
                    </code>
                  </div>
                </div>
                <va-button size="small" preset="secondary" color="primary" icon="content_copy" @click="emit('copy-auth-header')">
                  {{ t('integration.channels.copy_value', '값 복사') }}
                </va-button>
              </div>

              <!-- Real-time JSON Payload Sample Box -->
              <div style="font-size: 0.8rem; background: var(--va-background-primary); padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); color: var(--va-text-primary); display: flex; flex-direction: column; gap: 0.6rem;">
                <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.5rem;">
                  <strong style="display: flex; align-items: center; gap: 0.35rem; color: var(--va-text-primary);">
                    <va-icon name="code" size="small" color="primary" />
                    {{ t('integration.channels.sample_payload_title', 'JSON 페이로드 샘플') }}
                  </strong>
                  <div style="display: flex; gap: 0.4rem;">
                    <va-button size="small" preset="secondary" color="primary" icon="terminal" @click="emit('copy-curl')">
                      {{ t('integration.channels.copy_curl', 'cURL 복사') }}
                    </va-button>
                    <va-button size="small" preset="secondary" color="primary" icon="content_copy" @click="emit('copy-json-payload')">
                      {{ t('integration.channels.copy_json', 'JSON 복사') }}
                    </va-button>
                  </div>
                </div>
                <pre style="margin: 0; font-family: 'Fira Code', 'Consolas', 'Courier New', monospace; font-size: 0.82rem; background: #0f172a; color: #38bdf8; padding: 0.75rem 1rem; border-radius: 6px; overflow-x: auto; max-height: 220px; line-height: 1.45; border: 1px solid #1e293b;">{{ sampleJsonPayload }}</pre>
                <div style="font-size: 0.75rem; color: var(--va-text-secondary); display: flex; align-items: center; gap: 0.3rem;">
                  <va-icon name="info" size="extra-small" color="secondary" />
                  <span>{{ t('integration.channels.sample_payload_notice', '설정된 필드 매핑 구조에 따라 실시간으로 갱신되는 샘플 페이로드입니다.') }}</span>
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
                  {{ t('integration.channels.detail_config', '아웃바운드 연결 상세 설정') }}
                </div>
                <va-button size="small" preset="secondary" color="info" icon="cloud_done" @click="emit('test-connection')" :loading="isTesting">
                  {{ t('integration.channels.test_connection', '연결 테스트') }}
                </va-button>
              </div>

              <template v-if="formData.type === 'WEB_SERVICE'">
                <va-input v-model="uiConfig.wsUrl" :label="t('integration.channels.ws_url', '웹서비스 URL')" placeholder="http://api.example.com/webhook" required class="w-full" />
                <va-select 
                  v-model="uiConfig.wsMethod" 
                  :options="methodOptions" 
                  value-by="value"
                  text-by="text"
                  :label="t('integration.channels.ws_method', 'HTTP 메소드')" 
                  class="w-full" 
                />
                <div style="margin-top: 0.5rem;">
                  <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                    <span style="font-size: 0.85rem; font-weight: 700;">HTTP Headers</span>
                    <va-button size="small" preset="secondary" icon="add" @click="emit('add-ws-header')">{{ t('integration.channels.add_header', '+ 헤더 추가') }}</va-button>
                  </div>
                  <div v-if="uiConfig.wsHeaders.length === 0" style="font-size: 0.8rem; color: #888;">{{ t('integration.channels.no_headers', '등록된 헤더가 없습니다.') }}</div>
                  <div v-for="(header, index) in uiConfig.wsHeaders" :key="index" style="display: flex; gap: 0.5rem; margin-bottom: 0.5rem; align-items: center;">
                    <va-input v-model="header.key" placeholder="Header Name" style="flex: 1;" required />
                    <va-input v-model="header.value" placeholder="Header Value" style="flex: 1;" required />
                    <va-button preset="plain" color="danger" icon="remove_circle" @click="emit('remove-ws-header', index)" />
                  </div>
                </div>
              </template>
              <template v-else-if="formData.type === 'JDBC'">
                <va-input v-model="uiConfig.jdbcUrl" :label="t('integration.channels.db_url', '데이터베이스 JDBC URL')" placeholder="jdbc:mysql://localhost:3306/db" required class="w-full" />
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                  <va-input v-model="uiConfig.jdbcUser" :label="t('integration.channels.db_user', 'DB 계정')" />
                  <va-input v-model="uiConfig.jdbcPassword" type="password" :label="t('integration.channels.db_password', 'DB 비밀번호')" />
                </div>
                <va-input v-model="uiConfig.jdbcTable" :label="t('integration.channels.db_table', '대상 테이블명')" placeholder="integration_data" required class="w-full" />
              </template>
              <template v-else-if="formData.type === 'MESSAGE_QUEUE'">
                <va-input v-model="uiConfig.mqBroker" :label="t('integration.channels.mq_broker', 'MQ 브로커 주소')" placeholder="kafka://localhost:9092" required class="w-full" />
                <va-input v-model="uiConfig.mqTopic" :label="t('integration.channels.mq_topic', '토픽명 (Topic)')" placeholder="events.data.changed" required class="w-full" />
              </template>
            </div>
          </template>

          <!-- Domain & Node Target Selection -->
          <div style="background: var(--va-background-element); border-radius: 12px; padding: 1.25rem; border: 1px solid var(--va-background-border); display: flex; flex-direction: column; gap: 1rem;">
            <div style="font-weight: 700; font-size: 0.95rem; color: var(--va-primary); display: flex; align-items: center; gap: 0.4rem;">
              <va-icon name="account_tree" size="small" color="primary" />
              {{ t('integration.channels.select_domain_node', '연동 대상 마스터 도메인 및 분류 노드') }}
            </div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
              <va-select
                :model-value="selectedDomainId"
                :options="domains"
                value-by="id"
                text-by="name"
                :label="t('integration.channels.select_domain', '대상 도메인') + (formData.direction === 'INBOUND' ? ' *' : '')"
                :rules="[v => formData.direction !== 'INBOUND' || !!v || t('integration.channels.domain_required_for_inbound', '인바운드 채널은 대상 도메인이 필수입니다.')]"
                @update:model-value="val => emit('update:selectedDomainId', val)"
                clearable
              />
              <va-select
                v-model="formData.nodeId"
                :options="nodes"
                value-by="id"
                text-by="name"
                :label="t('integration.channels.select_node', '분류 노드') + (formData.direction === 'INBOUND' ? ' *' : '')"
                :rules="[v => formData.direction !== 'INBOUND' || !!v || t('integration.channels.node_required_for_inbound', '인바운드 채널은 분류 노드가 필수입니다.')]"
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
                {{ t('integration.channels.field_mapping', '필드 매핑') }}
              </h4>
              <p style="margin: 0.25rem 0 0 0; font-size: 0.82rem; color: var(--va-text-secondary);">
                * {{ formData.direction === 'INBOUND' ? t('integration.channels.mapping_desc_inbound', '외부 시스템의 JSON 필드를 내부 마스터 엔티티 필드로 매핑합니다.') : t('integration.channels.mapping_desc', '내부 마스터 필드를 외부 연계 필드로 변환합니다.') }}
              </p>
            </div>
            <div style="display: flex; gap: 0.5rem; align-items: center;">
              <va-button preset="secondary" size="small" icon="auto_awesome" :disabled="!selectedDomainId && rawFields.length === 0" @click="emit('auto-generate-mappings')">
                ⚡ {{ t('integration.channels.auto_map_fields', '자동 매핑') }}
              </va-button>
              <va-button size="small" color="primary" icon="add" @click="emit('add-mapping')">
                + {{ t('integration.channels.add_field', '필드 추가') }}
              </va-button>
            </div>
          </div>

          <va-input 
            v-if="formData.direction === 'INBOUND'" 
            :model-value="uiMappingRootPath" 
            :label="t('integration.channels.mapping_root_path', '매핑 루트 경로 (JSON Path)')" 
            :placeholder="t('integration.channels.mapping_root_path_placeholder', '예: data 또는 payload (루트인 경우 비워둠)')" 
            clearable 
            @update:model-value="val => emit('update:uiMappingRootPath', val)"
          />

          <div :class="{ 'ag-theme-quartz-dark': isDark }" style="height: 360px; width: 100%; border-radius: 8px; overflow: hidden; border: 1px solid var(--va-background-border);">
            <client-only>
              <AgGridVue
                v-if="modelValue"
                style="width: 100%; height: 100%;"
                :theme="gridTheme"
                :columnDefs="mappingColumnDefs"
                :rowData="uiMappings"
                @grid-ready="params => emit('mapping-grid-ready', params)"
                @cell-value-changed="params => emit('mapping-cell-changed', params)"
              />
            </client-only>
          </div>
        </div>
      </va-form>
    </div>

    <template #footer>
      <div style="display: flex; justify-content: flex-end; gap: 0.75rem; width: 100%; padding-top: 1rem; border-top: 1px solid var(--va-background-border);">
        <va-button preset="secondary" color="secondary" @click="emit('update:modelValue', false)">
          {{ t('close', '닫기') }}
        </va-button>
        <va-button color="primary" icon="save" @click="onSubmit">
          {{ t('save', '저장') }}
        </va-button>
      </div>
    </template>
  </AppModal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import MultilingualInput from '~/components/MultilingualInput.vue'
import { AgGridVue } from 'ag-grid-vue3'
import AppModal from '~/components/common/AppModal.vue'
import { useAgGridTheme } from '~/composables/useAgGridTheme'

const { t } = useI18n()
const { gridTheme, isDark } = useAgGridTheme()

const props = withDefaults(defineProps<{
  modelValue: boolean
  isEdit?: boolean
  formData?: any
  uiConfig?: any
  channelNameKo?: string
  channelNameEn?: string
  directionOptions?: any[]
  typeOptions?: any[]
  authTypeOptions?: any[]
  methodOptions?: any[]
  domains?: any[]
  nodes?: any[]
  selectedDomainId?: string | null
  rawFields?: any[]
  uiMappingRootPath?: string
  uiMappings?: any[]
  mappingColumnDefs?: any[]
  isTesting?: boolean
  webhookUrl?: string
  sampleJsonPayload?: string
}>(), {
  isEdit: false,
  formData: () => ({}),
  uiConfig: () => ({}),
  channelNameKo: '',
  channelNameEn: '',
  directionOptions: () => [],
  typeOptions: () => [],
  authTypeOptions: () => [],
  methodOptions: () => [],
  domains: () => [],
  nodes: () => [],
  selectedDomainId: null,
  rawFields: () => [],
  uiMappingRootPath: '',
  uiMappings: () => [],
  mappingColumnDefs: () => [],
  isTesting: false,
  webhookUrl: '',
  sampleJsonPayload: ''
})

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'update:channelNameKo', val: string): void
  (e: 'update:channelNameEn', val: string): void
  (e: 'update:selectedDomainId', val: string | null): void
  (e: 'update:uiMappingRootPath', val: string): void
  (e: 'direction-changed'): void
  (e: 'generate-token'): void
  (e: 'copy-webhook-url'): void
  (e: 'copy-auth-header'): void
  (e: 'copy-curl'): void
  (e: 'copy-json-payload'): void
  (e: 'test-connection'): void
  (e: 'add-ws-header'): void
  (e: 'remove-ws-header', index: number): void
  (e: 'auto-generate-mappings'): void
  (e: 'add-mapping'): void
  (e: 'mapping-grid-ready', params: any): void
  (e: 'mapping-cell-changed', params: any): void
  (e: 'submit'): void
}>()

const activeModalTab = ref('basic')

const onSubmit = () => {
  emit('submit')
}

defineExpose({
  activeModalTab,
  onSubmit
})
</script>
