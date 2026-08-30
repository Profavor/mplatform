<template>
  <div class="survivorship-container">
    
    <!-- Top Action Bar -->
    <div class="top-action-bar">
      <div class="header-title-section">
        <va-icon name="published_with_changes" size="28px" color="primary" />
        <div>
          <h2 class="header-main-title">
            {{ pageTitle }}
            <va-badge text="Golden Record" color="primary" size="small" />
          </h2>
          <span class="header-sub-title">
            {{ $t('survivorship.description') }}
          </span>
        </div>
      </div>

      <div class="header-controls-section">
        <va-select
          v-model="selectedDomainId"
          :options="domainOptions"
          value-by="value"
          text-by="text"
          :placeholder="$t('survivorship.select_domain_placeholder')"
          class="domain-select-input"
          dense
          @update:modelValue="onDomainChange"
        />
        <va-button
          color="success"
          icon="add"
          :disabled="!selectedDomainId"
          @click="addRule"
        >
          {{ $t('survivorship.add_rule') }}
        </va-button>
        <va-button
          color="primary"
          icon="save"
          :disabled="!selectedDomainId || isSaving"
          :loading="isSaving"
          @click="saveRules"
        >
          {{ $t('survivorship.save_settings') }}
        </va-button>
      </div>
    </div>

    <!-- Unified Strategy Guidance & KPI Panel (Decoupled Component) -->
    <SurvivorshipGuidePanel
      :selected-domain-id="selectedDomainId"
      :rules-count="rules.length"
      :domain-fields-count="domainFields.length"
      :current-domain-name="currentDomainName"
      :strategy-options="strategyOptions"
    />

    <!-- AG-Grid Table Card Section -->
    <div class="grid-card-container">
      <div class="grid-card-header">
        <div class="grid-title-group">
          <va-icon name="table_chart" color="primary" size="22px" />
          <span class="grid-title">{{ $t('survivorship.rule_list') }}</span>
          <va-chip v-if="selectedDomainId" size="small" color="primary" class="items-chip">
            {{ rules.length }}{{ $t('survivorship.items_count') }}
          </va-chip>
        </div>
        <va-button preset="secondary" icon="refresh" size="small" :disabled="!selectedDomainId" @click="fetchData">
          {{ $t('survivorship.refresh') }}
        </va-button>
      </div>

      <div class="grid-body-section">
        <div v-if="selectedDomainId && rules.length > 0" :class="{ 'ag-theme-quartz-dark': isDark }" class="ag-grid-wrapper">
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

        <!-- Unified Empty State Component -->
        <div v-else class="empty-state-box">
          <va-icon name="alt_route" size="52px" color="secondary" class="mb-3" />
          <h3 class="empty-state-title">
            {{ selectedDomainId ? ($t('survivorship.empty_no_rules')) : ($t('survivorship.empty_no_domain')) }}
          </h3>
          <p class="empty-state-desc">
            {{ selectedDomainId ? ($t('survivorship.empty_sub_rules')) : ($t('survivorship.empty_sub_domain')) }}
          </p>
          <va-button v-if="selectedDomainId" color="success" icon="add" size="small" @click="addRule">
            {{ $t('survivorship.add_first_rule') }}
          </va-button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'vuestic-ui'
import { AgGridVue } from 'ag-grid-vue3'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { usePageTitle } from '~/composables/usePageTitle'
import { useAgGridTheme } from '~/composables/useAgGridTheme'
import { useDomainStore } from '~/stores/useDomainStore'
import { useCodeStore } from '~/stores/useCodeStore'
import SurvivorshipGuidePanel from '~/components/admin/SurvivorshipGuidePanel.vue'
import { useCookie } from '#app'

const { t, locale } = useI18n()
const { pageTitle } = usePageTitle('survivorship.title', '생존 규칙 관리')
const { init } = useToast()
const { customFetch } = useCustomFetch()
const { gridTheme, isDark } = useAgGridTheme()

const domainStore = useDomainStore()
const codeStore = useCodeStore()
const selectedDomainId = ref<string | null>(null)
const domainOptions = computed(() => domainStore.domainOptions || [])

const rules = ref<any[]>([])
const domainFields = ref<any[]>([])
const isSaving = ref(false)

const currentDomainName = computed(() => {
  if (!selectedDomainId.value) return '-'
  return domainStore.getDomainName(selectedDomainId.value) || '-'
})

const strategyOptions = computed(() => codeStore.getDropdownOptions('SURVIVORSHIP_STRATEGY'))

const fieldOptions = computed(() => {
  return domainFields.value.map((f: any) => {
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
    return {
      text: `${pName} (${f.key})`,
      value: f.key
    }
  })
})

// AG-Grid Cell Renderers (DOM Node based with unified typography & controls)
const PriorityCellRenderer = (params: any) => {
  const rowIdx = params.node?.rowIndex ?? 0
  const priorityVal = params.value || (rowIdx + 1)

  const div = document.createElement('div')
  div.className = 'grid-cell-priority-box'

  const pill = document.createElement('span')
  pill.className = 'grid-rank-pill'
  pill.textContent = `#${priorityVal}`

  const input = document.createElement('input')
  input.type = 'number'
  input.value = String(priorityVal)
  input.min = '1'
  input.className = 'grid-input-number'
  input.addEventListener('change', (e: any) => {
    const val = Number(e.target.value) || 1
    if (rules.value[rowIdx]) {
      rules.value[rowIdx].priority = val
      if (params.node) params.node.setDataValue('priority', val)
    }
  })

  div.appendChild(pill)
  div.appendChild(input)
  return div
}

const FieldSelectCellRenderer = (params: any) => {
  const rowIdx = params.node?.rowIndex ?? 0
  const opts = fieldOptions.value

  const div = document.createElement('div')
  div.className = 'grid-cell-select-box'

  const select = document.createElement('select')
  select.className = 'grid-select-control'

  opts.forEach((opt: any) => {
    const option = document.createElement('option')
    option.value = opt.value
    option.textContent = opt.text
    if (opt.value === params.value) option.selected = true
    select.appendChild(option)
  })

  select.addEventListener('change', (e: any) => {
    if (rules.value[rowIdx]) {
      rules.value[rowIdx].fieldKey = e.target.value
      if (params.node) params.node.setDataValue('fieldKey', e.target.value)
    }
  })

  div.appendChild(select)
  return div
}

const StrategySelectCellRenderer = (params: any) => {
  const rowIdx = params.node?.rowIndex ?? 0
  const opts = strategyOptions.value

  const div = document.createElement('div')
  div.className = 'grid-cell-select-box'

  const select = document.createElement('select')
  select.className = 'grid-select-control'

  opts.forEach((opt: any) => {
    const option = document.createElement('option')
    option.value = opt.value
    option.textContent = opt.text
    if (opt.value === params.value) option.selected = true
    select.appendChild(option)
  })

  select.addEventListener('change', (e: any) => {
    if (rules.value[rowIdx]) {
      rules.value[rowIdx].strategy = e.target.value
      if (params.node) params.node.setDataValue('strategy', e.target.value)
    }
  })

  div.appendChild(select)
  return div
}

const ActionsCellRenderer = (params: any) => {
  const rowIdx = params.node?.rowIndex ?? 0

  const div = document.createElement('div')
  div.className = 'grid-cell-actions-box'

  const btn = document.createElement('button')
  btn.className = 'grid-btn-delete'
  btn.innerHTML = `<span class="material-icons" style="font-size: 16px;">delete</span> ${t('delete')}`

  btn.addEventListener('click', () => {
    removeRule(rowIdx)
  })

  div.appendChild(btn)
  return div
}

const defaultColDef = {
  sortable: false,
  resizable: true
}

const columnDefs = computed(() => [
  {
    field: 'priority',
    headerName: t('survivorship.priority'),
    width: 140,
    cellRenderer: PriorityCellRenderer
  },
  {
    field: 'fieldKey',
    headerName: t('survivorship.field_key'),
    flex: 1,
    cellRenderer: FieldSelectCellRenderer
  },
  {
    field: 'strategy',
    headerName: t('survivorship.strategy'),
    flex: 1.5,
    cellRenderer: StrategySelectCellRenderer
  },
  {
    field: 'actions',
    headerName: t('actions'),
    width: 110,
    cellRenderer: ActionsCellRenderer
  }
])

const loadDomains = async () => {
  try {
    await domainStore.fetchDomains()
    if (domainOptions.value && domainOptions.value.length > 0 && !selectedDomainId.value) {
      selectedDomainId.value = String(domainOptions.value[0].value)
      fetchData()
    }
  } catch (e) {
    console.error('Failed to load domains', e)
    init({ message: t('survivorship.load_domains_fail'), color: 'danger' })
  }
}

const fetchData = async () => {
  if (!selectedDomainId.value) return
  try {
    const [rulesRes, fieldsRes] = await Promise.all([
      customFetch(`/api/records/domains/${selectedDomainId.value}/survivorship-rules`),
      customFetch(`/api/domains/${selectedDomainId.value}/fields`)
    ])
    rules.value = rulesRes || []
    domainFields.value = fieldsRes || []
  } catch (e) {
    rules.value = []
    domainFields.value = []
  }
}

const onDomainChange = () => {
  rules.value = []
  domainFields.value = []
  fetchData()
}

const addRule = () => {
  rules.value.push({
    fieldKey: domainFields.value[0]?.key || '',
    strategy: strategyOptions.value[0]?.value || 'SOURCE_PRIORITY',
    priority: rules.value.length + 1
  })
}

const removeRule = (index: number) => {
  rules.value.splice(index, 1)
}

const saveRules = async () => {
  if (!selectedDomainId.value) return
  isSaving.value = true
  try {
    const payload = rules.value.map(r => ({
      fieldKey: r.fieldKey,
      strategy: r.strategy,
      priority: Number(r.priority) || 1
    }))
    await customFetch(`/api/records/domains/${selectedDomainId.value}/survivorship-rules`, {
      method: 'PUT',
      body: payload
    })
    init({ message: t('survivorship.save_success'), color: 'success' })
    await fetchData()
  } catch (e) {
    init({ message: t('survivorship.save_fail'), color: 'danger' })
  } finally {
    isSaving.value = false
  }
}

onMounted(async () => {
  await codeStore.preloadGroups(['SURVIVORSHIP_STRATEGY'])
  loadDomains()
})
</script>

<style scoped>
/* Unified Typography & Styling System */
.survivorship-container {
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

.domain-select-input {
  min-width: 220px;
}

/* Executive Panel */
.executive-panel {
  background: var(--va-background-element);
  border: 1px solid var(--va-background-border);
  border-radius: 12px;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.panel-title-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.panel-title-text {
  font-family: inherit;
  font-weight: 700;
  font-size: 0.95rem;
  color: var(--va-text-primary);
  letter-spacing: -0.2px;
}

.panel-kpi-group {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.kpi-chip {
  background: var(--va-background-primary);
  border: 1px solid var(--va-background-border);
  border-radius: 20px;
  padding: 4px 12px;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.8rem;
}

.kpi-chip-label {
  color: var(--va-text-secondary);
  font-weight: 500;
}

.kpi-chip-value {
  font-weight: 700;
}

.primary-val { color: var(--va-primary); }
.info-val { color: var(--va-info); }
.success-val { color: var(--va-success); }

/* Strategy Cards Grid */
.strategy-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 0.85rem;
}

.strategy-card {
  background: var(--va-background-primary);
  border: 1px solid var(--va-background-border);
  border-radius: 10px;
  padding: 0.85rem 1rem;
  display: flex;
  gap: 0.75rem;
  align-items: flex-start;
  transition: all 0.2s ease;
}

.strategy-icon-box {
  padding: 0.45rem;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.bg-primary-subtle { background: rgba(25, 118, 210, 0.1); }
.bg-warning-subtle { background: rgba(237, 108, 2, 0.1); }
.bg-success-subtle { background: rgba(46, 125, 50, 0.1); }

.strategy-body {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.strategy-title {
  font-family: inherit;
  font-weight: 700;
  font-size: 0.88rem;
  color: var(--va-text-primary);
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.badge-tag {
  font-size: 0.7rem;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 4px;
}

.tag-primary { background: rgba(25, 118, 210, 0.15); color: var(--va-primary); }
.tag-warning { background: rgba(237, 108, 2, 0.15); color: var(--va-warning); }
.tag-success { background: rgba(46, 125, 50, 0.15); color: var(--va-success); }

.strategy-desc {
  font-family: inherit;
  font-size: 0.78rem;
  color: var(--va-text-secondary);
  line-height: 1.45;
  margin: 0;
}

/* Grid Card Section */
.grid-card-container {
  background: var(--va-background-primary);
  border: 1px solid var(--va-background-border);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.grid-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.85rem 1.25rem;
  border-bottom: 1px solid var(--va-background-border);
}

.grid-title-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.grid-title {
  font-family: inherit;
  font-weight: 700;
  font-size: 1rem;
  color: var(--va-text-primary);
}

.items-chip {
  font-size: 0.75rem;
}

.grid-body-section {
  padding: 1rem;
}

.ag-grid-wrapper {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--va-background-border);
  min-height: 300px;
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

<style>
/* AG-Grid Native Cell Control Overrides for Perfect Unified Typography */
.grid-cell-priority-box {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  height: 100%;
}

.grid-rank-pill {
  display: inline-block;
  padding: 3px 9px;
  background: var(--va-primary);
  color: #ffffff;
  border-radius: 12px;
  font-family: inherit;
  font-weight: 700;
  font-size: 0.78rem;
  line-height: 1;
}

.grid-input-number {
  width: 60px;
  padding: 5px 8px;
  border-radius: 6px;
  border: 1px solid var(--va-background-border);
  background: var(--va-background-primary);
  color: var(--va-text-primary);
  font-family: inherit;
  font-size: 0.85rem;
  font-weight: 700;
  text-align: center;
  outline: none;
  transition: border-color 0.2s ease;
}

.grid-input-number:focus {
  border-color: var(--va-primary);
}

.grid-cell-select-box {
  display: flex;
  align-items: center;
  height: 100%;
  width: 100%;
}

.grid-select-control {
  width: 100%;
  padding: 6px 10px;
  border-radius: 6px;
  border: 1px solid var(--va-background-border);
  background: var(--va-background-primary);
  color: var(--va-text-primary);
  font-family: inherit;
  font-size: 0.85rem;
  font-weight: 600;
  outline: none;
  cursor: pointer;
  transition: border-color 0.2s ease;
}

.grid-select-control:focus {
  border-color: var(--va-primary);
}

.grid-cell-actions-box {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.grid-btn-delete {
  border: none;
  background: rgba(229, 57, 53, 0.1);
  color: var(--va-danger);
  border-radius: 6px;
  padding: 5px 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  font-family: inherit;
  font-weight: 600;
  font-size: 0.78rem;
  transition: background 0.2s ease;
}

.grid-btn-delete:hover {
  background: rgba(229, 57, 53, 0.2);
}
</style>
