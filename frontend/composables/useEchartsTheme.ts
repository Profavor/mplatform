/**
 * ECharts 다크/라이트 테마 색상 및 옵션 Composable
 */

export interface EchartsThemeOptions {
  isDark: boolean
  textColor: string
  borderColor: string
  backgroundColor: string
  colorPalette: string[]
}

export function getEchartsThemeOptions(isDark: boolean): EchartsThemeOptions {
  return {
    isDark,
    textColor: isDark ? '#f3f4f6' : '#1f2937',
    borderColor: isDark ? '#374151' : '#e5e7eb',
    backgroundColor: 'transparent',
    colorPalette: isDark
      ? ['#60a5fa', '#34d399', '#f87171', '#fbbf24', '#a78bfa']
      : ['#2563eb', '#10b981', '#ef4444', '#f59e0b', '#8b5cf6'],
  }
}

export function useEchartsTheme() {
  const isDark = ref(false)

  try {
    const colors = useColors()
    isDark.value = colors.currentPresetName.value === 'dark'
  } catch {
    isDark.value = false
  }

  const themeOptions = computed(() => getEchartsThemeOptions(isDark.value))

  return {
    isDark,
    themeOptions,
    getEchartsThemeOptions,
  }
}
