import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import DqRemediationModal from '../../components/dq/DqRemediationModal.vue'

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

vi.mock('vuestic-ui', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vuestic-ui')>()
  return {
    ...actual,
    useToast: () => ({
      init: vi.fn()
    })
  }
})

describe('DqRemediationModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders DQ auto-remediation modal properly', () => {
    const wrapper = mount(DqRemediationModal, {
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
    expect(wrapper.text()).toContain('auto_remediation')
  })
})
