import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import SystemLogs from '../../../pages/admin/system-logs.vue'
import { AgGridVue } from 'ag-grid-vue3'

import { ref } from 'vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key: string) => key, locale: ref('ko') })
}))

vi.mock('#app', () => ({
  useCookie: () => ({ value: 'fake-token' })
}))

vi.mock('vuestic-ui', async (importOriginal) => {
  const actual: any = await importOriginal()
  return {
    ...actual,
    useToast: () => ({ init: vi.fn() }),
    useColors: () => ({ currentPresetName: { value: 'light' } })
  }
})

vi.mock('~/composables/usePageTitle', () => ({
  usePageTitle: () => ({ pageTitle: { value: 'Logs' } })
}))

vi.mock('~/composables/useAgGridTheme', () => ({
  useAgGridTheme: () => ({ gridTheme: 'legacy', autoSizeStrategy: null, isDark: { value: false } })
}))

vi.mock('~/composables/usePermission', () => ({
  usePermission: () => ({ hasPermission: () => true })
}))

vi.mock('~/composables/useTimezoneDate', () => ({
  formatWithTimezone: (date: string) => date
}))

vi.mock('~/utils/formatters', () => ({
  formatEntityId: (id: string, prefix: string) => `${prefix}-${id}`
}))

vi.mock('~/utils/multilingual', () => ({
  getMultilingualText: (val: any) => val
}))

vi.mock('~/stores/useCodeStore', () => ({
  useCodeStore: () => ({
    loadGroup: vi.fn().mockResolvedValue([])
  })
}))

const mockFetch = vi.fn()
global.$fetch = mockFetch as any

const AgGridVueStub = {
  name: 'AgGridVue',
  props: ['gridOptions', 'rowModelType', 'datasource', 'columnDefs', 'theme'],
  emits: ['grid-ready'],
  template: '<div class="ag-grid-stub" />'
}

describe('System Logs Page with AG-Grid Community Infinite Model', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockFetch.mockImplementation(async (url) => {
      if (url.includes('/api/menus/logs')) {
        return {
          content: [
            { id: '1', menuId: 'm1', accessedAt: '2026-09-08T00:00:00Z', userId: 'u1', username: 'admin', ip: '127.0.0.1' }
          ],
          totalElements: 1
        }
      }
      if (url.includes('/api/auth/login-logs')) {
        return { content: [], totalElements: 0 }
      }
      if (url.includes('/api/menus')) {
        return []
      }
      if (url.includes('channels')) {
        return []
      }
      return { content: [], totalElements: 0 }
    })
  })

  it('renders AG-Grid with rowModelType infinite on Audit Logs tab', async () => {
    const mockGridApi = {
      setGridOption: vi.fn(),
      purgeInfiniteCache: vi.fn(),
      refreshInfiniteCache: vi.fn()
    }

    const wrapper = mount(SystemLogs, {
      global: {
        mocks: { $t: (key: string) => key },
        stubs: {
          'va-card': { template: '<div><slot /><slot name="actions" /></div>' },
          'va-card-title': { template: '<div><slot /></div>' },
          'va-card-content': { template: '<div><slot /></div>' },
          'va-tabs': { template: '<div><slot /></div>' },
          'va-tab': { template: '<div><slot /></div>' },
          'va-button': { template: '<button><slot /></button>' },
          'va-button-toggle': true,
          'va-icon': true,
          'va-badge': true,
          'va-select': true,
          'va-input': true,
          'va-switch': true,
          'va-modal': true,
          VaModal: true,
          AppModal: true,
          RegulatoryComplianceModal: true,
          ColdStorageArchiveModal: true,
          'v-chart': true,
          'client-only': { template: '<div><slot /></div>' },
          AgGridVue: AgGridVueStub,
          'ag-grid-vue': AgGridVueStub
        }
      }
    })

    await flushPromises()

    expect(wrapper.exists()).toBe(true)

    // AgGridVue component inspection
    const agGrid = wrapper.findComponent(AgGridVueStub)
    expect(agGrid.exists()).toBe(true)
    expect(agGrid.props('rowModelType')).toBe('infinite')

    // Trigger grid-ready
    agGrid.vm.$emit('grid-ready', { api: mockGridApi })

    // Verify datasource getRows
    const datasource = agGrid.props('datasource')
    expect(datasource).toBeDefined()
    expect(typeof datasource.getRows).toBe('function')

    const successCallback = vi.fn()
    const failCallback = vi.fn()
    await datasource.getRows({
      startRow: 0,
      endRow: 20,
      successCallback,
      failCallback
    })

    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining('/api/menus/logs'),
      expect.any(Object)
    )
    expect(successCallback).toHaveBeenCalledWith(
      expect.arrayContaining([expect.objectContaining({ id: '1' })]),
      1
    )
  })

  it('renders Error Logs tab and verifies timezone-aware formatting and resilient pagination', async () => {
    mockFetch.mockImplementation(async (url) => {
      if (url.includes('/api/admin/error-logs')) {
        return {
          content: [
            { id: 101, userId: 'usr-1', requestUri: '/api/test', errorMessage: 'NullPointerException', loggedAt: '2026-09-14T09:30:00Z' }
          ],
          totalElements: 50
        }
      }
      return { content: [], totalElements: 0 }
    })

    const wrapper = mount(SystemLogs, {
      global: {
        mocks: { $t: (key: string) => key },
        stubs: {
          'va-card': { template: '<div><slot /><slot name="actions" /></div>' },
          'va-card-title': { template: '<div><slot /></div>' },
          'va-card-content': { template: '<div><slot /></div>' },
          'va-tabs': { template: '<div><slot /></div>' },
          'va-tab': { template: '<div><slot /></div>' },
          'va-button': { template: '<button><slot /></button>' },
          'va-button-toggle': true,
          'va-icon': true,
          'va-badge': true,
          'va-select': true,
          'va-input': true,
          'va-switch': true,
          'va-modal': true,
          VaModal: true,
          AppModal: true,
          RegulatoryComplianceModal: true,
          ColdStorageArchiveModal: true,
          'v-chart': true,
          'client-only': { template: '<div><slot /></div>' },
          AgGridVue: AgGridVueStub,
          'ag-grid-vue': AgGridVueStub
        }
      }
    })

    await flushPromises()
    ;(wrapper.vm as any).activeTab = 'error'
    await flushPromises()

    const agGrids = wrapper.findAllComponents(AgGridVueStub)
    expect(agGrids.length).toBeGreaterThan(0)
    const errorGrid = agGrids[0]!

    // Verify errorColumnDefs has loggedAt formatted with timezone
    const colDefs = errorGrid.props('columnDefs')
    const loggedAtCol = colDefs.find((c: any) => c.field === 'loggedAt')
    expect(loggedAtCol).toBeDefined()
    expect(typeof loggedAtCol.valueFormatter).toBe('function')
    expect(loggedAtCol.valueFormatter({ value: '2026-09-14T09:30:00Z' })).toBe('2026-09-14T09:30:00Z')

    // Verify datasource getRows
    const datasource = errorGrid.props('datasource')
    const successCallback = vi.fn()
    const failCallback = vi.fn()

    await datasource.getRows({
      startRow: 0,
      endRow: 20,
      successCallback,
      failCallback
    })

    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining('/api/admin/error-logs'),
      expect.objectContaining({ params: { page: 0, size: 20 } })
    )
    expect(successCallback).toHaveBeenCalledWith(
      expect.arrayContaining([expect.objectContaining({ id: 101, errorMessage: 'NullPointerException' })]),
      50
    )

    // Test resilience when backend returns a raw array
    mockFetch.mockImplementationOnce(async () => [
      { id: 102, userId: 'usr-2', requestUri: '/api/fail', errorMessage: 'DB Timeout', loggedAt: '2026-09-14T10:00:00Z' }
    ])
    const successCallbackRaw = vi.fn()
    await datasource.getRows({
      startRow: 0,
      endRow: 20,
      successCallback: successCallbackRaw,
      failCallback: vi.fn()
    })
    expect(successCallbackRaw).toHaveBeenCalledWith(
      expect.arrayContaining([expect.objectContaining({ id: 102 })]),
      1
    )
  })

  it('renders Login Logs tab and verifies User UUID formatting and pagination', async () => {
    mockFetch.mockImplementation(async (url) => {
      if (url.includes('/api/auth/login-logs')) {
        return {
          content: [
            { id: 'log-1', userId: 'usr-abc', username: 'superadmin', clientIp: '192.168.1.1', userAgent: 'Mozilla', loginAt: '2026-09-14T09:00:00Z' }
          ],
          totalElements: 1
        }
      }
      return { content: [], totalElements: 0 }
    })

    const wrapper = mount(SystemLogs, {
      global: {
        mocks: { $t: (key: string) => key },
        stubs: {
          'va-card': { template: '<div><slot /><slot name="actions" /></div>' },
          'va-card-title': { template: '<div><slot /></div>' },
          'va-card-content': { template: '<div><slot /></div>' },
          'va-tabs': { template: '<div><slot /></div>' },
          'va-tab': { template: '<div><slot /></div>' },
          'va-button': { template: '<button><slot /></button>' },
          'va-button-toggle': true,
          'va-icon': true,
          'va-badge': true,
          'va-select': true,
          'va-input': true,
          'va-switch': true,
          'va-modal': true,
          VaModal: true,
          AppModal: true,
          RegulatoryComplianceModal: true,
          ColdStorageArchiveModal: true,
          'v-chart': true,
          'client-only': { template: '<div><slot /></div>' },
          AgGridVue: AgGridVueStub,
          'ag-grid-vue': AgGridVueStub
        }
      }
    })

    await flushPromises()
    ;(wrapper.vm as any).activeTab = 'login'
    await flushPromises()

    const agGrids = wrapper.findAllComponents(AgGridVueStub)
    expect(agGrids.length).toBeGreaterThan(0)
    const loginGrid = agGrids[0]!

    // Verify userId formatter produces USR- prefix
    const colDefs = loginGrid.props('columnDefs')
    const userIdCol = colDefs.find((c: any) => c.field === 'userId')
    expect(userIdCol).toBeDefined()
    expect(userIdCol.valueFormatter({ value: 'usr-abc' })).toBe('USR-usr-abc')

    // Verify datasource getRows
    const datasource = loginGrid.props('datasource')
    const successCallback = vi.fn()
    await datasource.getRows({
      startRow: 0,
      endRow: 20,
      successCallback,
      failCallback: vi.fn()
    })

    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining('/api/auth/login-logs'),
      expect.objectContaining({ params: { page: 0, size: 20 } })
    )
    expect(successCallback).toHaveBeenCalledWith(
      expect.arrayContaining([expect.objectContaining({ username: 'superadmin', userId: 'usr-abc' })]),
      1
    )
  })
})
