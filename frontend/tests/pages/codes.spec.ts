import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import Codes from '../../pages/admin/codes.vue'
import { AgGridVue } from 'ag-grid-vue3'

// Mock dependencies
vi.mock('vue3-emoji-picker', () => ({
  default: {
    template: '<div class="emoji-picker-stub"></div>'
  }
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
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
  usePageTitle: vi.fn()
}))

const mockFetch = vi.fn()
global.$fetch = mockFetch as any

describe('Codes Management Page with AG-Grid', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockFetch.mockImplementation(async (url) => {
      if (url.includes('/api/code-groups/page')) {
        return {
          content: [
            { id: '1', groupCode: 'TEST_1', name: { ko: '테스트1' }, isActive: true }
          ],
          totalElements: 1,
          totalPages: 1,
          number: 0
        }
      }
      if (url.includes('/api/code-groups')) {
        return []
      }
      return []
    })
  })

  it('mounts and initializes AG-Grid with infinite row model and datasource', async () => {
    const mockGridApi = {
      setGridOption: vi.fn(),
      purgeInfiniteCache: vi.fn(),
      refreshInfiniteCache: vi.fn()
    }

    const wrapper = mount(Codes, {
      global: {
        stubs: {
          'va-card': { template: '<div><slot /></div>' },
          'va-icon': true,
          'va-button': true,
          'va-badge': true,
          'va-modal': true,
          'va-input': true,
          'va-switch': true,
          'va-data-table': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    
    const agGrid = wrapper.findComponent(AgGridVue)
    expect(agGrid.exists()).toBe(true)
    expect(agGrid.props('rowModelType')).toBe('infinite')

    // Trigger grid-ready
    agGrid.vm.$emit('grid-ready', { api: mockGridApi })
    expect(mockGridApi.setGridOption).toHaveBeenCalledWith('datasource', expect.any(Object))

    // Test datasource getRows
    const lastCall = mockGridApi.setGridOption.mock.calls.find((c: any[]) => c[0] === 'datasource')
    const datasource = lastCall[1]
    expect(datasource).toBeDefined()

    const successCallback = vi.fn()
    const failCallback = vi.fn()
    await datasource.getRows({
      startRow: 0,
      endRow: 20,
      successCallback,
      failCallback
    })

    expect(mockFetch).toHaveBeenCalledWith(
      expect.stringContaining('/api/code-groups/page?page=0&size=20'),
      expect.any(Object)
    )
    expect(successCallback).toHaveBeenCalledWith(
      expect.arrayContaining([expect.objectContaining({ groupCode: 'TEST_1' })]),
      1
    )
  })
})
