import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import GovernanceMaturityModal from '../../components/admin/GovernanceMaturityModal.vue'

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

describe('GovernanceMaturityModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          overallLevel: 'Level 4 (Managed)',
          overallScore: 94,
          summary: '최우수 성숙도 달성',
          kpiSummary: {
            completeness: 99.2,
            timeliness: 99.8,
            consistency: 98.6,
            validity: 99.4
          },
          dimensions: [
            {
              dimensionName: '데이터 품질(DQ)',
              currentScore: 96,
              level: 'Level 4',
              strengths: 'DQ 룰 완비',
              gapAndRoadmap: 'AI 학습 모델 튜닝'
            }
          ]
        }
      }
    })
  })

  it('renders governance maturity modal properly', () => {
    const wrapper = mount(GovernanceMaturityModal, {
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
    expect(wrapper.text()).toContain('governance_maturity')
  })
})
