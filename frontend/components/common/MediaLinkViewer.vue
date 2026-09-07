<template>
  <div class="media-link-viewer-container">
    <!-- 1. READONLY / VIEW MODE -->
    <template v-if="readonly">
      <div v-if="mediaList.length === 0" class="empty-media-placeholder">
        <va-icon name="perm_media" size="large" color="secondary" />
        <span class="empty-text">{{ $t('media_unsupported_or_empty') }}</span>
      </div>

      <div v-else class="media-list-wrapper" :class="{ 'grid-layout': mediaList.length > 1 }">
        <div
          v-for="(item, idx) in mediaList"
          :key="idx"
          class="media-card"
        >
          <!-- Media Header / Type Badge & External Link -->
          <div class="media-card-header">
            <span class="media-type-badge" :class="getTypeBadgeClass(item.type)">
              {{ getTypeLabel(item.type) }}
            </span>
            <div class="media-actions">
              <button
                type="button"
                class="media-action-btn"
                :title="$t('media_copy_link')"
                @click.stop="copyLink(item.url)"
              >
                <va-icon name="content_copy" size="small" />
              </button>
              <a
                :href="item.url"
                target="_blank"
                rel="noopener noreferrer"
                class="media-action-btn"
                :title="$t('media_open_link')"
                @click.stop
              >
                <va-icon name="open_in_new" size="small" />
              </a>
            </div>
          </div>

          <!-- Media Body by Type -->
          <div class="media-card-body">
            <!-- 1.1 IMAGE -->
            <div
              v-if="item.type === 'IMAGE'"
              class="image-preview-wrapper"
              @click="openLightbox(item.url, item.title)"
            >
              <img
                :src="item.url"
                :alt="item.title || $t('media_type_image')"
                class="media-preview-img"
                loading="lazy"
              />
              <div class="image-zoom-overlay">
                <va-icon name="zoom_in" size="small" color="#ffffff" />
                <span>{{ $t('click_to_zoom') }}</span>
              </div>
            </div>

            <!-- 1.2 DIRECT VIDEO -->
            <div v-else-if="item.type === 'VIDEO'" class="video-preview-wrapper">
              <video
                :src="item.url"
                controls
                preload="metadata"
                class="media-preview-video"
              >
                {{ $t('media_video_not_supported') }}
              </video>
            </div>

            <!-- 1.3 YOUTUBE / VIMEO -->
            <div v-else-if="item.type === 'YOUTUBE' || item.type === 'VIMEO'" class="iframe-preview-wrapper">
              <iframe
                :src="item.embedUrl"
                class="media-preview-iframe"
                frameborder="0"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                allowfullscreen
              ></iframe>
            </div>

            <!-- 1.4 AUDIO -->
            <div v-else-if="item.type === 'AUDIO'" class="audio-preview-wrapper">
              <audio :src="item.url" controls class="media-preview-audio"></audio>
            </div>

            <!-- 1.5 FALLBACK WEB LINK -->
            <div v-else class="link-fallback-wrapper">
              <va-icon name="link" size="24px" color="primary" />
              <div class="link-text-info">
                <a :href="item.url" target="_blank" rel="noopener noreferrer" class="link-url-text">
                  {{ item.url }}
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 2. EDIT MODE (readonly = false) -->
    <template v-else>
      <div class="media-edit-wrapper">
        <!-- Single Value Mode -->
        <div v-if="!multiple" class="media-input-group">
          <div class="input-with-actions">
            <va-icon name="link" size="18px" class="input-leading-icon" />
            <input
              type="text"
              class="media-url-input"
              :value="singleUrl"
              :placeholder="placeholder || $t('media_link_placeholder')"
              :disabled="disabled"
              @input="onSingleInput($event)"
            />
            <button
              v-if="singleUrl"
              type="button"
              class="input-clear-btn"
              :disabled="disabled"
              @click="clearSingleUrl"
            >
              <va-icon name="close" size="16px" />
            </button>
          </div>

          <!-- Live Preview Box -->
          <div v-if="singleMedia && singleMedia.type !== 'EMPTY'" class="live-preview-box">
            <div class="live-preview-header">
              <span class="media-type-badge" :class="getTypeBadgeClass(singleMedia.type)">
                {{ getTypeLabel(singleMedia.type) }}
              </span>
              <span class="live-preview-title">{{ $t('media_preview') }}</span>
              <a
                :href="singleMedia.url"
                target="_blank"
                rel="noopener noreferrer"
                class="media-action-btn ml-auto"
                :title="$t('media_open_link')"
              >
                <va-icon name="open_in_new" size="small" />
              </a>
            </div>

            <div class="live-preview-content">
              <!-- Image Preview -->
              <div
                v-if="singleMedia.type === 'IMAGE'"
                class="image-preview-wrapper compact"
                @click="openLightbox(singleMedia.url)"
              >
                <img :src="singleMedia.url" class="media-preview-img" alt="Preview" />
                <div class="image-zoom-overlay">
                  <va-icon name="zoom_in" size="small" color="#ffffff" />
                </div>
              </div>

              <!-- Video Preview -->
              <video
                v-else-if="singleMedia.type === 'VIDEO'"
                :src="singleMedia.url"
                controls
                preload="metadata"
                class="media-preview-video compact"
              ></video>

              <!-- Iframe Preview -->
              <div
                v-else-if="singleMedia.type === 'YOUTUBE' || singleMedia.type === 'VIMEO'"
                class="iframe-preview-wrapper compact"
              >
                <iframe
                  :src="singleMedia.embedUrl"
                  class="media-preview-iframe"
                  frameborder="0"
                  allowfullscreen
                ></iframe>
              </div>

              <!-- Audio Preview -->
              <audio
                v-else-if="singleMedia.type === 'AUDIO'"
                :src="singleMedia.url"
                controls
                class="media-preview-audio"
              ></audio>

              <!-- Web link Preview -->
              <div v-else class="link-fallback-wrapper">
                <va-icon name="link" size="20px" color="primary" />
                <span class="link-url-text">{{ singleMedia.url }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Multiple Value Mode -->
        <div v-else class="multi-media-input-group">
          <div
            v-for="(urlItem, idx) in multiUrls"
            :key="idx"
            class="multi-input-row"
          >
            <div class="input-with-actions">
              <va-icon name="link" size="18px" class="input-leading-icon" />
              <input
                type="text"
                class="media-url-input"
                :value="urlItem"
                :placeholder="placeholder || $t('media_link_placeholder')"
                :disabled="disabled"
                @input="onMultiInput(idx, $event)"
              />
              <button
                type="button"
                class="input-remove-btn"
                :disabled="disabled"
                :title="$t('media_remove_link')"
                @click="removeMultiUrl(idx)"
              >
                <va-icon name="delete_outline" size="18px" color="danger" />
              </button>
            </div>

            <!-- Per-item mini preview -->
            <div
              v-if="detectItemMedia(urlItem).type !== 'EMPTY'"
              class="multi-item-preview"
            >
              <span class="media-type-badge small" :class="getTypeBadgeClass(detectItemMedia(urlItem).type)">
                {{ getTypeLabel(detectItemMedia(urlItem).type) }}
              </span>
              <img
                v-if="detectItemMedia(urlItem).type === 'IMAGE' || detectItemMedia(urlItem).thumbnailUrl"
                :src="detectItemMedia(urlItem).thumbnailUrl || detectItemMedia(urlItem).url"
                class="multi-thumb-img"
                alt="Thumbnail"
                @click="openLightbox(detectItemMedia(urlItem).url)"
              />
              <span v-else class="multi-preview-text">{{ urlItem }}</span>
            </div>
          </div>

          <va-button
            preset="secondary"
            size="small"
            icon="add"
            class="mt-2"
            :disabled="disabled"
            @click="addMultiUrl"
          >
            {{ $t('media_add_link') }}
          </va-button>
        </div>
      </div>
    </template>

    <!-- Image Lightbox Modal -->
    <ImageLightboxModal
      v-model="lightboxOpen"
      :images="lightboxImages"
      :initial-index="lightboxIndex"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { detectMediaType, parseMediaList, type MediaInfo, type MediaType } from '~/utils/mediaUtils'
import ImageLightboxModal from './ImageLightboxModal.vue'

const props = withDefaults(defineProps<{
  modelValue?: string | string[] | any
  readonly?: boolean
  disabled?: boolean
  multiple?: boolean
  placeholder?: string
  title?: string
}>(), {
  modelValue: '',
  readonly: false,
  disabled: false,
  multiple: false,
  placeholder: '',
  title: ''
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: any): void
}>()

const { t } = useI18n()

// Parsed Media List for Readonly View
const mediaList = computed<MediaInfo[]>(() => {
  return parseMediaList(props.modelValue)
})

// Single URL for Single Mode
const singleUrl = computed<string>(() => {
  if (Array.isArray(props.modelValue)) {
    return props.modelValue.length > 0 ? String(props.modelValue[0]) : ''
  }
  if (typeof props.modelValue === 'object' && props.modelValue !== null) {
    return props.modelValue.url || ''
  }
  return props.modelValue ? String(props.modelValue) : ''
})

const singleMedia = computed<MediaInfo>(() => {
  return detectMediaType(singleUrl.value)
})

// Multi URLs for Multi Mode
const multiUrls = computed<string[]>(() => {
  if (Array.isArray(props.modelValue)) {
    return props.modelValue.map(v => typeof v === 'object' && v ? (v.url || '') : String(v))
  }
  if (typeof props.modelValue === 'string' && props.modelValue.trim()) {
    try {
      const parsed = JSON.parse(props.modelValue)
      if (Array.isArray(parsed)) {
        return parsed.map(v => typeof v === 'object' && v ? (v.url || '') : String(v))
      }
    } catch (e) {
      return props.modelValue.split(',').map(s => s.trim()).filter(Boolean)
    }
  }
  return singleUrl.value ? [singleUrl.value] : []
})

const detectItemMedia = (url: string) => {
  return detectMediaType(url)
}

// Event Handlers for Single Mode
const onSingleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  emit('update:modelValue', target.value)
}

const clearSingleUrl = () => {
  emit('update:modelValue', '')
}

// Event Handlers for Multi Mode
const onMultiInput = (index: number, event: Event) => {
  const target = event.target as HTMLInputElement
  const updated = [...multiUrls.value]
  updated[index] = target.value
  emit('update:modelValue', updated)
}

const addMultiUrl = () => {
  const updated = [...multiUrls.value, '']
  emit('update:modelValue', updated)
}

const removeMultiUrl = (index: number) => {
  const updated = [...multiUrls.value]
  updated.splice(index, 1)
  emit('update:modelValue', updated)
}

// Lightbox state
const lightboxOpen = ref(false)
const lightboxImages = ref<{ url: string; name?: string }[]>([])
const lightboxIndex = ref(0)
const lightboxTitle = ref('')

const openLightbox = (url: string, name?: string) => {
  lightboxImages.value = [{ url, name: name || t('preview_image') }]
  lightboxIndex.value = 0
  lightboxTitle.value = name || t('preview_image')
  lightboxOpen.value = true
}

// Helper Labels and Badge Classes
const getTypeLabel = (type: MediaType) => {
  switch (type) {
    case 'IMAGE': return t('media_type_image')
    case 'VIDEO': return t('media_type_video')
    case 'YOUTUBE': return 'YouTube'
    case 'VIMEO': return 'Vimeo'
    case 'AUDIO': return t('media_type_audio')
    case 'LINK': return t('media_type_link')
    default: return ''
  }
}

const getTypeBadgeClass = (type: MediaType) => {
  switch (type) {
    case 'IMAGE': return 'badge-image'
    case 'VIDEO': return 'badge-video'
    case 'YOUTUBE': return 'badge-youtube'
    case 'VIMEO': return 'badge-vimeo'
    case 'AUDIO': return 'badge-audio'
    default: return 'badge-link'
  }
}

const copyLink = async (url: string) => {
  if (typeof navigator !== 'undefined' && navigator.clipboard) {
    try {
      await navigator.clipboard.writeText(url)
    } catch (e) {
      console.warn('Failed to copy link', e)
    }
  }
}
</script>

<style scoped>
.media-link-viewer-container {
  width: 100%;
}

.empty-media-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 1.5rem 1rem;
  background: var(--va-background-element, #f8fafc);
  border: 1px dashed var(--va-background-border, #cbd5e1);
  border-radius: 8px;
  gap: 0.5rem;
}

.empty-text {
  font-size: 0.85rem;
  color: var(--va-text-secondary, #64748b);
}

.media-list-wrapper {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  width: 100%;
}

.media-list-wrapper.grid-layout {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
}

.media-card {
  border: 1px solid var(--va-background-border, #e2e8f0);
  border-radius: 8px;
  overflow: hidden;
  background: var(--va-background-primary, #ffffff);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.media-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.4rem 0.6rem;
  background: var(--va-background-element, #f8fafc);
  border-bottom: 1px solid var(--va-background-border, #e2e8f0);
}

.media-type-badge {
  font-size: 0.72rem;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 12px;
  text-transform: uppercase;
}

.media-type-badge.small {
  font-size: 0.68rem;
  padding: 1px 6px;
}

.badge-image {
  background: rgba(16, 185, 129, 0.12);
  color: #059669;
}

.badge-video {
  background: rgba(59, 130, 246, 0.12);
  color: #2563eb;
}

.badge-youtube {
  background: rgba(239, 68, 68, 0.12);
  color: #dc2626;
}

.badge-vimeo {
  background: rgba(14, 165, 233, 0.12);
  color: #0284c7;
}

.badge-audio {
  background: rgba(168, 85, 247, 0.12);
  color: #9333ea;
}

.badge-link {
  background: rgba(100, 116, 139, 0.12);
  color: #475569;
}

.media-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.media-action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  color: var(--va-text-secondary, #64748b);
  cursor: pointer;
  padding: 3px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.media-action-btn:hover {
  background: rgba(0, 0, 0, 0.05);
  color: var(--va-primary, #2563eb);
}

.media-card-body {
  padding: 0.5rem;
  display: flex;
  justify-content: center;
  align-items: center;
}

.image-preview-wrapper {
  position: relative;
  width: 100%;
  max-height: 360px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  border-radius: 6px;
  background: #000;
  cursor: zoom-in;
}

.image-preview-wrapper.compact {
  max-height: 200px;
}

.media-preview-img {
  max-width: 100%;
  max-height: 360px;
  object-fit: contain;
  display: block;
}

.image-preview-wrapper:hover .image-zoom-overlay {
  opacity: 1;
}

.image-zoom-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #fff;
  font-size: 0.82rem;
  font-weight: 600;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.video-preview-wrapper {
  width: 100%;
  display: flex;
  justify-content: center;
  background: #000;
  border-radius: 6px;
  overflow: hidden;
}

.media-preview-video {
  width: 100%;
  max-height: 360px;
  object-fit: contain;
  outline: none;
}

.media-preview-video.compact {
  max-height: 200px;
}

.iframe-preview-wrapper {
  position: relative;
  width: 100%;
  padding-bottom: 56.25%; /* 16:9 Aspect Ratio */
  height: 0;
  overflow: hidden;
  border-radius: 6px;
  background: #000;
}

.iframe-preview-wrapper.compact {
  padding-bottom: 56.25%;
}

.media-preview-iframe {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.audio-preview-wrapper {
  width: 100%;
  padding: 0.5rem;
}

.media-preview-audio {
  width: 100%;
}

.link-fallback-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 0.5rem;
  overflow: hidden;
}

.link-url-text {
  font-size: 0.85rem;
  color: var(--va-primary, #2563eb);
  text-decoration: underline;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Edit Mode Styles */
.media-edit-wrapper {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  width: 100%;
}

.input-with-actions {
  display: flex;
  align-items: center;
  border: 1px solid var(--va-background-border, #cbd5e1);
  border-radius: 6px;
  padding: 0.35rem 0.6rem;
  background: var(--va-background-primary, #ffffff);
  transition: border-color 0.2s ease;
}

.input-with-actions:focus-within {
  border-color: var(--va-primary, #2563eb);
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.15);
}

.input-leading-icon {
  color: var(--va-text-secondary, #64748b);
  margin-right: 6px;
}

.media-url-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 0.875rem;
  background: transparent;
  color: var(--va-text-primary, #1e293b);
  min-width: 0;
}

.input-clear-btn,
.input-remove-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 2px 4px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--va-text-secondary, #64748b);
  border-radius: 4px;
}

.input-clear-btn:hover {
  background: rgba(0, 0, 0, 0.05);
}

.live-preview-box {
  margin-top: 0.4rem;
  border: 1px solid var(--va-background-border, #e2e8f0);
  border-radius: 6px;
  background: var(--va-background-element, #f8fafc);
  overflow: hidden;
}

.live-preview-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0.35rem 0.6rem;
  border-bottom: 1px solid var(--va-background-border, #e2e8f0);
  background: var(--va-background-element, #f1f5f9);
}

.live-preview-title {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--va-text-secondary, #475569);
}

.live-preview-content {
  padding: 0.4rem;
}

.multi-input-row {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  margin-bottom: 0.5rem;
}

.multi-item-preview {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0.2rem 0.4rem;
  font-size: 0.78rem;
  background: var(--va-background-element, #f8fafc);
  border-radius: 4px;
}

.multi-thumb-img {
  width: 24px;
  height: 24px;
  border-radius: 3px;
  object-fit: cover;
  cursor: pointer;
}

.multi-preview-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--va-text-secondary, #64748b);
}
</style>
