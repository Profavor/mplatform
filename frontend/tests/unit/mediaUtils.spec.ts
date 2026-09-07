import { describe, it, expect } from 'vitest'
import { detectMediaType, parseMediaList, isMediaLink } from '../../utils/mediaUtils'

describe('mediaUtils (TDD)', () => {
  describe('detectMediaType', () => {
    it('이미지 URL을 올바르게 감지한다', () => {
      const urls = [
        'https://example.com/photo.jpg',
        'https://example.com/image.png?size=large',
        'https://example.com/graphic.SVG',
        'https://example.com/banner.webp',
        'https://example.com/animation.gif',
        'https://images.unsplash.com/photo-12345?w=500&format=jpg',
        'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA'
      ]

      for (const url of urls) {
        const result = detectMediaType(url)
        expect(result.type).toBe('IMAGE')
        expect(result.url).toBe(url)
        expect(result.thumbnailUrl).toBe(url)
      }
    })

    it('직접 동영상 파일 URL을 올바르게 감지한다', () => {
      const urls = [
        'https://example.com/video.mp4',
        'https://example.com/clip.webm?auth=abc',
        'https://example.com/movie.mov',
        'https://example.com/sample.ogg',
        'https://example.com/stream.m4v',
        'data:video/mp4;base64,AAAAHGZ0eXBtcDQy'
      ]

      for (const url of urls) {
        const result = detectMediaType(url)
        expect(result.type).toBe('VIDEO')
        expect(result.url).toBe(url)
        expect(result.embedUrl).toBe(url)
      }
    })

    it('유튜브(YouTube) 일반, 단축, 쇼츠, 임베드 URL을 모두 파싱하여 embedUrl 및 썸네일을 생성한다', () => {
      const testCases = [
        {
          input: 'https://www.youtube.com/watch?v=dQw4w9WgXcQ',
          expectedId: 'dQw4w9WgXcQ'
        },
        {
          input: 'https://youtu.be/dQw4w9WgXcQ?t=10',
          expectedId: 'dQw4w9WgXcQ'
        },
        {
          input: 'https://www.youtube.com/shorts/dQw4w9WgXcQ',
          expectedId: 'dQw4w9WgXcQ'
        },
        {
          input: 'https://www.youtube.com/embed/dQw4w9WgXcQ',
          expectedId: 'dQw4w9WgXcQ'
        }
      ]

      for (const tc of testCases) {
        const result = detectMediaType(tc.input)
        expect(result.type).toBe('YOUTUBE')
        expect(result.videoId).toBe(tc.expectedId)
        expect(result.embedUrl).toContain(`https://www.youtube.com/embed/${tc.expectedId}`)
        expect(result.thumbnailUrl).toBe(`https://img.youtube.com/vi/${tc.expectedId}/hqdefault.jpg`)
      }
    })

    it('비메오(Vimeo) URL을 파싱하여 embedUrl을 생성한다', () => {
      const urls = [
        'https://vimeo.com/123456789',
        'https://player.vimeo.com/video/123456789?autoplay=1'
      ]

      for (const url of urls) {
        const result = detectMediaType(url)
        expect(result.type).toBe('VIMEO')
        expect(result.videoId).toBe('123456789')
        expect(result.embedUrl).toBe('https://player.vimeo.com/video/123456789')
      }
    })

    it('오디오 파일 URL을 올바르게 감지한다', () => {
      const urls = [
        'https://example.com/audio.mp3',
        'https://example.com/sound.wav?token=xyz',
        'https://example.com/music.m4a'
      ]

      for (const url of urls) {
        const result = detectMediaType(url)
        expect(result.type).toBe('AUDIO')
        expect(result.url).toBe(url)
      }
    })

    it('미디어 확장자가 없는 일반 웹 링크를 LINK 타입으로 분류한다', () => {
      const result = detectMediaType('https://example.com/docs/manual')
      expect(result.type).toBe('LINK')
      expect(result.url).toBe('https://example.com/docs/manual')
    })

    it('null, undefined, 빈 문자열인 경우 EMPTY 타입을 반환한다', () => {
      expect(detectMediaType(null).type).toBe('EMPTY')
      expect(detectMediaType(undefined).type).toBe('EMPTY')
      expect(detectMediaType('').type).toBe('EMPTY')
      expect(detectMediaType('   ').type).toBe('EMPTY')
    })
  })

  describe('parseMediaList', () => {
    it('단일 URL 문자열을 요소 1개의 미디어 정보 배열로 반환한다', () => {
      const list = parseMediaList('https://example.com/test.mp4')
      expect(list.length).toBe(1)
      expect(list[0].type).toBe('VIDEO')
    })

    it('콤마로 구분된 여러 URL을 각각 파싱한다', () => {
      const list = parseMediaList('https://example.com/a.jpg, https://youtu.be/dQw4w9WgXcQ')
      expect(list.length).toBe(2)
      expect(list[0].type).toBe('IMAGE')
      expect(list[1].type).toBe('YOUTUBE')
    })

    it('JSON 문자열 배열이나 객체 배열을 정상 파싱한다', () => {
      const jsonArr = '["https://example.com/photo.png", "https://example.com/video.mp4"]'
      const list = parseMediaList(jsonArr)
      expect(list.length).toBe(2)
      expect(list[0].type).toBe('IMAGE')
      expect(list[1].type).toBe('VIDEO')
    })
  })

  describe('isMediaLink', () => {
    it('이미지, 비디오, 유튜브, 비메오 링크에 대해 true를 반환한다', () => {
      expect(isMediaLink('https://example.com/photo.jpg')).toBe(true)
      expect(isMediaLink('https://example.com/video.mp4')).toBe(true)
      expect(isMediaLink('https://youtu.be/dQw4w9WgXcQ')).toBe(true)
      expect(isMediaLink('https://vimeo.com/123456789')).toBe(true)
      expect(isMediaLink('')).toBe(false)
      expect(isMediaLink(null)).toBe(false)
    })
  })
})
