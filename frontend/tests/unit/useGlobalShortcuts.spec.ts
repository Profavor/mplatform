import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useGlobalShortcuts } from '~/composables/useGlobalShortcuts'

describe('useGlobalShortcuts Composable (TDD)', () => {
  let listeners: Record<string, Function> = {}

  beforeEach(() => {
    listeners = {}
    vi.spyOn(window, 'addEventListener').mockImplementation((event, handler) => {
      listeners[event] = handler as Function
    })
    vi.spyOn(window, 'removeEventListener').mockImplementation((event) => {
      delete listeners[event]
    })
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('Ctrl+K 키 입력 시 onSearch 콜백을 호출하고 기본 동작을 방지한다', () => {
    const onSearch = vi.fn()
    const { enable, disable } = useGlobalShortcuts({ onSearch })
    enable()

    const event = new KeyboardEvent('keydown', {
      key: 'k',
      ctrlKey: true,
      bubbles: true,
      cancelable: true
    })
    const preventDefaultSpy = vi.spyOn(event, 'preventDefault')

    listeners['keydown']?.(event)

    expect(onSearch).toHaveBeenCalled()
    expect(preventDefaultSpy).toHaveBeenCalled()

    disable()
  })

  it('Ctrl+S 키 입력 시 onSave 콜백을 호출하고 브라우저 기본 저장을 방지한다', () => {
    const onSave = vi.fn()
    const { enable, disable } = useGlobalShortcuts({ onSave })
    enable()

    const event = new KeyboardEvent('keydown', {
      key: 's',
      ctrlKey: true,
      bubbles: true,
      cancelable: true
    })
    const preventDefaultSpy = vi.spyOn(event, 'preventDefault')

    listeners['keydown']?.(event)

    expect(onSave).toHaveBeenCalled()
    expect(preventDefaultSpy).toHaveBeenCalled()

    disable()
  })

  it('Escape 키 입력 시 onEscape 콜백을 호출한다', () => {
    const onEscape = vi.fn()
    const { enable, disable } = useGlobalShortcuts({ onEscape })
    enable()

    const event = new KeyboardEvent('keydown', {
      key: 'Escape',
      bubbles: true,
      cancelable: true
    })

    listeners['keydown']?.(event)

    expect(onEscape).toHaveBeenCalled()

    disable()
  })
})
