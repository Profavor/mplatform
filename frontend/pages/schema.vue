<template>
  <div style="display: flex; flex-direction: column; gap: 1.25rem; height: 100%; min-height: 0;">
    <!-- Top Action Bar -->
    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); padding: 1rem 1.25rem; border-radius: 12px; border: 1px solid var(--va-background-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04); flex: 0 0 auto;">
      <div style="display: flex; align-items: center; gap: 0.75rem;">
        <va-icon name="schema" size="large" color="primary" />
        <div>
          <h2 style="font-weight: 700; font-size: 1.35rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
            {{ pageTitle }}
            <va-badge :text="$t('governance')" color="primary" size="small" />
          </h2>
          <span style="font-size: 0.85rem; color: var(--va-text-secondary);">
            {{ $t('domain_schema_desc') }}
          </span>
        </div>
      </div>

      <div style="display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap;">
        <va-button preset="outline" color="info" icon="hub" size="small" @click="showOntologyModal = true">
          {{ $t('semantic_ontology') }}
        </va-button>
        <va-button preset="outline" color="warning" icon="published_with_changes" size="small" @click="showCompatibilityModal = true">
          {{ $t('schema_compatibility') }}
        </va-button>
        <va-button preset="outline" color="primary" icon="inventory_2" size="small" @click="showPackageModal = true">
          {{ $t('schema_package') }}
        </va-button>
      </div>
    </div>
    
    <div class="schema-layout" style="flex: 1; min-height: 0;">
      <!-- Tree Column -->
      <div class="schema-tree-column">
        <va-card>
          <va-card-title>
            {{ $t('classification_tree') }}
          </va-card-title>
          <va-card-content>
            <div class="schema-tree-wrapper">
              <ClassificationTree
                ref="treeRef"
                :selectedNode="selectedNode"
                :showEdit="true"
                :hideAxisSelect="true"
                :emptyMessage="$t('tree_empty_message')"
                @select="onNodeSelected"
                @edit="handleNodeEdit"
                @delete="handleNodeDelete"
                @loaded="onTreeLoaded"
              />
            </div>
            <div style="display: flex; gap: 0.75rem; margin-top: 1.5rem; padding: 0 0.5rem;">
              <va-button v-if="hasPermission('domain:write') || hasPermission('domain:*')" style="flex: 1; border-radius: 8px; box-shadow: 0 2px 6px rgba(21,78,193,0.15);" icon="create_new_folder" @click="openDomainModal()" color="primary">{{ $t('domain') }}</va-button>
              <va-button v-if="hasPermission('node:write') || hasPermission('node:*')" style="flex: 1; border-radius: 8px; box-shadow: 0 2px 6px rgba(21,78,193,0.15);" icon="note_add" @click="openNodeModal()" :disabled="!selectedNode" color="primary" :preset="selectedNode ? 'primary' : 'secondary'">{{ $t('node') }}</va-button>
            </div>
            <div style="margin-top: 0.75rem; padding: 0 0.5rem;">
              <va-button preset="secondary" style="width: 100%;" @click="showRequestAccessModal = true">{{ $t('request_domain_access') }}</va-button>
            </div>
            <div style="margin-top: 0.75rem; padding: 0 0.5rem;">
              <va-button style="width: 100%; border-radius: 8px; font-weight: 600;" color="info" icon="tune" @click="openSectorGroupModal" :disabled="!treeNodes || treeNodes.length === 0" preset="secondary">{{ $t('manage_sectors_groups') }}</va-button>
            </div>
          </va-card-content>
        </va-card>
      </div>
      
      <!-- Detail Column -->
      <div class="schema-detail-column">
        <va-card v-if="selectedNode" style="flex: 1; display: flex; flex-direction: column; min-height: 0;">
          <va-card-title>
            <va-tabs v-model="activeTab" style="width: 100%;">
              <template #tabs>
                <va-tab>{{ $t('tab_fields') }}</va-tab>
                <va-tab>{{ $t('schema_history.title') }}</va-tab>
                <va-tab v-if="selectedNode && selectedNode.type === 'domain'">{{ $t('classification_axes') }}</va-tab>
                <va-tab v-if="selectedNode && selectedNode.type === 'domain'">{{ $t('data_profiling') }}</va-tab>
              </template>
            </va-tabs>
          </va-card-title>
          <va-card-content style="flex: 1; display: flex; flex-direction: column; min-height: 0; padding: 0;">
            <!-- Fields Tab -->
            <div v-show="activeTab === 0" style="flex: 1; display: flex; flex-direction: column; min-height: 0; padding: 1rem;">
              <!-- Grid Title & Action Bar -->
              <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.5rem 0.85rem; margin-bottom: 0; background: var(--va-background-element, #f4f6f9); border: 1px solid var(--va-background-border); border-bottom: none; border-top-left-radius: 8px; border-top-right-radius: 8px;">
                <div style="display: flex; align-items: center; gap: 0.5rem;">
                  <va-icon name="list_alt" color="primary" size="1.1rem" />
                  <span style="font-weight: 700; font-size: 0.95rem; color: var(--va-text-primary);">
                    {{ selectedNode ? getTranslatedName(selectedNode.name) + ' ' + ($t('tab_fields')) : ($t('tab_fields')) }}
                  </span>
                  <va-chip size="small" color="primary" style="font-weight: 600;">{{ $t('item_count', { count: fields.length }) }}</va-chip>
                </div>

                <div style="display: flex; align-items: center; gap: 0.4rem;">
                  <va-button v-if="hasPermission('field:write') || hasPermission('field:*')" size="small" icon="add" @click="openFieldModal(null)">{{ $t('add_field') }}</va-button>
                  <va-button
                    size="small"
                    color="secondary"
                    outline
                    @click="showBusinessRuleBuilderModal = true"
                  >
                    <va-icon name="rule" class="mr-1" /> {{ $t('business_rule_builder') }}
                  </va-button>
                  <va-button preset="plain" color="secondary" size="small" icon="refresh" @click="refreshSchemaData">{{ $t('refresh') }}</va-button>
                </div>
              </div>

              <div class="schema-grid-wrapper" :class="{ 'ag-theme-quartz-dark': isDark }">
                <ag-grid-vue
                  style="width: 100%; height: 100%;"
                  :theme="gridTheme"
                  :autoSizeStrategy="autoSizeStrategy"
                  :columnDefs="columnDefs"
                  :rowHeight="42"
                  :rowSelection="{ mode: 'singleRow', headerCheckbox: false }"
                  rowModelType="infinite"
                  :pagination="true"
                  :paginationPageSize="20"
                  :cacheBlockSize="20"
                  @grid-ready="onGridReady"
                  @column-resized="onColumnChanged"
                  @column-moved="onColumnChanged"
                  @rowDoubleClicked="onRowDoubleClicked"
                />
              </div>
            </div>

            <!-- Schema History Tab -->
            <div v-show="activeTab === 1" style="flex: 1; display: flex; flex-direction: column; min-height: 0; padding: 1rem; overflow-y: auto;">
              <SchemaHistoryTab :domain-id="selectedNode?.domainId || selectedNode?.id || (selectedNode?.type === 'domain' ? selectedNode?.id : null)" />
            </div>

            <!-- Classification Axes Tab (Domain Only) -->
            <div v-if="selectedNode && selectedNode.type === 'domain'" v-show="activeTab === 2" style="flex: 1; display: flex; flex-direction: column; min-height: 0; padding: 1rem;">
              <ClassificationAxisTab :domain-id="selectedNode.id" />
            </div>

            <!-- Data Profiling Tab (Domain Only) -->
            <div v-if="selectedNode && selectedNode.type === 'domain'" v-show="activeTab === 3" style="flex: 1; display: flex; flex-direction: column; min-height: 0;">
              <DataProfilingTab :domain-id="selectedNode.id" />
            </div>
          </va-card-content>
        </va-card>
        <va-card v-else>
          <va-card-content style="text-align: center; padding: 3rem; color: #666;">
            {{ $t('select_node_prompt') }}
          </va-card-content>
        </va-card>
      </div>
    </div>

    <!-- Domain Modal -->
    <SchemaDomainModal
      v-model="showDomainModal"
      :is-edit-mode="isEditMode"
      :new-domain="newDomain"
      :domain-field-options="domainFieldOptions"
      :mapping-error="mappingError"
      @save="saveDomain"
      @open-icon-picker="openIconPicker"
    />

    <SchemaNodeModal
      v-model="showNodeModal"
      :is-edit-mode="isEditMode"
      :new-node="newNode"
      :selected-node="selectedNode"
      @save="saveNode"
      @delete="handleNodeDelete"
      @open-icon-picker="openIconPicker"
    />

    <!-- Field Modal -->
    <SchemaFieldModal
      v-model="showFieldModal"
      :is-edit-mode="isEditMode"
      :selected-node="selectedNode"
      :is-current-field-pending-approval="isCurrentFieldPendingApproval"
      :new-field="newField"
      :available-classification-nodes="availableClassificationNodes"
      :group-options="groupOptions"
      :field-types="fieldTypes"
      :masking-pattern-options="maskingPatternOptions"
      :domain-options="domainOptions"
      :unit-options="unitOptions"
      :is-dark="isDark"
      :grid-theme="gridTheme"
      :auto-size-strategy="autoSizeStrategy"
      :options-column-defs="optionsColumnDefs"
      :new-field-options-list="newFieldOptionsList"
      :options-default-col-def="optionsDefaultColDef"
      :new-field-table-columns="newFieldTableColumns"
      :available-condition-fields="availableConditionFields"
      :can-edit="hasPermission('field:write') || hasPermission('field:*')"
      @target-node-selected="onTargetNodeSelected"
      @is-domain-field-checked="onIsDomainFieldChecked"
      @add-grid-option="addGridOption"
      @remove-selected-grid-option="removeSelectedGridOption"
      @options-grid-ready="onOptionsGridReady"
      @add-table-column="handleAddTableColumn"
      @remove-table-column="handleRemoveTableColumn"
      @save="saveField"
    />

    <!-- Sector & Group Manager Modal -->
    <SchemaSectorGroupModal
      v-model="showSectorGroupModal"
      v-model:sgActiveTab="sgActiveTab"
      :is-dark="isDark"
      :grid-theme="gridTheme"
      :auto-size-strategy="autoSizeStrategy"
      :sector-column-defs="sectorColumnDefs"
      :domain-sectors="domainSectors"
      :group-column-defs="groupColumnDefs"
      :domain-groups="domainGroups"
      :sg-default-col-def="sgDefaultColDef"
      @add-sector-row="addSectorRow"
      @save-all-sectors="saveAllSectors"
      @delete-selected-sector="deleteSelectedSector"
      @sector-grid-ready="onSectorGridReady"
      @add-group-row="addGroupRow"
      @save-all-groups="saveAllGroups"
      @delete-selected-group="deleteSelectedGroup"
      @group-grid-ready="onGroupGridReady"
      @save-sector-group-changes="saveSectorGroupChanges"
    />

    <!-- Icon Picker Modal (Decoupled Component) -->
    <IconPickerModal
      v-model="showIconPickerModal"
      v-model:icon="tempIcon"
      @confirm="applyIcon"
    />

    <!-- Pre-change Impact Review Modal -->
    <SchemaImpactReportModal
      v-model="showImpactModal"
      :domainId="selectedDomainId"
      :changeRequest="impactChangeRequest"
      :isSubmitMode="true"
      :z-index="1200"
      @confirm="confirmImpactAnalysisAction"
      @confirm-submit="confirmImpactAnalysisAction"
    />

    <!-- Request Access Modal -->
    <DomainAccessRequestModal v-model="showRequestAccessModal" />

    <!-- DQ Rule Editor Modal -->
    <DqRuleEditor
      v-model="showDqRuleEditor"
      :fieldId="dqTargetFieldId"
      :fieldName="dqTargetFieldName"
    />
    <!-- System Notification Modal (Decoupled Component) -->
    <SystemNotificationModal
      v-model="showErrorAlertModal"
      :type="errorAlertType"
      :title="errorAlertTitle"
      :header="errorAlertHeader"
      :message="errorAlertMessage"
    />

    <!-- Submission Comment Modal (공통 상신 의견 작성 모달) -->
    <SubmissionCommentModal
      v-model="showFieldCommentModal"
      v-model:comment="draftFieldCommentText"
      @submit="handleSubmissionCommentSubmit"
    />

    <!-- Approval Details Viewer Modal (Decoupled Component) -->
    <ApprovalViewerModal
      v-model="showApprovalViewer"
      :request="selectedApprovalRequest"
    />

    <!-- Domain Schema Package Modal (Export / Import) -->
    <SchemaPackageModal
      v-model="showPackageModal"
      :domain-id="selectedDomainId"
      :domain-name="selectedDomainName"
      :domain-options="domainOptions"
      @imported="handlePackageImported"
    />

    <!-- Semantic Ontology Modal -->
    <SemanticOntologyModal
      v-model="showOntologyModal"
    />

    <!-- Schema Backward Compatibility Modal -->
    <SchemaCompatibilityModal
      v-model="showCompatibilityModal"
      :domainId="selectedDomainId"
    />

    <!-- Business Rule Builder Modal -->
    <BusinessRuleBuilderModal
      v-model="showBusinessRuleBuilderModal"
      :domainId="selectedDomainId"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useCookie, useState } from '#app'
import { AgGridVue } from 'ag-grid-vue3'
import { useI18n } from 'vue-i18n'
import { useToast, useColors } from 'vuestic-ui'
import { usePageTitle } from '~/composables/usePageTitle'
import { usePermission } from '~/composables/usePermission'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { useCodeStore } from '~/stores/useCodeStore'
import SchemaImpactReportModal from '~/components/SchemaImpactReportModal.vue'
import ApprovalViewerModal from '~/components/ApprovalViewerModal.vue'
import SystemNotificationModal from '~/components/common/SystemNotificationModal.vue'
import IconPickerModal from '~/components/common/IconPickerModal.vue'
import SchemaPackageModal from '~/components/schema/SchemaPackageModal.vue'
import SemanticOntologyModal from '~/components/schema/SemanticOntologyModal.vue'
import SchemaCompatibilityModal from '~/components/schema/SchemaCompatibilityModal.vue'
import BusinessRuleBuilderModal from '~/components/records/BusinessRuleBuilderModal.vue'
import SchemaHistoryTab from '~/components/schema/SchemaHistoryTab.vue'
import WorkflowConfigTab from '~/components/schema/WorkflowConfigTab.vue'
import ClassificationAxisTab from '~/components/schema/ClassificationAxisTab.vue'
import DataProfilingTab from '~/components/schema/DataProfilingTab.vue'

const toast = useToast()
const { customFetch } = useCustomFetch()
const { t, locale } = useI18n()
const { pageTitle } = usePageTitle('domain_schema_title', '도메인 스키마 관리')
const { hasPermission } = usePermission()
const codeStore = useCodeStore()
const currentLocale = useCookie('locale', { default: () => 'ko' })
const colors = useColors()
const currentPresetName = colors?.currentPresetName
const isDark = computed(() => currentPresetName?.value === 'dark')

const showApprovalViewer = ref(false)
const showBusinessRuleBuilderModal = ref(false)
const selectedApprovalRequest = ref(null)
const showPackageModal = ref(false)
const showOntologyModal = ref(false)
const showCompatibilityModal = ref(false)

const handlePackageImported = (result) => {
  if (treeRef.value?.loadTree) {
    treeRef.value.loadTree()
  }
}

const openApprovalViewer = async (requestId) => {
  if (!requestId) return
  try {
    const data = await customFetch(`/api/approval-requests/${requestId}`)
    selectedApprovalRequest.value = data
    showApprovalViewer.value = true
  } catch (e) {
    console.error('Failed to load approval request:', e)
    showCustomAlert(e.data?.message || t('approval_load_failed', '결재 내역을 불러오는데 실패했습니다.'), 'Error', 'Error', 'danger')
  }
}

const showImpactModal = ref(false)
const impactChangeRequest = ref({
  changeType: 'DELETE_FIELD',
  fieldDefinitionId: null
})

const pendingFieldAction = ref(null)

const triggerSaveFieldWithImpactAnalysis = () => {
  const fName = typeof newField.value.name === 'object' 
    ? (newField.value.name.ko || newField.value.name.en || newField.value.key) 
    : (newField.value.name || newField.value.key)

  impactChangeRequest.value = {
    changeType: isEditMode.value ? 'MODIFY_FIELD' : 'ADD_FIELD',
    fieldDefinitionId: isEditMode.value ? editingId.value : null,
    fieldKey: newField.value.key,
    fieldName: fName,
    newFieldType: newField.value.type
  }
  pendingFieldAction.value = () => executePendingFieldSave()
  showImpactModal.value = true
}

const pendingDeleteTargetField = ref(null)

const triggerDeleteFieldWithImpactAnalysis = (fieldData) => {
  if (!fieldData) return
  const fName = typeof fieldData.name === 'object'
    ? (fieldData.name.ko || fieldData.name.en || fieldData.key)
    : (fieldData.name || fieldData.key)

  impactChangeRequest.value = {
    changeType: 'DELETE_FIELD',
    fieldDefinitionId: fieldData.id,
    fieldKey: fieldData.key,
    fieldName: fName
  }
  pendingDeleteTargetField.value = fieldData
  pendingFieldAction.value = () => {
    draftFieldCommentText.value = ''
    showFieldCommentModal.value = true
  }
  showImpactModal.value = true
}

const confirmImpactAnalysisAction = async () => {
  showImpactModal.value = false
  if (pendingFieldAction.value) {
    const action = pendingFieldAction.value
    pendingFieldAction.value = null
    await action()
  }
}

const handleSubmissionCommentSubmit = async () => {
  showFieldCommentModal.value = false
  if (impactChangeRequest.value?.changeType === 'DELETE_FIELD' && pendingDeleteTargetField.value) {
    await executeDeleteField(pendingDeleteTargetField.value, draftFieldCommentText.value)
    pendingDeleteTargetField.value = null
  } else {
    await executePendingFieldSave()
  }
}

const executeDeleteField = async (fieldData, reasonText = '') => {
  if (!fieldData || !fieldData.id) return
  try {
    const dId = selectedNode.value?.domainId || selectedNode.value?.id
    let deleteUrl = selectedNode.value?.isDomain
      ? `/api/domains/${dId}/fields/${fieldData.id}`
      : `/api/nodes/${selectedNode.value?.id}/fields/${fieldData.id}`

    if (reasonText && reasonText.trim()) {
      deleteUrl += `?reason=${encodeURIComponent(reasonText.trim())}`
    }

    await customFetch(deleteUrl, {
      method: 'DELETE'
    })
    showCustomAlert(t('field_delete_approval_submitted'), t('approval_submitted_title'), 'Success', 'success')
    await onNodeSelected(selectedNode.value)
  } catch (e) {
    console.error('Failed to delete field:', e)
    showCustomAlert(t('field_delete_failed'), t('delete_error_title'), 'Error', 'error')
  }
}

const selectedDomainId = computed(() => {
  if (selectedNode.value) {
    return selectedNode.value.domainId || selectedNode.value.id || null
  }
  const rootDomain = treeNodes.value?.find(n => n.isDomain) || treeNodes.value?.[0]
  return rootDomain?.domainId || rootDomain?.id || null
})

const selectedDomainName = computed(() => {
  if (selectedNode.value) {
    return selectedNode.value.label || selectedNode.value.name?.ko || selectedNode.value.name?.en || ''
  }
  const rootDomain = treeNodes.value?.find(n => n.isDomain) || treeNodes.value?.[0]
  return rootDomain?.label || rootDomain?.name?.ko || rootDomain?.name?.en || ''
})

const { gridTheme, autoSizeStrategy } = useAgGridTheme()

const showErrorAlertModal = ref(false)
const errorAlertTitle = ref('')
const errorAlertHeader = ref('')
const errorAlertMessage = ref('')
const errorAlertType = ref('success')

const showCustomAlert = (msg, header = '', title = '', type = 'success') => {
  errorAlertMessage.value = msg
  errorAlertHeader.value = header
  errorAlertTitle.value = title
  errorAlertType.value = type
  showErrorAlertModal.value = true
}

const userCookie = useCookie('user_data')

const activeTab = ref(0)
const sgActiveTab = ref(0)
const workflowConfigs = ref({
  CREATE: { steps: [], observerIds: [] },
  UPDATE: { steps: [], observerIds: [] },
  DELETE: { steps: [], observerIds: [] }
})
const isSavingWorkflows = ref(false)
const userOptions = ref([])
const unitOptions = ref([])
const treeNodes = ref([])
const selectedNode = ref(null)
const fields = ref([])

const createFieldsDatasource = () => {
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
          ? `/api/domains/${selectedNode.value.id}/fields/page?page=${page}&size=${size}`
          : `/api/nodes/${selectedNode.value.id}/fields/effective/page?page=${page}&size=${size}`;
          
        const pageData = await customFetch(endpoint);
        params.successCallback(pageData.content, pageData.totalElements);
      } catch (e) {
        console.error('Failed to load fields page:', e);
        params.failCallback();
      }
    }
  };
};

const currentUser = computed(() => {
  if (userCookie.value) {
    return typeof userCookie.value === 'string' ? JSON.parse(userCookie.value) : userCookie.value
  }
  return null
})



const fetchFields = () => {
  if (fieldsGridApi.value) {
    fieldsGridApi.value.setGridOption('datasource', createFieldsDatasource());
  }
}

const fieldsGridApi = ref(null)
const gridApi = ref(null)

const showDomainModal = ref(false)
const showNodeModal = ref(false)
const showFieldModal = ref(false)
const showFieldCommentModal = ref(false)
const draftFieldCommentText = ref('')
const showSectorGroupModal = ref(false)
const showIconPickerModal = ref(false)
const tempIcon = ref('')
const isPickingForDomain = ref(true)

const openIconPicker = (forDomain) => {
  isPickingForDomain.value = forDomain
  tempIcon.value = forDomain ? newDomain.value.icon : newNode.value.icon
  showIconPickerModal.value = true
}

const applyIcon = () => {
  if (isPickingForDomain.value) {
    newDomain.value.icon = tempIcon.value
  } else {
    newNode.value.icon = tempIcon.value
  }
  showIconPickerModal.value = false
}

const showRequestAccessModal = ref(false)

// DQ Rule Editor
const showDqRuleEditor = ref(false)
const dqTargetFieldId = ref(null)
const dqTargetFieldName = ref('')
const openDqRuleEditor = (fieldData) => {
  if (!fieldData) return
  dqTargetFieldId.value = fieldData.id
  const nameObj = typeof fieldData.name === 'string' ? JSON.parse(fieldData.name || '{}') : fieldData.name
  dqTargetFieldName.value = nameObj?.[currentLocale.value] || nameObj?.ko || nameObj?.en || fieldData.key || 'Field'
  showDqRuleEditor.value = true
}

const isEditMode = ref(false)
const editingId = ref(null)

const newDomain = ref({ 
  name: { ko: '', en: '' }, 
  description: { ko: '', en: '' }, 
  identifierFieldId: null, 
  displayNameFieldId: null, 
  descriptionFieldId: null,
  icon: '',
  sortOrder: 0,
  autoDqScanEnabled: false
})
const domainFieldOptions = ref([])
const mappingError = ref({ id: false, name: false })

const newNode = ref({ name: { ko: '', en: '' }, order: 1, icon: '' })
const newField = ref({ 
  id: null,
  name: { ko: '', en: '' },
  hint: { ko: '', en: '' },
  fieldGroupId: null,
  key: '', 
  type: 'TEXT', 
  options: '', 
  formula: '',
  targetDomainId: null,
  required: false, 
  isMultiValue: false, 
  isSearchable: true, 
  isEncrypted: false,
  isReadOnly: false,
  isImmutable: false,
  isHidden: false,
  isHighlighted: false,
  unit: '',
  order: 0
})

watch(() => newField.value.key, (newVal) => {
  if (newVal) {
    const sanitized = newVal.toUpperCase().replace(/[^A-Z_]/g, '');
    if (newVal !== sanitized) {
      newField.value.key = sanitized;
    }
  }
})

const newFieldOptionsList = ref([])
const optionsGridApi = ref(null)

const optionsDefaultColDef = {
  flex: 1,
  editable: true,
  sortable: true,
  resizable: true,
}

const optionsColumnDefs = [
  { 
    headerName: 'Key', 
    field: 'key',
    valueSetter: params => {
      params.data.key = String(params.newValue || '').toUpperCase().replace(/[^A-Z_]/g, '');
      return true;
    },
    cellStyle: params => {
      if (!params.value || String(params.value).trim() === '') {
        return { backgroundColor: '#fff3cd' } // Soft yellow warning
      }
      return null
    }
  },
  { headerName: 'Label (KO)', field: 'label.ko', valueSetter: params => { params.data.label.ko = params.newValue; return true; } },
  { headerName: 'Label (EN)', field: 'label.en', valueSetter: params => { params.data.label.en = params.newValue; return true; } },
  { headerName: 'Order', field: 'order', valueParser: params => Number(params.newValue) || 0 }
]

const sgDefaultColDef = { flex: 1, sortable: true, resizable: true }

const sectorColumnDefs = [
  { field: 'name.ko', headerName: 'Name (KO)', editable: true },
  { field: 'name.en', headerName: 'Name (EN)', editable: true },
  { field: 'sortOrder', headerName: 'Order', width: 100, flex: 0, editable: true, valueParser: p => Number(p.newValue) || 0 }
]

const groupColumnDefs = [
  { 
    headerName: 'Sector',
    editable: true,
    cellEditor: 'agSelectCellEditor',
    cellEditorParams: params => ({ values: domainSectors.value.map(s => s.id) }),
    valueGetter: p => p.data.sector?.id,
    valueSetter: p => {
      if (!p.data.sector) p.data.sector = {}
      p.data.sector.id = p.newValue
      return true
    },
    valueFormatter: params => {
      const s = domainSectors.value.find(sec => sec.id === params.value)
      return s ? (s.name?.ko || s.name?.en) : ''
    }
  },
  { field: 'name.ko', headerName: 'Name (KO)', editable: true },
  { field: 'name.en', headerName: 'Name (EN)', editable: true },
  { field: 'sortOrder', headerName: 'Order', width: 100, flex: 0, editable: true, valueParser: p => Number(p.newValue) || 0 },
  { 
    field: 'isDefaultOpen', 
    headerName: 'Default Open', 
    editable: true,
    cellEditor: 'agSelectCellEditor',
    cellEditorParams: { values: [true, false] },
    valueFormatter: p => p.value !== false ? 'O' : 'X',
    width: 140,
    flex: 0 
  }
]

const onOptionsGridReady = (params) => {
  optionsGridApi.value = params.api
}

const addGridOption = () => {
  newFieldOptionsList.value.push({ key: '', label: { ko: '', en: '' }, order: 0 })
  if (optionsGridApi.value) {
    optionsGridApi.value.setRowData(newFieldOptionsList.value)
  }
}

const removeSelectedGridOption = () => {
  if (!optionsGridApi.value) return
  const selectedNodes = optionsGridApi.value.getSelectedNodes()
  if (selectedNodes.length > 0) {
    const selectedData = selectedNodes.map(node => node.data)
    newFieldOptionsList.value = newFieldOptionsList.value.filter(opt => !selectedData.includes(opt))
    optionsGridApi.value.setRowData(newFieldOptionsList.value)
  }
}

const newFieldTableColumns = ref([])

const handleAddTableColumn = () => {
  newFieldTableColumns.value.push({
    key: '',
    name: { ko: '', en: '' },
    type: 'TEXT',
    required: false,
    width: 150,
    optionsStr: ''
  })
}

const handleRemoveTableColumn = (idx) => {
  newFieldTableColumns.value.splice(idx, 1)
}

const formatColOptionsToStr = (options) => {
  if (!options) return ''
  if (typeof options === 'string') return options
  if (Array.isArray(options)) {
    return options.map(opt => {
      if (typeof opt === 'object' && opt !== null) {
        const k = opt.key || opt.value || ''
        const ko = opt.label?.ko || (typeof opt.label === 'string' ? opt.label : '') || ''
        const en = opt.label?.en || ''
        if (k && ko && en && (k !== ko || ko !== en)) {
          return `${k}:${ko}:${en}`
        } else if (k && ko && k !== ko) {
          return `${k}:${ko}`
        }
        return k || ko || JSON.stringify(opt)
      }
      return String(opt)
    }).join(', ')
  }
  return ''
}

const parseColOptionsStr = (str) => {
  if (!str || !str.trim()) return []
  const items = str.split(',').map(s => s.trim()).filter(Boolean)
  return items.map(item => {
    if (item.includes(':')) {
      const parts = item.split(':').map(p => p.trim())
      const k = parts[0]
      const ko = parts[1] || k
      const en = parts[2] || ko
      return {
        key: k,
        value: k,
        label: { ko, en }
      }
    }
    return {
      key: item,
      value: item,
      label: { ko: item, en: item }
    }
  })
}

const domainSectors = ref([])
const domainGroups = ref([])

const sectorGridApi = ref(null)
const groupGridApi = ref(null)

const onSectorGridReady = (params) => sectorGridApi.value = params.api
const onGroupGridReady = (params) => groupGridApi.value = params.api

const sectorOptions = computed(() => {
  return domainSectors.value.map(s => ({
    text: formatMultilingual(s.name) || s.id,
    value: s.id
  }))
})

const groupOptions = computed(() => {
  return domainGroups.value.map(g => {
    let sName = ''
    if (g.sector?.name) {
      sName = formatMultilingual(g.sector.name)
    } else if (g.sectorId || g.sector?.id) {
      const secId = g.sectorId || g.sector?.id
      const foundSector = domainSectors.value.find(s => s.id === secId)
      if (foundSector) {
        sName = formatMultilingual(foundSector.name)
      }
    }
    const gName = formatMultilingual(g.name) || g.id
    return {
      text: sName ? `[${sName}] ${gName}` : gName,
      value: g.id
    }
  })
})

const fieldTypes = computed(() => {
  const options = codeStore.getDropdownOptions('FIELD_TYPE') || []
  return options.filter(opt => !['ENUM', 'MULTI_SELECT', 'DECIMAL', 'FLOAT', 'INTEGER', 'CHECKBOX'].includes(opt.value))
})

const maskingPatternOptions = computed(() => [
  { value: 'GENERIC', text: t('masking_pattern_generic') },
  { value: 'CARD', text: t('masking_pattern_card') },
  { value: 'RRN', text: t('masking_pattern_rrn') },
  { value: 'PHONE', text: t('masking_pattern_phone') },
  { value: 'EMAIL', text: t('masking_pattern_email') }
])

const domainOptions = computed(() => {
  return treeNodes.value.filter(n => n.isDomain).map(d => ({
    value: d.id,
    text: d.label || d.id
  }))
})

const availableClassificationNodes = computed(() => {
  const options = []
  const flatten = (nodes, prefix = '') => {
    if (!nodes || !Array.isArray(nodes)) return
    for (const node of nodes) {
      if (node.isDomain && node.id) {
        const dName = node.label || node.name || 'Domain'
        options.push({
          text: `🌐 ${t('domain_bracket')} ${dName} (${t('domain_level')})`,
          value: `domain_${node.id}`,
          isDomain: true,
          domainId: node.id
        })
      } else if (node.id) {
        options.push({
          text: `${prefix}${node.label || node.name || 'Node'}`,
          value: node.id,
          isDomain: false
        })
      }
      if (node.children && node.children.length) {
        const nextPrefix = node.isDomain ? '' : `${prefix}${node.label || node.name} > `
        flatten(node.children, nextPrefix)
      }
    }
  }
  flatten(treeNodes.value)
  return options
})

const onTargetNodeSelected = (val) => {
  if (typeof val === 'string' && val.startsWith('domain_')) {
    newField.value.isDomainField = true
  } else {
    newField.value.isDomainField = false
  }
}

const onIsDomainFieldChecked = (val) => {
  if (val) {
    const currentDomainId = selectedNode.value?.isDomain ? selectedNode.value?.id : selectedNode.value?.domainId
    if (currentDomainId) {
      newField.value.targetNodeId = `domain_${currentDomainId}`
    }
  } else {
    if (typeof newField.value.targetNodeId === 'string' && newField.value.targetNodeId.startsWith('domain_')) {
      const firstNode = availableClassificationNodes.value.find(n => !n.isDomain)
      newField.value.targetNodeId = firstNode ? firstNode.value : null
    }
  }
}

const columnDefs = computed(() => [
  { 
    headerName: 'Status', 
    field: 'approvalStatus', 
    sortable: true,
    width: 150,
    cellRenderer: (params) => {
      if (!params || !params.data) return '';
      const isPending = pendingFieldIds.value.includes(params.data.id) || params.data.approvalStatus === 'PENDING_APPROVAL' || params.data.isPendingApproval;
      const statusText = isPending ? 'PENDING_APPROVAL' : (params.data.approvalStatus || 'ACTIVE');
      const color = isPending ? '#e6a23c' : (statusText === 'ACTIVE' ? '#2c82e0' : '#f56c6c');
      
      const span = document.createElement('span');
      span.style.padding = '2px 8px';
      span.style.borderRadius = '4px';
      span.style.background = color;
      span.style.color = 'white';
      span.style.fontSize = '12px';
      span.style.fontWeight = 'bold';
      span.innerText = statusText;
      return span;
    }
  },
  { 
    headerName: 'Name', 
    field: 'name', 
    sortable: true,
    flex: 1,
    valueGetter: (params) => {
      if (!params || !params.data) return '';
      const pName = typeof params.data.name === 'string' ? JSON.parse(params.data.name || '{}') : params.data.name;
      return pName?.[currentLocale.value] || pName?.ko || pName?.en || 'Unknown';
    }
  },
  { 
    headerName: 'Sector', 
    field: 'fieldGroup', 
    sortable: true,
    width: 150,
    valueGetter: (params) => {
      if (!params.data || !params.data.fieldGroup || !params.data.fieldGroup.sector) return '';
      const sName = params.data.fieldGroup.sector.name;
      return sName?.[currentLocale.value] || sName?.ko || sName?.en || '';
    }
  },
  { 
    headerName: 'Group', 
    field: 'fieldGroup', 
    sortable: true,
    width: 150,
    valueGetter: (params) => {
      if (!params.data || !params.data.fieldGroup) return '';
      const gName = params.data.fieldGroup.name;
      return gName?.[currentLocale.value] || gName?.ko || gName?.en || '';
    }
  },
  { headerName: 'Key', field: 'key', sortable: true, flex: 1 },
  { headerName: 'Order', field: 'order', sortable: true, width: 90 },
  { headerName: 'Grid Width', field: 'gridWidth', sortable: true, width: 120 },
  { headerName: 'AG-Grid Width', field: 'tableColumnWidth', sortable: true, width: 130 },
  { headerName: 'Type', field: 'type', sortable: true, width: 150 },
  { 
    headerName: 'Required', 
    field: 'required', 
    sortable: true,
    width: 110,
    cellRenderer: (params) => {
      if (!params || params.value === undefined) return '';
      const span = document.createElement('span');
      span.style.padding = '2px 6px';
      span.style.borderRadius = '4px';
      span.style.color = 'white';
      span.style.fontSize = '12px';
      span.style.fontWeight = 'bold';
      if (params.value) {
        span.style.background = '#e42222';
        span.innerText = 'Yes';
      } else {
        span.style.background = '#2c82e0';
        span.innerText = 'No';
      }
      return span;
    }
  },
  { 
    headerName: 'Actions', 
    field: 'id',
    width: 190,
    minWidth: 190,
    cellRenderer: (params) => {
      if (!params || !params.data) return '';
      const container = document.createElement('div');
      container.style.display = 'flex';
      container.style.gap = '8px';
      container.style.alignItems = 'center';
      container.style.height = '100%';

      const isPending = pendingFieldIds.value.includes(params.data.id) || params.data.approvalStatus === 'PENDING_APPROVAL' || params.data.isPendingApproval;

      if (isPending) {
        const lockBtn = document.createElement('span');
        lockBtn.style.cursor = 'pointer';
        lockBtn.style.color = '#e6a23c';
        lockBtn.style.fontWeight = '600';
        lockBtn.style.fontSize = '12px';
        lockBtn.style.lineHeight = '1';
        lockBtn.style.padding = '0 8px';
        lockBtn.style.height = '24px';
        lockBtn.style.borderRadius = '4px';
        lockBtn.style.border = '1px solid #e6a23c';
        lockBtn.style.display = 'inline-flex';
        lockBtn.style.alignItems = 'center';
        lockBtn.style.justifyContent = 'center';
        lockBtn.style.boxSizing = 'border-box';
        lockBtn.style.gap = '2px';
        lockBtn.title = '클릭하여 결재 진행 및 상세 이력 확인';
        lockBtn.innerText = '🔍 결재 이력';
        lockBtn.addEventListener('click', () => {
          if (params.data.approvalRequestId) {
            openApprovalViewer(params.data.approvalRequestId);
          } else {
            navigateTo('/approvals');
          }
        });
        container.appendChild(lockBtn);
      } else if (params.data.domainId && !selectedNode.value.isDomain) {
        const span = document.createElement('span');
        span.style.color = '#666';
        span.style.fontStyle = 'italic';
        span.innerText = 'Inherited';
        container.appendChild(span);
      } else {
        const editBtn = document.createElement('span');
        editBtn.style.cursor = 'pointer';
        editBtn.style.color = '#2c82e0';
        editBtn.style.fontWeight = '600';
        editBtn.style.fontSize = '12px';
        editBtn.style.lineHeight = '1';
        editBtn.style.padding = '0 8px';
        editBtn.style.height = '24px';
        editBtn.style.borderRadius = '4px';
        editBtn.style.border = '1px solid #2c82e0';
        editBtn.style.display = 'inline-flex';
        editBtn.style.alignItems = 'center';
        editBtn.style.justifyContent = 'center';
        editBtn.style.boxSizing = 'border-box';
        editBtn.innerText = 'Edit';
        editBtn.addEventListener('click', () => openFieldModal(params.data));
        container.appendChild(editBtn);

        const deleteBtn = document.createElement('span');
        deleteBtn.style.cursor = 'pointer';
        deleteBtn.style.color = '#f56c6c';
        deleteBtn.style.fontWeight = '600';
        deleteBtn.style.fontSize = '12px';
        deleteBtn.style.lineHeight = '1';
        deleteBtn.style.padding = '0 8px';
        deleteBtn.style.height = '24px';
        deleteBtn.style.borderRadius = '4px';
        deleteBtn.style.border = '1px solid #f56c6c';
        deleteBtn.style.display = 'inline-flex';
        deleteBtn.style.alignItems = 'center';
        deleteBtn.style.justifyContent = 'center';
        deleteBtn.style.boxSizing = 'border-box';
        deleteBtn.innerText = 'Delete';
        deleteBtn.addEventListener('click', () => triggerDeleteFieldWithImpactAnalysis(params.data));
        container.appendChild(deleteBtn);
      }

      // DQ Rules button — always visible
      const dqBtn = document.createElement('span');
      dqBtn.style.cursor = 'pointer';
      dqBtn.style.color = '#e88b24';
      dqBtn.style.fontWeight = '600';
      dqBtn.style.fontSize = '12px';
      dqBtn.style.lineHeight = '1';
      dqBtn.style.padding = '0 8px';
      dqBtn.style.height = '24px';
      dqBtn.style.borderRadius = '4px';
      dqBtn.style.border = '1px solid #e88b24';
      dqBtn.style.display = 'inline-flex';
      dqBtn.style.alignItems = 'center';
      dqBtn.style.justifyContent = 'center';
      dqBtn.style.boxSizing = 'border-box';
      dqBtn.innerText = 'DQ';
      dqBtn.addEventListener('click', () => openDqRuleEditor(params.data));
      container.appendChild(dqBtn);

      return container;
    }
  }
])

const onRowDoubleClicked = (params) => {
  if (params.data.domainId && !selectedNode.value.isDomain) return;
  
  const isPending = pendingFieldIds.value.includes(params.data.id) || params.data.approvalStatus === 'PENDING_APPROVAL' || params.data.isPendingApproval;
  if (isPending) return;

  openFieldModal(params.data)
}

const { saveState: saveSchemaGridState, restoreState: restoreSchemaGridState } = useGridState('schema_fields_grid_v2')

const onGridReady = (params) => {
  fieldsGridApi.value = params.api
  gridApi.value = params.api
  restoreSchemaGridState(params.api)
  if (params.api && params.api.moveColumn) {
    try {
      params.api.moveColumn('approvalStatus', 0)
    } catch (ignored) {}
  }
  fetchFields()
}

const onColumnChanged = () => {
  if (fieldsGridApi.value) {
    saveSchemaGridState(fieldsGridApi.value)
  }
}

const treeRef = ref(null)

const onTreeLoaded = (nodes) => {
  treeNodes.value = nodes
}

const loadTree = async () => {
  if (treeRef.value) {
    await treeRef.value.loadTree()
  }
}

onMounted(async () => {
  if (userCookie.value?.role === 'USER') {
    useRouter().push('/')
    return
  }
  loadTree()
  await codeStore.loadGroup('FIELD_TYPE')
  try {
    
    unitOptions.value = [
      'kg', 'g', 'mg', 't', 'lb', 'oz', 
      'm', 'cm', 'mm', 'km', 'in', 'ft', 'yd', 'mi', 
      'm²', 'cm²', 'km²', 'ha', 
      'L', 'mL', 'm³', 'cm³', 
      's', 'min', 'h', 'd', 
      '℃', '℉', 'K', 
      'Pa', 'kPa', 'MPa', 'bar', 'atm', 'psi', 
      'N', 'kN', 'J', 'kJ', 'cal', 'kcal', 
      'W', 'kW', 'MW', 
      'V', 'A', 'Ω', 'Hz', 'kHz', 'MHz', 'GHz', 
      'B', 'KB', 'MB', 'GB', 'TB', 
      '%', '‰', 'ppm', 
      '원', '$', '€', '¥', '£', 
      'EA', 'SET', 'BOX', 'ROLL', 'SHEET', 'PCS'
    ]
  } catch (e) {
    console.error('Failed to load metadata:', e)
  }
})


const addStep = (action) => {
  workflowConfigs.value[action].steps.push({
    id: Date.now(),
    users: [{ stepType: 'APPROVAL', assigneeId: null }]
  })
}

const removeStep = (action, sIdx) => {
  workflowConfigs.value[action].steps.splice(sIdx, 1)
}

const moveStepUp = (action, sIdx) => {
  if (sIdx > 0) {
    const arr = workflowConfigs.value[action].steps
    const temp = arr[sIdx]
    arr[sIdx] = arr[sIdx - 1]
    arr[sIdx - 1] = temp
  }
}

const moveStepDown = (action, sIdx) => {
  const arr = workflowConfigs.value[action].steps
  if (sIdx < arr.length - 1) {
    const temp = arr[sIdx]
    arr[sIdx] = arr[sIdx + 1]
    arr[sIdx + 1] = temp
  }
}

const addUserToStep = (action, sIdx) => {
  workflowConfigs.value[action].steps[sIdx].users.push({ stepType: 'CONSENSUS', assigneeId: null })
}

const removeUserFromStep = (action, sIdx, uIdx) => {
  workflowConfigs.value[action].steps[sIdx].users.splice(uIdx, 1)
}

const saveWorkflowConfigs = async () => {
  if (!selectedNode.value) return
  isSavingWorkflows.value = true
  try {
    const payloads = Object.keys(workflowConfigs.value).map(action => {
      const conf = workflowConfigs.value[action]
      const flatSteps = []
      conf.steps.forEach((step, sIdx) => {
        step.users.forEach(u => {
          const hasAssignee = u.assigneeType === 'ROLE' ? !!u.assigneeRole : !!u.assigneeId
          if (hasAssignee && u.stepType) {
            flatSteps.push({
              stepType: u.stepType,
              assigneeType: u.assigneeType || (u.assigneeRole ? 'ROLE' : 'USER'),
              assigneeId: u.assigneeType === 'ROLE' ? null : (u.assigneeId || null),
              assigneeRole: u.assigneeType === 'ROLE' ? (u.assigneeRole || null) : null,
              stepOrder: sIdx + 1
            })
          }
        })
      })
      
      const stepsConfig = JSON.stringify({
          steps: flatSteps,
          approvalLine: flatSteps,
          observerIds: conf.observerIds || []
      })
      
      return {
        actionType: action,
        stepsConfig: stepsConfig
      }
    })
    
    const url = selectedNode.value.isDomain 
      ? `/api/workflow-configs/domain/${selectedNode.value.id}`
      : `/api/workflow-configs/node/${selectedNode.value.id}`
      
    await customFetch(url, {
      method: 'POST',
      body: payloads
    })
    showCustomAlert('Workflow configurations saved successfully.', 'Save Success', 'Notification', 'success')
  } catch (e) {
    console.error('Failed to save workflows', e)
    showCustomAlert('Failed to save workflows.', 'Save Failed', 'Error', 'error')
  } finally {
    isSavingWorkflows.value = false
  }
}

watch(currentLocale, () => {
  if (gridApi.value) {
    gridApi.value.refreshCells({ force: true })
  }
})

const onNodeSelected = async (nodes) => {
  const node = Array.isArray(nodes) ? nodes[0] : nodes
  selectedNode.value = node || null
  
  if (!node) {
    fields.value = []
    domainSectors.value = []
    domainGroups.value = []
    return
  }
  
  const dId = node.domainId
  try {
    const wfUrl = node.isDomain ? `/api/workflow-configs/domain/${node.id}` : `/api/workflow-configs/node/${node.id}`
    const fieldUrl = node.isDomain ? `/api/domains/${node.id}/fields` : `/api/nodes/${node.id}/fields/effective`
    const [sData, gData, wfData, fData] = await Promise.all([
      customFetch(`/api/domains/${dId}/sectors`),
      customFetch(`/api/domains/${dId}/groups`),
      customFetch(wfUrl).catch(() => []),
      customFetch(fieldUrl).catch(() => [])
    ])
    domainSectors.value = sData
    domainGroups.value = gData
    fields.value = fData || []
    
    workflowConfigs.value = {
      CREATE: { steps: [], observerIds: [] },
      UPDATE: { steps: [], observerIds: [] },
      DELETE: { steps: [], observerIds: [] }
    }
    for (const wf of wfData) {
      if (workflowConfigs.value[wf.actionType]) {
         try {
             const parsed = wf.stepsConfig ? JSON.parse(wf.stepsConfig) : { steps: [], observerIds: [] }
             
             // Convert flat steps back into grouped UI steps
             const flatSteps = parsed.steps || parsed.approvalLine || []
             const grouped = []
             let currentOrder = -1
             let currentStep = null
             
             for (const fs of flatSteps) {
                 if (fs.stepOrder !== currentOrder) {
                     currentOrder = fs.stepOrder
                     currentStep = { id: Date.now() + Math.random(), users: [] }
                     grouped.push(currentStep)
                 }
                 currentStep.users.push({
                     stepType: fs.stepType,
                     assigneeType: fs.assigneeType || (fs.assigneeRole ? 'ROLE' : 'USER'),
                     assigneeId: fs.assigneeId || null,
                     assigneeRole: fs.assigneeRole || null
                 })
             }
             
             workflowConfigs.value[wf.actionType].steps = grouped
             workflowConfigs.value[wf.actionType].observerIds = parsed.observerIds || []
         } catch(e) {
             console.error('Failed to parse stepsConfig', e)
         }
      }
    } // end for wf
  } catch(e) {
    console.error('Failed to load node data', e)
  }
  
  fetchFields();
  await checkPendingSchemaStatus();
}

const refreshSchemaData = async () => {
  if (fieldsGridApi.value) {
    fieldsGridApi.value.refreshInfiniteCache()
    fieldsGridApi.value.purgeInfiniteCache()
  } else {
    fetchFields()
  }
  await checkPendingSchemaStatus()
}

const hasPendingSchemaApproval = ref(false)
const pendingFieldIds = ref([])

const isCurrentFieldPendingApproval = computed(() => {
  if (!isEditMode.value || !editingId.value) return false
  return pendingFieldIds.value.includes(editingId.value)
})

const checkPendingSchemaStatus = async () => {
  if (!selectedNode.value) {
    hasPendingSchemaApproval.value = false;
    pendingFieldIds.value = [];
    return;
  }
  const domainId = selectedNode.value.isDomain ? selectedNode.value.id : selectedNode.value.domainId;
  const nodeId = selectedNode.value.isDomain ? null : selectedNode.value.id;
  try {
    const res = await customFetch(`/api/approval-requests/pending-schema-status?domainId=${domainId || ''}&nodeId=${nodeId || ''}`);
    hasPendingSchemaApproval.value = Boolean(res?.hasPendingApproval);
    pendingFieldIds.value = Array.isArray(res?.pendingFieldIds) ? res.pendingFieldIds : [];
  } catch (e) {
    hasPendingSchemaApproval.value = false;
    pendingFieldIds.value = [];
  }
}

const handleNodeEdit = async (node) => {
  const targetNode = node || selectedNode.value
  if (!targetNode) return
  isEditMode.value = true
  if (targetNode.isDomain) {
    try {
      const dFields = await customFetch(`/api/domains/${targetNode.id}/fields`)
      domainFieldOptions.value = dFields.map(f => {
        const pName = typeof f.name === 'string' ? JSON.parse(f.name || '{}') : (f.name || {})
        return {
          value: f.id,
          text: pName[currentLocale.value] || pName.ko || pName.en || f.key || 'Unknown',
          type: f.type
        }
      })
    } catch (e) {
      domainFieldOptions.value = []
    }
    const rawDomain = targetNode.originalData || {}
    const pDesc = typeof rawDomain.description === 'string' ? JSON.parse(rawDomain.description || '{}') : (rawDomain.description || {})
    newDomain.value = { 
      ...targetNode, 
      name: { ...(targetNode.originalNameMap || {ko:'', en:''}) },
      description: { ko: pDesc.ko || '', en: pDesc.en || '' },
      identifierFieldId: rawDomain.identifierFieldId || null,
      displayNameFieldId: rawDomain.displayNameFieldId || null,
      descriptionFieldId: rawDomain.descriptionFieldId || null,
      imageFieldId: rawDomain.imageFieldId || null,
      icon: rawDomain.icon || '',
      sortOrder: rawDomain.sortOrder || 0,
      numberingPattern: rawDomain.numberingPattern || '',
      autoDqScanEnabled: rawDomain.autoDqScanEnabled || false
    }
    showDomainModal.value = true
  } else {
    const rawNode = targetNode.originalData || {}
    newNode.value = { 
      ...targetNode, 
      name: { ...(targetNode.originalNameMap || {ko:'', en:''}) },
      order: rawNode.order || 0,
      icon: rawNode.icon || ''
    }
    showNodeModal.value = true
  }
}

const openDomainModal = () => {
  isEditMode.value = false
  newDomain.value = { name: {ko:'', en:''}, description: {ko:'', en:''}, identifierFieldId: null, displayNameFieldId: null, descriptionFieldId: null, imageFieldId: null, icon: '', sortOrder: 0, numberingPattern: '', autoDqScanEnabled: false }
  showDomainModal.value = true
}

const openNodeModal = () => {
  if (!selectedNode.value) return
  isEditMode.value = false
  newNode.value = { name: {ko:'', en:''}, order: 0, icon: '' }
  showNodeModal.value = true
}

const getTranslatedName = (nameObj) => {
  if (!nameObj) return ''
  if (typeof nameObj === 'string') return nameObj
  return nameObj[currentLocale.value] || nameObj['ko'] || nameObj['en'] || Object.values(nameObj)[0] || ''
}

const availableConditionFields = computed(() => {
  return (fields.value || [])
    .filter(f => !isEditMode.value || f.id !== editingId.value)
    .map(f => ({
      value: f.key,
      text: `${getTranslatedName(f.name)} (${f.key})`
    }))
})

const resetConditionFields = () => {
  newField.value.conditionEnabled = false
  newField.value.conditionMode = 'GUI'
  newField.value.conditionAction = ['SHOW']
  newField.value.dependsOnFieldKey = ''
  newField.value.conditionOperator = 'EQUALS'
  newField.value.conditionValue = ''
  newField.value.conditionExpression = ''
}

const openFieldModal = async (rowData = null) => {
  await checkPendingSchemaStatus()
  if (selectedNode.value && (!fields.value || fields.value.length === 0)) {
    try {
      const fieldUrl = selectedNode.value.isDomain
        ? `/api/domains/${selectedNode.value.id}/fields`
        : `/api/nodes/${selectedNode.value.id}/fields/effective`
      fields.value = await customFetch(fieldUrl).catch(() => [])
    } catch (e) {
      console.error('Failed to load fields for modal:', e)
    }
  }

  if (rowData) {
    isEditMode.value = true
    editingId.value = rowData.id
    const isDomain = !rowData.definedAtNode && (!!rowData.domain || selectedNode.value?.isDomain)
    const domainId = rowData.domain?.id || (selectedNode.value?.isDomain ? selectedNode.value?.id : selectedNode.value?.domainId)
    const initialTargetId = isDomain ? (domainId ? `domain_${domainId}` : null) : (rowData.definedAtNode?.id || selectedNode.value?.id)

    let fType = (rowData.type === 'STRING' || !rowData.type) ? 'TEXT' : rowData.type
    let isMulti = !!rowData.isMultiValue
    if (fType === 'ENUM') {
      fType = 'SELECT'
    } else if (fType === 'MULTI_SELECT') {
      fType = 'SELECT'
      isMulti = true
    } else if (['DECIMAL', 'FLOAT', 'INTEGER'].includes(fType)) {
      fType = 'NUMBER'
    } else if (fType === 'CHECKBOX') {
      fType = 'BOOLEAN'
    }

    newField.value = { 
      ...rowData, 
      name: { ...rowData.name }, 
      hint: rowData.hint ? { ...rowData.hint } : { ko: '', en: '' }, 
      type: fType,
      isMultiValue: isMulti,
      formula: rowData.formula || '', 
      unit: rowData.unit || '',
      fieldGroupId: rowData.fieldGroup?.id || null,
      targetNodeId: initialTargetId,
      isDomainField: isDomain,
      gridWidth: rowData.gridWidth || null,
      tableColumnWidth: rowData.tableColumnWidth || null
    }
    if (['SELECT', 'MULTI_SELECT', 'ENUM'].includes(rowData.type)) {
      try {
        newFieldOptionsList.value = JSON.parse(rowData.options || '[]')
      } catch (e) { newFieldOptionsList.value = [] }
    } else {
      newFieldOptionsList.value = []
    }
    // Parse targetDomainId, formula, conditionRule, tableSchema from options
    if (rowData.options) {
      try {
        const opts = typeof rowData.options === 'string' ? JSON.parse(rowData.options) : rowData.options
        if (opts.targetDomainId) newField.value.targetDomainId = opts.targetDomainId
        if (opts.formula) newField.value.formula = opts.formula
        if (opts.dateFormat) newField.value.dateFormat = opts.dateFormat
        if (opts.tableSchema && Array.isArray(opts.tableSchema.columns)) {
          newFieldTableColumns.value = opts.tableSchema.columns.map(c => ({
            key: c.key || '',
            name: { ko: c.name?.ko || c.name || '', en: c.name?.en || '' },
            type: c.type || 'TEXT',
            required: !!c.required,
            width: c.width || 150,
            optionsStr: formatColOptionsToStr(c.options || c.optionsStr)
          }))
        } else {
          newFieldTableColumns.value = []
        }
        if (opts.conditionRule) {
          const cond = opts.conditionRule
          newField.value.conditionEnabled = cond.enabled !== false
          newField.value.conditionMode = cond.expression ? 'EXPRESSION' : 'GUI'
          let acts = ['SHOW']
          if (cond.action) {
            acts = Array.isArray(cond.action) ? cond.action : [cond.action]
          }
          newField.value.conditionAction = acts
          newField.value.dependsOnFieldKey = cond.dependsOnFieldKey || ''
          newField.value.conditionOperator = cond.operator || 'EQUALS'
          newField.value.conditionValue = cond.value || ''
          newField.value.conditionExpression = cond.expression || ''
        } else {
          resetConditionFields()
        }
      } catch (e) { 
        resetConditionFields()
        newFieldTableColumns.value = []
      }
    } else {
      resetConditionFields()
      newFieldTableColumns.value = []
    }
  } else {
    isEditMode.value = false
    editingId.value = null
    const isDomain = !!selectedNode.value?.isDomain
    const currentDomainId = selectedNode.value?.isDomain ? selectedNode.value?.id : selectedNode.value?.domainId
    const initialTargetId = isDomain ? (currentDomainId ? `domain_${currentDomainId}` : null) : selectedNode.value?.id

    newField.value = { 
      name: {ko:'', en:''}, hint: {ko:'', en:''}, key: '', type: 'TEXT', required: false, order: 0, 
      fieldGroupId: null, targetDomainId: null, isMultiValue: false, isSearchable: true, 
      isEncrypted: false, isReadOnly: false, isImmutable: false, isHidden: false, isHighlighted: false, 
      formula: '', unit: '', gridWidth: null, tableColumnWidth: null, dateFormat: '',
      targetNodeId: initialTargetId,
      isDomainField: isDomain
    }
    resetConditionFields()
    newFieldOptionsList.value = []
    newFieldTableColumns.value = []
  }
  showFieldModal.value = true
}
const saveDomain = async () => {
  try {
    const url = isEditMode.value ? `/api/domains/${newDomain.value.id}` : `/api/domains`
    const extractId = (val) => {
      if (!val) return null
      if (typeof val === 'string') return val
      if (typeof val === 'object' && val.value) return val.value
      return null
    }

    const payload = {
      name: { 
        ko: newDomain.value.name?.ko || '', 
        en: newDomain.value.name?.en || '' 
      },
      description: { 
        ko: newDomain.value.description?.ko || '', 
        en: newDomain.value.description?.en || '' 
      },
      identifierFieldId: extractId(newDomain.value.identifierFieldId),
      displayNameFieldId: extractId(newDomain.value.displayNameFieldId),
      descriptionFieldId: extractId(newDomain.value.descriptionFieldId),
      imageFieldId: extractId(newDomain.value.imageFieldId),
      icon: newDomain.value.icon || '',
      sortOrder: parseInt(newDomain.value.sortOrder) || 0,
      numberingPattern: newDomain.value.numberingPattern || '',
      autoDqScanEnabled: Boolean(newDomain.value.autoDqScanEnabled)
    }

    await customFetch(url, {
      method: isEditMode.value ? 'PUT' : 'POST',
      body: payload
    })
    showDomainModal.value = false
    await useDomain().fetchDomains(true)
    await loadTree()
  } catch (e) {
    const msg = e.response?._data?.message || e.message || 'Unknown error'
    console.error('Domain Save Error Details:', e.response?._data)
    showCustomAlert('Domain Save Error: ' + msg, 'Error', 'Error', 'error')
  }
}

const saveNode = async () => {
  if (!selectedNode.value) return
  
  try {
    const domainId = selectedNode.value.isDomain ? selectedNode.value.id : selectedNode.value.domainId
    const targetId = isEditMode.value ? newNode.value.id : domainId
    const url = isEditMode.value ? `/api/domains/${newNode.value.domainId}/nodes/${targetId}` : `/api/domains/${domainId}/nodes`
    const parentId = isEditMode.value ? undefined : (selectedNode.value.isDomain ? null : selectedNode.value.id)

    await customFetch(url, {
      method: isEditMode.value ? 'PUT' : 'POST',
      body: {
        name: newNode.value.name,
        order: newNode.value.order,
        icon: newNode.value.icon,
        parentId: parentId
      }
    })
    showNodeModal.value = false
    await loadTree()
  } catch (e) {
    showCustomAlert('Error saving node', 'Save Error', 'Error', 'error')
  }
}

const handleNodeDelete = async (node) => {
  const target = node || selectedNode.value
  if (!target || target.isDomain) return

  const nodeName = target.label || (target.originalNameMap ? (target.originalNameMap[currentLocale.value] || target.originalNameMap.ko || target.originalNameMap.en) : 'Node')
  const confirmMsg = t('confirm_delete_node', { name: nodeName })
  if (!confirm(confirmMsg)) return

  try {
    const domainId = target.domainId || selectedDomainId.value
    await customFetch(`/api/domains/${domainId}/nodes/${target.id}`, {
      method: 'DELETE'
    })
    try {
      toast.init({
        message: t('node_deleted_success'),
        color: 'success'
      })
    } catch (ignored) {}
    showNodeModal.value = false
    if (selectedNode.value?.id === target.id) {
      selectedNode.value = null
    }
    await loadTree()
  } catch (e) {
    showCustomAlert(e.message || t('node_delete_failed'), 'Delete Error', 'Error', 'error')
  }
}

const saveField = async () => {
  const duplicate = fields.value.find(f => f.key === newField.value.key && (!isEditMode.value || f.id !== editingId.value))
  if (duplicate) {
    showCustomAlert(t('field_key_already_exists_newfield_value_key'), 'Key Duplicate', 'Warning', 'warning')
    return
  }
  
  let existingOptsObj = {}
  if (['SELECT', 'MULTI_SELECT', 'ENUM'].includes(newField.value.type)) {
    const hasEmptyKey = newFieldOptionsList.value.some(opt => !opt.key || String(opt.key).trim() === '')
    if (hasEmptyKey) {
      showCustomAlert(t('enter_key_all_options'), 'Input Missing', 'Warning', 'warning')
      return
    }
    existingOptsObj = { optionsList: newFieldOptionsList.value }
  } else if (newField.value.type === 'DOMAIN_REFERENCE') {
    if (!newField.value.targetDomainId) {
      showCustomAlert(t('please_select_a_target_domain'), 'Domain Missing', 'Warning', 'warning')
      return
    }
    existingOptsObj = { targetDomainId: newField.value.targetDomainId }
  } else if (newField.value.type === 'CALCULATED') {
    if (!newField.value.formula || String(newField.value.formula).trim() === '') {
      showCustomAlert(t('enter_formula'), 'Formula Missing', 'Warning', 'warning')
      return
    }
    try {
      const testFormula = newField.value.formula.replace(/\${[^}]+}/g, '1')
      const ROUND = (val, dec=0) => Number(Math.round(val+'e'+dec)+'e-'+dec);
      const fn = new Function('ROUND', 'ABS', 'CEIL', 'FLOOR', `return ${testFormula};`)
      fn(ROUND, Math.abs, Math.ceil, Math.floor)
    } catch (e) {
      showCustomAlert(t('syntax_error_in_formula_e_message'), 'Formula Syntax Error', 'Error', 'error')
      return
    }
    existingOptsObj = { formula: newField.value.formula.trim() }
  } else if (newField.value.type === 'DATE') {
    existingOptsObj = { dateFormat: newField.value.dateFormat || 'YYYY-MM-DD' }
  } else if (newField.value.type === 'JSON') {
    if (newFieldTableColumns.value && newFieldTableColumns.value.length > 0) {
      const hasEmptyColKey = newFieldTableColumns.value.some(col => !col.key || String(col.key).trim() === '')
      if (hasEmptyColKey) {
        showCustomAlert(t('enter_key_all_options', '모든 컬럼의 식별 키(Key)를 입력해주세요.'), 'Input Missing', 'Warning', 'warning')
        return
      }
      existingOptsObj = {
        tableSchema: {
          columns: newFieldTableColumns.value.map(col => ({
            key: String(col.key || '').trim(),
            name: {
              ko: col.name?.ko || col.key || '',
              en: col.name?.en || col.name?.ko || col.key || ''
            },
            type: col.type || 'TEXT',
            required: !!col.required,
            width: Number(col.width) || 150,
            options: col.type === 'SELECT' ? parseColOptionsStr(col.optionsStr) : []
          }))
        }
      }
    }
  } else if (newField.value.options) {
    try {
      existingOptsObj = typeof newField.value.options === 'string' ? JSON.parse(newField.value.options) : newField.value.options
    } catch(e){}
  }

  if (typeof existingOptsObj !== 'object' || Array.isArray(existingOptsObj)) {
    existingOptsObj = { rawOptions: existingOptsObj }
  }

  if (newField.value.conditionEnabled) {
    let acts = newField.value.conditionAction || ['SHOW']
    if (!Array.isArray(acts)) acts = [acts]
    existingOptsObj.conditionRule = {
      enabled: true,
      action: acts,
      expression: newField.value.conditionMode === 'EXPRESSION' ? newField.value.conditionExpression : '',
      dependsOnFieldKey: newField.value.conditionMode === 'GUI' ? newField.value.dependsOnFieldKey : '',
      operator: newField.value.conditionMode === 'GUI' ? newField.value.conditionOperator : '',
      value: newField.value.conditionMode === 'GUI' ? newField.value.conditionValue : ''
    }
  } else {
    delete existingOptsObj.conditionRule
  }

  newField.value.options = JSON.stringify(existingOptsObj)
  
  triggerSaveFieldWithImpactAnalysis()
}

const executePendingFieldSave = async () => {
  newField.value.reason = draftFieldCommentText.value
  try {
    let url = ''
    if (selectedNode.value.isDomain) {
      url = isEditMode.value
        ? `/api/domains/${selectedNode.value.id}/fields/${editingId.value}`
        : `/api/domains/${selectedNode.value.id}/fields`
    } else {
      url = isEditMode.value
        ? `/api/nodes/${selectedNode.value.id}/fields/${editingId.value}`
        : `/api/nodes/${selectedNode.value.id}/fields`
    }
    const method = isEditMode.value ? 'PUT' : 'POST'

    const isDomainTarget = typeof newField.value.targetNodeId === 'string' && newField.value.targetNodeId.startsWith('domain_')
    const finalIsDomainField = Boolean(newField.value.isDomainField || isDomainTarget)
    const finalTargetNodeId = finalIsDomainField ? null : newField.value.targetNodeId

    const payload = {
      name: newField.value.name,
      hint: newField.value.hint,
      fieldGroupId: newField.value.fieldGroupId || newField.value.fieldGroup?.id || null,
      targetNodeId: finalTargetNodeId,
      isDomainField: finalIsDomainField,
      key: newField.value.key,
      type: newField.value.type,
      unit: newField.value.unit,
      options: newField.value.options,
      required: newField.value.required,
      defaultValue: newField.value.defaultValue,
      order: newField.value.order,
      gridWidth: newField.value.gridWidth ? Number(newField.value.gridWidth) : null,
      tableColumnWidth: newField.value.tableColumnWidth ? Number(newField.value.tableColumnWidth) : null,
      isMultiValue: newField.value.isMultiValue,
      isTable: newField.value.isTable,
      isEncrypted: Boolean(newField.value.isEncrypted),
      maskingPattern: newField.value.isEncrypted ? (newField.value.maskingPattern || 'GENERIC') : (newField.value.maskingPattern || null),
      isSearchable: newField.value.isSearchable,
      isReadOnly: newField.value.isReadOnly,
      isImmutable: newField.value.isImmutable,
      isHidden: newField.value.isHidden,
      isHighlighted: newField.value.isHighlighted,
      reason: newField.value.reason || ''
    }

    await customFetch(url, {
      method,
      body: payload
    })
    newField.value = { 
      name: { ko: '', en: '' }, 
      hint: { ko: '', en: '' },
      fieldGroupId: null,
      key: '', type: 'TEXT', options: '', required: false, isMultiValue: false, isSearchable: true, isEncrypted: false, isReadOnly: false, isImmutable: false, isHidden: false, isHighlighted: false, order: 0, reason: ''
    }
    showFieldModal.value = false
    showFieldCommentModal.value = false
    await onNodeSelected(selectedNode.value)
  } catch (error) {
    showCustomAlert('Error saving field', 'Save Error', 'Error', 'error')
  }
}

const openSectorGroupModal = async () => {
  if (!selectedNode.value && treeNodes.value && treeNodes.value.length > 0) {
    await onNodeSelected(treeNodes.value[0])
  }
  showSectorGroupModal.value = true
}

const addSectorRow = () => {
  domainSectors.value = [...domainSectors.value, { name: { ko: '', en: '' }, sortOrder: 0 }]
}

const addGroupRow = () => {
  domainGroups.value = [...domainGroups.value, { sector: { id: null }, name: { ko: '', en: '' }, sortOrder: 0, isDefaultOpen: true }]
}

const saveAllSectors = async () => {
  if (sectorGridApi.value) {
    sectorGridApi.value.stopEditing(false)
  }
  const dId = selectedNode.value?.domainId
  if (!dId) return

  let hasError = false
  let saveCount = 0

  for (const item of domainSectors.value) {
    const hasName = typeof item.name === 'object' ? (item.name.ko || item.name.en) : item.name
    if (!hasName) continue

    const payload = {
      name: typeof item.name === 'string' ? { ko: item.name } : item.name,
      sortOrder: item.sortOrder || 0
    }

    try {
      if (item.id) {
        await customFetch(`/api/domains/${dId}/sectors/${item.id}`, { method: 'PUT', body: payload })
      } else {
        await customFetch(`/api/domains/${dId}/sectors`, { method: 'POST', body: payload })
      }
      saveCount++
    } catch (e) {
      console.error('Failed to save sector', e)
      hasError = true
    }
  }

  domainSectors.value = await customFetch(`/api/domains/${dId}/sectors`)
  if (!hasError) {
    showCustomAlert(`섹터 ${saveCount}건이 성공적으로 저장되었습니다.`, '저장 완료', 'Notification', 'success')
  } else {
    showCustomAlert('일부 섹터 저장 중 오류가 발생했습니다.', '저장 오류', 'Error', 'error')
  }
}

const saveAllGroups = async () => {
  if (groupGridApi.value) {
    groupGridApi.value.stopEditing(false)
  }
  const dId = selectedNode.value?.domainId
  if (!dId) return

  let hasError = false
  let saveCount = 0

  for (const item of domainGroups.value) {
    const hasName = typeof item.name === 'object' ? (item.name.ko || item.name.en) : item.name
    if (!hasName) continue
    const sectorId = item.sector?.id || item.sectorId
    if (!sectorId) continue

    const payload = {
      sectorId: sectorId,
      name: typeof item.name === 'string' ? { ko: item.name } : item.name,
      sortOrder: item.sortOrder || 0,
      isDefaultOpen: item.isDefaultOpen !== false
    }

    try {
      if (item.id) {
        await customFetch(`/api/domains/${dId}/groups/${item.id}`, { method: 'PUT', body: payload })
      } else {
        await customFetch(`/api/domains/${dId}/groups`, { method: 'POST', body: payload })
      }
      saveCount++
    } catch (e) {
      console.error('Failed to save group', e)
      hasError = true
    }
  }

  domainGroups.value = await customFetch(`/api/domains/${dId}/groups`)
  if (!hasError) {
    showCustomAlert(`그룹 ${saveCount}건이 성공적으로 저장되었습니다.`, '저장 완료', 'Notification', 'success')
  } else {
    showCustomAlert('일부 그룹 저장 중 오류가 발생했습니다.', '저장 오류', 'Error', 'error')
  }
}

const saveSectorGroupChanges = async () => {
  if (sgActiveTab.value === 0) {
    await saveAllSectors()
  } else {
    await saveAllGroups()
  }
}

const deleteSelectedSector = async () => {
  if (!sectorGridApi.value) return
  const selected = sectorGridApi.value.getSelectedNodes()
  if (selected.length === 0) return
  const id = selected[0].data.id
  const dId = selectedNode.value.domainId
  if (!id) {
    domainSectors.value = await customFetch(`/api/domains/${dId}/sectors`)
    return
  }
  await deleteSector(id)
}

const deleteSelectedGroup = async () => {
  if (!groupGridApi.value) return
  const selected = groupGridApi.value.getSelectedNodes()
  if (selected.length === 0) return
  const id = selected[0].data.id
  const dId = selectedNode.value.domainId
  if (!id) {
    domainGroups.value = await customFetch(`/api/domains/${dId}/groups`)
    return
  }
  await deleteGroup(id)
}

const deleteGroup = async (id) => {
  const dId = selectedNode.value.domainId
  try {
    await customFetch(`/api/domains/${dId}/groups/${id}`, {
      method: 'DELETE'
    })
    domainGroups.value = await customFetch(`/api/domains/${dId}/groups`)
    cancelEditGroup()
  } catch (e) { showCustomAlert('Error deleting group.', 'Delete Error', 'Error', 'error') }
}

onMounted(() => {
  if (process.client) {
    window.addEventListener('approval-updated', refreshSchemaData)
  }
})

onUnmounted(() => {
  if (process.client) {
    window.removeEventListener('approval-updated', refreshSchemaData)
  }
})
</script>

<style scoped>
.schema-layout {
  display: flex;
  gap: 0.5rem;
  width: 100%;
  height: 100%;
  min-height: 0;
}
.schema-tree-column {
  width: 300px;
  min-width: 300px;
  max-width: 300px;
  overflow: hidden;
}
.schema-tree-wrapper {
  max-height: 400px;
  overflow-y: auto;
  overflow-x: hidden;
  margin-bottom: 1rem;
}
.schema-detail-column {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.schema-grid-wrapper {
  flex: 1;
  width: 100%;
  min-height: 0;
}

@media (max-width: 768px) {
  .schema-layout {
    flex-direction: column;
  }
  .schema-tree-column {
    width: 100%;
    max-width: 100%;
    min-width: 100%;
  }
  .schema-tree-wrapper {
    max-height: 250px;
  }
  .schema-detail-column {
    width: 100%;
    max-width: 100%;
    padding: 0.25rem 0;
  }
  .schema-grid-wrapper {
    height: 400px;
  }
}
</style>

<style scoped>
.mb-4 { margin-bottom: 1rem; }
.w-full { width: 100%; }

/* Tree Container */
:deep(.va-tree) {
  overflow-x: hidden;
}

.option-pill {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.45rem 0.65rem;
  border: 1px solid var(--va-background-border);
  border-radius: 8px;
  background: var(--va-background-element);
  cursor: pointer;
  user-select: none;
  transition: all 0.2s ease;
  font-size: 0.8rem;
  font-weight: 500;
}
.option-pill:hover {
  border-color: #2c82e0;
  background: var(--va-background-element, #f4f6f9);
}
.option-pill.active {
  border-color: #2c82e0;
  background: rgba(44, 130, 224, 0.08);
}
</style>
