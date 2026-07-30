import { getCurrentInstance, hasInjectionContext } from 'vue'
import { useCookie } from '#app'

/**
 * 개인화 타임존(Timezone) 설정 및 ISO-8601 LocalDateTime 파싱 방어 헬퍼 Composable
 */

/**
 * 안전하게 입력값을 Date 객체로 파싱하는 헬퍼 함수
 * ISO 8601, LocalDateTime 오프셋 누락 건, Date 객체 등 다양한 형태를 방어 파싱합니다.
 */
export function parseDate(dateInput: string | Date | null | undefined): Date | null {
  if (dateInput === null || dateInput === undefined || dateInput === '') {
    return null
  }

  if (dateInput instanceof Date) {
    return isNaN(dateInput.getTime()) ? null : dateInput
  }

  if (typeof dateInput === 'string') {
    const trimmed = dateInput.trim()
    if (!trimmed) return null

    // 1차 파싱 시도
    let parsed = new Date(trimmed)
    if (!isNaN(parsed.getTime())) {
      return parsed
    }

    // LocalDateTime '2026-07-25 02:00:00' 포맷 보완 (공백을 'T'로 교체)
    if (trimmed.includes(' ') && !trimmed.includes('T')) {
      parsed = new Date(trimmed.replace(' ', 'T'))
      if (!isNaN(parsed.getTime())) {
        return parsed
      }
    }
  }

  return null
}

/**
 * 지정된 타임존(미지정 시 쿠키의 개인화 타임존 'timezone' 사용, 기본값 Asia/Seoul)으로
 * 날짜를 포맷팅하여 반환합니다.
 */
export function formatWithTimezone(
  dateInput: string | Date | null | undefined,
  targetTimezone?: string,
  options?: Intl.DateTimeFormatOptions
): string {
  const date = parseDate(dateInput)
  if (!date) return '-'

  let timeZone = targetTimezone
  if (!timeZone) {
    const canInject = typeof hasInjectionContext === 'function' ? hasInjectionContext() : !!getCurrentInstance()
    if (canInject) {
      try {
        const cookieTz = useCookie('timezone', { default: () => 'Asia/Seoul' }).value
        timeZone = cookieTz || 'Asia/Seoul'
      } catch {
        timeZone = 'Asia/Seoul'
      }
    } else if (typeof document !== 'undefined') {
      const match = document.cookie.match(/(?:^|; )timezone=([^;]*)/)
      timeZone = match ? decodeURIComponent(match[1]) : 'Asia/Seoul'
    } else {
      timeZone = 'Asia/Seoul'
    }
  }

  try {
    const defaultOptions: Intl.DateTimeFormatOptions = {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false,
      timeZone,
      ...options,
    }
    return new Intl.DateTimeFormat('ko-KR', defaultOptions).format(date)
  } catch {
    // 타임존 파싱 에러 방어
    return date.toLocaleString()
  }
}

export function useTimezoneDate() {
  return {
    parseDate,
    formatWithTimezone,
  }
}
