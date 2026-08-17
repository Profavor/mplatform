import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import ImageLightboxModal from '../../components/common/ImageLightboxModal.vue'

const i18n = createI18n({
  legacy: false,
  locale: 'ko',
  messages: {
    ko: {
      preview_image: '이미지 미리보기',
      zoom_in: '확대',
      zoom_out: '축소',
      zoom_reset: '초기화',
      download_image: '다운로드',
      fullscreen: '전체화면',
      exit_fullscreen: '전체화면 종료',
      close: '닫기',
      image_carousel_prev: '이전',
      image_carousel_next: '다음'
    }
  }
})

describe('ImageLightboxModal.vue (TDD Component Test)', () => {
  it('renders modal with images list properly', async () => {
    const wrapper = mount(ImageLightboxModal, {
      props: {
        modelValue: true,
        images: [
          '/api/files/download/hash1.png?name=test1.png',
          '/api/files/download/hash2.png?name=test2.png'
        ],
        initialIndex: 0
      },
      global: {
        plugins: [i18n],
        stubs: {
          VaIcon: true,
          VaButton: true,
          VaProgressCircle: true,
          Teleport: true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.custom-lightbox-container').exists()).toBe(true)
    expect(wrapper.find('.lightbox-main-img').exists()).toBe(true)
    expect(wrapper.findAll('.strip-thumb').length).toBe(2)
    wrapper.unmount()
  })

  it('navigates next and prev images', async () => {
    const wrapper = mount(ImageLightboxModal, {
      props: {
        modelValue: true,
        images: [
          '/api/files/download/hash1.png?name=test1.png',
          '/api/files/download/hash2.png?name=test2.png'
        ],
        initialIndex: 0
      },
      global: {
        plugins: [i18n],
        stubs: {
          VaIcon: true,
          VaButton: true,
          VaProgressCircle: true,
          Teleport: true
        }
      }
    })

    const nextBtn = wrapper.find('.next-btn')
    expect(nextBtn.exists()).toBe(true)
    await nextBtn.trigger('click')
    
    const thumbs = wrapper.findAll('.strip-thumb')
    expect(thumbs[1].classes()).toContain('is-active')
    wrapper.unmount()
  })
})

