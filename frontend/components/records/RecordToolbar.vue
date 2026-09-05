<template>
  <div class="record-toolbar-container">
    <!-- Left: Selected State / Records Sub Info -->
    <div class="record-toolbar-left">
      <va-icon name="list_alt" color="primary" size="1.1rem" />
      <span style="font-weight: 600; font-size: 0.88rem; color: var(--va-text-primary);">
        {{ selectedNode ? formatNodeName(selectedNode.name) : t('master_data_record_list') }}
      </span>
      <va-badge
        v-if="selectedRecordRows && selectedRecordRows.length > 0"
        :text="`${selectedRecordRows.length} ${t('items_selected', '선택됨')}`"
        color="info"
        size="small"
      />
    </div>

    <!-- Right Action Buttons directly above AG-Grid -->
    <div class="record-toolbar-actions">
      <template v-if="selectedNode && !selectedNode.isDomain">
        <va-button
          v-if="hasPermission('record:write') || hasPermission('workflow:request')"
          size="small"
          color="primary"
          @click="$emit('create')"
        >
          <va-icon name="add" class="mr-1" /> {{ hasCreateWorkflow ? t('create_request') : t('create_record') }}
        </va-button>
        <va-button
          v-if="hasPermission('record:write') || hasPermission('workflow:request')"
          size="small"
          color="success"
          outline
          @click="$emit('uploadExcel')"
        >
          <va-icon name="upload" class="mr-1" /> {{ t('bulk_upload') }}
        </va-button>
      </template>
      <template v-else-if="selectedNode && selectedNode.isDomain">
        <va-button
          v-if="hasPermission('record:delete') || hasPermission('domain:write') || hasPermission('record:*') || hasPermission('domain:*')"
          size="small"
          color="danger"
          outline
          :title="t('reset_domain_records_btn')"
          @click="$emit('resetDomainRecords')"
        >
          <va-icon name="delete_sweep" class="mr-1" /> {{ t('reset_domain_records_btn') }}
        </va-button>
      </template>

      <va-button
        size="small"
        color="info"
        outline
        :disabled="(selectedRecordRows?.length || 0) !== 1"
        @click="$emit('openLineage')"
      >
        <va-icon name="account_tree" class="mr-1" /> {{ t('data_lineage') }}
      </va-button>

      <va-button
        size="small"
        color="warning"
        outline
        :disabled="(selectedRecordRows?.length || 0) < 2"
        :title="(selectedRecordRows?.length || 0) < 2 ? t('compare_min_selection_hint') : t('compare_records')"
        @click="$emit('openCompare')"
      >
        <va-icon name="scale" class="mr-1" /> {{ t('compare_records') }} ({{ selectedRecordRows?.length || 0 }})
      </va-button>

      <va-button
        size="small"
        color="secondary"
        outline
        :disabled="!selectedRecordRows || selectedRecordRows.length === 0"
        @click="$emit('openBulkReclassify')"
      >
        <va-icon name="drive_file_move" class="mr-1" /> {{ t('bulk_reclassify') }} ({{ selectedRecordRows?.length || 0 }})
      </va-button>

      <va-button
        size="small"
        color="warning"
        outline
        @click="$emit('openExport')"
      >
        <va-icon name="cloud_download" class="mr-1" /> {{ t('async_export') }}
      </va-button>

      <va-button
        size="small"
        color="warning"
        outline
        @click="$emit('openCdcStream')"
      >
        <va-icon name="sensors" class="mr-1" /> {{ t('cdc_stream') }}
      </va-button>

      <va-divider vertical class="mx-1" />

      <va-button preset="plain" color="secondary" size="small" icon="filter_alt_off" @click="$emit('resetFilters')">
        {{ t('reset_filters') }}
      </va-button>

      <va-button preset="plain" color="secondary" size="small" icon="refresh" @click="$emit('refresh')">
        {{ t('refresh') }}
      </va-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { usePermission } from '~/composables/usePermission'
import { formatMultilingual } from '~/composables/useMultilingual'

const props = defineProps<{
  selectedNode?: any
  selectedRecordRows?: any[]
  hasCreateWorkflow?: boolean
}>()

defineEmits<{
  (e: 'create'): void
  (e: 'uploadExcel'): void
  (e: 'resetDomainRecords'): void
  (e: 'openLineage'): void
  (e: 'openCompare'): void
  (e: 'openBulkReclassify'): void
  (e: 'downloadTemplate'): void
  (e: 'openExport'): void
  (e: 'openCdcStream'): void
  (e: 'resetFilters'): void
  (e: 'refresh'): void
}>()

const { t } = useI18n()
const { hasPermission } = usePermission()

const formatNodeName = (nameObj: any) => {
  if (!nameObj) return ''
  return formatMultilingual(nameObj)
}
</script>

<style scoped>
.record-toolbar-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0.85rem;
  margin-bottom: 0;
  background: var(--va-background-element, #f4f6f9);
  border: 1px solid var(--va-background-border);
  border-bottom: none;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
  gap: 0.5rem;
  flex-wrap: wrap;
  box-sizing: border-box;
}

.record-toolbar-left {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
  flex-shrink: 0;
}

.record-toolbar-actions {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  flex-wrap: nowrap;
  overflow-x: auto;
  max-width: 100%;
  scrollbar-width: thin;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 2px;
}

.record-toolbar-actions :deep(.va-button),
.record-toolbar-actions .va-button {
  flex-shrink: 0;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .record-toolbar-container {
    flex-direction: column;
    align-items: stretch;
    gap: 0.5rem;
    padding: 0.5rem 0.6rem;
  }
  .record-toolbar-actions {
    width: 100%;
  }
}
</style>
