<template>
  <div class="notification-bell-wrapper" style="display: inline-flex; align-items: center; gap: 0.25rem;">
    <!-- Mail Inbox Shortcut Button -->
    <div
      style="position: relative; display: inline-flex; align-items: center; justify-content: center; cursor: pointer;"
      @click="openInbox()"
    >
      <va-button
        preset="plain"
        class="notification-bell-btn"
        style="color: white !important; padding: 0.4rem; border-radius: 50%; min-width: 40px; height: 40px; display: inline-flex; align-items: center; justify-content: center; cursor: pointer;"
        :aria-label="$t('inbox.title')"
        :title="$t('inbox.title')"
        @click.stop="openInbox()"
      >
        <va-icon name="mail" size="22px" />
      </va-button>
      <span
        v-if="inboxUnreadCount > 0"
        style="position: absolute; top: 2px; right: 2px; background: #3b82f6; color: white; border-radius: 10px; padding: 1px 5px; font-size: 10px; font-weight: 700; line-height: 12px; min-width: 16px; text-align: center; border: 1.5px solid #1d4ed8; box-shadow: 0 2px 4px rgba(0,0,0,0.2); pointer-events: none;"
      >
        {{ inboxUnreadCount > 99 ? '99+' : inboxUnreadCount }}
      </span>
    </div>

    <va-dropdown placement="bottom-end" stick-to-edges class="notification-dropdown">
      <template #anchor>
        <div style="position: relative; display: inline-flex; align-items: center; justify-content: center;">
          <va-button
            preset="plain"
            class="notification-bell-btn"
            style="color: white !important; padding: 0.4rem; border-radius: 50%; min-width: 40px; height: 40px; display: inline-flex; align-items: center; justify-content: center;"
            :aria-label="$t('notifications.title')"
          >
            <va-icon name="notifications" size="24px" />
          </va-button>
          <span
            v-if="unreadCount > 0"
            style="position: absolute; top: 2px; right: 2px; background: #e53935; color: white; border-radius: 10px; padding: 1px 5px; font-size: 10px; font-weight: 700; line-height: 12px; min-width: 16px; text-align: center; border: 1.5px solid #2563eb; box-shadow: 0 2px 4px rgba(0,0,0,0.2); pointer-events: none;"
          >
            {{ unreadCount > 99 ? '99+' : unreadCount }}
          </span>
        </div>
      </template>

      <va-dropdown-content
        class="notification-dropdown-panel"
        :style="{
          padding: '0',
          width: '360px',
          maxWidth: '90vw',
          borderRadius: '16px',
          overflow: 'hidden',
          boxShadow: isDark ? '0 16px 45px rgba(0,0,0,0.7), 0 0 25px rgba(99,102,241,0.25)' : '0 12px 36px rgba(0,0,0,0.18)',
          border: isDark ? '1px solid rgba(139,92,246,0.35)' : '1px solid var(--va-background-border)',
          background: isDark ? 'rgba(31, 41, 55, 0.95)' : 'rgba(255, 255, 255, 0.95)',
          backdropFilter: 'blur(16px)'
        }"
      >
        <!-- Header -->
        <div class="notification-header">
          <div class="header-title-box">
            <va-icon name="notifications_active" size="20px" class="header-icon" />
            <span class="header-title">{{ $t('notifications.title') }}</span>
            <va-badge
              v-if="unreadCount > 0"
              :text="unreadCount"
              color="danger"
              size="small"
              class="header-unread-count"
            />
          </div>
          <div style="display: flex; align-items: center; gap: 0.25rem;">
            <va-button
              v-if="unreadCount > 0"
              preset="plain"
              size="small"
              color="primary"
              class="mark-all-btn"
              @click="markAllAsRead"
            >
              {{ $t('notifications.mark_all_read') }}
            </va-button>
            <va-button
              v-if="notifications.length > 0"
              preset="plain"
              size="small"
              color="secondary"
              class="clear-all-btn"
              title="알림 모두 비우기"
              @click="deleteAllNotifications"
            >
              <va-icon name="delete_sweep" size="18px" />
            </va-button>
          </div>
        </div>

        <va-divider style="margin: 0;" />

        <!-- Notification List -->
        <div class="notification-list">
          <template v-if="notifications.length > 0">
            <div
              v-for="item in notifications"
              :key="item.id"
              class="notification-item"
              :class="{ unread: !item.read }"
              @click="handleNotificationClick(item)"
            >
              <div class="item-left">
                <span class="type-dot" :class="getTypeClass(item.type)"></span>
              </div>

              <div class="item-body">
                <div class="item-top">
                  <div style="display: flex; align-items: center; gap: 0.35rem; flex-wrap: wrap;">
                    <va-badge
                      :color="item.message && item.message.includes('[처리 완료]') ? 'secondary' : getTypeBadgeColor(item.type)"
                      size="small"
                      class="type-badge"
                    >
                      {{ item.message && item.message.includes('[처리 완료]') ? '처리 완료' : getTypeLabel(item.type) }}
                    </va-badge>
                    <span v-if="parseNotificationContent(item).location" class="location-tag">
                      {{ parseNotificationContent(item).location }}
                    </span>
                  </div>
                  <span class="item-time">{{ formatTime(item.createdAt) }}</span>
                </div>
                <div class="item-title" :style="{ opacity: item.message && item.message.includes('[처리 완료]') ? 0.7 : 1 }">{{ formatTitle(item.title) }}</div>
                
                <div v-if="parseNotificationContent(item).mainText" class="item-main-text">
                  {{ parseNotificationContent(item).mainText }}
                </div>
                
                <div v-if="parseNotificationContent(item).detailText" class="item-detail-box">
                  <va-icon name="info" size="12px" color="primary" style="margin-right: 4px;" />
                  <span>{{ parseNotificationContent(item).detailText }}</span>
                </div>
              </div>

              <va-button
                preset="plain"
                size="small"
                color="secondary"
                class="item-delete-btn"
                title="삭제"
                @click.stop="deleteNotification(item)"
              >
                <va-icon name="close" size="14px" />
              </va-button>
            </div>
          </template>

          <!-- Empty State -->
          <div v-else class="empty-notifications">
            <va-icon name="notifications_off" size="40px" color="secondary" style="opacity: 0.5;" />
            <p class="empty-text">{{ $t('notifications.no_notifications') }}</p>
          </div>
        </div>
      </va-dropdown-content>
    </va-dropdown>

    <!-- Combined Global Modal for Approval Details & Action Review -->
    <AppModal v-model="showApprovalModal" size="large" hide-default-actions>
      <template #header>
        <div v-if="activeRequest" style="display: flex; align-items: center; justify-content: space-between; width: 100%;">
          <h3 style="margin: 0; font-size: 1.25rem; font-weight: 800; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
            <va-icon :name="isPendingAssignee ? 'rate_review' : 'verified_user'" color="primary" />
            {{ isPendingAssignee ? ($t('approval_review')) : ($t('details')) }}
          </h3>
          <div v-if="activeRequest.requesterName || activeRequest.requesterUsername || activeRequest.requesterId" style="font-size: 0.85rem; color: var(--va-text-secondary);">
            {{ $t('label_drafter') }}: {{ activeRequest.requesterName || activeRequest.requesterUsername || activeRequest.requesterId }}
          </div>
        </div>
      </template>

      <div v-if="activeRequest" style="padding: 0.5rem 0 0 0;">
        <!-- 1. Existing Approval Details & History Timeline -->
        <ApprovalDetailsViewer :request="activeRequest" />

        <!-- 2. Action Area (If user is pending assignee) -->
        <div v-if="isPendingAssignee" style="margin-top: 1.5rem; padding-top: 1rem; border-top: 1px dashed var(--va-background-border);">
          <div style="font-weight: 700; margin-bottom: 0.5rem; font-size: 0.95rem; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.4rem;">
            <va-icon name="edit_note" color="primary" size="18px" />
            심사 의견 작성
          </div>
          <textarea
            v-model="commentData" 
            placeholder="의견 추가 (선택)..." 
            style="width: 100%; box-sizing: border-box; background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 0.75rem 1rem; color: var(--va-text-primary); resize: vertical; min-height: 80px; font-family: inherit; font-size: 0.9rem; margin-bottom: 1rem;"
          ></textarea>
          <div style="display: flex; gap: 1rem;">
            <va-button color="success" icon="check" style="flex: 1;" @click="handleSingleAction('approve')">승인</va-button>
            <va-button color="danger" icon="close" preset="secondary" style="flex: 1;" @click="handleSingleAction('reject')">반려</va-button>
          </div>
        </div>
      </div>

      <template #footer>
        <div v-if="!isPendingAssignee" style="display: flex; justify-content: flex-end;">
          <va-button preset="secondary" @click="showApprovalModal = false">닫기</va-button>
        </div>
      </template>
    </AppModal>

    <!-- Global Inbox Modal -->
    <InboxModal
      v-model="showInboxModal"
      :initial-message-id="selectedInboxMessageId"
      @refresh-counts="fetchNotifications"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useCookie } from '#app'
import { useToast, useColors } from 'vuestic-ui'
import { useTimezoneDate } from '~/composables/useTimezoneDate'
import { useApprovalEnricher } from '~/composables/useApprovalEnricher'
import { useCustomFetch } from '~/composables/useCustomFetch'
import ApprovalDetailsViewer from '~/components/ApprovalDetailsViewer.vue'
import AppModal from '~/components/common/AppModal.vue'
import InboxModal from '~/components/inbox/InboxModal.vue'
import { useInbox } from '~/composables/useInbox'

const { customFetch } = useCustomFetch()
const { fetchUnreadCount } = useInbox()
const { init: notifyToast } = useToast()
const inboxUnreadCount = ref(0)
const showInboxModal = ref(false)
const selectedInboxMessageId = ref(null)

const openInbox = (msgId = null) => {
  selectedInboxMessageId.value = msgId
  showInboxModal.value = true
}

const closeInbox = () => {
  showInboxModal.value = false
  selectedInboxMessageId.value = null
}
const router = useRouter()
const { t, te, locale } = useI18n()
const currentLocale = computed(() => locale?.value || 'ko')
const { formatWithTimezone } = useTimezoneDate()
const colors = useColors()
const currentPresetName = colors?.currentPresetName
const { enrichRequest } = useApprovalEnricher()

const isDark = computed(() => {
  if (!currentPresetName) return false
  const val = typeof currentPresetName === 'object' && currentPresetName !== null && 'value' in currentPresetName ? currentPresetName.value : currentPresetName
  return val === 'dark'
})

const tokenCookie = useCookie('auth_token')
const notifications = ref([])
const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)

const showApprovalModal = ref(false)
const activeRequest = ref(null)
const pendingStepId = ref(null)
const isPendingAssignee = ref(false)
const commentData = ref('')

let eventSource = null
let reconnectTimer = null
let isComponentMounted = true

const parseJwtUserId = (token) => {
  if (!token) return null
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''))
    const parsed = JSON.parse(jsonPayload)
    return parsed.userId || parsed.uuid || parsed.sub || null
  } catch {
    return null
  }
}

const isValidUuid = (val) => {
  if (!val || typeof val !== 'string') return false
  return /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(val)
}

const fetchNotifications = async () => {
  if (!tokenCookie.value) return
  try {
    const unreadRes = await fetchUnreadCount().catch(() => null)
    if (unreadRes && typeof unreadRes.unreadCount === 'number') {
      inboxUnreadCount.value = unreadRes.unreadCount
    }
  } catch {}
  try {
    const data = await customFetch('/api/notifications')
    // Backend returns PageResponse { content: [...], totalElements, ... }
    const items = Array.isArray(data) ? data : (data && Array.isArray(data.content) ? data.content : [])
    notifications.value = items.map(n => ({
      id: n.id || Math.random(),
      title: n.title || '',
      message: n.message || n.content || '',
      type: (n.type || 'INFO').toUpperCase(),
      linkUrl: n.linkUrl || n.url || null,
      read: Boolean(n.read || n.isRead),
      createdAt: n.createdAt || n.timestamp || new Date().toISOString()
    }))
  } catch (e) {
    console.debug('Initial notifications fetch skipped or returned empty:', e)
  }
}

const recentProcessedNotifs = new Map()

const cleanOldProcessedNotifs = () => {
  const now = Date.now()
  for (const [key, time] of recentProcessedNotifs.entries()) {
    if (now - time > 10000) {
      recentProcessedNotifs.delete(key)
    }
  }
}

const handleIncomingNotification = (rawPayload) => {
  let payload = rawPayload
  if (typeof rawPayload === 'string') {
    try {
      payload = JSON.parse(rawPayload)
    } catch {
      payload = { title: rawPayload, message: '', type: 'INFO' }
    }
  }
  if (!payload) return

  if (payload.eventType === 'CHAT_MESSAGE' || payload.eventType === 'MUSIC_PLAY' || payload.eventType === 'MUSIC_STOP') {
    if (process.client) {
      window.dispatchEvent(new CustomEvent('chat-message-received', { detail: payload }))
    }
    if (payload.eventType === 'CHAT_MESSAGE') return
  }

  if (payload.eventType === 'ROOM_READ') {
    if (process.client) {
      window.dispatchEvent(new CustomEvent('chat-room-read', { detail: payload }))
    }
    return
  }

  if (payload.eventType === 'MESSAGE_DELETED') {
    if (process.client) {
      window.dispatchEvent(new CustomEvent('chat-message-deleted', { detail: payload }))
    }
    return
  }

  if (payload.eventType === 'FORCE_LOGOUT') {
    notifyToast({
      message: payload.message || '다른 기기에서 로그인되어 현재 세션이 종료되었습니다.',
      color: 'danger',
      duration: 5000
    })
    tokenCookie.value = null
    const userCookie = useCookie('user_data')
    userCookie.value = null
    if (process.client) {
      document.cookie = 'auth_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;'
      document.cookie = 'token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;'
      document.cookie = 'refresh_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;'
      document.cookie = 'user_data=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;'
      setTimeout(() => {
        if (window.location.pathname !== '/login') {
          window.location.href = '/login?expired=1'
        }
      }, 500)
    }
    return
  }

  const itemType = String(payload.type || 'INFO').toUpperCase()
  const notifTitle = payload.title || (payload.eventType === 'INBOX_MESSAGE' || payload.type === 'NEW_MESSAGE' ? t('inbox.new_message_received') : t('notifications.title'))
  const notifMessage = payload.message || payload.content || (payload.subject ? `${payload.senderName || payload.senderId || ''}: ${payload.subject}` : '')
  const notifId = (payload.id && isValidUuid(String(payload.id))) ? String(payload.id) : (payload.notificationId && isValidUuid(String(payload.notificationId)) ? String(payload.notificationId) : null)
  const messageId = payload.messageId || payload.inboxMessageId || null

  // Deduplication check
  cleanOldProcessedNotifs()
  const dedupKey = notifId
    ? `id_${notifId}`
    : (messageId
      ? `msg_${messageId}_${payload.eventType || itemType}`
      : `${itemType}_${notifTitle}_${notifMessage}_${payload.senderId || ''}`)

  const now = Date.now()
  if (recentProcessedNotifs.has(dedupKey) && (now - (recentProcessedNotifs.get(dedupKey) || 0)) < 5000) {
    return
  }
  recentProcessedNotifs.set(dedupKey, now)

  if (notifId && notifications.value.some(n => String(n.id) === notifId)) {
    return
  }

  if (payload.eventType === 'INBOX_MESSAGE' || payload.type === 'NEW_MESSAGE' || payload.eventType === 'NEW_MESSAGE') {
    fetchNotifications()
    if (process.client) {
      window.dispatchEvent(new CustomEvent('inbox-refresh-counts', { detail: payload }))
      window.dispatchEvent(new CustomEvent('inbox-message-received', { detail: payload }))
    }
  }

  const newNotif = {
    id: notifId || Date.now() + Math.random(),
    messageId: messageId,
    eventType: payload.eventType || (itemType === 'INBOX_MESSAGE' ? 'INBOX_MESSAGE' : null),
    title: notifTitle,
    message: notifMessage,
    type: itemType,
    linkUrl: payload.linkUrl || payload.link || payload.url || null,
    read: false,
    createdAt: payload.createdAt || payload.timestamp || new Date().toISOString()
  }

  notifications.value.unshift(newNotif)

  if (itemType === 'APPROVAL' || (payload.linkUrl && payload.linkUrl.includes('/approvals')) || (payload.eventType && payload.eventType.includes('APPROVAL'))) {
    if (process.client) {
      window.dispatchEvent(new CustomEvent('approval-updated', { detail: payload }))
    }
  }

  try {
    const toastMsg = buildCleanToastMessage(newNotif.title, newNotif.message)
    notifyToast({
      message: toastMsg,
      color: getTypeBadgeColor(newNotif.type),
      duration: 4000
    })
  } catch (err) {
    console.error('Toast notification error:', err)
  }
}

let sseRetryCount = 0
const MAX_SSE_RETRIES = 5

const connectSSE = () => {
  if (!process.client || typeof window === 'undefined' || !window.EventSource) return
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }

  const token = tokenCookie.value || ''
  if (!token) {
    return
  }

  const sseUrl = `/api/notifications/subscribe?token=${encodeURIComponent(token)}`

  try {
    eventSource = new EventSource(sseUrl, { withCredentials: true })

    eventSource.onopen = () => {
      sseRetryCount = 0
    }

    const onMessageReceived = (event) => {
      if (event && event.data) {
        handleIncomingNotification(event.data)
      }
    }

    eventSource.addEventListener('notification', onMessageReceived)
    eventSource.addEventListener('message', onMessageReceived)

    eventSource.onerror = (err) => {
      if (eventSource) {
        eventSource.close()
        eventSource = null
      }
      if (isComponentMounted) {
        sseRetryCount++
        if (sseRetryCount > MAX_SSE_RETRIES) {
          console.warn(`SSE connection failed ${MAX_SSE_RETRIES} times. Halting reconnect until token refresh.`)
          return
        }
        const delay = Math.min(30000, 3000 * Math.pow(1.5, sseRetryCount - 1))
        if (reconnectTimer) clearTimeout(reconnectTimer)
        reconnectTimer = setTimeout(() => {
          connectSSE()
        }, delay)
      }
    }
  } catch (e) {
    console.error('Error creating EventSource:', e)
  }
}


const handleNotificationClick = async (item) => {
  item.read = true
  if (item.id && isValidUuid(String(item.id)) && tokenCookie.value) {
    try {
      await customFetch(`/api/notifications/${item.id}/read`, {
        method: 'PATCH'
      })
    } catch {
      // Graceful ignore
    }
  }

  // Handle inbox/mail notifications directly
  if (item.type === 'INBOX_MESSAGE' || item.type === 'NEW_MESSAGE' || item.eventType === 'INBOX_MESSAGE' || item.messageId) {
    openInbox(item.messageId || null)
    return
  }

  // Extract approvalId from linkUrl or message
  let approvalId = null
  if (item.linkUrl) {
    if (item.linkUrl.includes('requestId=')) {
      approvalId = item.linkUrl.split('requestId=')[1].split('&')[0]
    } else if (item.linkUrl.startsWith('/approvals/')) {
      approvalId = item.linkUrl.replace('/approvals/', '')
    }
  }

  if (approvalId) {
    try {
      const fullReq = await customFetch(`/api/approval-requests/${approvalId}`)
      if (fullReq) {
        const enriched = await enrichRequest(fullReq)
        
        // Extract current user info (ID & Roles)
        const userCookie = useCookie('user_data')
        let userObj = null
        if (userCookie.value) {
          if (typeof userCookie.value === 'object') userObj = userCookie.value
          else {
            try { userObj = JSON.parse(userCookie.value) } catch {}
          }
        }
        const myUserId = userObj?.id || userObj?.uuid || parseJwtUserId(tokenCookie.value)
        const myRole = userObj?.role || ''
        const myRoles = Array.isArray(userObj?.roles) ? userObj.roles : (myRole ? [myRole] : [])

        const pendingStep = (enriched.steps || []).find(s => {
          if (s.status !== 'PENDING') return false
          
          if (myUserId && s.assigneeId && String(s.assigneeId) === String(myUserId)) {
            return true
          }
          if (s.assigneeRole) {
            const stepRole = String(s.assigneeRole).toUpperCase()
            if (myRoles.some(r => String(r).toUpperCase() === stepRole)) {
              return true
            }
          }
          if (item.type === 'APPROVAL' && enriched.status === 'PENDING' && s.stepOrder === enriched.currentStepOrder) {
            return true
          }
          return false
        })

        activeRequest.value = enriched
        commentData.value = ''

        if (pendingStep && enriched.status === 'PENDING') {
          pendingStepId.value = pendingStep.id
          isPendingAssignee.value = true
        } else {
          pendingStepId.value = null
          isPendingAssignee.value = false
        }

        showApprovalModal.value = true
        return
      }
    } catch (e) {
      console.warn('Could not open modal directly for notification, falling back to router:', e)
    }
  }


  if (item.linkUrl) {
    let targetUrl = item.linkUrl
    if (targetUrl.startsWith('/approvals/')) {
      const reqId = targetUrl.replace('/approvals/', '')
      targetUrl = `/approvals?requestId=${reqId}`
    }
    router.push(targetUrl)
  }
}

const handleSingleAction = async (action) => {
  if (!pendingStepId.value) return
  try {
    await customFetch(`/api/approval-requests/steps/${pendingStepId.value}/${action}`, {
      method: 'POST',
      body: { comment: commentData.value }
    })
    notifyToast({
      message: action === 'approve' ? '결재가 승인되었습니다.' : '결재가 반려되었습니다.',
      color: action === 'approve' ? 'success' : 'danger'
    })
    showApprovalModal.value = false
    fetchNotifications()
    if (process.client) {
      window.dispatchEvent(new CustomEvent('approval-updated'))
    }
  } catch (e) {
    notifyToast({ message: '처리 중 오류가 발생했습니다.', color: 'danger' })
  }
}

const markAllAsRead = async () => {
  notifications.value.forEach(n => { n.read = true })
  if (tokenCookie.value) {
    try {
      await customFetch('/api/notifications/mark-all-read', {
        method: 'PATCH'
      })
    } catch {
      // Graceful ignore
    }
  }
}

const deleteNotification = async (item) => {
  notifications.value = notifications.value.filter(n => n.id !== item.id)
  if (item.id && isValidUuid(String(item.id)) && tokenCookie.value) {
    try {
      await customFetch(`/api/notifications/${item.id}`, {
        method: 'DELETE'
      })
    } catch {
      // Graceful ignore
    }
  }
}

const deleteAllNotifications = async () => {
  notifications.value = []
  if (tokenCookie.value) {
    try {
      await customFetch('/api/notifications/clear-all', {
        method: 'DELETE'
      })
    } catch {
      // Graceful ignore
    }
  }
}

const getTypeBadgeColor = (type) => {
  const norm = String(type || '').toUpperCase()
  switch (norm) {
    case 'WARNING': return 'warning'
    case 'APPROVAL': return 'success'
    case 'DQ': return 'danger'
    case 'INFO':
    default:
      return 'primary'
  }
}

const getTypeLabel = (type) => {
  const norm = String(type || '').toUpperCase()
  switch (norm) {
    case 'WARNING': return t('notifications.type_warning')
    case 'APPROVAL': return t('notifications.type_approval')
    case 'DQ': return t('notifications.type_dq')
    case 'INFO':
    default:
      return t('notifications.type_info')
  }
}

const getTypeClass = (type) => {
  const norm = String(type || '').toUpperCase()
  return `type-dot-${norm.toLowerCase()}`
}

const buildCleanToastMessage = (rawTitle, rawMessage) => {
  const cleanTitle = formatTitle(rawTitle)
  let cleanMsg = formatMessage(rawMessage)

  if (cleanMsg) {
    cleanMsg = cleanMsg.replace(/\s*\([^)]*fieldId[^)]*\)/gi, '')
    cleanMsg = cleanMsg.replace(/\s*\([^)]*fieldGroupId[^)]*\)/gi, '')
    cleanMsg = cleanMsg.replace(/\s*\([^)]*key:[^)]*\)/gi, '')
    cleanMsg = cleanMsg.replace(/:\s*[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/g, '')
    cleanMsg = cleanMsg.trim()
  }

  if (cleanTitle && cleanMsg) {
    return `[${cleanTitle}] ${cleanMsg}`
  }
  return cleanTitle || cleanMsg || '새로운 알림이 도착하였습니다.'
}

const formatTitle = (title) => {

  if (!title) return ''
  if (title.startsWith('@i18n:')) {
    const key = title.replace('@i18n:', '')
    return te(key) ? t(key) : key
  }
  const normTitle = title.trim()
  if (normTitle === 'Approval Request Pending') return t('notifications.approval_pending')
  if (normTitle === 'Approval Step Approved') return t('notifications.approval_step_approved')
  if (normTitle === 'Approval Request Finalized') return t('notifications.approval_finalized')
  if (normTitle === 'Approval Request Rejected') return t('notifications.approval_rejected')
  if (te(normTitle)) {
    return t(normTitle)
  }
  return title
}

const formatMessage = (msg) => {
  if (!msg) return ''
  let result = msg
  const isEn = currentLocale.value === 'en'

  if (result.includes('New approval request received:')) {
    return t('notifications.approval_pending')
  }
  if (result.includes('New approval request step received:')) {
    return t('notifications.approval_step_approved')
  }
  if (result.includes('completed for request:')) {
    const match = result.match(/Approval step (\d+)/)
    const stepNum = match ? match[1] : ''
    return stepNum ? (isEn ? `Step ${stepNum} approval completed.` : `결재 ${stepNum}단계 승인이 완료되었습니다.`) : (isEn ? 'Step approval completed.' : '결재 단계 승인이 완료되었습니다.')
  }
  if (result.includes('has been fully approved.')) {
    return isEn ? 'Request has been fully approved.' : '요청하신 결재가 최종 승인되었습니다.'
  }

  // Filter out raw UUID patterns
  result = result.replace(/:\s*[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/g, '')
  
  // Clean technical JSON parameter strings & translate labels dynamically via vue-i18n locale
  result = result.replace(/fieldId,\s*/gi, '')
  result = result.replace(/fieldId:\s*[^,)]+,\s*/gi, '')
  result = result.replace(/fieldGroupId,\s*/gi, '')
  result = result.replace(/fieldGroupId:\s*[^,)]+,\s*/gi, '')
  result = result.replace(/\bdomainName:\s*/gi, isEn ? 'Domain: ' : '도메인: ')
  result = result.replace(/\bfieldName:\s*/gi, isEn ? 'Field Name: ' : '필드명: ')
  result = result.replace(/\bfieldKey:\s*/gi, isEn ? 'Field Key: ' : '필드키: ')
  result = result.replace(/\bkey:\s*/gi, isEn ? 'Field: ' : '항목: ')
  result = result.replace(/\btype:\s*/gi, isEn ? 'Type: ' : '유형: ')
  result = result.replace(/\bname:\s*/gi, isEn ? 'Name: ' : '이름: ')
  result = result.replace(/\bcode:\s*/gi, isEn ? 'Code: ' : '코드: ')

  return result.trim()
}

const parseNotificationContent = (item) => {
  const rawMessage = formatMessage(item.message || '')
  if (!rawMessage) return { location: '', mainText: '', detailText: '' }

  let location = ''
  let mainText = rawMessage
  let detailText = ''

  // 1. [Location > Category] Parsing
  const locMatch = mainText.match(/^\[(.*?)\]\s*/)
  if (locMatch) {
    location = locMatch[1]
    if (location === '도메인 > 분류' || location.includes('SCHEMA') || location.includes('스키마')) {
      location = currentLocale.value === 'en' ? 'Schema Change' : '스키마 변경'
    }
    mainText = mainText.replace(/^\[(.*?)\]\s*/, '')
  }

  // 2. (Detail Box Info) Parsing
  const detailMatch = mainText.match(/\((.*?)\)$/)
  if (detailMatch) {
    detailText = detailMatch[1]
    mainText = mainText.replace(/\s*\((.*?)\)$/, '')
  }

  return { location, mainText, detailText }
}

const formatTime = (dateInput) => {
  if (!dateInput) return ''
  return formatWithTimezone(dateInput)
}

const { connect: connectWS, disconnect: disconnectWS } = useWebSocket()

const handleOpenInboxModalEvent = (e) => {
  openInbox(e?.detail?.messageId)
}

const handleCloseInboxModalEvent = () => {
  closeInbox()
}

onMounted(async () => {
  isComponentMounted = true
  await fetchNotifications()
  connectSSE()
  if (process.client) {
    window.addEventListener('open-inbox-modal', handleOpenInboxModalEvent)
    window.addEventListener('close-inbox-modal', handleCloseInboxModalEvent)
    window.addEventListener('inbox-refresh-counts', fetchNotifications)
    window.addEventListener('inbox-message-read', fetchNotifications)
    connectWS((data) => {
      if (data) {
        handleIncomingNotification(data)
      } else {
        fetchNotifications()
        window.dispatchEvent(new CustomEvent('approval-updated'))
      }
    })
  }
})

onUnmounted(() => {
  isComponentMounted = false
  if (process.client) {
    window.removeEventListener('open-inbox-modal', handleOpenInboxModalEvent)
    window.removeEventListener('close-inbox-modal', handleCloseInboxModalEvent)
    window.removeEventListener('inbox-refresh-counts', fetchNotifications)
    window.removeEventListener('inbox-message-read', fetchNotifications)
  }
  if (reconnectTimer) clearTimeout(reconnectTimer)
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
  disconnectWS()
})
</script>

<style scoped>
.notification-bell-btn:hover {
  background: rgba(255, 255, 255, 0.2) !important;
  transform: scale(1.05);
  transition: all 0.2s ease;
}

.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.85rem 1rem;
  background: var(--va-background-secondary);
}

.header-title-box {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.header-title {
  font-weight: 700;
  font-size: 0.95rem;
}

.header-icon {
  color: var(--va-primary);
}

.mark-all-btn {
  font-size: 0.78rem !important;
  padding: 0 0.4rem !important;
  font-weight: 600;
}

.notification-list {
  max-height: 380px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.85rem 1rem;
  border-bottom: 1px solid var(--va-background-border);
  cursor: pointer;
  transition: background-color 0.2s ease, transform 0.15s ease;
  position: relative;
}

.notification-item:hover {
  background: var(--va-background-element);
}

.notification-item.unread {
  background: rgba(59, 130, 246, 0.06);
}

.item-left {
  padding-top: 0.2rem;
}

.type-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}
.type-dot-info { background-color: var(--va-primary); }
.type-dot-warning { background-color: var(--va-warning); }
.type-dot-approval { background-color: var(--va-success); }
.type-dot-dq { background-color: var(--va-danger); }

.item-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  overflow: hidden;
}

.item-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
}

.type-badge {
  font-size: 0.68rem;
  padding: 1px 6px;
  border-radius: 4px;
}

.item-time {
  font-size: 0.72rem;
  color: var(--va-text-secondary);
}

.item-title {
  font-size: 0.88rem;
  font-weight: 600;
  color: var(--va-text-primary);
  line-height: 1.3;
}

.location-tag {
  font-size: 0.68rem;
  padding: 1px 6px;
  border-radius: 4px;
  background: var(--va-background-element);
  color: var(--va-text-secondary);
  border: 1px solid var(--va-background-border);
  font-weight: 600;
}

.item-main-text {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--va-text-primary);
  line-height: 1.35;
  margin-top: 2px;
}

.item-detail-box {
  margin-top: 4px;
  padding: 4px 8px;
  border-radius: 6px;
  background: rgba(59, 130, 246, 0.08);
  font-size: 0.75rem;
  color: var(--va-primary);
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  word-break: break-all;
}

.unread-indicator {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--va-danger);
}

.empty-notifications {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2.5rem 1rem;
  gap: 0.5rem;
}

.empty-text {
  font-size: 0.85rem;
  color: var(--va-text-secondary);
  margin: 0;
}
</style>
