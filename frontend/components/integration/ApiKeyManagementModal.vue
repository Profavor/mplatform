<template>
  <AppModal
    v-model="show"
    :title="$t('api_key_mgmt')"
    icon="vpn_key"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🔑 {{ $t('api_key_mgmt_desc') }}
      </va-alert>

      <!-- New Key Creation Section -->
      <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; flex-direction: column; gap: 0.75rem;">
        <div style="font-weight: 700; font-size: 0.85rem;">{{ $t('issue_api_key') }}</div>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem;">
          <va-input v-model="newKeyName" :label="$t('key_name')" placeholder="예: ERP 마스터 연계 키" />
          <va-input v-model="newAllowedIps" :label="$t('allowed_ips')" placeholder="예: 10.0.0.0/8" />
        </div>
        <div style="display: flex; justify-content: flex-end;">
          <va-button color="success" size="small" :loading="creating" @click="createKey">
            {{ $t('issue_api_key') }}
          </va-button>
        </div>
      </div>

      <!-- Created Key Modal/Alert Banner -->
      <div v-if="createdRawKey" style="padding: 0.75rem 1rem; border-radius: 8px; background: rgba(33, 150, 243, 0.1); border: 1px dashed var(--va-primary); display: flex; flex-direction: column; gap: 0.3rem;">
        <span style="font-size: 0.8rem; font-weight: 700; color: var(--va-primary);">발급된 API 원본 Key (1회만 표시됨):</span>
        <code style="font-size: 0.85rem; background: var(--va-background-element); padding: 0.3rem 0.5rem; border-radius: 4px; user-select: all;">{{ createdRawKey }}</code>
      </div>

      <!-- Key List Table -->
      <va-inner-loading :loading="loading">
        <div v-if="apiKeys?.length > 0" style="max-height: 250px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
          <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
            <thead>
              <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                <th style="padding: 0.5rem 0.75rem;">키 명칭</th>
                <th style="padding: 0.5rem 0.75rem;">마스킹 Key</th>
                <th style="padding: 0.5rem 0.75rem;">{{ $t('permission_scopes') }}</th>
                <th style="padding: 0.5rem 0.75rem; width: 80px; text-align: center;">폐기</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="k in apiKeys"
                :key="k.keyId"
                style="border-bottom: 1px solid var(--va-background-border);"
              >
                <td style="padding: 0.5rem 0.75rem; font-weight: 700;">{{ k.name }}</td>
                <td style="padding: 0.5rem 0.75rem; font-family: monospace; font-size: 0.75rem; color: var(--va-text-secondary);">
                  {{ k.maskedKey }}
                </td>
                <td style="padding: 0.5rem 0.75rem;">
                  <va-badge
                    v-for="s in k.scopes"
                    :key="s"
                    :text="s"
                    color="info"
                    size="small"
                    style="margin-right: 0.25rem;"
                  />
                </td>
                <td style="padding: 0.5rem 0.75rem; text-align: center;">
                  <va-button
                    color="danger"
                    size="small"
                    preset="secondary"
                    @click="revokeKey(k.keyId)"
                  >
                    {{ $t('revoke_key') }}
                  </va-button>
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
import { ref, computed, watch } from 'vue'
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

const apiKeys = ref<any[]>([])
const loading = ref(false)
const creating = ref(false)
const newKeyName = ref('')
const newAllowedIps = ref('')
const createdRawKey = ref<string | null>(null)

const loadKeys = async () => {
  loading.value = true
  try {
    const res = await useCustomFetch('/integration/api-keys')
    if (res.data?.value) {
      apiKeys.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to load api keys', e)
  } finally {
    loading.value = false
  }
}

const createKey = async () => {
  if (!newKeyName.value.trim()) return
  creating.value = true
  try {
    const res = await useCustomFetch('/integration/api-keys', {
      method: 'POST',
      body: {
        name: newKeyName.value.trim(),
        allowedIpsCsv: newAllowedIps.value.trim(),
        scopes: ['record:read', 'record:write'],
        validDays: 365
      }
    })
    if (res.data?.value) {
      createdRawKey.value = res.data.value.rawApiKey
      newKeyName.value = ''
      newAllowedIps.value = ''
      await loadKeys()
    }
  } catch (e: any) {
    console.error('Failed to create API key', e)
  } finally {
    creating.value = false
  }
}

const revokeKey = async (keyId: string) => {
  if (!confirm(t('confirm_revoke_key'))) return
  try {
    const res = await useCustomFetch(`/integration/api-keys/${keyId}`, {
      method: 'DELETE'
    })
    if (res.data?.value) {
      await loadKeys()
    }
  } catch (e: any) {
    console.error('Failed to revoke API key', e)
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    createdRawKey.value = null
    loadKeys()
  }
})
</script>
