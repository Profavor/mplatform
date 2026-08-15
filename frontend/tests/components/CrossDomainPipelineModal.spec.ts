import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import CrossDomainPipelineModal from '../../components/integration/CrossDomainPipelineModal.vue'

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

describe('CrossDomainPipelineModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: [
          {
            pipelineId: 'PIPE-001',
            name: '인사 -> 결재 동기화',
            sourceDomainName: '인사',
            targetDomainName: '결재',
            cronExpression: '0 0 2 * * ?',
            status: 'SUCCESS',
            lastSyncedCount: 100
          }
        ]
      }
    })
  })

  it('renders cross-domain pipeline modal properly', () => {
    const wrapper = mount(CrossDomainPipelineModal, {
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
    expect(wrapper.text()).toContain('sync_pipeline')
  })
})
