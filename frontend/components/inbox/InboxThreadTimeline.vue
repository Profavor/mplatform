<template>
  <div class="thread-timeline">
    <div 
      v-for="msg in messages" 
      :key="msg.id" 
      class="thread-item"
      :class="{ 'active': msg.id === currentMessageId }"
      @click="$emit('select', msg)"
    >
      <div class="thread-header">
        <span class="sender">{{ getSenderName(msg) }}</span>
        <span class="date">{{ formatDate(msg.createdAt) }}</span>
      </div>
      <div class="preview">{{ getPreview(msg.body) }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { InboxMessage } from '~/composables/useInbox'
import { useUserStore } from '~/stores/useUserStore'

defineProps<{
  messages: InboxMessage[]
  currentMessageId: string
}>()

defineEmits(['select'])

const userStore = useUserStore()

const getSenderName = (msg: InboxMessage) => {
  return userStore.getUserName(msg.senderId, msg.senderName) || msg.senderEmail
}

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString()
}

const getPreview = (html: string) => {
  if (!html) return ''
  const text = html.replace(/<[^>]+>/g, ' ')
  return text.substring(0, 100) + (text.length > 100 ? '...' : '')
}
</script>

<style scoped>
.thread-timeline {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 1rem;
}
.thread-item {
  padding: 0.75rem;
  border-radius: 8px;
  background: var(--va-background-element);
  cursor: pointer;
  border: 1px solid transparent;
}
.thread-item:hover {
  border-color: var(--va-background-border);
}
.thread-item.active {
  background: var(--va-primary-lighten);
  border-color: var(--va-primary);
}
.thread-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.25rem;
}
.sender {
  font-weight: 600;
  font-size: 0.9rem;
}
.date {
  font-size: 0.8rem;
  color: var(--va-secondary);
}
.preview {
  font-size: 0.85rem;
  color: var(--va-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
