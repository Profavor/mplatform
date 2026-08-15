import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import VolumeAnomalyRadarModal from '../../components/admin/VolumeAnomalyRadarModal.vue'

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

describe('VolumeAnomalyRadarModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          status: 'SPIKE_DETECTED',
          currentThroughput: 4850,
          baselineThroughput: 1250,
          recommendation: '트래픽 급증 감지',
          history: [
            {
              timeBucket: '14:15',
              createCount: 850,
              updateCount: 1800,
              deleteCount: 42,
              apiCallCount: 4850,
              zScore: 3.4,
              isSpike: true
            }
          ]
        }
      }
    })
  })

  it('renders volume anomaly radar modal properly', () => {
    const wrapper = mount(VolumeAnomalyRadarModal, {
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
    expect(wrapper.text()).toContain('volume_radar')
  })
})
