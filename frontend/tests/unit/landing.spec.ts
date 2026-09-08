import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { ref } from 'vue'
import LandingPage from '../../pages/index.vue'
import LandingLayout from '../../layouts/landing.vue'

// Mock router
const replaceMock = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    replace: replaceMock,
    push: vi.fn()
  })
}))

// Mock useCookie
let mockToken: string | null = null
const createCookieMock = (name: string) => ({
  get value() {
    return name === 'auth_token' ? mockToken : (name === 'locale' ? 'ko' : 'light')
  },
  set value(v: any) {
    if (name === 'auth_token') mockToken = v
  }
})

vi.mock('#app', () => ({
  useCookie: (name: string) => createCookieMock(name),
  definePageMeta: vi.fn()
}))

vi.mock('#app/composables/cookie', () => ({
  useCookie: (name: string) => createCookieMock(name)
}))

vi.mock('#imports', () => ({
  useCookie: (name: string) => createCookieMock(name),
  definePageMeta: vi.fn(),
  useOidcAuth: () => ({
    loggedIn: { value: false }
  })
}))

vi.stubGlobal('useCookie', (name: string) => createCookieMock(name))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    locale: { value: 'ko' },
    setLocale: vi.fn()
  })
}))

vi.mock('vuestic-ui', async (importOriginal) => {
  const actual = await importOriginal<any>()
  return {
    ...actual,
    useColors: () => ({
      applyPreset: vi.fn(),
      currentPresetName: { value: 'light' }
    })
  }
})

describe('Landing Page & Layout (TDD Tests)', () => {
  beforeEach(() => {
    mockToken = null
    replaceMock.mockClear()
  })

  it('비로그인 사용자가 접근 시 랜딩 페이지의 모든 핵심 섹션이 렌더링되어야 한다', async () => {
    mockToken = null
    const wrapper = mount(LandingPage, {
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          'va-button': {
            template: '<button :to="to" :href="href"><slot /></button>',
            props: ['to', 'href']
          },
          'va-icon': {
            template: '<span class="va-icon">{{ name }}</span>',
            props: ['name']
          }
        }
      }
    })

    await flushPromises()

    // Hero Section
    expect(wrapper.text()).toContain('landing.hero.title')
    expect(wrapper.text()).toContain('landing.hero.cta_start')
    expect(wrapper.text()).toContain('landing.hero.cta_demo')

    // Why MDM Section
    expect(wrapper.text()).toContain('landing.why_mdm.title')
    expect(wrapper.text()).toContain('landing.why_mdm.problem1_title')

    // Core Features Section
    expect(wrapper.text()).toContain('landing.features.title')
    expect(wrapper.text()).toContain('landing.features.f1_title')

    // Cases Section
    expect(wrapper.text()).toContain('landing.cases.title')
    expect(wrapper.text()).toContain('landing.cases.c1_title')

    // Security & Trust Section
    expect(wrapper.text()).toContain('landing.trust.title')
    expect(wrapper.text()).toContain('landing.trust.t1_title')

    // FAQ Section
    expect(wrapper.text()).toContain('landing.faq.title')
    expect(wrapper.text()).toContain('landing.faq.q1')

    // Bottom CTA
    expect(wrapper.text()).toContain('landing.cta.title')

    // 비로그인 상태이므로 대시보드로 이동하지 않음
    expect(replaceMock).not.toHaveBeenCalled()
  })

  it('로그인 토큰이 존재하는 상태에서 랜딩 페이지 진입 시 /dashboard로 즉시 전환되어야 한다', async () => {
    const futureExp = Math.floor(Date.now() / 1000) + 3600
    const payload = btoa(JSON.stringify({ exp: futureExp, sub: 'user1' }))
    mockToken = `eyJhbGciOiJSUzI1NiJ9.${payload}.signature`
    mount(LandingPage, {
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          'va-button': { template: '<button><slot /></button>' },
          'va-icon': { template: '<span></span>' }
        }
      }
    })

    await flushPromises()

    expect(replaceMock).toHaveBeenCalledWith('/dashboard')
  })

  it('LandingLayout이 브랜드 로고, 네비게이션 앵커 및 액션 버튼을 렌더링해야 한다', async () => {
    const wrapper = mount(LandingLayout, {
      slots: {
        default: '<div class="test-slot-content">Main Landing Content</div>'
      },
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          NuxtLink: {
            template: '<a :href="to"><slot /></a>',
            props: ['to']
          },
          'va-button': {
            template: '<button :to="to"><slot /></button>',
            props: ['to']
          },
          'va-icon': {
            template: '<span class="va-icon">{{ name }}</span>',
            props: ['name']
          }
        }
      }
    })

    await flushPromises()

    expect(wrapper.text()).toContain('landing.nav.brand')
    expect(wrapper.text()).toContain('landing.nav.why_mdm')
    expect(wrapper.text()).toContain('landing.nav.features')
    expect(wrapper.text()).toContain('landing.nav.login')
    expect(wrapper.text()).toContain('landing.nav.start_trial')
    expect(wrapper.text()).toContain('Main Landing Content')
  })
})
