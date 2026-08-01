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
            도메인별 중복 레코드 판별을 위한 EXACT / FUZZY 매칭 규칙 및 유사도 임계값을 설정합니다.
          </span>
        </div>
      </div>

      <div class="header-controls-section">
        <va-select
          v-model="selectedDomainId"
          :options="domainOptions"
          value-by="value"
          text-by="text"
          placeholder="도메인 선택"
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
          규칙 추가
        </va-button>
        <va-button
          preset="secondary"
          icon="refresh"
          :disabled="!selectedDomainId"
          @click="fetchData"
        >
          새로고침
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
              <span>총 검토 건수: <strong>{{ stats.totalReviewed || 0 }}건</strong></span>
              <span>정탐률: <strong>{{ ((stats.precision || 0) * 100).toFixed(1) }}%</strong></span>
            </div>
            <div style="display: flex; gap: 0.5rem; margin-bottom: 0.5rem; font-size: 0.8rem;">
              <va-chip color="success" size="small">정탐(Confirmed): {{ stats.confirmedCount || 0 }}</va-chip>
              <va-chip color="danger" size="small">오탐(Rejected): {{ stats.rejectedCount || 0 }}</va-chip>
            </div>
            <div style="font-size: 0.8rem; color: var(--va-text-secondary); background: var(--va-background-element); padding: 0.5rem; border-radius: 6px; margin-top: 0.5rem;">
              <div>현재 Threshold: <strong>{{ stats.currentThreshold }}</strong></div>
              <div v-if="stats.recommendedThreshold">추천 Threshold: <strong style="color: var(--va-primary);">{{ stats.recommendedThreshold }}</strong></div>
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
          <span style="color: var(--va-text-primary);">매칭 규칙 목록</span>
          <va-chip v-if="selectedDomainId" size="small" color="primary">{{ rules.length }}개 항목</va-chip>
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
          />
        </div>

        <!-- Empty State Component -->
        <div v-else class="empty-state-box">
          <va-icon name="compare" size="52px" color="secondary" class="mb-3" />
          <h3 class="empty-state-title">
            {{ selectedDomainId ? '등록된 매칭 규칙이 없습니다.' : '상단 드롭다운에서 도메인을 먼저 선택해 주세요.' }}
          </h3>
          <p class="empty-state-desc">
            {{ selectedDomainId ? '우측 상단의 "+ 규칙 추가" 버튼을 클릭하여 중복 레코드 판별을 위한 새로운 매칭 규칙을 생성하세요.' : '도메인을 선택하면 해당 도메인의 중복 레코드 판단 규칙 목록이 AG-Grid에 표시됩니다.' }}
          </p>
          <va-button v-if="selectedDomainId" color="primary" icon="add" size="small" @click="openCreateModal">
            첫 번째 매칭 규칙 추가하기
          </va-button>
        </div>
      </va-card-content>
    </va-card>

    <!-- Rule Form Modal -->
    <va-modal
      v-model="showModal"
      :title="isEditMode ? '매칭 규칙 수정' : '새 매칭 규칙 추가'"
      hide-default-actions
      size="medium"
      no-outside-dismiss
    >
      <div style="padding: 0.5rem 0;">
        <va-input
          v-model="form.ruleName"
          label="규칙명 (Rule Name)"
          placeholder="예: 이름 및 연락처 일치 규칙"
          class="mb-3"
          required
        />

        <va-select
          v-model="form.matchType"
          :options="['EXACT', 'FUZZY']"
          label="매칭 방식 (Match Type)"
          class="mb-3"
        />

        <va-select
          v-if="domainFieldOptions.length > 0"
          v-model="form.selectedFields"
          :options="domainFieldOptions"
          value-by="value"
          text-by="text"
          multiple
          label="대상 필드 다중 선택 (Target Fields)"
          class="mb-3"
        />
        <va-input
          v-else
          v-model="form.targetFieldKeysInput"
          label="대상 필드 키 (comma separated)"
          placeholder="email, phone"
          class="mb-3"
        />

        <va-input
          v-if="form.matchType === 'FUZZY'"
          v-model.number="form.similarityThreshold"
          type="number"
          step="0.05"
          min="0.5"
          max="1.0"
          label="유사도 임계값 (Similarity Threshold 0.5 ~ 1.0)"
          class="mb-3"
        />

        <va-checkbox
          v-model="form.isActive"
          label="규칙 활성화 여부 (Is Active)"
          class="mt-2"
        />
      </div>

      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem;">
          <va-button preset="secondary" @click="showModal = false">취소</va-button>
          <va-button color="primary" :loading="isSaving" @click="saveRule">저장</va-button>
        </div>
      </template>
    </va-modal>
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

const { t } = useI18n()
const { pageTitle } = usePageTitle('matching_rules.title', '매칭 규칙 관리')
const { init } = useToast()
const { confirm } = useModal()
const { customFetch } = useCustomFetch()
const { gridTheme, isDark } = useAgGridTheme()

const domainStore = useDomain()
const selectedDomainId = ref('')
const domainOptions = computed(() => domainStore.domainOptions.value)
const domainFieldOptions = ref<any[]>([])

const rules = ref<any[]>([])
const feedbackSummaries = ref<any[]>([])

const showModal = ref(false)
const isEditMode = ref(false)
const isSaving = ref(false)
const editingRuleId = ref<any>(null)

const form = ref({
  ruleName: '',
  matchType: 'EXACT',
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
  pill.textContent = params.value || 'EXACT'

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
  pill.textContent = isActive ? '활성 (Active)' : '비활성'

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
    headerName: '규칙명',
    flex: 1.2,
    cellRenderer: RuleNameCellRenderer
  },
  {
    field: 'matchType',
    headerName: '매칭 방식',
    width: 130,
    cellRenderer: MatchTypeCellRenderer
  },
  {
    field: 'targetFieldKeys',
    headerName: '대상 필드',
    flex: 1.5,
    cellRenderer: TargetFieldsCellRenderer
  },
  {
    field: 'similarityThreshold',
    headerName: '유사도 Threshold',
    width: 160,
    cellRenderer: SimilarityCellRenderer
  },
  {
    field: 'isActive',
    headerName: '상태',
    width: 140,
    cellRenderer: IsActiveCellRenderer
  },
  {
    field: 'actions',
    headerName: '작업',
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
    matchType: 'EXACT',
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
    init({ message: '규칙명을 입력해주세요.', color: 'warning' })
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
      init({ message: '매칭 규칙이 수정되었습니다.', color: 'success' })
    } else {
      await customFetch(`/api/domains/${selectedDomainId.value}/matching-rules`, {
        method: 'POST',
        body: payload
      })
      init({ message: '새 매칭 규칙이 생성되었습니다.', color: 'success' })
    }
    showModal.value = false
    fetchData()
  } catch (e) {
    init({ message: '매칭 규칙 저장에 실패했습니다.', color: 'danger' })
  } finally {
    isSaving.value = false
  }
}

const deleteRule = async (rule: any) => {
  const isConfirmed = await confirm({
    title: '매칭 규칙 삭제',
    message: `[${rule.ruleName}] 규칙을 정말 삭제하시겠습니까?`,
    okText: '삭제',
    cancelText: '취소'
  })
  if (!isConfirmed) return

  try {
    await customFetch(`/api/domains/${selectedDomainId.value}/matching-rules/${rule.id}`, {
      method: 'DELETE'
    })
    init({ message: '매칭 규칙이 삭제되었습니다.', color: 'success' })
    fetchData()
  } catch (e) {
    init({ message: '매칭 규칙 삭제 실패', color: 'danger' })
  }
}

onMounted(() => {
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
