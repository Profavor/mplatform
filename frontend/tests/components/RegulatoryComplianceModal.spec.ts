import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import RegulatoryComplianceModal from '../../components/admin/RegulatoryComplianceModal.vue'

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

describe('RegulatoryComplianceModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          overallScore: 100,
          certificationReadiness: 'READY',
          summary: '규제 준수 완료',
          items: [
            {
              framework: 'ISMS-P',
              controlCode: '2.6.4',
              controlTitle: '개인정보 암호화',
              status: 'PASS',
              evidence: 'AES-256 적용'
            }
          ]
        }
      }
    })
  })

  it('renders regulatory compliance modal properly', () => {
    const wrapper = mount(RegulatoryComplianceModal, {
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
          'va-badge': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('regulatory_compliance')
  })
})
