<template>
  <div style="display: flex; flex-direction: column; gap: 1.25rem;">
    <!-- Title & Filters -->
    <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.75rem;">
      <div>
        <h4 style="font-weight: 800; font-size: 1.05rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
          <va-icon name="history_edu" color="primary" />
          <span>{{ $t('audit_logs_title') }}</span>
        </h4>
        <p style="font-size: 0.84rem; color: var(--va-text-secondary); margin: 0.25rem 0 0 0;">
          {{ $t('audit_logs_desc') }}
        </p>
      </div>

      <div style="display: flex; gap: 0.5rem; align-items: center;">
        <va-checkbox
          v-model="onlySelectedUser"
          :label="$t('target_user') + ': ' + (username || userId)"
          @update:modelValue="fetchLogs"
          dense
        />
        <va-button preset="outline" icon="refresh" size="small" @click="fetchLogs" />
      </div>
    </div>

    <!-- Logs Table -->
    <div v-if="isLoading" style="text-align: center; padding: 2rem;">
      <va-progress-circle indeterminate color="primary" />
    </div>
    <div v-else-if="logs.length === 0" style="padding: 2rem; text-align: center; background: var(--va-background-primary); border: 1px dashed var(--va-background-border); border-radius: 10px; color: var(--va-text-secondary);">
      <va-icon name="assignment" color="secondary" size="large" style="margin-bottom: 0.5rem; display: block;" />
      <span>{{ $t('no_audit_logs') }}</span>
    </div>
    <div v-else style="overflow-x: auto; border: 1px solid var(--va-background-border); border-radius: 10px;">
      <table style="width: 100%; border-collapse: collapse; font-size: 0.88rem; text-align: left;">
        <thead>
          <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('log_code') }}</th>
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('timestamp') }}</th>
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('action_type') }}</th>
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('target_resource') }}</th>
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('target_user') }}</th>
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('target_name_desc') }}</th>
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('before_value') }} / {{ $t('after_value') }}</th>
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('operator') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="l in logs"
            :key="l.id"
            style="border-bottom: 1px solid var(--va-background-border);"
          >
            <td style="padding: 0.75rem 1rem; font-weight: 700; font-family: monospace; color: var(--va-primary);">
              {{ l.logCode }}
            </td>
            <td style="padding: 0.75rem 1rem; color: var(--va-text-secondary); font-size: 0.82rem; white-space: nowrap;">
              {{ formatDate(l.createdAt) }}
            </td>
            <td style="padding: 0.75rem 1rem;">
              <va-badge
                :text="l.actionType"
                :color="getActionColor(l.actionType)"
                size="small"
              />
            </td>
            <td style="padding: 0.75rem 1rem; font-weight: 600;">
              {{ l.targetResource }}
            </td>
            <td style="padding: 0.75rem 1rem;">
              {{ l.targetUsername || l.targetUserId || '-' }}
            </td>
            <td style="padding: 0.75rem 1rem; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
              {{ l.targetName || '-' }}
            </td>
            <td style="padding: 0.75rem 1rem; font-size: 0.82rem;">
              <div style="display: flex; align-items: center; gap: 0.35rem;">
                <span style="color: var(--va-text-secondary);">{{ l.beforeValue || '-' }}</span>
                <va-icon name="east" size="small" color="primary" />
                <span style="color: var(--va-primary); font-weight: 600;">{{ l.afterValue || '-' }}</span>
              </div>
            </td>
            <td style="padding: 0.75rem 1rem; font-weight: 600;">
              <va-badge :text="l.operator || 'SYSTEM'" color="secondary" outline size="small" />
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" style="display: flex; justify-content: center; margin-top: 0.5rem;">
      <va-pagination
        v-model="currentPage"
        :pages="totalPages"
        :visible-pages="5"
        size="small"
        @update:modelValue="fetchLogs"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'

const props = defineProps({
  userId: {
    type: String,
    default: ''
  },
  username: {
    type: String,
    default: ''
  }
})

const token = useCookie('auth_token')
const logs = ref([])
const isLoading = ref(false)
const onlySelectedUser = ref(true)
const currentPage = ref(1)
const totalPages = ref(1)

const getActionColor = (action) => {
  if (!action) return 'info'
  if (action.includes('REVOKE') || action.includes('DELETE')) return 'danger'
  if (action.includes('GRANT') || action.includes('CREATE')) return 'success'
  return 'primary'
}

const parseDate = (dateString) => {
  if (!dateString) return null
  let str = String(dateString).trim()
  if (str.includes(' ') && !str.includes('T')) {
    str = str.replace(' ', 'T')
  }
  if (!str.endsWith('Z') && !str.includes('+') && !str.includes('-')) {
    const tz = useCookie('timezone', { default: () => 'Asia/Seoul' }).value
    let serverOffset = '+09:00'
    if (tz === 'UTC' || tz === 'GMT') serverOffset = 'Z'
    else if (tz === 'America/New_York') serverOffset = '-05:00'
    else if (tz === 'Europe/London') serverOffset = '+00:00'
    str += serverOffset
  }
  const d = new Date(str)
  return isNaN(d.getTime()) ? new Date(dateString) : d
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = parseDate(dateString)
  if (!date) return ''
  const tz = useCookie('timezone', { default: () => 'Asia/Seoul' }).value
  const formatted = date.toLocaleString(undefined, { timeZone: tz })
  return formatted.replace(/\s*(GMT|UTC|KST|PST|EST|CET)[-+0-9:]*/gi, '').trim()
}

const fetchLogs = async () => {
  isLoading.value = true
  try {
    const query = {
      page: currentPage.value - 1,
      size: 15
    }
    if (onlySelectedUser.value && props.userId) {
      query.targetUserId = props.userId
    }
    const res = await $fetch('/api/permissions/audit-logs', {
      headers: { Authorization: `Bearer ${token.value}` },
      query
    })
    logs.value = res.content || []
    totalPages.value = res.totalPages || 1
  } catch (e) {
    console.error('Failed to fetch audit logs', e)
    logs.value = []
  } finally {
    isLoading.value = false
  }
}

watch(() => props.userId, () => {
  currentPage.value = 1
  fetchLogs()
})

onMounted(() => {
  fetchLogs()
})
</script>
