import { ref, computed } from 'vue'
import { useCookie } from '#app'
import { defineStore } from 'pinia'

export interface MenuItem {
  id: number | string
  name: string | Record<string, string>
  path: string
  icon?: string
  parentId?: number | string | null
  children?: MenuItem[]
  requiredRoles?: string[]
}

export const useMenuStore = defineStore('menu', () => {
  const menuTree = ref<MenuItem[]>([])
  const flatMenuList = ref<MenuItem[]>([])
  const isInitialized = ref(false)
  const isLoading = ref(false)
  let fetchPromise: Promise<MenuItem[]> | null = null

  const token = useCookie('auth_token')

  const getActiveLocale = (): string => {
    try {
      const localeCookie = useCookie('locale')
      if (localeCookie.value) return String(localeCookie.value)
    } catch (e) {}
    try {
      const { locale } = useI18n()
      if (locale?.value) return String(locale.value)
    } catch (e) {}
    return 'ko'
  }

  const parseMultilingualText = (nameObj: any): string => {
    if (!nameObj) return ''
    const currentLoc = getActiveLocale().toLowerCase()
    if (typeof nameObj === 'object') {
      return currentLoc.startsWith('en') ? (nameObj.en || nameObj.ko || '') : (nameObj.ko || nameObj.en || '')
    }
    if (typeof nameObj === 'string') {
      if (nameObj.startsWith('{')) {
        try {
          const parsed = JSON.parse(nameObj)
          return currentLoc.startsWith('en') ? (parsed.en || parsed.ko || nameObj) : (parsed.ko || parsed.en || nameObj)
        } catch {
          return nameObj
        }
      }
      return nameObj
    }
    return String(nameObj)
  }

  const flattenTree = (items: MenuItem[]): MenuItem[] => {
    const list: MenuItem[] = []
    const traverse = (nodes: MenuItem[]) => {
      for (const node of nodes) {
        list.push(node)
        if (node.children && node.children.length > 0) {
          traverse(node.children)
        }
      }
    }
    traverse(items || [])
    return list
  }

  const fetchMenuTree = async (forceRefresh = false): Promise<MenuItem[]> => {
    if (!forceRefresh && isInitialized.value && menuTree.value.length > 0) {
      return menuTree.value
    }
    if (fetchPromise && !forceRefresh) {
      return await fetchPromise
    }

    isLoading.value = true
    fetchPromise = (async () => {
      try {
        const { customFetch } = useCustomFetch()
        const res = await customFetch<MenuItem[]>('/api/menus/tree')
        menuTree.value = res || []
        flatMenuList.value = flattenTree(menuTree.value)
        isInitialized.value = true
      } catch (e) {
        console.error('Failed to fetch menu tree in useMenuStore:', e)
      } finally {
        isLoading.value = false
        fetchPromise = null
      }
      return menuTree.value
    })()

    return await fetchPromise
  }

  const getMenuByPath = (targetPath: string): { title: string; icon: string; raw: MenuItem | null } => {
    if (!targetPath) return { title: '', icon: '', raw: null }
    const normPath = targetPath.endsWith('/') && targetPath.length > 1 ? targetPath.slice(0, -1) : targetPath
    const found = flatMenuList.value.find(m => {
      if (!m.path) return false
      const mNorm = m.path.endsWith('/') && m.path.length > 1 ? m.path.slice(0, -1) : m.path
      return mNorm === normPath
    })

    if (found) {
      return {
        title: parseMultilingualText(found.name),
        icon: found.icon || 'label',
        raw: found
      }
    }
    return { title: '', icon: '', raw: null }
  }

  const refreshMenus = async (): Promise<MenuItem[]> => {
    return await fetchMenuTree(true)
  }

  return {
    menuTree,
    flatMenuList,
    isInitialized,
    isLoading,
    fetchMenuTree,
    refreshMenus,
    getMenuByPath,
    parseMultilingualText
  }
})
