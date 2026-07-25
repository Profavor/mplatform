/**
 * 사용자 개인화 타임존(Timezone) 설정 쿠키 조회 및 GMT 시차 직렬화 오프셋 방어 헬퍼
 */

export function getUserTimezone(): string {
  if (typeof document === 'undefined') return 'Asia/Seoul'
  
  const match = document.cookie.match(new RegExp('(^| )user_timezone=([^;]+)'))
  if (match) {
    return decodeURIComponent(match[2])
  }
  
  try {
    return Intl.DateTimeFormat().resolvedOptions().timeZone || 'Asia/Seoul'
  } catch {
    return 'Asia/Seoul'
  }
}

export function parseDateWithTimezone(dateInput: string | Date | null | undefined): string {
  if (!dateInput) return '-'
  
  try {
    const d = typeof dateInput === 'string' ? new Date(dateInput) : dateInput
    if (isNaN(d.getTime())) return String(dateInput)

    const timeZone = getUserTimezone()
    return new Intl.DateTimeFormat('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false,
      timeZone: timeZone
    }).format(d)
  } catch (err) {
    return String(dateInput)
  }
}
