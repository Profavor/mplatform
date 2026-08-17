<template>
  <AppModal
    v-model="show"
    :title="$t('approval_sandbox')"
    icon="science"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="info" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🧪 {{ $t('approval_sandbox_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="previewData" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Summary Banner -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center;">
            <div style="display: flex; gap: 0.5rem; align-items: center;">
              <span style="font-weight: 700; font-size: 0.9rem; color: var(--va-text-primary);">
                {{ previewData.summary }}
              </span>
            </div>
            <div style="display: flex; gap: 0.4rem;">
              <va-badge :text="previewData.targetType" color="primary" size="small" />
              <va-badge :text="previewData.actionType" :color="previewData.actionType === 'CREATE' ? 'success' : 'warning'" size="small" />
            </div>
          </div>

          <!-- Target Records Simulated Diff -->
          <div v-for="target in previewData.targetRecords" :key="target.recordId" style="display: flex; flex-direction: column; gap: 0.5rem;">
            <div style="font-weight: 700; font-size: 0.88rem; color: var(--va-text-primary);">
              🎯 대상 레코드: {{ target.recordCode }}
            </div>

            <div style="max-height: 280px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
              <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
                <thead>
                  <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                    <th style="padding: 0.5rem 0.75rem;">필드</th>
                    <th style="padding: 0.5rem 0.75rem; width: 100px;">상태</th>
                    <th style="padding: 0.5rem 0.75rem;">현재 값 (Before)</th>
                    <th style="padding: 0.5rem 0.75rem;">승인 후 반영값 (After)</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="diff in target.fieldDiffs"
                    :key="diff.fieldKey"
                    style="border-bottom: 1px solid var(--va-background-border);"
                    :style="{ background: getRowBackground(diff.diffStatus) }"
                  >
                    <td style="padding: 0.5rem 0.75rem; font-weight: 600;">{{ diff.fieldName }}</td>
                    <td style="padding: 0.5rem 0.75rem;">
                      <va-badge
                        :text="diff.diffStatus"
                        :color="getStatusColor(diff.diffStatus)"
                        size="small"
                      />
                    </td>
                    <td style="padding: 0.5rem 0.75rem; color: var(--va-text-secondary);">{{ diff.v1Value }}</td>
                    <td style="padding: 0.5rem 0.75rem; font-weight: 700; color: var(--va-text-primary);">{{ diff.v2Value }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
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
  requestId?: string
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

const getStatusColor = (status: string) => {
  switch (status) {
    case 'ADDED': return 'success'
    case 'MODIFIED': return 'warning'
    case 'REMOVED': return 'danger'
    default: return 'secondary'
  }
}

const getRowBackground = (status: string) => {
  switch (status) {
    case 'ADDED': return 'rgba(var(--va-success-rgb), 0.05)'
    case 'MODIFIED': return 'rgba(var(--va-warning-rgb), 0.05)'
    case 'REMOVED': return 'rgba(var(--va-danger-rgb), 0.05)'
    default: return 'transparent'
  }
}

const fetchSandboxPreview = async () => {
  if (!props.requestId) return
  loading.value = true
  try {
    const res = await useCustomFetch(`/approvals/${props.requestId}/sandbox-preview`)
    if (res.data?.value) {
      previewData.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to fetch sandbox preview', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) fetchSandboxPreview()
})
</script>
