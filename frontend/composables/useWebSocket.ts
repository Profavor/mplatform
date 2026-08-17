import { Client } from '@stomp/stompjs'
import { ref } from 'vue'

// Module-level singleton state for WebSocket connection
let sharedClient: Client | null = null
const isConnected = ref(false)
const listeners = new Set<(event: any) => void>()

export const getWsAuthToken = (): string => {
  if (typeof document !== 'undefined') {
    const match = document.cookie.match(/(?:^|;\s*)(?:auth_token|token)=([^;]*)/)
    if (match && match[1]) {
      return decodeURIComponent(match[1])
    }
  }

  try {
    if (typeof useCookie === 'function') {
      const cookieToken = useCookie('auth_token').value || useCookie('token').value
      if (cookieToken) return String(cookieToken)
    }
  } catch {}

  try {
    if (typeof useOidcAuth === 'function') {
      const auth = useOidcAuth()
      if (auth?.user?.value?.accessToken) {
        return String(auth.user.value.accessToken)
      }
    }
  } catch {}

  return ''
}

const dispatchMessage = (rawBody: string, defaultType?: string) => {
  let payload: any
  try {
    payload = JSON.parse(rawBody)
    if (defaultType && !payload.type) {
      payload.type = defaultType
    }
  } catch {
    payload = rawBody
  }

  listeners.forEach((cb) => {
    try {
      cb(payload)
    } catch (e) {
      console.error('[WebSocket] Error in message listener callback:', e)
    }
  })
}

const initAndActivateClient = () => {
  if (typeof window === 'undefined') return
  if (sharedClient && sharedClient.active) return

  let wsUrl = ''
  try {
    if (typeof useRuntimeConfig === 'function') {
      const config = useRuntimeConfig()
      const rawApiBase = config?.public?.apiBaseUrl
      if (rawApiBase && (rawApiBase.startsWith('http://') || rawApiBase.startsWith('https://'))) {
        const wsBase = rawApiBase.replace(/^http/, 'ws').replace(/\/$/, '')
        wsUrl = `${wsBase}/ws-stomp`
      }
    }
  } catch {}

  if (!wsUrl) {
    const wsBase = window.location.origin.replace(/^http/, 'ws')
    wsUrl = `${wsBase}/ws-stomp`
  }

  sharedClient = new Client({
    brokerURL: wsUrl,
    connectHeaders: {},
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    beforeConnect: () => {
      const currentToken = getWsAuthToken()
      if (sharedClient) {
        sharedClient.connectHeaders = {
          token: currentToken
        }
      }
    },
    onConnect: () => {
      isConnected.value = true
      // STOMP is connected! Subscribe to presence updates and general chat events
      sharedClient?.subscribe('/topic/chat/presence', (message) => {
        dispatchMessage(message.body, 'PRESENCE_UPDATE')
      })
      sharedClient?.subscribe('/topic/chat/messages', (message) => {
        dispatchMessage(message.body)
      })
    },
    onStompError: (frame) => {
      console.error('Broker reported error: ' + frame.headers['message'])
      console.error('Additional details: ' + frame.body)
      isConnected.value = false
    },
    onWebSocketClose: () => {
      isConnected.value = false
    },
    onWebSocketError: () => {
      isConnected.value = false
    }
  })

  sharedClient.activate()
}

/**
 * useWebSocket Composable
 * Provides a shared STOMP WebSocket connection instance (Singleton) across all components.
 */
export const useWebSocket = () => {
  let localCallback: ((event: any) => void) | null = null

  const connect = (onMessageCallback?: (event: any) => void) => {
    if (onMessageCallback) {
      localCallback = onMessageCallback
      listeners.add(onMessageCallback)
    }
    initAndActivateClient()
  }

  const disconnect = (callback?: (event: any) => void) => {
    const cb = callback || localCallback
    if (cb) {
      listeners.delete(cb)
      if (localCallback === cb) {
        localCallback = null
      }
    }

    if (listeners.size === 0 && sharedClient) {
      sharedClient.deactivate()
      sharedClient = null
      isConnected.value = false
    }
  }

  return {
    isConnected,
    connect,
    disconnect
  }
}

/**
 * Helper to reset singleton state in unit tests
 */
export const resetWebSocketState = () => {
  if (sharedClient) {
    try {
      sharedClient.deactivate()
    } catch {}
    sharedClient = null
  }
  listeners.clear()
  isConnected.value = false
}
