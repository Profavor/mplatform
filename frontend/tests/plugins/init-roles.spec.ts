import { describe, it, expect } from 'vitest'

export function shouldFetchRolesOnMount(token: string | null | undefined): boolean {
  return !!token && token.trim().length > 0
}

describe('init-roles Client Plugin (TDD)', () => {
  it('토큰이 없는 로그인 이전 상태에서는 /api/roles 호출을 수행하지 않아야 함 (401 방지)', () => {
    expect(shouldFetchRolesOnMount(null)).toBe(false)
    expect(shouldFetchRolesOnMount(undefined)).toBe(false)
    expect(shouldFetchRolesOnMount('')).toBe(false)
  })

  it('유효한 로그인 토큰이 존재하는 경우 /api/roles 호출을 허용해야 함', () => {
    expect(shouldFetchRolesOnMount('valid-auth-token-123')).toBe(true)
  })
})
