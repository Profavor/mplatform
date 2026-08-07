import { Client } from '@stomp/stompjs'

export const useWebSocket = () => {
  const isConnected = ref(false)
  let stompClient: Client | null = null

  const connect = (onMessageCallback?: (event: any) => void) => {
    if (process.server) return

    const config = useRuntimeConfig()
    const apiBase = (config.public && config.public.apiBaseUrl) ? config.public.apiBaseUrl : 'http://localhost:8080'
    const wsBase = apiBase.replace(/^http/, 'ws')
    const wsUrl = `${wsBase}/ws-stomp`

    stompClient = new Client({
      brokerURL: wsUrl,
      connectHeaders: {},
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      beforeConnect: () => {
        const currentToken = useCookie('auth_token').value || ''
        stompClient!.connectHeaders = {
          token: currentToken
        }
      },
      onConnect: () => {
        isConnected.value = true
        // STOMP is connected! Subscribe to presence updates and general chat events
        stompClient?.subscribe('/topic/chat/presence', (message) => {
          if (onMessageCallback) {
            try {
              const body = JSON.parse(message.body)
              // Wrap with PRESENCE_UPDATE type if not present, to match old logic
              if (!body.type) body.type = 'PRESENCE_UPDATE'
              onMessageCallback(body)
            } catch (e) {
              onMessageCallback(message.body)
            }
          }
        })
        stompClient?.subscribe('/topic/chat/messages', (message) => {
           if (onMessageCallback) {
             try {
                const body = JSON.parse(message.body)
                onMessageCallback(body)
             } catch (e) {
                onMessageCallback(message.body)
             }
           }
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

    stompClient.activate()
  }

  const disconnect = () => {
    if (stompClient) {
      stompClient.deactivate()
      stompClient = null
    }
    isConnected.value = false
  }

  return {
    isConnected,
    connect,
    disconnect
  }
}
