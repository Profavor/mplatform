<template>
  <div class="image-uploader-wrapper" :class="{ 'is-readonly': readonly || disabled }">
    <!-- Hidden File Input -->
    <input
      ref="fileInputRef"
      type="file"
      accept="image/*"
      :multiple="multiple"
      style="display: none;"
      @change="onFileInputChange"
    />

    <!-- Dropzone (Only visible in edit mode) -->
    <div
      v-if="!readonly && !disabled && (multiple || imagesList.length === 0)"
      class="image-dropzone"
      :class="{ 'is-dragging': isDragging, 'is-uploading': isUploading }"
      @dragenter.prevent="isDragging = true"
      @dragover.prevent="isDragging = true"
      @dragleave.prevent="isDragging = false"
      @drop.prevent="handleDrop"
      @paste="handlePaste"
      @click="triggerFileInput"
      tabindex="0"
    >
      <div v-if="isUploading" class="dropzone-content">
        <va-progress-circle indeterminate size="32px" color="primary" />
        <span class="dropzone-text">{{ t('uploading_image') }}</span>
      </div>
      <div v-else class="dropzone-content">
        <va-icon name="add_photo_alternate" size="36px" color="primary" />
        <span class="dropzone-title">{{ t('upload_image') }}</span>
        <span class="dropzone-desc">{{ t('drag_drop_image_hint') }}</span>
      </div>
    </div>

    <!-- Empty State for Readonly -->
    <div v-if="(readonly || disabled) && imagesList.length === 0" class="empty-images-state">
      <va-icon name="image_not_supported" size="24px" color="secondary" />
      <span>{{ t('no_image') }}</span>
    </div>

    <!-- Image Thumbnail Cards Grid -->
    <div v-if="imagesList.length > 0" class="image-gallery-grid">
      <div
        v-for="(img, idx) in imagesList"
        :key="idx"
        class="image-thumbnail-card"
        @click="openLightbox(idx)"
      >
        <img :src="resolveBlobUrl(img.url)" :alt="img.name || `Image ${idx + 1}`" class="thumbnail-img" />
        
        <!-- Hover Action Overlay -->
        <div class="thumbnail-overlay">
          <div class="overlay-actions" @click.stop>
            <button
              type="button"
              class="action-btn"
              :title="t('preview_image')"
              @click.stop="openLightbox(idx)"
            >
              <va-icon name="zoom_in" size="small" color="#ffffff" />
            </button>
            <button
              type="button"
              class="action-btn"
              :title="t('download_image')"
              @click.stop="downloadImage(img)"
            >
              <va-icon name="download" size="small" color="#ffffff" />
            </button>
            <button
              v-if="!readonly && !disabled"
              type="button"
              class="action-btn action-btn-danger"
              :title="t('delete_image')"
              @click.stop="removeImage(idx)"
            >
              <va-icon name="delete" size="small" color="#ffffff" />
            </button>
          </div>
        </div>

        <!-- Badge for Multi-Image Index -->
        <div v-if="imagesList.length > 1" class="thumbnail-badge">
          {{ idx + 1 }}
        </div>
      </div>
    </div>

    <!-- Lightbox Carousel Modal Component -->
    <ImageLightboxModal
      v-model="showLightbox"
      :images="imagesList"
      :initial-index="currentImageIndex"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { useFileDownloader } from '~/composables/useFileDownloader'
import { useAuthenticatedImage } from '~/composables/useAuthenticatedImage'
import ModalControls from './ModalControls.vue'
import AppModal from './AppModal.vue'
import ImageLightboxModal from './ImageLightboxModal.vue'

export interface ImageItem {
  url: string
  name?: string
  fileId?: string
}

const { t } = useI18n()
const { customFetch } = useCustomFetch()
const { downloadFileWithAuth } = useFileDownloader()
const { getAuthenticatedImageUrl } = useAuthenticatedImage()

const props = withDefaults(defineProps<{
  modelValue?: string | string[] | ImageItem | ImageItem[] | null
  multiple?: boolean
  readonly?: boolean
  disabled?: boolean
}>(), {
  modelValue: null,
  multiple: false,
  readonly: false,
  disabled: false
})

const emit = defineEmits<{
  (e: 'update:modelValue', val: any): void
  (e: 'change', val: any): void
}>()

const fileInputRef = ref<HTMLInputElement | null>(null)
const isDragging = ref(false)
const isUploading = ref(false)

// Lightbox state
const showLightbox = ref(false)
const isLightboxFullscreen = ref(false)
const currentImageIndex = ref(0)

// 1x1 Transparent placeholder to prevent browser from sending unauthenticated GET requests
const TRANSPARENT_PIXEL = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="1" height="1"/>'

// Authenticated blob URL map (url -> blobUrl)
const blobMap = ref<Record<string, string>>({})

const resolveBlobUrl = (url: string): string => {
  if (!url) return TRANSPARENT_PIXEL
  if (url.startsWith('blob:') || url.startsWith('data:')) return url
  if (blobMap.value[url]) return blobMap.value[url]
  
  // Trigger async fetch if not present
  getAuthenticatedImageUrl(url).then((blobUrl) => {
    if (blobUrl) {
      blobMap.value[url] = blobUrl
    }
  })
  
  return TRANSPARENT_PIXEL
}

// Helper: Extract friendly filename from URL query params or clean path
const extractFriendlyImageName = (rawUrl: string, explicitName?: string, index: number = 0): string => {
  if (explicitName && explicitName.trim() && !explicitName.match(/^[a-f0-9-]{16,}(\.[a-zA-Z0-9]+)?$/i)) {
    return explicitName.trim()
  }
  if (!rawUrl) return t('preview_image')

  // Check URL query parameters (e.g. ?name=...)
  if (rawUrl.includes('?')) {
    try {
      const queryString = rawUrl.split('?')[1]
      const params = new URLSearchParams(queryString)
      const nameParam = params.get('name') || params.get('fileName')
      if (nameParam) {
        return decodeURIComponent(nameParam)
      }
    } catch (e) {}
  }

  // Check path part
  const basePart = rawUrl.split('?')[0].split('/').pop() || ''
  if (basePart) {
    try {
      const decoded = decodeURIComponent(basePart)
      // Check if it is a raw hash or UUID
      if (decoded.length > 20 && /^[a-f0-9-]+(\.[a-zA-Z0-9]+)?$/i.test(decoded)) {
        const ext = decoded.includes('.') ? '.' + decoded.split('.').pop() : '.png'
        return `IMG-${decoded.substring(0, 8)}${ext}`
      }
      return decoded
    } catch (e) {
      return basePart
    }
  }

  return `image_${index + 1}.png`
}

// Helper: Normalize modelValue into ImageItem array
const imagesList = computed<ImageItem[]>(() => {
  let val: any = props.modelValue
  if (!val) return []

  // If val is a JSON string representing an array or object, parse it
  if (typeof val === 'string') {
    const trimmed = val.trim()
    if (trimmed === '' || trimmed === '[]' || trimmed === '{}' || trimmed === 'null' || trimmed === 'undefined' || trimmed === '-') {
      return []
    }
    if ((trimmed.startsWith('[') && trimmed.endsWith(']')) || (trimmed.startsWith('{') && trimmed.endsWith('}'))) {
      try {
        val = JSON.parse(trimmed)
      } catch (e) {
        // Fallback: strip brackets and quotes if parse fails
        val = trimmed.replace(/^[\["\s']+|[\]"\s']+$/g, '')
      }
    }
  }
  
  const sanitizeUrl = (raw: string): string => {
    return String(raw).trim().replace(/^[\["\s']+|[\]"\s']+$/g, '')
  }

  if (Array.isArray(val)) {
    return val.map((item, idx) => {
      if (typeof item === 'string') {
        const cleanUrl = sanitizeUrl(item)
        if (!cleanUrl) return null
        const name = extractFriendlyImageName(cleanUrl, undefined, idx)
        return { url: cleanUrl, name }
      }
      if (item && item.url) {
        const cleanUrl = sanitizeUrl(item.url)
        if (!cleanUrl) return null
        const name = extractFriendlyImageName(cleanUrl, item.name, idx)
        return { ...item, url: cleanUrl, name }
      }
      return null
    }).filter((item): item is ImageItem => item !== null && !!item.url)
  }
  
  if (typeof val === 'string') {
    if (val.includes(',') && props.multiple) {
      return val.split(',').map(s => sanitizeUrl(s)).filter(Boolean).map((url, idx) => ({
        url,
        name: extractFriendlyImageName(url, undefined, idx)
      }))
    }
    const cleanUrl = sanitizeUrl(val)
    if (!cleanUrl) return []
    return [{
      url: cleanUrl,
      name: extractFriendlyImageName(cleanUrl, undefined, 0)
    }]
  }
  
  if (typeof val === 'object' && (val as ImageItem).url) {
    const cleanUrl = sanitizeUrl((val as ImageItem).url)
    if (!cleanUrl) return []
    const name = extractFriendlyImageName(cleanUrl, (val as ImageItem).name, 0)
    return [{ ...(val as ImageItem), url: cleanUrl, name }]
  }
  
  return []
})

// Watch imagesList and preload authenticated blob for each item
watch(imagesList, (list) => {
  list.forEach((item) => {
    if (item.url && !blobMap.value[item.url] && !item.url.startsWith('blob:') && !item.url.startsWith('data:')) {
      getAuthenticatedImageUrl(item.url).then((blobUrl) => {
        if (blobUrl) {
          blobMap.value[item.url] = blobUrl
        }
      })
    }
  })
}, { immediate: true, deep: true })

// Emit value changes back to parent
const updateParentValue = (list: ImageItem[]) => {
  if (props.multiple) {
    const urls = list.map(item => item.url)
    emit('update:modelValue', urls)
    emit('change', urls)
  } else {
    const singleUrl = list.length > 0 ? list[0].url : ''
    emit('update:modelValue', singleUrl)
    emit('change', singleUrl)
  }
}

// Upload file to MinIO
const uploadFile = async (file: File): Promise<ImageItem | null> => {
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const res: any = await customFetch('/api/files/upload', {
      method: 'POST',
      body: formData
    })
    
    const url = res?.url || (res?.fileId ? `/api/files/download/${res.fileId}?name=${encodeURIComponent(file.name)}` : null) || (typeof res === 'string' ? res : null)
    if (url) {
      // Preload blob URL into blobMap
      getAuthenticatedImageUrl(url).then((blobUrl) => {
        if (blobUrl) {
          blobMap.value[url] = blobUrl
        }
      })

      return {
        url,
        name: file.name,
        fileId: res?.fileId
      }
    }
    return null
  } catch (error) {
    console.error('Image upload failed:', error)
    return null
  }
}

// Process multiple image files
const processFiles = async (files: FileList | File[]) => {
  const imageFiles = Array.from(files).filter(f => f.type.startsWith('image/'))
  if (imageFiles.length === 0) return
  
  isUploading.value = true
  try {
    const newItems: ImageItem[] = []
    for (const file of imageFiles) {
      const item = await uploadFile(file)
      if (item) {
        newItems.push(item)
      }
      if (!props.multiple && newItems.length > 0) {
        break
      }
    }
    
    if (newItems.length > 0) {
      const updatedList = props.multiple ? [...imagesList.value, ...newItems] : newItems
      updateParentValue(updatedList)
    }
  } finally {
    isUploading.value = false
  }
}

// File input change
const onFileInputChange = (e: Event) => {
  const target = e.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    processFiles(target.files)
  }
}

// Trigger hidden input
const triggerFileInput = () => {
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
    fileInputRef.value.click()
  }
}

// Drag & drop
const handleDrop = (e: DragEvent) => {
  isDragging.value = false
  if (e.dataTransfer && e.dataTransfer.files) {
    processFiles(e.dataTransfer.files)
  }
}

// Clipboard paste
const handlePaste = (e: ClipboardEvent) => {
  const items = e.clipboardData?.items
  if (!items) return
  const files: File[] = []
  for (const item of Array.from(items)) {
    if (item.type.startsWith('image/')) {
      const f = item.getAsFile()
      if (f) files.push(f)
    }
  }
  if (files.length > 0) {
    e.preventDefault()
    processFiles(files)
  }
}

// Remove image
const removeImage = (index: number) => {
  const updated = [...imagesList.value]
  updated.splice(index, 1)
  updateParentValue(updated)
}

// Lightbox navigation
const openLightbox = (index: number) => {
  currentImageIndex.value = index
  showLightbox.value = true
}

const prevImage = () => {
  if (imagesList.value.length <= 1) return
  currentImageIndex.value = (currentImageIndex.value - 1 + imagesList.value.length) % imagesList.value.length
}

const nextImage = () => {
  if (imagesList.value.length <= 1) return
  currentImageIndex.value = (currentImageIndex.value + 1) % imagesList.value.length
}

// Download
const downloadImage = (img: ImageItem | null) => {
  if (!img) return
  const filename = img.name || img.url.split('/').pop()?.split('?')[0] || 'image.png'
  downloadFileWithAuth(img.url, filename)
}

// Keydown handler for lightbox
const onGlobalKeydown = (e: KeyboardEvent) => {
  if (!showLightbox.value) return
  if (e.key === 'ArrowLeft') {
    prevImage()
  } else if (e.key === 'ArrowRight') {
    nextImage()
  } else if (e.key === 'Escape') {
    showLightbox.value = false
  }
}

onMounted(() => {
  window.addEventListener('keydown', onGlobalKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onGlobalKeydown)
})
</script>

<style scoped>
.image-uploader-wrapper {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  width: 100%;
}

/* Dropzone */
.image-dropzone {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 1.25rem;
  border: 2px dashed var(--va-background-border, #cbd5e1);
  border-radius: 8px;
  background-color: var(--va-background-element, #ffffff);
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
  outline: none;
}

.image-dropzone:hover,
.image-dropzone.is-dragging {
  border-color: var(--va-primary, #2563eb);
  background-color: rgba(37, 99, 235, 0.04);
}

.image-dropzone:focus-visible {
  border-color: var(--va-primary, #2563eb);
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.2);
}

.dropzone-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.35rem;
  text-align: center;
}

.dropzone-title {
  font-size: 0.88rem;
  font-weight: 700;
  color: var(--va-text-primary, #1e293b);
}

.dropzone-desc {
  font-size: 0.75rem;
  color: var(--va-text-secondary, #64748b);
}

.empty-images-state {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: var(--va-background-secondary, #f8fafc);
  border: 1px solid var(--va-background-border, #e2e8f0);
  border-radius: 6px;
  color: var(--va-text-secondary, #64748b);
  font-size: 0.85rem;
}

/* Gallery Grid */
.image-gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 0.75rem;
}

.image-thumbnail-card {
  position: relative;
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid var(--va-background-border, #e2e8f0);
  background-color: var(--va-background-secondary, #f1f5f9);
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.image-thumbnail-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.thumbnail-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.thumbnail-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.image-thumbnail-card:hover .thumbnail-overlay {
  opacity: 1;
}

.overlay-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(4px);
  cursor: pointer;
  transition: background 0.15s ease, transform 0.1s ease;
}

.action-btn:hover {
  background: var(--va-primary, #2563eb);
  transform: scale(1.1);
}

.action-btn-danger:hover {
  background: var(--va-danger, #ef4444);
}

.thumbnail-badge {
  position: absolute;
  bottom: 4px;
  right: 4px;
  background: rgba(0, 0, 0, 0.65);
  color: #ffffff;
  font-size: 0.68rem;
  font-weight: 700;
  padding: 1px 5px;
  border-radius: 10px;
}

/* Lightbox Modal */
.lightbox-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--va-background-border, #e2e8f0);
}

.lightbox-title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex: 1;
}

.lightbox-title {
  font-weight: 700;
  font-size: 1rem;
  color: var(--va-text-primary, #1e293b);
  max-width: 480px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.lightbox-counter {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--va-text-secondary, #64748b);
  flex-shrink: 0;
}

.lightbox-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: auto;
  flex-shrink: 0;
}

.lightbox-body {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 380px;
  max-height: 68vh;
  background-color: #0f172a;
  border-radius: 6px;
  overflow: hidden;
  margin-top: 0.75rem;
}

:deep(.va-modal--fullscreen .lightbox-body) {
  max-height: calc(100vh - 160px);
  height: calc(100vh - 160px);
}

:deep(.va-modal--fullscreen .lightbox-main-img) {
  max-height: calc(100vh - 180px);
}

.lightbox-image-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.lightbox-main-img {
  max-width: 100%;
  max-height: 62vh;
  object-fit: contain;
  border-radius: 4px;
}

.carousel-nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  cursor: pointer;
  transition: background 0.2s ease, transform 0.15s ease;
  z-index: 10;
}

.carousel-nav-btn:hover {
  background: var(--va-primary, #2563eb);
  transform: translateY(-50%) scale(1.08);
}

.prev-btn {
  left: 12px;
}

.next-btn {
  right: 12px;
}

/* Lightbox Thumbnail Strip */
.lightbox-thumbnail-strip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  overflow-x: auto;
  padding: 0.75rem 0 0 0;
}

.strip-thumb {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  overflow: hidden;
  border: 2px solid transparent;
  opacity: 0.6;
  cursor: pointer;
  transition: all 0.15s ease;
  flex-shrink: 0;
}

.strip-thumb:hover {
  opacity: 0.9;
}

.strip-thumb.is-active {
  border-color: var(--va-primary, #2563eb);
  opacity: 1;
  transform: scale(1.05);
}

.strip-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
