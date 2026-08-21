import { describe, it, expect, vi } from 'vitest'
import { useRecordBulkActions } from '~/composables/useRecordBulkActions'

describe('useRecordBulkActions', () => {
  const bulkActions = useRecordBulkActions()

  it('parseJwtUserId가 JWT 토큰에서 사용자 ID를 정상 파싱해야 한다', () => {
    // Header.Payload.Signature
    // Payload: {"userId":"user_123","username":"testuser"}
    const payloadBase64 = btoa(JSON.stringify({ userId: 'user_123', username: 'testuser' }))
    const dummyJwt = `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.${payloadBase64}.signature`

    const parsedId = bulkActions.parseJwtUserId(dummyJwt)
    expect(parsedId).toBe('user_123')
  })

  it('잘못된 토큰인 경우 parseJwtUserId는 null을 반환해야 한다', () => {
    expect(bulkActions.parseJwtUserId('')).toBeNull()
    expect(bulkActions.parseJwtUserId('invalid.token')).toBeNull()
  })

  it('handleBulkDelete가 선택된 행이 없을 때 경고 토스트를 표시해야 한다', async () => {
    const initToast = vi.fn()
    const t = vi.fn((key, def) => def)
    const customFetch = vi.fn()
    const confirm = vi.fn()

    await bulkActions.handleBulkDelete({
      selectedRows: [],
      t,
      confirm,
      customFetch,
      initToast,
      onSuccess: vi.fn()
    })

    expect(initToast).toHaveBeenCalledWith(expect.objectContaining({
      color: 'warning'
    }))
    expect(confirm).not.toHaveBeenCalled()
  })

  it('handleBulkDelete가 사용자가 확인하면 일괄 삭제 요청을 보내고 성공 콜백을 실행해야 한다', async () => {
    const initToast = vi.fn()
    const t = vi.fn((key, def) => def)
    const customFetch = vi.fn().mockResolvedValue({ success: true })
    const confirm = vi.fn().mockResolvedValue(true)
    const onSuccess = vi.fn()

    const selectedRows = [
      { id: 'rec-1' },
      { id: 'rec-2' }
    ]

    await bulkActions.handleBulkDelete({
      selectedRows,
      t,
      confirm,
      customFetch,
      initToast,
      onSuccess
    })

    expect(confirm).toHaveBeenCalled()
    expect(customFetch).toHaveBeenCalledTimes(2)
    expect(onSuccess).toHaveBeenCalled()
    expect(initToast).toHaveBeenCalledWith(expect.objectContaining({
      color: 'success'
    }))
  })
})
