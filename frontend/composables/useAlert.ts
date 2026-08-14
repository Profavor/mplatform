import { ref } from 'vue'

export type AlertType = 'danger' | 'warning' | 'info' | 'success'

export function useAlert() {
  const showErrorAlertModal = ref(false)
  const errorAlertTitle = ref('')
  const errorAlertHeader = ref('')
  const errorAlertMessage = ref('')
  const errorAlertType = ref<AlertType>('danger')

  const showCustomAlert = (title: string, message: string, type: AlertType = 'danger', header: string = '') => {
    errorAlertTitle.value = title || ''
    errorAlertHeader.value = header || title || ''
    errorAlertMessage.value = message || ''
    errorAlertType.value = type
    showErrorAlertModal.value = true
  }

  const closeAlert = () => {
    showErrorAlertModal.value = false
  }

  return {
    showErrorAlertModal,
    errorAlertTitle,
    errorAlertHeader,
    errorAlertMessage,
    errorAlertType,
    showCustomAlert,
    closeAlert
  }
}
