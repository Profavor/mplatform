import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import ClassificationTree from '../../components/ClassificationTree.vue'

const i18n = createI18n({
  legacy: false,
  locale: 'ko',
  messages: {
    ko: {
      axis: {
        select_axis: '분류 축 선택',
        primary_tree: '주 분류체계'
      },
      empty_tree: '트리 데이터가 없습니다.'
    }
  }
})

// Mock custom fetch & cookie
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: vi.fn().mockResolvedValue([])
  })
}))

vi.stubGlobal('useCookie', () => ({
  value: 'ko'
}))

describe('ClassificationTree Scroll Container (TDD)', () => {
  it('트리 컴포넌트의 루트는 classification-tree-root 클래스와 height: 100%, min-height: 0을 가져야 한다', () => {
    const wrapper = mount(ClassificationTree, {
      global: {
        plugins: [i18n],
        stubs: {
          'va-select': true,
          'va-icon': true,
          'SchemaTreeNode': true
        }
      },
      props: {
        emptyMessage: '데이터 없음'
      }
    })

    const root = wrapper.find('.classification-tree-root')
    expect(root.exists()).toBe(true)
    const style = root.attributes('style') || ''
    expect(style).toContain('height: 100%')
    expect(style).toContain('min-height: 0')
  })

  it('트리 내부 스크롤 컨테이너는 tree-scroll-container 및 custom-scrollbar 클래스를 가지고 overflow-y: auto 속성을 가져야 한다', () => {
    const wrapper = mount(ClassificationTree, {
      global: {
        plugins: [i18n],
        stubs: {
          'va-select': true,
          'va-icon': true,
          'SchemaTreeNode': true
        }
      },
      props: {
        emptyMessage: '데이터 없음'
      }
    })

    const scrollContainer = wrapper.find('.tree-scroll-container')
    expect(scrollContainer.exists()).toBe(true)
    expect(scrollContainer.classes()).toContain('custom-scrollbar')
    const style = scrollContainer.attributes('style') || ''
    expect(style).toContain('overflow-y: auto')
    expect(style).toContain('min-height: 0')
  })
})
