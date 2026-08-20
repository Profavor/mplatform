import { ref, reactive } from 'vue'
import { useCustomFetch } from '~/composables/useCustomFetch'

// Global in-memory cache for authenticated blob URLs to avoid redundant requests
const blobUrlCache = new Map<string, string>()
const pendingPromises = new Map<string, Promise<string>>()

// Reverse mapping: blobUrl -> originalUrl (for restoring HTML before saving)
const reverseUrlMap = new Map<string, string>()

const TRANSPARENT_PIXEL = 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="1" height="1"/>'

export const useAuthenticatedImage = () => {
  const { customFetch } = useCustomFetch()

  /**
   * Fetches an image URL with Authorization header and returns an in-memory blob URL.
   */
  const getAuthenticatedImageUrl = async (rawUrl: string | null | undefined): Promise<string> => {
    if (!rawUrl) return TRANSPARENT_PIXEL
    
    // Clean string in case of accidental JSON array brackets or quotes
    const url = String(rawUrl).trim().replace(/^[\["\s']+|[\]"\s']+$/g, '')
    if (!url || url === '-' || url === '[]' || url === '{}') return TRANSPARENT_PIXEL

    // If it's already a blob URL or base64 or external absolute URL, return as is
    if (url.startsWith('blob:') || url.startsWith('data:')) {
      return url
    }

    // Only process /api/files/download URLs
    if (!url.includes('/api/files/download')) {
      return url
    }

    // Check memory cache
    if (blobUrlCache.has(url)) {
      return blobUrlCache.get(url)!
    }

    // Check if there is an in-flight promise for this URL
    if (pendingPromises.has(url)) {
      return pendingPromises.get(url)!
    }

    const fetchPromise = (async () => {
      try {
        const response: any = await customFetch(url, {
          responseType: 'blob',
          silent: true
        })

        if (response instanceof Blob) {
          const blobUrl = URL.createObjectURL(response)
          blobUrlCache.set(url, blobUrl)
          reverseUrlMap.set(blobUrl, url)
          return blobUrl
        }
        return TRANSPARENT_PIXEL
      } catch (error) {
        // Silent catch for missing local files (404)
        return TRANSPARENT_PIXEL
      } finally {
        pendingPromises.delete(url)
      }
    })()

    pendingPromises.set(url, fetchPromise)
    return fetchPromise
  }

  /**
   * Replaces all /api/files/download/ image sources in an HTML string with authenticated blob URLs for display in editor.
   */
  const transformHtmlImagesToBlob = async (html: string | null | undefined): Promise<string> => {
    if (!html || !html.includes('/api/files/download')) {
      return html || ''
    }

    const imgRegex = /<img\b([^>]*?)src=(["'])(.*?)\2([^>]*?)>/gi
    const matches: { fullTag: string; prefix: string; src: string; suffix: string }[] = []
    let match

    while ((match = imgRegex.exec(html)) !== null) {
      const src = match[3]
      if (src && src.includes('/api/files/download')) {
        matches.push({ fullTag: match[0], prefix: match[1], src, suffix: match[4] })
      }
    }

    if (matches.length === 0) return html

    let transformedHtml = html
    for (const { fullTag, prefix, src, suffix } of matches) {
      const cleanSrc = src.replace(/&amp;/g, '&')
      const blobUrl = await getAuthenticatedImageUrl(cleanSrc)
      if (blobUrl && blobUrl !== TRANSPARENT_PIXEL) {
        const newTag = `<img ${prefix}src="${blobUrl}"${suffix}>`
        transformedHtml = transformedHtml.replace(fullTag, newTag)
      } else {
        const newTag = `<img ${prefix}src="${cleanSrc}" onerror="this.onerror=null; this.style.display='none';"${suffix}>`
        transformedHtml = transformedHtml.replace(fullTag, newTag)
      }
    }

    return transformedHtml
  }

  /**
   * Replaces all blob URLs in an HTML string with their original /api/files/download URLs before saving to DB.
   */
  const restoreBlobImagesToOriginal = (html: string | null | undefined): string => {
    if (!html || !html.includes('blob:')) {
      return html || ''
    }

    let restoredHtml = html
    for (const [blobUrl, originalUrl] of reverseUrlMap.entries()) {
      if (restoredHtml.includes(blobUrl)) {
        restoredHtml = restoredHtml.split(blobUrl).join(originalUrl)
      }
    }

    return restoredHtml
  }

  /**
   * Returns cached blob URL synchronously if available
   */
  const getCachedBlobUrl = (rawUrl: string | null | undefined): string | null => {
    if (!rawUrl) return null
    const url = String(rawUrl).trim().replace(/^[\["\s']+|[\]"\s']+$/g, '')
    if (blobUrlCache.has(url)) {
      return blobUrlCache.get(url)!
    }
    return null
  }

  return {
    getAuthenticatedImageUrl,
    getCachedBlobUrl,
    transformHtmlImagesToBlob,
    restoreBlobImagesToOriginal
  }
}
