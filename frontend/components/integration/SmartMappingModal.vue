<template>
  <va-modal
    v-model="show"
    :title="$t('smart_mapping')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1rem; padding: 0.5rem;">
      <va-alert color="info" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        💡 {{ $t('smart_mapping_desc') }}
      </va-alert>

      <!-- Sample Payload Input -->
      <div>
        <label style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">
          {{ $t('sample_payload') }}
        </label>
        <va-textarea
          v-model="samplePayload"
          :placeholder="$t('sample_payload_placeholder')"
          :rows="4"
          style="width: 100%; font-family: monospace; font-size: 0.85rem;"
        />
      </div>

      <div style="display: flex; justify-content: flex-end;">
        <va-button
          color="primary"
          icon="auto_fix_high"
          :loading="loading"
          :disabled="!samplePayload.trim() || loading"
          @click="fetchRecommendations"
        >
          {{ $t('recommend_mapping') }}
        </va-button>
      </div>

      <!-- Recommendations Table -->
      <div v-if="recommendations.length > 0" style="display: flex; flex-direction: column; gap: 0.5rem;">
        <div style="font-weight: 700; font-size: 0.9rem; color: var(--va-text-primary);">
          ✨ 추천 매핑 결과 ({{ recommendations.length }}건):
        </div>
        <div style="max-height: 240px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
          <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
            <thead>
              <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                <th style="padding: 0.4rem 0.6rem;">{{ $t('source_field') }}</th>
                <th style="padding: 0.4rem 0.6rem;">{{ $t('target_field') }}</th>
                <th style="padding: 0.4rem 0.6rem; width: 70px;">{{ $t('confidence_score') }}</th>
                <th style="padding: 0.4rem 0.6rem;">{{ $t('match_reason') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(rec, idx) in recommendations" :key="idx" style="border-bottom: 1px solid var(--va-background-border);">
                <td style="padding: 0.4rem 0.6rem; font-weight: 600;">{{ rec.sourceField }}</td>
                <td style="padding: 0.4rem 0.6rem; color: var(--va-primary); font-weight: bold;">
                  {{ rec.targetFieldName }} ({{ rec.targetFieldKey }})
                </td>
                <td style="padding: 0.4rem 0.6rem;">
                  <va-badge
                    :text="`${rec.confidenceScore}%`"
                    :color="rec.confidenceScore >= 90 ? 'success' : (rec.confidenceScore >= 70 ? 'warning' : 'info')"
                    size="small"
                  />
                </td>
                <td style="padding: 0.4rem 0.6rem; font-size: 0.78rem; color: var(--va-text-secondary);">
                  {{ rec.matchReason }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem;">
        <va-button preset="secondary" @click="show = false">
          {{ $t('close') }}
        </va-button>
        <va-button
          v-if="recommendations.length > 0"
          color="success"
          icon="check"
          @click="applySuggested"
        >
          {{ $t('apply_recommendations') }}
        </va-button>
      </div>
    </div>
  </va-modal>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'vuestic-ui'
import { useCustomFetch } from '~/composables/useCustomFetch'

const props = defineProps<{
  modelValue: boolean
  domainId?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'apply', mappings: any[]): void
}>()

const { t } = useI18n()
const toast = useToast()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const samplePayload = ref('{\n  "name": "홍길동",\n  "phone_no": "010-1234-5678",\n  "email_addr": "hong@example.com"\n}')
const loading = ref(false)
const recommendations = ref<any[]>([])

const fetchRecommendations = async () => {
  if (!props.domainId || !samplePayload.value.trim()) return
  loading.value = true
  try {
    const res = await useCustomFetch('/admin/integration/channels/smart-mapping-recommend', {
      method: 'POST',
      body: {
        domainId: props.domainId,
        samplePayload: samplePayload.value
      }
    })
    if (res.data?.value) {
      recommendations.value = res.data.value
      if (recommendations.value.length === 0) {
        toast.init({
          message: '일치하는 추천 매핑 필드가 없습니다.',
          color: 'warning'
        })
      }
    }
  } catch (e: any) {
    toast.init({
      message: '스마트 매핑 추천 조회 실패: ' + (e.message || ''),
      color: 'danger'
    })
  } finally {
    loading.value = false
  }
}

const applySuggested = () => {
  emit('apply', recommendations.value)
  show.value = false
  toast.init({
    message: `${recommendations.value.length}개의 추천 매핑이 적용되었습니다.`,
    color: 'success'
  })
}
</script>
