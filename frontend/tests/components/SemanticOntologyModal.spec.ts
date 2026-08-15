import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import SemanticOntologyModal from '../../components/schema/SemanticOntologyModal.vue'

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => {
  const fn = (...args: any[]) => mockCustomFetch(...args)
  fn.customFetch = (...args: any[]) => mockCustomFetch(...args)
  return {
    useCustomFetch: () => fn
  }
})

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    te: () => false,
    locale: { value: 'ko' }
  })
}))

describe('SemanticOntologyModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      summary: '전사 온톨로지 정상',
      nodes: [
        { id: 'NODE-CUST', label: '고객 마스터', domainCode: 'DOM-CUST' }
      ],
      edges: [
        { sourceId: 'NODE-ORD', targetId: 'NODE-CUST', relationType: 'PURCHASED_BY', weight: 1.0 }
      ]
    })
  })

  it('renders semantic ontology modal properly', () => {
    const wrapper = mount(SemanticOntologyModal, {
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
          'va-input': true,
          'va-chip': true,
          'va-inner-loading': {
            template: '<div><slot /></div>'
          },
          'va-badge': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('semantic_ontology')
  })
})
