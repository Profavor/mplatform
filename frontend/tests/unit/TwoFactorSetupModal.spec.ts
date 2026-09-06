import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import TwoFactorSetupModal from '../../components/auth/TwoFactorSetupModal.vue'

const mockCustomFetch = vi.fn()

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    te: () => false,
    locale: { value: 'ko' }
  })
}))

vi.mock('#app', () => ({
  useCookie: () => ({ value: null }),
  navigateTo: vi.fn()
}))

vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: mockCustomFetch
  })
}))

describe('TwoFactorSetupModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  const createWrapper = (props = {}) => {
    return mount(TwoFactorSetupModal, {
      props: {
        isOpen: true,
        username: 'admin',
        isAlreadyEnabled: false,
        ...props
      },
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          'va-modal': {
            template: '<div v-if="modelValue" class="va-modal-stub"><slot /><slot name="footer" /></div>',
            props: ['modelValue']
          },
          'va-input': {
            template: '<input class="va-input-stub" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue']
          },
          'va-button': {
            template: '<button class="va-button-stub" @click="$emit(\'click\')"><slot /></button>',
            props: ['loading', 'disabled', 'color']
          },
          'va-alert': { template: '<div class="va-alert-stub"><slot /></div>' },
          'va-icon': { template: '<i />' },
          'va-badge': { template: '<span class="va-badge-stub"><slot /></span>' }
        }
      }
    })
  }

  it('fetches secret and QR url when opened in setup mode', async () => {
    mockCustomFetch.mockResolvedValueOnce({
      secret: 'SECRET123',
      otpAuthUrl: 'otpauth://...',
      qrCodeUrl: 'https://qr.test/code.png'
    })

    const wrapper = createWrapper()
    await wrapper.vm.loadSetupData()

    expect(mockCustomFetch).toHaveBeenCalledWith('/api/auth/2fa/setup', expect.objectContaining({
      method: 'POST',
      params: { username: 'admin' }
    }))
    expect(wrapper.vm.secret).toBe('SECRET123')
    expect(wrapper.vm.qrCodeUrl).toBe('https://qr.test/code.png')
  })

  it('enables 2FA and shows backup codes on valid code submission', async () => {
    mockCustomFetch.mockResolvedValueOnce({
      success: true,
      backupCodes: ['CODE1', 'CODE2', 'CODE3']
    })

    const wrapper = createWrapper()
    wrapper.vm.secret = 'SECRET123'
    wrapper.vm.verificationCode = '123456'

    await wrapper.vm.handleEnable()

    expect(mockCustomFetch).toHaveBeenCalledWith('/api/auth/2fa/enable', expect.objectContaining({
      method: 'POST',
      params: { username: 'admin' },
      body: {
        secret: 'SECRET123',
        code: '123456',
        type: 'TOTP'
      }
    }))
    expect(wrapper.vm.backupCodes).toEqual(['CODE1', 'CODE2', 'CODE3'])
    expect(wrapper.vm.step).toBe(3)
    expect(wrapper.emitted('enabled')).toBeTruthy()
  })

  it('disables 2FA when clicking disable', async () => {
    mockCustomFetch.mockResolvedValueOnce({
      success: true,
      message: 'Disabled'
    })

    const wrapper = createWrapper({ isAlreadyEnabled: true })
    await wrapper.vm.handleDisable()

    expect(mockCustomFetch).toHaveBeenCalledWith('/api/auth/2fa/disable', expect.objectContaining({
      method: 'POST',
      params: { username: 'admin' }
    }))
    expect(wrapper.emitted('disabled')).toBeTruthy()
  })
})
