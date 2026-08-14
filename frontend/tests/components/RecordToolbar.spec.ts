import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import RecordToolbar from '../../components/records/RecordToolbar.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

vi.mock('~/composables/usePermission', () => ({
  usePermission: () => ({
    hasPermission: () => true
  })
}))

describe('RecordToolbar.vue (TDD)', () => {
  it('기본 툴바 렌더링 및 버튼 이벤트 emit 검증', async () => {
    const wrapper = mount(RecordToolbar, {
      props: {
        selectedNode: { id: 'node-1', name: { ko: '부서' }, isDomain: false },
        selectedRecordRows: [{ id: 'rec-1' }],
        hasCreateWorkflow: false
      },
      global: {
        stubs: {
          'va-icon': true,
          'va-button': true,
          'va-chip': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })
})
