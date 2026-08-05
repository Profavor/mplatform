<template>
  <div style="padding: 1.5rem; display: flex; flex-direction: column; gap: 1.25rem; height: calc(100vh - 80px); overflow: hidden;">
    
    <!-- Top Action Bar -->
    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); padding: 1rem 1.25rem; border-radius: 12px; border: 1px solid var(--va-background-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04);">
      <div style="display: flex; align-items: center; gap: 0.75rem;">
        <va-icon name="account_tree" size="large" color="primary" />
        <div>
          <h2 style="font-weight: 700; font-size: 1.35rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
            {{ pageTitle }}
            <va-badge :text="$t('ag_grid_unified_list')" color="primary" size="small" />
          </h2>
          <span style="font-size: 0.85rem; color: var(--va-text-secondary);">
            {{ $t('workflow_management_desc') }}
          </span>
        </div>
      </div>

      <div style="display: flex; gap: 0.75rem; align-items: center;">
        <va-button
          color="primary"
          icon="add"
          @click="openCreateModal"
          style="font-weight: 700; padding: 0.55rem 1.25rem;"
        >
          {{ $t('new_workflow_register') }}
        </va-button>
      </div>
    </div>

    <!-- Filtering & Control Bar -->
    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); padding: 0.75rem 1.25rem; border-radius: 10px; border: 1px solid var(--va-background-border);">
      <!-- Action Type Filter Tabs -->
      <div style="display: flex; gap: 0.35rem; background: var(--va-background-element); padding: 0.25rem; border-radius: 8px; border: 1px solid var(--va-background-border);">
        <va-button
          v-for="tab in actionFilterTabs"
          :key="tab.value"
          :preset="selectedActionFilter === tab.value ? 'primary' : 'plain'"
          :color="selectedActionFilter === tab.value ? 'primary' : 'secondary'"
          size="small"
          style="font-weight: 600;"
          @click="onActionFilterChanged(tab.value)"
        >
          {{ tab.text }}
        </va-button>
      </div>

      <!-- Search & Scope Controls -->
      <div style="display: flex; gap: 0.75rem; align-items: center;">
        <va-input
          v-model="searchQuery"
          :placeholder="$t('search_workflow_placeholder')"
          preset="outline"
          dense
          clearable
          style="width: 240px;"
          @update:model-value="onSearchChanged"
        >
          <template #prependInner>
            <va-icon name="search" size="small" color="secondary" />
          </template>
        </va-input>
        <va-button preset="outline" color="primary" icon="refresh" size="small" @click="fetchWorkflows">
          {{ $t('refresh') || '새로고침' }}
        </va-button>
      </div>
    </div>

    <!-- AG-Grid Table Container -->
    <va-card style="flex: 1; display: flex; flex-direction: column; min-height: 0; overflow: hidden; padding: 0.5rem;">
      <div :class="{ 'ag-theme-quartz-dark': isDark }" style="width: 100%; height: 100%; flex: 1;">
        <ag-grid-vue
          style="width: 100%; height: 100%;"
          :theme="gridTheme"
          :autoSizeStrategy="autoSizeStrategy"
          :columnDefs="columnDefs"
          :rowData="workflowList"
          :loading="isLoading"
          :defaultColDef="defaultColDef"
          :pagination="true"
          :paginationPageSize="pageSize"
          :suppressCellFocus="true"
          @grid-ready="onGridReady"
        />
      </div>
    </va-card>

    <!-- WORKFLOW CONFIG EDIT / CREATE MODAL -->
    <va-modal
      v-model="showModal"
      :title="modalData.id ? $t('edit_workflow_title') : $t('create_workflow_title')"
      size="large"
      style="--va-modal-max-width: 960px;"
      :ok-text="$t('save') || '저장'"
      :cancel-text="$t('cancel') || '취소'"
      @ok="saveWorkflowModal"
    >
      <div style="display: flex; flex-direction: column; gap: 1.25rem; max-height: 70vh; overflow-y: auto; overflow-x: hidden; padding: 0 4px;">
        
        <!-- SECTION 1: Basic Info -->
        <div style="background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 1rem; display: flex; flex-direction: column; gap: 0.85rem;">
          <h4 style="font-weight: 700; font-size: 0.95rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.4rem;">
            <va-icon name="info" color="primary" size="small" />
            {{ $t('basic_info') }}
          </h4>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.85rem;">
            <!-- Form Name KO -->
            <div>
              <label style="font-size: 0.78rem; font-weight: 700; color: var(--va-text-secondary); display: block; margin-bottom: 0.25rem;">
                {{ $t('form_name_ko') }}
              </label>
              <va-input v-model="modalData.nameKo" :placeholder="$t('form_name_ko_placeholder')" style="width: 100%;" dense>
                <template #prependInner>
                  <span style="font-size: 0.75rem; color: var(--va-text-secondary); font-weight: 600; margin-right: 0.4rem; border-right: 1px solid var(--va-background-border); padding-right: 0.4rem; white-space: nowrap;">
                    {{ locale === 'en' ? 'Korean' : '한국어' }}
                  </span>
                </template>
              </va-input>
            </div>

            <!-- Form Name EN -->
            <div>
              <label style="font-size: 0.78rem; font-weight: 700; color: var(--va-text-secondary); display: block; margin-bottom: 0.25rem;">
                {{ $t('form_name_en') }}
              </label>
              <va-input v-model="modalData.nameEn" :placeholder="$t('form_name_en_placeholder')" style="width: 100%;" dense>
                <template #prependInner>
                  <span style="font-size: 0.75rem; color: var(--va-text-secondary); font-weight: 600; margin-right: 0.4rem; border-right: 1px solid var(--va-background-border); padding-right: 0.4rem; white-space: nowrap;">
                    {{ locale === 'en' ? 'English' : '영어' }}
                  </span>
                </template>
              </va-input>
            </div>
          </div>

          <!-- Description -->
          <div>
            <label style="font-size: 0.78rem; font-weight: 700; color: var(--va-text-secondary); display: block; margin-bottom: 0.25rem;">
              {{ $t('form_description') }}
            </label>
            <va-input v-model="modalData.description" :placeholder="$t('form_description_placeholder')" style="width: 100%;" dense />
          </div>

          <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 0.85rem; align-items: end;">
            <!-- Action Type -->
            <div>
              <label style="font-size: 0.78rem; font-weight: 700; color: var(--va-text-secondary); display: block; margin-bottom: 0.25rem;">
                {{ $t('action_type_col') }} *
              </label>
              <va-select
                v-model="modalData.actionType"
                :options="actionTypeOptions"
                value-by="value"
                text-by="text"
                style="width: 100%;"
                dense
              />
            </div>

            <!-- Scope Level -->
            <div>
              <label style="font-size: 0.78rem; font-weight: 700; color: var(--va-text-secondary); display: block; margin-bottom: 0.25rem;">
                {{ $t('scope_level') }}
              </label>
              <va-select
                v-model="modalData.scopeLevel"
                :options="scopeLevelOptions"
                value-by="value"
                text-by="text"
                style="width: 100%;"
                dense
                @update:model-value="onModalScopeLevelChanged"
              />
            </div>

            <!-- Domain Select -->
            <div>
              <label style="font-size: 0.78rem; font-weight: 700; color: var(--va-text-secondary); display: block; margin-bottom: 0.25rem;">
                {{ $t('target_domain') }}
              </label>
              <va-select
                v-model="modalData.domainId"
                :options="domainOptions"
                value-by="value"
                text-by="text"
                :placeholder="$t('select_domain_placeholder')"
                style="width: 100%;"
                dense
                @update:model-value="onModalDomainChanged"
              />
            </div>
          </div>

          <!-- Classification Node Select (When Scope is NODE) -->
          <div v-if="modalData.scopeLevel === 'NODE'">
            <label style="font-size: 0.78rem; font-weight: 700; color: var(--va-text-secondary); display: block; margin-bottom: 0.25rem;">
              {{ $t('target_node') }}
            </label>
            <va-select
              v-model="modalData.nodeId"
              :options="modalNodeOptions"
              value-by="value"
              text-by="text"
              :placeholder="$t('select_node_placeholder')"
              style="width: 100%;"
              dense
            />
          </div>

          <!-- Options Checkboxes -->
          <div style="display: flex; gap: 1.5rem; align-items: center; margin-top: 0.25rem;">
            <va-checkbox v-model="modalData.isDefault" :label="$t('set_default_workflow_desc')" />
            <va-checkbox v-model="modalData.isActive" :label="$t('is_active_status')" />
          </div>
        </div>

        <!-- SECTION 2: Permissions & Field Rules -->
        <div style="background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 1rem; display: flex; flex-direction: column; gap: 0.85rem;">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <h4 style="font-weight: 700; font-size: 0.95rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.4rem;">
              <va-icon name="assignment_ind" color="primary" size="small" />
              {{ modalData.actionType === 'SCHEMA_CHANGE' ? $t('initiator_rules_title') : $t('permissions_rules_title') }}
            </h4>
            <va-button size="small" color="primary" icon="add" preset="secondary" @click="addModalPermissionRule">
              {{ $t('add_rule') }}
            </va-button>
          </div>

          <div v-if="modalData.permissions.length === 0" style="font-size: 0.8rem; color: var(--va-text-secondary); text-align: center; padding: 0.5rem;">
            {{ modalData.actionType === 'SCHEMA_CHANGE' ? $t('no_rules_schema_change') : $t('no_rules_default') }}
          </div>

          <div
            v-for="(rule, rIdx) in modalData.permissions"
            :key="rIdx"
            style="border: 1px solid var(--va-background-border); border-radius: 6px; padding: 0.85rem; background: var(--va-background-primary); display: flex; flex-direction: column; gap: 0.6rem;"
          >
            <!-- Target Selection Row -->
            <div style="display: flex; justify-content: space-between; align-items: center; gap: 0.5rem;">
              <div style="display: flex; gap: 0.5rem; align-items: center; flex: 1;">
                <va-select style="width: 140px;" v-model="rule.targetType" :options="permissionTargetTypeOptions" value-by="value" text-by="text" dense />
                <va-select v-if="rule.targetType === 'USER'" style="flex: 1; max-width: 320px;" v-model="rule.targetId" :options="userOptions" value-by="value" text-by="text" :placeholder="$t('select_user')" dense />
                <va-select v-else style="flex: 1; max-width: 320px;" v-model="rule.targetRole" :options="roleOptions" value-by="value" text-by="text" :placeholder="$t('select_role')" dense />
              </div>
              <va-button size="small" color="danger" preset="plain" icon="delete" @click="modalData.permissions.splice(rIdx, 1)">
                {{ $t('delete') }}
              </va-button>
            </div>

            <!-- Field Controls (Only for Data Record Actions: CREATE, UPDATE, DELETE) -->
            <div v-if="modalData.actionType !== 'SCHEMA_CHANGE'" style="display: flex; flex-direction: column; gap: 0.75rem;">
              <div>
                <label style="font-size: 0.78rem; font-weight: 700; color: var(--va-primary); display: block; margin-bottom: 0.3rem;">
                  {{ $t('editable_fields_title') }}
                </label>
                
                <!-- Selected Editable Chips List -->
                <div v-if="rule.editableFields && rule.editableFields.length > 0" style="display: flex; flex-wrap: wrap; gap: 0.35rem; margin-bottom: 0.4rem; padding: 0.4rem; background: var(--va-background-element); border-radius: 6px; border: 1px solid var(--va-background-border);">
                  <va-chip
                    v-for="fKey in rule.editableFields"
                    :key="fKey"
                    size="small"
                    color="primary"
                    style="font-weight: 600; font-size: 0.78rem; padding: 3px 8px; display: inline-flex; align-items: center;"
                  >
                    <span>{{ getFieldOptionLabel(fKey) }}</span>
                    <va-icon
                      name="close"
                      size="14px"
                      style="margin-left: 6px; cursor: pointer; opacity: 0.85;"
                      :title="$t('delete')"
                      @click.stop="onRemoveEditableField(rule, fKey)"
                    />
                  </va-chip>
                </div>

                <va-select
                  v-model="rule._tempEditable"
                  :options="getRemainingFieldOptions(rule.editableFields)"
                  value-by="value"
                  text-by="text"
                  :placeholder="$t('add_editable_field_placeholder')"
                  style="width: 100%;"
                  dense
                  @update:model-value="onAddEditableField(rule, $event)"
                />
              </div>

              <div>
                <label style="font-size: 0.78rem; font-weight: 700; color: #dc2626; display: block; margin-bottom: 0.3rem;">
                  {{ $t('hidden_fields_title') }}
                </label>

                <!-- Selected Hidden Chips List -->
                <div v-if="rule.hiddenFields && rule.hiddenFields.length > 0" style="display: flex; flex-wrap: wrap; gap: 0.35rem; margin-bottom: 0.4rem; padding: 0.4rem; background: var(--va-background-element); border-radius: 6px; border: 1px solid var(--va-background-border);">
                  <va-chip
                    v-for="fKey in rule.hiddenFields"
                    :key="fKey"
                    size="small"
                    color="danger"
                    style="font-weight: 600; font-size: 0.78rem; padding: 3px 8px; display: inline-flex; align-items: center;"
                  >
                    <span>{{ getFieldOptionLabel(fKey) }}</span>
                    <va-icon
                      name="close"
                      size="14px"
                      style="margin-left: 6px; cursor: pointer; opacity: 0.85;"
                      :title="$t('delete')"
                      @click.stop="onRemoveHiddenField(rule, fKey)"
                    />
                  </va-chip>
                </div>

                <va-select
                  v-model="rule._tempHidden"
                  :options="getRemainingFieldOptions(rule.hiddenFields)"
                  value-by="value"
                  text-by="text"
                  :placeholder="$t('add_hidden_field_placeholder')"
                  style="width: 100%;"
                  dense
                  @update:model-value="onAddHiddenField(rule, $event)"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- SECTION 3: Approval Line Timeline -->
        <div style="background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 1rem; display: flex; flex-direction: column; gap: 0.85rem;">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <h4 style="font-weight: 700; font-size: 0.95rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.4rem;">
              <va-icon name="linear_scale" color="primary" size="small" />
              {{ $t('approval_line_title') }}
            </h4>
            <va-button size="small" color="primary" icon="add" preset="secondary" @click="addModalApprovalStep">
              {{ $t('add_approval_step') }}
            </va-button>
          </div>

          <div v-if="modalData.steps.length === 0" style="font-size: 0.8rem; color: var(--va-text-secondary); text-align: center; padding: 0.5rem;">
            {{ $t('no_approval_steps') }}
          </div>

          <!-- Step Timeline Cards -->
          <div
            v-for="(step, sIdx) in modalData.steps"
            :key="sIdx"
            style="border: 1px solid var(--va-background-border); border-radius: 6px; padding: 0.85rem; background: var(--va-background-primary); display: flex; flex-direction: column; gap: 0.5rem;"
          >
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span style="font-weight: 700; font-size: 0.85rem; color: var(--va-primary);">
                STEP {{ sIdx + 1 }}
              </span>
              <va-button size="small" color="danger" preset="plain" icon="delete" @click="modalData.steps.splice(sIdx, 1)">
                {{ $t('delete') }}
              </va-button>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr 90px 1.4fr 90px; gap: 0.4rem; align-items: center; width: 100%; min-width: 0; box-sizing: border-box;">
              <va-input v-model="step.stepNameKo" :placeholder="$t('step_name_ko_placeholder')" dense style="min-width: 0;">
                <template #prependInner>
                  <span style="font-size: 0.75rem; color: var(--va-text-secondary); font-weight: 600; margin-right: 0.3rem; border-right: 1px solid var(--va-background-border); padding-right: 0.3rem; white-space: nowrap;">
                    {{ locale === 'en' ? 'Korean' : '한국어' }}
                  </span>
                </template>
              </va-input>
              <va-input v-model="step.stepNameEn" :placeholder="$t('step_name_en_placeholder')" dense style="min-width: 0;">
                <template #prependInner>
                  <span style="font-size: 0.75rem; color: var(--va-text-secondary); font-weight: 600; margin-right: 0.3rem; border-right: 1px solid var(--va-background-border); padding-right: 0.3rem; white-space: nowrap;">
                    {{ locale === 'en' ? 'English' : '영어' }}
                  </span>
                </template>
              </va-input>
              <va-select v-model="step.assigneeType" :options="stepAssigneeTypeOptions" value-by="value" text-by="text" dense style="min-width: 0;" />
              <va-select v-if="step.assigneeType === 'USER'" v-model="step.assigneeId" :options="userOptions" value-by="value" text-by="text" :placeholder="$t('select_approver_user')" dense style="min-width: 0;" />
              <va-select v-else v-model="step.assigneeRole" :options="roleOptions" value-by="value" text-by="text" :placeholder="$t('select_approver_role')" dense style="min-width: 0;" />
              <va-select v-model="step.stepType" :options="stepTypeOptions" value-by="value" text-by="text" dense style="min-width: 0;" />
            </div>
          </div>
        </div>

      </div>
    </va-modal>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { usePageTitle } from '~/composables/usePageTitle'

const { pageTitle } = usePageTitle('workflow_center_title', '워크플로우 관리')
import { AgGridVue } from 'ag-grid-vue3'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { useAgGridTheme } from '~/composables/useAgGridTheme'
import { useRoleStore } from '~/stores/useRoleStore'
import { useCodeStore } from '~/stores/useCodeStore'

const { customFetch } = useCustomFetch()
const { gridTheme, autoSizeStrategy, isDark } = useAgGridTheme()
const { t, locale } = useI18n()
const codeStore = useCodeStore()

// Multilingual helper to resolve name objects / JSON / fallback strings according to active locale
const getLocalizedName = (nameObj: any) => {
  if (!nameObj) return ''
  const currentLang = locale.value || 'ko'
  if (typeof nameObj === 'object') {
    return nameObj[currentLang] || nameObj.ko || nameObj.en || ''
  }
  if (typeof nameObj === 'string' && nameObj.startsWith('{')) {
    try {
      const p = JSON.parse(nameObj)
      return p[currentLang] || p.ko || p.en || nameObj
    } catch {
      return nameObj
    }
  }
  return String(nameObj)
}

const actionTypeOptions = computed(() => {
  return codeStore.getDropdownOptions('WORKFLOW_ACTION')
})

const actionFilterTabs = computed(() => [
  { value: 'ALL', text: t('action_type_all') },
  { value: 'CREATE', text: t('action_type_create_short') },
  { value: 'UPDATE', text: t('action_type_update_short') },
  { value: 'DELETE', text: t('action_type_delete_short') },
  { value: 'SCHEMA_CHANGE', text: t('action_type_schema_change_short') },
  { value: 'MERGE', text: t('action_type_merge_short') }
])

const scopeLevelOptions = computed(() => [
  { value: 'DOMAIN', text: t('scope_domain') },
  { value: 'NODE', text: t('scope_node') }
])

const permissionTargetTypeOptions = computed(() => [
  { value: 'USER', text: t('applicant_user') },
  { value: 'ROLE', text: t('applicant_role') }
])

const stepAssigneeTypeOptions = computed(() => [
  { value: 'USER', text: t('assignee_user') },
  { value: 'ROLE', text: t('assignee_role') }
])

const stepTypeOptions = computed(() => [
  { value: 'APPROVAL', text: t('step_type_approval') },
  { value: 'CONSULTATION', text: t('step_type_consultation') }
])

const selectedActionFilter = ref('ALL')
const searchQuery = ref('')
const isLoading = ref(false)
const workflowList = ref<any[]>([])
const pageSize = ref(20)

// Raw Master Data REFs
const rawDomainList = ref<any[]>([])
const rawUserList = ref<any[]>([])
const rawFieldList = ref<any[]>([])
const rawNodeTree = ref<any[]>([])

// Role Store (centralized - no direct API call)
const roleStore = useRoleStore()
const roleOptions = roleStore.roleOptions
const roleMap = computed(() => {
  const map: Record<string, string> = {}
  roleStore.rolesList.value.forEach((r: any) => {
    if (!r || !r.name) return
    map[r.name] = roleStore.getRoleDisplayName(r.name) || r.name
  })
  return map
})

// Reactive Computed Master Data & Options
const domainOptions = computed(() => {
  return rawDomainList.value.map((d: any) => {
    const label = getLocalizedName(d.name) || d.code || d.id
    return { value: d.id, text: label }
  })
})

const domainMap = computed(() => {
  const map: Record<string, string> = {}
  rawDomainList.value.forEach((d: any) => {
    map[d.id] = getLocalizedName(d.name) || d.code || d.id
  })
  return map
})

const userOptions = computed(() => {
  return rawUserList.value.map((u: any) => {
    const username = u.username || u.id
    return { value: username, text: username }
  })
})

const modalAvailableFields = computed(() => {
  return rawFieldList.value.map((f: any) => {
    const fName = getLocalizedName(f.name) || f.key
    return { value: f.key, text: `${fName} (${f.key})` }
  })
})

const modalNodeOptions = computed(() => {
  const flattenNodes = (nodes: any[]): any[] => {
    let acc: any[] = []
    nodes.forEach(n => {
      const label = getLocalizedName(n.name) || n.label || n.id
      acc.push({ value: n.id, text: label })
      if (n.children && n.children.length > 0) {
        acc = acc.concat(flattenNodes(n.children))
      }
    })
    return acc
  }
  return flattenNodes(rawNodeTree.value)
})

const gridApi = ref<any>(null)

const onGridReady = (params: any) => {
  gridApi.value = params.api
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

// AG-Grid Column Definitions
const columnDefs = computed<any[]>(() => [
  {
    headerName: t('workflow_name_col'),
    field: 'name',
    flex: 1.5,
    minWidth: 180,
    cellRenderer: (params: any) => {
      const data = params.data
      let nameStr = ''
      if (data.name) {
        if (typeof data.name === 'object') {
          nameStr = data.name[locale.value] || data.name.ko || data.name.en || ''
        } else if (typeof data.name === 'string' && data.name.startsWith('{')) {
          try {
            const p = JSON.parse(data.name)
            nameStr = p[locale.value] || p.ko || p.en || data.name
          } catch {
            nameStr = data.name
          }
        } else {
          nameStr = data.name
        }
      }
      if (!nameStr) nameStr = t('default_badge')
      const isDef = data.isDefault ? `<span style="color:#d97706; font-weight:700; margin-left:4px;">${t('default_badge')}</span>` : ''
      return `<div><strong style="color:var(--va-text-primary);">${nameStr}</strong> ${isDef}</div>`
    }
  },
  {
    headerName: t('action_type_col'),
    field: 'actionType',
    width: 140,
    cellRenderer: (params: any) => {
      const type = params.value || 'CREATE'
      let badgeClass = 'primary'
      let label = t('action_type_create_short')
      if (type === 'UPDATE') { badgeClass = 'info'; label = t('action_type_update_short') }
      else if (type === 'DELETE') { badgeClass = 'danger'; label = t('action_type_delete_short') }
      else if (type === 'SCHEMA_CHANGE') { badgeClass = 'warning'; label = t('action_type_schema_change_short') }
      else if (type === 'MERGE') { badgeClass = 'purple'; label = t('action_type_merge_short') }

      const colors: any = {
        primary: '#2563eb',
        info: '#0284c7',
        danger: '#dc2626',
        warning: '#d97706',
        purple: '#9333ea'
      }
      return `<span style="background:${colors[badgeClass]}; color:#fff; font-size:0.75rem; font-weight:700; padding:3px 8px; border-radius:12px;">${label}</span>`
    }
  },
  {
    headerName: t('scope_col'),
    field: 'scope',
    flex: 1.2,
    minWidth: 160,
    valueGetter: (params: any) => {
      const data = params.data
      if (data.nodeId) {
        return `${t('scope_node')} (${data.nodeId.substring(0, 8)}...)`
      }
      if (data.domainId) {
        const dName = domainMap.value[data.domainId] || data.domainId.substring(0, 8)
        return `${t('scope_domain')}: ${dName}`
      }
      return t('action_type_all')
    }
  },
  {
    headerName: t('description_col'),
    field: 'description',
    flex: 1.8,
    minWidth: 200,
    valueGetter: (params: any) => params.data.description || '-'
  },
  {
    headerName: t('approval_steps_col'),
    field: 'stepCount',
    width: 120,
    valueGetter: (params: any) => {
      if (!params.data.stepsConfig) return t('steps_count', { count: 0 })
      try {
        const parsed = typeof params.data.stepsConfig === 'string' ? JSON.parse(params.data.stepsConfig) : params.data.stepsConfig
        const steps = parsed.approvalLine || parsed.steps || []
        return t('steps_count', { count: steps.length })
      } catch {
        return t('steps_count', { count: 0 })
      }
    }
  },
  {
    headerName: t('status_col'),
    field: 'isActive',
    width: 100,
    cellRenderer: (params: any) => {
      const active = params.value !== false
      return active
        ? `<span style="color:#16a34a; font-weight:700;">${t('status_active')}</span>`
        : `<span style="color:#dc2626; font-weight:700;">${t('status_inactive')}</span>`
    }
  },
  {
    headerName: t('actions_col'),
    field: 'actions',
    width: 140,
    cellRenderer: (params: any) => {
      return `
        <div style="display:flex; gap:6px; align-items:center; height:100%;">
          <button class="btn-edit" style="background:#2563eb; color:#fff; border:none; border-radius:4px; padding:3px 8px; font-size:0.78rem; cursor:pointer;">✏️ ${t('edit')}</button>
          <button class="btn-delete" style="background:#ef4444; color:#fff; border:none; border-radius:4px; padding:3px 8px; font-size:0.78rem; cursor:pointer;">🗑️ ${t('delete')}</button>
        </div>
      `
    },
    onCellClicked: (params: any) => {
      const target = params.event.target
      if (target.classList.contains('btn-edit')) {
        openEditModal(params.data)
      } else if (target.classList.contains('btn-delete')) {
        deleteWorkflow(params.data)
      }
    }
  }
])

const defaultColDef = {
  sortable: true,
  resizable: true,
  filter: true
}

const domainStore = useDomain()

// Fetch Master Data
const fetchDomains = async () => {
  try {
    const list = await domainStore.fetchDomains()
    rawDomainList.value = list
  } catch (e) {
    console.error('Failed to fetch domains', e)
  }
}

const fetchUsers = async () => {
  try {
    const res: any = await customFetch('/api/users')
    rawUserList.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error('Failed to fetch users', e)
  }
}

const fetchRoles = async () => {
  try {
    await roleStore.dispatch('fetchRoles')
  } catch (e) {
    console.error('Failed to fetch roles via store', e)
  }
}

const fetchWorkflows = async () => {
  isLoading.value = true
  try {
    let url = `/api/workflow-configs/page?page=0&size=100`
    if (selectedActionFilter.value && selectedActionFilter.value !== 'ALL') {
      url += `&actionType=${selectedActionFilter.value}`
    }
    if (searchQuery.value && searchQuery.value.trim()) {
      url += `&query=${encodeURIComponent(searchQuery.value.trim())}`
    }
    const res: any = await customFetch(url)
    workflowList.value = res?.content || (Array.isArray(res) ? res : [])
  } catch (e) {
    console.error('Failed to fetch workflows', e)
    workflowList.value = []
  } finally {
    isLoading.value = false
  }
}

const onActionFilterChanged = (val: string) => {
  selectedActionFilter.value = val
  fetchWorkflows()
}

const onSearchChanged = () => {
  fetchWorkflows()
}

// MODAL DATA & HANDLERS
const showModal = ref(false)
const modalData = ref<any>({
  id: null,
  nameKo: '',
  nameEn: '',
  description: '',
  actionType: 'CREATE',
  scopeLevel: 'DOMAIN',
  domainId: null,
  nodeId: null,
  isDefault: false,
  isActive: true,
  permissions: [],
  steps: []
})

const parseMultilingualName = (name: any) => {
  if (!name) return { ko: '', en: '' }
  if (typeof name === 'object') return { ko: name.ko || '', en: name.en || '' }
  if (typeof name === 'string' && name.startsWith('{')) {
    try { const p = JSON.parse(name); return { ko: p.ko || '', en: p.en || '' } } catch {}
  }
  return { ko: String(name), en: '' }
}

const openCreateModal = () => {
  modalData.value = {
    id: null,
    nameKo: '',
    nameEn: '',
    description: '',
    actionType: selectedActionFilter.value !== 'ALL' ? selectedActionFilter.value : 'CREATE',
    scopeLevel: 'DOMAIN',
    domainId: domainOptions.value[0]?.value || null,
    nodeId: null,
    isDefault: false,
    isActive: true,
    permissions: [],
    steps: []
  }
  if (modalData.value.domainId) {
    onModalDomainChanged(modalData.value.domainId)
  }
  showModal.value = true
}

const openEditModal = (row: any) => {
  const parsedName = parseMultilingualName(row.name)
  let stepsConfig: any = {}
  if (row.stepsConfig) {
    try {
      stepsConfig = typeof row.stepsConfig === 'string' ? JSON.parse(row.stepsConfig) : row.stepsConfig
    } catch {}
  }

  const rawPermissions = stepsConfig.permissions || []
  const parsedPermissions = rawPermissions.map((p: any) => ({
    targetType: p.targetType || (p.targetId ? 'USER' : 'ROLE'),
    targetId: p.targetId || null,
    targetRole: p.targetRole || null,
    editableFields: Array.isArray(p.editableFields) ? p.editableFields : [],
    hiddenFields: Array.isArray(p.hiddenFields) ? p.hiddenFields : []
  }))

  const rawSteps = stepsConfig.approvalLine || stepsConfig.steps || []
  const parsedSteps = rawSteps.map((s: any, idx: number) => {
    let sType = s.stepType || 'APPROVAL'
    if (typeof sType === 'string') sType = sType.toUpperCase()
    if (sType !== 'APPROVAL' && sType !== 'CONSULTATION') sType = 'APPROVAL'

    let aType = s.assigneeType || (s.assigneeId ? 'USER' : 'ROLE')
    if (typeof aType === 'string') aType = aType.toUpperCase()

    const parsedStepName = parseMultilingualName(s.stepName)

    return {
      stepOrder: s.stepOrder || idx + 1,
      stepNameKo: parsedStepName.ko || `${idx + 1}차 승인`,
      stepNameEn: parsedStepName.en || `Step ${idx + 1} Approval`,
      assigneeType: aType,
      assigneeRole: s.assigneeRole || (roleOptions.value[0]?.value || 'ROLE_ADMIN'),
      assigneeId: s.assigneeId || null,
      stepType: sType
    }
  })

  modalData.value = {
    id: row.id,
    nameKo: parsedName.ko,
    nameEn: parsedName.en,
    description: row.description || '',
    actionType: row.actionType || 'CREATE',
    scopeLevel: row.nodeId ? 'NODE' : 'DOMAIN',
    domainId: row.domainId,
    nodeId: row.nodeId,
    isDefault: row.isDefault || false,
    isActive: row.isActive !== false,
    permissions: parsedPermissions,
    steps: parsedSteps
  }

  if (modalData.value.domainId) {
    onModalDomainChanged(modalData.value.domainId)
  }
  showModal.value = true
}

const onModalScopeLevelChanged = (val: string) => {
  if (val === 'DOMAIN') {
    modalData.value.nodeId = null
  }
}

const onModalDomainChanged = async (domainId: string) => {
  if (!domainId) return
  try {
    const fieldsRes: any = await customFetch(`/api/domains/${domainId}/fields`)
    rawFieldList.value = Array.isArray(fieldsRes) ? fieldsRes : (fieldsRes?.content || [])

    const nodesRes: any = await customFetch(`/api/domains/${domainId}/nodes/tree`)
    rawNodeTree.value = Array.isArray(nodesRes) ? nodesRes : (nodesRes?.content || [])
  } catch (e) {
    console.error('Failed to load modal domain data', e)
  }
}

const getFieldOptionLabel = (fieldKey: string) => {
  const f = modalAvailableFields.value.find((opt: any) => opt.value === fieldKey)
  return f ? f.text : fieldKey
}

const getRemainingFieldOptions = (selectedList: string[] = []) => {
  const current = selectedList || []
  return modalAvailableFields.value.filter((opt: any) => !current.includes(opt.value))
}

const onAddEditableField = (rule: any, val: string) => {
  if (!val) return
  if (!Array.isArray(rule.editableFields)) {
    rule.editableFields = []
  }
  if (!rule.editableFields.includes(val)) {
    rule.editableFields.push(val)
  }
  nextTick(() => {
    rule._tempEditable = ''
  })
}

const onRemoveEditableField = (rule: any, fieldKey: string) => {
  if (Array.isArray(rule.editableFields)) {
    rule.editableFields = rule.editableFields.filter((k: string) => k !== fieldKey)
  }
}

const onAddHiddenField = (rule: any, val: string) => {
  if (!val) return
  if (!Array.isArray(rule.hiddenFields)) {
    rule.hiddenFields = []
  }
  if (!rule.hiddenFields.includes(val)) {
    rule.hiddenFields.push(val)
  }
  nextTick(() => {
    rule._tempHidden = ''
  })
}

const onRemoveHiddenField = (rule: any, fieldKey: string) => {
  if (Array.isArray(rule.hiddenFields)) {
    rule.hiddenFields = rule.hiddenFields.filter((k: string) => k !== fieldKey)
  }
}

const addModalPermissionRule = () => {
  modalData.value.permissions.push({
    targetType: 'USER',
    targetId: userOptions.value[0]?.value || null,
    targetRole: roleOptions.value[0]?.value || null,
    editableFields: [],
    hiddenFields: []
  })
}

const addModalApprovalStep = () => {
  const stepIdx = modalData.value.steps.length + 1
  modalData.value.steps.push({
    stepOrder: stepIdx,
    stepNameKo: `${stepIdx}차 승인`,
    stepNameEn: `Step ${stepIdx} Approval`,
    assigneeType: 'ROLE',
    assigneeRole: roleOptions.value[0]?.value || 'ROLE_ADMIN',
    assigneeId: null,
    stepType: 'APPROVAL'
  })
}

const saveWorkflowModal = async () => {
  if (!modalData.value.domainId && modalData.value.scopeLevel === 'DOMAIN') {
    alert(t('select_target_domain_alert'))
    return
  }

  const approvalLine = modalData.value.steps.map((s: any, idx: number) => {
    const sNameKo = s.stepNameKo || (typeof s.stepName === 'object' ? s.stepName?.ko : s.stepName) || `${idx + 1}차 승인`
    const sNameEn = s.stepNameEn || (typeof s.stepName === 'object' ? s.stepName?.en : s.stepName) || `Step ${idx + 1} Approval`
    return {
      stepOrder: idx + 1,
      stepName: { ko: sNameKo, en: sNameEn },
      assigneeType: s.assigneeType,
      assigneeId: s.assigneeType === 'USER' ? s.assigneeId : null,
      assigneeRole: s.assigneeType === 'ROLE' ? s.assigneeRole : null,
      stepType: s.stepType || 'APPROVAL'
    }
  })

  const stepsConfigPayload = JSON.stringify({
    permissions: modalData.value.permissions,
    approvalLine: approvalLine,
    steps: approvalLine
  })

  const payload = {
    id: modalData.value.id,
    name: JSON.stringify({ ko: modalData.value.nameKo || '기본 서식', en: modalData.value.nameEn || '' }),
    description: modalData.value.description,
    actionType: modalData.value.actionType,
    domainId: modalData.value.domainId,
    nodeId: modalData.value.scopeLevel === 'NODE' ? modalData.value.nodeId : null,
    isDefault: modalData.value.isDefault,
    isActive: modalData.value.isActive,
    stepsConfig: stepsConfigPayload
  }

  try {
    await customFetch('/api/workflow-configs', {
      method: 'POST',
      body: payload
    })
    showModal.value = false
    fetchWorkflows()
  } catch (e) {
    console.error('Failed to save workflow config modal', e)
    alert(t('save_failed', '워크플로우 저장 중 오류가 발생했습니다.'))
  }
}

const deleteWorkflow = async (row: any) => {
  const parsed = parseMultilingualName(row.name)
  const nameToDisplay = row.description || getLocalizedName(row.name) || parsed.ko || '선택한 서식'
  if (!confirm(t('delete_workflow_confirm', { name: nameToDisplay }))) return
  try {
    await customFetch(`/api/workflow-configs/${row.id}`, { method: 'DELETE' })
    fetchWorkflows()
  } catch (e) {
    console.error('Failed to delete workflow config', e)
    alert(t('delete_failed', '삭제 처리 실패'))
  }
}

onMounted(async () => {
  await codeStore.preloadGroups(['WORKFLOW_ACTION'])
  await fetchRoles()
  await fetchUsers()
  await fetchDomains()
  await fetchWorkflows()
})
</script>
