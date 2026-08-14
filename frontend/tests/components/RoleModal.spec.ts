import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import RoleModal from '../../components/org/RoleModal.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('RoleModal.vue (TDD)', () => {
  it('역할 등록 모달 기본 렌더링', async () => {
    const wrapper = mount(RoleModal, {
      props: {
        modelValue: true,
        mode: 'create',
        roleForm: {
          name: '',
          displayNameKo: '',
          displayNameEn: '',
          descriptionKo: '',
          descriptionEn: '',
          permissions: []
        }
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /></div>'
          },
          'va-input': true,
          'va-textarea': true,
          'va-button': {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.find('.va-modal-stub').exists()).toBe(true)
  })
})
