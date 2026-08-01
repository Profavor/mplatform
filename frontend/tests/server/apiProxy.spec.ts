import { describe, it, expect } from 'vitest'

export function resolveProxyTarget(apiBaseUrl: string, requestPath: string): string {
  let rawUrl = apiBaseUrl || 'http://localhost:8080'
  if (rawUrl && !rawUrl.startsWith('http://') && !rawUrl.startsWith('https://')) {
    rawUrl = `https://${rawUrl}`
  }
  const targetUrl = rawUrl.replace(/\/$/, '')
  return `${targetUrl}${requestPath}`
}

describe('Nitro API Dynamic Proxy Helper (TDD)', () => {
  it('도메인 전용 URL과 API 경로가 정상적으로 포매팅되어야 함', () => {
    const result = resolveProxyTarget('https://mplatform-backend.onrender.com', '/api/roles')
    expect(result).toBe('https://mplatform-backend.onrender.com/api/roles')
  })

  it('프로토콜 스키마가 없는 경우 https:// 자동 보정되어야 함', () => {
    const result = resolveProxyTarget('mplatform-backend.onrender.com', '/api/system/install-status')
    expect(result).toBe('https://mplatform-backend.onrender.com/api/system/install-status')
  })

  it('기본값 포트(localhost:8080) 보정 검증', () => {
    const result = resolveProxyTarget('', '/api/auth/login')
    expect(result).toBe('http://localhost:8080/api/auth/login')
  })
})
