import { ref } from 'vue'
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

// Cache role lists by organization ID
const orgRolesMap = ref<Record<string, RoleInfo[]>>({})
// Global role lookup map for code -> RoleInfo (fetched purely from DB)
const globalRoleLookupMap = ref<Record<string, RoleInfo>>({})
let globalRolesPromise: Promise<void> | null = null

export function useRoles() {
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

  // Purely fetch all roles from DB using async/await Promise caching (NO HARDCODING)
  const initGlobalRoles = async (forceRefresh = false): Promise<void> => {
    if (globalRolesPromise && !forceRefresh) {
      await globalRolesPromise
      return
    }

    globalRolesPromise = (async () => {
      try {
        const headers = token.value ? { Authorization: `Bearer ${token.value}` } : {}
        const list = await $fetch<RoleInfo[]>('/api/roles', { headers })
        if (Array.isArray(list)) {
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
          globalRoleLookupMap.value = newMap
        }
      } catch (e) {
        console.error('Failed to fetch roles from DB:', e)
        globalRolesPromise = null
      }
    })()

    await globalRolesPromise
  }

  const fetchRolesForOrg = async (orgId?: string | null, forceRefresh = false): Promise<RoleInfo[]> => {
    const targetOrgId = orgId || getUserOrgId()
    const cacheKey = targetOrgId || 'GLOBAL'

    // Always await DB global roles fetch first
    await initGlobalRoles(forceRefresh)

    if (!forceRefresh && orgRolesMap.value[cacheKey]) {
      return orgRolesMap.value[cacheKey]
    }

    try {
      const endpoint = targetOrgId ? `/api/roles/org/${targetOrgId}` : '/api/roles'
      const headers = token.value ? { Authorization: `Bearer ${token.value}` } : {}
      const list = await $fetch<RoleInfo[]>(endpoint, { headers })

      if (Array.isArray(list)) {
        orgRolesMap.value[cacheKey] = list
        list.forEach(r => {
          if (r && r.name) {
            globalRoleLookupMap.value[r.name] = r
            const clean = r.name.startsWith('ROLE_') ? r.name.replace('ROLE_', '') : r.name
            const prefixed = r.name.startsWith('ROLE_') ? r.name : `ROLE_${r.name}`
            globalRoleLookupMap.value[clean] = r
            globalRoleLookupMap.value[prefixed] = r
          }
        })
        return list
      }
    } catch (e) {
      console.error(`Failed to fetch roles for org (${cacheKey}):`, e)
    }

    return orgRolesMap.value[cacheKey] || []
  }

  // Pure DB lookup only - NO hardcoding
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

  return {
    orgRolesMap,
    globalRoleLookupMap,
    initGlobalRoles,
    fetchRolesForOrg,
    getRoleDisplayName,
    formatRoleText,
    getUserOrgId
  }
}
