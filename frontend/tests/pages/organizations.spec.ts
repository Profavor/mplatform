import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import OrganizationsPage from '../../pages/admin/organizations.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, params?: any) => key,
    locale: { value: 'ko' }
  })
}))

vi.mock('~/composables/usePageTitle', () => ({
  usePageTitle: () => ({
    pageTitle: { value: '조직 및 권한 관리' }
  })
}))

vi.mock('~/composables/usePermission', () => ({
  usePermission: () => ({
    hasPermission: () => true
  })
}))

vi.mock('~/stores/useRoleStore', () => ({
  useRoleStore: () => ({
    syncDefaultRoles: vi.fn().mockResolvedValue(true),
    dumpSeedFiles: vi.fn().mockResolvedValue(true)
  })
}))

vi.mock('vuestic-ui', async (importOriginal) => {
  const actual: any = await importOriginal()
  return {
    ...actual,
    useToast: () => ({ init: vi.fn() }),
    useColors: () => ({ currentPresetName: { value: 'light' } })
  }
})

vi.mock('#app', () => ({
  useCookie: () => ({ value: 'test-token' })
}))

describe('pages/admin/organizations.vue (TDD)', () => {
  it('조직 관리 화면 렌더링 검증', async () => {
    const wrapper = mount(OrganizationsPage, {
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-icon': true,
          'va-badge': true,
          'va-button': true,
          'va-card': true,
          'va-card-title': true,
          'va-card-content': true,
          'va-tabs': true,
          'va-tab': true,
          'va-input': true,
          'va-modal': true,
          DepartmentModal: true,
          RoleModal: true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })
})
