export default defineNuxtRouteMiddleware(async (to, from) => {
  if (to.path === '/login' || to.path === '/install') {
    return
  }

  const token = useCookie('auth_token')
  const refreshToken = useCookie('refresh_token')
  const userDataCookie = useCookie('user_data')

  // 1. Access Token이 없는데 Refresh Token이 유효하게 남아있는 경우 미들웨어 단계에서 자동 갱신 시도
  if (!token.value && refreshToken.value) {
    try {
      const res: any = await $fetch('/api/auth/refresh', {
        method: 'POST',
        body: { refreshToken: refreshToken.value }
      })

      if (res && res.token) {
        token.value = res.token
        if (res.refreshToken) {
          refreshToken.value = res.refreshToken
        }
        return
      }
    } catch (e) {
      console.warn('Middleware failed to auto refresh token:', e)
    }
  }

  // 2. Refresh Token마저 없거나 갱신에 최종 실패한 경우에만 로그인 페이지로 리다이렉트
  if (!token.value) {
    return navigateTo('/login')
  }
})
