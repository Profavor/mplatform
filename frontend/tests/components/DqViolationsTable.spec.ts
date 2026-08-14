import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DqViolationsTable from '../../components/dq/DqViolationsTable.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    locale: { value: 'ko' }
  })
}))

describe('DqViolationsTable.vue (TDD Component Test)', () => {
  const createMockViolations = () => [
    {
      id: 1,
      recordId: 'rec-01',
      recordIdentifier: 'REC-001',
      nodeName: { ko: '개인고객' },
      fieldKey: 'email',
      severity: 'ERROR',
      message: '이메일 형식 오류',
      actualValue: 'invalid-email',
      checkedAt: '2026-08-14T03:00:00Z'
    }
  ]

  const mockSeverityOptions = [
    { label: '전체', value: '' },
    { label: 'ERROR', value: 'ERROR' }
  ]

  it('위반 목록 비어있을 때 Empty State 표시 검증', () => {
    const wrapper = mount(DqViolationsTable, {
      props: {
        violationList: [],
        loadingViolations: false,
        filterSeverity: '',
        filterFieldKey: '',
        severityOptions: mockSeverityOptions,
        availableFieldFilterOptions: [],
        violationPage: 0,
        violationSize: 10,
        totalViolationsCount: 0,
        totalViolationPages: 0
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
          'va-select': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.text()).toContain('dq_dashboard.no_violations_found')
  })

  it('위반 데이터 바인딩 및 레코드 이동 이벤트 방출 검증', async () => {
    const violations = createMockViolations()
    const wrapper = mount(DqViolationsTable, {
      props: {
        violationList: violations,
        loadingViolations: false,
        filterSeverity: '',
        filterFieldKey: '',
        severityOptions: mockSeverityOptions,
        availableFieldFilterOptions: [],
        violationPage: 0,
        violationSize: 10,
        totalViolationsCount: 1,
        totalViolationPages: 1
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
          'va-select': true,
          'va-chip': true,
          'va-badge': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.text()).toContain('REC-001')
    expect(wrapper.text()).toContain('invalid-email')

    wrapper.vm.onGoToRecord('rec-01')
    expect(wrapper.emitted('go-to-record')).toBeTruthy()
    expect(wrapper.emitted('go-to-record')![0]).toEqual(['rec-01'])
  })
})
