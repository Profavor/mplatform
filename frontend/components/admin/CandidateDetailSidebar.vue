<template>
  <va-card v-if="candidate" style="width: 340px; flex-shrink: 0; display: flex; flex-direction: column; overflow: hidden; padding: 0;">
    <!-- 사이드바 헤더 -->
    <div style="display: flex; align-items: center; justify-content: space-between; padding: 0.85rem 1rem; border-bottom: 1px solid var(--va-background-border);">
      <div style="display: flex; align-items: center; gap: 0.5rem; font-size: 0.88rem; font-weight: 700; color: var(--va-text-primary);">
        <va-icon name="compare_arrows" size="18px" color="primary" />
        {{ t('match_review.sidebar_title', '후보 상세 비교') }}
      </div>
      <va-badge
        :text="(candidate.score * 100).toFixed(1) + '%'"
        :color="getScoreColor(candidate.score)"
      />
    </div>

    <!-- 스크롤 영역 -->
    <div style="flex: 1; overflow-y: auto; padding: 0.75rem; display: flex; flex-direction: column; gap: 0.75rem;">
      <!-- 기존 레코드 -->
      <div style="border-radius: 8px; overflow: hidden; border: 1px solid var(--va-background-border);">
        <div style="display: flex; align-items: center; gap: 6px; padding: 0.45rem 0.75rem; font-size: 0.72rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; background: var(--va-background-element); color: var(--va-text-secondary); border-bottom: 1px solid var(--va-background-border);">
          <va-icon name="inventory_2" size="13px" color="primary" />
          {{ t('match_review.existing_record', '기존 레코드') }}
        </div>
        <div>
          <div
            v-for="(val, key) in parseRecordData(candidate.existingRecord)"
            :key="'ex-'+key"
            style="display: flex; justify-content: space-between; align-items: baseline; padding: 0.35rem 0.75rem; font-size: 0.75rem; border-bottom: 1px solid var(--va-background-border); gap: 0.5rem;"
          >
            <span style="font-weight: 600; color: var(--va-text-secondary); white-space: nowrap; flex-shrink: 0;">{{ key }}</span>
            <span style="font-family: 'Fira Code', monospace; font-size: 0.72rem; color: var(--va-text-primary); word-break: break-all; text-align: right;">{{ formatValue(val) }}</span>
          </div>
        </div>
      </div>

      <!-- 신규 유입 레코드 -->
      <div style="border-radius: 8px; overflow: hidden; border: 1px solid var(--va-background-border);">
        <div style="display: flex; align-items: center; gap: 6px; padding: 0.45rem 0.75rem; font-size: 0.72rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; background: var(--va-background-element); color: var(--va-text-secondary); border-bottom: 1px solid var(--va-background-border);">
          <va-icon name="move_to_inbox" size="13px" color="info" />
          {{ t('match_review.incoming_data', '신규 유입 데이터') }}
        </div>
        <div>
          <div
            v-for="(val, key) in parseRecordData(candidate.incomingData)"
            :key="'in-'+key"
            style="display: flex; justify-content: space-between; align-items: baseline; padding: 0.35rem 0.75rem; font-size: 0.75rem; border-bottom: 1px solid var(--va-background-border); gap: 0.5rem;"
          >
            <span style="font-weight: 600; color: var(--va-text-secondary); white-space: nowrap; flex-shrink: 0;">{{ key }}</span>
            <span style="font-family: 'Fira Code', monospace; font-size: 0.72rem; color: var(--va-text-primary); word-break: break-all; text-align: right;">{{ formatValue(val) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 사이드바 액션 버튼 -->
    <div
      v-if="hasWritePermission && candidate.status === 'PENDING_REVIEW'"
      style="display: flex; gap: 0.5rem; padding: 0.75rem; border-top: 1px solid var(--va-background-border);"
    >
      <va-button preset="secondary" color="danger" style="flex: 1;" @click="emit('reject', candidate)">
        <va-icon name="cancel" size="15px" />
        {{ t('match_review.reject_new', '신규 생성 (거절)') }}
      </va-button>
      <va-button color="success" style="flex: 1;" @click="emit('merge', candidate)">
        <va-icon name="merge_type" size="15px" />
        {{ t('match_review.confirm_merge', '병합 승인') }}
      </va-button>
    </div>
  </va-card>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

defineProps<{
  candidate: any
  hasWritePermission?: boolean
}>()

const emit = defineEmits<{
  (e: 'reject', candidate: any): void
  (e: 'merge', candidate: any): void
}>()

const getScoreColor = (score: number) => {
  if (score >= 0.9) return 'success'
  if (score >= 0.75) return 'warning'
  return 'danger'
}

const parseRecordData = (recordOrData: any): Record<string, any> => {
  if (!recordOrData) return {}
  if (recordOrData.data && typeof recordOrData.data === 'object') {
    return recordOrData.data
  }
  if (typeof recordOrData === 'string') {
    try {
      return JSON.parse(recordOrData)
    } catch {
      return { raw: recordOrData }
    }
  }
  return recordOrData
}

const formatValue = (val: any) => {
  if (val === null || val === undefined || val === '') return '-'
  if (typeof val === 'object') return JSON.stringify(val)
  return String(val)
}
</script>
