<template>
  <va-modal
    v-model="show"
    :title="$t('data_lineage_title') || '마스터 데이터 생애주기 & 계보 (Data Lineage)'"
    size="large"
    hide-default-actions
  >
    <div style="padding: 0.5rem; display: flex; flex-direction: column; gap: 1.25rem;">
      <div v-if="loading" style="display: flex; justify-content: center; align-items: center; padding: 2rem;">
        <va-progress-circle indeterminate size="large" />
      </div>

      <template v-else-if="lineageData">
        <div style="display: flex; align-items: center; justify-content: space-between; background: var(--va-background-element); padding: 0.75rem 1rem; border-radius: 8px;">
          <div>
            <span style="font-weight: 700; font-size: 1.1rem; color: var(--va-primary);">
              {{ formattedRecordTitle }}
            </span>
            <span style="margin-left: 0.5rem; font-size: 0.85rem; color: var(--va-text-secondary);">
              ({{ $t('total_nodes') || 'Total Lineage Nodes' }}: {{ lineageData.nodes.length }}{{ $t('node_count_suffix') }})
            </span>
          </div>

          <!-- View Mode Switcher (Modern Premium Segmented Control) -->
          <div style="display: flex; align-items: center; gap: 0.75rem;">
            <div style="display: inline-flex; background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 20px; padding: 3px; gap: 3px;">
              <button
                type="button"
                :class="['lineage-tab-btn', { active: viewMode === 'graph' }]"
                @click="viewMode = 'graph'"
              >
                <span>🕸️</span> {{ $t('visual_graph') || '비주얼 그래프' }}
              </button>
              <button
                type="button"
                :class="['lineage-tab-btn', { active: viewMode === 'timeline' }]"
                @click="viewMode = 'timeline'"
              >
                <span>📜</span> {{ $t('timeline_list') || '타임라인 리스트' }}
              </button>
            </div>
            <va-badge text="Data Governance Verified" color="success" size="small" />
          </div>
        </div>

        <!-- Node type descriptions guide for Graph View -->
        <va-alert v-if="viewMode === 'graph'" color="info" outline style="width: 100%; box-sizing: border-box; font-size: 0.82rem; line-height: 1.5; margin: 0;">
          💡 <b>{{ $t('lineage_guide_title') || '데이터 계보(Data Lineage) 시각화 안내' }}</b><br/>
          • {{ $t('lineage_guide_zoom') || '그래프 노드 줌/드래그: 마우스 휠로 확대/축소 및 노드 클릭 시 변경이력 상세 비교 가능' }}<br/>
          • {{ $t('lineage_guide_flow') || '파이프라인 흐름: 소스 시스템(파랑) → 변경 이력(주황) → Golden Master Record(보라) → 외부 연계(초록)' }}
        </va-alert>

        <!-- Node type descriptions guide for Timeline View -->
        <va-alert v-else-if="viewMode === 'timeline'" color="info" outline style="width: 100%; box-sizing: border-box; font-size: 0.82rem; line-height: 1.5; margin: 0;">
          💡 <b>{{ $t('lineage_timeline_guide_title') || '데이터 계보(Data Lineage) 노드 구별 안내' }}</b><br/>
          • {{ $t('lineage_node_source_desc') || '소스 시스템: 최초 데이터가 유입/생성된 원출처 시스템' }}<br/>
          • {{ $t('lineage_node_history_desc') || '변경 이력: 최초 생성(Version 1)부터 정보 변경(Version 2...)까지의 시간순 이력 현황' }}<br/>
          • {{ $t('lineage_node_master_desc') || '마스터 레코드: 여러 이력을 거쳐 최종 통합 관리되고 있는 최신 Golden Record' }}<br/>
          • {{ $t('lineage_node_outbound_desc') || '외부 연계: 해당 마스터 레코드가 외부 타 시스템으로 전파된 연계 이력 현황' }}
        </va-alert>

        <!-- ECharts Visual Graph View -->
        <div v-if="viewMode === 'graph'" style="position: relative; background: var(--va-background-element); border-radius: 8px; border: 1px solid var(--va-background-border); padding: 0.5rem; min-height: 400px;">
          <v-chart
            :option="graphChartOption"
            style="height: 400px; width: 100%;"
            autoresize
            @click="onChartClick"
          />
        </div>

        <!-- Node timeline / list -->
        <div v-else-if="viewMode === 'timeline'" style="display: flex; flex-direction: column; gap: 0.75rem; max-height: 420px; overflow-y: auto;">
          <va-card
            v-for="node in lineageData.nodes"
            :key="node.id"
            flat
            bordered
            style="border-left: 4px solid var(--va-primary);"
          >
            <va-card-content style="display: flex; justify-content: space-between; align-items: center; padding: 0.85rem;">
              <div style="flex: 1;">
                <div style="display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap;">
                  <va-chip size="small" :color="getNodeTypeColor(node.type)">{{ getNodeTypeName(node.type) }}</va-chip>
                  <span style="font-weight: 600; font-size: 0.95rem;">{{ formatNodeLabel(node) }}</span>
                </div>

                <!-- Timestamp -->
                <div style="font-size: 0.82rem; color: var(--va-primary); margin-top: 0.35rem; font-weight: 500; display: flex; align-items: center; gap: 0.35rem;">
                  <va-icon name="schedule" size="14px" color="primary" /> {{ formatDate(node.timestamp) }}
                </div>

                <div v-if="hasSimpleDetails(node.details)" style="display: flex; gap: 0.4rem; flex-wrap: wrap; margin-top: 0.5rem;">
                  <span v-for="(val, key) in getSimpleDetails(node.details)" :key="key" style="font-size: 0.78rem; color: var(--va-text-primary); background: var(--va-background-element); padding: 2px 8px; border-radius: 4px; border: 1px solid var(--va-background-border);">
                    <b>{{ getDetailLabel(String(key)) }}:</b> {{ val }}
                  </span>
                </div>
              </div>

              <!-- Action Button -->
              <div style="display: flex; align-items: center; gap: 0.5rem;">
                <va-button
                  v-if="node.type === 'RECORD_VERSION' || node.details?.newData || node.details?.previousData"
                  size="small"
                  color="primary"
                  icon="compare_arrows"
                  @click="openNodeDiff(node)"
                >
                  {{ $t('compare_changes') || '변경 내역 비교' }}
                </va-button>
                <va-icon v-else name="account_tree" color="primary" />
              </div>
            </va-card-content>
          </va-card>
        </div>
      </template>

      <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem;">
        <va-button preset="secondary" @click="show = false">{{ $t('close') || '닫기' }}</va-button>
      </div>
    </div>

    <!-- Node Diff Detail Modal (Tabular Diff View) -->
    <va-modal v-model="showDiffModal" :title="$t('history_version_diff_detail') || '이력 버전 변경 항목 상세 비교'" size="large" hide-default-actions>
      <div style="padding: 0.5rem; max-height: 65vh; overflow-y: auto;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; background: var(--va-background-element); padding: 0.75rem 1rem; border-radius: 8px;">
          <div>
            <h4 style="font-weight: 700; margin: 0; color: var(--va-primary);">
              {{ selectedNode?.label }}
            </h4>
            <span style="font-size: 0.85rem; color: var(--va-text-secondary);">
              {{ $t('changed_by') || '작성자/변경자' }}: <b>{{ selectedNode?.details?.changedBy || 'System Admin' }}</b> | {{ $t('date_time') || '일시' }}: {{ formatDate(selectedNode?.timestamp) }}
            </span>
          </div>
          <va-badge text="Diff Table" color="info" />
        </div>

        <!-- Table View -->
        <table style="width: 100%; border-collapse: collapse; font-size: 0.88rem; text-align: left;">
          <thead>
            <tr style="background: var(--va-background-element); border-bottom: 2px solid var(--va-background-border);">
              <th style="padding: 0.6rem 0.8rem; width: 30%;">{{ $t('field_name') || '속성 / 필드명' }}</th>
              <th style="padding: 0.6rem 0.8rem; width: 35%; color: var(--va-danger);">{{ $t('before_change') || '변경 전 (Previous Value)' }}</th>
              <th style="padding: 0.6rem 0.8rem; width: 35%; color: var(--va-success);">{{ $t('after_change') || '변경 후 (New Value)' }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="diffRows.length === 0">
              <td colspan="3" style="text-align: center; padding: 2rem; color: var(--va-text-secondary);">
                {{ $t('no_diff_or_initial_version') || '변경 사항이 없거나 최초 생성 버전입니다.' }}
              </td>
            </tr>
            <tr v-for="row in diffRows" :key="row.key" style="border-bottom: 1px solid var(--va-background-border);">
              <td style="padding: 0.6rem 0.8rem; font-weight: 600; color: var(--va-text-primary);">
                {{ getFieldLabel(row.key) }}
              </td>
              <td style="padding: 0.6rem 0.8rem; color: #b91c1c; background: rgba(239, 68, 68, 0.04);">
                <template v-if="isFieldEncrypted(row.key)">
                  <span v-if="row.before === $t('none') || row.before === '(없음)'">{{ row.before }}</span>
                  <span v-else>{{ decryptedValues[row.key]?.before || row.before }}</span>
                </template>
                <template v-else>{{ row.before }}</template>
              </td>
              <td style="padding: 0.6rem 0.8rem; color: #15803d; background: rgba(34, 197, 94, 0.04); font-weight: 600;">
                <div style="display:flex; align-items:center; justify-content:space-between;">
                  <template v-if="isFieldEncrypted(row.key)">
                    <span>
                      <template v-if="row.after === $t('none') || row.after === '(없음)'">{{ row.after }}</template>
                      <template v-else>{{ decryptedValues[row.key]?.after || row.after }}</template>
                    </span>
                    <span v-if="(row.before !== $t('none') && row.before !== '(없음)') || (row.after !== $t('none') && row.after !== '(없음)')" style="margin-left:8px; display:inline-flex; align-items:center; gap:4px; font-size:0.75rem; color:#888;">
                      <va-icon name="lock" size="small" />
                      <template v-if="!decryptedValues[row.key]">
                        <span style="cursor:pointer; text-decoration:underline; color:var(--va-primary); font-weight:normal;" @click.stop="requestDecrypt(row.key)">
                          {{ $t('view_original') || '원본 보기' }}
                        </span>
                        <va-icon v-if="decryptingFields[row.key]" name="sync" size="small" spin />
                      </template>
                      <template v-else>
                        <span style="cursor:pointer; text-decoration:underline; color:var(--va-primary); font-weight:normal;" @click.stop="hideDecrypt(row.key)">
                          {{ $t('hide_original') || '원본 숨기기' }}
                        </span>
                        <span v-if="decryptRemainingTime[row.key]" style="margin-left:4px; font-variant-numeric: tabular-nums;">
                          (00:{{ String(decryptRemainingTime[row.key]).padStart(2, '0') }})
                        </span>
                      </template>
                    </span>
                  </template>
                  <template v-else>{{ row.after }}</template>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <div style="display: flex; justify-content: flex-end; margin-top: 1.25rem;">
          <va-button preset="secondary" @click="showDiffModal = false">닫기</va-button>
        </div>
      </div>
    </va-modal>

    <UnmaskReasonModal
      v-model="showUnmaskReasonModal"
      @confirm="executePendingDecrypt"
    />
  </va-modal>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { formatWithTimezone } from '~/composables/useTimezoneDate'
import { useCookie } from '#app'
import UnmaskReasonModal from './UnmaskReasonModal.vue'

const props = defineProps<{
  modelValue: boolean
  recordId: string | number | null
  fields?: Array<any>
}>()

const emit = defineEmits(['update:modelValue'])

const { t, te, locale } = useI18n()
const { customFetch } = useCustomFetch()

const getTranslatedNameFromObj = (nameObj: any): string => {
  if (!nameObj) return ''
  if (typeof nameObj === 'string') return nameObj
  if (typeof nameObj === 'object') {
    const loc = locale?.value || 'ko'
    return nameObj[loc] || nameObj.ko || nameObj.en || Object.values(nameObj)[0] || ''
  }
  return String(nameObj)
}

const getFieldLabel = (key: string) => {
  if (!key) return ''
  
  // 1. Prioritize frontend AG-Grid schema fields metadata from props (No backend overhead)
  if (props.fields && props.fields.length > 0) {
    const f = props.fields.find((field: any) =>
      field.key === key ||
      String(field.id) === String(key) ||
      (field.key && String(field.key).toLowerCase() === String(key).toLowerCase())
    )
    if (f && f.name) {
      const translated = getTranslatedNameFromObj(f.name)
      if (translated) return translated
    }
  }

  // 2. Fallback to API fieldLabels metadata
  if (lineageData.value?.fieldLabels && lineageData.value.fieldLabels[key]) {
    const labelObj = lineageData.value.fieldLabels[key]
    const translated = getTranslatedNameFromObj(labelObj)
    if (translated) return translated
  }

  return key
}

const formattedRecordTitle = computed(() => {
  if (!lineageData.value) return ''
  
  const nameVal = lineageData.value.recordNameObj
    ? getTranslatedNameFromObj(lineageData.value.recordNameObj)
    : null
  
  const empNo = lineageData.value.empNo
  const empLabel = getFieldLabel('EMP_NO') || (locale?.value === 'en' ? 'Employee ID' : '사번')

  if (nameVal && empNo) {
    return `${nameVal} (${empLabel}: ${empNo})`
  } else if (empNo) {
    return `${empLabel}: ${empNo}`
  } else if (nameVal) {
    return nameVal
  }
  
  return lineageData.value.recordCode || `REC-${lineageData.value.recordId}`
})

const formatNodeLabel = (node: any) => {
  if (!node) return ''
  if (node.type === 'RECORD') {
    return `Golden Master Record (${formattedRecordTitle.value})`
  }
  return node.label
}

const show = ref(props.modelValue)
const loading = ref(false)
const lineageData = ref<any>(null)
const viewMode = ref<'graph' | 'timeline'>('graph')

const showDiffModal = ref(false)
const selectedNode = ref<any>(null)

const decryptedValues = ref<Record<string, any>>({})
const decryptingFields = ref<Record<string, boolean>>({})
const decryptRemainingTime = ref<Record<string, number>>({})
const decryptTimers = ref<Record<string, any>>({})
const decryptIntervals = ref<Record<string, any>>({})

const isFieldEncrypted = (key: string) => {
  const f = props.fields?.find((f: any) => f.key === key || f.id === key)
  return !!f?.isEncrypted
}

const showUnmaskReasonModal = ref(false)
const pendingDecryptKey = ref<string | null>(null)

const requestDecrypt = (key: string) => {
  if (!selectedNode.value || !selectedNode.value.id) return
  pendingDecryptKey.value = key
  showUnmaskReasonModal.value = true
}

const executePendingDecrypt = async (reason: string) => {
  const key = pendingDecryptKey.value
  if (!key) return
  
  decryptingFields.value[key] = true
  try {
    const historyId = String(selectedNode.value.id).replace('HIST-', '')
    const token = useCookie('auth_token').value
    const res = await $fetch(`/api/sensitive-data/history/${historyId}/decrypt`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` },
      body: { fieldKeys: [key], accessReason: reason }
    })
    if (res && res[key]) {
      decryptedValues.value[key] = { after: res[key], before: res[key] } // Set both before and after if decrypted since backend only returns newData
      
      if (decryptTimers.value[key]) clearTimeout(decryptTimers.value[key])
      if (decryptIntervals.value[key]) clearInterval(decryptIntervals.value[key])
      
      decryptRemainingTime.value[key] = 30
      
      decryptIntervals.value[key] = setInterval(() => {
        if (decryptRemainingTime.value[key] > 0) {
          decryptRemainingTime.value[key]--
        }
      }, 1000)

      decryptTimers.value[key] = setTimeout(() => {
        hideDecrypt(key)
      }, 30000)
    }
  } catch (e) {
    console.error(e)
  } finally {
    decryptingFields.value[key] = false
  }
}

const hideDecrypt = (key: string) => {
  if (decryptTimers.value[key]) {
    clearTimeout(decryptTimers.value[key])
    delete decryptTimers.value[key]
  }
  if (decryptIntervals.value[key]) {
    clearInterval(decryptIntervals.value[key])
    delete decryptIntervals.value[key]
  }
  delete decryptRemainingTime.value[key]
  delete decryptedValues.value[key]
}

watch(() => showDiffModal.value, (val) => {
  if (!val) {
    for (const k of Object.keys(decryptedValues.value)) {
      hideDecrypt(k)
    }
    decryptedValues.value = {}
    decryptingFields.value = {}
  }
})

const graphChartOption = computed(() => {
  if (!lineageData.value || !lineageData.value.nodes) return {}

  const rawNodes = lineageData.value.nodes || []
  const rawEdges = lineageData.value.edges || []

  const sourceNodes = rawNodes.filter((n: any) => n.type === 'SOURCE')
  const versionNodes = rawNodes.filter((n: any) => n.type === 'RECORD_VERSION')
  const recordNodes = rawNodes.filter((n: any) => n.type === 'RECORD')
  const outboundNodes = rawNodes.filter((n: any) => n.type === 'OUTBOUND')

  const categories = [
    { name: t('source_system_node') || (locale?.value === 'en' ? 'Source System' : '소스 시스템'), itemStyle: { color: '#2563eb' } },
    { name: t('record_version_node') || (locale?.value === 'en' ? 'Version History' : '변경 이력'), itemStyle: { color: '#f59e0b' } },
    { name: t('master_record_node') || (locale?.value === 'en' ? 'Master Record' : '마스터 레코드'), itemStyle: { color: '#7c3aed' } },
    { name: t('outbound_node') || (locale?.value === 'en' ? 'Outbound Integration' : '외부 연계'), itemStyle: { color: '#10b981' } }
  ]

  const timeLabel = t('date_time') || (locale?.value === 'en' ? 'Date & Time' : '일시')
  const authorLabel = t('changed_by') || (locale?.value === 'en' ? 'Author / Modifier' : '작성자/변경자')
  const statusLabel = t('status') || (locale?.value === 'en' ? 'Status' : '상태')
  const clickHint = locale?.value === 'en' ? '💡 Click node for detailed diff' : '💡 클릭 시 변경 내역 비교 모달 오픈'

  const nodes: any[] = []

  // 1. Source Nodes (x: 80)
  sourceNodes.forEach((n: any, idx: number) => {
    nodes.push({
      id: n.id,
      name: n.label,
      rawNode: n,
      category: 0,
      x: 80,
      y: 180 + idx * 90,
      symbolSize: 55,
      symbol: 'roundRect',
      itemStyle: { color: '#2563eb', borderColor: '#1d4ed8', borderWidth: 2 },
      label: { show: true, position: 'bottom', fontSize: 11, fontWeight: 'bold' },
      tooltip: { formatter: `<b>${n.label}</b><br/>${timeLabel}: ${formatDate(n.timestamp)}` }
    })
  })

  // 2. Record Version Nodes (x: 240, 390, 540...)
  versionNodes.forEach((n: any, idx: number) => {
    const xPos = 240 + idx * 150
    nodes.push({
      id: n.id,
      name: n.label,
      rawNode: n,
      category: 1,
      x: xPos,
      y: 180 + (idx % 2 === 0 ? 0 : 35),
      symbolSize: 48,
      symbol: 'circle',
      itemStyle: { color: '#f59e0b', borderColor: '#d97706', borderWidth: 2 },
      label: { show: true, position: 'top', fontSize: 11 },
      tooltip: { formatter: `<b>${n.label}</b><br/>${authorLabel}: ${n.details?.changedBy || 'System Admin'}<br/>${timeLabel}: ${formatDate(n.timestamp)}<br/><span style="color:#f59e0b;font-weight:bold;">${clickHint}</span>` }
    })
  })

  // 3. Golden Master Record Node (x: lastVersion + 170)
  const lastVersionX = 240 + Math.max(versionNodes.length - 1, 0) * 150
  const recordX = Math.max(lastVersionX + 170, 480)
  recordNodes.forEach((n: any, idx: number) => {
    const nodeTitle = formatNodeLabel(n)
    nodes.push({
      id: n.id,
      name: nodeTitle,
      rawNode: n,
      category: 2,
      x: recordX,
      y: 180 + idx * 90,
      symbolSize: 62,
      symbol: 'diamond',
      itemStyle: { color: '#7c3aed', borderColor: '#5b21b6', borderWidth: 3 },
      label: { show: true, position: 'bottom', fontSize: 12, fontWeight: 'bold' },
      tooltip: { formatter: `<b>${nodeTitle}</b><br/>${statusLabel}: ${n.details?.status || 'ACTIVE'}<br/>${timeLabel}: ${formatDate(n.timestamp)}` }
    })
  })

  // 4. Outbound Integration Nodes (x: recordX + 170)
  const outboundX = recordX + 170
  outboundNodes.forEach((n: any, idx: number) => {
    nodes.push({
      id: n.id,
      name: n.label,
      rawNode: n,
      category: 3,
      x: outboundX,
      y: 140 + idx * 100,
      symbolSize: 52,
      symbol: 'rect',
      itemStyle: { color: '#10b981', borderColor: '#047857', borderWidth: 2 },
      label: { show: true, position: 'right', fontSize: 11 },
      tooltip: { formatter: `<b>${n.label}</b><br/>${statusLabel}: ${n.details?.status || 'SUCCESS'}<br/>${timeLabel}: ${formatDate(n.timestamp)}` }
    })
  })

  const getRelationshipLabel = (rel: string) => {
    if (!rel) return ''
    if (te(rel)) return t(rel)
    const relKey = `rel_${rel}`
    if (te(relKey)) return t(relKey)
    return rel
  }

  const links = rawEdges.map((e: any) => ({
    source: e.source,
    target: e.target,
    value: getRelationshipLabel(e.relationship),
    label: { show: true, formatter: getRelationshipLabel(e.relationship), fontSize: 9, color: '#64748b' },
    lineStyle: { width: 2.5, curveness: 0.12, color: '#94a3b8' }
  }))

  return {
    tooltip: { trigger: 'item', backgroundColor: 'rgba(15, 23, 42, 0.9)', textStyle: { color: '#fff' } },
    legend: {
      data: categories.map(c => c.name),
      top: 5,
      textStyle: { color: 'var(--va-text-primary)' }
    },
    series: [
      {
        type: 'graph',
        layout: 'none',
        categories: categories,
        data: nodes,
        links: links,
        roam: true,
        label: { show: true },
        edgeSymbol: ['circle', 'arrow'],
        edgeSymbolSize: [4, 10],
        lineStyle: { opacity: 0.9, width: 2, curveness: 0.12 },
        emphasis: { focus: 'adjacency', lineStyle: { width: 4 } }
      }
    ]
  }
})

const onChartClick = (params: any) => {
  if (params.dataType === 'node' && params.data?.rawNode) {
    const node = params.data.rawNode
    if (node.type === 'RECORD_VERSION' || node.details?.newData || node.details?.previousData) {
      openNodeDiff(node)
    }
  }
}

watch(() => props.modelValue, (val) => {
  show.value = val
  if (val && props.recordId) {
    fetchLineage()
  }
})

watch(show, (val) => {
  emit('update:modelValue', val)
})

const fetchLineage = async () => {
  if (!props.recordId) return
  loading.value = true
  try {
    const res = await customFetch(`/api/records/${props.recordId}/lineage`)
    lineageData.value = res
  } catch (e) {
    console.error('Failed to fetch lineage:', e)
  } finally {
    loading.value = false
  }
}

const getNodeTypeColor = (type: string) => {
  switch (type) {
    case 'SOURCE': return 'info'
    case 'RECORD': return 'primary'
    case 'RECORD_VERSION': return 'warning'
    case 'OUTBOUND': return 'success'
    default: return 'secondary'
  }
}

const getNodeTypeName = (type: string) => {
  switch (type) {
    case 'SOURCE': return t('source_system_node') || (locale?.value === 'en' ? 'Source System' : '소스 시스템')
    case 'RECORD': return t('master_record_node') || (locale?.value === 'en' ? 'Master Record' : '마스터 레코드')
    case 'RECORD_VERSION': return t('record_version_node') || (locale?.value === 'en' ? 'Version History' : '변경 이력')
    case 'OUTBOUND': return t('outbound_node') || (locale?.value === 'en' ? 'Outbound Integration' : '외부 연계')
    default: return type
  }
}

const formatDate = (dateStr: any) => {
  if (dateStr === null || dateStr === undefined || dateStr === '' || dateStr === 'null' || dateStr === 'undefined') {
    return '일시 정보 없음'
  }

  // Case 1: Already a proper date string like "2026-08-02 14:30:00"
  if (typeof dateStr === 'string' && dateStr.length > 5) {
    try {
      return formatWithTimezone(dateStr)
    } catch (e) {
      return dateStr
    }
  }

  // Case 2: Array format [2026, 8, 2, 14, 30, 0] (Jackson LocalDateTime as array)
  if (Array.isArray(dateStr) && dateStr.length >= 3) {
    const [y, m, d, h = 0, min = 0, s = 0] = dateStr
    const isoStr = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}T${String(h).padStart(2, '0')}:${String(min).padStart(2, '0')}:${String(s).padStart(2, '0')}`
    try {
      return formatWithTimezone(isoStr)
    } catch (e) {
      return isoStr
    }
  }

  // Case 3: Object format { year, monthValue, dayOfMonth, hour, minute, second }
  if (typeof dateStr === 'object' && dateStr !== null) {
    const y = dateStr.year
    const m = dateStr.monthValue || dateStr.month
    const d = dateStr.dayOfMonth || dateStr.day
    if (y && m && d) {
      const h = dateStr.hour || 0
      const min = dateStr.minute || 0
      const s = dateStr.second || 0
      return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(min).padStart(2, '0')}:${String(s).padStart(2, '0')}`
    }
    // Empty object {} -> fallback
    if (Object.keys(dateStr).length === 0) {
      return '일시 정보 없음'
    }
    return JSON.stringify(dateStr)
  }

  return String(dateStr)
}

const getSimpleDetails = (details: any) => {
  if (!details || typeof details !== 'object') return {}
  const res: Record<string, any> = {}
  for (const k in details) {
    if (k !== 'previousData' && k !== 'newData') {
      res[k] = details[k]
    }
  }
  return res
}

const hasSimpleDetails = (details: any) => {
  const simple = getSimpleDetails(details)
  return Object.keys(simple).length > 0
}

const getDetailLabel = (key: string) => {
  switch (key) {
    case 'changedBy': return t('changed_by') || '작성자/변경자'
    case 'version': return t('version') || '버전'
    case 'status': return t('status') || '상태'
    default: return getFieldLabel(key)
  }
}

const openNodeDiff = (node: any) => {
  selectedNode.value = node
  showDiffModal.value = true
}

const parseJsonIfNeeded = (val: any) => {
  if (!val) return {}
  if (typeof val === 'object') return val
  try {
    return JSON.parse(val)
  } catch (e) {
    return {}
  }
}

const formatDisplayValue = (val: any): string => {
  if (val === undefined || val === null || val === '') return t('none') || '(없음)'
  if (typeof val === 'object') {
    if (val.ko || val.en) {
      return val.ko ? `${val.ko} (${val.en || ''})` : val.en
    }
    return JSON.stringify(val)
  }
  return String(val)
}

const diffRows = computed(() => {
  if (!selectedNode.value || !selectedNode.value.details) return []
  const prev = parseJsonIfNeeded(selectedNode.value.details.previousData)
  const next = parseJsonIfNeeded(selectedNode.value.details.newData)

  const allKeys = Array.from(new Set([...Object.keys(prev), ...Object.keys(next)]))
    .filter(k => !k.startsWith('_idx_'))
  const rows: Array<{ key: string; before: string; after: string }> = []

  for (const k of allKeys) {
    const bStr = formatDisplayValue(prev[k])
    const aStr = formatDisplayValue(next[k])
    if (bStr !== aStr) {
      rows.push({
        key: k,
        before: bStr,
        after: aStr
      })
    }
  }

  return rows
})
</script>

<style scoped>
.lineage-tab-btn {
  padding: 5px 14px;
  border-radius: 16px;
  border: none;
  font-size: 0.82rem;
  font-weight: 500;
  color: var(--va-text-secondary);
  background: transparent;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 5px;
}

.lineage-tab-btn.active {
  font-weight: 700;
  color: #ffffff;
  background: var(--va-primary);
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.35);
}
</style>
