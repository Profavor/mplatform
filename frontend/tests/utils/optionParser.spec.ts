import { describe, it, expect } from 'vitest'
import { parseOptions } from '../../utils/optionParser'

describe('optionParser', () => {
  it('should parse DB optionsList JSON object correctly', () => {
    const dbOptions = {
      optionsList: [
        { key: 'ACTIVE', label: { en: 'Active', ko: '정상' }, value: 'ACTIVE' },
        { key: 'DORMANT', label: { en: 'Dormant', ko: '휴면' }, value: 'DORMANT' },
        { key: 'BLOCKED', label: { en: 'Blocked', ko: '거래정지' }, value: 'BLOCKED' },
        { key: 'TERMINATED', label: { en: 'Terminated', ko: '해지' }, value: 'TERMINATED' }
      ]
    }

    const parsedKo = parseOptions(dbOptions, 'ko')
    expect(parsedKo).toHaveLength(4)
    expect(parsedKo[0]).toEqual({ text: '정상', value: 'ACTIVE', order: 0 })
    expect(parsedKo[1]).toEqual({ text: '휴면', value: 'DORMANT', order: 0 })
    expect(parsedKo[2]).toEqual({ text: '거래정지', value: 'BLOCKED', order: 0 })
    expect(parsedKo[3]).toEqual({ text: '해지', value: 'TERMINATED', order: 0 })

    const parsedEn = parseOptions(dbOptions, 'en')
    expect(parsedEn[0]).toEqual({ text: 'Active', value: 'ACTIVE', order: 0 })
  })

  it('should parse stringified JSON optionsList object', () => {
    const stringified = JSON.stringify({
      optionsList: [
        { key: 'VIP', label: { en: 'VIP', ko: 'VIP' }, value: 'VIP' },
        { key: 'GOLD', label: { en: 'Gold', ko: '골드' }, value: 'GOLD' }
      ]
    })

    const parsed = parseOptions(stringified, 'ko')
    expect(parsed).toHaveLength(2)
    expect(parsed[0]).toEqual({ text: 'VIP', value: 'VIP', order: 0 })
    expect(parsed[1]).toEqual({ text: '골드', value: 'GOLD', order: 0 })
  })

  it('should parse comma-separated string', () => {
    const raw = 'VIP, GOLD, SILVER'
    const parsed = parseOptions(raw, 'ko')
    expect(parsed).toHaveLength(3)
    expect(parsed[0]).toEqual({ text: 'VIP', value: 'VIP', order: 0 })
    expect(parsed[1]).toEqual({ text: 'GOLD', value: 'GOLD', order: 0 })
    expect(parsed[2]).toEqual({ text: 'SILVER', value: 'SILVER', order: 0 })
  })

  it('should return empty array for null/empty options', () => {
    expect(parseOptions(null)).toEqual([])
    expect(parseOptions('')).toEqual([])
    expect(parseOptions({})).toEqual([])
  })
})
