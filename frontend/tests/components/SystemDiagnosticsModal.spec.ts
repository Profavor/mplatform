import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import SystemDiagnosticsModal from '../../components/admin/SystemDiagnosticsModal.vue'

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

describe('SystemDiagnosticsModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          overallStatus: 'HEALTHY',
          averageLatencyMs: 2.5,
          summary: '전체 인프라 정상',
          components: [
            {
              componentName: 'PostgreSQL DB',
              status: 'UP',
              latencyMs: 3,
              details: '정상 가동 중'
            }
          ]
        }
      }
    })
  })

  it('renders system diagnostics modal properly', () => {
    const wrapper = mount(SystemDiagnosticsModal, {
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
    expect(wrapper.text()).toContain('system_diagnostics')
  })
})
