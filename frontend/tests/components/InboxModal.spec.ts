import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import InboxModal from '../../components/inbox/InboxModal.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key: string) => key })
}))

vi.mock('~/composables/useInbox', () => ({
  useInbox: () => ({
    fetchFolderCounts: vi.fn().mockResolvedValue([
      { folder: 'INBOX', total: 10, unread: 3 }
    ]),
    fetchMessage: vi.fn().mockResolvedValue({
      id: 'msg-1',
      subject: 'Test Subject',
      body: 'Test Body',
      senderEmail: 'sender@mplatform.com'
    }),
    moveToFolder: vi.fn().mockResolvedValue({}),
    moveToTrash: vi.fn().mockResolvedValue({})
  })
}))

describe('InboxModal', () => {
  beforeEach(() => {
    if (typeof localStorage !== 'undefined') {
      localStorage.clear()
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
    const wrapper = mount(InboxModal, {
      props: {
        modelValue: true
      },
      global: defaultGlobal
    })

    expect(wrapper.find('.app-modal-stub').exists()).toBe(true)
    expect(wrapper.find('.inbox-modal-content').exists()).toBe(true)
    expect(wrapper.find('.inbox-splitter-gutter').exists()).toBe(true)
    expect(wrapper.find('.splitter-handle-bar').exists()).toBe(true)
  })

  it('toggles view mode between split and list mode', async () => {
    const wrapper = mount(InboxModal, {
      props: {
        modelValue: true
      },
      global: defaultGlobal
    })

    expect(wrapper.find('.mode-split').exists()).toBe(true)
    
    // Switch to list mode
    ;(wrapper.vm as any).setViewMode('list')
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.mode-list').exists()).toBe(true)
    expect(wrapper.find('.inbox-splitter-gutter').exists()).toBe(false)
  })

  it('supports splitter mousedown and dblclick reset', async () => {
    const wrapper = mount(InboxModal, {
      props: {
        modelValue: true
      },
      global: defaultGlobal
    })

    const splitter = wrapper.find('.inbox-splitter-gutter')
    expect(splitter.exists()).toBe(true)

    await splitter.trigger('mousedown', { clientX: 500, preventDefault: () => {} })
    await splitter.trigger('dblclick')
    expect(wrapper.find('.inbox-modal-content').attributes('style')).toContain('--inbox-list-width')
  })

  it('does not render content when modelValue is false', () => {
    const wrapper = mount(InboxModal, {
      props: {
        modelValue: false
      },
      global: defaultGlobal
    })

    expect(wrapper.find('.app-modal-stub').exists()).toBe(false)
  })
})
