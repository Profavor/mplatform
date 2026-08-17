<template>
  <AppModal
    v-model="show"
    :title="$t('rollback_confirm_title')"
    icon="history"
    size="medium"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1rem; padding: 0.5rem 0;">
      <va-alert color="warning" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        💡 {{ $t('rollback_confirm_desc', { version: targetVersion }) }}
      </va-alert>

      <div style="background: var(--va-background-element); padding: 0.75rem 1rem; border-radius: 8px; font-size: 0.9rem;">
        <div><b>{{ $t('target_type_record') }}:</b> {{ recordDisplayCode }}</div>
        <div style="margin-top: 0.25rem;"><b>{{ $t('rollback_record') }}:</b> Version {{ targetVersion }}</div>
      </div>

      <!-- Diff Preview summary if available -->
      <div v-if="diffRows && diffRows.length > 0" style="max-height: 180px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
        <div style="padding: 0.4rem 0.75rem; background: var(--va-background-element); font-weight: 600; font-size: 0.82rem; border-bottom: 1px solid var(--va-background-border);">
          {{ $t('rollback_diff_preview') }} ({{ diffRows.length }}개 속성)
        </div>
        <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem;">
          <tbody>
            <tr v-for="row in diffRows" :key="row.key" style="border-bottom: 1px solid var(--va-background-border);">
              <td style="padding: 0.4rem 0.6rem; font-weight: 600; width: 35%;">{{ row.label || row.key }}</td>
              <td style="padding: 0.4rem 0.6rem; color: var(--va-text-secondary); width: 65%;">{{ row.before }} ➔ <b style="color: var(--va-primary);">{{ row.after }}</b></td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Reason Input -->
      <div>
        <label style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">
          {{ $t('rollback_reason') }} <span style="color: var(--va-danger);">*</span>
        </label>
        <va-input
          v-model="reason"
          type="textarea"
          :autosize="true"
          :min-rows="3"
          :placeholder="$t('rollback_reason_placeholder')"
        />
      </div>

      <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem;">
        <va-button preset="secondary" @click="show = false">{{ $t('cancel') }}</va-button>
        <va-button
          color="warning"
          icon="history"
          :disabled="!reason.trim() || submitting"
          :loading="submitting"
          @click="submitRollback"
        >
          {{ $t('rollback_btn') }}
        </va-button>
      </div>
    </div>
  </AppModal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'vuestic-ui'
import { useCustomFetch } from '~/composables/useCustomFetch'
import AppModal from '~/components/common/AppModal.vue'

const props = defineProps<{
  modelValue: boolean
  recordId: string
  recordDisplayCode?: string
  targetVersion: number
  diffRows?: Array<{ key: string; label?: string; before: string; after: string }>
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success', data: any): void
}>()

const { t } = useI18n()
const toast = useToast()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const reason = ref('')
const submitting = ref(false)

watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    reason.value = ''
  }
})

const submitRollback = async () => {
  if (!reason.value.trim() || !props.recordId || !props.targetVersion) return
  submitting.value = true
  try {
    const res = await useCustomFetch(`/records/${props.recordId}/rollback`, {
      method: 'POST',
      body: {
        targetVersion: props.targetVersion,
        reason: reason.value.trim()
      }
    })
    if (res.data?.value?.success || res.status?.value === 'success') {
      toast.init({
        message: t('rollback_success', { version: props.targetVersion }),
        color: 'success'
      })
      emit('success', res.data?.value)
      show.value = false
    } else {
      throw new Error(res.error?.value?.message || 'Rollback failed')
    }
  } catch (err: any) {
    toast.init({
      message: err.message || 'Error occurred during rollback',
      color: 'danger'
    })
  } finally {
    submitting.value = false
  }
}
</script>
