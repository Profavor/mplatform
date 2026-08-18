import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import InboxModal from '../../components/inbox/InboxModal.vue'

let activeWrapper: VueWrapper | null = null

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key: string) => key })
}))

const mockFetchFolderCounts = vi.fn().mockResolvedValue([
  { folder: 'INBOX', total: 10, unread: 3 }
])

vi.mock('~/composables/useInbox', () => ({
  useInbox: () => ({
    fetchFolderCounts: mockFetchFolderCounts,
    fetchMessages: vi.fn().mockResolvedValue({ content: [], totalElements: 0 }),
    fetchUnreadCount: vi.fn().mockResolvedValue({ unreadCount: 0 }),
    customFetch: vi.fn().mockResolvedValue([]),
    fetchMessage: vi.fn().mockResolvedValue({
      id: 'msg-1',
      subject: 'Test Subject',
      body: 'Test Body',
      senderEmail: 'sender@mplatform.com'
    }),
    moveToFolder: vi.fn().mockResolvedValue({}),
    moveToTrash: vi.fn().mockResolvedValue({}),
    markAsRead: vi.fn().mockResolvedValue({})
  })
}))

describe('InboxModal', () => {
  beforeEach(() => {
    if (typeof localStorage !== 'undefined') {
      localStorage.clear()
    }
  })

  afterEach(() => {
    if (activeWrapper) {
      activeWrapper.unmount()
      activeWrapper = null
    }
  })

  const defaultGlobal = {
    mocks: {
      $t: (key: string) => key
    },
    stubs: {
      AppModal: {
        props: ['modelValue', 'title', 'icon', 'showMaximize', 'noPadding', 'hideDefaultActions'],
        template: '<div class="app-modal-stub" v-if="modelValue"><slot name="header-actions" /><slot name="header" /><slot /><slot name="footer" /></div>'
      },
      VaButton: {
        template: '<button class="va-button" :class="$attrs.class" @click="$emit(\'click\')"><slot /></button>'
      },
      InboxFolderSidebar: { template: '<div class="folder-sidebar-stub" />' },
      InboxMessageList: { template: '<div class="message-list-stub" />' },
      InboxMessageDetail: { template: '<div class="message-detail-stub" />' },
      InboxComposeModal: { template: '<div class="compose-modal-stub" />' }
    }
  }

  it('renders modal content and resizable splitter when modelValue is true in split mode', () => {
    activeWrapper = mount(InboxModal, {
      props: {
        modelValue: true
      },
      global: defaultGlobal
    })

    expect(activeWrapper.find('.app-modal-stub').exists()).toBe(true)
    expect(activeWrapper.find('.inbox-modal-content').exists()).toBe(true)
    expect(activeWrapper.find('.inbox-splitter-gutter').exists()).toBe(true)
    expect(activeWrapper.find('.splitter-handle-bar').exists()).toBe(true)
  })

  it('toggles view mode between split and list mode', async () => {
    activeWrapper = mount(InboxModal, {
      props: {
        modelValue: true
      },
      global: defaultGlobal
    })

    expect(activeWrapper.find('.mode-split').exists()).toBe(true)
    
    // Switch to list mode
    ;(activeWrapper.vm as any).setViewMode('list')
    await activeWrapper.vm.$nextTick()

    expect(activeWrapper.find('.mode-list').exists()).toBe(true)
    expect(activeWrapper.find('.inbox-splitter-gutter').exists()).toBe(false)
  })

  it('supports splitter mousedown and dblclick reset', async () => {
    activeWrapper = mount(InboxModal, {
      props: {
        modelValue: true
      },
      global: defaultGlobal
    })

    const splitter = activeWrapper.find('.inbox-splitter-gutter')
    expect(splitter.exists()).toBe(true)

    await splitter.trigger('mousedown', { clientX: 500, preventDefault: () => {} })
    await splitter.trigger('dblclick')
    expect(activeWrapper.find('.inbox-modal-content').attributes('style')).toContain('--inbox-list-width')
  })

  it('does not render content when modelValue is false', () => {
    activeWrapper = mount(InboxModal, {
      props: {
        modelValue: false
      },
      global: defaultGlobal
    })

    expect(activeWrapper.find('.app-modal-stub').exists()).toBe(false)
  })

  it('refreshes folder counts when inbox-refresh-counts or inbox-message-received window event is received', async () => {
    mockFetchFolderCounts.mockClear()
    activeWrapper = mount(InboxModal, {
      props: {
        modelValue: true
      },
      global: defaultGlobal
    })

    expect(mockFetchFolderCounts).toHaveBeenCalledTimes(1)

    window.dispatchEvent(new CustomEvent('inbox-refresh-counts'))
    await activeWrapper.vm.$nextTick()
    expect(mockFetchFolderCounts).toHaveBeenCalledTimes(2)

    window.dispatchEvent(new CustomEvent('inbox-message-received'))
    await activeWrapper.vm.$nextTick()
    expect(mockFetchFolderCounts).toHaveBeenCalledTimes(3)
  })
})
