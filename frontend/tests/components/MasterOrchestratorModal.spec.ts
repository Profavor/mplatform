import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import MasterOrchestratorModal from '../../components/admin/MasterOrchestratorModal.vue'

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

describe('MasterOrchestratorModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          totalFeatures: 50,
          healthyFeatures: 50,
          systemMaturityLevel: 'Level 5 - Autonomous Master',
          summary: '50대 기능 정상 가동 중',
          modules: [
            {
              featureNo: 1,
              category: 'SCHEMA_LIFECYCLE',
              featureName: '레코드 롤백',
              status: 'ONLINE_HEALTHY',
              healthScore: 100
            }
          ]
        }
      }
    })
  })

  it('renders master orchestrator modal properly', () => {
    const wrapper = mount(MasterOrchestratorModal, {
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
    expect(wrapper.text()).toContain('master_orchestrator')
  })
})
