<template>
  <va-modal
    :model-value="modelValue"
    @update:model-value="val => emit('update:modelValue', val)"
    :title="title || t('system_notification', '시스템 알림')"
    hide-default-actions
    size="small"
    :prevent-click-outside="true"
    :no-outside-dismiss="true"
  >
    <div class="notification-modal-body">
      <div
        v-if="type === 'success'"
        class="icon-circle success-circle"
      >
        <va-icon name="check_circle" size="2.5rem" color="success" />
      </div>
      <div
        v-else-if="type === 'warning'"
        class="icon-circle warning-circle"
      >
        <va-icon name="warning" size="2.5rem" color="warning" />
      </div>
      <div
        v-else
        class="icon-circle danger-circle"
      >
        <va-icon name="error" size="2.5rem" color="danger" />
      </div>

      <h3
        class="notification-header"
        :class="{
          'text-success': type === 'success',
          'text-warning': type === 'warning',
          'text-danger': type !== 'success' && type !== 'warning'
        }"
      >
        {{ header || t('system_notification', '시스템 알림') }}
      </h3>

      <div class="notification-message-box">
        {{ message }}
      </div>

      <div class="action-footer">
        <va-button
          :color="type === 'success' ? 'success' : (type === 'warning' ? 'warning' : 'primary')"
          preset="solid"
          class="confirm-btn"
          @click="closeModal"
        >
          {{ t('confirm', '확인') }}
        </va-button>
      </div>
    </div>
  </va-modal>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    type?: 'success' | 'warning' | 'error' | 'danger' | string
    title?: string
    header?: string
    message?: string
  }>(),
  {
    type: 'error',
    title: '',
    header: '',
    message: ''
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
}>()

const closeModal = () => {
  emit('update:modelValue', false)
}
</script>

<style scoped>
.notification-modal-body {
  padding: 1.25rem 0;
  text-align: center;
}

.icon-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.25rem auto;
}

.success-circle {
  background: rgba(30, 203, 114, 0.12);
  color: #15803d;
}

.warning-circle {
  background: rgba(232, 139, 36, 0.12);
  color: #c2410c;
}

.danger-circle {
  background: rgba(229, 57, 53, 0.12);
  color: #b91c1c;
}

.notification-header {
  margin: 0 0 0.75rem 0;
  font-weight: 700;
  font-size: 1.25rem;
}

.text-success { color: #15803d; }
.text-warning { color: #c2410c; }
.text-danger { color: #b91c1c; }

.notification-message-box {
  background: var(--va-background-secondary);
  border: 1px solid var(--va-background-border);
  border-radius: 8px;
  padding: 1rem 1.25rem;
  text-align: left;
  font-size: 0.92rem;
  color: var(--va-text-primary);
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: 1.5rem;
  word-break: break-word;
  white-space: pre-wrap;
}

.action-footer {
  display: flex;
  justify-content: center;
}

.confirm-btn {
  min-width: 120px;
  font-weight: 700;
}
</style>
