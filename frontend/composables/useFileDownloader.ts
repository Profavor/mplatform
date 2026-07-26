import { useCookie } from '#app'

export const useFileDownloader = () => {
  const downloadFileWithAuth = async (url: string, defaultName?: string) => {
    if (!url || url === '#' || url === '-') return
    const token = useCookie('auth_token')
    
    try {
      // 1. Fetch file blob with Authorization header
      const blob: Blob = await $fetch(url, {
        headers: { Authorization: `Bearer ${token.value}` },
        responseType: 'blob'
      })
      
      // 2. Parse filename
      let fileName = defaultName || ''
      if (!fileName) {
        if (url.includes('?name=')) {
          fileName = decodeURIComponent(url.split('?name=')[1].split('&')[0])
        } else {
          fileName = decodeURIComponent(url.split('/').pop()?.split('?')[0] || '') || 'download'
        }
      }
      
      // 3. Trigger browser download using blob URL
      const blobUrl = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = blobUrl
      a.download = fileName
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      URL.revokeObjectURL(blobUrl)
    } catch (e) {
      console.error('Failed to download file with auth:', e)
    }
  }

  return {
    downloadFileWithAuth
  }
}
