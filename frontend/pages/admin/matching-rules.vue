<template>
  <div class="matching-rules-container p-4">
    <!-- Header -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
      <div>
        <h1 style="font-size: 1.75rem; font-weight: 800; color: var(--va-text-primary); margin: 0; display: flex; align-items: center; gap: 0.5rem;">
          <va-icon name="rule" color="primary" size="32px" />
          <span>{{ t('matching_rules.title') || '매칭 규칙 관리 (Matching Rules)' }}</span>
        </h1>
        <p style="color: var(--va-text-secondary); margin: 0.25rem 0 0 0; font-size: 0.9rem;">
          도메인별 중복 레코드 판별을 위한 EXACT / FUZZY 매칭 규칙과 정탐률/오탐률 기반 임계값 튜닝 지표를 제공합니다.
        </p>
      </div>

      <div style="display: flex; gap: 0.75rem; align-items: center;">
        <va-select
          v-model="selectedDomainId"
          :options="domainOptions"
          value-by="value"
          label-by="label"
          placeholder="도메인 선택"
          style="min-width: 220px;"
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
      </div>
    </div>

    <!-- Feedback Summary Cards -->
    <div v-if="feedbackSummaries.length > 0" class="row" style="margin-bottom: 1.5rem; row-gap: 1rem;">
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
            <div style="font-size: 0.8rem; color: var(--va-text-secondary); background: var(--va-background-element); padding: 0.5rem; border-radius: 4px; margin-top: 0.5rem;">
              <div>현재 Threshold: <strong>{{ stats.currentThreshold }}</strong></div>
              <div v-if="stats.recommendedThreshold">추천 Threshold: <strong style="color: var(--va-primary);">{{ stats.recommendedThreshold }}</strong></div>
              <div v-if="stats.recommendation" style="margin-top: 0.25rem; font-weight: 600; color: var(--va-primary);">💡 {{ stats.recommendation }}</div>
            </div>
          </va-card-content>
        </va-card>
      </div>
    </div>

    <!-- Rules Data Table -->
    <va-card>
      <va-card-title>
        <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
          <span>매칭 규칙 목록 (총 {{ rules.length }}건)</span>
          <va-button preset="secondary" icon="refresh" size="small" @click="fetchData">새로고침</va-button>
        </div>
      </va-card-title>
      <va-card-content>
        <va-data-table
          :items="rules"
          :columns="columns"
          striped
          hoverable
          :no-data-html="selectedDomainId ? '등록된 매칭 규칙이 없습니다.' : '상단에서 도메인을 선택해주세요.'"
        >
          <template #cell(ruleName)="{ rowData }">
            <span style="font-weight: 700; color: var(--va-primary);">{{ rowData.ruleName }}</span>
          </template>

          <template #cell(matchType)="{ rowData }">
            <va-badge :text="rowData.matchType" :color="rowData.matchType === 'EXACT' ? 'info' : 'warning'" />
          </template>

          <template #cell(targetFieldKeys)="{ rowData }">
            <span style="font-family: monospace; font-size: 0.85rem;">{{ parseFields(rowData.targetFieldKeys) }}</span>
          </template>

          <template #cell(similarityThreshold)="{ rowData }">
            <span>{{ rowData.similarityThreshold != null ? rowData.similarityThreshold : '-' }}</span>
          </template>

          <template #cell(isActive)="{ rowData }">
            <va-badge :text="rowData.isActive ? '활성 (Active)' : '비활성'" :color="rowData.isActive ? 'success' : 'secondary'" />
          </template>

          <template #cell(actions)="{ rowData }">
            <div style="display: flex; gap: 0.25rem;">
              <va-button preset="plain" icon="edit" color="primary" size="small" @click="openEditModal(rowData)" />
              <va-button preset="plain" icon="delete" color="danger" size="small" @click="deleteRule(rowData)" />
            </div>
          </template>
        </va-data-table>
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
          label-by="label"
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
          <va-button color="primary" @click="saveRule" :loading="isSaving">저장</va-button>
        </div>
      </template>
    </va-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast, useModal } from 'vuestic-ui'
import { useCustomFetch } from '~/composables/useCustomFetch'

const { t } = useI18n()
const { init } = useToast()
const { confirm } = useModal()
const { customFetch } = useCustomFetch()

const selectedDomainId = ref('')
const domainOptions = ref([])
const domainFieldOptions = ref([])

const rules = ref([])
const feedbackSummaries = ref([])

const showModal = ref(false)
const isEditMode = ref(false)
const isSaving = ref(false)
const editingRuleId = ref(null)

const form = ref({
  ruleName: '',
  matchType: 'EXACT',
  selectedFields: [],
  targetFieldKeysInput: '',
  similarityThreshold: 0.85,
  isActive: true
})

const columns = computed(() => [
  { key: 'ruleName', label: '규칙명' },
  { key: 'matchType', label: '매칭 방식', width: 120 },
  { key: 'targetFieldKeys', label: '대상 필드' },
  { key: 'similarityThreshold', label: '유사도 Threshold', width: 150 },
  { key: 'isActive', label: '상태', width: 130 },
  { key: 'actions', label: '액션', width: 90 }
])

const parseFields = (raw) => {
  if (!raw) return '-'
  try {
    const parsed = typeof raw === 'string' ? JSON.parse(raw) : raw
    if (Array.isArray(parsed)) return parsed.join(', ')
    return String(parsed)
  } catch (e) {
    return String(raw)
  }
}

const loadDomains = async () => {
  try {
    const res = await customFetch('/api/domains')
    const list = res?.content || res || []
    domainOptions.value = list.map(d => ({
      label: d.name || d.domainName,
      value: d.id
    }))
    if (domainOptions.value.length > 0 && !selectedDomainId.value) {
      selectedDomainId.value = domainOptions.value[0].value
      onDomainChange()
    }
  } catch (e) {
    domainOptions.value = []
  }
}

const loadDomainFields = async () => {
  if (!selectedDomainId.value) return
  try {
    const res = await customFetch(`/api/domains/${selectedDomainId.value}/fields`)
    const list = res || []
    domainFieldOptions.value = list.map(f => ({
      label: `${f.name} (${f.key})`,
      value: f.key
    }))
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

const openEditModal = (rule) => {
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

const deleteRule = async (rule) => {
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
