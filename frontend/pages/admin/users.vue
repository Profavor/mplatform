<template>
  <div style="display: flex; flex-direction: column; padding-bottom: 2rem;">
    <h1 style="font-size: 2rem; font-weight: bold; margin-bottom: 1.5rem; color: var(--va-text-primary);">
      {{ $t('user_management') }}
    </h1>

    <div style="display: flex; gap: 1.5rem; align-items: flex-start; flex-wrap: wrap;">
      <!-- User List -->
      <va-card style="flex: 1; min-width: 320px; display: flex; flex-direction: column;">
        <va-card-title style="display: flex; justify-content: space-between; align-items: center;">
          <span>{{ $t('user_management') }}</span>
          <va-input v-model="searchQuery" :placeholder="$t('search') || 'Search'" @keydown="onSearchKeydown" clearable @clear="fetchUsers" style="max-width: 150px;" />
        </va-card-title>
        <va-card-content style="flex: 1; display: flex; flex-direction: column;">
          <va-list style="flex: 1;">
            <va-list-item
              v-for="user in users"
              :key="user.id"
              @click="selectUser(user)"
              style="cursor: pointer; padding: 0.75rem 0.5rem; border-radius: 6px; margin-bottom: 0.25rem;"
              :style="{
                backgroundColor: selectedUser?.id === user.id ? 'var(--va-background-element)' : 'transparent',
                border: selectedUser?.id === user.id ? '1px solid var(--va-primary)' : '1px solid transparent'
              }"
            >
              <va-list-item-section avatar>
                <va-icon name="account_circle" size="large" />
              </va-list-item-section>
              <va-list-item-section>
                <va-list-item-label style="font-weight: bold; font-size: 1rem;">{{ user.username }}</va-list-item-label>
                <div style="display: flex; gap: 0.35rem; align-items: center; margin-top: 0.2rem; flex-wrap: wrap;">
                  <RoleBadge :value="user.role" />
                  <va-badge :text="getOrgName(user.organizationId)" color="info" outline size="small" />
                  <va-badge v-if="getDeptName(user.departmentId)" :text="getDeptName(user.departmentId)" color="success" outline size="small" />
                </div>
              </va-list-item-section>
            </va-list-item>
          </va-list>
          <div style="margin-top: 1rem; display: flex; justify-content: center;" v-if="totalPages > 1">
            <va-pagination v-model="currentPage" :pages="totalPages" @update:modelValue="fetchUsers" :visible-pages="5" size="small" />
          </div>
        </va-card-content>
      </va-card>

      <!-- Permissions & Org Assignment -->
      <va-card v-if="selectedUser" style="flex: 2; min-width: 420px;">
        <va-card-title style="display: flex; justify-content: space-between; align-items: center;">
          <span>{{ $t('permissions') }}: {{ selectedUser.username }}</span>
          <va-badge :text="selectedUser.username" color="success" />
        </va-card-title>
        <va-card-content>
          
          <!-- User System Role Setting -->
          <div style="background: var(--va-background-secondary); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 1.25rem; margin-bottom: 1.5rem;">
            <h3 style="font-weight: 700; margin-bottom: 0.5rem; color: var(--va-text-primary); font-size: 1.05rem; display: flex; align-items: center; gap: 0.5rem;">
              <va-icon name="manage_accounts" color="primary" />
              {{ $t('user_role') || '사용자 시스템 권한 역할' }}
            </h3>
            
            <div style="display: flex; gap: 0.5rem; margin-bottom: 1rem; align-items: center; font-size: 0.88rem; color: var(--va-text-secondary); flex-wrap: wrap;">
              <span>{{ $t('current_affiliation') }}</span>
              <va-badge :text="getOrgName(selectedUser.organizationId)" color="info" outline size="small" />
              <va-badge v-if="getDeptName(selectedUser.departmentId)" :text="getDeptName(selectedUser.departmentId)" color="success" outline size="small" />
              <span v-else style="font-style: italic; color: #888;">{{ $t('no_dept_assigned_tip') }}</span>
            </div>

            <div style="display: flex; gap: 0.75rem; align-items: flex-end;">
              <UserRoleSelect
                v-model="selectedUserRoles"
                multiple
                :org-id="selectedUser?.organizationId"
                :label="getLabel('user_roles', '사용자 시스템 역할 (다중 선택 가능)')"
                style="flex: 1;"
              />
              <va-button v-if="hasPermission('admin:write') || hasPermission('admin:*') || hasPermission('org:write') || hasPermission('org:*')" color="primary" icon="save" @click="updateUserRoleOnly">
                {{ $t('save_changes') || '역할 저장' }}
              </va-button>
            </div>
          </div>

          <va-divider style="margin: 1.5rem 0;" />

          <!-- Domain Permissions -->
          <div style="margin-bottom: 1rem;">
            <h3 style="font-weight: bold; margin-bottom: 0.5rem; color: var(--va-text-primary);">{{ $t('granted_domains') }}</h3>
            <div v-if="userPermissions.length === 0" style="color: var(--va-text-secondary); font-size: 0.85rem;">{{ $t('no_specific_domain_permissions') }}</div>
            <div v-else style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
              <va-chip v-for="perm in userPermissions" :key="perm.id" color="success" style="margin-bottom: 0.5rem;">
                {{ getDomainName(perm.domain.name) }}
                <va-icon v-if="hasPermission('admin:write') || hasPermission('admin:*') || hasPermission('org:write') || hasPermission('org:*')" name="close" size="small" style="margin-left: 0.5rem; cursor: pointer;" @click="revokePermission(perm.domain.id)" />
              </va-chip>
            </div>
          </div>
          
          <va-divider style="margin: 1.5rem 0;" />
          
          <div>
            <h3 style="font-weight: bold; margin-bottom: 0.5rem; color: var(--va-text-primary);">{{ $t('grant_new_permission') }}</h3>
            <div style="display: flex; gap: 0.5rem; align-items: flex-end;">
              <va-select
                v-model="selectedDomainsToGrant"
                multiple
                :options="availableDomains"
                value-by="id"
                text-by="label"
                :placeholder="$t('select_a_domain') || '도메인을 선택하세요 (다중 선택 가능)'"
                style="flex: 1;"
              />
              <va-button v-if="hasPermission('admin:write') || hasPermission('admin:*') || hasPermission('org:write') || hasPermission('org:*')" @click="grantPermissions" :disabled="!selectedDomainsToGrant || selectedDomainsToGrant.length === 0">
                {{ $t('grant') || '권한 부여' }}
              </va-button>
            </div>
          </div>

          <va-divider style="margin: 1.5rem 0;" />

          <!-- Organization Change History (Admin Only) -->
          <div>
            <h3 style="font-weight: bold; margin-bottom: 0.75rem; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.4rem;">
              <va-icon name="history" color="primary" />
              <span>{{ $t('org_history_title') }}</span>
            </h3>
            <div v-if="!userOrgHistory || userOrgHistory.length === 0" style="color: var(--va-text-secondary); font-size: 0.85rem; padding: 0.5rem 0;">
              {{ $t('no_org_history') }}
            </div>
            <div v-else style="display: flex; flex-direction: column; gap: 0.5rem; max-height: 220px; overflow-y: auto;">
              <div
                v-for="h in userOrgHistory"
                :key="h.id"
                style="padding: 0.75rem 1rem; background: var(--va-background-element); border-radius: 8px; border: 1px solid var(--va-background-border); font-size: 0.85rem;"
              >
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.35rem;">
                  <span style="font-weight: 600; color: var(--va-text-secondary);">📅 {{ formatDate(h.changedAt) }}</span>
                  <va-badge size="small" color="info" :text="h.changedBy || 'SYSTEM'" />
                </div>
                <div style="display: flex; align-items: center; gap: 0.5rem; font-weight: 700; color: var(--va-text-primary);">
                  <span>{{ getI18nText(h.prevDepartmentName || h.prevOrganizationName) || '-' }}</span>
                  <va-icon name="arrow_forward" size="small" color="primary" />
                  <span style="color: var(--va-primary);">{{ getI18nText(h.newDepartmentName || h.newOrganizationName) || '-' }}</span>
                </div>
              </div>
            </div>
          </div>
        </va-card-content>
      </va-card>
    </div>

    <!-- Requests -->
    <va-card style="margin-top: 1.5rem;">
      <va-card-title>{{ $t('pending_domain_access_requests') }}</va-card-title>
      <va-card-content>
        <div v-if="pendingRequests.length === 0" style="color: var(--va-text-secondary); font-size: 0.85rem;">{{ $t('no_pending_requests') }}</div>
        <div v-else style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 1.5rem;">
          <va-card v-for="group in groupedPendingRequests" :key="group.userId" outlined style="transition: transform 0.2s, box-shadow 0.2s; cursor: default; background-color: var(--va-background-primary);" class="hoverable-card">
            <va-card-content style="display: flex; flex-direction: column; height: 100%; padding: 1.5rem;">
              <div style="display: flex; align-items: center; gap: 1rem; margin-bottom: 1.25rem;">
                <va-avatar size="large" color="primary" style="font-size: 1.25rem; font-weight: bold; width: 48px; height: 48px;">
                  {{ group.username.charAt(0).toUpperCase() }}
                </va-avatar>
                <div style="flex: 1; overflow: hidden;">
                  <div style="font-weight: 700; font-size: 1.15rem; color: var(--va-text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                    {{ group.username }}
                  </div>
                  <div style="font-size: 0.8rem; color: var(--va-text-secondary); margin-top: 0.2rem;">
                    {{ group.formattedDate }}
                  </div>
                </div>
              </div>

              <div style="flex: 1; margin-bottom: 1.25rem;">
                <div style="font-size: 0.85rem; font-weight: 600; color: var(--va-text-secondary); margin-bottom: 0.5rem;">
                  {{ $t('requested_domains') }} ({{ group.domains.length }})
                </div>
                <div style="display: flex; flex-wrap: wrap; gap: 0.4rem; max-height: 100px; overflow-y: auto; padding: 0.2rem;">
                  <va-chip v-for="(dom, idx) in group.domains" :key="idx" size="small" color="primary" outline>
                    {{ dom }}
                  </va-chip>
                </div>
              </div>

              <div style="display: flex; gap: 0.75rem; justify-content: flex-end; border-top: 1px solid var(--va-background-element); padding-top: 1rem;">
                <va-button v-if="hasPermission('admin:write') || hasPermission('admin:*') || hasPermission('org:write') || hasPermission('org:*')" color="danger" preset="secondary" size="small" @click="handleBatchReject(group.reqIds)">
                  {{ $t('reject') }}
                </va-button>
                <va-button v-if="hasPermission('admin:write') || hasPermission('admin:*') || hasPermission('org:write') || hasPermission('org:*')" color="success" size="small" @click="handleBatchApprove(group.reqIds)">
                  {{ $t('approve') }}
                </va-button>
              </div>
            </va-card-content>
          </va-card>
        </div>
      </va-card-content>
    </va-card>



    <!-- System Notification Modal -->
    <va-modal
      v-model="showErrorAlertModal"
      :title="errorAlertTitle || $t('system_notification')"
      hide-default-actions
      size="small"
      :prevent-click-outside="true"
      :no-outside-dismiss="true"
    >
      <div style="padding: 1.25rem 0; text-align: center;">
        <div
          v-if="errorAlertType === 'success'"
          style="width: 60px; height: 60px; border-radius: 50%; background: rgba(30, 203, 114, 0.12); color: #15803d; display: flex; align-items: center; justify-content: center; margin: 0 auto 1.25rem auto;"
        >
          <va-icon name="check_circle" size="2.5rem" color="success" />
        </div>
        <div
          v-else-if="errorAlertType === 'warning'"
          style="width: 60px; height: 60px; border-radius: 50%; background: rgba(232, 139, 36, 0.12); color: #c2410c; display: flex; align-items: center; justify-content: center; margin: 0 auto 1.25rem auto;"
        >
          <va-icon name="warning" size="2.5rem" color="warning" />
        </div>
        <div
          v-else
          style="width: 60px; height: 60px; border-radius: 50%; background: rgba(229, 57, 53, 0.12); color: #b91c1c; display: flex; align-items: center; justify-content: center; margin: 0 auto 1.25rem auto;"
        >
          <va-icon name="error" size="2.5rem" color="danger" />
        </div>

        <h3
          style="margin: 0 0 0.75rem 0; font-weight: 700; font-size: 1.25rem;"
          :style="{
            color: errorAlertType === 'success' ? '#15803d' : (errorAlertType === 'warning' ? '#c2410c' : '#b91c1c')
          }"
        >
          {{ errorAlertHeader || $t('system_notification') }}
        </h3>

        <div style="background: var(--va-background-secondary); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 1rem 1.25rem; text-align: left; font-size: 0.92rem; color: var(--va-text-primary); max-height: 200px; overflow-y: auto; margin-bottom: 1.5rem; word-break: break-word; white-space: pre-wrap;">
          {{ errorAlertMessage }}
        </div>

        <div style="display: flex; justify-content: center;">
          <va-button
            :color="errorAlertType === 'success' ? 'success' : (errorAlertType === 'warning' ? 'warning' : 'primary')"
            preset="solid"
            style="min-width: 120px;"
            @click="showErrorAlertModal = false"
          >
            {{ $t('close') || '확인' }}
          </va-button>
        </div>
      </div>
    </va-modal>
  </div>
</template>

<script setup>
import { usePermission } from '~/composables/usePermission'

const { t, locale } = useI18n()
const { hasPermission } = usePermission()

const getLabel = (key, fallback) => {
  const res = t(key)
  return (!res || res === key) ? fallback : res
}

const getI18nText = (textStr) => {
  if (!textStr) return ''
  try {
    const parsed = typeof textStr === 'object' ? textStr : JSON.parse(textStr)
    if (parsed && typeof parsed === 'object') {
      const loc = (locale?.value || 'ko').toLowerCase()
      return loc.startsWith('en') ? (parsed.en || parsed.ko || '') : (parsed.ko || parsed.en || '')
    }
    return String(textStr)
  } catch (e) {
    return textStr
  }
}

const getDomainName = (nameObj) => {
  if (!nameObj) return 'Unknown'
  const lang = (locale && locale.value) ? locale.value : 'ko'
  if (typeof nameObj === 'string') {
    try {
      const parsed = JSON.parse(nameObj)
      return parsed[lang] || parsed.ko || parsed.en || 'Unknown'
    } catch {
      return nameObj
    }
  }
  return nameObj[lang] || nameObj.ko || nameObj.en || 'Unknown'
}

const token = useCookie('auth_token')
const showErrorAlertModal = ref(false)
const errorAlertTitle = ref('')
const errorAlertHeader = ref('')
const errorAlertMessage = ref('')
const errorAlertType = ref('success')

const showCustomAlert = (msg, header = '', title = '', type = 'success') => {
  errorAlertMessage.value = msg
  errorAlertHeader.value = header
  errorAlertTitle.value = title
  errorAlertType.value = type
  showErrorAlertModal.value = true
}
const users = ref([])
const organizations = ref([])
const currentPage = ref(1)
const totalPages = ref(1)
const searchQuery = ref('')

const fetchUsers = async () => {
  try {
    const res = await $fetch('/api/permissions/users', {
      headers: { Authorization: `Bearer ${token.value}` },
      query: {
        page: currentPage.value - 1,
        size: 100,
        search: searchQuery.value || ''
      }
    })
    users.value = res.content || []
    totalPages.value = res.totalPages || 1
  } catch (e) {
    console.error(e)
  }
}

const onSearchKeydown = (e) => {
  if (e && e.key === 'Enter') {
    fetchUsers()
  }
}
const selectedUser = ref(null)
const selectedUserRoles = ref([])
const selectedUserOrgId = ref(null)
const selectedUserDeptId = ref(null)
const departments = ref([])

const userPermissions = ref([])
const allDomains = ref([])
const selectedDomainsToGrant = ref([])
const pendingRequests = ref([])

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

const groupedPendingRequests = computed(() => {
  const groups = {}
  pendingRequests.value.forEach(req => {
    const key = req.user.id
    if (!groups[key]) {
      groups[key] = {
        userId: req.user.id,
        username: req.user.username,
        domains: [],
        reqIds: [],
        createdAt: req.createdAt
      }
    }
    groups[key].domains.push(getDomainName(req.domain.name))
    groups[key].reqIds.push(req.id)
    if (new Date(req.createdAt) < new Date(groups[key].createdAt)) {
      groups[key].createdAt = req.createdAt
    }
  })
  return Object.values(groups).map(g => ({
    ...g,
    formattedDate: formatDate(g.createdAt)
  }))
})

const allDepartmentsMap = ref({})

const fetchAllDepartments = async () => {
  try {
    const orgs = organizations.value
    for (const org of orgs) {
      const depts = await $fetch(`/api/organizations/${org.id}/departments`, {
        headers: { Authorization: `Bearer ${token.value}` }
      })
      if (depts) {
        depts.forEach(d => {
          allDepartmentsMap.value[d.id] = d.name
        })
      }
    }
  } catch (e) {
    console.error('Failed to fetch all departments:', e)
  }
}

const fetchOrganizations = async () => {
  try {
    const res = await $fetch('/api/organizations', {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    organizations.value = res || []
    await fetchAllDepartments()
  } catch (e) {
    console.error('Failed to fetch orgs:', e)
  }
}

const getOrgName = (orgId) => {
  if (!orgId) return getLabel('unassigned', '미지정')
  if (!organizations.value || organizations.value.length === 0) return getLabel('unassigned', '미지정')
  const found = organizations.value.find(o => o.id === orgId)
  return found ? (getI18nText(found.displayName) || found.name) : getLabel('unassigned', '미지정')
}

const getDeptName = (deptId) => {
  if (!deptId) return null
  const raw = allDepartmentsMap.value[deptId]
  return raw ? getI18nText(raw) : null
}

const getUserRolesArray = (role) => {
  if (!role) return ['USER']
  if (Array.isArray(role)) return role
  return String(role).split(',').map(r => r.trim()).filter(Boolean)
}

const selectUser = async (user) => {
  if (user) {
    selectedUser.value = user
    selectedUserRoles.value = getUserRolesArray(user.role)
    selectedUserOrgId.value = user.organizationId
    selectedUserDeptId.value = user.departmentId
    await fetchAllDepartments()
    await loadUserPermissions(user.id)
    await loadUserOrgHistory(user.id)
  }
}

const userOrgHistory = ref([])

const loadUserOrgHistory = async (userId) => {
  try {
    userOrgHistory.value = await $fetch(`/api/users/${userId}/org-history`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
  } catch (e) {
    userOrgHistory.value = []
  }
}

const updateUserRoleOnly = async () => {
  if (!selectedUser.value) return
  const roleStr = Array.isArray(selectedUserRoles.value) ? selectedUserRoles.value.join(',') : (selectedUserRoles.value || 'USER')
  try {
    await $fetch(`/api/permissions/users/${selectedUser.value.id}/tenant-info`, {
      method: 'PUT',
      headers: { Authorization: `Bearer ${token.value}` },
      body: {
        role: roleStr,
        organizationId: selectedUser.value.organizationId,
        departmentId: selectedUser.value.departmentId
      }
    })
    selectedUser.value.role = roleStr
    await fetchUsers()
    showCustomAlert(
      getLabel('role_updated_success', '사용자 역할 권한이 성공적으로 변경되었습니다.'),
      getLabel('update_success', '수정 완료'),
      getLabel('notification', '알림'),
      'success'
    )
  } catch (e) {
    showCustomAlert('Failed to update user role: ' + (e.message || String(e)), getLabel('error', '오류'), getLabel('notification', '알림'), 'error')
  }
}

const loadUserPermissions = async (userId) => {
  userPermissions.value = await $fetch(`/api/permissions/users/${userId}/domains`, {
    headers: { Authorization: `Bearer ${token.value}` }
  })
}



const availableDomains = computed(() => {
  const grantedIds = userPermissions.value.map(p => p.domain.id)
  return allDomains.value
    .filter(d => !grantedIds.includes(d.id))
    .map(d => ({ id: d.id, label: getDomainName(d.name) }))
})

const grantPermissions = async () => {
  if (!selectedUser.value || !selectedDomainsToGrant.value || selectedDomainsToGrant.value.length === 0) return
  try {
    await Promise.all(
      selectedDomainsToGrant.value.map(domainId =>
        $fetch(`/api/permissions/users/${selectedUser.value.id}/domains/${domainId}`, {
          method: 'POST',
          headers: { Authorization: `Bearer ${token.value}` }
        })
      )
    )
    selectedDomainsToGrant.value = []
    await loadUserPermissions(selectedUser.value.id)
    showCustomAlert(getLabel('grant_permission_success', '선택한 도메인 권한이 성공적으로 부여되었습니다.'), getLabel('update_success', '부여 완료'), getLabel('notification', '알림'), 'success')
  } catch (e) {
    showCustomAlert('Failed to grant permissions: ' + (e.message || String(e)), getLabel('error', '오류'), getLabel('notification', '알림'), 'error')
  }
}

const revokePermission = async (domainId) => {
  if (!selectedUser.value) return
  await $fetch(`/api/permissions/users/${selectedUser.value.id}/domains/${domainId}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${token.value}` }
  })
  await loadUserPermissions(selectedUser.value.id)
}

const handleBatchApprove = async (reqIds) => {
  try {
    await Promise.all(reqIds.map(id => 
      $fetch(`/api/permissions/requests/${id}/approve`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token.value}` }
      })
    ))
    await fetchPendingRequests()
    if (selectedUser.value) {
      await loadUserPermissions(selectedUser.value.id)
    }
  } catch (e) {
    console.error(e)
  }
}

const handleBatchReject = async (reqIds) => {
  try {
    await Promise.all(reqIds.map(id => 
      $fetch(`/api/permissions/requests/${id}/reject`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token.value}` },
      })
    ))
    await fetchPendingRequests()
  } catch (e) {
    console.error(e)
  }
}

const fetchDomains = async () => {
  try {
    const res = await $fetch('/api/domains', {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    allDomains.value = res || []
  } catch (e) {
    console.error('Failed to fetch domains:', e)
  }
}

const fetchPendingRequests = async () => {
  try {
    const res = await $fetch('/api/permissions/requests/pending', {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    pendingRequests.value = res || []
  } catch (e) {
    console.error('Failed to fetch pending requests:', e)
  }
}

onMounted(() => {
  fetchOrganizations()
  fetchUsers()
  fetchDomains()
  fetchPendingRequests()
})
</script>
