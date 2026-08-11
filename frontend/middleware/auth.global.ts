import { useOidcAuth } from '#imports'

export default defineNuxtRouteMiddleware((to, from) => {
  if (to.path === '/login' || to.path === '/install') {
    return
  }

  const { loggedIn } = useOidcAuth()
  
  if (!loggedIn.value) {
    return navigateTo('/login')
  }
})
