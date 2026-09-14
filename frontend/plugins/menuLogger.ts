import { defineNuxtPlugin } from '#app'

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.hook('page:finish', async () => {
    // Only run on client side after navigation
    if (import.meta.client) {
      const route = useRoute()
      const { logAccess } = useMenu()
      await logAccess(route.path)
    }
  })
})
