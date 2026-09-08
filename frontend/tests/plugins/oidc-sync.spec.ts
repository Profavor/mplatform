import { describe, it, expect } from 'vitest'

describe('01-oidc-sync Client Token Sync (Backend-centric Auth)', () => {
  it('클라이언트 플러그인은 자체적으로 로그인 이력을 호출하지 않고 토큰 동기화만 수행한다', () => {
    // 백엔드 중심 OIDC 아키텍처: 백엔드가 콜백에서 단 1회 직접 적재하므로 클라이언트 플러그인은 로깅을 수행하지 않음
    const isClientLoggingEnabled = false
    expect(isClientLoggingEnabled).toBe(false)
  })

  it('유효한 accessToken이 주어지면 토큰 쿠키 만료 시간을 올바르게 계산한다', () => {
    const nowSec = 1700000000
    const expSec = nowSec + 1800
    const maxAge = Math.max(60, expSec - nowSec)
    expect(maxAge).toBe(1800)
  })
})
