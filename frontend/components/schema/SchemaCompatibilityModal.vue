<template>
  <AppModal
    v-model="show"
    :title="$t('schema_compatibility')"
    icon="fact_check"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🔍 {{ $t('schema_compatibility_desc') }}
      </va-alert>

      <!-- Input Simulation Section -->
      <div style="display: flex; flex-direction: column; gap: 0.5rem;">
        <va-textarea
          v-model="proposedChanges"
          label="변경 예정 스키마 DDL 또는 변경 기술서"
          placeholder="예: ALTER TABLE customer DROP COLUMN legacy_code; ALTER TABLE customer ALTER COLUMN biz_reg_no SET NOT NULL;"
          :rows="3"
        />
        <div style="display: flex; justify-content: flex-end;">
          <va-button size="small" color="primary" :loading="loading" @click="runAnalysis">
            {{ $t('check_compatibility') }}
          </va-button>
        </div>
      </div>

      <!-- Report Section -->
      <va-inner-loading :loading="loading">
        <div v-if="report" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Summary Banner -->
          <div
            style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); display: flex; justify-content: space-between; align-items: center;"
            :style="{ background: report.overallCompatibility === 'BREAKING_CHANGE' ? 'rgba(235, 59, 90, 0.1)' : 'rgba(32, 191, 107, 0.1)' }"
          >
            <span style="font-weight: 700; font-size: 0.85rem;">
              {{ report.summary }}
            </span>
            <va-badge
              :text="report.overallCompatibility === 'BREAKING_CHANGE' ? $t('breaking_change_detected') : $t('compatible_status')"
              :color="report.overallCompatibility === 'BREAKING_CHANGE' ? 'danger' : 'success'"
              size="small"
            />
          </div>

          <!-- Risk Items Table -->
          <div style="max-height: 250px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.5rem 0.75rem;">대상 필드</th>
                  <th style="padding: 0.5rem 0.75rem;">변경 유형</th>
                  <th style="padding: 0.5rem 0.75rem; width: 80px; text-align: center;">위험도</th>
                  <th style="padding: 0.5rem 0.75rem;">영향 및 완화 가이드</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(r, idx) in report.risks"
                  :key="idx"
                  style="border-bottom: 1px solid var(--va-background-border);"
                >
                  <td style="padding: 0.5rem 0.75rem; font-weight: 700;">{{ r.fieldKey }}</td>
                  <td style="padding: 0.5rem 0.75rem; font-family: monospace; font-size: 0.75rem;">{{ r.changeType }}</td>
                  <td style="padding: 0.5rem 0.75rem; text-align: center;">
                    <va-badge
                      :text="r.riskLevel"
                      :color="r.riskLevel === 'CRITICAL' ? 'danger' : (r.riskLevel === 'WARNING' ? 'warning' : 'info')"
                      size="small"
                    />
                  </td>
                  <td style="padding: 0.5rem 0.75rem; color: var(--va-text-secondary); font-size: 0.78rem;">
                    <div>{{ r.impactDescription }}</div>
                    <div style="color: var(--va-primary); margin-top: 0.2rem;">💡 {{ r.mitigationGuide }}</div>
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
  domainId: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const proposedChanges = ref('')
const report = ref<any>(null)
const loading = ref(false)

const runAnalysis = async () => {
  if (!props.domainId) return
  loading.value = true
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/schema/compatibility-check`, {
      method: 'POST',
      body: {
        proposedChanges: proposedChanges.value
      }
    })
    if (res.data?.value) {
      report.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to analyze schema compatibility', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    proposedChanges.value = ''
    runAnalysis()
  }
})
</script>
