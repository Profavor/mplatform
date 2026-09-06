import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import TwoFactorVerifyModal from '../../components/auth/TwoFactorVerifyModal.vue'

const mockCustomFetch = vi.fn()
const mockAuthTokenCookie = { value: '' }
const mockRefreshTokenCookie = { value: '' }

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, params?: any) => {
      if (params?.days) return `${key}:${params.days}`
      return key
    },
    te: () => false,
    locale: { value: 'ko' }
  })
}))

vi.mock('#app', () => ({
  useCookie: (name: string) => {
    if (name === 'auth_token') return mockAuthTokenCookie
    if (name === 'refresh_token') return mockRefreshTokenCookie
    return { value: '' }
  },
  navigateTo: vi.fn()
}))

vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: mockCustomFetch
  })
}))

describe('TwoFactorVerifyModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockAuthTokenCookie.value = ''
    mockRefreshTokenCookie.value = ''
  })

  const createWrapper = (props = {}) => {
    return mount(TwoFactorVerifyModal, {
      props: {
        isOpen: true,
        username: 'admin',
        tempToken: 'TEMP_TOKEN_123',
        maskedEmail: 'ad***@example.com',
        gracePeriodRemainingDays: 5,
        ...props
      },
      global: {
        mocks: {
          $t: (k: string, params?: any) => {
            if (params?.days) return `${k}:${params.days}`
            return k
          }
        },
        stubs: {
          'va-modal': {
            template: '<div v-if="modelValue" class="va-modal-stub"><slot /><slot name="footer" /></div>',
            props: ['modelValue']
          },
          'va-tabs': { template: '<div class="va-tabs-stub"><slot /></div>', props: ['modelValue'] },
          'va-tab': { template: '<button class="va-tab-stub" @click="$emit(\'click\')"><slot /></button>' },
          'va-input': {
            template: '<input class="va-input-stub" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue']
          },
          'va-button': {
            template: '<button class="va-button-stub" @click="$emit(\'click\')"><slot /></button>',
            props: ['loading', 'disabled', 'color']
          },
          'va-alert': { template: '<div class="va-alert-stub"><slot /></div>' },
          'va-icon': { template: '<i />' }
        }
      }
    })
  }

  it('renders modal with TOTP tab as default and displays grace period banner when grace days exist', () => {
    const wrapper = createWrapper({ gracePeriodRemainingDays: 5 })
    expect(wrapper.find('.va-modal-stub').exists()).toBe(true)
    expect(wrapper.text()).toContain('two_factor_title')
    expect(wrapper.text()).toContain('two_factor_grace_banner:5')
    expect(wrapper.text()).toContain('two_factor_grace_skip')
  })

  it('verifies TOTP code and emits verified event with tokens on success', async () => {
    mockCustomFetch.mockResolvedValueOnce({
      token: 'FINAL_ACCESS_TOKEN',
      refreshToken: 'FINAL_REFRESH_TOKEN'
    })

    const wrapper = createWrapper()
    const input = wrapper.find('.va-input-stub')
    await input.setValue('123456')

    await wrapper.vm.handleVerify()

    expect(mockCustomFetch).toHaveBeenCalledWith('/api/auth/2fa/verify', expect.objectContaining({
      method: 'POST',
      body: {
        username: 'admin',
        tempToken: 'TEMP_TOKEN_123',
        code: '123456',
        type: 'TOTP'
      }
    }))

    expect(mockAuthTokenCookie.value).toBe('FINAL_ACCESS_TOKEN')
    expect(mockRefreshTokenCookie.value).toBe('FINAL_REFRESH_TOKEN')
    expect(wrapper.emitted('verified')).toBeTruthy()
  })

  it('sends email OTP when clicking send email in EMAIL tab', async () => {
    mockCustomFetch.mockResolvedValueOnce({
      success: true,
      message: 'Email sent'
    })

    const wrapper = createWrapper()
    wrapper.vm.activeTab = 'EMAIL'
    await wrapper.vm.$nextTick()

    await wrapper.vm.handleSendEmailOtp()

    expect(mockCustomFetch).toHaveBeenCalledWith('/api/auth/2fa/send-email', expect.objectContaining({
      method: 'POST',
      body: {
        username: 'admin',
        tempToken: 'TEMP_TOKEN_123'
      }
    }))
  })

  it('verifies BACKUP_CODE and emits verified event on success', async () => {
    mockCustomFetch.mockResolvedValueOnce({
      token: 'FINAL_ACCESS_TOKEN',
      refreshToken: 'FINAL_REFRESH_TOKEN'
    })

    const wrapper = createWrapper()
    wrapper.vm.activeTab = 'BACKUP_CODE'
    await wrapper.vm.$nextTick()

    const input = wrapper.find('.va-input-stub')
    await input.setValue('ABCD1234')

    await wrapper.vm.handleVerify()

    expect(mockCustomFetch).toHaveBeenCalledWith('/api/auth/2fa/verify', expect.objectContaining({
      method: 'POST',
      body: {
        username: 'admin',
        tempToken: 'TEMP_TOKEN_123',
        code: 'ABCD1234',
        type: 'BACKUP_CODE'
      }
    }))

    expect(wrapper.emitted('verified')).toBeTruthy()
  })

  it('displays error message when verification fails', async () => {
    mockCustomFetch.mockRejectedValueOnce(new Error('Invalid code'))

    const wrapper = createWrapper()
    const input = wrapper.find('.va-input-stub')
    await input.setValue('000000')

    await wrapper.vm.handleVerify()

    expect(wrapper.vm.errorMessage).toBe('two_factor_invalid_code')
    expect(mockAuthTokenCookie.value).toBe('')
  })

  it('emits skip event when clicking skip grace period button', async () => {
    const wrapper = createWrapper({ gracePeriodRemainingDays: 3 })
    await wrapper.vm.handleSkip()
    expect(wrapper.emitted('skip')).toBeTruthy()
  })
})
