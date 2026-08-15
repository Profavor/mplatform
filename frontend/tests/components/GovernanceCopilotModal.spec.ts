import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import GovernanceCopilotModal from '../../components/admin/GovernanceCopilotModal.vue'

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

describe('GovernanceCopilotModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      reply: '품질 점수는 96.8점입니다.',
      suggestedActions: ['품질 대시보드 열기'],
      metricCards: { 'DQ 점수': '96.8점' },
      timestamp: '14:39:00'
    })
  })

  it('renders governance copilot modal properly', () => {
    const wrapper = mount(GovernanceCopilotModal, {
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
          'va-inner-loading': true,
          'va-chip': true,
          'va-input': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('governance_copilot')
  })
})
