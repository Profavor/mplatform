/**
 * 사용자 화면단 Raw UUID 표출 방지 및 비즈니스 식별 코드 포맷팅 유틸리티
 */

/**
 * UUID 또는 ID를 사용자 친화적인 식별 코드(예: REC-340a0917)로 변환합니다.
 * @param id UUID 문자열 또는 ID
 * @param prefix 식별자 접두사 (기본값: 'ID')
 * @returns 포맷팅된 식별 코드 (예: 'REC-340a0917')
 */
export function formatEntityId(id: string | null | undefined, prefix: string = 'ID'): string {
  if (!id || typeof id !== 'string') {
    return '-'
  }

  const trimmed = id.trim()
  if (!trimmed) {
    return '-'
  }

  // 이미 포맷팅된 식별 코드 형태인 경우 (예: 'REC-1234', 'ORG-001')
  if (trimmed.includes('-') && !isValidUuid(trimmed)) {
    return trimmed
  }

  // UUID에서 앞 8자리 추출하여 Prefix와 결합
  const cleanId = trimmed.replace(/-/g, '')
  const shortId = cleanId.substring(0, 8)
  return `${prefix}-${shortId}`
}

/**
 * 레코드 ID 전용 포맷터 (REC-340a0917)
 */
export function formatRecordCode(id: string | null | undefined): string {
  return formatEntityId(id, 'REC')
}

/**
 * 조직/부서/사용자 ID 전용 포맷터
 */
export function formatOrgCode(id: string | null | undefined): string {
  return formatEntityId(id, 'ORG')
}

export function formatUserCode(id: string | null | undefined): string {
  return formatEntityId(id, 'USR')
}

/**
 * 표준 UUID 형식인지 검증하는 헬퍼 함수
 */
function isValidUuid(str: string): boolean {
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i
  return uuidRegex.test(str)
}
