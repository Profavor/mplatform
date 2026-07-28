export default defineNuxtRouteMiddleware(async (to, from) => {
  if (to.path === '/login' || to.path === '/install') {
    return
  }

  const token = useCookie('auth_token')
  const refreshToken = useCookie('refresh_token')
  const userDataCookie = useCookie('user_data')

  // 1. Access Token이 없는데 Refresh Token이 유효하게 남아있는 경우 미들웨어 단계에서 자동 갱신 시도
  if (!token.value && refreshToken.value) {
    try {
      const res: any = await $fetch('/api/auth/refresh', {
        method: 'POST',
        body: { refreshToken: refreshToken.value }
      })

      if (res && res.token) {
        token.value = res.token
        if (res.refreshToken) {
          refreshToken.value = res.refreshToken
        }
        return
      }
    } catch (e) {
      console.warn('Middleware failed to auto refresh token:', e)
    }
  }

  // 2. Refresh Token마저 없거나 갱신에 최종 실패한 경우에만 로그인 페이지로 리다이렉트
  if (!token.value) {
    return navigateTo('/login')
  }

  // 3. 관리자 라우트 인가(Authorization) 가드: /admin/* 접근 시 역할 확인
  if (to.path.startsWith('/admin')) {
    try {
      const userData = userDataCookie.value
      if (userData) {
        const parsed = typeof userData === 'string' ? JSON.parse(userData) : userData
        const role = parsed?.role || ''
        const roles = role.split(',').map((r: string) => r.trim().toUpperCase())
        const adminRoles = ['ADMIN', 'ROLE_ADMIN', 'ORG_ADMIN', 'ROLE_ORG_ADMIN']
        const hasAdminAccess = roles.some((r: string) => adminRoles.includes(r))
        if (!hasAdminAccess) {
          return navigateTo('/')
        }
      }
    } catch (e) {
      // user_data 파싱 실패 시 접근 허용 (백엔드 @PreAuthorize가 최종 방어)
    }
  }
})
