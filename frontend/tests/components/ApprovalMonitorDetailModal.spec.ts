import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ApprovalMonitorDetailModal from '../../components/admin/ApprovalMonitorDetailModal.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    te: () => true
  })
}))

describe('ApprovalMonitorDetailModal.vue (TDD Component Test)', () => {
  const mockFlow = {
    id: 'flow-123',
    targetType: 'RECORD_CREATE',
    status: 'PENDING',
    requesterId: 'user-1',
    requesterName: '홍길동',
    createdAt: '2026-08-14T09:00:00Z',
    steps: [
      {
        id: 'step-1',
        stepType: 'DRAFT',
        status: 'SUBMITTED',
        assigneeId: 'user-1',
        assigneeName: '홍길동',
        createdAt: '2026-08-14T09:00:00Z'
      },
      {
        id: 'step-2',
        stepType: 'APPROVAL',
        status: 'PENDING',
        assigneeRole: 'ROLE_ADMIN',
        createdAt: '2026-08-14T09:05:00Z'
      }
    ]
  }

  it('모달 렌더링 및 결재 상세 정보 노출 검증', async () => {
    const wrapper = mount(ApprovalMonitorDetailModal, {
      props: {
        modelValue: true,
        selectedFlow: mockFlow
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /></div>'
          },
          'va-badge': true,
          'va-icon': true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          },
          'va-accordion': true,
          'va-collapse': true,
          'ApprovalDetailsViewer': true
        }
      }
    })

    expect(wrapper.find('.va-modal-stub').exists()).toBe(true)
    expect(wrapper.text()).toMatch(/RECORD_CREATE|target_type_RECORD_CREATE|신규/)
    expect(wrapper.text()).toContain('홍길동')
  })

  it('대리 승인 버튼 클릭 시 proxy-approve 이벤트 방출', async () => {
    const wrapper = mount(ApprovalMonitorDetailModal, {
      props: {
        modelValue: true,
        selectedFlow: mockFlow
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /></div>'
          },
          'va-badge': true,
          'va-icon': true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          },
          'va-accordion': true,
          'va-collapse': true,
          'ApprovalDetailsViewer': true
        }
      }
    })

    const buttons = wrapper.findAll('.va-btn-stub')
    expect(buttons.length).toBeGreaterThanOrEqual(2)
    await buttons[0].trigger('click')

    expect(wrapper.emitted('proxy-approve')).toBeTruthy()
    expect(wrapper.emitted('proxy-approve')![0]).toEqual(['step-2'])
  })

  it('대리 반려 버튼 클릭 시 proxy-reject 이벤트 방출', async () => {
    const wrapper = mount(ApprovalMonitorDetailModal, {
      props: {
        modelValue: true,
        selectedFlow: mockFlow
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /></div>'
          },
          'va-badge': true,
          'va-icon': true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          },
          'va-accordion': true,
          'va-collapse': true,
          'ApprovalDetailsViewer': true
        }
      }
    })

    const buttons = wrapper.findAll('.va-btn-stub')
    expect(buttons.length).toBeGreaterThanOrEqual(2)
    await buttons[1].trigger('click')

    expect(wrapper.emitted('proxy-reject')).toBeTruthy()
    expect(wrapper.emitted('proxy-reject')![0]).toEqual(['step-2'])
  })
})
