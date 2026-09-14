import { describe, it, expect } from 'vitest'

describe('Dashboard DQ Severity Normalization', () => {
  const normalizeSeverityData = (rawList: Array<{ severity?: string; count?: number }>) => {
    const dataMap: Record<string, number> = { HIGH: 0, MEDIUM: 0, LOW: 0 }
    ;(rawList || []).forEach(item => {
      if (!item.severity) return
      const rawSev = String(item.severity).toUpperCase().trim()
      const count = Number(item.count) || 0
      if (rawSev === 'CRITICAL' || rawSev === 'ERROR' || rawSev === 'HIGH') {
        dataMap['HIGH'] += count
      } else if (rawSev === 'WARNING' || rawSev === 'WARN' || rawSev === 'MEDIUM') {
        dataMap['MEDIUM'] += count
      } else {
        dataMap['LOW'] += count
      }
    })
    return dataMap
  }

  it('correctly maps WARNING to MEDIUM', () => {
    const raw = [{ severity: 'WARNING', count: 2954 }]
    const result = normalizeSeverityData(raw)
    expect(result.HIGH).toBe(0)
    expect(result.MEDIUM).toBe(2954)
    expect(result.LOW).toBe(0)
  })

  it('correctly aggregates multiple severities including CRITICAL, ERROR, WARNING, and INFO', () => {
    const raw = [
      { severity: 'CRITICAL', count: 10 },
      { severity: 'ERROR', count: 5 },
      { severity: 'WARNING', count: 20 },
      { severity: 'WARN', count: 15 },
      { severity: 'INFO', count: 50 },
      { severity: 'LOW', count: 25 }
    ]
    const result = normalizeSeverityData(raw)
    expect(result.HIGH).toBe(15) // 10 + 5
    expect(result.MEDIUM).toBe(35) // 20 + 15
    expect(result.LOW).toBe(75) // 50 + 25
  })

  it('handles empty or missing input gracefully', () => {
    expect(normalizeSeverityData([])).toEqual({ HIGH: 0, MEDIUM: 0, LOW: 0 })
    expect(normalizeSeverityData([{ severity: '', count: 100 }])).toEqual({ HIGH: 0, MEDIUM: 0, LOW: 0 })
  })
})
