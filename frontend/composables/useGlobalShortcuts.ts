import { onMounted, onUnmounted } from 'vue'

export interface ShortcutOptions {
  onSearch?: () => void
  onSave?: () => void
  onEscape?: () => void
  onRefresh?: () => void
}

export function useGlobalShortcuts(options: ShortcutOptions = {}) {
  const handleKeyDown = (event: KeyboardEvent) => {
    // 1. Ctrl + K / Cmd + K -> Search
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
      if (options.onSearch) {
        event.preventDefault()
        options.onSearch()
      }
    }

    // 2. Ctrl + S / Cmd + S -> Save
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 's') {
      if (options.onSave) {
        event.preventDefault()
        options.onSave()
      }
    }

    // 3. Escape -> Close modal/drawer
    if (event.key === 'Escape') {
      if (options.onEscape) {
        options.onEscape()
      }
    }

    // 4. Alt + R -> Refresh grid
    if (event.altKey && event.key.toLowerCase() === 'r') {
      if (options.onRefresh) {
        event.preventDefault()
        options.onRefresh()
      }
    }
  }

  const enable = () => {
    if (typeof window !== 'undefined') {
      window.addEventListener('keydown', handleKeyDown)
    }
  }

  const disable = () => {
    if (typeof window !== 'undefined') {
      window.removeEventListener('keydown', handleKeyDown)
    }
  }

  try {
    onMounted(() => enable())
    onUnmounted(() => disable())
  } catch {
    // In unit test or non-lifecycle environments, manual enable/disable is available
  }

  return {
    enable,
    disable
  }
}
