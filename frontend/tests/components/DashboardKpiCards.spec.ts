import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DashboardKpiCards from '../../components/dashboard/DashboardKpiCards.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('DashboardKpiCards.vue (TDD Component Test)', () => {
  const createMockStats = () => ({
    totalDomains: 12,
    pendingApprovals: 5,
    activeRecords: 35000,
    pendingMatches: 8
  })

  it('4개 대시보드 KPI 카드 메트릭 바인딩 검증', () => {
    const stats = createMockStats()
    const wrapper = mount(DashboardKpiCards, {
      props: {
        stats: stats
      },
      global: {
        stubs: {
          'va-icon': true
        }
      }
    })

    expect(wrapper.text()).toContain('12')
    expect(wrapper.text()).toContain('5')
    expect(wrapper.text()).toContain('35,000')
    expect(wrapper.text()).toContain('8')
    expect(wrapper.text()).toContain('action_required')
  })

  it('결재 대기 건수 0일 때 all_tasks_cleared 표출 검증', () => {
    const wrapper = mount(DashboardKpiCards, {
      props: {
        stats: {
          totalDomains: 5,
          pendingApprovals: 0,
          activeRecords: 100,
          pendingMatches: 0
        }
      },
      global: {
        stubs: {
          'va-icon': true
        }
      }
    })

    expect(wrapper.text()).toContain('all_tasks_cleared')
  })
})
