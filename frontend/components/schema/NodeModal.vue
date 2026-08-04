<template>
  <va-modal
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="isEditMode ? `Edit Node` : `Add Node to ${selectedNode?.label}`"
    :ok-text="isEditMode ? 'Save' : 'Create'"
    cancel-text="Cancel"
    @ok="$emit('save')"
  >
    <div style="display: flex; gap: 1rem;">
      <va-input v-model="newNode.name.ko" label="Node Name (KO)" class="mb-4" style="flex: 1; min-width: 0;" />
      <va-input v-model="newNode.name.en" label="Node Name (EN)" class="mb-4" style="flex: 1; min-width: 0;" />
    </div>
    <va-input v-model="newNode.order" type="number" label="Order" class="mb-4 w-full" />
    <div class="mb-4">
      <label style="font-weight: bold; margin-bottom: 0.5rem; display: block; font-size: 0.9rem; color: var(--va-text-primary);">Node Icon</label>
      <div style="display: flex; align-items: center; gap: 1rem;">
        <va-icon :name="newNode.icon || 'article'" size="large" color="primary" />
        <va-button size="small" preset="secondary" border-color="primary" @click="$emit('open-icon-picker', false)">Select Icon</va-button>
      </div>
    </div>
  </va-modal>
</template>

<script setup>
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

defineEmits(['update:modelValue', 'save', 'open-icon-picker'])
</script>
