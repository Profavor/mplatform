<template>
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
                <va-badge
                  :color="getTypeBadgeColor(item.type)"
                  size="small"
                  class="type-badge"
                >
                  {{ getTypeLabel(item.type) }}
                </va-badge>
                <span class="item-time">{{ formatTime(item.createdAt) }}</span>
              </div>
              <div class="item-title">{{ item.title }}</div>
              <div v-if="item.message" class="item-message">{{ item.message }}</div>
            </div>

            <div v-if="!item.read" class="unread-indicator"></div>
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
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useCookie } from '#app'
import { useToast, useColors } from 'vuestic-ui'
import { useTimezoneDate } from '~/composables/useTimezoneDate'

const router = useRouter()
const { t } = useI18n()
const { formatWithTimezone } = useTimezoneDate()
const { init: notifyToast } = useToast()
const { currentPresetName } = useColors()

const isDark = computed(() => currentPresetName.value === 'dark')

const tokenCookie = useCookie('auth_token')
const notifications = ref([])
const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)

let eventSource = null
let reconnectTimer = null
let isComponentMounted = true

const fetchNotifications = async () => {
  if (!tokenCookie.value) return
  try {
    const data = await $fetch('/api/notifications', {
      headers: { Authorization: `Bearer ${tokenCookie.value}` }
    })
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

  const itemType = String(payload.type || 'INFO').toUpperCase()
  const newNotif = {
    id: payload.id || Date.now() + Math.random(),
    title: payload.title || t('notifications.title'),
    message: payload.message || payload.content || '',
    type: itemType,
    linkUrl: payload.linkUrl || payload.link || payload.url || null,
    read: false,
    createdAt: payload.createdAt || payload.timestamp || new Date().toISOString()
  }

  notifications.value.unshift(newNotif)

  try {
    notifyToast({
      message: newNotif.title + (newNotif.message ? `: ${newNotif.message}` : ''),
      color: getTypeBadgeColor(newNotif.type),
      duration: 4000
    })
  } catch (err) {
    console.error('Toast notification error:', err)
  }
}

const connectSSE = () => {
  if (!process.client || typeof window === 'undefined' || !window.EventSource) return
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }

  const token = tokenCookie.value || ''
  const sseUrl = token
    ? `/api/notifications/subscribe?token=${encodeURIComponent(token)}`
    : '/api/notifications/subscribe'

  try {
    eventSource = new EventSource(sseUrl, { withCredentials: true })

    const onMessageReceived = (event) => {
      if (event && event.data) {
        handleIncomingNotification(event.data)
      }
    }

    eventSource.addEventListener('notification', onMessageReceived)
    eventSource.addEventListener('message', onMessageReceived)

    eventSource.onerror = (err) => {
      console.warn('SSE connection disconnected. Reconnecting in 5s...', err)
      if (eventSource) {
        eventSource.close()
        eventSource = null
      }
      if (isComponentMounted) {
        if (reconnectTimer) clearTimeout(reconnectTimer)
        reconnectTimer = setTimeout(() => {
          connectSSE()
        }, 5000)
      }
    }
  } catch (e) {
    console.error('Error creating EventSource:', e)
  }
}

const handleNotificationClick = async (item) => {
  item.read = true
  if (item.id && tokenCookie.value) {
    try {
      await $fetch(`/api/notifications/${item.id}/read`, {
        method: 'PUT',
        headers: { Authorization: `Bearer ${tokenCookie.value}` }
      })
    } catch {
      // Graceful ignore
    }
  }
  if (item.linkUrl) {
    router.push(item.linkUrl)
  }
}

const markAllAsRead = async () => {
  notifications.value.forEach(n => { n.read = true })
  if (tokenCookie.value) {
    try {
      await $fetch('/api/notifications/read-all', {
        method: 'PUT',
        headers: { Authorization: `Bearer ${tokenCookie.value}` }
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

const formatTime = (dateInput) => {
  if (!dateInput) return ''
  return formatWithTimezone(dateInput)
}

onMounted(async () => {
  isComponentMounted = true
  await fetchNotifications()
  connectSSE()
})

onUnmounted(() => {
  isComponentMounted = false
  if (reconnectTimer) clearTimeout(reconnectTimer)
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
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

.item-message {
  font-size: 0.8rem;
  color: var(--va-text-secondary);
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
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
