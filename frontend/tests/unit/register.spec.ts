import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { ref } from 'vue'
import RegisterPage from '../../pages/register.vue'

// Mock router
const replaceMock = vi.fn()
const navigateToMock = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    replace: replaceMock,
    push: vi.fn()
  }),
  useRoute: () => ({
    query: {}
  })
}))

// Mock Nuxt #app
let mockAuthToken: string | null = null
let mockRefreshToken: string | null = null
vi.mock('#app', () => ({
  useRoute: () => ({ query: {} }),
  useCookie: (name: string) => ({
    get value() {
      if (name === 'auth_token') return mockAuthToken
      if (name === 'refresh_token') return mockRefreshToken
      return null
    },
    set value(val) {
      if (name === 'auth_token') mockAuthToken = val
      if (name === 'refresh_token') mockRefreshToken = val
    }
  }),
  navigateTo: (path: string) => navigateToMock(path),
  useHead: vi.fn(),
  definePageMeta: vi.fn()
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    locale: { value: 'ko' }
  })
}))

const mockToast = vi.fn()
vi.mock('vuestic-ui', async (importOriginal) => {
  const actual = await importOriginal<any>()
  return {
    ...actual,
    useToast: () => ({ init: mockToast }),
    useColors: () => ({
      currentPresetName: { value: 'light' }
    })
  }
})

describe('Register Page (B2B Self-Service Funnel Tests)', () => {
  beforeEach(() => {
    mockAuthToken = null
    mockRefreshToken = null
    replaceMock.mockClear()
    navigateToMock.mockClear()
    mockToast.mockClear()
    vi.restoreAllMocks()
  })

  it('회원가입 화면에 회사명, 아이디, 이메일, 비밀번호, 약관 동의 등 필수 요소가 렌더링되어야 한다', () => {
    const wrapper = mount(RegisterPage, {
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          'va-icon': true,
          'va-card': { template: '<div class="va-card"><slot /></div>' },
          'va-card-content': { template: '<div class="va-card-content"><slot /></div>' },
          'va-input': {
            template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue', 'label', 'placeholder', 'type']
          },
          'va-checkbox': {
            template: '<label><input type="checkbox" :checked="modelValue" @change="$emit(\'update:modelValue\', $event.target.checked)" /><span>{{ label }}</span></label>',
            props: ['modelValue', 'label']
          },
          'va-button': {
            template: '<button :disabled="disabled" :loading="loading" @click="$emit(\'click\')"><slot /></button>',
            props: ['disabled', 'loading', 'to']
          },
          'NuxtLink': {
            template: '<a :href="to"><slot /></a>',
            props: ['to']
          }
        }
      }
    })

    expect(wrapper.find('.register-container').exists()).toBe(true)
    expect(wrapper.text()).toContain('register_title')
    expect(wrapper.text()).toContain('terms_agree')
  })

  it('약관 미동의 시 회원가입 제출이 방지되어야 한다', async () => {
    const fetchSpy = vi.spyOn(global, 'fetch')

    const wrapper = mount(RegisterPage, {
      global: {
        mocks: { $t: (k: string) => k },
        stubs: {
          'va-icon': true,
          'va-card': { template: '<div><slot /></div>' },
          'va-card-content': { template: '<div><slot /></div>' },
          'va-input': true,
          'va-checkbox': true,
          'va-button': {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          },
          'NuxtLink': true
        }
      }
    })

    // VM 인스턴스 데이터 설정
    const vm = wrapper.vm as any
    vm.form.companyName = '(주)알파테크'
    vm.form.username = 'alphatech'
    vm.form.email = 'admin@alphatech.com'
    vm.form.password = 'Pass1234!'
    vm.form.confirmPassword = 'Pass1234!'
    vm.form.termsAgreed = false

    await vm.handleSubmit()

    expect(fetchSpy).not.toHaveBeenCalled()
    expect(mockToast).toHaveBeenCalledWith(expect.objectContaining({
      color: 'warning'
    }))
  })

  it('가입 성공 시 토큰 쿠키가 설정되고 대시보드로 이동해야 한다', async () => {
    const mockApiResponse = {
      token: 'jwt-access-token-xyz',
      refreshToken: 'jwt-refresh-token-xyz',
      username: 'alphatech',
      role: 'ROLE_USER,ORG_ADMIN'
    }

    vi.spyOn(global, 'fetch').mockResolvedValueOnce({
      ok: true,
      json: async () => mockApiResponse
    } as any)

    const wrapper = mount(RegisterPage, {
      global: {
        mocks: { $t: (k: string) => k },
        stubs: {
          'va-icon': true,
          'va-card': { template: '<div><slot /></div>' },
          'va-card-content': { template: '<div><slot /></div>' },
          'va-input': true,
          'va-checkbox': true,
          'va-button': true,
          'NuxtLink': true
        }
      }
    })

    const vm = wrapper.vm as any
    vm.form.companyName = '(주)알파테크'
    vm.form.username = 'alphatech'
    vm.form.email = 'admin@alphatech.com'
    vm.form.password = 'Pass1234!'
    vm.form.confirmPassword = 'Pass1234!'
    vm.form.termsAgreed = true

    await vm.handleSubmit()
    await flushPromises()

    expect(mockAuthToken).toBe('jwt-access-token-xyz')
    expect(mockRefreshToken).toBe('jwt-refresh-token-xyz')
    expect(navigateToMock).toHaveBeenCalledWith('/dashboard')
  })

  it('사용자가 체험 템플릿(LEASE_CONTRACT 등)을 선택하면 회원가입 요청 body에 templateCategory가 포함되어야 한다', async () => {
    let capturedBody: any = null
    vi.spyOn(global, 'fetch').mockImplementation(async (url: any, options: any) => {
      if (typeof url === 'string' && url.includes('/api/auth/self-register')) {
        capturedBody = JSON.parse(options.body)
        return {
          ok: true,
          json: async () => ({ token: 'mock-token', refreshToken: 'mock-refresh' })
        } as any
      }
      return { ok: true, json: async () => ({}) } as any
    })

    const wrapper = mount(RegisterPage, {
      global: {
        mocks: { $t: (k: string) => k },
        stubs: {
          'va-icon': true,
          'va-card': { template: '<div><slot /></div>' },
          'va-card-content': { template: '<div><slot /></div>' },
          'va-input': true,
          'va-checkbox': true,
          'va-button': true,
          'NuxtLink': true
        }
      }
    })

    const vm = wrapper.vm as any
    vm.form.companyName = '(주)부동산임대'
    vm.form.username = 'estate_master'
    vm.form.email = 'owner@estate.com'
    vm.form.password = 'Pass1234!'
    vm.form.confirmPassword = 'Pass1234!'
    vm.form.termsAgreed = true
    vm.form.templateCategory = 'LEASE_CONTRACT'

    await vm.handleSubmit()
    await flushPromises()

    expect(capturedBody).not.toBeNull()
    expect(capturedBody.templateCategory).toBe('LEASE_CONTRACT')
    expect(capturedBody.companyName).toBe('(주)부동산임대')
  })
})
