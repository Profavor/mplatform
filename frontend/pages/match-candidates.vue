<template>
  <div style="display: flex; flex-direction: column; gap: 1.25rem; padding-bottom: 2rem;">
    <!-- Top Action Bar -->
    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); padding: 1rem 1.25rem; border-radius: 12px; border: 1px solid var(--va-background-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04);">
      <div style="display: flex; align-items: center; gap: 0.75rem;">
        <va-icon name="find_replace" size="large" color="primary" />
        <div>
          <h2 style="font-weight: 700; font-size: 1.35rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
            {{ pageTitle }}
            <va-badge text="Deduplication" color="primary" size="small" />
          </h2>
          <span style="font-size: 0.85rem; color: var(--va-text-secondary);">
            {{ $t('deduplication.subtitle') }}
          </span>
        </div>
      </div>

      <div style="display: flex; gap: 0.75rem; align-items: center;">
        <va-button preset="outline" color="primary" icon="refresh" size="small" @click="loadCandidates">{{ $t('refresh') }}</va-button>
      </div>
    </div>

    <!-- Filter & Executive KPI Summary Bar -->
    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); padding: 1rem 1.25rem; border-radius: 12px; border: 1px solid var(--va-background-border); flex-wrap: wrap; gap: 1rem;">
      <div style="display: flex; align-items: center; gap: 1rem;">
        <div style="font-size: 0.88rem; font-weight: 700; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.35rem;">
          <va-icon name="filter_alt" size="small" color="primary" />
          <span>{{ $t('status_filter') }}</span>
        </div>
        <va-select
          v-model="selectedStatus"
          :options="statusOptions"
          value-by="value"
          text-by="text"
          style="min-width: 200px;"
          dense
          @update:model-value="loadCandidates"
        />
      </div>

      <!-- Clean KPI Stat Badges -->
      <div style="display: flex; gap: 0.75rem; align-items: center; flex-wrap: wrap;">
        <div style="display: flex; align-items: center; gap: 0.5rem; background: var(--va-background-element); padding: 0.45rem 0.85rem; border-radius: 8px; border: 1px solid var(--va-background-border);">
          <va-icon name="hourglass_top" color="warning" size="small" />
          <span style="font-size: 0.85rem; font-weight: 700; color: var(--va-text-primary);">{{ $t('status_pending') }}</span>
          <span style="font-size: 0.95rem; font-weight: 800; color: var(--va-warning); background: rgba(245, 158, 11, 0.15); padding: 1px 8px; border-radius: 6px;">
            {{ candidates.filter(c => c.status === 'PENDING').length }}건
          </span>
        </div>

        <div style="display: flex; align-items: center; gap: 0.5rem; background: var(--va-background-element); padding: 0.45rem 0.85rem; border-radius: 8px; border: 1px solid var(--va-background-border);">
          <va-icon name="call_merge" color="success" size="small" />
          <span style="font-size: 0.85rem; font-weight: 700; color: var(--va-text-primary);">{{ $t('status_merged') }}</span>
          <span style="font-size: 0.95rem; font-weight: 800; color: var(--va-success); background: rgba(34, 197, 94, 0.15); padding: 1px 8px; border-radius: 6px;">
            {{ candidates.filter(c => c.status === 'MERGED').length }}건
          </span>
        </div>

        <div style="display: flex; align-items: center; gap: 0.5rem; background: var(--va-background-element); padding: 0.45rem 0.85rem; border-radius: 8px; border: 1px solid var(--va-background-border);">
          <va-icon name="block" color="secondary" size="small" />
          <span style="font-size: 0.85rem; font-weight: 700; color: var(--va-text-primary);">{{ $t('status_ignored') }}</span>
          <span style="font-size: 0.95rem; font-weight: 800; color: var(--va-text-secondary); background: rgba(148, 163, 184, 0.15); padding: 1px 8px; border-radius: 6px;">
            {{ candidates.filter(c => c.status === 'IGNORED').length }}건
          </span>
        </div>
      </div>
    </div>

    <!-- Candidate List Table / Cards -->
    <va-card>
      <va-card-content style="padding: 1.25rem;">
        <div v-if="loading" style="display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 3.5rem 1rem;">
          <va-progress-circle indeterminate color="primary" size="3.5rem" />
          <span style="margin-top: 1rem; color: var(--va-text-secondary); font-size: 0.9rem;">매칭 후보 데이터를 불러오는 중입니다...</span>
        </div>
        <div v-else-if="!candidates.length" style="padding: 3.5rem 1rem; text-align: center; background: var(--va-background-element); border: 1px dashed var(--va-background-border); border-radius: 12px; color: var(--va-text-secondary);">
          <va-icon name="task_alt" color="success" size="4rem" style="margin-bottom: 0.75rem; display: block;" />
          <div style="font-size: 1.15rem; font-weight: 800; color: var(--va-text-primary); margin-bottom: 0.35rem;">{{ $t('deduplication.no_candidates') }}</div>
          <div style="font-size: 0.85rem; color: var(--va-text-secondary);">현재 상태 조건에 해당하는 중복 레코드 검토 후보가 없습니다.</div>
        </div>
        <div v-else style="display: flex; flex-direction: column; gap: 1rem;">
          <div
            v-for="item in candidates"
            :key="item.id"
            style="border: 1px solid var(--va-background-border); border-radius: 12px; padding: 1.25rem; background: var(--va-background-element); box-shadow: 0 2px 8px rgba(0,0,0,0.02);"
          >
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; flex-wrap: wrap; gap: 0.5rem;">
              <div style="display: flex; align-items: center; gap: 0.75rem;">
                <va-badge
                  :text="item.status"
                  :color="item.status === 'PENDING' ? 'warning' : item.status === 'MERGED' ? 'success' : 'secondary'"
                  style="font-weight: 800;"
                />
                <span style="font-weight: 800; font-size: 1.1rem; color: var(--va-text-primary);">
                  {{ item.ruleName || $t('deduplication.rule_default') }}
                </span>
                <va-chip size="small" color="primary" style="font-weight: 700;">
                  <va-icon name="auto_awesome" size="small" style="margin-right: 4px;" />
                  {{ $t('deduplication.similarity') }}: {{ (item.similarityScore * 100).toFixed(1) }}%
                </va-chip>
              </div>

              <div style="display: flex; gap: 0.5rem;">
                <va-button color="primary" icon="compare_arrows" size="small" style="font-weight: 700;" @click="openDiffModal(item)">
                  {{ $t('deduplication.compare_and_action') }}
                </va-button>
              </div>
            </div>

            <!-- Summary Comparison Info -->
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; background: var(--va-background-primary); padding: 1rem; border-radius: 10px; border: 1px solid var(--va-background-border);">
              <div style="display: flex; align-items: center; gap: 0.75rem;">
                <va-avatar color="primary" size="medium" style="font-weight: 800;">
                  M
                </va-avatar>
                <div>
                  <div style="font-size: 0.8rem; color: var(--va-text-secondary); font-weight: 600;">{{ $t('deduplication.target_record') }} (Survivor)</div>
                  <div style="font-weight: 800; font-size: 0.95rem; color: var(--va-text-primary); font-family: monospace;">ID: {{ item.sourceRecordId }}</div>
                </div>
              </div>
              <div style="display: flex; align-items: center; gap: 0.75rem;">
                <va-avatar color="warning" size="medium" style="font-weight: 800;">
                  C
                </va-avatar>
                <div>
                  <div style="font-size: 0.8rem; color: var(--va-text-secondary); font-weight: 600;">{{ $t('deduplication.candidate_record') }} (Candidate)</div>
                  <div style="font-weight: 800; font-size: 0.95rem; color: var(--va-text-primary); font-family: monospace;">ID: {{ item.candidateRecordId }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </va-card-content>
    </va-card>

    <!-- Side-by-Side Diff Modal -->
    <AppModal v-model="showDiffModal" size="large" hide-default-actions>
      <template #header>
        <div style="display: flex; align-items: center; gap: 0.65rem; width: 100%;">
          <va-icon name="compare" color="primary" size="large" />
          <h3 style="margin: 0; font-size: 1.25rem; font-weight: 800; color: var(--va-text-primary);">
            {{ $t('deduplication.modal_title') }}
          </h3>
        </div>
      </template>

      <div v-if="selectedCandidate" style="padding: 0.5rem 0 0 0;">
        <div style="display: flex; gap: 1rem; margin-bottom: 1rem;">
          <div style="flex: 1; border: 1px solid var(--va-primary); padding: 1rem; border-radius: 10px; background: var(--va-background-element);">
            <h4 style="margin-top: 0; margin-bottom: 0.5rem; font-weight: 800; color: var(--va-primary); font-size: 1.05rem; display: flex; align-items: center; gap: 0.35rem;">
              <va-icon name="verified" size="small" />
              {{ $t('deduplication.master_record') }}
            </h4>
            <div style="font-size: 0.85rem; color: var(--va-text-secondary); margin-bottom: 0.75rem; font-weight: 600; font-family: monospace;">
              ID: {{ selectedCandidate.sourceRecordId }}
            </div>
            <pre style="background: var(--va-background-primary); padding: 0.85rem; border-radius: 8px; font-size: 0.85rem; overflow-x: auto; color: var(--va-text-primary); border: 1px solid var(--va-background-border);">
{{ formatJson(selectedCandidate.sourceData) }}
            </pre>
          </div>

          <div style="flex: 1; border: 1px solid var(--va-warning); padding: 1rem; border-radius: 10px; background: var(--va-background-element);">
            <h4 style="margin-top: 0; margin-bottom: 0.5rem; font-weight: 800; color: var(--va-warning); font-size: 1.05rem; display: flex; align-items: center; gap: 0.35rem;">
              <va-icon name="control_point_duplicate" size="small" />
              {{ $t('deduplication.duplicate_candidate') }}
            </h4>
            <div style="font-size: 0.85rem; color: var(--va-text-secondary); margin-bottom: 0.75rem; font-weight: 600; font-family: monospace;">
              ID: {{ selectedCandidate.candidateRecordId }}
            </div>
            <pre style="background: var(--va-background-primary); padding: 0.85rem; border-radius: 8px; font-size: 0.85rem; overflow-x: auto; color: var(--va-text-primary); border: 1px solid var(--va-background-border);">
{{ formatJson(selectedCandidate.candidateData) }}
            </pre>
          </div>
        </div>
      </div>

      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 0.75rem; padding-top: 1rem; border-top: 1px solid var(--va-background-border);">
          <va-button preset="outline" @click="showDiffModal = false">{{ $t('cancel') }}</va-button>
          <va-button preset="outline" color="warning" icon="block" @click="ignoreCandidate" style="font-weight: 700;">
            {{ $t('deduplication.keep_separate') }}
          </va-button>
          <va-button color="success" icon="call_merge" @click="mergeCandidate" style="font-weight: 700;">
            {{ $t('deduplication.confirm_merge') }}
          </va-button>
        </div>
      </template>
    </AppModal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { usePageTitle } from '~/composables/usePageTitle'
import AppModal from '~/components/common/AppModal.vue'

const { pageTitle } = usePageTitle('deduplication.title', '중복 후보 검토 큐')
const { t } = useI18n()

const loading = ref(false)
const selectedStatus = ref('PENDING')
const candidates = ref<any[]>([])
const showDiffModal = ref(false)
const selectedCandidate = ref<any>(null)

const statusOptions = computed(() => [
  { value: 'PENDING', text: `PENDING (${t('status_pending')})` },
  { value: 'MERGED', text: `MERGED (${t('status_merged')})` },
  { value: 'IGNORED', text: `IGNORED (${t('status_ignored')})` }
])

const token = useCookie('auth_token')

const loadCandidates = async () => {
  loading.value = true
  try {
    const res = await $fetch<any>('/api/match-candidates', {
      headers: token.value ? { Authorization: `Bearer ${token.value}` } : {},
      params: { status: selectedStatus.value }
    }).catch(() => null)
    const items = Array.isArray(res) ? res : (res?.content || [])
    candidates.value = items
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
      headers: token.value ? { Authorization: `Bearer ${token.value}` } : {},
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
      method: 'POST',
      headers: token.value ? { Authorization: `Bearer ${token.value}` } : {}
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
