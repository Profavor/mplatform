<template>
  <div class="auth-container">
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
            @click="handleLogin"
          >
            {{ $t('btn_login') || 'Login with Keycloak' }}
          </va-button>
        </va-card-content>
      </va-card>

      <div class="auth-footer">
        &copy; 2026 Domain System. All rights reserved.
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute } from '#app'
import { useI18n } from 'vue-i18n'
import { useToast } from 'vuestic-ui'

definePageMeta({
  layout: false
})

const route = useRoute()
const { t } = useI18n()
const { init: initToast } = useToast()

onMounted(() => {
  if (route.query.error) {
    initToast({
      message: t('auth_login_error_message') || '인증에 실패했습니다. 계정 정보를 다시 확인해주세요.',
      color: 'danger',
      duration: 5000,
      position: 'top-right'
    })
  }
})

const handleLogin = () => {
  window.location.href = '/auth/keycloak/login'
}
</script>

<style scoped>
.auth-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: radial-gradient(circle at 50% 30%, #f1f5f9 0%, #e2e8f0 100%);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  padding: 1.5rem;
}

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
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  box-shadow: 0 10px 25px -5px rgba(37, 99, 235, 0.18), 0 0 15px rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.6);
  margin-bottom: 1.25rem;
}

.title {
  font-size: 1.85rem;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.02em;
  margin: 0 0 0.5rem 0;
}

.subtitle {
  font-size: 0.95rem;
  color: #64748b;
  font-weight: 500;
  margin: 0;
}

.auth-card {
  border-radius: 24px;
  box-shadow: 0 20px 45px -10px rgba(30, 41, 59, 0.12), 0 0 25px 0 rgba(59, 130, 246, 0.05);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.8);
}

.auth-content {
  padding: 2.25rem 2rem 2.25rem 2rem !important;
}

.luxury-btn {
  height: 48px;
  border-radius: 12px !important;
  font-weight: 700 !important;
  font-size: 1rem !important;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%) !important;
  color: white !important;
  box-shadow: 0 8px 20px -4px rgba(37, 99, 235, 0.4) !important;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.luxury-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px -4px rgba(37, 99, 235, 0.5) !important;
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
  color: #94a3b8;
  font-weight: 500;
}
</style>
