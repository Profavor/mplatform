<template>
  <Teleport to="body">
    <div
      v-if="modelValue"
      class="video-player-modal-backdrop"
      tabindex="-1"
      @keydown.esc.stop="closeModal"
      @click.self="closeModal"
    >
      <div class="video-player-modal-container">
        <!-- Header -->
        <div class="video-player-header">
          <div class="header-left">
            <va-icon name="play_circle" color="#ffffff" size="20px" class="mr-2" />
            <span class="video-title">{{ title || $t('media_video_player') }}</span>
          </div>

          <div class="header-right">
            <a
              v-if="mediaInfo.url"
              :href="mediaInfo.url"
              target="_blank"
              rel="noopener noreferrer"
              class="header-btn"
              :title="$t('media_open_link')"
            >
              <va-icon name="open_in_new" size="18px" color="#ffffff" />
            </a>
            <button
              type="button"
              class="header-btn close-btn"
              :title="$t('close')"
              @click.stop="closeModal"
            >
              <va-icon name="close" size="20px" color="#ffffff" />
            </button>
          </div>
        </div>

        <!-- Video Player Body -->
        <div class="video-player-body">
          <!-- Direct Video Player -->
          <div v-if="mediaInfo.type === 'VIDEO'" class="player-wrapper">
            <video
              :src="mediaInfo.url"
              controls
              autoplay
              preload="auto"
              class="html5-video-player"
            >
              {{ $t('media_video_not_supported') }}
            </video>
          </div>

          <!-- YouTube / Vimeo Iframe Embed -->
          <div v-else-if="mediaInfo.type === 'YOUTUBE' || mediaInfo.type === 'VIMEO'" class="player-wrapper iframe-box">
            <iframe
              :src="mediaInfo.embedUrl"
              class="embed-iframe"
              frameborder="0"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
              allowfullscreen
            ></iframe>
          </div>

          <!-- Fallback -->
          <div v-else class="player-fallback">
            <va-icon name="link" size="32px" color="primary" class="mb-2" />
            <span class="fallback-text">{{ mediaInfo.url }}</span>
            <a
              :href="mediaInfo.url"
              target="_blank"
              rel="noopener noreferrer"
              class="fallback-open-btn mt-3"
            >
              {{ $t('media_open_link') }}
            </a>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { detectMediaType, type MediaInfo } from '~/utils/mediaUtils'

const props = withDefaults(defineProps<{
  modelValue: boolean
  url: string
  title?: string
}>(), {
  modelValue: false,
  url: '',
  title: ''
})

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
}>()

const { t } = useI18n()

const mediaInfo = computed<MediaInfo>(() => {
  return detectMediaType(props.url)
})

const closeModal = () => {
  emit('update:modelValue', false)
}

// Lock body scroll while open
watch(() => props.modelValue, (isOpen) => {
  if (typeof document !== 'undefined') {
    if (isOpen) {
      document.body.style.overflow = 'hidden'
    } else {
      document.body.style.overflow = ''
    }
  }
})
</script>

<style scoped>
.video-player-modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(4px);
  z-index: 999999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
  box-sizing: border-box;
}

.video-player-modal-container {
  width: 100%;
  max-width: 900px;
  background: #111827;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
}

.video-player-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1.25rem;
  background: #1f2937;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  overflow: hidden;
}

.video-title {
  color: #f9fafb;
  font-size: 0.95rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-btn {
  background: rgba(255, 255, 255, 0.1);
  border: none;
  color: #ffffff;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  text-decoration: none;
  transition: background 0.2s;
}

.header-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.video-player-body {
  position: relative;
  width: 100%;
  background: #000;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 320px;
}

.player-wrapper {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.player-wrapper.iframe-box {
  position: relative;
  width: 100%;
  padding-bottom: 56.25%; /* 16:9 ratio */
  height: 0;
}

.html5-video-player {
  width: 100%;
  max-height: 70vh;
  object-fit: contain;
  outline: none;
}

.embed-iframe {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.player-fallback {
  padding: 3rem 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.fallback-text {
  color: #9ca3af;
  font-size: 0.9rem;
  word-break: break-all;
  max-width: 600px;
}

.fallback-open-btn {
  display: inline-flex;
  align-items: center;
  background: #2563eb;
  color: #ffffff;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 500;
  text-decoration: none;
  transition: background 0.2s;
}

.fallback-open-btn:hover {
  background: #1d4ed8;
}
</style>
