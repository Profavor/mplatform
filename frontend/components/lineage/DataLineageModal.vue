<template>
  <AppModal
    v-model="show"
    :title="$t('data_lineage')"
    icon="account_tree"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="info" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🌐 {{ $t('data_lineage_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <!-- Interactive Lineage Graph Container -->
        <div v-if="lineageData" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Lineage Nodes Grid -->
          <div style="font-weight: 700; font-size: 0.95rem; color: var(--va-text-primary);">
            🧩 {{ $t('lineage_nodes') }} ({{ lineageData.nodes?.length || 0 }}개):
          </div>

          <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 0.75rem; max-height: 220px; overflow-y: auto;">
            <div
              v-for="node in lineageData.nodes"
              :key="node.id"
              style="padding: 0.75rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; flex-direction: column; gap: 0.35rem;"
            >
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span style="font-weight: 700; font-size: 0.88rem; color: var(--va-text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                  {{ node.label }}
                </span>
                <va-badge
                  :text="node.type"
                  :color="getNodeBadgeColor(node.type)"
                  size="small"
                />
              </div>
              <div v-if="node.timestamp" style="font-size: 0.75rem; color: var(--va-text-secondary);">
                {{ formatWithTimezone(node.timestamp) }}
              </div>
            </div>
          </div>

          <!-- Lineage Pipeline Edges Flow -->
          <div style="font-weight: 700; font-size: 0.95rem; color: var(--va-text-primary); margin-top: 0.5rem;">
            🔄 {{ $t('lineage_relationships') }} ({{ lineageData.edges?.length || 0 }}개 파이프라인):
          </div>

          <div style="max-height: 180px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.4rem 0.6rem;">출발지 (Source)</th>
                  <th style="padding: 0.4rem 0.6rem; width: 140px;">연계 관계</th>
                  <th style="padding: 0.4rem 0.6rem;">도착지 (Target)</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(edge, idx) in lineageData.edges" :key="idx" style="border-bottom: 1px solid var(--va-background-border);">
                  <td style="padding: 0.4rem 0.6rem; font-weight: 600;">{{ resolveNodeLabel(edge.source) }}</td>
                  <td style="padding: 0.4rem 0.6rem;">
                    <va-badge
                      :text="edge.relationship"
                      color="secondary"
                      outline
                      size="small"
                    />
                  </td>
                  <td style="padding: 0.4rem 0.6rem; font-weight: 600; color: var(--va-primary);">
                    {{ resolveNodeLabel(edge.target) }}
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
  </AppModal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { formatWithTimezone } from '~/composables/useTimezoneDate'
import AppModal from '~/components/common/AppModal.vue'

const props = defineProps<{
  modelValue: boolean
  domainId?: string
  recordId?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const lineageData = ref<any>(null)
const loading = ref(false)

const getNodeBadgeColor = (type: string) => {
  switch (type) {
    case 'DOMAIN': return 'primary'
    case 'NODE': return 'info'
    case 'CHANNEL': return 'warning'
    case 'RECORD': return 'success'
    default: return 'secondary'
  }
}

const resolveNodeLabel = (nodeId: string) => {
  if (!lineageData.value?.nodes) return nodeId
  const node = lineageData.value.nodes.find((n: any) => n.id === nodeId)
  return node ? node.label : nodeId
}

const fetchLineage = async () => {
  if (!props.domainId && !props.recordId) return
  loading.value = true
  try {
    const url = props.domainId
      ? `/lineage/domains/${props.domainId}`
      : `/lineage/records/${props.recordId}`
    const res = await useCustomFetch(url)
    if (res.data?.value) {
      lineageData.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to fetch lineage data', e)
  } finally {
    loading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) fetchLineage()
})
</script>
