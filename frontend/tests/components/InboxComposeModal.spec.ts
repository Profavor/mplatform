import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import InboxComposeModal from '../../components/inbox/InboxComposeModal.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key: string) => key })
}))

vi.mock('~/composables/useInbox', () => ({
  useInbox: () => ({
    sendMessage: vi.fn().mockResolvedValue({ id: 'msg-1' }),
    saveDraft: vi.fn().mockResolvedValue({ id: 'msg-2' })
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
    formatWithTimezone: (date: string) => '2026. 08. 18. 20:10:58'
  })
}))

vi.mock('~/composables/useAuthUser', () => ({
  useAuthUser: () => ({
    currentUserId: 'my-user-id',
    currentUser: { id: 'my-user-id', username: 'me' }
  })
}))

vi.mock('vuestic-ui', async (importOriginal) => {
  const actual = await importOriginal<any>()
  return {
    ...actual,
    useToast: () => ({
      init: vi.fn()
    })
  }
})

describe('InboxComposeModal', () => {
  const commonStubs = {
    AppModal: { template: '<div class="app-modal-stub"><slot name="header" /><slot /><slot name="footer" /></div>' },
    VaInput: { template: '<input />' },
    VaSelect: { template: '<div class="select-stub" />' },
    VaButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
    VaIcon: true,
    InboxRecipientPicker: { template: '<div class="recipient-picker" />' },
    InboxAttachmentUploader: { template: '<div class="attachment-uploader-stub" />' },
    HtmlEditor: { template: '<div class="html-editor" />' }
  }

  it('renders correctly in compose mode', () => {
    const wrapper = mount(InboxComposeModal, {
      props: {
        modelValue: true,
        mode: 'compose',
        originalMessage: null
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: commonStubs
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.app-modal-stub').exists()).toBe(true)
  })

  it('populates fields correctly in reply mode', () => {
    const original = {
      id: 'orig-1',
      senderId: 'user-sender',
      senderName: 'Sender',
      senderEmail: 'sender@mplatform.com',
      subject: 'Hello World',
      body: '<p>Original Body</p>',
      importance: 'NORMAL',
      messageType: 'INTERNAL',
      parentMessageId: null,
      rootMessageId: null,
      relatedApprovalId: null,
      isDraft: false,
      isRead: true,
      isStarred: false,
      folder: 'INBOX',
      hasAttachments: false,
      attachmentCount: 0,
      recipientCount: 1,
      threadCount: 1,
      toRecipients: [],
      ccRecipients: [],
      attachments: [],
      sentAt: '2026-08-18T00:00:00Z',
      createdAt: '2026-08-18T00:00:00Z'
    }

    const wrapper = mount(InboxComposeModal, {
      props: {
        modelValue: true,
        mode: 'reply',
        originalMessage: original
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: commonStubs
      }
    })

    expect(wrapper.exists()).toBe(true)
    const formData = (wrapper.vm as any).formData
    expect(formData.subject).toBe('Re: Hello World')
    expect(formData.toRecipients).toEqual(['user-sender'])
    expect(formData.parentMessageId).toBe('orig-1')
    expect(formData.body).toContain('inbox.original_message')
    expect(formData.body).toContain('Sender &lt;sender@mplatform.com&gt;')
    expect(formData.body).toContain('2026. 08. 18. 20:10:58')
    expect(formData.body).toContain('Hello World')
    expect(formData.body).toContain('<blockquote')
    expect(formData.body).toContain('Original Body')
  })

  it('populates fields correctly in replyAll mode', () => {
    const original = {
      id: 'orig-2',
      senderId: 'user-sender',
      senderName: 'Sender',
      senderEmail: 'sender@mplatform.com',
      subject: 'Re: Team Sync',
      body: '<p>Team notes</p>',
      importance: 'HIGH',
      messageType: 'INTERNAL',
      toRecipients: [{ userId: 'user-sender' }, { userId: 'my-user-id' }, { userId: 'user-colleague' }],
      ccRecipients: [{ userId: 'user-manager' }],
      attachments: []
    }

    const wrapper = mount(InboxComposeModal, {
      props: {
        modelValue: true,
        mode: 'replyAll',
        originalMessage: original
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: commonStubs
      }
    })

    expect(wrapper.exists()).toBe(true)
    const formData = (wrapper.vm as any).formData
    expect(formData.subject).toBe('Re: Team Sync')
    expect(formData.toRecipients).toContain('user-sender')
    expect(formData.toRecipients).toContain('user-colleague')
    expect(formData.toRecipients).not.toContain('my-user-id')
    expect(formData.ccRecipients).toContain('user-manager')
    expect(formData.body).toContain('inbox.original_message')
    expect(formData.body).toContain('user-manager')
    expect(formData.body).toContain('<blockquote')
    expect(formData.body).toContain('Team notes')
  })
})
