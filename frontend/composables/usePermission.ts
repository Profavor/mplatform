/**
 * 사용자 권한 판별 공통 Composable
 */

export function hasPermission(
  requiredPermission: string,
  userPermissions?: any[] | null
): boolean {
  if (!userPermissions || !Array.isArray(userPermissions) || userPermissions.length === 0) {
    return false
  }

  const normalizePerm = (perm: any): string => {
    if (!perm) return ''
    if (typeof perm === 'string') return perm.trim().toLowerCase()
    if (typeof perm === 'object') {
      const val = perm.authority || perm.name || perm.value || ''
      return String(val).trim().toLowerCase()
    }
    return String(perm).trim().toLowerCase()
  }

  // 1. 앞자리가 '*' 인 경우 (전역 모든 권한: '*', '*:*', '*:write' 등)
  const hasGlobalWildcard = userPermissions.some(perm => {
    const trimmed = normalizePerm(perm)
    return trimmed === '*' || trimmed === '*:*' || trimmed.startsWith('*:')
  })
  if (hasGlobalWildcard) {
    return true
  }

  // 2. 정확한 권한 코드 매칭 (Exact Match: 'domain:write' === 'domain:write')
  const normalizedRequired = requiredPermission.trim().toLowerCase()
  const hasExactMatch = userPermissions.some(perm => normalizePerm(perm) === normalizedRequired)
  if (hasExactMatch) {
    return true
  }

  // 3. 뒷자리가 '*' 인 경우 (해당 리소스의 모든 권한: 'domain:*' -> 'domain:write', 'domain:read')
  if (normalizedRequired.includes(':')) {
    const domainPrefix = normalizedRequired.split(':')[0] + ':*'
    const hasDomainWildcard = userPermissions.some(perm => normalizePerm(perm) === domainPrefix)
    if (hasDomainWildcard) {
      return true
    }
  }

  return false
}

export function usePermission() {
  const userPermissionsCookie = useCookie<any>('user_permissions')
  const userDataCookie = useCookie<any>('user_data')
  const userCookie = useCookie<any>('user')

  let userData: any = {}
  try {
    const rawData = userDataCookie.value || userCookie.value
    if (rawData) {
      userData = typeof rawData === 'string' ? JSON.parse(rawData) : rawData
    }
  } catch (e) {
    userData = {}
  }

  let permissions = userPermissionsCookie.value || userData.permissions || []
  if (typeof permissions === 'string') {
    try {
      permissions = JSON.parse(permissions)
    } catch {
      permissions = [permissions]
    }
  }

  const checkPermission = (perm: string) => hasPermission(perm, permissions)

  return {
    hasPermission: checkPermission
  }
}
