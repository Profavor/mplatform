import { describe, it, expect } from 'vitest'

// fetch-interceptor의 핵심 유틸 함수들 독립 테스트

function applyAuthHeader(options: { headers?: any }, token: string): { headers: any } {
  options.headers = options.headers || {}
  if (Array.isArray(options.headers)) {
    options.headers = (options.headers as [string, string][]).filter(([k]) => k.toLowerCase() !== 'authorization')
    options.headers.push(['Authorization', `Bearer ${token}`])
  } else {
    (options.headers as Record<string, string>)['Authorization'] = `Bearer ${token}`
  }
  return options as { headers: any }
}

function shouldRedirectToLogin(status: number, refreshToken: string | null, newToken: string | null): boolean {
  if (status !== 401) return false
  if (!refreshToken) return true
  if (!newToken) return true
  return false
}

function isAuthUrl(url: string): boolean {
  return url.includes('/api/auth/login') || url.includes('/api/auth/refresh')
}

describe('fetch-interceptor 재시도 로직 (TDD)', () => {
  it('Authorization 헤더가 올바르게 주입되어야 함', () => {
    const options = { headers: {} as Record<string, string> }
    applyAuthHeader(options, 'my-token-123')
    expect(options.headers['Authorization']).toBe('Bearer my-token-123')
  })

  it('기존 Authorization 헤더가 배열일 때 교체되어야 함', () => {
    const options = { headers: [['Authorization', 'Bearer old']] as [string, string][] }
    applyAuthHeader(options, 'new-token')
    const found = (options.headers as [string, string][]).find(([k]) => k === 'Authorization')
    expect(found?.[1]).toBe('Bearer new-token')
    const count = (options.headers as [string, string][]).filter(([k]) => k === 'Authorization').length
    expect(count).toBe(1)
  })

  it('refresh_token 없으면 로그인 리다이렉트 해야 함', () => {
    expect(shouldRedirectToLogin(401, null, null)).toBe(true)
  })

  it('새 토큰 발급 실패 시 로그인 리다이렉트 해야 함', () => {
    expect(shouldRedirectToLogin(401, 'refresh-token', null)).toBe(true)
  })

  it('새 토큰 발급 성공 시 로그인 리다이렉트 하지 않아야 함', () => {
    expect(shouldRedirectToLogin(401, 'refresh-token', 'new-access-token')).toBe(false)
  })

  it('401 외 상태코드는 리다이렉트하지 않아야 함', () => {
    expect(shouldRedirectToLogin(403, null, null)).toBe(false)
    expect(shouldRedirectToLogin(500, null, null)).toBe(false)
  })

  it('auth 관련 URL은 인터셉터에서 제외되어야 함', () => {
    expect(isAuthUrl('/api/auth/login')).toBe(true)
    expect(isAuthUrl('/api/auth/refresh')).toBe(true)
    expect(isAuthUrl('/api/records/123')).toBe(false)
    expect(isAuthUrl('/api/domains/abc/axes')).toBe(false)
  })
})
