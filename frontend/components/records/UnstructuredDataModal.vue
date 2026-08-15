<template>
  <va-modal
    v-model="show"
    :title="$t('ai_structurizer')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🤖 {{ $t('ai_structurizer_desc') }}
      </va-alert>

      <!-- Input Text Section -->
      <div style="display: flex; flex-direction: column; gap: 0.5rem;">
        <va-textarea
          v-model="rawText"
          :placeholder="$t('raw_text_placeholder')"
          :rows="4"
        />
        <div style="display: flex; justify-content: flex-end;">
          <va-button size="small" color="primary" :loading="extracting" @click="runExtraction">
            {{ $t('extract_fields') }}
          </va-button>
        </div>
      </div>

      <!-- AI Extracted Fields Result -->
      <va-inner-loading :loading="extracting">
        <div v-if="result" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Header Banner -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center;">
            <div style="display: flex; flex-direction: column; gap: 0.2rem;">
              <span style="font-weight: 700; font-size: 0.85rem;">추천 레코드 식별 코드: {{ result.suggestedRecordCode }}</span>
              <span style="font-size: 0.75rem; color: var(--va-text-secondary);">
                {{ $t('overall_confidence') }}: <strong style="color: var(--va-primary);">{{ Math.round(result.overallConfidence * 100) }}%</strong>
              </span>
            </div>
            <va-badge
              :text="$t('extracted_fields_count') + ': ' + result.fields.length"
              color="success"
              size="small"
            />
          </div>

          <!-- Fields Table -->
          <div style="max-height: 250px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.5rem 0.75rem;">도메인 필드</th>
                  <th style="padding: 0.5rem 0.75rem;">AI 추출값</th>
                  <th style="padding: 0.5rem 0.75rem; width: 80px; text-align: center;">신뢰도</th>
                  <th style="padding: 0.5rem 0.75rem;">추출 원문 스니펫</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(f, idx) in result.fields"
                  :key="idx"
                  style="border-bottom: 1px solid var(--va-background-border);"
                >
                  <td style="padding: 0.5rem 0.75rem; font-weight: 700; color: var(--va-primary);">
                    {{ f.fieldKey }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem; font-weight: 600;">
                    {{ f.extractedValue }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: center;">
                    <va-badge
                      :text="Math.round(f.confidenceScore * 100) + '%'"
                      color="info"
                      size="small"
                    />
                  </td>
                  <td style="padding: 0.5rem 0.75rem; color: var(--va-text-secondary); font-size: 0.75rem;">
                    {{ f.sourceSnippet }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
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
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'

const props = defineProps<{
  modelValue: boolean
  domainId: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const rawText = ref('')
const result = ref<any>(null)
const extracting = ref(false)

const runExtraction = async () => {
  if (!props.domainId || !rawText.value.trim()) return
  extracting.value = true
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/ai/extract-unstructured`, {
      method: 'POST',
      body: {
        rawText: rawText.value.trim()
      }
    })
    if (res.data?.value) {
      result.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to extract fields', e)
  } finally {
    extracting.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    rawText.value = '주식회사 글로벌테크 (사업자등록번호: 220-81-45678, 금액: 15,000,000원, 이메일: info@globaltech.kr)'
    runExtraction()
  }
})
</script>
