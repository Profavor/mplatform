<template>
  <div style="padding: 1.5rem;">
    <!-- Top Header -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
      <div>
        <h1 style="font-size: 1.8rem; font-weight: bold; margin: 0; display: flex; align-items: center; gap: 0.5rem;">
          <va-icon name="find_in_page" color="primary" size="medium" />
          {{ t('title') }}
        </h1>
        <p style="color: var(--va-text-secondary); margin-top: 0.25rem; font-size: 0.95rem;">
          {{ t('subtitle') }}
        </p>
      </div>
      <va-button preset="secondary" icon="refresh" @click="loadCandidates">{{ t('refresh') }}</va-button>
    </div>

    <!-- Filter & Stats Bar -->
    <div style="display: flex; gap: 1rem; align-items: center; margin-bottom: 1rem;">
      <va-select
        v-model="selectedStatus"
        :options="statusOptions"
        value-by="value"
        label-by="text"
        :label="t('status_filter')"
        style="width: 200px;"
        @update:model-value="loadCandidates"
      />
      <va-badge text="PENDING" color="warning" style="padding: 0.5rem 0.75rem;">
        {{ t('pending_count', { count: pendingCount }) }}
      </va-badge>
    </div>

    <!-- Candidate List Table / Cards -->
    <va-card style="margin-bottom: 1.5rem;">
      <va-card-content>
        <div v-if="loading" style="display: flex; justify-content: center; padding: 2rem;">
          <va-progress-circle indeterminate />
        </div>
        <div v-else-if="!candidates.length" style="text-align: center; padding: 2rem; color: var(--va-text-secondary);">
          <va-icon name="check_circle_outline" size="large" color="success" style="margin-bottom: 0.5rem;" />
          <div>{{ t('no_candidates') }}</div>
        </div>
        <div v-else style="display: flex; flex-direction: column; gap: 1rem;">
          <div
            v-for="item in candidates"
            :key="item.id"
            style="border: 1px solid var(--va-background-element); border-radius: 8px; padding: 1rem; background: var(--va-background-primary);"
          >
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
              <div style="display: flex; align-items: center; gap: 0.75rem;">
                <va-badge
                  :text="item.status"
                  :color="item.status === 'PENDING' ? 'warning' : item.status === 'MERGED' ? 'success' : 'secondary'"
                />
                <span style="font-weight: bold; font-size: 1.05rem;">
                  {{ item.ruleName || t('rule_default') }}
                </span>
                <va-chip size="small" color="info" outline>
                  {{ t('similarity') }}: {{ (item.similarityScore * 100).toFixed(1) }}%
                </va-chip>
              </div>

              <div style="display: flex; gap: 0.5rem;">
                <va-button preset="primary" icon="compare_arrows" size="small" @click="openDiffModal(item)">
                  {{ t('compare_and_action') }}
                </va-button>
              </div>
            </div>

            <!-- Summary Info -->
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; background: var(--va-background-element); padding: 0.75rem; border-radius: 6px;">
              <div>
                <div style="font-size: 0.8rem; color: var(--va-text-secondary);">{{ t('target_record') }} (Survivor)</div>
                <div style="font-weight: 600;">ID: {{ item.sourceRecordId }}</div>
              </div>
              <div>
                <div style="font-size: 0.8rem; color: var(--va-text-secondary);">{{ t('candidate_record') }} (Merged candidate)</div>
                <div style="font-weight: 600;">ID: {{ item.candidateRecordId }}</div>
              </div>
            </div>
          </div>
        </div>
      </va-card-content>
    </va-card>

    <!-- Side-by-Side Diff Modal -->
    <va-modal v-model="showDiffModal" size="large" close-button hide-default-actions>
      <template #header>
        <h3 style="margin: 0; font-size: 1.25rem; font-weight: bold; display: flex; align-items: center; gap: 0.5rem;">
          <va-icon name="compare" color="primary" />
          {{ t('modal_title') }}
        </h3>
      </template>

      <div v-if="selectedCandidate" style="padding: 1rem 0;">
        <div style="display: flex; gap: 1rem; margin-bottom: 1rem;">
          <div style="flex: 1; border: 1px solid var(--va-primary); padding: 1rem; border-radius: 8px;">
            <h4 style="margin-top: 0; font-weight: bold; color: var(--va-primary);">
              {{ t('master_record') }} (Survivor Target)
            </h4>
            <div style="font-size: 0.85rem; color: var(--va-text-secondary); margin-bottom: 0.5rem;">
              ID: {{ selectedCandidate.sourceRecordId }}
            </div>
            <pre style="background: var(--va-background-element); padding: 0.75rem; border-radius: 4px; font-size: 0.85rem; overflow-x: auto;">
{{ formatJson(selectedCandidate.sourceData) }}
            </pre>
          </div>

          <div style="flex: 1; border: 1px solid var(--va-warning); padding: 1rem; border-radius: 8px;">
            <h4 style="margin-top: 0; font-weight: bold; color: var(--va-warning);">
              {{ t('duplicate_candidate') }}
            </h4>
            <div style="font-size: 0.85rem; color: var(--va-text-secondary); margin-bottom: 0.5rem;">
              ID: {{ selectedCandidate.candidateRecordId }}
            </div>
            <pre style="background: var(--va-background-element); padding: 0.75rem; border-radius: 4px; font-size: 0.85rem; overflow-x: auto;">
{{ formatJson(selectedCandidate.candidateData) }}
            </pre>
          </div>
        </div>
      </div>

      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 0.75rem;">
          <va-button preset="secondary" @click="showDiffModal = false">{{ t('cancel') }}</va-button>
          <va-button preset="primary" color="warning" icon="block" @click="ignoreCandidate">
            {{ t('keep_separate') }}
          </va-button>
          <va-button preset="primary" color="success" icon="call_merge" @click="mergeCandidate">
            {{ t('confirm_merge') }}
          </va-button>
        </div>
      </template>
    </va-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'

const { locale } = useI18n()

const t = (key: string, params?: Record<string, any>) => {
  const messages: Record<string, Record<string, string>> = {
    ko: {
      title: '중복 후보 검토 큐 (Data Steward Review Queue)',
      subtitle: '퍼지 매칭 및 매칭 룰에 의해 발견된 유사 중복 데이터를 검토하고 병합 또는 분리를 처리합니다.',
      refresh: '새로고침',
      status_filter: '상태 필터',
      pending_count: '검토 대기: {count}건',
      no_candidates: '검토할 중복 후보 레코드가 없습니다.',
      rule_default: '기본 퍼지 매칭 룰',
      similarity: '유사도',
      compare_and_action: '비교 및 처리',
      target_record: '기준 마스터 레코드',
      candidate_record: '중복 후보 레코드',
      modal_title: 'Side-by-Side 레코드 필드 비교',
      master_record: '마스터 레코드 (유지)',
      duplicate_candidate: '중복 후보 레코드 (병합 대상)',
      cancel: '취소',
      keep_separate: '별도 레코드로 유지 (Ignore)',
      confirm_merge: '마스터로 병합 승인 (Merge)'
    },
    en: {
      title: 'Match Candidate Review Queue (Data Steward)',
      subtitle: 'Review potential duplicate records found by fuzzy matching and approve merge or separation.',
      refresh: 'Refresh',
      status_filter: 'Status Filter',
      pending_count: 'Pending: {count}',
      no_candidates: 'No candidate records to review.',
      rule_default: 'Fuzzy Match Rule',
      similarity: 'Similarity',
      compare_and_action: 'Compare & Act',
      target_record: 'Target Master Record',
      candidate_record: 'Candidate Record',
      modal_title: 'Side-by-Side Field Comparison',
      master_record: 'Master Record (Survivor)',
      duplicate_candidate: 'Candidate Record (To Merge)',
      cancel: 'Cancel',
      keep_separate: 'Keep Separate',
      confirm_merge: 'Approve Merge'
    }
  }
  const lang = locale.value === 'en' ? 'en' : 'ko'
  let text = messages[lang]?.[key] || key
  if (params) {
    Object.keys(params).forEach(p => {
      text = text.replace(`{${p}}`, params[p])
    })
  }
  return text
}

const loading = ref(false)
const selectedStatus = ref('PENDING')
const candidates = ref<any[]>([])
const showDiffModal = ref(false)
const selectedCandidate = ref<any>(null)

const statusOptions = [
  { value: 'PENDING', text: 'PENDING (대기)' },
  { value: 'MERGED', text: 'MERGED (병합완료)' },
  { value: 'IGNORED', text: 'IGNORED (별도유지)' }
]

const pendingCount = computed(() => {
  return candidates.value.filter(c => c.status === 'PENDING').length
})

const loadCandidates = async () => {
  loading.value = true
  try {
    const res = await $fetch<any[]>('/api/match-candidates', {
      params: { status: selectedStatus.value }
    }).catch(() => [])
    candidates.value = res || []
  } finally {
    loading.value = false
  }
}

const openDiffModal = (item: any) => {
  selectedCandidate.value = item
  showDiffModal.value = true
}

const formatJson = (data: any) => {
  if (!data) return '{}'
  try {
    return typeof data === 'string' ? JSON.stringify(JSON.parse(data), null, 2) : JSON.stringify(data, null, 2)
  } catch {
    return String(data)
  }
}

const mergeCandidate = async () => {
  if (!selectedCandidate.value) return
  try {
    await $fetch('/api/records/merge', {
      method: 'POST',
      body: {
        survivorRecordId: selectedCandidate.value.sourceRecordId,
        mergedRecordIds: [selectedCandidate.value.candidateRecordId]
      }
    })
    showDiffModal.value = false
    await loadCandidates()
  } catch (err: any) {
    alert('Failed to merge record: ' + (err.message || err))
  }
}

const ignoreCandidate = async () => {
  if (!selectedCandidate.value) return
  try {
    await $fetch(`/api/match-candidates/${selectedCandidate.value.id}/ignore`, {
      method: 'POST'
    })
    showDiffModal.value = false
    await loadCandidates()
  } catch (err: any) {
    alert('Failed to update status: ' + (err.message || err))
  }
}

onMounted(() => {
  loadCandidates()
})
</script>
