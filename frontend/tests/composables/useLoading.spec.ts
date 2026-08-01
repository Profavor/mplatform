import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { useLoading } from '../../composables/useLoading'

describe('useLoading Composable (TDD - Anti Flickering)', () => {
  let loading: ReturnType<typeof useLoading>

  beforeEach(() => {
    vi.useFakeTimers()
    loading = useLoading()
    loading.resetLoading()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('초기 상태에서는 isLoading이 false이고 activeRequests가 0이어야 한다', () => {
    expect(loading.isLoading.value).toBe(false)
    expect(loading.activeRequests.value).toBe(0)
  })

  it('200ms 미만의 아주 빠른 요청 시 오버레이가 켜지지 않아 깜빡임을 방지한다', () => {
    loading.showLoading('Fast Query')
    expect(loading.activeRequests.value).toBe(1)
    expect(loading.isLoading.value).toBe(false) // 아직 지연 대기 중

    // 100ms 후 요청 완료
    vi.advanceTimersByTime(100)
    loading.hideLoading()

    // 200ms 지난 후에도 오버레이는 켜진 적 없음
    vi.advanceTimersByTime(200)
    expect(loading.isLoading.value).toBe(false)
    expect(loading.activeRequests.value).toBe(0)
  })

  it('200ms 이상의 느린 DB 요청 시 오버레이가 켜진다', () => {
    loading.showLoading('Slow DB Query')
    expect(loading.isLoading.value).toBe(false)

    // 200ms 경과
    vi.advanceTimersByTime(200)
    expect(loading.isLoading.value).toBe(true)
    expect(loading.loadingText.value).toBe('Slow DB Query')
  })

  it('요청 완료 후 150ms 디바운스를 거쳐 깜빡임 없이 부드럽게 해제된다', () => {
    loading.showLoading('Slow DB Query')
    vi.advanceTimersByTime(200)
    expect(loading.isLoading.value).toBe(true)

    loading.hideLoading()
    // 즉시 꺼지지 않고 유지
    expect(loading.isLoading.value).toBe(true)

    // 150ms 디바운스 경과 후 꺼짐
    vi.advanceTimersByTime(150)
    expect(loading.isLoading.value).toBe(false)
  })

  it('연속된 요청 시 해제 대기 중인 타이머가 취소되고 로딩이 계속 유지된다', () => {
    loading.showLoading('Req 1')
    vi.advanceTimersByTime(200)
    expect(loading.isLoading.value).toBe(true)

    loading.hideLoading() // Req 1 완료 -> 150ms 해제 예약
    vi.advanceTimersByTime(50) // 50ms 후 Req 2 시작

    loading.showLoading('Req 2')
    vi.advanceTimersByTime(200) // 시간이 지나도 계속 로딩 유지
    expect(loading.isLoading.value).toBe(true)

    loading.hideLoading() // Req 2 완료
    vi.advanceTimersByTime(150)
    expect(loading.isLoading.value).toBe(false)
  })
})
