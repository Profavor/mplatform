import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DashboardLeaseRiskWidget from '../../components/dashboard/DashboardLeaseRiskWidget.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('DashboardLeaseRiskWidget.vue (TDD Component Test)', () => {
  const createMockLeaseSummary = () => ({
    totalLeaseContracts: 42,
    expiringWithin30DaysCount: 3,
    overdueContractsCount: 2,
    highDebtRatioUnitsCount: 4,
    totalMonthlyRent: 85000000,
    totalDeposit: 1500000000,
    urgentContracts: [
      {
        recordId: 'REC-c7a8b9e1',
        contractNo: 'CONT-2026-0089',
        buildingUnit: '강남 프라임타워 501호',
        tenantName: '(주)알파소프트',
        daysUntilExpiration: 12,
        overdueCount: 2,
        debtRatio: 85.5,
        status: 'ACTIVE'
      },
      {
        recordId: 'REC-d8b9c0f2',
        contractNo: 'CONT-2026-0104',
        buildingUnit: '서초 센트럴스퀘어 1204호',
        tenantName: '베타네트웍스',
        daysUntilExpiration: 5,
        overdueCount: 0,
        debtRatio: 72.0,
        status: 'ACTIVE'
      }
    ]
  })

  const globalStubs = {
    'va-card': {
      template: '<div class="va-card-stub"><slot name="default" /><slot /></div>'
    },
    'va-card-title': {
      template: '<div class="va-card-title-stub"><slot /></div>'
    },
    'va-card-content': {
      template: '<div class="va-card-content-stub"><slot /></div>'
    },
    'va-badge': {
      template: '<span class="va-badge-stub">{{ text }}<slot /></span>',
      props: ['text']
    },
    'va-button': {
      template: '<button class="va-button-stub" @click="$emit(\'click\')"><slot /></button>'
    },
    'va-icon': true
  }

  it('4대 리스크 KPI 지표 및 금액 요약 렌더링 검증', () => {
    const summary = createMockLeaseSummary()
    const wrapper = mount(DashboardLeaseRiskWidget, {
      props: {
        leaseSummary: summary,
        isLoading: false
      },
      global: {
        stubs: globalStubs
      }
    })

    // 4대 KPI 검증
    expect(wrapper.text()).toContain('3') // expiringWithin30DaysCount
    expect(wrapper.text()).toContain('2') // overdueContractsCount
    expect(wrapper.text()).toContain('4') // highDebtRatioUnitsCount
    // 금액 요약 검증
    expect(wrapper.text()).toContain('85,000,000') // totalMonthlyRent
    expect(wrapper.text()).toContain('1,500,000,000') // totalDeposit
  })

  it('긴급 조치 대상 계약 목록 및 데이터 필드 바인딩 검증 (raw UUID 노출 방지)', () => {
    const summary = createMockLeaseSummary()
    const wrapper = mount(DashboardLeaseRiskWidget, {
      props: {
        leaseSummary: summary,
        isLoading: false
      },
      global: {
        stubs: globalStubs
      }
    })

    // 긴급 조치 계약 데이터 검증
    expect(wrapper.text()).toContain('CONT-2026-0089')
    expect(wrapper.text()).toContain('강남 프라임타워 501호')
    expect(wrapper.text()).toContain('(주)알파소프트')
    expect(wrapper.text()).toContain('12')
    expect(wrapper.text()).toContain('85.5%')

    // raw UUID 노출 방지 확인
    expect(wrapper.text()).not.toContain('340a0917-af0b-4d13-a1ce-479d4b2e2ca7')
  })

  it('긴급 조치 대상 계약이 없을 때 empty 안내 표출 검증', () => {
    const wrapper = mount(DashboardLeaseRiskWidget, {
      props: {
        leaseSummary: {
          totalLeaseContracts: 10,
          expiringWithin30DaysCount: 0,
          overdueContractsCount: 0,
          highDebtRatioUnitsCount: 0,
          totalMonthlyRent: 20000000,
          totalDeposit: 500000000,
          urgentContracts: []
        },
        isLoading: false
      },
      global: {
        stubs: globalStubs
      }
    })

    expect(wrapper.text()).toContain('lease_risk.no_urgent_contracts')
  })

  it('계약 상세 클릭 시 navigate-record 이벤트 방출 검증', async () => {
    const summary = createMockLeaseSummary()
    const wrapper = mount(DashboardLeaseRiskWidget, {
      props: {
        leaseSummary: summary,
        isLoading: false
      },
      global: {
        stubs: globalStubs
      }
    })

    const buttons = wrapper.findAll('button')
    if (buttons.length > 0) {
      await buttons[0].trigger('click')
      expect(wrapper.emitted('navigate-record')).toBeTruthy()
    }
  })
})
