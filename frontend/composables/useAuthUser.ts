import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { useCookie } from '#app'

export interface AuthUser {
  id?: string
  username?: string
  role?: string
  timezone?: string
  organizationId?: string
  departmentId?: string
  teamId?: string
  permissions?: string[]
  mustChangePassword?: boolean
}

export const useAuthUser = defineStore('authUser', () => {
  const currentUserState = ref<AuthUser | null>(null)
  const isLoadingUser = ref(false)
  
  const { customFetch } = useCustomFetch()
  const tokenCookie = useCookie('auth_token')
  const timezoneCookie = useCookie('timezone', { default: () => 'Asia/Seoul' })

  const fetchCurrentUser = async (force = false) => {
    if (!tokenCookie.value) {
      currentUserState.value = null
      return null
    }

    if (currentUserState.value && !force) {
      return currentUserState.value
    }

    isLoadingUser.value = true
    try {
      const data = await customFetch<AuthUser>('/api/auth/me')
      if (data) {
        currentUserState.value = data
        if (data.timezone) {
          timezoneCookie.value = data.timezone
        }
      }
      return currentUserState.value
    } catch (e) {
      console.error('Failed to fetch current user (/api/auth/me):', e)
      currentUserState.value = null
      return null
    } finally {
      isLoadingUser.value = false
    }
  }

  const setCurrentUser = (user: AuthUser | null) => {
    currentUserState.value = user
    if (user?.timezone) {
      timezoneCookie.value = user.timezone
    }
  }

  const currentUser = computed(() => currentUserState.value)
  const currentUsername = computed(() => currentUserState.value?.username || '')
  const currentUserId = computed(() => currentUserState.value?.id || '')
  const currentUserRole = computed(() => currentUserState.value?.role || '')

  return {
    currentUser,
    currentUsername,
    currentUserId,
    currentUserRole,
    isLoadingUser,
    fetchCurrentUser,
    setCurrentUser
  }
})
