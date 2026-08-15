<template>
  <va-modal
    v-model="show"
    :title="$t('dynamic_routing')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🔀 {{ $t('dynamic_routing_desc') }}
      </va-alert>

      <!-- Action Header -->
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <span style="font-weight: 700; font-size: 0.9rem;">
          📋 등록된 결재선 템플릿: {{ templates.length }}개
        </span>
        <va-button
          color="primary"
          icon="add"
          size="small"
          @click="showCreateForm = !showCreateForm"
        >
          {{ $t('add_template') }}
        </va-button>
      </div>

      <!-- Create Form Collapsible -->
      <div
        v-if="showCreateForm"
        style="padding: 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; flex-direction: column; gap: 0.75rem;"
      >
        <div style="font-weight: 700; font-size: 0.88rem;">
          ➕ 신규 동적 결재선 규칙 등록
        </div>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem;">
          <va-input v-model="form.templateName" :label="$t('template_name')" placeholder="예: VIP 고객 2단계 결재" />
          <va-input v-model="form.conditionField" :label="$t('condition_field')" placeholder="예: grade 또는 amount" />
          <va-select
            v-model="form.conditionOperator"
            :options="['EQUALS', 'CONTAINS', 'GTE']"
            :label="$t('condition_operator')"
          />
          <va-input v-model="form.conditionValue" :label="$t('condition_value')" placeholder="예: VIP 또는 1000000" />
        </div>

        <div style="font-size: 0.8rem; font-weight: 600; margin-top: 0.25rem;">
          배정 결재 단계:
        </div>
        <div style="display: flex; gap: 0.5rem; flex-direction: column;">
          <div
            v-for="(step, idx) in form.steps"
            :key="idx"
            style="display: flex; gap: 0.5rem; align-items: center;"
          >
            <va-badge :text="`${step.stepOrder}단계`" color="primary" size="small" />
            <va-input v-model="step.stepName" placeholder="단계명 (예: 부서장 승인)" style="flex: 1;" />
            <va-select
              v-model="step.requiredRole"
              :options="['ROLE_DEPT_HEAD', 'ROLE_SECURITY_ADMIN', 'ROLE_EXECUTIVE', 'ROLE_ADMIN']"
              placeholder="담당 역할"
              style="width: 180px;"
            />
          </div>
        </div>

        <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 0.25rem;">
          <va-button preset="secondary" size="small" @click="addStep">
            + 결재 단계 추가
          </va-button>
          <div style="display: flex; gap: 0.5rem;">
            <va-button preset="secondary" size="small" @click="showCreateForm = false">
              {{ $t('cancel') }}
            </va-button>
            <va-button color="success" size="small" :loading="creating" @click="submitTemplate">
              {{ $t('save') }}
            </va-button>
          </div>
        </div>
      </div>

      <!-- Templates Table -->
      <va-inner-loading :loading="loading">
        <div style="max-height: 280px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
          <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
            <thead>
              <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                <th style="padding: 0.5rem 0.75rem;">{{ $t('template_name') }}</th>
                <th style="padding: 0.5rem 0.75rem;">적용 조건</th>
                <th style="padding: 0.5rem 0.75rem;">{{ $t('approval_steps') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="t in templates"
                :key="t.id"
                style="border-bottom: 1px solid var(--va-background-border);"
              >
                <td style="padding: 0.5rem 0.75rem; font-weight: 700; color: var(--va-primary);">{{ t.templateName }}</td>
                <td style="padding: 0.5rem 0.75rem;">
                  <span v-if="t.conditionField" style="font-family: monospace;">
                    {{ t.conditionField }} {{ t.conditionOperator }} '{{ t.conditionValue }}'
                  </span>
                  <span v-else color="secondary">기본값</span>
                </td>
                <td style="padding: 0.5rem 0.75rem;">
                  <div style="display: flex; gap: 0.3rem; flex-wrap: wrap;">
                    <va-badge
                      v-for="s in t.steps"
                      :key="s.stepOrder"
                      :text="`${s.stepOrder}. ${s.stepName || s.requiredRole}`"
                      color="secondary"
                      size="small"
                    />
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
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
import { ref, computed, watch, reactive } from 'vue'
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

const templates = ref<any[]>([])
const loading = ref(false)
const creating = ref(false)
const showCreateForm = ref(false)

const form = reactive({
  templateName: '',
  conditionField: '',
  conditionOperator: 'EQUALS',
  conditionValue: '',
  steps: [
    { stepOrder: 1, stepName: '부서장 검토', requiredRole: 'ROLE_DEPT_HEAD' },
    { stepOrder: 2, stepName: '임원 최종 승인', requiredRole: 'ROLE_EXECUTIVE' }
  ]
})

const addStep = () => {
  form.steps.push({
    stepOrder: form.steps.length + 1,
    stepName: '관리자 승인',
    requiredRole: 'ROLE_ADMIN'
  })
}

const fetchTemplates = async () => {
  loading.value = true
  try {
    const url = props.domainId ? `/approvals/routing-templates?domainId=${props.domainId}` : '/approvals/routing-templates'
    const res = await useCustomFetch(url)
    if (res.data?.value) {
      templates.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to fetch routing templates', e)
  } finally {
    loading.value = false
  }
}

const submitTemplate = async () => {
  if (!form.templateName) return
  creating.value = true
  try {
    const res = await useCustomFetch('/approvals/routing-templates', {
      method: 'POST',
      body: {
        templateName: form.templateName,
        domainId: props.domainId,
        conditionField: form.conditionField,
        conditionOperator: form.conditionOperator,
        conditionValue: form.conditionValue,
        steps: form.steps
      }
    })
    if (res.data?.value) {
      templates.value.unshift(res.data.value)
      showCreateForm.value = false
      form.templateName = ''
      form.conditionField = ''
      form.conditionValue = ''
    }
  } catch (e: any) {
    console.error('Failed to create routing template', e)
  } finally {
    creating.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) fetchTemplates()
})
</script>
