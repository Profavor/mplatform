<template>
  <va-modal
    v-model="show"
    size="small"
    :title="$t('unmask_reason_title') || '원본 보기 (마스킹 해제) 사유 입력'"
    hide-default-actions
    @cancel="cancel"
    @click-outside="cancel"
  >
    <div style="padding: 0.5rem 0;">
      <p style="margin-bottom: 1rem; font-size: 0.9rem; color: var(--va-text-secondary);">
        {{ $t('unmask_reason_desc') || '민감한 개인정보 원본을 열람하기 위해 접근 사유를 입력해 주세요. 입력된 사유는 감사 로그에 안전하게 기록됩니다.' }}
      </p>

      <va-input
        v-model="reason"
        type="textarea"
        style="width: 100%;"
        :min-rows="3"
        :autosize="true"
        :label="$t('access_reason') || '접근 사유'"
        :placeholder="$t('access_reason_placeholder') || '예: 업무 처리, 고객 요청 등'"
        required-mark
        :rules="[(v) => !!v || ($t('access_reason_required') || '접근 사유를 입력해 주세요.')]"
        @keyup.enter="submit"
        autofocus
      />
    </div>

    <template #footer>
      <div style="display: flex; justify-content: flex-end; gap: 0.5rem; width: 100%;">
        <va-button preset="secondary" border-color="secondary" @click="cancel">
          {{ $t('cancel') || '취소' }}
        </va-button>
        <va-button color="primary" @click="submit" :disabled="!reason.trim()">
          {{ $t('vuestic.confirm') || '확인' }}
        </va-button>
      </div>
    </template>
  </va-modal>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const { t } = useI18n()
const show = ref(props.modelValue)
const reason = ref('')

watch(() => props.modelValue, (newVal) => {
  show.value = newVal
  if (newVal) {
    reason.value = '' // Reset when modal opens
  }
})

watch(show, (newVal) => {
  emit('update:modelValue', newVal)
  if (!newVal && !reason.value) {
    emit('cancel')
  }
})

const cancel = () => {
  show.value = false
  reason.value = ''
  emit('cancel')
}

const submit = () => {
  if (!reason.value.trim()) return
  show.value = false
  emit('confirm', reason.value.trim())
}
</script>
