<template>
  <va-modal
    v-model="show"
    :title="$t('rejection_analytics')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        📊 {{ $t('rejection_analytics_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="analyticsData" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Summary Banner -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center;">
            <div style="display: flex; gap: 0.5rem; align-items: center;">
              <span style="font-weight: 700; font-size: 0.9rem;">
                {{ analyticsData.summary }}
              </span>
            </div>
            <va-badge :text="`총 ${analyticsData.totalRejections}건 분석됨`" color="danger" size="small" />
          </div>

          <!-- Cause Distribution -->
          <div style="display: flex; flex-direction: column; gap: 0.5rem;">
            <div style="font-weight: 700; font-size: 0.85rem;">{{ $t('rejection_cause_distribution') }}</div>
            <div
              v-for="cat in analyticsData.topCategories"
              :key="cat.category"
              style="display: flex; flex-direction: column; gap: 0.25rem; padding: 0.5rem 0.75rem; border: 1px solid var(--va-background-border); border-radius: 6px;"
            >
              <div style="display: flex; justify-content: space-between; font-size: 0.82rem;">
                <span style="font-weight: 600;">{{ cat.category }} ({{ cat.count }}건)</span>
                <span style="font-weight: 700; color: var(--va-danger);">{{ cat.percentage }}%</span>
              </div>
              <va-progress-bar :model-value="cat.percentage" color="danger" size="small" />
              <div style="font-size: 0.75rem; color: var(--va-text-secondary); margin-top: 0.2rem;">
                💡 {{ cat.guide }}
              </div>
            </div>
          </div>

          <!-- Resubmit Checklist -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; flex-direction: column; gap: 0.4rem;">
            <div style="font-weight: 700; font-size: 0.85rem; color: var(--va-primary);">
              ✅ {{ $t('resubmit_checklist') }}
            </div>
            <ul style="margin: 0; padding-left: 1.2rem; font-size: 0.8rem; color: var(--va-text-primary); line-height: 1.6;">
              <li v-for="(item, i) in analyticsData.recommendedChecklist" :key="i">
                {{ item }}
              </li>
            </ul>
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

const analyticsData = ref<any>(null)
const loading = ref(false)

const loadAnalytics = async () => {
  loading.value = true
  try {
    const res = await useCustomFetch('/approvals/analytics/rejections')
    if (res.data?.value) {
      analyticsData.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to load rejection analytics', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) loadAnalytics()
})
</script>
