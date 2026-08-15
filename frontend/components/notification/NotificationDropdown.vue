<template>
  <va-dropdown placement="bottom-end" :offset="[0, 8]">
    <template #anchor>
      <va-button preset="plain" color="secondary" size="small" style="position: relative; padding: 0.4rem;">
        <va-icon name="notifications" size="1.3rem" />
        <va-badge
          v-if="unreadCount > 0"
          :text="unreadCount > 99 ? '99+' : String(unreadCount)"
          color="danger"
          style="position: absolute; top: -4px; right: -4px; font-size: 0.65rem;"
        />
      </va-button>
    </template>

    <va-dropdown-content style="width: 320px; max-height: 400px; padding: 0; display: flex; flex-direction: column; box-shadow: var(--va-box-shadow); border-radius: 8px; overflow: hidden; background: var(--va-background-primary);">
      <!-- Header -->
      <div style="padding: 0.75rem 1rem; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--va-background-border); background: var(--va-background-element);">
        <span style="font-weight: 700; font-size: 0.9rem; color: var(--va-text-primary);">
          🔔 {{ $t('notifications') }}
        </span>
        <va-button
          v-if="unreadCount > 0"
          preset="plain"
          size="small"
          color="primary"
          style="font-size: 0.75rem; padding: 0;"
          @click="markAllRead"
        >
          {{ $t('mark_all_read') }}
        </va-button>
      </div>

      <!-- Notifications List -->
      <div style="overflow-y: auto; max-height: 320px;">
        <div
          v-for="item in notifications"
          :key="item.id"
          style="padding: 0.75rem 1rem; border-bottom: 1px solid var(--va-background-border); cursor: pointer; transition: background 0.2s;"
          :style="{ background: item.isRead ? 'transparent' : 'rgba(var(--va-primary-rgb), 0.05)' }"
          @click="handleNotificationClick(item)"
        >
          <div style="display: flex; gap: 0.6rem; align-items: flex-start;">
            <va-icon
              :name="getTypeIcon(item.type)"
              :color="getTypeColor(item.type)"
              size="1.1rem"
              style="margin-top: 0.15rem;"
            />
            <div style="display: flex; flex-direction: column; gap: 0.2rem; flex: 1;">
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span style="font-weight: 600; font-size: 0.84rem; color: var(--va-text-primary);">
                  {{ item.title }}
                </span>
                <span v-if="!item.isRead" style="width: 6px; height: 6px; border-radius: 50%; background: var(--va-primary);" />
              </div>
              <span style="font-size: 0.78rem; color: var(--va-text-secondary); line-height: 1.3;">
                {{ item.message }}
              </span>
              <span style="font-size: 0.7rem; color: var(--va-text-secondary); margin-top: 0.2rem;">
                {{ formatWithTimezone(item.createdAt) }}
              </span>
            </div>
          </div>
        </div>

        <div v-if="notifications.length === 0" style="padding: 2rem 1rem; text-align: center; color: var(--va-text-secondary); font-size: 0.85rem;">
          <va-icon name="notifications_off" size="1.8rem" color="secondary" style="margin-bottom: 0.4rem;" />
          <p style="margin: 0;">{{ $t('no_notifications') }}</p>
        </div>
      </div>
    </va-dropdown-content>
  </va-dropdown>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { formatWithTimezone } from '~/composables/useTimezoneDate'

const { t } = useI18n()

const notifications = ref<any[]>([])
const unreadCount = ref<number>(0)

const getTypeIcon = (type: string) => {
  switch (type) {
    case 'APPROVAL': return 'verified'
    case 'DQ_ALERT': return 'warning'
    case 'IMPORT_COMPLETE': return 'cloud_done'
    default: return 'info'
  }
}

const getTypeColor = (type: string) => {
  switch (type) {
    case 'APPROVAL': return 'primary'
    case 'DQ_ALERT': return 'warning'
    case 'IMPORT_COMPLETE': return 'success'
    default: return 'info'
  }
}

const fetchNotifications = async () => {
  try {
    const res = await useCustomFetch('/notifications')
    if (res.data?.value) {
      notifications.value = res.data.value
    }
    const countRes = await useCustomFetch('/notifications/unread-count')
    if (countRes.data?.value) {
      unreadCount.value = countRes.data.value.unreadCount || 0
    }
  } catch (e: any) {
    console.error('Failed to fetch notifications', e)
  }
}

const handleNotificationClick = async (item: any) => {
  if (!item.isRead) {
    try {
      await useCustomFetch(`/notifications/${item.id}/read`, { method: 'PATCH' })
      item.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (e) {
      console.error(e)
    }
  }
  if (item.linkUrl) {
    navigateTo(item.linkUrl)
  }
}

const markAllRead = async () => {
  try {
    await useCustomFetch('/notifications/mark-all-read', { method: 'PATCH' })
    notifications.value.forEach(n => { n.isRead = true })
    unreadCount.value = 0
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchNotifications()
})
</script>
