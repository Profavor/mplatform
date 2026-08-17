export const useFileDownloader = () => {
  const { customFetch } = useCustomFetch()

  const downloadFileWithAuth = async (url: string, defaultName?: string) => {
    if (!url || url === '#' || url === '-') return

    // Helper to extract clean filename
    let fileName = defaultName || ''
    if (!fileName) {
      if (url.includes('?name=')) {
        fileName = decodeURIComponent(url.split('?name=')[1].split('&')[0])
      } else if (url.includes('?fileName=')) {
        fileName = decodeURIComponent(url.split('?fileName=')[1].split('&')[0])
      } else {
        const lastSegment = url.split('/').pop()?.split('?')[0] || ''
        fileName = decodeURIComponent(lastSegment) || 'download'
      }
    }

    const triggerDownload = (downloadUrl: string, shouldRevoke = false) => {
      const a = document.createElement('a')
      a.style.display = 'none'
      a.href = downloadUrl
      a.setAttribute('download', fileName)
      a.rel = 'noopener noreferrer'
      document.body.appendChild(a)
      
      const evt = new MouseEvent('click', {
        bubbles: true,
        cancelable: true,
        view: window
      })
      a.dispatchEvent(evt)

      setTimeout(() => {
        if (a.parentNode) {
          document.body.removeChild(a)
        }
        if (shouldRevoke) {
          try {
            URL.revokeObjectURL(downloadUrl)
          } catch (err) {}
        }
      }, 3000)
    }

    try {
      // If the URL is already a blob URL or base64 data URL, trigger download directly
      if (url.startsWith('blob:') || url.startsWith('data:')) {
        triggerDownload(url, false)
        return
      }

      // Fetch file blob with Authorization header
      const response: any = await customFetch(url, {
        responseType: 'blob'
      })

      const blob = response instanceof Blob ? response : new Blob([response])
      const blobUrl = URL.createObjectURL(blob)
      triggerDownload(blobUrl, true)
    } catch (e) {
      console.error('Failed to download file with auth:', e)
    }
  }

  return {
    downloadFileWithAuth
  }
}
