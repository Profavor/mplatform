import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import MenuEditForm from '../../components/admin/MenuEditForm.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('MenuEditForm.vue (TDD Component Test)', () => {
  const createMockMenu = () => ({
    id: 1,
    path: '/admin/users',
    icon: 'people',
    isActive: true
  })

  it('메뉴 미선택 시 Empty 안내 메시지 표출 검증', () => {
    const wrapper = mount(MenuEditForm, {
      props: {
        selectedMenu: null,
        selectedMenuNameKo: '',
        selectedMenuNameEn: '',
        selectedMenuRoles: [],
        selectedMenuHasChildren: false
      },
      global: {
        stubs: {
          'va-icon': true
        }
      }
    })

    expect(wrapper.text()).toContain('select_menu_prompt')
  })

  it('메뉴 선택 시 폼 바인딩, 아이콘 선택기 트리거 및 저장 이벤트 방출 검증', async () => {
    const mockMenu = createMockMenu()
    const wrapper = mount(MenuEditForm, {
      props: {
        selectedMenu: mockMenu,
        selectedMenuNameKo: '사용자 관리',
        selectedMenuNameEn: 'User Management',
        selectedMenuRoles: ['ROLE_ADMIN'],
        selectedMenuHasChildren: true
      },
      global: {
        stubs: {
          MultilingualInput: true,
          'va-input': true,
          'va-icon': true,
          'va-button': true,
          'va-switch': true,
          UserRoleSelect: true
        }
      }
    })

    expect(wrapper.text()).toContain('edit_menu')
    expect(wrapper.text()).toContain('menu_children_role_union_notice')

    wrapper.vm.onOpenIconPicker()
    expect(wrapper.emitted('open-icon-picker')).toBeTruthy()

    wrapper.vm.onSave()
    expect(wrapper.emitted('save')).toBeTruthy()
  })
})
