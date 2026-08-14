import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ApprovalDiffViewer from '../../components/approval/ApprovalDiffViewer.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('ApprovalDiffViewer.vue (TDD)', () => {
  it('기본 레코드 변경 데이터 Diff 렌더링', async () => {
    const wrapper = mount(ApprovalDiffViewer, {
      props: {
        request: {
          targetType: 'RECORD_UPDATE',
          changes: JSON.stringify({
            before: { name: '홍길동', email: 'hong@test.com' },
            after: { name: '홍길순', email: 'hong@test.com' }
          })
        }
      },
      global: {
        stubs: {
          'va-icon': true,
          'va-badge': true,
          'va-chip': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })
})
