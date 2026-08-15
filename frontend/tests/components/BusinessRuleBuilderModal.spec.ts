import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import BusinessRuleBuilderModal from '../../components/records/BusinessRuleBuilderModal.vue'

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

describe('BusinessRuleBuilderModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: [
          {
            ruleId: 'BR-001',
            ruleName: 'VIP 고객 필수 사업자번호 및 신용등급 검증',
            conditionExpr: "grade == 'VIP'",
            validationExpr: "biz_no != null && rating in ['A', 'B']",
            enabled: true
          }
        ]
      }
    })
  })

  it('renders business rule builder modal properly', () => {
    const wrapper = mount(BusinessRuleBuilderModal, {
      props: {
        modelValue: true,
        domainId: 'd-123'
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
          'va-input': true,
          'va-inner-loading': {
            template: '<div><slot /></div>'
          },
          'va-badge': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('business_rules')
  })
})
