<template>
  <AppModal
    v-model="show"
    :title="$t('sync_pipeline')"
    icon="sync_alt"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🔄 {{ $t('sync_pipeline_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="pipelines?.length > 0" style="max-height: 300px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
          <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
            <thead>
              <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                <th style="padding: 0.5rem 0.75rem;">{{ $t('pipeline_name') }}</th>
                <th style="padding: 0.5rem 0.75rem; width: 140px;">{{ $t('cron_schedule') }}</th>
                <th style="padding: 0.5rem 0.75rem; width: 110px;">{{ $t('last_synced') }}</th>
                <th style="padding: 0.5rem 0.75rem; width: 80px; text-align: center;">상태</th>
                <th style="padding: 0.5rem 0.75rem; width: 90px; text-align: center;">실행</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="p in pipelines"
                :key="p.pipelineId"
                style="border-bottom: 1px solid var(--va-background-border);"
              >
                <td style="padding: 0.5rem 0.75rem;">
                  <div style="font-weight: 700; color: var(--va-primary);">{{ p.name }}</div>
                  <div style="font-size: 0.72rem; color: var(--va-text-secondary);">
                    {{ p.sourceDomainName }} ➔ {{ p.targetDomainName }} (ID: {{ p.pipelineId }})
                  </div>
                </td>
                <td style="padding: 0.5rem 0.75rem; font-family: monospace; font-size: 0.75rem;">
                  {{ p.cronExpression }}
                </td>
                <td style="padding: 0.5rem 0.75rem; color: var(--va-text-secondary); font-size: 0.75rem;">
                  {{ p.lastSyncedCount > 0 ? `${p.lastSyncedCount}건 완료` : '-' }}
                </td>
                <td style="padding: 0.5rem 0.75rem; text-align: center;">
                  <va-badge
                    :text="p.status"
                    :color="p.status === 'SUCCESS' ? 'success' : 'secondary'"
                    size="small"
                  />
                </td>
                <td style="padding: 0.5rem 0.75rem; text-align: center;">
                  <va-button
                    color="primary"
                    size="small"
                    preset="secondary"
                    @click="triggerPipeline(p.pipelineId)"
                  >
                    {{ $t('trigger_pipeline') }}
                  </va-button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-else style="text-align: center; padding: 2rem; color: var(--va-text-secondary);">
          <p>{{ $t('no_pipelines') }}</p>
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
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const pipelines = ref<any[]>([])
const loading = ref(false)

const loadPipelines = async () => {
  loading.value = true
  try {
    const res = await useCustomFetch('/integration/pipelines')
    if (res.data?.value) {
      pipelines.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to load pipelines', e)
  } finally {
    loading.value = false
  }
}

const triggerPipeline = async (pipelineId: string) => {
  try {
    const res = await useCustomFetch(`/integration/pipelines/${pipelineId}/trigger`, {
      method: 'POST'
    })
    if (res.data?.value) {
      alert(res.data.value.message || '파이프라인이 성공적으로 실행되었습니다.')
      await loadPipelines()
    }
  } catch (e: any) {
    console.error('Failed to trigger pipeline', e)
  }
}

watch(() => props.modelValue, (val) => {
  if (val) loadPipelines()
})
</script>
