<template>
  <AppModal
    :model-value="modelValue"
    @update:model-value="val => emit('update:modelValue', val)"
    :title="$t('inbox.title')"
    icon="mail"
    size="large"
    custom-class="inbox-app-modal-wrapper"
    :show-maximize="true"
    :no-padding="true"
    :hide-default-actions="true"
    class="inbox-app-modal"
  >
    <!-- View Mode Switcher in Modal Header -->
    <template #header-actions>
      <div class="view-mode-toggle-group">
        <va-button
          preset="secondary"
          size="small"
          icon="vertical_split"
          class="view-mode-btn"
          :class="{ 'is-active': viewMode === 'split' }"
          :title="$t('inbox.view_mode_split', '좌우 분할 보기')"
          @click="setViewMode('split')"
        />
        <va-button
          preset="secondary"
          size="small"
          icon="table_rows"
          class="view-mode-btn"
          :class="{ 'is-active': viewMode === 'list' }"
          :title="$t('inbox.view_mode_list', '목록 전용 보기 (클릭 시 팝업)')"
          @click="setViewMode('list')"
        />
      </div>
    </template>

    <div
      v-if="modelValue"
      ref="containerRef"
      :class="['inbox-modal-content', `mode-${viewMode}`]"
      :style="viewMode === 'split' ? {
        '--inbox-sidebar-width': '220px',
        '--inbox-list-width': `${listPaneWidth}px`
      } : {
        '--inbox-sidebar-width': '220px'
      }"
    >
      <!-- Pane 1: Folder Sidebar -->
      <InboxFolderSidebar
        :activeFolder="activeFolder"
        :folderCounts="folderCounts"
        @select-folder="onSelectFolder"
        @compose="openCompose"
        class="inbox-sidebar-pane"
      />
      
      <!-- Pane 2: Message List (AG-Grid with Wide Area) -->
      <InboxMessageList
        :folder="activeFolder"
        v-model:searchKeyword="searchKeyword"
        @select-message="onSelectMessage"
        @refresh="refreshData"
        class="inbox-list-pane"
      />

      <!-- Draggable Splitter Divider (Split Mode Only) -->
      <div
        v-if="viewMode === 'split'"
        class="inbox-splitter-gutter"
        :class="{ 'is-dragging': isDraggingSplitter }"
        @mousedown="startSplitterDrag"
        @dblclick="resetSplitterWidth"
        :title="$t('inbox.drag_to_resize')"
      >
        <div class="splitter-handle-bar"></div>
      </div>
      
      <!-- Pane 3: Message Detail View (Split Mode Only) -->
      <InboxMessageDetail
        v-if="viewMode === 'split' && (selectedMessage || showDetail)"
        :message="selectedMessage"
        :activeFolder="activeFolder"
        @reply="openReply"
        @reply-all="openReplyAll"
        @forward="openForward"
        @delete="deleteMessage"
        @archive="archiveMessage"
        @refresh="refreshData"
        class="inbox-detail-pane"
      />
    </div>

    <!-- Detail Modal (List View Mode) -->
    <AppModal
      v-model="showDetailModal"
      size="large"
      :show-maximize="true"
      :show-close="true"
      :title="detailModalTitle"
      icon="mail"
      custom-class="inbox-detail-modal-wrapper"
      hide-default-actions
    >
      <InboxMessageDetail
        v-if="selectedMessage"
        :message="selectedMessage"
        :activeFolder="activeFolder"
        @reply="onDetailReply"
        @reply-all="onDetailReplyAll"
        @forward="onDetailForward"
        @delete="onDetailDelete"
        @archive="onDetailArchive"
        @refresh="refreshData"
      />
    </AppModal>

    <!-- Compose Modal -->
    <InboxComposeModal
      v-model="showComposeModal"
      :mode="composeMode"
      :originalMessage="selectedMessage"
      @sent="onMessageSent"
      @drafted="onMessageSent"
    />
  </AppModal>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import AppModal from '~/components/common/AppModal.vue'
import InboxFolderSidebar from '~/components/inbox/InboxFolderSidebar.vue'
import InboxMessageList from '~/components/inbox/InboxMessageList.vue'
import InboxMessageDetail from '~/components/inbox/InboxMessageDetail.vue'
import InboxComposeModal from '~/components/inbox/InboxComposeModal.vue'
import { useInbox, type InboxMessage, type FolderCount } from '~/composables/useInbox'

const props = defineProps<{
  modelValue: boolean
  initialMessageId?: string | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'refresh-counts'): void
}>()

const { t } = useI18n()
const {
  fetchFolderCounts,
  fetchMessage,
  markAsRead,
  moveToFolder,
  moveToTrash
} = useInbox()

const containerRef = ref<HTMLElement | null>(null)
const listPaneWidth = ref(620) // Default wide grid area in split mode
const isDraggingSplitter = ref(false)
const viewMode = ref<'split' | 'list'>('split')

const activeFolder = ref('INBOX')
const folderCounts = ref<FolderCount[]>([])
const searchKeyword = ref('')
const selectedMessage = ref<InboxMessage | null>(null)
const showDetail = ref(true)

const showDetailModal = ref(false)
const showComposeModal = ref(false)
const composeMode = ref<'compose' | 'reply' | 'replyAll' | 'forward'>('compose')

const detailModalTitle = computed(() => {
  return selectedMessage.value?.subject || t('inbox.message_detail_modal', '메시지 상세 조회')
})

const setViewMode = (mode: 'split' | 'list') => {
  viewMode.value = mode
  if (typeof window !== 'undefined') {
    try {
      localStorage.setItem('inbox_view_mode', mode)
    } catch (e) {
      console.debug('Failed to save view mode to localStorage', e)
    }
    window.dispatchEvent(new Event('resize'))
  }
}

const startSplitterDrag = (e: MouseEvent) => {
  e.preventDefault()
  isDraggingSplitter.value = true
  if (typeof document !== 'undefined') {
    document.body.style.cursor = 'col-resize'
    document.body.style.userSelect = 'none'
  }

  const startX = e.clientX
  const initialWidth = listPaneWidth.value

  const onMouseMove = (moveEvent: MouseEvent) => {
    if (!isDraggingSplitter.value) return
    const deltaX = moveEvent.clientX - startX
    const newWidth = initialWidth + deltaX

    const containerWidth = containerRef.value?.clientWidth || 1200
    const minWidth = 360
    const maxWidth = Math.max(minWidth, containerWidth - 220 - 320)

    listPaneWidth.value = Math.min(Math.max(newWidth, minWidth), maxWidth)
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new Event('resize'))
    }
  }

  const onMouseUp = () => {
    isDraggingSplitter.value = false
    if (typeof document !== 'undefined') {
      document.body.style.cursor = ''
      document.body.style.userSelect = ''
    }
    window.removeEventListener('mousemove', onMouseMove)
    window.removeEventListener('mouseup', onMouseUp)
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new Event('resize'))
    }
  }

  window.addEventListener('mousemove', onMouseMove)
  window.addEventListener('mouseup', onMouseUp)
}

const resetSplitterWidth = () => {
  listPaneWidth.value = 620
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new Event('resize'))
  }
}

const loadFolderCounts = async () => {
  try {
    const data: any = await fetchFolderCounts()
    if (data) {
      folderCounts.value = Array.isArray(data) ? data : (data.content || data.data || [])
    }
  } catch (e) {
    console.debug('Failed to fetch folder counts:', e)
  }
}

watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    loadFolderCounts()
    if (props.initialMessageId) {
      loadInitialMessage(props.initialMessageId)
    }
  }
})

const loadInitialMessage = async (msgId: string) => {
  try {
    const msg = await fetchMessage(msgId)
    if (msg) {
      selectedMessage.value = msg
      showDetail.value = true
      if (viewMode.value === 'list') {
        showDetailModal.value = true
      }
      if (!msg.isRead) {
        msg.isRead = true
        await markAsRead(msg.id)
      }
      await loadFolderCounts()
      emit('refresh-counts')
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('inbox-refresh-counts'))
        window.dispatchEvent(new CustomEvent('inbox-message-read', { detail: { messageId: msg.id } }))
      }
    }
  } catch (e) {
    console.error('Failed to load initial message:', e)
  }
}

const onSelectFolder = (folderKey: string) => {
  activeFolder.value = folderKey
  selectedMessage.value = null
}

const onSelectMessage = async (message: InboxMessage) => {
  selectedMessage.value = message
  showDetail.value = true
  if (viewMode.value === 'list') {
    showDetailModal.value = true
  }

  // If unread, mark as read on backend and refresh counts
  if (message && !message.isRead) {
    message.isRead = true
    try {
      await markAsRead(message.id)
      await loadFolderCounts()
      emit('refresh-counts')
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('inbox-refresh-counts'))
        window.dispatchEvent(new CustomEvent('inbox-message-read', { detail: { messageId: message.id } }))
      }
    } catch (e) {
      console.debug('Failed to mark message as read:', e)
    }
  }

  // Also fetch the full message detail in background to ensure all fields are populated
  try {
    const detailed = await fetchMessage(message.id)
    if (detailed && selectedMessage.value?.id === message.id) {
      selectedMessage.value = detailed
    }
  } catch (e) {}
}

const openCompose = () => {
  composeMode.value = 'compose'
  showComposeModal.value = true
}

const openReply = (msg: InboxMessage) => {
  selectedMessage.value = msg
  composeMode.value = 'reply'
  showComposeModal.value = true
}

const openReplyAll = (msg: InboxMessage) => {
  selectedMessage.value = msg
  composeMode.value = 'replyAll'
  showComposeModal.value = true
}

const openForward = (msg: InboxMessage) => {
  selectedMessage.value = msg
  composeMode.value = 'forward'
  showComposeModal.value = true
}

const deleteMessage = async (msgId: string) => {
  if (activeFolder.value === 'TRASH') {
    await moveToFolder(msgId, 'PERMANENT_DELETE')
  } else {
    await moveToTrash(msgId)
  }
  selectedMessage.value = null
  refreshData()
}

const archiveMessage = async (msgId: string) => {
  await moveToFolder(msgId, 'ARCHIVE')
  selectedMessage.value = null
  refreshData()
}

const onDetailReply = (msg: InboxMessage) => {
  showDetailModal.value = false
  openReply(msg)
}

const onDetailReplyAll = (msg: InboxMessage) => {
  showDetailModal.value = false
  openReplyAll(msg)
}

const onDetailForward = (msg: InboxMessage) => {
  showDetailModal.value = false
  openForward(msg)
}

const onDetailDelete = async (msgId: string) => {
  showDetailModal.value = false
  await deleteMessage(msgId)
}

const onDetailArchive = async (msgId: string) => {
  showDetailModal.value = false
  await archiveMessage(msgId)
}

const onMessageSent = () => {
  showComposeModal.value = false
  refreshData()
}

const refreshData = () => {
  loadFolderCounts()
  emit('refresh-counts')
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new CustomEvent('inbox-refresh-counts'))
  }
}

onMounted(() => {
  if (typeof window !== 'undefined') {
    const saved = localStorage.getItem('inbox_view_mode')
    if (saved === 'list' || saved === 'split') {
      viewMode.value = saved
    }
  }
  if (props.modelValue) {
    loadFolderCounts()
  }
})
</script>

<style>
/* Global unscoped style for Inbox Modals (Parent Inbox, Detail Popup Modal, Compose Modal) width expansion (non-fullscreen) */
.inbox-app-modal-wrapper:not(.va-modal--fullscreen) .va-modal__dialog,
.inbox-app-modal:not(.va-modal--fullscreen) .va-modal__dialog,
.inbox-detail-modal-wrapper:not(.va-modal--fullscreen) .va-modal__dialog,
.inbox-compose-modal-wrapper:not(.va-modal--fullscreen) .va-modal__dialog {
  width: 92vw !important;
  max-width: 1560px !important;
  min-width: 960px !important;
}

/* Fullscreen expansion */
.inbox-app-modal-wrapper.va-modal--fullscreen .va-modal__dialog,
.inbox-app-modal.va-modal--fullscreen .va-modal__dialog,
.inbox-detail-modal-wrapper.va-modal--fullscreen .va-modal__dialog,
.inbox-compose-modal-wrapper.va-modal--fullscreen .va-modal__dialog,
.va-modal--fullscreen .va-modal__dialog {
  width: 100vw !important;
  max-width: 100vw !important;
  min-width: 100vw !important;
  height: 100vh !important;
  max-height: 100vh !important;
  min-height: 100vh !important;
  border-radius: 0 !important;
  margin: 0 !important;
  top: 0 !important;
  left: 0 !important;
  transform: none !important;
}

@media (max-width: 1024px) {
  .inbox-app-modal-wrapper:not(.va-modal--fullscreen) .va-modal__dialog,
  .inbox-app-modal:not(.va-modal--fullscreen) .va-modal__dialog,
  .inbox-detail-modal-wrapper:not(.va-modal--fullscreen) .va-modal__dialog,
  .inbox-compose-modal-wrapper:not(.va-modal--fullscreen) .va-modal__dialog {
    width: 96vw !important;
    max-width: 96vw !important;
    min-width: 0 !important;
  }
}
</style>

<style scoped>

/* Header Actions: View Mode Switcher */
.view-mode-toggle-group {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  background: var(--va-background-element, #f1f5f9);
  padding: 2px;
  border-radius: 6px;
  border: 1px solid var(--va-background-border, #e2e8f0);
}

:global([data-vuestic-preset="dark"]) .view-mode-toggle-group,
:global(.va-theme-dark) .view-mode-toggle-group {
  background: #1e293b !important;
  border-color: #334155 !important;
}

.view-mode-btn {
  padding: 3px 6px !important;
  height: 26px !important;
  min-width: 28px !important;
  border-radius: 4px !important;
  color: var(--va-secondary, #64748b) !important;
  transition: all 0.2s ease;
}

.view-mode-btn.is-active {
  background: #3b82f6 !important;
  color: #ffffff !important;
  font-weight: 700;
}

/* Modal Content Container */
.inbox-modal-content {
  height: calc(85vh - 70px);
  min-height: 600px;
  width: 100%;
  overflow: hidden;
  background: var(--va-background-primary);
  position: relative;
}

/* Split View Mode Layout */
.inbox-modal-content.mode-split {
  display: grid;
  grid-template-columns: var(--inbox-sidebar-width, 220px) var(--inbox-list-width, 620px) 6px 1fr;
}

/* List Only View Mode Layout */
.inbox-modal-content.mode-list {
  display: grid;
  grid-template-columns: var(--inbox-sidebar-width, 220px) 1fr;
}

.inbox-sidebar-pane {
  height: 100%;
  border-right: 1px solid var(--va-background-border);
  overflow-y: auto;
}

.inbox-list-pane {
  height: 100%;
  overflow: hidden;
}

/* Resizable Splitter Gutter */
.inbox-splitter-gutter {
  width: 6px;
  height: 100%;
  background: var(--va-background-border);
  cursor: col-resize;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  user-select: none;
  transition: background-color 0.15s ease;
  z-index: 5;
}

.inbox-splitter-gutter:hover,
.inbox-splitter-gutter.is-dragging {
  background: var(--va-primary);
}

.splitter-handle-bar {
  width: 2px;
  height: 32px;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.4);
  transition: all 0.15s ease;
}

.inbox-splitter-gutter:hover .splitter-handle-bar,
.inbox-splitter-gutter.is-dragging .splitter-handle-bar {
  background: #ffffff;
  height: 48px;
}

.inbox-detail-pane {
  height: 100%;
  overflow-y: auto;
  min-width: 320px;
}

:deep(.va-modal--fullscreen) .inbox-modal-content,
.va-modal--fullscreen .inbox-modal-content {
  height: calc(100vh - 65px) !important;
  max-height: calc(100vh - 65px) !important;
}

@media (max-width: 1024px) {
  .inbox-modal-content.mode-split {
    grid-template-columns: 180px 1fr;
  }
  .inbox-splitter-gutter {
    display: none;
  }
  .inbox-detail-pane {
    display: none;
  }
}
</style>
