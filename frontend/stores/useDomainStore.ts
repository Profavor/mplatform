import { ref, computed } from 'vue'
import { useCookie } from '#app'
import { defineStore } from 'pinia'

export interface DomainInfo {
  id: string
  name: any
  description?: any
  domainName?: string
  code?: string
  [key: string]: any
}

export function parseDomainName(val: any): string {
  if (!val) return ''
  if (typeof val === 'object' && val !== null) {
    return val.ko || val.en || Object.values(val)[0] || ''
  }
  if (typeof val === 'string') {
    const trimmed = val.trim()
    if (trimmed.startsWith('{') || trimmed.startsWith('[')) {
      try {
        const parsed = JSON.parse(trimmed)
        if (typeof parsed === 'object' && parsed !== null) {
          return parsed.ko || parsed.en || Object.values(parsed)[0] || val
        }
      } catch {}
    }
    return val
  }
  return String(val)
}

export const useDomainStore = defineStore('domain', () => {
  // Global reactive Store state (singleton across client session)
  const domainsList = ref<DomainInfo[]>([])
  const domainMap = ref<Record<string, DomainInfo>>({})
  const isInitialized = ref(false)
  const isLoading = ref(false)
  let domainsPromise: Promise<DomainInfo[]> | null = null

  const token = useCookie('auth_token')

  const fetchDomains = async (forceRefresh = false): Promise<DomainInfo[]> => {
    if (!forceRefresh && isInitialized.value && domainsList.value.length > 0) {
      return domainsList.value
    }

    if (domainsPromise && !forceRefresh) {
      return await domainsPromise
    }

    isLoading.value = true
    domainsPromise = (async () => {
      try {
        const headers = token.value ? { Authorization: `Bearer ${token.value}` } : {}
        const res = await $fetch<any>('/api/domains', { headers })
        const list = Array.isArray(res) ? res : (res?.content || [])

        if (Array.isArray(list)) {
          domainsList.value = list
          const newMap: Record<string, DomainInfo> = {}
          list.forEach(d => {
            if (d && d.id) {
              newMap[d.id] = d
            }
          })
          domainMap.value = newMap
          isInitialized.value = true
          return list
        }
      } catch (e) {
        console.error('Failed to fetch domains:', e)
      } finally {
        isLoading.value = false
        domainsPromise = null
      }
      return domainsList.value
    })()

    return await domainsPromise
  }

  const domainOptions = computed(() => {
    return domainsList.value.map(d => {
      const nameStr = parseDomainName(d.name || d.domainName || d.code) || 'Unknown Domain'
      return {
        label: nameStr,
        text: nameStr,
        value: d.id,
        domain: d
      }
    })
  })

  const getDomainById = (id?: string | null): DomainInfo | null => {
    if (!id) return null
    return domainMap.value[id] || domainsList.value.find(d => d.id === id) || null
  }

  const getDomainName = (id?: string | null): string => {
    if (!id) return ''
    const dom = getDomainById(id)
    if (!dom) return id
    return parseDomainName(dom.name || dom.domainName || dom.code) || id
  }

  const addDomainToStore = (newDomain: DomainInfo) => {
    if (!newDomain || !newDomain.id) return
    const idx = domainsList.value.findIndex(d => d.id === newDomain.id)
    if (idx >= 0) {
      domainsList.value[idx] = { ...domainsList.value[idx], ...newDomain }
    } else {
      domainsList.value.push(newDomain)
    }
    domainMap.value[newDomain.id] = newDomain
  }

  const updateDomainInStore = (updatedDomain: DomainInfo) => {
    addDomainToStore(updatedDomain)
  }

  const removeDomainFromStore = (domainId: string) => {
    if (!domainId) return
    domainsList.value = domainsList.value.filter(d => d.id !== domainId)
    delete domainMap.value[domainId]
  }

  return {
    domains: domainsList,
    domainMap,
    domainOptions,
    isInitialized,
    isLoading,
    fetchDomains,
    getDomainById,
    getDomainName,
    addDomainToStore,
    updateDomainInStore,
    removeDomainFromStore,
    parseDomainName
  }
})
