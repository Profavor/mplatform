<template>
  <va-modal
    v-model="show"
    :title="$t('hash_chain_ledger')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        ⛓️ {{ $t('hash_chain_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="ledgerData" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Status Banner -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center;">
            <div style="display: flex; gap: 0.5rem; align-items: center;">
              <span style="font-weight: 700; font-size: 0.9rem;">
                {{ ledgerData.summary }}
              </span>
            </div>
            <div style="display: flex; gap: 0.4rem;">
              <va-badge
                :text="ledgerData.isChainIntact ? $t('chain_status_intact') : $t('chain_status_corrupted')"
                :color="ledgerData.isChainIntact ? 'success' : 'danger'"
                size="small"
              />
              <va-badge :text="`블록: ${ledgerData.totalBlocks}개`" color="primary" size="small" />
            </div>
          </div>

          <!-- Blocks Table -->
          <div style="max-height: 280px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.5rem 0.75rem; width: 60px;">#</th>
                  <th style="padding: 0.5rem 0.75rem; width: 80px;">액션</th>
                  <th style="padding: 0.5rem 0.75rem; width: 80px;">수행자</th>
                  <th style="padding: 0.5rem 0.75rem;">블록 해시 (SHA-256)</th>
                  <th style="padding: 0.5rem 0.75rem; width: 80px; text-align: center;">검증</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="b in ledgerData.blocks"
                  :key="b.blockIndex"
                  style="border-bottom: 1px solid var(--va-background-border);"
                >
                  <td style="padding: 0.5rem 0.75rem; font-weight: 700;">{{ b.blockIndex }}</td>
                  <td style="padding: 0.5rem 0.75rem;">
                    <va-badge :text="b.actionType" color="secondary" size="small" />
                  </td>
                  <td style="padding: 0.5rem 0.75rem; color: var(--va-text-secondary);">{{ b.actor }}</td>
                  <td style="padding: 0.5rem 0.75rem; font-family: monospace; font-size: 0.75rem; max-width: 250px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" :title="b.blockHash">
                    {{ b.blockHash }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: center;">
                    <va-badge :text="b.valid ? 'VALID' : 'INVALID'" :color="b.valid ? 'success' : 'danger'" size="small" />
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

const ledgerData = ref<any>(null)
const loading = ref(false)

const verifyLedger = async () => {
  if (!props.recordId) return
  loading.value = true
  try {
    const res = await useCustomFetch(`/records/${props.recordId}/ledger/verify`)
    if (res.data?.value) {
      ledgerData.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to verify ledger', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) verifyLedger()
})
</script>
