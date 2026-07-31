/**
 * Authorization 및 Timezone 헤더 자동 부여 $fetch 공통 래퍼 Composable
 */

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
  const token = useCookie('auth_token').value || useCookie('token').value
  const timezone = useCookie('timezone', { default: () => 'Asia/Seoul' }).value
  const { handleError } = useApiError()

  const customFetch = async <T = any>(url: string, options: any = {}): Promise<T> => {
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
  }
}
