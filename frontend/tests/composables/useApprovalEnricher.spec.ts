import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useApprovalEnricher } from '../../composables/useApprovalEnricher'

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: (...args: any[]) => mockCustomFetch(...args)
  })
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, fallback?: string) => fallback || key,
    locale: { value: 'ko' }
  })
}))

vi.mock('#app', () => ({
  useCookie: () => ({ value: 'ko' })
}))

vi.mock('~/stores/useUserStore', () => ({
  useUserStore: () => ({
    getUserName: (id: string, name?: string) => name || id
  })
}))

vi.mock('~/stores/useCodeStore', () => ({
  useCodeStore: () => ({
    loadGroup: vi.fn().mockResolvedValue([]),
    getCodeName: (g: string, c: string, fallback: string) => fallback || c
  })
}))

vi.mock('~/composables/useTimezoneDate', () => ({
  formatWithTimezone: (d: any) => d
}))

describe('useApprovalEnricher.ts (TDD - 100% DB Schema Driven)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('식별자 필드가 없거나 자동채번 예정으로 비어있는 경우 ID속성을 임의 유추하지 않고 비워둔다', async () => {
    const { enrichRequest } = useApprovalEnricher()

    mockCustomFetch.mockImplementation(async (url: string) => {
      if (url.includes('/api/nodes/node-1/fields/effective')) {
        return [
          { id: 'f-1', key: 'EP_NAME', name: '이름', isDisplayName: true },
          { id: 'f-2', key: 'EP_NO', name: '사번', isIdentifier: true }
        ]
      }
      return []
    })

    const req = {
      id: 'app-1',
      targetType: 'RECORD_CREATE',
      changes: JSON.stringify({
        nodeId: 'node-1',
        data: {
          EP_NAME: '인치국'
          // EP_NO is missing (auto-generated)
        }
      })
    }

    const enriched = await enrichRequest(req)

    // Name attribute matches schema isDisplayName field
    expect(enriched.nameAttribute).toBe('인치국')
    // ID attribute is empty because EP_NO is missing and no guessing is performed
    expect(enriched.idAttribute).toBe('')
  })

  it('스키마에 정의된 식별자 필드에 값이 존재할 때만 ID속성이 정상 할당된다', async () => {
    const { enrichRequest } = useApprovalEnricher()

    mockCustomFetch.mockImplementation(async (url: string) => {
      if (url.includes('/api/nodes/node-1/fields/effective')) {
        return [
          { id: 'f-1', key: 'EP_NAME', name: '이름', isDisplayName: true },
          { id: 'f-2', key: 'EP_NO', name: '사번', isIdentifier: true }
        ]
      }
      return []
    })

    const req = {
      id: 'app-2',
      targetType: 'RECORD_CREATE',
      changes: JSON.stringify({
        nodeId: 'node-1',
        data: {
          EP_NAME: '인치국',
          EP_NO: '0000001'
        }
      })
    }

    const enriched = await enrichRequest(req)

    expect(enriched.nameAttribute).toBe('인치국')
    expect(enriched.idAttribute).toBe('0000001')
  })
})
