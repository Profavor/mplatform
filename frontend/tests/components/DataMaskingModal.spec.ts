import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import DataMaskingModal from '../../components/security/DataMaskingModal.vue'

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

describe('DataMaskingModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          recordId: 'rec-1',
          recordCode: 'REC-001',
          maskedData: {
            phone: '010-****-5678',
            email: 'h***@example.com'
          },
          isMasked: true,
          maskedFieldCount: 2
        }
      }
    })
  })

  it('renders data masking modal properly', () => {
    const wrapper = mount(DataMaskingModal, {
      props: {
        modelValue: true,
        recordId: 'rec-1'
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
          'va-switch': true,
          'va-badge': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('data_masking')
  })
})
