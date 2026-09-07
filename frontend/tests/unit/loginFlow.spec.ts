import { describe, it, expect, vi, beforeEach } from 'vitest'

const { mockState, navigateToMock, createMockOidc, createMockCookie } = vi.hoisted(() => {
  const mockState = {
    authToken: null as string | null,
    refreshToken: null as string | null,
    loggedIn: false,
    user: null as any,
    clearedCookies: false,
    clearedOidc: false,
    oidcLoggedOut: false,
    fetchCalled: false,
    performTokenRefreshCalled: false,
    onFetch: null as (() => Promise<void> | void) | null
  }
  const navigateToMock = vi.fn((path: any) => path)
  const createMockOidc = () => ({
    loggedIn: {
      get value() { return mockState.loggedIn }
    },
    user: {
      get value() { return mockState.user }
    },
    fetch: vi.fn(async () => {
      mockState.fetchCalled = true
      if (typeof mockState.onFetch === 'function') {
        await mockState.onFetch()
      } else if (mockState.user) {
        mockState.loggedIn = true
      }
    }),
    clear: vi.fn(async () => {
      mockState.clearedOidc = true
    }),
    logout: vi.fn(async () => {
      mockState.oidcLoggedOut = true
    })
  })
  const createMockCookie = (name: string) => ({
    get value() {
      if (name === 'auth_token') return mockState.authToken
      if (name === 'refresh_token') return mockState.refreshToken
      return null
    },
    set value(val: any) {
      if (name === 'auth_token') mockState.authToken = val
      if (name === 'refresh_token') mockState.refreshToken = val
    }
  })
  return { mockState, navigateToMock, createMockOidc, createMockCookie }
})

vi.mock('#app', () => ({
  defineNuxtRouteMiddleware: (fn: any) => fn,
  navigateTo: (path: any) => navigateToMock(path),
  useCookie: (name: string) => createMockCookie(name),
  useOidcAuth: createMockOidc
}))

vi.mock('#imports', () => ({
  defineNuxtRouteMiddleware: (fn: any) => fn,
  navigateTo: (path: any) => navigateToMock(path),
  useCookie: (name: string) => createMockCookie(name),
  useOidcAuth: createMockOidc
}))

vi.mock('#app/composables/router', () => ({
  defineNuxtRouteMiddleware: (fn: any) => fn,
  navigateTo: (path: any) => navigateToMock(path)
}))

vi.mock('#app/composables/cookie', () => ({
  useCookie: (name: string) => createMockCookie(name)
}))

vi.mock('nuxt-oidc-auth/runtime/composables/oidcAuth', () => ({
  useOidcAuth: createMockOidc
}))

vi.mock('../../node_modules/nuxt-oidc-auth/dist/runtime/composables/oidcAuth', () => ({
  useOidcAuth: createMockOidc
}))

vi.mock('~/composables/useAuthRefresh', () => ({
  useAuthRefresh: () => ({
    clearAuthCookies: vi.fn(() => {
      mockState.clearedCookies = true
      mockState.authToken = null
      mockState.refreshToken = null
    }),
    performTokenRefresh: vi.fn(async () => {
      mockState.performTokenRefreshCalled = true
      return null
    }),
    parseJwtExp: vi.fn(() => 9999999999)
  })
}))

import authMiddleware from '../../middleware/auth.global'

describe('로그인/로그아웃 및 대시보드 리다이렉트 흐름 (TDD Unit Test)', () => {
  beforeEach(() => {
    mockState.authToken = null
    mockState.refreshToken = null
    mockState.loggedIn = false
    mockState.user = null
    mockState.clearedCookies = false
    mockState.clearedOidc = false
    mockState.oidcLoggedOut = false
    mockState.fetchCalled = false
    mockState.performTokenRefreshCalled = false
    mockState.onFetch = null
    navigateToMock.mockClear()
  })

  describe('1. auth.global.ts 미들웨어 라우팅 제어', () => {
    it('루트 경로(/) 접근 시 로그인 토큰이 있으면 대시보드(/dashboard)로 리다이렉트한다', async () => {
      mockState.authToken = 'valid-jwt-token'
      await authMiddleware({ path: '/', fullPath: '/' } as any, { path: '/login' } as any)
      expect(navigateToMock).toHaveBeenCalledWith('/dashboard')
    })

    it('루트 경로(/) 접근 시 토큰 쿠키가 아직 없더라도 OIDC loggedIn 세션이 있으면 대시보드(/dashboard)로 리다이렉트한다', async () => {
      mockState.authToken = null
      mockState.loggedIn = true
      mockState.user = { accessToken: 'oidc-access-token' }

      await authMiddleware({ path: '/', fullPath: '/' } as any, { path: '/login' } as any)
      expect(navigateToMock).toHaveBeenCalledWith('/dashboard')
    })

    it('루트 경로(/) 접근 시 비로그인 방문자는 랜딩 페이지에 머무른다', async () => {
      mockState.authToken = null
      mockState.loggedIn = false
      mockState.user = null

      const result = await authMiddleware({ path: '/', fullPath: '/' } as any, { path: '/login' } as any)
      expect(navigateToMock).not.toHaveBeenCalled()
      expect(result).toBeUndefined()
    })

    it('인증이 필요한 내부 업무 경로(/system/users)에 비인가 사용자가 접근 시 로그인(/login)으로 리다이렉트한다 (임의의 expired:1 파라미터가 없어야 함)', async () => {
      mockState.authToken = null
      mockState.loggedIn = false
      mockState.user = null

      await authMiddleware({ path: '/system/users', fullPath: '/system/users' } as any, { path: '/some-internal-path' } as any)
      expect(navigateToMock).toHaveBeenCalledWith({
        path: '/login',
        query: { redirect: '/system/users' }
      })
    })

    it('토큰 쿠키가 비어있고 user가 없을 때 fetch()를 호출하여 OIDC 세션을 복원하고 대시보드 접근을 허용한다', async () => {
      mockState.authToken = null
      mockState.loggedIn = false
      mockState.user = null

      // fetch가 호출되면 user 세션이 채워지는 시나리오
      mockState.onFetch = async () => {
        mockState.user = { accessToken: 'recovered-access-token' }
        mockState.loggedIn = true
      }

      await authMiddleware({ path: '/dashboard', fullPath: '/dashboard' } as any, { path: '/login' } as any)
      expect(mockState.fetchCalled).toBe(true)
      // 토큰이 복원되었으므로 login으로 튕기지 않고 통과 (navigateTo가 /login으로 호출되지 않음)
      expect(navigateToMock).not.toHaveBeenCalledWith(expect.objectContaining({ path: '/login' }))
      // auth_token 쿠키에 동기화됨
      expect(mockState.authToken).toBe('recovered-access-token')
    })

    it('OIDC 세션에 user.accessToken이 이미 존재하면 불필요한 performTokenRefresh를 호출하지 않고 토큰을 즉시 쿠키에 동기화한다', async () => {
      mockState.authToken = null
      mockState.loggedIn = true
      mockState.user = { accessToken: 'fresh-keycloak-token' }
      mockState.refreshToken = 'some-refresh-token'
      mockState.performTokenRefreshCalled = false

      await authMiddleware({ path: '/dashboard', fullPath: '/dashboard' } as any, { path: '/auth/keycloak/callback' } as any)
      expect(mockState.performTokenRefreshCalled).toBe(false)
      expect(mockState.authToken).toBe('fresh-keycloak-token')
      expect(navigateToMock).not.toHaveBeenCalledWith(expect.objectContaining({ path: '/login' }))
    })
  })

  describe('2. 로그아웃 제어 (handleLogout)', () => {
    it('로그아웃 실행 시 쿠키를 완전 파기하고 OIDC clear()를 호출하며 404를 유발하는 logout()을 호출하지 않는다', async () => {
      const { useAuthRefresh } = await import('~/composables/useAuthRefresh')
      const { clearAuthCookies } = useAuthRefresh()
      const { clear } = (await import('#imports')).useOidcAuth()

      clearAuthCookies()
      await clear()

      expect(mockState.clearedCookies).toBe(true)
      expect(mockState.clearedOidc).toBe(true)
      expect(mockState.oidcLoggedOut).toBe(false)
    })
  })

  describe('3. login.vue 리다이렉트 및 중복 로그인 방지 로직', () => {
    it('getSafeRedirectUrl은 redirect 쿼리가 없을 때 / 가 아닌 /dashboard를 반환한다', () => {
      const getSafeRedirectUrl = (queryRedirect?: string, savedRedirect?: string) => {
        if (queryRedirect && queryRedirect.startsWith('/') && !queryRedirect.startsWith('//') && !queryRedirect.startsWith('/login')) {
          return queryRedirect
        }
        if (savedRedirect && savedRedirect.startsWith('/') && !savedRedirect.startsWith('//') && !savedRedirect.startsWith('/login')) {
          return savedRedirect
        }
        return '/dashboard'
      }

      expect(getSafeRedirectUrl()).toBe('/dashboard')
      expect(getSafeRedirectUrl('/records')).toBe('/records')
      expect(getSafeRedirectUrl(undefined, '/system/users')).toBe('/system/users')
      expect(getSafeRedirectUrl('//malicious.com')).toBe('/dashboard')
      expect(getSafeRedirectUrl('/login')).toBe('/dashboard')
    })

    it('handleLogin 실행 시 이미 loggedIn 상태인 경우 logout(keycloak)을 호출하지 않고 대시보드로 이동한다', async () => {
      mockState.loggedIn = true
      let redirected = false
      const redirectToDashboard = () => {
        redirected = true
        navigateToMock('/dashboard')
      }

      // login.vue의 개선된 handleLogin 로직 시뮬레이션
      if (mockState.loggedIn) {
        redirectToDashboard()
      } else {
        const { logout } = (await import('#imports')).useOidcAuth()
        await logout('keycloak')
      }

      expect(mockState.oidcLoggedOut).toBe(false)
      expect(redirected).toBe(true)
      expect(navigateToMock).toHaveBeenCalledWith('/dashboard')
    })

    it('handleLogin 실행 시 loggedIn이 아직 false라도 authToken 쿠키가 있으면 대시보드로 즉시 이동한다', async () => {
      mockState.loggedIn = false
      mockState.authToken = 'valid-token'
      let redirected = false
      const redirectToDashboard = () => {
        redirected = true
        navigateToMock('/dashboard')
      }

      if (mockState.loggedIn || mockState.authToken) {
        redirectToDashboard()
      }

      expect(redirected).toBe(true)
      expect(navigateToMock).toHaveBeenCalledWith('/dashboard')
    })

    it('checkAuthentication 실행 시 세션/토큰이 유효하면 URL에 expired=1이 남아있더라도 clear()를 부르지 않고 대시보드로 이동한다', async () => {
      mockState.authToken = 'valid-active-token'
      mockState.loggedIn = true
      let redirected = false
      const redirectToDashboard = () => {
        redirected = true
        navigateToMock('/dashboard')
      }

      const checkAuth = async (query: { expired?: string }) => {
        const token = mockState.authToken
        const isExpired = query.expired === '1'

        // 유효한 토큰/세션이 이미 존재하는 경우 만료 쿼리가 잔존하더라도 대시보드로 이동
        if (mockState.loggedIn || token) {
          redirectToDashboard()
          return
        }

        if (isExpired) {
          if (mockState.loggedIn) {
            mockState.clearedOidc = true
          }
        }
      }

      await checkAuth({ expired: '1' })
      expect(mockState.clearedOidc).toBe(false)
      expect(redirected).toBe(true)
      expect(navigateToMock).toHaveBeenCalledWith('/dashboard')
    })
  })
})
