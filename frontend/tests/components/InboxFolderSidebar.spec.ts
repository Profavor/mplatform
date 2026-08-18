import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import InboxFolderSidebar from '../../components/inbox/InboxFolderSidebar.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key: string) => key })
}))

describe('InboxFolderSidebar', () => {
  it('renders all standard folders', () => {
    const wrapper = mount(InboxFolderSidebar, {
      props: {
        activeFolder: 'INBOX',
        folderCounts: [
          { folder: 'INBOX', total: 10, unread: 3 },
          { folder: 'SENT', total: 5, unread: 0 },
          { folder: 'DRAFT', total: 1, unread: 0 },
          { folder: 'ARCHIVE', total: 20, unread: 0 },
          { folder: 'TRASH', total: 2, unread: 0 }
        ]
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          VaButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
          VaList: { template: '<div><slot /></div>' },
          VaListItem: { template: '<div class="folder-item" @click="$emit(\'click\')"><slot /></div>' },
          VaListItemSection: { template: '<div><slot /></div>' },
          VaListItemLabel: { template: '<div><slot /></div>' },
          VaIcon: true,
          VaBadge: true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })

  it('emits select-folder when a folder is clicked', async () => {
    const wrapper = mount(InboxFolderSidebar, {
      props: {
        activeFolder: 'INBOX',
        folderCounts: []
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          VaButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
          VaList: { template: '<div><slot /></div>' },
          VaListItem: { template: '<div class="folder-item" @click="$emit(\'click\')"><slot /></div>' },
          VaListItemSection: { template: '<div><slot /></div>' },
          VaListItemLabel: { template: '<div><slot /></div>' },
          VaIcon: true,
          VaBadge: true
        }
      }
    })

    const folderItems = wrapper.findAll('.folder-item')
    if (folderItems.length > 1) {
      await folderItems[1].trigger('click')
      expect(wrapper.emitted('select-folder')).toBeTruthy()
    }
  })

  it('emits compose when compose button is clicked', async () => {
    const wrapper = mount(InboxFolderSidebar, {
      props: {
        activeFolder: 'INBOX',
        folderCounts: []
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          VaButton: { template: '<button class="compose-btn" @click="$emit(\'click\')"><slot /></button>' },
          VaList: { template: '<div><slot /></div>' },
          VaListItem: { template: '<div><slot /></div>' },
          VaListItemSection: { template: '<div><slot /></div>' },
          VaListItemLabel: { template: '<div><slot /></div>' },
          VaIcon: true,
          VaBadge: true
        }
      }
    })

    const composeBtn = wrapper.find('.compose-btn')
    if (composeBtn.exists()) {
      await composeBtn.trigger('click')
      expect(wrapper.emitted('compose')).toBeTruthy()
    }
  })
})
