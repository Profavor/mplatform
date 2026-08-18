import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import MailServerAdminPage from '../../../pages/admin/mail-server.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key: string) => key })
}))

vi.mock('#app', () => ({
  useHead: vi.fn(),
  useCookie: () => ({ value: '14px' })
}))

vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: vi.fn().mockImplementation((url: string) => {
      if (url.includes('/status')) {
        return Promise.resolve({ connected: true, domain: 'mplatform.com' })
      }
      if (url.includes('/accounts')) {
        return Promise.resolve({ content: [], totalElements: 0 })
      }
      return Promise.resolve({})
    })
  })
}))

vi.mock('~/stores/useUserStore', () => ({
  useUserStore: () => ({
    getUserName: (id: string, fallback: string) => fallback || id,
    fetchUserMap: vi.fn().mockResolvedValue({})
  })
}))

describe('MailServer Admin Page', () => {
  it('renders correctly with AG-Grid and controls', () => {
    const wrapper = mount(MailServerAdminPage, {
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          AgGridVue: { template: '<div class="ag-grid-stub" />' },
          VaCard: { template: '<div><slot /></div>' },
          VaCardTitle: { template: '<div><slot /></div>' },
          VaCardContent: { template: '<div><slot /></div>' },
          VaButton: { template: '<button><slot /></button>' },
          VaBadge: true,
          VaIcon: true,
          VaModal: true,
          VaInput: true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })
})
