import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import UnstructuredDataModal from '../../components/records/UnstructuredDataModal.vue'

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

describe('UnstructuredDataModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          domainId: 'd-123',
          overallConfidence: 0.95,
          suggestedRecordCode: 'REC-AI-1001',
          fields: [
            {
              fieldKey: 'biz_reg_no',
              extractedValue: '123-45-67890',
              confidenceScore: 0.98,
              sourceSnippet: '123-45-67890'
            }
          ]
        }
      }
    })
  })

  it('renders unstructured data modal properly', () => {
    const wrapper = mount(UnstructuredDataModal, {
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
          'va-textarea': true,
          'va-inner-loading': {
            template: '<div><slot /></div>'
          },
          'va-badge': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('ai_structurizer')
  })
})
