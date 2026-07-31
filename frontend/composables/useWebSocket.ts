export const useWebSocket = () => {
  const isConnected = ref(false)
  let socket: WebSocket | null = null

  const connect = (onMessageCallback?: (event: any) => void) => {
    if (process.server) return

    const config = useRuntimeConfig()
    const apiBase = (config.public && config.public.apiBaseUrl) ? config.public.apiBaseUrl : 'http://localhost:8080'
    const wsBase = apiBase.replace(/^http/, 'ws')
    const wsUrl = `${wsBase}/ws-stomp`

    try {
      socket = new WebSocket(wsUrl)
      socket.onopen = () => {
        isConnected.value = true
      }
      socket.onmessage = (msg) => {
        if (onMessageCallback && msg.data) {
          try {
            const parsed = JSON.parse(msg.data)
            onMessageCallback(parsed)
          } catch (e) {
            onMessageCallback(msg.data)
          }
        }
      }
      socket.onclose = () => {
        isConnected.value = false
      }
      socket.onerror = () => {
        isConnected.value = false
      }
    } catch (e) {
      console.debug('[WebSocket] Connection attempt skipped:', e)
    }
  }

  const disconnect = () => {
    if (socket) {
      socket.close()
      socket = null
    }
  }

  return {
    isConnected,
    connect,
    disconnect
  }
}
