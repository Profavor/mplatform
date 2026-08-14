<template>
  <va-modal
    :model-value="modelValue"
    :title="mode === 'create' ? t('create_role_title') : t('edit_role_title')"
    hide-default-actions
    size="medium"
    :prevent-click-outside="true"
    :no-outside-dismiss="true"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div style="padding: 1rem; display: flex; flex-direction: column; gap: 1.1rem;">
      <va-input
        v-model="roleForm.name"
        :label="t('role_code_label')"
        :disabled="mode === 'edit'"
        placeholder="영문 대문자"
        required
      />
      <div>
        <div style="font-size: 0.6rem; font-weight: 700; color: var(--va-primary); margin-bottom: 0.25rem; text-transform: uppercase; letter-spacing: 0.4px;">
          {{ t('role_display_name_label') }} <span style="color: var(--va-danger);">*</span>
        </div>
        <div style="display: flex; gap: 0.5rem; flex-direction: row; min-width: 0;">
          <va-input v-model="roleForm.displayNameKo" style="flex: 1; min-width: 0;" required>
            <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">Korean</span></template>
          </va-input>
          <va-input v-model="roleForm.displayNameEn" style="flex: 1; min-width: 0;" required>
            <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">English</span></template>
          </va-input>
        </div>
      </div>
      <div>
        <div style="font-size: 0.6rem; font-weight: 700; color: var(--va-primary); margin-bottom: 0.25rem; text-transform: uppercase; letter-spacing: 0.4px;">
          {{ t('role_description_label') }}
        </div>
        <div style="display: flex; gap: 0.5rem; flex-direction: row; min-width: 0;">
          <va-textarea v-model="roleForm.descriptionKo" style="flex: 1; min-width: 0;" :min-rows="2">
            <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap; margin-top: 0.25rem;">Korean</span></template>
          </va-textarea>
          <va-textarea v-model="roleForm.descriptionEn" style="flex: 1; min-width: 0;" :min-rows="2">
            <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap; margin-top: 0.25rem;">English</span></template>
          </va-textarea>
        </div>
      </div>
      <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem;">
        <va-button preset="secondary" @click="$emit('update:modelValue', false)">{{ t('cancel') }}</va-button>
        <va-button color="primary" :loading="loading" @click="$emit('save')">{{ mode === 'create' ? t('save_role') : t('save') }}</va-button>
      </div>
    </div>
  </va-modal>
</template>

<script setup>
import { useI18n } from 'vue-i18n'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  mode: { type: String, default: 'create' }, // 'create' | 'edit'
  roleForm: { type: Object, required: true },
  loading: { type: Boolean, default: false }
})

defineEmits(['update:modelValue', 'save'])

const { t } = useI18n()
</script>
