import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ChatMessageList from '../../components/chat/ChatMessageList.vue'
import type { ChatMessage } from '../../types/messenger'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

vi.mock('~/composables/useTimezoneDate', () => ({
  useTimezoneDate: () => ({
    formatWithTimezone: (date: string) => date ? '2026-08-14 10:00' : ''
  })
}))

describe('ChatMessageList.vue (TDD)', () => {
  const mockMessages: ChatMessage[] = [
    {
      id: 'msg-1',
      roomId: 'room-1',
      senderId: 'user-1',
      senderName: '홍길동',
      content: '안녕하세요!',
      createdAt: '2026-08-14T10:00:00Z',
      unreadCount: 1
    },
    {
      id: 'msg-2',
      roomId: 'room-1',
      senderId: 'my-user-id',
      senderName: '나',
      content: '반갑습니다!',
      createdAt: '2026-08-14T10:01:00Z',
      unreadCount: 0
    }
  ]

  it('메시지 목록 렌더링 및 내가 보낸 메시지 구분', async () => {
    const wrapper = mount(ChatMessageList, {
      props: {
        messages: mockMessages,
        currentUserId: 'my-user-id'
      },
      global: {
        stubs: {
          'va-icon': true,
          'va-badge': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    const text = wrapper.text()
    expect(text).toContain('안녕하세요!')
    expect(text).toContain('반갑습니다!')
  })
})
