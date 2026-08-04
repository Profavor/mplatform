<template>
  <div style="display: flex; flex-direction: column; gap: 1rem; height: 100%; min-height: 0;">
    <!-- Top Action Bar -->
    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); padding: 0.85rem 1.25rem; border-radius: 12px; border: 1px solid var(--va-background-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04); flex: 0 0 auto;">
      <div style="display: flex; align-items: center; gap: 0.75rem;">
        <va-icon name="dataset" size="large" color="primary" />
        <div>
          <h2 style="font-weight: 700; font-size: 1.35rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
            {{ pageTitle }}
            <va-badge text="Master Data" color="primary" size="small" />
          </h2>
          <span style="font-size: 0.85rem; color: var(--va-text-secondary);">
            {{ $t('records_management_desc') || '도메인별 마스터 데이터 레코드를 조회, 신규 생성, 일괄 변경 및 서바이버십 병합을 진행합니다.' }}
          </span>
        </div>
      </div>
    </div>

    <div class="records-container records-layout" style="flex: 1; min-height: 0;">
    <!-- Left Column: Classification Tree -->
    <div class="left-tree records-tree-column">
      <h3 style="padding: 0.5rem; margin: 0; border-bottom: 1px solid #ddd; font-size: 1rem; font-weight: bold; color: #555; text-transform: uppercase;">
        Classification Tree
      </h3>
      <div style="flex: 1; overflow-y: auto;">
        <va-card flat>
          <va-card-content style="padding: 0;">
            <ClassificationTree
              ref="treeRef"
              :selectedNode="selectedNode"
              :showEdit="false"
              @select="selectNode"
              @loaded="onTreeLoaded"
            />
          </va-card-content>
        </va-card>
      </div>
    </div>

    <!-- Right Column: Record List & Data Grid -->
    <div class="right-content records-detail-column">
      <!-- Grid Title & Action Bar -->
      <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.5rem 0.85rem; margin-bottom: 0; background: var(--va-background-element, #f4f6f9); border: 1px solid var(--va-background-border); border-bottom: none; border-top-left-radius: 8px; border-top-right-radius: 8px;">
        <div style="display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap;">
          <va-icon name="table_chart" color="primary" size="1.1rem" />
          <span style="font-weight: 700; font-size: 0.95rem; color: var(--va-text-primary);">
            {{ selectedNode ? getTranslatedName(selectedNode.name) : '마스터 데이터 레코드 목록' }}
          </span>
          <va-chip v-if="selectedNode" size="small" color="primary" style="font-weight: 600;">
            {{ selectedNode.isDomain ? 'Domain' : 'Node' }}
          </va-chip>
          <va-button v-if="searchableFields.length > 0" preset="secondary" size="small" icon="filter_list" @click="showAdvancedSearch = !showAdvancedSearch">
            상세 검색
          </va-button>
          <va-chip
            v-for="(val, key) in activeFilters"
            :key="key"
            v-show="val"
            color="primary"
            outline
            style="display: inline-flex; align-items: center; font-weight: 500;"
          >
            <span>{{ getFilterFieldLabel(key) }}: {{ formatFilterValue(key, val) }}</span>
            <va-icon
              name="close"
              size="14px"
              style="margin-left: 6px; cursor: pointer; opacity: 0.85;"
              title="필터 삭제"
              @click.stop="removeFilter(key)"
            />
          </va-chip>
          <va-button
            v-if="Object.keys(activeFilters).some(k => activeFilters[k])"
            preset="secondary"
            size="small"
            color="danger"
            icon="clear_all"
            @click="clearFilters"
          >
            전체 초기화
          </va-button>
        </div>

        <div style="display: flex; align-items: center; gap: 0.4rem; flex-wrap: wrap;">
          <template v-if="selectedNode && !selectedNode.isDomain">
            <va-button v-if="hasPermission('record:write') || hasPermission('workflow:request')" size="small" color="primary" @click="openCreateModal">
              <va-icon name="add" class="mr-1"/> {{ hasCreateWorkflow ? '신규 등록 요청' : 'Create Record' }}
            </va-button>
            <va-button v-if="hasPermission('record:write') || hasPermission('workflow:request')" size="small" color="success" outline @click="showExcelUploader = true">
              <va-icon name="upload" class="mr-1"/> Bulk Upload
            </va-button>
          </template>
          <va-button size="small" color="info" outline :disabled="!selectedRecordId" @click="showLineageModal = true">
            <va-icon name="account_tree" class="mr-1"/> {{ $t('data_lineage') || '데이터 계보' }}
          </va-button>
          <va-button size="small" color="warning" outline :disabled="(selectedRecordRows?.length || 0) < 2" @click="showCompareModal = true">
            <va-icon name="scale" class="mr-1"/> {{ $t('compare_records') || '레코드 비교' }} ({{ selectedRecordRows?.length || 0 }})
          </va-button>
          <va-button size="small" color="warning" outline @click="showAsyncExportModal = true">
            <va-icon name="cloud_download" class="mr-1"/> {{ $t('async_export') || '대용량 Export' }}
          </va-button>
          <va-button preset="plain" color="secondary" size="small" icon="refresh" @click="refreshRecords">
            {{ $t('refresh') || '새로고침' }}
          </va-button>
        </div>
      </div>

      <!-- Advanced Search Panel -->
      <va-card v-if="showAdvancedSearch" class="mb-4" style="background-color: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px;">
        <va-card-content style="padding: 1.25rem;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; padding-bottom: 0.5rem; border-bottom: 1px dashed var(--va-background-border);">
            <div style="display: flex; align-items: center; gap: 0.5rem;">
              <va-icon name="tune" color="primary" size="18px" />
              <span style="font-weight: 700; font-size: 0.9rem; color: var(--va-primary);">상세 검색 조건 (Advanced Search)</span>
            </div>
            <va-badge v-if="Object.keys(activeFilters).filter(k => activeFilters[k]).length > 0" :text="`적용 필터 ${Object.keys(activeFilters).filter(k => activeFilters[k]).length}개`" color="primary" />
          </div>

          <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 1.25rem; align-items: start;">
            <div v-for="field in searchableFields" :key="field.id" style="display: flex; flex-direction: column; gap: 0.4rem;">
              <span style="font-size: 0.75rem; color: var(--va-text-secondary); font-weight: 600; text-transform: uppercase;">{{ getTranslatedName(field.name) }}</span>
              
              <va-select
                v-if="['SELECT', 'MULTI_SELECT'].includes(field.type)"
                v-model="draftFilters[field.key]"
                :options="parseOptions(field.options)"
                value-by="value"
                :placeholder="$t('op.select_option')"
                clearable
                class="w-full"
              />
              <va-select
                v-else-if="field.type === 'BOOLEAN'"
                v-model="draftFilters[field.key]"
                :options="['true', 'false']"
                :placeholder="$t('op.select_option')"
                clearable
                class="w-full"
              />
              <div v-else-if="['NUMBER', 'DECIMAL', 'FLOAT', 'INTEGER'].includes(field.type)" style="display: flex; flex-direction: column; gap: 0.4rem; width: 100%;">
                <va-input
                  v-model="draftFilters[field.key]"
                  type="number"
                  :placeholder="$t('op.enter_number')"
                  clearable
                  class="w-full"
                  @keydown="onFilterKeydown"
                >
                  <template #prependInner>
                    <select 
                      :value="draftFiltersOp[field.key] || 'EQ'" 
                      @change="draftFiltersOp[field.key] = $event.target.value"
                      @click.stop
                      @mousedown.stop
                      style="border: none; outline: none; background: transparent; font-weight: bold; color: var(--va-primary); cursor: pointer; padding-right: 0.2rem; margin-right: 0.5rem; border-right: 1px solid var(--va-background-border); font-size: 0.85rem;"
                    >
                      <option value="EQ">=</option>
                      <option value="GT">&gt;</option>
                      <option value="GTE">&gt;=</option>
                      <option value="LT">&lt;</option>
                      <option value="LTE">&lt;=</option>
                      <option value="BETWEEN">{{ $t('op.range') }}</option>
                    </select>
                  </template>
                </va-input>
                <va-input
                  v-if="draftFiltersOp[field.key] === 'BETWEEN'"
                  v-model="draftFiltersMax[field.key]"
                  type="number"
                  :placeholder="$t('op.max_value')"
                  clearable
                  class="w-full"
                  @keydown="onFilterKeydown"
                >
                  <template #prependInner>
                    <span style="font-weight: bold; color: #666; margin-right: 0.5rem; border-right: 1px solid #ccc; padding-right: 0.5rem; font-size: 0.8rem;">~ {{ $t('op.below') }}</span>
                  </template>
                </va-input>
              </div>
              <va-input
                v-else
                v-model="draftFilters[field.key]"
                :placeholder="$t('op.enter_keyword')"
                clearable
                class="w-full"
                @keydown="onFilterKeydown"
              >
                <template #prependInner>
                  <select 
                    :value="draftFiltersOp[field.key] || 'EQ'" 
                    @change="draftFiltersOp[field.key] = $event.target.value"
                    @click.stop
                    @mousedown.stop
                    style="border: none; outline: none; background: transparent; font-weight: bold; color: var(--va-primary); cursor: pointer; padding-right: 0.2rem; margin-right: 0.5rem; border-right: 1px solid var(--va-background-border); font-size: 0.85rem;"
                  >
                    <option value="EQ">{{ $t('op.eq') }}</option>
                    <option value="CONTAINS">{{ $t('op.contains') }}</option>
                    <option value="STARTS_WITH">{{ $t('op.starts_with') }}</option>
                    <option value="ENDS_WITH">{{ $t('op.ends_with') }}</option>
                  </select>
                </template>
              </va-input>
            </div>
          </div>
          <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1.25rem; padding-top: 0.75rem; border-top: 1px dashed var(--va-background-border);">
            <va-button preset="secondary" icon="restart_alt" @click="clearFilters">초기화</va-button>
            <va-button color="primary" icon="search" @click="applyFilters">검색</va-button>
          </div>
        </va-card-content>
      </va-card>
      
      <div style="flex: 1; display: flex; flex-direction: column; min-height: 0;">
        <va-card v-if="selectedNode" style="width: 100%; flex: 1; display: flex; flex-direction: column; min-height: 0;">
          <va-card-content style="padding: 0; flex: 1; display: flex; flex-direction: column; min-height: 0;">
            <div class="records-grid-wrapper" :class="{ 'ag-theme-quartz-dark': isDark }">
              <ag-grid-vue
                style="width: 100%; height: 100%;"
                :theme="gridTheme"
                :autoSizeStrategy="autoSizeStrategy"
                :columnDefs="columnDefs"
                :defaultColDef="defaultColDef"
                rowModelType="infinite"
                :cacheBlockSize="20"
                :rowSelection="{ mode: 'multiRow', enableClickSelection: false, headerCheckbox: false }"
                :pagination="true"
                :paginationPageSize="20"
                :paginationPageSizeSelector="[10, 20, 50]"
                @grid-ready="onGridReady"
                @selection-changed="onSelectionChanged"
                @row-double-clicked="onRowDoubleClicked"
                @cell-double-clicked="onCellDoubleClicked"
                @rowDoubleClicked="onRowDoubleClicked"
                @cellDoubleClicked="onCellDoubleClicked"
              />

            </div>
          </va-card-content>
        </va-card>
        
        <va-card v-else>
          <va-card-content style="text-align: center; padding: 3rem; color: #666;">
            Select a Classification Node from the tree to view or manage records.
          </va-card-content>
        </va-card>
      </div>
    </div>

    <ExcelUploader
      v-if="showExcelUploader"
      :nodeId="selectedNode?.id"
      :nodeFields="nodeFields"
      :domainReferences="domainReferences"
      @close="showExcelUploader = false"
      @uploaded="handleExcelUploaded"
    />

    <!-- Modularized Create Record Modal -->
    <RecordFormModal
      :show="showCreateModal"
      :is-edit="false"
      :record="recordFormData"
      :fields="nodeFields"
      :node-label="selectedNode?.label"
      :has-workflow="hasCreateWorkflow"
      :workflow-permission="createWorkflowPermission"
      :available-workflows="availableWorkflows"
      :selected-domain-info="selectedDomainInfo"
      :domain-references="domainReferences"
      @close="showCreateModal = false"
      @save="promptDraftComment('CREATE', $event)"
      @openDomainRef="openDomainRefModal($event.fieldKey, $event.isCreate)"
      @selectWorkflow="handleWorkflowSelectionChanged"
    />

    <!-- Modularized Record Detail & History Drawer -->
    <RecordDetailDrawer
      :show="showDetailModal"
      :record="selectedRecordData"
      :fields="nodeFields"
      :history="historyLogs"
      :node-label="selectedNode?.label"
      :is-snapshot-mode="isSnapshotMode"
      :has-pending-update="hasPendingUpdate"
      :is-editing-record="isEditingRecord"
      :has-update-workflow="hasUpdateWorkflow"
      :can-delete="hasPermission('record:write') || hasPermission('workflow:request')"
      :can-write="hasPermission('record:write') || hasPermission('workflow:request')"
      :can-read-history="hasPermission('record:read') || hasPermission('record:*')"
      :selected-domain-info="selectedDomainInfo"
      :domain-references="domainReferences"
      :user-list="userList"
      @close="showDetailModal = false"
      @delete="requestDeleteRecord"
      @unmerge="handleUnmergeRecord"
      @openHistory="openHistory"
      @save="promptDraftComment('UPDATE', $event)"
      @openDomainRef="openDomainRefModal($event.fieldKey, $event.isCreate)"
      @viewDiffDetails="viewDiffDetails"
      @viewSnapshot="viewSnapshot"
      @viewApprovalHistory="viewApprovalHistory"
      @viewIntegrationHistory="viewIntegrationHistory"
      @secondaryNodesUpdated="fetchRecords"
    />

    <!-- Dedicated Snapshot Modal -->
    <RecordDetailDrawer
      :show="showSnapshotModal"
      :record="snapshotRecordData"
      :fields="nodeFields"
      :history="[]"
      :node-label="selectedNode?.label"
      :is-snapshot-mode="true"
      :has-pending-update="false"
      :is-editing-record="false"
      :has-update-workflow="false"
      :can-delete="false"
      :can-write="false"
      :can-read-history="false"
      :selected-domain-info="selectedDomainInfo"
      :domain-references="domainReferences"
      :user-list="userList"
      @close="showSnapshotModal = false"
    />

    <!-- Dedicated Record Compare Modal -->
    <RecordCompareModal
      :show="showCompareModal"
      :records="selectedRecordRows"
      :fields="nodeFields"
      @close="showCompareModal = false"
    />



    <!-- DQ Validation Modal -->
    <RecordsDqValidationModal
      v-model="showDqValidationModal"
      :dq-validating="dqValidating"
      :dq-validation-result="dqValidationResult"
      :current-locale="currentLocale"
      :node-fields="nodeFields"
      @fix-data="fixDataAndReturn"
      @proceed="proceedToDraftComment"
    />

    <!-- Required Fields Warning Modal -->
    <RecordsRequiredWarningModal
      v-model="showRequiredWarningModal"
      :current-locale="currentLocale"
      :missing-required-fields="missingRequiredFields"
      @confirm="focusFirstMissingField"
    />

    <!-- System Notification Modal -->
    <ErrorAlertModal
      v-model="showErrorAlertModal"
      :error-alert-type="errorAlertType"
      :error-alert-title="errorAlertTitle"
      :error-alert-header="errorAlertHeader"
      :error-alert-message="errorAlertMessage"
      :current-locale="currentLocale"
    />

    <!-- Draft Comment Modal (공통 상신 의견 작성 모달) -->
    <SubmissionCommentModal
      v-model="showDraftCommentModal"
      v-model:comment="draftCommentText"
      @submit="executePendingSave"
    />

    <!-- Diff Modal -->
    <RecordsDiffModal
      v-model="showDiffModal"
      :selected-diffs="selectedDiffs"
    />

    <!-- Approval / Integration History Modal -->
    <va-modal v-model="showApprovalHistoryModal" :title="selectedApprovalRequest?.isIntegration ? ($t('integration.channels.integration_detail_title') || $t('integration_detail_title') || '연계 내역 상세') : ($t('integration.channels.approval_detail_title') || $t('approval_detail_title') || '결재 내역 상세')" hide-default-actions size="large" :prevent-click-outside="true" :no-outside-dismiss="true">
      <div style="max-height: 60vh; overflow-y: auto; padding: 1rem; box-sizing: border-box; width: 100%;">
        <div v-if="!selectedApprovalRequest" style="text-align: center; color: #777;">
          데이터를 불러오는 중입니다...
        </div>
        <div v-else>
          <ApprovalDetailsViewer :request="selectedApprovalRequest" />
        </div>
      </div>
      <div style="display: flex; justify-content: flex-end; margin-top: 1rem;">
        <va-button @click="showApprovalHistoryModal = false">Close</va-button>
      </div>
    </va-modal>

    <RecordsDomainRefModal
      v-model="showDomainRefModal"
      :is-dark="isDark"
      :grid-theme="gridTheme"
      :auto-size-strategy="autoSizeStrategy"
      :domain-ref-col-defs="domainRefColDefs"
      :domain-ref-row-data="domainRefRowData"
      @row-double-clicked="onDomainRefRowDoubleClicked"
    />
    <!-- Record Lineage Modal -->
    <RecordLineageModal
      v-model="showLineageModal"
      :recordId="selectedRecordId"
      :fields="nodeFields"
    />

    <!-- Async Export Modal -->
    <AsyncBatchExportModal
      v-model="showAsyncExportModal"
      :domainId="selectedDomainId"
      :gridApi="gridApi"
    />
  </div>
</div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { usePageTitle } from '~/composables/usePageTitle'

const { pageTitle } = usePageTitle('records_management', '마스터 데이터 레코드 관리')
import { useCookie } from '#app'
import { AgGridVue } from 'ag-grid-vue3'
import ExcelUploader from '~/components/ExcelUploader.vue'
import RecordFormModal from '~/components/records/RecordFormModal.vue'
import RecordDetailDrawer from '~/components/records/RecordDetailDrawer.vue'
import RecordCompareModal from '~/components/records/RecordCompareModal.vue'
import RecordLineageModal from '~/components/RecordLineageModal.vue'
import AsyncBatchExportModal from '~/components/AsyncBatchExportModal.vue'

import { useColors, useModal, useToast } from 'vuestic-ui'
import { useI18n } from 'vue-i18n'
import { usePermission } from '~/composables/usePermission'

const { t } = useI18n()
const { confirm } = useModal()
const { init: initToast } = useToast()
const { gridTheme, autoSizeStrategy } = useAgGridTheme()
const { hasPermission } = usePermission()
const { downloadFileWithAuth } = useFileDownloader()

const { currentPresetName } = useColors()
const isDark = computed(() => currentPresetName.value === 'dark')

const currentLocale = useCookie('locale', { default: () => 'ko' })
const token = useCookie('auth_token', { default: () => '' })

const showCompareModal = ref(false)
const showLineageModal = ref(false)
const showAsyncExportModal = ref(false)
const selectedRecordRows = ref([])

const selectedDomainId = computed(() => {
  return selectedNode.value?.domainId || selectedNode.value?.id || null
})

const onSelectionChanged = (event) => {
  if (!event || !event.api) return
  const rows = event.api.getSelectedRows() || []
  selectedRecordRows.value = rows
  if (rows.length > 0) {
    selectedRecordId.value = rows[0].id || rows[0].recordId || null
  }
}


const userCookie = useCookie('user_data')
const currentUser = computed(() => {
  if (userCookie.value) {
    return typeof userCookie.value === 'string' ? JSON.parse(userCookie.value) : userCookie.value
  }
  return null
})

const parseJwtUserId = (tStr) => {
  if (!tStr) return null
  try {
    const base64Url = tStr.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''))
    const parsed = JSON.parse(jsonPayload)
    return parsed.userId || parsed.uuid || parsed.username || parsed.sub || null
  } catch {
    return null
  }
}

const myUuid = computed(() => {
  const u = currentUser.value
  let uid = u?.id || u?.userId || u?.uuid || u?.username
  if (!uid) {
     uid = parseJwtUserId(token.value) || ''
  }
  return uid
})

const userList = ref([])

const getUserName = (uuid, nameFallback) => {
  return nameFallback || uuid || ''
}

const treeNodes = ref([])
const selectedNode = ref(null)
const hasCreateWorkflow = ref(true)
const hasUpdateWorkflow = ref(true)

const selectedDomainInfo = ref(null)

const nodeFields = ref([])
const rowData = ref([])
const domainReferences = ref({}) 
const showExcelUploader = ref(false)

const loadDomainReferences = async (fields) => {
  domainReferences.value = {}
  for (const f of fields) {
    if (f.type === 'DOMAIN_REFERENCE') {
      try {
        const opts = JSON.parse(f.options || '{}')
        const tDomainId = opts.targetDomainId
        if (!tDomainId) continue
        
        const domains = await $fetch('/api/domains', { headers: { Authorization: `Bearer ${token.value}` } })
        const tDomain = domains.find(d => d.id === tDomainId)
        
        const tFields = await $fetch(`/api/domains/${tDomainId}/fields`, { headers: { Authorization: `Bearer ${token.value}` } })
        const tRecords = await $fetch(`/api/records/domain/${tDomainId}`, { headers: { Authorization: `Bearer ${token.value}` } })
        
        domainReferences.value[f.key] = {
          targetDomainId: tDomainId,
          domainInfo: tDomain,
          fields: tFields,
          records: tRecords
        }
      } catch (e) {
        console.error('Failed to load domain reference info for field:', f.key, e)
      }
    }
  }
}

const showDomainRefModal = ref(false)
const domainRefColDefs = ref([])
const domainRefRowData = ref([])
const currentDomainRefFieldKey = ref(null)
const isDomainRefForCreate = ref(false)

const openDomainRefModal = (fieldKey, isCreate = false) => {
  const refInfo = domainReferences.value[fieldKey]
  if (!refInfo) {
    showCustomAlert(t('target_domain_ref_not_loaded') || 'Target domain reference info not loaded.', t('notice') || 'Notice', t('notification') || 'Notification', 'warning')
    return
  }
  currentDomainRefFieldKey.value = fieldKey
  isDomainRefForCreate.value = isCreate
  const tDomain = refInfo.domainInfo
  
  const idField = refInfo.fields.find(f => f.id === tDomain.identifierFieldId)
  const nameField = refInfo.fields.find(f => f.id === tDomain.displayNameFieldId)
  const descField = refInfo.fields.find(f => f.id === tDomain.descriptionFieldId)
  
  const createColDef = (field) => {
    const def = {
      headerName: getTranslatedName(field.name),
      field: `data.${field.key}`,
    }
    if (field.tableColumnWidth) {
      def.width = field.tableColumnWidth
    } else {
      def.flex = 1
    }
    
    def.valueFormatter = (params) => {
      if (!params.value) return ''
      if (field.type === 'MULTILINGUAL') {
        try {
          const obj = typeof params.value === 'string' ? JSON.parse(params.value) : params.value;
          return obj[currentLocale.value] || obj.ko || obj.en || JSON.stringify(params.value);
        } catch(e) { return String(params.value); }
      }
      if (typeof params.value === 'object') return JSON.stringify(params.value);
      return String(params.value);
    }
    return def;
  }

  const cols = []
  if (idField) cols.push(createColDef(idField))
  if (nameField) cols.push(createColDef(nameField))
  if (descField) cols.push(createColDef(descField))
  
  if (cols.length === 0) cols.push({ headerName: 'System ID', field: 'id', flex: 1 })
  
  domainRefColDefs.value = cols
  domainRefRowData.value = refInfo.records.map(r => ({
    id: r.id,
    data: typeof r.data === 'string' ? JSON.parse(r.data) : r.data
  }))
  showDomainRefModal.value = true
}

const onDomainRefRowDoubleClicked = (params) => {
  if (currentDomainRefFieldKey.value) {
    if (isDomainRefForCreate.value) {
      recordFormData.value[currentDomainRefFieldKey.value] = params.data.id
    } else {
      selectedRecordData.value[currentDomainRefFieldKey.value] = params.data.id
    }
  }
  showDomainRefModal.value = false
}

const domainRefDisplayMap = ref({})

const extractMultilingualField = (dataObj, fieldKey) => {
  if (!dataObj || !fieldKey) return null;
  const rawVal = dataObj[fieldKey];
  if (rawVal === null || rawVal === undefined || rawVal === '') return null;
  if (typeof rawVal === 'string') {
    try {
      const parsed = JSON.parse(rawVal);
      if (parsed && typeof parsed === 'object') {
        return parsed[currentLocale.value] || parsed.ko || parsed.en || rawVal;
      }
    } catch(e) {}
    return rawVal;
  } else if (typeof rawVal === 'object') {
    return rawVal[currentLocale.value] || rawVal.ko || rawVal.en || JSON.stringify(rawVal);
  }
  return String(rawVal);
}

const buildDomainRefDisplayString = (dataObj, tDomain, tFields) => {
  if (!dataObj || !tDomain || !tFields) return null;
  
  const idFieldId = tDomain.identifierFieldId;
  let dFieldId = tDomain.displayNameFieldId || tDomain.identifierFieldId;
  
  let idF = tFields.find(x => x.id === idFieldId);
  let nameF = tFields.find(x => x.id === dFieldId);
  if (!nameF) {
    nameF = tFields.find(x => {
      const n = JSON.stringify(x.name).toLowerCase();
      return n.includes('name') || n.includes('\uC774\uB984') || n.includes('\uC0AC\uC6D0\uBA85') || n.includes('title') || n.includes('\uC81C\uBAA9');
    });
    if (!nameF) nameF = tFields.find(x => x.type === 'TEXT');
  }
  
  const idStr = extractMultilingualField(dataObj, idF?.key);
  const nameStr = extractMultilingualField(dataObj, nameF?.key);
  
  if (idStr && nameStr && idStr !== nameStr) {
    return `[${idStr}] ${nameStr}`;
  } else if (nameStr) {
    return nameStr;
  } else if (idStr) {
    return `[${idStr}]`;
  }
  return null;
}

const fetchDomainRefName = async (uuid, targetDomainId) => {
  if (!uuid || domainRefDisplayMap.value[uuid]) return;
  domainRefDisplayMap.value[uuid] = 'Loading...';
  try {
    const rec = await $fetch(`/api/records/${uuid}`, { headers: { Authorization: `Bearer ${token.value}` } }).catch(() => null);
    if (!rec) {
      const uname = getUserName(uuid);
      domainRefDisplayMap.value[uuid] = (uname && uname !== uuid) ? uname : uuid;
      return;
    }
    
    let tDomainId = targetDomainId;
    if (!tDomainId && rec.node) {
      tDomainId = rec.node.domain?.id || rec.node.domainId;
    }
    if (!tDomainId) tDomainId = rec.domainId;
    
    if (!tDomainId) {
      domainRefDisplayMap.value[uuid] = uuid;
      return;
    }

    const domains = await $fetch('/api/domains', { headers: { Authorization: `Bearer ${token.value}` } });
    const tDomain = domains.find(d => d.id === tDomainId);
    if (!tDomain) {
      domainRefDisplayMap.value[uuid] = uuid;
      return;
    }
    
    const tFields = await $fetch(`/api/domains/${tDomainId}/fields`, { headers: { Authorization: `Bearer ${token.value}` } });
    
    if (rec.data) {
      const dataObj = typeof rec.data === 'string' ? JSON.parse(rec.data) : rec.data;
      const displayStr = buildDomainRefDisplayString(dataObj, tDomain, tFields);
      domainRefDisplayMap.value[uuid] = displayStr || uuid;
    } else {
      domainRefDisplayMap.value[uuid] = uuid;
    }
  } catch (e) {
    const uname = getUserName(uuid);
    domainRefDisplayMap.value[uuid] = (uname && uname !== uuid) ? uname : uuid;
  }
}

const getDomainRefDisplayName = (fieldKey, recordId) => {
  if (!recordId) return ''
  const refInfo = domainReferences.value[fieldKey]
  if (!refInfo) return recordId
  
  const record = refInfo.records?.find(r => r.id === recordId)
  if (record) {
    const data = typeof record.data === 'string' ? JSON.parse(record.data) : record.data;
    const displayStr = buildDomainRefDisplayString(data, refInfo.domainInfo, refInfo.fields);
    if (displayStr) return displayStr;
    return recordId;
  }
  
  if (!domainRefDisplayMap.value[recordId]) {
    fetchDomainRefName(recordId, refInfo.targetDomainId);
  }
  return domainRefDisplayMap.value[recordId] || 'Loading...';
}

const gridApi = ref(null)

const showCreateModal = ref(false)
const recordFormData = ref({})

const parseName = (nameObj) => {
  if (!nameObj) return null;
  if (typeof nameObj === 'string') {
    try { return JSON.parse(nameObj); } catch (e) { return null; }
  }
  return nameObj;
}

const getTranslatedName = (nameObj) => {
  const pName = parseName(nameObj)
  return pName?.[currentLocale.value] || pName?.ko || pName?.en || ''
}

const parseOptions = (opts) => {
  if (!opts) return []
  if (typeof opts === 'string') {
    if (opts.trim().startsWith('[')) {
      try { 
        const parsed = JSON.parse(opts) 
        const mapped = parsed.map(o => {
          if (typeof o === 'string') return { text: o, value: o, order: 0 }
          return {
            value: o.key,
            text: o.label?.[currentLocale.value] || o.label?.ko || o.label?.en || o.key,
            order: o.order || 0
          }
        })
        return mapped.sort((a, b) => a.order - b.order)
      } catch(e){}
    }
    return opts.split(',').map(s => {
      const val = s.trim();
      return { text: val, value: val };
    })
  }
  return opts
}

const route = useRoute()
const initialRouteHandled = ref(false)

function findNodeInTree(nodeId, nodesList = treeNodes.value) {
  if (!nodesList || !Array.isArray(nodesList)) return null
  for (const n of nodesList) {
    if (n.id === nodeId || n.domainId === nodeId) return n
    if (n.children && n.children.length > 0) {
      const found = findNodeInTree(nodeId, n.children)
      if (found) return found
    }
  }
  return null
}

const extractFilename = (input) => {
  if (!input) return '';
  if (typeof input === 'object') {
    if (input.name && input.name !== 'Download') return input.name;
    if (input.originalName) return input.originalName;
    if (input.url) input = input.url;
    else return '';
  }
  let str = String(input).trim();
  if (!str || str === '-' || str === '[]' || str === '{}' || str === 'null' || str === 'undefined') return '';
  
  try {
    if (str.startsWith('{') || str.startsWith('[')) {
      const parsed = JSON.parse(str);
      if (Array.isArray(parsed) && parsed.length > 0) return extractFilename(parsed[0]);
      if (typeof parsed === 'object' && (parsed.name || parsed.originalName)) return parsed.name || parsed.originalName;
    }
  } catch (e) {}

  try {
    if (str.includes('?name=')) return decodeURIComponent(str.split('?name=')[1].split('&')[0]);
    if (str.includes('?filename=')) return decodeURIComponent(str.split('?filename=')[1].split('&')[0]);
    const fname = decodeURIComponent(str.split('/').pop().split('?')[0]);
    if (fname && fname !== '-' && fname !== 'null') return fname;
  } catch (e) {}
  
  return str;
};

const processRecordDataWithFields = (rawDataObj, fields) => {
  let rawData = {}
  if (typeof rawDataObj === 'string') {
    if (rawDataObj.trim()) {
      try {
        rawData = JSON.parse(rawDataObj)
      } catch (e) {
        rawData = {}
      }
    }
  } else if (rawDataObj && typeof rawDataObj === 'object') {
    rawData = rawDataObj
  }
  const data = { ...rawData }
  const fieldsToProcess = fields || []
  
  const rawDataUpperMap = new Map()
  Object.keys(rawData).forEach(k => {
    if (k) rawDataUpperMap.set(k.trim().toUpperCase(), rawData[k])
  })

  fieldsToProcess.forEach(f => {
    if (!f || !f.key) return
    const fKeyUpper = f.key.trim().toUpperCase()
    
    let rawVal = data[f.key] !== undefined 
      ? data[f.key] 
      : (rawDataUpperMap.has(fKeyUpper) ? rawDataUpperMap.get(fKeyUpper) : undefined)

    if (rawVal === undefined) {
      for (const [uKey, val] of rawDataUpperMap.entries()) {
        if (uKey === fKeyUpper || uKey.endsWith('_' + fKeyUpper) || fKeyUpper.endsWith('_' + uKey) || uKey.includes(fKeyUpper) || fKeyUpper.includes(uKey)) {
          rawVal = val
          break
        }
      }
    }
    
    if (rawVal === undefined) {
      const keyLower = f.key.toLowerCase();
      if (keyLower.includes('en') || keyLower.includes('eng')) {
        const baseKey = f.key.replace(/_?en(g(lish)?)?$/i, '').replace(/^en(g(lish)?)?_?/i, '');
        if (baseKey && data[baseKey]) {
          const parentVal = data[baseKey];
          if (parentVal && typeof parentVal === 'object' && parentVal.en) {
            rawVal = parentVal.en;
          } else if (typeof parentVal === 'string') {
            try {
              const parsed = JSON.parse(parentVal);
              if (parsed && parsed.en) rawVal = parsed.en;
            } catch(e){}
          }
        }
      }
    }
    
    if (f.type === 'MULTILINGUAL') {
      if (rawVal === null || rawVal === undefined) {
        data[f.key] = { ko: '', en: '' }
      } else if (typeof rawVal === 'string') {
        try {
          const parsed = JSON.parse(rawVal)
          if (parsed && typeof parsed === 'object') {
            data[f.key] = { ko: parsed.ko || '', en: parsed.en || '' }
          } else {
            data[f.key] = { ko: rawVal, en: '' }
          }
        } catch (e) {
          data[f.key] = { ko: rawVal, en: '' }
        }
      } else if (typeof rawVal === 'object' && rawVal !== null) {
        data[f.key] = { ko: rawVal.ko || '', en: rawVal.en || '' }
      } else {
        data[f.key] = { ko: String(rawVal), en: '' }
      }
    } else if (f.type === 'FILE') {
      if (rawVal) {
        try {
          const arr = typeof rawVal === 'string' ? JSON.parse(rawVal) : rawVal
          if (Array.isArray(arr)) {
            data[f.key] = arr.map(fileObj => typeof fileObj === 'string' ? { name: extractFilename(fileObj), url: fileObj } : fileObj)
          } else if (typeof rawVal === 'string') {
            data[f.key] = [{ name: extractFilename(rawVal), url: rawVal }]
          }
        } catch(e) {
          if (typeof rawVal === 'string') {
            data[f.key] = [{ name: extractFilename(rawVal), url: rawVal }]
          }
        }
      } else {
        data[f.key] = []
      }
    } else {
      if (rawVal !== null && rawVal !== undefined) {
        if (typeof rawVal === 'object') {
          data[f.key] = rawVal.en || rawVal.ko || JSON.stringify(rawVal)
        } else {
          data[f.key] = rawVal
        }
      }
    }

  })

  return data
}

const openRecordDetailModal = async (record) => {
  if (!record) return
  selectedRecordId.value = record.id

  const targetNodeId = record.node?.id || record.domainId || selectedNode.value?.id
  if (targetNodeId) {
    try {
      const isDomain = (selectedNode.value?.isDomain && selectedNode.value?.id === targetNodeId) || 
                       (record.node && !record.node.parent) || 
                       (!record.node && record.domainId)
      const fieldsUrl = isDomain ? `/api/domains/${targetNodeId}/fields` : `/api/nodes/${targetNodeId}/fields/effective`
      const fetched = await $fetch(fieldsUrl, { headers: { Authorization: `Bearer ${token.value}` } }).catch(() => null)
      if (fetched && Array.isArray(fetched) && fetched.length > 0) {
        nodeFields.value = fetched
      }
    } catch (e) {
      console.error('Failed to load node fields for detail modal:', e)
    }
  }

  const data = processRecordDataWithFields(record.data, nodeFields.value)

  // 메타 필드(id, domainId, status 등)를 필드 데이터와 함께 병합
  selectedRecordData.value = {
    ...data,
    id: record.id,
    domainId: record.domainId,
    status: record.status,
    code: record.code,
    node: record.node,
    sourceSystem: record.sourceSystem,
    createdAt: record.createdAt,
    updatedAt: record.updatedAt
  }
  originalRecordData.value = JSON.parse(JSON.stringify(selectedRecordData.value))
  isEditingRecord.value = false
  isSnapshotMode.value = false
  hasPendingUpdate.value = record.status === 'PENDING_APPROVAL' || false
  
  openHistory()
  showDetailModal.value = true
}

const handleUnmergeRecord = async (record) => {
  const targetId = record?.id || selectedRecordId.value
  if (!targetId) return

  const isConfirmed = await confirm({
    title: t('merge.unmerge_confirm_title') || '병합 취소 (Unmerge)',
    message: t('merge.unmerge_confirm_msg') || '이 레코드의 병합 상태를 되돌리시겠습니까? 병합 이전 상태로 데이터가 복원됩니다.',
    okText: t('confirm') || '확인',
    cancelText: t('cancel') || '취소'
  })
  if (!isConfirmed) return

  try {
    await $fetch(`/api/records/${targetId}/unmerge`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    init({ message: t('merge.unmerge_success') || '레코드가 성공적으로 언머지(복구) 되었습니다.', color: 'success' })
    showDetailModal.value = false
    refreshRecords()
  } catch (e) {
    init({ message: t('merge.unmerge_fail') || '언머지 처리 중 오류가 발생했습니다.', color: 'danger' })
  }
}


const handleInitialRouteParams = async () => {
  if (initialRouteHandled.value) return
  const queryRecordId = route.query.recordId
  const queryDomainId = route.query.domainId
  const queryNodeId = route.query.nodeId

  if (queryRecordId) {
    initialRouteHandled.value = true
    try {
      const rec = await $fetch(`/api/records/${queryRecordId}`, {
        headers: { Authorization: `Bearer ${token.value}` }
      }).catch(() => null)

      if (rec) {
        let targetNode = null
        if (rec.node) {
          targetNode = findNodeInTree(rec.node.id) || {
            id: rec.node.id,
            label: typeof rec.node.name === 'object' ? (rec.node.name[currentLocale.value] || rec.node.name.ko || rec.node.name.en) : rec.node.name,
            isDomain: false,
            domainId: rec.node.domainId || (rec.node.domain ? rec.node.domain.id : null)
          }
        } else if (rec.domainId) {
          targetNode = findNodeInTree(rec.domainId) || {
            id: rec.domainId,
            label: 'Domain',
            isDomain: true
          }
        }

        if (targetNode) {
          await selectNode(targetNode)
        }
        openRecordDetailModal(rec)
        return
      }
    } catch (e) {
      console.error('Failed to load record by query params:', e)
    }
  }

  if (queryDomainId || queryNodeId) {
    initialRouteHandled.value = true
    const targetId = queryDomainId || queryNodeId
    const foundNode = findNodeInTree(targetId)
    if (foundNode) {
      await selectNode(foundNode)
      return
    }
  }

  if (!selectedNode.value && treeNodes.value && treeNodes.value.length > 0) {
    initialRouteHandled.value = true
    await selectNode(treeNodes.value[0])
  }
}

const treeRef = ref(null)

const onTreeLoaded = async (nodes) => {
  treeNodes.value = nodes
  await handleInitialRouteParams()
}

const loadTree = async () => {
  if (treeRef.value) {
    await treeRef.value.loadTree()
  }
}

watch(currentLocale, () => {
  if (gridApi.value && columnDefs.value) {
    gridApi.value.setGridOption('columnDefs', buildColumnDefs(nodeFields.value))
  }
})

const refreshRecords = () => {
  if (gridApi.value) {
    gridApi.value.refreshInfiniteCache()
    gridApi.value.purgeInfiniteCache()
  }
}

onMounted(async () => {
  await loadTree()
  await handleInitialRouteParams()
  if (process.client) {
    window.addEventListener('approval-updated', refreshRecords)
  }
})

onUnmounted(() => {
  if (process.client) {
    window.removeEventListener('approval-updated', refreshRecords)
  }
})

const selectNode = async (node) => {
  selectedNode.value = node || null
  selectedDomainInfo.value = null
  
  if (!node) {
    nodeFields.value = []
    rowData.value = []
    columnDefs.value = []
    return
  }

  let targetDomainId = null
  if (node.isDomain) {
    targetDomainId = node.id
  } else if (node.domainId) {
    targetDomainId = node.domainId
  } else if (node.originalData && node.originalData.domainId) {
    targetDomainId = node.originalData.domainId
  }

  if (targetDomainId) {
    try {
      const dom = await $fetch(`/api/domains/${targetDomainId}`, {
        headers: { Authorization: `Bearer ${token.value}` }
      })
      selectedDomainInfo.value = dom
    } catch (e) {
      console.error('Failed to load selected domain details:', e)
    }
  }
  
  if (node.isDomain) {
    try {
      const fields = await $fetch(`/api/domains/${node.id}/fields`, {
        headers: { Authorization: `Bearer ${token.value}` }
      }).catch(() => [])
      nodeFields.value = fields
      await loadDomainReferences(fields)
      columnDefs.value = buildColumnDefs(fields, true)
      await fetchRecords()
    } catch (e) {
      console.error(e)
    }
    return
  }
  
  try {
    const fields = await $fetch(`/api/nodes/${node.id}/fields/effective`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    nodeFields.value = fields
    await loadDomainReferences(fields)
    
    const isParentNode = node.children && node.children.length > 0;
    columnDefs.value = buildColumnDefs(fields, isParentNode)
    
    try {
      const [createRes, updateRes] = await Promise.all([
        $fetch(`/api/approval-requests/effective-workflow/${node.id}?actionType=CREATE`, { headers: { Authorization: `Bearer ${token.value}` } }).catch(() => true),
        $fetch(`/api/approval-requests/effective-workflow/${node.id}?actionType=UPDATE`, { headers: { Authorization: `Bearer ${token.value}` } }).catch(() => true)
      ])
      hasCreateWorkflow.value = createRes === true
      hasUpdateWorkflow.value = updateRes === true
    } catch(e) {
      hasCreateWorkflow.value = true
      hasUpdateWorkflow.value = true
    }
    
    await fetchRecords()
  } catch (e) {
    console.error(e)
  }
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
  const formatted = date.toLocaleString(undefined, { timeZone: tz })
  return formatted.replace(/\s*(GMT|UTC|KST|PST|EST|CET)[-+0-9:]*/gi, '').trim()
}

const buildColumnDefs = (fields, showNodeColumn = false) => {
  const defs = [
    { field: 'id', headerName: 'ID', sortable: true, width: 100 },


    { 
      field: 'nodeName', 
      headerName: 'Classification Node', 
      sortable: true, 
      width: 180,
      hide: !showNodeColumn 
    },
    { 
      field: 'status', 
      colId: 'sys_record_status',
      headerName: 'Status', 
      sortable: true, 
      width: 150,
      cellRenderer: (params) => {
        if (!params || params.value === undefined || params.value === null || params.value === '') {
          return '';
        }
        const color = params.value === 'ACTIVE' ? '#2c82e0' : (params.value === 'PENDING_APPROVAL' ? '#e6a23c' : '#f56c6c')
        const span = document.createElement('span');
        span.style.padding = '2px 8px';
        span.style.borderRadius = '4px';
        span.style.background = color;
        span.style.color = 'white';
        span.style.fontSize = '12px';
        span.style.fontWeight = 'bold';
        span.innerText = params.value;
        return span;
      }
    }
  ]
  
  const groupMap = {}

  fields.forEach(f => {
    const colDef = {
      headerName: getTranslatedName(f.name),
      field: `data.${f.key}`,
      colId: f.key,
      valueGetter: (params) => {
        if (!params.data || !params.data.data) return null;
        const d = params.data.data;
        if (d[f.key] !== undefined && d[f.key] !== null && d[f.key] !== '') return d[f.key];
        const lowerKey = String(f.key).toLowerCase();
        if (d[lowerKey] !== undefined && d[lowerKey] !== null && d[lowerKey] !== '') return d[lowerKey];
        const upperKey = String(f.key).toUpperCase();
        if (d[upperKey] !== undefined && d[upperKey] !== null && d[upperKey] !== '') return d[upperKey];
        
        // English field fallback (e.g. f.key is name_en, englishName, etc.)
        if (lowerKey.includes('en') || lowerKey.includes('eng')) {
          const baseKey = f.key.replace(/_?en(g(lish)?)?$/i, '').replace(/^en(g(lish)?)?_?/i, '');
          if (baseKey && d[baseKey]) {
            const parentVal = d[baseKey];
            if (parentVal && typeof parentVal === 'object' && parentVal.en) {
              return parentVal.en;
            } else if (typeof parentVal === 'string') {
              try {
                const parsed = JSON.parse(parentVal);
                if (parsed && parsed.en) return parsed.en;
              } catch(e){}
            }
          }
        }
        return null;
      },
      sortable: true
    }
    if (f.tableColumnWidth) {
      colDef.width = f.tableColumnWidth
    } else {
      colDef.flex = 1
    }
    if (f.type === 'FILE') {
      colDef.cellRenderer = (params) => {
        if (!params || !params.value) return '-';
        let val = params.value;
        if (typeof val === 'string') {
          val = val.trim();
          if (val === '' || val === '-' || val === '[]' || val === '{}' || val === 'null' || val === 'undefined') return '-';
        }
        
        let fileList = [];
        if (Array.isArray(val)) {
          fileList = val;
        } else if (typeof val === 'string') {
          try {
            const parsed = JSON.parse(val);
            if (Array.isArray(parsed)) fileList = parsed;
            else if (typeof parsed === 'object') fileList = [parsed];
            else fileList = [val];
          } catch (e) {
            fileList = [val];
          }
        } else if (typeof val === 'object') {
          fileList = [val];
        }

        const validFiles = fileList.filter(item => {
          if (!item) return false;
          const fn = extractFilename(item);
          const url = typeof item === 'object' ? item.url : String(item);
          return fn !== '' && url !== '' && url !== '-';
        });

        if (validFiles.length === 0) return '-';

        const container = document.createElement('div');
        container.style.cssText = 'display: flex; flex-direction: column; justify-content: center; gap: 2px; height: 100%;';

        validFiles.forEach((fileItem) => {
          const fname = extractFilename(fileItem);
          const url = typeof fileItem === 'object' ? (fileItem.url || '#') : String(fileItem);
          const a = document.createElement('a');
          a.href = '#';
          a.style.color = '#2563eb';
          a.style.fontWeight = '500';
          a.style.textDecoration = 'underline';
          a.style.overflow = 'hidden';
          a.style.textOverflow = 'ellipsis';
          a.style.whiteSpace = 'nowrap';
          a.innerText = `📎 ${fname}`;
          a.onclick = (e) => {
            e.preventDefault();
            if (typeof window !== 'undefined' && window.downloadFileWithAuth) {
              window.downloadFileWithAuth(url, fname);
            }
          };
          container.appendChild(a);
        });

        return container;
      }
    } else if (f.type === 'MULTILINGUAL') {
      colDef.cellRenderer = (params) => {
        if (!params.value) return ''
        try {
          const obj = typeof params.value === 'string' ? JSON.parse(params.value) : params.value;
          const keyLower = String(f.key).toLowerCase();
          const nameLower = String(getTranslatedName(f.name)).toLowerCase();
          if (keyLower.includes('en') || nameLower.includes('영문') || nameLower.includes('english')) {
            return obj.en || obj.ko || '';
          }
          return obj[currentLocale.value] || obj.ko || obj.en || JSON.stringify(params.value);
        } catch(e) {
          return String(params.value);
        }
      }
    }
 else if (f.type === 'DOMAIN_REFERENCE') {
      colDef.cellRenderer = (params) => {
        if (!params.value) return ''
        const displayVal = getDomainRefDisplayName(f.key, params.value)
        return displayVal ? displayVal : params.value
      }
    } else if (f.type === 'CALCULATED') {
      const opts = JSON.parse(f.options || '{}')
      if (opts.formula) {
        colDef.valueGetter = (params) => {
          if (!params.data || !params.data.data) return ''
          const rawData = params.data.data
          const dataObj = typeof rawData === 'string' ? JSON.parse(rawData) : rawData
          const result = evaluateFormula(opts.formula, dataObj)
          if (result !== null && !isNaN(result)) {
            let formatted = Number(result).toLocaleString('ko-KR')
            if (f.unit) {
              formatted += ` ${f.unit}`
            }
            return formatted;
          }
          return ''
        }
      }
    }
    if (f.type === 'DATE') {
      colDef.valueFormatter = (params) => {
        if (!params.value) return '';
        const date = parseDate(params.value);
        if (!date || isNaN(date.getTime())) return params.value;
        const tz = useCookie('timezone', { default: () => 'Asia/Seoul' }).value;
        const formatted = date.toLocaleString(undefined, { timeZone: tz });
        return formatted.replace(/\s*(GMT|UTC|KST|PST|EST|CET)[-+0-9:]*/gi, '').trim();
      }
    }
    if (['NUMBER', 'INTEGER', 'DECIMAL'].includes(f.type)) {
      colDef.valueFormatter = (params) => {
        if (params.value === null || params.value === undefined || params.value === '') return '';
        const num = Number(params.value);
        if (isNaN(num)) return params.value;
        let formatted = num.toLocaleString('ko-KR');
        if (f.unit) {
          formatted += ` ${f.unit}`;
        }
        return formatted;
      };
    }
    
    if (f.fieldGroup && f.fieldGroup.name) {
      const gName = getTranslatedName(f.fieldGroup.name)
      if (!groupMap[gName]) {
        groupMap[gName] = {
          headerName: gName,
          _sortOrder: f.fieldGroup.sortOrder || 0,
          openByDefault: f.fieldGroup.isDefaultOpen !== false,
          children: []
        }
      }
      
      if (groupMap[gName].children.length > 0) {
        colDef.columnGroupShow = 'open';
      }
      
      groupMap[gName].children.push(colDef)
    } else {
      defs.push(colDef)
    }
  })
  
  Object.values(groupMap)
    .sort((a, b) => a._sortOrder - b._sortOrder)
    .forEach(g => {
      delete g._sortOrder
      defs.push(g)
    })
  
  defs.push({
    field: 'updatedAt',
    headerName: t('updatedAt') || '변경일',
    sortable: true,
    width: 210,
    valueGetter: (params) => params.data?.updatedAt || params.data?.createdAt,
    valueFormatter: (params) => formatDate(params.value)
  })
  return defs
}

const columnDefs = ref([])
  
const activeFilters = ref({})
const activeFiltersOp = ref({})
const activeFiltersMax = ref({})

const draftFilters = ref({})
const draftFiltersOp = ref({})
const draftFiltersMax = ref({})

const showAdvancedSearch = ref(false)
const searchableFields = computed(() => nodeFields.value.filter(f => f.isSearchable && !f.isRemoved))

const onFilterKeydown = (e) => {
  if (e && e.key === 'Enter') {
    applyFilters()
  }
}

const applyFilters = () => {
  activeFilters.value = { ...draftFilters.value }
  activeFiltersOp.value = { ...draftFiltersOp.value }
  activeFiltersMax.value = { ...draftFiltersMax.value }
  fetchRecords()
}

const clearFilters = () => {
  draftFilters.value = {}
  draftFiltersOp.value = {}
  draftFiltersMax.value = {}
  activeFilters.value = {}
  activeFiltersOp.value = {}
  activeFiltersMax.value = {}
  fetchRecords()
}

const removeFilter = (key) => {
  const nextDraft = { ...draftFilters.value }
  delete nextDraft[key]
  draftFilters.value = nextDraft

  const nextDraftOp = { ...draftFiltersOp.value }
  delete nextDraftOp[key]
  draftFiltersOp.value = nextDraftOp

  const nextDraftMax = { ...draftFiltersMax.value }
  delete nextDraftMax[key]
  draftFiltersMax.value = nextDraftMax

  const nextActive = { ...activeFilters.value }
  delete nextActive[key]
  activeFilters.value = nextActive

  const nextActiveOp = { ...activeFiltersOp.value }
  delete nextActiveOp[key]
  activeFiltersOp.value = nextActiveOp

  const nextActiveMax = { ...activeFiltersMax.value }
  delete nextActiveMax[key]
  activeFiltersMax.value = nextActiveMax

  fetchRecords()
}

const getFilterFieldLabel = (key) => {
  const f = searchableFields.value.find(f => f.key === key)
  return f ? getTranslatedName(f.name) : key
}

const formatFilterValue = (key, val) => {
  const op = activeFiltersOp.value[key] || 'EQ'
  const maxVal = activeFiltersMax.value[key]
  if (op === 'BETWEEN') return `${val} ~ ${maxVal || ''}`
  if (op === 'GT') return `> ${val}`
  if (op === 'LT') return `< ${val}`
  if (op === 'GTE') return `>= ${val}`
  if (op === 'LTE') return `<= ${val}`
  if (op === 'CONTAINS') return `[${t('op.contains')}] ${val}`
  if (op === 'STARTS_WITH') return `[${t('op.starts_with')}] ${val}`
  if (op === 'ENDS_WITH') return `[${t('op.ends_with')}] ${val}`
  if (op === 'EQ') return `= ${val}`
  return val
}

const createDatasource = () => {
  return {
    getRows: async (params) => {
      if (!selectedNode.value) {
        params.successCallback([], 0);
        return;
      }
      
      const size = params.endRow - params.startRow;
      const page = Math.floor(params.startRow / size);
      
      try {
        const endpoint = selectedNode.value.isDomain 
          ? `/api/records/domain/${selectedNode.value.id}` 
          : `/api/nodes/${selectedNode.value.id}/records?includeChildren=true`
        
        const searchParams = new URLSearchParams()
        if (endpoint.includes('?')) {
          const parts = endpoint.split('?')
          const qs = new URLSearchParams(parts[1])
          qs.forEach((v, k) => searchParams.append(k, v))
        }
        Object.entries(activeFilters.value).forEach(([k, v]) => {
          if (v !== null && v !== '') {
            searchParams.append('search_' + k, v)
            const op = activeFiltersOp.value[k] || 'EQ'
            searchParams.append('search_op_' + k, op)
            if (op === 'BETWEEN' && activeFiltersMax.value[k]) {
              searchParams.append('search_' + k + '_max', activeFiltersMax.value[k])
            }
          }
        })
        
        if (params.sortModel && params.sortModel.length > 0) {
          const sort = params.sortModel[0]
          let colId = sort.colId || ''
          if (colId.startsWith('data.')) {
            colId = colId.substring(5)
          }
          searchParams.append('sortField', colId)
          searchParams.append('sortOrder', sort.sort.toUpperCase())
        }
        
        searchParams.append('page', page);
        searchParams.append('size', size);
        
        const finalEndpoint = endpoint.split('?')[0] + '?' + searchParams.toString();
          
        const pageData = await $fetch(finalEndpoint, {
          headers: { Authorization: `Bearer ${token.value}` }
        });
        
        const rows = pageData.content.map(r => {
          const parsedData = processRecordDataWithFields(r.data, nodeFields.value)
          
          const nodeNameMap = r.node?.name || {}
          const nodeName = parseName(nodeNameMap)?.[currentLocale.value] || parseName(nodeNameMap)?.ko || parseName(nodeNameMap)?.en || r.node?.id || 'Unknown'
          
          return { ...r, data: parsedData, nodeName }
        });

        
        params.successCallback(rows, pageData.totalElements);
        
      } catch (e) {
        console.error('Failed to load records:', e);
        params.failCallback();
      }
    }
  };
};

const fetchRecords = async () => {
  if (gridApi.value) {
    gridApi.value.setGridOption('datasource', createDatasource());
  }
}

const onGridReady = (params) => {
  gridApi.value = params.api
  params.api.addEventListener('cellDoubleClicked', onCellDoubleClicked)
  params.api.addEventListener('rowDoubleClicked', onRowDoubleClicked)
  fetchRecords()
}

const showDetailModal = ref(false)
const showRequiredWarningModal = ref(false)
const missingRequiredFields = ref([])
const firstMissingFieldKey = ref(null)

const showErrorAlertModal = ref(false)
const errorAlertTitle = ref('')
const errorAlertHeader = ref('')
const errorAlertMessage = ref('')
const errorAlertType = ref('error')

const showCustomAlert = (msg, header = '', title = '', type = 'error') => {
  errorAlertMessage.value = msg
  errorAlertHeader.value = header
  errorAlertTitle.value = title
  errorAlertType.value = type
  showErrorAlertModal.value = true
}

const { showLoading, hideLoading } = useLoading()
const historyLogs = ref([])
const showDiffModal = ref(false)
const selectedDiffs = ref([])

const showApprovalHistoryModal = ref(false)
const selectedApprovalRequest = ref(null)
const selectedReflectionTime = ref(null)

const isMultiple = (field) => {
  if (!field.options) return false;
  try {
    const opts = JSON.parse(field.options);
    return opts.multiple === true;
  } catch (e) {
    return false;
  }
}

const viewApprovalHistory = async (row) => {
  if (!row.approvalRequestId) return
  selectedApprovalRequest.value = null
  selectedReflectionTime.value = row.changedAt
  showApprovalHistoryModal.value = true
  try {
    const res = await $fetch(`/api/approval-requests/${row.approvalRequestId}`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    selectedApprovalRequest.value = res
  } catch (e) {
    console.error('Failed to load approval details', e)
    showCustomAlert(t('failed_load_approval_details') || 'Failed to load approval details', t('error') || 'Error', t('notification') || 'Notification', 'error')
    showApprovalHistoryModal.value = false
  }
}

const viewIntegrationHistory = async (row) => {
  selectedApprovalRequest.value = null
  selectedReflectionTime.value = row.changedAt
  showApprovalHistoryModal.value = true
  try {
    const logs = await $fetch(`/api/admin/integration/logs/by-record/${row.recordId}`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    const log = logs && logs.length > 0 ? logs[0] : null
    selectedApprovalRequest.value = {
      isIntegration: true,
      sourceSystem: row.sourceSystem || (log ? 'INBOUND' : 'SYSTEM'),
      createdAt: row.changedAt,
      changes: row.newData,
      targetType: 'RECORD_CREATE',
      targetId: row.recordId,
      nodeId: selectedNode.value?.id,
      integrationLog: log
    }
  } catch (e) {
    console.error('Failed to load integration log details', e)
    selectedApprovalRequest.value = {
      isIntegration: true,
      sourceSystem: row.sourceSystem || 'INBOUND',
      createdAt: row.changedAt,
      changes: row.newData,
      targetType: 'RECORD_CREATE',
      targetId: row.recordId,
      nodeId: selectedNode.value?.id,
      integrationLog: null
    }
  }
}

const selectedRecordData = ref({})
const originalRecordData = ref({})
const selectedRecordId = ref(null)
const isEditingRecord = ref(false)
const hasPendingUpdate = ref(false)
const isSnapshotMode = ref(false)

const showSnapshotModal = ref(false)
const snapshotRecordData = ref({})

const viewSnapshot = (dataString) => {
  if (!dataString) return
  try {
    const data = processRecordDataWithFields(dataString, nodeFields.value)
    snapshotRecordData.value = data
    showSnapshotModal.value = true
  } catch(e) {
    console.error('Failed to view snapshot:', e)
  }
}


const getParsedDiffs = (prev, next) => {
  let p = {};
  let n = {};
  
  if (typeof prev === 'string') {
    try {
      const parsed = JSON.parse(prev);
      if (parsed && typeof parsed === 'object' && ('before' in parsed || 'after' in parsed)) {
        p = parsed.before || {};
        n = parsed.after || {};
      } else if (next === 'RECORD_UPDATE') {
        p = parsed.before || {};
        n = parsed.after || {};
      } else {
        n = parsed || {};
        // p는 현재 레코드 데이터에서 복사
        p = { ...(selectedRecordData.value || {}) };
      }
    } catch (e) {
      p = { ...(selectedRecordData.value || {}) };
      n = {};
    }
  } else if (prev && typeof prev === 'object') {
    if ('before' in prev || 'after' in prev) {
      p = prev.before || {};
      n = prev.after || {};
    } else {
      n = prev;
      p = { ...(selectedRecordData.value || {}) };
    }
  } else {
    p = selectedRecordData.value || {};
    n = typeof next === 'object' ? (next || {}) : {};
  }

  const diffs = [];
  const keys = [...new Set([...Object.keys(p), ...Object.keys(n)])];

  keys.forEach(k => {
    let valBeforeRaw = p[k];
    let valAfterRaw = n[k];

    // p(before)에 이전 값이 누락된 경우, 레코드 원본 데이터(selectedRecordData)에서 기존 값을 가져옴
    if ((valBeforeRaw === undefined || valBeforeRaw === null) && selectedRecordData.value && selectedRecordData.value[k] !== undefined) {
      valBeforeRaw = selectedRecordData.value[k];
    }

    const field = nodeFields.value?.find(f => f.key === k || getTranslatedName(f.name) === k);
    const fName = field ? getTranslatedName(field.name) : k;
    const fType = field ? field.type : '';

    let valBefore = valBeforeRaw;
    let valAfter = valAfterRaw;

    if (field && field.type === 'DOMAIN_REFERENCE') {
      let tDomainId = null;
      try { tDomainId = JSON.parse(field.options || '{}').targetDomainId; } catch(e){}
      if (valBefore && typeof valBefore === 'string' && valBefore.length === 36) {
        if (!domainRefDisplayMap.value[valBefore]) fetchDomainRefName(valBefore, tDomainId);
        valBefore = domainRefDisplayMap.value[valBefore] || valBefore;
      }
      if (valAfter && typeof valAfter === 'string' && valAfter.length === 36) {
        if (!domainRefDisplayMap.value[valAfter]) fetchDomainRefName(valAfter, tDomainId);
        valAfter = domainRefDisplayMap.value[valAfter] || valAfter;
      }
    }

    const formatValue = (val, type) => {
      if (val === undefined || val === null) return '';
      if (type === 'MULTILINGUAL' || (typeof val === 'object' && (val.ko || val.en))) {
        return formatMultilingual(val);
      }
      if (typeof val === 'object') {
        return Object.entries(val).map(([key, v]) => `${key.toUpperCase()}: ${v}`).join(', ');
      }
      if (typeof val === 'number') return val.toLocaleString('ko-KR');
      return String(val);
    };

    const strBefore = formatValue(valBefore, fType).trim();
    const strAfter = formatValue(valAfter, fType).trim();

    // 값 동등성 검사
    let isSame = false;
    if (strBefore === strAfter) {
      isSame = true;
    } else if (JSON.stringify(valBeforeRaw) === JSON.stringify(valAfterRaw)) {
      isSame = true;
    }

    if (!isSame) {
      diffs.push({
        fieldKey: k,
        fieldName: fName,
        fieldType: fType,
        before: strBefore,
        after: strAfter,
        rawBefore: valBeforeRaw,
        rawAfter: valAfterRaw
      });
    }
  });

  return diffs;
}

const formatDiffDisplay = (val, rawVal, fieldType) => {
  if (val === undefined || val === null || val === '') return { isFile: false, text: '' };
  const str = typeof rawVal === 'string' ? rawVal : (typeof val === 'string' ? val : JSON.stringify(rawVal || val));
  if (fieldType === 'FILE' || str.includes('/api/files/download/') || str.includes('name=')) {
    const fn = extractFilename(rawVal || val);
    let url = '#';
    if (typeof rawVal === 'object' && rawVal !== null) {
      url = rawVal.url || '#';
    } else if (typeof rawVal === 'string') {
      url = rawVal;
    } else if (typeof val === 'string') {
      url = val;
    }
    // JSON 배열 형태인 경우 예외처리
    if (url.startsWith('["') && url.endsWith('"]')) {
      try {
        const arr = JSON.parse(url);
        if (arr.length > 0) url = arr[0];
      } catch (e) {}
    }
    return { isFile: true, fname: fn || '파일 다운로드', url };
  }
  return { isFile: false, text: String(val) };
};

const viewDiffDetails = (prev, next, isPendingCreation = false) => {
  selectedDiffs.value = getParsedDiffs(prev, next);
  showDiffModal.value = true;
}

const openHistory = async () => {
  if (!selectedRecordId.value) return
  showLoading('이력을 불러오는 중입니다...')
  try {
    const res = await $fetch(`/api/records/${selectedRecordId.value}/history`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    historyLogs.value = res || []
    
    try {
      const approvalsRes = await $fetch('/api/approval-requests?status=PENDING&size=100', { headers: { Authorization: `Bearer ${token.value}` } })
      const list = approvalsRes?.content || (Array.isArray(approvalsRes) ? approvalsRes : [])
      const pending = list.find(a => String(a.targetId) === String(selectedRecordId.value) && a.status === 'PENDING')
      if (pending) {
        const fullPending = await $fetch(`/api/approval-requests/${pending.id}`, { headers: { Authorization: `Bearer ${token.value}` } })
        if (!historyLogs.value.some(log => log.id === 'pending-approval-log')) {
          historyLogs.value = [
            {
              id: 'pending-approval-log',
              changedAt: fullPending.createdAt,
              changedBy: fullPending.requesterName || fullPending.requesterId,
              changeType: 'PENDING_APPROVAL',
              previousData: null,
              newData: null,
              approvalRequestId: fullPending.id,
              rawRequest: fullPending
            },
            ...historyLogs.value
          ]
        }
      }
    } catch (e) {
      console.error('Failed to fetch pending approval for history', e)
    }
  } catch (e) {
    console.error('Failed to load history', e)
    showCustomAlert(t('failed_load_history') || 'Failed to load history', t('error') || 'Error', t('notification') || 'Notification', 'error')
  } finally {
    hideLoading()
  }
}

const onCellDoubleClicked = (params) => {
  const record = params?.data || params?.node?.data
  if (record) openRecordDetailModal(record)
}

const onRowDoubleClicked = (params) => {
  const record = params?.data || params?.node?.data
  if (record) openRecordDetailModal(record)
}

const formatDataForSave = (dataObj) => {
  // 메타 필드는 data 페이로드에서 제외
  const META_FIELDS = new Set(['id', 'domainId', 'status', 'code', 'node', 'sourceSystem', 'createdAt', 'updatedAt'])
  const formatted = {}
  Object.keys(dataObj || {}).forEach(k => {
    if (!META_FIELDS.has(k)) formatted[k] = dataObj[k]
  })
  nodeFields.value?.forEach(field => {
    const val = formatted[field.key]
    if (val !== undefined && val !== null && val !== '') {
      if (['NUMBER', 'DECIMAL', 'FLOAT', 'INTEGER'].includes(field.type)) {
        formatted[field.key] = Number(val)
      } else if (field.type === 'BOOLEAN') {
        formatted[field.key] = Boolean(val)
      }
    }
    if (field.type === 'CALCULATED') {
      try {
        const opts = JSON.parse(field.options || '{}')
        if (opts.formula) {
          const result = evaluateFormula(opts.formula, formatted)
          if (result !== null && !isNaN(result)) {
            formatted[field.key] = Number(result)
          }
        }
      } catch (e) {
        console.warn('Failed to compute calculated field', field.key, e)
      }
    }
  })
  return formatted
}

const evaluateConditionExpression = (expr, formData) => {
  if (!expr || !expr.trim() || !formData) return false
  try {
    const replaced = expr.replace(/#{([a-zA-Z0-9_]+)}/g, (_, key) => {
      const val = formData[key]
      if (val === undefined || val === null) return 'null'
      if (typeof val === 'number' || typeof val === 'boolean') return String(val)
      if (typeof val === 'object') return JSON.stringify(JSON.stringify(val))
      return JSON.stringify(String(val))
    })
    const fn = new Function(`return Boolean(${replaced});`)
    return fn()
  } catch (e) {
    return false
  }
}

const evalConditionRule = (field, formData) => {
  const defaultRes = { show: true, highlight: field?.isHighlighted || false, required: field?.required || false, readOnly: field?.isReadOnly || false, disabled: false }
  if (!field || !field.options || !formData) return defaultRes
  
  try {
    const opts = typeof field.options === 'string' ? JSON.parse(field.options) : field.options
    const rule = opts.conditionRule
    if (!rule || rule.enabled === false) return defaultRes

    let actions = ['SHOW']
    if (rule.action) {
      actions = Array.isArray(rule.action) ? rule.action : [rule.action]
    }
    let isMatch = false

    if (rule.expression && String(rule.expression).trim() !== '') {
      isMatch = evaluateConditionExpression(rule.expression, formData)
    } else if (rule.dependsOnFieldKey) {
      const targetVal = String(formData[rule.dependsOnFieldKey] ?? '').trim()
      const condVal = String(rule.value ?? '').trim()
      const op = rule.operator || 'EQUALS'

      if (op === 'EQUALS' || op === '==') isMatch = targetVal.toLowerCase() === condVal.toLowerCase()
      else if (op === 'NOT_EQUALS' || op === '!=') isMatch = targetVal.toLowerCase() !== condVal.toLowerCase()
      else if (op === 'CONTAINS') isMatch = targetVal.toLowerCase().includes(condVal.toLowerCase())
      else if (op === 'NOT_EMPTY') isMatch = targetVal.length > 0
      else if (op === 'EMPTY') isMatch = targetVal.length === 0
      else if (op === 'GREATER_THAN' || op === '>') isMatch = Number(targetVal) > Number(condVal)
      else if (op === 'GREATER_THAN_OR_EQUAL' || op === '>=') isMatch = Number(targetVal) >= Number(condVal)
      else if (op === 'LESS_THAN' || op === '<') isMatch = Number(targetVal) < Number(condVal)
      else if (op === 'LESS_THAN_OR_EQUAL' || op === '<=') isMatch = Number(targetVal) <= Number(condVal)
    } else {
      return defaultRes
    }

    let show = true
    if (actions.includes('SHOW')) show = isMatch
    let highlight = field?.isHighlighted || false
    if (actions.includes('HIGHLIGHT')) highlight = isMatch ? true : highlight
    let required = field?.required || false
    if (actions.includes('REQUIRE')) required = isMatch ? true : required
    let readOnly = field?.isReadOnly || false
    if (actions.includes('READ_ONLY')) readOnly = isMatch ? true : readOnly
    let disabled = false
    if (actions.includes('DISABLE') || actions.includes('EDIT_FORBIDDEN')) disabled = isMatch ? true : disabled

    return { show, highlight, required, readOnly, disabled }
  } catch (e) {}

  return defaultRes
}

const saveEditedRecord = async () => {
  try {
    const reqId = myUuid.value
    const dataToSave = { ...selectedRecordData.value }
    for (const field of nodeFields.value) {
      if (field.type === 'FILE') {
        let files = selectedRecordData.value[field.key]
        if (!files) files = []
        if (!Array.isArray(files)) files = [files]
        
        const uploadedUrls = []
        for (const file of files) {
          if (file instanceof File) {
            const fd = new FormData()
            fd.append('file', file)
            const res = await $fetch('/api/files/upload', {
              method: 'POST',
              headers: { Authorization: `Bearer ${token.value}` },
              body: fd
            })
            uploadedUrls.push(res.url)
          } else if (typeof file === 'string') {
            uploadedUrls.push(file)
          } else if (file && file.url) {
            uploadedUrls.push(file.url)
          }
        }
        if (uploadedUrls.length > 0) {
          dataToSave[field.key] = JSON.stringify(uploadedUrls)
        } else {
          dataToSave[field.key] = "[]"
        }
      } else {
        dataToSave[field.key] = selectedRecordData.value[field.key]
      }
    }
    const payload = { requesterId: reqId, data: JSON.stringify(formatDataForSave(dataToSave)), comment: draftCommentText.value }
    const res = await $fetch(`/api/records/${selectedRecordId.value}/update-request`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` },
      body: payload
    })
    
    // Save Secondary Nodes if any
    if (pendingSecondaryNodes.value.length > 0) {
      await $fetch(`/api/records/${selectedRecordId.value}/secondary-nodes`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token.value}` },
        body: { nodeIds: pendingSecondaryNodes.value }
      }).catch(e => console.error('Failed to save secondary nodes on update', e))
    }

    isEditingRecord.value = false
    showDetailModal.value = false
    showCustomAlert(
      currentLocale.value === 'en' ? 'Record update request submitted successfully for approval.' : '레코드 수정 요청이 정상 상신되었습니다.',
      currentLocale.value === 'en' ? 'Request Submitted' : '요청 완료',
      currentLocale.value === 'en' ? 'Notification' : '알림',
      'success'
    )
    await fetchRecords()
  } catch (e) {
    console.error('Failed to update record:', e)
    let errorMsg = typeof e.response?._data === 'string' ? e.response._data : (e.response?._data?.message || e.message || 'Failed to update record.')
    if (errorMsg.includes('Deduplication Failed') || errorMsg.includes('Duplicate found')) {
      const match = errorMsg.match(/Identifier Field \((.*?)\)/)
      if (match) {
        errorMsg = t('error_dedup_failed', { field: match[1] })
      }
    } else if (errorMsg.includes('Domain is missing required field mappings')) {
      errorMsg = t('error_domain_missing_id')
    }
    showCustomAlert(
      errorMsg,
      currentLocale.value === 'en' ? 'Failed to Submit Update Request' : '수정 요청 상신 실패',
      currentLocale.value === 'en' ? 'Data Quality / Validation Error' : '데이터 품질 / 검증 오류',
      'error'
    )
  }
}

const requestDeleteRecord = async () => {
  try {
    const reqId = myUuid.value
    const payload = { requesterId: reqId, data: "{}" }
    await $fetch(`/api/records/${selectedRecordId.value}/delete-request`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` },
      body: payload
    })
    showDetailModal.value = false
    showCustomAlert(
      currentLocale.value === 'en' ? 'Record deletion request submitted successfully for approval.' : '레코드 삭제 요청이 정상 상신되었습니다.',
      currentLocale.value === 'en' ? 'Request Submitted' : '요청 완료',
      currentLocale.value === 'en' ? 'Notification' : '알림',
      'success'
    )
    await fetchRecords()
  } catch (e) {
    console.error('Failed to request deletion:', e)
    const errorMsg = typeof e.response?._data === 'string' ? e.response._data : (e.response?._data?.message || e.message || 'Failed to request deletion.')
    showCustomAlert(
      errorMsg,
      currentLocale.value === 'en' ? 'Failed to Submit Deletion Request' : '삭제 요청 상신 실패',
      currentLocale.value === 'en' ? 'Error' : '오류',
      'error'
    )
  }
}

const defaultColDef = {
  minWidth: 100,
  resizable: true,
  cellDataType: false
}

const evaluateFormula = (formula, data) => {
  try {
    const replaced = formula.replace(/\${([^}]+)}/g, (_, key) => {
      const val = data[key]
      return val != null && val !== '' ? val : '0'
    })
    const ROUND = (val, dec=0) => Number(Math.round(val+'e'+dec)+'e-'+dec);
    const fn = new Function('ROUND', 'ABS', 'CEIL', 'FLOOR', `return ${replaced};`)
    return fn(ROUND, Math.abs, Math.ceil, Math.floor)
  } catch (e) {
    console.warn('Formula evaluation failed', e)
    return null
  }
}

let isCalculating = false;

const handleCalculatedFields = (newData) => {
  if (isCalculating) return;
  
  const calculatedFields = nodeFields.value.filter(f => f.type === 'CALCULATED')
  if (calculatedFields.length === 0) return

  isCalculating = true;
  let changed = false;
  
  for (let pass = 0; pass < 3; pass++) {
    let passChanged = false;
    for (const field of calculatedFields) {
      try {
        const opts = JSON.parse(field.options || '{}')
        if (opts.formula) {
          const result = evaluateFormula(opts.formula, newData)
          if (result !== null && !isNaN(result) && String(newData[field.key]) !== String(result)) {
            newData[field.key] = result
            passChanged = true
            changed = true
          }
        }
      } catch(e) {}
    }
    if (!passChanged) break;
  }
  isCalculating = false;
}

watch(recordFormData, handleCalculatedFields, { deep: true })
watch(selectedRecordData, handleCalculatedFields, { deep: true })

const availableWorkflows = ref([])
const createWorkflowPermission = ref({})
const selectedWorkflowConfigId = ref(null)

const fetchEffectivePermissionForWorkflow = async (nodeId, actionType, workflowId = null) => {
  if (!nodeId) return {}
  try {
    let url = `/api/approval-requests/effective-permission/${nodeId}?actionType=${actionType}`
    if (workflowId) {
      url += `&workflowId=${workflowId}`
    }
    const res = await $fetch(url, { headers: { Authorization: `Bearer ${token.value}` } })
    return res || {}
  } catch (e) {
    return {}
  }
}

const openCreateModal = async () => {
  const initialData = {}
  nodeFields.value.forEach(f => {
    if (f.type === 'MULTILINGUAL') initialData[f.key] = { ko: '', en: '' }
  })
  recordFormData.value = initialData
  selectedWorkflowConfigId.value = null

  if (selectedNode.value && selectedNode.value.id) {
    try {
      const wfList = await $fetch(`/api/approval-requests/available-workflows/${selectedNode.value.id}?actionType=CREATE`, {
        headers: { Authorization: `Bearer ${token.value}` }
      })
      availableWorkflows.value = wfList || []
      if (!availableWorkflows.value || availableWorkflows.value.length === 0) {
        initToast({
          message: t('no_active_workflow') || '적용된 결재 워크플로우가 없어 레코드를 작성할 수 없습니다.',
          color: 'warning'
        })
        return
      }
      const defaultWf = availableWorkflows.value.find(w => w.isDefault) || availableWorkflows.value[0]
      if (defaultWf) {
        selectedWorkflowConfigId.value = defaultWf.id
      }
      createWorkflowPermission.value = await fetchEffectivePermissionForWorkflow(
        selectedNode.value.id,
        'CREATE',
        defaultWf ? defaultWf.id : null
      )
    } catch (e) {
      availableWorkflows.value = []
      createWorkflowPermission.value = {}
      initToast({
        message: t('no_active_workflow') || '적용된 결재 워크플로우가 없어 레코드를 작성할 수 없습니다.',
        color: 'warning'
      })
      return
    }
  } else {
    initToast({
      message: t('no_active_workflow') || '적용된 결재 워크플로우가 없어 레코드를 작성할 수 없습니다.',
      color: 'warning'
    })
    return
  }

  showCreateModal.value = true
}

const handleWorkflowSelectionChanged = async (workflowId) => {
  if (selectedNode.value && selectedNode.value.id && workflowId) {
    selectedWorkflowConfigId.value = workflowId
    createWorkflowPermission.value = await fetchEffectivePermissionForWorkflow(
      selectedNode.value.id,
      'CREATE',
      workflowId
    )
  }
}

const showDraftCommentModal = ref(false)
const draftCommentText = ref('')
const pendingSaveAction = ref(null)

const showDqValidationModal = ref(false)
const dqValidationResult = ref({ valid: true, errors: [], warnings: [] })
const dqValidating = ref(false)

const handleExcelUploaded = () => {
  showExcelUploader.value = false;
  fetchRecords();
  showCustomAlert(
    currentLocale.value === 'en' ? 'Bulk upload completed! Requests are now in PENDING status.' : '대량 엑셀 업로드가 완료되었습니다. 결재 대기 중(PENDING) 상태로 상신되었습니다.',
    currentLocale.value === 'en' ? 'Upload Completed' : '업로드 완료',
    currentLocale.value === 'en' ? 'Notification' : '알림',
    'success'
  );
}

const getFieldLabelByKey = (key) => {
  if (!key) return ''
  const f = nodeFields.value?.find(field => field.key === key || String(field.id) === String(key) || (field.key && String(field.key).toLowerCase() === String(key).toLowerCase()))
  return f ? getTranslatedName(f.name) : key
}

const getViolationMessageText = (msgObj) => {
  if (!msgObj) return 'Validation error'
  if (typeof msgObj === 'string') return msgObj
  return msgObj[currentLocale.value] || msgObj.ko || msgObj.en || Object.values(msgObj)[0] || 'Validation error'
}

const validateRequiredFields = (targetData) => {
  const missingFields = []
  firstMissingFieldKey.value = null
  if (!nodeFields.value || !targetData) return missingFields

  nodeFields.value.forEach(field => {
    if (field.isRemoved) return
    const rule = evalConditionRule(field, targetData)
    if (!rule.show) return

    // 자동 채번 필드는 최종 승인 시 서버에서 생성되므로 기안 시 필수 입력 검사 스킵
    const isAutoNumbering = (
      selectedDomainInfo.value &&
      field.id === selectedDomainInfo.value.identifierFieldId &&
      selectedDomainInfo.value.numberingPattern &&
      selectedDomainInfo.value.numberingPattern.trim() !== ''
    )
    if (isAutoNumbering) return

    const isReq = Boolean(rule.required || field.required)

    if (isReq) {
      const val = targetData[field.key]
      let isEmpty = false

      if (val === null || val === undefined) {
        isEmpty = true
      } else if (field.type === 'MULTILINGUAL') {
        let obj = val
        if (typeof val === 'string') {
          try { obj = JSON.parse(val) } catch (e) {}
        }
        if (typeof obj === 'object' && obj !== null) {
          const hasVal = Object.values(obj).some(v => v !== null && v !== undefined && String(v).trim() !== '')
          if (!hasVal) isEmpty = true
        } else if (String(val).trim() === '') {
          isEmpty = true
        }
      } else if (typeof val === 'string') {
        if (val.trim() === '') isEmpty = true
      } else if (Array.isArray(val)) {
        if (val.length === 0) isEmpty = true
      }

      if (isEmpty) {
        const labelName = getTranslatedName(field.name) || field.key
        missingFields.push(labelName)
        if (!firstMissingFieldKey.value) {
          firstMissingFieldKey.value = field.key
        }
      }
    }
  })

  return missingFields
}

const focusFirstMissingField = () => {
  showRequiredWarningModal.value = false
  if (!firstMissingFieldKey.value) return
  const targetKey = firstMissingFieldKey.value
  
  nextTick(() => {
    setTimeout(() => {
      const wrapper = document.querySelector(`[data-field-key="${targetKey}"]`)
      if (wrapper) {
        wrapper.scrollIntoView({ behavior: 'smooth', block: 'center' })
        const inputEl = wrapper.querySelector('input, textarea, select')
        if (inputEl && typeof inputEl.focus === 'function') {
          inputEl.focus()
        }
      }
    }, 150)
  })
}

const pendingSecondaryNodes = ref([])

const promptDraftComment = async (action, eventPayload) => {
  if (eventPayload && eventPayload.secondaryNodes) {
    pendingSecondaryNodes.value = eventPayload.secondaryNodes
  } else {
    pendingSecondaryNodes.value = []
  }
  const targetData = action === 'CREATE' ? recordFormData.value : selectedRecordData.value

  const missing = validateRequiredFields(targetData)
  if (missing.length > 0) {
    missingRequiredFields.value = missing
    showRequiredWarningModal.value = true
    return
  }

  if (action === 'UPDATE') {
    const orig = JSON.stringify(formatDataForSave(originalRecordData.value))
    const curr = JSON.stringify(formatDataForSave(selectedRecordData.value))
    if (orig === curr) {
      showCustomAlert(
        currentLocale.value === 'en' ? 'No data has been modified.' : '변경된 데이터가 없습니다.',
        currentLocale.value === 'en' ? 'No Changes' : '변경 없음',
        currentLocale.value === 'en' ? 'Notice' : '안내',
        'warning'
      )
      return
    }
  }
  pendingSaveAction.value = action

  const formattedData = formatDataForSave(targetData)

  dqValidating.value = true
  showDqValidationModal.value = true
  try {
    const recIdQuery = action === 'UPDATE' && selectedRecordId.value ? `&recordId=${selectedRecordId.value}` : ''
    const res = await $fetch(`/api/dq-rules/validate?nodeId=${selectedNode.value.id}${recIdQuery}`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` },
      body: { data: formattedData }
    })
    dqValidationResult.value = res || { valid: true, errors: [], warnings: [] }
  } catch (e) {
    console.error('DQ Validation error:', e)
    dqValidationResult.value = {
      valid: false,
      errors: [{ fieldKey: '_server', severity: 'ERROR', message: { ko: 'DQ 품질 검증 서버 처리 중 오류가 발생했습니다.', en: 'DQ Validation error occurred.' } }],
      warnings: []
    }
  } finally {
    dqValidating.value = false
  }
}

const fixDataAndReturn = () => {
  showDqValidationModal.value = false
}

const proceedToDraftComment = () => {
  showDqValidationModal.value = false
  draftCommentText.value = ''
  showDraftCommentModal.value = true
}

const executePendingSave = async () => {
  if (pendingSaveAction.value === 'CREATE') {
    await saveRecord()
  } else if (pendingSaveAction.value === 'UPDATE') {
    await saveEditedRecord()
  }
  showDraftCommentModal.value = false
}

const saveRecord = async () => {
  if (!selectedNode.value) return
  try {
    const reqId = myUuid.value

    const dataToSave = { ...recordFormData.value }
    for (const field of nodeFields.value) {
      if (field.type === 'FILE' && recordFormData.value[field.key]) {
        let files = recordFormData.value[field.key]
        if (!Array.isArray(files)) {
          files = [files]
        }
        
        const uploadedUrls = []
        for (const file of files) {
          if (file instanceof File) {
            const fd = new FormData()
            fd.append('file', file)
            const res = await $fetch('/api/files/upload', {
              method: 'POST',
              headers: { Authorization: `Bearer ${token.value}` },
              body: fd
            })
            uploadedUrls.push(res.url)
          } else if (typeof file === 'string') {
            uploadedUrls.push(file)
          }
        }
        
        if (uploadedUrls.length > 0) {
          dataToSave[field.key] = isMultiple(field) ? JSON.stringify(uploadedUrls) : uploadedUrls[0]
        } else {
          dataToSave[field.key] = null
        }
      } else {
        dataToSave[field.key] = recordFormData.value[field.key]
      }
    }

    const formattedData = formatDataForSave(dataToSave)

    const payload = {
      data: JSON.stringify(formattedData),
      requesterId: reqId,
      comment: draftCommentText.value,
      workflowConfigId: selectedWorkflowConfigId.value
    }
    
    const res = await $fetch(`/api/nodes/${selectedNode.value.id}/records`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` },
      body: payload
    })
    
    // Save Secondary Nodes if any
    if (res && res.record && res.record.id && pendingSecondaryNodes.value.length > 0) {
      await $fetch(`/api/records/${res.record.id}/secondary-nodes`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token.value}` },
        body: { nodeIds: pendingSecondaryNodes.value }
      }).catch(e => console.error('Failed to save secondary nodes', e))
    }

    showCreateModal.value = false
    await fetchRecords()
  } catch (error) {
    let errorMsg = error.response?._data?.message || error.message || String(error)
    if (errorMsg.includes('Deduplication Failed') || errorMsg.includes('Duplicate found')) {
      const match = errorMsg.match(/Identifier Field \((.*?)\)/)
      if (match) {
        errorMsg = t('error_dedup_failed', { field: match[1] })
      }
    } else if (errorMsg.includes('Domain is missing required field mappings')) {
      errorMsg = t('error_domain_missing_id')
    }
    showCustomAlert(
      errorMsg,
      currentLocale.value === 'en' ? 'Failed to Create Record' : '레코드 생성 실패',
      currentLocale.value === 'en' ? 'Data Quality / Validation Error' : '데이터 품질 / 검증 오류',
      'error'
    )
    console.error('Full error:', error, error.response?._data)
  }
}
</script>

<style scoped>
.records-layout {
  display: flex;
  height: 100%;
  width: 100%;
  min-height: 0;
}
.records-tree-column {
  width: 300px;
  min-width: 300px;
  border-right: 1px solid #ddd;
  display: flex;
  flex-direction: column;
}
.records-detail-column {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 0 0 0 1rem;
  box-sizing: border-box;
}
.records-grid-wrapper {
  flex: 1;
  width: 100%;
  min-height: 0;
}

@media (max-width: 768px) {
  .records-layout {
    flex-direction: column;
  }
  .records-tree-column {
    width: 100%;
    min-width: 100%;
    border-right: none;
    border-bottom: 1px solid #ddd;
    max-height: 250px;
  }
  .records-detail-column {
    padding: 0.25rem 0;
  }
  .records-grid-wrapper {
    height: 400px;
  }
}

.mb-4 { margin-bottom: 1rem; }
.mt-2 { margin-top: 0.5rem; }
.w-full { width: 100%; }

:deep(.va-tree) {
  overflow-x: hidden;
}
</style>
