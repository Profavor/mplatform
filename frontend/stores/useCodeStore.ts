import { ref } from 'vue'
import { defineStore } from 'pinia'
import { useCookie } from '#app'

export const useCodeStore = defineStore('code', () => {
  // Global state moved inside
  const detailsByGroup = ref<Record<string, any>>({}) // { groupCode: { detailCode: CodeDetail } }
  const groupsLoaded = ref(new Set<string>())

  const token = useCookie('auth_token')

  const loadGroup = async (groupCode: string) => {
    if (groupsLoaded.value.has(groupCode)) return;

    try {
      const details = await $fetch<any[]>(`/api/code-groups/code/${groupCode}/details`, {
        headers: { Authorization: `Bearer ${token.value}` }
      })
      
      const map: Record<string, any> = {}
      details.forEach(d => {
        map[d.detailCode] = d
      })
      detailsByGroup.value[groupCode] = map
      groupsLoaded.value.add(groupCode)
    } catch (e) {
      console.error(`Failed to load code group: ${groupCode}`, e)
    }
  }

  // Helper to synchronously get name if loaded
  const getCodeName = (groupCode: string, detailCode: string, fallback: string | null = null) => {
    const locale = useCookie('locale', { default: () => 'ko' }).value
    const group = detailsByGroup.value[groupCode]
    if (!group) return fallback || detailCode
    
    const detail = group[detailCode]
    if (!detail) return fallback || detailCode
    
    const nameObj = detail.name
    if (!nameObj) return fallback || detailCode
    
    if (typeof nameObj === 'string') return nameObj
    return nameObj[locale] || nameObj.ko || nameObj.en || fallback || detailCode
  }

  // Bulk load multiple groups
  const preloadGroups = async (groupCodes: string[]) => {
    const promises = groupCodes.map(code => loadGroup(code))
    await Promise.all(promises)
  }

  const getDropdownOptions = (groupCode: string) => {
    const locale = useCookie('locale', { default: () => 'ko' }).value
    const group = detailsByGroup.value[groupCode]
    if (!group) return []
    
    return Object.values(group)
      .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
      .map(detail => {
        const nameObj = detail.name
        let text = detail.detailCode
        if (nameObj) {
          text = typeof nameObj === 'string' ? nameObj : (nameObj[locale] || nameObj.ko || nameObj.en || detail.detailCode)
        }
        return { value: detail.detailCode, text }
      })
  }

  const invalidateCache = () => {
    detailsByGroup.value = {}
    groupsLoaded.value.clear()
  }

  return {
    detailsByGroup,
    loadGroup,
    preloadGroups,
    getCodeName,
    getDropdownOptions,
    invalidateCache
  }
})
