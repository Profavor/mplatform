import { useOidcAuth, useCookie, navigateTo } from '#imports'
import { useAuthRefresh } from '~/composables/useAuthRefresh'

export default defineNuxtRouteMiddleware(async (to, from) => {
  if (
    to.path === '/login' ||
    to.path === '/install' ||
    to.path === '/roi-calculator' ||
    to.path.startsWith('/auth') ||
    to.path.startsWith('/api')
  ) {
    return
  }

  const { loggedIn, user, fetch: fetchOidcSession } = useOidcAuth()
  let token = useCookie('auth_token').value
  const refreshToken = useCookie('refresh_token').value

  const { performTokenRefresh, parseJwtExp } = useAuthRefresh()

  // 1. 토큰 쿠키가 없고 user 세션 정보도 없는 경우 세션 상태 복원 시도 (SSR 및 클라이언트 공통)
  if (!token && !user.value && typeof fetchOidcSession === 'function') {
    try {
      await fetchOidcSession()
    } catch (e) {
      console.warn('Auth middleware: OIDC session fetch failed', e)
    }
  }

  // 2. OIDC 세션에서 토큰이 확인되는 경우 즉시 token에 할당하고 쿠키에 동기화
  if (!token && (loggedIn.value || user.value?.accessToken) && user.value?.accessToken) {
    token = user.value.accessToken
    try {
      const exp = parseJwtExp(token)
      const nowSec = Math.floor(Date.now() / 1000)
      const maxAge = exp && exp > nowSec ? Math.max(60, exp - nowSec) : 1800
      const tokenCookie = useCookie('auth_token', { maxAge, path: '/', sameSite: 'lax' })
      tokenCookie.value = token
    } catch (e) {}
  }

  // 3. 토큰 쿠키가 여전히 비어있고 리프레시 토큰이 살아있는 경우 무중단 토큰 갱신 시도
  if (!token && !!refreshToken) {
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

  // 루트 경로('/') 접근 시 로그인 상태에 따라 분기:
  // 1) 로그인된 사용자는 내부 업무 대시보드(/dashboard)로 자동 리다이렉트
  // 2) 비로그인 방문자는 소개 홈페이지(랜딩 페이지) 자유 열람 허용
  if (to.path === '/') {
    if (token || loggedIn.value) {
      return navigateTo('/dashboard')
    }
    return
  }

  // 회원가입 경로('/register') 접근 시:
  // 이미 로그인된 사용자는 대시보드로 리다이렉트, 비로그인 방문자는 회원가입 허용
  if (to.path === '/register') {
    if (token || loggedIn.value) {
      return navigateTo('/dashboard')
    }
    return
  }

  if (!token) {
    // 기존에 리프레시 토큰이 존재했으나 갱신에 실패한 만료 세션인 경우에만 expired: '1' 표시
    const isExpired = !!refreshToken
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
