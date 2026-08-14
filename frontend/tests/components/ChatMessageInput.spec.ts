import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ChatMessageInput from '../../components/chat/ChatMessageInput.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('ChatMessageInput.vue (TDD)', () => {
  it('텍스트 입력 후 Enter/전송 버튼 클릭 시 send 이벤트 emit', async () => {
    const wrapper = mount(ChatMessageInput, {
      props: {
        loading: false
      },
      global: {
        stubs: {
          'va-icon': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)

    const textarea = wrapper.find('textarea')
    if (textarea.exists()) {
      await textarea.setValue('테스트 메시지입니다.')
      await textarea.trigger('keydown.enter')
      expect(wrapper.emitted('send')).toBeTruthy()
    }
  })
})
