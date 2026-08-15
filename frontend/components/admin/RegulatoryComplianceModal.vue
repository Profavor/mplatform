<template>
  <va-modal
    v-model="show"
    :title="$t('regulatory_compliance')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🛡️ {{ $t('regulatory_compliance_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="report" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Compliance Score & Readiness Banner -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center;">
            <div style="display: flex; flex-direction: column; gap: 0.2rem;">
              <span style="font-weight: 700; font-size: 0.88rem;">{{ report.summary }}</span>
              <span style="font-size: 0.75rem; color: var(--va-text-secondary);">
                {{ $t('compliance_score') }}: <strong style="color: var(--va-success); font-size: 0.9rem;">{{ report.overallScore }}%</strong>
              </span>
            </div>
            <div style="display: flex; gap: 0.5rem; align-items: center;">
              <va-badge
                :text="$t('certification_readiness') + ': ' + report.certificationReadiness"
                color="success"
                size="small"
              />
              <va-button size="small" preset="secondary" @click="loadAudit">
                {{ $t('run_audit') }}
              </va-button>
            </div>
          </div>

          <!-- Items Table -->
          <div style="max-height: 280px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.5rem 0.75rem;">규제 프레임워크</th>
                  <th style="padding: 0.5rem 0.75rem;">{{ $t('control_code') }} / 명칭</th>
                  <th style="padding: 0.5rem 0.75rem; width: 70px; text-align: center;">상태</th>
                  <th style="padding: 0.5rem 0.75rem;">{{ $t('evidence') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(item, idx) in report.items"
                  :key="idx"
                  style="border-bottom: 1px solid var(--va-background-border);"
                >
                  <td style="padding: 0.5rem 0.75rem; font-weight: 700; color: var(--va-primary);">
                    {{ item.framework }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem;">
                    <div style="font-weight: 600;">{{ item.controlCode }}</div>
                    <div style="font-size: 0.75rem; color: var(--va-text-secondary);">{{ item.controlTitle }}</div>
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: center;">
                    <va-badge
                      :text="item.status"
                      color="success"
                      size="small"
                    />
                  </td>
                  <td style="padding: 0.5rem 0.75rem; font-size: 0.75rem; color: var(--va-text-primary);">
                    <div>{{ item.evidence }}</div>
                    <div style="font-size: 0.7rem; color: var(--va-text-secondary); margin-top: 0.2rem;">💡 {{ item.remediation }}</div>
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
  </va-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()
const { customFetch } = useCustomFetch()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const report = ref<any>(null)
const loading = ref(false)

const loadAudit = async () => {
  loading.value = true
  try {
    const res = await customFetch('/api/compliance/regulatory-audit')
    const payload = res?.checklist ? res : res?.data?.value
    if (payload) {
      report.value = payload
    }
  } catch (e: any) {
    console.error('Failed to load regulatory audit report', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) loadAudit()
}, { immediate: true })
</script>
