<template>
  <AppModal
    v-model="show"
    :title="$t('webhook_hub')"
    icon="webhook"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        ⚡ {{ $t('webhook_hub_desc') }}
      </va-alert>

      <!-- Action Header -->
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <span style="font-weight: 700; font-size: 0.9rem;">
          🔗 등록된 웹훅 엔드포인트: {{ webhooks.length }}개
        </span>
        <va-button
          color="primary"
          icon="add"
          size="small"
          @click="showCreateForm = !showCreateForm"
        >
          {{ $t('add_webhook') }}
        </va-button>
      </div>

      <!-- Create Form Collapsible -->
      <div
        v-if="showCreateForm"
        style="padding: 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; flex-direction: column; gap: 0.75rem;"
      >
        <div style="font-weight: 700; font-size: 0.88rem;">
          ➕ 신규 실시간 이벤트 웹훅 등록
        </div>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem;">
          <va-input v-model="form.name" label="웹훅 명칭" placeholder="예: Slack 알림 채널" />
          <va-input v-model="form.targetUrl" :label="$t('target_url')" placeholder="https://api.domain.com/webhook" />
          <va-input v-model="form.secretKey" label="HMAC 서명 시크릿 키 (선택)" placeholder="미입력 시 자동 생성" />
          <va-select
            v-model="form.events"
            :options="['RECORD_CREATED', 'RECORD_UPDATED', 'RECORD_DELETED', 'APPROVAL_COMPLETED']"
            :label="$t('subscribed_events')"
            multiple
          />
        </div>
        <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.25rem;">
          <va-button preset="secondary" size="small" @click="showCreateForm = false">
            {{ $t('cancel') }}
          </va-button>
          <va-button color="success" size="small" :loading="creating" @click="submitWebhook">
            {{ $t('save') }}
          </va-button>
        </div>
      </div>

      <!-- Webhook Table -->
      <va-inner-loading :loading="loading">
        <div style="max-height: 280px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
          <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
            <thead>
              <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                <th style="padding: 0.5rem 0.75rem;">웹훅 명칭</th>
                <th style="padding: 0.5rem 0.75rem;">{{ $t('target_url') }}</th>
                <th style="padding: 0.5rem 0.75rem;">{{ $t('subscribed_events') }}</th>
                <th style="padding: 0.5rem 0.75rem; width: 100px; text-align: center;">연동 테스트</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="w in webhooks"
                :key="w.id"
                style="border-bottom: 1px solid var(--va-background-border);"
              >
                <td style="padding: 0.5rem 0.75rem; font-weight: 700; color: var(--va-primary);">{{ w.name }}</td>
                <td style="padding: 0.5rem 0.75rem; font-family: monospace; font-size: 0.75rem; max-width: 250px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                  {{ w.targetUrl }}
                </td>
                <td style="padding: 0.5rem 0.75rem;">
                  <div style="display: flex; gap: 0.25rem; flex-wrap: wrap;">
                    <va-badge
                      v-for="evt in w.events"
                      :key="evt"
                      :text="evt"
                      color="info"
                      size="small"
                    />
                  </div>
                </td>
                <td style="padding: 0.5rem 0.75rem; text-align: center;">
                  <va-button
                    color="warning"
                    size="small"
                    preset="secondary"
                    :loading="testingId === w.id"
                    @click="testWebhook(w)"
                  >
                    {{ $t('test_webhook') }}
                  </va-button>
                </td>
              </tr>
              <tr v-if="webhooks.length === 0">
                <td colspan="4" style="text-align: center; padding: 2rem; color: var(--va-text-secondary);">
                  {{ $t('no_webhooks') }}
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
  </AppModal>
</template>

<script setup lang="ts">
import { ref, computed, watch, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'
import AppModal from '~/components/common/AppModal.vue'

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

const webhooks = ref<any[]>([])
const loading = ref(false)
const creating = ref(false)
const showCreateForm = ref(false)
const testingId = ref<string | null>(null)

const form = reactive({
  name: '',
  targetUrl: '',
  secretKey: '',
  events: ['RECORD_CREATED', 'RECORD_UPDATED']
})

const fetchWebhooks = async () => {
  loading.value = true
  try {
    const res = await useCustomFetch('/integration/webhooks')
    if (res.data?.value) {
      webhooks.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to fetch webhooks', e)
  } finally {
    loading.value = false
  }
}

const submitWebhook = async () => {
  if (!form.name || !form.targetUrl) return
  creating.value = true
  try {
    const res = await useCustomFetch('/integration/webhooks', {
      method: 'POST',
      body: {
        name: form.name,
        targetUrl: form.targetUrl,
        secretKey: form.secretKey,
        events: form.events
      }
    })
    if (res.data?.value) {
      webhooks.value.unshift(res.data.value)
      showCreateForm.value = false
      form.name = ''
      form.targetUrl = ''
      form.secretKey = ''
    }
  } catch (e: any) {
    console.error('Failed to create webhook', e)
  } finally {
    creating.value = false
  }
}

const testWebhook = async (w: any) => {
  testingId.value = w.id
  try {
    const res = await useCustomFetch(`/integration/webhooks/${w.id}/test`, {
      method: 'POST'
    })
    if (res.data?.value?.success) {
      alert(`[웹훅 테스트 성공]\n서명: ${res.data.value.signatureHeader}\n메시지: ${res.data.value.message}`)
    }
  } catch (e: any) {
    console.error('Failed to test webhook', e)
  } finally {
    testingId.value = null
  }
}

watch(() => props.modelValue, (val) => {
  if (val) fetchWebhooks()
})
</script>
