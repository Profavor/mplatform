import { ref } from 'vue'

const isInboxModalOpen = ref(false)
const initialInboxMessageId = ref<string | null>(null)

export const useInboxModal = () => {
  const openInbox = (messageId?: string | null) => {
    initialInboxMessageId.value = messageId || null
    isInboxModalOpen.value = true
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('open-inbox-modal', { detail: { messageId: messageId || null } }))
    }
  }

  const closeInbox = () => {
    isInboxModalOpen.value = false
    initialInboxMessageId.value = null
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('close-inbox-modal'))
    }
  }

  const toggleInbox = () => {
    isInboxModalOpen.value = !isInboxModalOpen.value
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('toggle-inbox-modal'))
    }
  }

  return {
    isInboxModalOpen,
    initialInboxMessageId,
    openInbox,
    closeInbox,
    toggleInbox
  }
}
