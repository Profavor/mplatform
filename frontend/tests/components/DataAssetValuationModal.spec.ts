import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import DataAssetValuationModal from '../../components/schema/DataAssetValuationModal.vue'

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

describe('DataAssetValuationModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          totalDomainsEvaluated: 1,
          totalEstimatedAssetValueWon: 120000000,
          averageQualityScore: 97.5,
          summary: '데이터 자산 평가 완료',
          domainValuations: [
            {
              domainId: 'domain-1',
              domainName: '고객 도메인',
              recordCount: 1500,
              connectedChannelCount: 3,
              assetRating: 'AAA',
              estimatedAssetValueWon: 120000000
            }
          ]
        }
      }
    })
  })

  it('renders data asset valuation modal properly', () => {
    const wrapper = mount(DataAssetValuationModal, {
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
    expect(wrapper.text()).toContain('data_asset_valuation')
  })
})
