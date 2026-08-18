import { useOidcAuth, useCookie } from '#imports'

export default defineNuxtRouteMiddleware((to, from) => {
  if (
    to.path === '/login' ||
    to.path === '/install' ||
    to.path.startsWith('/auth') ||
    to.path.startsWith('/api')
  ) {
    return
  }

  const { loggedIn } = useOidcAuth()
  const token = useCookie('auth_token').value
  
  if (!loggedIn.value || !token) {
    return navigateTo('/login')
  }
})
