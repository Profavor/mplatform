<template>
  <va-modal
    v-model="show"
    size="small"
    :title="$t('unmask_reason_title')"
    hide-default-actions
    @cancel="cancel"
    @click-outside="cancel"
  >
    <div style="padding: 0.5rem 0;">
      <p style="margin-bottom: 1rem; font-size: 0.9rem; color: var(--va-text-secondary);">
        {{ $t('unmask_reason_desc') }}
      </p>

      <va-input
        v-model="reason"
        type="textarea"
        style="width: 100%;"
        :min-rows="3"
        :autosize="true"
        :label="$t('access_reason')"
        :placeholder="$t('access_reason_placeholder')"
        required-mark
        :rules="[(v) => !!v || ($t('access_reason_required'))]"
        @keyup.enter="submit"
        autofocus
      />
    </div>

    <template #footer>
      <div style="display: flex; justify-content: flex-end; gap: 0.5rem; width: 100%;">
        <va-button preset="secondary" border-color="secondary" @click="cancel">
          {{ $t('cancel') }}
        </va-button>
        <va-button color="primary" @click="submit" :disabled="!reason.trim()">
          {{ $t('vuestic.confirm') }}
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
