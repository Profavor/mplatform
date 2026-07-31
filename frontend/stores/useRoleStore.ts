import { ref, computed } from 'vue'
import { useCookie } from '#app'
import { getMultilingualText } from '~/utils/multilingual'

export interface RoleInfo {
  id?: string
  organizationId?: string
  name: string
  displayName?: string
  description?: string
  isSystemRole?: boolean
}

// Global reactive Store state (singleton across client session)
const rolesList = ref<RoleInfo[]>([])
const orgRolesMap = ref<Record<string, RoleInfo[]>>({})
const globalRoleLookupMap = ref<Record<string, RoleInfo>>({})
const isInitialized = ref(false)
const isLoading = ref(false)
let globalRolesPromise: Promise<RoleInfo[]> | null = null

export function useRoleStore() {
  const token = useCookie('auth_token')
  const userCookie = useCookie<any>('user_data')

  const getUserOrgId = (): string | null => {
    if (!userCookie.value) return null
    try {
      const data = typeof userCookie.value === 'string' ? JSON.parse(userCookie.value) : userCookie.value
      return data?.organizationId || null
    } catch {
      return null
    }
  }

  const fetchRolesForOrg = async (orgId?: string | null, forceRefresh = false): Promise<RoleInfo[]> => {
    const targetOrgId = orgId || getUserOrgId()
    const cacheKey = targetOrgId || 'GLOBAL'

    if (!forceRefresh && isInitialized.value && orgRolesMap.value[cacheKey] && orgRolesMap.value[cacheKey].length > 0) {
      return orgRolesMap.value[cacheKey]
    }

    if (globalRolesPromise && !forceRefresh) {
      return await globalRolesPromise
    }

    isLoading.value = true
    globalRolesPromise = (async () => {
      try {
        const endpoint = targetOrgId ? `/api/roles/org/${targetOrgId}` : '/api/roles'
        const headers = token.value ? { Authorization: `Bearer ${token.value}` } : {}
        const list = await $fetch<RoleInfo[]>(endpoint, { headers })

        if (Array.isArray(list)) {
          rolesList.value = list
          orgRolesMap.value[cacheKey] = list
          const newMap: Record<string, RoleInfo> = {}
          list.forEach(r => {
            if (r && r.name) {
              newMap[r.name] = r
              const clean = r.name.startsWith('ROLE_') ? r.name.replace('ROLE_', '') : r.name
              const prefixed = r.name.startsWith('ROLE_') ? r.name : `ROLE_${r.name}`
              newMap[clean] = r
              newMap[prefixed] = r
            }
          })
          globalRoleLookupMap.value = { ...globalRoleLookupMap.value, ...newMap }
          isInitialized.value = true
          return list
        }
      } catch (e) {
        console.error(`Failed to fetch roles for org (${cacheKey}):`, e)
      } finally {
        isLoading.value = false
        globalRolesPromise = null
      }
      return orgRolesMap.value[cacheKey] || rolesList.value || []
    })()

    return await globalRolesPromise
  }

  // Dispatch action to fetch or refresh roles in store
  const dispatch = async (action: 'fetchRoles' | 'refresh', payload?: { orgId?: string; forceRefresh?: boolean }): Promise<RoleInfo[]> => {
    if (action === 'fetchRoles' || action === 'refresh') {
      return await fetchRolesForOrg(payload?.orgId, payload?.forceRefresh || action === 'refresh')
    }
    return rolesList.value
  }

  const getRoleDisplayName = (code: string): string => {
    if (!code) return ''
    const cleanInput = code.trim()
    const cleanCode = cleanInput.startsWith('ROLE_') ? cleanInput.replace('ROLE_', '') : cleanInput

    const role = globalRoleLookupMap.value[cleanInput] || globalRoleLookupMap.value[cleanCode]
    if (role && role.displayName) {
      const text = getMultilingualText(role.displayName)
      if (text) return text
    }
    return cleanInput
  }

  const formatRoleText = (code: string, hideCode = false): string => {
    if (!code) return ''
    const cleanInput = code.trim()
    const rawDisp = getRoleDisplayName(cleanInput)
    const disp = getMultilingualText(rawDisp)
    if (hideCode) {
      return disp || cleanInput
    }
    if (disp && disp !== cleanInput && !disp.startsWith(cleanInput)) {
      return `${cleanInput} (${disp})`
    }
    return disp || cleanInput
  }

  const roleOptions = computed(() => {
    return rolesList.value.map(r => ({
      value: r.name,
      text: getRoleDisplayName(r.name) || r.name
    }))
  })

  const syncDefaultRoles = async (orgId?: string | null): Promise<boolean> => {
    try {
      isLoading.value = true
      const endpoint = orgId ? `/api/roles/org/${orgId}/sync-defaults` : '/api/roles/sync-defaults'
      const headers = token.value ? { Authorization: `Bearer ${token.value}` } : {}
      await $fetch(endpoint, { method: 'POST', headers })
      await fetchRolesForOrg(orgId, true)
      return true
    } catch (e) {
      console.error('Failed to sync default roles:', e)
      return false
    } finally {
      isLoading.value = false
    }
  }

  return {
    rolesList,
    orgRolesMap,
    globalRoleLookupMap,
    isInitialized,
    isLoading,
    roleOptions,
    dispatch,
    initGlobalRoles: (force = false) => fetchRolesForOrg(null, force),
    fetchRolesForOrg,
    syncDefaultRoles,
    getRoleDisplayName,
    formatRoleText,
    getUserOrgId
  }
}

