import { useOidcAuth, useCookie, useRuntimeConfig } from '#imports'

let isRefreshing = false
let refreshPromise: Promise<string | null> | null = null
let silentRefreshTimer: ReturnType<typeof setTimeout> | null = null

export function useAuthRefresh(options?: { oidcAuth?: any }) {
  const config = useRuntimeConfig()
  const oidc = options?.oidcAuth || useOidcAuth()
  const accessMaxAge = Number(config?.public?.accessTokenExpirationSec || 1800)
  const refreshMaxAge = Number(config?.public?.refreshTokenExpirationSec || 86400)

  const getCookieValue = (name: string): string | null => {
    if (typeof document !== 'undefined' && document.cookie) {
      const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'))
      if (match) return decodeURIComponent(match[2])
    }
    try {
      const cv = useCookie(name).value
      if (cv) return cv
    } catch {}
    return null
  }

  const setAuthCookies = (authToken: string, refreshToken?: string, expSec?: number) => {
    let maxAge = accessMaxAge
    if (expSec && expSec > 0) {
      const nowSec = Math.floor(Date.now() / 1000)
      const remaining = expSec - nowSec
      if (remaining > 0) {
        maxAge = remaining
      }
    }
    if (typeof document !== 'undefined') {
      document.cookie = `auth_token=${authToken}; max-age=${maxAge}; path=/; SameSite=Lax`
      if (refreshToken) {
        document.cookie = `refresh_token=${refreshToken}; max-age=${refreshMaxAge}; path=/; SameSite=Lax`
      }
    }
    try {
      const cookieRef = useCookie('auth_token', { maxAge, path: '/' })
      cookieRef.value = authToken
    } catch {}

    if (refreshToken) {
      try {
        const refCookie = useCookie('refresh_token', { maxAge: refreshMaxAge, path: '/' })
        refCookie.value = refreshToken
      } catch {}
    }
  }

  const clearAuthCookies = () => {
    if (silentRefreshTimer) {
      clearTimeout(silentRefreshTimer)
      silentRefreshTimer = null
    }
    const cookies = ['auth_token', 'token', 'refresh_token', 'user_data']
    if (typeof document !== 'undefined') {
      cookies.forEach((c) => {
        document.cookie = `${c}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`
        try {
          if (typeof window !== 'undefined') {
            document.cookie = `${c}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=${window.location.hostname};`
          }
        } catch {}
      })
    }
    cookies.forEach((c) => {
      try {
        useCookie(c).value = null
      } catch {}
    })
  }

  const parseJwtExp = (token: string): number | null => {
    try {
      if (!token || typeof token !== 'string') return null
      const parts = token.split('.')
      if (parts.length < 2) return null
      let base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
      while (base64.length % 4 !== 0) {
        base64 += '='
      }
      let jsonStr = ''
      if (typeof atob === 'function') {
        jsonStr = atob(base64)
      } else if (typeof Buffer !== 'undefined') {
        jsonStr = Buffer.from(base64, 'base64').toString('utf-8')
      } else {
        return null
      }
      const payload = JSON.parse(jsonStr)
      return typeof payload.exp === 'number' ? payload.exp : null
    } catch {
      return null
    }
  }

  const scheduleSilentRefresh = (token: string) => {
    if (silentRefreshTimer) {
      clearTimeout(silentRefreshTimer)
      silentRefreshTimer = null
    }

    const exp = parseJwtExp(token)
    if (!exp) return

    const nowSec = Math.floor(Date.now() / 1000)
    const remainingSec = exp - nowSec
    if (remainingSec <= 10) {
      // 이미 만료 직전이면 즉시 갱신 시도
      performTokenRefresh()
      return
    }

    // 만료 60초 전(또는 120초 미만 남은 경우 남은 시간의 50% 시점)에 백그라운드 갱신
    const refreshLeadSec = remainingSec > 120 ? 60 : Math.max(5, Math.floor(remainingSec * 0.5))
    const delayMs = Math.max(3000, (remainingSec - refreshLeadSec) * 1000)

    silentRefreshTimer = setTimeout(async () => {
      try {
        console.info('[Auth] Triggering silent background token refresh...')
        const newToken = await performTokenRefresh()
        if (newToken) {
          console.info('[Auth] Silent token refresh succeeded.')
        }
      } catch (e) {
        console.warn('[Auth] Silent token refresh failed:', e)
      }
    }, delayMs)
  }

  const resetRefreshState = () => {
    isRefreshing = false
    refreshPromise = null
    if (silentRefreshTimer) {
      clearTimeout(silentRefreshTimer)
      silentRefreshTimer = null
    }
  }

  const performTokenRefresh = async (): Promise<string | null> => {
    if (isRefreshing && refreshPromise) {
      return await refreshPromise
    }

    isRefreshing = true
    refreshPromise = (async () => {
      try {
        // 1. OIDC (Keycloak SSO) 방식 갱신 우선 시도
        try {
          const { refresh, user, loggedIn } = oidc
          if (loggedIn?.value) {
            await refresh()
            if (loggedIn?.value && user?.value?.accessToken) {
              const newAccessToken = user.value.accessToken
              const newRefreshToken = user.value.refreshToken || (user.value as any)?.providerInfo?.refreshToken
              const exp = parseJwtExp(newAccessToken)
              setAuthCookies(newAccessToken, newRefreshToken, exp || undefined)
              scheduleSilentRefresh(newAccessToken)
              return newAccessToken
            }
          }
        } catch (oidcErr) {
          console.warn('useAuthRefresh: OIDC token refresh failed, attempting fallback', oidcErr)
        }

        // 2. 백엔드 내부 리프레시 토큰(/api/auth/refresh) 시도
        let internalRefreshToken = getCookieValue('refresh_token')
        if (!internalRefreshToken) {
          try {
            internalRefreshToken = useCookie('refresh_token').value
          } catch {}
        }
        if (internalRefreshToken) {
          try {
            const res = await fetch('/api/auth/refresh', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ refreshToken: internalRefreshToken })
            })
            if (res.ok) {
              const data = await res.json()
              if (data && (data.token || data.accessToken)) {
                const token = data.token || data.accessToken
                const refToken = data.refreshToken || internalRefreshToken
                const exp = parseJwtExp(token)
                setAuthCookies(token, refToken, exp || undefined)
                scheduleSilentRefresh(token)
                return token
              }
            }
          } catch (internalErr) {
            console.warn('useAuthRefresh: Internal token refresh failed', internalErr)
          }
        }

        return null
      } catch (e) {
        console.warn('useAuthRefresh: Token refresh failed completely', e)
        return null
      } finally {
        isRefreshing = false
        refreshPromise = null
      }
    })()

    return await refreshPromise
  }

  return {
    performTokenRefresh,
    setAuthCookies,
    clearAuthCookies,
    scheduleSilentRefresh,
    parseJwtExp,
    getCookieValue,
    resetRefreshState
  }
}
