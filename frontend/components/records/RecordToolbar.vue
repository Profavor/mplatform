<template>
  <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.5rem 0.85rem; margin-bottom: 0; background: var(--va-background-element, #f4f6f9); border: 1px solid var(--va-background-border); border-bottom: none; border-top-left-radius: 8px; border-top-right-radius: 8px;">
    <!-- Left Title & Selected Node Info -->
    <div style="display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap;">
      <va-icon name="table_chart" color="primary" size="1.1rem" />
      <span style="font-weight: 700; font-size: 0.95rem; color: var(--va-text-primary);">
        {{ selectedNode ? formatNodeName(selectedNode.name) : t('master_data_record_list') }}
      </span>
      <va-chip v-if="selectedNode" size="small" color="primary" style="font-weight: 600;">
        {{ selectedNode.isDomain ? t('domain') : t('node') }}
      </va-chip>
      <slot name="search-chips" />
    </div>

    <!-- Right Action Buttons -->
    <div style="display: flex; align-items: center; gap: 0.4rem; flex-wrap: wrap;">
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
        color="success"
        outline
        @click="$emit('downloadTemplate')"
      >
        <va-icon name="download" class="mr-1" /> {{ t('download_template') }}
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
        color="primary"
        outline
        @click="$emit('openAutonomousCleansing')"
      >
        <va-icon name="auto_fix_high" class="mr-1" /> {{ t('autonomous_cleansing') }}
      </va-button>

      <va-button
        size="small"
        color="info"
        outline
        @click="$emit('openAiStructurizer')"
      >
        <va-icon name="psychology" class="mr-1" /> {{ t('ai_structurizer') }}
      </va-button>

      <va-button
        size="small"
        color="secondary"
        outline
        @click="$emit('openBusinessRuleBuilder')"
      >
        <va-icon name="rule" class="mr-1" /> {{ t('business_rule_builder') }}
      </va-button>

      <va-button
        size="small"
        color="warning"
        outline
        @click="$emit('openCdcStream')"
      >
        <va-icon name="sensors" class="mr-1" /> {{ t('cdc_stream') }}
      </va-button>

      <va-button preset="plain" color="secondary" size="small" icon="restart_alt" @click="$emit('resetFilters')">
        {{ t('reset') }}
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
  (e: 'openLineage'): void
  (e: 'openCompare'): void
  (e: 'openBulkReclassify'): void
  (e: 'downloadTemplate'): void
  (e: 'openExport'): void
  (e: 'openAutonomousCleansing'): void
  (e: 'openAiStructurizer'): void
  (e: 'openBusinessRuleBuilder'): void
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
