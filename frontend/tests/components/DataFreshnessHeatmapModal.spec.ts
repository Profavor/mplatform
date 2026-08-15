import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import DataFreshnessHeatmapModal from '../../components/admin/DataFreshnessHeatmapModal.vue'

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

describe('DataFreshnessHeatmapModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          overallFreshnessScore: 95,
          totalDomains: 5,
          staleCount: 0,
          summary: '데이터 신선도 최상',
          domains: [
            {
              domainCode: 'DOM-01',
              domainName: '고객 마스터',
              lastUpdatedTime: '3분 전',
              freshnessSlaMinutes: 10,
              delayMinutes: 3,
              freshnessScore: 99,
              status: 'FRESH'
            }
          ]
        }
      }
    })
  })

  it('renders data freshness heatmap modal properly', () => {
    const wrapper = mount(DataFreshnessHeatmapModal, {
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
    expect(wrapper.text()).toContain('freshness_heatmap')
  })
})
