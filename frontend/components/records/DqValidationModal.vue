<template>
  <va-modal
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="currentLocale === 'en' ? 'Data Quality Check' : 'DQ 품질 검증 결과'"
    hide-default-actions
    size="medium"
    :prevent-click-outside="true"
    :no-outside-dismiss="true"
  >
    <div style="padding: 0.5rem 0; min-height: 250px;">
      <div v-if="dqValidating" style="text-align: center; padding: 3rem;">
        <va-progress-circle indeterminate color="primary" />
        <p style="margin-top: 1rem; color: var(--va-secondary); font-weight: 500;">
          {{ currentLocale === 'en' ? 'Evaluating Data Quality Rules...' : '품질 규칙(DQ Rule) 검증 중...' }}
        </p>
      </div>

      <div v-else>
        <!-- Status Banner -->
        <div
          v-if="(dqValidationResult.errors || []).length === 0 && (dqValidationResult.warnings || []).length === 0"
          style="padding: 1.25rem; background: rgba(30, 203, 114, 0.1); border: 1px solid #1ecb72; border-radius: 8px; text-align: center; margin-bottom: 1.25rem;"
        >
          <va-icon name="check_circle" color="success" size="2.5rem" />
          <h4 style="margin: 0.5rem 0 0.25rem 0; font-weight: 700; color: #15803d;">
            {{ currentLocale === 'en' ? 'All Data Quality Checks Passed!' : '모든 품질 검증을 통과했습니다!' }}
          </h4>
          <p style="margin: 0; font-size: 0.85rem; color: var(--va-secondary);">
            {{ currentLocale === 'en' ? 'No DQ violations detected. Click below to enter your submission comment.' : '감지된 DQ 위반 사항이 없습니다. 아래 버튼을 눌러 상신 의견을 작성해 주세요.' }}
          </p>
        </div>

        <div
          v-else
          style="padding: 1rem; border-radius: 8px; margin-bottom: 1.25rem; display: flex; align-items: center; gap: 0.75rem;"
          :style="{
            background: (dqValidationResult.errors || []).length > 0 ? 'rgba(228, 34, 34, 0.1)' : 'rgba(232, 139, 36, 0.1)',
            border: '1px solid ' + ((dqValidationResult.errors || []).length > 0 ? '#e42222' : '#e88b24')
          }"
        >
          <va-icon
            :name="(dqValidationResult.errors || []).length > 0 ? 'error' : 'warning'"
            :color="(dqValidationResult.errors || []).length > 0 ? 'danger' : 'warning'"
            size="2rem"
          />
          <div>
            <h5 style="margin: 0; font-weight: 700;" :style="{ color: (dqValidationResult.errors || []).length > 0 ? '#b91c1c' : '#c2410c' }">
              <template v-if="(dqValidationResult.errors || []).length > 0">
                {{ currentLocale === 'en' ? 'DQ Errors Detected' : 'DQ 품질 오류가 발견되었습니다' }}
              </template>
              <template v-else>
                {{ currentLocale === 'en' ? 'DQ Warnings Detected' : 'DQ 품질 경고가 발생했습니다' }}
              </template>
            </h5>
            <span style="font-size: 0.85rem; color: var(--va-text-primary);">
              <template v-if="currentLocale === 'en'">
                {{ (dqValidationResult.errors || []).length }} error(s), {{ (dqValidationResult.warnings || []).length }} warning(s) found.
              </template>
              <template v-else>
                오류 {{ (dqValidationResult.errors || []).length }}건, 경고 {{ (dqValidationResult.warnings || []).length }}건이 확인되었습니다.
              </template>
            </span>
          </div>
        </div>

        <!-- List of Violations -->
        <div v-if="((dqValidationResult.errors || []).length + (dqValidationResult.warnings || []).length) > 0"
             style="max-height: 260px; overflow-y: auto; display: flex; flex-direction: column; gap: 0.6rem; margin-bottom: 1.25rem;">
          <!-- Errors -->
          <div
            v-for="(v, idx) in (dqValidationResult.errors || [])"
            :key="'err-' + idx"
            style="padding: 0.75rem; border-radius: 6px; border: 1px solid rgba(228, 34, 34, 0.3); background: var(--va-background-element); display: flex; align-items: flex-start; gap: 0.75rem;"
          >
            <va-badge text="ERROR" color="danger" />
            <div style="flex: 1;">
              <div style="font-weight: 700; font-size: 0.9rem; color: var(--va-text-primary);">
                {{ getFieldLabelByKey(v.fieldKey) }} <span style="font-size: 0.75rem; color: var(--va-secondary); font-weight: normal;">({{ v.fieldKey }})</span>
              </div>
              <div style="font-size: 0.85rem; color: #b91c1c; margin-top: 0.2rem;">
                {{ getViolationMessageText(v.message) }}
              </div>
              <div style="font-size: 0.75rem; color: var(--va-secondary); margin-top: 0.2rem;">
                {{ currentLocale === 'en' ? 'Input value:' : '입력값:' }} <code>{{ v.actualValue || (currentLocale === 'en' ? '(null/empty)' : '(null/빈값)') }}</code>
              </div>
            </div>
          </div>

          <!-- Warnings -->
          <div
            v-for="(v, idx) in (dqValidationResult.warnings || [])"
            :key="'warn-' + idx"
            style="padding: 0.75rem; border-radius: 6px; border: 1px solid rgba(232, 139, 36, 0.3); background: var(--va-background-element); display: flex; align-items: flex-start; gap: 0.75rem;"
          >
            <va-badge text="WARNING" color="warning" />
            <div style="flex: 1;">
              <div style="font-weight: 700; font-size: 0.9rem; color: var(--va-text-primary);">
                {{ getFieldLabelByKey(v.fieldKey) }} <span style="font-size: 0.75rem; color: var(--va-secondary); font-weight: normal;">({{ v.fieldKey }})</span>
              </div>
              <div style="font-size: 0.85rem; color: #c2410c; margin-top: 0.2rem;">
                {{ getViolationMessageText(v.message) }}
              </div>
              <div style="font-size: 0.75rem; color: var(--va-secondary); margin-top: 0.2rem;">
                {{ currentLocale === 'en' ? 'Input value:' : '입력값:' }} <code>{{ v.actualValue || (currentLocale === 'en' ? '(null/empty)' : '(null/빈값)') }}</code>
              </div>
            </div>
          </div>
        </div>

        <!-- Action Buttons -->
        <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 1rem; padding-top: 0.75rem; border-top: 1px solid var(--va-background-border);">
          <va-button preset="secondary" icon="edit" @click="$emit('fix-data')">
            {{ currentLocale === 'en' ? 'Fix Data' : '데이터 수정하기' }}
          </va-button>
          
          <va-button color="primary" icon="arrow_forward" @click="$emit('proceed')">
            {{ currentLocale === 'en' ? 'Proceed to Submit' : '상신 의견 작성하기' }}
          </va-button>
        </div>
      </div>
    </div>
  </va-modal>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  dqValidating: {
    type: Boolean,
    default: false
  },
  dqValidationResult: {
    type: Object,
    default: () => ({ valid: true, errors: [], warnings: [] })
  },
  currentLocale: {
    type: String,
    default: 'ko'
  },
  nodeFields: {
    type: Array,
    default: () => []
  }
})

defineEmits(['update:modelValue', 'fix-data', 'proceed'])

const getTranslatedName = (nameObj) => {
  if (!nameObj) return ''
  if (typeof nameObj === 'string') return nameObj
  return nameObj[props.currentLocale] || nameObj.ko || nameObj.en || Object.values(nameObj)[0] || ''
}

const getFieldLabelByKey = (key) => {
  if (!key) return ''
  const f = props.nodeFields?.find(field => field.key === key || String(field.id) === String(key) || (field.key && String(field.key).toLowerCase() === String(key).toLowerCase()))
  return f ? getTranslatedName(f.name) : key
}

const getViolationMessageText = (msgObj) => {
  if (!msgObj) return 'Validation error'
  if (typeof msgObj === 'string') return msgObj
  return msgObj[props.currentLocale] || msgObj.ko || msgObj.en || Object.values(msgObj)[0] || 'Validation error'
}
</script>
