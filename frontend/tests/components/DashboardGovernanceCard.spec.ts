import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DashboardGovernanceCard from '../../components/dashboard/DashboardGovernanceCard.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, params?: any) => {
      if (params) return `${key}:${JSON.stringify(params)}`
      return key
    }
  })
}))

describe('DashboardGovernanceCard.vue (TDD Component Test)', () => {
  const createMockStats = () => ({
    openDqViolations: 15,
    pendingMatches: 4,
    approvedApprovals: 80,
    rejectedApprovals: 20
  })

  it('거버넌스 건전성 지표 데이터 및 승인율 바인딩 검증', () => {
    const stats = createMockStats()
    const wrapper = mount(DashboardGovernanceCard, {
      props: {
        stats: stats
      },
      global: {
        stubs: {
          'va-card': {
            template: '<div class="va-card-stub"><slot name="default" /><slot /></div>'
          },
          'va-card-title': {
            template: '<div class="va-card-title-stub"><slot /></div>'
          },
          'va-card-content': {
            template: '<div class="va-card-content-stub"><slot /></div>'
          },
          'va-icon': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.text()).toContain('15')
    expect(wrapper.text()).toContain('4')
    expect(wrapper.text()).toContain('80%')
  })

  it('승인율 계산 함수 검증 (분모 0일 때 0 반환)', () => {
    const wrapper = mount(DashboardGovernanceCard, {
      props: {
        stats: {
          openDqViolations: 0,
          pendingMatches: 0,
          approvedApprovals: 0,
          rejectedApprovals: 0
        }
      },
      global: {
        stubs: {
          'va-card': true,
          'va-card-title': true,
          'va-card-content': true,
          'va-icon': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.vm.getApprovalRate()).toBe(0)
  })
})
