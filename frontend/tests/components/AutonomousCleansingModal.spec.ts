import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import AutonomousCleansingModal from '../../components/records/AutonomousCleansingModal.vue'

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

describe('AutonomousCleansingModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          totalAnomalies: 3,
          summary: '이상치 3건 발견',
          items: [
            {
              recordCode: 'REC-001',
              fieldKey: 'age',
              anomalyValue: '-5',
              recommendedValue: '35',
              cleansingStrategy: 'MEDIAN_INTERPOLATION',
              reason: '음수 나이 보정'
            }
          ]
        }
      }
    })
  })

  it('renders autonomous cleansing modal properly', () => {
    const wrapper = mount(AutonomousCleansingModal, {
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
          'va-inner-loading': {
            template: '<div><slot /></div>'
          },
          'va-badge': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('autonomous_cleansing')
  })
})
