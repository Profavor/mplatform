import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import Codes from '../pages/admin/codes.vue'
import { AgGridVue } from 'ag-grid-vue3'

// Mock dependencies
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

  it('mounts and initializes AG-Grid Server-Side Row Model', async () => {
    const wrapper = mount(Codes, {
      global: {
        stubs: {
          'va-card': true,
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
  })
})
