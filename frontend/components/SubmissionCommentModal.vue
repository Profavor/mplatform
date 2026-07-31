<template>
  <va-modal
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    :title="title || $t('submission_comment_title') || '상신 의견 작성'"
    :ok-text="$t('btn_submit') || '상신'"
    :cancel-text="$t('btn_cancel') || '취소'"
    @ok="onSubmit"
    @cancel="onCancel"
    :prevent-click-outside="true"
    :no-outside-dismiss="true"
  >
    <div style="padding: 1rem;">
      <p style="margin-bottom: 1rem; color: var(--va-text-secondary); font-size: 0.9rem;">
        {{ notice || $t('submission_comment_notice') || '(선택사항) 결재권자에게 남길 상신 사유(의견)를 작성해 주세요.' }}
      </p>
      <va-textarea
        :model-value="comment"
        @update:model-value="emit('update:comment', $event)"
        :placeholder="placeholder || $t('submission_comment_placeholder') || '상신 사유(의견)를 작성해 주세요...'"
        style="width: 100%;"
        :rows="4"
      />
    </div>
  </va-modal>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
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
