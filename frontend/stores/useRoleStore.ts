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

  const getRoleColor = (code: string): string => {
    if (!code) return 'secondary'
    const norm = code.replace(/^ROLE_/, '').toUpperCase().trim()
    switch (norm) {
      case 'ADMIN': return 'danger'              // 🔴 Red (#ef4444)
      case 'ORG_ADMIN': return 'warning'         // 🟡 Gold/Amber (#f59e0b)
      case 'DATA_STEWARD': return 'primary'      // 🔵 Blue (#2563eb)
      case 'DOMAIN_EDITOR': return 'info'        // 🩵 Light Cyan (#06b6d4)
      case 'DQ_MANAGER': return 'success'        // 🟢 Emerald Green (#10b981)
      case 'INTEGRATION': return '#8b5cf6'       // 🟣 Purple (#8b5cf6)
      case 'WORKFLOW': return '#f97316'          // 🟠 Orange (#f97316)
      case 'USER': return '#6366f1'              // 🫐 Indigo (#6366f1)
      case 'VIEWER': return 'secondary'          // ⚪ Slate Gray (#64748b)
      default: {
        const colors = ['#8b5cf6', '#f97316', '#ec4899', '#14b8a6', '#6366f1', '#84cc16', '#a855f7']
        let hash = 0
        for (let i = 0; i < norm.length; i++) {
          hash = norm.charCodeAt(i) + ((hash << 5) - hash)
        }
        return colors[Math.abs(hash) % colors.length]
      }
    }
  }

  const getRoleBadgeStyle = (code: string, isDark = false): string => {
    if (!code) return ''
    const norm = code.replace(/^ROLE_/, '').toUpperCase().trim()
    if (isDark) {
      switch (norm) {
        case 'ADMIN':
          return 'background: linear-gradient(135deg, #dc2626, #991b1b); color: #fee2e2; border: 1px solid rgba(252,165,165,0.4);'
        case 'ORG_ADMIN':
          return 'background: linear-gradient(135deg, #d97706, #92400e); color: #fef3c7; border: 1px solid rgba(252,211,77,0.4);'
        case 'DATA_STEWARD':
          return 'background: linear-gradient(135deg, #2563eb, #1e40af); color: #dbeafe; border: 1px solid rgba(147,197,253,0.4);'
        case 'DOMAIN_EDITOR':
          return 'background: linear-gradient(135deg, #0891b2, #164e63); color: #cffafe; border: 1px solid rgba(103,232,249,0.4);'
        case 'DQ_MANAGER':
          return 'background: linear-gradient(135deg, #059669, #065f46); color: #d1fae5; border: 1px solid rgba(110,231,183,0.4);'
        case 'INTEGRATION':
          return 'background: linear-gradient(135deg, #7c3aed, #5b21b6); color: #ede9fe; border: 1px solid rgba(196,181,253,0.4);'
        case 'WORKFLOW':
          return 'background: linear-gradient(135deg, #ea580c, #9a3412); color: #ffedd5; border: 1px solid rgba(254,215,170,0.4);'
        case 'USER':
          return 'background: linear-gradient(135deg, #4f46e5, #3730a3); color: #e0e7ff; border: 1px solid rgba(165,180,252,0.4);'
        case 'VIEWER':
          return 'background: linear-gradient(135deg, #4b5563, #1f2937); color: #f3f4f6; border: 1px solid rgba(209,213,219,0.3);'
        default:
          return 'background: linear-gradient(135deg, #475569, #334155); color: #f8fafc;'
      }
    }
    switch (norm) {
      case 'ADMIN': return 'background: linear-gradient(135deg, #ef4444, #dc2626); color: white;'
      case 'ORG_ADMIN': return 'background: linear-gradient(135deg, #f59e0b, #d97706); color: white;'
      case 'DATA_STEWARD': return 'background: linear-gradient(135deg, #2563eb, #1d4ed8); color: white;'
      case 'DOMAIN_EDITOR': return 'background: linear-gradient(135deg, #06b6d4, #0891b2); color: white;'
      case 'DQ_MANAGER': return 'background: linear-gradient(135deg, #10b981, #059669); color: white;'
      case 'INTEGRATION': return 'background: linear-gradient(135deg, #8b5cf6, #7c3aed); color: white;'
      case 'WORKFLOW': return 'background: linear-gradient(135deg, #f97316, #ea580c); color: white;'
      case 'USER': return 'background: linear-gradient(135deg, #6366f1, #4f46e5); color: white;'
      case 'VIEWER': return 'background: linear-gradient(135deg, #64748b, #475569); color: white;'
      default: return 'background: linear-gradient(135deg, #64748b, #475569); color: white;'
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
    getRoleColor,
    getRoleBadgeStyle,
    getUserOrgId
  }
}

