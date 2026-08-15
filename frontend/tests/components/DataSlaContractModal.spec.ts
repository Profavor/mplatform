import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import DataSlaContractModal from '../../components/admin/DataSlaContractModal.vue'

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

describe('DataSlaContractModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          totalContracts: 3,
          compliantCount: 3,
          overallComplianceRate: 100.0,
          summary: 'SLA 100% 준수 중',
          contracts: [
            {
              slaId: 'SLA-01',
              contractName: 'ERP SLA',
              targetChannelOrDomain: 'SAP ERP',
              latencyThresholdMs: 100,
              currentLatencyMs: 32,
              availabilityTargetPercent: 99.9,
              currentAvailabilityPercent: 99.99,
              qualityCompliancePercent: 99.8,
              status: 'MEETING_SLA'
            }
          ]
        }
      }
    })
  })

  it('renders data sla contract modal properly', () => {
    const wrapper = mount(DataSlaContractModal, {
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
    expect(wrapper.text()).toContain('data_sla')
  })
})
