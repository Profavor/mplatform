import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import SchemaImpactModal from '../../components/schema/SchemaImpactModal.vue'

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

describe('SchemaImpactModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders schema impact modal properly', () => {
    const wrapper = mount(SchemaImpactModal, {
      props: {
        modelValue: true,
        domainId: 'domain-1',
        fieldKey: 'email',
        action: 'DELETE'
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
    expect(wrapper.text()).toContain('schema_simulation')
  })
})
