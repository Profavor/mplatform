import { describe, it, expect } from 'vitest'
import { parseDate, formatWithTimezone } from '../../composables/useTimezoneDate'

describe('useTimezoneDate Composable (TDD)', () => {
  describe('parseDate 방어 헬퍼 함수 검증', () => {
    it('null, undefined, 빈 문자열 전달 시 null을 반환해야 함', () => {
      expect(parseDate(null)).toBeNull()
      expect(parseDate(undefined)).toBeNull()
      expect(parseDate('')).toBeNull()
    })

    it('Date 객체를 전달하면 유효한 Date 객체를 반환해야 함', () => {
      const now = new Date()
      const parsed = parseDate(now)
      expect(parsed).toBeInstanceOf(Date)
      expect(parsed?.getTime()).toBe(now.getTime())
    })

    it('ISO 8601 표준 문자열을 정확히 파싱해야 함', () => {
      const isoStr = '2026-07-25T02:00:00Z'
      const parsed = parseDate(isoStr)
      expect(parsed).toBeInstanceOf(Date)
      expect(parsed?.toISOString()).toBe('2026-07-25T02:00:00.000Z')
    })

    it('LocalDateTime 오프셋 오간 포맷(예: 2026-07-25T02:00:00)을 안전하게 파싱해야 함', () => {
      const localStr = '2026-07-25T02:00:00'
      const parsed = parseDate(localStr)
      expect(parsed).toBeInstanceOf(Date)
      expect(parsed?.getFullYear()).toBe(2026)
      expect(parsed?.getMonth()).toBe(6) // 7월 (0-indexed)
      expect(parsed?.getDate()).toBe(25)
    })

    it('잘못된 날짜 문자열 전달 시 null을 반환하여 런타임 에러를 방지해야 함', () => {
      expect(parseDate('invalid-date-string')).toBeNull()
    })
  })

  describe('formatWithTimezone 타임존 포맷팅 검증', () => {
    it('주어진 타임존(Asia/Seoul vs UTC)에 맞춰 시각이 변환되어 표시되어야 함', () => {
      const targetDate = '2026-07-25T00:00:00Z' // UTC 기준 00:00
      
      const formattedUtc = formatWithTimezone(targetDate, 'UTC')
      const formattedKst = formatWithTimezone(targetDate, 'Asia/Seoul')

      expect(formattedUtc).toContain('2026')
      expect(formattedKst).toContain('2026')
      // KST(UTC+9)는 09:00이 포함되어야 함
      expect(formattedKst).toMatch(/09|9/)
    })

    it('날짜가 null/잘못된 경우 기본 대시(-) 또는 빈 문자열을 반환해야 함', () => {
      expect(formatWithTimezone(null, 'Asia/Seoul')).toBe('-')
      expect(formatWithTimezone('invalid', 'Asia/Seoul')).toBe('-')
    })
  })
})
