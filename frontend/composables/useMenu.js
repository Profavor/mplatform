import { useMenuStore } from '~/stores/useMenuStore'

export const useMenu = () => {
  const store = useMenuStore()

  const fetchMenus = async (forceRefresh = false, includeInactive = false) => {
    return await store.fetchMenuTree(forceRefresh)
  }

  const fetchMenuTree = fetchMenus

  const logAccess = async (menuPath) => {
    try {
      if (!menuPath || menuPath === '/install' || menuPath === '/login') return

      const token = useCookie('auth_token')
      if (!token.value) return

      const menuInfo = store.getMenuByPath(menuPath)
      const menuId = menuInfo?.raw?.id || null

      await $fetch('/api/menus/access', {
        method: 'POST',
        headers: { Authorization: `Bearer ${token.value}` },
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
