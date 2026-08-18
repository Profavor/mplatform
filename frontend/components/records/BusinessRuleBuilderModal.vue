<template>
  <AppModal
    v-model="show"
    :title="$t('business_rules')"
    icon="rule"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        ⚖️ {{ $t('business_rules_desc') }}
      </va-alert>

      <!-- Add New Rule Section -->
      <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; flex-direction: column; gap: 0.75rem;">
        <div style="font-weight: 700; font-size: 0.85rem;">{{ $t('add_rule') }}</div>
        <va-input v-model="newRuleName" :label="$t('rule_name')" placeholder="예: VIP 고객 필수 사업자번호 및 신용등급 검증" />
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem;">
          <va-input v-model="newConditionExpr" :label="$t('condition_expr')" placeholder="예: grade == 'VIP'" />
          <va-input v-model="newValidationExpr" :label="$t('validation_expr')" placeholder="예: biz_no != null && rating in ['A', 'B']" />
        </div>
        <div style="display: flex; justify-content: flex-end;">
          <va-button color="success" size="small" :loading="saving" @click="saveRule">
            {{ $t('add_rule') }}
          </va-button>
        </div>
      </div>

      <!-- Action Button -->
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <span style="font-weight: 700; font-size: 0.85rem;">등록된 비즈니스 룰 목록 ({{ rules.length }})</span>
        <va-button size="small" color="primary" :loading="evaluating" @click="evaluateRules">
          {{ $t('evaluate_rules') }}
        </va-button>
      </div>

      <!-- Evaluation Results / Rule List -->
      <va-inner-loading :loading="loading">
        <div style="max-height: 250px; overflow-y: auto; display: flex; flex-direction: column; gap: 0.75rem;">
          <div
            v-for="r in rules"
            :key="r.ruleId"
            style="padding: 0.75rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-card); display: flex; flex-direction: column; gap: 0.4rem;"
          >
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span style="font-weight: 700; font-size: 0.85rem;">{{ r.ruleName }}</span>
              <va-badge :text="r.active ? '활성' : '비활성'" :color="r.active ? 'success' : 'secondary'" size="small" />
            </div>
            <div style="font-size: 0.75rem; color: var(--va-text-secondary); font-family: monospace;">
              조건: IF {{ r.conditionExpr }} THEN {{ r.validationExpr }}
            </div>
            <div v-if="evalResultsMap[r.ruleId]" style="font-size: 0.75rem; padding: 0.4rem; border-radius: 4px; background: rgba(235, 59, 90, 0.08); color: var(--va-danger);">
              <div v-for="(v, vIdx) in evalResultsMap[r.ruleId].sampleViolations" :key="vIdx">
                ⚠️ [{{ v.recordCode }}] {{ v.reason }}
              </div>
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

const { t } = useI18n()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const rules = ref<any[]>([])
const evalResultsMap = ref<Record<string, any>>({})
const loading = ref(false)
const saving = ref(false)
const evaluating = ref(false)

const newRuleName = ref('')
const newConditionExpr = ref('')
const newValidationExpr = ref('')

const loadRules = async () => {
  if (!props.domainId) return
  loading.value = true
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/business-rules`)
    if (res.data?.value) {
      rules.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to load business rules', e)
  } finally {
    loading.value = false
  }
}

const saveRule = async () => {
  if (!newRuleName.value.trim() || !newConditionExpr.value.trim() || !newValidationExpr.value.trim()) return
  saving.value = true
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/business-rules`, {
      method: 'POST',
      body: {
        ruleName: newRuleName.value.trim(),
        conditionExpr: newConditionExpr.value.trim(),
        validationExpr: newValidationExpr.value.trim(),
        errorMessage: '비즈니스 유효성 검증 실패',
        enabled: true
      }
    })
    if (res.data?.value) {
      newRuleName.value = ''
      newConditionExpr.value = ''
      newValidationExpr.value = ''
      await loadRules()
    }
  } catch (e: any) {
    console.error('Failed to save business rule', e)
  } finally {
    saving.value = false
  }
}

const evaluateRules = async () => {
  if (!props.domainId) return
  evaluating.value = true
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/business-rules/evaluate`, {
      method: 'POST'
    })
    if (res.data?.value) {
      const map: Record<string, any> = {}
      for (const item of res.data.value) {
        map[item.ruleId] = item
      }
      evalResultsMap.value = map
    }
  } catch (e: any) {
    console.error('Failed to evaluate rules', e)
  } finally {
    evaluating.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    evalResultsMap.value = {}
    loadRules()
  }
})
</script>
