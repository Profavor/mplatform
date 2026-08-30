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
    credentials: 'include' // Allow sending cookies in cross-origin or proxy environments
  }
}

export function wrapResponse(data: any): any {
  if (data === null || data === undefined) return data
  if (typeof data !== 'object') return data
  if (typeof Blob !== 'undefined' && data instanceof Blob) return data
  if (typeof ArrayBuffer !== 'undefined' && data instanceof ArrayBuffer) return data
  if (typeof FormData !== 'undefined' && data instanceof FormData) return data
  if (data.constructor && data.constructor.name !== 'Object' && data.constructor.name !== 'Array') return data

  return new Proxy(data, {
    get(target, prop, receiver) {
      if (Object.prototype.hasOwnProperty.call(target, prop) || (typeof prop === 'string' && prop in target)) {
        const val = Reflect.get(target, prop, receiver)
        if (typeof val === 'function') {
          return val.bind(target)
        }
        return val
      }
      if (prop === 'data') {
        return { value: target }
      }
      if (prop === 'status') {
        return { value: 'success' }
      }
      if (prop === 'error') {
        return { value: null }
      }
      const val = Reflect.get(target, prop, receiver)
      if (typeof val === 'function') {
        return val.bind(target)
      }
      return val
    }
  })
}

export function useCustomFetch(urlOrOptions?: any, options?: any): any {
  const getAuthToken = (): string | null => {
    try {
      const cookieToken = useCookie('auth_token').value || useCookie('token').value
      if (cookieToken) return String(cookieToken)
    } catch (e) {}

    if (import.meta.client) {
      try {
        const cookies = document.cookie.split(';')
        for (const cookie of cookies) {
          const [name, val] = cookie.trim().split('=')
          if ((name === 'auth_token' || name === 'token') && val) {
            return decodeURIComponent(val)
          }
        }
      } catch (e) {}
    }

    try {
      const { user, loggedIn } = useOidcAuth()
      if (loggedIn.value && user.value?.accessToken) {
        return String(user.value.accessToken)
      }
    } catch (e) {}

    if (import.meta.client) {
      try {
        const stored = localStorage.getItem('auth_token') || localStorage.getItem('token')
        if (stored) return stored
      } catch (e) {}
    }

    return null
  }

  const timezone = useCookie('timezone', { default: () => 'Asia/Seoul' }).value
  const { handleError } = useApiError()

  const customFetch = async <T = any>(url: string, opts: any = {}): Promise<T> => {
    const token = getAuthToken()
    const prepared = prepareFetchOptions(opts, token, timezone)
    const normalizedUrl = url.startsWith('/api') || url.startsWith('http') ? url : `/api${url.startsWith('/') ? '' : '/'}${url}`
    try {
      const rawRes = await $fetch<any>(normalizedUrl, prepared)
      return wrapResponse(rawRes) as T
    } catch (err: any) {
      if (!opts?.silent) {
        handleError(err)
      }
      throw err
    }
  }

  if (typeof urlOrOptions === 'string') {
    return customFetch(urlOrOptions, options)
  }

  const resultObj: any = (url: string, opts?: any) => customFetch(url, opts)
  resultObj.customFetch = customFetch
  resultObj.prepareFetchOptions = prepareFetchOptions
  resultObj.getAuthToken = getAuthToken

  return resultObj
}
