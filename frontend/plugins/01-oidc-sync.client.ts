import { useOidcAuth, useCookie } from '#imports'
import { useAuthRefresh } from '~/composables/useAuthRefresh'

export default defineNuxtPlugin(async (nuxtApp) => {
  if (!process.client || typeof document === 'undefined') return

  const { loggedIn, user, fetch: fetchOidcSession } = useOidcAuth()
  const { setAuthCookies, clearAuthCookies, scheduleSilentRefresh, parseJwtExp, performTokenRefresh } = useAuthRefresh()

  // 클라이언트 기동 시 세션 정보가 없으면 /api/_auth/session을 호출하여 세션 상태 복원
  if (!user.value) {
    try {
      await fetchOidcSession()
    } catch (e) {}
  }

  const syncTokens = () => {
    if (typeof document === 'undefined') return
    if (loggedIn.value && user.value) {
      const accessToken = user.value.accessToken
      const refToken = user.value.refreshToken || (user.value as any)?.providerInfo?.refreshToken
      if (accessToken) {
        const exp = parseJwtExp(accessToken)
        setAuthCookies(accessToken, refToken, exp || undefined)
        scheduleSilentRefresh(accessToken)

        // 신규 로그인 세션 감지 시 백엔드 로그인 이력 적재 (클라이언트 Fallback & 5초 이내 중복 방지)
        try {
          const tokenKey = accessToken.slice(-24)
          if (sessionStorage.getItem('last_login_recorded_token') !== tokenKey) {
            sessionStorage.setItem('last_login_recorded_token', tokenKey)
            const username = user.value.userName || (user.value as any)?.claims?.preferred_username
            if (username) {
              const { customFetch } = useCustomFetch()
              customFetch('/api/auth/record-login', {
                method: 'POST',
                body: { username }
              }).catch(() => {})
            }
          }
        } catch (e) {}
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

  watch([loggedIn, () => user.value], () => {
    syncTokens()
  }, { immediate: true, deep: true })

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
