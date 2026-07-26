import { defineNuxtPlugin, useCookie } from '#app'

export default defineNuxtPlugin(() => {
  if (process.client) {
    (window as any).downloadFileWithAuth = async (url: string, defaultName?: string) => {
      if (!url || url === '#' || url === '-') return
      const token = useCookie('auth_token')
      
      try {
        const blob: Blob = await $fetch(url, {
          headers: { Authorization: `Bearer ${token.value}` },
          responseType: 'blob'
        })
        
        let fileName = defaultName || ''
        if (!fileName) {
          if (url.includes('?name=')) {
            fileName = decodeURIComponent(url.split('?name=')[1].split('&')[0])
          } else {
            fileName = decodeURIComponent(url.split('/').pop()?.split('?')[0] || '') || 'download'
          }
        }
        
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
  }
})
