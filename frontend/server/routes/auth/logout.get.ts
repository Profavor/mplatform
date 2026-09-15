import { defineEventHandler, deleteCookie, sendRedirect, useSession, getHeader, getCookie, getQuery } from 'h3'

export default defineEventHandler(async (event) => {
  const query = getQuery(event)
  const idToken = getCookie(event, 'id_token')

  try {
    const session = await useSession(event, { name: 'nuxt-oidc-auth', password: '' })
    await session.clear()
  } catch (e) {}

  try {
    const oidcSession = await useSession(event, { name: 'oidc', password: '' })
    await oidcSession.clear()
  } catch (e) {}

  const cookiesToClear = [
    'auth_token',
    'token',
    'refresh_token',
    'id_token',
    'user_data',
    'nuxt-oidc-auth',
    'oidc'
  ]

  cookiesToClear.forEach((name) => {
    try {
      deleteCookie(event, name, { path: '/' })
      deleteCookie(event, name, { path: '' })
      deleteCookie(event, name, { path: '/', secure: true })
      deleteCookie(event, name, { path: '', secure: true })
    } catch {}
  })

  // 로컬 전용 로그아웃 요청 시 Keycloak 리다이렉트 없이 /login으로 직접 이동
  if (query.local_only === 'true' || query.local === '1') {
    return sendRedirect(event, '/login?logout=true', 302)
  }

  const host = getHeader(event, 'x-forwarded-host') || getHeader(event, 'host') || 'mplatform.local'
  const proto = getHeader(event, 'x-forwarded-proto') || (host.includes('mplat.store') ? 'https' : 'https')
  const baseUrl = `${proto}://${host}`
  const postLogoutRedirectUri = `${baseUrl}/login?logout=true`
  const clientId = process.env.KEYCLOAK_CLIENT_ID || 'mdm-frontend'
  const realm = process.env.KEYCLOAK_REALM || 'mplatform'

  let keycloakLogoutUrl = `${baseUrl}/auth/realms/${realm}/protocol/openid-connect/logout?post_logout_redirect_uri=${encodeURIComponent(postLogoutRedirectUri)}&client_id=${encodeURIComponent(clientId)}`
  if (idToken) {
    keycloakLogoutUrl += `&id_token_hint=${encodeURIComponent(idToken)}`
  }

  return sendRedirect(event, keycloakLogoutUrl, 302)
})
