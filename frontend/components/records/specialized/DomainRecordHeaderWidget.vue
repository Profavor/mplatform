<template>
  <va-card
    v-if="hasAnyAttributeField"
    outlined
    class="domain-record-header-widget"
  >
    <va-card-content class="widget-content">
      <div class="header-row">
        <!-- 이미지/아바타 -->
        <div class="avatar-area">
          <va-avatar
            v-if="imageValue"
            :src="imageValue"
            :size="54"
            class="record-avatar"
          />
          <va-avatar
            v-else
            :icon="domainIcon || 'dataset'"
            color="primary"
            :size="54"
            class="record-avatar"
          />
        </div>

        <!-- 메인 정보 -->
        <div class="main-info">
          <!-- 상단 행: 좌측 명칭/도메인 + 우측 감사 메타 블록 -->
          <div class="header-top-row">
            <div class="header-title-group">
              <div class="name-row">
                <span v-if="nameValue" class="record-name">{{ nameValue }}</span>
                <span v-else class="record-name placeholder">—</span>
                <span v-if="idValue" class="id-badge">{{ idValue }}</span>
                <span v-if="recordStatus" class="status-badge" :class="'status-' + String(recordStatus).toLowerCase()">
                  {{ statusLabel }}
                </span>
              </div>
              <div v-if="descValue" class="desc-text">{{ descValue }}</div>
              <!-- 도메인 이름 표시 -->
              <div class="domain-label-row">
                <va-icon name="folder_open" size="14px" color="secondary" />
                <span class="domain-label-text">{{ domainLabel }}</span>
              </div>
            </div>

            <!-- 우측: 생성자/생성일, 변경자/변경일 마스터 감사 메타정보 블록 (상단 유저명, 하단 날짜/버전 2열 구조) -->
            <div class="audit-meta-block">
              <div class="audit-meta-grid">
                <!-- 생성 정보 -->
                <div v-if="createdByValue || createdAtFormatted" class="audit-meta-col">
                  <div class="audit-user-line">
                    <span class="audit-meta-label">{{ $t('created_info') || '생성' }}:</span>
                    <span class="audit-meta-value" :title="createdByValue">{{ createdByValue }}</span>
                  </div>
                  <div v-if="createdAtFormatted" class="audit-date-line">
                    {{ createdAtFormatted }}
                  </div>
                </div>

                <!-- 세로 구분선 -->
                <div v-if="(createdByValue || createdAtFormatted) && (updatedAtFormatted || updatedByValue)" class="audit-col-divider" />

                <!-- 수정 정보 -->
                <div v-if="updatedAtFormatted || updatedByValue" class="audit-meta-col">
                  <div class="audit-user-line">
                    <span class="audit-meta-label">{{ $t('updated_info') || '수정' }}:</span>
                    <span class="audit-meta-value" :title="updatedByValue">{{ updatedByValue }}</span>
                  </div>
                  <div v-if="updatedAtFormatted" class="audit-date-line">
                    <span>{{ updatedAtFormatted }}</span>
                    <span v-if="versionValue" class="version-tag">v{{ versionValue }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 하단: 커스텀 추가 속성 필드 칩 목록 -->
          <div v-if="customSubFields?.length > 0" class="custom-subfields-chips">
            <span
              v-for="sub in customSubFields"
              :key="sub.key"
              class="subfield-chip"
            >
              <strong class="subfield-label">{{ sub.label }}:</strong>
              <span class="subfield-val">{{ sub.value }}</span>
            </span>
          </div>
        </div>
      </div>
    </va-card-content>
  </va-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatOptionLabel } from '~/utils/optionParser'

interface FieldDef {
  id: string
  key: string
  name: Record<string, string> | string
  type?: string
  options?: any
}

interface DomainInfo {
  id?: string
  name?: Record<string, string> | string
  icon?: string
  identifierFieldId?: string
  displayNameFieldId?: string
  descriptionFieldId?: string
  imageFieldId?: string
}

const props = defineProps<{
  domain: DomainInfo | null
  recordData: Record<string, any>
  fields: FieldDef[]
  customSubFieldKeys?: string[]
}>()

const localeCookie = useCookie('locale', { default: () => 'ko' })
const locale = computed(() => localeCookie.value || 'ko')

/** 필드 ID -> 필드 key 변환 헬퍼 */
const findFieldKey = (fieldId: string | undefined): string | null => {
  if (!fieldId || !props.fields?.length) return null
  const f = props.fields.find((fd) => fd.id === fieldId)
  return f?.key ?? null
}

/** 레코드 데이터에서 fieldKey의 값을 다국어 처리하여 반환 */
const resolveValue = (fieldKey: string | null): string => {
  if (!fieldKey) return ''
  const raw = props.recordData?.[fieldKey]
  if (raw === undefined || raw === null || raw === '') return ''

  const f: any = props.fields?.find((fd) => fd.key === fieldKey || fd.id === fieldKey)

  // 1. SELECT, CODE, ENUM, MULTI_SELECT 등 옵션 정의가 있는 경우
  if (f?.options) {
    const optLabel = formatOptionLabel(f.options, raw, locale.value)
    if (optLabel) return optLabel
  }

  // 2. 다국어 객체
  if (typeof raw === 'object') {
    return raw[locale.value] || raw.ko || raw.en || Object.values(raw)[0] || JSON.stringify(raw)
  }

  // 3. 날짜 타입 포맷
  if (f?.type === 'DATE' || f?.type === 'DATETIME') {
    if (typeof raw === 'string' && /^\d{4}-\d{2}-\d{2}/.test(raw)) {
      return f.type === 'DATE' ? raw.substring(0, 10) : formatDateTime(raw)
    }
  }

  // 4. BOOLEAN인 경우
  if (f?.type === 'BOOLEAN' || typeof raw === 'boolean') {
    return raw ? (locale.value === 'ko' ? '예' : 'Yes') : (locale.value === 'ko' ? '아니오' : 'No')
  }

  return String(raw)
}

const idKey = computed(() => findFieldKey(props.domain?.identifierFieldId))
const nameKey = computed(() => findFieldKey(props.domain?.displayNameFieldId))
const descKey = computed(() => findFieldKey(props.domain?.descriptionFieldId))
const imageKey = computed(() => findFieldKey(props.domain?.imageFieldId))

const idValue = computed(() => resolveValue(idKey.value))
const nameValue = computed(() => resolveValue(nameKey.value))
const descValue = computed(() => resolveValue(descKey.value))
const imageValue = computed(() => resolveValue(imageKey.value))

/** 커스텀 추가 속성 필드들의 명칭 및 값 목록 */
const customSubFields = computed(() => {
  if (!props.customSubFieldKeys?.length || !props.fields?.length) return []
  return props.customSubFieldKeys.map((key) => {
    const f = props.fields.find((fd) => fd.key === key || fd.id === key)
    let label = key
    if (f) {
      if (typeof f.name === 'object') {
        label = (f.name as Record<string, string>)[locale.value] || (f.name as Record<string, string>).ko || (f.name as Record<string, string>).en || key
      } else if (typeof f.name === 'string') {
        try {
          const parsed = JSON.parse(f.name)
          label = parsed[locale.value] || parsed.ko || parsed.en || f.name
        } catch {
          label = f.name
        }
      }
    }
    const val = resolveValue(key) || '-'
    return { key, label, value: val }
  })
})

const userTimezone = useCookie('user_timezone', { default: () => Intl.DateTimeFormat().resolvedOptions().timeZone || 'Asia/Seoul' })

const formatDateTime = (dateVal: any) => {
  if (!dateVal) return ''
  try {
    let d: Date
    if (typeof dateVal === 'string') {
      let isoStr = dateVal
      if (!isoStr.endsWith('Z') && !isoStr.includes('+') && !isoStr.includes('-', 10)) {
        isoStr += 'Z'
      }
      d = new Date(isoStr)
    } else {
      d = new Date(dateVal)
    }
    if (isNaN(d.getTime())) return String(dateVal)
    return new Intl.DateTimeFormat(locale.value === 'en' ? 'en-US' : 'ko-KR', {
      timeZone: userTimezone.value || 'Asia/Seoul',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    }).format(d)
  } catch (e) {
    return String(dateVal)
  }
}

const createdByValue = computed(() => {
  return props.recordData?.createdBy || props.recordData?.created_by || props.recordData?.creator || 'system'
})

const createdAtFormatted = computed(() => {
  const raw = props.recordData?.createdAt || props.recordData?.created_at || props.recordData?.createDate
  return raw ? formatDateTime(raw) : ''
})

const updatedByValue = computed(() => {
  return props.recordData?.updatedBy || props.recordData?.updated_by || props.recordData?.updater || createdByValue.value
})

const updatedAtFormatted = computed(() => {
  const raw = props.recordData?.updatedAt || props.recordData?.updated_at || props.recordData?.updateDate
  return raw ? formatDateTime(raw) : createdAtFormatted.value
})

const versionValue = computed(() => {
  return props.recordData?.version || props.recordData?.ver || ''
})

const recordStatus = computed(() => {
  return props.recordData?.status || ''
})

const statusLabel = computed(() => {
  const s = String(recordStatus.value).toUpperCase()
  if (s === 'ACTIVE') return locale.value === 'en' ? 'ACTIVE' : '활성'
  if (s === 'PENDING_APPROVAL') return locale.value === 'en' ? 'PENDING' : '승인대기'
  if (s === 'DELETED') return locale.value === 'en' ? 'DELETED' : '삭제됨'
  return s
})

/** 도메인 이름 다국어 처리 */
const domainLabel = computed(() => {
  const n = props.domain?.name
  if (!n) return ''
  if (typeof n === 'object') return (n as Record<string, string>)[locale.value] || (n as Record<string, string>).ko || (n as Record<string, string>).en || ''
  try {
    const parsed = JSON.parse(n as string)
    return parsed[locale.value] || parsed.ko || parsed.en || n
  } catch {
    return n as string
  }
})

const domainIcon = computed(() => props.domain?.icon || '')

/** 도메인에 4가지 속성 필드 중 하나라도 설정된 경우에만 위젯 표시 */
const hasAnyAttributeField = computed(() =>
  !!(
    props.domain?.identifierFieldId ||
    props.domain?.displayNameFieldId ||
    props.domain?.descriptionFieldId ||
    props.domain?.imageFieldId
  )
)
</script>

<style scoped>
.domain-record-header-widget {
  border-radius: 10px;
  background: var(--va-background-primary);
  margin-bottom: 1rem;
  border: 1px solid var(--va-background-border);
}

.widget-content {
  padding: 0.85rem 1.15rem;
}

.header-row {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  flex-wrap: wrap;
}

.avatar-area {
  flex-shrink: 0;
  margin-top: 2px;
}

.record-avatar {
  border: 2px solid var(--va-background-border);
}

.main-info {
  flex: 1;
  min-width: 240px;
  display: flex;
  flex-direction: column;
}

.header-top-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
  width: 100%;
}

.header-title-group {
  flex: 1;
  min-width: 0;
}

.audit-meta-block {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  margin-left: auto;
  font-size: 0.72rem;
  line-height: 1.4;
}

.audit-meta-grid {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.audit-meta-col {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  text-align: right;
}

.audit-col-divider {
  width: 1px;
  height: 22px;
  background: var(--va-background-border, rgba(0, 0, 0, 0.08));
}

.audit-user-line {
  display: flex;
  align-items: center;
  gap: 3px;
  white-space: nowrap;
}

.audit-meta-label {
  font-weight: 500;
  color: var(--va-text-secondary, #64748b);
  font-size: 0.7rem;
}

.audit-meta-value {
  font-weight: 600;
  color: var(--va-text-primary, #334155);
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.audit-date-line {
  font-size: 0.68rem;
  color: var(--va-text-secondary, #94a3b8);
  font-variant-numeric: tabular-nums;
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

.version-tag {
  display: inline-block;
  background: rgba(0, 0, 0, 0.05);
  color: var(--va-text-secondary, #475569);
  border: 1px solid var(--va-background-border, rgba(0, 0, 0, 0.08));
  font-size: 0.6rem;
  font-weight: 700;
  padding: 0px 3px;
  border-radius: 3px;
}

.status-badge {
  font-size: 0.68rem;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 4px;
  margin-left: 4px;
  text-transform: uppercase;
}

.status-badge.status-active {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.status-badge.status-pending_approval {
  background: rgba(245, 158, 11, 0.15);
  color: #f59e0b;
  border: 1px solid rgba(245, 158, 11, 0.3);
}

.status-badge.status-deleted {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.name-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-bottom: 0.2rem;
}

.record-name {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--va-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-name.placeholder {
  color: var(--va-text-secondary);
  font-weight: 400;
}

.id-badge {
  font-size: 0.75rem;
  padding: 2px 8px;
  border-radius: 4px;
  background: var(--va-background-element);
  color: var(--va-text-secondary);
  font-family: monospace;
  font-weight: 600;
  white-space: nowrap;
  flex-shrink: 0;
}

.desc-text {
  font-size: 0.88rem;
  color: var(--va-text-secondary);
  margin-bottom: 0.3rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.domain-label-row {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 0.25rem;
}

.domain-label-text {
  font-size: 0.78rem;
  color: var(--va-text-secondary);
}

.custom-subfields-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
  padding-top: 6px;
  border-top: 1px dashed var(--va-background-border);
}

.subfield-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 0.75rem;
  background: var(--va-background-element);
  border: 1px solid var(--va-background-border);
  border-radius: 4px;
  padding: 2px 8px;
}

.subfield-chip .subfield-label {
  color: var(--va-text-secondary);
}

.subfield-chip .subfield-val {
  color: var(--va-primary);
  font-weight: 600;
}
</style>
