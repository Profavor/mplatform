<template>
  <va-card class="section-card todo-card">
    <va-card-title class="card-header-title">
      <va-icon name="task" size="small" color="warning" />
      {{ t('my_to_do_list', '내 결재 대기 작업') }}
    </va-card-title>
    <va-card-content>
      <div v-if="!todos || todos.length === 0" class="empty-todo-state">
        <va-icon name="check_circle_outline" size="2.5rem" color="success" />
        <p>{{ t('no_pending_tasks_you', '처리 대기 중인 결재 작업이 없습니다.') }}</p>
      </div>
      <div v-else class="todo-list">
        <div v-for="todo in todos" :key="todo.id" class="todo-item-card">
          <div class="todo-item-main">
            <div class="todo-badges">
              <va-badge :text="getStepTypeLabel(todo.stepType)" :color="todo.stepType === 'CONSENSUS' ? 'warning' : 'danger'" class="badge-bold" />
              <va-badge :text="getActionTypeLabel(todo.approvalRequest?.changes)" color="info" outline class="badge-bold" />
            </div>

            <div class="todo-details">
              <div v-if="todo.approvalRequest?.classificationNode" class="todo-node-info">
                <span><strong>{{ t('domain', '도메인') }}:</strong> {{ todo.approvalRequest.classificationNode.domainName?.[currentLocale] || todo.approvalRequest.classificationNode.domainName?.['en'] || 'Unknown' }}</span>
                <span><strong>{{ t('classification', '분류') }}:</strong> {{ todo.approvalRequest.classificationNode.name?.[currentLocale] || todo.approvalRequest.classificationNode.name?.['en'] || 'Unknown' }}</span>
              </div>
              <div class="todo-requester">
                <strong>{{ t('requester', '기안자') }}:</strong> {{ todo.approvalRequest?.requesterName || todo.approvalRequest?.requesterId || 'Unknown' }}
              </div>
              <div class="todo-date">
                <strong>{{ t('date', '기안일시') }}:</strong> {{ formatDateTime(todo.approvalRequest?.createdAt) }}
              </div>
            </div>
          </div>

          <!-- Display info snippet -->
          <div class="todo-info-box">
            <div v-if="displayInfo[todo.id]?.displayId || displayInfo[todo.id]?.displayName" class="info-snippet">
              <div v-if="displayInfo[todo.id]?.displayId" class="info-id">
                {{ displayInfo[todo.id].idField?.name?.[currentLocale] || displayInfo[todo.id].idField?.name?.ko || displayInfo[todo.id].idField?.name?.en || 'ID' }}: {{ displayInfo[todo.id].displayId }}
              </div>
              <div v-if="displayInfo[todo.id]?.displayName" class="info-name">
                {{ displayInfo[todo.id].nameField?.name?.[currentLocale] || displayInfo[todo.id].nameField?.name?.ko || displayInfo[todo.id].nameField?.name?.en || 'Name' }}: {{ displayInfo[todo.id].displayName }}
              </div>
            </div>
            <div v-else class="info-snippet-fallback">
              <em>{{ t('waiting_for_field_data', '필드 정보 조회 대기 중') }}</em>
            </div>
          </div>

          <div class="todo-action">
            <va-button size="small" color="primary" class="review-btn" @click="onGoToApprovals(todo)">
              {{ t('review', '검토') }}
            </va-button>
          </div>
        </div>
      </div>
    </va-card-content>
  </va-card>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { formatWithTimezone } from '~/composables/useTimezoneDate'

const { t } = useI18n()

const props = defineProps<{
  todos: any[]
  displayInfo: Record<string, any>
  currentLocale: string
}>()

const emit = defineEmits<{
  (e: 'review', todo: any): void
}>()

const onGoToApprovals = (todo: any) => {
  emit('review', todo)
}

const getStepTypeLabel = (stepType?: string) => {
  if (stepType === 'CONSENSUS') return t('consensus_approval', '합의')
  if (stepType === 'APPROVAL') return t('standard_approval', '승인')
  return stepType || t('approval', '결재')
}

const getActionTypeLabel = (changes?: any[]) => {
  if (!changes || changes.length === 0) return t('action_unknown', '변경')
  const action = changes[0]?.action
  if (action === 'CREATE') return t('action_create', '신규 생성')
  if (action === 'UPDATE') return t('action_update', '수정 변경')
  if (action === 'DELETE') return t('action_delete', '삭제 요청')
  return action || t('action_change', '변경')
}

const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return '-'
  try {
    return formatWithTimezone(dateStr, 'YYYY-MM-DD HH:mm')
  } catch (e) {
    return dateStr
  }
}

defineExpose({
  onGoToApprovals,
  getStepTypeLabel,
  getActionTypeLabel
})
</script>

<style scoped>
.section-card {
  border-radius: 12px;
  border: 1px solid var(--va-background-border);
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
  background: var(--va-background-primary);
}

.card-header-title {
  font-size: 1rem;
  font-weight: 700;
  color: var(--va-text-primary);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem 1.25rem 0.5rem 1.25rem;
}

.empty-todo-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2.5rem 1rem;
  color: var(--va-text-secondary);
  gap: 0.75rem;
}

.empty-todo-state p {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 600;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 0.5rem 0;
}

.todo-item-card {
  background: var(--va-background-element);
  border: 1px solid var(--va-background-border);
  border-radius: 10px;
  padding: 1rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.todo-item-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.todo-item-main {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  flex: 1;
}

.todo-badges {
  display: flex;
  gap: 0.5rem;
}

.badge-bold {
  font-weight: 700;
  font-size: 0.75rem;
}

.todo-details {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  font-size: 0.82rem;
  color: var(--va-text-secondary);
}

.todo-node-info {
  display: flex;
  gap: 0.75rem;
}

.todo-info-box {
  flex: 1;
  background: var(--va-background-primary);
  border: 1px solid var(--va-background-border);
  border-radius: 8px;
  padding: 0.5rem 0.75rem;
  font-size: 0.82rem;
}

.info-snippet {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.info-id {
  font-family: monospace;
  font-weight: 700;
  color: var(--va-primary);
}

.info-name {
  color: var(--va-text-primary);
  font-weight: 600;
}

.info-snippet-fallback {
  color: var(--va-text-secondary);
  font-style: italic;
}

.todo-action {
  flex: 0 0 auto;
}

.review-btn {
  font-weight: 700;
  border-radius: 8px;
}
</style>
