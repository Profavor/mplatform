<template>
  <va-modal
    v-model="show"
    :title="$t('schema_impact_title') || '스키마 변경 영향도 사전 분석 보고서'"
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
              {{ reportData.totalAffectedRecords.toLocaleString() }} 건
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
              {{ reportData.affectedFieldName || '사번 (Employee ID)' }}
            </div>
          </div>
        </div>

        <!-- Impact Summary Callout -->
        <div v-if="reportData.impactSummary" style="background: rgba(44, 130, 224, 0.08); border-left: 4px solid #2c82e0; padding: 0.85rem 1rem; border-radius: 6px; font-size: 0.88rem; line-height: 1.5; color: var(--va-text-primary);">
          <strong>💡 {{ $t('schema_impact_summary') || '영향 요약' }}:</strong> {{ reportData.impactSummary }}
        </div>

        <!-- Warning list -->
        <va-card v-if="reportData.warnings && reportData.warnings.length > 0" color="warning" outline>
          <va-card-content style="padding: 0.85rem 1rem;">
            <div style="font-weight: 700; margin-bottom: 0.4rem; display: flex; align-items: center; gap: 0.5rem; font-size: 0.95rem;">
              <va-icon name="warning" color="warning" /> {{ $t('impact_warnings') || '경고 및 주의사항' }}
            </div>
            <ul style="margin: 0; padding-left: 1.25rem; font-size: 0.88rem; line-height: 1.5;">
              <li v-for="(warn, idx) in reportData.warnings" :key="idx" style="margin-bottom: 0.25rem;">
                {{ warn }}
              </li>
            </ul>
          </va-card-content>
        </va-card>

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
                  <th style="padding: 0.6rem 0.75rem; font-weight: 600;">Record ID</th>
                  <th style="padding: 0.6rem 0.75rem; font-weight: 600;">Classification Node</th>
                  <th style="padding: 0.6rem 0.75rem; font-weight: 600;">Current Value</th>
                  <th style="padding: 0.6rem 0.75rem; font-weight: 600;">Updated At</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(rec, index) in reportData.sampleAffectedRecords"
                  :key="index"
                  style="border-bottom: 1px solid var(--va-background-border);"
                >
                  <td style="padding: 0.5rem 0.75rem; font-family: monospace; font-weight: 600; color: var(--va-primary);">
                    {{ rec.recordCode }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem;">
                    {{ rec.nodeName }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem; font-weight: 600;">
                    {{ rec.fieldValue }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem; color: var(--va-text-secondary); font-size: 0.8rem;">
                    {{ rec.updatedAt }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Affected Channels & DQ Rules -->
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem;">
          <div v-if="reportData.affectedIntegrationChannels && reportData.affectedIntegrationChannels.length > 0">
            <div style="font-weight: 600; font-size: 0.85rem; margin-bottom: 0.4rem;">
              🔗 {{ $t('affected_channels') || '영향 받는 연동 채널' }}
            </div>
            <div style="display: flex; gap: 0.4rem; flex-wrap: wrap;">
              <va-chip v-for="ch in reportData.affectedIntegrationChannels" :key="ch" color="info" outline size="small">
                {{ ch }}
              </va-chip>
            </div>
          </div>

          <div v-if="reportData.affectedDqRules && reportData.affectedDqRules.length > 0">
            <div style="font-weight: 600; font-size: 0.85rem; margin-bottom: 0.4rem;">
              🛡️ {{ $t('affected_dq_rules') || '연관 품질(DQ) 검칙' }}
            </div>
            <div style="display: flex; gap: 0.4rem; flex-wrap: wrap;">
              <va-chip v-for="rule in reportData.affectedDqRules" :key="rule" color="warning" outline size="small">
                {{ rule }}
              </va-chip>
            </div>
          </div>
        </div>
      </template>

      <!-- Footer & Confirm Button Area -->
      <div style="display: flex; flex-direction: column; gap: 0.75rem; margin-top: 1rem; border-top: 1px solid var(--va-background-border); padding-top: 1rem;">
        <div style="font-size: 0.8rem; color: var(--va-text-secondary); text-align: right;">
          ℹ️ {{ $t('confirm_risk_desc') || '위험도 및 경고 사항을 모두 확인하였으며, 스키마 변경을 최종 승인 및 적용합니다.' }}
        </div>
        <div style="display: flex; justify-content: flex-end; gap: 0.75rem;">
          <va-button preset="secondary" @click="show = false">{{ $t('cancel') || '취소' }}</va-button>
          <va-button color="danger" icon="check_circle" @click="confirmChange">
            {{ $t('confirm_risk_apply') || '경고 확인 및 변경 사항 최종 적용' }}
          </va-button>
        </div>
      </div>
    </div>
  </va-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useCustomFetch } from '~/composables/useCustomFetch'

const props = defineProps<{
  modelValue: boolean
  domainId: string | null
  changeRequest: any
}>()

const emit = defineEmits(['update:modelValue', 'confirm'])

const { customFetch } = useCustomFetch()

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
      body: props.changeRequest
    })
    reportData.value = res
  } catch (e) {
    console.error('Impact analysis failed:', e)
  } finally {
    loading.value = false
  }
}

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
</script>
