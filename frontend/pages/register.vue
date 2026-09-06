<template>
  <div :class="['register-container', isDark ? 'theme-dark' : 'theme-light']">
    <main class="register-box" role="main">
      <!-- Header -->
      <div class="register-header">
        <div class="logo-container">
          <va-icon name="hub" size="32px" color="primary" />
        </div>
        <h1 class="title">{{ $t('register_title') }}</h1>
        <p class="subtitle">{{ $t('register_subtitle') }}</p>
      </div>

      <va-card class="register-card">
        <va-card-content class="register-content">
          <form @submit.prevent="handleSubmit" class="register-form">
            <!-- Company / Org Name -->
            <div class="form-group">
              <label class="form-label">{{ $t('company_name') }} *</label>
              <va-input
                v-model="form.companyName"
                :placeholder="$t('company_placeholder')"
                class="w-full"
                required
              >
                <template #prependInner>
                  <va-icon name="business" color="secondary" size="small" />
                </template>
              </va-input>
            </div>

            <!-- Username with duplicate check -->
            <div class="form-group">
              <label class="form-label">{{ $t('label_username') }} *</label>
              <va-input
                v-model="form.username"
                :placeholder="$t('username_placeholder')"
                class="w-full"
                :error="Boolean(usernameError)"
                :error-messages="usernameError ? [usernameError] : []"
                :success="isUsernameAvailable"
                :messages="isUsernameAvailable ? [$t('username_available')] : []"
                @blur="checkUsernameAvailability"
                required
              >
                <template #prependInner>
                  <va-icon name="person" color="secondary" size="small" />
                </template>
                <template #appendInner>
                  <va-icon v-if="isCheckingUsername" name="sync" spin size="small" color="primary" />
                </template>
              </va-input>
            </div>

            <!-- Email -->
            <div class="form-group">
              <label class="form-label">{{ $t('email') }} *</label>
              <va-input
                v-model="form.email"
                type="email"
                :placeholder="$t('email_placeholder')"
                class="w-full"
                required
              >
                <template #prependInner>
                  <va-icon name="email" color="secondary" size="small" />
                </template>
              </va-input>
            </div>

            <!-- Password -->
            <div class="form-group">
              <label class="form-label">{{ $t('new_password') }} *</label>
              <va-input
                v-model="form.password"
                type="password"
                :placeholder="$t('password_placeholder')"
                class="w-full"
                required
              >
                <template #prependInner>
                  <va-icon name="lock" color="secondary" size="small" />
                </template>
              </va-input>
            </div>

            <!-- Confirm Password -->
            <div class="form-group">
              <label class="form-label">{{ $t('confirm_password') }} *</label>
              <va-input
                v-model="form.confirmPassword"
                type="password"
                :placeholder="$t('confirm_password_placeholder')"
                class="w-full"
                required
              >
                <template #prependInner>
                  <va-icon name="lock_outline" color="secondary" size="small" />
                </template>
              </va-input>
            </div>

            <!-- Terms & Privacy Agreement -->
            <div class="form-group terms-group">
              <va-checkbox
                v-model="form.termsAgreed"
                :label="$t('terms_agree')"
                class="terms-checkbox"
              />
            </div>

            <!-- Submit Button -->
            <va-button
              type="submit"
              size="large"
              class="w-full luxury-btn mt-2"
              :loading="isSubmitting"
              :disabled="isSubmitting"
            >
              {{ isSubmitting ? $t('btn_registering') : $t('btn_start_trial') }}
            </va-button>
          </form>

          <!-- Login Link for Existing Users -->
          <div class="login-link-container">
            <span class="login-prompt">{{ $t('already_have_account') }}</span>
            <NuxtLink to="/login" class="login-link">
              {{ $t('link_login') }}
            </NuxtLink>
          </div>
        </va-card-content>
      </va-card>

      <!-- Footer -->
      <div class="register-footer">
        {{ $t('footer.copyright', { year: new Date().getFullYear() }) }}
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRoute, navigateTo, useCookie, useHead } from '#app'
import { useI18n } from 'vue-i18n'
import { useToast, useColors } from 'vuestic-ui'

definePageMeta({
  layout: false
})

const { t, locale } = useI18n()
const { init: initToast } = useToast()
const colors = useColors()
const currentPresetName = colors?.currentPresetName
const isDark = computed(() => currentPresetName?.value === 'dark')

useHead({
  title: computed(() => `${t('register_title')} — ${t('footer.system_name')}`),
  htmlAttrs: {
    lang: computed(() => (locale?.value || 'ko').startsWith('en') ? 'en' : 'ko')
  }
})

const authToken = useCookie('auth_token')
const refreshToken = useCookie('refresh_token')

const form = reactive({
  companyName: '',
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  termsAgreed: false
})

const isSubmitting = ref(false)
const isCheckingUsername = ref(false)
const isUsernameAvailable = ref(false)
const usernameError = ref('')

const checkUsernameAvailability = async () => {
  if (!form.username || form.username.trim().length < 3) {
    isUsernameAvailable.value = false
    usernameError.value = ''
    return
  }

  isCheckingUsername.value = true
  usernameError.value = ''
  try {
    const res = await fetch(`/api/auth/check-username?username=${encodeURIComponent(form.username.trim())}`)
    if (res.ok) {
      const data = await res.json()
      if (data && data.available) {
        isUsernameAvailable.value = true
        usernameError.value = ''
      } else {
        isUsernameAvailable.value = false
        usernameError.value = t('username_exists')
      }
    }
  } catch (e) {
    console.warn('Username check failed:', e)
  } finally {
    isCheckingUsername.value = false
  }
}

const handleSubmit = async () => {
  // 1. Validation
  if (!form.companyName.trim() || !form.username.trim() || !form.email.trim() || !form.password) {
    initToast({
      message: t('fill_all_fields'),
      color: 'warning',
      duration: 4000
    })
    return
  }

  if (form.password.length < 8) {
    initToast({
      message: t('min_8_chars'),
      color: 'warning',
      duration: 4000
    })
    return
  }

  if (form.password !== form.confirmPassword) {
    initToast({
      message: t('passwords_do_not_match'),
      color: 'danger',
      duration: 4000
    })
    return
  }

  if (!form.termsAgreed) {
    initToast({
      message: t('terms_agree'),
      color: 'warning',
      duration: 4000
    })
    return
  }

  isSubmitting.value = true
  try {
    const res = await fetch('/api/auth/self-register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        companyName: form.companyName.trim(),
        username: form.username.trim(),
        email: form.email.trim(),
        password: form.password,
        termsAgreed: true,
        timezone: Intl.DateTimeFormat().resolvedOptions().timeZone || 'Asia/Seoul'
      })
    })

    if (!res.ok) {
      const errData = await res.json().catch(() => ({}))
      throw new Error(errData.message || t('register_failed'))
    }

    const data = await res.json()
    if (data.token) {
      authToken.value = data.token
    }
    if (data.refreshToken) {
      refreshToken.value = data.refreshToken
    }

    initToast({
      message: t('register_success'),
      color: 'success',
      duration: 5000
    })

    navigateTo('/dashboard')
  } catch (err: any) {
    initToast({
      message: err.message || t('register_failed'),
      color: 'danger',
      duration: 5000
    })
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  padding: 2rem 1.5rem;
  transition: all 0.3s ease;
}

.theme-light {
  background: radial-gradient(circle at 50% 0%, #f1f5f9 0%, #e2e8f0 100%);
  color: #0f172a;
}

.theme-dark {
  background: radial-gradient(circle at 50% 0%, #1e293b 0%, #0f172a 100%);
  color: #f8fafc;
}

.register-box {
  width: 100%;
  max-width: 480px;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.register-header {
  text-align: center;
}

.logo-container {
  display: inline-flex;
  padding: 0.75rem;
  border-radius: 1rem;
  background: rgba(var(--va-primary-rgb), 0.1);
  margin-bottom: 0.75rem;
}

.title {
  font-size: 1.65rem;
  font-weight: 800;
  letter-spacing: -0.025em;
  margin-bottom: 0.4rem;
}

.subtitle {
  font-size: 0.9rem;
  color: var(--va-text-secondary);
  line-height: 1.4;
}

.register-card {
  border-radius: 1.25rem;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.08), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.register-content {
  padding: 2rem;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 1.1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.form-label {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--va-text-primary);
}

.terms-group {
  margin-top: 0.25rem;
}

.terms-checkbox {
  font-size: 0.85rem;
}

.luxury-btn {
  font-weight: 700;
  letter-spacing: 0.02em;
  height: 46px;
  border-radius: 0.75rem;
}

.login-link-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 1.25rem;
  padding-top: 1rem;
  border-top: 1px solid var(--va-background-element);
  font-size: 0.875rem;
}

.login-prompt {
  color: var(--va-text-secondary);
}

.login-link {
  color: var(--va-primary);
  font-weight: 600;
  text-decoration: none;
  transition: opacity 0.2s ease;
}

.login-link:hover {
  text-decoration: underline;
}

.register-footer {
  text-align: center;
  font-size: 0.8rem;
  color: var(--va-text-secondary);
}
</style>
