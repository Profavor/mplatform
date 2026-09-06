<template>
  <AppModal
    v-model="show"
    :title="$t('schema_compatibility')"
    icon="fact_check"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1rem; padding: 0.25rem 0;">
      <!-- Tab Bar -->
      <va-tabs v-model="activeTab" style="margin-bottom: 0.5rem;">
        <template #tabs>
          <va-tab name="field">
            {{ $t('field_impact.tab_field_sim') }}
          </va-tab>
          <va-tab name="ddl">
            {{ $t('field_impact.tab_ddl_sim') }}
          </va-tab>
        </template>
      </va-tabs>

      <!-- TAB 1: Field Impact Simulator -->
      <div v-if="activeTab === 'field'" style="display: flex; flex-direction: column; gap: 1rem;">
        <va-alert color="info" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
          🎯 {{ $t('schema_compatibility_desc') }}
        </va-alert>

        <!-- Simulation Parameter Controls -->
        <div style="background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 1rem; display: flex; flex-direction: column; gap: 0.75rem;">
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
            <!-- Field Select -->
            <div>
              <label style="display: block; font-size: 0.8rem; font-weight: 600; margin-bottom: 0.25rem; color: var(--va-text-primary);">
                {{ $t('field_impact.select_field') }} *
              </label>
              <va-select
                v-model="selectedFieldId"
                :options="fieldOptions"
                value-by="value"
                text-by="label"
                :placeholder="$t('field_impact.select_field_placeholder')"
                :loading="loadingFields"
                style="width: 100%;"
              />
            </div>

            <!-- Change Type Select -->
            <div>
              <label style="display: block; font-size: 0.8rem; font-weight: 600; margin-bottom: 0.25rem; color: var(--va-text-primary);">
                {{ $t('field_impact.change_type') }} *
              </label>
              <va-select
                v-model="changeType"
                :options="changeTypeOptions"
                value-by="value"
                text-by="label"
                style="width: 100%;"
              />
            </div>
          </div>

          <!-- Dynamic parameter inputs depending on changeType -->
          <div v-if="changeType === 'TYPE_CHANGE'" style="display: flex; gap: 1rem;">
            <div style="flex: 1;">
              <label style="display: block; font-size: 0.8rem; font-weight: 600; margin-bottom: 0.25rem; color: var(--va-text-primary);">
                {{ $t('field_impact.new_data_type') }}
              </label>
              <va-select
                v-model="newType"
                :options="typeOptions"
                style="width: 100%;"
              />
            </div>
          </div>

          <div v-if="changeType === 'RENAME'" style="display: flex; gap: 1rem;">
            <div style="flex: 1;">
              <label style="display: block; font-size: 0.8rem; font-weight: 600; margin-bottom: 0.25rem; color: var(--va-text-primary);">
                {{ $t('field_impact.new_field_key') }}
              </label>
              <va-input
                v-model="newKey"
                placeholder="new_field_key"
                style="width: 100%;"
              />
            </div>
          </div>

          <div style="display: flex; justify-content: flex-end; margin-top: 0.25rem;">
            <va-button
              size="small"
              color="primary"
              :disabled="!selectedFieldId"
              :loading="simulating"
              icon="play_arrow"
              @click="runFieldSimulation"
            >
              {{ $t('field_impact.btn_simulate') }}
            </va-button>
          </div>
        </div>

        <!-- Field Simulation Result Report -->
        <va-inner-loading :loading="simulating">
          <div v-if="fieldReport" style="display: flex; flex-direction: column; gap: 1rem;">
            <!-- Overall Status Banner -->
            <div
              style="padding: 0.85rem 1.25rem; border-radius: 8px; border: 1px solid var(--va-background-border); display: flex; justify-content: space-between; align-items: center;"
              :style="{
                background: fieldReport.compatibilityStatus === 'BREAKING' ? 'rgba(235, 59, 90, 0.12)' : (fieldReport.compatibilityStatus === 'WARNING' ? 'rgba(254, 211, 48, 0.15)' : 'rgba(32, 191, 107, 0.12)'),
                borderColor: fieldReport.compatibilityStatus === 'BREAKING' ? '#eb3b5a' : (fieldReport.compatibilityStatus === 'WARNING' ? '#fed330' : '#20bf6b')
              }"
            >
              <div style="display: flex; flex-direction: column; gap: 0.2rem;">
                <span style="font-weight: 700; font-size: 0.95rem; color: var(--va-text-primary);">
                  {{ fieldReport.summary }}
                </span>
                <span style="font-size: 0.78rem; color: var(--va-text-secondary);">
                  {{ fieldReport.fieldKey }} ({{ getFieldDisplayName(fieldReport.fieldKey) }})
                </span>
              </div>
              <div style="display: flex; align-items: center; gap: 0.5rem;">
                <va-badge
                  :text="fieldReport.compatibilityStatus === 'BREAKING' ? $t('field_impact.status_breaking') : (fieldReport.compatibilityStatus === 'WARNING' ? $t('field_impact.status_warning') : $t('field_impact.status_safe'))"
                  :color="fieldReport.compatibilityStatus === 'BREAKING' ? 'danger' : (fieldReport.compatibilityStatus === 'WARNING' ? 'warning' : 'success')"
                />
              </div>
            </div>

            <!-- 4 Major Impact KPIs Grid -->
            <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 0.75rem;">
              <div style="background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 0.75rem; text-align: center;">
                <div style="font-size: 0.75rem; color: var(--va-text-secondary); margin-bottom: 0.25rem;">{{ $t('field_impact.kpi_total_records') }}</div>
                <div style="font-size: 1.15rem; font-weight: 700;">{{ (fieldReport.totalDomainRecords || 0).toLocaleString() }}</div>
              </div>
              <div style="background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 0.75rem; text-align: center;">
                <div style="font-size: 0.75rem; color: var(--va-text-secondary); margin-bottom: 0.25rem;">{{ $t('field_impact.kpi_affected_records') }}</div>
                <div style="font-size: 1.15rem; font-weight: 700;" :style="{ color: (fieldReport.affectedRecordCount || 0) > 0 ? '#eb3b5a' : 'inherit' }">
                  {{ (fieldReport.affectedRecordCount || 0).toLocaleString() }}
                  <span style="font-size: 0.75rem; font-weight: normal; color: var(--va-text-secondary);">({{ fieldReport.affectedRecordPercentage || 0 }}%)</span>
                </div>
              </div>
              <div style="background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 0.75rem; text-align: center;">
                <div style="font-size: 0.75rem; color: var(--va-text-secondary); margin-bottom: 0.25rem;">{{ $t('field_impact.kpi_affected_dq_rules') }}</div>
                <div style="font-size: 1.15rem; font-weight: 700;" :style="{ color: (fieldReport.affectedDqRules?.length || 0) > 0 ? '#fa8231' : 'inherit' }">
                  {{ fieldReport.affectedDqRules?.length || 0 }}
                </div>
              </div>
              <div style="background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 0.75rem; text-align: center;">
                <div style="font-size: 0.75rem; color: var(--va-text-secondary); margin-bottom: 0.25rem;">{{ $t('field_impact.kpi_affected_channels') }}</div>
                <div style="font-size: 1.15rem; font-weight: 700;" :style="{ color: (fieldReport.affectedChannels?.length || 0) > 0 ? '#fa8231' : 'inherit' }">
                  {{ fieldReport.affectedChannels?.length || 0 }}
                </div>
              </div>
            </div>

            <!-- Detailed Risk Table -->
            <div style="border: 1px solid var(--va-background-border); border-radius: 8px; overflow: hidden;">
              <div style="padding: 0.6rem 0.85rem; background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border); font-weight: 700; font-size: 0.825rem;">
                📌 {{ $t('field_impact.impact_details_title') }}
              </div>
              <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
                <thead>
                  <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                    <th style="padding: 0.5rem 0.75rem;">{{ $t('field_impact.col_target_field') }}</th>
                    <th style="padding: 0.5rem 0.75rem;">{{ $t('field_impact.col_change_type') }}</th>
                    <th style="padding: 0.5rem 0.75rem; width: 80px; text-align: center;">{{ $t('field_impact.col_risk_level') }}</th>
                    <th style="padding: 0.5rem 0.75rem;">{{ $t('field_impact.col_impact_desc') }}</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="(r, idx) in fieldReport.risks"
                    :key="idx"
                    style="border-bottom: 1px solid var(--va-background-border);"
                  >
                    <td style="padding: 0.5rem 0.75rem; font-weight: 700;">{{ r.fieldKey }}</td>
                    <td style="padding: 0.5rem 0.75rem; font-family: monospace; font-size: 0.75rem;">{{ r.changeType }}</td>
                    <td style="padding: 0.5rem 0.75rem; text-align: center;">
                      <va-badge
                        :text="r.riskLevel"
                        :color="r.riskLevel === 'CRITICAL' ? 'danger' : (r.riskLevel === 'WARNING' ? 'warning' : 'info')"
                        size="small"
                      />
                    </td>
                    <td style="padding: 0.5rem 0.75rem; color: var(--va-text-secondary); font-size: 0.78rem;">
                      <div>{{ r.impactDescription }}</div>
                      <div style="color: var(--va-primary); margin-top: 0.2rem;">💡 {{ r.mitigationGuide }}</div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Mitigation Recommendation Guides -->
            <div v-if="fieldReport.mitigationGuides && fieldReport.mitigationGuides.length > 0" style="background: rgba(43, 114, 186, 0.05); border: 1px dashed var(--va-primary); border-radius: 8px; padding: 0.85rem;">
              <div style="font-weight: 700; font-size: 0.825rem; color: var(--va-primary); margin-bottom: 0.5rem;">
                🛡️ {{ $t('field_impact.mitigation_guides_title') }}
              </div>
              <ul style="margin: 0; padding-left: 1.25rem; display: flex; flex-direction: column; gap: 0.35rem; font-size: 0.8rem; color: var(--va-text-primary);">
                <li v-for="(guide, gIdx) in fieldReport.mitigationGuides" :key="gIdx">
                  {{ guide }}
                </li>
              </ul>
            </div>

            <!-- Downstream Dependencies (DQ & Channels) -->
            <div v-if="(fieldReport.affectedDqRules && fieldReport.affectedDqRules.length > 0) || (fieldReport.affectedChannels && fieldReport.affectedChannels.length > 0)" style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem;">
              <!-- Affected DQ Rules -->
              <div style="background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 0.75rem;">
                <div style="font-weight: 700; font-size: 0.8rem; margin-bottom: 0.35rem;">
                  🔍 {{ $t('field_impact.kpi_affected_dq_rules') }} ({{ fieldReport.affectedDqRules?.length || 0 }})
                </div>
                <div v-if="fieldReport.affectedDqRules?.length > 0" style="display: flex; flex-direction: column; gap: 0.25rem;">
                  <va-chip
                    v-for="(rule, rIdx) in fieldReport.affectedDqRules"
                    :key="rIdx"
                    size="small"
                    color="warning"
                    outline
                  >
                    {{ rule }}
                  </va-chip>
                </div>
                <div v-else style="font-size: 0.75rem; color: var(--va-text-secondary);">
                  {{ $t('field_impact.no_downstream_deps') }}
                </div>
              </div>

              <!-- Affected Channels -->
              <div style="background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 0.75rem;">
                <div style="font-weight: 700; font-size: 0.8rem; margin-bottom: 0.35rem;">
                  🔌 {{ $t('field_impact.kpi_affected_channels') }} ({{ fieldReport.affectedChannels?.length || 0 }})
                </div>
                <div v-if="fieldReport.affectedChannels?.length > 0" style="display: flex; flex-direction: column; gap: 0.25rem;">
                  <va-chip
                    v-for="(ch, cIdx) in fieldReport.affectedChannels"
                    :key="cIdx"
                    size="small"
                    color="primary"
                    outline
                  >
                    {{ ch }}
                  </va-chip>
                </div>
                <div v-else style="font-size: 0.75rem; color: var(--va-text-secondary);">
                  {{ $t('field_impact.no_downstream_deps') }}
                </div>
              </div>
            </div>
          </div>
        </va-inner-loading>
      </div>

      <!-- TAB 2: DDL / Proposed Changes Analyzer -->
      <div v-if="activeTab === 'ddl'" style="display: flex; flex-direction: column; gap: 1rem;">
        <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
          🔍 {{ $t('schema_compatibility_desc') }}
        </va-alert>

        <!-- Input Simulation Section -->
        <div style="display: flex; flex-direction: column; gap: 0.5rem;">
          <va-textarea
            v-model="proposedChanges"
            label="변경 예정 스키마 DDL 또는 변경 기술서"
            placeholder="예: ALTER TABLE customer DROP COLUMN legacy_code; ALTER TABLE customer ALTER COLUMN biz_reg_no SET NOT NULL;"
            :rows="3"
          />
          <div style="display: flex; justify-content: flex-end;">
            <va-button size="small" color="primary" :loading="loadingDdl" @click="runDdlAnalysis">
              {{ $t('check_compatibility') }}
            </va-button>
          </div>
        </div>

        <!-- DDL Report Section -->
        <va-inner-loading :loading="loadingDdl">
          <div v-if="ddlReport" style="display: flex; flex-direction: column; gap: 1rem;">
            <!-- Summary Banner -->
            <div
              style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); display: flex; justify-content: space-between; align-items: center;"
              :style="{ background: ddlReport.overallCompatibility === 'BREAKING_CHANGE' ? 'rgba(235, 59, 90, 0.1)' : 'rgba(32, 191, 107, 0.1)' }"
            >
              <span style="font-weight: 700; font-size: 0.85rem;">
                {{ ddlReport.summary }}
              </span>
              <va-badge
                :text="ddlReport.overallCompatibility === 'BREAKING_CHANGE' ? $t('breaking_change_detected') : $t('compatible_status')"
                :color="ddlReport.overallCompatibility === 'BREAKING_CHANGE' ? 'danger' : 'success'"
                size="small"
              />
            </div>

            <!-- Risk Items Table -->
            <div style="max-height: 250px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
              <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
                <thead>
                  <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                    <th style="padding: 0.5rem 0.75rem;">{{ $t('field_impact.col_target_field') }}</th>
                    <th style="padding: 0.5rem 0.75rem;">{{ $t('field_impact.col_change_type') }}</th>
                    <th style="padding: 0.5rem 0.75rem; width: 80px; text-align: center;">{{ $t('field_impact.col_risk_level') }}</th>
                    <th style="padding: 0.5rem 0.75rem;">{{ $t('field_impact.col_impact_desc') }}</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="(r, idx) in ddlReport.risks"
                    :key="idx"
                    style="border-bottom: 1px solid var(--va-background-border);"
                  >
                    <td style="padding: 0.5rem 0.75rem; font-weight: 700;">{{ r.fieldKey }}</td>
                    <td style="padding: 0.5rem 0.75rem; font-family: monospace; font-size: 0.75rem;">{{ r.changeType }}</td>
                    <td style="padding: 0.5rem 0.75rem; text-align: center;">
                      <va-badge
                        :text="r.riskLevel"
                        :color="r.riskLevel === 'CRITICAL' ? 'danger' : (r.riskLevel === 'WARNING' ? 'warning' : 'info')"
                        size="small"
                      />
                    </td>
                    <td style="padding: 0.5rem 0.75rem; color: var(--va-text-secondary); font-size: 0.78rem;">
                      <div>{{ r.impactDescription }}</div>
                      <div style="color: var(--va-primary); margin-top: 0.2rem;">💡 {{ r.mitigationGuide }}</div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </va-inner-loading>
      </div>

      <!-- Modal Footer Close Button -->
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
  domainId?: string | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t, locale } = useI18n()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const activeTab = ref<'field' | 'ddl'>('field')

// Tab 1: Field Simulation State
const fields = ref<any[]>([])
const loadingFields = ref(false)
const selectedFieldId = ref<string | null>(null)
const changeType = ref<string>('DROP')
const newType = ref<string>('STRING')
const newKey = ref<string>('')
const simulating = ref(false)
const fieldReport = ref<any>(null)

const typeOptions = ['STRING', 'NUMBER', 'BOOLEAN', 'DATE', 'DATETIME', 'JSON']

const changeTypeOptions = computed(() => [
  { value: 'DROP', label: t('field_impact.type_drop') },
  { value: 'MAKE_REQUIRED', label: t('field_impact.type_make_required') },
  { value: 'TYPE_CHANGE', label: t('field_impact.type_type_change') },
  { value: 'RENAME', label: t('field_impact.type_rename') }
])

const fieldOptions = computed(() => {
  return fields.value.map(f => {
    let nameStr = f.key
    if (f.name) {
      if (typeof f.name === 'object') {
        nameStr = locale.value === 'ko' ? (f.name.ko || f.name.en || f.key) : (f.name.en || f.name.ko || f.key)
      } else {
        nameStr = String(f.name)
      }
    }
    return {
      value: f.id,
      label: `${f.key} (${nameStr})`,
      raw: f
    }
  })
})

const getFieldDisplayName = (fieldKey: string) => {
  const f = fields.value.find(item => item.key === fieldKey)
  if (!f || !f.name) return fieldKey
  if (typeof f.name === 'object') {
    return locale.value === 'ko' ? (f.name.ko || f.name.en || fieldKey) : (f.name.en || f.name.ko || fieldKey)
  }
  return String(f.name)
}

const { customFetch } = useCustomFetch()

const loadDomainFields = async () => {
  if (!props.domainId) return
  loadingFields.value = true
  try {
    const res: any = await customFetch(`/api/domains/${props.domainId}/fields`)
    const fieldData = res?.data?.value ?? res
    fields.value = Array.isArray(fieldData) ? fieldData : []
    if (fields.value.length > 0 && !selectedFieldId.value) {
      selectedFieldId.value = fields.value[0].id
    }
  } catch (e) {
    console.error('Failed to load domain fields', e)
  } finally {
    loadingFields.value = false
  }
}

const runFieldSimulation = async () => {
  if (!props.domainId || !selectedFieldId.value) return
  simulating.value = true
  try {
    const res: any = await customFetch(`/api/domains/${props.domainId}/schema/simulate-field-impact`, {
      method: 'POST',
      body: {
        fieldDefinitionId: selectedFieldId.value,
        changeType: changeType.value,
        newType: changeType.value === 'TYPE_CHANGE' ? newType.value : undefined,
        newKey: changeType.value === 'RENAME' ? newKey.value : undefined,
        newRequired: changeType.value === 'MAKE_REQUIRED' ? true : undefined
      }
    })
    fieldReport.value = res?.data?.value ?? res
  } catch (e) {
    console.error('Failed to simulate field impact', e)
  } finally {
    simulating.value = false
  }
}

// Tab 2: DDL Analysis State
const proposedChanges = ref('')
const ddlReport = ref<any>(null)
const loadingDdl = ref(false)

const runDdlAnalysis = async () => {
  if (!props.domainId) return
  loadingDdl.value = true
  try {
    const res: any = await customFetch(`/api/domains/${props.domainId}/schema/compatibility-check`, {
      method: 'POST',
      body: {
        proposedChanges: proposedChanges.value
      }
    })
    ddlReport.value = res?.data?.value ?? res
  } catch (e: any) {
    console.error('Failed to analyze schema compatibility', e)
  } finally {
    loadingDdl.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    fieldReport.value = null
    ddlReport.value = null
    loadDomainFields()
  }
}, { immediate: true })

watch(() => selectedFieldId.value, () => {
  if (selectedFieldId.value && props.modelValue && activeTab.value === 'field') {
    runFieldSimulation()
  }
})
</script>

