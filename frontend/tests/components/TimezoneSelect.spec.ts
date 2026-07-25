import { describe, it, expect } from 'vitest'
import { defaultTimezoneOptions } from '../../components/TimezoneSelect.vue'

describe('TimezoneSelect Component (TDD)', () => {
  it('기본 타임존 옵션 리스트에 Asia/Seoul 및 UTC가 포함되어야 함', () => {
    expect(defaultTimezoneOptions).toBeDefined()
    expect(Array.isArray(defaultTimezoneOptions)).toBe(true)

    const seoul = defaultTimezoneOptions.find(opt => opt.value === 'Asia/Seoul')
    const utc = defaultTimezoneOptions.find(opt => opt.value === 'UTC')

    expect(seoul).toBeDefined()
    expect(seoul?.label).toContain('Asia/Seoul')

    expect(utc).toBeDefined()
    expect(utc?.label).toContain('UTC')
  })
})
