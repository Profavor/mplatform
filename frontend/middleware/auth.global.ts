import { useOidcAuth, useCookie, navigateTo } from '#imports'
import { useAuthRefresh } from '~/composables/useAuthRefresh'

export default defineNuxtRouteMiddleware(async (to, from) => {
  if (
    to.path === '/login' ||
    to.path === '/install' ||
    to.path.startsWith('/auth') ||
    to.path.startsWith('/api')
  ) {
    return
  }

  const { loggedIn, user } = useOidcAuth()
  let token = useCookie('auth_token').value
  const refreshToken = useCookie('refresh_token').value

  const { performTokenRefresh, parseJwtExp } = useAuthRefresh()

  // 토큰 쿠키가 비어있으나 OIDC 세션이나 리프레시 토큰이 살아있는 경우 무중단 토큰 갱신 시도
  if (!token && (loggedIn.value || !!refreshToken)) {
    try {
      token = await performTokenRefresh()
    } catch (e) {
      console.warn('Auth middleware: Silent refresh failed', e)
    }
  } else if (token && (loggedIn.value || !!refreshToken)) {
    // 토큰 만료 30초 이내 임박 시 라우트 이동 시점에 사전 갱신
    const exp = parseJwtExp(token)
    const nowSec = Math.floor(Date.now() / 1000)
    if (exp && exp - nowSec <= 30) {
      try {
        const refreshed = await performTokenRefresh()
        if (refreshed) token = refreshed
      } catch (e) {
        console.warn('Auth middleware: Proactive refresh failed', e)
      }
    }
  }

  // OIDC 세션에서 토큰이 확인되는 경우 쿠키가 채워질 때까지 동기화 허용
  if (!token && loggedIn.value && user.value?.accessToken) {
    token = user.value.accessToken
  }

  // 루트 경로('/') 접근 시 로그인 상태에 따라 분기:
  // 1) 로그인된 사용자는 내부 업무 대시보드(/dashboard)로 자동 리다이렉트
  // 2) 비로그인 방문자는 소개 홈페이지(랜딩 페이지) 자유 열람 허용
  if (to.path === '/') {
    if (token) {
      return navigateTo('/dashboard')
    }
    return
  }

  if (!token) {
    const isExpired = !!refreshToken || Boolean(from && from.path && from.path !== '/login' && from.path !== '/')
    const redirectPath = (to.fullPath && to.fullPath !== '/' && to.fullPath !== '/login') ? to.fullPath : undefined
    return navigateTo({
      path: '/login',
      query: {
        ...(redirectPath ? { redirect: redirectPath } : {}),
        ...(isExpired ? { expired: '1' } : {})
      }
    })
  }

  // 관리자 전용 라우트 (/admin/**) 접근 제어 (일반 사용자의 관리자 페이지 직접 접근 차단)
  if (to.path.startsWith('/admin')) {
    let userRole = ''
    try {
      const authUserStore = useAuthUser()
      userRole = authUserStore?.currentUser?.role || ''
    } catch (e) {}

    if (!userRole) {
      const userCookie = useCookie<any>('user_data')
      try {
        const raw = userCookie.value
        const parsed = typeof raw === 'string' ? JSON.parse(raw) : raw
        userRole = parsed?.role || ''
      } catch (e) {}
    }

    const roles = Array.isArray(userRole)
      ? userRole
      : String(userRole).split(',').map(r => r.trim())
    const hasAdminAccess = roles.some(r =>
      r === 'ROLE_ADMIN' || r === 'ADMIN' || r === 'ORG_ADMIN' || r === 'ROLE_ORG_ADMIN' || r === 'DATA_STEWARD'
    )

    if (!hasAdminAccess) {
      return navigateTo('/')
    }
  }
})
