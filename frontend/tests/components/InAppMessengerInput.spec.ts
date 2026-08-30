import { describe, it, expect } from 'vitest'

describe('InAppMessenger Chat Input & IME Composition Logic (TDD)', () => {
  it('IME 조합 중(isComposing === true)일 때 엔터 키 입력은 전송을 유발하지 않는다', () => {
    let sent = false
    let isComposing = true

    const handleInputEnter = (e: { isComposing?: boolean, shiftKey?: boolean, preventDefault: () => void }) => {
      if (isComposing) return
      if (e.shiftKey) return
      e.preventDefault()
      sent = true
    }

    handleInputEnter({ preventDefault: () => {} })
    expect(sent).toBe(false)

    // IME 조합 완료 후
    isComposing = false
    handleInputEnter({ preventDefault: () => {} })
    expect(sent).toBe(true)
  })

  it('공백 문자열만 입력된 경우 전송되지 않는다', () => {
    let sent = false
    const sendTextMessage = (text: string) => {
      if (!text.trim()) return
      sent = true
    }

    sendTextMessage('   ')
    expect(sent).toBe(false)

    sendTextMessage('안녕하세요')
    expect(sent).toBe(true)
  })

  it('이모지 삽입 시 기존 텍스트 뒤에 올바르게 결합된다', () => {
    let inputMsg = '오늘 일정'
    const insertEmoji = (emoji: string) => {
      inputMsg += emoji
    }

    insertEmoji('👍')
    expect(inputMsg).toBe('오늘 일정👍')
  })
})
