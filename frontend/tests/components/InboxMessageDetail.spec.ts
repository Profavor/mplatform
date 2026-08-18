import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import InboxMessageDetail from '../../components/inbox/InboxMessageDetail.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key: string, params?: any) => key })
}))

vi.mock('~/composables/useTimezoneDate', () => ({
  useTimezoneDate: () => ({
    formatWithTimezone: (date: string) => date
  })
}))

vi.mock('~/composables/useInbox', () => ({
  useInbox: () => ({
    recallMessage: vi.fn().mockResolvedValue({
      messageId: 'msg-1',
      totalRecipients: 2,
      recalledBeforeReadCount: 1,
      recalledAfterReadCount: 1,
      externalCount: 0,
      details: []
    })
  })
}))

vi.mock('~/stores/useUserStore', () => ({
  useUserStore: () => ({
    getUserName: (id: string, fallback: string) => fallback || id,
    fetchUserMap: vi.fn().mockResolvedValue({})
  })
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() })
}))

describe('InboxMessageDetail', () => {
  it('renders empty state when no message is provided', () => {
    const wrapper = mount(InboxMessageDetail, {
      props: {
        message: null
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          VaIcon: true,
          VaButton: true,
          VaModal: true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })

  it('renders message content with top attachments when provided', () => {
    const message = {
      id: 'msg-1',
      senderId: 'user-1',
      senderName: 'Alice',
      senderEmail: 'alice@mplatform.com',
      subject: 'Test Subject',
      body: '<p>Test message content</p>',
      importance: 'HIGH',
      messageType: 'INTERNAL',
      parentMessageId: null,
      rootMessageId: null,
      relatedApprovalId: null,
      isDraft: false,
      isRead: true,
      isStarred: false,
      folder: 'INBOX',
      hasAttachments: true,
      attachmentCount: 1,
      recipientCount: 1,
      threadCount: 1,
      toRecipients: [{ userId: 'user-2', name: 'Bob', email: 'bob@mplatform.com', recipientType: 'TO' }],
      ccRecipients: [],
      attachments: [
        { id: 'att-1', fileName: 'sample.pdf', fileSize: 10240, contentType: 'application/pdf' }
      ],
      sentAt: '2026-08-18T00:00:00Z',
      createdAt: '2026-08-18T00:00:00Z'
    }

    const wrapper = mount(InboxMessageDetail, {
      props: {
        message
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          VaIcon: true,
          VaBadge: true,
          VaButton: { template: '<button @click="$emit(\'click\')"><slot /></button>' },
          VaChip: true,
          VaModal: true,
          InboxThreadTimeline: true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.top-attachments-wrapper').exists()).toBe(true)
    expect(wrapper.find('.file-name-text').text()).toBe('sample.pdf')
  })

  it('shows recall button and read receipts when viewing in SENT folder', async () => {
    const message = {
      id: 'msg-1',
      senderId: 'user-1',
      senderName: 'Alice',
      senderEmail: 'alice@mplatform.com',
      subject: 'Sent Subject',
      body: '<p>Body</p>',
      importance: 'NORMAL',
      messageType: 'INTERNAL',
      parentMessageId: null,
      rootMessageId: null,
      relatedApprovalId: null,
      isDraft: false,
      isRead: true,
      isStarred: false,
      folder: 'SENT',
      hasAttachments: false,
      attachmentCount: 0,
      recipientCount: 2,
      threadCount: 1,
      toRecipients: [
        { userId: 'user-2', name: 'Bob', email: 'bob@mplatform.com', recipientType: 'TO', isRead: true, readAt: '2026-08-18T08:00:00Z', isRecalled: false },
        { userId: 'user-3', name: 'Charlie', email: 'charlie@mplatform.com', recipientType: 'TO', isRead: false, isRecalled: false }
      ],
      ccRecipients: [],
      attachments: [],
      sentAt: '2026-08-18T00:00:00Z',
      createdAt: '2026-08-18T00:00:00Z'
    }

    const wrapper = mount(InboxMessageDetail, {
      props: {
        message,
        activeFolder: 'SENT'
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          VaIcon: true,
          VaBadge: { template: '<span class="va-badge"><slot /></span>' },
          VaButton: { template: '<button class="recall-btn" @click="$emit(\'click\')"><slot /></button>' },
          VaChip: true,
          VaModal: true
        }
      }
    })

    expect(wrapper.find('.recall-btn').exists()).toBe(true)
    const badges = wrapper.findAll('.va-badge')
    expect(badges.length).toBeGreaterThan(0)
  })
})
