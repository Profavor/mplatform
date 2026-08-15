import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import IntegrationDlqModal from '../../components/integration/IntegrationDlqModal.vue'

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: (...args: any[]) => mockCustomFetch(...args)
}))

vi.mock('~/composables/useTimezoneDate', () => ({
  formatWithTimezone: (date: string) => date
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    te: () => false,
    locale: { value: 'ko' }
  })
}))

describe('IntegrationDlqModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: [
          {
            logId: 'log-1',
            channelId: 'ch-1',
            channelName: 'ERP 연계',
            recordCode: 'REC-001',
            eventType: 'CREATE',
            status: 'FAIL',
            errorMessage: 'Timeout',
            retryCount: 1,
            createdAt: '2026-08-15T12:00:00'
          }
        ]
      }
    })
  })

  it('renders integration DLQ modal properly', () => {
    const wrapper = mount(IntegrationDlqModal, {
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
    expect(wrapper.text()).toContain('dlq_hub')
  })
})
