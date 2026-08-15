import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import BusinessGlossaryModal from '../../components/glossary/BusinessGlossaryModal.vue'

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

describe('BusinessGlossaryModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: [
          {
            id: 't-1',
            termCode: 'BIZ_NO',
            termName: { ko: '사업자등록번호' },
            abbreviation: 'BRN',
            sensitivityLevel: 'SENSITIVE'
          }
        ]
      }
    })
  })

  it('renders business glossary modal properly', () => {
    const wrapper = mount(BusinessGlossaryModal, {
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
          'va-input': true,
          'va-select': true,
          'va-button': true,
          'va-badge': true,
          'va-icon': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('business_glossary')
  })
})
