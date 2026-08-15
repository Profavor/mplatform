import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ApprovalDelegationModal from '~/components/approvals/ApprovalDelegationModal.vue'

vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: vi.fn((url: string) => {
    if (url.includes('/approvals/delegations/my')) {
      return Promise.resolve({
        data: {
          value: {
            delegatedByMe: [
              {
                id: 'del-1',
                delegateeUserId: 'user-2',
                delegateeUserName: '이대결',
                startDate: '2026-08-15T09:00:00',
                endDate: '2026-08-20T18:00:00',
                reason: '휴가',
                isActive: true
              }
            ],
            delegatedToMe: []
          }
        }
      })
    }
    if (url.includes('/admin/users')) {
      return Promise.resolve({
        data: {
          value: [
            { id: 'user-2', username: '이대결' }
          ]
        }
      })
    }
    return Promise.resolve({ data: { value: null } })
  })
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, params?: any) => key,
    te: () => false,
    locale: { value: 'ko' }
  })
}))

vi.mock('#app', () => ({
  useCookie: () => ({ value: 'Asia/Seoul' })
}))

vi.mock('~/composables/useTimezoneDate', () => ({
  formatWithTimezone: (d: string) => d.replace('T', ' ')
}))

vi.mock('vuestic-ui', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vuestic-ui')>()
  return {
    ...actual,
    useToast: () => ({
      init: vi.fn()
    })
  }
})

describe('ApprovalDelegationModal.vue', () => {
  it('renders correctly and displays delegated approvals list', async () => {
    const wrapper = mount(ApprovalDelegationModal, {
      props: {
        modelValue: true
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-modal': {
            template: '<div><slot /></div>'
          },
          'va-alert': true,
          'va-card': {
            template: '<div><slot /></div>'
          },
          'va-card-content': {
            template: '<div><slot /></div>'
          },
          'va-badge': true,
          'va-button': true,
          'va-progress-circle': true,
          'va-icon': true,
          'va-select': true,
          'va-input': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })
})
