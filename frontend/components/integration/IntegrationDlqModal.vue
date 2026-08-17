<template>
  <AppModal
    v-model="show"
    :title="$t('dlq_hub')"
    icon="mark_email_unread"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="danger" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        📬 {{ $t('dlq_hub_desc') }}
      </va-alert>

      <!-- Action Bar -->
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div style="display: flex; gap: 0.5rem; align-items: center;">
          <va-button
            preset="secondary"
            size="small"
            icon="refresh"
            :loading="loading"
            @click="fetchDlqItems"
          >
            {{ $t('refresh') }}
          </va-button>
          <span style="font-size: 0.85rem; color: var(--va-text-secondary);">
            총 {{ dlqItems.length }}건 대기
          </span>
        </div>

        <div style="display: flex; gap: 0.5rem;">
          <va-button
            color="warning"
            size="small"
            :disabled="selectedIds.length === 0"
            :loading="retrying"
            @click="retrySelected"
          >
            {{ $t('retry_selected') }} ({{ selectedIds.length }})
          </va-button>
          <va-button
            color="danger"
            size="small"
            :disabled="dlqItems.length === 0"
            :loading="retrying"
            @click="retryAll"
          >
            {{ $t('retry_all') }}
          </va-button>
        </div>
      </div>

      <!-- DLQ Table -->
      <va-inner-loading :loading="loading">
        <div style="max-height: 320px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
          <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
            <thead>
              <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                <th style="padding: 0.5rem 0.75rem; width: 40px; text-align: center;">
                  <input
                    type="checkbox"
                    :checked="isAllSelected"
                    @change="toggleSelectAll"
                  />
                </th>
                <th style="padding: 0.5rem 0.75rem;">채널명</th>
                <th style="padding: 0.5rem 0.75rem;">대상 레코드</th>
                <th style="padding: 0.5rem 0.75rem;">이벤트</th>
                <th style="padding: 0.5rem 0.75rem;">{{ $t('error_message') }}</th>
                <th style="padding: 0.5rem 0.75rem; width: 80px;">{{ $t('retry_count') }}</th>
                <th style="padding: 0.5rem 0.75rem; width: 130px;">발생 시각</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="item in dlqItems"
                :key="item.logId"
                style="border-bottom: 1px solid var(--va-background-border);"
              >
                <td style="padding: 0.5rem 0.75rem; text-align: center;">
                  <input
                    type="checkbox"
                    :value="item.logId"
                    v-model="selectedIds"
                  />
                </td>
                <td style="padding: 0.5rem 0.75rem; font-weight: 600;">{{ item.channelName }}</td>
                <td style="padding: 0.5rem 0.75rem; font-weight: 700; color: var(--va-primary);">{{ item.recordCode }}</td>
                <td style="padding: 0.5rem 0.75rem;">
                  <va-badge :text="item.eventType" color="secondary" size="small" />
                </td>
                <td style="padding: 0.5rem 0.75rem; color: var(--va-danger); max-width: 250px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" :title="item.errorMessage">
                  {{ item.errorMessage || 'Unknown Error' }}
                </td>
                <td style="padding: 0.5rem 0.75rem; text-align: center;">
                  <va-badge :text="String(item.retryCount)" color="warning" size="small" />
                </td>
                <td style="padding: 0.5rem 0.75rem; color: var(--va-text-secondary); font-size: 0.75rem;">
                  {{ formatDate(item.createdAt) }}
                </td>
              </tr>
              <tr v-if="dlqItems.length === 0">
                <td colspan="7" style="text-align: center; padding: 2rem; color: var(--va-text-secondary);">
                  {{ $t('no_dlq_items') }}
                </td>
              </tr>
            </tbody>
          </table>
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
import { formatWithTimezone } from '~/composables/useTimezoneDate'
import AppModal from '~/components/common/AppModal.vue'

const props = defineProps<{
  modelValue: boolean
  channelId?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const dlqItems = ref<any[]>([])
const selectedIds = ref<string[]>([])
const loading = ref(false)
const retrying = ref(false)

const isAllSelected = computed(() => {
  return dlqItems.value.length > 0 && selectedIds.value.length === dlqItems.value.length
})

const toggleSelectAll = (e: any) => {
  if (e.target.checked) {
    selectedIds.value = dlqItems.value.map(i => i.logId)
  } else {
    selectedIds.value = []
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return formatWithTimezone(dateStr, 'YYYY-MM-DD HH:mm')
}

const fetchDlqItems = async () => {
  loading.value = true
  try {
    const url = props.channelId ? `/integration/dlq?channelId=${props.channelId}` : '/integration/dlq'
    const res = await useCustomFetch(url)
    if (res.data?.value) {
      dlqItems.value = res.data.value
      selectedIds.value = []
    }
  } catch (e: any) {
    console.error('Failed to fetch DLQ items', e)
  } finally {
    loading.value = false
  }
}

const retrySelected = async () => {
  if (selectedIds.value.length === 0) return
  retrying.value = true
  try {
    await useCustomFetch('/integration/dlq/retry', {
      method: 'POST',
      body: { logIds: selectedIds.value }
    })
    await fetchDlqItems()
  } catch (e: any) {
    console.error('Failed to retry DLQ items', e)
  } finally {
    retrying.value = false
  }
}

const retryAll = async () => {
  selectedIds.value = dlqItems.value.map(i => i.logId)
  await retrySelected()
}

watch(() => props.modelValue, (val) => {
  if (val) fetchDlqItems()
})
</script>
