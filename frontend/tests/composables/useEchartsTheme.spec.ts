import { describe, it, expect } from 'vitest'
import { getEchartsThemeOptions } from '../../composables/useEchartsTheme'

describe('useEchartsTheme Composable (TDD)', () => {
  it('다크 모드 시 배경색 및 텍스트 색상이 다크 테마 팔레트로 설정되어야 함', () => {
    const theme = getEchartsThemeOptions(true)
    expect(theme.textColor).toBe('#f3f4f6')
    expect(theme.backgroundColor).toBe('transparent')
  })

  it('라이트 모드 시 배경색 및 텍스트 색상이 라이트 테마 팔레트로 설정되어야 함', () => {
    const theme = getEchartsThemeOptions(false)
    expect(theme.textColor).toBe('#1f2937')
    expect(theme.backgroundColor).toBe('transparent')
  })
})
