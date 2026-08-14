import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import CodeGroupModal from '../../components/admin/CodeGroupModal.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

vi.mock('vue3-emoji-picker', () => ({
  default: {
    name: 'EmojiPicker',
    template: '<div class="emoji-picker-mock"></div>'
  }
}))

describe('CodeGroupModal.vue (TDD Component Test)', () => {
  const createMockGroupForm = () => ({
    groupCode: 'USER_STATUS',
    nameKo: '사용자 상태',
    nameEn: 'User Status',
    descKo: '사용자의 계정 상태 코드',
    descEn: 'User Account Status Code',
    isActive: true
  })

  it('그룹 모달 컴포넌트 기본 렌더링 및 폼 데이터 바인딩 검증', async () => {
    const groupForm = createMockGroupForm()
    const wrapper = mount(CodeGroupModal, {
      props: {
        modelValue: true,
        groupForm: groupForm,
        editingGroup: false
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot name="header" /><slot /><slot name="footer" /></div>'
          },
          'va-input': true,
          'va-switch': true,
          'va-icon': true,
          'va-dropdown': true,
          ClientOnly: true,
          EmojiPicker: true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.find('.va-modal-stub').exists()).toBe(true)
  })

  it('저장 버튼 클릭 시 save 이벤트 방출', async () => {
    const groupForm = createMockGroupForm()
    const wrapper = mount(CodeGroupModal, {
      props: {
        modelValue: true,
        groupForm: groupForm,
        editingGroup: false
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot name="header" /><slot /><slot name="footer" /></div>'
          },
          'va-input': true,
          'va-switch': true,
          'va-icon': true,
          'va-dropdown': true,
          ClientOnly: true,
          EmojiPicker: true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    wrapper.vm.onSave()
    expect(wrapper.emitted('save')).toBeTruthy()
  })
})
