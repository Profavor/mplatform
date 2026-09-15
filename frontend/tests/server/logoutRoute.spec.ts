import { describe, it, expect, vi, beforeEach } from 'vitest'

const { mockDeleteCookie, mockSendRedirect, mockSessionClear, mockGetHeader, mockGetCookie, mockGetQuery } = vi.hoisted(() => ({
  mockDeleteCookie: vi.fn(),
  mockSendRedirect: vi.fn((event, url, code) => ({ redirectedTo: url, statusCode: code })),
  mockSessionClear: vi.fn(async () => {}),
  mockGetHeader: vi.fn((event, name) => name === 'host' ? 'mplatform.local' : undefined),
  mockGetCookie: vi.fn(() => undefined),
  mockGetQuery: vi.fn(() => ({}))
}))

vi.mock('h3', () => ({
  defineEventHandler: (fn: any) => fn,
  deleteCookie: (event: any, name: string, opts?: any) => mockDeleteCookie(event, name, opts),
  sendRedirect: (event: any, url: string, code?: number) => mockSendRedirect(event, url, code),
  getHeader: (event: any, name: string) => mockGetHeader(event, name),
  getCookie: (event: any, name: string) => mockGetCookie(event, name),
  getQuery: (event: any) => mockGetQuery(event),
  useSession: vi.fn(async () => ({
    clear: mockSessionClear
  }))
}))

import logoutHandler from '~/server/routes/auth/logout.get'

describe('Server Route: GET /auth/logout (TDD Unit Test)', () => {
  beforeEach(() => {
    mockDeleteCookie.mockClear()
    mockSendRedirect.mockClear()
    mockSessionClear.mockClear()
    mockGetHeader.mockClear()
    mockGetCookie.mockClear()
    mockGetQuery.mockReset()
    mockGetQuery.mockReturnValue({})
  })

  it('OIDC 세션을 파기하고 모든 인증 쿠키를 삭제한 후 Keycloak 로그아웃 엔드포인트로 302 리다이렉트한다', async () => {
    const mockEvent = {
      node: { req: {}, res: {} }
    }

    const res = await logoutHandler(mockEvent as any)

    // 세션 clear 호출 검증
    expect(mockSessionClear).toHaveBeenCalled()

    // 주요 인증 쿠키 삭제 호출 검증
    const deletedCookieNames = mockDeleteCookie.mock.calls.map(call => call[1])
    expect(deletedCookieNames).toContain('auth_token')
    expect(deletedCookieNames).toContain('refresh_token')
    expect(deletedCookieNames).toContain('user_data')
    expect(deletedCookieNames).toContain('token')
    expect(deletedCookieNames).toContain('id_token')

    // Keycloak OIDC 로그아웃 리다이렉트 검증
    expect(mockSendRedirect).toHaveBeenCalledWith(
      mockEvent,
      expect.stringContaining('/auth/realms/mplatform/protocol/openid-connect/logout?post_logout_redirect_uri='),
      302
    )
    expect(res.statusCode).toBe(302)
    expect(res.redirectedTo).toContain('/auth/realms/mplatform/protocol/openid-connect/logout')
  })

  it('local_only 파라미터가 있는 경우 Keycloak 리다이렉트 없이 /login으로 직접 302 리다이렉트한다', async () => {
    mockGetQuery.mockReturnValue({ local_only: 'true' })
    const mockEvent = {
      node: { req: {}, res: {} }
    }

    const res = await logoutHandler(mockEvent as any)

    expect(mockSendRedirect).toHaveBeenCalledWith(mockEvent, '/login?logout=true', 302)
    expect(res).toEqual({ redirectedTo: '/login?logout=true', statusCode: 302 })
  })
})
