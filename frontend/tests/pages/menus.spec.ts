import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import MenusPage from '../../pages/admin/menus.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    te: () => true,
    locale: { value: 'ko' }
  })
}))

vi.mock('~/composables/useMenu', () => ({
  useMenu: () => ({
    menus: { value: [] },
    fetchMenus: vi.fn().mockResolvedValue([]),
    refreshMenus: vi.fn()
  })
}))

vi.mock('~/composables/usePageTitle', () => ({
  usePageTitle: () => ({
    pageTitle: { value: '메뉴 관리' }
  })
}))

vi.mock('~/composables/usePermission', () => ({
  usePermission: () => ({
    hasPermission: () => true
  })
}))

vi.mock('vuestic-ui', async (importOriginal) => {
  const actual: any = await importOriginal()
  return {
    ...actual,
    useToast: () => ({ init: vi.fn() })
  }
})

vi.mock('#app', () => ({
  useCookie: () => ({ value: 'test-token' })
}))

describe('pages/admin/menus.vue (TDD)', () => {
  it('메뉴 관리 화면 렌더링 검증', async () => {
    const wrapper = mount(MenusPage, {
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-icon': true,
          'va-badge': true,
          'va-button': true,
          'va-card': true,
          'va-card-content': true,
          'va-tree-view': true,
          'va-modal': true,
          'va-input': true,
          'va-switch': true,
          MultilingualInput: true,
          UserRoleSelect: true,
          IconPicker: true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })
})
