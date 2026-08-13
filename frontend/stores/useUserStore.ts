import { ref } from 'vue'
import { useCookie } from '#app'
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', () => {
  const userMap = ref<Record<string, string>>({})
  const isInitialized = ref(false)
  const isLoading = ref(false)
  let userMapPromise: Promise<Record<string, string>> | null = null

  const token = useCookie('auth_token')

  const fetchUserMap = async (forceRefresh = false): Promise<Record<string, string>> => {
    if (!forceRefresh && isInitialized.value && Object.keys(userMap.value).length > 0) {
      return userMap.value
    }

    if (userMapPromise && !forceRefresh) {
      return await userMapPromise
    }

    isLoading.value = true
    userMapPromise = (async () => {
      try {
        const headers = token.value ? { Authorization: `Bearer ${token.value}` } : {}
        const res = await $fetch<Record<string, string>>('/api/users/map', { headers })
        if (res && typeof res === 'object' && !Array.isArray(res)) {
          userMap.value = res
          isInitialized.value = true
          return res
        }
      } catch (e) {
        console.error('Failed to fetch user map:', e)
      } finally {
        isLoading.value = false
        userMapPromise = null
      }
      return userMap.value
    })()

    return await userMapPromise
  }

  const parseI18nVal = (val: any): string => {
    if (!val) return ''
    if (typeof val === 'object') {
      return val.ko || val.en || Object.values(val)[0] || ''
    }
    if (typeof val === 'string' && val.trim().startsWith('{')) {
      try {
        const parsed = JSON.parse(val)
        if (typeof parsed === 'object' && parsed !== null) {
          return parsed.ko || parsed.en || Object.values(parsed)[0] || val
        }
      } catch {}
    }
    return String(val)
  }

  const getUserName = (idOrUuid?: string | null, fallbackName?: string | null): string => {
    const parsedFallback = parseI18nVal(fallbackName)
    const parsedId = parseI18nVal(idOrUuid)
    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i

    // 1. If fallbackName is valid username (not UUID), return fallbackName
    if (parsedFallback && !uuidRegex.test(parsedFallback.trim())) {
      return parsedFallback.trim()
    }
    // 2. Lookup idOrUuid in userMap
    if (idOrUuid && userMap.value[idOrUuid]) {
      return userMap.value[idOrUuid]
    }
    // 3. Lookup parsedFallback in userMap
    if (parsedFallback && userMap.value[parsedFallback]) {
      return userMap.value[parsedFallback]
    }
    // 4. If parsedId is valid username (not UUID), return parsedId
    if (parsedId && !uuidRegex.test(parsedId.trim())) {
      return parsedId.trim()
    }
    // 5. Fallback: check logged in user cookie
    try {
      const userData = useCookie<any>('user_data').value
      if (userData && userData.username) {
        return userData.username
      }
    } catch {}

    // 6. Fallback: return first username in userMap if available
    const mapValues = Object.values(userMap.value)
    if (mapValues.length > 0 && mapValues[0]) {
      return mapValues[0]
    }

    return parsedFallback || parsedId || ''
  }

  return {
    userMap,
    isInitialized,
    isLoading,
    fetchUserMap,
    getUserName,
    parseI18nVal
  }
})
