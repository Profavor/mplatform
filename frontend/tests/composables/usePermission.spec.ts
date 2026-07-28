import { describe, it, expect } from 'vitest'
import { hasPermission } from '../../composables/usePermission'

describe('usePermission Composable (TDD)', () => {
  describe('hasPermission 권한 체크 검증', () => {
    it('앞자리가 *인 전역 와일드카드(*, *:*, *:write) 보유 시 항상 true를 반환해야 함', () => {
      expect(hasPermission('domain:write', ['*'])).toBe(true)
      expect(hasPermission('domain:write', ['*:*'])).toBe(true)
      expect(hasPermission('domain:write', ['*:write'])).toBe(true)
    })

    it('뒷자리가 *인 도메인 와일드카드(domain:*) 보유 시 해당 영역의 모든 액션에 true를 반환해야 함', () => {
      expect(hasPermission('domain:write', ['domain:*'])).toBe(true)
      expect(hasPermission('domain:read', ['domain:*'])).toBe(true)
      expect(hasPermission('node:write', ['domain:*'])).toBe(false)
    })

    it('정확히 매칭되는 권한 보유 시 true를 반환해야 함', () => {
      expect(hasPermission('node:write', ['node:write', 'domain:read'])).toBe(true)
    })

    it('DB에서 와일드카드(*) 권한을 전달받은 경우 모든 권한 검증에 true를 반환해야 함', () => {
      expect(hasPermission('domain:write', ['*'])).toBe(true)
      expect(hasPermission('org:write', ['*'])).toBe(true)
    })

    it('권한 미보유 시 false를 반환해야 함', () => {
      expect(hasPermission('node:write', ['domain:read'])).toBe(false)
    })
  })
})
