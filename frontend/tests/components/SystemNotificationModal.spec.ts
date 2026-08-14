import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import SystemNotificationModal from '../../components/common/SystemNotificationModal.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('SystemNotificationModal.vue (TDD Component Test)', () => {
  it('성공 상태 렌더링 및 닫기 이벤트 방출 검증', async () => {
    const wrapper = mount(SystemNotificationModal, {
      props: {
        modelValue: true,
        type: 'success',
        title: '알림',
        header: '저장 완료',
        message: '성공적으로 저장되었습니다.'
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot name="default" /><slot /></div>'
          },
          'va-icon': true,
          'va-button': {
            template: '<button class="va-button-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.text()).toContain('저장 완료')
    expect(wrapper.text()).toContain('성공적으로 저장되었습니다.')

    const button = wrapper.find('.va-button-stub')
    await button.trigger('click')

    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0]).toEqual([false])
  })

  it('경고 및 에러 상태 렌더링 검증', () => {
    const wrapper = mount(SystemNotificationModal, {
      props: {
        modelValue: true,
        type: 'warning',
        title: '경고',
        header: '유효성 오류',
        message: '입력값을 확인해주세요.'
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot name="default" /><slot /></div>'
          },
          'va-icon': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.text()).toContain('유효성 오류')
    expect(wrapper.text()).toContain('입력값을 확인해주세요.')
  })
})
