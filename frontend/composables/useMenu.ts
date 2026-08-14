import { useMenuStore } from '~/stores/useMenuStore'
import { useCookie } from '#app'

export const useMenu = () => {
  const store = useMenuStore()
  const { customFetch } = useCustomFetch()

  const fetchMenus = async (forceRefresh: boolean = false, includeInactive: boolean = false) => {
    return await store.fetchMenuTree(forceRefresh)
  }

  const fetchMenuTree = fetchMenus

  const logAccess = async (menuPath: string) => {
    try {
      if (!menuPath || menuPath === '/install' || menuPath === '/login') return

      const token = useCookie('auth_token')
      if (!token.value) return

      const menuInfo = store.getMenuByPath(menuPath)
      const menuId = menuInfo?.raw?.id || null

      await customFetch('/api/menus/access', {
        method: 'POST',
        body: { menuId, menuPath }
      }).catch(() => null)
    } catch (error) {
      // Ignore menu access logging errors
    }
  }

  return {
    menus: store.menuTree,
    fetchMenus,
    fetchMenuTree,
    refreshMenus: store.refreshMenus,
    logAccess,
    getMenuByPath: store.getMenuByPath
  }
}
