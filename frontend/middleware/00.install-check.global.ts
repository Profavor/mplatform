import { defineNuxtRouteMiddleware, navigateTo } from '#app'
import { useCookie, useOidcAuth, useRequestFetch } from '#imports'

export default defineNuxtRouteMiddleware(async (to, from) => {
  try {
    let fetcher: any = $fetch
    try {
      if (import.meta.server) {
        fetcher = useRequestFetch()
      }
    } catch (e) {}

    const response: any = await fetcher('/api/system/install-status').catch(() => null)

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
          // Check if user is already authenticated (#96)
          let isAuthenticated = false
          try {
            const token = useCookie('auth_token').value || useCookie('token').value
            if (token) {
              isAuthenticated = true
            }
          } catch (e) {}

          if (!isAuthenticated) {
            try {
              const { loggedIn } = useOidcAuth()
              if (loggedIn?.value) {
                isAuthenticated = true
              }
            } catch (e) {}
          }

          if (isAuthenticated) {
            return navigateTo('/')
          } else {
            return navigateTo('/login')
          }
        }
      }
    }
  } catch (e) {
    // Proceed on error
  }
})
