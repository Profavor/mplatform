import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ApprovalHistoryTimeline from '../../components/approval/ApprovalHistoryTimeline.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('ApprovalHistoryTimeline.vue (TDD)', () => {
  it('결재선 타임라인 기본 렌더링', async () => {
    const wrapper = mount(ApprovalHistoryTimeline, {
      props: {
        request: {
          status: 'PENDING',
          steps: [
            { stepOrder: 1, stepType: 'CONSENSUS', status: 'APPROVED', assigneeName: '결재자1' },
            { stepOrder: 2, stepType: 'APPROVAL', status: 'PENDING', assigneeName: '결재자2' }
          ]
        }
      },
      global: {
        stubs: {
          'va-icon': true,
          'va-badge': true,
          'ApprovalSteps': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })
})
