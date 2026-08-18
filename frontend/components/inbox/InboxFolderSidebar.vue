<template>
  <div class="inbox-sidebar">
    <div class="inbox-sidebar-header">
      <va-button block preset="primary" class="compose-btn" icon="edit" @click="$emit('compose')">
        {{ $t('inbox.compose') }}
      </va-button>
    </div>
    <div class="inbox-sidebar-content">
      <va-list>
        <va-list-item
          v-for="folder in folders"
          :key="folder.id"
          class="folder-item"
          :class="{ 'active': activeFolder === folder.id }"
          @click="$emit('select-folder', folder.id)"
        >
          <va-list-item-section icon>
            <va-icon :name="folder.icon" :color="activeFolder === folder.id ? 'primary' : 'secondary'" />
          </va-list-item-section>
          <va-list-item-section>
            <va-list-item-label :class="{ 'text-primary font-bold': activeFolder === folder.id }">
              {{ folder.label }}
            </va-list-item-label>
          </va-list-item-section>
          <va-list-item-section right v-if="getUnreadCount(folder.id) > 0">
            <va-badge :text="getUnreadCount(folder.id).toString()" color="primary" />
          </va-list-item-section>
        </va-list-item>
      </va-list>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  activeFolder: string
  folderCounts: { folder: string; total: number; unread: number }[]
}>()

defineEmits(['select-folder', 'compose'])

const { t } = useI18n()

const folders = computed(() => [
  { id: 'INBOX', icon: 'inbox', label: t('inbox.folder_inbox') },
  { id: 'SENT', icon: 'send', label: t('inbox.folder_sent') },
  { id: 'DRAFT', icon: 'drafts', label: t('inbox.folder_draft') },
  { id: 'STARRED', icon: 'star', label: t('inbox.folder_starred') },
  { id: 'ARCHIVE', icon: 'archive', label: t('inbox.folder_archive') },
  { id: 'TRASH', icon: 'delete', label: t('inbox.folder_trash') },
])

const getUnreadCount = (folderId: string) => {
  const count = props.folderCounts.find(c => c.folder === folderId)
  return count ? count.unread : 0
}
</script>

<style scoped>
.inbox-sidebar {
  display: flex;
  flex-direction: column;
  height: 100%;
  border-right: 1px solid var(--va-background-border);
  background: var(--va-background-element);
}
.inbox-sidebar-header {
  padding: 1.25rem;
  border-bottom: 1px solid var(--va-background-border);
}
.inbox-sidebar-content {
  flex-grow: 1;
  overflow-y: auto;
  padding: 0.75rem 0;
}
.folder-item {
  cursor: pointer;
  padding: 0.5rem 1.25rem;
  margin: 0.25rem 0.75rem;
  border-radius: 8px;
  transition: all 0.2s ease;
}
.folder-item:hover {
  background-color: var(--va-background-primary);
}
.folder-item.active {
  background-color: var(--va-primary-lighten);
}
.text-primary {
  color: var(--va-primary);
}
.font-bold {
  font-weight: bold;
}
</style>
