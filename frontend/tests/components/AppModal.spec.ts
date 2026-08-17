import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import AppModal from '../../components/common/AppModal.vue'

const i18n = createI18n({
  legacy: false,
  locale: 'ko',
  messages: {
    ko: {
      modal_maximize: '전체 화면으로 펼치기',
      modal_restore: '원래 크기로 복귀',
      btn_close: '닫기'
    }
  }
})

describe('AppModal.vue (Global Standard Modal)', () => {
  it('renders modal title and window controls', () => {
    const wrapper = mount(AppModal, {
      props: {
        modelValue: true,
        title: '테스트 모달'
      },
      global: {
        plugins: [i18n],
        stubs: {
          VaModal: {
            template: '<div class="va-modal-stub"><slot name="header" /><slot /><slot name="footer" /></div>'
          },
          VaIcon: true
        }
      }
    })

    expect(wrapper.text()).toContain('테스트 모달')
    expect(wrapper.find('.modal-window-controls').exists()).toBe(true)
  })

  it('toggles fullscreen state when maximize button is clicked', async () => {
    const wrapper = mount(AppModal, {
      props: {
        modelValue: true,
        title: '테스트 모달'
      },
      global: {
        plugins: [i18n],
        stubs: {
          VaModal: {
            template: '<div class="va-modal-stub"><slot name="header" /><slot /></div>'
          },
          VaIcon: true
        }
      }
    })

    const maxBtn = wrapper.find('.modal-control-btn.btn-maximize')
    expect(maxBtn.exists()).toBe(true)

    await maxBtn.trigger('click')
    expect(wrapper.emitted('update:fullscreen')).toBeTruthy()
  })

  it('emits update:modelValue with false when close button is clicked', async () => {
    const wrapper = mount(AppModal, {
      props: {
        modelValue: true,
        title: '테스트 모달'
      },
      global: {
        plugins: [i18n],
        stubs: {
          VaModal: {
            template: '<div class="va-modal-stub"><slot name="header" /><slot /></div>'
          },
          VaIcon: true
        }
      }
    })

    const closeBtn = wrapper.find('.modal-control-btn.btn-close')
    await closeBtn.trigger('click')
    expect(wrapper.emitted('update:modelValue')![0]).toEqual([false])
  })
})
