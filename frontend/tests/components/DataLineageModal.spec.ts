import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import DataLineageModal from '../../components/lineage/DataLineageModal.vue'

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
  formatWithTimezone: () => '2026-08-15 12:00:00'
}))

describe('DataLineageModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders data lineage modal properly', () => {
    const wrapper = mount(DataLineageModal, {
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
          'va-badge': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('data_lineage')
  })
})
