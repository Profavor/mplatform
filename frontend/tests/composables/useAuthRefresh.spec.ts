import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

// Mock dependencies of #imports
const mockLoggedIn = { value: true }
const mockUser = {
  value: {
    accessToken: 'mock-access-token-1',
    refreshToken: 'mock-refresh-token-1'
  }
}
const mockRefresh = vi.fn()
const mockLogin = vi.fn()
const mockLogout = vi.fn()

const mockCookieStore: Record<string, any> = {}

vi.mock('#imports', () => ({
  useOidcAuth: () => ({
    loggedIn: mockLoggedIn,
    user: mockUser,
    refresh: mockRefresh,
    login: mockLogin,
    logout: mockLogout
  }),
  useCookie: (name: string, options?: any) => ({
    get value() {
      return mockCookieStore[name] ?? null
    },
    set value(v: any) {
      mockCookieStore[name] = v
    }
  }),
  useRuntimeConfig: () => ({
    public: {
      accessTokenExpirationSec: 1800,
      refreshTokenExpirationSec: 86400
    }
  })
}))

import { useAuthRefresh } from '../../composables/useAuthRefresh'

describe('useAuthRefresh (TDD Unit Test)', () => {
  const createMockJwt = (expSec: number, iss = 'http://mplatform.local/auth/realms/mplatform') => {
    const header = btoa(JSON.stringify({ alg: 'RS256', typ: 'JWT' }))
    const payload = btoa(JSON.stringify({
      exp: expSec,
      iss: iss,
      sub: 'user-1',
      preferred_username: 'superadmin'
    }))
    return `${header}.${payload}.sig`
  }

  const getAuth = () => useAuthRefresh({
    oidcAuth: {
      loggedIn: mockLoggedIn,
      user: mockUser,
      refresh: mockRefresh,
      login: mockLogin,
      logout: mockLogout
    }
  })

  beforeEach(() => {
    vi.useFakeTimers()
    const { resetRefreshState, clearAuthCookies } = getAuth()
    resetRefreshState()
    clearAuthCookies()
    mockLoggedIn.value = true
    mockUser.value = {
      accessToken: 'mock-access-token-1',
      refreshToken: 'mock-refresh-token-1'
    }
    mockRefresh.mockReset()
    mockLogin.mockReset()
    mockLogout.mockReset()
  })

  afterEach(() => {
    vi.useRealTimers()
    vi.restoreAllMocks()
  })

  it('1. parseJwtExp - 유효한 JWT 토큰에서 exp(만료 시각)를 정상 추출하고 잘못된 형식은 null 반환', () => {
    const { parseJwtExp } = getAuth()
    const nowSec = Math.floor(Date.now() / 1000)
    const expSec = nowSec + 1800

    const validToken = createMockJwt(expSec)
    expect(parseJwtExp(validToken)).toBe(expSec)

    expect(parseJwtExp('invalid-token')).toBeNull()
    expect(parseJwtExp('')).toBeNull()
  })

  it('2. performTokenRefresh - OIDC refresh 성공 시 새 토큰을 쿠키에 설정하고 반환한다', async () => {
    const { performTokenRefresh, getCookieValue } = getAuth()
    const nowSec = Math.floor(Date.now() / 1000)
    const newExpSec = nowSec + 1800
    const newAccessToken = createMockJwt(newExpSec)

    mockRefresh.mockImplementation(async () => {
      mockUser.value = {
        accessToken: newAccessToken,
        refreshToken: 'mock-new-refresh-token'
      }
    })

    const token = await performTokenRefresh()
    expect(token).toBe(newAccessToken)
    expect(getCookieValue('auth_token')).toBe(newAccessToken)
    expect(getCookieValue('refresh_token')).toBe('mock-new-refresh-token')
  })

  it('3. performTokenRefresh - OIDC refresh 실패 시 /api/auth/refresh fallback을 호출하여 복구한다', async () => {
    const { performTokenRefresh, setAuthCookies, getCookieValue } = getAuth()
    const nowSec = Math.floor(Date.now() / 1000)
    const fallbackToken = createMockJwt(nowSec + 1800)

    // OIDC 실패 시뮬레이션
    mockRefresh.mockRejectedValue(new Error('Keycloak OIDC failed'))
    setAuthCookies('old-token', 'existing-refresh-token')

    // fetch mock for /api/auth/refresh
    const mockFetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({
        token: fallbackToken,
        refreshToken: 'fallback-refreshed-token'
      })
    })
    globalThis.fetch = mockFetch

    const token = await performTokenRefresh()
    expect(token).toBe(fallbackToken)
    expect(mockFetch).toHaveBeenCalledWith('/api/auth/refresh', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ refreshToken: 'existing-refresh-token' })
    }))
    expect(getCookieValue('auth_token')).toBe(fallbackToken)
  })

  it('4. scheduleSilentRefresh - 만료 60초 전에 자동 갱신 타이머를 스케줄링한다', async () => {
    const { scheduleSilentRefresh } = getAuth()
    const nowSec = Math.floor(Date.now() / 1000)
    const expSec = nowSec + 1800 // 30분 뒤
    const token = createMockJwt(expSec)

    const nextToken = createMockJwt(nowSec + 3600)
    mockRefresh.mockImplementation(async () => {
      mockUser.value = {
        accessToken: nextToken,
        refreshToken: 'next-refresh-token'
      }
    })

    scheduleSilentRefresh(token)

    // 29분(1740초) 경과 전에는 아직 실행되지 않음
    vi.advanceTimersByTime(1730 * 1000)
    expect(mockRefresh).not.toHaveBeenCalled()

    // 29분 도달 시 자동 갱신 트리거
    await vi.advanceTimersByTime(15 * 1000)
    expect(mockRefresh).toHaveBeenCalled()
  })
})
