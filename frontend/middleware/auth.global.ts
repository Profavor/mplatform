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

  // 토큰 쿠키가 비어있으나 OIDC 세션이나 리프레시 토큰이 살아있는 경우 무중단 토큰 갱신 시도
  if (!token && (loggedIn.value || !!refreshToken)) {
    try {
      const { performTokenRefresh } = useAuthRefresh()
      token = await performTokenRefresh()
    } catch (e) {
      console.warn('Auth middleware: Silent refresh failed', e)
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
