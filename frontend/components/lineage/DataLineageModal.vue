<template>
  <AppModal
    v-model="show"
    :title="$t('data_lineage')"
    icon="account_tree"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <!-- Guide Alert -->
      <va-alert color="info" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🌐 {{ $t('data_lineage_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div v-if="lineageData" style="display: flex; flex-direction: column; gap: 1.25rem;">
          
          <!-- 1. 5-Stage Pipeline Flow (Stepper View) -->
          <div style="display: flex; flex-direction: column; gap: 0.5rem;">
            <div style="font-weight: 700; font-size: 0.95rem; color: var(--va-text-primary); display: flex; align-items: center; justify-content: space-between;">
              <span>🚀 {{ $t('pipeline_stages') }}</span>
              <va-button
                v-if="selectedStage"
                preset="secondary"
                size="small"
                @click="selectedStage = null"
              >
                전체 보기
              </va-button>
            </div>

            <!-- Stages Horizontal Bar -->
            <div style="display: flex; align-items: stretch; gap: 0.5rem; overflow-x: auto; padding-bottom: 0.5rem;">
              <template v-for="(stageItem, index) in pipelineStages" :key="stageItem.stage">
                <div
                  :style="{
                    flex: '1',
                    minWidth: '130px',
                    padding: '0.65rem 0.75rem',
                    borderRadius: '8px',
                    border: selectedStage === stageItem.stage ? '2px solid var(--va-primary)' : '1px solid var(--va-background-border)',
                    background: selectedStage === stageItem.stage ? 'var(--va-background-primary-light, #f0f4ff)' : 'var(--va-background-element)',
                    cursor: 'pointer',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'space-between',
                    gap: '0.4rem',
                    transition: 'all 0.2s'
                  }"
                  @click="toggleStage(stageItem.stage)"
                >
                  <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span style="font-size: 0.75rem; font-weight: 700; color: var(--va-text-secondary);">
                      STEP {{ stageItem.order }}
                    </span>
                    <va-badge
                      :text="getHealthLabel(stageItem.status)"
                      :color="getHealthColor(stageItem.status)"
                      size="small"
                    />
                  </div>
                  <div style="font-weight: 700; font-size: 0.88rem; color: var(--va-text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                    {{ getStageLabel(stageItem.stage) }}
                  </div>
                  <div style="font-size: 0.75rem; color: var(--va-text-secondary); display: flex; justify-content: space-between;">
                    <span>노드</span>
                    <span style="font-weight: 700;">{{ stageItem.nodeCount }}개</span>
                  </div>
                </div>

                <!-- Arrow between stages -->
                <div
                  v-if="index < pipelineStages.length - 1"
                  style="display: flex; align-items: center; justify-content: center; color: var(--va-text-secondary); font-size: 1rem;"
                >
                  ➔
                </div>
              </template>
            </div>
          </div>

          <!-- 2. Nodes Grid within Pipeline -->
          <div style="display: flex; flex-direction: column; gap: 0.5rem;">
            <div style="font-weight: 700; font-size: 0.95rem; color: var(--va-text-primary);">
              🧩 {{ $t('lineage_nodes') }} ({{ displayedNodes.length }}개):
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 0.75rem; max-height: 220px; overflow-y: auto;">
              <div
                v-for="node in displayedNodes"
                :key="node.id"
                class="lineage-node-card"
                :data-node-id="node.id"
                :style="{
                  padding: '0.75rem',
                  borderRadius: '8px',
                  border: selectedNode?.id === node.id ? '2px solid var(--va-primary)' : '1px solid var(--va-background-border)',
                  background: selectedNode?.id === node.id ? 'var(--va-background-primary-light, #f0f4ff)' : 'var(--va-background-element)',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '0.4rem',
                  cursor: 'pointer',
                  position: 'relative',
                  transition: 'all 0.15s'
                }"
                @click="selectNode(node)"
              >
                <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 0.5rem;">
                  <span style="font-weight: 700; font-size: 0.88rem; color: var(--va-text-primary); line-height: 1.3; word-break: break-word;">
                    {{ node.label }}
                  </span>
                  <div style="display: flex; flex-direction: column; align-items: flex-end; gap: 0.2rem;">
                    <va-badge
                      :text="node.type"
                      :color="getNodeBadgeColor(node.type)"
                      size="small"
                    />
                    <va-badge
                      v-if="node.healthStatus && node.healthStatus !== 'HEALTHY'"
                      :text="getHealthLabel(node.healthStatus)"
                      :color="getHealthColor(node.healthStatus)"
                      size="small"
                    />
                  </div>
                </div>

                <!-- Anomaly Reason Alert Badge -->
                <div
                  v-if="node.anomalyReason"
                  style="font-size: 0.75rem; color: var(--va-danger); background: rgba(239, 68, 68, 0.1); padding: 0.25rem 0.4rem; border-radius: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;"
                  :title="node.anomalyReason"
                >
                  ⚠️ {{ node.anomalyReason }}
                </div>

                <div v-if="node.timestamp" style="font-size: 0.75rem; color: var(--va-text-secondary); margin-top: auto;">
                  🕒 {{ formatWithTimezone(node.timestamp) }}
                </div>
              </div>
            </div>
          </div>

          <!-- 3. Selected Node Detailed Inspection Panel -->
          <div
            v-if="selectedNode"
            style="border: 1px solid var(--va-background-border); border-radius: 8px; padding: 1rem; background: var(--va-background-element); display: flex; flex-direction: column; gap: 0.75rem;"
          >
            <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--va-background-border); padding-bottom: 0.5rem;">
              <div style="display: flex; align-items: center; gap: 0.5rem;">
                <span style="font-weight: 700; font-size: 0.95rem; color: var(--va-text-primary);">
                  🔍 {{ $t('node_detail') }}: {{ selectedNode.label }}
                </span>
                <va-badge
                  :text="getHealthLabel(selectedNode.healthStatus)"
                  :color="getHealthColor(selectedNode.healthStatus)"
                  size="small"
                />
              </div>
              <va-button preset="secondary" size="small" @click="selectedNode = null">
                ✕
              </va-button>
            </div>

            <!-- Anomaly Full Reason -->
            <va-alert v-if="selectedNode.anomalyReason" color="danger" outline style="margin: 0; font-size: 0.82rem;">
              🚨 <strong>{{ $t('anomaly_detected') }}:</strong> {{ selectedNode.anomalyReason }}
            </va-alert>

            <!-- SpEL Field Mapping Rules Table (if available on node) -->
            <div v-if="selectedNode.mappingRules && selectedNode.mappingRules.length > 0" style="display: flex; flex-direction: column; gap: 0.4rem;">
              <div style="font-weight: 700; font-size: 0.88rem; color: var(--va-text-primary);">
                📋 {{ $t('mapping_rules') }} ({{ selectedNode.mappingRules.length }}건)
              </div>
              <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left; border: 1px solid var(--va-background-border); border-radius: 6px; overflow: hidden;">
                <thead>
                  <tr style="background: var(--va-background-card); border-bottom: 1px solid var(--va-background-border);">
                    <th style="padding: 0.4rem 0.6rem;">{{ $t('target_field') }}</th>
                    <th style="padding: 0.4rem 0.6rem;">{{ $t('expression') }}</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(rule, rIdx) in selectedNode.mappingRules" :key="rIdx" style="border-bottom: 1px solid var(--va-background-border);">
                    <td style="padding: 0.4rem 0.6rem; font-weight: 600; color: var(--va-primary);">{{ rule.targetField }}</td>
                    <td style="padding: 0.4rem 0.6rem; font-family: monospace; font-size: 0.78rem;">{{ rule.expression }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Node Details Map (e.g. Changed fields, user, version) -->
            <div v-if="selectedNode.details && Object.keys(selectedNode.details).length > 0" style="display: flex; flex-wrap: wrap; gap: 0.75rem; font-size: 0.82rem;">
              <div v-if="selectedNode.details.changedBy" style="background: var(--va-background-card); padding: 0.35rem 0.6rem; border-radius: 6px; border: 1px solid var(--va-background-border);">
                <span style="color: var(--va-text-secondary);">{{ $t('changed_by') }}:</span>
                <strong style="margin-left: 0.3rem;">{{ selectedNode.details.changedBy }}</strong>
              </div>
              <div v-if="selectedNode.details.version" style="background: var(--va-background-card); padding: 0.35rem 0.6rem; border-radius: 6px; border: 1px solid var(--va-background-border);">
                <span style="color: var(--va-text-secondary);">버전:</span>
                <strong style="margin-left: 0.3rem;">v{{ selectedNode.details.version }}</strong>
              </div>
              <div v-if="selectedNode.details.changedFields && selectedNode.details.changedFields.length > 0" style="background: var(--va-background-card); padding: 0.35rem 0.6rem; border-radius: 6px; border: 1px solid var(--va-background-border);">
                <span style="color: var(--va-text-secondary);">{{ $t('changed_fields') }}:</span>
                <strong style="margin-left: 0.3rem;">{{ selectedNode.details.changedFields.join(', ') }}</strong>
              </div>
              <div v-if="selectedNode.details.channelCode" style="background: var(--va-background-card); padding: 0.35rem 0.6rem; border-radius: 6px; border: 1px solid var(--va-background-border);">
                <span style="color: var(--va-text-secondary);">채널 코드:</span>
                <strong style="margin-left: 0.3rem;">{{ selectedNode.details.channelCode }}</strong>
              </div>
            </div>
          </div>

          <!-- 4. Channel Consumption Summary Table (when available) -->
          <div v-if="lineageData.channelConsumption && lineageData.channelConsumption.length > 0" style="display: flex; flex-direction: column; gap: 0.5rem;">
            <div style="font-weight: 700; font-size: 0.95rem; color: var(--va-text-primary);">
              📊 {{ $t('channel_consumption') }} ({{ lineageData.channelConsumption.length }}개 연계 채널):
            </div>

            <div style="max-height: 160px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
              <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
                <thead>
                  <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                    <th style="padding: 0.4rem 0.6rem;">{{ $t('channel_name') }}</th>
                    <th style="padding: 0.4rem 0.6rem; text-align: center;">{{ $t('total_sent') }}</th>
                    <th style="padding: 0.4rem 0.6rem; text-align: center;">{{ $t('success_count') }}</th>
                    <th style="padding: 0.4rem 0.6rem; text-align: center;">{{ $t('fail_count') }}</th>
                    <th style="padding: 0.4rem 0.6rem;">{{ $t('last_dispatched') }}</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(ch, cIdx) in lineageData.channelConsumption" :key="cIdx" style="border-bottom: 1px solid var(--va-background-border);">
                    <td style="padding: 0.4rem 0.6rem; font-weight: 600;">{{ ch.channelName }}</td>
                    <td style="padding: 0.4rem 0.6rem; text-align: center;">{{ ch.totalSent || 0 }}건</td>
                    <td style="padding: 0.4rem 0.6rem; text-align: center; color: var(--va-success); font-weight: 700;">{{ ch.successCount || 0 }}</td>
                    <td style="padding: 0.4rem 0.6rem; text-align: center; color: var(--va-danger); font-weight: 700;">{{ ch.failCount || 0 }}</td>
                    <td style="padding: 0.4rem 0.6rem; font-size: 0.78rem; color: var(--va-text-secondary);">
                      {{ ch.lastDispatchedAt ? formatWithTimezone(ch.lastDispatchedAt) : '-' }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 5. Lineage Pipeline Edges Flow -->
          <div style="display: flex; flex-direction: column; gap: 0.5rem;">
            <div style="font-weight: 700; font-size: 0.95rem; color: var(--va-text-primary);">
              🔄 {{ $t('lineage_relationships') }} ({{ lineageData.edges?.length || 0 }}개 연계):
            </div>

            <div style="max-height: 160px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
              <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
                <thead>
                  <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                    <th style="padding: 0.4rem 0.6rem;">{{ $t('lineage_edge_source') }}</th>
                    <th style="padding: 0.4rem 0.6rem; width: 140px;">{{ $t('lineage_edge_rel') }}</th>
                    <th style="padding: 0.4rem 0.6rem;">{{ $t('lineage_edge_target') }}</th>
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
const selectedStage = ref<string | null>(null)
const selectedNode = ref<any | null>(null)

const pipelineStages = computed(() => {
  if (lineageData.value?.stages && lineageData.value.stages.length > 0) {
    return lineageData.value.stages
  }
  // Default 5 stages fallback if stages array not populated
  return [
    { stage: 'SOURCE', order: 1, nodeCount: 0, status: 'HEALTHY' },
    { stage: 'INBOUND_PIPELINE', order: 2, nodeCount: 0, status: 'HEALTHY' },
    { stage: 'MASTER_RECORD', order: 3, nodeCount: 0, status: 'HEALTHY' },
    { stage: 'OUTBOUND_PIPELINE', order: 4, nodeCount: 0, status: 'HEALTHY' },
    { stage: 'DOWNSTREAM_CONSUMER', order: 5, nodeCount: 0, status: 'HEALTHY' }
  ]
})

const displayedNodes = computed(() => {
  if (!lineageData.value?.nodes) return []
  if (!selectedStage.value) return lineageData.value.nodes
  return lineageData.value.nodes.filter((n: any) => n.stage === selectedStage.value)
})

const toggleStage = (stageKey: string) => {
  if (selectedStage.value === stageKey) {
    selectedStage.value = null
  } else {
    selectedStage.value = stageKey
  }
}

const selectNode = (node: any) => {
  selectedNode.value = selectedNode.value?.id === node.id ? null : node
}

const getStageLabel = (stageKey: string) => {
  switch (stageKey) {
    case 'SOURCE': return t('stage_source')
    case 'INBOUND_PIPELINE': return t('stage_inbound')
    case 'MASTER_RECORD': return t('stage_master')
    case 'OUTBOUND_PIPELINE': return t('stage_outbound')
    case 'DOWNSTREAM_CONSUMER': return t('stage_consumer')
    default: return stageKey
  }
}

const getHealthColor = (status?: string) => {
  switch (status) {
    case 'ERROR': return 'danger'
    case 'WARNING': return 'warning'
    case 'HEALTHY': return 'success'
    default: return 'success'
  }
}

const getHealthLabel = (status?: string) => {
  switch (status) {
    case 'ERROR': return t('health_error')
    case 'WARNING': return t('health_warning')
    case 'HEALTHY': return t('health_healthy')
    default: return t('health_healthy')
  }
}

const getNodeBadgeColor = (type: string) => {
  switch (type) {
    case 'SOURCE': return 'info'
    case 'INBOUND': return 'warning'
    case 'RECORD_VERSION': return 'secondary'
    case 'RECORD': return 'primary'
    case 'OUTBOUND': return 'success'
    case 'CONSUMER': return 'purple'
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
  selectedNode.value = null
  selectedStage.value = null
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
}, { immediate: true })
</script>
