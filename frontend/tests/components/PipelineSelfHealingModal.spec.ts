import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import PipelineSelfHealingModal from '../../components/admin/PipelineSelfHealingModal.vue'

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

describe('PipelineSelfHealingModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          totalIncidents: 3,
          autoHealedCount: 3,
          healingSuccessRate: 100.0,
          summary: '파이프라인 자율 복구 가동 중',
          actions: [
            {
              actionId: 'HEAL-01',
              pipelineChannel: 'SAP ERP',
              errorType: 'SCHEMA_MISMATCH',
              diagnosedCause: '통화 누락',
              healingStrategy: 'PAYLOAD_TRANSFORMATION',
              recoveredCount: 142,
              status: 'AUTO_RESOLVED'
            }
          ]
        }
      }
    })
  })

  it('renders pipeline self healing modal properly', () => {
    const wrapper = mount(PipelineSelfHealingModal, {
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
          'va-badge': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('pipeline_self_healing')
  })
})
