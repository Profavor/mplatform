import { useCustomFetch } from './useCustomFetch'

export interface InboxMessage {
  id: string
  senderId: string
  senderName: string
  senderEmail: string
  subject: string
  body: string
  importance: string
  messageType: string
  parentMessageId: string | null
  rootMessageId: string | null
  relatedApprovalId: string | null
  isDraft: boolean
  isRead: boolean
  isStarred: boolean
  folder: string
  hasAttachments: boolean
  attachmentCount: number
  recipientCount: number
  threadCount: number
  toRecipients: RecipientInfo[]
  ccRecipients: RecipientInfo[]
  attachments: AttachmentInfo[]
  sentAt: string
  createdAt: string
}

export interface RecipientInfo {
  userId: string | null
  name: string
  email: string
  recipientType: string
  isRead?: boolean
  readAt?: string | null
  isRecalled?: boolean
  recalledAt?: string | null
}

export interface RecallResultResponse {
  messageId: string
  totalRecipients: number
  recalledBeforeReadCount: number
  recalledAfterReadCount: number
  externalCount: number
  details: RecipientRecallDetail[]
}

export interface RecipientRecallDetail {
  userId: string | null
  name: string
  email: string
  recipientType: string
  wasRead: boolean
  readAt: string | null
  isRecalled: boolean
  status: string
}

export interface AttachmentInfo {
  id: string
  fileName: string
  fileSize: number
  contentType: string
}

export interface InboxMessageRequest {
  subject: string
  body: string
  importance?: string
  toRecipients: string[]
  ccRecipients?: string[]
  bccRecipients?: string[]
  attachmentIds?: string[]
  parentMessageId?: string
  isDraft?: boolean
}

export interface FolderCount {
  folder: string
  total: number
  unread: number
}

export function useInbox() {
  const { customFetch } = useCustomFetch()

  const fetchMessages = async (folder: string, page: number, size: number, keyword?: string) => {
    const params = new URLSearchParams({ folder, page: String(page), size: String(size) })
    if (keyword) params.append('keyword', keyword)
    return await customFetch(`/inbox/messages?${params.toString()}`)
  }

  const fetchMessage = async (id: string) => {
    return await customFetch(`/inbox/messages/${id}`)
  }

  const sendMessage = async (request: InboxMessageRequest) => {
    return await customFetch('/inbox/messages', { method: 'POST', body: request })
  }

  const saveDraft = async (request: InboxMessageRequest) => {
    return await customFetch('/inbox/messages', { method: 'POST', body: { ...request, isDraft: true } })
  }

  const updateDraft = async (id: string, request: InboxMessageRequest) => {
    return await customFetch(`/inbox/messages/${id}`, { method: 'PUT', body: request })
  }

  const replyMessage = async (id: string, request: InboxMessageRequest) => {
    return await customFetch(`/inbox/messages/${id}/reply`, { method: 'POST', body: request })
  }

  const replyAllMessage = async (id: string, request: InboxMessageRequest) => {
    return await customFetch(`/inbox/messages/${id}/reply-all`, { method: 'POST', body: request })
  }

  const forwardMessage = async (id: string, request: InboxMessageRequest) => {
    return await customFetch(`/inbox/messages/${id}/forward`, { method: 'POST', body: request })
  }

  const markAsRead = async (id: string) => {
    return await customFetch(`/inbox/messages/${id}/read`, { method: 'PATCH', body: { isRead: true } })
  }

  const markAsUnread = async (id: string) => {
    return await customFetch(`/inbox/messages/${id}/read`, { method: 'PATCH', body: { isRead: false } })
  }

  const bulkMarkAsRead = async (ids: string[]) => {
    return await customFetch('/inbox/messages/bulk-action', { method: 'POST', body: { action: 'MARK_READ', messageIds: ids } })
  }

  const toggleStar = async (id: string) => {
    return await customFetch(`/inbox/messages/${id}/star`, { method: 'PATCH' })
  }

  const moveToFolder = async (id: string, folder: string) => {
    return await customFetch(`/inbox/messages/${id}/folder`, { method: 'PATCH', body: { folder } })
  }

  const moveToTrash = async (id: string) => {
    return await customFetch(`/inbox/messages/${id}`, { method: 'DELETE' })
  }

  const bulkMoveToTrash = async (ids: string[]) => {
    return await customFetch('/inbox/messages/bulk-action', { method: 'POST', body: { action: 'MOVE_TO_TRASH', messageIds: ids } })
  }

  const permanentDelete = async (id: string) => {
    return await customFetch(`/inbox/messages/${id}?permanent=true`, { method: 'DELETE' })
  }

  const fetchFolderCounts = async () => {
    return await customFetch('/inbox/folder-counts')
  }

  const fetchUnreadCount = async () => {
    return await customFetch('/inbox/unread-count')
  }

  const fetchThread = async (id: string) => {
    return await customFetch(`/inbox/messages/${id}/thread`)
  }

  const recallMessage = async (id: string) => {
    return await customFetch(`/inbox/messages/${id}/recall`, { method: 'POST' })
  }

  return {
    fetchMessages, fetchMessage, sendMessage, saveDraft, updateDraft,
    replyMessage, replyAllMessage, forwardMessage,
    markAsRead, markAsUnread, bulkMarkAsRead,
    toggleStar, moveToFolder, moveToTrash, bulkMoveToTrash, permanentDelete,
    fetchFolderCounts, fetchUnreadCount, fetchThread, recallMessage
  }
}
