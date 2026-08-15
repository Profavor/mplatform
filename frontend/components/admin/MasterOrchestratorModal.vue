<template>
  <va-modal
    v-model="show"
    :title="$t('master_orchestrator')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🌟 {{ $t('master_orchestrator_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="orchestratorData" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Overall Banner -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center;">
            <div style="display: flex; flex-direction: column; gap: 0.2rem;">
              <span style="font-weight: 700; font-size: 0.88rem;">{{ orchestratorData.summary }}</span>
              <span style="font-size: 0.75rem; color: var(--va-text-secondary);">
                {{ orchestratorData.systemMaturityLevel }}
              </span>
            </div>
            <va-badge
              :text="'가동: ' + orchestratorData.healthyFeatures + ' / ' + orchestratorData.totalFeatures + ' (100%)'"
              color="success"
              size="large"
            />
          </div>

          <!-- Category Distribution Badges -->
          <div style="display: flex; flex-wrap: wrap; gap: 0.5rem;">
            <va-badge text="품질(DQ): 8" color="info" size="small" />
            <va-badge text="보안·규제: 7" color="warning" size="small" />
            <va-badge text="결재·워크플로우: 6" color="primary" size="small" />
            <va-badge text="연계·파이프라인: 11" color="success" size="small" />
            <va-badge text="스키마·수명주기: 10" color="secondary" size="small" />
            <va-badge text="AI·지능형 혁신: 8" color="danger" size="small" />
          </div>

          <!-- 50 Features Table -->
          <div style="max-height: 270px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.5rem 0.75rem; width: 60px; text-align: center;">{{ $t('feature_no') }}</th>
                  <th style="padding: 0.5rem 0.75rem; width: 140px;">{{ $t('feature_category') }}</th>
                  <th style="padding: 0.5rem 0.75rem;">{{ $t('feature_name') }}</th>
                  <th style="padding: 0.5rem 0.75rem; text-align: center; width: 90px;">상태</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(mod, idx) in orchestratorData.modules"
                  :key="idx"
                  style="border-bottom: 1px solid var(--va-background-border);"
                >
                  <td style="padding: 0.5rem 0.75rem; text-align: center; font-weight: 700; color: var(--va-primary);">
                    #{{ mod.featureNo }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem; font-size: 0.75rem;">
                    <va-badge :text="mod.category" color="info" size="small" />
                  </td>
                  <td style="padding: 0.5rem 0.75rem; font-weight: 600;">
                    {{ mod.featureName }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: center;">
                    <va-badge text="Online (100)" color="success" size="small" />
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

const orchestratorData = ref<any>(null)
const loading = ref(false)

const loadOrchestrator = async () => {
  loading.value = true
  try {
    const res = await useCustomFetch('/system/master-orchestrator')
    if (res.data?.value) {
      orchestratorData.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to load master orchestrator data', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) loadOrchestrator()
})
</script>
