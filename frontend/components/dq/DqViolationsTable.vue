<template>
  <va-card class="violations-table-card">
    <va-card-title class="table-card-title">
      <div class="table-title-group">
        <div class="title-icon-box">
          <va-icon name="report_problem" color="danger" size="medium" />
        </div>
        <div>
          <div class="table-main-title">{{ t('dq_dashboard.violation_table_title') }}</div>
          <div class="table-sub-title">{{ t('dq_dashboard.violation_table_sub') }}</div>
        </div>
      </div>

      <!-- Filters Bar -->
      <div class="filters-toolbar">
        <va-select
          :model-value="filterSeverity"
          @update:model-value="val => emit('update:filterSeverity', val)"
          :options="severityOptions"
          text-by="label"
          value-by="value"
          :label="t('dq_dashboard.severity')"
          class="filter-select"
        />
        <va-select
          :model-value="filterFieldKey"
          @update:model-value="val => emit('update:filterFieldKey', val)"
          :options="availableFieldFilterOptions"
          text-by="label"
          value-by="value"
          :label="t('dq_dashboard.field')"
          class="filter-select-wide"
        />
        <va-button
          v-if="filterSeverity || filterFieldKey"
          size="small"
          preset="secondary"
          icon="clear"
          @click="emit('reset-filters')"
        >
          {{ t('reset') }}
        </va-button>
      </div>
    </va-card-title>

    <va-card-content class="table-content">
      <div v-if="loadingViolations" class="table-loading">
        <va-progress-circle indeterminate size="2rem" />
        <span>{{ t('dq_dashboard.loading_violations') }}</span>
      </div>

      <div v-else-if="!violationList || violationList.length === 0" class="empty-state">
        <span class="empty-icon">🎉</span>
        <p>{{ t('dq_dashboard.no_violations_found') }}</p>
      </div>

      <template v-else>
        <div class="table-responsive">
          <table class="custom-dq-table">
            <thead>
              <tr>
                <th>{{ t('dq_dashboard.record_id') }}</th>
                <th>{{ t('classification_node') }}</th>
                <th>{{ t('dq_dashboard.violated_field') }}</th>
                <th>{{ t('dq_dashboard.severity') }}</th>
                <th>{{ t('dq_dashboard.rule_name') }}</th>
                <th>{{ t('dq_dashboard.violation_message') }}</th>
                <th>{{ t('createdAt') }}</th>
                <th class="text-center">{{ t('action') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="v in violationList" :key="v.id" class="table-row">
                <td>
                  <div class="record-id-cell">
                    <va-icon name="folder" size="small" color="primary" />
                    <span>{{ getRecordIdentifierDisplay(v) }}</span>
                  </div>
                </td>
                <td>
                  <span class="node-cell">{{ getNodeDisplayName(v.nodeName) }}</span>
                </td>
                <td>
                  <va-chip size="small" preset="outline" class="field-chip">
                    {{ getFieldDisplayName(v.fieldKey) }}
                  </va-chip>
                </td>
                <td>
                  <va-badge
                    :text="codeStore.getCodeName('DQ_SEVERITY', v.severity, v.severity)"
                    :color="v.severity === 'ERROR' || v.severity === 'CRITICAL' || v.severity === 'HIGH' ? 'danger' : 'warning'"
                    class="severity-badge-table"
                  />
                </td>
                <td>
                  <span class="violation-msg">{{ getViolationMessage(v.message) }}</span>
                </td>
                <td>
                  <code class="actual-value-code">
                    {{ v.actualValue || t('dq_dashboard.empty_value') }}
                  </code>
                </td>
                <td>
                  <span class="date-cell">{{ formatDateTime(v.checkedAt) }}</span>
                </td>
                <td class="text-center">
                  <va-button
                    size="small"
                    color="primary"
                    icon="launch"
                    class="goto-btn"
                    @click="onGoToRecord(v.recordId)"
                  >
                    {{ t('dq_dashboard.details') }}
                  </va-button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Custom Pagination Toolbar -->
        <div class="pagination-toolbar">
          <span class="pagination-info">
            {{ t('dq_dashboard.pagination_summary', {
              total: totalViolationsCount.toLocaleString(),
              start: (violationPage * violationSize) + 1,
              end: Math.min((violationPage + 1) * violationSize, totalViolationsCount)
            }) }}
          </span>

          <div class="pagination-controls">
            <va-button
              size="small"
              preset="secondary"
              icon="chevron_left"
              :disabled="violationPage === 0"
              @click="emit('update:violationPage', violationPage - 1)"
            />
            <span class="page-badge">
              {{ violationPage + 1 }} / {{ totalViolationPages || 1 }}
            </span>
            <va-button
              size="small"
              preset="secondary"
              icon="chevron_right"
              :disabled="violationPage + 1 >= totalViolationPages"
              @click="emit('update:violationPage', violationPage + 1)"
            />
          </div>
        </div>
      </template>
    </va-card-content>
  </va-card>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { formatWithTimezone } from '~/composables/useTimezoneDate'
import { formatRecordCode } from '~/utils/formatters'
import { useCodeStore } from '~/stores/useCodeStore'

const { t, locale } = useI18n()
const codeStore = useCodeStore()
codeStore.loadGroup('DQ_SEVERITY').catch(console.error)

const props = defineProps<{
  violationList: any[]
  loadingViolations: boolean
  filterSeverity: string
  filterFieldKey: string
  severityOptions: any[]
  availableFieldFilterOptions: any[]
  violationPage: number
  violationSize: number
  totalViolationsCount: number
  totalViolationPages: number
  fieldMap?: Record<string, any>
}>()

const emit = defineEmits<{
  (e: 'update:filterSeverity', val: string): void
  (e: 'update:filterFieldKey', val: string): void
  (e: 'update:violationPage', val: number): void
  (e: 'reset-filters'): void
  (e: 'go-to-record', recordId: string): void
}>()

const onGoToRecord = (recordId: string) => {
  emit('go-to-record', recordId)
}

const getFieldDisplayName = (fieldKey: string) => {
  if (!fieldKey) return '-'
  if (props.fieldMap && props.fieldMap[fieldKey]) {
    const f = props.fieldMap[fieldKey]
    if (f.name) {
      if (typeof f.name === 'string') return f.name
      return f.name[locale.value] || f.name.ko || f.name.en || fieldKey
    }
  }
  return fieldKey
}

const getNodeDisplayName = (nodeNameObj?: any) => {
  if (!nodeNameObj) return '-'
  if (typeof nodeNameObj === 'string') return nodeNameObj
  return nodeNameObj[locale.value] || nodeNameObj.ko || nodeNameObj.en || '-'
}

const getViolationMessage = (msgMap?: any) => {
  if (!msgMap) return '품질 검증 규칙 위반'
  if (typeof msgMap === 'string') return msgMap
  return msgMap[locale.value] || msgMap.ko || msgMap.en || Object.values(msgMap)[0] || '품질 검증 규칙 위반'
}

const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return '-'
  try {
    return formatWithTimezone(dateStr, 'YYYY-MM-DD HH:mm')
  } catch (e) {
    return dateStr
  }
}

const getRecordIdentifierDisplay = (v?: any) => {
  if (!v) return '-'
  if (v.recordIdentifier && typeof v.recordIdentifier === 'string') {
    const stripped = v.recordIdentifier.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ').trim()
    if (stripped) return stripped
  }
  return formatRecordCode(v.recordId)
}

defineExpose({
  onGoToRecord
})
</script>

<style scoped>
.violations-table-card {
  border-radius: 12px;
  border: 1px solid var(--va-background-border);
  overflow: hidden;
  background: var(--va-background-primary);
}

.table-card-title {
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--va-background-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.table-title-group {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.title-icon-box {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(239, 68, 68, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.table-main-title {
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--va-text-primary);
  font-family: 'Pretendard', 'Inter', sans-serif;
}

.table-sub-title {
  font-size: 0.8rem;
  color: var(--va-text-secondary);
}

.filters-toolbar {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.filter-select {
  width: 140px;
}

.filter-select-wide {
  width: 180px;
}

.table-content {
  padding: 0;
}

.table-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  gap: 0.75rem;
  color: var(--va-text-secondary);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  color: var(--va-text-secondary);
  gap: 0.5rem;
}

.empty-icon {
  font-size: 2.5rem;
}

.table-responsive {
  overflow-x: auto;
  width: 100%;
}

.custom-dq-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.85rem;
  text-align: left;
}

.custom-dq-table th {
  background: var(--va-background-element);
  color: var(--va-text-secondary);
  font-weight: 600;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid var(--va-background-border);
  white-space: nowrap;
}

.custom-dq-table td {
  padding: 0.75rem 1rem;
  border-bottom: 1px solid var(--va-background-border);
  color: var(--va-text-primary);
  vertical-align: middle;
}

.table-row:hover {
  background: var(--va-background-element);
}

.record-id-cell {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-family: monospace;
  font-weight: 700;
}

.node-cell {
  font-weight: 600;
}

.field-chip {
  font-family: monospace;
  font-size: 0.75rem;
}

.severity-badge-table {
  font-weight: 700;
  font-size: 0.75rem;
}

.violation-msg {
  font-weight: 500;
}

.actual-value-code {
  background: rgba(0,0,0,0.06);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.8rem;
  color: var(--va-danger);
}

.date-cell {
  color: var(--va-text-secondary);
  font-size: 0.8rem;
  white-space: nowrap;
}

.goto-btn {
  font-weight: 600;
  border-radius: 6px;
}

.pagination-toolbar {
  padding: 0.75rem 1.25rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--va-background-primary);
  border-top: 1px solid var(--va-background-border);
  flex-wrap: wrap;
  gap: 0.5rem;
}

.pagination-info {
  font-size: 0.82rem;
  color: var(--va-text-secondary);
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.page-badge {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--va-text-primary);
  padding: 2px 8px;
  background: var(--va-background-element);
  border-radius: 6px;
}
</style>
