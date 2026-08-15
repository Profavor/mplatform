<template>
  <va-modal
    v-model="show"
    :title="$t('multi_tenant')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🏢 {{ $t('multi_tenant_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="routingData" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Summary Banner -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center;">
            <span style="font-weight: 700; font-size: 0.85rem;">{{ routingData.summary }}</span>
            <va-badge
              :text="'활성 테넌트: ' + routingData.activeTenants + ' / ' + routingData.totalTenants"
              color="success"
              size="small"
            />
          </div>

          <!-- Rules Table -->
          <div style="max-height: 270px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.5rem 0.75rem;">{{ $t('tenant_name') }}</th>
                  <th style="padding: 0.5rem 0.75rem; width: 120px;">{{ $t('partition_type') }}</th>
                  <th style="padding: 0.5rem 0.75rem;">{{ $t('routing_expression') }}</th>
                  <th style="padding: 0.5rem 0.75rem; text-align: center; width: 80px;">상태</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(rule, idx) in routingData.rules"
                  :key="idx"
                  style="border-bottom: 1px solid var(--va-background-border);"
                >
                  <td style="padding: 0.5rem 0.75rem; font-weight: 700;">
                    <div>{{ rule.tenantName }}</div>
                    <span style="font-size: 0.72rem; color: var(--va-text-secondary); font-family: monospace;">{{ rule.tenantCode }} ({{ rule.targetDomainCount }}개 도메인)</span>
                  </td>
                  <td style="padding: 0.5rem 0.75rem;">
                    <va-badge :text="rule.partitionType" color="info" size="small" />
                  </td>
                  <td style="padding: 0.5rem 0.75rem; font-family: monospace; font-size: 0.75rem; color: var(--va-text-primary);">
                    {{ rule.expression }}
                  </td>
                  <td style="padding: 0.5rem 0.75rem; text-align: center;">
                    <va-switch
                      v-model="rule.active"
                      size="small"
                      @update:model-value="toggle(rule)"
                    />
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
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()
const { customFetch } = useCustomFetch()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const routingData = ref<any>(null)
const loading = ref(false)

const loadRouting = async () => {
  loading.value = true
  try {
    const res = await customFetch('/api/admin/multi-tenant/routing-rules')
    const payload = res?.rules ? res : res?.data?.value
    if (payload) {
      routingData.value = payload
    }
  } catch (e: any) {
    console.error('Failed to load multi-tenant routing rules', e)
  } finally {
    loading.value = false
  }
}

const toggle = async (rule: any) => {
  try {
    await customFetch(`/api/admin/multi-tenant/routing-rules/${rule.tenantCode}/toggle?active=${rule.active}`, {
      method: 'POST'
    })
  } catch (e: any) {
    console.error('Failed to toggle multi-tenant rule', e)
    rule.active = !rule.active
  }
}

watch(() => props.modelValue, (val) => {
  if (val) loadRouting()
}, { immediate: true })
</script>
