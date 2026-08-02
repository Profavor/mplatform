import { describe, it, expect } from 'vitest'

describe('Schema Impact Analysis Helper', () => {
  it('Impact Analysis 위험도 등급(Risk Level)에 맞는 뱃지 색상을 반환한다', () => {
    const getRiskColor = (level: string) => {
      switch (level) {
        case 'CRITICAL': return 'danger'
        case 'HIGH': return 'warning'
        case 'MEDIUM': return 'info'
        default: return 'success'
      }
    }

    expect(getRiskColor('CRITICAL')).toBe('danger')
    expect(getRiskColor('HIGH')).toBe('warning')
    expect(getRiskColor('LOW')).toBe('success')
  })
})
