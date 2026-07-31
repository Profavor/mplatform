import { translateBackendError } from '~/utils/errorTranslator'
import { ofetch } from 'ofetch'

let isRefreshing = false
let refreshPromise: Promise<string | null> | null = null

export default defineNuxtPlugin((nuxtApp) => {
  const config = useRuntimeConfig()
  const accessMaxAge = Number(config.public.accessTokenExpirationSec || 1800)
  const refreshMaxAge = Number(config.public.refreshTokenExpirationSec || 172800)

  const getCookieValue = (name: string) => {
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

  // Native pure fetch for refreshing tokens without any interceptor interference or Authorization headers
  const performTokenRefresh = async (refreshToken: string): Promise<string | null> => {
    if (isRefreshing && refreshPromise) {
      return await refreshPromise
    }

    isRefreshing = true
    refreshPromise = (async () => {
      try {
        const response = await window.fetch('/api/auth/refresh', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ refreshToken })
        })

        if (!response.ok) {
          clearAuthCookies()
          return null
        }

        const res = await response.json()
        if (res && res.token) {
          setAuthCookies(res.token, res.refreshToken)
          return res.token
        }
        clearAuthCookies()
        return null
      } catch (err) {
        clearAuthCookies()
        return null
        return null
      } finally {
        isRefreshing = false
        refreshPromise = null
      }
    })()

    return await refreshPromise
  }

  const customFetch = ofetch.create({
    async onRequest({ request, options }) {
      if (!process.client) return
      const reqUrl = request.toString()

      if (reqUrl.includes('/api/auth/login') || reqUrl.includes('/api/auth/refresh')) {
        return
      }

      let authToken = getCookieValue('auth_token')

      if (authToken) {
        options.headers = options.headers || {}
        if (options.headers instanceof Headers) {
          options.headers.set('Authorization', `Bearer ${authToken}`)
        } else if (Array.isArray(options.headers)) {
          options.headers = options.headers.filter(([k]) => k.toLowerCase() !== 'authorization')
          options.headers.push(['Authorization', `Bearer ${authToken}`])
        } else {
          (options.headers as Record<string, string>)['Authorization'] = `Bearer ${authToken}`
        }
      }
    },

    async onResponseError({ request, response, options }) {
      if (response.status === 401 && process.client) {
        const reqUrl = request.toString()
        if (reqUrl.includes('/api/auth/login') || reqUrl.includes('/api/auth/refresh')) {
          if (reqUrl.includes('/api/auth/refresh')) {
            clearAuthCookies()
          }
          return
        }

        const respBody = typeof response._data === 'string' ? response._data : JSON.stringify(response._data || '')
        if (respBody.includes('another device') || respBody.includes('Session expired')) {
          clearAuthCookies()
          if (window.location.pathname !== '/login') {
            window.location.href = '/login'
          }
          return
        }

        const refreshToken = getCookieValue('refresh_token')
        if (!refreshToken) {
          clearAuthCookies()
          if (window.location.pathname !== '/login') {
            window.location.href = '/login'
          }
          return
        }

        // Try token refresh via native fetch
        const newToken = await performTokenRefresh(refreshToken)
        if (newToken) {
          // Update headers with new token
          options.headers = options.headers || {}
          if (options.headers instanceof Headers) {
            options.headers.set('Authorization', `Bearer ${newToken}`)
          } else if (Array.isArray(options.headers)) {
            options.headers = options.headers.filter(([k]) => k.toLowerCase() !== 'authorization')
            options.headers.push(['Authorization', `Bearer ${newToken}`])
          } else {
            (options.headers as Record<string, string>)['Authorization'] = `Bearer ${newToken}`
          }
          // Retry original request with new token
          return customFetch(request, options)
        } else {
          clearAuthCookies()
          if (window.location.pathname !== '/login') {
            window.location.href = '/login'
          }
          return
        }
      }

      // Universal Error Interceptor for message translation
      try {
        const i18n = nuxtApp.vueApp.config.globalProperties.$i18n
        if (i18n && i18n.t) {
          const t = (key: string, params?: any) => i18n.t(key, params)
          if (response._data) {
            if (typeof response._data === 'object' && response._data.errorCode) {
              response._data.translatedMessage = translateBackendError(response._data, t)
            } else if (typeof response._data === 'string') {
              response._data = translateBackendError(response._data, t)
            }
          }
        }
      } catch (e) {
        console.warn('Global error translation failed', e)
      }
    }
  })

  globalThis.$fetch = customFetch
  if (process.client) {
    (window as any).$fetch = customFetch
  }

  return {
    provide: {
      fetch: customFetch
    }
  }
})

function clearAuthCookies() {
  if (process.client) {
    document.cookie = 'auth_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;'
    document.cookie = 'refresh_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;'
    document.cookie = 'user_data=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;'
  }
}
