import { useOidcAuth, useCookie } from '#imports'
import { useAuthRefresh } from '~/composables/useAuthRefresh'

export default defineNuxtPlugin((nuxtApp) => {
  if (!process.client || typeof document === 'undefined') return

  const { loggedIn, user } = useOidcAuth()
  const { setAuthCookies, clearAuthCookies, scheduleSilentRefresh, parseJwtExp, performTokenRefresh } = useAuthRefresh()

  const syncTokens = () => {
    if (typeof document === 'undefined') return
    if (loggedIn.value && user.value) {
      const accessToken = user.value.accessToken
      const refToken = user.value.refreshToken || (user.value as any)?.providerInfo?.refreshToken
      if (accessToken) {
        const exp = parseJwtExp(accessToken)
        setAuthCookies(accessToken, refToken, exp || undefined)
        scheduleSilentRefresh(accessToken)
      }
    } else if (!loggedIn.value) {
      try {
        const authToken = useCookie('auth_token').value
        const refreshToken = useCookie('refresh_token').value
        if (!authToken && !refreshToken) {
          clearAuthCookies()
        }
      } catch {}
    }
  }

  // 즉시 동기화
  syncTokens()

  watchEffect(() => {
    syncTokens()
  })

  // 브라우저 탭 활성화 시 토큰 만료 여부 확인 및 무중단 갱신
  if (typeof document !== 'undefined' && document.addEventListener) {
    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible') {
        try {
          const token = useCookie('auth_token').value
          const refreshToken = useCookie('refresh_token').value
          if (token) {
            const exp = parseJwtExp(token)
            const nowSec = Math.floor(Date.now() / 1000)
            if (exp && exp - nowSec <= 60) {
              console.info('[Auth] Tab became visible and token is near expiry. Refreshing...')
              performTokenRefresh()
            }
          } else if (loggedIn.value || refreshToken) {
            console.info('[Auth] Tab became visible without auth_token. Attempting refresh...')
            performTokenRefresh()
          }
        } catch {}
      }
    })
  }
})
