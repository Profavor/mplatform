import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import AnomalyAccessDetectionModal from '../../components/admin/AnomalyAccessDetectionModal.vue'

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

describe('AnomalyAccessDetectionModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          threatLevelScore: 85,
          activeThreatCount: 1,
          summary: '위협 1건 탐지됨',
          events: [
            {
              eventId: 'SEC-001',
              userId: 'u-1',
              username: '외주 계정',
              threatLevel: 'CRITICAL',
              actionType: '대량 조회',
              details: '500건 시도',
              sourceIp: '1.2.3.4',
              blocked: false
            }
          ]
        }
      }
    })
  })

  it('renders anomaly detection modal properly', () => {
    const wrapper = mount(AnomalyAccessDetectionModal, {
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
    expect(wrapper.text()).toContain('anomaly_detection')
  })
})
