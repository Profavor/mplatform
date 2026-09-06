import { describe, it, expect, beforeEach } from 'vitest'
import { ref } from 'vue'
import {
  BASE_MODAL_Z_INDEX,
  MODAL_Z_INDEX_STEP,
  modalZIndexStack,
  calculateModalZIndex,
  useModalStack
} from '../../composables/useModalStack'

describe('Nested Modal Z-Index Stacking Management (TDD)', () => {
  beforeEach(() => {
    // 각 테스트 전 스택 초기화
    modalZIndexStack.splice(0, modalZIndexStack.length)
  })

  it('기본 1차 모달이 열릴 때 기본 z-index(1050)가 할당되어야 한다', () => {
    const isVisible = ref(true)
    const { modalId, activeZIndex } = useModalStack(isVisible)

    expect(modalZIndexStack).toContain(modalId)
    expect(activeZIndex.value).toBe(BASE_MODAL_Z_INDEX) // 1050
  })

  it('1차 모달이 열린 상태에서 2차 서브 모달이 열리면 더 높은 z-index(1100)가 자동으로 계산되어야 한다', () => {
    // 1. 부모 모달(RecordDetailDrawer) 오픈
    const parentVisible = ref(true)
    const parentModal = useModalStack(parentVisible)

    expect(parentModal.activeZIndex.value).toBe(BASE_MODAL_Z_INDEX) // 1050

    // 2. 자식 모달(ApprovalViewerModal) 오픈
    const childVisible = ref(true)
    const childModal = useModalStack(childVisible)

    expect(childModal.activeZIndex.value).toBe(BASE_MODAL_Z_INDEX + MODAL_Z_INDEX_STEP) // 1100
    expect(childModal.activeZIndex.value).toBeGreaterThan(parentModal.activeZIndex.value)
  })

  it('명시적인 zIndex prop(예: 1200)이 전달된 경우 자동 계산 대신 해당 값이 최우선 적용되어야 한다', () => {
    const isVisible = ref(true)
    const explicitZIndex = ref(1200)
    const { activeZIndex } = useModalStack(isVisible, explicitZIndex)

    expect(activeZIndex.value).toBe(1200)
  })

  it('모달이 닫히면(isVisible = false) 스택에서 정상 제거되어야 한다', () => {
    const isVisible = ref(true)
    const { modalId, unregister } = useModalStack(isVisible)

    expect(modalZIndexStack).toContain(modalId)

    isVisible.value = false
    // watch에 의해 unregister 호출
    expect(modalZIndexStack).not.toContain(modalId)
  })

  it('3단계 이상 깊게 중첩된 모달도 순차적으로 높은 z-index를 보장해야 한다', () => {
    const modal1 = useModalStack(ref(true))
    const modal2 = useModalStack(ref(true))
    const modal3 = useModalStack(ref(true))

    expect(modal1.activeZIndex.value).toBe(1050)
    expect(modal2.activeZIndex.value).toBe(1100)
    expect(modal3.activeZIndex.value).toBe(1150)
    expect(modal3.activeZIndex.value).toBeGreaterThan(modal2.activeZIndex.value)
    expect(modal2.activeZIndex.value).toBeGreaterThan(modal1.activeZIndex.value)
  })
})
