<template>
  <va-modal
    :model-value="modelValue"
    @update:model-value="val => emit('update:modelValue', val)"
    :title="title || t('select_icon', '아이콘 선택')"
    size="medium"
    hide-default-actions
  >
    <IconPicker
      :model-value="tempIcon"
      @update:model-value="val => tempIcon = val"
    />
    <div class="modal-action-footer">
      <va-button preset="secondary" @click="onCancel">
        {{ t('cancel', '취소') }}
      </va-button>
      <va-button @click="onConfirm">
        {{ t('confirm', '확인') }}
      </va-button>
    </div>
  </va-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import IconPicker from '~/components/IconPicker.vue'

const { t } = useI18n()

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    icon?: string
    title?: string
  }>(),
  {
    icon: '',
    title: ''
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'update:icon', val: string): void
  (e: 'confirm', icon: string): void
}>()

const tempIcon = ref(props.icon)

watch(() => props.icon, (newVal) => {
  tempIcon.value = newVal || ''
})

const onCancel = () => {
  emit('update:modelValue', false)
}

const onConfirm = () => {
  emit('update:icon', tempIcon.value)
  emit('confirm', tempIcon.value)
  emit('update:modelValue', false)
}

defineExpose({
  onCancel,
  onConfirm
})
</script>

<style scoped>
.modal-action-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 1rem;
  gap: 0.5rem;
}
</style>
