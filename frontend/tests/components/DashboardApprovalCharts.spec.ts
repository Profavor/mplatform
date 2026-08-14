import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DashboardApprovalCharts from '../../components/dashboard/DashboardApprovalCharts.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('DashboardApprovalCharts.vue (TDD Component Test)', () => {
  it('결재 추이 차트 및 도메인 분포 차트 카드 렌더링 검증', () => {
    const wrapper = mount(DashboardApprovalCharts, {
      props: {
        trendChartOption: { title: { text: 'Trend' } },
        distributionChartOption: { title: { text: 'Distribution' } }
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
          'ClientOnly': {
            template: '<div class="client-only-stub"><slot /></div>'
          },
          'v-chart': true
        }
      }
    })

    expect(wrapper.text()).toContain('approval_trend_title')
    expect(wrapper.text()).toContain('domain_distribution_title')
  })
})
