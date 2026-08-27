import { describe, it, expect } from 'vitest'
import { resolveDynamicOrigin, adjustOidcUrl } from '../../utils/oidcDynamicUrl'

describe('OIDC Dynamic URL Resolution Helper (TDD)', () => {
  it('Cloudflare 터널 헤더(x-forwarded-host, x-forwarded-proto) 유입 시 해당 도메인 origin을 반환해야 함', () => {
    const origin = resolveDynamicOrigin({
      xForwardedHost: 'schedule-joined-affiliates-estimates.trycloudflare.com',
      xForwardedProto: 'https',
      host: '10.244.0.42:3000'
    })
    expect(origin).toBe('https://schedule-joined-affiliates-estimates.trycloudflare.com')
  })

  it('로컬 mplatform.local 접속 시 https://mplatform.local origin을 반환해야 함', () => {
    const origin = resolveDynamicOrigin({
      xForwardedHost: 'mplatform.local',
      xForwardedProto: 'https',
      host: 'frontend:3000'
    })
    expect(origin).toBe('https://mplatform.local')
  })

  it('로컬 개발 환경(localhost:3000)인 경우 http://localhost:3000을 반환해야 함', () => {
    const origin = resolveDynamicOrigin({
      host: 'localhost:3000'
    })
    expect(origin).toBe('http://localhost:3000')
  })

  it('기존 하드코딩된 Keycloak authorizationUrl의 호스트를 dynamicOrigin으로 치환해야 함', () => {
    const dynamicOrigin = 'https://schedule-joined-affiliates-estimates.trycloudflare.com'
    const originalUrl = 'http://mplatform.local/auth/realms/mplatform/protocol/openid-connect/auth'
    const adjusted = adjustOidcUrl(originalUrl, dynamicOrigin)
    expect(adjusted).toBe('https://schedule-joined-affiliates-estimates.trycloudflare.com/auth/realms/mplatform/protocol/openid-connect/auth')
  })

  it('기존 하드코딩된 redirectUri의 호스트를 dynamicOrigin으로 치환해야 함', () => {
    const dynamicOrigin = 'https://schedule-joined-affiliates-estimates.trycloudflare.com'
    const originalUrl = 'http://mplatform.local/auth/keycloak/callback'
    const adjusted = adjustOidcUrl(originalUrl, dynamicOrigin)
    expect(adjusted).toBe('https://schedule-joined-affiliates-estimates.trycloudflare.com/auth/keycloak/callback')
  })
})
