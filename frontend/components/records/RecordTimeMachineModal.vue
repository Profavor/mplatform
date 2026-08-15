<template>
  <va-modal
    v-model="show"
    :title="$t('time_machine')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        ⏳ {{ $t('time_machine_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="diffData" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Version Selector Bar -->
          <div style="display: flex; gap: 1rem; align-items: center; background: var(--va-background-element); padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border);">
            <div style="display: flex; align-items: center; gap: 0.5rem; flex: 1;">
              <span style="font-size: 0.85rem; font-weight: 600; color: var(--va-text-secondary);">
                {{ $t('base_version') }}:
              </span>
              <va-select
                v-model="selectedV1"
                :options="versionOptions"
                value-by="value"
                text-by="text"
                size="small"
                style="width: 140px;"
                @update:model-value="fetchDiff"
              />
            </div>

            <va-icon name="compare_arrows" size="1.4rem" color="primary" />

            <div style="display: flex; align-items: center; gap: 0.5rem; flex: 1;">
              <span style="font-size: 0.85rem; font-weight: 600; color: var(--va-text-secondary);">
                {{ $t('target_version') }}:
              </span>
              <va-select
                v-model="selectedV2"
                :options="versionOptions"
                value-by="value"
                text-by="text"
                size="small"
                style="width: 140px;"
                @update:model-value="fetchDiff"
              />
            </div>
          </div>

          <!-- Field Diff Comparison Table -->
          <div style="max-height: 320px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.5rem 0.75rem;">필드명</th>
                  <th style="padding: 0.5rem 0.75rem; width: 100px;">{{ $t('diff_status') }}</th>
                  <th style="padding: 0.5rem 0.75rem;">v{{ selectedV1 }} (이전 값)</th>
                  <th style="padding: 0.5rem 0.75rem;">v{{ selectedV2 }} (이후 값)</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="item in diffData.fieldDiffs"
                  :key="item.fieldKey"
                  style="border-bottom: 1px solid var(--va-background-border);"
                  :style="{ background: getRowBackground(item.diffStatus) }"
                >
                  <td style="padding: 0.5rem 0.75rem; font-weight: 600;">{{ item.fieldName }}</td>
                  <td style="padding: 0.5rem 0.75rem;">
                    <va-badge
                      :text="getDiffLabel(item.diffStatus)"
                      :color="getDiffBadgeColor(item.diffStatus)"
                      size="small"
                    />
                  </td>
                  <td style="padding: 0.5rem 0.75rem; color: var(--va-text-secondary);">
                    {{ item.v1Value }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem; font-weight: 600; color: var(--va-text-primary);">
                    {{ item.v2Value }}
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
  recordId?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const diffData = ref<any>(null)
const selectedV1 = ref<number>(1)
const selectedV2 = ref<number>(1)
const loading = ref(false)

const versionOptions = computed(() => {
  if (!diffData.value?.allVersions) return []
  return diffData.value.allVersions.map((v: any) => ({
    text: `v${v.version} (${v.changeType})`,
    value: v.version
  }))
})

const getDiffBadgeColor = (status: string) => {
  switch (status) {
    case 'ADDED': return 'success'
    case 'MODIFIED': return 'warning'
    case 'REMOVED': return 'danger'
    default: return 'secondary'
  }
}

const getDiffLabel = (status: string) => {
  switch (status) {
    case 'ADDED': return t('diff_added')
    case 'MODIFIED': return t('diff_modified')
    case 'REMOVED': return t('diff_removed')
    default: return t('diff_unchanged')
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

const fetchDiff = async () => {
  if (!props.recordId) return
  loading.value = true
  try {
    const url = `/records/${props.recordId}/timemachine/diff?v1=${selectedV1.value}&v2=${selectedV2.value}`
    const res = await useCustomFetch(url)
    if (res.data?.value) {
      diffData.value = res.data.value
      selectedV1.value = res.data.value.v1
      selectedV2.value = res.data.value.v2
    }
  } catch (e: any) {
    console.error('Failed to fetch time-machine diff', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    selectedV1.value = 1
    selectedV2.value = 1
    fetchDiff()
  }
})
</script>
