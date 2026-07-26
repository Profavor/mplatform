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
})
