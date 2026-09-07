import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import MediaLinkViewer from '../../components/common/MediaLinkViewer.vue'

const i18n = createI18n({
  legacy: false,
  locale: 'ko',
  messages: {
    ko: {
      media_link: '미디어 링크',
      media_link_placeholder: '이미지 또는 동영상 URL을 입력하세요 (예: YouTube, Vimeo, MP4, PNG, JPG 등)',
      media_preview: '미디어 미리보기',
      media_unsupported_or_empty: '등록된 미디어가 없습니다.',
      media_open_link: '새 탭에서 열기',
      media_copy_link: '링크 복사',
      media_type_image: '이미지',
      media_type_video: '동영상',
      media_type_audio: '오디오',
      media_type_link: '웹 링크',
      media_add_link: '미디어 링크 추가',
      media_remove_link: '삭제',
      click_to_zoom: '클릭하여 확대'
    }
  }
})

describe('MediaLinkViewer.vue (TDD Component Test)', () => {
  it('이미지 URL이 주어지면 img 태그를 렌더링한다', async () => {
    const wrapper = mount(MediaLinkViewer, {
      props: {
        modelValue: 'https://example.com/photo.jpg',
        readonly: true
      },
      global: {
        plugins: [i18n],
        stubs: {
          VaIcon: true,
          VaButton: true,
          VaInput: true,
          ImageLightboxModal: true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    const img = wrapper.find('img.media-preview-img')
    expect(img.exists()).toBe(true)
    expect(img.attributes('src')).toBe('https://example.com/photo.jpg')
  })

  it('직접 동영상 URL(MP4)이 주어지면 video 태그를 렌더링한다', async () => {
    const wrapper = mount(MediaLinkViewer, {
      props: {
        modelValue: 'https://example.com/movie.mp4',
        readonly: true
      },
      global: {
        plugins: [i18n],
        stubs: {
          VaIcon: true,
          VaButton: true,
          VaInput: true,
          ImageLightboxModal: true
        }
      }
    })

    const video = wrapper.find('video.media-preview-video')
    expect(video.exists()).toBe(true)
    expect(video.attributes('src')).toBe('https://example.com/movie.mp4')
  })

  it('유튜브 URL이 주어지면 iframe 임베드 태그를 렌더링한다', async () => {
    const wrapper = mount(MediaLinkViewer, {
      props: {
        modelValue: 'https://www.youtube.com/watch?v=dQw4w9WgXcQ',
        readonly: true
      },
      global: {
        plugins: [i18n],
        stubs: {
          VaIcon: true,
          VaButton: true,
          VaInput: true,
          ImageLightboxModal: true
        }
      }
    })

    const iframe = wrapper.find('iframe.media-preview-iframe')
    expect(iframe.exists()).toBe(true)
    expect(iframe.attributes('src')).toContain('https://www.youtube.com/embed/dQw4w9WgXcQ')
  })

  it('수정 모드(readonly=false)에서 URL 입력 시 update:modelValue 이벤트를 발생시키고 라이브 프리뷰를 표시한다', async () => {
    const wrapper = mount(MediaLinkViewer, {
      props: {
        modelValue: '',
        readonly: false
      },
      global: {
        plugins: [i18n],
        stubs: {
          VaIcon: true,
          VaButton: true,
          ImageLightboxModal: true
        }
      }
    })

    const input = wrapper.find('input.media-url-input')
    expect(input.exists()).toBe(true)

    await input.setValue('https://example.com/demo.mp4')
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')?.[0]).toEqual(['https://example.com/demo.mp4'])
  })

  it('값이 없을 때 readonly 모드에서 빈 상태 안내 메시지를 표시한다', () => {
    const wrapper = mount(MediaLinkViewer, {
      props: {
        modelValue: '',
        readonly: true
      },
      global: {
        plugins: [i18n],
        stubs: {
          VaIcon: true,
          VaButton: true,
          ImageLightboxModal: true
        }
      }
    })

    expect(wrapper.find('.empty-media-placeholder').exists()).toBe(true)
  })
})
