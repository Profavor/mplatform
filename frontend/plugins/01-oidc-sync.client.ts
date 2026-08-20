import { useOidcAuth, useCookie } from '#imports'

export default defineNuxtPlugin((nuxtApp) => {
  const { loggedIn, user } = useOidcAuth()
  const authTokenCookie = useCookie('auth_token', { maxAge: 1800, path: '/' })
  const refreshTokenCookie = useCookie('refresh_token', { maxAge: 86400, path: '/' })

  const syncTokens = () => {
    if (loggedIn.value && user.value) {
      if (user.value.accessToken) {
        authTokenCookie.value = user.value.accessToken
      }
      const refToken = user.value.refreshToken || (user.value as any)?.providerInfo?.refreshToken
      if (refToken) {
        refreshTokenCookie.value = refToken
      }
    } else if (!loggedIn.value) {
      authTokenCookie.value = null
      refreshTokenCookie.value = null
    }
  }

  // 즉시 동기화 (plugin 실행 시점)
  syncTokens()

  watchEffect(() => {
    syncTokens()
  })
})

