import { useState } from '#app'
import { ref } from 'vue'

const SHOW_DELAY_MS = 200 // 200ms 이상 걸리는 DB/API 요청일 때만 로딩 표시 (빠른 요청 깜빡임 방지)
const HIDE_DELAY_MS = 150 // 요청 종료 후 연속 요청에 대비해 150ms 간격 디바운스 대기 후 해제

export const useLoading = () => {
  const isLoading = useState<boolean>('globalLoading', () => false)
  const loadingText = useState<string>('globalLoadingText', () => '')
  const activeRequests = useState<number>('globalRequestCount', () => 0)
  
  const showTimer = ref<ReturnType<typeof setTimeout> | null>(null)
  const hideTimer = ref<ReturnType<typeof setTimeout> | null>(null)

  const showLoading = (text = '') => {
    activeRequests.value += 1
    if (text) {
      loadingText.value = text
    }

    // 해제 예약 타이머가 진행 중이었다면 취소 (연속 요청 시 로딩 상태 유지)
    if (hideTimer.value) {
      clearTimeout(hideTimer.value)
      hideTimer.value = null
    }

    // 200ms 이내에 끝나는 미세 fetch 요청의 깜빡임 방지 (지연 노출)
    if (!isLoading.value && !showTimer.value) {
      showTimer.value = setTimeout(() => {
        if (activeRequests.value > 0) {
          isLoading.value = true
        }
        showTimer.value = null
      }, SHOW_DELAY_MS)
    }
  }

  const hideLoading = () => {
    if (activeRequests.value > 0) {
      activeRequests.value -= 1
    }

    if (activeRequests.value === 0) {
      if (showTimer.value) {
        clearTimeout(showTimer.value)
        showTimer.value = null
      }

      // 즉시 끄지 않고 디바운스 대기하여 연속 fetch 간의 깜빡임 완전히 제거
      if (!hideTimer.value) {
        hideTimer.value = setTimeout(() => {
          if (activeRequests.value === 0) {
            isLoading.value = false
            loadingText.value = ''
          }
          hideTimer.value = null
        }, HIDE_DELAY_MS)
      }
    }
  }

  const resetLoading = () => {
    if (showTimer.value) clearTimeout(showTimer.value)
    if (hideTimer.value) clearTimeout(hideTimer.value)
    showTimer.value = null
    hideTimer.value = null
    activeRequests.value = 0
    isLoading.value = false
    loadingText.value = ''
  }

  return {
    isLoading,
    loadingText,
    activeRequests,
    showLoading,
    hideLoading,
    resetLoading
  }
}
