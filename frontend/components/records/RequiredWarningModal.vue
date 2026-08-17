<template>
  <AppModal
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="currentLocale === 'en' ? 'Required Fields Missing' : '필수 항목 입력 안내'"
    icon="warning"
    hide-default-actions
    size="small"
  >
    <div style="padding: 1rem 0; text-align: center;">
      <div style="width: 56px; height: 56px; border-radius: 50%; background: rgba(229, 57, 53, 0.1); color: var(--va-danger); display: flex; align-items: center; justify-content: center; margin: 0 auto 1rem auto;">
        <va-icon name="warning" size="2rem" color="danger" />
      </div>
      <h4 style="margin: 0 0 0.5rem 0; font-weight: 700; color: var(--va-text-primary); font-size: 1.1rem;">
        {{ currentLocale === 'en' ? 'Please fill in all required fields' : '필수 입력 항목을 확인해 주세요' }}
      </h4>
      <p style="font-size: 0.85rem; color: var(--va-text-secondary); margin-bottom: 1.25rem;">
        {{ currentLocale === 'en' ? 'The following fields must be completed before saving:' : '아래 필수 입력 항목이 누락되었습니다. 작성 후 다시 시도해 주세요.' }}
      </p>

      <div style="background: var(--va-background-secondary); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 0.75rem 1rem; text-align: left; max-height: 180px; overflow-y: auto; margin-bottom: 1.5rem;">
        <div v-for="(item, idx) in missingRequiredFields" :key="idx" style="display: flex; align-items: center; gap: 0.5rem; padding: 0.35rem 0; font-size: 0.88rem; color: var(--va-danger); font-weight: 600;">
          <va-icon name="error_outline" size="small" color="danger" />
          <span>{{ item }}</span>
        </div>
      </div>

      <div style="display: flex; justify-content: center;">
        <va-button color="primary" preset="solid" style="min-width: 110px;" @click="$emit('confirm')">
          {{ currentLocale === 'en' ? 'Got it' : '확인' }}
        </va-button>
      </div>
    </div>
  </AppModal>
</template>

<script setup>
import AppModal from '~/components/common/AppModal.vue'
defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  currentLocale: {
    type: String,
    default: 'ko'
  },
  missingRequiredFields: {
    type: Array,
    default: () => []
  }
})

defineEmits(['update:modelValue', 'confirm'])
</script>
