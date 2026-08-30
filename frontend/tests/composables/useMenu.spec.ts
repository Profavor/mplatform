import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useMenu } from '../../composables/useMenu'

// Mock useCustomFetch
const mockCustomFetch = vi.fn().mockResolvedValue({})
const mockGetAuthToken = vi.fn().mockReturnValue('mock-token')

vi.mock('../../composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: mockCustomFetch,
    getAuthToken: mockGetAuthToken
  })
}))

describe('useMenu Composable (TDD)', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('logAccess 호출 시 silent: true 옵션이 포함되어 에러 토스트를 유발하지 않아야 한다', async () => {
    const { logAccess } = useMenu()

    await logAccess('/records')

    expect(mockCustomFetch).toHaveBeenCalledTimes(1)
    expect(mockCustomFetch).toHaveBeenCalledWith(
      '/api/menus/access',
      expect.objectContaining({
        method: 'POST',
        body: expect.objectContaining({ menuPath: '/records' }),
        silent: true
      })
    )
  })

  it('/install 또는 /login 경로에서는 logAccess가 API를 호출하지 않아야 한다', async () => {
    const { logAccess } = useMenu()

    await logAccess('/login')
    await logAccess('/install')

    expect(mockCustomFetch).not.toHaveBeenCalled()
  })

  it('logAccess API 호출이 실패하더라도 예외를 던지지 않고 안전하게 무시되어야 한다', async () => {
    mockCustomFetch.mockRejectedValueOnce(new Error('Network error'))
    const { logAccess } = useMenu()

    await expect(logAccess('/dashboard')).resolves.not.toThrow()
  })
})
