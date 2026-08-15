import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import CreateUserModal from '../../components/admin/CreateUserModal.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('CreateUserModal.vue (TDD Component Test)', () => {
  const createMockNewUser = () => ({
    username: 'newuser01',
    role: 'ROLE_USER',
    organizationId: 'org-1',
    departmentId: 'dept-1'
  })

  const mockOrganizations = [
    { id: 'org-1', name: '본사', displayName: { ko: '본사', en: 'HQ' } }
  ]

  const mockDepartments = [
    { id: 'dept-1', name: '개발팀' }
  ]

  it('사용자 생성 모달 기본 렌더링 및 폼 데이터 바인딩 검증', async () => {
    const newUser = createMockNewUser()
    const wrapper = mount(CreateUserModal, {
      props: {
        modelValue: true,
        newUser: newUser,
        organizations: mockOrganizations,
        departments: mockDepartments,
        isUsernameChecked: true,
        checkedUsername: 'newuser01',
        isCheckingUsername: false,
        isCreatingUser: false
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /></div>'
          },
          'va-input': true,
          'va-select': true,
          UserRoleSelect: true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.find('.va-modal-stub').exists()).toBe(true)
  })

  it('중복확인 및 사용자 생성 이벤트 방출 검증', async () => {
    const newUser = createMockNewUser()
    const wrapper = mount(CreateUserModal, {
      props: {
        modelValue: true,
        newUser: newUser,
        organizations: mockOrganizations,
        departments: mockDepartments,
        isUsernameChecked: false,
        checkedUsername: '',
        isCheckingUsername: false,
        isCreatingUser: false
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /></div>'
          },
          'va-input': true,
          'va-select': true,
          UserRoleSelect: true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    wrapper.vm.onCheckDuplicate()
    expect(wrapper.emitted('check-username')).toBeTruthy()

    wrapper.vm.onCreate()
    expect(wrapper.emitted('create')).toBeTruthy()
  })

  it('조직 선택 시 이메일 도메인 힌트가 올바르게 계산되는지 검증', async () => {
    const orgWithDomain = [
      { id: 'org-1', name: '본사', emailDomain: 'profavor.com' }
    ]
    const newUser = {
      username: 'newuser01',
      email: '',
      role: 'ROLE_USER',
      organizationId: 'org-1',
      departmentId: 'dept-1'
    }

    const wrapper = mount(CreateUserModal, {
      props: {
        modelValue: true,
        newUser: newUser,
        organizations: orgWithDomain,
        departments: mockDepartments,
        isUsernameChecked: true,
        checkedUsername: 'newuser01',
        isCheckingUsername: false,
        isCreatingUser: false
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /></div>'
          },
          'va-input': true,
          'va-select': true,
          UserRoleSelect: true,
          'va-button': true
        }
      }
    })

    expect((wrapper.vm as any).selectedOrgEmailDomain).toBe('profavor.com')
  })
})
