<template>
  <AppModal
    :model-value="modelValue"
    :title="title || $t('submission_comment_title')"
    icon="rate_review"
    hide-default-actions
    size="small"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <div style="padding: 0.5rem 0;">
      <p style="margin-bottom: 1rem; color: var(--va-text-secondary); font-size: 0.9rem;">
        {{ notice || $t('submission_comment_notice') }}
      </p>
      <va-textarea
        :model-value="comment"
        @update:model-value="emit('update:comment', $event)"
        :placeholder="placeholder || $t('submission_comment_placeholder')"
        style="width: 100%;"
        :rows="4"
      />
    </div>
    <template #footer>
      <div style="display: flex; justify-content: flex-end; gap: 0.5rem;">
        <va-button preset="secondary" @click="onCancel">{{ $t('btn_cancel') }}</va-button>
        <va-button color="primary" @click="onSubmit">{{ $t('btn_submit') }}</va-button>
      </div>
    </template>
  </AppModal>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import AppModal from '~/components/common/AppModal.vue'
const { t } = useI18n()

const props = defineProps({
  modelValue: { type: Boolean, required: true },
  comment: { type: String, default: '' },
  title: { type: String, default: '' },
  notice: { type: String, default: '' },
  placeholder: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue', 'update:comment', 'submit', 'cancel'])

const onSubmit = () => {
  emit('submit', props.comment)
}

const onCancel = () => {
  emit('cancel')
}
</script>
