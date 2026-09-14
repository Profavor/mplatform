import { shallowReactive, computed, onBeforeUnmount, watch, getCurrentInstance, type Ref } from 'vue'

export const BASE_MODAL_Z_INDEX = 1200
export const MODAL_Z_INDEX_STEP = 50

// 전역 모달 스택 (열려 있는 모달들의 인스턴스 ID 목록)
export const modalZIndexStack = shallowReactive<string[]>([])

let idCounter = 0
export function generateModalId(): string {
  return `modal_${Date.now()}_${++idCounter}`
}

export function calculateModalZIndex(
  modalId: string,
  explicitZIndex?: number | string | null
): number {
  const index = modalZIndexStack.indexOf(modalId)
  const level = index === -1 ? modalZIndexStack.length : index
  const baseFromStack = BASE_MODAL_Z_INDEX + level * MODAL_Z_INDEX_STEP
  if (explicitZIndex !== undefined && explicitZIndex !== null && !isNaN(Number(explicitZIndex)) && Number(explicitZIndex) > 0) {
    return Math.max(Number(explicitZIndex) + level * MODAL_Z_INDEX_STEP, baseFromStack)
  }
  return baseFromStack
}

export function useModalStack(
  isVisible: Ref<boolean> | { value: boolean },
  explicitZIndex?: Ref<number | string | null | undefined> | number | string | null
) {
  const modalId = generateModalId()

  const register = () => {
    if (!modalZIndexStack.includes(modalId)) {
      modalZIndexStack.push(modalId)
    }
  }

  const unregister = () => {
    const idx = modalZIndexStack.indexOf(modalId)
    if (idx !== -1) {
      modalZIndexStack.splice(idx, 1)
    }
  }

  const activeZIndex = computed(() => {
    const expZ = typeof explicitZIndex === 'object' && explicitZIndex !== null && 'value' in explicitZIndex
      ? explicitZIndex.value
      : explicitZIndex
    return calculateModalZIndex(modalId, expZ)
  })

  watch(
    () => isVisible.value,
    (val) => {
      if (val) {
        register()
      } else {
        unregister()
      }
    },
    { immediate: true, flush: 'sync' }
  )

  if (getCurrentInstance()) {
    onBeforeUnmount(() => {
      unregister()
    })
  }

  return {
    modalId,
    activeZIndex,
    register,
    unregister
  }
}
