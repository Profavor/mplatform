import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import HtmlEditor from '../../components/common/HtmlEditor.vue'

const i18n = createI18n({
  legacy: false,
  locale: 'ko',
  messages: {
    ko: {
      editor_bold: '굵게',
      editor_italic: '기울임',
      editor_underline: '밑줄',
      editor_strike: '취소선',
      editor_heading1: '제목 1 (H1)',
      editor_heading2: '제목 2 (H2)',
      editor_heading3: '제목 3 (H3)',
      editor_paragraph: '본문',
      editor_bullet_list: '글머리 기호 목록',
      editor_ordered_list: '번호 매기기 목록',
      editor_blockquote: '인용구',
      editor_code_block: '코드 블록',
      editor_align_left: '왼쪽 정렬',
      editor_align_center: '가운데 정렬',
      editor_align_right: '오른쪽 정렬',
      editor_align_justify: '양쪽 맞춤',
      editor_horizontal_rule: '구분선',
      editor_undo: '실행 취소',
      editor_redo: '다시 실행',
      editor_placeholder: '내용을 입력하세요...',
      editor_image: '이미지 삽입',
      uploading_image: '이미지 업로드 중...',
      failed_upload_image: '이미지 업로드에 실패했습니다.',
      editor_font_family: '글꼴',
      editor_font_size: '글자 크기',
      editor_text_color: '글자 색상',
      editor_highlight: '형광펜 배경색',
      editor_table: '표',
      editor_insert_table: '표 삽입 (3x3)',
      editor_task_list: '체크리스트 (To-Do)',
      editor_clear_formatting: '서식 지우기',
      editor_fullscreen: '전체화면',
      editor_exit_fullscreen: '전체화면 종료',
      editor_link: '링크 삽입',
      editor_unlink: '링크 제거',
      editor_character_count: '{count}자',
      editor_copy_code: '코드 복사',
      editor_code_copied: '코드가 복사되었습니다.'
    }
  }
})

describe('HtmlEditor.vue (TDD Component Test)', () => {
  it('renders editor and toolbar properly with initial modelValue', async () => {
    const wrapper = mount(HtmlEditor, {
      props: {
        modelValue: '<p>Hello <strong>World</strong></p>',
        placeholder: '테스트 에디터'
      },
      global: {
        plugins: [i18n],
        stubs: {
          VaIcon: true,
          VaButton: true,
          VaProgressCircle: true,
          ImageLightboxModal: true,
          AppModal: true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.html-editor-container').exists()).toBe(true)
    expect(wrapper.find('.html-editor-toolbar').exists()).toBe(true)
    expect(wrapper.find('input[type="file"]').exists()).toBe(true)
    expect(wrapper.find('.font-family-select').exists()).toBe(true)
    expect(wrapper.find('.font-size-select').exists()).toBe(true)
    wrapper.unmount()
  })

  it('hides toolbar and applies readonly class when disabled or readonly is true', async () => {
    const wrapper = mount(HtmlEditor, {
      props: {
        modelValue: '<p>Readonly Content</p>',
        readonly: true
      },
      global: {
        plugins: [i18n],
        stubs: {
          VaIcon: true,
          VaButton: true,
          VaProgressCircle: true,
          ImageLightboxModal: true,
          AppModal: true
        }
      }
    })

    expect(wrapper.find('.html-editor-toolbar').exists()).toBe(false)
    expect(wrapper.find('.is-readonly').exists()).toBe(true)
    wrapper.unmount()
  })

  it('contains essential formatting, color, table and image buttons in toolbar', async () => {
    const wrapper = mount(HtmlEditor, {
      props: {
        modelValue: '<p>Formatting test</p>'
      },
      global: {
        plugins: [i18n],
        stubs: {
          VaIcon: true,
          VaButton: true,
          VaProgressCircle: true,
          ImageLightboxModal: true,
          AppModal: true
        }
      }
    })

    const buttons = wrapper.findAll('.editor-btn')
    expect(buttons.length).toBeGreaterThan(12)
    const imageBtn = buttons.find(b => b.text().includes('🖼️'))
    expect(imageBtn).toBeDefined()
    const tableBtn = buttons.find(b => b.text().includes('⊞'))
    expect(tableBtn).toBeDefined()
    wrapper.unmount()
  })
})
