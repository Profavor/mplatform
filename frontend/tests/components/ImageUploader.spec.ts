import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import ImageUploader from '../../components/common/ImageUploader.vue'

const i18n = createI18n({
  legacy: false,
  locale: 'ko',
  messages: {
    ko: {
      upload_image: '이미지 업로드',
      drag_drop_image_hint: '이미지를 드래그하여 놓거나 클릭하여 업로드하세요 (Ctrl+V 붙여넣기 지원)',
      preview_image: '이미지 미리보기',
      delete_image: '이미지 삭제',
      download_image: '이미지 다운로드',
      no_image: '등록된 이미지가 없습니다.',
      image_count: '{count}개의 이미지',
      image_carousel_prev: '이전 이미지',
      image_carousel_next: '다음 이미지',
      uploading_image: '이미지 업로드 중...'
    }
  }
})

describe('ImageUploader.vue (TDD Component Test)', () => {
  it('renders single image thumbnail properly', async () => {
    const wrapper = mount(ImageUploader, {
      props: {
        modelValue: '/api/files/download/test-img-1.png',
        multiple: false
      },
      global: {
        plugins: [i18n],
        stubs: {
          VaIcon: true,
          VaButton: true,
          VaProgressCircle: true,
          VaModal: true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    const imgs = wrapper.findAll('.thumbnail-img')
    expect(imgs.length).toBe(1)
    expect(imgs[0].attributes('src')).toBeDefined()
  })

  it('renders multi-image gallery thumbnails properly with multiple=true', async () => {
    const wrapper = mount(ImageUploader, {
      props: {
        modelValue: [
          '/api/files/download/img-1.png',
          '/api/files/download/img-2.jpg',
          '/api/files/download/img-3.webp'
        ],
        multiple: true
      },
      global: {
        plugins: [i18n],
        stubs: {
          VaIcon: true,
          VaButton: true,
          VaProgressCircle: true,
          VaModal: true
        }
      }
    })

    const cards = wrapper.findAll('.image-thumbnail-card')
    expect(cards.length).toBe(3)
    const badges = wrapper.findAll('.thumbnail-badge')
    expect(badges.length).toBe(3)
  })

  it('hides dropzone and shows empty state when readonly and no images', async () => {
    const wrapper = mount(ImageUploader, {
      props: {
        modelValue: null,
        readonly: true
      },
      global: {
        plugins: [i18n],
        stubs: {
          VaIcon: true,
          VaButton: true,
          VaProgressCircle: true,
          VaModal: true
        }
      }
    })

    expect(wrapper.find('.image-dropzone').exists()).toBe(false)
    expect(wrapper.find('.empty-images-state').exists()).toBe(true)
  })
})
