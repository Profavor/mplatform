import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import DqViolationsTable from '~/components/dq/DqViolationsTable.vue'

const i18n = createI18n({
  legacy: false,
  locale: 'ko',
  messages: {
    ko: {
      'dq_dashboard.violation_table_title': 'DQ 위반 상세 레코드 목록',
      'dq_dashboard.violation_table_sub': '실시간 검증 실패 상세 레코드 모니터링',
      'dq_dashboard.record_id': '레코드 식별자',
      'classification_node': '분류 노드',
      'dq_dashboard.field': '필드',
      'dq_dashboard.violated_field': '위반 필드',
      'dq_dashboard.severity': '심각도',
      'dq_dashboard.rule_name': '검증 규칙',
      'dq_dashboard.violation_message': '위반 내용',
      'createdAt': '생성일시',
      'action': '동작',
      'dq_dashboard.details': '상세보기',
      'dq_dashboard.empty_value': '(빈 값)',
      'dq_dashboard.pagination_summary': '총 {total}건 중 {start} - {end}건 표시'
    }
  }
})

describe('DqViolationsTable.vue Component', () => {
  it('레코드 식별자에 HTML 태그가 포함되어 있어도 텍스트만 깔끔하게 정제되어 렌더링되어야 한다', () => {
    const wrapper = mount(DqViolationsTable, {
      props: {
        violationList: [
          {
            id: 'viol-1',
            recordId: '340a0917-af0b-4d13-a1ce-479d4b2e2ca7',
            recordIdentifier: '<p>CUST-2026-0001</p>',
            nodeName: '전체 고객',
            fieldKey: 'CONTACT_EMAIL',
            severity: 'WARNING',
            message: '유효한 이메일 형식이 아닙니다.',
            actualValue: 'test@example.com',
            checkedAt: '2026-08-28T02:00:00Z'
          }
        ],
        loadingViolations: false,
        filterSeverity: '',
        filterFieldKey: '',
        severityOptions: [],
        availableFieldFilterOptions: [],
        violationPage: 0,
        violationSize: 10,
        totalViolationsCount: 1,
        totalViolationPages: 1
      },
      global: {
        plugins: [i18n],
        stubs: {
          'va-card': { template: '<div class="va-card"><slot /><slot name="title" /></div>' },
          'va-card-title': { template: '<div class="va-card-title"><slot /></div>' },
          'va-card-content': { template: '<div class="va-card-content"><slot /></div>' },
          'va-icon': { template: '<i class="va-icon" />' },
          'va-select': { template: '<div class="va-select" />' },
          'va-button': { template: '<button class="va-button"><slot /></button>' },
          'va-progress-circle': { template: '<div class="va-progress-circle" />' },
          'va-chip': { template: '<span class="va-chip"><slot /></span>' },
          'va-badge': { template: '<span class="va-badge"><slot /></span>' }
        }
      }
    })

    const recordCell = wrapper.find('.record-id-cell')
    expect(recordCell.exists()).toBe(true)
    expect(recordCell.text()).toContain('CUST-2026-0001')
    expect(recordCell.text()).not.toContain('<p>')
  })

  it('recordIdentifier가 없을 때 REC-xxxxxxxx 포맷으로 식별 코드가 렌더링되어야 한다', () => {
    const wrapper = mount(DqViolationsTable, {
      props: {
        violationList: [
          {
            id: 'viol-2',
            recordId: '340a0917-af0b-4d13-a1ce-479d4b2e2ca7',
            recordIdentifier: null,
            nodeName: '전체 고객',
            fieldKey: 'CONTACT_EMAIL',
            severity: 'WARNING',
            message: '유효한 이메일 형식이 아닙니다.',
            actualValue: 'test@example.com',
            checkedAt: '2026-08-28T02:00:00Z'
          }
        ],
        loadingViolations: false,
        filterSeverity: '',
        filterFieldKey: '',
        severityOptions: [],
        availableFieldFilterOptions: [],
        violationPage: 0,
        violationSize: 10,
        totalViolationsCount: 1,
        totalViolationPages: 1
      },
      global: {
        plugins: [i18n],
        stubs: {
          'va-card': { template: '<div><slot /></div>' },
          'va-card-title': { template: '<div><slot /></div>' },
          'va-card-content': { template: '<div><slot /></div>' },
          'va-icon': true,
          'va-select': true,
          'va-button': true,
          'va-progress-circle': true,
          'va-chip': true,
          'va-badge': true
        }
      }
    })

    const recordCell = wrapper.find('.record-id-cell')
    expect(recordCell.exists()).toBe(true)
    expect(recordCell.text()).toContain('REC-340a0917')
  })
})
