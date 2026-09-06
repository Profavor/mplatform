import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DqBenchmarkMatrix from '../../components/dq/DqBenchmarkMatrix.vue'
import DqMultiDomainTrendChart from '../../components/dq/DqMultiDomainTrendChart.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, params?: any) => {
      if (params && params.count !== undefined) return `${key}:${params.count}`
      return key
    },
    locale: { value: 'ko' }
  })
}))

const mockBenchmarkData = {
  averageScore: 91.5,
  totalMonitoredDomains: 2,
  highestDomain: {
    domainId: 'domain-uuid-1',
    domainCode: 'CUSTOMER',
    domainName: { ko: '고객 마스터', en: 'Customer Master' },
    score: 95.0,
    grade: 'A',
    totalRecords: 1000,
    totalViolations: 5,
    errorCount: 1,
    warningCount: 4,
    ruleCount: 8,
    riskLevel: 'HEALTHY',
    trendDelta: 2.5
  },
  lowestDomain: {
    domainId: 'domain-uuid-2',
    domainCode: 'STOCK',
    domainName: { ko: '주식 마스터', en: 'Stock Master' },
    score: 88.0,
    grade: 'B',
    totalRecords: 2000,
    totalViolations: 20,
    errorCount: 5,
    warningCount: 15,
    ruleCount: 12,
    riskLevel: 'WARNING',
    trendDelta: -1.2
  },
  totalViolations: 25,
  highRiskDomainCount: 0,
  items: [
    {
      domainId: 'domain-uuid-1',
      domainCode: 'CUSTOMER',
      domainName: { ko: '고객 마스터', en: 'Customer Master' },
      score: 95.0,
      grade: 'A',
      totalRecords: 1000,
      totalViolations: 5,
      errorCount: 1,
      warningCount: 4,
      ruleCount: 8,
      riskLevel: 'HEALTHY',
      trendDelta: 2.5
    },
    {
      domainId: 'domain-uuid-2',
      domainCode: 'STOCK',
      domainName: { ko: '주식 마스터', en: 'Stock Master' },
      score: 88.0,
      grade: 'B',
      totalRecords: 2000,
      totalViolations: 20,
      errorCount: 5,
      warningCount: 15,
      ruleCount: 12,
      riskLevel: 'WARNING',
      trendDelta: -1.2
    }
  ]
}

const mockTrendData = [
  {
    domainId: 'domain-uuid-1',
    domainCode: 'CUSTOMER',
    domainName: { ko: '고객 마스터', en: 'Customer Master' },
    dataPoints: [
      { recordedAt: '2026-09-01T10:00:00', score: 92.0, totalViolations: 8 },
      { recordedAt: '2026-09-02T10:00:00', score: 95.0, totalViolations: 5 }
    ]
  },
  {
    domainId: 'domain-uuid-2',
    domainCode: 'STOCK',
    domainName: { ko: '주식 마스터', en: 'Stock Master' },
    dataPoints: [
      { recordedAt: '2026-09-01T10:00:00', score: 86.0, totalViolations: 25 },
      { recordedAt: '2026-09-02T10:00:00', score: 88.0, totalViolations: 20 }
    ]
  }
]

describe('DqBenchmarkMatrix.vue', () => {
  it('renders benchmark KPI summary and matrix table rows properly', () => {
    const wrapper = mount(DqBenchmarkMatrix, {
      props: {
        benchmarkData: mockBenchmarkData,
        loading: false
      },
      global: {
        stubs: {
          'va-card': { template: '<div><slot /></div>' },
          'va-card-content': { template: '<div><slot /></div>' },
          'va-icon': true,
          'va-badge': true,
          'va-button': { template: '<button @click="$emit(\'click\')"><slot /></button>' }
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('91.5')
    expect(wrapper.text()).toContain('고객 마스터')
    expect(wrapper.text()).toContain('CUSTOMER')
    expect(wrapper.text()).toContain('주식 마스터')
    expect(wrapper.text()).toContain('STOCK')

    const rows = wrapper.findAll('.benchmark-row')
    expect(rows).toHaveLength(2)
  })

  it('emits select-domain event when clicking a table row', async () => {
    const wrapper = mount(DqBenchmarkMatrix, {
      props: {
        benchmarkData: mockBenchmarkData,
        loading: false
      },
      global: {
        stubs: {
          'va-card': { template: '<div><slot /></div>' },
          'va-card-content': { template: '<div><slot /></div>' },
          'va-icon': true,
          'va-badge': true,
          'va-button': true
        }
      }
    })

    const firstRow = wrapper.findAll('.benchmark-row')[0]
    await firstRow.trigger('click')

    expect(wrapper.emitted('select-domain')).toBeTruthy()
    expect(wrapper.emitted('select-domain')![0]).toEqual(['domain-uuid-1'])
  })
})

describe('DqMultiDomainTrendChart.vue', () => {
  it('renders multi-domain SVG chart and domain legends', () => {
    const wrapper = mount(DqMultiDomainTrendChart, {
      props: {
        multiDomainTrends: mockTrendData,
        days: 30,
        loading: false
      },
      global: {
        stubs: {
          'va-card': { template: '<div><slot /></div>' },
          'va-card-content': { template: '<div><slot /></div>' },
          'va-icon': true,
          'va-progress-circle': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('고객 마스터')
    expect(wrapper.text()).toContain('주식 마스터')

    const legends = wrapper.findAll('.legend-item')
    expect(legends).toHaveLength(2)

    const points = wrapper.findAll('circle.chart-point')
    expect(points).toHaveLength(4)
  })

  it('emits change-days event when period button is clicked', async () => {
    const wrapper = mount(DqMultiDomainTrendChart, {
      props: {
        multiDomainTrends: mockTrendData,
        days: 30,
        loading: false
      },
      global: {
        stubs: {
          'va-card': { template: '<div><slot /></div>' },
          'va-card-content': { template: '<div><slot /></div>' },
          'va-icon': true,
          'va-progress-circle': true
        }
      }
    })

    const periodBtns = wrapper.findAll('.period-btn')
    expect(periodBtns.length).toBeGreaterThanOrEqual(3)

    // Click '최근 7일' button (value: 7)
    await periodBtns[0].trigger('click')
    expect(wrapper.emitted('change-days')).toBeTruthy()
    expect(wrapper.emitted('change-days')![0]).toEqual([7])
  })
})
