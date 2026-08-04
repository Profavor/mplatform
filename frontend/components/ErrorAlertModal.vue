<template>
  <va-modal
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="errorAlertTitle || (currentLocale === 'en' ? 'System Notification' : '시스템 알림')"
    hide-default-actions
    size="small"
    :prevent-click-outside="true"
    :no-outside-dismiss="true"
  >
    <div style="padding: 1.25rem 0; text-align: center;">
      <div
        v-if="errorAlertType === 'success'"
        style="width: 60px; height: 60px; border-radius: 50%; background: rgba(30, 203, 114, 0.12); color: #15803d; display: flex; align-items: center; justify-content: center; margin: 0 auto 1.25rem auto;"
      >
        <va-icon name="check_circle" size="2.5rem" color="success" />
      </div>

      <div
        v-else-if="errorAlertType === 'warning'"
        style="width: 60px; height: 60px; border-radius: 50%; background: rgba(232, 139, 36, 0.12); color: #c2410c; display: flex; align-items: center; justify-content: center; margin: 0 auto 1.25rem auto;"
      >
        <va-icon name="warning" size="2.5rem" color="warning" />
      </div>

      <div
        v-else
        style="width: 60px; height: 60px; border-radius: 50%; background: rgba(229, 57, 53, 0.12); color: #b91c1c; display: flex; align-items: center; justify-content: center; margin: 0 auto 1.25rem auto;"
      >
        <va-icon name="error" size="2.5rem" color="danger" />
      </div>

      <h3
        style="margin: 0 0 0.75rem 0; font-weight: 700; font-size: 1.25rem;"
        :style="{
          color: errorAlertType === 'success' ? '#15803d' : (errorAlertType === 'warning' ? '#c2410c' : '#b91c1c')
        }"
      >
        {{ errorAlertHeader || (currentLocale === 'en' ? 'System Notification' : '시스템 알림') }}
      </h3>

      <div style="background: var(--va-background-secondary); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 1rem 1.25rem; text-align: left; font-size: 0.92rem; color: var(--va-text-primary); max-height: 200px; overflow-y: auto; margin-bottom: 1.5rem; word-break: break-word; white-space: pre-wrap;">
        {{ errorAlertMessage }}
      </div>

      <div style="display: flex; justify-content: center;">
        <va-button
          :color="errorAlertType === 'success' ? 'success' : (errorAlertType === 'warning' ? 'warning' : 'primary')"
          preset="solid"
          style="min-width: 120px;"
          @click="$emit('update:modelValue', false)"
        >
          {{ currentLocale === 'en' ? 'Close' : '확인' }}
        </va-button>
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
  errorAlertType: {
    type: String,
    default: 'error'
  },
  errorAlertTitle: {
    type: String,
    default: ''
  },
  errorAlertHeader: {
    type: String,
    default: ''
  },
  errorAlertMessage: {
    type: String,
    default: ''
  },
  currentLocale: {
    type: String,
    default: 'ko'
  }
})

defineEmits(['update:modelValue'])
</script>
