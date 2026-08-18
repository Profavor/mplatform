<template>
  <div class="message-detail-container">
    <div v-if="!message" class="empty-state">
      <va-icon name="mail" size="large" color="secondary" />
      <p>{{ $t('inbox.no_message_selected') }}</p>
    </div>
    <div v-else class="message-content">
      <div class="message-header">
        <div class="subject-row">
          <h2 class="subject">{{ message.subject }}</h2>
          <va-badge v-if="message.importance === 'URGENT'" :text="$t('inbox.importance_urgent')" color="danger" />
          <va-badge v-else-if="message.importance === 'HIGH'" :text="$t('inbox.importance_high')" color="warning" />
        </div>
        <div class="sender-info">
          <va-avatar size="medium">{{ senderInitials }}</va-avatar>
          <div class="sender-details">
            <div class="sender-name">{{ senderName }} <span class="sender-email">&lt;{{ message.senderEmail }}&gt;</span></div>
            <div class="message-date">{{ formattedDate }}</div>
          </div>
        </div>
        <div class="recipients-info">
          <div class="recipient-row" v-if="message.toRecipients && message.toRecipients.length">
            <span class="label">{{ $t('inbox.recipient_to') }}:</span>
            <div class="recipient-chip-group">
              <div v-for="r in message.toRecipients" :key="r.email || r.userId" class="recipient-badge-item">
                <va-chip size="small" flat>{{ getRecipientDisplayName(r) }}</va-chip>
                <template v-if="isSentFolder">
                  <va-badge v-if="r.isRecalled" :text="$t('inbox.recalled')" color="danger" size="small" />
                  <va-badge v-else-if="r.isRead" :text="$t('inbox.read') + (r.readAt ? ' ' + formatTime(r.readAt) : '')" color="success" size="small" />
                  <va-badge v-else :text="$t('inbox.unread')" color="secondary" size="small" />
                </template>
              </div>
            </div>
          </div>
          <div class="recipient-row" v-if="message.ccRecipients && message.ccRecipients.length">
            <span class="label">{{ $t('inbox.recipient_cc') }}:</span>
            <div class="recipient-chip-group">
              <div v-for="r in message.ccRecipients" :key="r.email || r.userId" class="recipient-badge-item">
                <va-chip size="small" flat>{{ getRecipientDisplayName(r) }}</va-chip>
                <template v-if="isSentFolder">
                  <va-badge v-if="r.isRecalled" :text="$t('inbox.recalled')" color="danger" size="small" />
                  <va-badge v-else-if="r.isRead" :text="$t('inbox.read') + (r.readAt ? ' ' + formatTime(r.readAt) : '')" color="success" size="small" />
                  <va-badge v-else :text="$t('inbox.unread')" color="secondary" size="small" />
                </template>
              </div>
            </div>
          </div>
        </div>
        <div class="action-bar">
          <va-button preset="secondary" size="small" icon="reply" @click="$emit('reply', message)">{{ $t('inbox.reply') }}</va-button>
          <va-button preset="secondary" size="small" icon="reply_all" @click="$emit('reply-all', message)">{{ $t('inbox.reply_all') }}</va-button>
          <va-button preset="secondary" size="small" icon="forward" @click="$emit('forward', message)">{{ $t('inbox.forward') }}</va-button>
          <va-button
            v-if="isSentFolder"
            preset="secondary"
            size="small"
            color="warning"
            icon="undo"
            class="recall-btn"
            @click="showRecallConfirm = true"
          >
            {{ $t('inbox.recall_message') }}
          </va-button>
          <div class="spacer"></div>
          <va-button preset="secondary" size="small" icon="archive" @click="$emit('archive', message.id)">{{ $t('inbox.move_to_archive') }}</va-button>
          <va-button preset="secondary" size="small" color="danger" icon="delete" @click="$emit('delete', message.id)">{{ $t('inbox.delete') }}</va-button>
        </div>
      </div>
      
      <!-- Top Attachments Section (Directly below headers and above message body) -->
      <div v-if="displayAttachments && displayAttachments.length" class="top-attachments-wrapper">
        <div class="attachments-header">
          <div class="attachments-title-group">
            <va-icon name="attach_file" size="small" color="primary" />
            <span class="attachments-title">{{ $t('inbox.attachments') }}</span>
            <span class="count-badge">({{ displayAttachments.length }})</span>
            <span v-if="totalAttachmentsSize" class="total-size-badge">{{ totalAttachmentsSize }}</span>
          </div>
          <va-button
            v-if="displayAttachments.length > 1"
            preset="secondary"
            size="small"
            icon="download_for_offline"
            class="download-all-btn"
            @click="downloadAllAttachments"
          >
            {{ $t('inbox.attachment_download_all', '전체 다운로드') }}
          </va-button>
        </div>

        <div class="attachments-cards-grid">
          <div
            v-for="att in displayAttachments"
            :key="att.id"
            class="attachment-card-item"
          >
            <div class="file-icon-box" :class="getFileFormatClass(att.fileName)">
              <va-icon :name="getFileIcon(att.fileName)" size="small" />
            </div>
            <div class="file-info-area" :title="att.fileName">
              <span class="file-name-text">{{ att.fileName }}</span>
              <span v-if="formatFileSize(att.fileSize)" class="file-size-text">{{ formatFileSize(att.fileSize) }}</span>
            </div>
            <div class="file-action-area">
              <va-button
                preset="secondary"
                size="small"
                icon="download"
                class="card-download-btn"
                :title="$t('inbox.attachment_download', '다운로드')"
                @click="downloadAttachment(att)"
              />
            </div>
          </div>
        </div>
      </div>

      <va-divider class="body-divider" />
      
      <!-- Rich Text Body Container (Cleaned of bottom attachment links) -->
      <div class="message-body" v-html="cleanedBody"></div>
    </div>

    <!-- 발송 취소 확인 모달 -->
    <va-modal
      v-model="showRecallConfirm"
      :title="$t('inbox.recall_message')"
      :message="$t('inbox.recall_confirm')"
      :ok-text="$t('inbox.recall_message')"
      :cancel-text="$t('cancel')"
      @ok="handleRecallMessage"
    />

    <!-- 발송 취소 결과 모달 -->
    <va-modal
      v-model="showRecallResult"
      :title="$t('inbox.recall_success')"
      hide-default-actions
    >
      <div v-if="recallResult" class="recall-result-content">
        <div class="recall-summary-cards">
          <div class="summary-card total">
            <span class="count">{{ recallResult.totalRecipients }}</span>
            <span class="label">{{ $t('inbox.recall_result_total', { count: recallResult.totalRecipients }) }}</span>
          </div>
          <div class="summary-card before">
            <span class="count">{{ recallResult.recalledBeforeReadCount }}</span>
            <span class="label">{{ $t('inbox.recall_result_before_read', { count: recallResult.recalledBeforeReadCount }) }}</span>
          </div>
          <div class="summary-card after">
            <span class="count">{{ recallResult.recalledAfterReadCount }}</span>
            <span class="label">{{ $t('inbox.recall_result_after_read', { count: recallResult.recalledAfterReadCount }) }}</span>
          </div>
          <div v-if="recallResult.externalCount > 0" class="summary-card external">
            <span class="count">{{ recallResult.externalCount }}</span>
            <span class="label">{{ $t('inbox.recall_result_external', { count: recallResult.externalCount }) }}</span>
          </div>
        </div>

        <div class="recall-details-list">
          <div v-for="d in recallResult.details" :key="d.userId || d.email" class="detail-row">
            <div class="detail-user">
              <strong>{{ d.userId ? userStore.getUserName(d.userId, d.name) : d.email }}</strong>
              <span class="detail-email">{{ d.email }}</span>
            </div>
            <div class="detail-status">
              <va-badge
                v-if="d.status === 'RECALLED_BEFORE_READ'"
                :text="$t('inbox.recipient_recall_status_before')"
                color="success"
              />
              <va-badge
                v-else-if="d.status === 'RECALLED_AFTER_READ'"
                :text="$t('inbox.recipient_recall_status_after', { time: d.readAt ? formatTime(d.readAt) : '' })"
                color="warning"
              />
              <va-badge
                v-else
                :text="$t('inbox.recipient_recall_status_external')"
                color="secondary"
              />
            </div>
          </div>
        </div>

        <div class="modal-footer-actions">
          <va-button preset="primary" @click="showRecallResult = false">{{ $t('inbox.close') }}</va-button>
        </div>
      </div>
    </va-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useInbox, type InboxMessage, type RecipientInfo, type RecallResultResponse } from '~/composables/useInbox'
import { useUserStore } from '~/stores/useUserStore'
import { useTimezoneDate } from '~/composables/useTimezoneDate'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { useCookie } from '#app'

const props = defineProps<{
  message: InboxMessage | null
  activeFolder?: string
}>()

const emit = defineEmits(['reply', 'reply-all', 'forward', 'delete', 'archive', 'refresh'])

const { t } = useI18n()
const userStore = useUserStore()
const { formatWithTimezone } = useTimezoneDate()
const { recallMessage } = useInbox()

const showRecallConfirm = ref(false)
const showRecallResult = ref(false)
const recallResult = ref<RecallResultResponse | null>(null)

const isSentFolder = computed(() => {
  return props.activeFolder === 'SENT' || props.message?.folder === 'SENT'
})

const senderName = computed(() => {
  if (!props.message) return ''
  return userStore.getUserName(props.message.senderId, props.message.senderName) || props.message.senderEmail
})

const senderInitials = computed(() => {
  return senderName.value ? senderName.value.substring(0, 2).toUpperCase() : '?'
})

const formattedDate = computed(() => {
  if (!props.message?.createdAt) return ''
  return formatTime(props.message.createdAt)
})

interface DisplayAttachment {
  id: string
  fileName: string
  fileSize?: number
  downloadUrl?: string
}

const asyncFileSizes = ref<Record<string, number>>({})

const fetchFileSize = async (url: string, id: string) => {
  if (!url || asyncFileSizes.value[id] !== undefined || !process.client) return

  // 1. Direct size from URL param
  const sizeParam = url.match(/[?&]size=(\d+)/)
  if (sizeParam) {
    const bytes = parseInt(sizeParam[1], 10)
    if (!isNaN(bytes) && bytes > 0) {
      asyncFileSizes.value[id] = bytes
      return
    }
  }

  // 2. Fetch from /api/files/info/{fileName}
  if (url.includes('/api/files/download/')) {
    const fileNamePart = url.split('/api/files/download/')[1].split('?')[0]
    if (fileNamePart) {
      try {
        const res = await fetch(`/api/files/info/${encodeURIComponent(fileNamePart)}`)
        if (res.ok) {
          const data = await res.json().catch(() => null)
          if (data && data.size) {
            const bytes = Number(data.size)
            if (!isNaN(bytes) && bytes > 0) {
              asyncFileSizes.value[id] = bytes
              return
            }
          }
        }
      } catch (err) {}
    }
  }

  asyncFileSizes.value[id] = 0
}

const displayAttachments = computed<DisplayAttachment[]>(() => {
  const list: DisplayAttachment[] = []
  const seenUrls = new Set<string>()

  // 1. Explicit message.attachments
  if (props.message?.attachments && Array.isArray(props.message.attachments)) {
    for (const att of props.message.attachments) {
      if (att && att.fileName) {
        const downloadUrl = (att as any).downloadUrl || 
          ((att as any).filePath ? `/api/files/download/${encodeURIComponent((att as any).filePath)}?name=${encodeURIComponent(att.fileName)}` : `/api/files/download/${encodeURIComponent(att.fileName)}?name=${encodeURIComponent(att.fileName)}`)
        const id = String(att.id || att.fileName)
        let size = att.fileSize || asyncFileSizes.value[id] || 0
        if (!size && downloadUrl) {
          const match = downloadUrl.match(/[?&]size=(\d+)/)
          if (match) size = parseInt(match[1], 10)
        }
        list.push({
          id,
          fileName: att.fileName,
          fileSize: size,
          downloadUrl
        })
        seenUrls.add(att.fileName)
      }
    }
  }

  // 2. Parse any attachment download links from message.body
  if (props.message?.body) {
    const regex = /<a\s+[^>]*href=["']([^"']+)["'][^>]*>([\s\S]*?)<\/a>/gi
    let match: RegExpExecArray | null
    while ((match = regex.exec(props.message.body)) !== null) {
      const url = match[1]
      let rawText = match[2]?.replace(/<[^>]+>/g, '').replace(/^[📎\s]+/, '').trim()
      
      let name = rawText
      if (url.includes('name=')) {
        try {
          name = decodeURIComponent(url.split('name=')[1].split('&')[0])
        } catch {}
      }
      
      const id = `parsed_${url}_${name}`
      let size = asyncFileSizes.value[id] || 0
      if (!size) {
        const sizeMatch = url.match(/[?&]size=(\d+)/)
        if (sizeMatch) {
          size = parseInt(sizeMatch[1], 10)
        }
      }
      
      if (name && !seenUrls.has(name) && (url.includes('/api/files/') || url.includes('/download') || url.includes('attachment') || rawText.includes('.'))) {
        seenUrls.add(name)
        list.push({
          id,
          fileName: name,
          fileSize: size,
          downloadUrl: url
        })
      }
    }
  }

  return list
})

watch(
  displayAttachments,
  (attachments) => {
    if (process.client) {
      for (const att of attachments) {
        if (!att.fileSize && att.downloadUrl) {
          fetchFileSize(att.downloadUrl, att.id)
        }
      }
    }
  },
  { immediate: true }
)

const cleanedBody = computed(() => {
  let body = props.message?.body || ''
  if (!body) return ''

  // Strip trailing <hr/><h4>첨부파일</h4>... or <h4>Attachments</h4>... block
  body = body.replace(/<hr\s*\/?>\s*<h4[^>]*>(?:첨부파일|Attachments|Attachment)<\/h4>[\s\S]*?(?:<\/p>|<\/div>|$)/gi, '')
  body = body.replace(/<h4[^>]*>(?:첨부파일|Attachments|Attachment)<\/h4>[\s\S]*?(?:<hr\s*\/?>|$)/gi, '')
  
  // Strip isolated attachment link paragraphs like <p><a href="/api/files/download/...">📎 ...</a></p>
  body = body.replace(/<p>\s*<a\s+[^>]*href=["']\/api\/files\/download\/[^"']+["'][^>]*>[\s\S]*?<\/a>\s*<\/p>/gi, '')

  return body
})

const totalAttachmentsSize = computed(() => {
  if (!displayAttachments.value.length) return ''
  const totalBytes = displayAttachments.value.reduce((acc, a) => acc + (a.fileSize || 0), 0)
  if (totalBytes === 0) return ''
  return `(${formatFileSize(totalBytes)})`
})

const formatTime = (dateStr: string) => {
  if (!dateStr) return ''
  try {
    return formatWithTimezone(dateStr)
  } catch (e) {
    return new Date(dateStr).toLocaleString()
  }
}

const formatFileSize = (bytes?: number) => {
  if (bytes === undefined || bytes === null || bytes <= 0) return ''
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

const getFileIcon = (fileName?: string) => {
  if (!fileName) return 'insert_drive_file'
  const ext = fileName.split('.').pop()?.toLowerCase() || ''
  if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg'].includes(ext)) return 'image'
  if (['pdf'].includes(ext)) return 'picture_as_pdf'
  if (['xls', 'xlsx', 'csv'].includes(ext)) return 'table_chart'
  if (['doc', 'docx', 'txt', 'rtf'].includes(ext)) return 'description'
  if (['zip', 'rar', '7z', 'tar', 'gz'].includes(ext)) return 'folder_zip'
  if (['ppt', 'pptx'].includes(ext)) return 'slideshow'
  return 'insert_drive_file'
}

const getFileFormatClass = (fileName?: string) => {
  if (!fileName) return 'format-other'
  const ext = fileName.split('.').pop()?.toLowerCase() || ''
  if (['xls', 'xlsx', 'csv'].includes(ext)) return 'format-excel'
  if (['pdf'].includes(ext)) return 'format-pdf'
  if (['doc', 'docx'].includes(ext)) return 'format-word'
  if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg'].includes(ext)) return 'format-image'
  if (['zip', 'rar', '7z', 'tar', 'gz'].includes(ext)) return 'format-zip'
  return 'format-other'
}

const downloadAttachment = (att: DisplayAttachment) => {
  if (!att) return
  const link = document.createElement('a')
  const downloadHref = att.downloadUrl || `/api/files/download/${encodeURIComponent(att.fileName)}?name=${encodeURIComponent(att.fileName)}`
  link.href = downloadHref
  link.download = att.fileName
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const downloadAllAttachments = () => {
  if (!displayAttachments.value.length) return
  displayAttachments.value.forEach(att => {
    downloadAttachment(att)
  })
}

const getRecipientDisplayName = (r: RecipientInfo) => {
  if (r.userId) {
    return userStore.getUserName(r.userId, r.name) || r.email || r.userId
  }
  return r.name || r.email
}

const handleRecallMessage = async () => {
  if (!props.message?.id) return
  try {
    const result = await recallMessage(props.message.id)
    recallResult.value = result
    showRecallResult.value = true
    emit('refresh')
  } catch (e) {
    console.error('Failed to recall message:', e)
  }
}
</script>

<style scoped>
.message-detail-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--va-background-primary);
  border-left: 1px solid var(--va-background-border);
  overflow-y: auto;
}
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--va-secondary);
  gap: 1rem;
}
.message-content {
  display: flex;
  flex-direction: column;
}
.message-header {
  padding: 1.25rem 1.5rem 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.subject-row {
  display: flex;
  align-items: center;
  gap: 1rem;
}
.subject {
  margin: 0;
  font-size: 1.35rem;
  font-weight: 700;
  word-break: break-word;
}
.sender-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}
.sender-details {
  display: flex;
  flex-direction: column;
}
.sender-name {
  font-weight: 600;
  font-size: 0.95rem;
}
.sender-email {
  color: var(--va-secondary);
  font-size: 0.85rem;
}
.message-date {
  font-size: 0.8rem;
  color: var(--va-secondary);
}
.recipients-info {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}
.recipient-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.recipient-chip-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}
.recipient-badge-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.label {
  font-size: 0.85rem;
  color: var(--va-secondary);
  min-width: 40px;
}
.action-bar {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.25rem;
  flex-wrap: wrap;
}
.spacer {
  flex-grow: 1;
}

/* Top Attachments Section */
.top-attachments-wrapper {
  margin: 0.5rem 1.5rem;
  padding: 0.85rem 1rem;
  background: var(--va-background-element, #f8fafc);
  border: 1px solid var(--va-background-border, #e2e8f0);
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

:global([data-vuestic-preset="dark"]) .top-attachments-wrapper,
:global(.va-theme-dark) .top-attachments-wrapper {
  background: #1e293b !important;
  border-color: #334155 !important;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3) !important;
}

.attachments-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.65rem;
}

.attachments-title-group {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.attachments-title {
  font-weight: 700;
  font-size: 0.9rem;
  color: var(--va-text-primary);
}

:global([data-vuestic-preset="dark"]) .attachments-title,
:global(.va-theme-dark) .attachments-title {
  color: #f8fafc !important;
}

.count-badge {
  font-weight: 700;
  color: #3b82f6;
  font-size: 0.85rem;
}

.total-size-badge {
  font-size: 0.8rem;
  color: var(--va-secondary, #64748b);
  margin-left: 0.25rem;
}

:global([data-vuestic-preset="dark"]) .total-size-badge,
:global(.va-theme-dark) .total-size-badge {
  color: #94a3b8 !important;
}

.download-all-btn {
  font-size: 0.8rem !important;
  padding: 0.2rem 0.6rem !important;
}

.attachments-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 0.6rem;
}

.attachment-card-item {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  padding: 0.6rem 0.85rem;
  background: var(--va-background-primary, #ffffff);
  border: 1px solid var(--va-background-border, #e2e8f0);
  border-radius: 8px;
  transition: all 0.2s ease;
}

.attachment-card-item:hover {
  border-color: #3b82f6;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
}

:global([data-vuestic-preset="dark"]) .attachment-card-item,
:global(.va-theme-dark) .attachment-card-item {
  background: #1e293b !important;
  border-color: #334155 !important;
}

:global([data-vuestic-preset="dark"]) .attachment-card-item:hover,
:global(.va-theme-dark) .attachment-card-item:hover {
  border-color: #60a5fa !important;
  box-shadow: 0 2px 8px rgba(96, 165, 250, 0.25) !important;
}

.file-icon-box {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.file-icon-box.format-excel {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
}

.file-icon-box.format-pdf {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

.file-icon-box.format-word {
  background: rgba(59, 130, 246, 0.15);
  color: #3b82f6;
}

.file-icon-box.format-image {
  background: rgba(245, 158, 11, 0.15);
  color: #f59e0b;
}

.file-icon-box.format-zip {
  background: rgba(168, 85, 247, 0.15);
  color: #a855f7;
}

.file-icon-box.format-other {
  background: rgba(100, 116, 139, 0.15);
  color: #64748b;
}

.file-info-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.file-name-text {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--va-text-primary, #0f172a);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

:global([data-vuestic-preset="dark"]) .file-name-text,
:global(.va-theme-dark) .file-name-text {
  color: #f8fafc !important;
}

.file-size-text {
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--va-text-secondary, #64748b);
}

:global([data-vuestic-preset="dark"]) .file-size-text,
:global(.va-theme-dark) .file-size-text {
  color: #94a3b8 !important;
}

.card-download-btn {
  padding: 0.2rem !important;
}

.body-divider {
  margin: 0 !important;
}

.message-body {
  padding: 1.5rem;
  line-height: 1.7;
  font-size: 0.95rem;
  color: var(--va-text-primary);
}

.message-body :deep(blockquote) {
  margin: 0.75rem 0;
  padding: 0.5rem 1rem;
  border-left: 3px solid #6366f1;
  background: rgba(99, 102, 241, 0.04);
  border-radius: 0 6px 6px 0;
}

:global([data-vuestic-preset="dark"]) .message-body,
:global(.va-theme-dark) .message-body {
  color: #f1f5f9 !important;
}

.recall-result-content {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding: 0.5rem;
}
.recall-summary-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 0.75rem;
}
.summary-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0.75rem;
  border-radius: 8px;
  background: var(--va-background-element);
}
.summary-card .count {
  font-size: 1.5rem;
  font-weight: 700;
}
.summary-card .label {
  font-size: 0.75rem;
  color: var(--va-secondary);
}
.recall-details-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-height: 240px;
  overflow-y: auto;
}
.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0.75rem;
  background: var(--va-background-element);
  border-radius: 6px;
}
.detail-user {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.detail-email {
  font-size: 0.8rem;
  color: var(--va-secondary);
}
.modal-footer-actions {
  display: flex;
  justify-content: flex-end;
}
</style>
