/**
 * 사용자 역할 및 권한 판별 공통 Composable
 */

export function isAdmin(userRole?: string | string[] | null): boolean {
  if (!userRole) return false
  if (Array.isArray(userRole)) {
    return userRole.some(r => isAdmin(r))
  }
  const upper = String(userRole).toUpperCase()
  return upper.includes('ROLE_ADMIN') || upper.includes('ADMIN')
}

export function hasPermission(
  requiredPermission: string,
  userPermissions?: string[] | null,
  userRole?: string | string[] | null
): boolean {
  if (isAdmin(userRole)) {
    return true
  }

  if (!userPermissions || !Array.isArray(userPermissions) || userPermissions.length === 0) {
    return false
  }

  if (userPermissions.includes('*')) {
    return true
  }

  if (userPermissions.includes(requiredPermission)) {
    return true
  }

  // 와일드카드 체크 (예: node:* -> node:write, node:read)
  if (requiredPermission.includes(':')) {
    const domainPrefix = requiredPermission.split(':')[0] + ':*'
    if (userPermissions.includes(domainPrefix)) {
      return true
    }
  }

  return false
}

export function usePermission() {
  const userDataCookie = useCookie<any>('user_data')
  const userPermissionsCookie = useCookie<any>('user_permissions')
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

  const role = userData.role || ''

  const checkPermission = (perm: string) => hasPermission(perm, permissions, role)
  const checkIsAdmin = () => isAdmin(role)

  return {
    hasPermission: checkPermission,
    isAdmin: checkIsAdmin,
  }
}
