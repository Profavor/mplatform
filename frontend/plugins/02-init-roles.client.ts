import { useRoleStore } from '~/stores/useRoleStore'
import { useOidcAuth } from '#imports'

export default defineNuxtPlugin(async () => {
  const roleStore = useRoleStore()
  
  // OIDC 세션 복원(fetch) 비동기 처리가 있을 수 있으므로 먼저 OIDC의 accessToken을 확인합니다.
  const { user, loggedIn } = useOidcAuth()
  
  const checkAndFetchRoles = async () => {
    let tokenCookie = useCookie('auth_token').value || useCookie('token').value
    if (!tokenCookie || (typeof tokenCookie === 'string' && !tokenCookie.trim())) {
      if (user.value?.accessToken) {
        tokenCookie = user.value.accessToken
      }
    }
    
    // 토큰이 존재하면 역할을 가져옵니다.
    if (tokenCookie) {
      try {
        await roleStore.dispatch('fetchRoles')
      } catch (e) {
        console.error('Failed to auto-dispatch role store initialization:', e)
      }
    }
  }

  // 초기 로드 시 시도
  await checkAndFetchRoles()

  // 만약 비동기로 OIDC 로그인 상태가 true로 변경되면 역할을 다시 가져옵니다.
  if (process.client) {
    watch(loggedIn, async (newVal) => {
      if (newVal) {
        await checkAndFetchRoles()
      }
    })
  }
})
