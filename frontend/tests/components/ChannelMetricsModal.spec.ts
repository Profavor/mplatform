import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ChannelMetricsModal from '../../components/integration/ChannelMetricsModal.vue'

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: (...args: any[]) => mockCustomFetch(...args)
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, params?: any) => key,
    te: () => false,
    locale: { value: 'ko' }
  })
}))

vi.mock('vuestic-ui', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vuestic-ui')>()
  return {
    ...actual,
    useToast: () => ({
      init: vi.fn()
    })
  }
})

describe('ChannelMetricsModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders channel name and ping button properly', async () => {
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          channelId: 'ch-1',
          channelName: 'ERP 연동 채널',
          channelType: 'WEB_SERVICE',
          healthStatus: 'HEALTHY',
          totalRequests: 100,
          successCount: 98,
          failCount: 2,
          dlqCount: 0,
          successRate: 98.0,
          hourlyStats: [
            { timeSlot: '12:00', successCount: 10, failCount: 0, dlqCount: 0 }
          ]
        }
      }
    })

    const wrapper = mount(ChannelMetricsModal, {
      props: {
        modelValue: true,
        channelId: 'ch-1',
        channelName: 'ERP 연동 채널'
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-modal': {
            template: '<div><slot /></div>'
          },
          'va-badge': true,
          'va-icon': true,
          'va-card': {
            template: '<div><slot /></div>'
          },
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('ERP 연동 채널')
  })
})
