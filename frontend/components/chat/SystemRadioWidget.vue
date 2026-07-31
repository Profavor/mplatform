<template>
  <div class="system-radio-widget-wrapper">
    <!-- Hidden Youtube Player Container (항시 마운트 보장) -->
    <div id="youtube-audio-player-element" style="position: fixed; bottom: 0; right: 0; width: 1px; height: 1px; opacity: 0.001; pointer-events: none; overflow: hidden; z-index: -1;"></div>

    <!-- Floating Audio Widget Box -->
    <div v-if="track.isPlaying" class="system-radio-widget-container" style="position: fixed; bottom: 92px; left: 24px; z-index: 998;">

      <!-- Floating Audio Widget Box -->
      <div
        class="radio-glass-card"
        style="background: rgba(15, 23, 42, 0.85); backdrop-filter: blur(12px); border: 1px solid rgba(255, 255, 255, 0.15); color: white; padding: 10px 14px; border-radius: 20px; box-shadow: 0 12px 32px rgba(0, 0, 0, 0.4); display: flex; align-items: center; gap: 12px; min-width: 260px; transition: all 0.3s;"
      >
        <!-- Play / Equalizer Button -->
        <va-button
          v-if="needUserGesture"
          color="success"
          size="small"
          style="border-radius: 12px; font-weight: 800;"
          @click="resumeAudio"
        >
          ▶️ {{ $t('radioTitle') || '라디오 듣기' }}
        </va-button>

        <!-- Equalizer Visualizer Animation -->
        <div v-else class="eq-bars" style="display: flex; align-items: flex-end; gap: 3px; height: 18px; padding-bottom: 2px;">
          <span class="bar bar1" style="width: 3px; background: #3b82f6; border-radius: 2px;"></span>
          <span class="bar bar2" style="width: 3px; background: #60a5fa; border-radius: 2px;"></span>
          <span class="bar bar3" style="width: 3px; background: #93c5fd; border-radius: 2px;"></span>
          <span class="bar bar4" style="width: 3px; background: #3b82f6; border-radius: 2px;"></span>
        </div>

        <div style="flex: 1; overflow: hidden;">
          <div style="display: flex; align-items: center; gap: 6px;">
            <span style="font-size: 0.7rem; background: #2563eb; color: white; padding: 1px 6px; border-radius: 10px; font-weight: 800; letter-spacing: 0.5px;">LIVE RADIO</span>
            <span style="font-size: 0.72rem; color: #94a3b8;">DJ: {{ track.djName || 'Admin' }}</span>
          </div>
          <div style="font-weight: 700; font-size: 0.85rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 160px; margin-top: 2px; color: #f8fafc;">
            {{ track.title }}
          </div>
        </div>

        <!-- Volume & Mute Controls -->
        <div style="display: flex; align-items: center; gap: 4px;">
          <va-button
            preset="plain"
            size="small"
            :color="isMuted ? '#f87171' : '#ffffff'"
            @click="toggleMute"
          >
            <va-icon :name="isMuted ? 'volume_off' : (volume > 50 ? 'volume_up' : 'volume_down')" size="20px" />
          </va-button>

          <input
            type="range"
            min="0"
            max="100"
            v-model.number="volume"
            style="width: 50px; accent-color: #3b82f6; cursor: pointer;"
            @input="onVolumeChange"
            @change="onVolumeChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'

const track = ref<any>({ isPlaying: false, videoId: '', seekSeconds: 0 })
const isMuted = ref(false)
const volume = ref<number>(70)
const needUserGesture = ref(false)

let ytPlayer: any = null
let isYtApiReady = false

const resumeAudio = () => {
  needUserGesture.value = false
  if (ytPlayer && ytPlayer.playVideo) {
    try {
      applyPlayerVolume()
      ytPlayer.playVideo()
    } catch (e) {}
  }
}

const handleGlobalUserGesture = () => {
  if (needUserGesture.value) {
    resumeAudio()
  }
}

const applyPlayerVolume = () => {
  if (!ytPlayer) return
  try {
    const volNum = Number(volume.value)
    if (isMuted.value || volNum === 0) {
      if (typeof ytPlayer.mute === 'function') ytPlayer.mute()
      if (typeof ytPlayer.setVolume === 'function') ytPlayer.setVolume(0)
    } else {
      if (typeof ytPlayer.unMute === 'function') ytPlayer.unMute()
      if (typeof ytPlayer.setVolume === 'function') ytPlayer.setVolume(volNum)
    }
  } catch (e) {}
}

const loadYoutubeIframeApi = () => {
  if (window && (window as any).YT) {
    isYtApiReady = true
    return
  }
  const tag = document.createElement('script')
  tag.src = 'https://www.youtube.com/iframe_api'
  const firstScriptTag = document.getElementsByTagName('script')[0]
  if (firstScriptTag && firstScriptTag.parentNode) {
    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag)
  }

  ;(window as any).onYouTubeIframeAPIReady = () => {
    isYtApiReady = true
    if (track.value.isPlaying && track.value.videoId) {
      initPlayer(track.value.videoId, track.value.seekSeconds)
    }
  }
}

const initPlayer = (videoId: string, startSeconds: number = 0) => {
  if (!process.client) return
  if (!(window as any).YT || !(window as any).YT.Player) {
    setTimeout(() => initPlayer(videoId, startSeconds), 500)
    return
  }

  if (ytPlayer) {
    try {
      ytPlayer.loadVideoById({
        videoId,
        startSeconds: startSeconds || 0
      })
      applyPlayerVolume()
      ytPlayer.playVideo()
      return
    } catch (e) {}
  }

  ytPlayer = new (window as any).YT.Player('youtube-audio-player-element', {
    height: '1',
    width: '1',
    videoId: videoId,
    playerVars: {
      autoplay: 1,
      controls: 0,
      disablekb: 1,
      fs: 0,
      rel: 0,
      start: Math.floor(startSeconds || 0)
    },
    events: {
      onReady: (event: any) => {
        applyPlayerVolume()
        event.target.playVideo()
      }
    }
  })
}

const stopPlayer = () => {
  if (ytPlayer && ytPlayer.stopVideo) {
    try {
      ytPlayer.stopVideo()
    } catch (e) {}
  }
}

const toggleMute = () => {
  isMuted.value = !isMuted.value
  applyPlayerVolume()
}

const onVolumeChange = () => {
  if (isMuted.value && volume.value > 0) {
    isMuted.value = false
  }
  applyPlayerVolume()
}

watch(volume, () => {
  applyPlayerVolume()
})

watch(isMuted, () => {
  applyPlayerVolume()
})

const fetchState = async () => {
  try {
    const res: any = await $fetch('/api/music/state')
    if (res && res.isPlaying && res.videoId) {
      track.value = res
      initPlayer(res.videoId, res.seekSeconds)
    } else {
      track.value.isPlaying = false
      stopPlayer()
    }
  } catch (e) {}
}

const handleIncomingEvent = (event: any) => {
  const payload = event?.detail
  if (!payload) return

  if (payload.eventType === 'MUSIC_PLAY') {
    track.value = {
      isPlaying: true,
      videoId: payload.videoId,
      title: payload.title,
      seekSeconds: payload.seekSeconds || 0,
      djName: payload.djName
    }
    initPlayer(payload.videoId, payload.seekSeconds)
  } else if (payload.eventType === 'MUSIC_STOP') {
    track.value.isPlaying = false
    stopPlayer()
  }
}

onMounted(() => {
  if (process.client) {
    loadYoutubeIframeApi()
    window.addEventListener('chat-message-received', handleIncomingEvent)
    window.addEventListener('click', handleGlobalUserGesture)
    fetchState()
  }
})

onUnmounted(() => {
  if (process.client) {
    window.removeEventListener('chat-message-received', handleIncomingEvent)
    window.removeEventListener('click', handleGlobalUserGesture)
    stopPlayer()
  }
})
</script>

<style scoped>
@keyframes barBounce1 { 0%, 100% { height: 4px; } 50% { height: 16px; } }
@keyframes barBounce2 { 0%, 100% { height: 14px; } 50% { height: 6px; } }
@keyframes barBounce3 { 0%, 100% { height: 8px; } 50% { height: 18px; } }
@keyframes barBounce4 { 0%, 100% { height: 16px; } 50% { height: 8px; } }

.bar1 { animation: barBounce1 0.8s infinite ease-in-out; }
.bar2 { animation: barBounce2 0.7s infinite ease-in-out; }
.bar3 { animation: barBounce3 0.9s infinite ease-in-out; }
.bar4 { animation: barBounce4 0.65s infinite ease-in-out; }
</style>
