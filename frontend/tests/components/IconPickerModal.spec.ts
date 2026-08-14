import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import IconPickerModal from '../../components/common/IconPickerModal.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('IconPickerModal.vue (TDD Component Test)', () => {
  it('모달 렌더링 및 아이콘 선택 후 확인 이벤트 방출 검증', async () => {
    const wrapper = mount(IconPickerModal, {
      props: {
        modelValue: true,
        icon: 'folder'
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot name="default" /><slot /></div>'
          },
          'IconPicker': true,
          'va-button': {
            template: '<button class="va-button-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.text()).toContain('cancel')
    expect(wrapper.text()).toContain('confirm')

    wrapper.vm.onConfirm()
    expect(wrapper.emitted('confirm')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0]).toEqual([false])
  })

  it('취소 버튼 클릭 시 모달 닫기 이벤트 방출 검증', async () => {
    const wrapper = mount(IconPickerModal, {
      props: {
        modelValue: true,
        icon: 'star'
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot name="default" /><slot /></div>'
          },
          'IconPicker': true,
          'va-button': {
            template: '<button class="va-button-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    wrapper.vm.onCancel()
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0]).toEqual([false])
  })
})
