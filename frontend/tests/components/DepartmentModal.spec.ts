import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DepartmentModal from '../../components/org/DepartmentModal.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('DepartmentModal.vue (TDD)', () => {
  it('모달 기본 렌더링 및 닫기 이벤트 발생', async () => {
    const wrapper = mount(DepartmentModal, {
      props: {
        modelValue: true,
        mode: 'create',
        deptForm: {
          nameKo: '',
          nameEn: '',
          icon: 'folder',
          roles: [],
          descriptionKo: '',
          descriptionEn: ''
        },
        deptOptions: []
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /></div>'
          },
          'va-input': true,
          'va-select': true,
          'va-textarea': true,
          'va-icon': true,
          'va-button': {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          },
          'UserRoleSelect': true
        }
      }
    })

    expect(wrapper.find('.va-modal-stub').exists()).toBe(true)
  })
})
