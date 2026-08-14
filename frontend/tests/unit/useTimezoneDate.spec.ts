import { describe, it, expect } from 'vitest'
import { parseDate, formatWithTimezone } from '../../composables/useTimezoneDate'

describe('useTimezoneDate composable (TDD)', () => {
  describe('parseDate 방어 파싱 검증', () => {
    it('ISO 8601 UTC 문자열을 정상적으로 Date 객체로 파싱한다', () => {
      const date = parseDate('2026-08-14T00:00:00Z')
      expect(date).toBeInstanceOf(Date)
      expect(date?.getTime()).not.toBeNaN()
    })

    it('공백으로 구분된 LocalDateTime 문자열도 방어적으로 파싱한다', () => {
      const date = parseDate('2026-08-14 15:30:00')
      expect(date).toBeInstanceOf(Date)
      expect(date?.getTime()).not.toBeNaN()
    })

    it('Date 인스턴스가 입력된 경우 유효성을 검사하여 그대로 반환한다', () => {
      const original = new Date(2026, 7, 14, 12, 0, 0)
      const parsed = parseDate(original)
      expect(parsed).toEqual(original)
    })

    it('null, undefined, 빈 문자열 입력 시 null을 반환한다', () => {
      expect(parseDate(null)).toBeNull()
      expect(parseDate(undefined)).toBeNull()
      expect(parseDate('')).toBeNull()
      expect(parseDate('   ')).toBeNull()
    })
  })

  describe('formatWithTimezone 포맷팅 검증', () => {
    it('지정된 타임존(Asia/Seoul)으로 포맷팅을 수행한다', () => {
      const utcString = '2026-08-14T00:00:00Z'
      // UTC 00:00 -> KST (UTC+9) -> 09:00
      const formatted = formatWithTimezone(utcString, 'Asia/Seoul')
      expect(formatted).toContain('2026')
      expect(formatted).toContain('09')
    })

    it('null 입력 시 "-"를 반환한다', () => {
      expect(formatWithTimezone(null)).toBe('-')
    })
  })
})
