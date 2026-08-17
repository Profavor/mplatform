<template>
  <AppModal
    v-model="show"
    :title="$t('auto_remediation')"
    icon="auto_fix_high"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="info" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🪄 {{ $t('auto_remediation_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <!-- Action Header -->
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <va-button
            preset="secondary"
            icon="search"
            size="small"
            :loading="loading"
            @click="scanProposals"
          >
            {{ $t('scan_remediation') }}
          </va-button>

          <va-button
            v-if="proposals.length > 0"
            color="success"
            icon="auto_fix_high"
            size="small"
            :loading="applying"
            @click="applyAll"
          >
            {{ $t('apply_all_remediation') }} ({{ proposals.length }}건)
          </va-button>
        </div>

        <!-- Proposals List Table -->
        <div v-if="proposals.length > 0" style="margin-top: 0.75rem;">
          <div style="font-weight: 700; font-size: 0.9rem; margin-bottom: 0.5rem;">
            🔍 보정 제안 대상 ({{ proposals.length }}건):
          </div>

          <div style="max-height: 280px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.4rem 0.6rem;">식별 코드</th>
                  <th style="padding: 0.4rem 0.6rem;">필드</th>
                  <th style="padding: 0.4rem 0.6rem;">{{ $t('current_val') }}</th>
                  <th style="padding: 0.4rem 0.6rem;">{{ $t('proposed_val') }}</th>
                  <th style="padding: 0.4rem 0.6rem;">{{ $t('remediation_reason') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(p, idx) in proposals" :key="idx" style="border-bottom: 1px solid var(--va-background-border);">
                  <td style="padding: 0.4rem 0.6rem; font-weight: bold; color: var(--va-primary);">{{ p.recordCode }}</td>
                  <td style="padding: 0.4rem 0.6rem;">{{ p.fieldName }}</td>
                  <td style="padding: 0.4rem 0.6rem; color: var(--va-danger); text-decoration: line-through;">
                    {{ p.currentValue }}
                  </td>
                  <td style="padding: 0.4rem 0.6rem; color: var(--va-success); font-weight: bold;">
                    {{ p.proposedValue }}
                  </td>
                  <td style="padding: 0.4rem 0.6rem; font-size: 0.78rem; color: var(--va-text-secondary);">
                    {{ p.reason }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div v-else-if="!loading" style="text-align: center; padding: 2.5rem; color: var(--va-text-secondary);">
          <va-icon name="check_circle" size="large" color="success" style="margin-bottom: 0.5rem;" />
          <p>{{ $t('no_remediations_needed') }}</p>
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
import { useToast } from 'vuestic-ui'
import { useCustomFetch } from '~/composables/useCustomFetch'
import AppModal from '~/components/common/AppModal.vue'

const props = defineProps<{
  modelValue: boolean
  domainId?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'applied', result: any): void
}>()

const { t } = useI18n()
const toast = useToast()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const proposals = ref<any[]>([])
const loading = ref(false)
const applying = ref(false)

const scanProposals = async () => {
  if (!props.domainId) return
  loading.value = true
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/dq/remediation/scan`, {
      method: 'POST'
    })
    if (res.data?.value) {
      proposals.value = res.data.value
    }
  } catch (e: any) {
    toast.init({
      message: '보정 제안 스캔 실패: ' + (e.message || ''),
      color: 'danger'
    })
  } finally {
    loading.value = false
  }
}

const applyAll = async () => {
  if (!props.domainId || proposals.value.length === 0) return
  applying.value = true
  try {
    const items = proposals.value.map(p => ({
      recordId: p.recordId,
      fieldKey: p.fieldKey,
      newValue: p.proposedValue
    }))

    const res = await useCustomFetch(`/domains/${props.domainId}/dq/remediation/apply`, {
      method: 'POST',
      body: { items }
    })

    if (res.data?.value) {
      toast.init({
        message: res.data.value.message || '보정이 성공적으로 반영되었습니다.',
        color: 'success'
      })
      emit('applied', res.data.value)
      proposals.value = []
      show.value = false
    }
  } catch (e: any) {
    toast.init({
      message: '보정 반영 실패: ' + (e.message || ''),
      color: 'danger'
    })
  } finally {
    applying.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) scanProposals()
})
</script>
