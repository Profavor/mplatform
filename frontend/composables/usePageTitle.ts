import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useMenuStore } from '~/stores/useMenuStore'
import { useI18n } from 'vue-i18n'

export function usePageTitle(fallbackKey?: string, fallbackText?: string) {
  const route = useRoute()
  const menuStore = useMenuStore()
  const { t } = useI18n()

  const pageTitle = computed(() => {
    // 1. Try to get dynamically from Pinia Menu Store
    const menuInfo = menuStore.getMenuByPath(route.path)
    if (menuInfo && menuInfo.title) {
      return menuInfo.title
    }

    // 2. Try to resolve via fallback i18n key
    if (fallbackKey) {
      const res = t(fallbackKey)
      if (res && res !== fallbackKey) {
        return res
      }
    }

    // 3. Static fallback text
    return fallbackText || ''
  })

  return {
    pageTitle
  }
}
