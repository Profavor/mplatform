import { describe, it, expect } from 'vitest'
import { hasPermission } from '../../composables/usePermission'

describe('usePermission Composable (TDD)', () => {
  describe('hasPermission 권한 체크 검증', () => {
    it('와일드카드(*) 권한 보유 시 항상 true를 반환해야 함', () => {
      expect(hasPermission('node:write', ['*'])).toBe(true)
    })

    it('도메인 와일드카드(node:*) 권한 보유 시 해당 영역 true를 반환해야 함', () => {
      expect(hasPermission('node:write', ['node:*'])).toBe(true)
      expect(hasPermission('domain:read', ['node:*'])).toBe(false)
    })

    it('정확히 매칭되는 권한 보유 시 true를 반환해야 함', () => {
      expect(hasPermission('node:write', ['node:write', 'domain:read'])).toBe(true)
    })

    it('권한 미보유 시 false를 반환해야 함', () => {
      expect(hasPermission('node:write', ['domain:read'])).toBe(false)
    })
  })
})
