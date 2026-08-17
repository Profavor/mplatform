<template>
  <AppModal
    v-model="visible"
    :title="t('unmerge_preview_title')"
    icon="call_split"
    size="large"
    hide-default-actions
    class="unmerge-preview-modal"
  >
    <div style="display: flex; flex-direction: column; gap: 1rem; padding: 0.5rem 0;">
      <va-alert color="warning" outline class="mb-2">
        <div style="display: flex; align-items: center; gap: 0.5rem;">
          <va-icon name="warning" />
          <span>{{ t('unmerge_warning_desc') }}</span>
        </div>
      </va-alert>

      <!-- Golden Master Record Summary -->
      <div style="background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 1rem;">
        <div style="font-weight: 700; font-size: 0.85rem; color: var(--va-text-secondary); margin-bottom: 0.5rem;">
          👑 {{ t('current_golden_record') }}
        </div>
        <div style="display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
          <span style="font-weight: 800; font-size: 1rem; color: var(--va-primary);">
            {{ masterRecord?.name || t('unnamed_record') }}
          </span>
          <va-chip size="small" color="primary" square outline style="font-weight: 700; font-family: monospace;">
            {{ formatRecordCode(masterRecord?.id) }}
          </va-chip>
          <va-chip size="small" color="warning" square style="font-weight: 700;">
            {{ masterRecord?.status || 'MERGED' }}
          </va-chip>
        </div>
      </div>

      <!-- Target Restored Records List -->
      <div style="font-weight: 700; font-size: 0.9rem; margin-top: 0.25rem;">
        🔄 {{ t('restoring_records_count', { count: sourceRecords.length }) }}
      </div>

      <div style="overflow-y: auto; flex: 1; display: flex; flex-direction: column; gap: 0.75rem; max-height: 360px;">
        <div
          v-for="(rec, index) in sourceRecords"
          :key="rec.id || index"
          style="border: 1px solid var(--va-background-border); border-radius: 8px; padding: 0.85rem; background: var(--va-background-primary);"
        >
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
            <div style="display: flex; align-items: center; gap: 0.5rem;">
              <span style="font-weight: 700; font-size: 0.95rem;">
                {{ rec.name || `${t('source_record')} #${index + 1}` }}
              </span>
              <va-chip size="small" color="info" square outline style="font-weight: 700; font-family: monospace;">
                {{ formatRecordCode(rec.id) }}
              </va-chip>
            </div>
            <va-chip v-if="rec.sourceSystem" size="small" color="secondary" square>
              {{ rec.sourceSystem }}
            </va-chip>
          </div>

          <!-- Restored Data Attributes Preview -->
          <div v-if="rec.data" style="display: flex; flex-wrap: wrap; gap: 0.5rem; margin-top: 0.35rem;">
            <div
              v-for="(val, key) in rec.data"
              :key="key"
              style="font-size: 0.8rem; background: var(--va-background-element); padding: 0.2rem 0.5rem; border-radius: 4px; border: 1px solid var(--va-background-border);"
            >
              <span style="color: var(--va-text-secondary); font-weight: 600;">{{ key }}: </span>
              <span style="font-weight: 700;">{{ val }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer Actions -->
    <template #footer>
      <div style="display: flex; justify-content: flex-end; gap: 0.5rem; width: 100%;">
        <va-button preset="secondary" color="secondary" @click="visible = false">
          {{ t('btn_cancel') }}
        </va-button>
        <va-button color="warning" icon="call_split" @click="handleConfirm">
          {{ t('unmerge_confirm_btn') }}
        </va-button>
      </div>
    </template>
  </AppModal>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { formatRecordCode } from '~/utils/formatters'
import AppModal from '~/components/common/AppModal.vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  masterRecord: {
    type: Object,
    default: () => ({})
  },
  sourceRecords: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'confirm'])

let t = (k, params) => {
  if (params?.count !== undefined) return `${params.count}`
  return k
}
try {
  const i18n = useI18n()
  if (i18n?.t) t = i18n.t
} catch {
  // test environment fallback
}

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const handleConfirm = () => {
  emit('confirm', props.masterRecord)
  visible.value = false
}
</script>

