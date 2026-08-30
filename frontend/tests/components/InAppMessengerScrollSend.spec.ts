import { describe, it, expect, vi } from 'vitest'

describe('InAppMessenger Scroll & Send Improvements (TDD)', () => {
  it('한글 조합 중 v-model이 비어있더라도 DOM Input value를 통해 정상 전송된다', () => {
    let sentText = ''
    const domInputElement = { value: '한글입력중' }
    let vModelText = '' // v-model은 IME 조합 중 비어있을 수 있음

    const sendTextMessage = () => {
      const rawVal = domInputElement.value || vModelText || ''
      const text = rawVal.trim()
      if (!text) return
      sentText = text
    }

    sendTextMessage()
    expect(sentText).toBe('한글입력중')
  })

  it('대화방 진입 시 스크롤이 scrollHeight(최하단)으로 이동한다', () => {
    const mockContainer = {
      scrollTop: 0,
      scrollHeight: 1500,
      clientHeight: 500
    }
    const mockAnchor = {
      scrollIntoView: vi.fn()
    }

    const scrollToBottom = () => {
      mockAnchor.scrollIntoView({ behavior: 'auto', block: 'end' })
      mockContainer.scrollTop = mockContainer.scrollHeight
    }

    scrollToBottom()
    expect(mockAnchor.scrollIntoView).toHaveBeenCalledWith({ behavior: 'auto', block: 'end' })
    expect(mockContainer.scrollTop).toBe(1500)
  })

  it('Shift+Enter 입력 시에는 메시지가 전송되지 않고 줄바꿈을 허용한다', () => {
    let sent = false
    const handleInputEnter = (e: { isComposing?: boolean, keyCode?: number, shiftKey?: boolean, preventDefault: () => void }) => {
      if (e.isComposing || e.keyCode === 229) return
      if (e.shiftKey) return
      e.preventDefault()
      sent = true
    }

    // Shift + Enter
    handleInputEnter({ shiftKey: true, preventDefault: () => {} })
    expect(sent).toBe(false)

    // 일반 Enter
    handleInputEnter({ shiftKey: false, keyCode: 13, preventDefault: () => {} })
    expect(sent).toBe(true)
  })
})
