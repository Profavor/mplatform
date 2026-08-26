export default defineEventHandler((event) => {
  if (event.path.startsWith('/api/_auth')) {
    return
  }
  const config = useRuntimeConfig(event)
  let rawUrl = process.env.API_BASE_URL || config.public.apiBaseUrl || process.env.NUXT_PUBLIC_API_BASE_URL || process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8080'

  if (rawUrl && !rawUrl.startsWith('http://') && !rawUrl.startsWith('https://')) {
    rawUrl = `https://${rawUrl}`
  }

  const targetUrl = rawUrl.replace(/\/$/, '')
  const targetPath = event.path

  return proxyRequest(event, `${targetUrl}${targetPath}`)
})
