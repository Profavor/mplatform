import { describe, it, expect } from 'vitest'
import { useAlert } from '../../composables/useAlert'

describe('useAlert composable (TDD)', () => {
  it('기본 상태가 정상적으로 초기화된다', () => {
    const { showErrorAlertModal, errorAlertTitle, errorAlertMessage, errorAlertType } = useAlert()
    expect(showErrorAlertModal.value).toBe(false)
    expect(errorAlertTitle.value).toBe('')
    expect(errorAlertMessage.value).toBe('')
    expect(errorAlertType.value).toBe('danger')
  })

  it('showCustomAlert 호출 시 상태가 올바르게 갱신된다', () => {
    const { showErrorAlertModal, errorAlertTitle, errorAlertMessage, errorAlertType, showCustomAlert } = useAlert()

    showCustomAlert('경고', '주의가 필요합니다.', 'warning')

    expect(showErrorAlertModal.value).toBe(true)
    expect(errorAlertTitle.value).toBe('경고')
    expect(errorAlertMessage.value).toBe('주의가 필요합니다.')
    expect(errorAlertType.value).toBe('warning')
  })

  it('closeAlert 호출 시 모달 상태가 닫힌다', () => {
    const { showErrorAlertModal, showCustomAlert, closeAlert } = useAlert()

    showCustomAlert('에러', '오류 발생')
    expect(showErrorAlertModal.value).toBe(true)

    closeAlert()
    expect(showErrorAlertModal.value).toBe(false)
  })
})
