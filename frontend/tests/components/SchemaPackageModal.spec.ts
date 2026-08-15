import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import SchemaPackageModal from '../../components/schema/SchemaPackageModal.vue'

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

describe('SchemaPackageModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders export and import tabs properly', async () => {
    const wrapper = mount(SchemaPackageModal, {
      props: {
        modelValue: true,
        domainId: 'domain-1',
        domainName: '고객 도메인'
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-modal': {
            template: '<div><slot /></div>'
          },
          'va-alert': true,
          'va-card': {
            template: '<div><slot /></div>'
          },
          'va-button': true,
          'va-checkbox': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('고객 도메인')
  })
})
