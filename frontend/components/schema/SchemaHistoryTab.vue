<template>
  <div class="schema-history-tab flex flex-col h-full min-h-[400px]">
    <!-- Header Toolbar -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
      <h3 style="font-size: 1.125rem; font-weight: 700; color: var(--va-text-primary);">
        {{ $t('schema_history.title') }}
      </h3>
      <va-button preset="secondary" icon="refresh" @click="fetchHistory">
        {{ $t('match_review.refresh') }}
      </va-button>
    </div>

    <!-- History Data Table Container -->
    <div style="flex: 1; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
      <div v-if="isLoading" style="display: flex; justify-content: center; align-items: center; padding: 3rem;">
        <va-progress-circle indeterminate color="primary" />
      </div>

      <table v-else class="va-table va-table--hoverable" style="width: 100%; border-collapse: collapse; font-size: 0.875rem;">
        <thead>
          <tr style="background: var(--va-background-element); border-bottom: 2px solid var(--va-background-border); text-align: left;">
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('schema_history.target_type') }}</th>
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('schema_history.action') }}</th>
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('schema_history.changed_by') }}</th>
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('schema_history.changed_at') }}</th>
            <th style="padding: 0.75rem 1rem; text-align: right; width: 160px;"></th>
          </tr>
        </thead>
        <tbody>
          <template v-for="(row, index) in historyList" :key="row.id || index">
            <!-- Data Row -->
            <tr style="border-bottom: 1px solid var(--va-background-border); transition: background 0.15s ease;">
              <td style="padding: 0.75rem 1rem;">
                <va-badge :text="$t('schema_history.' + (row.targetType || '').toLowerCase())" :color="getTypeColor(row.targetType)" />
              </td>
              <td style="padding: 0.75rem 1rem;">
                <va-badge :text="$t('schema_history.' + (row.action || '').toLowerCase())" :color="getActionColor(row.action)" />
              </td>
              <td style="padding: 0.75rem 1rem; font-weight: 600;">
                {{ row.changedBy || '-' }}
              </td>
              <td style="padding: 0.75rem 1rem; color: var(--va-text-secondary);">
                {{ formatWithTimezone(row.changedAt) }}
              </td>
              <td style="padding: 0.75rem 1rem; text-align: right;">
                <va-button
                  preset="plain"
                  size="small"
                  :icon="expandedRowId === (row.id || index) ? 'expand_less' : 'expand_more'"
                  @click="toggleRow(row.id || index)"
                >
                  {{ expandedRowId === (row.id || index) ? ($t('vuestic.close') || '닫기') : ($t('schema_history.view_changes') || '변경 사항 보기') }}
                </va-button>
              </td>
            </tr>

            <!-- Expanded Details Row -->
            <tr v-if="expandedRowId === (row.id || index)" style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
              <td colspan="5" style="padding: 1rem 1.25rem;">
                <div v-if="getDiffItems(row).length > 0" style="overflow-x: auto;">
                  <table style="width: 100%; border-collapse: collapse; font-size: 0.825rem; background: var(--va-background-secondary); border-radius: 6px; border: 1px solid var(--va-background-border);">
                    <thead>
                      <tr style="background: rgba(0, 0, 0, 0.04); border-bottom: 1px solid var(--va-background-border); text-align: left;">
                        <th style="padding: 0.5rem 0.85rem; font-weight: 700; width: 160px; color: var(--va-text-secondary);">속성 / 항목</th>
                        <th v-if="row.action === 'UPDATE' || row.action === 'DELETE'" style="padding: 0.5rem 0.85rem; font-weight: 700; color: #dc2626; width: 40%;">{{ $t('schema_history.before') || '변경 전' }}</th>
                        <th v-if="row.action === 'UPDATE' || row.action === 'CREATE'" style="padding: 0.5rem 0.85rem; font-weight: 700; color: #16a34a; width: 40%;">{{ $t('schema_history.after') || '변경 후' }}</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr
                        v-for="item in getDiffItems(row)"
                        :key="item.key"
                        :style="{ background: (item.isChanged && row.action === 'UPDATE') ? 'rgba(234, 179, 8, 0.15)' : 'transparent' }"
                        style="border-bottom: 1px solid var(--va-background-border);"
                      >
                        <td style="padding: 0.5rem 0.85rem; font-weight: 600;">
                          <div style="display: flex; align-items: center; gap: 0.35rem;">
                            <span>{{ item.label }}</span>
                            <va-chip v-if="item.isChanged && row.action === 'UPDATE'" size="small" color="warning" square style="font-size: 9px; padding: 0 4px; height: 16px;">
                              DIFF
                            </va-chip>
                          </div>
                        </td>

                        <td v-if="row.action === 'UPDATE' || row.action === 'DELETE'" style="padding: 0.5rem 0.85rem; color: var(--va-text-primary); font-family: monospace;">
                          {{ item.beforeVal }}
                        </td>

                        <td v-if="row.action === 'UPDATE' || row.action === 'CREATE'" style="padding: 0.5rem 0.85rem; color: var(--va-text-primary); font-family: monospace;">
                          {{ item.afterVal }}
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <div v-else style="color: var(--va-text-secondary); font-style: italic; padding: 0.5rem;">
                  {{ $t('schema_history.no_history') }}
                </div>
              </td>
            </tr>
          </template>

          <tr v-if="historyList.length === 0">
            <td colspan="5" style="text-align: center; padding: 3rem; color: var(--va-text-secondary);">
              {{ $t('schema_history.no_history') }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div style="display: flex; justify-content: center; margin-top: 1rem;" v-if="totalPages > 1">
      <va-pagination
        v-model="currentPage"
        :pages="totalPages"
        @update:modelValue="fetchHistory"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useToast } from 'vuestic-ui'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { useTimezoneDate } from '~/composables/useTimezoneDate'

const props = defineProps<{
  domainId: string | null
}>()

const { init } = useToast()
const { customFetch } = useCustomFetch()
const { formatWithTimezone } = useTimezoneDate()

const isLoading = ref(false)
const historyList = ref<any[]>([])
const currentPage = ref(1)
const totalPages = ref(1)
const size = 20
const expandedRowId = ref<any>(null)

const toggleRow = (id: any) => {
  if (expandedRowId.value === id) {
    expandedRowId.value = null
  } else {
    expandedRowId.value = id
  }
}

const getTypeColor = (type: string) => {
  if (type === 'FIELD') return 'info'
  if (type === 'NODE') return 'warning'
  if (type === 'DOMAIN') return 'primary'
  if (type === 'GROUP') return 'secondary'
  return 'gray'
}

const getActionColor = (action: string) => {
  if (action === 'CREATE') return 'success'
  if (action === 'UPDATE') return 'warning'
  if (action === 'DELETE') return 'danger'
  return 'gray'
}

const propertyLabels: Record<string, string> = {
  name: '필드명',
  key: '필드 키',
  type: '데이터 타입',
  required: '필수 여부',
  isSearchable: '검색 가능 여부',
  isMultiValue: '다중값 허용',
  isEncrypted: '암호화 여부',
  isReadOnly: '읽기 전용',
  isHidden: '숨김 여부',
  isImmutable: '수정 불가',
  order: '정렬 순서',
  group: '필드 그룹',
  unit: '단위',
  id: '식별 ID'
}

const parseSnapshot = (raw: any): Record<string, any> => {
  if (!raw) return {}
  if (typeof raw === 'object') return raw
  try {
    return JSON.parse(raw)
  } catch {
    return {}
  }
}

const formatDisplayValue = (val: any): string => {
  if (val === null || val === undefined || val === '') return '-'
  if (typeof val === 'boolean') return val ? '예 (True)' : '아니오 (False)'

  // Handle Array (e.g. children nodes or options list)
  if (Array.isArray(val)) {
    if (val.length === 0) return '-'
    const names = val.map(item => {
      if (!item) return ''
      if (typeof item === 'object') {
        const nameVal = item.name || item.title || item.key || item.label
        if (nameVal) {
          if (typeof nameVal === 'object') return nameVal.ko || nameVal.en || JSON.stringify(nameVal)
          if (typeof nameVal === 'string' && nameVal.startsWith('{')) {
            try {
              const p = JSON.parse(nameVal)
              return p.ko || p.en || nameVal
            } catch {}
          }
          return String(nameVal)
        }
      }
      return String(item)
    }).filter(Boolean)
    return names.length > 0 ? names.join(', ') : `${val.length}개 항목`
  }

  // Handle Object (e.g. multilingual name {"ko": "...", "en": "..."})
  if (typeof val === 'object') {
    if (val.ko || val.en) return val.ko ? (val.en ? `${val.ko} (${val.en})` : val.ko) : val.en
    return JSON.stringify(val)
  }

  const str = String(val).trim()
  
  // Handle JSON Array String (e.g. "[{...}]")
  if (str.startsWith('[') && str.endsWith(']')) {
    try {
      const parsedArr = JSON.parse(str)
      if (Array.isArray(parsedArr)) {
        return formatDisplayValue(parsedArr)
      }
    } catch {}
  }

  // Handle JSON Object String (e.g. "{\"ko\":\"...\"}")
  if (str.startsWith('{') && str.endsWith('}')) {
    try {
      const parsed = JSON.parse(str)
      if (parsed && typeof parsed === 'object') {
        if (parsed.ko || parsed.en) return parsed.ko ? (parsed.en ? `${parsed.ko} (${parsed.en})` : parsed.ko) : parsed.en
      }
    } catch {}
  }

  return str
}

const getDiffItems = (row: any) => {
  const beforeObj = parseSnapshot(row.beforeData || row.beforeSnapshot)
  const afterObj = parseSnapshot(row.afterData || row.afterSnapshot)
  
  const hasBefore = Object.keys(beforeObj).length > 0
  const hasAfter = Object.keys(afterObj).length > 0
  
  const allKeys = Array.from(new Set([...Object.keys(beforeObj), ...Object.keys(afterObj)]))
  const filterKeys = ['id', 'definedAtNode', 'domain', 'domainId', 'axisId', 'createdNodeId']
  
  const displayKeys = allKeys.filter(k => !filterKeys.includes(k))
  
  return displayKeys.map(k => {
    const bVal = hasBefore ? formatDisplayValue(beforeObj[k]) : '-'
    let aVal = hasAfter ? formatDisplayValue(afterObj[k]) : '-'
    
    // 이전 백엔드 직렬화 오류로 인해 afterData가 NULL로 저장되었던 과거 수정이력 처리
    if (row.action === 'UPDATE' && !hasAfter && hasBefore) {
      aVal = '(이전 버전 데이터 유실)'
    }
    
    const label = propertyLabels[k] || k
    const isChanged = bVal !== aVal
    return {
      key: k,
      label,
      beforeVal: bVal,
      afterVal: aVal,
      isChanged
    }
  })
}

const fetchHistory = async () => {
  if (!props.domainId) return
  isLoading.value = true
  try {
    const res: any = await customFetch(`/api/domains/${props.domainId}/schema-history?page=${currentPage.value - 1}&size=${size}`)
    historyList.value = res.content || []
    totalPages.value = res.totalPages || 1
  } catch (e) {
    console.error(e)
    init({ message: '이력 조회 실패', color: 'danger' })
  } finally {
    isLoading.value = false
  }
}

watch(() => props.domainId, () => {
  currentPage.value = 1
  expandedRowId.value = null
  fetchHistory()
}, { immediate: true })

</script>
