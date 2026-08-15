<template>
  <va-modal v-model="isOpen" size="large" :title="modalTitle" hide-default-actions>
    <div style="max-height: 70vh; overflow-y: auto; padding: 0.5rem 0.5rem 1.5rem 0.5rem;">
      <!-- Rule List -->
      <div v-if="!editingRule" style="display: flex; flex-direction: column; gap: 1rem;">
        <div v-if="loading" style="display: flex; justify-content: center; padding: 2rem;">
          <va-progress-circle indeterminate />
        </div>
        
        <div v-else-if="rules.length === 0" style="text-align: center; padding: 2rem; color: var(--va-text-secondary);">
          <va-icon name="rule" size="3rem" style="opacity: 0.4;" />
          <p style="margin-top: 1rem;">No DQ rules configured for this field.</p>
        </div>

        <div v-else>
          <div v-for="rule in rules" :key="rule.id"
               style="display: flex; align-items: center; gap: 0.75rem; padding: 0.75rem; border: 1px solid var(--va-background-border); border-radius: 8px; background: var(--va-background-element);">
            <va-checkbox v-model="rule.isActive" @update:modelValue="toggleRule(rule)" />
            <va-badge :text="rule.ruleType" :color="rule.severity === 'ERROR' ? 'danger' : 'warning'" />
            <span style="flex: 1; font-size: 0.9rem;">
              {{ getMessageText(rule.message) || getRuleDescription(rule) }}
            </span>
            <va-badge :text="rule.severity" :color="rule.severity === 'ERROR' ? 'danger' : 'warning'" style="opacity: 0.7;" />
            <va-button preset="plain" icon="edit" size="small" @click="startEdit(rule)" />
            <va-button preset="plain" icon="delete" size="small" color="danger" @click="deleteRule(rule)" />
          </div>
        </div>

        <div style="display: flex; justify-content: flex-end; margin-top: 0.5rem;">
          <va-button icon="add" @click="startCreate()" size="small">Add Rule</va-button>
        </div>
      </div>

      <!-- Rule Editor -->
      <div v-else style="display: flex; flex-direction: column; gap: 1rem;">
        <va-select
          v-model="editForm.ruleType"
          :options="ruleTypeOptions"
          label="Rule Type"
          :disabled="!!editForm.id"
        />

        <va-select
          v-model="editForm.severity"
          :options="['ERROR', 'WARNING']"
          label="Severity"
        />

        <!-- Dynamic params based on rule type -->
        <template v-if="editForm.ruleType === 'REGEX'">
          <va-input v-model="editParams.pattern" label="Regex Pattern" placeholder="^[A-Z]{2}\d{4}$" />
        </template>
        <template v-else-if="editForm.ruleType === 'RANGE'">
          <div style="display: flex; gap: 1rem;">
            <va-input v-model.number="editParams.min" label="Min" type="number" style="flex: 1;" />
            <va-input v-model.number="editParams.max" label="Max" type="number" style="flex: 1;" />
          </div>
        </template>
        <template v-else-if="editForm.ruleType === 'LENGTH'">
          <div style="display: flex; gap: 1rem;">
            <va-input v-model.number="editParams.minLength" label="Min Length" type="number" style="flex: 1;" />
            <va-input v-model.number="editParams.maxLength" label="Max Length" type="number" style="flex: 1;" />
          </div>
        </template>
        <template v-else-if="editForm.ruleType === 'ENUM'">
          <va-input v-model="editParams.valuesStr" label="Allowed Values (comma separated)" placeholder="KR, US, JP" />
        </template>
        <template v-else-if="editForm.ruleType === 'DATE_RANGE'">
          <div style="display: flex; gap: 1rem;">
            <va-input v-model="editParams.after" label="After (yyyy-MM-dd)" placeholder="2020-01-01" style="flex: 1;" />
            <va-input v-model="editParams.before" label="Before (yyyy-MM-dd)" placeholder="2030-12-31" style="flex: 1;" />
          </div>
        </template>
        <template v-else-if="editForm.ruleType === 'CROSS_FIELD'">
          <div style="display: flex; gap: 1rem;">
            <va-input v-model="editParams.targetFieldKey" label="Target Field Key" style="flex: 1;" />
            <va-select v-model="editParams.operator" :options="['==', '!=', '>', '>=', '<', '<=']" label="Operator" style="flex: 1;" />
          </div>
        </template>
        <template v-else-if="editForm.ruleType === 'CUSTOM_SPEL'">
          <va-input v-model="editParams.expression" label="SpEL Expression" placeholder="#value.contains('@')" />
        </template>

        <va-input v-model="editForm.messageEn" label="Error Message (EN)" />
        <va-input v-model="editForm.messageKo" label="Error Message (KO)" />
        <va-input v-model.number="editForm.sortOrder" label="Sort Order" type="number" />

        <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem;">
          <va-button preset="secondary" @click="cancelEdit()">Cancel</va-button>
          <va-button @click="saveRule()" :loading="saving">{{ editForm.id ? 'Update' : 'Create' }}</va-button>
        </div>
      </div>
    </div>
  </va-modal>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'

const props = defineProps({
  modelValue: Boolean,
  fieldId: String,
  fieldName: String,
})
const emit = defineEmits(['update:modelValue'])

const isOpen = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
})

const modalTitle = computed(() => `DQ Rules: ${props.fieldName || 'Field'}`)

const ruleTypeOptions = ['NOT_NULL', 'REGEX', 'RANGE', 'LENGTH', 'ENUM', 'DATE_RANGE', 'UNIQUE', 'CROSS_FIELD', 'CUSTOM_SPEL', 'BUSINESS_NO_CHECKSUM', 'CORPORATE_NO_CHECKSUM']

const rules = ref([])
const loading = ref(false)
const saving = ref(false)
const editingRule = ref(false)

const editForm = reactive({
  id: null,
  ruleType: 'NOT_NULL',
  severity: 'ERROR',
  messageEn: '',
  messageKo: '',
  sortOrder: 0,
})

const editParams = reactive({
  pattern: '',
  min: null,
  max: null,
  minLength: null,
  maxLength: null,
  valuesStr: '',
  after: '',
  before: '',
  targetFieldKey: '',
  operator: '>=',
  expression: '',
})

// Fetch rules when fieldId changes or modal opens
watch(() => [props.fieldId, props.modelValue], async ([fieldId, open]) => {
  if (open && fieldId) {
    await fetchRules()
  }
}, { immediate: true })

const token = useCookie('auth_token')
const getHeaders = () => (token.value ? { Authorization: `Bearer ${token.value}` } : {})

async function fetchRules() {
  if (!props.fieldId) return
  loading.value = true
  try {
    const res = await $fetch(`/api/fields/${props.fieldId}/dq-rules`, { headers: getHeaders() })
    rules.value = res
  } catch (e) {
    console.error('Failed to fetch DQ rules:', e)
  } finally {
    loading.value = false
  }
}

function startCreate() {
  editingRule.value = true
  editForm.id = null
  editForm.ruleType = 'NOT_NULL'
  editForm.severity = 'ERROR'
  editForm.messageEn = ''
  editForm.messageKo = ''
  editForm.sortOrder = rules.value.length
  resetEditParams()
}

function startEdit(rule) {
  editingRule.value = true
  editForm.id = rule.id
  editForm.ruleType = rule.ruleType
  editForm.severity = rule.severity
  editForm.messageEn = rule.message?.en || ''
  editForm.messageKo = rule.message?.ko || ''
  editForm.sortOrder = rule.sortOrder || 0
  resetEditParams()

  // Parse params
  if (rule.params) {
    try {
      const p = typeof rule.params === 'string' ? JSON.parse(rule.params) : rule.params
      editParams.pattern = p.pattern || ''
      editParams.min = p.min ?? null
      editParams.max = p.max ?? null
      editParams.minLength = p.minLength ?? null
      editParams.maxLength = p.maxLength ?? null
      editParams.valuesStr = p.values ? p.values.join(', ') : ''
      editParams.after = p.after || ''
      editParams.before = p.before || ''
      editParams.targetFieldKey = p.targetFieldKey || ''
      editParams.operator = p.operator || '>='
      editParams.expression = p.expression || ''
    } catch (e) { /* ignore parse error */ }
  }
}

function cancelEdit() {
  editingRule.value = false
}

function resetEditParams() {
  editParams.pattern = ''
  editParams.min = null
  editParams.max = null
  editParams.minLength = null
  editParams.maxLength = null
  editParams.valuesStr = ''
  editParams.after = ''
  editParams.before = ''
  editParams.targetFieldKey = ''
  editParams.operator = '>='
  editParams.expression = ''
}

function buildParams() {
  const type = editForm.ruleType
  if (type === 'NOT_NULL' || type === 'UNIQUE' || type === 'BUSINESS_NO_CHECKSUM' || type === 'CORPORATE_NO_CHECKSUM') return null
  if (type === 'REGEX') return JSON.stringify({ pattern: editParams.pattern })
  if (type === 'RANGE') {
    const p = {}
    if (editParams.min !== null && editParams.min !== '') p.min = Number(editParams.min)
    if (editParams.max !== null && editParams.max !== '') p.max = Number(editParams.max)
    return JSON.stringify(p)
  }
  if (type === 'LENGTH') {
    const p = {}
    if (editParams.minLength !== null && editParams.minLength !== '') p.minLength = Number(editParams.minLength)
    if (editParams.maxLength !== null && editParams.maxLength !== '') p.maxLength = Number(editParams.maxLength)
    return JSON.stringify(p)
  }
  if (type === 'ENUM') {
    const values = editParams.valuesStr.split(',').map(v => v.trim()).filter(Boolean)
    return JSON.stringify({ values })
  }
  if (type === 'DATE_RANGE') {
    const p = {}
    if (editParams.after) p.after = editParams.after
    if (editParams.before) p.before = editParams.before
    return JSON.stringify(p)
  }
  if (type === 'CROSS_FIELD') {
    return JSON.stringify({ targetFieldKey: editParams.targetFieldKey, operator: editParams.operator })
  }
  if (type === 'CUSTOM_SPEL') {
    return JSON.stringify({ expression: editParams.expression })
  }
  return null
}

async function saveRule() {
  saving.value = true
  try {
    const message = {}
    if (editForm.messageEn) message.en = editForm.messageEn
    if (editForm.messageKo) message.ko = editForm.messageKo

    const body = {
      fieldDefinitionId: props.fieldId,
      ruleType: editForm.ruleType,
      severity: editForm.severity,
      params: buildParams(),
      message: Object.keys(message).length > 0 ? message : null,
      sortOrder: editForm.sortOrder,
      isActive: true,
    }

    const headers = getHeaders()
    if (editForm.id) {
      await $fetch(`/api/dq-rules/${editForm.id}`, { method: 'PUT', headers, body })
    } else {
      await $fetch(`/api/fields/${props.fieldId}/dq-rules`, { method: 'POST', headers, body })
    }

    editingRule.value = false
    await fetchRules()
  } catch (e) {
    console.error('Failed to save DQ rule:', e)
  } finally {
    saving.value = false
  }
}

async function toggleRule(rule) {
  try {
    await $fetch(`/api/dq-rules/${rule.id}`, {
      method: 'PUT',
      headers: getHeaders(),
      body: { isActive: rule.isActive }
    })
  } catch (e) {
    console.error('Failed to toggle rule:', e)
  }
}

async function deleteRule(rule) {
  try {
    await $fetch(`/api/dq-rules/${rule.id}`, { method: 'DELETE', headers: getHeaders() })
    await fetchRules()
  } catch (e) {
    console.error('Failed to delete rule:', e)
  }
}

function getMessageText(message) {
  if (!message) return ''
  return message.en || message.ko || ''
}

function getRuleDescription(rule) {
  const type = rule.ruleType
  if (type === 'NOT_NULL') return 'Value is required'
  if (type === 'UNIQUE') return 'Must be unique within domain'
  if (!rule.params) return type
  try {
    const p = typeof rule.params === 'string' ? JSON.parse(rule.params) : rule.params
    if (type === 'REGEX') return `Pattern: ${p.pattern}`
    if (type === 'RANGE') return `Range: ${p.min ?? '∞'} ~ ${p.max ?? '∞'}`
    if (type === 'LENGTH') return `Length: ${p.minLength ?? 0} ~ ${p.maxLength ?? '∞'}`
    if (type === 'ENUM') return `Values: ${p.values?.join(', ')}`
    if (type === 'DATE_RANGE') return `Date: ${p.after ?? '∞'} ~ ${p.before ?? '∞'}`
    if (type === 'CROSS_FIELD') return `${p.operator} ${p.targetFieldKey}`
    if (type === 'CUSTOM_SPEL') return `SpEL: ${p.expression}`
  } catch (e) { /* ignore */ }
  return type
}
</script>
