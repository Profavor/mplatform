<template>
  <va-modal
    v-model="show"
    :title="$t('multilingual_sync')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🌐 {{ $t('multilingual_sync_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="scanData" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Status Banner -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center;">
            <div style="display: flex; gap: 0.5rem; align-items: center;">
              <span style="font-weight: 700; font-size: 0.9rem;">
                {{ $t('missing_locales_count') }}: {{ scanData.missingCount }}개 (총 {{ scanData.totalFields }}개 필드)
              </span>
            </div>
            <va-button
              v-if="scanData.missingCount > 0"
              color="success"
              size="small"
              :loading="syncing"
              @click="applySync"
            >
              {{ $t('sync_now') }}
            </va-button>
          </div>

          <!-- Missing Locales Table -->
          <div v-if="scanData.missingItems?.length > 0" style="max-height: 280px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.5rem 0.75rem;">필드 키</th>
                  <th style="padding: 0.5rem 0.75rem;">현재 정의</th>
                  <th style="padding: 0.5rem 0.75rem;">{{ $t('missing_langs') }}</th>
                  <th style="padding: 0.5rem 0.75rem;">{{ $t('suggested_translation') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="item in scanData.missingItems"
                  :key="item.fieldId"
                  style="border-bottom: 1px solid var(--va-background-border);"
                >
                  <td style="padding: 0.5rem 0.75rem; font-weight: 700; color: var(--va-primary);">{{ item.fieldKey }}</td>
                  <td style="padding: 0.5rem 0.75rem; color: var(--va-text-secondary);">
                    {{ JSON.stringify(item.currentNameMap) }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem;">
                    <va-badge
                      v-for="lang in item.missingLanguages"
                      :key="lang"
                      :text="lang"
                      color="danger"
                      size="small"
                      style="margin-right: 0.25rem;"
                    />
                  </td>
                  <td style="padding: 0.5rem 0.75rem; font-weight: 600; color: var(--va-success);">
                    {{ item.suggestedTermName }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-else style="text-align: center; padding: 2rem; color: var(--va-text-secondary);">
            <va-icon name="verified" size="large" color="success" style="margin-bottom: 0.5rem;" />
            <p>{{ $t('all_locales_complete') }}</p>
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

const scanData = ref<any>(null)
const loading = ref(false)
const syncing = ref(false)

const scanMissing = async () => {
  if (!props.domainId) return
  loading.value = true
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/multilingual/scan`)
    if (res.data?.value) {
      scanData.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to scan missing locales', e)
  } finally {
    loading.value = false
  }
}

const applySync = async () => {
  if (!props.domainId) return
  syncing.value = true
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/multilingual/sync`, {
      method: 'POST'
    })
    if (res.data?.value) {
      alert(res.data.value.message || '다국어 메타데이터 동기화가 완료되었습니다.')
      await scanMissing()
    }
  } catch (e: any) {
    console.error('Failed to sync multilingual metadata', e)
  } finally {
    syncing.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) scanMissing()
})
</script>
