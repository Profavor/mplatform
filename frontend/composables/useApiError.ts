/**
 * API 예외 공통 포맷팅 및 Toast 에러 처리 Composable
 */

export function parseErrorMessage(error: any): string {
  if (!error) return '알 수 없는 오류가 발생했습니다.'

  const status = error.response?.status || error.status
  const backendMsg = error.response?._data?.message || error._data?.message || error.message

  if (status === 401) {
    return '인증이 필요하거나 세션이 만료되었습니다. 다시 로그인해주세요.'
  }
  if (status === 403) {
    return '해당 작업에 대한 접근 권한이 없습니다.'
  }
  if (status === 404) {
    return '요청한 리소스를 찾을 수 없습니다.'
  }
  if (status === 409) {
    return backendMsg
      ? `요청 처리 중 충돌이 발생했습니다: ${backendMsg}`
      : '다른 작업과 요청 처리 중 충돌이 발생했습니다.'
  }
  if (status >= 500) {
    return '서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.'
  }

  if (error?.message && (error.message.includes('Failed to fetch') || error.message.includes('NetworkError') || error.message.includes('<no response>'))) {
    return '네트워크 통신 오류가 발생했거나 서버 응답이 지연되었습니다. 새로고침 후 상태를 확인해주세요.'
  }

  return backendMsg || '요청 처리 중 오류가 발생했습니다.'
}

export function useApiError() {
  const handleError = (error: any, defaultMsg?: string) => {
    const msg = parseErrorMessage(error)
    try {
      const { init } = useToast()
      init({ message: defaultMsg ? `${defaultMsg}: ${msg}` : msg, color: 'danger' })
    } catch {
      console.error('API Error:', msg)
    }
    return msg
  }

  return {
    parseErrorMessage,
    handleError,
  }
}
