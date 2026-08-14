import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ApprovalSteps from '../../components/ApprovalSteps.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => {
      const map: Record<string, string> = {
        'consensus': '합의',
        'draft': '기안',
        'step_approval': '결재',
        'draft_completed': '기안완료',
        'processed': '처리됨',
        'no_comment': '의견 없음',
        'observers_list': '참조자 목록'
      }
      return map[key] || key
    }
  })
}))

describe('ApprovalSteps.vue (TDD Component Test)', () => {
  it('스텝 정보가 주어졌을 때 다국어 및 식별자 포맷팅이 올바르게 렌더링된다', () => {
    const mockRequest = {
      steps: [
        {
          id: 'step-1',
          stepOrder: 1,
          stepType: 'DRAFT',
          assigneeId: '12345678-abcd-ef01-2345-6789abcdef01',
          assigneeName: '홍길동',
          status: 'APPROVED',
          updatedAt: '2026-08-14T09:00:00Z',
          comment: '기안 상신합니다.'
        }
      ],
      observerIds: '["87654321-dcba-fe10-5432-10fedcba9876"]'
    }

    const wrapper = mount(ApprovalSteps, {
      props: {
        request: mockRequest
      },
      global: {
        stubs: {
          'va-badge': { template: '<span class="va-badge-stub"><slot />{{ $attrs.text }}</span>' }
        }
      }
    })

    expect(wrapper.text()).toContain('기안')
    expect(wrapper.text()).toContain('홍길동')
    expect(wrapper.text()).toContain('기안 상신합니다.')
    expect(wrapper.text()).toContain('참조자 목록')
    // Raw UUID가 아닌 USR- 식별 코드로 노출되는지 검증
    expect(wrapper.text()).toContain('USR-87654321')
    expect(wrapper.text()).not.toContain('87654321-dcba-fe10-5432-10fedcba9876')
  })
})
