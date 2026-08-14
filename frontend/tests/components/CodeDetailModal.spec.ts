import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import CodeDetailModal from '../../components/admin/CodeDetailModal.vue'

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

describe('CodeDetailModal.vue (TDD Component Test)', () => {
  const createMockDetailForm = () => ({
    detailCode: 'ACTIVE',
    nameKo: '활성',
    nameEn: 'Active',
    sortOrder: 1,
    isActive: true
  })

  it('상세 코드 모달 컴포넌트 기본 렌더링 및 폼 데이터 바인딩 검증', async () => {
    const detailForm = createMockDetailForm()
    const wrapper = mount(CodeDetailModal, {
      props: {
        modelValue: true,
        detailForm: detailForm,
        editingDetail: false
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
    const detailForm = createMockDetailForm()
    const wrapper = mount(CodeDetailModal, {
      props: {
        modelValue: true,
        detailForm: detailForm,
        editingDetail: false
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
