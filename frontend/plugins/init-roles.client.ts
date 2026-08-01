import { useRoleStore } from '~/stores/useRoleStore'

export default defineNuxtPlugin(async () => {
  const tokenCookie = useCookie('auth_token').value || useCookie('token').value
  if (!tokenCookie || (typeof tokenCookie === 'string' && !tokenCookie.trim())) {
    return
  }

  const roleStore = useRoleStore()
  try {
    await roleStore.dispatch('fetchRoles')
  } catch (e) {
    console.error('Failed to auto-dispatch role store initialization:', e)
  }
})
