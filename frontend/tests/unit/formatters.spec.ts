import { describe, it, expect } from 'vitest'
import { formatEntityId, formatRecordCode } from '../../utils/formatters'

describe('formatters utility (TDD)', () => {
  describe('formatEntityId', () => {
    it('UUID를 접두사(Prefix)와 함께 8자리 축약 식별 코드로 변환한다', () => {
      const uuid = '340a0917-af0b-4d13-a1ce-479d4b2e2ca7'
      const formatted = formatEntityId(uuid, 'REC')
      expect(formatted).toBe('REC-340a0917')
    })

    it('접두사가 없는 경우 ID- 접두사를 기본값으로 사용한다', () => {
      const uuid = '340a0917-af0b-4d13-a1ce-479d4b2e2ca7'
      const formatted = formatEntityId(uuid)
      expect(formatted).toBe('ID-340a0917')
    })

    it('이미 접두사가 붙어있는 식별 코드인 경우 원본을 유지한다', () => {
      expect(formatEntityId('REC-340a0917', 'REC')).toBe('REC-340a0917')
      expect(formatEntityId('ORG-001', 'ORG')).toBe('ORG-001')
    })

    it('null 또는 undefined 입력 시 "-"를 반환한다', () => {
      expect(formatEntityId(null)).toBe('-')
      expect(formatEntityId(undefined)).toBe('-')
      expect(formatEntityId('')).toBe('-')
    })
  })

  describe('formatRecordCode', () => {
    it('레코드 UUID를 REC- 식별 코드로 변환한다', () => {
      const uuid = 'e1a2b3c4-1234-5678-90ab-cdef12345678'
      expect(formatRecordCode(uuid)).toBe('REC-e1a2b3c4')
    })
  })
})
