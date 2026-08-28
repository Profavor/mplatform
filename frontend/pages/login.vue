<template>
  <div :class="['auth-container', isDark ? 'theme-dark' : 'theme-light']">
    <div class="auth-box">
      <!-- Welcome Header -->
      <div class="auth-header">
        <div class="logo-container">
          <va-icon name="hub" size="32px" color="primary" />
        </div>
        <h1 class="title">Domain System</h1>
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
        </va-card-content>
      </va-card>

      <div class="auth-footer">
        &copy; 2026 Domain System. All rights reserved.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, navigateTo, useCookie } from '#app'
import { useI18n } from 'vue-i18n'
import { useToast, useColors } from 'vuestic-ui'

definePageMeta({
  layout: false
})

const route = useRoute()
const { t } = useI18n()
const { init: initToast } = useToast()
const colors = useColors()
const currentPresetName = colors?.currentPresetName
const isDark = computed(() => currentPresetName?.value === 'dark')

const { loggedIn, login, logout } = useOidcAuth()
const authToken = useCookie('auth_token')

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

const redirectToDashboard = () => {
  if (isRedirecting.value) return
  isRedirecting.value = true
  navigateTo('/', { replace: true })
}

const checkAuthentication = async () => {
  const token = authToken.value
  if (route.query.error || route.query.expired) {
    if (loggedIn.value) {
      try {
        await logout('keycloak')
      } catch (e) {}
    }
    initToast({
      message: t('auth_login_error_message'),
      color: 'danger',
      duration: 5000,
      position: 'top-right'
    })
    isCheckingAuth.value = false
    return
  }

  if (loggedIn.value && token) {
    redirectToDashboard()
    return
  }
  isCheckingAuth.value = false
}

// Keycloak 콜백 복귀 시 비동기로 세션 및 토큰이 채워지는 즉시 감지하여 대시보드로 이동
watch([loggedIn, () => authToken.value], ([isLoggedIn, currentToken]) => {
  if (isLoggedIn && currentToken && !route.query.error && !route.query.expired) {
    redirectToDashboard()
  }
}, { immediate: true })

onMounted(async () => {
  await checkAuthentication()
})

const handleLogin = async () => {
  if (isButtonDisabled.value) return
  isLoggingIn.value = true

  try {
    const loginPromise = login('keycloak')
    const timeoutPromise = new Promise((_, reject) =>
      setTimeout(() => reject(new Error('TIMEOUT')), 3000)
    )
    await Promise.race([loginPromise, timeoutPromise])
  } catch (e) {
    console.warn('OIDC client login failed or timed out, falling back to direct login redirect', e)
    window.location.href = '/auth/keycloak/login'
  }
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
  color: #94a3b8;
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
  color: #64748b;
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
</style>
