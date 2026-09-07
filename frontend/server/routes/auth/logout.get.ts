import { defineEventHandler, deleteCookie, sendRedirect, useSession } from 'h3'

export default defineEventHandler(async (event) => {
  try {
    const session = await useSession(event, { name: 'nuxt-oidc-auth' })
    await session.clear()
  } catch (e) {}

  try {
    const oidcSession = await useSession(event, { name: 'oidc' })
    await oidcSession.clear()
  } catch (e) {}

  const cookiesToClear = [
    'auth_token',
    'token',
    'refresh_token',
    'user_data',
    'nuxt-oidc-auth',
    'oidc'
  ]

  cookiesToClear.forEach((name) => {
    try {
      deleteCookie(event, name, { path: '/' })
    } catch {}
  })

  return sendRedirect(event, '/login', 302)
})
