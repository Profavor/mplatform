<template>
  <AppModal
    v-model="show"
    :title="$t('golden_record')"
    icon="star"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🌟 {{ $t('golden_record_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="previewData" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Summary Banner -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center;">
            <div style="display: flex; gap: 0.5rem; align-items: center;">
              <span style="font-weight: 700; font-size: 0.9rem;">
                {{ previewData.summary }}
              </span>
            </div>
            <div style="display: flex; gap: 0.4rem; align-items: center;">
              <span style="font-size: 0.8rem; color: var(--va-text-secondary);">신뢰도:</span>
              <va-badge :text="`${previewData.confidenceScore}%`" color="success" size="small" />
            </div>
          </div>

          <!-- Candidates Bar -->
          <div style="display: flex; gap: 0.5rem; align-items: center;">
            <span style="font-size: 0.82rem; font-weight: 600;">{{ $t('candidate_records') }}:</span>
            <va-badge
              v-for="code in previewData.candidateRecordCodes"
              :key="code"
              :text="code"
              color="primary"
              size="small"
            />
          </div>

          <!-- Field Choices Table -->
          <div style="max-height: 280px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.5rem 0.75rem;">필드</th>
                  <th style="padding: 0.5rem 0.75rem; color: var(--va-success);">{{ $t('golden_value') }}</th>
                  <th style="padding: 0.5rem 0.75rem;">{{ $t('source_record') }}</th>
                  <th style="padding: 0.5rem 0.75rem;">선정 근거</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(f, idx) in previewData.fieldChoices"
                  :key="idx"
                  style="border-bottom: 1px solid var(--va-background-border);"
                >
                  <td style="padding: 0.5rem 0.75rem; font-weight: 700;">{{ f.fieldName }}</td>
                  <td style="padding: 0.5rem 0.75rem; font-weight: 700; color: var(--va-success);">
                    {{ f.selectedGoldenValue }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem; font-family: monospace;">{{ f.winningSourceRecordCode }}</td>
                  <td style="padding: 0.5rem 0.75rem; font-size: 0.75rem; color: var(--va-text-secondary);">
                    {{ f.reason }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
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
  targetRecordIds?: string[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const previewData = ref<any>(null)
const loading = ref(false)

const buildGoldenRecord = async () => {
  if (!props.targetRecordIds || props.targetRecordIds.length === 0) return
  loading.value = true
  try {
    const res = await useCustomFetch('/records/golden-record/preview', {
      method: 'POST',
      body: {
        targetRecordIds: props.targetRecordIds,
        priorityStrategy: 'SOURCE_PRIORITY'
      }
    })
    if (res.data?.value) {
      previewData.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to build golden record', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) buildGoldenRecord()
})
</script>
