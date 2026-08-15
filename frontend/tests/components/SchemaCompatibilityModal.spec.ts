import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import SchemaCompatibilityModal from '../../components/schema/SchemaCompatibilityModal.vue'

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

describe('SchemaCompatibilityModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          domainId: 'd-123',
          overallCompatibility: 'BREAKING_CHANGE',
          riskScore: 80,
          summary: '브레이킹 체인지 감지',
          risks: [
            {
              fieldKey: 'legacy_code',
              changeType: 'REMOVED',
              riskLevel: 'CRITICAL',
              impactDescription: 'API 호출 오류 위험',
              mitigationGuide: 'Deprecated 권장'
            }
          ]
        }
      }
    })
  })

  it('renders schema compatibility modal properly', () => {
    const wrapper = mount(SchemaCompatibilityModal, {
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
    expect(wrapper.text()).toContain('schema_compatibility')
  })
})
