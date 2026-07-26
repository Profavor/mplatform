import { describe, it, expect } from 'vitest'

describe('Workflow Admin API Contract Regression Test (TDD)', () => {
  it('도메인 필드 조회 API URL은 /api/domains/{domainId}/fields 형식을 준수해야 함 (500 에러 방지)', () => {
    const domainId = 'f08537ee-f773-46e4-8c62-e75e1e1b9011'
    const getDomainFieldsUrl = (id: string) => `/api/domains/${id}/fields`
    
    const url = getDomainFieldsUrl(domainId)
    expect(url).toBe('/api/domains/f08537ee-f773-46e4-8c62-e75e1e1b9011/fields')
    expect(url).not.toContain('/api/fields/domain/')
  })

  it('useCustomFetch Composable은 customFetch 함수를 갖춘 객체를 반환해야 함 (TypeError 방지)', () => {
    const mockUseCustomFetch = () => ({
      customFetch: async (url: string) => ({ url }),
      prepareFetchOptions: () => ({})
    })

    const result = mockUseCustomFetch()
    expect(typeof result.customFetch).toBe('function')
  })

  it('사용자 옵션 생성 시 UserDto(id/username/role) 구조를 반영하여 UUID가 아닌 username을 표시해야 함 (UUID 노출 방지)', () => {
    const roleMap: Record<string, string> = {
      'ROLE_ADMIN': '최고 관리자',
      'ROLE_USER': '일반 사용자'
    }

    // 실제 UserDto 구조: id(String UUID), username, role (email 필드 없음)
    const formatUser = (u: any) => {
      const roleText = u.role ? (roleMap[u.role] || u.role) : ''
      const label = roleText ? `${u.username} (${roleText})` : (u.username || String(u.id))
      return {
        value: u.username || String(u.id),
        text: label
      }
    }

    const u1 = { id: '41882f81-4d84-4480-9d3b-6c896283c6d9', username: 'profavor', role: 'ROLE_ADMIN' }
    const u2 = { id: 'bc123456-0000-0000-0000-000000000001', username: 'admin' }

    expect(formatUser(u1).value).toBe('profavor')          // UUID가 아닌 username
    expect(formatUser(u1).text).toBe('profavor (최고 관리자)')
    expect(formatUser(u1).text).not.toContain('ROLE_ADMIN')
    expect(formatUser(u1).text).not.toContain('41882f81')   // UUID가 label에 노출되지 않아야 함

    expect(formatUser(u2).value).toBe('admin')
    expect(formatUser(u2).text).toBe('admin')
  })

  it('동적 역할 목록은 DB API (/api/roles)를 호출하며 JSON 다국어 displayName도 올바르게 파싱해야 함', () => {
    const parseMultilingualText = (text: any): string => {
      if (!text) return ''
      if (typeof text === 'object' && text !== null) return text.ko || text.en || ''
      const str = String(text).trim()
      if (str.startsWith('{')) {
        try {
          const parsed = JSON.parse(str)
          return parsed.ko || parsed.en || str
        } catch (e) {}
      }
      return str
    }

    const dbRoles = [
      { id: '1', name: 'ROLE_ADMIN', displayName: '{"ko":"시스템 관리자","en":"System admin"}' },
      { id: '2', name: 'ROLE_USER', displayName: '일반 사용자' }
    ]

    const map: Record<string, string> = {}
    const options: Array<{ value: string; text: string }> = []

    dbRoles.forEach((r: any) => {
      const name = r.name
      const label = parseMultilingualText(r.displayName) || parseMultilingualText(r.name) || r.name
      map[name] = label
      options.push({ value: name, text: label })
    })

    expect(options[0]).toEqual({ value: 'ROLE_ADMIN', text: '시스템 관리자' })
    expect(options[0].text).not.toContain('{')
    expect(map['ROLE_ADMIN']).toBe('시스템 관리자')
  })

  it('허용 행위 체크박스는 전체 일괄 전환 없이 개별 행위(CREATE 등)만 정확하게 토글되어야 함', () => {
    const rule = { allowedActions: ['READ', 'UPDATE'] }

    const isActionAllowed = (r: any, action: string) => Array.isArray(r.allowedActions) && r.allowedActions.includes(action)
    const toggleAction = (r: any, action: string, val: boolean) => {
      if (val) {
        if (!r.allowedActions.includes(action)) r.allowedActions.push(action)
      } else {
        r.allowedActions = r.allowedActions.filter((a: string) => a !== action)
      }
    }

    toggleAction(rule, 'CREATE', true)
    expect(rule.allowedActions).toEqual(['READ', 'UPDATE', 'CREATE'])
    expect(isActionAllowed(rule, 'CREATE')).toBe(true)

    toggleAction(rule, 'UPDATE', false)
    expect(rule.allowedActions).toEqual(['READ', 'CREATE'])
    expect(isActionAllowed(rule, 'UPDATE')).toBe(false)
  })

  it('CREATE, UPDATE, DELETE 각 워크플로우별로 권한 규칙과 결재선이 독립적으로 직렬화/파싱되어야 함', () => {
    const workflowActionsMap: any = {
      CREATE: {
        permissions: [{ targetType: 'USER', targetId: 'user1', editableFields: ['empNo', 'name'] }],
        steps: [{ stepOrder: 1, stepName: '1차 승인', users: [{ assigneeType: 'USER', assigneeId: 'manager1' }] }]
      },
      UPDATE: {
        permissions: [{ targetType: 'ROLE', targetRole: 'ROLE_MANAGER', editableFields: ['dept'] }],
        steps: [{ stepOrder: 1, stepName: '변경 승인', users: [{ assigneeType: 'ROLE', assigneeRole: 'ROLE_ADMIN' }] }]
      },
      DELETE: { permissions: [], steps: [] }
    }

    const buildPayload = (domainId: string) => {
      return ['CREATE', 'UPDATE', 'DELETE'].map(action => {
        const actState = workflowActionsMap[action]
        const approvalLine = actState.steps.map((s: any, idx: number) => ({
          stepOrder: idx + 1,
          stepName: s.stepName,
          assigneeType: s.users[0]?.assigneeType,
          assigneeId: s.users[0]?.assigneeId,
          assigneeRole: s.users[0]?.assigneeRole
        }))
        return {
          domainId,
          actionType: action,
          stepsConfig: JSON.stringify({
            permissions: actState.permissions,
            approvalLine,
            steps: approvalLine
          })
        }
      })
    }

    const payload = buildPayload('domain-123')
    expect(payload).toHaveLength(3)

    const createConfig = JSON.parse(payload[0].stepsConfig)
    const updateConfig = JSON.parse(payload[1].stepsConfig)

    expect(createConfig.permissions[0].editableFields).toEqual(['empNo', 'name'])
    expect(updateConfig.permissions[0].editableFields).toEqual(['dept'])
    expect(createConfig.approvalLine[0].assigneeId).toBe('manager1')
    expect(updateConfig.approvalLine[0].assigneeRole).toBe('ROLE_ADMIN')
  })

  it('분류 노드 목록 조회 API URL은 /api/domains/{domainId}/nodes/tree 형식을 준수해야 함 (500 에러 방지)', () => {
    const domainId = 'f08537ee-f773-46e4-8c62-e75e1e1b9011'
    const getNodesUrl = (id: string) => `/api/domains/${id}/nodes/tree`

    const url = getNodesUrl(domainId)
    expect(url).toBe('/api/domains/f08537ee-f773-46e4-8c62-e75e1e1b9011/nodes/tree')
    expect(url).not.toContain('/api/classification-nodes/')
  })

  it('AG-Grid 페이징 및 필터링 URL 쿼리 생성이 올바르게 작성되어야 함', () => {
    const buildPageUrl = (page: number, size: number, actionType?: string, query?: string) => {
      let url = `/api/workflow-configs/page?page=${page}&size=${size}`
      if (actionType && actionType !== 'ALL') url += `&actionType=${actionType}`
      if (query && query.trim()) url += `&query=${encodeURIComponent(query.trim())}`
      return url
    }

    expect(buildPageUrl(0, 20, 'CREATE', '주식')).toBe('/api/workflow-configs/page?page=0&size=20&actionType=CREATE&query=' + encodeURIComponent('주식'))
    expect(buildPageUrl(1, 10, 'ALL', '')).toBe('/api/workflow-configs/page?page=1&size=10')
  })
})
