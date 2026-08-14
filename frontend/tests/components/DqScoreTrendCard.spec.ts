import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DqScoreTrendCard from '../../components/dq/DqScoreTrendCard.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('DqScoreTrendCard.vue (TDD Component Test)', () => {
  const createMockSnapshots = () => [
    {
      id: 1,
      score: 95.0,
      totalRecords: 1000,
      totalViolations: 5,
      scanType: 'SCHEDULED',
      recordedAt: '2026-08-14T00:00:00Z'
    },
    {
      id: 2,
      score: 90.0,
      totalRecords: 1000,
      totalViolations: 10,
      scanType: 'MANUAL',
      recordedAt: '2026-08-13T00:00:00Z'
    }
  ]

  it('스냅샷 비어있을 때 Empty State 표시 검증', () => {
    const wrapper = mount(DqScoreTrendCard, {
      props: {
        recentSnapshots: [],
        trendPeriod: 30,
        scanning: false,
        avgTrendScore: '0',
        maxTrendScore: '0'
      },
      global: {
        stubs: {
          'va-card': {
            template: '<div class="va-card-stub"><slot name="default" /><slot /></div>'
          },
          'va-card-content': {
            template: '<div class="va-card-content-stub"><slot /></div>'
          },
          'va-icon': true,
          'va-button': true,
          'va-chip': true
        }
      }
    })

    expect(wrapper.text()).toContain('dq_dashboard.no_snapshots')
  })

  it('스냅샷 존재 시 요약 배너 및 스캔 트리거 이벤트 방출 검증', async () => {
    const snapshots = createMockSnapshots()
    const wrapper = mount(DqScoreTrendCard, {
      props: {
        recentSnapshots: snapshots,
        trendPeriod: 30,
        scanning: false,
        avgTrendScore: '92.5',
        maxTrendScore: '95.0'
      },
      global: {
        stubs: {
          'va-card': {
            template: '<div class="va-card-stub"><slot name="default" /><slot /></div>'
          },
          'va-card-content': {
            template: '<div class="va-card-content-stub"><slot /></div>'
          },
          'va-icon': true,
          'va-button': true,
          'va-chip': true
        }
      }
    })

    expect(wrapper.text()).toContain('92.5')
    expect(wrapper.text()).toContain('95.0')

    wrapper.vm.onTriggerScan()
    expect(wrapper.emitted('trigger-scan')).toBeTruthy()
  })
})
