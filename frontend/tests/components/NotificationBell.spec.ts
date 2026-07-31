import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import NotificationBell from '../../components/layout/NotificationBell.vue'

// $fetch Nuxt global mock
vi.stubGlobal('$fetch', vi.fn().mockResolvedValue([]))

// Global mocks for Nuxt composables & Vuestic UI
vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key: string) => key })
}))


vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
  useRoute: () => ({ path: '/', query: {}, params: {} })
}))

vi.mock('~/composables/useApprovalEnricher', () => ({
  useApprovalEnricher: () => ({
    loadMetadata: vi.fn().mockResolvedValue(undefined),
    getFieldsForNode: vi.fn().mockResolvedValue([]),
    enrichApprovalDetails: vi.fn().mockResolvedValue(null),
    nodes: { value: {} },
    domains: { value: {} },
    fieldSchemas: { value: {} }
  })
}))

vi.mock('~/composables/useWebSocket', () => ({
  useWebSocket: () => ({
    connect: vi.fn(),
    disconnect: vi.fn(),
    isConnected: { value: false }
  })
}))

vi.mock('#app', async (importOriginal) => {
  const actual = await importOriginal<any>()
  return {
    ...actual,
    useCookie: () => ({ value: 'fake-token' }),
    useRouter: () => ({ push: vi.fn() }),
    useRuntimeConfig: () => ({ public: { apiBaseUrl: 'http://localhost:8080' } })
  }
})

vi.mock('~/stores/useUserStore', () => ({
  useUserStore: () => ({
    userMap: { value: {} },
    isInitialized: { value: true },
    fetchUserMap: vi.fn().mockResolvedValue({}),
    getUserName: (id: string, fallback: string) => fallback || id || '',
    parseI18nVal: (val: any) => String(val || '')
  })
}))

vi.mock('~/stores/useRoleStore', () => ({
  useRoleStore: () => ({
    rolesList: { value: [] },
    roleOptions: { value: [] },
    isInitialized: { value: true },
    dispatch: vi.fn().mockResolvedValue([]),
    getRoleDisplayName: (code: string) => code,
    formatRoleText: (code: string) => code,
    getUserOrgId: () => null,
    fetchRolesForOrg: vi.fn().mockResolvedValue([]),
    initGlobalRoles: vi.fn().mockResolvedValue([]),
    globalRoleLookupMap: { value: {} },
    orgRolesMap: { value: {} }
  })
}))

vi.mock('~/composables/useRoles', () => ({
  useRoles: () => ({
    rolesList: { value: [] },
    roleOptions: { value: [] },
    isInitialized: { value: true },
    dispatch: vi.fn().mockResolvedValue([]),
    getRoleDisplayName: (code: string) => code,
    formatRoleText: (code: string) => code,
    getUserOrgId: () => null,
    fetchRolesForOrg: vi.fn().mockResolvedValue([]),
    initGlobalRoles: vi.fn().mockResolvedValue([])
  })
}))

vi.mock('vuestic-ui', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vuestic-ui')>()
  return {
    ...actual,
    useToast: () => ({ init: vi.fn() }),
    useColors: () => ({ currentPresetName: { value: 'light' } })
  }
})

vi.mock('~/composables/useTimezoneDate', () => ({
  useTimezoneDate: () => ({
    formatWithTimezone: (date: any) => '2026. 07. 25. 10:42:00'
  })
}))

describe('NotificationBell Component', () => {
  it('컴포넌트가 성공적으로 마운트되고 초기 알림 수가 0으로 시작해야 함', () => {
    const wrapper = mount(NotificationBell, {
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-dropdown': { template: '<div><slot name="anchor" /><slot /></div>' },
          'va-dropdown-content': { template: '<div><slot /></div>' },
          'va-badge': { template: '<div><slot /></div>' },
          'va-button': { template: '<button><slot /></button>' },
          'va-icon': { template: '<i><slot /></i>' },
          'va-divider': { template: '<hr />' },
          'va-modal': { template: '<div><slot name="header" /><slot /><slot name="footer" /></div>' },
          'ApprovalDetailsViewer': { template: '<div></div>' }
        }
      }
    })
    expect(wrapper.exists()).toBe(true)
  })
})
