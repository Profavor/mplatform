import { describe, it, expect } from 'vitest'

export function shouldRecordLoginSession(
  accessToken: string | null | undefined,
  savedTokenKey: string | null | undefined
): { shouldRecord: boolean; tokenKey: string | null } {
  if (!accessToken || typeof accessToken !== 'string' || accessToken.trim().length === 0) {
    return { shouldRecord: false, tokenKey: null }
  }
  const tokenKey = accessToken.slice(-24)
  if (savedTokenKey !== tokenKey) {
    return { shouldRecord: true, tokenKey }
  }
  return { shouldRecord: false, tokenKey }
}

describe('01-oidc-sync Login Log Recording & Session Fallback (TDD)', () => {
  it('신규 로그인 세션에서 새로운 access token이 발급되면 로그인 이력 적재를 수행해야 한다', () => {
    const token = 'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.tokenA'
    const res = shouldRecordLoginSession(token, null)
    expect(res.shouldRecord).toBe(true)
    expect(res.tokenKey).toBe(token.slice(-24))
  })

  it('동일 세션 내에서 토큰이 변경되지 않은 경우(새로고침 등) 중복 적재를 방지해야 한다', () => {
    const token = 'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.tokenA'
    const tokenKey = token.slice(-24)
    const res = shouldRecordLoginSession(token, tokenKey)
    expect(res.shouldRecord).toBe(false)
  })

  it('로그아웃 후 재로그인하여 다른 access token이 발급되면 새롭게 적재를 수행해야 한다', () => {
    const oldTokenKey = 'old_session_token_key_123'
    const newToken = 'eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.tokenB'
    const res = shouldRecordLoginSession(newToken, oldTokenKey)
    expect(res.shouldRecord).toBe(true)
    expect(res.tokenKey).toBe(newToken.slice(-24))
  })

  it('토큰이 없는 비로그인 상태에서는 적재를 수행하지 않아야 한다', () => {
    expect(shouldRecordLoginSession(null, null).shouldRecord).toBe(false)
    expect(shouldRecordLoginSession('', null).shouldRecord).toBe(false)
    expect(shouldRecordLoginSession(undefined, 'saved').shouldRecord).toBe(false)
  })
})
