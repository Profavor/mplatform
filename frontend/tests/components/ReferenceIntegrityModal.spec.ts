import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ReferenceIntegrityModal from '../../components/integrity/ReferenceIntegrityModal.vue'

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

describe('ReferenceIntegrityModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          domainId: 'domain-1',
          totalScannedRecords: 100,
          totalReferenceFields: 2,
          orphanCount: 0,
          integrityScore: 100,
          violations: []
        }
      }
    })
  })

  it('renders reference integrity modal properly', () => {
    const wrapper = mount(ReferenceIntegrityModal, {
      props: {
        modelValue: true,
        domainId: 'domain-1'
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
          'va-button': true,
          'va-icon': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('reference_integrity')
  })
})
