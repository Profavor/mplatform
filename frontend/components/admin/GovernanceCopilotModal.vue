<template>
  <va-modal
    v-model="show"
    :title="$t('governance_copilot')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1rem; padding: 0.5rem; height: 520px;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🤖 {{ $t('governance_copilot_desc') }}
      </va-alert>

      <!-- Quick Prompt Chips -->
      <div style="display: flex; gap: 0.4rem; flex-wrap: wrap;">
        <va-chip
          v-for="(prompt, idx) in quickPrompts"
          :key="idx"
          size="small"
          color="primary"
          outline
          style="cursor: pointer;"
          @click="sendPrompt(prompt)"
        >
          💡 {{ prompt }}
        </va-chip>
      </div>

      <!-- Chat History Area -->
      <div style="flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 0.75rem; padding: 0.5rem; border: 1px solid var(--va-background-border); border-radius: 8px; background: var(--va-background-element);">
        <div
          v-for="(msg, idx) in messages"
          :key="idx"
          style="display: flex; flex-direction: column;"
          :style="{ alignItems: msg.role === 'USER' ? 'flex-end' : 'flex-start' }"
        >
          <div
            style="max-width: 85%; padding: 0.75rem 1rem; border-radius: 12px; font-size: 0.85rem; line-height: 1.5;"
            :style="{
              background: msg.role === 'USER' ? 'var(--va-primary)' : 'var(--va-background-card)',
              color: msg.role === 'USER' ? '#fff' : 'var(--va-text-primary)',
              border: msg.role === 'USER' ? 'none' : '1px solid var(--va-background-border)'
            }"
          >
            <div>{{ msg.content }}</div>

            <!-- Metric Cards (if provided) -->
            <div v-if="msg.metricCards && Object.keys(msg.metricCards).length > 0" style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 0.4rem; margin-top: 0.6rem;">
              <div
                v-for="(val, key) in msg.metricCards"
                :key="key"
                style="padding: 0.4rem; border-radius: 6px; background: var(--va-background-element); border: 1px solid var(--va-background-border); text-align: center;"
              >
                <div style="font-size: 0.7rem; color: var(--va-text-secondary);">{{ key }}</div>
                <div style="font-weight: 700; font-size: 0.85rem; color: var(--va-primary); margin-top: 0.1rem;">{{ val }}</div>
              </div>
            </div>

            <!-- Suggested Actions -->
            <div v-if="msg.suggestedActions && msg.suggestedActions.length > 0" style="display: flex; gap: 0.3rem; flex-wrap: wrap; margin-top: 0.6rem;">
              <va-chip
                v-for="(act, aIdx) in msg.suggestedActions"
                :key="aIdx"
                size="small"
                color="info"
                style="cursor: pointer;"
                @click="sendPrompt(act)"
              >
                ⚡ {{ act }}
              </va-chip>
            </div>
          </div>
          <span style="font-size: 0.7rem; color: var(--va-text-secondary); margin-top: 0.2rem; padding: 0 0.4rem;">
            {{ msg.timestamp }}
          </span>
        </div>

        <div v-if="loading" style="display: flex; align-items: center; gap: 0.5rem; color: var(--va-text-secondary); font-size: 0.82rem;">
          <va-inner-loading :loading="true" size="small" />
          Copilot이 전사 메타데이터 및 지표를 분석 중입니다...
        </div>
      </div>

      <!-- Input Bar -->
      <div style="display: flex; gap: 0.5rem;">
        <va-input
          v-model="inputPrompt"
          style="flex: 1;"
          :placeholder="$t('copilot_placeholder')"
          @keydown.enter="send"
        />
        <va-button color="primary" icon="send" :loading="loading" @click="send">
          {{ $t('copilot_send') }}
        </va-button>
      </div>

      <div style="display: flex; justify-content: flex-end;">
        <va-button preset="secondary" size="small" @click="show = false">
          {{ $t('close') }}
        </va-button>
      </div>
    </div>
  </va-modal>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const inputPrompt = ref('')
const loading = ref(false)

const quickPrompts = [
  '전사 데이터 품질 현황 요약해줘',
  'SLA 지연시간 및 계약 상태 점검해줘',
  '파이프라인 장애 및 자율 복구 내역 알려줘'
]

const messages = ref<any[]>([
  {
    role: 'COPILOT',
    content: '안녕하세요! 전사 마스터 데이터 거버넌스 AI Copilot입니다. 무엇을 도와드릴까요?',
    timestamp: '14:38',
    metricCards: { '거버넌스 성숙도': 'Level 5', '가동 기능': '50/50', '상태': 'Online' },
    suggestedActions: ['전사 품질 요약', 'SLA 계약 점검', '파이프라인 복구 내역']
  }
])

const { customFetch } = useCustomFetch()

const sendPrompt = (prompt: string) => {
  inputPrompt.value = prompt
  send()
}

const send = async () => {
  const text = inputPrompt.value.trim()
  if (!text || loading.value) return

  messages.value.push({
    role: 'USER',
    content: text,
    timestamp: new Date().toTimeString().slice(0, 5)
  })
  inputPrompt.value = ''
  loading.value = true

  try {
    const res = await customFetch('/api/governance/copilot/chat', {
      method: 'POST',
      body: { prompt: text, history: [] }
    })
    const payload = res?.reply ? res : res?.data?.value
    if (payload) {
      messages.value.push({
        role: 'COPILOT',
        content: payload.reply,
        metricCards: payload.metricCards,
        suggestedActions: payload.suggestedActions,
        timestamp: payload.timestamp || new Date().toTimeString().slice(0, 5)
      })
    }
  } catch (e: any) {
    console.error('Failed to chat with Copilot', e)
  } finally {
    loading.value = false
  }
}
</script>
