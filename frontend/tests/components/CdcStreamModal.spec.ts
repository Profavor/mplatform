import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import CdcStreamModal from '../../components/records/CdcStreamModal.vue'

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

vi.mock('~/composables/useTimezoneDate', () => ({
  useTimezoneDate: () => ({
    formatWithTimezone: (d: string) => d
  })
}))

describe('CdcStreamModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          activeOffset: 2048,
          eventsPerSecond: 10.5,
          events: [
            {
              eventId: 'CDC-001',
              timestamp: '2026-08-15T00:00:00',
              operation: 'u',
              recordCode: 'REC-001',
              beforePayload: { name: '홍길동' },
              afterPayload: { name: '홍길동2' }
            }
          ]
        }
      }
    })
  })

  it('renders cdc stream modal properly', () => {
    const wrapper = mount(CdcStreamModal, {
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
    expect(wrapper.text()).toContain('cdc_stream')
  })

  it('passes recordId parameter to stream API when recordId prop is provided', async () => {
    const wrapper = mount(CdcStreamModal, {
      props: {
        modelValue: true,
        domainId: 'd-123',
        recordId: 'rec-uuid-1234'
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

    expect(mockCustomFetch).toHaveBeenCalledWith('/domains/d-123/cdc/stream?recordId=rec-uuid-1234')
  })

  it('renders attribute diff table for selected CDC event', async () => {
    const wrapper = mount(CdcStreamModal, {
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
          'va-button': {
            template: '<button><slot /></button>'
          }
        }
      }
    })

    await wrapper.vm.$nextTick()
    expect(wrapper.text()).toContain('table_view')
    expect(wrapper.text()).toContain('홍길동')
    expect(wrapper.text()).toContain('홍길동2')
  })
})
