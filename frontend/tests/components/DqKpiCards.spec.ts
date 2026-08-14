import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DqKpiCards from '../../components/dq/DqKpiCards.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('DqKpiCards.vue (TDD Component Test)', () => {
  const createMockScoreData = () => ({
    score: 95.5,
    totalRecords: 12500,
    totalViolations: 42
  })

  it('4개 KPI 메트릭 데이터 바인딩 및 렌더링 검증', () => {
    const scoreData = createMockScoreData()
    const wrapper = mount(DqKpiCards, {
      props: {
        scoreData: scoreData,
        ruleCount: 8
      },
      global: {
        stubs: {
          'va-badge': true,
          'va-icon': true,
          'va-progress-circle': {
            template: '<div class="progress-circle-stub"><slot /></div>'
          }
        }
      }
    })

    expect(wrapper.text()).toContain('95.5')
    expect(wrapper.text()).toContain('12,500')
    expect(wrapper.text()).toContain('42')
    expect(wrapper.text()).toContain('8')
  })

  it('품질 점수별 등급 뱃지 및 색상 로직 검증', () => {
    const scoreData = createMockScoreData()
    const wrapper = mount(DqKpiCards, {
      props: {
        scoreData: scoreData,
        ruleCount: 8
      },
      global: {
        stubs: {
          'va-badge': true,
          'va-icon': true,
          'va-progress-circle': true
        }
      }
    })

    expect(wrapper.vm.getGradeLabel(95)).toBe('Grade A (우수)')
    expect(wrapper.vm.getGradeLabel(85)).toBe('Grade B (양호)')
    expect(wrapper.vm.getGradeLabel(75)).toBe('Grade C (보통)')
    expect(wrapper.vm.getGradeLabel(50)).toBe('Grade D (주의)')

    expect(wrapper.vm.getScoreColor(95)).toBe('success')
    expect(wrapper.vm.getScoreColor(85)).toBe('primary')
    expect(wrapper.vm.getScoreColor(75)).toBe('warning')
    expect(wrapper.vm.getScoreColor(50)).toBe('danger')
  })
})
