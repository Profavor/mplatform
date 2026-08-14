<template>
  <div class="chat-message-list-container" style="display: flex; flex-direction: column; gap: 0.75rem; padding: 1rem; overflow-y: auto;">
    <div v-if="messages.length === 0" style="text-align: center; color: var(--va-text-secondary); margin: auto; font-size: 0.9rem;">
      {{ t('messenger.noMessages') }}
    </div>

    <template v-for="(msg, idx) in messages" :key="msg.id || idx">
      <!-- System Message -->
      <div v-if="msg.messageType === 'SYSTEM'" style="text-align: center; margin: 0.5rem 0;">
        <span style="display: inline-block; background: rgba(0,0,0,0.06); padding: 4px 12px; border-radius: 12px; font-size: 0.75rem; color: var(--va-text-secondary);">
          {{ msg.content }}
        </span>
      </div>

      <!-- User Chat Message -->
      <div
        v-else
        :style="{
          display: 'flex',
          flexDirection: 'column',
          alignItems: isMyMessage(msg) ? 'flex-end' : 'flex-start',
          maxWidth: '80%',
          alignSelf: isMyMessage(msg) ? 'flex-end' : 'flex-start'
        }"
      >
        <!-- Sender Name (Other users only) -->
        <span
          v-if="!isMyMessage(msg)"
          style="font-size: 0.75rem; color: var(--va-text-secondary); margin-bottom: 2px; margin-left: 4px;"
        >
          {{ msg.senderName || msg.senderUsername || msg.senderId }}
        </span>

        <div style="display: flex; align-items: flex-end; gap: 6px;" :style="{ flexDirection: isMyMessage(msg) ? 'row' : 'row-reverse' }">
          <!-- Time & Unread Count -->
          <div style="display: flex; flex-direction: column; align-items: flex-end; font-size: 0.7rem; color: var(--va-text-secondary);">
            <span v-if="msg.unreadCount && msg.unreadCount > 0" style="color: var(--va-warning); font-weight: bold;">
              {{ msg.unreadCount }}
            </span>
            <span>{{ formatTime(msg.createdAt) }}</span>
          </div>

          <!-- Message Bubble -->
          <div
            :style="{
              padding: '8px 12px',
              borderRadius: '12px',
              backgroundColor: isMyMessage(msg) ? 'var(--va-primary)' : 'var(--va-background-element)',
              color: isMyMessage(msg) ? '#ffffff' : 'var(--va-text-primary)',
              border: isMyMessage(msg) ? 'none' : '1px solid var(--va-background-border)',
              wordBreak: 'break-word',
              fontSize: '0.9rem',
              lineHeight: '1.4'
            }"
          >
            <!-- Content -->
            <div>{{ msg.content }}</div>

            <!-- Attachments -->
            <div v-if="msg.attachments && msg.attachments.length > 0" style="margin-top: 6px; display: flex; flex-direction: column; gap: 4px;">
              <div
                v-for="(att, aIdx) in msg.attachments"
                :key="aIdx"
                style="display: flex; align-items: center; gap: 4px; padding: 4px 8px; border-radius: 6px; background: rgba(0,0,0,0.1); cursor: pointer;"
                @click="$emit('downloadAttachment', att)"
              >
                <va-icon name="attach_file" size="small" />
                <span style="font-size: 0.8rem; text-decoration: underline;">{{ att.name }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import type { ChatMessage, ChatAttachment } from '~/types/messenger'
import { useTimezoneDate } from '~/composables/useTimezoneDate'

const props = defineProps<{
  messages: ChatMessage[]
  currentUserId?: string
}>()

defineEmits<{
  (e: 'downloadAttachment', attachment: ChatAttachment): void
}>()

const { t } = useI18n()
const { formatWithTimezone } = useTimezoneDate()

const isMyMessage = (msg: ChatMessage) => {
  return props.currentUserId && msg.senderId === props.currentUserId
}

const formatTime = (dateStr: string) => {
  if (!dateStr) return ''
  return formatWithTimezone(dateStr, 'HH:mm')
}
</script>
