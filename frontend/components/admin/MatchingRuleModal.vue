<template>
  <va-modal
    :model-value="modelValue"
    :title="isEditMode ? t('matchingRules.edit_title') : t('matchingRules.create_title')"
    hide-default-actions
    size="medium"
    no-outside-dismiss
    @update:model-value="val => emit('update:modelValue', val)"
  >
    <div style="padding: 0.5rem 0;">
      <va-input
        :model-value="form.ruleName"
        :label="t('matchingRules.rule_name')"
        :placeholder="t('matchingRules.rule_name_placeholder')"
        class="mb-3"
        required
        @update:model-value="val => form.ruleName = val"
      />

      <va-select
        :model-value="form.matchType"
        :options="matchTypeOptions"
        value-by="value"
        text-by="text"
        :label="t('matchingRules.match_type')"
        class="mb-3"
        @update:model-value="val => form.matchType = val"
      />

      <va-select
        v-if="domainFieldOptions.length > 0"
        :model-value="form.selectedFields"
        :options="domainFieldOptions"
        value-by="value"
        text-by="text"
        multiple
        :label="t('matchingRules.target_fields')"
        class="mb-3"
        @update:model-value="val => form.selectedFields = val"
      />
      <va-input
        v-else
        :model-value="form.targetFieldKeysInput"
        :label="t('matchingRules.target_fields_csv')"
        placeholder="email, phone"
        class="mb-3"
        @update:model-value="val => form.targetFieldKeysInput = val"
      />

      <va-input
        v-if="form.matchType === 'FUZZY'"
        :model-value="form.similarityThreshold"
        type="number"
        step="0.05"
        min="0.5"
        max="1.0"
        :label="t('matchingRules.similarity_threshold')"
        class="mb-3"
        @update:model-value="val => form.similarityThreshold = Number(val)"
      />

      <va-checkbox
        :model-value="form.isActive"
        :label="t('matchingRules.is_active')"
        class="mt-2"
        @update:model-value="val => form.isActive = val"
      />
    </div>

    <template #footer>
      <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem;">
        <va-button preset="secondary" @click="emit('update:modelValue', false)">
          {{ t('matchingRules.cancel') }}
        </va-button>
        <va-button color="primary" :loading="isSaving" @click="emit('save')">
          {{ t('matchingRules.save') }}
        </va-button>
      </div>
    </template>
  </va-modal>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

interface OptionItem {
  text: string
  value: any
}

interface MatchingRuleFormData {
  ruleName: string
  matchType: string
  selectedFields: string[]
  targetFieldKeysInput: string
  similarityThreshold: number
  isActive: boolean
}

defineProps<{
  modelValue: boolean
  isEditMode: boolean
  isSaving: boolean
  form: MatchingRuleFormData
  matchTypeOptions: OptionItem[]
  domainFieldOptions: OptionItem[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'save'): void
}>()
</script>
