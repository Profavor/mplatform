import { describe, it, expect } from 'vitest'

describe('MatchCandidateReviewNotification - #148 중복 검토 인박스 알림 및 라우팅', () => {

  const formatIdentifier = (rawId: string, prefix = 'CAND'): string => {
    if (!rawId) return '-'
    const clean = rawId.replace(/-/g, '')
    if (clean.length >= 8) {
      return `${prefix}-${clean.substring(0, 8)}`
    }
    return `${prefix}-${rawId}`
  }

  const getHealthBadgeColor = (status: string, successRate: number): string => {
    if (status === 'CRITICAL' || successRate < 80) return 'danger'
    if (status === 'WARNING' || successRate < 95) return 'warning'
    if (status === 'HEALTHY') return 'success'
    return 'secondary'
  }

  const parseReviewRoute = (msgBody: string): { path: string; candidateId: string | null } => {
    const match = msgBody.match(/href='(\/admin\/match-review\?candidateId=([a-zA-Z0-9-]+))'/)
    if (match) {
      return { path: match[1], candidateId: match[2] }
    }
    return { path: '/admin/match-review', candidateId: null }
  }

  it('#148: raw UUID를 CAND-xxxxxxxx 및 REC-xxxxxxxx 형태의 식별 코드로 안전하게 치환한다', () => {
    const rawCandidateId = '76e172c3-41bb-45e0-82be-f5bc8129d38c'
    const rawRecordId = 'ec0b7f89-b5a0-4e17-957f-e15796d45f96'

    const candCode = formatIdentifier(rawCandidateId, 'CAND')
    const recCode = formatIdentifier(rawRecordId, 'REC')

    expect(candCode).toBe('CAND-76e172c3')
    expect(recCode).toBe('REC-ec0b7f89')
    expect(candCode).not.toContain('41bb')
    expect(recCode).not.toContain('b5a0')
  })

  it('#148: 인박스 메시지 본문에서 매칭 검토 경로와 candidateId를 정확히 파싱하여 라우팅할 수 있다', () => {
    const rawCandidateId = 'a3735565-0b1f-4d8a-969d-63dd48ba804e'
    const htmlBody = `
      <p>중복 의심 레코드 후보가 등록되었습니다.</p>
      <p><a href='/admin/match-review?candidateId=${rawCandidateId}'>중복 검토 화면으로 이동</a></p>
    `

    const routeInfo = parseReviewRoute(htmlBody)
    expect(routeInfo.candidateId).toBe(rawCandidateId)
    expect(routeInfo.path).toBe(`/admin/match-review?candidateId=${rawCandidateId}`)
  })

  it('#148: 유사도 점수에 따른 배지 색상이 올바르게 매핑된다', () => {
    expect(getHealthBadgeColor('HEALTHY', 98.5)).toBe('success')
    expect(getHealthBadgeColor('WARNING', 88.0)).toBe('warning')
    expect(getHealthBadgeColor('CRITICAL', 65.0)).toBe('danger')
    expect(getHealthBadgeColor('IDLE', 100.0)).toBe('secondary')
  })
})
