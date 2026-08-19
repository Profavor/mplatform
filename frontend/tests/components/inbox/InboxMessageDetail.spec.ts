import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import InboxMessageDetail from '../../../components/inbox/InboxMessageDetail.vue'

const mockCustomFetch = vi.fn()
const mockInitToast = vi.fn()

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key: string, fallback?: string) => fallback || key })
}))

vi.mock('#app', () => ({
  useCookie: (key: string) => {
    if (key === 'user_data' || key === 'user') {
      return { value: mockCurrentUser }
    }
    return { value: 'Asia/Seoul' }
  }
}))

vi.mock('~/composables/usePermission', () => ({
  usePermission: () => ({
    hasPermission: () => false
  })
}))

vi.mock('vuestic-ui', async (importOriginal) => {
  const actual = await importOriginal<Record<string, any>>()
  return {
    ...actual,
    useToast: () => ({
      init: mockInitToast
    })
  }
})

vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({ customFetch: mockCustomFetch })
}))

vi.mock('~/composables/useTimezoneDate', () => ({
  useTimezoneDate: () => ({
    formatWithTimezone: (date: string) => date
  })
}))

vi.mock('~/composables/useInbox', () => ({
  useInbox: () => ({
    recallMessage: vi.fn()
  })
}))

const mockCurrentUser = {
  id: 'user-requester-id',
  username: 'requesterUser',
  role: 'ROLE_USER'
}

vi.mock('~/stores/useUserStore', () => ({
  useUserStore: () => ({
    getUserName: (id: string, name: string) => name || id
  })
}))

describe('InboxMessageDetail - Cancel Approval Request', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders Cancel Approval button when current user is requester and approval is PENDING', async () => {
    mockCustomFetch.mockResolvedValueOnce({
      id: 'appr-123',
      requesterId: 'user-requester-id',
      status: 'PENDING',
      currentStepOrder: 1,
      steps: [
        { id: 'step-1', stepOrder: 1, stepType: 'APPROVAL', status: 'PENDING', assigneeId: 'approver-1' }
      ]
    })

    const wrapper = mount(InboxMessageDetail, {
      props: {
        message: {
          id: 'msg-1',
          subject: 'Memo Approval Request',
          senderId: 'user-requester-id',
          senderName: 'Requester',
          senderEmail: 'requester@test.com',
          relatedApprovalId: 'appr-123',
          createdAt: '2026-08-19T20:00:00',
          folder: 'SENT'
        } as any,
        activeFolder: 'SENT'
      },
      global: {
        mocks: {
          $t: (key: string, fallback?: string) => fallback || key
        },
        stubs: {
          'va-icon': true,
          'va-badge': true,
          'va-avatar': true,
          'va-chip': true,
          'va-button': {
            props: ['preset', 'color', 'icon', 'loading'],
            template: '<button class="va-button-stub" :class="$attrs.class" @click="$emit(\'click\')"><slot /></button>'
          },
          'va-divider': true,
          'va-modal': {
            props: ['modelValue', 'title'],
            template: '<div v-if="modelValue" class="va-modal-stub"><slot /></div>'
          },
          'va-textarea': true
        }
      }
    })

    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()

    const cancelBtn = wrapper.find('.cancel-approval-btn')
    expect(cancelBtn.exists()).toBe(true)
  })

  it('does not render Cancel Approval button when approval is already APPROVED or REJECTED', async () => {
    mockCustomFetch.mockResolvedValueOnce({
      id: 'appr-456',
      requesterId: 'user-requester-id',
      status: 'APPROVED',
      steps: []
    })

    const wrapper = mount(InboxMessageDetail, {
      props: {
        message: {
          id: 'msg-2',
          subject: 'Completed Memo Approval',
          senderId: 'user-requester-id',
          relatedApprovalId: 'appr-456',
          createdAt: '2026-08-19T20:00:00'
        } as any
      },
      global: {
        mocks: {
          $t: (key: string, fallback?: string) => fallback || key
        },
        stubs: {
          'va-icon': true,
          'va-badge': true,
          'va-avatar': true,
          'va-chip': true,
          'va-button': true,
          'va-divider': true,
          'va-modal': true,
          'va-textarea': true
        }
      }
    })

    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()

    const cancelBtn = wrapper.find('.cancel-approval-btn')
    expect(cancelBtn.exists()).toBe(false)
  })
})
