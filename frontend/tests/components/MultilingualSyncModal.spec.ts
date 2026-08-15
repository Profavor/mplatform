import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import MultilingualSyncModal from '../../components/schema/MultilingualSyncModal.vue'

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

describe('MultilingualSyncModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          domainId: 'domain-1',
          totalFields: 10,
          missingCount: 1,
          missingItems: [
            {
              fieldId: 'f-1',
              fieldKey: 'biz_no',
              currentNameMap: { ko: '사업자등록번호' },
              missingLanguages: ['en'],
              suggestedTermName: 'Business Registration No'
            }
          ]
        }
      }
    })
  })

  it('renders multilingual sync modal properly', () => {
    const wrapper = mount(MultilingualSyncModal, {
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
          'va-button': true,
          'va-icon': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('multilingual_sync')
  })
})
