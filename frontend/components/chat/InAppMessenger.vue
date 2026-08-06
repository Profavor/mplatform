<!-- InAppMessenger.vue: In-App Messenger Component -->
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
      ref="messengerPanelRef"
      class="messenger-panel"
      :style="panelComputedStyle"
    >
      <!-- 8-Direction Edge & Corner Resize Handles -->
      <div class="resize-handle-top" style="position: absolute; top: 0; left: 0; width: 100%; height: 8px; cursor: ns-resize; z-index: 1002;" @mousedown="startResize('top', $event)" />
      <div class="resize-handle-bottom" style="position: absolute; bottom: 0; left: 0; width: 100%; height: 8px; cursor: ns-resize; z-index: 1002;" @mousedown="startResize('bottom', $event)" />
      <div class="resize-handle-left" style="position: absolute; top: 0; left: 0; width: 8px; height: 100%; cursor: ew-resize; z-index: 1002;" @mousedown="startResize('left', $event)" />
      <div class="resize-handle-right" style="position: absolute; top: 0; right: 0; width: 8px; height: 100%; cursor: ew-resize; z-index: 1002;" @mousedown="startResize('right', $event)" />

      <div class="resize-handle-top-left" style="position: absolute; top: 0; left: 0; width: 14px; height: 14px; cursor: nwse-resize; z-index: 1003;" @mousedown="startResize('top-left', $event)" />
      <div class="resize-handle-top-right" style="position: absolute; top: 0; right: 0; width: 14px; height: 14px; cursor: nesw-resize; z-index: 1003;" @mousedown="startResize('top-right', $event)" />
      <div class="resize-handle-bottom-left" style="position: absolute; bottom: 0; left: 0; width: 14px; height: 14px; cursor: nesw-resize; z-index: 1003;" @mousedown="startResize('bottom-left', $event)" />
      <div class="resize-handle-bottom-right" style="position: absolute; bottom: 0; right: 0; width: 14px; height: 14px; cursor: nwse-resize; z-index: 1003;" @mousedown="startResize('bottom-right', $event)" />
      <!-- Header (Draggable Handle) -->
      <div
        class="messenger-header"
        style="padding: 14px 16px; background: var(--va-primary); color: white; display: flex; align-items: center; justify-content: space-between; cursor: move; user-select: none;"
        @mousedown="startDrag"
      >
        <div style="display: flex; align-items: center; gap: 8px;">
          <va-button v-if="activeRoom" preset="plain" color="#ffffff" size="small" @click.stop="activeRoom = null">
            <va-icon name="arrow_back" size="20px" />
          </va-button>
          <span style="font-weight: 700; font-size: 1rem;">
            {{ activeRoom ? activeRoom.name : $t('messenger.title') }}
          </span>
        </div>
        <div style="display: flex; gap: 4px; align-items: center;">
          <va-button preset="plain" color="#ffffff" size="small" :title="isExpanded ? '창 크기 축소' : '창 너비 확대'" @click.stop="toggleExpand">
            <va-icon :name="isExpanded ? 'fullscreen_exit' : 'fullscreen'" size="20px" />
          </va-button>
          <va-button v-if="activeRoom" preset="plain" color="#ffffff" size="small" :title="$t('messenger.viewMembersTooltip')" @click.stop="showMembersModal">
            <va-icon name="group" size="20px" />
          </va-button>
          <va-button v-if="!activeRoom" preset="plain" color="#ffffff" size="small" :title="$t('messenger.createRoomTooltip')" @click.stop="showCreateModal = true">
            <va-icon name="group_add" size="20px" />
          </va-button>
          <va-button preset="plain" color="#ffffff" size="small" @click.stop="isOpen = false">
            <va-icon name="close" size="20px" />
          </va-button>
        </div>
      </div>

      <!-- Copy Toast Notification -->
      <va-badge v-if="copyToast" color="success" style="position: absolute; top: 54px; left: 50%; transform: translateX(-50%); z-index: 1005; box-shadow: 0 4px 12px rgba(0,0,0,0.25); padding: 4px 10px;">
        {{ copyToastMsg }}
      </va-badge>

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
      <div v-else class="chat-room-view" style="flex: 1; display: flex; flex-direction: column; overflow-x: hidden; overflow-y: hidden; width: 100%;">
        <!-- Messages Area (block layout for continuous text selection like KakaoTalk) -->
        <div ref="msgContainer" class="messages-area" style="flex: 1; overflow-y: auto; overflow-x: hidden; padding: 14px; display: block; width: 100%;" @dragover.prevent @drop.prevent="handleDrop" @mousedown="onMsgAreaMouseDown" @mousemove="onMsgAreaMouseMove" @mouseup="onMsgAreaMouseUp">
          <template v-for="(msg, idx) in messages" :key="msg.id">
            <!-- Date Separator (KakaoTalk style, clickable to open calendar) -->
            <div v-if="shouldShowDateSeparator(idx)" class="msg-date-separator" :data-date="getDateKey(msg.createdAt)" style="display: flex; align-items: center; justify-content: center; margin: 16px 0 12px 0; user-select: none; gap: 10px; cursor: pointer;" @click.stop="openCalendarDialog(msg.createdAt)">
              <div style="flex: 1; height: 1px; background: var(--va-background-border);"></div>
              <span class="msg-date-pill" style="font-size: 0.78rem; color: var(--va-text-secondary); background: var(--va-background-element); padding: 4px 14px; border-radius: 20px; white-space: nowrap; font-weight: 600; display: inline-flex; align-items: center; gap: 5px; transition: all 0.15s;">
                <span style="font-size: 0.85rem;">📅</span> {{ formatDateSeparator(msg.createdAt) }}
              </span>
              <div style="flex: 1; height: 1px; background: var(--va-background-border);"></div>
            </div>
            <div class="msg-bubble-wrapper" :data-sender="msg.senderName" :data-time="formatDateTime(msg.createdAt)" :data-msg-id="msg.id" :data-type="msg.messageType" :style="{ marginLeft: isMyMsg(msg) ? 'auto' : '0', marginRight: isMyMsg(msg) ? '0' : 'auto', maxWidth: msg.messageType === 'IMAGE' ? '92%' : (parseTableContent(msg.content).isTable ? '96%' : '85%'), width: 'fit-content', marginBottom: '10px' }">
            <!-- Sender Name (only for other people's messages visually) -->
            <div v-if="!isMyMsg(msg)" class="msg-sender-name" style="font-size: 0.75rem; color: var(--va-text-secondary); margin-bottom: 3px; font-weight: 700; user-select: none; display: block;">
              {{ msg.senderName }}
            </div>
            
            <div style="display: flex; align-items: flex-end; gap: 6px; max-width: 100%;" :style="{ flexDirection: isMyMsg(msg) ? 'row' : 'row-reverse' }">
              <!-- Unread Count & Time (Visual only, excluded from text selection) -->
              <div
                class="msg-time-area"
                style="font-size: 0.7rem; color: var(--va-text-secondary); display: flex; flex-direction: column; align-items: flex-end; gap: 1px; flex-shrink: 0; user-select: none !important; -webkit-user-select: none !important; -moz-user-select: none !important;"
              >
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
                  color: isMyMsg(msg) ? '#ffffff' : 'var(--va-text-primary)',
                  fontSize: '0.9rem',
                  wordBreak: 'break-word',
                  userSelect: 'text',
                  webkitUserSelect: 'text',
                  mozUserSelect: 'text',
                  cursor: 'text',
                  maxWidth: '100%',
                  overflowX: 'hidden'
                }"
              >
                <!-- TEXT & EMOJI -->
                <div v-if="msg.messageType === 'TEXT' || msg.messageType === 'EMOJI'">
                  <!-- Table Data Rendering if content is TSV/Markdown/CSV table -->
                  <div v-if="parseTableContent(msg.content).isTable" style="margin: 4px 0;">
                    <div style="display: flex; justify-content: flex-end; align-items: center; margin-bottom: 6px; gap: 4px;">
                      <va-button
                        size="small"
                        preset="plain"
                        :style="{
                          background: isMyMsg(msg) ? 'rgba(255,255,255,0.25)' : 'rgba(0,0,0,0.08)',
                          borderRadius: '6px',
                          padding: '2px 6px',
                          fontSize: '0.75rem',
                          fontWeight: '700',
                          color: isMyMsg(msg) ? '#ffffff' : 'var(--va-primary)',
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '3px'
                        }"
                        :title="$t('open_table_modal_btn_title')"
                        @click.stop="openTableDataModal(msg)"
                      >
                        <va-icon name="zoom_in" size="14px" />
                        <span>{{ $t('open_table_modal_btn') }}</span>
                      </va-button>

                      <va-button
                        size="small"
                        preset="plain"
                        :style="{
                          background: isMyMsg(msg) ? 'rgba(255,255,255,0.25)' : 'rgba(0,0,0,0.08)',
                          borderRadius: '6px',
                          padding: '2px 6px',
                          fontSize: '0.75rem',
                          fontWeight: '700',
                          color: isMyMsg(msg) ? '#ffffff' : 'var(--va-primary)',
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '3px'
                        }"
                        :title="$t('copy_table_btn_title')"
                        @click.stop="copyMsgContent(msg)"
                      >
                        <va-icon name="content_copy" size="14px" />
                        <span>{{ $t('copy_table_btn') }}</span>
                      </va-button>
                    </div>

                    <!-- HTML Table Grid inside Messenger -->
                    <div
                      style="overflow-x: auto; border: 2px solid #3f51b5; border-radius: 6px; max-width: 100%; box-shadow: 0 4px 12px rgba(0,0,0,0.15);"
                    >
                      <table style="width: 100%; border-collapse: collapse; font-size: 0.83rem; text-align: center; background: #ffffff;">
                        <thead>
                          <tr>
                            <th
                              v-for="(h, hIdx) in parseTableContent(msg.content).headers"
                              :key="hIdx"
                              :style="getCellStyle(h, 'header')"
                            >
                              {{ getCellText(h) }}
                            </th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr
                            v-for="(row, rIdx) in parseTableContent(msg.content).rows"
                            :key="rIdx"
                          >
                            <td
                              v-for="(val, cIdx) in row"
                              :key="cIdx"
                              :style="getCellStyle(val, 'data')"
                            >
                              {{ getCellText(val) }}
                            </td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                  </div>

                  <!-- Regular Text Rendering -->
                  <div v-else style="white-space: pre-wrap; user-select: text !important; -webkit-user-select: text !important; color: inherit; cursor: text;">
                    {{ msg.content }}
                  </div>

                  <!-- Translation Result Box -->
                  <div v-if="msg.showTranslation" style="margin-top: 8px; padding: 6px 10px; background: rgba(0,0,0,0.15); border-left: 3px solid #60a5fa; border-radius: 4px; font-size: 0.82rem; color: inherit;">
                    <div style="font-size: 0.68rem; opacity: 0.85; font-weight: 700; display: flex; align-items: center; gap: 4px;">
                      <span>🌐 {{ $t('messenger.translationResult') }}</span>
                    </div>
                    <div v-if="msg.isTranslating" style="font-style: italic; opacity: 0.7; margin-top: 2px;">
                      {{ $t('messenger.translating') }}
                    </div>
                    <div v-else style="margin-top: 2px;">
                      {{ msg.translatedText }}
                    </div>
                  </div>
                </div>
                
                <!-- IMAGE -->
                <div v-else-if="msg.messageType === 'IMAGE'">
                  <img
                    :src="getAuthenticatedImageUrl(msg.fileUrl || msg.content)"
                    style="max-width: 480px; width: 100%; max-height: 420px; border-radius: 10px; cursor: pointer; border: 1px solid rgba(0,0,0,0.12); box-shadow: 0 4px 14px rgba(0,0,0,0.15); object-fit: contain;"
                    :title="$t('click_image_to_expand_tip')"
                    @click="previewImg(getAuthenticatedImageUrl(msg.fileUrl || msg.content))"
                  />
                </div>

                <!-- FILE -->
                <div v-else-if="msg.messageType === 'FILE'" style="display: flex; align-items: center; gap: 8px; padding: 2px 0;">
                  <div style="background: rgba(255,255,255,0.2); padding: 8px; border-radius: 8px; display: flex; align-items: center; justify-content: center;">
                    <va-icon :name="getFileIcon(msg.fileName)" size="22px" :color="isMyMsg(msg) ? '#ffffff' : 'var(--va-primary)'" />
                  </div>
                  <div style="flex: 1; overflow: hidden;">
                    <div style="font-weight: 700; font-size: 0.85rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 120px;">
                      {{ msg.fileName || '첨부파일' }}
                    </div>
                    <div style="font-size: 0.72rem; opacity: 0.85;">
                      {{ formatFileSize(msg.fileSize) }}
                    </div>
                  </div>

                  <!-- Dedicated Excel Viewer Button -->
                  <va-button
                    v-if="isExcelFile(msg.fileName)"
                    size="small"
                    preset="plain"
                    :style="{
                      background: isMyMsg(msg) ? 'rgba(255,255,255,0.25)' : 'rgba(0,0,0,0.06)',
                      borderRadius: '12px',
                      padding: '2px 8px',
                      fontSize: '0.75rem',
                      fontWeight: '700',
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: '4px'
                    }"
                    :title="$t('excel_viewer_open')"
                    @click="openExcelViewer(msg)"
                  >
                    <va-icon name="table_chart" size="16px" :color="isMyMsg(msg) ? '#ffffff' : 'var(--va-primary)'" />
                    <span>{{ $t('excel_viewer_btn') }}</span>
                  </va-button>

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
          </template>
        </div>

        <!-- Calendar Jump Dialog -->
        <va-modal v-model="showCalendarDialog" hide-default-actions size="small" :title="$t('messenger.calendarTitle')" style="z-index: 99999;">
          <div style="padding: 8px 0;">
            <!-- Month Navigation -->
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px;">
              <va-button preset="plain" size="small" @click="calendarPrevMonth"><va-icon name="chevron_left" /></va-button>
              <span style="font-weight: 700; font-size: 1rem;">{{ calendarYear }}{{ $t('messenger.year') }} {{ calendarMonth }}{{ $t('messenger.month') }}</span>
              <va-button preset="plain" size="small" @click="calendarNextMonth"><va-icon name="chevron_right" /></va-button>
            </div>
            <!-- Day of Week Headers -->
            <div style="display: grid; grid-template-columns: repeat(7, 1fr); text-align: center; font-size: 0.75rem; color: var(--va-text-secondary); margin-bottom: 6px; font-weight: 600;">
              <span v-for="dow in calendarDowLabels" :key="dow">{{ dow }}</span>
            </div>
            <!-- Calendar Grid -->
            <div style="display: grid; grid-template-columns: repeat(7, 1fr); gap: 2px;">
              <div v-for="(cell, ci) in calendarCells" :key="ci"
                :style="{
                  textAlign: 'center',
                  padding: '7px 0',
                  borderRadius: '8px',
                  fontSize: '0.85rem',
                  fontWeight: cell.hasMessages ? '700' : '400',
                  color: cell.day === 0 ? 'transparent' : (cell.hasMessages ? 'var(--va-primary)' : 'var(--va-text-secondary)'),
                  background: cell.isSelected ? 'var(--va-primary)' : (cell.hasMessages ? 'rgba(59,130,246,0.1)' : 'transparent'),
                  cursor: cell.hasMessages ? 'pointer' : 'default',
                  opacity: cell.day === 0 ? '0' : (cell.hasMessages ? '1' : '0.4'),
                  transition: 'all 0.15s'
                }"
                :class="{ 'calendar-day-active': cell.hasMessages }"
                @click="cell.hasMessages && jumpToDate(cell.dateKey)"
              >
                <span :style="{ color: cell.isSelected ? '#ffffff' : undefined }">{{ cell.day || '' }}</span>
              </div>
            </div>
          </div>
        </va-modal>

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
            >
              <template #appendInner>
                <va-dropdown :close-on-content-click="false" trigger="click" placement="top-end">
                  <template #anchor>
                    <va-icon name="sentiment_satisfied_alt" size="small" style="cursor: pointer" />
                  </template>
                  <ClientOnly>
                    <EmojiPicker :native="true" @select="(e) => inputMsg += e.i" />
                  </ClientOnly>
                </va-dropdown>
              </template>
            </va-input>
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
    <va-modal v-model="showImgModal" size="large" hide-default-actions>
      <div style="padding: 4px; display: flex; justify-content: center; align-items: center;">
        <img :src="previewImgUrl" style="max-width: 90vw; max-height: 85vh; border-radius: 8px; object-fit: contain; cursor: pointer;" @click="showImgModal = false" />
      </div>
    </va-modal>

    <!-- Room Members Modal with Online/Offline Presence Status -->
    <va-modal v-model="showMembersModalFlag" :title="`👥 ${$t('messenger.roomMembersTitle')} (${roomMembers.length})`" hide-default-actions>
      <div style="max-height: 280px; overflow-y: auto; padding: 4px;">
        <div v-for="m in roomMembers" :key="m.userId" style="display: flex; align-items: center; justify-content: space-between; padding: 10px 12px; border-bottom: 1px solid var(--va-background-border);">
          <div style="display: flex; align-items: center; gap: 12px;">
            <!-- Avatar with Presence Status Dot -->
            <div style="position: relative; display: inline-flex;">
              <va-avatar size="small" color="primary">{{ (m.username || 'U').charAt(0).toUpperCase() }}</va-avatar>
              <span
                :style="{
                  position: 'absolute',
                  bottom: '-2px',
                  right: '-2px',
                  width: '10px',
                  height: '10px',
                  borderRadius: '50%',
                  background: isUserOnline(m) ? '#22c55e' : '#9ca3af',
                  border: '2px solid white',
                  boxShadow: '0 1px 3px rgba(0,0,0,0.3)'
                }"
                :title="isUserOnline(m) ? $t('messenger.onlineStatus') : $t('messenger.offlineStatus')"
              />
            </div>

            <div>
              <div style="font-weight: 700; font-size: 0.9rem; display: flex; align-items: center; gap: 6px;">
                <span>{{ m.username }}</span>
                <va-badge v-if="isMe(m)" color="success" size="small">{{ $t('messenger.meBadge') }}</va-badge>
                <va-badge v-if="isCreator(m)" color="warning" size="small">{{ $t('messenger.creatorBadge') }}</va-badge>
              </div>
              <div style="font-size: 0.75rem; color: var(--va-text-secondary); margin-top: 1px;">
                {{ m.role || 'USER' }}
              </div>
            </div>
          </div>

          <!-- Presence Status Badge -->
          <div style="display: flex; align-items: center; gap: 4px;">
            <va-badge
              :color="isUserOnline(m) ? 'success' : 'secondary'"
              size="small"
              style="font-size: 0.72rem; font-weight: 700;"
            >
              {{ isUserOnline(m) ? `🟢 ${$t('messenger.onlineStatus')}` : `⚪ ${$t('messenger.offlineStatus')}` }}
            </va-badge>
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
      :style="{ position: 'fixed', top: contextMenu.y + 'px', left: contextMenu.x + 'px', zIndex: 9999, background: 'var(--va-background-element)', border: '1px solid var(--va-background-border)', borderRadius: '8px', boxShadow: '0 8px 24px rgba(0,0,0,0.25)', padding: '4px 0', minWidth: '130px' }"
    >
      <div v-if="contextMenu.msg && (contextMenu.msg.messageType === 'TEXT' || contextMenu.msg.messageType === 'EMOJI')" style="padding: 8px 12px; cursor: pointer; display: flex; align-items: center; gap: 8px; font-size: 0.85rem;" @click="toggleTranslateMsg">
        <va-icon name="g_translate" size="16px" color="primary" /> {{ contextMenu.msg.showTranslation ? $t('messenger.hideTranslation') : $t('messenger.translateMessage') }}
      </div>
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

    <!-- Dedicated Excel Preview Modal -->
    <ExcelPreviewModal
      v-model="showExcelModal"
      :fileUrl="selectedExcelUrl"
      :fileName="selectedExcelName"
    />

    <!-- Dedicated Table Data Viewer Modal -->
    <TableDataViewerModal
      v-model="showTableDataModal"
      :headers="tableModalHeaders"
      :rows="tableModalRows"
      :rawContent="tableModalRawContent"
    />

    <!-- Paste Send Format Option Selection Modal -->
    <va-modal v-model="showPasteOptionModal" :title="$t('paste_option_title')" hide-default-actions>
      <div style="padding: 0.5rem; display: flex; flex-direction: column; gap: 1rem;">
        <p style="font-size: 0.9rem; color: var(--va-text-secondary); margin: 0;">
          {{ $t('paste_option_desc') }}
        </p>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; margin-top: 0.5rem;">
          <!-- Option 1: Send as Text / Table Data -->
          <div
            style="border: 2px solid var(--va-primary); padding: 1rem; border-radius: 10px; cursor: pointer; background: rgba(44, 130, 224, 0.05); text-align: center; transition: all 0.2s;"
            @click="sendPastedAsText"
          >
            <va-icon name="table_chart" size="32px" color="primary" style="margin-bottom: 8px;" />
            <div style="font-weight: 700; font-size: 0.95rem; color: var(--va-primary);">
              {{ $t('send_as_text_data') }}
            </div>
            <div style="font-size: 0.78rem; color: var(--va-text-secondary); margin-top: 4px;">
              (수신자가 셀 데이터를 복사 및 데이터로 활용 가능)
            </div>
          </div>

          <!-- Option 2: Send as Image -->
          <div
            style="border: 2px solid var(--va-background-border); padding: 1rem; border-radius: 10px; cursor: pointer; background: var(--va-background-element); text-align: center; transition: all 0.2s;"
            @click="sendPastedAsImage"
          >
            <va-icon name="image" size="32px" color="secondary" style="margin-bottom: 8px;" />
            <div style="font-weight: 700; font-size: 0.95rem;">
              {{ $t('send_as_image') }}
            </div>
            <div style="font-size: 0.78rem; color: var(--va-text-secondary); margin-top: 4px;">
              (시각적 캡처 이미지로 표 표출)
            </div>
          </div>
        </div>

        <div style="display: flex; justify-content: flex-end; margin-top: 0.5rem;">
          <va-button preset="secondary" @click="showPasteOptionModal = false">{{ $t('cancel') }}</va-button>
        </div>
      </div>
    </va-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, defineAsyncComponent } from 'vue'
import ExcelPreviewModal from '~/components/chat/ExcelPreviewModal.vue'
import TableDataViewerModal from '~/components/chat/TableDataViewerModal.vue'
const EmojiPicker = defineAsyncComponent(() => import('vue3-emoji-picker'))
import 'vue3-emoji-picker/css'

const { t } = useI18n()

// Drag & Resize Position State
const messengerPanelRef = ref<HTMLElement | null>(null)
const isExpanded = ref(false)
const panelPosition = ref({ x: 0, y: 0 })
const isDragging = ref(false)
const dragStart = ref({ x: 0, y: 0 })

const panelWidth = ref(380)
const panelHeight = ref(580)

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
  if (isExpanded.value) {
    panelWidth.value = 720
  } else {
    panelWidth.value = 380
  }
}

const startDrag = (e: MouseEvent) => {
  if ((e.target as HTMLElement).closest('button')) return

  if (messengerPanelRef.value && panelPosition.value.x === 0 && panelPosition.value.y === 0) {
    const rect = messengerPanelRef.value.getBoundingClientRect()
    panelPosition.value = { x: rect.left, y: rect.top }
  }

  isDragging.value = true
  dragStart.value = { x: e.clientX - panelPosition.value.x, y: e.clientY - panelPosition.value.y }
  window.addEventListener('mousemove', onDrag)
  window.addEventListener('mouseup', stopDrag)
}

const onDrag = (e: MouseEvent) => {
  if (!isDragging.value) return
  panelPosition.value = {
    x: e.clientX - dragStart.value.x,
    y: e.clientY - dragStart.value.y
  }
}

const stopDrag = () => {
  isDragging.value = false
  window.removeEventListener('mousemove', onDrag)
  window.removeEventListener('mouseup', stopDrag)
}

const activeResizeDirection = ref<string | null>(null)
const resizeStartData = ref({
  x: 0,
  y: 0,
  startW: 380,
  startH: 580,
  startX: 0,
  startY: 0
})

const startResize = (dir: string, e: MouseEvent) => {
  e.stopPropagation()
  e.preventDefault()

  if (messengerPanelRef.value && panelPosition.value.x === 0 && panelPosition.value.y === 0) {
    const rect = messengerPanelRef.value.getBoundingClientRect()
    panelPosition.value = { x: rect.left, y: rect.top }
  }

  activeResizeDirection.value = dir
  resizeStartData.value = {
    x: e.clientX,
    y: e.clientY,
    startW: panelWidth.value,
    startH: panelHeight.value,
    startX: panelPosition.value.x,
    startY: panelPosition.value.y
  }

  window.addEventListener('mousemove', onResizing)
  window.addEventListener('mouseup', stopResizing)
}

const onResizing = (e: MouseEvent) => {
  if (!activeResizeDirection.value) return

  const dir = activeResizeDirection.value
  const dx = e.clientX - resizeStartData.value.x
  const dy = e.clientY - resizeStartData.value.y

  let newW = resizeStartData.value.startW
  let newH = resizeStartData.value.startH
  let newX = resizeStartData.value.startX
  let newY = resizeStartData.value.startY

  const minW = 340
  const minH = 420
  const maxW = window.innerWidth - 40
  const maxH = window.innerHeight - 40

  // Horizontal Resize
  if (dir.includes('right')) {
    newW = Math.max(minW, Math.min(maxW, resizeStartData.value.startW + dx))
  } else if (dir.includes('left')) {
    const calculatedW = resizeStartData.value.startW - dx
    if (calculatedW >= minW && calculatedW <= maxW) {
      newW = calculatedW
      newX = resizeStartData.value.startX + dx
    }
  }

  // Vertical Resize
  if (dir.includes('bottom')) {
    newH = Math.max(minH, Math.min(maxH, resizeStartData.value.startH + dy))
  } else if (dir.includes('top')) {
    const calculatedH = resizeStartData.value.startH - dy
    if (calculatedH >= minH && calculatedH <= maxH) {
      newH = calculatedH
      newY = resizeStartData.value.startY + dy
    }
  }

  panelWidth.value = newW
  panelHeight.value = newH
  panelPosition.value = { x: newX, y: newY }
}

const stopResizing = () => {
  activeResizeDirection.value = null
  window.removeEventListener('mousemove', onResizing)
  window.removeEventListener('mouseup', stopResizing)
}

const panelComputedStyle = computed(() => {
  const styleObj: Record<string, string> = {
    position: 'fixed',
    zIndex: '1000',
    width: `${panelWidth.value}px`,
    height: `${panelHeight.value}px`,
    maxWidth: '95vw',
    maxHeight: '90vh',
    background: 'var(--va-background-secondary)',
    borderRadius: '18px',
    boxShadow: '0 16px 40px rgba(0,0,0,0.25)',
    display: 'flex',
    flexDirection: 'column',
    overflow: 'hidden',
    border: '1px solid var(--va-background-border)'
  }

  if (panelPosition.value.x === 0 && panelPosition.value.y === 0) {
    styleObj.bottom = '92px'
    styleObj.right = '24px'
  } else {
    styleObj.left = `${panelPosition.value.x}px`
    styleObj.top = `${panelPosition.value.y}px`
  }

  return styleObj
})

// Table Data Viewer Modal State
const showTableDataModal = ref(false)
const tableModalHeaders = ref<string[]>([])
const tableModalRows = ref<string[][]>([])
const tableModalRawContent = ref('')

const openTableDataModal = (msg: any) => {
  const parsed = parseTableContent(msg.content)
  if (parsed.isTable) {
    tableModalHeaders.value = parsed.headers.map(h => getCellText(h))
    tableModalRows.value = parsed.rows.map(r => r.map(c => getCellText(c)))
    tableModalRawContent.value = msg.content
    showTableDataModal.value = true
  }
}

export interface ParsedTableCell {
  text: string
  bg?: string
  color?: string
  fontWeight?: string
}

export interface ParsedTable {
  isTable: boolean
  headers: (string | ParsedTableCell)[]
  rows: (string | ParsedTableCell)[][]
  isHtmlExcel?: boolean
}

const getCellText = (cell: string | ParsedTableCell): string => {
  if (!cell) return ''
  if (typeof cell === 'string') return cell
  return cell.text || ''
}

const getCellStyle = (cell: string | ParsedTableCell, type: 'header' | 'data') => {
  const isHeader = type === 'header'
  const defaultBg = isHeader ? '#5c6bc0' : '#f5f5f5'
  const defaultColor = isHeader ? '#ffffff' : '#111111'
  const defaultBorder = isHeader ? '1px solid #3f51b5' : '1px solid #9e9e9e'

  if (typeof cell === 'string' || !cell) {
    return {
      padding: isHeader ? '7px 12px' : '6px 12px',
      fontWeight: isHeader ? '700' : '500',
      whiteSpace: 'nowrap',
      border: defaultBorder,
      background: defaultBg,
      color: defaultColor,
      textAlign: (isHeader ? 'center' : 'left') as any
    }
  }

  return {
    padding: isHeader ? '7px 12px' : '6px 12px',
    fontWeight: cell.fontWeight || (isHeader ? '700' : '500'),
    whiteSpace: 'nowrap',
    border: defaultBorder,
    background: cell.bg || defaultBg,
    color: cell.color || defaultColor,
    textAlign: (isHeader ? 'center' : 'left') as any
  }
}

const formatUuidForDisplay = (val: string): string => {
  if (!val) return ''
  const uuidRegex = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/
  if (uuidRegex.test(val.trim())) {
    return 'REC-' + val.trim().substring(0, 8)
  }
  return val
}

const parseTableContent = (content: string): ParsedTable => {
  if (!content || typeof content !== 'string') {
    return { isTable: false, headers: [], rows: [] }
  }

  const trimmed = content.trim()

  // Case 0: HTML Excel Table with inline styles (e.g. yellow, red, green backgrounds)
  if (trimmed.toLowerCase().includes('<table') && process.client) {
    try {
      const parser = new DOMParser()
      const doc = parser.parseFromString(trimmed, 'text/html')
      const table = doc.querySelector('table')
      if (table) {
        const trs = Array.from(table.querySelectorAll('tr'))
        if (trs.length > 0) {
          const extractCell = (el: Element): ParsedTableCell => {
            const txt = (el.textContent || '').trim()
            const style = el.getAttribute('style') || ''
            let bg = ''
            let color = ''

            // Extract background-color or bgcolor
            const bgMatch = style.match(/background(?:-color)?\s*:\s*([^;]+)/i)
            if (bgMatch) bg = bgMatch[1].trim()
            else if (el.getAttribute('bgcolor')) bg = el.getAttribute('bgcolor')!

            // Extract text color
            const colorMatch = style.match(/color\s*:\s*([^;]+)/i)
            if (colorMatch) color = colorMatch[1].trim()

            const isBold = style.toLowerCase().includes('font-weight') && !style.toLowerCase().includes('normal')
            return {
              text: txt,
              bg: bg || undefined,
              color: color || undefined,
              fontWeight: isBold ? '700' : undefined
            }
          }

          let headers: ParsedTableCell[] = []
          let rowStartIndex = 0

          const ths = Array.from(trs[0].querySelectorAll('th, td'))
          if (ths.length > 0) {
            headers = ths.map(extractCell)
            rowStartIndex = 1
          }

          const rows: ParsedTableCell[][] = []
          for (let i = rowStartIndex; i < trs.length; i++) {
            const tds = Array.from(trs[i].querySelectorAll('td, th'))
            if (tds.length > 0) {
              rows.push(tds.map(extractCell))
            }
          }

          if (headers.length > 0 || rows.length > 0) {
            return { isTable: true, headers, rows, isHtmlExcel: true }
          }
        }
      }
    } catch (e) {
      console.error('Failed to parse HTML table:', e)
    }
  }

  const lines = trimmed.split('\n').filter(l => l.trim().length > 0)
  if (lines.length === 0) return { isTable: false, headers: [], rows: [] }

  // Case 1: Markdown table (| Col1 | Col2 |)
  if (lines[0].includes('|') && lines.length >= 2) {
    const cleanLine = (l: string) => l.split('|').map(c => c.trim()).filter((c, idx, arr) => idx > 0 && idx < arr.length - 1)
    const headers = cleanLine(lines[0])
    let dataLines = lines.slice(1)
    if (dataLines.length > 0 && dataLines[0].includes('---')) {
      dataLines = dataLines.slice(1)
    }
    const rows = dataLines.map(l => cleanLine(l))
    if (headers.length > 0 && rows.length > 0) {
      return { isTable: true, headers, rows }
    }
  }

  // Case 2: Tab delimited TSV (Excel copy paste: Col1\tCol2\nVal1\tVal2)
  if (lines[0].includes('\t') && lines.length >= 1) {
    const headers = lines[0].split('\t').map(s => s.trim())
    const rows = lines.slice(1).map(l => l.split('\t').map(s => s.trim()))
    if (headers.length >= 2 || rows.length >= 1) {
      return { isTable: true, headers, rows }
    }
  }

  // Case 3: Comma CSV with multiple lines
  if (lines[0].includes(',') && lines.length >= 2) {
    const headers = lines[0].split(',').map(s => s.trim())
    if (headers.length >= 2) {
      const rows = lines.slice(1).map(l => l.split(',').map(s => s.trim()))
      return { isTable: true, headers, rows }
    }
  }

  return { isTable: false, headers: [], rows: [] }
}

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

const showExcelModal = ref(false)
const selectedExcelUrl = ref<string | null>(null)
const selectedExcelName = ref<string>('')

const showPasteOptionModal = ref(false)
const pendingPastedText = ref('')
const pendingPastedImageFile = ref<File | null>(null)

const sendPastedAsText = () => {
  if (pendingPastedText.value) {
    inputMsg.value = pendingPastedText.value
    sendTextMessage()
  }
  showPasteOptionModal.value = false
  pendingPastedText.value = ''
  pendingPastedImageFile.value = null
}

const sendPastedAsImage = async () => {
  if (pendingPastedImageFile.value) {
    await uploadAndSendImage(pendingPastedImageFile.value)
  }
  showPasteOptionModal.value = false
  pendingPastedText.value = ''
  pendingPastedImageFile.value = null
}

const uploadAndSendImage = async (imageFile: File) => {
  const formData = new FormData()
  formData.append('file', imageFile, 'paste_image.png')
  try {
    const res: any = await $fetch('/api/chat/upload', {
      method: 'POST',
      headers: { Authorization: `Bearer ${tokenCookie.value}` },
      body: formData
    })
    if (res && res.fileUrl) {
      sendMessage('IMAGE', res.fileUrl)
    }
  } catch (e) {
    console.error('Failed to upload image:', e)
  }
}

const isExcelFile = (fileName?: string) => {
  if (!fileName) return false
  const ext = fileName.toLowerCase()
  return ext.endsWith('.xlsx') || ext.endsWith('.xls') || ext.endsWith('.csv')
}

const openExcelViewer = (msg: any) => {
  const url = msg.fileUrl || msg.content
  selectedExcelUrl.value = url
  selectedExcelName.value = msg.fileName || 'Excel_File.xlsx'
  showExcelModal.value = true
}

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

const toggleTranslateMsg = async () => {
  const msg = contextMenu.value.msg
  contextMenu.value.show = false
  if (!msg || !msg.content) return

  if (msg.showTranslation) {
    msg.showTranslation = false
    return
  }

  msg.showTranslation = true
  if (msg.translatedText) return

  msg.isTranslating = true
  try {
    const res: any = await $fetch('/api/chat/translate', {
      method: 'POST',
      headers: { Authorization: `Bearer ${tokenCookie.value}` },
      body: { text: msg.content }
    })
    msg.translatedText = res?.translated || msg.content
  } catch (e) {
    msg.translatedText = t('messenger.translationError')
  } finally {
    msg.isTranslating = false
  }
}

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

  const textData = clipboardData.getData('text/plain') || ''
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

  if (imageFile && textData && textData.trim().length > 0) {
    event.preventDefault()
    pendingPastedText.value = textData
    pendingPastedImageFile.value = imageFile
    showPasteOptionModal.value = true
    return
  }

  if (imageFile) {
    event.preventDefault()
    await uploadAndSendImage(imageFile)
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
  if (!member) return false
  const mId = String(member.userId || member.id || '')
  return (!!myUuid.value && mId === myUuid.value) || (!!currentUser.value?.username && member.username === currentUser.value.username)
}

const isCreator = (member: any) => {
  if (!activeRoom.value || !member) return false
  const cId = String(activeRoom.value.createdBy || '')
  const mId = String(member.userId || member.id || '')
  return cId === mId
}

const isUserOnline = (member: any): boolean => {
  if (!member) return false
  if (isMe(member)) return true
  if (typeof member.isOnline === 'boolean') return member.isOnline
  if (member.status === 'ONLINE' || member.status === 'ACTIVE' || member.online === true) return true
  return false
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

const formatDateTime = (timeStr: any) => {
  if (!timeStr) return ''
  try {
    const date = new Date(timeStr)
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    const time = date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    return `${y}-${m}-${d} ${time}`
  } catch {
    return ''
  }
}

const getDateKey = (timeStr: any): string => {
  if (!timeStr) return ''
  try {
    const date = new Date(timeStr)
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  } catch {
    return ''
  }
}

const shouldShowDateSeparator = (idx: number): boolean => {
  if (idx === 0) return true
  const prevDate = getDateKey(messages.value[idx - 1]?.createdAt)
  const currDate = getDateKey(messages.value[idx]?.createdAt)
  return prevDate !== currDate
}

const formatDateSeparator = (timeStr: any): string => {
  if (!timeStr) return ''
  try {
    const date = new Date(timeStr)
    const dayNames = [t('messenger.daySun'), t('messenger.dayMon'), t('messenger.dayTue'), t('messenger.dayWed'), t('messenger.dayThu'), t('messenger.dayFri'), t('messenger.daySat')]
    const y = date.getFullYear()
    const m = date.getMonth() + 1
    const d = date.getDate()
    const dayOfWeek = dayNames[date.getDay()]
    return `${y}${t('messenger.year')} ${m}${t('messenger.month')} ${d}${t('messenger.day')} ${dayOfWeek}`
  } catch {
    return ''
  }
}

// ---- Calendar Jump Dialog ----
const showCalendarDialog = ref(false)
const calendarYear = ref(new Date().getFullYear())
const calendarMonth = ref(new Date().getMonth() + 1)
const calendarSelectedDate = ref('')

const calendarDowLabels = computed(() => [
  t('messenger.daySun'), t('messenger.dayMon'), t('messenger.dayTue'),
  t('messenger.dayWed'), t('messenger.dayThu'), t('messenger.dayFri'), t('messenger.daySat')
].map(s => s.substring(0, 1)))

const messageDatesSet = computed((): Set<string> => {
  const set = new Set<string>()
  for (const msg of messages.value) {
    const key = getDateKey(msg.createdAt)
    if (key) set.add(key)
  }
  return set
})

const calendarCells = computed(() => {
  const y = calendarYear.value
  const m = calendarMonth.value
  const firstDay = new Date(y, m - 1, 1).getDay()
  const daysInMonth = new Date(y, m, 0).getDate()
  const cells: { day: number; dateKey: string; hasMessages: boolean; isSelected: boolean }[] = []

  // Empty cells before first day
  for (let i = 0; i < firstDay; i++) {
    cells.push({ day: 0, dateKey: '', hasMessages: false, isSelected: false })
  }

  for (let d = 1; d <= daysInMonth; d++) {
    const dateKey = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    cells.push({
      day: d,
      dateKey,
      hasMessages: messageDatesSet.value.has(dateKey),
      isSelected: calendarSelectedDate.value === dateKey
    })
  }

  return cells
})

const calendarPrevMonth = () => {
  if (calendarMonth.value === 1) {
    calendarMonth.value = 12
    calendarYear.value--
  } else {
    calendarMonth.value--
  }
}

const calendarNextMonth = () => {
  if (calendarMonth.value === 12) {
    calendarMonth.value = 1
    calendarYear.value++
  } else {
    calendarMonth.value++
  }
}

const openCalendarDialog = (timeStr: any) => {
  try {
    const date = new Date(timeStr)
    calendarYear.value = date.getFullYear()
    calendarMonth.value = date.getMonth() + 1
    calendarSelectedDate.value = getDateKey(timeStr)
  } catch {
    calendarYear.value = new Date().getFullYear()
    calendarMonth.value = new Date().getMonth() + 1
  }
  showCalendarDialog.value = true
}

const jumpToDate = (dateKey: string) => {
  showCalendarDialog.value = false
  calendarSelectedDate.value = dateKey
  nextTick(() => {
    const separator = document.querySelector(`.msg-date-separator[data-date="${dateKey}"]`)
    if (separator && msgContainer.value) {
      separator.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  })
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

const copyToast = ref(false)
const copyToastMsg = ref('')

const clearTextSelection = () => {
  if (process.client && window.getSelection) {
    const sel = window.getSelection()
    if (sel) {
      sel.removeAllRanges()
    }
  }
}

// KakaoTalk-style block selection via mouse tracking
const isBlockSelectMode = ref(false)
const blockDragStartWrapper = ref<Element | null>(null)
const isMouseDragging = ref(false)

const getWrapperFromEvent = (e: MouseEvent): Element | null => {
  const el = e.target as HTMLElement
  return el?.closest('.msg-bubble-wrapper') || null
}

const getAllWrappers = (): Element[] => {
  const msgContainer = document.querySelector('.messages-area')
  if (!msgContainer) return []
  return Array.from(msgContainer.querySelectorAll('.msg-bubble-wrapper'))
}

const applyBlockHighlight = (startWrapper: Element, endWrapper: Element) => {
  const all = getAllWrappers()
  const startIdx = all.indexOf(startWrapper)
  const endIdx = all.indexOf(endWrapper)
  if (startIdx < 0 || endIdx < 0) return

  const lo = Math.min(startIdx, endIdx)
  const hi = Math.max(startIdx, endIdx)

  if (hi - lo < 1) {
    // Same wrapper or not enough - no block mode
    isBlockSelectMode.value = false
    for (const w of all) w.classList.remove('msg-block-selected')
    return
  }

  isBlockSelectMode.value = true
  // Clear browser native selection so it doesn't conflict
  const sel = window.getSelection()
  if (sel) sel.removeAllRanges()

  for (let i = 0; i < all.length; i++) {
    if (i >= lo && i <= hi) {
      all[i].classList.add('msg-block-selected')
    } else {
      all[i].classList.remove('msg-block-selected')
    }
  }
}

const onMsgAreaMouseDown = (e: MouseEvent) => {
  // Only left click
  if (e.button !== 0) return
  const wrapper = getWrapperFromEvent(e)
  if (!wrapper) return
  blockDragStartWrapper.value = wrapper
  isMouseDragging.value = true
  // Clear previous block selection
  clearBlockSelection()
}

const onMsgAreaMouseMove = (e: MouseEvent) => {
  if (!isMouseDragging.value || !blockDragStartWrapper.value) return
  const currentWrapper = getWrapperFromEvent(e)
  if (!currentWrapper) return
  if (currentWrapper !== blockDragStartWrapper.value) {
    applyBlockHighlight(blockDragStartWrapper.value, currentWrapper)
  }
}

const onMsgAreaMouseUp = () => {
  isMouseDragging.value = false
  // blockDragStartWrapper stays so copy can use it
}

const clearBlockSelection = () => {
  isBlockSelectMode.value = false
  const all = getAllWrappers()
  for (const w of all) w.classList.remove('msg-block-selected')
}

const extractBubbleContent = (bubble: Element): string => {
  // Check if bubble contains a table - extract as TSV for Excel
  const table = bubble.querySelector('table')
  if (table) {
    const tsvLines: string[] = []
    const rows = table.querySelectorAll('tr')
    for (const row of rows) {
      const cells = row.querySelectorAll('th, td')
      const cellTexts: string[] = []
      for (const cell of cells) {
        cellTexts.push((cell.textContent || '').trim().replace(/\t/g, ' '))
      }
      tsvLines.push(cellTexts.join('\t'))
    }
    return tsvLines.join('\n')
  }
  // Plain text fallback
  return bubble.textContent?.trim() || ''
}

const handleCopyEvent = (e: ClipboardEvent) => {
  // If block-select mode, copy all block-selected messages
  if (isBlockSelectMode.value) {
    const selectedEls = document.querySelectorAll('.msg-block-selected')
    if (selectedEls.length >= 2) {
      const lines: string[] = []
      for (const wrapper of selectedEls) {
        const msgType = wrapper.getAttribute('data-type') || ''
        if (msgType === 'FILE' || msgType === 'IMAGE') continue
        const sender = wrapper.getAttribute('data-sender') || ''
        const time = wrapper.getAttribute('data-time') || ''
        const bubble = wrapper.querySelector('.msg-bubble')
        const content = bubble ? extractBubbleContent(bubble) : ''
        if (content) {
          lines.push(`[${sender}] ${time}\n${content}`)
        }
      }
      if (lines.length > 0) {
        e.preventDefault()
        e.clipboardData?.setData('text/plain', lines.join('\n\n'))
        clearBlockSelection()
        return
      }
    }
  }

  // Single message or partial text: copy plain text only (strips HTML white color)
  const sel = window.getSelection()
  if (!sel || sel.isCollapsed) return
  const selectedText = sel.toString()
  if (!selectedText.trim()) return
  e.preventDefault()
  e.clipboardData?.setData('text/plain', selectedText)
}

const copyMsgContent = async (msgObj?: any) => {
  const targetMsg = (msgObj && msgObj.content) ? msgObj : contextMenu.value.msg
  closeContextMenu()
  if (!targetMsg) return

  const sender = targetMsg.senderName || ''
  const timeInfo = formatDateTime(targetMsg.createdAt)
  let bodyContent = ''

  if (targetMsg.messageType === 'IMAGE') {
    bodyContent = targetMsg.fileUrl || targetMsg.content || ''
  } else if (targetMsg.messageType === 'FILE') {
    bodyContent = `${targetMsg.fileName || ''}\n${targetMsg.fileUrl || ''}`
  } else {
    bodyContent = targetMsg.content || ''
  }

  const textToCopy = `[${sender}] ${timeInfo}\n${bodyContent}`

  if (process.client && navigator.clipboard) {
    try {
      await navigator.clipboard.writeText(textToCopy)
      copyToastMsg.value = t('messenger.copiedToClipboard')
      copyToast.value = true
      setTimeout(() => { copyToast.value = false }, 3000)
    } catch (e) {
      console.error('Failed to copy message content:', e)
    }
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
    const timeInfo = formatDateTime(targetMsg.createdAt)
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
    window.addEventListener('copy', handleCopyEvent)
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
    window.removeEventListener('copy', handleCopyEvent)
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

.messages-area,
.msg-bubble-wrapper,
.msg-bubble,
.msg-bubble * {
  user-select: text !important;
  -webkit-user-select: text !important;
  -moz-user-select: text !important;
}

.msg-time-area,
.msg-time-area * {
  user-select: none !important;
  -webkit-user-select: none !important;
  -moz-user-select: none !important;
  pointer-events: none !important;
}

.messenger-header {
  user-select: none !important;
  -webkit-user-select: none !important;
}

.messages-area ::selection,
.messages-area *::selection,
.msg-bubble ::selection,
.msg-bubble *::selection {
  background-color: #f59e0b !important;
  color: #000000 !important;
  text-shadow: none !important;
}

/* KakaoTalk-style block selection highlight */
.msg-block-selected {
  background-color: rgba(59, 130, 246, 0.15) !important;
  border-radius: 8px;
}

.msg-block-selected .msg-bubble {
  opacity: 0.85;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.4);
}

/* Date separator pill hover */
.msg-date-pill:hover {
  background: var(--va-primary) !important;
  color: #ffffff !important;
  transform: scale(1.03);
}

/* Calendar day hover */
.calendar-day-active:hover {
  background: rgba(59, 130, 246, 0.25) !important;
  transform: scale(1.1);
}
</style>
