import { describe, it, expect, vi, beforeEach } from 'vitest'

const { mockDeleteCookie, mockSendRedirect, mockSessionClear } = vi.hoisted(() => ({
  mockDeleteCookie: vi.fn(),
  mockSendRedirect: vi.fn((event, url, code) => ({ redirectedTo: url, statusCode: code })),
  mockSessionClear: vi.fn(async () => {})
}))

vi.mock('h3', () => ({
  defineEventHandler: (fn: any) => fn,
  deleteCookie: (event: any, name: string, opts?: any) => mockDeleteCookie(event, name, opts),
  sendRedirect: (event: any, url: string, code?: number) => mockSendRedirect(event, url, code),
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
  })

  it('OIDC 세션을 파기하고 모든 인증 쿠키를 삭제한 후 /login으로 302 리다이렉트한다', async () => {
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

    // /login 리다이렉트 응답 검증 (404 방지)
    expect(mockSendRedirect).toHaveBeenCalledWith(mockEvent, '/login', 302)
    expect(res).toEqual({ redirectedTo: '/login', statusCode: 302 })
  })
})
