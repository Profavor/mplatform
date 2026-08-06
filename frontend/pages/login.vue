<template>
  <div class="auth-container" style="opacity: 0;" :style="{ opacity: isMounted ? 1 : 0, transition: 'opacity 0.4s ease' }">
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
        <div class="tab-header-container">
          <va-tabs v-model="activeTab" style="width: 100%;">
            <template #tabs>
              <va-tab name="login" style="flex: 1; text-align: center; font-weight: 700; font-size: 0.95rem;">
                {{ $t('tab_login') }}
              </va-tab>
              <va-tab name="register" style="flex: 1; text-align: center; font-weight: 700; font-size: 0.95rem;">
                {{ $t('tab_register') }}
              </va-tab>
            </template>
          </va-tabs>
        </div>

        <va-card-content class="auth-content">
          <!-- LOGIN FORM -->
          <form v-if="activeTab === 'login'" @submit.prevent="handleLogin" class="auth-form">
            <va-input 
              ref="usernameInputRef"
              v-model="loginForm.username" 
              :label="$t('label_username')" 
              :placeholder="$t('placeholder_username')"
              class="w-full mb-4"
              outline
              tabindex="1"
              :error="!!errorMessage && activeTab === 'login'"
              @keydown.tab.prevent="focusPassword"
            >
              <template #prependInner>
                <va-icon name="person" color="secondary" />
              </template>
            </va-input>
            
            <va-input 
              ref="passwordInputRef"
              v-model="loginForm.password" 
              :label="$t('label_password')" 
              type="password" 
              :placeholder="$t('placeholder_password')"
              class="w-full mb-4"
              outline
              tabindex="2"
              :error="!!errorMessage && activeTab === 'login'"
            >
              <template #prependInner>
                <va-icon name="lock" color="secondary" />
              </template>
            </va-input>
            
            <va-alert v-if="errorMessage && activeTab === 'login'" color="danger" class="mb-4 text-sm" outline>
              <template #icon><va-icon name="warning" /></template>
              {{ errorMessage }}
            </va-alert>
            
            <va-button 
              type="submit" 
              size="large" 
              class="w-full mt-2 luxury-btn" 
              :loading="loading" 
            >
              {{ $t('btn_login') }}
            </va-button>
          </form>

          <!-- REGISTER FORM -->
          <form v-if="activeTab === 'register'" @submit.prevent="handleRegister" class="auth-form">
            <div class="mb-4">
              <label class="custom-field-label">{{ $t('label_username') }}</label>
              <div class="input-with-button">
                <div class="flex-grow">
                  <va-input 
                    v-model="registerForm.username" 
                    :placeholder="$t('placeholder_username')"
                    class="w-full"
                    outline
                    tabindex="1"
                    @update:model-value="onUsernameChange"
                    @blur="onUsernameBlur"
                  >
                    <template #prependInner>
                      <va-icon name="person_add" color="secondary" />
                    </template>
                  </va-input>
                </div>
                <va-button 
                  preset="secondary"
                  border-color="primary"
                  class="check-duplicate-btn"
                  :loading="isCheckingUsername"
                  @click="checkUsernameAvailability"
                >
                  <va-icon name="verified_user" size="small" style="margin-right: 4px;" />
                  {{ $t('btn_check_duplicate') }}
                </va-button>
              </div>
              <div v-if="usernameCheckMessage" class="status-msg" :class="{ 'text-success': isUsernameAvailable, 'text-danger': !isUsernameAvailable }">
                <va-icon :name="isUsernameAvailable ? 'check_circle' : 'cancel'" size="small" class="mr-1" />
                {{ usernameCheckMessage }}
              </div>
            </div>
            
            <va-input 
              v-model="registerForm.password" 
              :label="$t('label_password')" 
              type="password" 
              :placeholder="$t('placeholder_password')"
              class="w-full mb-4"
              outline
              tabindex="2"
            >
              <template #prependInner>
                <va-icon name="lock" color="secondary" />
              </template>
            </va-input>

            <va-input 
              v-model="registerForm.confirmPassword" 
              :label="$t('label_confirm_password')" 
              type="password" 
              :placeholder="$t('placeholder_confirm_password')"
              class="w-full mb-4"
              outline
              tabindex="3"
              :error="passwordMismatch"
              :error-messages="passwordMismatch ? [$t('msg_password_mismatch')] : []"
            >
              <template #prependInner>
                <va-icon name="lock_reset" color="secondary" />
              </template>
            </va-input>

            <TimezoneSelect
              v-model="registerForm.timezone"
              tabindex="4"
              class="mb-4"
            />
            
            <va-alert v-if="errorMessage && activeTab === 'register'" color="danger" class="mb-4 text-sm" outline>
              <template #icon><va-icon name="warning" /></template>
              {{ errorMessage }}
            </va-alert>
            
            <va-alert v-if="successMessage && activeTab === 'register'" color="success" class="mb-4 text-sm" outline>
              <template #icon><va-icon name="check_circle" /></template>
              {{ successMessage }}
            </va-alert>
            
            <va-button 
              type="submit" 
              size="large" 
              class="w-full mt-2 luxury-btn luxury-btn-register" 
              :loading="loading" 
            >
              {{ $t('btn_register') }}
            </va-button>
          </form>
        </va-card-content>
      </va-card>

      <div class="auth-footer">
        &copy; 2026 Domain System. All rights reserved.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCookie, useRuntimeConfig } from '#app'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const isMounted = ref(false)
onMounted(() => {
  isMounted.value = true
})
const usernameInputRef = ref(null)
const passwordInputRef = ref(null)

const focusPassword = () => {
  if (passwordInputRef.value) {
    if (typeof passwordInputRef.value.focus === 'function') {
      passwordInputRef.value.focus()
    } else if (passwordInputRef.value.$el) {
      const inputEl = passwordInputRef.value.$el.querySelector('input')
      if (inputEl) inputEl.focus()
    }
  }
}

const activeTab = ref('login')

const loginForm = ref({ username: '', password: '' })
const registerForm = ref({ username: '', password: '', confirmPassword: '', role: 'ROLE_USER', timezone: 'Asia/Seoul' })

const isCheckingUsername = ref(false)
const isUsernameChecked = ref(false)
const isUsernameAvailable = ref(false)
const usernameCheckMessage = ref('')

const onUsernameChange = () => {
  isUsernameChecked.value = false
  isUsernameAvailable.value = false
  usernameCheckMessage.value = ''
}

// 탭(Tab) 키로 포커스 이탈 시 자동 중복 확인 (username이 있고 아직 미체크인 경우만 실행)
const onUsernameBlur = () => {
  const username = registerForm.value.username ? registerForm.value.username.trim() : ''
  if (username && !isUsernameChecked.value) {
    checkUsernameAvailability()
  }
}

const checkUsernameAvailability = async () => {
  const username = registerForm.value.username ? registerForm.value.username.trim() : ''
  if (!username) {
    usernameCheckMessage.value = t('msg_username_check_required')
    isUsernameAvailable.value = false
    return
  }

  isCheckingUsername.value = true
  usernameCheckMessage.value = ''

  try {
    const res = await $fetch('/api/auth/check-username', {
      params: { username }
    })
    isUsernameChecked.value = true
    isUsernameAvailable.value = !!res.available
    if (res.available) {
      usernameCheckMessage.value = t('msg_username_available')
    } else {
      usernameCheckMessage.value = t('msg_username_exists')
    }
  } catch {
    isUsernameChecked.value = false
    isUsernameAvailable.value = false
    usernameCheckMessage.value = 'Failed to check username availability.'
  } finally {
    isCheckingUsername.value = false
  }
}

const passwordMismatch = computed(() => {
  if (!registerForm.value.confirmPassword) return false
  return registerForm.value.password !== registerForm.value.confirmPassword
})

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const router = useRouter()

const config = useRuntimeConfig()
const accessMaxAge = Number(config.public.accessTokenExpirationSec || 1800)
const refreshMaxAge = Number(config.public.refreshTokenExpirationSec || 172800)

const tokenCookie = useCookie('auth_token', { maxAge: accessMaxAge })
const refreshTokenCookie = useCookie('refresh_token', { maxAge: refreshMaxAge })
const userCookie = useCookie('user_data', { maxAge: accessMaxAge })
const userPermissionsCookie = useCookie('user_permissions', { maxAge: accessMaxAge })

definePageMeta({
  layout: false
})

const handleLogin = async () => {
  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const response = await $fetch('/api/auth/login', {
      method: 'POST',
      body: loginForm.value
    })
    
    tokenCookie.value = response.token
    if (response.refreshToken) {
      refreshTokenCookie.value = response.refreshToken
    }
    if (response.permissions) {
      userPermissionsCookie.value = response.permissions
    }
    userCookie.value = JSON.stringify({
      id: response.id || response.uuid,
      uuid: response.uuid || response.id,
      username: response.username,
      role: response.role,
      organizationId: response.organizationId,
      departmentId: response.departmentId,
      timezone: response.timezone,
      serverOffset: response.serverOffset,
      permissions: response.permissions || [],
      mustChangePassword: response.mustChangePassword || false
    })
    
    const tzCookie = useCookie('timezone', { default: () => 'Asia/Seoul' })
    tzCookie.value = response.timezone || 'Asia/Seoul'
    
    const serverOffsetCookie = useCookie('server_offset', { default: () => '+09:00' })
    serverOffsetCookie.value = response.serverOffset || '+09:00'
    
    window.location.href = '/'
  } catch (error) {
    console.error("Login error:", error);
    errorMessage.value = error.response?.status === 401 ? 'Invalid username or password.' : 'Login failed. Please check the backend connection. (' + (error.message || '') + ')'
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  if (!registerForm.value.username || !registerForm.value.password || !registerForm.value.confirmPassword || !registerForm.value.timezone) {
    errorMessage.value = 'Please fill out all required fields.'
    loading.value = false
    return
  }

  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    errorMessage.value = t('msg_password_mismatch')
    loading.value = false
    return
  }

  if (!isUsernameChecked.value || !isUsernameAvailable.value) {
    errorMessage.value = t('msg_username_check_required')
    loading.value = false
    return
  }

  try {
    await $fetch('/api/auth/register', {
      method: 'POST',
      body: {
        username: registerForm.value.username,
        password: registerForm.value.password,
        role: registerForm.value.role,
        timezone: registerForm.value.timezone
      }
    })
    
    successMessage.value = 'Account created successfully! You can now log in.'
    
    // Auto switch to login tab after brief delay and populate username
    setTimeout(() => {
      loginForm.value.username = registerForm.value.username
      loginForm.value.password = ''
      registerForm.value = { username: '', password: '', confirmPassword: '', role: 'ROLE_USER', timezone: 'Asia/Seoul' }
      isUsernameChecked.value = false
      isUsernameAvailable.value = false
      usernameCheckMessage.value = ''
      successMessage.value = ''
      activeTab.value = 'login'
    }, 1500)
    
  } catch (error) {
    errorMessage.value = error.response?._data || 'Registration failed. Username might already exist.'
  } finally {
    loading.value = false
  }
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

.tab-header-container {
  border-bottom: 1px solid #f1f5f9;
  background: rgba(248, 250, 252, 0.6);
}

.auth-content {
  padding: 2.25rem 2rem 2.25rem 2rem !important;
}

.auth-form {
  display: flex;
  flex-direction: column;
}

.custom-field-label {
  display: block;
  font-size: 0.85rem;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 0.35rem;
}

.input-with-button {
  display: flex;
  gap: 0.6rem;
  align-items: center;
}

.flex-grow {
  flex: 1;
}

.check-duplicate-btn {
  margin-top: 0;
  height: 44px;
  white-space: nowrap;
  font-weight: 600;
  font-size: 0.85rem;
  border-radius: 10px;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.08);
}

.check-duplicate-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.15);
}

.status-msg {
  display: flex;
  align-items: center;
  font-size: 0.8rem;
  font-weight: 600;
  margin-top: 0.4rem;
}

.text-success {
  color: #10b981;
}

.text-danger {
  color: #ef4444;
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

.luxury-btn-register {
  background: linear-gradient(135deg, #059669 0%, #047857 100%) !important;
  box-shadow: 0 8px 20px -4px rgba(16, 185, 129, 0.35) !important;
}

.luxury-btn-register:hover {
  box-shadow: 0 12px 24px -4px rgba(16, 185, 129, 0.45) !important;
}

.w-full {
  width: 100%;
}

.mb-4 {
  margin-bottom: 1.25rem;
}

.mt-2 {
  margin-top: 0.75rem;
}

.text-sm {
  font-size: 0.875rem;
}

.auth-footer {
  text-align: center;
  margin-top: 2rem;
  font-size: 0.85rem;
  color: #94a3b8;
  font-weight: 500;
}

:deep(.va-input-wrapper__field) {
  border-radius: 10px;
  height: 44px;
}
</style>
