import { getCurrentInstance, hasInjectionContext } from 'vue'
import { useCookie } from '#app'

const getLocaleFromCookie = (): string => {
  const canInject = typeof hasInjectionContext === 'function' ? hasInjectionContext() : !!getCurrentInstance()
  if (canInject) {
    try {
      const c = useCookie('locale', { default: () => 'ko' })
      if (c && c.value) return c.value
    } catch (e) {}
  }
  if (typeof document !== 'undefined') {
    const match = document.cookie.match(/(?:^|; )locale=([^;]*)/)
    if (match) return decodeURIComponent(match[1])
  }
  return 'ko'
}

export const formatMultilingual = (val: any, targetLocale?: string): string => {
  if (val === null || val === undefined) return ''
  const locale = targetLocale || getLocaleFromCookie()

  // Object 타입인 경우
  if (typeof val === 'object') {
    if (val[locale]) return String(val[locale])
    if (val.ko) return String(val.ko)
    if (val.en) return String(val.en)
    const keys = Object.keys(val)
    if (keys.length > 0 && val[keys[0]] !== undefined) return String(val[keys[0]])
    return JSON.stringify(val)
  }

  // String 타입인 경우 JSON 형태 감지
  if (typeof val === 'string') {
    const trimmed = val.trim()
    if ((trimmed.startsWith('{') && trimmed.endsWith('}')) || (trimmed.startsWith('[') && trimmed.endsWith(']'))) {
      try {
        const parsed = JSON.parse(trimmed)
        if (typeof parsed === 'object' && parsed !== null && !Array.isArray(parsed)) {
          if (parsed[locale]) return String(parsed[locale])
          if (parsed.ko) return String(parsed.ko)
          if (parsed.en) return String(parsed.en)
          const keys = Object.keys(parsed)
          if (keys.length > 0 && parsed[keys[0]] !== undefined) return String(parsed[keys[0]])
        }
      } catch (e) {}
    }
    return trimmed
  }

  return String(val)
}

export const useMultilingual = () => {
  return {
    formatMultilingual
  }
}
