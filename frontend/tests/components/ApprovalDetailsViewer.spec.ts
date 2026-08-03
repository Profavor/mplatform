import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ApprovalDetailsViewer from '~/components/ApprovalDetailsViewer.vue'

vi.mock('#app', () => ({
  useCookie: () => ({ value: 'ko' })
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    locale: { value: 'ko' }
  })
}))

// Mock global $fetch for component unit tests
// @ts-ignore
globalThis.$fetch = vi.fn().mockResolvedValue([])

describe('ApprovalDetailsViewer Component - RECORD_UPDATE Filtering Test', () => {
  it('RECORD_UPDATE 시 변경되지 않은 영문 이름은 제외하고 새 파일 추가 항목만 표출되어야 함', () => {
    const mockRequest = {
      id: 'req-1',
      targetType: 'RECORD_UPDATE',
      changes: JSON.stringify({
        before: {
          file: null,
          name: { ko: '인유하', en: 'Test233' }
        },
        after: {
          file: '["/api/files/download/test.xlsx?name=%EB%A0%EC%BD%94%EB%93%9C%EB%B9%84%EA%B5%9F.xlsx"]',
          name: { ko: '인유하', en: 'Test233' }
        }
      }),
      steps: [
        { stepOrder: 0, stepType: 'DRAFT', assigneeName: 'profavor.manager', status: 'APPROVED' },
        { stepOrder: 1, stepType: 'APPROVAL', assigneeName: 'profavor', status: 'PENDING' }
      ]
    }

    const wrapper = mount(ApprovalDetailsViewer, {
      props: {
        request: mockRequest
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          VaIcon: true,
          VaBadge: true,
          VaButton: true,
          VaChip: true,
          ApprovalSteps: true
        }
      }
    })

    const text = wrapper.text()
    // 1. 새 파일 항목(file)은 포함되어야 함
    expect(text).toContain('test.xlsx')

    // 2. 변경되지 않은 영문 이름(Test233)은 변경 항목(수정됨)에 표출되지 않아야 함
    expect(text).not.toContain('Test233')
  })

  it('SCHEMA_FIELD_UPDATE 시 스키마 전용 카드 UI와 필드 속성 비교표가 표출되어야 함', () => {
    const mockSchemaRequest = {
      id: 'req-schema-1',
      targetType: 'SCHEMA_FIELD_UPDATE',
      targetId: 'domain-uuid-123',
      changes: JSON.stringify({
        fieldId: 'field-uuid-456',
        request: {
          name: { ko: '입사일', en: 'Join Date' },
          key: 'JOIN_DATE',
          type: 'DATE',
          required: true,
          order: 4
        },
        before: {
          name: { ko: '입사일', en: 'Join Date' },
          key: 'JOIN_DATE',
          type: 'DATE',
          required: false,
          order: 4
        }
      }),
      steps: [
        { stepOrder: 1, stepType: 'APPROVAL', assigneeRole: 'DOMAIN_EDITOR', status: 'PENDING' }
      ]
    }

    const wrapper = mount(ApprovalDetailsViewer, {
      props: {
        request: mockSchemaRequest
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          VaIcon: true,
          VaBadge: true,
          VaButton: true,
          VaChip: true
        }
      }
    })

    const text = wrapper.text()
    expect(text).toContain('입사일')
    expect(text).toContain('JOIN_DATE')
    expect(text).toContain('schema_change_comparison')
  })

  it('마스킹된 주민등록번호(860104-1******)가 정상적으로 표출되어야 함', () => {
    const mockMaskedRequest = {
      id: 'req-masked-1',
      targetType: 'RECORD_UPDATE',
      changes: JSON.stringify({
        before: { jumin: '-' },
        after: { jumin: '860104-1******' }
      }),
      steps: []
    }

    const wrapper = mount(ApprovalDetailsViewer, {
      props: { request: mockMaskedRequest },
      global: {
        mocks: { $t: (key: string) => key },
        stubs: { VaIcon: true, VaBadge: true, VaButton: true, VaChip: true, ApprovalSteps: true }
      }
    })

    expect(wrapper.text()).toContain('860104-1******')
  })
})
