import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import GoldenRecordModal from '../../components/records/GoldenRecordModal.vue'

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

describe('GoldenRecordModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          candidateRecordCodes: ['REC-001', 'REC-002'],
          confidenceScore: 95,
          summary: '골든 레코드 조립 완료',
          fieldChoices: [
            { fieldKey: 'name', chosenValue: '홍길동', sourceSystem: 'ERP', chosenRecordCode: 'REC-001' }
          ],
          assembledData: { name: '홍길동' }
        }
      }
    })
  })

  it('renders golden record modal properly', () => {
    const wrapper = mount(GoldenRecordModal, {
      props: {
        modelValue: true,
        targetRecordIds: ['rec-1', 'rec-2']
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
    expect(wrapper.text()).toContain('golden_record')
  })
})
