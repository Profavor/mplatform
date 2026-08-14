import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DashboardDqCharts from '../../components/dashboard/DashboardDqCharts.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('DashboardDqCharts.vue (TDD Component Test)', () => {
  it('DQ 위반 추이 차트 및 DQ 심각도 분포 차트 카드 렌더링 검증', () => {
    const wrapper = mount(DashboardDqCharts, {
      props: {
        dqTrendChartOption: { title: { text: 'DQ Trend' } },
        dqSeverityChartOption: { title: { text: 'DQ Severity' } }
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

    expect(wrapper.text()).toContain('dq_violation_trend')
    expect(wrapper.text()).toContain('dq_severity_distribution')
  })
})
