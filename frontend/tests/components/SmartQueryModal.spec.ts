import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import SmartQueryModal from '../../components/records/SmartQueryModal.vue'

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

describe('SmartQueryModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          naturalLanguageQuery: 'VIP 서울',
          explanation: 'VIP 조건 1건 검색됨',
          parsedFilters: [{ fieldKey: 'grade', operator: 'EQUALS', value: 'VIP' }],
          matchedRecordCount: 1,
          records: [{ _recordCode: 'REC-001', grade: 'VIP', address: '서울시' }]
        }
      }
    })
  })

  it('renders smart query modal properly', () => {
    const wrapper = mount(SmartQueryModal, {
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
          'va-input': true,
          'va-inner-loading': {
            template: '<div><slot /></div>'
          },
          'va-badge': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('smart_query')
  })
})
