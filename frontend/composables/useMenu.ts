import { storeToRefs } from 'pinia'
import { useMenuStore } from '~/stores/useMenuStore'

export const useMenu = () => {
  const store = useMenuStore()
  const { menuTree, flatMenuList, isInitialized, isLoading } = storeToRefs(store)
  const { customFetch, getAuthToken } = useCustomFetch()

  const fetchMenus = async (forceRefresh: boolean = false, includeInactive: boolean = false) => {
    return await store.fetchMenuTree(forceRefresh)
  }

  const fetchMenuTree = fetchMenus

  const logAccess = async (menuPath: string) => {
    try {
      if (!menuPath || menuPath === '/install' || menuPath === '/login') return

      const menuInfo = store.getMenuByPath(menuPath)
      const menuId = menuInfo?.raw?.id || null

      await customFetch('/api/menus/access', {
        method: 'POST',
        body: { menuId, menuPath },
        silent: true
      }).catch(() => null)
    } catch (error) {
      // Ignore menu access logging errors
    }
  }

  return {
    menus: menuTree,
    flatMenuList,
    isInitialized,
    isLoading,
    fetchMenus,
    fetchMenuTree,
    refreshMenus: store.refreshMenus,
    logAccess,
    getMenuByPath: store.getMenuByPath
  }
}
