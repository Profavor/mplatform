import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import SurvivorshipPage from '../../pages/admin/survivorship.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    locale: { value: 'ko' }
  })
}))

vi.mock('~/composables/usePageTitle', () => ({
  usePageTitle: () => ({
    pageTitle: { value: '생존 규칙 관리' }
  })
}))

vi.mock('~/composables/useAgGridTheme', () => ({
  useAgGridTheme: () => ({
    gridTheme: { value: 'ag-theme-quartz' },
    isDark: { value: false }
  })
}))

vi.mock('vuestic-ui', async (importOriginal) => {
  const actual: any = await importOriginal()
  return {
    ...actual,
    useToast: () => ({ init: vi.fn() })
  }
})

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: mockCustomFetch
  })
}))

vi.mock('~/stores/useDomainStore', () => ({
  useDomainStore: () => ({
    domainOptions: [{ text: '도메인 A', value: 'dom-1' }],
    fetchDomains: vi.fn(),
    getDomainName: () => '도메인 A'
  })
}))

vi.mock('~/stores/useCodeStore', () => ({
  useCodeStore: () => ({
    preloadGroups: vi.fn().mockResolvedValue([]),
    getDropdownOptions: () => [
      { text: '최신 우선', value: 'MOST_RECENT' },
      { text: '소스 우선', value: 'SOURCE_PRIORITY' }
    ]
  })
}))

vi.mock('#app', () => ({
  useCookie: () => ({ value: 'Asia/Seoul' })
}))

describe('pages/admin/survivorship.vue (TDD)', () => {
  beforeEach(() => {
    mockCustomFetch.mockReset()
    mockCustomFetch.mockImplementation((url: string) => {
      if (url.includes('/survivorship-rules')) {
        return Promise.resolve([])
      }
      if (url.includes('/fields')) {
        return Promise.resolve([
          { id: 'f-1', key: 'cust_name', name: { ko: '고객명' } },
          { id: 'f-2', key: 'email', name: { ko: '이메일' } }
        ])
      }
      return Promise.resolve([])
    })
  })

  it('서바이버쉽 관리 페이지가 정상 마운트되고 초기 렌더링 검증', async () => {
    const wrapper = mount(SurvivorshipPage, {
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-icon': true,
          'va-badge': true,
          'va-button': true,
          'va-select': true,
          'va-chip': true,
          SurvivorshipGuidePanel: true,
          AgGridVue: true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })
})
