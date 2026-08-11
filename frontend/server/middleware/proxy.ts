import { defineEventHandler, proxyRequest } from 'h3'

export default defineEventHandler((event) => {
  const url = event.node.req.url
  if (url && url.startsWith('/api/') && !url.startsWith('/api/_auth/')) {
    const config = useRuntimeConfig()
    let rawUrl = config.public.apiBaseUrl || 'http://localhost:8080'
    if (rawUrl && !rawUrl.startsWith('http://') && !rawUrl.startsWith('https://')) {
      rawUrl = `https://${rawUrl}`
    }
    const targetUrl = rawUrl.replace(/\/$/, '')
    
    return proxyRequest(event, `${targetUrl}${url}`)
  }
})
