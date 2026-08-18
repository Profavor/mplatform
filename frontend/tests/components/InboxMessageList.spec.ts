import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import InboxMessageList from '../../components/inbox/InboxMessageList.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key: string) => key })
}))

vi.mock('~/composables/useInbox', () => ({
  useInbox: () => ({
    fetchMessages: vi.fn().mockResolvedValue({ content: [], totalElements: 0 }),
    toggleStar: vi.fn().mockResolvedValue({}),
    bulkMarkAsRead: vi.fn().mockResolvedValue({}),
    bulkMoveToTrash: vi.fn().mockResolvedValue({})
  })
}))

vi.mock('~/stores/useUserStore', () => ({
  useUserStore: () => ({
    getUserName: (id: string, fallback: string) => fallback || id,
    fetchUserMap: vi.fn().mockResolvedValue({})
  })
}))

vi.mock('~/composables/useTimezoneDate', () => ({
  useTimezoneDate: () => ({
    formatWithTimezone: (date: string) => date
  })
}))

vi.mock('vuestic-ui', async (importOriginal) => {
  const actual = await importOriginal<any>()
  return {
    ...actual,
    useToast: () => ({ init: vi.fn() }),
    useColors: () => ({ currentPresetName: { value: 'light' } })
  }
})

describe('InboxMessageList', () => {
  it('mounts properly with AG-Grid and controls', () => {
    const wrapper = mount(InboxMessageList, {
      props: {
        folder: 'INBOX',
        searchKeyword: ''
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          AgGridVue: { template: '<div class="ag-grid-stub" />' },
          VaInput: { template: '<input />' },
          VaButton: { template: '<button><slot /></button>' },
          VaIcon: true,
          VaBadge: true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })
})
