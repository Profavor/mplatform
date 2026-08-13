import { useOidcAuth, useCookie } from '#imports'

export default defineNuxtPlugin((nuxtApp) => {
  const { loggedIn, user } = useOidcAuth()
  const authTokenCookie = useCookie('auth_token', { maxAge: 1800, path: '/' })

  // 즉시 동기화 (plugin 실행 시점)
  if (loggedIn.value && user.value?.accessToken) {
    authTokenCookie.value = user.value.accessToken
  }

  watchEffect(() => {
    if (loggedIn.value && user.value?.accessToken) {
      authTokenCookie.value = user.value.accessToken
    } else if (!loggedIn.value) {
      authTokenCookie.value = null
    }
  })
})
