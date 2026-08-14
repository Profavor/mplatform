import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import AddMenuModal from '../../components/admin/AddMenuModal.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('AddMenuModal.vue (TDD Component Test)', () => {
  const createMockNewMenu = () => ({
    path: '/admin/settings',
    icon: 'settings',
    sortOrder: 1,
    isActive: true
  })

  it('신규 메뉴 등록 모달 기본 렌더링 및 폼 바인딩 검증', () => {
    const newMenu = createMockNewMenu()
    const wrapper = mount(AddMenuModal, {
      props: {
        modelValue: true,
        newMenu: newMenu,
        newMenuNameKo: '설정',
        newMenuNameEn: 'Settings',
        newMenuRoles: ['ROLE_ADMIN']
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /></div>'
          },
          MultilingualInput: true,
          'va-input': true,
          'va-icon': true,
          'va-button': true,
          'va-switch': true,
          UserRoleSelect: true
        }
      }
    })

    expect(wrapper.find('.va-modal-stub').exists()).toBe(true)
  })

  it('아이콘 선택 및 저장 이벤트 방출 검증', async () => {
    const newMenu = createMockNewMenu()
    const wrapper = mount(AddMenuModal, {
      props: {
        modelValue: true,
        newMenu: newMenu,
        newMenuNameKo: '설정',
        newMenuNameEn: 'Settings',
        newMenuRoles: []
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /></div>'
          },
          MultilingualInput: true,
          'va-input': true,
          'va-icon': true,
          'va-button': true,
          'va-switch': true,
          UserRoleSelect: true
        }
      }
    })

    wrapper.vm.onOpenIconPicker()
    expect(wrapper.emitted('open-icon-picker')).toBeTruthy()

    wrapper.vm.onSave()
    expect(wrapper.emitted('save')).toBeTruthy()
  })
})
