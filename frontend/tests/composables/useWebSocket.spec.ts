import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { ref } from 'vue'

// Mock Client class from @stomp/stompjs
const mockSubscribe = vi.fn()
const mockActivate = vi.fn()
const mockDeactivate = vi.fn()

let mockClientConfig: any = null
let mockClientInstance: any = null
let capturedSubscriptions: Record<string, (msg: any) => void> = {}

vi.mock('@stomp/stompjs', () => {
  return {
    Client: vi.fn().mockImplementation((config: any) => {
      mockClientConfig = config
      mockClientInstance = {
        connectHeaders: config.connectHeaders || {},
        active: true,
        activate: mockActivate.mockImplementation(() => {
          if (config.onConnect) {
            config.onConnect()
          }
        }),
        deactivate: mockDeactivate,
        subscribe: mockSubscribe.mockImplementation((topic: string, handler: (msg: any) => void) => {
          capturedSubscriptions[topic] = handler
          return { unsubscribe: vi.fn() }
        })
      }
      return mockClientInstance
    })
  }
})

describe('useWebSocket Composable (Singleton TDD)', () => {
  let useWebSocket: typeof import('../../composables/useWebSocket').useWebSocket
  let resetWebSocketState: typeof import('../../composables/useWebSocket').resetWebSocketState

  beforeEach(async () => {
    vi.clearAllMocks()
    capturedSubscriptions = {}
    mockClientConfig = null
    mockClientInstance = null
    document.cookie = 'auth_token=; Max-Age=0'

    // Dynamically import module to test
    const mod = await import('../../composables/useWebSocket')
    useWebSocket = mod.useWebSocket
    resetWebSocketState = mod.resetWebSocketState
    if (resetWebSocketState) {
      resetWebSocketState()
    }
  })

  afterEach(() => {
    if (resetWebSocketState) {
      resetWebSocketState()
    }
  })

  it('여러 컴포넌트에서 connect를 호출하더라도 단 1개의 STOMP Client만 생성되어야 함 (싱글톤)', () => {
    const ws1 = useWebSocket()
    const ws2 = useWebSocket()

    const cb1 = vi.fn()
    const cb2 = vi.fn()

    ws1.connect(cb1)
    ws2.connect(cb2)

    // STOMP Client 인스턴스는 1번만 생성 및 activate 되어야 함
    expect(mockActivate).toHaveBeenCalledTimes(1)
    expect(ws1.isConnected.value).toBe(true)
    expect(ws2.isConnected.value).toBe(true)
  })

  it('토픽 메시지 수신 시 등록된 모든 콜백 함수(NotificationBell, InAppMessenger 등)로 디스패치되어야 함', () => {
    const ws1 = useWebSocket()
    const ws2 = useWebSocket()

    const cb1 = vi.fn()
    const cb2 = vi.fn()

    ws1.connect(cb1)
    ws2.connect(cb2)

    // STOMP 메시지 도착 시뮬레이션
    expect(capturedSubscriptions['/topic/chat/presence']).toBeDefined()
    const presenceHandler = capturedSubscriptions['/topic/chat/presence']

    presenceHandler({
      body: JSON.stringify({ type: 'PRESENCE_UPDATE', username: 'user1', status: 'ONLINE' })
    })

    expect(cb1).toHaveBeenCalledWith(expect.objectContaining({
      type: 'PRESENCE_UPDATE',
      username: 'user1',
      status: 'ONLINE'
    }))
    expect(cb2).toHaveBeenCalledWith(expect.objectContaining({
      type: 'PRESENCE_UPDATE',
      username: 'user1',
      status: 'ONLINE'
    }))
  })

  it('한 컴포넌트가 disconnect를 호출해도 다른 컴포넌트의 리스너가 남아있으면 연결이 유지되어야 함', () => {
    const ws1 = useWebSocket()
    const ws2 = useWebSocket()

    const cb1 = vi.fn()
    const cb2 = vi.fn()

    ws1.connect(cb1)
    ws2.connect(cb2)

    // ws1 해제
    ws1.disconnect()

    // STOMP Client는 아직 deactivate되지 않아야 함
    expect(mockDeactivate).not.toHaveBeenCalled()
    expect(ws1.isConnected.value).toBe(true)

    // ws2 해제 시 비로소 deactivate
    ws2.disconnect()
    expect(mockDeactivate).toHaveBeenCalledTimes(1)
    expect(ws1.isConnected.value).toBe(false)
  })

  it('beforeConnect 실행 시 쿠키 또는 토큰이 connectHeaders에 주입되어야 함', () => {
    document.cookie = 'auth_token=mock-jwt-token-from-cookie'
    const ws = useWebSocket()
    ws.connect()

    expect(mockClientConfig).toBeDefined()
    mockClientConfig.beforeConnect()
    expect(mockClientInstance.connectHeaders.token).toBe('mock-jwt-token-from-cookie')
  })

  it('useRuntimeConfig의 apiBaseUrl 또는 window.location 기반으로 brokerURL이 올바르게 구성되어야 함', () => {
    const ws = useWebSocket()
    ws.connect()

    expect(mockClientConfig).toBeDefined()
    expect(mockClientConfig.brokerURL).toMatch(/^ws(s)?:\/\/.+\/ws-stomp$/)
  })

  it('사용자 정보가 있을 때 개인 알림 토픽(/topic/notifications/{userId})을 구독하고 메시지를 수신해야 함', () => {
    document.cookie = 'user_data=' + encodeURIComponent(JSON.stringify({ id: 'user-abc-123', username: 'tester1' }))
    const ws = useWebSocket()
    const cb = vi.fn()
    ws.connect(cb)

    expect(capturedSubscriptions['/topic/notifications/user-abc-123']).toBeDefined()
    expect(capturedSubscriptions['/topic/notifications/tester1']).toBeDefined()

    const notifHandler = capturedSubscriptions['/topic/notifications/user-abc-123']
    notifHandler({
      body: JSON.stringify({ eventType: 'INBOX_MESSAGE', type: 'NEW_MESSAGE', subject: 'New Mail' })
    })

    expect(cb).toHaveBeenCalledWith(expect.objectContaining({
      eventType: 'INBOX_MESSAGE',
      type: 'NEW_MESSAGE',
      subject: 'New Mail'
    }))
  })
})

