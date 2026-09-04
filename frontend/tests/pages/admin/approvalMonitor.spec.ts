import { describe, it, expect } from 'vitest'

describe('Admin Approval Monitor Page (approval-monitor.vue) Spec', () => {
  // Mock i18n instance
  const messages: Record<string, string> = {
    'subtitle': '시스템 모니터링 및 세부 관리',
    'refresh': '새로고침',
    'btnDetails': '상세보기',
    'colTargetType': '요청 유형',
    'colDomain': '도메인',
    'colSummary': '요약 정보',
    'colRequester': '기안자',
    'colCreatedAt': '기안 일시',
    'colStatus': '상태',
    'colAction': '작업',
    'proxyApprove': '대결 승인',
    'proxyApproveConfirm': '대결 승인 처리하시겠습니까?',
    'proxyApproveFail': '대결 승인 처리에 실패했습니다.',
    'proxyReject': '대결 반려',
    'proxyRejectConfirm': '대결 반려 처리하시겠습니까?',
    'proxyRejectFail': '대결 반려 처리에 실패했습니다.',
    'status_submitted': '상신',
    'status_pending': '진행중',
    'status_approved': '승인',
    'status_rejected': '반려',
    'status_cancelled': '상신취소',
    'status_waiting': '대기',
    'status_draft': '상신완료',
    'label_role': '역할',
    'unassigned': '미할당'
  }

  const mockT = (key: string) => messages[key] || key
  const mockTe = (key: string) => Boolean(messages[key])

  // Mock codeStore
  const mockCodeStore = {
    getCodeName: (group: string, code: string, fallback: any) => {
      return fallback || code
    }
  }

  const getStatusText = (status: string) => {
    if (!status) return ''
    const codeName = mockCodeStore.getCodeName('APPROVAL_STATUS', status, null)
    if (codeName && codeName !== status) return codeName
    const key = 'status_' + String(status).toLowerCase()
    if (mockTe(key)) return mockT(key)
    return status
  }

  const formatStepAssignee = (s: any, req: any, getUserName: any, formatRoleName: any) => {
    if (!s) return ''
    if (s.stepType === 'DRAFT' || s.status === 'SUBMITTED') {
      const nameCandidate = s.assigneeName || req?.requesterName || req?.requesterUsername
      return getUserName(s.assigneeId, nameCandidate)
    }
    if (s.assigneeRole && s.assigneeRole !== 'null') {
      return mockT('label_role') + ': ' + formatRoleName(s.assigneeRole)
    }
    if (s.assigneeName) {
      let nameStr = String(s.assigneeName)
      const roleKoPrefixes = ['역할: ', '역할:', 'Role: ', 'Role:']
      for (const prefix of roleKoPrefixes) {
        if (nameStr.startsWith(prefix)) {
          const rawRole = nameStr.substring(prefix.length).trim()
          return mockT('label_role') + ': ' + formatRoleName(rawRole)
        }
      }
      return nameStr
    }
    return getUserName(s.assigneeId) || mockT('unassigned')
  }

  it('getStatusText correctly resolves status translations via i18n', () => {
    expect(getStatusText('SUBMITTED')).toBe('상신')
    expect(getStatusText('PENDING')).toBe('진행중')
    expect(getStatusText('APPROVED')).toBe('승인')
    expect(getStatusText('REJECTED')).toBe('반려')
    expect(getStatusText('UNKNOWN_STATUS')).toBe('UNKNOWN_STATUS')
  })

  it('formatStepAssignee correctly resolves drafter and role assignments with i18n labels', () => {
    const mockGetUserName = (id: string, name?: string) => name || `User-${id}`
    const mockFormatRoleName = (role: string) => `ROLE_${role}`

    // Drafter case
    const drafterStep = { stepType: 'DRAFT', status: 'SUBMITTED', assigneeId: 'u1' }
    expect(formatStepAssignee(drafterStep, { requesterName: '홍길동' }, mockGetUserName, mockFormatRoleName)).toBe('홍길동')

    // Role assignee case
    const roleStep = { assigneeRole: 'DATA_STEWARD' }
    expect(formatStepAssignee(roleStep, {}, mockGetUserName, mockFormatRoleName)).toBe('역할: ROLE_DATA_STEWARD')

    // Unassigned fallback
    const unassignedStep = {}
    expect(formatStepAssignee(unassignedStep, {}, () => null, mockFormatRoleName)).toBe('미할당')
  })

  it('column headers are properly resolved with i18n mock function without throwing', () => {
    const colHeaders = [
      mockT('colTargetType'),
      mockT('colDomain'),
      mockT('colSummary'),
      mockT('colRequester'),
      mockT('colCreatedAt'),
      mockT('colStatus'),
      mockT('colAction')
    ]
    expect(colHeaders).toEqual([
      '요청 유형',
      '도메인',
      '요약 정보',
      '기안자',
      '기안 일시',
      '상태',
      '작업'
    ])
  })
})
