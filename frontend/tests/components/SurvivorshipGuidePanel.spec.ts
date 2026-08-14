import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import SurvivorshipGuidePanel from '../../components/admin/SurvivorshipGuidePanel.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('SurvivorshipGuidePanel.vue (TDD Component Test)', () => {
  const mockStrategyOptions = [
    { value: 'SOURCE_PRIORITY', text: '원천 소스 시스템 우선' },
    { value: 'MOST_RECENT', text: '최신 수정 시각 기준' },
    { value: 'MOST_COMPLETE', text: '최고 완전성 / 최장 길이' }
  ]

  it('가이드 패널 기본 렌더링 및 전략 카드 목록 검증', () => {
    const wrapper = mount(SurvivorshipGuidePanel, {
      props: {
        selectedDomainId: 'domain-1',
        rulesCount: 5,
        domainFieldsCount: 12,
        currentDomainName: '고객 마스터',
        strategyOptions: mockStrategyOptions
      },
      global: {
        stubs: {
          'va-icon': true
        }
      }
    })

    expect(wrapper.text()).toContain('survivorship.guide_title')
    expect(wrapper.text()).toContain('5')
    expect(wrapper.text()).toContain('12')
    expect(wrapper.text()).toContain('고객 마스터')
    expect(wrapper.text()).toContain('SOURCE_PRIORITY')
    expect(wrapper.text()).toContain('MOST_RECENT')
    expect(wrapper.text()).toContain('MOST_COMPLETE')
  })

  it('도메인이 미선택되었을 때 KPI 칩 숨김 검증', () => {
    const wrapper = mount(SurvivorshipGuidePanel, {
      props: {
        selectedDomainId: null,
        rulesCount: 0,
        domainFieldsCount: 0,
        currentDomainName: '-',
        strategyOptions: mockStrategyOptions
      },
      global: {
        stubs: {
          'va-icon': true
        }
      }
    })

    expect(wrapper.find('.panel-kpi-group').exists()).toBe(false)
  })
})
