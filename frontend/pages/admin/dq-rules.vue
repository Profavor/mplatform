<template>
  <div style="display: flex; flex-direction: column; gap: 1.25rem; height: 100%; min-height: 0;">
    <!-- Top Action Bar -->
    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); padding: 1rem 1.25rem; border-radius: 12px; border: 1px solid var(--va-background-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04); flex: 0 0 auto;">
      <div style="display: flex; align-items: center; gap: 0.75rem;">
        <va-icon name="gavel" size="large" color="primary" />
        <div>
          <h2 style="font-weight: 700; font-size: 1.35rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
            {{ $t('dq_rules_management') || '데이터 품질 검칙 관리' }}
            <va-badge text="Quality" color="primary" size="small" />
          </h2>
          <span style="font-size: 0.85rem; color: var(--va-text-secondary);">
            {{ $t('dq_rules_desc') || '도메인 필드별 데이터 품질 검증 규칙 및 검사 파라미터를 설정합니다.' }}
          </span>
        </div>
      </div>
    </div>

    <div class="dq-rules-layout" style="flex: 1; min-height: 0; display: flex; gap: 1.5rem;">
      <!-- Tree Column -->
      <div class="tree-column" style="width: 350px; flex: 0 0 auto; display: flex; flex-direction: column;">
        <va-card style="flex: 1; display: flex; flex-direction: column;">
          <va-card-title>
            {{ $t('classification_tree') || 'Classification Tree' }}
          </va-card-title>
          <va-card-content style="flex: 1; overflow-y: auto;">
            <ClassificationTree
              ref="treeRef"
              :selectedNode="selectedNode"
              :showEdit="false"
              @select="onNodeSelected"
            />
          </va-card-content>
        </va-card>
      </div>
      
      <!-- Detail Column -->
      <div class="detail-column" style="flex: 1; min-width: 0; display: flex; flex-direction: column;">
        <va-card style="flex: 1; display: flex; flex-direction: column; min-height: 0;">
          <va-card-title>
            <div style="display: flex; flex-direction: column; width: 100%; gap: 1rem;">
              <div style="display: flex; justify-content: space-between; align-items: flex-end;">
                <div style="flex: 1; max-width: 400px;">
                  <div style="font-size: 0.85rem; font-weight: 600; color: var(--va-text-secondary); margin-bottom: 0.25rem;">
                    대상 필드 (Target Field)
                  </div>
                  <va-select
                    v-model="selectedFieldId"
                    :options="fieldOptions"
                    value-by="id"
                    text-by="text"
                    placeholder="트리에서 노드를 선택한 후 필드를 선택하세요."
                    :disabled="!selectedNode"
                    @update:modelValue="onFieldSelected"
                  />
                </div>
                <va-button
                  color="primary"
                  icon="add"
                  :disabled="!selectedFieldId"
                  @click="openRuleModal(null)"
                >
                  {{ $t('add_dq_rule') || 'Add Rule' }}
                </va-button>
              </div>
            </div>
          </va-card-title>
          <va-card-content style="flex: 1; display: flex; flex-direction: column; min-height: 0; padding: 1rem;">
            <div v-if="!selectedFieldId" style="flex: 1; display: flex; align-items: center; justify-content: center; flex-direction: column; color: var(--va-text-secondary);">
              <va-icon name="playlist_add_check" size="4rem" color="secondary" style="opacity: 0.5; margin-bottom: 1rem;" />
              <div style="font-size: 1.1rem; font-weight: 600;">좌측 트리에서 노드를 선택하고 필드를 지정해주세요.</div>
            </div>
            <div v-else style="flex: 1; width: 100%; height: 100%;">
              <ag-grid-vue
                style="width: 100%; height: 100%;"
                :theme="gridTheme"
                :columnDefs="columnDefs"
                :rowData="rules"
                :rowSelection="{ mode: 'singleRow', headerCheckbox: false }"
                :pagination="false"
              />
            </div>
          </va-card-content>
        </va-card>
      </div>
    </div>

    <!-- Rule Edit Modal -->
    <va-modal
      v-model="showRuleModal"
      :title="editingRuleId ? ($t('edit_dq_rule') || 'Edit Rule') : ($t('add_dq_rule') || 'Add Rule')"
      size="medium"
      @ok="saveRule"
      @cancel="showRuleModal = false"
    >
      <div style="padding: 1rem 0; display: flex; flex-direction: column; gap: 1.25rem;">
        <va-select
          v-model="ruleFormData.ruleType"
          :options="ruleTypeOptions"
          :label="$t('dq_rule_type') || 'Rule Type'"
          required
        />
        
        <va-select
          v-model="ruleFormData.severity"
          :options="severityOptions"
          :label="$t('dq_severity') || 'Severity'"
          required
        />

        <va-input
          v-model="ruleFormData.params"
          :label="$t('dq_params') || 'Parameters'"
          placeholder="e.g. ^[0-9]+$"
          type="textarea"
          :min-rows="2"
        />

        <va-input
          v-model="ruleFormData.message"
          :label="$t('dq_error_message') || 'Error Message'"
          placeholder="e.g. 숫자만 입력 가능합니다."
        />

        <div style="display: flex; gap: 1rem;">
          <va-input
            v-model="ruleFormData.sortOrder"
            :label="$t('dq_sort_order') || 'Sort Order'"
            type="number"
            style="flex: 1;"
          />
          <va-checkbox
            v-model="ruleFormData.isActive"
            label="활성화 (Active)"
            style="flex: 1; margin-top: 1.25rem;"
          />
        </div>
      </div>
    </va-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { AgGridVue } from 'ag-grid-vue3'
import { useAgGridTheme } from '~/composables/useAgGridTheme'
import { useI18n } from 'vue-i18n'
import { useToast } from 'vuestic-ui'

const { t } = useI18n()
const { init } = useToast()
const { gridTheme } = useAgGridTheme()

const token = useCookie('token')

const treeRef = ref(null)
const selectedNode = ref(null)
const selectedFieldId = ref(null)
const fieldOptions = ref([])
const rules = ref([])

const showRuleModal = ref(false)
const editingRuleId = ref(null)
const ruleFormData = ref({
  ruleType: 'REGEX',
  severity: 'ERROR',
  params: '',
  message: '',
  sortOrder: 0,
  isActive: true
})

const ruleTypeOptions = [
  'REQUIRED',
  'REGEX',
  'MIN_LENGTH',
  'MAX_LENGTH',
  'LENGTH_RANGE',
  'MIN_VALUE',
  'MAX_VALUE',
  'VALUE_RANGE',
  'ENUM_IN',
  'CUSTOM_SCRIPT'
]

const severityOptions = [
  'ERROR',
  'WARNING'
]

const columnDefs = computed(() => [
  { field: 'ruleType', headerName: t('dq_rule_type') || 'Type', width: 140, sortable: true },
  { 
    field: 'severity', 
    headerName: t('dq_severity') || 'Severity', 
    width: 100, 
    sortable: true,
    cellRenderer: (params) => {
      if (!params || !params.value) return '';
      const color = params.value === 'ERROR' ? '#e53935' : '#e6a23c';
      return `<span style="background: ${color}; color: white; padding: 2px 6px; border-radius: 4px; font-size: 11px; font-weight: bold;">${params.value}</span>`;
    }
  },
  { field: 'params', headerName: t('dq_params') || 'Params', flex: 1 },
  { field: 'message', headerName: t('dq_error_message') || 'Message', flex: 1 },
  { field: 'sortOrder', headerName: t('dq_sort_order') || 'Order', width: 90, sortable: true },
  { 
    field: 'isActive', 
    headerName: 'Active', 
    width: 90,
    cellRenderer: (params) => {
      const color = params.value ? '#1ebc72' : '#999';
      const text = params.value ? 'ON' : 'OFF';
      return `<span style="color: ${color}; font-weight: bold;">${text}</span>`;
    }
  },
  {
    field: 'actions',
    headerName: 'Actions',
    width: 120,
    sortable: false,
    cellRenderer: (params) => {
      const div = document.createElement('div')
      div.style.cssText = 'display: flex; gap: 4px; align-items: center; height: 100%;'
      
      const editBtn = document.createElement('button')
      editBtn.innerHTML = '✏️'
      editBtn.style.cssText = 'background: transparent; border: none; cursor: pointer; font-size: 14px;'
      editBtn.onclick = () => openRuleModal(params.data)
      
      const delBtn = document.createElement('button')
      delBtn.innerHTML = '🗑️'
      delBtn.style.cssText = 'background: transparent; border: none; cursor: pointer; font-size: 14px;'
      delBtn.onclick = () => deleteRule(params.data.id)
      
      div.appendChild(editBtn)
      div.appendChild(delBtn)
      return div
    }
  }
])

const onNodeSelected = async (node) => {
  selectedNode.value = node
  selectedFieldId.value = null
  rules.value = []
  
  if (!node) {
    fieldOptions.value = []
    return
  }
  
  try {
    const fieldsUrl = node.isDomain 
      ? `/api/domains/${node.id}/fields`
      : `/api/nodes/${node.id}/fields/effective`
      
    const fields = await $fetch(fieldsUrl, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    
    fieldOptions.value = (fields || []).map(f => {
      const displayStr = `${f.key} (${parseName(f.name)})`
      return {
        id: f.id,
        label: displayStr,
        text: displayStr
      }
    })
  } catch (e) {
    console.error('Failed to load fields', e)
    init({ message: '필드 목록을 불러오지 못했습니다.', color: 'danger' })
  }
}

const onFieldSelected = async () => {
  if (!selectedFieldId.value) {
    rules.value = []
    return
  }
  
  try {
    const res = await $fetch(`/api/fields/${selectedFieldId.value}/dq-rules`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    rules.value = res || []
  } catch (e) {
    console.error('Failed to load dq rules', e)
    init({ message: '규칙 목록을 불러오지 못했습니다.', color: 'danger' })
  }
}

const openRuleModal = (rule = null) => {
  if (rule) {
    editingRuleId.value = rule.id
    ruleFormData.value = {
      ruleType: rule.ruleType,
      severity: rule.severity,
      params: rule.params || '',
      message: rule.message || '',
      sortOrder: rule.sortOrder || 0,
      isActive: rule.isActive
    }
  } else {
    editingRuleId.value = null
    ruleFormData.value = {
      ruleType: 'REGEX',
      severity: 'ERROR',
      params: '',
      message: '',
      sortOrder: 0,
      isActive: true
    }
  }
  showRuleModal.value = true
}

const saveRule = async () => {
  if (!selectedFieldId.value) return
  
  try {
    if (editingRuleId.value) {
      await $fetch(`/api/dq-rules/${editingRuleId.value}`, {
        method: 'PUT',
        headers: { Authorization: `Bearer ${token.value}` },
        body: ruleFormData.value
      })
      init({ message: '규칙이 수정되었습니다.', color: 'success' })
    } else {
      await $fetch(`/api/fields/${selectedFieldId.value}/dq-rules`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token.value}` },
        body: {
          ...ruleFormData.value,
          nodeId: selectedNode.value.id,
          domainId: selectedNode.value.domainId || (selectedNode.value.type === 'domain' ? selectedNode.value.id : null)
        }
      })
      init({ message: '규칙이 추가되었습니다.', color: 'success' })
    }
    showRuleModal.value = false
    await onFieldSelected()
  } catch (e) {
    console.error('Failed to save rule', e)
    init({ message: '규칙 저장에 실패했습니다.', color: 'danger' })
  }
}

const deleteRule = async (id) => {
  if (!confirm('정말로 이 규칙을 삭제하시겠습니까?')) return
  try {
    await $fetch(`/api/dq-rules/${id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    init({ message: '규칙이 삭제되었습니다.', color: 'success' })
    await onFieldSelected()
  } catch (e) {
    console.error('Failed to delete rule', e)
    init({ message: '규칙 삭제에 실패했습니다.', color: 'danger' })
  }
}

const parseName = (nameObj) => {
  if (!nameObj) return ''
  if (typeof nameObj === 'string') {
    try { 
      const parsed = JSON.parse(nameObj) 
      return parsed.ko || parsed.en || ''
    } catch (e) { 
      return nameObj 
    }
  }
  return nameObj.ko || nameObj.en || ''
}
</script>

<style scoped>
.tree-column {
  min-width: 300px;
}
</style>
