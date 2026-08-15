<template>
  <va-modal
    v-model="show"
    :title="$t('smart_query')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🤖 {{ $t('smart_query_desc') }}
      </va-alert>

      <!-- Query Input -->
      <div style="display: flex; gap: 0.5rem;">
        <va-input
          v-model="queryInput"
          :placeholder="$t('query_placeholder')"
          style="flex: 1;"
          @keyup.enter="searchQuery"
        />
        <va-button
          color="primary"
          :loading="loading"
          @click="searchQuery"
        >
          {{ $t('execute_query') }}
        </va-button>
      </div>

      <va-inner-loading :loading="loading">
        <div v-if="resultData" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Explanation & Parsed Filters -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; flex-direction: column; gap: 0.5rem;">
            <div style="font-weight: 700; font-size: 0.88rem; color: var(--va-primary);">
              💡 {{ resultData.explanation }}
            </div>
            <div style="display: flex; gap: 0.4rem; align-items: center; flex-wrap: wrap;">
              <span style="font-size: 0.8rem; font-weight: 600;">{{ $t('parsed_filters') }}:</span>
              <va-badge
                v-for="(f, i) in resultData.parsedFilters"
                :key="i"
                :text="`${f.fieldKey} ${f.operator} '${f.value}'`"
                color="info"
                size="small"
              />
            </div>
          </div>

          <!-- Results Table -->
          <div v-if="resultData.records?.length > 0" style="max-height: 260px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.5rem 0.75rem; width: 110px;">식별 코드</th>
                  <th style="padding: 0.5rem 0.75rem;">데이터 상세</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(r, i) in resultData.records"
                  :key="i"
                  style="border-bottom: 1px solid var(--va-background-border);"
                >
                  <td style="padding: 0.5rem 0.75rem; font-weight: 700; color: var(--va-primary);">{{ r._recordCode }}</td>
                  <td style="padding: 0.5rem 0.75rem; color: var(--va-text-secondary);">
                    {{ formatRecordData(r) }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-else style="text-align: center; padding: 1.5rem; color: var(--va-text-secondary); font-size: 0.85rem;">
            검색 조건과 일치하는 레코드가 없습니다.
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
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'

const props = defineProps<{
  modelValue: boolean
  domainId?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const queryInput = ref('')
const resultData = ref<any>(null)
const loading = ref(false)

const searchQuery = async () => {
  if (!props.domainId || !queryInput.value.trim()) return
  loading.value = true
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/smart-query`, {
      method: 'POST',
      body: {
        naturalLanguageQuery: queryInput.value.trim()
      }
    })
    if (res.data?.value) {
      resultData.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to execute smart query', e)
  } finally {
    loading.value = false
  }
}

const formatRecordData = (row: any) => {
  const clone = { ...row }
  delete clone._recordCode
  return JSON.stringify(clone)
}
</script>
