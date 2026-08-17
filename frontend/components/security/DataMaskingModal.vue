<template>
  <AppModal
    v-model="show"
    :title="$t('data_masking')"
    icon="visibility_off"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🔒 {{ $t('data_masking_desc') }}
      </va-alert>

      <!-- Toggle Mode Bar -->
      <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element);">
        <div style="display: flex; gap: 0.5rem; align-items: center;">
          <span style="font-weight: 700; font-size: 0.9rem;">
            🎯 식별 코드: {{ maskedData?.recordCode || '-' }}
          </span>
          <va-badge
            v-if="maskedData"
            :text="`마스킹 필드: ${maskedData.maskedFieldCount}개`"
            color="warning"
            size="small"
          />
        </div>

        <div style="display: flex; gap: 0.5rem; align-items: center;">
          <span style="font-size: 0.82rem; color: var(--va-text-secondary);">
            {{ simulateUnmasked ? $t('unmasked_preview') : $t('masked_preview') }}
          </span>
          <va-switch
            v-model="simulateUnmasked"
            size="small"
            color="primary"
            @update:model-value="fetchMaskedData"
          />
        </div>
      </div>

      <!-- Data Table -->
      <va-inner-loading :loading="loading">
        <div style="max-height: 300px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
          <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
            <thead>
              <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                <th style="padding: 0.5rem 0.75rem; width: 140px;">필드 키</th>
                <th style="padding: 0.5rem 0.75rem;">표시 데이터</th>
                <th style="padding: 0.5rem 0.75rem; width: 100px;">보안 상태</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(val, key) in currentFields"
                :key="key"
                style="border-bottom: 1px solid var(--va-background-border);"
              >
                <td style="padding: 0.5rem 0.75rem; font-weight: 600;">{{ key }}</td>
                <td style="padding: 0.5rem 0.75rem; font-family: monospace;">{{ val || '-' }}</td>
                <td style="padding: 0.5rem 0.75rem;">
                  <va-badge
                    v-if="isFieldMasked(String(val))"
                    text="MASKED"
                    color="warning"
                    size="small"
                  />
                  <va-badge
                    v-else
                    text="PLAIN"
                    color="secondary"
                    size="small"
                  />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </va-inner-loading>

      <div style="display: flex; justify-content: flex-end; margin-top: 0.5rem;">
        <va-button preset="secondary" @click="show = false">
          {{ $t('close') }}
        </va-button>
      </div>
    </div>
  </AppModal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'
import AppModal from '~/components/common/AppModal.vue'

const props = defineProps<{
  modelValue: boolean
  recordId?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const maskedData = ref<any>(null)
const loading = ref(false)
const simulateUnmasked = ref(false)

const currentFields = computed(() => {
  if (!maskedData.value) return {}
  return simulateUnmasked.value && maskedData.value.originalData && Object.keys(maskedData.value.originalData).length > 0
    ? maskedData.value.originalData
    : maskedData.value.maskedData || {}
})

const isFieldMasked = (val: string) => {
  return val && val.includes('*')
}

const fetchMaskedData = async () => {
  if (!props.recordId) return
  loading.value = true
  try {
    const res = await useCustomFetch(`/records/${props.recordId}/masked/preview?simulateUnmasked=${simulateUnmasked.value}`, {
      method: 'POST'
    })
    if (res.data?.value) {
      maskedData.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to fetch masked record data', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    simulateUnmasked.value = false
    fetchMaskedData()
  }
})
</script>
