<template>
  <div :class="['auth-container', isDark ? 'theme-dark' : 'theme-light']">
    <main class="auth-box" role="main">
      <!-- Welcome Header -->
      <div class="auth-header">
        <div class="logo-container">
          <va-icon name="hub" size="32px" color="primary" />
        </div>
        <h1 class="title">{{ $t('footer.system_name') }}</h1>
        <p class="subtitle">{{ $t('login_title_sub') }}</p>
      </div>

      <va-card class="auth-card">
        <va-card-content class="auth-content">
          <va-button 
            size="large" 
            class="w-full mt-2 luxury-btn" 
            :loading="isButtonLoading"
            :disabled="isButtonDisabled"
            @click="handleLogin"
          >
            {{ buttonLabel }}
          </va-button>
          
          <div v-if="isRedirecting || isCheckingAuth" style="margin-top: 1rem; text-align: center; font-size: 0.85rem; color: var(--va-text-secondary); display: flex; align-items: center; justify-content: center; gap: 0.4rem;">
            <va-progress-circle indeterminate size="16px" color="primary" />
            <span>{{ isRedirecting ? $t('auth_redirecting') : $t('auth_checking') }}</span>
          </div>

          <div class="register-prompt-container">
            <span class="register-prompt">{{ $t('need_account') }}</span>
            <NuxtLink to="/register" class="register-link">
              {{ $t('link_register') }}
            </NuxtLink>
          </div>
        </va-card-content>
      </va-card>

      <!-- 2FA Step-up Verification Modal -->
      <TwoFactorVerifyModal
        v-model:is-open="is2FaModalOpen"
        :username="twoFactorUsername"
        :temp-token="twoFactorTempToken"
        :masked-email="twoFactorMaskedEmail"
        :grace-period-remaining-days="twoFactorGraceDays"
        :two-factor-type="twoFactorType"
        @verified="handle2FaVerified"
        @skip="handle2FaSkip"
      />

      <div class="auth-footer">
        {{ $t('footer.copyright', { year: new Date().getFullYear() }) }}
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, navigateTo, useCookie, useHead } from '#app'
import { useI18n } from 'vue-i18n'
import { useToast, useColors } from 'vuestic-ui'
import TwoFactorVerifyModal from '~/components/auth/TwoFactorVerifyModal.vue'

definePageMeta({
  layout: false
})

const route = useRoute()
const { t, locale } = useI18n()

useHead({
  title: computed(() => t('footer.system_name')),
  htmlAttrs: {
    lang: computed(() => (locale?.value || 'ko').startsWith('en') ? 'en' : 'ko')
  }
})
const { init: initToast } = useToast()
const colors = useColors()
const currentPresetName = colors?.currentPresetName
const isDark = computed(() => currentPresetName?.value === 'dark')

const { loggedIn, login, logout } = useOidcAuth()
const authToken = useCookie('auth_token')

const is2FaModalOpen = ref(false)
const twoFactorUsername = ref('')
const twoFactorTempToken = ref('')
const twoFactorMaskedEmail = ref('')
const twoFactorGraceDays = ref(null)
const twoFactorType = ref('TOTP')

const handle2FaVerified = () => {
  redirectToDashboard()
}

const handle2FaSkip = () => {
  redirectToDashboard()
}

const isCheckingAuth = ref(true)
const isLoggingIn = ref(false)
const isRedirecting = ref(false)

const isButtonDisabled = computed(() => isLoggingIn.value || isRedirecting.value || isCheckingAuth.value)
const isButtonLoading = computed(() => isLoggingIn.value || isRedirecting.value || isCheckingAuth.value)

const buttonLabel = computed(() => {
  if (isRedirecting.value) return t('auth_redirecting')
  if (isLoggingIn.value) return t('btn_logging_in')
  if (isCheckingAuth.value) return t('auth_checking')
  return t('btn_login')
})

const getSafeRedirectUrl = () => {
  const queryRedirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
  if (queryRedirect && queryRedirect.startsWith('/') && !queryRedirect.startsWith('//') && !queryRedirect.startsWith('/login')) {
    if (process.client) {
      try {
        sessionStorage.setItem('post_login_redirect', queryRedirect)
      } catch (e) {}
    }
    return queryRedirect
  }
  if (process.client) {
    try {
      const saved = sessionStorage.getItem('post_login_redirect')
      if (saved && saved.startsWith('/') && !saved.startsWith('//') && !saved.startsWith('/login')) {
        return saved
      }
    } catch (e) {}
  }
  return '/dashboard'
}

const redirectToDashboard = () => {
  if (isRedirecting.value) return
  isRedirecting.value = true
  const target = getSafeRedirectUrl()
  if (process.client) {
    try {
      sessionStorage.removeItem('post_login_redirect')
    } catch (e) {}
  }
  navigateTo(target, { replace: true })
}

const checkAuthentication = async () => {
  const token = authToken.value
  const isExpired = route.query.expired === '1' || route.query.expired === 'true' || route.query.reason === 'expired'
  const isError = Boolean(route.query.error)

  // 이미 유효한 토큰 또는 로그인 세션이 확보된 경우 만료 파라미터가 잔존하더라도 대시보드로 이동
  if (loggedIn.value || token) {
    redirectToDashboard()
    return
  }

  if (isError || isExpired) {
    if (loggedIn.value) {
      try {
        const { clear } = useOidcAuth()
        await clear()
      } catch (e) {}
    }
    initToast({
      message: isExpired ? t('auth_session_expired') : t('auth_login_error_message'),
      color: isExpired ? 'warning' : 'danger',
      duration: 6000,
      position: 'top-right'
    })
    isCheckingAuth.value = false
    return
  }

  isCheckingAuth.value = false
}

// Keycloak 콜백 복귀 시 비동기로 세션 및 토큰이 채워지는 즉시 감지하여 대시보드로 이동
watch([loggedIn, () => authToken.value], ([isLoggedIn, currentToken]) => {
  if (isLoggedIn || currentToken) {
    redirectToDashboard()
  }
}, { immediate: true })

onMounted(async () => {
  if (typeof document !== 'undefined') {
    document.documentElement.lang = (locale?.value || 'ko').startsWith('en') ? 'en-US' : 'ko-KR'
  }
  if (route.query.temp_token && route.query.username) {
    twoFactorUsername.value = String(route.query.username)
    twoFactorTempToken.value = String(route.query.temp_token)
    twoFactorMaskedEmail.value = String(route.query.masked_email || '')
    twoFactorType.value = String(route.query.two_factor_type || 'TOTP')
    if (route.query.grace_days) {
      twoFactorGraceDays.value = parseInt(String(route.query.grace_days), 10)
    }
    is2FaModalOpen.value = true
    isCheckingAuth.value = false
    return
  }
  await checkAuthentication()
})

const handleLogin = async () => {
  if (isButtonDisabled.value) return
  isLoggingIn.value = true

  // Save redirect URL in sessionStorage before redirecting to SSO
  if (process.client) {
    const queryRedirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    if (queryRedirect && queryRedirect.startsWith('/') && !queryRedirect.startsWith('//') && !queryRedirect.startsWith('/login')) {
      try {
        sessionStorage.setItem('post_login_redirect', queryRedirect)
      } catch (e) {}
    }
  }

  if (loggedIn.value || authToken.value) {
    redirectToDashboard()
    return
  }

  const target = getSafeRedirectUrl()
  window.location.href = `/api/auth/oidc/login?client=web&redirect=${encodeURIComponent(target)}`
}
</script>

<style scoped>
.auth-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  padding: 1.5rem;
  transition: all 0.3s ease;
}

/* Light Theme */
.theme-light {
  background: radial-gradient(circle at 50% 30%, #f1f5f9 0%, #e2e8f0 100%);
}
.theme-light .logo-container {
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 10px 25px -5px rgba(37, 99, 235, 0.18), 0 0 15px rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.6);
}
.theme-light .title {
  color: #0f172a;
}
.theme-light .subtitle {
  color: #64748b;
}
.theme-light .auth-card {
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 20px 45px -10px rgba(30, 41, 59, 0.12), 0 0 25px 0 rgba(59, 130, 246, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.8);
}
.theme-light .auth-footer {
  color: #475569;
}

/* Dark Theme */
.theme-dark {
  background: radial-gradient(circle at 50% 30%, #1e293b 0%, #0f172a 100%);
}
.theme-dark .logo-container {
  background: rgba(30, 41, 59, 0.9);
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3), 0 0 15px rgba(37, 99, 235, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.1);
}
.theme-dark .title {
  color: #f8fafc;
}
.theme-dark .subtitle {
  color: #94a3b8;
}
.theme-dark .auth-card {
  background: rgba(30, 41, 59, 0.95);
  box-shadow: 0 20px 45px -10px rgba(0, 0, 0, 0.5), 0 0 25px 0 rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
}
.theme-dark .auth-footer {
  color: #cbd5e1;
}

/* Common Styles */
.auth-box {
  width: 100%;
  max-width: 440px;
}

.auth-header {
  text-align: center;
  margin-bottom: 2rem;
}

.logo-container {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 68px;
  height: 68px;
  border-radius: 20px;
  margin-bottom: 1.25rem;
  transition: all 0.3s ease;
}

.title {
  font-size: 1.85rem;
  font-weight: 800;
  letter-spacing: -0.02em;
  margin: 0 0 0.5rem 0;
  transition: color 0.3s ease;
}

.subtitle {
  font-size: 0.95rem;
  font-weight: 500;
  margin: 0;
  transition: color 0.3s ease;
}

.auth-card {
  border-radius: 24px;
  overflow: hidden;
  backdrop-filter: blur(16px);
  transition: all 0.3s ease;
}

.auth-content {
  padding: 2.25rem 2rem 2.25rem 2rem !important;
}

.luxury-btn {
  height: 48px;
  border-radius: 12px !important;
  font-weight: 700 !important;
  font-size: 1rem !important;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%) !important;
  color: white !important;
  box-shadow: 0 8px 20px -4px rgba(37, 99, 235, 0.4) !important;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.luxury-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px -4px rgba(37, 99, 235, 0.6) !important;
}

.w-full {
  width: 100%;
}

.mt-2 {
  margin-top: 0.75rem;
}

.auth-footer {
  text-align: center;
  margin-top: 2rem;
  font-size: 0.85rem;
  font-weight: 500;
  transition: color 0.3s ease;
}

.register-prompt-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 1.5rem;
  padding-top: 1.25rem;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  font-size: 0.875rem;
}

.theme-dark .register-prompt-container {
  border-top-color: rgba(255, 255, 255, 0.1);
}

.register-prompt {
  color: var(--va-text-secondary);
}

.register-link {
  color: var(--va-primary);
  font-weight: 600;
  text-decoration: none;
  transition: opacity 0.2s ease;
}

.register-link:hover {
  text-decoration: underline;
}
</style>
