import { defineNuxtRouteMiddleware, navigateTo } from '#app'

export default defineNuxtRouteMiddleware(async (to, from) => {
  if (import.meta.server) return

  try {
    const response: any = await $fetch('/api/system/install-status').catch(() => null)

    if (response) {
      const isInstalled = response.isInstalled ?? response.installed ?? false
      const hasAdminAccount = response.hasAdminAccount ?? false
      const isSystemReady = Boolean(isInstalled && hasAdminAccount)

      if (!isSystemReady) {
        if (to.path !== '/install') {
          return navigateTo('/install')
        }
      } else {
        if (to.path === '/install') {
          return navigateTo('/login')
        }
      }
    }
  } catch (e) {
    // Proceed on error
  }
})
