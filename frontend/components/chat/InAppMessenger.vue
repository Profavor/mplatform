<template>
  <div class="in-app-messenger-container">
    <!-- Floating Toggle Button -->
    <div style="position: fixed; bottom: 24px; right: 24px; z-index: 999; display: inline-flex;">
      <va-button
        preset="primary"
        :class="['messenger-toggle-btn', { 'has-unread-pulse': totalUnreadCount > 0 && !isOpen }]"
        style="width: 56px; height: 56px; border-radius: 28px; box-shadow: 0 8px 24px rgba(37,99,235,0.4);"
        @click="toggleMessenger"
      >
        <va-icon :name="isOpen ? 'close' : 'chat'" size="28px" color="#ffffff" />
      </va-button>
      <span
        v-if="totalUnreadCount > 0 && !isOpen"
        style="position: absolute; top: -4px; right: -4px; background: #e53935; color: white; border-radius: 12px; padding: 2px 7px; font-size: 11px; font-weight: 800; border: 2px solid white; box-shadow: 0 4px 8px rgba(0,0,0,0.3); pointer-events: none;"
      >
        {{ totalUnreadCount > 99 ? '99+' : totalUnreadCount }}
      </span>
    </div>

    <!-- Messenger Panel -->
    <div
      v-if="isOpen"
      class="messenger-panel"
      style="position: fixed; bottom: 92px; right: 24px; z-index: 1000; width: 380px; height: 580px; max-width: 92vw; max-height: 80vh; background: var(--va-background-secondary); border-radius: 18px; box-shadow: 0 16px 40px rgba(0,0,0,0.25); display: flex; flex-direction: column; overflow: hidden; border: 1px solid var(--va-background-border);"
    >
      <!-- Header -->
      <div class="messenger-header" style="padding: 14px 16px; background: var(--va-primary); color: white; display: flex; align-items: center; justify-content: space-between;">
        <div style="display: flex; align-items: center; gap: 8px;">
          <va-button v-if="activeRoom" preset="plain" color="#ffffff" size="small" @click="activeRoom = null">
            <va-icon name="arrow_back" size="20px" />
          </va-button>
          <span style="font-weight: 700; font-size: 1rem;">
            {{ activeRoom ? activeRoom.name : $t('messenger.title') }}
          </span>
        </div>
        <div style="display: flex; gap: 6px;">
          <va-button v-if="activeRoom" preset="plain" color="#ffffff" size="small" :title="$t('messenger.viewMembersTooltip')" @click="showMembersModal">
            <va-icon name="group" size="20px" />
          </va-button>
          <va-button v-if="!activeRoom" preset="plain" color="#ffffff" size="small" :title="$t('messenger.createRoomTooltip')" @click="showCreateModal = true">
            <va-icon name="group_add" size="20px" />
          </va-button>
          <va-button preset="plain" color="#ffffff" size="small" @click="isOpen = false">
            <va-icon name="close" size="20px" />
          </va-button>
        </div>
      </div>

      <!-- Room List View -->
      <div v-if="!activeRoom" class="room-list-view" style="flex: 1; overflow-y: auto; padding: 12px;">
        <div v-if="rooms.length === 0" style="text-align: center; color: var(--va-text-secondary); margin-top: 80px;">
          <va-icon name="chat_bubble_outline" size="48px" style="opacity: 0.5; margin-bottom: 12px;" />
          <div>{{ $t('messenger.noRooms') }}</div>
          <va-button preset="plain" size="small" style="margin-top: 8px;" @click="showCreateModal = true">{{ $t('messenger.createGroupRoomBtn') }}</va-button>
        </div>

        <div
          v-for="room in rooms"
          :key="room.id"
          class="room-item"
          style="padding: 12px; border-radius: 12px; margin-bottom: 8px; background: var(--va-background-element); cursor: pointer; display: flex; align-items: center; gap: 12px; transition: all 0.2s;"
          @click="selectRoom(room)"
        >
          <va-avatar color="primary" size="medium">
            {{ room.isGroup ? '👥' : '👤' }}
          </va-avatar>
          <div style="flex: 1; overflow: hidden;">
            <div style="font-weight: 700; font-size: 0.95rem; display: flex; justify-content: space-between; align-items: center;">
              <span>{{ room.name }}</span>
              <span style="font-size: 0.75rem; color: var(--va-text-secondary); font-weight: normal;">{{ formatTime(room.lastMessageAt) }}</span>
            </div>
            <div style="font-size: 0.8rem; color: var(--va-text-secondary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-top: 2px;">
              {{ room.lastMessage || $t('messenger.noDialogue') }}
            </div>
          </div>
        </div>
      </div>

      <!-- Active Chat Room View -->
      <div v-else class="chat-room-view" style="flex: 1; display: flex; flex-direction: column; overflow: hidden;">
        <!-- Messages Area -->
        <div ref="msgContainer" class="messages-area" style="flex: 1; overflow-y: auto; padding: 14px; display: flex; flex-direction: column; gap: 10px;" @dragover.prevent @drop.prevent="handleDrop">
          <div v-for="msg in messages" :key="msg.id" class="msg-bubble-wrapper" :style="{ alignSelf: isMyMsg(msg) ? 'flex-end' : 'flex-start', maxWidth: '80%' }">
            <div v-if="!isMyMsg(msg)" style="font-size: 0.75rem; color: var(--va-text-secondary); margin-bottom: 2px;">{{ msg.senderName }}</div>
            
            <div style="display: flex; align-items: flex-end; gap: 6px;" :style="{ flexDirection: isMyMsg(msg) ? 'row' : 'row-reverse' }">
              <!-- Unread Count & Time -->
              <div style="font-size: 0.7rem; color: var(--va-text-secondary); display: flex; flex-direction: column; align-items: flex-end; gap: 1px;">
                <span v-if="msg.unreadCount && msg.unreadCount > 0" style="color: #f59e0b; font-weight: 800; font-size: 0.75rem;">{{ msg.unreadCount }}</span>
                <span>{{ formatTime(msg.createdAt) }}</span>
              </div>

              <!-- Bubble Content -->
              <div
                class="msg-bubble"
                @contextmenu.prevent="onContextMenu($event, msg)"
                :style="{
                  padding: '10px 14px',
                  borderRadius: '16px',
                  background: isMyMsg(msg) ? 'var(--va-primary)' : 'var(--va-background-element)',
                  color: isMyMsg(msg) ? 'white' : 'var(--va-text-primary)',
                  fontSize: '0.9rem',
                  wordBreak: 'break-word'
                }"
              >
                <!-- TEXT & EMOJI -->
                <span v-if="msg.messageType === 'TEXT' || msg.messageType === 'EMOJI'">{{ msg.content }}</span>
                
                <!-- IMAGE -->
                <div v-else-if="msg.messageType === 'IMAGE'">
                  <img :src="getAuthenticatedImageUrl(msg.fileUrl || msg.content)" style="max-width: 200px; border-radius: 8px; cursor: pointer;" @click="previewImg(getAuthenticatedImageUrl(msg.fileUrl || msg.content))" />
                </div>

                <!-- FILE -->
                <div v-else-if="msg.messageType === 'FILE'" style="display: flex; align-items: center; gap: 10px; padding: 2px 0;">
                  <div style="background: rgba(255,255,255,0.2); padding: 8px; border-radius: 8px; display: flex; align-items: center; justify-content: center;">
                    <va-icon :name="getFileIcon(msg.fileName)" size="22px" :color="isMyMsg(msg) ? '#ffffff' : 'var(--va-primary)'" />
                  </div>
                  <div style="flex: 1; overflow: hidden;">
                    <div style="font-weight: 700; font-size: 0.85rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 150px;">
                      {{ msg.fileName || '첨부파일' }}
                    </div>
                    <div style="font-size: 0.72rem; opacity: 0.85;">
                      {{ formatFileSize(msg.fileSize) }}
                    </div>
                  </div>
                  <va-button
                    size="small"
                    preset="plain"
                    :style="{
                      background: isMyMsg(msg) ? 'rgba(255,255,255,0.25)' : 'rgba(0,0,0,0.06)',
                      borderRadius: '50%',
                      width: '32px',
                      height: '32px',
                      padding: 0,
                      display: 'inline-flex',
                      alignItems: 'center',
                      justifyContent: 'center'
                    }"
                    :title="$t('messenger.downloadFile')"
                    @click="downloadAuthenticatedFile(msg)"
                  >
                    <va-icon name="download" size="18px" :color="isMyMsg(msg) ? '#ffffff' : 'var(--va-primary)'" />
                  </va-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Input Area -->
        <div class="chat-input-area" style="padding: 10px; background: var(--va-background-element); border-top: 1px solid var(--va-background-border); display: flex; flex-direction: column; gap: 6px;">
          <!-- Quick Emoji Toolbar -->
          <div style="display: flex; gap: 6px; overflow-x: auto; padding-bottom: 4px;">
            <span v-for="emoji in quickEmojis" :key="emoji" style="cursor: pointer; font-size: 1.2rem;" @click="sendEmoji(emoji)">{{ emoji }}</span>
          </div>

          <div style="display: flex; align-items: center; gap: 6px;">
            <input ref="fileInputRef" type="file" style="display: none;" @change="handleFileSelect" />
            <va-button preset="plain" color="primary" size="small" :title="$t('messenger.attachFileTooltip')" @click="triggerFileInput">
              <va-icon name="attach_file" size="22px" />
            </va-button>
            <va-input
              v-model="inputMsg"
              :placeholder="$t('messenger.placeholderMsg')"
              style="flex: 1;"
              @keyup.enter="sendTextMessage"
              @paste="handlePaste"
            />
            <va-button preset="primary" @click="sendTextMessage">{{ $t('messenger.sendBtn') }}</va-button>
          </div>
        </div>
      </div>
    </div>

    <!-- Create Group Room Modal -->
    <va-modal v-model="showCreateModal" :title="$t('messenger.createGroupRoomTitle')" :ok-text="$t('messenger.createBtn')" :cancel-text="$t('messenger.cancelBtn')" @ok="createNewRoom">
      <va-input v-model="newRoomName" :label="$t('messenger.roomNameLabel')" style="margin-bottom: 12px;" />
      <div style="font-size: 0.85rem; color: var(--va-text-secondary); margin-bottom: 6px;">{{ $t('messenger.selectUsersLabel') }}</div>
      <div style="max-height: 180px; overflow-y: auto;">
        <div v-for="u in selectableUsers" :key="u.id" style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px;">
          <va-checkbox v-model="selectedUserIds" :array-value="u.id" :label="u.username + ' (' + u.role + ')'" />
        </div>
      </div>
    </va-modal>

    <!-- Image Preview Modal -->
    <va-modal v-model="showImgModal" hide-default-actions>
      <img :src="previewImgUrl" style="max-width: 100%; max-height: 70vh; border-radius: 8px;" />
    </va-modal>

    <!-- Room Members Modal -->
    <va-modal v-model="showMembersModalFlag" :title="`👥 ${$t('messenger.roomMembersTitle')} (${roomMembers.length})`" hide-default-actions>
      <div style="max-height: 260px; overflow-y: auto; padding: 4px;">
        <div v-for="m in roomMembers" :key="m.userId" style="display: flex; align-items: center; justify-content: space-between; padding: 8px 12px; border-bottom: 1px solid var(--va-background-border);">
          <div style="display: flex; align-items: center; gap: 10px;">
            <va-avatar size="small" color="primary">{{ (m.username || 'U').charAt(0).toUpperCase() }}</va-avatar>
            <div>
              <div style="font-weight: 700; font-size: 0.9rem; display: flex; align-items: center; gap: 4px;">
                <span>{{ m.username }}</span>
                <va-badge v-if="isMe(m)" color="success" size="small">{{ $t('messenger.meBadge') }}</va-badge>
                <va-badge v-if="isCreator(m)" color="warning" size="small">{{ $t('messenger.creatorBadge') }}</va-badge>
              </div>
              <div style="font-size: 0.75rem; color: var(--va-text-secondary);">{{ m.role || 'USER' }}</div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <va-button preset="secondary" @click="showMembersModalFlag = false">{{ $t('messenger.closeBtn') }}</va-button>
      </template>
    </va-modal>

    <!-- Context Menu Popup -->
    <div
      v-if="contextMenu.show"
      class="chat-context-menu"
      :style="{ position: 'fixed', top: contextMenu.y + 'px', left: contextMenu.x + 'px', zIndex: 9999, background: 'var(--va-background-element)', border: '1px solid var(--va-background-border)', borderRadius: '8px', boxShadow: '0 8px 24px rgba(0,0,0,0.25)', padding: '4px 0', minWidth: '120px' }"
    >
      <div style="padding: 8px 12px; cursor: pointer; display: flex; align-items: center; gap: 8px; font-size: 0.85rem;" @click="copyMsgContent">
        <va-icon name="content_copy" size="16px" /> {{ $t('messenger.contextCopy') }}
      </div>
      <div style="padding: 8px 12px; cursor: pointer; display: flex; align-items: center; gap: 8px; font-size: 0.85rem;" @click="openForwardModal">
        <va-icon name="shortcut" size="16px" /> {{ $t('messenger.contextForward') }}
      </div>
      <div style="padding: 8px 12px; cursor: pointer; display: flex; align-items: center; gap: 8px; font-size: 0.85rem; color: #ef4444;" @click="deleteMsg">
        <va-icon name="delete" size="16px" color="danger" /> {{ $t('messenger.contextDelete') }}
      </div>
    </div>

    <!-- Forward Message Modal -->
    <va-modal v-model="showForwardModalFlag" :title="$t('messenger.forwardTitle')" hide-default-actions>
      <va-input v-model="searchUserQuery" :placeholder="$t('messenger.searchUserPlaceholder')" style="margin-bottom: 12px;" />
      <div style="max-height: 220px; overflow-y: auto;">
        <div v-if="searchFilteredUsers.length === 0" style="text-align: center; color: var(--va-text-secondary); padding: 20px;">
          {{ $t('messenger.noUserFound') }}
        </div>
        <div
          v-for="u in searchFilteredUsers"
          :key="u.id"
          style="display: flex; align-items: center; justify-content: space-between; padding: 8px 12px; border-bottom: 1px solid var(--va-background-border); cursor: pointer;"
          @click="forwardToUser(u)"
        >
          <div style="display: flex; align-items: center; gap: 10px;">
            <va-avatar size="small" color="primary">{{ (u.username || 'U').charAt(0).toUpperCase() }}</va-avatar>
            <div>
              <div style="font-weight: 700; font-size: 0.85rem;">{{ u.username }}</div>
              <div style="font-size: 0.75rem; color: var(--va-text-secondary);">{{ u.role || 'USER' }}</div>
            </div>
          </div>
          <va-button size="small" preset="secondary">{{ $t('messenger.contextForward') }}</va-button>
        </div>
      </div>
      <template #footer>
        <va-button preset="secondary" @click="showForwardModalFlag = false">{{ $t('messenger.cancelBtn') }}</va-button>
      </template>
    </va-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'

const { t } = useI18n()

const isOpen = ref(false)
const rooms = ref<any[]>([])
const activeRoom = ref<any>(null)
const messages = ref<any[]>([])
const inputMsg = ref('')
const showCreateModal = ref(false)
const newRoomName = ref('')
const availableUsers = ref<any[]>([])
const selectedUserIds = ref<string[]>([])
const showImgModal = ref(false)

const imageBlobUrls = ref<Record<string, string>>({})
const loadingBlobUrls = new Set<string>()
const transparentPixel = 'data:image/svg+xml;charset=utf-8,%3Csvg xmlns="http://www.w3.org/2000/svg"/%3E'

const loadAuthenticatedImage = async (url: string) => {
  if (!url || imageBlobUrls.value[url] || loadingBlobUrls.has(url)) return
  if (url.startsWith('data:')) {
    imageBlobUrls.value[url] = url
    return
  }
  loadingBlobUrls.add(url)
  try {
    const blob: any = await $fetch(url, {
      headers: { Authorization: `Bearer ${tokenCookie.value}` },
      responseType: 'blob'
    })
    imageBlobUrls.value[url] = URL.createObjectURL(blob)
  } catch (e) {
    console.error('Failed to load authenticated image blob:', e)
  } finally {
    loadingBlobUrls.delete(url)
  }
}

const getAuthenticatedImageUrl = (url: string) => {
  if (!url) return ''
  if (url.startsWith('data:')) return url
  if (imageBlobUrls.value[url]) return imageBlobUrls.value[url]
  loadAuthenticatedImage(url)
  return transparentPixel
}

const fileInputRef = ref<HTMLInputElement | null>(null)

const triggerFileInput = () => {
  fileInputRef.value?.click()
}

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target && target.files && target.files.length > 0) {
    const file = target.files[0]
    uploadAndSendFile(file)
    target.value = ''
  }
}

const handleDrop = (event: DragEvent) => {
  if (event.dataTransfer && event.dataTransfer.files && event.dataTransfer.files.length > 0) {
    const file = event.dataTransfer.files[0]
    uploadAndSendFile(file)
  }
}

const uploadAndSendFile = async (file: File) => {
  if (!activeRoom.value || !tokenCookie.value) return

  const isImage = file.type && file.type.startsWith('image/')
  const formData = new FormData()
  formData.append('file', file)

  try {
    const res: any = await $fetch('/api/chat/upload', {
      method: 'POST',
      headers: { Authorization: `Bearer ${tokenCookie.value}` },
      body: formData
    })

    if (res && res.fileUrl) {
      if (isImage) {
        await postMessage('IMAGE', file.name, res.fileUrl, res.fileName, res.fileSize)
      } else {
        await postMessage('FILE', file.name, res.fileUrl, res.fileName, res.fileSize)
      }
      scrollToBottom()
    }
  } catch (e) {}
}

const formatFileSize = (bytes?: number) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

const getFileIcon = (fileName?: string) => {
  if (!fileName) return 'insert_drive_file'
  const lower = fileName.toLowerCase()
  if (lower.endsWith('.pdf')) return 'picture_as_pdf'
  if (lower.endsWith('.xls') || lower.endsWith('.xlsx') || lower.endsWith('.csv')) return 'table_chart'
  if (lower.endsWith('.doc') || lower.endsWith('.docx') || lower.endsWith('.txt')) return 'description'
  if (lower.endsWith('.zip') || lower.endsWith('.rar') || lower.endsWith('.7z')) return 'folder_zip'
  if (lower.endsWith('.mp3') || lower.endsWith('.wav')) return 'audiotrack'
  if (lower.endsWith('.mp4') || lower.endsWith('.avi')) return 'movie'
  return 'insert_drive_file'
}

const downloadAuthenticatedFile = async (msg: any) => {
  if (!msg.fileUrl || !tokenCookie.value) return
  try {
    const blob: any = await $fetch(msg.fileUrl, {
      headers: { Authorization: `Bearer ${tokenCookie.value}` },
      responseType: 'blob'
    })
    const downloadUrl = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = downloadUrl
    a.download = msg.fileName || 'download'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(downloadUrl)
  } catch (e) {}
}
const previewImgUrl = ref('')
const msgContainer = ref<any>(null)

const showMembersModalFlag = ref(false)
const roomMembers = ref<any[]>([])

const contextMenu = ref({
  show: false,
  x: 0,
  y: 0,
  msg: null as any
})

const showForwardModalFlag = ref(false)
const searchUserQuery = ref('')

const searchFilteredUsers = computed(() => {
  if (!availableUsers.value || !Array.isArray(availableUsers.value)) return []
  const q = searchUserQuery.value.trim().toLowerCase()
  return availableUsers.value.filter((u: any) => {
    const name = String(u.username || '').toLowerCase()
    const role = String(u.role || '').toLowerCase()
    return !q || name.includes(q) || role.includes(q)
  })
})

const quickEmojis = ['👍', '❤️', '😂', '🎉', '🔥', '✅', '🙏']

const tokenCookie = useCookie('auth_token')
const userCookie = useCookie('user_data')

const parseJwtUserId = (token: any) => {
  if (!token) return null
  try {
    const base64Url = String(token).split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''))
    const parsed = JSON.parse(jsonPayload)
    return parsed.userId || parsed.uuid || parsed.sub || null
  } catch {
    return null
  }
}

const currentUser = computed(() => {
  if (userCookie.value) {
    try {
      return typeof userCookie.value === 'string' ? JSON.parse(userCookie.value) : userCookie.value
    } catch {
      return null
    }
  }
  return null
})

const myUuid = computed(() => {
  if (currentUser.value?.id) return String(currentUser.value.id)
  if (currentUser.value?.uuid) return String(currentUser.value.uuid)
  return parseJwtUserId(tokenCookie.value) || ''
})

const selectableUsers = computed(() => {
  if (!availableUsers.value || !Array.isArray(availableUsers.value)) return []
  return availableUsers.value.filter((u: any) => {
    const uId = String(u.id || u.uuid || '')
    const uName = String(u.username || '')
    return uId !== myUuid.value && uName !== currentUser.value?.username
  })
})

const { connect: connectWS } = useWebSocket()

const toggleMessenger = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    fetchRooms()
    fetchTotalUnreadCount()
    if (activeRoom.value) {
      markAsRead(activeRoom.value.id)
      fetchRoomMessages(activeRoom.value.id)
    }
    nextTick(() => {
      scrollToBottom()
    })
    setTimeout(() => {
      scrollToBottom()
    }, 150)
    setTimeout(() => {
      scrollToBottom()
    }, 300)
  }
}

const fetchRooms = async () => {
  if (!tokenCookie.value) return
  try {
    rooms.value = await $fetch('/api/chat/rooms', {
      headers: { Authorization: `Bearer ${tokenCookie.value}` }
    })
  } catch (e) {}
}



const fetchRoomMessages = async (roomId: string) => {
  if (!tokenCookie.value) return
  try {
    messages.value = await $fetch(`/api/chat/rooms/${roomId}/messages`, {
      headers: { Authorization: `Bearer ${tokenCookie.value}` }
    })
    scrollToBottom()
  } catch (e) {}
}

const isMyMsg = (msg: any) => {
  return String(msg.senderId) === String(myUuid.value)
}

const sendTextMessage = async () => {
  if (!inputMsg.value.trim() || !activeRoom.value) return
  const text = inputMsg.value.trim()
  inputMsg.value = ''
  await postMessage('TEXT', text)
}

const sendEmoji = async (emoji: string) => {
  if (!activeRoom.value) return
  await postMessage('EMOJI', emoji)
}

const postMessage = async (type: string, content: string, fileUrl?: string, fileName?: string, fileSize?: number) => {
  try {
    const res = await $fetch(`/api/chat/rooms/${activeRoom.value.id}/messages`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${tokenCookie.value}` },
      body: {
        roomId: activeRoom.value.id,
        senderId: myUuid.value,
        messageType: type,
        content,
        fileUrl,
        fileName,
        fileSize
      }
    })
    messages.value.push(res)
    scrollToBottom()
  } catch (e) {}
}

const handlePaste = async (event: ClipboardEvent) => {
  if (!isOpen.value || !activeRoom.value) return
  const clipboardData = event.clipboardData || (event as any).originalEvent?.clipboardData
  if (!clipboardData) return

  const items = clipboardData.items
  const files = clipboardData.files

  let imageFile: File | null = null

  if (items && items.length > 0) {
    for (let i = 0; i < items.length; i++) {
      if (items[i].type && items[i].type.indexOf('image') !== -1) {
        imageFile = items[i].getAsFile()
        break
      }
    }
  }

  if (!imageFile && files && files.length > 0) {
    for (let i = 0; i < files.length; i++) {
      if (files[i].type && files[i].type.indexOf('image') !== -1) {
        imageFile = files[i]
        break
      }
    }
  }

  if (imageFile) {
    event.preventDefault()
    const formData = new FormData()
    formData.append('file', imageFile, 'paste_image.png')
    try {
      const res: any = await $fetch('/api/chat/upload', {
        method: 'POST',
        headers: { Authorization: `Bearer ${tokenCookie.value}` },
        body: formData
      })
      if (res && res.fileUrl) {
        await postMessage('IMAGE', '[이미지 캡처]', res.fileUrl)
        scrollToBottom()
      }
    } catch (e) {
      console.error('Failed to upload pasted image:', e)
    }
  }
}

const previewImg = (url: string) => {
  previewImgUrl.value = url
  showImgModal.value = true
}

const createNewRoom = async () => {
  if (!tokenCookie.value) return
  try {
    const finalMembers = Array.from(new Set([...(selectedUserIds.value || []), myUuid.value])).filter(Boolean)
    const room = await $fetch('/api/chat/rooms', {
      method: 'POST',
      headers: { Authorization: `Bearer ${tokenCookie.value}` },
      body: {
        roomName: newRoomName.value || '신규 그룹방',
        isGroup: true,
        memberUserIds: finalMembers
      }
    })
    showCreateModal.value = false
    newRoomName.value = ''
    selectedUserIds.value = []
    fetchRooms()
    selectRoom(room)
  } catch (e) {}
}

const showMembersModal = async () => {
  if (!activeRoom.value || !tokenCookie.value) return
  try {
    roomMembers.value = await $fetch(`/api/chat/rooms/${activeRoom.value.id}/members`, {
      headers: { Authorization: `Bearer ${tokenCookie.value}` }
    })
    showMembersModalFlag.value = true
  } catch (e) {}
}

const isMe = (member: any) => {
  const mId = String(member.userId || '')
  return mId === myUuid.value || member.username === currentUser.value?.username
}

const isCreator = (member: any) => {
  if (!activeRoom.value) return false
  const cId = String(activeRoom.value.createdBy || '')
  return cId === String(member.userId || '')
}

const totalUnreadCount = ref(0)

const fetchTotalUnreadCount = async () => {
  if (!tokenCookie.value) return
  try {
    const res = await $fetch('/api/chat/unread-count', {
      headers: { Authorization: `Bearer ${tokenCookie.value}` }
    })
    totalUnreadCount.value = Number(res || 0)
  } catch (e) {}
}

const selectRoom = async (room: any) => {
  activeRoom.value = room
  await markAsRead(room.id)
  await fetchRoomMessages(room.id)
  scrollToBottom()
}

const markAsRead = async (roomId: string) => {
  if (!tokenCookie.value) return
  try {
    await $fetch(`/api/chat/rooms/${roomId}/read`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${tokenCookie.value}` }
    })
    fetchTotalUnreadCount()
  } catch (e) {}
}

const formatTime = (timeStr: any) => {
  if (!timeStr) return ''
  try {
    const date = new Date(timeStr)
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  } catch {
    return ''
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (msgContainer.value) {
      msgContainer.value.scrollTop = msgContainer.value.scrollHeight
    }
  })
  setTimeout(() => {
    if (msgContainer.value) {
      msgContainer.value.scrollTop = msgContainer.value.scrollHeight
    }
  }, 50)
  setTimeout(() => {
    if (msgContainer.value) {
      msgContainer.value.scrollTop = msgContainer.value.scrollHeight
    }
  }, 180)
}

const handleIncomingChatMessage = (event: any) => {
  const detail = event?.detail
  if (!detail) return
  
  fetchTotalUnreadCount()
  fetchRooms()

  // 메신저 모달 창이 열려 있고(isOpen) 해당 방을 활성 열람 중일 때만 읽음 처리!
  if (isOpen.value && activeRoom.value && String(activeRoom.value.id) === String(detail.roomId)) {
    fetchRoomMessages(activeRoom.value.id)
    markAsRead(activeRoom.value.id)
  }
}

const handleRoomRead = (event: any) => {
  const detail = event?.detail
  if (!detail) return

  if (activeRoom.value && String(activeRoom.value.id) === String(detail.roomId)) {
    fetchRoomMessages(activeRoom.value.id)
  }
}

const onContextMenu = (e: MouseEvent, msg: any) => {
  contextMenu.value = {
    show: true,
    x: e.clientX,
    y: e.clientY,
    msg
  }
}

const closeContextMenu = () => {
  contextMenu.value.show = false
}

const copyMsgContent = () => {
  const targetMsg = contextMenu.value.msg
  closeContextMenu()
  if (!targetMsg) return

  let textToCopy = targetMsg.content || ''
  if (targetMsg.messageType === 'IMAGE') textToCopy = targetMsg.fileUrl || targetMsg.content || ''
  else if (targetMsg.messageType === 'FILE') textToCopy = targetMsg.fileUrl || ''

  if (process.client && navigator.clipboard) {
    navigator.clipboard.writeText(textToCopy)
  }
}

const openForwardModal = () => {
  const targetMsg = contextMenu.value.msg
  closeContextMenu()
  if (!targetMsg) return
  contextMenu.value.msg = targetMsg
  showForwardModalFlag.value = true
}

const forwardToUser = async (user: any) => {
  const targetMsg = contextMenu.value.msg
  showForwardModalFlag.value = false
  if (!targetMsg || !tokenCookie.value) return

  try {
    const uId = String(user.id || user.uuid || user.username)
    const room: any = await $fetch('/api/chat/rooms', {
      method: 'POST',
      headers: { Authorization: `Bearer ${tokenCookie.value}` },
      body: {
        roomName: user.username + '님과의 대화',
        isGroup: false,
        memberUserIds: [uId, myUuid.value]
      }
    })

    const sender = targetMsg.senderName || 'User'
    const timeInfo = formatTime(targetMsg.createdAt)
    let forwardContent = `${t('messenger.forwardedPrefix')}\n👤 ${t('messenger.writerLabel')}: ${sender} (${timeInfo})\n💬 ${t('messenger.contentLabel')}: ${targetMsg.content}`
    if (targetMsg.messageType === 'IMAGE') {
      forwardContent = `${t('messenger.forwardedImgPrefix')}\n👤 ${t('messenger.writerLabel')}: ${sender} (${timeInfo})`
    }

    await $fetch(`/api/chat/rooms/${room.id}/messages`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${tokenCookie.value}` },
      body: {
        roomId: room.id,
        senderId: myUuid.value,
        messageType: targetMsg.messageType,
        content: forwardContent,
        fileUrl: targetMsg.fileUrl,
        fileName: targetMsg.fileName,
        fileSize: targetMsg.fileSize
      }
    })

    fetchRooms()
    selectRoom(room)
  } catch (e) {}
}

const deleteMsg = async () => {
  const targetMsg = contextMenu.value.msg
  closeContextMenu()
  if (!targetMsg || !tokenCookie.value) return

  try {
    await $fetch(`/api/chat/messages/${targetMsg.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${tokenCookie.value}` }
    })
    messages.value = messages.value.filter((m: any) => String(m.id) !== String(targetMsg.id))
  } catch (e) {}
}

const handleMessageDeleted = (event: any) => {
  const detail = event?.detail
  if (!detail) return
  if (activeRoom.value && String(activeRoom.value.id) === String(detail.roomId)) {
    messages.value = messages.value.filter((m: any) => String(m.id) !== String(detail.messageId))
  }
}

onMounted(async () => {
  if (process.client) {
    window.addEventListener('chat-message-received', handleIncomingChatMessage)
    window.addEventListener('chat-room-read', handleRoomRead)
    window.addEventListener('chat-message-deleted', handleMessageDeleted)
    window.addEventListener('paste', handlePaste)
    window.addEventListener('click', closeContextMenu)
  }
  if (tokenCookie.value) {
    try {
      availableUsers.value = await $fetch('/api/users', {
        headers: { Authorization: `Bearer ${tokenCookie.value}` }
      })
      fetchTotalUnreadCount()
      fetchRooms()
    } catch (e) {}
  }
})

onUnmounted(() => {
  if (process.client) {
    window.removeEventListener('chat-message-received', handleIncomingChatMessage)
    window.removeEventListener('chat-room-read', handleRoomRead)
    window.removeEventListener('chat-message-deleted', handleMessageDeleted)
    window.removeEventListener('paste', handlePaste)
    window.removeEventListener('click', closeContextMenu)
  }
})
</script>

<style scoped>
@keyframes blink-pulse {
  0% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.7);
  }
  50% {
    transform: scale(1.1);
    box-shadow: 0 0 0 14px rgba(239, 68, 68, 0);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(239, 68, 68, 0);
  }
}

.has-unread-pulse {
  animation: blink-pulse 1.2s infinite ease-in-out !important;
  background-color: #ef4444 !important;
}
</style>
