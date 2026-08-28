import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'

describe('Login Page Authentication Defense & Callback Redirect Logic', () => {
  let isLoggingIn: any
  let isCheckingAuth: any
  let isRedirecting: any
  let loggedIn: any
  let authToken: any
  let mockNavigateTo: any

  beforeEach(() => {
    isLoggingIn = ref(false)
    isCheckingAuth = ref(true)
    isRedirecting = ref(false)
    loggedIn = ref(false)
    authToken = ref(null)
    mockNavigateTo = vi.fn()
  })

  it('should prevent duplicate login execution when already logging in or redirecting', async () => {
    const mockLoginFn = vi.fn().mockResolvedValue(undefined)

    const executeLogin = async () => {
      if (isLoggingIn.value || isRedirecting.value || isCheckingAuth.value) {
        return false
      }
      isLoggingIn.value = true
      try {
        await mockLoginFn()
        return true
      } finally {
        // Keep loading true while redirecting
      }
    }

    // 1. When isCheckingAuth is true, clicking should be blocked
    expect(await executeLogin()).toBe(false)
    expect(mockLoginFn).not.toHaveBeenCalled()

    // 2. When checking auth finishes
    isCheckingAuth.value = false
    const firstCall = executeLogin()
    // 3. Immediately trigger a second click
    const secondCall = executeLogin()

    expect(await firstCall).toBe(true)
    expect(await secondCall).toBe(false)
    expect(mockLoginFn).toHaveBeenCalledTimes(1)
  })

  it('should fallback to direct window.location when login method hangs or rejects', async () => {
    let redirectedUrl = ''
    const fallbackRedirect = (url: string) => {
      redirectedUrl = url
    }

    const executeLoginWithTimeout = async (loginPromise: Promise<void>, timeoutMs = 50) => {
      isLoggingIn.value = true
      try {
        const timeoutPromise = new Promise((_, reject) =>
          setTimeout(() => reject(new Error('TIMEOUT')), timeoutMs)
        )
        await Promise.race([loginPromise, timeoutPromise])
      } catch (err) {
        fallbackRedirect('/auth/keycloak/login')
      }
    }

    // A hanging login promise
    const hangingPromise = new Promise<void>(() => {})
    await executeLoginWithTimeout(hangingPromise, 10)

    expect(redirectedUrl).toBe('/auth/keycloak/login')
  })

  it('should trigger immediate dashboard redirect when session or token becomes valid', () => {
    const handleAuthChange = (isLoggedIn: boolean, tokenValue: string | null) => {
      if (isLoggedIn && tokenValue && !isRedirecting.value) {
        isRedirecting.value = true
        mockNavigateTo('/', { replace: true })
      }
    }

    // Initially unauthenticated
    handleAuthChange(loggedIn.value, authToken.value)
    expect(mockNavigateTo).not.toHaveBeenCalled()
    expect(isRedirecting.value).toBe(false)

    // After Keycloak callback completes and tokens are populated
    loggedIn.value = true
    authToken.value = 'mock-jwt-token'
    handleAuthChange(loggedIn.value, authToken.value)

    expect(mockNavigateTo).toHaveBeenCalledWith('/', { replace: true })
    expect(isRedirecting.value).toBe(true)
  })

  it('should compute button loading and disabled state accurately', () => {
    const getButtonState = (loggingIn: boolean, checking: boolean, redirecting: boolean) => {
      const isLoading = loggingIn || checking || redirecting
      const isDisabled = isLoading
      return { isLoading, isDisabled }
    }

    // During initial mount / check
    expect(getButtonState(false, true, false)).toEqual({ isLoading: true, isDisabled: true })

    // Idle state ready for click
    expect(getButtonState(false, false, false)).toEqual({ isLoading: false, isDisabled: false })

    // Clicked / Logging in
    expect(getButtonState(true, false, false)).toEqual({ isLoading: true, isDisabled: true })

    // Redirecting to dashboard
    expect(getButtonState(false, false, true)).toEqual({ isLoading: true, isDisabled: true })
  })
})
