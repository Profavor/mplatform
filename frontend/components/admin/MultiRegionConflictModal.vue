<template>
  <AppModal
    v-model="show"
    :title="$t('multi_region_conflict')"
    icon="public"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🌐 {{ $t('multi_region_conflict_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="conflictReport" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Summary Banner -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center;">
            <span style="font-weight: 700; font-size: 0.85rem;">{{ conflictReport.summary }}</span>
            <va-badge
              :text="'자율 해소: ' + conflictReport.autoResolvedCount + ' / ' + (conflictReport.autoResolvedCount + conflictReport.activeConflicts) + ' (100%)'"
              color="success"
              size="small"
            />
          </div>

          <!-- Conflicts Table -->
          <div style="max-height: 270px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.5rem 0.75rem;">식별 코드 / 필드</th>
                  <th style="padding: 0.5rem 0.75rem;">{{ $t('region_pair') }}</th>
                  <th style="padding: 0.5rem 0.75rem; color: var(--va-success);">{{ $t('resolved_value') }}</th>
                  <th style="padding: 0.5rem 0.75rem; width: 140px;">{{ $t('resolution_strategy') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(item, idx) in conflictReport.conflicts"
                  :key="idx"
                  style="border-bottom: 1px solid var(--va-background-border);"
                >
                  <td style="padding: 0.5rem 0.75rem; font-weight: 700;">
                    <div>{{ item.recordCode }}</div>
                    <span style="font-size: 0.72rem; color: var(--va-text-secondary); font-family: monospace;">{{ item.domainCode }} &bull; {{ item.fieldKey }}</span>
                  </td>
                  <td style="padding: 0.5rem 0.75rem; font-size: 0.75rem;">
                    <div>A: {{ item.regionA }} (<strong>{{ item.valueA }}</strong>)</div>
                    <div style="color: var(--va-text-secondary); margin-top: 0.2rem;">B: {{ item.regionB }} (<strong>{{ item.valueB }}</strong>)</div>
                  </td>
                  <td style="padding: 0.5rem 0.75rem; font-weight: 700; color: var(--va-success);">
                    {{ item.resolvedValue }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem;">
                    <va-badge :text="item.resolutionStrategy" color="info" size="small" />
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

const conflictReport = ref<any>(null)
const loading = ref(false)

const loadConflicts = async () => {
  loading.value = true
  try {
    const res = await customFetch('/api/system/multi-region-conflicts')
    const payload = res?.regions ? res : res?.data?.value
    if (payload) {
      conflictReport.value = payload
    }
  } catch (e: any) {
    console.error('Failed to load multi-region conflicts', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) loadConflicts()
}, { immediate: true })
</script>
