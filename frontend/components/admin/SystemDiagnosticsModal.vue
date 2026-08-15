<template>
  <va-modal
    v-model="show"
    :title="$t('system_diagnostics')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🩺 {{ $t('system_diagnostics_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="diagnosticsData" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Summary Banner -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center;">
            <div style="display: flex; flex-direction: column; gap: 0.2rem;">
              <span style="font-weight: 700; font-size: 0.9rem;">
                {{ diagnosticsData.summary }}
              </span>
            </div>
            <div style="display: flex; gap: 0.5rem; align-items: center;">
              <va-badge
                :text="diagnosticsData.overallStatus"
                :color="diagnosticsData.overallStatus === 'HEALTHY' ? 'success' : 'warning'"
                size="small"
              />
              <va-button preset="secondary" size="small" @click="loadDiagnostics">
                {{ $t('run_diagnostics') }}
              </va-button>
            </div>
          </div>

          <!-- Components Table -->
          <div style="max-height: 280px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.5rem 0.75rem;">{{ $t('component_name') }}</th>
                  <th style="padding: 0.5rem 0.75rem; width: 80px; text-align: center;">상태</th>
                  <th style="padding: 0.5rem 0.75rem; width: 90px; text-align: right;">{{ $t('latency') }}</th>
                  <th style="padding: 0.5rem 0.75rem;">세부 진단 정보</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="c in diagnosticsData.components"
                  :key="c.componentName"
                  style="border-bottom: 1px solid var(--va-background-border);"
                >
                  <td style="padding: 0.5rem 0.75rem; font-weight: 700; color: var(--va-primary);">
                    {{ c.componentName }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: center;">
                    <va-badge
                      :text="c.status"
                      :color="c.status === 'UP' ? 'success' : 'danger'"
                      size="small"
                    />
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: right; font-weight: 600;">
                    {{ c.latencyMs }}ms
                  </td>
                  <td style="padding: 0.5rem 0.75rem; color: var(--va-text-secondary); font-size: 0.78rem;">
                    {{ c.details }}
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

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const diagnosticsData = ref<any>(null)
const loading = ref(false)

const loadDiagnostics = async () => {
  loading.value = true
  try {
    const res = await useCustomFetch('/system/diagnostics')
    if (res.data?.value) {
      diagnosticsData.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to load system diagnostics', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) loadDiagnostics()
})
</script>
