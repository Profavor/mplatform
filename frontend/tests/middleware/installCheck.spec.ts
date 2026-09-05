import { describe, it, expect, vi, beforeEach } from 'vitest'

const { mockState, mockNavigateTo, mockFetch } = vi.hoisted(() => {
  return {
    mockState: {
      cookieVal: null as string | null,
      loggedIn: false
    },
    mockNavigateTo: vi.fn((path: string) => `navigated-to-${path}`),
    mockFetch: vi.fn()
  }
})

vi.mock('#app', () => ({
  defineNuxtRouteMiddleware: (fn: any) => fn,
  navigateTo: (path: string) => mockNavigateTo(path),
  useCookie: () => ({
    get value() {
      return mockState.cookieVal
    },
    set value(v: any) {
      mockState.cookieVal = v
    }
  }),
  useOidcAuth: () => ({
    loggedIn: {
      get value() {
        return mockState.loggedIn
      }
    }
  }),
  useRequestFetch: () => mockFetch
}))

vi.mock('#imports', () => ({
  useCookie: () => ({
    get value() {
      return mockState.cookieVal
    },
    set value(v: any) {
      mockState.cookieVal = v
    }
  }),
  useOidcAuth: () => ({
    loggedIn: {
      get value() {
        return mockState.loggedIn
      }
    }
  }),
  useRequestFetch: () => mockFetch,
  navigateTo: (path: string) => mockNavigateTo(path)
}))

;(globalThis as any).$fetch = mockFetch

import middlewareFn from '~/middleware/00.install-check.global'

describe('00.install-check.global middleware', () => {
  beforeEach(() => {
    try {
      useCookie('auth_token').value = null
      useCookie('token').value = null
    } catch (e) {}
    mockState.cookieVal = null
    mockState.loggedIn = false
    mockNavigateTo.mockClear()
    mockFetch.mockReset()
  })

  it('시스템 설치가 완료되지 않은 경우 /install 이 아닌 모든 경로는 /install 로 리다이렉트된다', async () => {
    mockFetch.mockResolvedValueOnce({ isInstalled: false, hasAdminAccount: false })

    const result = await middlewareFn({ path: '/records' }, { path: '/' })
    expect(mockNavigateTo).toHaveBeenCalledWith('/install')
    expect(result).toBe('navigated-to-/install')
  })

  it('시스템 설치가 완료되지 않은 상태에서 /install 접근 시 리다이렉트 없이 진행된다', async () => {
    mockFetch.mockResolvedValueOnce({ isInstalled: false, hasAdminAccount: false })

    const result = await middlewareFn({ path: '/install' }, { path: '/' })
    expect(mockNavigateTo).not.toHaveBeenCalled()
    expect(result).toBeUndefined()
  })

  it('시스템 설치가 완료된 상태에서 로그인된 사용자가 /install 접근 시 홈(/)으로 안전하게 리다이렉트된다 (#96)', async () => {
    mockFetch.mockResolvedValueOnce({ isInstalled: true, hasAdminAccount: true })
    useCookie('auth_token').value = 'valid-jwt-token'

    const result = await middlewareFn({ path: '/install' }, { path: '/' })
    expect(mockNavigateTo).toHaveBeenCalledWith('/')
    expect(result).toBe('navigated-to-/')
  })

  it('시스템 설치가 완료된 상태에서 미인증 사용자가 /install 접근 시 /login 으로 리다이렉트된다', async () => {
    mockFetch.mockResolvedValueOnce({ isInstalled: true, hasAdminAccount: true })
    mockState.cookieVal = null
    mockState.loggedIn = false

    const result = await middlewareFn({ path: '/install' }, { path: '/' })
    expect(mockNavigateTo).toHaveBeenCalledWith('/login')
    expect(result).toBe('navigated-to-/login')
  })

  it('시스템 설치가 완료된 상태에서 일반 업무 페이지(/records) 접근 시 install 미들웨어는 간섭하지 않는다', async () => {
    mockFetch.mockResolvedValueOnce({ isInstalled: true, hasAdminAccount: true })
    mockState.cookieVal = 'valid-jwt-token'
    mockState.loggedIn = true

    const result = await middlewareFn({ path: '/records' }, { path: '/' })
    expect(mockNavigateTo).not.toHaveBeenCalled()
    expect(result).toBeUndefined()
  })
})
