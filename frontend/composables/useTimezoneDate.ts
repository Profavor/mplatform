import { getCurrentInstance, hasInjectionContext } from 'vue'
import { useCookie } from '#app'

/**
 * 개인화 타임존(Timezone) 설정 및 ISO-8601 LocalDateTime 파싱 방어 헬퍼 Composable
 */

function getServerOffset(): string {
  const canInject = typeof hasInjectionContext === 'function' ? hasInjectionContext() : !!getCurrentInstance()
  if (canInject) {
    try {
      const cookieOffset = useCookie('server_offset', { default: () => '+09:00' }).value
      return cookieOffset || '+09:00'
    } catch {
      return '+09:00'
    }
  } else if (typeof document !== 'undefined') {
    const match = document.cookie.match(/(?:^|; )server_offset=([^;]*)/)
    return match ? decodeURIComponent(match[1]) : '+09:00'
  }
  return '+09:00'
}

/**
 * 안전하게 입력값을 Date 객체로 파싱하는 헬퍼 함수
 * ISO 8601, LocalDateTime 오프셋 누락 건, Date 객체 등 다양한 형태를 방어 파싱합니다.
 */
export function parseDate(dateInput: string | number | Date | null | undefined): Date | null {
  if (dateInput === null || dateInput === undefined || dateInput === '') {
    return null
  }

  if (dateInput instanceof Date) {
    return isNaN(dateInput.getTime()) ? null : dateInput
  }

  if (typeof dateInput === 'number') {
    const d = new Date(dateInput)
    return isNaN(d.getTime()) ? null : d
  }

  if (typeof dateInput === 'string') {
    let trimmed = dateInput.trim()
    if (!trimmed) return null

    // Epoch 밀리초 타임스탬프 (예: '1789345885560')
    if (/^\d{10,13}$/.test(trimmed)) {
      const d = new Date(parseInt(trimmed, 10))
      return isNaN(d.getTime()) ? null : d
    }

    // LocalDateTime '2026-07-25 02:00:00' 포맷 보완 (공백을 'T'로 교체)
    if (trimmed.includes(' ') && !trimmed.includes('T')) {
      trimmed = trimmed.replace(' ', 'T')
    }

    // ISO-8601 날짜+시간 형식인데 타임존(Z 또는 +/-HH:mm)이 없는 경우:
    // 백엔드 DB/서버 오프셋(+09:00)을 명시적으로 부여하여 클라이언트 브라우저 로컬 타임존 편차 방지
    if (/\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/.test(trimmed)) {
      if (!trimmed.endsWith('Z') && !trimmed.endsWith('z') && !/[-+]\d{2}(?::?\d{2})?$/.test(trimmed)) {
        const offset = getServerOffset()
        trimmed += offset
      }
    }

    const parsed = new Date(trimmed)
    if (!isNaN(parsed.getTime())) {
      return parsed
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
    formatDate: formatWithTimezone,
  }
}
