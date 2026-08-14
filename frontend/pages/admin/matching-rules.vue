<template>
  <div class="matching-rules-container">
    <!-- Top Action Bar -->
    <div class="top-action-bar">
      <div class="header-title-section">
        <va-icon name="compare" size="large" color="primary" />
        <div>
          <h2 class="header-main-title">
            {{ pageTitle }}
            <va-badge text="Deduplication" color="primary" size="small" />
          </h2>
          <span class="header-sub-title">
            {{ t('matchingRules.subtitle') }}
          </span>
        </div>
      </div>

      <div class="header-controls-section">
        <va-select
          v-model="selectedDomainId"
          :options="domainOptions"
          value-by="value"
          text-by="text"
          :placeholder="t('matchingRules.select_domain_placeholder')"
          style="min-width: 220px;"
          dense
          @update:modelValue="onDomainChange"
        />
        <va-button
          color="primary"
          icon="add"
          :disabled="!selectedDomainId"
          @click="openCreateModal"
        >
          {{ t('matchingRules.add_rule') }}
        </va-button>
        <va-button
          preset="secondary"
          icon="refresh"
          :disabled="!selectedDomainId"
          @click="fetchData"
        >
          {{ t('matchingRules.refresh') }}
        </va-button>
      </div>
    </div>

    <!-- Feedback Summary Cards -->
    <div v-if="feedbackSummaries.length > 0" class="row" style="margin-bottom: 0.5rem; row-gap: 1rem;">
      <div v-for="stats in feedbackSummaries" :key="stats.ruleId" class="flex xs12 sm6 md4">
        <va-card stripe stripe-color="primary" class="h-full">
          <va-card-title class="flex justify-between items-center">
            <span style="font-weight: 700;">{{ stats.ruleName }}</span>
            <va-badge :text="stats.matchType" :color="stats.matchType === 'EXACT' ? 'info' : 'warning'" size="small" />
          </va-card-title>
          <va-card-content>
            <div style="display: flex; justify-content: space-between; margin-bottom: 0.5rem; font-size: 0.85rem;">
              <span>{{ t('matchingRules.total_reviewed') }}: <strong>{{ stats.totalReviewed || 0 }}</strong></span>
              <span>{{ t('matchingRules.precision') }}: <strong>{{ ((stats.precision || 0) * 100).toFixed(1) }}%</strong></span>
            </div>
            <div style="display: flex; gap: 0.5rem; margin-bottom: 0.5rem; font-size: 0.8rem;">
              <va-chip color="success" size="small">{{ t('matchingRules.confirmed') }}: {{ stats.confirmedCount || 0 }}</va-chip>
              <va-chip color="danger" size="small">{{ t('matchingRules.rejected') }}: {{ stats.rejectedCount || 0 }}</va-chip>
            </div>
            <div style="font-size: 0.8rem; color: var(--va-text-secondary); background: var(--va-background-element); padding: 0.5rem; border-radius: 6px; margin-top: 0.5rem;">
              <div>{{ t('matchingRules.current_threshold') }}: <strong>{{ stats.currentThreshold }}</strong></div>
              <div v-if="stats.recommendedThreshold">{{ t('matchingRules.recommended_threshold') }}: <strong style="color: var(--va-primary);">{{ stats.recommendedThreshold }}</strong></div>
              <div v-if="stats.recommendation" style="margin-top: 0.25rem; font-weight: 600; color: var(--va-primary);">💡 {{ stats.recommendation }}</div>
            </div>
          </va-card-content>
        </va-card>
      </div>
    </div>

    <!-- AG-Grid Rules Table Card -->
    <va-card style="flex: 1; display: flex; flex-direction: column; overflow: hidden; border-radius: 12px; border: 1px solid var(--va-background-border);">
      <va-card-title class="flex justify-between items-center" style="padding: 1rem 1.25rem;">
        <div class="flex items-center gap-2 font-bold text-lg">
          <va-icon name="table_chart" color="primary" />
          <span style="color: var(--va-text-primary);">{{ t('matchingRules.rule_list') }}</span>
          <va-chip v-if="selectedDomainId" size="small" color="primary">
            {{ t('matchingRules.items_count', { count: rules.length }) }}
          </va-chip>
        </div>
      </va-card-title>

      <va-card-content style="padding: 0 1.25rem 1.25rem 1.25rem;">
        <div v-if="selectedDomainId && rules.length > 0" :class="{ 'ag-theme-quartz-dark': isDark }" style="width: 100%; min-height: 380px;">
          <AgGridVue
            style="width: 100%; height: 420px;"
            :theme="gridTheme"
            :column-defs="columnDefs"
            :row-data="rules"
            :default-col-def="defaultColDef"
            :animate-rows="true"
            :row-height="54"
            :header-height="46"
            :suppress-cell-focus="true"
            :pagination="true"
            :pagination-page-size="10"
            :pagination-page-size-selector="[5, 10, 20, 50]"
          />
        </div>

        <!-- Empty State Component -->
        <div v-else class="empty-state-box">
          <va-icon name="compare" size="52px" color="secondary" class="mb-3" />
          <h3 class="empty-state-title">
            {{ selectedDomainId ? t('matchingRules.empty_no_rules') : t('matchingRules.empty_select_domain') }}
          </h3>
          <p class="empty-state-desc">
            {{ selectedDomainId ? t('matchingRules.empty_no_rules_desc') : t('matchingRules.empty_select_domain_desc') }}
          </p>
          <va-button v-if="selectedDomainId" color="primary" icon="add" size="small" @click="openCreateModal">
            {{ t('matchingRules.add_first_rule') }}
          </va-button>
        </div>
      </va-card-content>
    </va-card>

    <!-- Rule Form Modal (Decoupled Component) -->
    <MatchingRuleModal
      v-model="showModal"
      :is-edit-mode="isEditMode"
      :is-saving="isSaving"
      :form="form"
      :match-type-options="matchTypeOptions"
      :domain-field-options="domainFieldOptions"
      @save="saveRule"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast, useModal } from 'vuestic-ui'
import { AgGridVue } from 'ag-grid-vue3'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { usePageTitle } from '~/composables/usePageTitle'
import { useAgGridTheme } from '~/composables/useAgGridTheme'
import { useCodeStore } from '~/stores/useCodeStore'
import { useDomain } from '~/composables/useDomain'
import MatchingRuleModal from '~/components/admin/MatchingRuleModal.vue'

const { t } = useI18n()
const { pageTitle } = usePageTitle('matchingRules.title', '매칭 규칙 관리')
const { init } = useToast()
const { confirm } = useModal()
const { customFetch } = useCustomFetch()
const { gridTheme, isDark } = useAgGridTheme()
const codeStore = useCodeStore()

const domainStore = useDomain()
const selectedDomainId = ref('')
const domainOptions = computed(() => domainStore.domainOptions || [])
const domainFieldOptions = ref<any[]>([])

const rules = ref<any[]>([])
const feedbackSummaries = ref<any[]>([])

const showModal = ref(false)
const isEditMode = ref(false)
const isSaving = ref(false)
const editingRuleId = ref<any>(null)

const matchTypeOptions = computed(() => codeStore.getDropdownOptions('MATCH_TYPE'))

const form = ref({
  ruleName: '',
  matchType: matchTypeOptions.value[0]?.value || 'EXACT',
  selectedFields: [] as string[],
  targetFieldKeysInput: '',
  similarityThreshold: 0.85,
  isActive: true
})

const parseFields = (raw: any) => {
  if (!raw) return '-'
  try {
    const parsed = typeof raw === 'string' ? JSON.parse(raw) : raw
    if (Array.isArray(parsed)) return parsed.join(', ')
    return String(parsed)
  } catch (e) {
    return String(raw)
  }
}

// AG-Grid Custom Renderers
const RuleNameCellRenderer = (params: any) => {
  const div = document.createElement('div')
  div.style.cssText = 'display: flex; align-items: center; height: 100%; font-weight: 700; color: var(--va-primary); font-family: inherit;'
  div.textContent = params.value || '-'
  return div
}

const MatchTypeCellRenderer = (params: any) => {
  const div = document.createElement('div')
  div.style.cssText = 'display: flex; align-items: center; height: 100%;'
  const isExact = params.value === 'EXACT'

  const pill = document.createElement('span')
  pill.style.cssText = `padding: 2px 8px; border-radius: 12px; font-weight: 700; font-size: 0.75rem; font-family: inherit; ${
    isExact
      ? 'background: rgba(25, 118, 210, 0.12); color: var(--va-primary); border: 1px solid rgba(25, 118, 210, 0.3);'
      : 'background: rgba(237, 108, 2, 0.12); color: var(--va-warning); border: 1px solid rgba(237, 108, 2, 0.3);'
  }`
  
  const localizedName = codeStore.getCodeName('MATCH_TYPE', params.value, params.value)
  pill.textContent = localizedName

  div.appendChild(pill)
  return div
}

const TargetFieldsCellRenderer = (params: any) => {
  const div = document.createElement('div')
  div.style.cssText = 'display: flex; align-items: center; gap: 0.35rem; height: 100%; flex-wrap: wrap;'

  const fieldStr = parseFields(params.value)
  if (fieldStr === '-') {
    div.textContent = '-'
    return div
  }

  const fields = fieldStr.split(',').map(s => s.trim())
  fields.forEach((f: string) => {
    const chip = document.createElement('span')
    chip.style.cssText = 'padding: 2px 6px; background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 4px; font-family: monospace; font-size: 0.78rem; font-weight: 600; color: var(--va-text-primary);'
    chip.textContent = f
    div.appendChild(chip)
  })

  return div
}

const SimilarityCellRenderer = (params: any) => {
  const div = document.createElement('div')
  div.style.cssText = 'display: flex; align-items: center; height: 100%; font-weight: 700; font-family: inherit; font-size: 0.85rem;'

  if (params.value == null) {
    div.textContent = '-'
    div.style.color = 'var(--va-text-secondary)'
  } else {
    const val = Number(params.value)
    const pct = (val * 100).toFixed(0)
    div.textContent = `${val} (${pct}%)`
    div.style.color = val >= 0.85 ? 'var(--va-success)' : 'var(--va-warning)'
  }

  return div
}

const IsActiveCellRenderer = (params: any) => {
  const div = document.createElement('div')
  div.style.cssText = 'display: flex; align-items: center; height: 100%;'

  const isActive = Boolean(params.value)
  const pill = document.createElement('span')
  pill.style.cssText = `padding: 2px 8px; border-radius: 12px; font-weight: 700; font-size: 0.75rem; font-family: inherit; ${
    isActive
      ? 'background: rgba(46, 125, 50, 0.12); color: var(--va-success); border: 1px solid rgba(46, 125, 50, 0.3);'
      : 'background: rgba(158, 158, 158, 0.12); color: var(--va-text-secondary); border: 1px solid rgba(158, 158, 158, 0.3);'
  }`
  pill.textContent = isActive ? t('matchingRules.active') : t('matchingRules.inactive')

  div.appendChild(pill)
  return div
}

const ActionsCellRenderer = (params: any) => {
  const div = document.createElement('div')
  div.style.cssText = 'display: flex; align-items: center; justify-content: center; gap: 0.35rem; height: 100%;'

  const editBtn = document.createElement('button')
  editBtn.style.cssText = 'border: none; background: rgba(25, 118, 210, 0.1); color: var(--va-primary); border-radius: 6px; padding: 4px 8px; cursor: pointer; display: flex; align-items: center; font-weight: 600; font-size: 0.78rem;'
  editBtn.innerHTML = `<span class="material-icons" style="font-size: 16px;">edit</span>`
  editBtn.addEventListener('click', () => openEditModal(params.data))

  const deleteBtn = document.createElement('button')
  deleteBtn.style.cssText = 'border: none; background: rgba(229, 57, 53, 0.1); color: var(--va-danger); border-radius: 6px; padding: 4px 8px; cursor: pointer; display: flex; align-items: center; font-weight: 600; font-size: 0.78rem;'
  deleteBtn.innerHTML = `<span class="material-icons" style="font-size: 16px;">delete</span>`
  deleteBtn.addEventListener('click', () => deleteRule(params.data))

  div.appendChild(editBtn)
  div.appendChild(deleteBtn)
  return div
}

const defaultColDef = {
  sortable: true,
  resizable: true
}

const columnDefs = computed(() => [
  {
    field: 'ruleName',
    headerName: t('matchingRules.rule_name'),
    flex: 1.2,
    cellRenderer: RuleNameCellRenderer
  },
  {
    field: 'matchType',
    headerName: t('matchingRules.match_type'),
    width: 130,
    cellRenderer: MatchTypeCellRenderer
  },
  {
    field: 'targetFieldKeys',
    headerName: t('matchingRules.target_fields'),
    flex: 1.5,
    cellRenderer: TargetFieldsCellRenderer
  },
  {
    field: 'similarityThreshold',
    headerName: t('matchingRules.similarity_threshold'),
    width: 160,
    cellRenderer: SimilarityCellRenderer
  },
  {
    field: 'isActive',
    headerName: t('matchingRules.is_active'),
    width: 140,
    cellRenderer: IsActiveCellRenderer
  },
  {
    field: 'actions',
    headerName: t('common.action', '작업'),
    width: 100,
    sortable: false,
    cellRenderer: ActionsCellRenderer
  }
])

const loadDomains = async () => {
  try {
    await domainStore.fetchDomains()
    if (domainOptions.value.length > 0 && !selectedDomainId.value) {
      selectedDomainId.value = String(domainOptions.value[0].value)
      onDomainChange()
    }
  } catch (e) {
    console.error('Failed to load domains', e)
  }
}

const loadDomainFields = async () => {
  if (!selectedDomainId.value) return
  try {
    const res = await customFetch(`/api/domains/${selectedDomainId.value}/fields`)
    const list = res || []
    domainFieldOptions.value = list.map((f: any) => {
      let pName = f.key || ''
      if (f.name) {
        if (typeof f.name === 'object') {
          pName = f.name.ko || f.name.en || f.key
        } else if (typeof f.name === 'string') {
          try {
            const parsed = JSON.parse(f.name)
            pName = parsed.ko || parsed.en || f.name
          } catch (e) {
            pName = f.name
          }
        }
      }
      const displayLabel = `${pName} (${f.key})`
      return {
        label: displayLabel,
        text: displayLabel,
        value: f.key
      }
    })
  } catch (e) {
    domainFieldOptions.value = []
  }
}

const fetchData = async () => {
  if (!selectedDomainId.value) return
  try {
    const [rulesRes, summaryRes] = await Promise.all([
      customFetch(`/api/domains/${selectedDomainId.value}/matching-rules`),
      customFetch(`/api/domains/${selectedDomainId.value}/matching-rules/feedback-summary`)
    ])
    rules.value = rulesRes || []
    feedbackSummaries.value = summaryRes || []
  } catch (e) {
    rules.value = []
    feedbackSummaries.value = []
  }
}

const onDomainChange = () => {
  fetchData()
  loadDomainFields()
}

const openCreateModal = () => {
  isEditMode.value = false
  editingRuleId.value = null
  form.value = {
    ruleName: '',
    matchType: matchTypeOptions.value[0]?.value || 'EXACT',
    selectedFields: [],
    targetFieldKeysInput: '',
    similarityThreshold: 0.85,
    isActive: true
  }
  showModal.value = true
}

const openEditModal = (rule: any) => {
  isEditMode.value = true
  editingRuleId.value = rule.id
  let fieldsArr = []
  try {
    const parsed = typeof rule.targetFieldKeys === 'string' ? JSON.parse(rule.targetFieldKeys) : rule.targetFieldKeys
    fieldsArr = Array.isArray(parsed) ? parsed : [String(parsed)]
  } catch (e) {
    fieldsArr = rule.targetFieldKeys ? [rule.targetFieldKeys] : []
  }

  form.value = {
    ruleName: rule.ruleName,
    matchType: rule.matchType,
    selectedFields: fieldsArr,
    targetFieldKeysInput: fieldsArr.join(', '),
    similarityThreshold: rule.similarityThreshold != null ? rule.similarityThreshold : 0.85,
    isActive: rule.isActive
  }
  showModal.value = true
}

const saveRule = async () => {
  if (!form.value.ruleName) {
    init({ message: t('matchingRules.rule_name_placeholder'), color: 'warning' })
    return
  }

  let fieldKeysJson = '[]'
  if (domainFieldOptions.value.length > 0 && form.value.selectedFields.length > 0) {
    fieldKeysJson = JSON.stringify(form.value.selectedFields)
  } else if (form.value.targetFieldKeysInput) {
    const keys = form.value.targetFieldKeysInput.split(',').map(k => k.trim()).filter(Boolean)
    fieldKeysJson = JSON.stringify(keys)
  }

  const payload = {
    ruleName: form.value.ruleName,
    matchType: form.value.matchType,
    targetFieldKeys: fieldKeysJson,
    similarityThreshold: form.value.matchType === 'FUZZY' ? form.value.similarityThreshold : null,
    isActive: form.value.isActive
  }

  isSaving.value = true
  try {
    if (isEditMode.value) {
      await customFetch(`/api/domains/${selectedDomainId.value}/matching-rules/${editingRuleId.value}`, {
        method: 'PUT',
        body: payload
      })
      init({ message: t('matchingRules.save_success'), color: 'success' })
    } else {
      await customFetch(`/api/domains/${selectedDomainId.value}/matching-rules`, {
        method: 'POST',
        body: payload
      })
      init({ message: t('matchingRules.save_success'), color: 'success' })
    }
    showModal.value = false
    fetchData()
  } catch (e) {
    init({ message: t('matchingRules.save_failed'), color: 'danger' })
  } finally {
    isSaving.value = false
  }
}

const deleteRule = async (rule: any) => {
  const isConfirmed = await confirm({
    title: t('matchingRules.delete_confirm', { name: rule.ruleName }),
    message: t('matchingRules.delete_confirm', { name: rule.ruleName }),
    okText: t('matchingRules.delete_success', '삭제'),
    cancelText: t('matchingRules.cancel')
  })
  if (!isConfirmed) return

  try {
    await customFetch(`/api/domains/${selectedDomainId.value}/matching-rules/${rule.id}`, {
      method: 'DELETE'
    })
    init({ message: t('matchingRules.delete_success'), color: 'success' })
    fetchData()
  } catch (e) {
    init({ message: t('matchingRules.delete_failed'), color: 'danger' })
  }
}

onMounted(async () => {
  await codeStore.preloadGroups(['MATCH_TYPE'])
  loadDomains()
})
</script>

<style scoped>
.matching-rules-container {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  padding-bottom: 2rem;
  width: 100%;
  font-family: 'Pretendard', 'Inter', -apple-system, BlinkMacSystemFont, system-ui, Roboto, sans-serif;
  color: var(--va-text-primary);
}

.top-action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--va-background-primary);
  padding: 1rem 1.25rem;
  border-radius: 12px;
  border: 1px solid var(--va-background-border);
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
}

.header-title-section {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.header-main-title {
  font-family: inherit;
  font-weight: 700;
  font-size: 1.25rem;
  margin: 0;
  color: var(--va-text-primary);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  letter-spacing: -0.2px;
}

.header-sub-title {
  font-family: inherit;
  font-size: 0.85rem;
  color: var(--va-text-secondary);
  display: block;
  margin-top: 2px;
}

.header-controls-section {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.empty-state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 3rem 1rem;
  text-align: center;
}

.empty-state-title {
  font-family: inherit;
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--va-text-primary);
  margin: 0 0 0.4rem 0;
}

.empty-state-desc {
  font-family: inherit;
  font-size: 0.85rem;
  color: var(--va-text-secondary);
  margin: 0 0 1.25rem 0;
}
</style>
