<template>
  <div class="inbox-attachment-uploader">
    <!-- Dropzone Bar / Upload Trigger -->
    <div
      class="attachment-dropzone"
      :class="{ 'is-dragging': isDragging }"
      @dragover.prevent="isDragging = true"
      @dragleave.prevent="isDragging = false"
      @drop.prevent="onDropFiles"
      @click="triggerFileInput"
    >
      <input
        ref="fileInputRef"
        type="file"
        multiple
        class="hidden-file-input"
        @change="onFilesSelected"
      />
      <div class="dropzone-content">
        <div class="dropzone-icon-box">
          <va-icon name="cloud_upload" size="24px" class="upload-icon" />
        </div>
        <div class="dropzone-text-group">
          <span class="dropzone-title">{{ $t('inbox.drop_or_click_files') }}</span>
          <span class="dropzone-subtitle">{{ $t('inbox.file_size_limit') }}</span>
        </div>
        <va-button
          preset="secondary"
          size="small"
          icon="attach_file"
          class="attach-btn"
          @click.stop="triggerFileInput"
        >
          {{ $t('inbox.attach_files_btn') }}
        </va-button>
      </div>
    </div>

    <!-- Header Summary (When files are attached) -->
    <div v-if="filesList.length > 0" class="attachment-header-summary">
      <div class="summary-left">
        <span class="summary-badge">
          <va-icon name="attachment" size="16px" class="mr-1" />
          {{ $t('inbox.total_files_summary', { count: filesList.length, size: formatTotalSize }) }}
        </span>
        <span v-if="hasUploadingFiles" class="uploading-indicator">
          <va-progress-circle indeterminate size="small" />
          {{ $t('inbox.upload_in_progress') }}
        </span>
      </div>
      <va-button
        preset="plain"
        size="small"
        color="danger"
        class="clear-all-btn"
        @click="clearAllFiles"
      >
        {{ $t('inbox.clear_all_attachments') }}
      </va-button>
    </div>

    <!-- Attached Files List -->
    <div v-if="filesList.length > 0" class="attachment-files-list">
      <div
        v-for="item in filesList"
        :key="item.id"
        class="attachment-card"
        :class="[`status-${item.status}`]"
      >
        <!-- Thumbnail / File Type Icon -->
        <div class="file-icon-preview">
          <img
            v-if="item.previewUrl"
            :src="item.previewUrl"
            :alt="item.name"
            class="thumb-img"
          />
          <div
            v-else
            class="type-icon-wrapper"
            :class="getFileTypeClass(item.name)"
          >
            <va-icon :name="getFileIcon(item.name)" size="20px" />
          </div>
        </div>

        <!-- File Info -->
        <div class="file-details">
          <div class="file-name-row">
            <span class="file-name" :title="item.name">{{ item.name }}</span>
            <span class="file-size">{{ formatFileSize(item.size) }}</span>
          </div>

          <!-- Status Indicator Bar -->
          <div class="file-status-row">
            <!-- Ready State (Before clicking send) -->
            <div v-if="item.status === 'ready'" class="status-badge ready">
              <va-icon name="schedule" size="13px" class="mr-1" />
              <span>{{ $t('inbox.upload_ready') }}</span>
            </div>

            <!-- Uploading State -->
            <div v-else-if="item.status === 'uploading'" class="status-badge uploading">
              <va-progress-circle indeterminate size="12px" class="mr-1" />
              <span>{{ $t('inbox.upload_in_progress') }}</span>
            </div>

            <!-- Success State -->
            <div v-else-if="item.status === 'success'" class="status-badge success">
              <va-icon name="check_circle" size="14px" color="success" class="mr-1" />
              <span>{{ $t('inbox.upload_success') }}</span>
            </div>

            <!-- Error State -->
            <div v-else-if="item.status === 'error'" class="status-badge error">
              <va-icon name="error" size="14px" color="danger" class="mr-1" />
              <span>{{ $t('inbox.upload_failed') }}</span>
              <button type="button" class="retry-btn" @click.stop="retrySingleUpload(item)">
                {{ $t('inbox.upload_retry') }}
              </button>
            </div>
          </div>
        </div>

        <!-- Remove Action -->
        <button
          type="button"
          class="remove-file-btn"
          :title="$t('inbox.delete')"
          @click.stop="removeFile(item.id)"
        >
          <va-icon name="close" size="16px" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'

export interface AttachedFileItem {
  id: string
  name: string
  size: number
  status: 'ready' | 'uploading' | 'success' | 'error'
  url?: string
  file: File
  previewUrl?: string
}

const props = withDefaults(defineProps<{
  modelValue: AttachedFileItem[]
}>(), {
  modelValue: () => []
})

const emit = defineEmits<{
  (e: 'update:modelValue', val: AttachedFileItem[]): void
}>()

const { t } = useI18n()
const { customFetch } = useCustomFetch()

const fileInputRef = ref<HTMLInputElement | null>(null)
const isDragging = ref(false)

const filesList = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const hasUploadingFiles = computed(() => {
  return filesList.value.some(f => f.status === 'uploading')
})

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

const formatTotalSize = computed(() => {
  const totalBytes = filesList.value.reduce((acc, f) => acc + (f.size || 0), 0)
  return formatFileSize(totalBytes)
})

const getFileTypeClass = (filename: string): string => {
  const ext = filename.split('.').pop()?.toLowerCase() || ''
  if (['xlsx', 'xls', 'csv'].includes(ext)) return 'type-excel'
  if (['pdf'].includes(ext)) return 'type-pdf'
  if (['docx', 'doc', 'hwp', 'txt'].includes(ext)) return 'type-word'
  if (['zip', 'rar', '7z', 'tar', 'gz'].includes(ext)) return 'type-zip'
  if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'].includes(ext)) return 'type-image'
  return 'type-default'
}

const getFileIcon = (filename: string): string => {
  const ext = filename.split('.').pop()?.toLowerCase() || ''
  if (['xlsx', 'xls', 'csv'].includes(ext)) return 'table_chart'
  if (['pdf'].includes(ext)) return 'picture_as_pdf'
  if (['docx', 'doc', 'hwp'].includes(ext)) return 'article'
  if (['zip', 'rar', '7z', 'tar', 'gz'].includes(ext)) return 'folder_zip'
  if (['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'].includes(ext)) return 'image'
  return 'insert_drive_file'
}

const triggerFileInput = () => {
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
    fileInputRef.value.click()
  }
}

const onFilesSelected = (e: Event) => {
  const target = e.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    addFiles(Array.from(target.files))
  }
}

const onDropFiles = (e: DragEvent) => {
  isDragging.value = false
  if (e.dataTransfer && e.dataTransfer.files && e.dataTransfer.files.length > 0) {
    addFiles(Array.from(e.dataTransfer.files))
  }
}

const addFiles = (newFiles: File[]) => {
  const newItems: AttachedFileItem[] = newFiles.map(file => {
    let previewUrl: string | undefined = undefined
    if (file.type.startsWith('image/')) {
      try {
        previewUrl = URL.createObjectURL(file)
      } catch (err) {
        console.debug(err)
      }
    }
    return {
      id: `att_${Date.now()}_${Math.random().toString(36).substring(2, 8)}`,
      name: file.name || 'file',
      size: file.size || 0,
      status: 'ready' as const,
      file,
      previewUrl
    }
  })

  emit('update:modelValue', [...filesList.value, ...newItems])
}

const uploadSingleFile = async (item: AttachedFileItem): Promise<string | null> => {
  try {
    let uploadFile = item.file
    if (!uploadFile.name || uploadFile.name === 'blob' || !uploadFile.name.includes('.')) {
      const ext = uploadFile.type ? uploadFile.type.split('/')[1] : 'bin'
      uploadFile = new File([uploadFile], `attachment_${Date.now()}.${ext}`, { type: uploadFile.type || 'application/octet-stream' })
    }

    const formData = new FormData()
    formData.append('file', uploadFile)

    const res: any = await customFetch('/api/files/upload', {
      method: 'POST',
      body: formData
    })

    const fileName = uploadFile.name
    let url = res?.url || (res?.fileId ? `/api/files/download/${res.fileId}?name=${encodeURIComponent(fileName)}` : null) || (typeof res === 'string' ? res : null)
    if (url && !url.includes('size=') && item.size) {
      url += (url.includes('?') ? '&' : '?') + 'size=' + item.size
    }

    if (url) {
      updateItemStatus(item.id, 'success', url)
      return url
    } else {
      updateItemStatus(item.id, 'error')
      return null
    }
  } catch (err) {
    console.error('File upload failed for:', item.name, err)
    updateItemStatus(item.id, 'error')
    return null
  }
}

const updateItemStatus = (id: string, status: 'ready' | 'uploading' | 'success' | 'error', url?: string) => {
  const list = [...filesList.value]
  const target = list.find(f => f.id === id)
  if (target) {
    target.status = status
    if (url) target.url = url
    emit('update:modelValue', list)
  }
}

const retrySingleUpload = (item: AttachedFileItem) => {
  updateItemStatus(item.id, 'uploading')
  uploadSingleFile(item)
}

/**
 * Upload all pending/ready files at the time of sending/drafting.
 */
const uploadAll = async (): Promise<{ success: boolean; urls: string[] }> => {
  if (filesList.value.length === 0) {
    return { success: true, urls: [] }
  }

  const urls: string[] = []
  let allSuccess = true

  for (const item of filesList.value) {
    if (item.status === 'success' && item.url) {
      urls.push(item.url)
      continue
    }

    updateItemStatus(item.id, 'uploading')
    const uploadedUrl = await uploadSingleFile(item)
    if (uploadedUrl) {
      urls.push(uploadedUrl)
    } else {
      allSuccess = false
    }
  }

  return {
    success: allSuccess,
    urls
  }
}

const removeFile = (id: string) => {
  const item = filesList.value.find(f => f.id === id)
  if (item && item.previewUrl) {
    try {
      URL.revokeObjectURL(item.previewUrl)
    } catch (e) {}
  }
  emit('update:modelValue', filesList.value.filter(f => f.id !== id))
}

const clearAllFiles = () => {
  filesList.value.forEach(item => {
    if (item.previewUrl) {
      try {
        URL.revokeObjectURL(item.previewUrl)
      } catch (e) {}
    }
  })
  emit('update:modelValue', [])
}

defineExpose({
  uploadAll,
  clearAllFiles
})
</script>

<style scoped>
.inbox-attachment-uploader {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  width: 100%;
}

.hidden-file-input {
  display: none;
}

/* High Contrast Dropzone */
.attachment-dropzone {
  border: 1.5px dashed var(--va-primary, #3b82f6);
  border-radius: 10px;
  padding: 0.65rem 1rem;
  background: var(--va-background-element, rgba(241, 245, 249, 0.6));
  cursor: pointer;
  transition: all 0.2s ease;
}

:global([data-vuestic-preset="dark"]) .attachment-dropzone,
:global(.va-theme-dark) .attachment-dropzone {
  border-color: #3b82f6;
  background: rgba(30, 41, 59, 0.7);
}

.attachment-dropzone:hover,
.attachment-dropzone.is-dragging {
  border-color: #2563eb;
  background: rgba(59, 130, 246, 0.12);
}

.dropzone-content {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.dropzone-icon-box {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: rgba(59, 130, 246, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.upload-icon {
  color: var(--va-primary, #3b82f6);
}

.dropzone-text-group {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.dropzone-title {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--va-text-primary, #0f172a);
}

:global([data-vuestic-preset="dark"]) .dropzone-title,
:global(.va-theme-dark) .dropzone-title {
  color: #f8fafc;
}

.dropzone-subtitle {
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--va-text-secondary, #64748b);
}

:global([data-vuestic-preset="dark"]) .dropzone-subtitle,
:global(.va-theme-dark) .dropzone-subtitle {
  color: #94a3b8;
}

.attach-btn {
  font-size: 0.8rem !important;
  height: 28px !important;
  padding: 0 0.65rem !important;
  font-weight: 600 !important;
}

.attachment-header-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.15rem 0.25rem;
}

.summary-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.summary-badge {
  font-size: 0.82rem;
  font-weight: 700;
  color: var(--va-text-primary, #1e293b);
  display: inline-flex;
  align-items: center;
}

:global([data-vuestic-preset="dark"]) .summary-badge,
:global(.va-theme-dark) .summary-badge {
  color: #e2e8f0;
}

.uploading-indicator {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.75rem;
  font-weight: 600;
  color: #3b82f6;
}

.clear-all-btn {
  font-size: 0.75rem !important;
  padding: 0 !important;
  height: auto !important;
  font-weight: 600 !important;
}

.attachment-files-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 0.5rem;
  max-height: 180px;
  overflow-y: auto;
  padding: 2px;
}

/* Card Styling for Light & Dark */
.attachment-card {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.5rem 0.65rem;
  border-radius: 8px;
  background: var(--va-background-secondary, #ffffff);
  border: 1.5px solid var(--va-background-border, #cbd5e1);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 0.15s ease;
  position: relative;
}

:global([data-vuestic-preset="dark"]) .attachment-card,
:global(.va-theme-dark) .attachment-card {
  background: #1e293b;
  border-color: #475569;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
}

.attachment-card.status-ready {
  border-color: #94a3b8;
}

.attachment-card.status-uploading {
  border-color: #38bdf8;
  background: rgba(56, 189, 248, 0.08);
}

.attachment-card.status-success {
  border-color: #22c55e;
  background: rgba(34, 197, 94, 0.08);
}

.attachment-card.status-error {
  border-color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.file-icon-preview {
  width: 38px;
  height: 38px;
  border-radius: 6px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: rgba(148, 163, 184, 0.15);
  border: 1px solid rgba(148, 163, 184, 0.3);
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.type-icon-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.type-excel { background: rgba(16, 185, 129, 0.2); color: #10b981; }
.type-pdf { background: rgba(239, 68, 68, 0.2); color: #ef4444; }
.type-word { background: rgba(59, 130, 246, 0.2); color: #3b82f6; }
.type-zip { background: rgba(168, 85, 247, 0.2); color: #a855f7; }
.type-image { background: rgba(245, 158, 11, 0.2); color: #f59e0b; }
.type-default { background: rgba(148, 163, 184, 0.2); color: #94a3b8; }

.file-details {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.file-name-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 0.35rem;
}

.file-name {
  font-size: 0.82rem;
  font-weight: 700;
  color: var(--va-text-primary, #0f172a);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

:global([data-vuestic-preset="dark"]) .file-name,
:global(.va-theme-dark) .file-name {
  color: #f1f5f9;
}

.file-size {
  font-size: 0.72rem;
  font-weight: 600;
  color: var(--va-text-secondary, #64748b);
  flex-shrink: 0;
}

:global([data-vuestic-preset="dark"]) .file-size,
:global(.va-theme-dark) .file-size {
  color: #94a3b8;
}

.file-status-row {
  display: flex;
  align-items: center;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  font-size: 0.72rem;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 4px;
}

.status-badge.ready {
  color: #64748b;
  background: rgba(100, 116, 139, 0.12);
}

:global([data-vuestic-preset="dark"]) .status-badge.ready,
:global(.va-theme-dark) .status-badge.ready {
  color: #cbd5e1;
  background: rgba(148, 163, 184, 0.15);
}

.status-badge.uploading {
  color: #0284c7;
  background: rgba(2, 132, 199, 0.12);
}

:global([data-vuestic-preset="dark"]) .status-badge.uploading,
:global(.va-theme-dark) .status-badge.uploading {
  color: #38bdf8;
  background: rgba(56, 189, 248, 0.15);
}

.status-badge.success {
  color: #16a34a;
  background: rgba(22, 163, 74, 0.12);
}

:global([data-vuestic-preset="dark"]) .status-badge.success,
:global(.va-theme-dark) .status-badge.success {
  color: #4ade80;
  background: rgba(74, 222, 128, 0.15);
}

.status-badge.error {
  color: #dc2626;
  background: rgba(220, 38, 38, 0.12);
  gap: 0.35rem;
}

:global([data-vuestic-preset="dark"]) .status-badge.error,
:global(.va-theme-dark) .status-badge.error {
  color: #f87171;
  background: rgba(248, 113, 113, 0.15);
}

.retry-btn {
  background: none;
  border: 1px solid currentColor;
  border-radius: 4px;
  font-size: 0.65rem;
  font-weight: 700;
  padding: 1px 4px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.remove-file-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--va-text-secondary, #94a3b8);
  padding: 4px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s ease;
  flex-shrink: 0;
}

.remove-file-btn:hover {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}
</style>
