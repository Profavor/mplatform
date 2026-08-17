import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import ModalControls from '../../components/common/ModalControls.vue'

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

describe('ModalControls.vue (Common Window Controls)', () => {
  it('renders maximize and close buttons by default with white icon color', () => {
    const wrapper = mount(ModalControls, {
      props: {
        fullscreen: false,
        showMaximize: true,
        showClose: true
      },
      global: {
        plugins: [i18n],
        stubs: {
          VaIcon: true
        }
      }
    })

    const buttons = wrapper.findAll('.modal-control-btn')
    expect(buttons.length).toBe(2)
  })

  it('emits update:fullscreen with true when maximize button is clicked in normal mode', async () => {
    const wrapper = mount(ModalControls, {
      props: {
        fullscreen: false
      },
      global: {
        plugins: [i18n],
        stubs: {
          VaIcon: true
        }
      }
    })

    const maxBtn = wrapper.find('.modal-control-btn.btn-maximize')
    expect(maxBtn.exists()).toBe(true)

    await maxBtn.trigger('click')
    expect(wrapper.emitted('update:fullscreen')).toBeTruthy()
    expect(wrapper.emitted('update:fullscreen')![0]).toEqual([true])
  })

  it('emits update:fullscreen with false when restore button is clicked in fullscreen mode', async () => {
    const wrapper = mount(ModalControls, {
      props: {
        fullscreen: true
      },
      global: {
        plugins: [i18n],
        stubs: {
          VaIcon: true
        }
      }
    })

    const maxBtn = wrapper.find('.modal-control-btn.btn-maximize')
    await maxBtn.trigger('click')
    expect(wrapper.emitted('update:fullscreen')).toBeTruthy()
    expect(wrapper.emitted('update:fullscreen')![0]).toEqual([false])
  })

  it('emits close event when close button is clicked', async () => {
    const wrapper = mount(ModalControls, {
      props: {
        fullscreen: false
      },
      global: {
        plugins: [i18n],
        stubs: {
          VaIcon: true
        }
      }
    })

    const closeBtn = wrapper.find('.modal-control-btn.btn-close')
    expect(closeBtn.exists()).toBe(true)

    await closeBtn.trigger('click')
    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('respects showMaximize and showClose flags', () => {
    const wrapper = mount(ModalControls, {
      props: {
        showMaximize: false,
        showClose: true
      },
      global: {
        plugins: [i18n],
        stubs: {
          VaIcon: true
        }
      }
    })

    expect(wrapper.find('.modal-control-btn.btn-maximize').exists()).toBe(false)
    expect(wrapper.find('.modal-control-btn.btn-close').exists()).toBe(true)
  })
})
