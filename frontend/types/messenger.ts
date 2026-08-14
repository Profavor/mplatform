export interface ChatAttachment {
  id?: string
  name: string
  url: string
  size?: number
  contentType?: string
}

export interface ChatMember {
  id: string
  userId: string
  username: string
  displayName?: string
  avatarUrl?: string
  role?: string
  isOnline?: boolean
}

export interface ChatMessage {
  id: string
  roomId: string
  senderId: string
  senderName?: string
  senderUsername?: string
  content: string
  createdAt: string
  messageType?: 'CHAT' | 'SYSTEM' | 'FILE' | 'IMAGE'
  attachments?: ChatAttachment[]
  unreadCount?: number
  isDeleted?: boolean
}

export interface ChatRoom {
  id: string
  name: string
  type: 'DIRECT' | 'GROUP' | 'CHANNEL'
  creatorId?: string
  unreadCount?: number
  lastMessage?: string
  lastMessageAt?: string
  members?: ChatMember[]
}

export interface UserPresence {
  userId: string
  status: 'ONLINE' | 'OFFLINE' | 'AWAY' | 'BUSY'
  lastSeenAt?: string
}
