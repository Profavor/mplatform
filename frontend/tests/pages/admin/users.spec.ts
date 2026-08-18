import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import UsersPage from '../../../pages/admin/users.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, fallback?: any) => key,
    locale: { value: 'ko' }
  })
}))

vi.mock('#app', () => ({
  useHead: vi.fn(),
  useCookie: () => ({ value: 'test-token' })
}))

vi.mock('~/composables/usePermission', () => ({
  usePermission: () => ({
    hasPermission: () => true
  })
}))

vi.mock('~/composables/usePageTitle', () => ({
  usePageTitle: () => ({
    pageTitle: { value: '사용자 및 권한 관리' }
  })
}))

// Mock global $fetch
global.$fetch = vi.fn().mockImplementation((url: string) => {
  if (url.includes('/api/permissions/users')) {
    return Promise.resolve({
      content: [
        { id: 'user-1', username: 'superadmin', email: 'admin@company.com', role: 'ROLE_ADMIN', organizationId: null, departmentId: null, isActive: true },
        { id: 'user-2', username: 'testuser', email: 'test@company.com', role: 'ROLE_USER', organizationId: null, departmentId: null, isActive: true }
      ],
      totalPages: 1
    })
  }
  if (url.includes('/api/organizations')) return Promise.resolve([])
  if (url.includes('/api/domains')) return Promise.resolve([])
  if (url.includes('/api/permissions/requests/pending')) return Promise.resolve([])
  if (url.includes('/domains')) return Promise.resolve([])
  if (url.includes('/org-history')) return Promise.resolve([])
  return Promise.resolve([])
}) as any

describe('Users Admin Page - Email Modification Feature', () => {
  it('renders user list and selects user to display email edit field', async () => {
    const wrapper = mount(UsersPage, {
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-icon': true,
          'va-badge': true,
          'va-button': true,
          'va-card': { template: '<div class="va-card-stub"><slot /></div>' },
          'va-card-title': { template: '<div class="va-card-title-stub"><slot /></div>' },
          'va-card-content': { template: '<div class="va-card-content-stub"><slot /></div>' },
          'va-list': { template: '<div class="va-list-stub"><slot /></div>' },
          'va-list-item': { template: '<div class="va-list-item-stub" @click="$emit(\'click\')"><slot /></div>' },
          'va-list-item-section': { template: '<div class="va-list-item-sec-stub"><slot /></div>' },
          'va-avatar': true,
          'va-input': {
            props: ['modelValue', 'label'],
            template: '<input class="va-input-stub" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />'
          },
          'va-select': true,
          'va-chip': true,
          'va-pagination': true,
          RoleBadge: true,
          UserRoleSelect: true,
          AppModal: true,
          CreateUserModal: true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)

    // Select a user
    const userToSelect = {
      id: 'user-1',
      username: 'superadmin',
      email: 'admin@company.com',
      role: 'ROLE_ADMIN',
      organizationId: null,
      departmentId: null
    }

    await (wrapper.vm as any).selectUser(userToSelect)

    // Verify selected user state
    expect((wrapper.vm as any).selectedUser).toBeTruthy()
    expect((wrapper.vm as any).selectedUser.username).toBe('superadmin')
    expect((wrapper.vm as any).selectedUserEmail).toBe('admin@company.com')
  })
})
