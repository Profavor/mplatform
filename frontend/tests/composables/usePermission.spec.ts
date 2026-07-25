import { describe, it, expect } from 'vitest'
import { hasPermission, isAdmin } from '../../composables/usePermission'

describe('usePermission Composable (TDD)', () => {
  describe('isAdmin 판별 검증', () => {
    it('role이 ROLE_ADMIN 또는 ADMIN이면 true를 반환해야 함', () => {
      expect(isAdmin('ROLE_ADMIN')).toBe(true)
      expect(isAdmin('ADMIN')).toBe(true)
      expect(isAdmin('ROLE_ADMIN,ROLE_USER')).toBe(true)
      expect(isAdmin(['ROLE_USER', 'ROLE_ADMIN'])).toBe(true)
    })

    it('일반 사용자 role이면 false를 반환해야 함', () => {
      expect(isAdmin('ROLE_USER')).toBe(false)
      expect(isAdmin(undefined)).toBe(false)
    })
  })


  describe('hasPermission 권한 체크 검증', () => {
    it('ROLE_ADMIN 권한 보유 시 항상 true를 반환해야 함', () => {
      expect(hasPermission('node:write', [], 'ROLE_ADMIN')).toBe(true)
    })

    it('와일드카드(*) 권한 보유 시 항상 true를 반환해야 함', () => {
      expect(hasPermission('node:write', ['*'], 'ROLE_USER')).toBe(true)
    })

    it('도메인 와일드카드(node:*) 권한 보유 시 해당 영역 true를 반환해야 함', () => {
      expect(hasPermission('node:write', ['node:*'], 'ROLE_USER')).toBe(true)
      expect(hasPermission('domain:read', ['node:*'], 'ROLE_USER')).toBe(false)
    })

    it('정확히 매칭되는 권한 보유 시 true를 반환해야 함', () => {
      expect(hasPermission('node:write', ['node:write', 'domain:read'], 'ROLE_USER')).toBe(true)
    })

    it('권한 미보유 시 false를 반환해야 함', () => {
      expect(hasPermission('node:write', ['domain:read'], 'ROLE_USER')).toBe(false)
    })
  })
})
