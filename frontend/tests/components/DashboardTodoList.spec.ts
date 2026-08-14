import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DashboardTodoList from '../../components/dashboard/DashboardTodoList.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('DashboardTodoList.vue (TDD Component Test)', () => {
  const createMockTodos = () => [
    {
      id: 'step-01',
      stepType: 'CONSENSUS',
      approvalRequest: {
        id: 'req-01',
        requesterName: '홍길동',
        createdAt: '2026-08-14T02:00:00Z',
        changes: [{ action: 'CREATE' }],
        classificationNode: {
          domainName: { ko: '고객', en: 'Customer' },
          name: { ko: '개인고객', en: 'Individual' }
        }
      }
    }
  ]

  it('Todo 비어있을 때 Empty 안내 표시 검증', () => {
    const wrapper = mount(DashboardTodoList, {
      props: {
        todos: [],
        displayInfo: {},
        currentLocale: 'ko'
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
          'va-badge': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.text()).toContain('no_pending_tasks_you')
  })

  it('Todo 존재 시 데이터 바인딩 및 검토 이벤트 방출 검증', async () => {
    const todos = createMockTodos()
    const wrapper = mount(DashboardTodoList, {
      props: {
        todos: todos,
        displayInfo: {
          'step-01': {
            displayId: 'CUST-001',
            displayName: '홍길동',
            idField: { name: { ko: '고객ID' } },
            nameField: { name: { ko: '고객명' } }
          }
        },
        currentLocale: 'ko'
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
          'va-badge': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.text()).toContain('홍길동')
    expect(wrapper.text()).toContain('CUST-001')

    wrapper.vm.onGoToApprovals(todos[0])
    expect(wrapper.emitted('review')).toBeTruthy()
    expect(wrapper.emitted('review')![0]).toEqual([todos[0]])
  })
})
