<template>
  <AppModal
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="isEditMode ? 'Edit Domain' : 'Create New Domain'"
    icon="domain"
    hide-default-actions
    size="large"
  >
    <div style="padding: 0.5rem 0;">
      <div style="display: flex; gap: 1rem;">
        <va-input v-model="newDomain.name.ko" label="Domain Name (KO)" class="mb-4" style="flex: 1; min-width: 0;" />
        <va-input v-model="newDomain.name.en" label="Domain Name (EN)" class="mb-4" style="flex: 1; min-width: 0;" />
      </div>
      <div style="display: flex; gap: 1rem;">
        <va-input v-model="newDomain.description.ko" label="Description (KO)" class="mb-4" style="flex: 1; min-width: 0;" />
        <va-input v-model="newDomain.description.en" label="Description (EN)" class="mb-4" style="flex: 1; min-width: 0;" />
      </div>
      <div class="mb-4">
        <label style="font-weight: bold; margin-bottom: 0.5rem; display: block; font-size: 0.9rem; color: var(--va-text-primary);">Domain Icon</label>
        <div style="display: flex; align-items: center; gap: 1rem;">
          <va-icon :name="newDomain.icon || 'folder'" size="large" color="primary" />
          <va-button size="small" preset="secondary" border-color="primary" @click="$emit('open-icon-picker', true)">Select Icon</va-button>
        </div>
      </div>
      <va-input v-model="newDomain.numberingPattern" label="Numbering Pattern" placeholder="e.g. ITEM-{YYYY}-{SEQ:5} (Leave empty for manual)" class="mb-4 w-full" />
      <va-input v-model="newDomain.sortOrder" type="number" label="Sort Order" class="mb-4 w-full" />
      <va-switch v-model="newDomain.autoDqScanEnabled" label="Auto Data Quality Scan (매일 새벽 2시 자동 정기 검사)" class="mb-4 w-full" color="primary" />
      
      <!-- Field Mappings (Only show in Edit mode because we need fields) -->
      <div v-if="isEditMode" style="margin-top: 1rem; border-top: 1px solid rgba(255, 255, 255, 0.1); padding-top: 1rem;">
        <div style="margin-bottom: 0.75rem; font-weight: bold; font-size: 0.9rem; color: var(--va-text-secondary, #94a3b8);">Domain Field Mappings (Required)</div>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 0.75rem;">
          <va-select
            v-model="newDomain.identifierFieldId"
            :options="domainFieldOptions.filter(o => o.type === 'TEXT')"
            value-by="value"
            text-by="text"
            label="Identifier Field (ID)*"
            class="w-full"
            :error="mappingError.id"
            :error-messages="['Required']"
          />
          <va-select
            v-model="newDomain.displayNameFieldId"
            :options="domainFieldOptions.filter(o => o.type === 'MULTILINGUAL' || o.type === 'MULTILINGUAL_TEXT')"
            value-by="value"
            text-by="text"
            label="Display Name Field*"
            class="w-full"
            :error="mappingError.name"
            :error-messages="['Required']"
          />
          <va-select
            v-model="newDomain.descriptionFieldId"
            :options="domainFieldOptions.filter(o => o.type === 'MULTILINGUAL' || o.type === 'MULTILINGUAL_TEXT')"
            value-by="value"
            text-by="text"
            label="Description Field (Optional)"
            class="w-full"
            clearable
          />
          <va-select
            v-model="newDomain.imageFieldId"
            :options="domainFieldOptions.filter(o => o.type === 'IMAGE' || o.type === 'IMAGE_FILE')"
            value-by="value"
            text-by="text"
            label="Image Field (Optional)"
            class="w-full"
            clearable
          />
        </div>
      </div>
    </div>
    <template #footer>
      <div style="display: flex; justify-content: flex-end; gap: 0.5rem;">
        <va-button preset="secondary" @click="$emit('update:modelValue', false)">Cancel</va-button>
        <va-button color="primary" @click="$emit('save')">{{ isEditMode ? 'Save' : 'Create' }}</va-button>
      </div>
    </template>
  </AppModal>
</template>

<script setup>
import AppModal from '~/components/common/AppModal.vue'
defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  isEditMode: {
    type: Boolean,
    default: false
  },
  newDomain: {
    type: Object,
    required: true
  },
  domainFieldOptions: {
    type: Array,
    default: () => []
  },
  mappingError: {
    type: Object,
    default: () => ({ id: false, name: false })
  }
})

defineEmits(['update:modelValue', 'save', 'open-icon-picker'])
</script>
