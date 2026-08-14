import { useOidcAuth, useCookie } from '#imports'

export function prepareFetchOptions(options: any = {}, token?: string | null, timezone?: string | null): any {
  const headers = { ...(options.headers || {}) }

  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  if (timezone) {
    headers['X-Timezone'] = timezone
  }

  return {
    ...options,
    headers,
  }
}

export function useCustomFetch() {
  const getAuthToken = (): string | null => {
    try {
      const cookieToken = useCookie('auth_token').value || useCookie('token').value
      if (cookieToken) return String(cookieToken)
    } catch (e) {}

    try {
      const { user, loggedIn } = useOidcAuth()
      if (loggedIn.value && user.value?.accessToken) {
        return String(user.value.accessToken)
      }
    } catch (e) {}

    return null
  }

  const timezone = useCookie('timezone', { default: () => 'Asia/Seoul' }).value
  const { handleError } = useApiError()

  const customFetch = async <T = any>(url: string, options: any = {}): Promise<T> => {
    const token = getAuthToken()
    const prepared = prepareFetchOptions(options, token, timezone)
    try {
      return await $fetch<T>(url, prepared)
    } catch (err: any) {
      handleError(err)
      throw err
    }
  }

  return {
    customFetch,
    prepareFetchOptions,
    getAuthToken,
  }
}
