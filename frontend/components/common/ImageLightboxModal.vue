<template>
  <Teleport to="body">
    <div
      v-if="modelValue"
      class="custom-lightbox-backdrop"
      tabindex="-1"
      @keydown.left.stop="prevImage"
      @keydown.right.stop="nextImage"
      @keydown.esc.stop="closeLightbox"
      @click.self="closeLightbox"
    >
      <div class="custom-lightbox-container" :class="{ 'is-fullscreen': isFullscreen }">
        <!-- Header Bar -->
        <div class="lightbox-header-bar">
          <div class="lightbox-header-left">
            <va-icon name="image" color="#ffffff" size="20px" class="mr-2" />
            <h3 class="lightbox-title">{{ lightboxTitle }}</h3>
          </div>

          <div class="lightbox-header-actions">
            <!-- Zoom Out Button -->
            <button
              type="button"
              class="lightbox-btn"
              :title="t('zoom_out')"
              :style="{ opacity: zoomLevel <= 0.5 ? 0.4 : 0.9 }"
              @click.stop="zoomOut"
            >
              <va-icon name="zoom_out" size="20px" color="#ffffff" />
            </button>

            <!-- Zoom Level Reset Indicator -->
            <button
              type="button"
              class="lightbox-btn zoom-indicator"
              :title="t('zoom_reset')"
              @click.stop="resetZoom"
            >
              {{ Math.round(zoomLevel * 100) }}%
            </button>

            <!-- Zoom In Button -->
            <button
              type="button"
              class="lightbox-btn"
              :title="t('zoom_in')"
              :style="{ opacity: zoomLevel >= 4 ? 0.4 : 0.9 }"
              @click.stop="zoomIn"
            >
              <va-icon name="zoom_in" size="20px" color="#ffffff" />
            </button>

            <!-- Download Button -->
            <button
              type="button"
              class="lightbox-btn"
              :title="t('download_image')"
              @click.stop="downloadCurrentImage"
            >
              <va-icon name="download" size="20px" color="#ffffff" />
            </button>

            <!-- Fullscreen Toggle -->
            <button
              type="button"
              class="lightbox-btn"
              :title="isFullscreen ? t('exit_fullscreen') : t('fullscreen')"
              @click.stop="toggleFullscreen"
            >
              <va-icon :name="isFullscreen ? 'fullscreen_exit' : 'fullscreen'" size="20px" color="#ffffff" />
            </button>

            <!-- Close Button -->
            <button
              type="button"
              class="lightbox-btn close-btn"
              :title="t('close')"
              @click.stop="closeLightbox"
            >
              <va-icon name="close" size="22px" color="#ffffff" />
            </button>
          </div>
        </div>

        <!-- Main Display Area -->
        <div
          class="lightbox-body-area"
          @wheel.prevent.stop="handleWheel"
        >
          <!-- Prev Button -->
          <button
            v-if="normalizedImages.length > 1"
            type="button"
            class="carousel-nav-btn prev-btn"
            :title="t('image_carousel_prev')"
            @click.stop="prevImage"
          >
            <va-icon name="chevron_left" size="32px" color="#ffffff" />
          </button>

          <!-- Image Wrapper with Pan/Zoom -->
          <div
            class="lightbox-image-wrapper"
            :style="{
              cursor: zoomLevel > 1 ? (isDragging ? 'grabbing' : 'grab') : 'zoom-in'
            }"
            @mousedown.stop="handleMouseDown"
            @mousemove.stop="handleMouseMove"
            @mouseup.stop="handleMouseUp"
            @mouseleave.stop="handleMouseUp"
            @dblclick.stop="handleDoubleClick"
          >
            <va-progress-circle v-if="isLoadingImage && !currentBlobUrl" indeterminate size="large" color="#ffffff" style="position: absolute;" />
            <img
              v-if="currentImage"
              :src="currentBlobUrl || currentImage.url"
              :alt="currentImage.name || 'Preview'"
              class="lightbox-main-img"
              :style="imageTransformStyle"
              draggable="false"
            />
          </div>

          <!-- Next Button -->
          <button
            v-if="normalizedImages.length > 1"
            type="button"
            class="carousel-nav-btn next-btn"
            :title="t('image_carousel_next')"
            @click.stop="nextImage"
          >
            <va-icon name="chevron_right" size="32px" color="#ffffff" />
          </button>
        </div>

        <!-- Bottom Thumbnail Strip -->
        <div v-if="normalizedImages.length > 1" class="lightbox-thumbnail-strip">
          <div
            v-for="(img, idx) in normalizedImages"
            :key="idx"
            class="strip-thumb"
            :class="{ 'is-active': idx === currentIndex }"
            @click.stop="currentIndex = idx"
          >
            <img :src="blobUrls[img.url] || img.url" :alt="img.name || ''" />
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthenticatedImage } from '~/composables/useAuthenticatedImage'
import { useFileDownloader } from '~/composables/useFileDownloader'

export interface LightboxImageItem {
  url: string
  name?: string
  size?: number
}

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    images: (LightboxImageItem | string)[]
    initialIndex?: number
  }>(),
  {
    modelValue: false,
    images: () => [],
    initialIndex: 0
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
}>()

const { t } = useI18n()
const { getAuthenticatedImageUrl, getCachedBlobUrl } = useAuthenticatedImage()
const { downloadFileWithAuth } = useFileDownloader()

const isFullscreen = ref(false)
const currentIndex = ref(props.initialIndex)
const blobUrls = ref<Record<string, string>>({})

// Magnifier / Zoom / Pan state
const zoomLevel = ref(1)
const panX = ref(0)
const panY = ref(0)
const isDragging = ref(false)
const dragStartX = ref(0)
const dragStartY = ref(0)
const dragStartPanX = ref(0)
const dragStartPanY = ref(0)

const resetZoom = () => {
  zoomLevel.value = 1
  panX.value = 0
  panY.value = 0
  isDragging.value = false
}

const zoomIn = () => {
  zoomLevel.value = Math.min(Math.round((zoomLevel.value + 0.25) * 100) / 100, 4)
}

const zoomOut = () => {
  zoomLevel.value = Math.max(Math.round((zoomLevel.value - 0.25) * 100) / 100, 0.5)
  if (zoomLevel.value <= 1) {
    panX.value = 0
    panY.value = 0
  }
}

const handleWheel = (e: WheelEvent) => {
  const delta = e.deltaY < 0 ? 0.2 : -0.2
  const nextZoom = Math.min(Math.max(Math.round((zoomLevel.value + delta) * 100) / 100, 0.5), 4)
  zoomLevel.value = nextZoom
  if (nextZoom <= 1) {
    panX.value = 0
    panY.value = 0
  }
}

const handleDoubleClick = () => {
  if (zoomLevel.value === 1) {
    zoomLevel.value = 2.2
  } else {
    resetZoom()
  }
}

const handleMouseDown = (e: MouseEvent) => {
  if (zoomLevel.value <= 1) {
    // Zoom in on click if at 1x
    return
  }
  isDragging.value = true
  dragStartX.value = e.clientX
  dragStartY.value = e.clientY
  dragStartPanX.value = panX.value
  dragStartPanY.value = panY.value
}

const handleMouseMove = (e: MouseEvent) => {
  if (!isDragging.value || zoomLevel.value <= 1) return
  const dx = e.clientX - dragStartX.value
  const dy = e.clientY - dragStartY.value
  panX.value = dragStartPanX.value + dx
  panY.value = dragStartPanY.value + dy
}

const handleMouseUp = () => {
  isDragging.value = false
}

const imageTransformStyle = computed(() => {
  return {
    transform: `scale(${zoomLevel.value}) translate(${panX.value / zoomLevel.value}px, ${panY.value / zoomLevel.value}px)`,
    transition: isDragging.value ? 'none' : 'transform 0.18s cubic-bezier(0.25, 0.8, 0.25, 1)'
  }
})

const normalizedImages = computed<LightboxImageItem[]>(() => {
  return props.images.map((item, idx) => {
    if (typeof item === 'string') {
      return { url: item, name: `${t('preview_image')} ${idx + 1}` }
    }
    return {
      url: item.url,
      name: item.name || `${t('preview_image')} ${idx + 1}`,
      size: item.size
    }
  })
})

const currentImage = computed<LightboxImageItem | null>(() => {
  return normalizedImages.value[currentIndex.value] || null
})

const lightboxTitle = computed(() => {
  const name = currentImage.value?.name || t('preview_image')
  if (normalizedImages.value.length > 1) {
    return `${name}  (${currentIndex.value + 1} / ${normalizedImages.value.length})`
  }
  return name
})

const isLoadingImage = ref(false)

const closeLightbox = () => {
  emit('update:modelValue', false)
  isFullscreen.value = false
  resetZoom()
}

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
  resetZoom()
}

const currentBlobUrl = computed(() => {
  if (!currentImage.value) return ''
  return resolveBlobUrl(currentImage.value.url)
})

const resolveBlobUrl = (url: string) => {
  if (!url) return ''
  if (blobUrls.value[url]) return blobUrls.value[url]
  if (typeof getCachedBlobUrl === 'function') {
    const cached = getCachedBlobUrl(url)
    if (cached) return cached
  }
  return url
}

const prevImage = () => {
  if (normalizedImages.value.length === 0) return
  resetZoom()
  currentIndex.value = (currentIndex.value - 1 + normalizedImages.value.length) % normalizedImages.value.length
}

const nextImage = () => {
  if (normalizedImages.value.length === 0) return
  resetZoom()
  currentIndex.value = (currentIndex.value + 1) % normalizedImages.value.length
}

const downloadCurrentImage = () => {
  if (!currentImage.value) return
  const url = currentImage.value.url
  const filename = currentImage.value.name || url.split('/').pop()?.split('?')[0] || 'image.png'
  downloadFileWithAuth(url, filename)
}

// Preload blob URLs
const loadCurrentBlob = async () => {
  if (!currentImage.value) return
  const url = currentImage.value.url
  if (url && !blobUrls.value[url]) {
    isLoadingImage.value = true
    try {
      const blobUrl = await getAuthenticatedImageUrl(url)
      if (blobUrl) {
        blobUrls.value = { ...blobUrls.value, [url]: blobUrl }
      }
    } catch (e) {
      console.error('Failed to load authenticated image blob:', e)
    } finally {
      isLoadingImage.value = false
    }
  }
}

watch(
  () => props.images,
  (newImages) => {
    if (!newImages) return
    newImages.forEach(item => {
      const url = typeof item === 'string' ? item : item.url
      if (url && !blobUrls.value[url]) {
        getAuthenticatedImageUrl(url).then(blobUrl => {
          if (blobUrl) {
            blobUrls.value = { ...blobUrls.value, [url]: blobUrl }
          }
        }).catch(() => {})
      }
    })
  },
  { immediate: true, deep: true }
)

watch(
  () => currentIndex.value,
  () => {
    resetZoom()
    loadCurrentBlob()
  }
)

watch(
  () => props.initialIndex,
  (val) => {
    currentIndex.value = val || 0
    resetZoom()
    loadCurrentBlob()
  }
)

watch(
  () => props.modelValue,
  (val) => {
    if (!val) {
      isFullscreen.value = false
      resetZoom()
    } else {
      currentIndex.value = props.initialIndex || 0
      resetZoom()
      loadCurrentBlob()
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.custom-lightbox-backdrop {
  position: fixed;
  inset: 0;
  z-index: 999999;
  background: rgba(15, 23, 42, 0.88);
  backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
  box-sizing: border-box;
}

.custom-lightbox-container {
  display: flex;
  flex-direction: column;
  background: #1e293b;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.7);
  width: 900px;
  max-width: 95vw;
  height: 720px;
  max-height: 90vh;
  overflow: hidden;
  position: relative;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.custom-lightbox-container.is-fullscreen {
  width: 100vw !important;
  max-width: 100vw !important;
  height: 100vh !important;
  max-height: 100vh !important;
  border-radius: 0 !important;
  border: none !important;
}

/* Header Bar */
.lightbox-header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.85rem 1.25rem;
  background: rgba(15, 23, 42, 0.6);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  user-select: none;
  flex-shrink: 0;
}

.lightbox-header-left {
  display: flex;
  align-items: center;
  min-width: 0;
}

.lightbox-title {
  color: #f8fafc;
  font-size: 1.05rem;
  font-weight: 600;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.lightbox-header-actions {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  flex-shrink: 0;
}

.lightbox-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 6px;
  border: none;
  background: rgba(255, 255, 255, 0.08);
  cursor: pointer;
  transition: background 0.15s ease, transform 0.1s ease;
}

.lightbox-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.lightbox-btn.zoom-indicator {
  width: 48px;
  font-size: 0.8rem;
  font-weight: 700;
  font-family: monospace;
  color: #ffffff;
}

.lightbox-btn.close-btn:hover {
  background: #ef4444;
}

/* Body Area */
.lightbox-body-area {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #090d16;
  overflow: hidden;
  user-select: none;
}

.lightbox-image-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.lightbox-main-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 4px;
  transform-origin: center center;
  will-change: transform;
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
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  cursor: pointer;
  transition: background 0.2s ease, transform 0.15s ease;
  z-index: 20;
}

.carousel-nav-btn:hover {
  background: var(--va-primary, #2563eb);
  transform: translateY(-50%) scale(1.08);
}

.prev-btn {
  left: 16px;
}

.next-btn {
  right: 16px;
}

/* Bottom Thumbnail Strip */
.lightbox-thumbnail-strip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  overflow-x: auto;
  padding: 0.75rem 1rem;
  background: rgba(15, 23, 42, 0.6);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  flex-shrink: 0;
}

.strip-thumb {
  width: 52px;
  height: 52px;
  border-radius: 6px;
  overflow: hidden;
  border: 2px solid transparent;
  opacity: 0.5;
  cursor: pointer;
  transition: all 0.15s ease;
  flex-shrink: 0;
  background: #000;
}

.strip-thumb:hover {
  opacity: 0.85;
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
