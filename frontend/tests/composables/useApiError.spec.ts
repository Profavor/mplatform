import { describe, it, expect } from 'vitest'
import { parseErrorMessage } from '../../composables/useApiError'

describe('useApiError Composable (TDD)', () => {
  it('401 Unauthorized 에러 시 세션 만료 메시지를 반환해야 함', () => {
    const error = { response: { status: 401 } }
    const msg = parseErrorMessage(error)
    expect(msg).toContain('인증이 필요하거나 세션이 만료되었습니다')
  })

  it('403 Forbidden 에러 시 권한 부족 메시지를 반환해야 함', () => {
    const error = { response: { status: 403 } }
    const msg = parseErrorMessage(error)
    expect(msg).toContain('권한이 없습니다')
  })

  it('409 Conflict 에러 시 동시 처리/충돌 메시지를 반환해야 함', () => {
    const error = { response: { status: 409, _data: { message: 'Conflict occurred' } } }
    const msg = parseErrorMessage(error)
    expect(msg).toContain('요청 처리 중 충돌이 발생했습니다')
  })

  it('커스텀 에러 메시지가 있을 경우 해당 메시지를 포함해야 함', () => {
    const error = { response: { status: 400, _data: { message: '잘못된 입력 파라미터입니다' } } }
    const msg = parseErrorMessage(error)
    expect(msg).toContain('잘못된 입력 파라미터입니다')
  })
})
