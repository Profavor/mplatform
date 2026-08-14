import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ApprovalViewerModal from '../../components/ApprovalViewerModal.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('ApprovalViewerModal.vue (TDD Component Test)', () => {
  const createMockRequest = () => ({
    id: 'req-123',
    targetType: 'SCHEMA_FIELD_ADD',
    requesterName: '홍길동',
    requesterId: 'user01',
    createdAt: '2026-08-14T04:00:00Z',
    status: 'PENDING'
  })

  it('결재 뷰어 모달 기본 렌더링 및 결재 정보 바인딩 검증', () => {
    const mockRequest = createMockRequest()
    const wrapper = mount(ApprovalViewerModal, {
      props: {
        modelValue: true,
        request: mockRequest
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot name="header" /><slot /></div>'
          },
          'va-badge': true,
          'va-icon': true,
          ApprovalDetailsViewer: true
        }
      }
    })

    expect(wrapper.find('.va-modal-stub').exists()).toBe(true)
    expect(wrapper.text()).toContain('approval_history')
    expect(wrapper.text()).toContain('홍길동')
  })

  it('닫기 이벤트 방출 검증', async () => {
    const mockRequest = createMockRequest()
    const wrapper = mount(ApprovalViewerModal, {
      props: {
        modelValue: true,
        request: mockRequest
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot name="header" /><slot /></div>'
          },
          'va-badge': true,
          'va-icon': true,
          ApprovalDetailsViewer: true
        }
      }
    })

    wrapper.vm.onClose()
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0]).toEqual([false])
  })
})
