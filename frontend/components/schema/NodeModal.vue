<template>
  <AppModal
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="isEditMode ? $t('edit_node') : $t('add_node_to', { name: selectedNode?.label })"
    icon="account_tree"
    hide-default-actions
    size="small"
  >
    <div style="padding: 0.5rem 0;">
      <div style="display: flex; gap: 1rem;">
        <va-input v-model="newNode.name.ko" :label="$t('node_name_ko')" class="mb-4" style="flex: 1; min-width: 0;" />
        <va-input v-model="newNode.name.en" :label="$t('node_name_en')" class="mb-4" style="flex: 1; min-width: 0;" />
      </div>
      <va-input v-model="newNode.order" type="number" :label="$t('node_order')" class="mb-4 w-full" />
      <div class="mb-4">
        <label style="font-weight: bold; margin-bottom: 0.5rem; display: block; font-size: 0.9rem; color: var(--va-text-primary);">
          {{ $t('node_icon') }}
        </label>
        <div style="display: flex; align-items: center; gap: 1rem;">
          <va-icon :name="newNode.icon || 'article'" size="large" color="primary" />
          <va-button size="small" preset="secondary" border-color="primary" @click="$emit('open-icon-picker', false)">
            {{ $t('select_icon') }}
          </va-button>
        </div>
      </div>
    </div>

    <template #footer>
      <div v-if="isEditMode" style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
        <va-button
          color="danger"
          preset="secondary"
          icon="delete"
          @click="$emit('delete', selectedNode)"
        >
          {{ $t('delete_node') }}
        </va-button>
        <div style="display: flex; gap: 0.5rem;">
          <va-button preset="secondary" @click="$emit('update:modelValue', false)">
            {{ $t('cancel') }}
          </va-button>
          <va-button color="primary" @click="$emit('save')">
            {{ $t('save') }}
          </va-button>
        </div>
      </div>
      <div v-else style="display: flex; justify-content: flex-end; gap: 0.5rem; width: 100%;">
        <va-button preset="secondary" @click="$emit('update:modelValue', false)">
          {{ $t('cancel') }}
        </va-button>
        <va-button color="primary" @click="$emit('save')">
          {{ $t('create') }}
        </va-button>
      </div>
    </template>
  </AppModal>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import AppModal from '~/components/common/AppModal.vue'

const { t } = useI18n()

defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  isEditMode: {
    type: Boolean,
    default: false
  },
  newNode: {
    type: Object,
    required: true
  },
  selectedNode: {
    type: Object,
    default: null
  }
})

defineEmits(['update:modelValue', 'save', 'delete', 'open-icon-picker'])
</script>
