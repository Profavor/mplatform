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
      btn_close: '닫기',
      inbox: {
        drag_to_resize: '드래그하여 크기 조절 (더블클릭 시 초기화)'
      }
    }
  }
})

describe('AppModal.vue (Global Standard Modal)', () => {
  const defaultGlobal = {
    plugins: [i18n],
    stubs: {
      VaModal: {
        template: '<div class="va-modal-stub"><slot /><slot name="header" /><slot name="footer" /></div>'
      },
      VaIcon: true
    }
  }

  it('renders modal title and window controls', () => {
    const wrapper = mount(AppModal, {
      props: {
        modelValue: true,
        title: '테스트 모달'
      },
      global: defaultGlobal
    })

    expect(wrapper.text()).toContain('테스트 모달')
    expect(wrapper.find('.modal-window-controls').exists()).toBe(true)
  })

  it('renders 8-directional resize handles when resizable is true', () => {
    const wrapper = mount(AppModal, {
      props: {
        modelValue: true,
        title: '테스트 모달',
        resizable: true
      },
      global: defaultGlobal
    })

    expect(wrapper.find('.handle-top').exists()).toBe(true)
    expect(wrapper.find('.handle-bottom').exists()).toBe(true)
    expect(wrapper.find('.handle-left').exists()).toBe(true)
    expect(wrapper.find('.handle-right').exists()).toBe(true)
    expect(wrapper.find('.handle-top-left').exists()).toBe(true)
    expect(wrapper.find('.handle-top-right').exists()).toBe(true)
    expect(wrapper.find('.handle-bottom-left').exists()).toBe(true)
    expect(wrapper.find('.handle-bottom-right').exists()).toBe(true)
    expect(wrapper.find('.corner-grip-lines').exists()).toBe(true)
  })

  it('handles mousedown and double click reset on bottom-right handle', async () => {
    const wrapper = mount(AppModal, {
      props: {
        modelValue: true,
        title: '테스트 모달',
        resizable: true
      },
      global: defaultGlobal
    })

    const brHandle = wrapper.find('.handle-bottom-right')
    expect(brHandle.exists()).toBe(true)

    await brHandle.trigger('mousedown', { clientX: 300, clientY: 200, preventDefault: () => {}, stopPropagation: () => {} })
    await brHandle.trigger('dblclick')
  })

  it('toggles fullscreen state when maximize button is clicked', async () => {
    const wrapper = mount(AppModal, {
      props: {
        modelValue: true,
        title: '테스트 모달'
      },
      global: defaultGlobal
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
      global: defaultGlobal
    })

    const closeBtn = wrapper.find('.modal-control-btn.btn-close')
    await closeBtn.trigger('click')
    expect(wrapper.emitted('update:modelValue')![0]).toEqual([false])
  })
})
