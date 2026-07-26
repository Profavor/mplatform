import { ref, computed } from 'vue'
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
}

const currentUserState = ref<AuthUser | null>(null)
const isLoadingUser = ref(false)

export function useAuthUser() {
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
      const headers: Record<String, String> = {
        Authorization: `Bearer ${tokenCookie.value}`
      }
      const data = await $fetch<AuthUser>('/api/auth/me', { headers })
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
}
