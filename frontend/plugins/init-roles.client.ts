import { useRoleStore } from '~/stores/useRoleStore'

export default defineNuxtPlugin(async () => {
  const roleStore = useRoleStore()
  // Automatically dispatch fetchRoles when application mounts or page refreshes
  try {
    await roleStore.dispatch('fetchRoles')
  } catch (e) {
    console.error('Failed to auto-dispatch role store initialization:', e)
  }
})
