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

        <!-- 이름 + 식별코드 -->
        <div class="main-info">
          <div class="name-row">
            <span v-if="nameValue" class="record-name">{{ nameValue }}</span>
            <span v-else class="record-name placeholder">—</span>
            <span v-if="idValue" class="id-badge">{{ idValue }}</span>
          </div>
          <div v-if="descValue" class="desc-text">{{ descValue }}</div>
          <!-- 도메인 이름 표시 -->
          <div class="domain-label-row">
            <va-icon name="folder_open" size="14px" color="secondary" />
            <span class="domain-label-text">{{ domainLabel }}</span>
          </div>
        </div>
      </div>
    </va-card-content>
  </va-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface FieldDef {
  id: string
  key: string
  name: Record<string, string> | string
  type?: string
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
  if (raw === undefined || raw === null) return ''
  if (typeof raw === 'object') {
    return raw[locale.value] || raw.ko || raw.en || JSON.stringify(raw)
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
  padding: 1rem 1.25rem;
}

.header-row {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.avatar-area {
  flex-shrink: 0;
}

.record-avatar {
  border: 2px solid var(--va-background-border);
}

.main-info {
  flex: 1;
  min-width: 0;
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
</style>
