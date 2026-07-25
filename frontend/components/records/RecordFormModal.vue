<template>
  <va-modal
    v-model="modalVisible"
    hide-default-actions
    :prevent-click-outside="true"
    :no-outside-dismiss="true"
    class="custom-record-modal"
  >
    <template #header>
      <div class="custom-modal-header-wrapper" style="display: flex; align-items: center; justify-content: space-between; width: 100%; min-height: 24px; padding-top: 0; margin-top: -20px !important; margin-left: -20px !important;">
        <h3 style="margin: 0; padding: 0; font-size: 1.05rem; font-weight: 800; color: var(--va-text-primary); text-transform: uppercase; display: inline-flex; align-items: center; gap: 0.4rem; line-height: 1;">
          <va-icon name="edit_note" color="primary" size="20px" style="display: inline-flex; align-items: center;" />
          <span style="display: inline-flex; align-items: center;">{{ modalTitle }}</span>
        </h3>
      </div>
    </template>







    <div style="max-height: 60vh; overflow-y: auto; overflow-x: hidden; padding: 1rem; box-sizing: border-box; width: 100%;">
      <div
        v-if="!hasWorkflow"
        style="margin-bottom: 1rem; padding: 0.5rem; background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; border-radius: 4px; text-align: center; font-weight: bold;"
      >
        {{ isEdit 
          ? 'This classification node does not have an UPDATE workflow configured. You cannot save records.' 
          : 'This classification node does not have a CREATE workflow configured. You cannot save records.' 
        }}
      </div>

      <!-- Sector Tabs -->
      <va-tabs v-model="activeSectorTab" style="margin-bottom: 1rem;">
        <template #tabs>
          <va-tab v-for="(sector, idx) in groupedFieldsArray" :key="sector.key" :name="idx">
            {{ sector.label }}
          </va-tab>
        </template>
      </va-tabs>

      <!-- Sector Content -->
      <div v-for="(sector, idx) in groupedFieldsArray" :key="sector.key" v-show="activeSectorTab === idx">
        <va-accordion multiple style="width: 100%;" class="mb-4">
          <va-collapse
            v-for="group in sector.groups"
            :key="group.key"
            :header="group.label"
            v-model="group.isOpen"
            solid
            color="background-element"
            style="margin-bottom: 0.5rem;"
          >
            <div style="padding: 0.5rem 1rem; overflow: visible; box-sizing: border-box;">
              <div class="row" style="row-gap: 1.25rem; margin: 0 -0.5rem; display: flex; flex-wrap: wrap;">
                <template v-for="field in group.fields" :key="field.id">
                  <div
                    v-if="evalConditionRule(field, localRecord).show"
                    :class="['flex', 'xs' + (field.gridWidth || 12)]"
                    :data-field-key="field.key"
                    style="padding: 0 0.5rem; min-width: 0; margin-bottom: 0.5rem;"
                  >
                    <div style="display: flex; flex-direction: column; gap: 0.25rem; width: 100%; box-sizing: border-box; min-width: 0; --va-input-font-size: 0.9rem;">
                      <!-- External Label -->
                      <span :style="{ fontSize: '0.75rem', color: evalConditionRule(field, localRecord).highlight ? 'var(--va-primary)' : 'var(--va-text-secondary)', fontWeight: evalConditionRule(field, localRecord).highlight ? '800' : '600', textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: '4px', minHeight: '18px', lineHeight: '18px' }">
                        <va-icon v-if="evalConditionRule(field, localRecord).highlight" name="star" size="small" color="primary" />
                        {{ getTranslatedName(field.name) }}{{ evalConditionRule(field, localRecord).required ? ' *' : '' }}{{ field.type === 'CALCULATED' ? ' (계산됨)' : '' }}
                      </span>

                      <!-- Text / Number / Date -->
                      <va-input
                        v-if="['TEXT', 'NUMBER', 'DECIMAL', 'FLOAT', 'INTEGER', 'DATE'].includes(field.type)"
                        v-model="localRecord[field.key]"
                        :type="field.type === 'DATE' ? (focusedDateFields[field.key] || localRecord[field.key] ? 'date' : 'text') : (['NUMBER', 'DECIMAL', 'FLOAT', 'INTEGER'].includes(field.type) ? 'number' : 'text')"
                        :readonly="evalConditionRule(field, localRecord).readOnly"
                        :disabled="isAutoNumberingField(field) || evalConditionRule(field, localRecord).disabled"
                        :lang="locale === 'en' ? 'en-US' : 'ko-KR'"
                        :placeholder="isAutoNumberingField(field) ? (locale === 'en' ? 'Auto-generated on final approval' : '자동 채번됩니다 (최종 승인 시)') : (field.type === 'DATE' ? (locale === 'en' ? 'YYYY-MM-DD' : '연도-월-일') : '')"
                        class="w-full"
                        @focus="focusedDateFields[field.key] = true"
                        @blur="focusedDateFields[field.key] = false"
                      />

                      <!-- Multilingual -->
                      <div v-else-if="field.type === 'MULTILINGUAL'" class="w-full" style="display: flex; gap: 0.5rem; flex-direction: row; min-width: 0;">
                        <va-input v-model="localRecord[field.key].ko" style="flex: 1; min-width: 0;" class="slim-multilingual-input" :readonly="evalConditionRule(field, localRecord).readOnly">
                          <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">{{ locale === 'en' ? 'Korean' : '한국어' }}</span></template>
                        </va-input>
                        <va-input v-model="localRecord[field.key].en" style="flex: 1; min-width: 0;" class="slim-multilingual-input" :readonly="evalConditionRule(field, localRecord).readOnly">
                          <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">{{ locale === 'en' ? 'English' : '영어' }}</span></template>
                        </va-input>
                      </div>

                      <!-- Calculated -->
                      <va-input
                        v-else-if="field.type === 'CALCULATED'"
                        v-model="localRecord[field.key]"
                        readonly
                        class="w-full"
                        style="background-color: #f4f6f8;"
                      />

                      <!-- Select -->
                      <va-select
                        v-else-if="['SELECT', 'MULTI_SELECT'].includes(field.type)"
                        v-model="localRecord[field.key]"
                        :options="parseOptions(field.options)"
                        :multiple="field.type === 'MULTI_SELECT' || field.isMultiValue"
                        value-by="value"
                        class="w-full"
                        :readonly="evalConditionRule(field, localRecord).readOnly"
                      />

                      <!-- Domain Reference -->
                      <div v-else-if="field.type === 'DOMAIN_REFERENCE'" class="w-full" style="display: flex; gap: 0.5rem; align-items: center;">
                        <va-input
                          :model-value="getDomainRefDisplayName(field.key, localRecord[field.key])"
                          readonly
                          style="flex: 1;"
                        />
                        <va-button icon="search" @click="$emit('openDomainRef', { fieldKey: field.key, isCreate: !isEdit })" />
                      </div>

                      <!-- Checkbox / Boolean -->
                      <va-checkbox
                        v-else-if="field.type === 'BOOLEAN'"
                        v-model="localRecord[field.key]"
                        class="w-full"
                        :readonly="evalConditionRule(field, localRecord).readOnly"
                      />

                      <!-- File Upload -->
                      <div v-else-if="field.type === 'FILE'" class="w-full">
                        <va-file-upload v-model="localRecord[field.key]" :type="field.isMultiValue ? 'list' : 'single'" dropzone class="w-full file-upload-wrapper">
                          <div style="display: flex; flex-direction: row; align-items: center; gap: 1rem; padding: 0.5rem; justify-content: center; width: 100%;">
                            <span style="font-size: 0.9rem; color: #666;">여기로 파일을 드래그 하거나</span>
                            <va-button size="small">내 PC에서 선택</va-button>
                          </div>
                        </va-file-upload>
                        <transition-group name="flip-list" tag="div" v-if="localRecord[field.key] && localRecord[field.key].length > 0" class="custom-file-list" @dragover.prevent>
                          <div
                            v-for="(fileObj, i) in localRecord[field.key]"
                            :key="fileObj.url || fileObj.name"
                            class="custom-file-item"
                            :draggable="field.isMultiValue"
                            @dragstart="onDragStart($event, i, localRecord[field.key])"
                            @dragenter.prevent="onDragEnter($event, i, localRecord[field.key])"
                            @dragover.prevent
                            @drop.prevent="onDrop($event, i, localRecord[field.key])"
                            @dragend="onDragEnd($event)"
                            :style="field.isMultiValue ? 'cursor: grab;' : ''"
                          >
                            <div class="custom-file-info" style="display: flex; align-items: center;">
                              <va-icon v-if="field.isMultiValue" name="drag_indicator" style="color: #666; margin-right: 8px; cursor: grab;" />
                              {{ fileObj.name || extractFilename(fileObj.url || fileObj) }}
                            </div>
                            <div class="custom-file-actions">
                              <va-icon name="delete" style="cursor: pointer; color: #E53935;" @click="removeFile(localRecord[field.key], i)" />
                            </div>
                          </div>
                        </transition-group>
                      </div>
                    </div>
                  </div>
                </template>
              </div>
            </div>
          </va-collapse>
        </va-accordion>
      </div>
    </div>
    <div style="display: flex; justify-content: flex-end; margin-top: 1rem; gap: 0.5rem;">
      <va-button
        color="success"
        :disabled="!hasWorkflow"
        @click="handleSave"
      >
        {{ isEdit ? 'Save' : 'Create & Submit for Approval' }}
      </va-button>
      <va-button preset="secondary" @click="handleClose">Cancel</va-button>
    </div>
  </va-modal>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useCookie } from '#app'

const props = defineProps({
  show: { type: Boolean, default: false },
  isEdit: { type: Boolean, default: false },
  record: { type: Object, default: () => ({}) },
  fields: { type: Array, default: () => [] },
  nodeLabel: { type: String, default: '' },
  hasWorkflow: { type: Boolean, default: true },
  selectedDomainInfo: { type: Object, default: null },
  domainReferences: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['close', 'save', 'openDomainRef', 'update:show'])

const localeCookie = useCookie('locale', { default: () => 'ko' })
const locale = computed(() => localeCookie.value || 'ko')

const modalVisible = computed({
  get: () => props.show,
  set: (val) => {
    emit('update:show', val)
    if (!val) emit('close')
  }
})

const modalTitle = computed(() => {
  if (props.isEdit) {
    return props.nodeLabel ? `Edit Record - ${props.nodeLabel}` : 'Edit Record'
  }
  return props.nodeLabel ? `Create Record in ${props.nodeLabel}` : 'Create Record'
})

const activeSectorTab = ref(0)
const focusedDateFields = ref({})
const localRecord = ref({})

watch(
  () => props.record,
  (newVal) => {
    localRecord.value = newVal || {}
  },
  { immediate: true, deep: true }
)

const handleClose = () => {
  emit('close')
}

const handleSave = () => {
  emit('save', { isEdit: props.isEdit, record: localRecord.value })
}

const isAutoNumberingField = (field) => {
  if (!props.selectedDomainInfo || !field) return false
  return (
    field.id === props.selectedDomainInfo.identifierFieldId &&
    props.selectedDomainInfo.numberingPattern &&
    props.selectedDomainInfo.numberingPattern.trim() !== ''
  )
}

const parseName = (nameObj) => {
  if (!nameObj) return null
  if (typeof nameObj === 'string') {
    try { return JSON.parse(nameObj) } catch (e) { return null }
  }
  return nameObj
}

const getTranslatedName = (nameObj) => {
  const pName = parseName(nameObj)
  return pName?.[locale.value] || pName?.ko || pName?.en || ''
}

const groupedFieldsArray = computed(() => {
  const map = new Map()
  const sortedFields = [...(props.fields || [])].sort((a, b) => (a.order || 0) - (b.order || 0))

  sortedFields.forEach((f) => {
    const sObj = f.fieldGroup?.sector
    const gObj = f.fieldGroup

    const sName = getTranslatedName(sObj?.name) || (locale.value === 'ko' ? '일반' : 'General')
    const sKey = sObj?.id || 'default'
    const sOrder = sObj?.sortOrder || 0

    const gName = getTranslatedName(gObj?.name) || (locale.value === 'ko' ? '기본 필드' : 'Fields')
    const gKey = gObj?.id || 'default'
    const gOrder = gObj?.sortOrder || 0

    if (!map.has(sKey)) {
      map.set(sKey, { key: sKey, label: sName, order: sOrder, groups: new Map() })
    }
    const sectorObj = map.get(sKey)

    if (!sectorObj.groups.has(gKey)) {
      sectorObj.groups.set(gKey, { key: gKey, label: gName, order: gOrder, fields: [], isOpen: gObj?.isDefaultOpen ?? true })
    }
    sectorObj.groups.get(gKey).fields.push(f)
  })

  const sectors = Array.from(map.values())
  sectors.sort((a, b) => a.order - b.order)

  return sectors.map((s) => {
    const groups = Array.from(s.groups.values())
    groups.sort((a, b) => a.order - b.order)
    return { key: s.key, label: s.label, groups: groups }
  })
})

const parseOptions = (opts) => {
  if (!opts) return []
  if (typeof opts === 'string') {
    if (opts.trim().startsWith('[')) {
      try {
        const parsed = JSON.parse(opts)
        const mapped = parsed.map((o) => {
          if (typeof o === 'string') return { text: o, value: o, order: 0 }
          return {
            value: o.key,
            text: o.label?.[locale.value] || o.label?.ko || o.label?.en || o.key,
            order: o.order || 0
          }
        })
        return mapped.sort((a, b) => a.order - b.order)
      } catch (e) {}
    }
    return opts.split(',').map((s) => {
      const val = s.trim()
      return { text: val, value: val }
    })
  }
  return opts
}

const getDomainRefDisplayName = (fieldKey, recordId) => {
  if (!recordId) return ''
  const refInfo = props.domainReferences?.[fieldKey]
  if (!refInfo) return recordId

  const record = refInfo.records?.find((r) => r.id === recordId)
  if (record) {
    const data = typeof record.data === 'string' ? JSON.parse(record.data) : record.data
    const idFieldId = refInfo.domainInfo?.identifierFieldId
    let dFieldId = refInfo.domainInfo?.displayNameFieldId || idFieldId
    const idF = refInfo.fields?.find((x) => x.id === idFieldId)
    const nameF = refInfo.fields?.find((x) => x.id === dFieldId)

    const extractVal = (d, key) => {
      if (!d || !key) return null
      const v = d[key]
      if (v && typeof v === 'object') return v[locale.value] || v.ko || v.en || JSON.stringify(v)
      return v ? String(v) : null
    }

    const idStr = extractVal(data, idF?.key)
    const nameStr = extractVal(data, nameF?.key)

    if (idStr && nameStr && idStr !== nameStr) return `[${idStr}] ${nameStr}`
    if (nameStr) return nameStr
    if (idStr) return `[${idStr}]`
  }
  return recordId
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
  const defaultRes = {
    show: true,
    highlight: field?.isHighlighted || false,
    required: field?.required || false,
    readOnly: field?.isReadOnly || false,
    disabled: false
  }
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

const extractFilename = (url) => {
  if (!url) return 'Download'
  try {
    if (url.includes('?name=')) return decodeURIComponent(url.split('?name=')[1].split('&')[0])
    return decodeURIComponent(url.split('/').pop().split('?')[0]) || 'Download'
  } catch (e) {
    return 'Download'
  }
}

let draggedItemIndex = null
let currentArrayRef = null

const onDragStart = (event, index, arr) => {
  draggedItemIndex = index
  currentArrayRef = arr
  if (event.dataTransfer) event.dataTransfer.effectAllowed = 'move'
  setTimeout(() => {
    if (event.target && event.target.style) event.target.style.opacity = '0.5'
  }, 0)
}

const onDragEnter = (event, index, arr) => {
  if (draggedItemIndex === null || currentArrayRef !== arr) return
  if (draggedItemIndex === index) return
  const temp = arr[draggedItemIndex]
  arr.splice(draggedItemIndex, 1)
  arr.splice(index, 0, temp)
  draggedItemIndex = index
}

const onDrop = () => {}

const onDragEnd = (event) => {
  if (event.target && event.target.style) event.target.style.opacity = '1'
  draggedItemIndex = null
  currentArrayRef = null
}

const removeFile = (arr, index) => {
  if (!arr || !Array.isArray(arr)) return
  arr.splice(index, 1)
}
</script>

<style scoped>
.mb-4 { margin-bottom: 1rem; }
.w-full { width: 100%; }

.file-upload-wrapper :deep(.va-file-upload) {
  width: 100% !important;
  max-width: 100% !important;
}
.file-upload-wrapper :deep(.va-file-upload-dropzone) {
  box-sizing: border-box !important;
  padding: 0.5rem 1rem !important;
  min-height: 60px !important;
}
.file-upload-wrapper :deep(.va-file-upload-dropzone__content) {
  width: 100% !important;
  box-sizing: border-box !important;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 1rem;
}
.file-upload-wrapper :deep(.va-file-upload-list) {
  display: none !important;
}
.custom-file-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-top: 0.5rem;
}
.custom-file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 1rem;
  background-color: var(--va-background-element);
  border-radius: 0.5rem;
  font-size: 0.9rem;
}
.custom-file-info {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.custom-file-actions {
  display: flex;
  gap: 0.25rem;
  align-items: center;
}
.flip-list-move {
  transition: transform 0.3s cubic-bezier(0.25, 0.8, 0.5, 1);
}

.slim-multilingual-input :deep(.va-input-wrapper__container) {
  align-items: center;
  margin: 0;
}
.slim-multilingual-input :deep(.va-input-wrapper__field) {
  align-items: center;
}

.custom-modal-header-wrapper {
  margin-top: -20px !important;
  margin-left: -20px !important;
}

.custom-record-modal :deep(.va-modal__header) {
  padding: 0.35rem 1.25rem 0.15rem 1.25rem !important;
  margin-top: 0 !important;
  display: flex !important;
  align-items: center !important;
  min-height: 28px !important;
}

.custom-record-modal :deep(.va-modal__close) {
  top: 0.35rem !important;
  right: 1.25rem !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.custom-record-modal :deep(.va-modal__message) {
  padding-top: 0.15rem !important;
}
</style>





