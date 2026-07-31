<template>
  <va-modal
    v-model="modelValue"
    :title="`🎵 ${$t('messenger.radioDjTitle')}`"
    hide-default-actions
    size="large"
  >
    <div style="padding: 4px 0;">
      <!-- Tab Navigation -->
      <div style="display: flex; gap: 8px; margin-bottom: 16px; border-bottom: 1px solid var(--va-background-border); padding-bottom: 8px;">
        <va-button
          :preset="activeTab === 'broadcast' ? 'primary' : 'secondary'"
          size="small"
          @click="activeTab = 'broadcast'"
        >
          {{ $t('messenger.radioBroadcastTab') }}
        </va-button>
        <va-button
          :preset="activeTab === 'config' ? 'primary' : 'secondary'"
          size="small"
          @click="activeTab = 'config'"
        >
          {{ $t('messenger.radioConnectTab') }}
        </va-button>
      </div>

      <!-- Tab 1: Broadcast Control -->
      <div v-if="activeTab === 'broadcast'" style="display: flex; flex-direction: column; gap: 14px;">
        <div v-if="currentTrack.isPlaying" style="background: rgba(37,99,235,0.1); border: 1px solid var(--va-primary); border-radius: 10px; padding: 12px; display: flex; align-items: center; justify-content: space-between;">
          <div style="display: flex; align-items: center; gap: 10px;">
            <span style="font-size: 1.5rem; animation: pulse 1.5s infinite;">📻</span>
            <div>
              <div style="font-size: 0.75rem; color: var(--va-primary); font-weight: 800;">● {{ $t('messenger.radioPlayingNow') }}</div>
              <div style="font-weight: 700; font-size: 0.95rem;">{{ currentTrack.title }}</div>
              <div style="font-size: 0.75rem; color: var(--va-text-secondary);">DJ: {{ currentTrack.djName || 'Admin' }}</div>
            </div>
          </div>
          <va-button color="danger" size="small" @click="stopBroadcast">
            {{ $t('messenger.radioStopBroadcast') }}
          </va-button>
        </div>

        <va-input
          v-model="youtubeUrlInput"
          :label="$t('messenger.radioTrackTitle')"
          :placeholder="$t('messenger.radioUrlPlaceholder')"
          style="width: 100%;"
        />

        <va-input
          v-model="trackTitleInput"
          :label="$t('messenger.radioCustomTitleLabel')"
          :placeholder="$t('messenger.radioCustomTitlePlaceholder')"
          style="width: 100%;"
        />

        <!-- Preset Fast Picker if user config exists -->
        <div v-if="savedConfig && savedConfig.playlistId" style="background: var(--va-background-element); border-radius: 8px; padding: 10px;">
          <div style="font-size: 0.8rem; font-weight: 700; margin-bottom: 6px;">🔗 {{ savedConfig.playlistTitle || $t('messenger.radioMyPlaylistDefault') }}</div>
          <div style="font-size: 0.75rem; color: var(--va-text-secondary); margin-bottom: 6px;">Playlist ID: {{ savedConfig.playlistId }}</div>
          <va-button size="small" preset="secondary" @click="usePlaylistPreset">
            {{ $t('messenger.radioPlayThisPlaylist') }}
          </va-button>
        </div>

        <div style="display: flex; justify-content: flex-end; gap: 8px; margin-top: 8px;">
          <va-button color="success" icon="campaign" @click="startBroadcast">
            {{ $t('messenger.radioStartBroadcast') }}
          </va-button>
        </div>
      </div>

      <!-- Tab 2: Sync YouTube Account & Playlist -->
      <div v-else-if="activeTab === 'config'" style="display: flex; flex-direction: column; gap: 12px;">
        <div style="font-size: 0.85rem; color: var(--va-text-secondary); line-height: 1.4;">
          {{ $t('messenger.radioConfigDesc') }}
        </div>

        <va-input
          v-model="configForm.channelUrl"
          :label="$t('messenger.radioChannelUrl')"
          :placeholder="$t('messenger.radioChannelUrlPlaceholder')"
        />

        <va-input
          v-model="configForm.playlistUrl"
          :label="$t('messenger.radioPlaylistUrl')"
          :placeholder="$t('messenger.radioPlaylistUrlPlaceholder')"
        />

        <va-input
          v-model="configForm.playlistTitle"
          :label="$t('messenger.radioPlaylistTitle')"
          :placeholder="$t('messenger.radioPlaylistTitlePlaceholder')"
        />

        <div style="display: flex; justify-content: flex-end; margin-top: 8px;">
          <va-button color="primary" @click="saveConfig">
            {{ $t('messenger.radioSaveConfig') }}
          </va-button>
        </div>
      </div>
    </div>

    <template #footer>
      <va-button preset="secondary" @click="modelValue = false">{{ $t('messenger.closeBtn') }}</va-button>
    </template>
  </va-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits(['update:modelValue', 'broadcast-updated'])

const modelValue = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const activeTab = ref<'broadcast' | 'config'>('broadcast')

const youtubeUrlInput = ref('')
const trackTitleInput = ref('')

const configForm = ref({
  channelUrl: '',
  playlistUrl: '',
  playlistTitle: '',
  apiKey: ''
})

const savedConfig = ref<any>(null)
const currentTrack = ref<any>({ isPlaying: false })
const tokenCookie = useCookie('auth_token')

const extractVideoId = (urlOrId: string) => {
  if (!urlOrId) return ''
  const trimmed = urlOrId.trim()

  if (/^[a-zA-Z0-9_-]{11}$/.test(trimmed)) return trimmed

  try {
    if (trimmed.includes('v=')) {
      const vParam = trimmed.split('v=')[1].split('&')[0].split('#')[0]
      if (vParam && vParam.length >= 11) return vParam.substring(0, 11)
    }
    if (trimmed.includes('youtu.be/')) {
      const pathParam = trimmed.split('youtu.be/')[1].split('?')[0].split('/')[0]
      if (pathParam && pathParam.length >= 11) return pathParam.substring(0, 11)
    }
    if (trimmed.includes('/shorts/') || trimmed.includes('/embed/')) {
      const parts = trimmed.split(/\/shorts\/|\/embed\//)
      if (parts[1]) {
        const id = parts[1].split('?')[0].split('/')[0]
        if (id && id.length >= 11) return id.substring(0, 11)
      }
    }
  } catch (e) {}

  const regExp = /(?:youtube\.com\/(?:[^\/]+\/.+\/|(?:v|e(?:mbed)?)\/|.*[?&]v=)|youtu\.be\/)([^"&?\/\s]{11})/
  const match = trimmed.match(regExp)
  if (match && match[1]) {
    return match[1]
  }

  return trimmed
}

const extractPlaylistId = (urlOrId: string) => {
  if (!urlOrId) return ''
  const trimmed = urlOrId.trim()
  if (trimmed.includes('list=')) {
    const parts = trimmed.split('list=')
    return parts[1].split('&')[0]
  }
  return trimmed
}

const fetchCurrentState = async () => {
  try {
    const res: any = await $fetch('/api/music/state')
    if (res) {
      currentTrack.value = res
    }
  } catch (e) {}
}

const fetchSavedConfig = async () => {
  if (!tokenCookie.value) return
  try {
    const res: any = await $fetch('/api/music/admin/youtube-config', {
      headers: { Authorization: `Bearer ${tokenCookie.value}` }
    })
    if (res) {
      savedConfig.value = res
      configForm.value.channelUrl = res.youtubeChannelUrl || ''
      configForm.value.playlistUrl = res.playlistId || ''
      configForm.value.playlistTitle = res.playlistTitle || ''
    }
  } catch (e) {}
}

const saveConfig = async () => {
  if (!tokenCookie.value) return
  try {
    const pId = extractPlaylistId(configForm.value.playlistUrl)
    const res = await $fetch('/api/music/admin/youtube-config', {
      method: 'POST',
      headers: { Authorization: `Bearer ${tokenCookie.value}` },
      body: {
        youtubeChannelUrl: configForm.value.channelUrl,
        playlistId: pId,
        playlistTitle: configForm.value.playlistTitle
      }
    })
    savedConfig.value = res
    activeTab.value = 'broadcast'
  } catch (e) {}
}

const usePlaylistPreset = () => {
  if (savedConfig.value && savedConfig.value.playlistId) {
    youtubeUrlInput.value = `https://www.youtube.com/playlist?list=${savedConfig.value.playlistId}`
    trackTitleInput.value = savedConfig.value.playlistTitle || '유튜브 뮤직 재생목록'
  }
}

const startBroadcast = async () => {
  const rawInput = youtubeUrlInput.value.trim()
  if (!rawInput || !tokenCookie.value) return
  
  const videoId = extractVideoId(rawInput)
  try {
    const res: any = await $fetch('/api/music/admin/play', {
      method: 'POST',
      headers: { Authorization: `Bearer ${tokenCookie.value}` },
      body: {
        videoId,
        title: trackTitleInput.value.trim() || '라이브 음악 방송',
        seekSeconds: 0.0,
        playlistTitle: savedConfig.value?.playlistTitle
      }
    })
    currentTrack.value = res
    emit('broadcast-updated', res)

    // 관리자 본인 브라우저의 SystemRadioWidget에도 방송 시작 이벤트 즉시 전달
    if (process.client) {
      window.dispatchEvent(new CustomEvent('chat-message-received', {
        detail: {
          eventType: 'MUSIC_PLAY',
          videoId: res.videoId,
          title: res.title,
          seekSeconds: 0,
          djName: res.djName
        }
      }))
    }

    youtubeUrlInput.value = ''
    trackTitleInput.value = ''
  } catch (e) {}
}

const stopBroadcast = async () => {
  if (!tokenCookie.value) return
  try {
    const res: any = await $fetch('/api/music/admin/stop', {
      method: 'POST',
      headers: { Authorization: `Bearer ${tokenCookie.value}` }
    })
    currentTrack.value = res
    emit('broadcast-updated', res)

    // 관리자 본인 브라우저의 SystemRadioWidget에도 방송 중단 이벤트 즉시 전달
    if (process.client) {
      window.dispatchEvent(new CustomEvent('chat-message-received', {
        detail: { eventType: 'MUSIC_STOP' }
      }))
    }
  } catch (e) {}
}

watch(modelValue, (newVal) => {
  if (newVal) {
    fetchCurrentState()
    fetchSavedConfig()
  }
})

onMounted(() => {
  fetchCurrentState()
  fetchSavedConfig()
})
</script>
