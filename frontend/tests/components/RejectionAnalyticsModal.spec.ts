import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import RejectionAnalyticsModal from '../../components/approvals/RejectionAnalyticsModal.vue'

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

describe('RejectionAnalyticsModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          totalRejections: 5,
          summary: '반려 사유 분석 완료',
          topCategories: [
            { category: '필수값 누락', count: 3, percentage: 60.0, guide: '필수 필드를 채우세요.' }
          ],
          recommendedChecklist: ['1. 필수 입력 필드 확인']
        }
      }
    })
  })

  it('renders rejection analytics modal properly', () => {
    const wrapper = mount(RejectionAnalyticsModal, {
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
          'va-progress-bar': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('rejection_analytics')
  })
})
