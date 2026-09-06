<template>
  <div style="display: flex; flex-direction: column; gap: 1.25rem;">
    <!-- Title & Add Scope Button -->
    <div style="display: flex; justify-content: space-between; align-items: center;">
      <div>
        <h4 style="font-weight: 800; font-size: 1.05rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
          <va-icon name="shield" color="primary" />
          <span>{{ $t('data_scopes_title') }}</span>
        </h4>
        <p style="font-size: 0.84rem; color: var(--va-text-secondary); margin: 0.25rem 0 0 0;">
          {{ $t('data_scopes_desc') }}
        </p>
      </div>
      <va-button
        v-if="canWrite"
        size="small"
        color="primary"
        icon="add_moderator"
        @click="openGrantModal"
        style="font-weight: 700;"
      >
        {{ $t('grant_data_scope') }}
      </va-button>
    </div>

    <!-- Scopes Table -->
    <div v-if="isLoading" style="text-align: center; padding: 2rem;">
      <va-progress-circle indeterminate color="primary" />
    </div>
    <div v-else-if="scopes.length === 0" style="padding: 2rem; text-align: center; background: var(--va-background-primary); border: 1px dashed var(--va-background-border); border-radius: 10px; color: var(--va-text-secondary);">
      <va-icon name="security" color="secondary" size="large" style="margin-bottom: 0.5rem; display: block;" />
      <span>{{ $t('no_data_scopes') }}</span>
    </div>
    <div v-else style="overflow-x: auto; border: 1px solid var(--va-background-border); border-radius: 10px;">
      <table style="width: 100%; border-collapse: collapse; font-size: 0.88rem; text-align: left;">
        <thead>
          <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('scope_code') }}</th>
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('domain_name') }}</th>
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('node_name') }}</th>
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('permission_level') }}</th>
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('timestamp') }}</th>
            <th v-if="canWrite" style="padding: 0.75rem 1rem; font-weight: 700; text-align: center;">{{ $t('delete') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="s in scopes"
            :key="s.id"
            style="border-bottom: 1px solid var(--va-background-border);"
          >
            <td style="padding: 0.75rem 1rem; font-weight: 700; font-family: monospace; color: var(--va-primary);">
              {{ s.scopeCode }}
            </td>
            <td style="padding: 0.75rem 1rem; font-weight: 600;">
              {{ getI18nText(s.domainName) }}
            </td>
            <td style="padding: 0.75rem 1rem;">
              <span v-if="s.nodeName" style="color: var(--va-text-primary);">
                {{ getI18nText(s.nodeName) }}
              </span>
              <va-badge v-else :text="$t('scope_all_nodes')" color="secondary" outline size="small" />
            </td>
            <td style="padding: 0.75rem 1rem;">
              <va-badge
                :text="s.permissionLevel"
                :color="s.permissionLevel === 'ADMIN' ? 'danger' : (s.permissionLevel === 'WRITE' ? 'warning' : 'info')"
                size="small"
              />
            </td>
            <td style="padding: 0.75rem 1rem; color: var(--va-text-secondary); font-size: 0.82rem;">
              {{ formatDate(s.createdAt) }}
            </td>
            <td v-if="canWrite" style="padding: 0.75rem 1rem; text-align: center;">
              <va-button
                preset="plain"
                icon="delete"
                color="danger"
                size="small"
                @click="confirmRevoke(s)"
              />
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Grant Scope Modal -->
    <AppModal
      v-model="showGrantModal"
      :title="$t('grant_data_scope')"
      icon="add_moderator"
      size="medium"
      @ok="handleGrantScope"
      :ok-text="$t('grant')"
      :cancel-text="$t('cancel')"
      :ok-disabled="!grantForm.domainId || isGranting"
    >
      <div style="display: flex; flex-direction: column; gap: 1rem; padding: 0.5rem 0;">
        <va-select
          v-model="grantForm.domainId"
          :label="$t('domain_name')"
          :options="domainOptions"
          value-by="id"
          text-by="label"
          @update:modelValue="onDomainSelected"
          style="width: 100%;"
        />

        <va-select
          v-model="grantForm.nodeId"
          :label="$t('node_name')"
          :options="nodeOptions"
          value-by="id"
          text-by="label"
          clearable
          :placeholder="$t('scope_all_nodes')"
          :disabled="!grantForm.domainId || isLoadingNodes"
          :loading="isLoadingNodes"
          style="width: 100%;"
        />

        <va-select
          v-model="grantForm.permissionLevel"
          :label="$t('permission_level')"
          :options="levelOptions"
          value-by="value"
          text-by="text"
          style="width: 100%;"
        />
      </div>
    </AppModal>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import AppModal from '~/components/common/AppModal.vue'

const props = defineProps({
  userId: {
    type: String,
    required: true
  },
  username: {
    type: String,
    default: ''
  },
  canWrite: {
    type: Boolean,
    default: false
  },
  domains: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['updated', 'notify'])

const { t, locale } = useI18n()
const token = useCookie('auth_token')

const scopes = ref([])
const isLoading = ref(false)
const showGrantModal = ref(false)
const isGranting = ref(false)
const isLoadingNodes = ref(false)
const availableNodes = ref([])

const grantForm = ref({
  domainId: null,
  nodeId: null,
  permissionLevel: 'READ'
})

const levelOptions = computed(() => [
  { value: 'READ', text: t('scope_level_read') },
  { value: 'WRITE', text: t('scope_level_write') },
  { value: 'ADMIN', text: t('scope_level_admin') }
])

const domainOptions = computed(() => {
  return props.domains.map(d => ({
    id: d.id,
    label: getI18nText(d.name)
  }))
})

const nodeOptions = computed(() => {
  return availableNodes.value.map(n => ({
    id: n.id,
    label: getI18nText(n.name)
  }))
})

const getI18nText = (textStr) => {
  if (!textStr) return ''
  try {
    const parsed = typeof textStr === 'object' ? textStr : JSON.parse(textStr)
    if (parsed && typeof parsed === 'object') {
      const loc = (locale?.value || 'ko').toLowerCase()
      return loc.startsWith('en') ? (parsed.en || parsed.ko || '') : (parsed.ko || parsed.en || '')
    }
    return String(textStr)
  } catch {
    return String(textStr)
  }
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

const fetchScopes = async () => {
  if (!props.userId) return
  isLoading.value = true
  try {
    const res = await $fetch(`/api/permissions/users/${props.userId}/scopes`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    scopes.value = res || []
  } catch (e) {
    console.error('Failed to fetch user scopes', e)
    scopes.value = []
  } finally {
    isLoading.value = false
  }
}

const onDomainSelected = async (domainId) => {
  grantForm.value.nodeId = null
  availableNodes.value = []
  if (!domainId) return

  isLoadingNodes.value = true
  try {
    const res = await $fetch(`/api/domains/${domainId}/nodes`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    availableNodes.value = res || []
  } catch (e) {
    console.error('Failed to load nodes for domain', e)
    availableNodes.value = []
  } finally {
    isLoadingNodes.value = false
  }
}

const openGrantModal = () => {
  grantForm.value = {
    domainId: props.domains.length > 0 ? props.domains[0].id : null,
    nodeId: null,
    permissionLevel: 'READ'
  }
  if (grantForm.value.domainId) {
    onDomainSelected(grantForm.value.domainId)
  }
  showGrantModal.value = true
}

const handleGrantScope = async () => {
  if (!grantForm.value.domainId || !props.userId) return
  isGranting.value = true
  try {
    await $fetch(`/api/permissions/users/${props.userId}/scopes`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` },
      body: {
        domainId: grantForm.value.domainId,
        nodeId: grantForm.value.nodeId,
        permissionLevel: grantForm.value.permissionLevel
      }
    })
    showGrantModal.value = false
    emit('notify', { message: t('scope_granted_success'), type: 'success' })
    await fetchScopes()
    emit('updated')
  } catch (e) {
    const msg = e.response?._data?.message || e.message || String(e)
    emit('notify', { message: msg, type: 'error' })
  } finally {
    isGranting.value = false
  }
}

const confirmRevoke = async (scope) => {
  if (!confirm(t('revoke_scope_confirm'))) return
  try {
    await $fetch(`/api/permissions/scopes/${scope.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    emit('notify', { message: t('scope_revoked_success'), type: 'success' })
    await fetchScopes()
    emit('updated')
  } catch (e) {
    const msg = e.response?._data?.message || e.message || String(e)
    emit('notify', { message: msg, type: 'error' })
  }
}

watch(() => props.userId, () => {
  fetchScopes()
})

onMounted(() => {
  fetchScopes()
})
</script>
