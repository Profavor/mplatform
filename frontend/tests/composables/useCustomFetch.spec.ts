import { describe, it, expect } from 'vitest'
import { prepareFetchOptions, wrapResponse } from '../../composables/useCustomFetch'

describe('useCustomFetch Composable (TDD)', () => {
  it('토큰이 제공되면 Authorization 헤더가 주입되어야 함', () => {
    const opts = prepareFetchOptions({ headers: {} }, 'test-token-123', 'Asia/Seoul')
    expect(opts.headers.Authorization).toBe('Bearer test-token-123')
  })

  it('타임존이 제공되면 X-Timezone 헤더가 주입되어야 함', () => {
    const opts = prepareFetchOptions({ headers: {} }, 'test-token-123', 'Asia/Seoul')
    expect(opts.headers['X-Timezone']).toBe('Asia/Seoul')
  })

  it('기존 헤더가 유지된 채 추가 헤더가 병합되어야 함', () => {
    const opts = prepareFetchOptions({ headers: { 'Content-Type': 'application/json' } }, 'token', 'UTC')
    expect(opts.headers['Content-Type']).toBe('application/json')
    expect(opts.headers.Authorization).toBe('Bearer token')
    expect(opts.headers['X-Timezone']).toBe('UTC')
  })

  it('Blob 인스턴스는 Proxy로 감싸지 않고 원본 그대로 반환해야 함', () => {
    const blob = new Blob(['test content'], { type: 'text/plain' })
    const res = wrapResponse(blob)
    expect(res).toBe(blob)
    expect(res instanceof Blob).toBe(true)
  })
})
