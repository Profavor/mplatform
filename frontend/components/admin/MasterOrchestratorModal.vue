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
              :text="'가동: ' + orchestratorData.healthyFeatures + ' / ' + orchestratorData.totalFeatures + ' (' + Math.round((orchestratorData.healthyFeatures / orchestratorData.totalFeatures) * 100) + '%)'"
              color="success"
              size="large"
            />
          </div>

          <!-- Category Distribution & Filter Badges -->
          <div style="display: flex; flex-wrap: wrap; gap: 0.5rem; align-items: center;">
            <span style="font-size: 0.78rem; font-weight: 600; color: var(--va-text-secondary); margin-right: 0.25rem;">카테고리 필터:</span>
            <va-badge
              text="전체 (50)"
              :color="selectedCategory === null ? 'primary' : 'secondary'"
              size="small"
              style="cursor: pointer;"
              @click="selectedCategory = null"
            />
            <va-badge
              text="품질(DQ): 8"
              :color="selectedCategory === 'DQ_QUALITY' ? 'info' : 'info'"
              :outline="selectedCategory !== null && selectedCategory !== 'DQ_QUALITY'"
              size="small"
              style="cursor: pointer;"
              @click="toggleCategory('DQ_QUALITY')"
            />
            <va-badge
              text="보안·규제: 7"
              :color="selectedCategory === 'SECURITY_COMPLIANCE' ? 'warning' : 'warning'"
              :outline="selectedCategory !== null && selectedCategory !== 'SECURITY_COMPLIANCE'"
              size="small"
              style="cursor: pointer;"
              @click="toggleCategory('SECURITY_COMPLIANCE')"
            />
            <va-badge
              text="결재·워크플로우: 6"
              :color="selectedCategory === 'WORKFLOW_APPROVAL' ? 'primary' : 'primary'"
              :outline="selectedCategory !== null && selectedCategory !== 'WORKFLOW_APPROVAL'"
              size="small"
              style="cursor: pointer;"
              @click="toggleCategory('WORKFLOW_APPROVAL')"
            />
            <va-badge
              text="연계·파이프라인: 11"
              :color="selectedCategory === 'INTEGRATION_PIPELINE' ? 'success' : 'success'"
              :outline="selectedCategory !== null && selectedCategory !== 'INTEGRATION_PIPELINE'"
              size="small"
              style="cursor: pointer;"
              @click="toggleCategory('INTEGRATION_PIPELINE')"
            />
            <va-badge
              text="스키마·수명주기: 10"
              :color="selectedCategory === 'SCHEMA_LIFECYCLE' ? 'secondary' : 'secondary'"
              :outline="selectedCategory !== null && selectedCategory !== 'SCHEMA_LIFECYCLE'"
              size="small"
              style="cursor: pointer;"
              @click="toggleCategory('SCHEMA_LIFECYCLE')"
            />
            <va-badge
              text="AI·지능형 혁신: 8"
              :color="selectedCategory === 'AI_INNOVATION' ? 'danger' : 'danger'"
              :outline="selectedCategory !== null && selectedCategory !== 'AI_INNOVATION'"
              size="small"
              style="cursor: pointer;"
              @click="toggleCategory('AI_INNOVATION')"
            />
          </div>

          <!-- 50 Features Table -->
          <div style="max-height: 290px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border); position: sticky; top: 0; z-index: 1;">
                  <th style="padding: 0.5rem 0.75rem; width: 60px; text-align: center;">{{ $t('feature_no') }}</th>
                  <th style="padding: 0.5rem 0.75rem; width: 140px;">{{ $t('feature_category') }}</th>
                  <th style="padding: 0.5rem 0.75rem;">{{ $t('feature_name') }}</th>
                  <th style="padding: 0.5rem 0.75rem; text-align: center; width: 110px;">상태</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(mod, idx) in filteredModules"
                  :key="idx"
                  style="border-bottom: 1px solid var(--va-background-border);"
                >
                  <td style="padding: 0.5rem 0.75rem; text-align: center; font-weight: 700; color: var(--va-primary);">
                    #{{ mod.featureNo }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem; font-size: 0.75rem;">
                    <va-badge :text="getCategoryLabel(mod.category)" :color="getCategoryColor(mod.category)" size="small" />
                  </td>
                  <td style="padding: 0.5rem 0.75rem; font-weight: 600;">
                    {{ $t(mod.featureNameKey) }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: center;">
                    <va-badge
                      :text="mod.status === 'ONLINE_HEALTHY' ? 'ONLINE (' + mod.healthScore + ')' : 'DEGRADED (' + mod.healthScore + ')'"
                      :color="mod.status === 'ONLINE_HEALTHY' ? 'success' : 'warning'"
                      size="small"
                    />
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

const orchestratorData = ref<any>(null)
const loading = ref(false)
const selectedCategory = ref<string | null>(null)

const toggleCategory = (cat: string) => {
  selectedCategory.value = selectedCategory.value === cat ? null : cat
}

const getCategoryColor = (category: string): string => {
  switch (category) {
    case 'DQ_QUALITY':
      return 'info'
    case 'SECURITY_COMPLIANCE':
      return 'warning'
    case 'WORKFLOW_APPROVAL':
      return 'primary'
    case 'INTEGRATION_PIPELINE':
      return 'success'
    case 'SCHEMA_LIFECYCLE':
      return 'secondary'
    case 'AI_INNOVATION':
      return 'danger'
    default:
      return 'primary'
  }
}

const getCategoryLabel = (category: string): string => {
  switch (category) {
    case 'DQ_QUALITY':
      return '품질 (DQ)'
    case 'SECURITY_COMPLIANCE':
      return '보안·규제'
    case 'WORKFLOW_APPROVAL':
      return '결재·워크플로우'
    case 'INTEGRATION_PIPELINE':
      return '연계·파이프라인'
    case 'SCHEMA_LIFECYCLE':
      return '스키마·수명주기'
    case 'AI_INNOVATION':
      return 'AI·지능형 혁신'
    default:
      return category
  }
}

const filteredModules = computed(() => {
  if (!orchestratorData.value?.modules) return []
  if (!selectedCategory.value) return orchestratorData.value.modules
  return orchestratorData.value.modules.filter((m: any) => m.category === selectedCategory.value)
})

const loadOrchestrator = async () => {
  loading.value = true
  try {
    const res = await customFetch('/api/system/master-orchestrator')
    const payload = res?.modules ? res : res?.data?.value
    if (payload) {
      orchestratorData.value = payload
    }
  } catch (e: any) {
    console.error('Failed to load master orchestrator data', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) loadOrchestrator()
}, { immediate: true })
</script>
