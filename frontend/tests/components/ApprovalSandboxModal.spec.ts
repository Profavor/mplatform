import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ApprovalSandboxModal from '../../components/approvals/ApprovalSandboxModal.vue'

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: (...args: any[]) => mockCustomFetch(...args)
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    te: () => false,
    locale: { value: 'ko' }
  })
}))

describe('ApprovalSandboxModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          approvalRequestId: 'req-1',
          targetType: 'RECORD',
          actionType: 'UPDATE',
          summary: '결재 승인 시 2개 필드가 변경됩니다.',
          targetRecords: [
            {
              recordId: 'rec-1',
              recordCode: 'REC-001',
              fieldDiffs: [
                { fieldKey: 'phone', fieldName: '전화번호', v1Value: '010-1111', v2Value: '010-2222', diffStatus: 'MODIFIED' }
              ]
            }
          ]
        }
      }
    })
  })

  it('renders approval sandbox modal properly', () => {
    const wrapper = mount(ApprovalSandboxModal, {
      props: {
        modelValue: true,
        requestId: 'req-1'
      },
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          'va-modal': {
            template: '<div><h1>{{ title }}</h1><slot /></div>',
            props: ['title']
          },
          'va-alert': true,
          'va-inner-loading': {
            template: '<div><slot /></div>'
          },
          'va-badge': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('approval_sandbox')
  })
})
