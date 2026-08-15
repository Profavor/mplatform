<template>
  <va-modal
    v-model="show"
    :title="$t('data_retention')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🛡️ {{ $t('data_retention_desc') }}
      </va-alert>

      <!-- Scan Configuration -->
      <div style="display: flex; gap: 1rem; align-items: center; padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element);">
        <div style="display: flex; gap: 0.5rem; align-items: center;">
          <span style="font-size: 0.85rem; font-weight: 600;">{{ $t('retention_years') }}:</span>
          <va-select
            v-model="years"
            :options="[1, 3, 5, 10]"
            style="width: 100px;"
          />
        </div>
        <va-button
          color="primary"
          size="small"
          :loading="loading"
          @click="scanExpired"
        >
          {{ $t('scan_expired') }}
        </va-button>
      </div>

      <va-inner-loading :loading="loading">
        <div v-if="scanData" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Status Banner -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center;">
            <div style="display: flex; gap: 0.5rem; align-items: center;">
              <span style="font-weight: 700; font-size: 0.9rem;">
                {{ scanData.summary }}
              </span>
            </div>
            <va-badge :text="`${scanData.expiredCount}건`" :color="scanData.expiredCount > 0 ? 'warning' : 'success'" size="small" />
          </div>

          <!-- Expired Records List -->
          <div v-if="scanData.expiredCount > 0" style="display: flex; flex-direction: column; gap: 0.75rem;">
            <div style="display: flex; gap: 0.4rem; align-items: center; flex-wrap: wrap;">
              <span style="font-size: 0.82rem; font-weight: 600;">만료 대상:</span>
              <va-badge
                v-for="code in scanData.expiredRecordCodes"
                :key="code"
                :text="code"
                color="secondary"
                size="small"
              />
            </div>

            <!-- Purge Execution Actions -->
            <div style="display: flex; gap: 1rem; align-items: center; margin-top: 0.5rem;">
              <va-select
                v-model="purgeType"
                :options="[
                  { text: $t('purge_anonymize'), value: 'ANONYMIZE' },
                  { text: $t('purge_hard_delete'), value: 'HARD_DELETE' }
                ]"
                value-by="value"
                text-by="text"
                style="flex: 1;"
              />
              <va-button
                color="danger"
                :loading="purging"
                @click="executePurge"
              >
                {{ $t('execute_purge') }}
              </va-button>
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

const years = ref(3)
const purgeType = ref('ANONYMIZE')
const scanData = ref<any>(null)
const loading = ref(false)
const purging = ref(false)

const scanExpired = async () => {
  if (!props.domainId) return
  loading.value = true
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/retention/scan?years=${years.value}`)
    if (res.data?.value) {
      scanData.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to scan expired records', e)
  } finally {
    loading.value = false
  }
}

const executePurge = async () => {
  if (!props.domainId) return
  purging.value = true
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/retention/purge`, {
      method: 'POST',
      body: {
        retentionYears: years.value,
        purgeType: purgeType.value
      }
    })
    if (res.data?.value) {
      alert(res.data.value.summary || '데이터 파기가 완료되었습니다.')
      await scanExpired()
    }
  } catch (e: any) {
    console.error('Failed to purge records', e)
  } finally {
    purging.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) scanExpired()
})
</script>
