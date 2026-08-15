import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ChangePasswordForm from '../../components/auth/ChangePasswordForm.vue'

const mockCustomFetch = vi.fn()

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    te: () => false,
    locale: { value: 'ko' }
  })
}))

vi.mock('#app', () => ({
  useCookie: () => ({ value: null })
}))

vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: mockCustomFetch
  })
}))

describe('ChangePasswordForm.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders inputs properly in forceMode', () => {
    const wrapper = mount(ChangePasswordForm, {
      props: {
        forceMode: true
      },
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          'va-alert': { template: '<div><slot /></div>' },
          'va-icon': { template: '<i />' },
          'va-input': { template: '<input />' },
          'va-button': { template: '<button><slot /></button>' }
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })

  it('calls customFetch with password payload on submit', async () => {
    mockCustomFetch.mockResolvedValueOnce({ success: true })

    const wrapper = mount(ChangePasswordForm, {
      props: {
        forceMode: false
      },
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          'va-alert': { template: '<div><slot /></div>' },
          'va-icon': { template: '<i />' },
          'va-input': { template: '<input />' },
          'va-button': { template: '<button><slot /></button>' }
        }
      }
    })

    wrapper.vm.oldPassword = 'OldPassword1!'
    wrapper.vm.newPassword = 'NewPassword1!'
    wrapper.vm.confirmPassword = 'NewPassword1!'

    await wrapper.vm.handleSubmit()

    expect(mockCustomFetch).toHaveBeenCalledWith('/api/users/me/password', {
      method: 'PUT',
      body: {
        oldPassword: 'OldPassword1!',
        newPassword: 'NewPassword1!'
      }
    })
    expect(wrapper.emitted('success')).toBeTruthy()
  })
})
