import { translateBackendError } from '~/utils/errorTranslator'
import { ofetch, type FetchOptions } from 'ofetch'
import { useOidcAuth } from '#imports'

let isRefreshing = false
let refreshPromise: Promise<string | null> | null = null

export default defineNuxtPlugin((nuxtApp) => {
  const config = useRuntimeConfig()
  const accessMaxAge = Number(config.public.accessTokenExpirationSec || 1800)
  const refreshMaxAge = Number(config.public.refreshTokenExpirationSec || 86400)

  const getCookieValue = (name: string): string | null => {
    if (!process.client) return null
    const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'))
    return match ? decodeURIComponent(match[2]) : null
  }

  const setAuthCookies = (authToken: string, refreshToken?: string) => {
    if (!process.client) return
    document.cookie = `auth_token=${authToken}; max-age=${accessMaxAge}; path=/;`
    if (refreshToken) {
      document.cookie = `refresh_token=${refreshToken}; max-age=${refreshMaxAge}; path=/;`
    }
  }

  const performTokenRefresh = async (): Promise<string | null> => {
    // 이미 리프레시 중이면 기존 프로미스 재사용 (중복 방지)
    if (isRefreshing && refreshPromise) {
      return await refreshPromise
    }

    isRefreshing = true
    refreshPromise = (async () => {
      try {
        // 1. OIDC (Keycloak SSO) 로그인 방식인 경우 useOidcAuth().refresh() 우선 시도
        try {
          const { refresh, user, loggedIn } = useOidcAuth()
          if (loggedIn.value) {
            await refresh()
            if (loggedIn.value && user.value?.accessToken) {
              const newAccessToken = user.value.accessToken
              const newRefreshToken = user.value.refreshToken || (user.value as any)?.providerInfo?.refreshToken
              setAuthCookies(newAccessToken, newRefreshToken)
              return newAccessToken
            }
          }
        } catch (oidcErr) {
          console.warn('Fetch Interceptor: OIDC token refresh failed', oidcErr)
        }

        // 2. 백엔드 내부 리프레시 토큰(/api/auth/refresh) 시도
        const internalRefreshToken = getCookieValue('refresh_token')
        if (internalRefreshToken) {
          try {
            const res = await fetch('/api/auth/refresh', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ refreshToken: internalRefreshToken })
            })
            if (res.ok) {
              const data = await res.json()
              if (data && data.token) {
                setAuthCookies(data.token, data.refreshToken)
                return data.token
              }
            }
          } catch (internalErr) {
            console.warn('Fetch Interceptor: Internal token refresh failed', internalErr)
          }
        }
        
        return null
      } catch (e) {
        console.warn('Fetch Interceptor: Token refresh failed completely', e)
        return null
      } finally {
        isRefreshing = false
        refreshPromise = null
      }
    })()

    return await refreshPromise
  }

    const applyAuthHeader = (options: FetchOptions, token: string): FetchOptions => {
      options.headers = options.headers || {}
      if (options.headers instanceof Headers) {
        options.headers.set('Authorization', `Bearer ${token}`)
      } else if (Array.isArray(options.headers)) {
        options.headers = (options.headers as [string, string][]).filter(([k]) => k.toLowerCase() !== 'authorization')
        options.headers.push(['Authorization', `Bearer ${token}`])
      } else {
        (options.headers as Record<string, string>)['Authorization'] = `Bearer ${token}`
      }
      return options
    }

    const applyLocaleHeader = (options: FetchOptions, locale: string): FetchOptions => {
      options.headers = options.headers || {}
      if (options.headers instanceof Headers) {
        options.headers.set('Accept-Language', locale)
      } else if (Array.isArray(options.headers)) {
        options.headers = (options.headers as [string, string][]).filter(([k]) => k.toLowerCase() !== 'accept-language')
        options.headers.push(['Accept-Language', locale])
      } else {
        (options.headers as Record<string, string>)['Accept-Language'] = locale
      }
      return options
    }

  // 재시도 가능한 fetch 래퍼: 401 발생 시 토큰 갱신 후 1회 재시도
  const fetchWithRetry = async (request: any, options: FetchOptions = {}): Promise<any> => {
    const reqUrl = typeof request === 'string' ? request : request?.toString?.() || ''
    const isAuthUrl = reqUrl.includes('/api/auth/login') || reqUrl.includes('/api/auth/refresh')
    const skipLoading = (options as any)?.skipLoading || (options as any)?.headers?.['x-skip-loading'] === 'true'
    const { showLoading, hideLoading } = useLoading()

    // 최초 요청 시 현재 토큰 헤더 주입
    if (!isAuthUrl && process.client) {
      let token = getCookieValue('auth_token')
      if (!token) {
        try {
          const { user } = useOidcAuth()
          if (user.value?.accessToken) {
             token = user.value.accessToken
          }
        } catch(e) {}
      }
      if (token) applyAuthHeader(options, token)
    }

    // 다국어 Accept-Language 헤더 주입
    if (process.client) {
      try {
        let localeStr = null
        const i18n = nuxtApp.vueApp.config.globalProperties.$i18n
        if (i18n && i18n.locale) {
          localeStr = typeof i18n.locale === 'string' ? i18n.locale : (i18n.locale.value || null)
        }
        if (!localeStr) {
          localeStr = getCookieValue('i18n_redirected')
        }
        if (localeStr) applyLocaleHeader(options, localeStr)
      } catch (e) {
        console.warn('Failed to inject Accept-Language header', e)
      }
    }

    if (!skipLoading && process.client) {
      showLoading()
    }

    try {
      try {
        return await baseFetch(request, options)
      } catch (err: any) {
        const status = err?.response?.status ?? err?.status

        // 401이 아니거나 auth 경로면 그대로 throw
        if (status !== 401 || isAuthUrl) {
          translateError(err)
          throw err
        }

        console.error('Fetch Interceptor: 401 Unauthorized caught. Checking tokens...');

        // 세션 만료 / 다른 기기 로그인 메시지 체크
        const body = JSON.stringify(err?.response?._data || err?.data || '')
        if (body.includes('another device') || body.includes('Session expired') || body.includes('SESSION_EXPIRED')) {
          console.warn('Fetch Interceptor: Session expired from backend');
          clearAuthCookies()
          if (process.client && window.location.pathname !== '/login') {
            window.location.href = '/login?expired=1'
          }
          throw err
        }

        // OIDC 토큰 갱신 시도
        const newToken = await performTokenRefresh()
        if (!newToken) {
          console.warn('Fetch Interceptor: Token refresh failed. Logging out.');
          clearAuthCookies()
          try {
            const { logout, loggedIn } = useOidcAuth()
            if (loggedIn.value) {
              logout('keycloak').catch(() => {})
            }
          } catch(e) {}
          if (process.client && window.location.pathname !== '/login') {
            window.location.href = '/login?expired=1'
          }
          throw err
        }

        // 새 토큰으로 원래 요청 재시도 (1회)
        console.info('[Auth] Token refreshed. Retrying request:', reqUrl)
        applyAuthHeader(options, newToken)
        try {
          return await baseFetch(request, options)
        } catch (retryErr: any) {
          const retryStatus = retryErr?.response?.status ?? retryErr?.status
          if (retryStatus === 401) {
            console.warn('Fetch Interceptor: Retried request failed with 401. Logging out.');
            clearAuthCookies()
            if (process.client && window.location.pathname !== '/login') {
              window.location.href = '/login?expired=1'
            }
          }
          throw retryErr
        }
      }
    } finally {
      if (!skipLoading && process.client) {
        hideLoading()
      }
    }
  }

  const translateError = (err: any) => {
    try {
      const i18n = nuxtApp.vueApp.config.globalProperties.$i18n
      if (!i18n?.t) return
      const t = (key: string, params?: any) => i18n.t(key, params)
      const data = err?.response?._data
      if (!data) return
      if (typeof data === 'object' && data.errorCode) {
        data.translatedMessage = translateBackendError(data, t)
      } else if (typeof data === 'string') {
        err.response._data = translateBackendError(data, t)
      }
    } catch (e) {
      console.warn('Global error translation failed', e)
    }
  }

  // 인터셉터 없는 순수 ofetch (실제 네트워크 요청용)
  const baseFetch = ofetch.create({
    async onResponseError({ response }) {
      // 에러 번역만 담당 (401 재시도는 fetchWithRetry에서 처리)
      if (response.status === 401) return
      try {
        const i18n = nuxtApp.vueApp.config.globalProperties.$i18n
        if (!i18n?.t) return
        const t = (key: string, params?: any) => i18n.t(key, params)
        if (response._data) {
          if (typeof response._data === 'object' && response._data.errorCode) {
            response._data.translatedMessage = translateBackendError(response._data, t)
          } else if (typeof response._data === 'string') {
            response._data = translateBackendError(response._data, t)
          }
        }
      } catch (e) {
        console.warn('Global error translation failed', e)
      }
    }
  })

  // 전역 $fetch 교체
  globalThis.$fetch = fetchWithRetry as any
  if (process.client) {
    (window as any).$fetch = fetchWithRetry
  }

  return {
    provide: {
      fetch: fetchWithRetry
    }
  }
})

function clearAuthCookies() {
  if (process.client) {
    const cookies = ['auth_token', 'token', 'refresh_token', 'user_data']
    cookies.forEach((c) => {
      document.cookie = `${c}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`
      try {
        document.cookie = `${c}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=${window.location.hostname};`
      } catch {}
    })
  }
}
