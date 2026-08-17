<template>
  <AppModal
    v-model="show"
    :title="$t('reference_integrity')"
    icon="link"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🔗 {{ $t('reference_integrity_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="report" style="display: flex; flex-direction: column; gap: 1.25rem;">
          <!-- Score Summary Card -->
          <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 0.75rem;">
            <div style="padding: 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); text-align: center;">
              <div style="font-size: 0.8rem; color: var(--va-text-secondary); margin-bottom: 0.25rem;">{{ $t('integrity_score') }}</div>
              <div style="font-size: 1.8rem; font-weight: 800;" :style="{ color: report.integrityScore >= 90 ? 'var(--va-success)' : 'var(--va-danger)' }">
                {{ report.integrityScore }}점
              </div>
            </div>

            <div style="padding: 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); text-align: center;">
              <div style="font-size: 0.8rem; color: var(--va-text-secondary); margin-bottom: 0.25rem;">{{ $t('scanned_records') }}</div>
              <div style="font-size: 1.6rem; font-weight: 700; color: var(--va-text-primary);">
                {{ report.totalScannedRecords }}건
              </div>
            </div>

            <div style="padding: 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); text-align: center;">
              <div style="font-size: 0.8rem; color: var(--va-text-secondary); margin-bottom: 0.25rem;">{{ $t('orphan_count') }}</div>
              <div style="font-size: 1.6rem; font-weight: 700;" :style="{ color: report.orphanCount > 0 ? 'var(--va-danger)' : 'var(--va-success)' }">
                {{ report.orphanCount }}건
              </div>
            </div>
          </div>

          <!-- Orphan Violations Table -->
          <div v-if="report.violations?.length > 0">
            <div style="font-weight: 700; font-size: 0.9rem; margin-bottom: 0.5rem; color: var(--va-danger);">
              ⚠️ {{ $t('orphan_details') }} ({{ report.violations.length }}건):
            </div>

            <div style="max-height: 250px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
              <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
                <thead>
                  <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                    <th style="padding: 0.5rem 0.75rem;">식별 코드</th>
                    <th style="padding: 0.5rem 0.75rem;">참조 필드</th>
                    <th style="padding: 0.5rem 0.75rem;">대상 ID</th>
                    <th style="padding: 0.5rem 0.75rem;">진단 결과</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(v, idx) in report.violations" :key="idx" style="border-bottom: 1px solid var(--va-background-border);">
                    <td style="padding: 0.5rem 0.75rem; font-weight: 700; color: var(--va-primary);">{{ v.sourceRecordCode }}</td>
                    <td style="padding: 0.5rem 0.75rem;">{{ v.sourceFieldKey }}</td>
                    <td style="padding: 0.5rem 0.75rem; font-family: monospace; font-size: 0.75rem;">{{ v.targetRecordId }}</td>
                    <td style="padding: 0.5rem 0.75rem; color: var(--va-danger);">{{ v.message }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div v-else style="text-align: center; padding: 2rem; color: var(--va-text-secondary);">
            <va-icon name="verified" size="large" color="success" style="margin-bottom: 0.5rem;" />
            <p>{{ $t('no_orphan_records') }}</p>
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
  domainId?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const report = ref<any>(null)
const loading = ref(false)

const scanIntegrity = async () => {
  if (!props.domainId) return
  loading.value = true
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/integrity/scan`, {
      method: 'POST'
    })
    if (res.data?.value) {
      report.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to scan reference integrity', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) scanIntegrity()
})
</script>
