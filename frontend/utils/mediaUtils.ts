export type MediaType = 'IMAGE' | 'VIDEO' | 'YOUTUBE' | 'VIMEO' | 'AUDIO' | 'LINK' | 'EMPTY'

export interface MediaInfo {
  type: MediaType
  url: string
  embedUrl?: string
  thumbnailUrl?: string
  videoId?: string
  title?: string
}

const IMAGE_REGEX = /(?:\.(?:jpe?g|png|gif|webp|svg|avif|bmp|ico)|[\?&]format=(?:jpe?g|png|gif|webp|svg|avif))(?:\?.*)?$/i
const VIDEO_REGEX = /\.(?:mp4|webm|ogg|ogv|mov|m4v|mkv)(?:\?.*)?$/i
const AUDIO_REGEX = /\.(?:mp3|wav|ogg|oga|aac|m4a|flac)(?:\?.*)?$/i
const YOUTUBE_REGEX = /(?:youtube\.com\/(?:[^\/]+\/.+\/|(?:v|e(?:mbed)?|shorts)\/|.*[?&]v=)|youtu\.be\/)([^"&?\/\s]{11})/i
const VIMEO_REGEX = /(?:vimeo\.com\/(?:video\/)?)([\d]+)/i

/**
 * 주어진 URL 문자열을 분석하여 미디어 타입 및 재생/썸네일 정보를 반환합니다.
 */
export function detectMediaType(rawUrl: string | null | undefined): MediaInfo {
  if (!rawUrl || typeof rawUrl !== 'string' || !rawUrl.trim()) {
    return {
      type: 'EMPTY',
      url: ''
    }
  }

  const url = rawUrl.trim()

  // 1. Data URI
  if (url.startsWith('data:image/')) {
    return {
      type: 'IMAGE',
      url,
      thumbnailUrl: url
    }
  }
  if (url.startsWith('data:video/')) {
    return {
      type: 'VIDEO',
      url,
      embedUrl: url
    }
  }
  if (url.startsWith('data:audio/')) {
    return {
      type: 'AUDIO',
      url
    }
  }

  // 2. YouTube
  const ytMatch = url.match(YOUTUBE_REGEX)
  if (ytMatch && ytMatch[1]) {
    const videoId = ytMatch[1]
    return {
      type: 'YOUTUBE',
      url,
      videoId,
      embedUrl: `https://www.youtube.com/embed/${videoId}?rel=0&enablejsapi=1`,
      thumbnailUrl: `https://img.youtube.com/vi/${videoId}/hqdefault.jpg`
    }
  }

  // 3. Vimeo
  const vimeoMatch = url.match(VIMEO_REGEX)
  if (vimeoMatch && vimeoMatch[1]) {
    const videoId = vimeoMatch[1]
    return {
      type: 'VIMEO',
      url,
      videoId,
      embedUrl: `https://player.vimeo.com/video/${videoId}`
    }
  }

  // 4. Direct Image
  if (IMAGE_REGEX.test(url)) {
    return {
      type: 'IMAGE',
      url,
      thumbnailUrl: url
    }
  }

  // 5. Direct Video
  if (VIDEO_REGEX.test(url)) {
    return {
      type: 'VIDEO',
      url,
      embedUrl: url
    }
  }

  // 6. Audio
  if (AUDIO_REGEX.test(url)) {
    return {
      type: 'AUDIO',
      url
    }
  }

  // 7. Fallback Link
  return {
    type: 'LINK',
    url
  }
}

/**
 * 문자열, 배열, JSON 등 다양한 형식의 미디어 입력값을 정규화된 MediaInfo 배열로 변환합니다.
 */
export function parseMediaList(val: any): MediaInfo[] {
  if (!val) return []

  let list: any[] = []

  if (Array.isArray(val)) {
    list = val
  } else if (typeof val === 'string') {
    const trimmed = val.trim()
    if (!trimmed || trimmed === '-' || trimmed === '[]' || trimmed === '{}' || trimmed === 'null') {
      return []
    }
    if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
      try {
        const parsed = JSON.parse(trimmed)
        if (Array.isArray(parsed)) list = parsed
      } catch (e) {
        list = trimmed.split(',').map(s => s.trim()).filter(Boolean)
      }
    } else {
      list = trimmed.split(',').map(s => s.trim()).filter(Boolean)
    }
  } else if (typeof val === 'object') {
    list = [val]
  }

  return list
    .map(item => {
      const rawUrl = typeof item === 'object' && item !== null ? (item.url || item.src || item.link || '') : String(item)
      const media = detectMediaType(rawUrl)
      if (typeof item === 'object' && item !== null && item.title) {
        media.title = item.title
      }
      return media
    })
    .filter(m => m.type !== 'EMPTY')
}

/**
 * 해당 URL이 이미지, 비디오, 오디오 등의 미디어 링크인지 여부를 반환합니다.
 */
export function isMediaLink(url: string | null | undefined): boolean {
  if (!url) return false
  const info = detectMediaType(url)
  return ['IMAGE', 'VIDEO', 'YOUTUBE', 'VIMEO', 'AUDIO'].includes(info.type)
}
