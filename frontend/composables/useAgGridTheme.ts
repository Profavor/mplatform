import { computed } from 'vue'
import 'ag-grid-enterprise'
import { themeQuartz, colorSchemeDark } from 'ag-grid-community'
import { useColors } from 'vuestic-ui'

import { useCookie } from '#app'

/**
 * AG Grid 테마 및 공통 옵션을 중앙에서 관리하는 composable.
 * 
 * 테마나 공통 동작을 변경하고 싶으면 이 파일 하나만 수정하면 됩니다.
 * Vuestic UI의 다크모드 프리셋에 연동하여 자동으로 라이트/다크 테마를 전환합니다.
 *
 * 사용법:
 *   const { gridTheme, autoSizeStrategy } = useAgGridTheme()
 *   <ag-grid-vue :theme="gridTheme" :autoSizeStrategy="autoSizeStrategy" ... />
 */
export function useAgGridTheme() {
  const colors = useColors()
  const currentPresetName = colors?.currentPresetName

  const isDark = computed(() => currentPresetName?.value === 'dark')
  const savedFontSize = useCookie('fontSize', { default: () => '14px' })

  const gridTheme = computed(() => {
    const sizeStr = savedFontSize.value || '14px'
    const numericSize = parseInt(sizeStr.replace('px', '')) || 14
    
    const baseTheme = themeQuartz.withParams({
      fontSize: numericSize
    })

    return isDark.value
      ? baseTheme.withPart(colorSchemeDark)
      : baseTheme
  })

  /** 자동 리사이즈 끄기: 명시적 컬럼 너비(width)와 flex 설정을 존중하고 가로 스크롤을 활성화하기 위해 */
  const autoSizeStrategy = null

  return { gridTheme, autoSizeStrategy, isDark }
}
