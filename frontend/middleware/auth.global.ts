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

  if (!token) {
    return navigateTo('/login')
  }
})
