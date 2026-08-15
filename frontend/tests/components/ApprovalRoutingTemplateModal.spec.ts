import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ApprovalRoutingTemplateModal from '../../components/approvals/ApprovalRoutingTemplateModal.vue'

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

describe('ApprovalRoutingTemplateModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: [
          {
            id: 'tpl-1',
            templateName: 'VIP Routing',
            conditionField: 'grade',
            conditionOperator: 'EQUALS',
            conditionValue: 'VIP',
            steps: [
              { stepOrder: 1, stepName: '부서장 검토', requiredRole: 'ROLE_DEPT_HEAD' }
            ]
          }
        ]
      }
    })
  })

  it('renders approval routing template modal properly', () => {
    const wrapper = mount(ApprovalRoutingTemplateModal, {
      props: {
        modelValue: true
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
          'va-input': true,
          'va-select': true,
          'va-badge': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('dynamic_routing')
  })
})
