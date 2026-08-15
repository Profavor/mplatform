import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import MultiTenantRouterModal from '../../components/admin/MultiTenantRouterModal.vue'

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

describe('MultiTenantRouterModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          totalTenants: 3,
          activeTenants: 3,
          summary: '가상 테넌트 격리 가동 중',
          rules: [
            {
              tenantCode: 'HQ_KR',
              tenantName: '한국 본사',
              partitionType: 'ROW_FILTER',
              expression: "country_code == 'KR'",
              targetDomainCount: 12,
              active: true
            }
          ]
        }
      }
    })
  })

  it('renders multi tenant router modal properly', () => {
    const wrapper = mount(MultiTenantRouterModal, {
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
          'va-switch': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('multi_tenant')
  })
})
