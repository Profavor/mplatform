<template>
  <va-modal
    v-model="show"
    :title="isSubmitMode ? ($t('impact_check_title') || '변경 사전 영향도 검토') : ($t('schema_impact_title') || '스키마 변경 영향도 사전 분석 보고서')"
    size="large"
    hide-default-actions
  >
    <div style="padding: 0.5rem; display: flex; flex-direction: column; gap: 1.25rem;">
      <div v-if="loading" style="display: flex; justify-content: center; align-items: center; padding: 3rem;">
        <va-progress-circle indeterminate size="large" />
      </div>

      <template v-else-if="reportData">
        <!-- Metric Grid Header -->
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(130px, 1fr)); gap: 0.75rem;">
          <div style="background: var(--va-background-element); padding: 0.85rem; border-radius: 8px; border-left: 4px solid var(--va-primary);">
            <div style="font-size: 0.75rem; color: var(--va-text-secondary); text-transform: uppercase; font-weight: 600;">
              {{ $t('risk_level') || '위험도 등급' }}
            </div>
            <div style="font-weight: 700; font-size: 1.15rem; margin-top: 0.25rem;">
              <va-badge :text="reportData.riskLevel" :color="getRiskColor(reportData.riskLevel)" size="large" />
            </div>
          </div>

          <div style="background: var(--va-background-element); padding: 0.85rem; border-radius: 8px; border-left: 4px solid #2c82e0;">
            <div style="font-size: 0.75rem; color: var(--va-text-secondary); text-transform: uppercase; font-weight: 600;">
              {{ $t('affected_records') || '영향 받는 레코드' }}
            </div>
            <div style="font-weight: 700; font-size: 1.15rem; color: var(--va-primary); margin-top: 0.25rem;">
              {{ (reportData.totalAffectedRecords || 0).toLocaleString() }} 건
            </div>
          </div>

          <div style="background: var(--va-background-element); padding: 0.85rem; border-radius: 8px; border-left: 4px solid #e6a23c;">
            <div style="font-size: 0.75rem; color: var(--va-text-secondary); text-transform: uppercase; font-weight: 600;">
              {{ $t('expected_dq_violations') || '예상 DQ 위반' }}
            </div>
            <div style="font-weight: 700; font-size: 1.15rem; color: #e6a23c; margin-top: 0.25rem;">
              {{ (reportData.expectedDqViolations || 0).toLocaleString() }} 건
            </div>
          </div>

          <div style="background: var(--va-background-element); padding: 0.85rem; border-radius: 8px; border-left: 4px solid #8e44ad;">
            <div style="font-size: 0.75rem; color: var(--va-text-secondary); text-transform: uppercase; font-weight: 600;">
              {{ $t('affected_target_field') || '대상 속성 필드' }}
            </div>
            <div style="font-weight: 700; font-size: 0.95rem; margin-top: 0.25rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
              {{ reportData.affectedFieldName || targetFieldName || '사번 (Employee ID)' }}
            </div>
          </div>
        </div>

        <!-- Impact Summary Callout -->
        <div v-if="computedImpactSummary" style="background: rgba(44, 130, 224, 0.08); border-left: 4px solid #2c82e0; padding: 0.85rem 1rem; border-radius: 6px; font-size: 0.88rem; line-height: 1.5; color: var(--va-text-primary);">
          <strong>💡 {{ $t('schema_impact_summary') || '영향 요약' }}:</strong> {{ computedImpactSummary }}
        </div>

        <!-- Warning or Safety notice list -->
        <div v-if="computedWarnings && computedWarnings.length > 0" 
             :style="{
               background: reportData?.riskLevel === 'LOW' ? 'rgba(40, 167, 69, 0.08)' : 'rgba(245, 158, 11, 0.08)',
               border: reportData?.riskLevel === 'LOW' ? '1px solid rgba(40, 167, 69, 0.3)' : '1px solid rgba(245, 158, 11, 0.3)',
               borderRadius: '8px',
               padding: '0.85rem 1rem'
             }">
          <div :style="{ fontWeight: '700', marginBottom: '0.4rem', fontSize: '0.95rem', color: reportData?.riskLevel === 'LOW' ? '#198754' : '#d97706' }">
            {{ reportData?.riskLevel === 'LOW' ? '✅ ' + ($t('impact_safety_notice') || '검토 사항') : '⚠️ ' + ($t('impact_warnings') || '경고 및 주의사항') }}
          </div>
          <ul style="margin: 0; padding-left: 1.25rem; font-size: 0.88rem; line-height: 1.5; color: var(--va-text-primary);">
            <li v-for="(warn, idx) in computedWarnings" :key="idx" style="margin-bottom: 0.25rem;">
              {{ warn }}
            </li>
          </ul>
        </div>

        <!-- Affected Sample Records Breakdown Table -->
        <div v-if="reportData.sampleAffectedRecords && reportData.sampleAffectedRecords.length > 0" style="display: flex; flex-direction: column; gap: 0.5rem;">
          <div style="font-weight: 600; font-size: 0.9rem; display: flex; justify-content: space-between; align-items: center;">
            <span>📋 {{ $t('affected_records_breakdown') || '영향 받는 실데이터 샘플 목록 (Breakdown)' }}</span>
            <span style="font-size: 0.75rem; color: var(--va-text-secondary);">Top {{ reportData.sampleAffectedRecords.length }} 건 표출</span>
          </div>

          <div style="border: 1px solid var(--va-background-border); border-radius: 6px; overflow: hidden;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.85rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.6rem 0.8rem;">{{ extractHeaderLabel(reportData?.idFieldHeaderName, 'record_id_attr', 'ID') }}</th>
                  <th style="padding: 0.6rem 0.8rem;">{{ extractHeaderLabel(reportData?.nameFieldHeaderName, 'record_name_attr', 'Name') }}</th>
                  <th style="padding: 0.6rem 0.8rem;">분류 노드</th>
                  <th style="padding: 0.6rem 0.8rem;">현재 속성값</th>
                  <th style="padding: 0.6rem 0.8rem;">최종 변경일</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(rec, rIdx) in reportData.sampleAffectedRecords" :key="rIdx" style="border-bottom: 1px solid var(--va-background-border);">
                  <td style="padding: 0.6rem 0.8rem; font-weight: 600; color: var(--va-primary);">{{ rec.idAttributeValue || rec.recordCode }}</td>
                  <td style="padding: 0.6rem 0.8rem; font-weight: 500;">{{ rec.nameAttributeValue || '-' }}</td>
                  <td style="padding: 0.6rem 0.8rem;">{{ rec.nodeName }}</td>
                  <td style="padding: 0.6rem 0.8rem; font-family: monospace;">{{ rec.currentValue || rec.fieldValue || '-' }}</td>
                  <td style="padding: 0.6rem 0.8rem; color: var(--va-text-secondary); font-size: 0.8rem;">{{ rec.updatedAt }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Affected Channels & DQ Rules -->
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem;">
          <div>
            <div style="font-weight: 600; font-size: 0.85rem; margin-bottom: 0.4rem;">
              🔗 {{ $t('affected_channels') || '영향 받는 연동 채널' }}
            </div>
            <div v-if="reportData.affectedIntegrationChannels && reportData.affectedIntegrationChannels.length > 0" style="display: flex; gap: 0.4rem; flex-wrap: wrap;">
              <va-chip v-for="ch in reportData.affectedIntegrationChannels" :key="ch" color="info" outline size="small">
                {{ ch }}
              </va-chip>
            </div>
            <div v-else style="font-size: 0.8rem; color: var(--va-text-secondary); font-style: italic;">
              {{ $t('no_affected_channels') || '연결된 활성 연동 채널 없음' }}
            </div>
          </div>

          <div>
            <div style="font-weight: 600; font-size: 0.85rem; margin-bottom: 0.4rem;">
              🛡️ {{ $t('affected_dq_rules') || '연관 품질(DQ) 검칙' }}
            </div>
            <div v-if="reportData.affectedDqRules && reportData.affectedDqRules.length > 0" style="display: flex; gap: 0.4rem; flex-wrap: wrap;">
              <va-chip v-for="rule in reportData.affectedDqRules" :key="rule" color="warning" outline size="small">
                {{ rule }}
              </va-chip>
            </div>
            <div v-else style="font-size: 0.8rem; color: var(--va-text-secondary); font-style: italic;">
              {{ $t('no_affected_dq_rules') || '연결된 DQ 검칙 없음' }}
            </div>
          </div>
        </div>
      </template>

      <!-- Bottom Action Buttons -->
      <div style="display: flex; flex-direction: column; gap: 0.75rem; margin-top: 1rem; border-top: 1px solid var(--va-background-border); padding-top: 1rem;">
        <div style="font-size: 0.8rem; color: var(--va-text-secondary); text-align: right;">
          ℹ️ {{ isSubmitMode ? '위의 변경 영향도를 확인하였습니다. 버튼을 누르면 결재 요청이 전송됩니다.' : ($t('confirm_risk_desc') || '위험도 및 경고 사항을 모두 확인하였으며, 스키마 변경을 최종 승인 및 적용합니다.') }}
        </div>
        <div style="display: flex; justify-content: flex-end; gap: 0.75rem;">
          <va-button preset="secondary" @click="show = false">{{ $t('cancel') || '취소' }}</va-button>
          
          <template v-if="isSubmitMode">
            <va-button v-if="reportData?.riskLevel === 'LOW'" color="success" icon="check_circle" @click="confirmSubmit">
              {{ $t('confirm_safety_submit') || '안전 확인 및 결재 상신' }}
            </va-button>
            <va-button v-else color="primary" icon="send" @click="confirmSubmit">
              {{ $t('confirm_and_submit') || '영향도 확인 및 결재 상신' }}
            </va-button>
          </template>

          <template v-else>
            <va-button v-if="reportData?.riskLevel === 'LOW'" color="success" icon="check_circle" @click="confirmChange">
              {{ $t('confirm_safety_apply') || '안전 확인 및 변경 사항 적용' }}
            </va-button>
            <va-button v-else color="danger" icon="warning" @click="confirmChange">
              {{ $t('confirm_risk_apply') || '경고 확인 및 변경 사항 최종 적용' }}
            </va-button>
          </template>
        </div>
      </div>
    </div>
  </va-modal>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'

const props = defineProps<{
  modelValue: boolean
  domainId: string | null
  changeRequest?: any
  isSubmitMode?: boolean
  targetFieldName?: string
}>()

const emit = defineEmits(['update:modelValue', 'confirm', 'confirm-submit'])

const { t, locale } = useI18n()
const { customFetch } = useCustomFetch()

const extractHeaderLabel = (headerObj: any, fallbackKey: string, fallbackDefault: string) => {
  if (!headerObj) return t(fallbackKey) || fallbackDefault
  if (typeof headerObj === 'string') return headerObj
  if (typeof headerObj === 'object') {
    const lang = locale.value || 'ko'
    return headerObj[lang] || headerObj['ko'] || headerObj['en'] || t(fallbackKey) || fallbackDefault
  }
  return t(fallbackKey) || fallbackDefault
}

const show = ref(props.modelValue)
const loading = ref(false)
const reportData = ref<any>(null)

watch(() => props.modelValue, (val) => {
  show.value = val
  if (val && props.domainId) {
    runImpactAnalysis()
  }
})

watch(show, (val) => {
  emit('update:modelValue', val)
})

const runImpactAnalysis = async () => {
  if (!props.domainId) return
  loading.value = true
  try {
    const res = await customFetch(`/api/domains/${props.domainId}/impact-analysis`, {
      method: 'POST',
      body: props.changeRequest || {}
    })
    reportData.value = res
  } catch (e) {
    console.error('Impact analysis failed:', e)
  } finally {
    loading.value = false
  }
}

const computedImpactSummary = computed(() => {
  if (!reportData.value) return ''
  const fieldName = reportData.value.affectedFieldName || props.targetFieldName || 'Field'
  const count = reportData.value.totalAffectedRecords || 0
  const isDelete = props.changeRequest?.changeType === 'DELETE_FIELD'
  
  if (isDelete) {
    return count > 0 
      ? t('impact_summary_delete', { field: fieldName, count })
      : t('impact_summary_delete_empty', { field: fieldName })
  } else {
    return count > 0
      ? t('impact_summary_modify', { field: fieldName, count })
      : t('impact_summary_modify_empty', { field: fieldName })
  }
})

const computedWarnings = computed(() => {
  if (!reportData.value) return []
  const list: string[] = []
  const count = reportData.value.totalAffectedRecords || 0
  const isDelete = props.changeRequest?.changeType === 'DELETE_FIELD'
  
  if (isDelete) {
    if (count > 0) {
      list.push(t('warning_delete_records', { count }))
    } else {
      list.push(t('warning_delete_records_zero'))
    }
  } else {
    if (count > 0) {
      list.push(t('warning_modify_records'))
    } else {
      list.push(t('warning_modify_records_zero'))
    }
  }
  
  const chCount = reportData.value.affectedIntegrationChannels?.length || 0
  if (chCount > 0) {
    list.push(t('warning_channels', { count: chCount }))
  }
  
  const dqCount = reportData.value.affectedDqRules?.length || 0
  if (dqCount > 0) {
    list.push(t('warning_dq_rules', { count: dqCount }))
  }
  
  return list
})

const getRiskColor = (level: string) => {
  switch (level) {
    case 'CRITICAL': return 'danger'
    case 'HIGH': return 'warning'
    case 'MEDIUM': return 'info'
    default: return 'success'
  }
}

const confirmChange = () => {
  show.value = false
  emit('confirm')
}

const confirmSubmit = () => {
  show.value = false
  emit('confirm-submit')
}
</script>
