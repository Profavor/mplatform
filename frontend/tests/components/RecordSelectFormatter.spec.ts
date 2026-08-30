import { describe, it, expect } from 'vitest'
import { formatOptionLabel } from '../../utils/optionParser'

describe('Record AG-Grid Select Field Formatter Spec', () => {
  const genderOptions = JSON.stringify({
    optionsList: [
      { key: 'MALE', label: { ko: '남성', en: 'Male' }, value: 'MALE' },
      { key: 'FEMALE', label: { ko: '여성', en: 'Female' }, value: 'FEMALE' }
    ]
  })

  const channelOptions = [
    { value: 'WEB', label: { ko: '웹 사이트', en: 'Website' } },
    { value: 'APP', label: { ko: '모바일 앱', en: 'Mobile App' } },
    { value: 'STORE', label: { ko: '오프라인 매장', en: 'Offline Store' } }
  ]

  it('formats SELECT field code to Korean label', () => {
    const formatted = formatOptionLabel(genderOptions, 'MALE', 'ko')
    expect(formatted).toBe('남성')
  })

  it('formats SELECT field code to English label', () => {
    const formatted = formatOptionLabel(genderOptions, 'MALE', 'en')
    expect(formatted).toBe('Male')
  })

  it('formats MULTI_SELECT field array to localized comma-separated labels', () => {
    const formattedKo = formatOptionLabel(channelOptions, ['WEB', 'APP'], 'ko')
    expect(formattedKo).toBe('웹 사이트, 모바일 앱')

    const formattedEn = formatOptionLabel(channelOptions, ['WEB', 'APP'], 'en')
    expect(formattedEn).toBe('Website, Mobile App')
  })

  it('formats stringified array for multi-select field', () => {
    const formatted = formatOptionLabel(channelOptions, '["APP", "STORE"]', 'ko')
    expect(formatted).toBe('모바일 앱, 오프라인 매장')
  })

  it('returns fallback value gracefully when code is unknown', () => {
    const formatted = formatOptionLabel(genderOptions, 'OTHER', 'ko')
    expect(formatted).toBe('OTHER')
  })
})
