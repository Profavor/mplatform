import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'

describe('Token Refresh & Seamless Auth Flow (TDD)', () => {
  let loggedIn: any
  let user: any
  let authTokenCookie: any
  let refreshTokenCookie: any
  let isRefreshing: boolean
  let refreshPromise: Promise<string | null> | null

  beforeEach(() => {
    loggedIn = ref(false)
    user = ref<any>(null)
    authTokenCookie = ref<string | null>(null)
    refreshTokenCookie = ref<string | null>(null)
    isRefreshing = false
    refreshPromise = null
  })

  // 토큰 갱신 시뮬레이션 함수
  const createTokenRefreshHandler = (options: {
    oidcRefreshSuccess?: boolean
    internalRefreshSuccess?: boolean
    newAccessToken?: string
    newRefreshToken?: string
  } = {}) => {
    let oidcCallCount = 0
    let internalCallCount = 0

    const performTokenRefresh = async (): Promise<string | null> => {
      if (isRefreshing && refreshPromise) {
        return await refreshPromise
      }

      isRefreshing = true
      refreshPromise = (async () => {
        try {
          // 실제 네트워크 I/O 비동기 지연 시뮬레이션
          await new Promise((resolve) => setTimeout(resolve, 15))

          // 1. OIDC 세션 리프레시 시도
          if (loggedIn.value) {
            oidcCallCount++
            if (options.oidcRefreshSuccess !== false) {
              const newToken = options.newAccessToken || 'new-oidc-access-token'
              const newRefToken = options.newRefreshToken || 'new-oidc-refresh-token'
              authTokenCookie.value = newToken
              refreshTokenCookie.value = newRefToken
              if (user.value) {
                user.value.accessToken = newToken
                user.value.refreshToken = newRefToken
              }
              return newToken
            }
          }

          // 2. 백엔드 내부 리프레시 토큰 시도
          if (refreshTokenCookie.value) {
            internalCallCount++
            if (options.internalRefreshSuccess !== false) {
              const newToken = options.newAccessToken || 'new-internal-access-token'
              const newRefToken = options.newRefreshToken || 'new-internal-refresh-token'
              authTokenCookie.value = newToken
              refreshTokenCookie.value = newRefToken
              loggedIn.value = true
              return newToken
            }
          }

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
      getOidcCallCount: () => oidcCallCount,
      getInternalCallCount: () => internalCallCount
    }
  }

  it('액세스 토큰 쿠키가 만료되었으나 OIDC 세션이 유효한 경우, 토큰 갱신 후 라우트 통과해야 함', async () => {
    loggedIn.value = true
    user.value = { accessToken: null, refreshToken: 'valid-refresh-token' }
    authTokenCookie.value = null // 쿠키 만료로 소멸됨

    const { performTokenRefresh } = createTokenRefreshHandler({
      oidcRefreshSuccess: true,
      newAccessToken: 'refreshed-jwt-token'
    })

    // 라우트 가드 시뮬레이션
    const routeGuard = async (toPath: string) => {
      if (toPath === '/login') return { allowed: true }

      let currentToken = authTokenCookie.value
      if (!currentToken && (loggedIn.value || refreshTokenCookie.value)) {
        currentToken = await performTokenRefresh()
      }

      if (!currentToken) {
        return { allowed: false, redirect: '/login' }
      }
      return { allowed: true }
    }

    const result = await routeGuard('/admin/users')
    expect(result.allowed).toBe(true)
    expect(result.redirect).toBeUndefined()
    expect(authTokenCookie.value).toBe('refreshed-jwt-token')
  })

  it('액세스 토큰 쿠키가 만료되었으나 백엔드 refresh_token 쿠키가 있는 경우 복구 후 라우트 통과해야 함', async () => {
    loggedIn.value = false
    authTokenCookie.value = null
    refreshTokenCookie.value = 'valid-internal-refresh-token'

    const { performTokenRefresh, getInternalCallCount } = createTokenRefreshHandler({
      internalRefreshSuccess: true,
      newAccessToken: 'recovered-access-token'
    })

    const routeGuard = async (toPath: string) => {
      if (toPath === '/login') return { allowed: true }

      let currentToken = authTokenCookie.value
      if (!currentToken && (loggedIn.value || refreshTokenCookie.value)) {
        currentToken = await performTokenRefresh()
      }

      if (!currentToken) {
        return { allowed: false, redirect: '/login' }
      }
      return { allowed: true }
    }

    const result = await routeGuard('/records')
    expect(result.allowed).toBe(true)
    expect(authTokenCookie.value).toBe('recovered-access-token')
    expect(getInternalCallCount()).toBe(1)
  })

  it('모든 리프레시 토큰이 만료되어 갱신에 실패한 경우에만 /login으로 리다이렉트되어야 함', async () => {
    loggedIn.value = true
    authTokenCookie.value = null
    refreshTokenCookie.value = 'expired-refresh-token'

    const { performTokenRefresh } = createTokenRefreshHandler({
      oidcRefreshSuccess: false,
      internalRefreshSuccess: false
    })

    const routeGuard = async (toPath: string) => {
      if (toPath === '/login') return { allowed: true }

      let currentToken = authTokenCookie.value
      if (!currentToken && (loggedIn.value || refreshTokenCookie.value)) {
        currentToken = await performTokenRefresh()
      }

      if (!currentToken) {
        return { allowed: false, redirect: '/login' }
      }
      return { allowed: true }
    }

    const result = await routeGuard('/records')
    expect(result.allowed).toBe(false)
    expect(result.redirect).toBe('/login')
  })

  it('동시에 여러 API 요청에서 401이 발생해도 토큰 갱신은 단 1회만 호출되어야 함 (Deduplication)', async () => {
    loggedIn.value = true
    user.value = { accessToken: 'old-token' }

    const { performTokenRefresh, getOidcCallCount } = createTokenRefreshHandler({
      oidcRefreshSuccess: true,
      newAccessToken: 'new-shared-token'
    })

    // 5개의 동시 요청 시뮬레이션
    const results = await Promise.all([
      performTokenRefresh(),
      performTokenRefresh(),
      performTokenRefresh(),
      performTokenRefresh(),
      performTokenRefresh()
    ])

    expect(results).toEqual([
      'new-shared-token',
      'new-shared-token',
      'new-shared-token',
      'new-shared-token',
      'new-shared-token'
    ])
    expect(getOidcCallCount()).toBe(1)
  })

  it('JWT exp 파싱 및 만료 60초 전 사전 갱신(Silent Refresh) 시간 계산이 정확해야 함', () => {
    // Fake JWT with payload exp = now + 300초 (5분 뒤)
    const nowSec = Math.floor(Date.now() / 1000)
    const expSec = nowSec + 300
    const payloadBase64 = Buffer.from(JSON.stringify({ exp: expSec, sub: 'user1' })).toString('base64url')
    const fakeJwt = `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.${payloadBase64}.signature`

    const getJwtExp = (token: string): number | null => {
      try {
        const parts = token.split('.')
        if (parts.length < 2) return null
        const decoded = JSON.parse(Buffer.from(parts[1], 'base64url').toString('utf8'))
        return typeof decoded.exp === 'number' ? decoded.exp : null
      } catch {
        return null
      }
    }

    const calculateSilentRefreshDelay = (token: string, thresholdSec = 60): number => {
      const exp = getJwtExp(token)
      if (!exp) return 0
      const currentSec = Math.floor(Date.now() / 1000)
      const remainingSec = exp - currentSec
      const delaySec = Math.max(0, remainingSec - thresholdSec)
      return delaySec * 1000 // ms
    }

    const exp = getJwtExp(fakeJwt)
    expect(exp).toBe(expSec)

    const delayMs = calculateSilentRefreshDelay(fakeJwt, 60)
    // 300초 - 60초 = 240초 (약 240,000ms)
    expect(delayMs).toBeGreaterThanOrEqual(239000)
    expect(delayMs).toBeLessThanOrEqual(240000)
  })
})
