<template>
  <AppModal
    :model-value="modelValue"
    :title="isEdit ? t('edit_dq_rule', '품질 검칙 규칙 수정') : t('add_dq_rule', '품질 검칙 규칙 추가')"
    icon="fact_check"
    size="medium"
    @ok="onSave"
    @cancel="onCancel"
    @update:model-value="val => emit('update:modelValue', val)"
  >
    <div style="padding: 0.5rem 0; display: flex; flex-direction: column; gap: 1.25rem;">
      <va-select
        v-model="formData.ruleType"
        :options="ruleTypeOptions"
        :label="t('dq_rule_type', '규칙 유형')"
        required
      />
      
      <va-select
        v-model="formData.severity"
        :options="severityOptions"
        :label="t('dq_severity', '심각도')"
        required
      />

      <va-input
        v-model="formData.params"
        :label="t('dq_params', '검사 파라미터')"
        placeholder="e.g. ^[0-9]+$"
        type="textarea"
        :min-rows="2"
      />

      <va-input
        v-model="formData.message"
        :label="t('dq_error_message', '위반 시 오류 메시지')"
        placeholder="e.g. 숫자만 입력 가능합니다."
      />

      <div style="display: flex; gap: 1rem;">
        <va-input
          v-model="formData.sortOrder"
          :label="t('dq_sort_order', '적용 순서')"
          type="number"
          style="flex: 1;"
        />
        <va-checkbox
          v-model="formData.isActive"
          :label="t('is_active_label', '사용 여부 (Active)')"
          style="flex: 1; margin-top: 1.25rem;"
        />
      </div>
    </div>
  </AppModal>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import AppModal from '~/components/common/AppModal.vue'

const { t } = useI18n()

const props = defineProps<{
  modelValue: boolean
  isEdit: boolean
  formData: any
  ruleTypeOptions: any[]
  severityOptions: any[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'save'): void
}>()

const onSave = () => {
  emit('save')
}

const onCancel = () => {
  emit('update:modelValue', false)
}

defineExpose({
  onSave,
  onCancel
})
</script>
