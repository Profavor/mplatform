export interface DynamicUrlOptions {
  xForwardedHost?: string | null
  xForwardedProto?: string | null
  host?: string | null
  fallbackOrigin?: string
}

export function resolveDynamicOrigin(options: DynamicUrlOptions): string {
  const host = options.xForwardedHost || options.host || 'localhost:3000'
  let proto = options.xForwardedProto
  if (!proto) {
    proto = host.includes('localhost') || host.includes('127.0.0.1') ? 'http' : 'https'
  }
  if (proto.includes(',')) {
    proto = proto.split(',')[0].trim()
  }
  return `${proto}://${host}`
}

export function adjustOidcUrl(url: string | undefined | null, dynamicOrigin: string): string {
  if (!url) return ''
  return url.replace(/^https?:\/\/[^\/]+/, dynamicOrigin)
}
