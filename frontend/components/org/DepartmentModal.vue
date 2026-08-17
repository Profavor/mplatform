<template>
  <AppModal
    :model-value="modelValue"
    :title="mode === 'create' ? t('add_new_dept') : t('edit_dept')"
    icon="corporate_fare"
    hide-default-actions
    size="small"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div style="padding: 0.5rem 0; display: flex; flex-direction: column; gap: 1rem;">
      <va-select
        v-model="deptForm.parentDepartmentId"
        :options="deptOptions"
        value-by="id"
        text-by="displayNameText"
        :label="t('parent_dept')"
        clearable
      />
      <div>
        <div style="font-size: 0.6rem; font-weight: 700; color: var(--va-primary); margin-bottom: 0.25rem; text-transform: uppercase; letter-spacing: 0.4px;">
          {{ t('dept_name') }} <span style="color: var(--va-danger);">*</span>
        </div>
        <div style="display: flex; gap: 0.5rem; flex-direction: row; min-width: 0;">
          <va-input v-model="deptForm.nameKo" style="flex: 1; min-width: 0;" required>
            <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">Korean</span></template>
          </va-input>
          <va-input v-model="deptForm.nameEn" style="flex: 1; min-width: 0;" required>
            <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">English</span></template>
          </va-input>
        </div>
      </div>
      <div>
        <label style="display: block; font-size: 0.85rem; font-weight: 700; color: var(--va-text-primary); margin-bottom: 0.5rem;">
          {{ t('node_icon') }}
        </label>
        <div style="display: flex; align-items: center; gap: 1rem; background: var(--va-background-element); padding: 0.5rem 0.75rem; border-radius: 8px; border: 1px solid var(--va-background-border);">
          <va-icon :name="deptForm.icon || 'folder'" color="primary" size="medium" />
          <va-button preset="primary" outline icon="palette" size="small" @click="$emit('open-icon-picker', mode)">
            {{ t('select_icon') }}
          </va-button>
        </div>
      </div>
      <UserRoleSelect
        v-model="deptForm.roles"
        multiple
        :org-id="orgId"
        :label="t('dept_roles')"
        clearable
      />
      <div>
        <div style="font-size: 0.6rem; font-weight: 700; color: var(--va-primary); margin-bottom: 0.25rem; text-transform: uppercase; letter-spacing: 0.4px;">
          {{ t('description') }}
        </div>
        <div style="display: flex; gap: 0.5rem; flex-direction: row; min-width: 0;">
          <va-textarea v-model="deptForm.descriptionKo" style="flex: 1; min-width: 0;" :min-rows="2">
            <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap; margin-top: 0.25rem;">Korean</span></template>
          </va-textarea>
          <va-textarea v-model="deptForm.descriptionEn" style="flex: 1; min-width: 0;" :min-rows="2">
            <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap; margin-top: 0.25rem;">English</span></template>
          </va-textarea>
        </div>
      </div>
      <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem;">
        <va-button preset="secondary" @click="$emit('update:modelValue', false)">{{ t('cancel') }}</va-button>
        <va-button color="primary" :loading="loading" @click="$emit('save')">{{ mode === 'create' ? t('add_department') : t('save') }}</va-button>
      </div>
    </div>
  </AppModal>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import AppModal from '~/components/common/AppModal.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  mode: { type: String, default: 'create' }, // 'create' | 'edit'
  deptForm: { type: Object, required: true },
  deptOptions: { type: Array, default: () => [] },
  orgId: { type: String, default: '' },
  loading: { type: Boolean, default: false }
})

defineEmits(['update:modelValue', 'save', 'open-icon-picker'])

const { t } = useI18n()
</script>
