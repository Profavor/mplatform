import { describe, it, expect } from 'vitest'
import { getUserTimezone, parseDateWithTimezone } from '../utils/timezone'

describe('timezone utils', () => {
  it('getUserTimezone - 기본 타임존 반환', () => {
    const tz = getUserTimezone()
    expect(tz).toBeDefined()
    expect(typeof tz).toBe('string')
  })

  it('parseDateWithTimezone - 유효한 날짜 입력 시 지정 포맷으로 변환', () => {
    const dateStr = '2026-07-25T18:30:00.000Z'
    const formatted = parseDateWithTimezone(dateStr)
    expect(formatted).toBeDefined()
    expect(formatted).not.toBe('-')
  })

  it('parseDateWithTimezone - null 입력 시 기본 하이픈 반환', () => {
    expect(parseDateWithTimezone(null)).toBe('-')
  })
})
