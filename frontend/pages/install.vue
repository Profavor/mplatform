<template>
  <div v-if="isMounted" class="install-container fade-in">
    <!-- Animated Ambient Background Glowing Spheres -->
    <div class="ambient-glow glow-1"></div>
    <div class="ambient-glow glow-2"></div>

    <!-- Top Right Language Switcher -->
    <div class="lang-switcher">
      <va-button-toggle
        v-model="currentLocale"
        preset="secondary"
        size="small"
        :options="[
          { label: '한국어', value: 'ko' },
          { label: 'English', value: 'en' }
        ]"
        @update:model-value="changeLocale"
      />
    </div>

    <div class="install-card-wrapper">
      <div class="install-card">
        <!-- Header -->
        <div class="card-header">
          <div class="logo-wrapper">
            <va-icon name="rocket_launch" size="2.5rem" color="primary" />
          </div>
          <h1 class="title">{{ t('install_title') }}</h1>
          <p class="subtitle">{{ t('install_subtitle') }}</p>
        </div>

        <!-- Form Stepper Indicator -->
        <div class="stepper-bar">
          <div class="step-item" :class="{ active: currentStep === 1, completed: currentStep > 1 }">
            <span class="step-num">1</span>
            <span class="step-label">{{ t('install_step1_label') }}</span>
          </div>
          <div class="step-line" :class="{ active: currentStep > 1 }"></div>
          <div class="step-item" :class="{ active: currentStep === 2, completed: isInstalledSuccess }">
            <span class="step-num">2</span>
            <span class="step-label">{{ t('install_step2_label') }}</span>
          </div>
        </div>

        <!-- Alert Error Message -->
        <va-alert v-if="errorMessage" color="danger" dense class="mb-4">
          <va-icon name="error" class="mr-2" />
          {{ errorMessage }}
        </va-alert>

        <!-- Step 1 Form: Organization Setup -->
        <div v-if="currentStep === 1" class="step-content">
          <div class="form-grid mb-4">
            <div class="form-group">
              <label class="form-label">
                <va-icon name="corporate_fare" size="small" class="mr-1" />
                {{ t('install_org_ko') }}
              </label>

              <va-input
                v-model="form.organizationNameKo"
                :placeholder="t('install_org_ko_placeholder')"
                class="w-full"
                :rules="[(v) => !!v || t('install_require_org_ko')]"
              />
            </div>

            <div class="form-group">
              <label class="form-label">
                <va-icon name="language" size="small" class="mr-1" />
                {{ t('install_org_en') }}
              </label>

              <va-input
                v-model="form.organizationNameEn"
                :placeholder="t('install_org_en_placeholder')"
                class="w-full"
                :rules="[(v) => !!v || t('install_require_org_en')]"
              />
            </div>
          </div>
          <span class="input-tip mb-4 block">{{ t('install_org_tip') }}</span>

          <div class="actions">
            <va-button
              size="large"
              color="primary"
              class="w-full shadow-button"
              :disabled="!form.organizationNameKo.trim()"
              @click="currentStep = 2"
            >
              {{ t('install_btn_next') }}
              <va-icon name="arrow_forward" class="ml-2" />
            </va-button>
          </div>
        </div>

        <!-- Step 2 Form: Super Admin Account Setup -->
        <div v-else-if="currentStep === 2" class="step-content">
          <div class="form-group mb-3">
            <label class="form-label">
              <va-icon name="account_circle" size="small" class="mr-1" />
              {{ t('install_admin_username') }}
            </label>
            <va-input
              v-model="form.adminUsername"
              placeholder="superadmin"
              class="w-full"
              :rules="[(v) => !!v || t('install_require_username')]"
            />
          </div>

          <div class="form-grid mb-4">
            <div class="form-group">
              <label class="form-label">
                <va-icon name="lock" size="small" class="mr-1" />
                {{ t('install_admin_pwd') }}
              </label>
              <va-input
                v-model="form.adminPassword"
                type="password"
                placeholder="******"
                class="w-full"
                :rules="[(v) => (v && v.length >= 6) || t('install_require_pwd_len')]"
              />
            </div>

            <div class="form-group">
              <label class="form-label">
                <va-icon name="check_circle" size="small" class="mr-1" />
                {{ t('install_admin_pwd_confirm') }}
              </label>
              <va-input
                v-model="form.confirmPassword"
                type="password"
                placeholder="******"
                class="w-full"
                :rules="[(v) => v === form.adminPassword || t('install_require_pwd_match')]"
              />
            </div>
          </div>

          <div class="actions flex gap-3">
            <va-button
              preset="secondary"
              size="large"
              class="w-1/3"
              @click="currentStep = 1"
            >
              <va-icon name="arrow_back" class="mr-1" />
              {{ t('install_btn_prev') }}
            </va-button>

            <va-button
              size="large"
              color="primary"
              class="w-2/3 shadow-button"
              :loading="isSubmitting"
              :disabled="!isFormValid"
              @click="submitInstall"
            >
              {{ t('install_btn_submit') }}
              <va-icon name="task_alt" class="ml-2" />
            </va-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from '#app'
import { useI18n } from 'vue-i18n'

definePageMeta({
  layout: false
})

const isMounted = ref(false)
onMounted(() => {
  isMounted.value = true
})

const { t, locale } = useI18n()
const currentLocale = ref(locale.value || 'ko')

const changeLocale = (val: string) => {
  locale.value = val
  currentLocale.value = val
}

const router = useRouter()

const currentStep = ref(1)
const isSubmitting = ref(false)
const isInstalledSuccess = ref(false)
const errorMessage = ref('')

const form = ref({
  organizationNameKo: '',
  organizationNameEn: '',
  adminUsername: '',
  adminPassword: '',
  confirmPassword: '',
  timezone: 'Asia/Seoul'
})

const isFormValid = computed(() => {
  return (
    form.value.organizationNameKo.trim() !== '' &&
    form.value.adminUsername.trim() !== '' &&
    form.value.adminPassword.length >= 6 &&
    form.value.adminPassword === form.value.confirmPassword
  )
})

const submitInstall = async () => {
  if (!isFormValid.value) return

  isSubmitting.value = true
  errorMessage.value = ''

  try {
    const res: any = await $fetch('/api/system/install', {
      method: 'POST',
      body: {
        organizationNameKo: form.value.organizationNameKo.trim(),
        organizationNameEn: form.value.organizationNameEn.trim(),
        adminUsername: form.value.adminUsername.trim(),
        adminPassword: form.value.adminPassword,
        timezone: form.value.timezone
      }
    })

    if (res && res.token) {
      isInstalledSuccess.value = true

      setTimeout(() => {
        router.push('/login')
      }, 500)
    }
  } catch (e: any) {
    console.error('Install Failed:', e)
    const errText = e.data?.error || e.message || '시스템 설치 중 오류가 발생했습니다.'
    errorMessage.value = errText

    if (typeof errText === 'string' && errText.includes('이미 설치가 완료된 시스템')) {
      setTimeout(() => {
        router.push('/login')
      }, 1200)
    }
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.install-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #0f172a;
  color: #f8fafc;
  position: relative;
  overflow: hidden;
  font-family: 'Inter', sans-serif;
}

.fade-in {
  animation: fadeIn 0.35s ease-out forwards;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.99);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.lang-switcher {
  position: absolute;
  top: 1.5rem;
  right: 1.5rem;
  z-index: 20;
}

.ambient-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(120px);
  opacity: 0.45;
  pointer-events: none;
}

.glow-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #3b82f6 0%, rgba(59, 130, 246, 0) 70%);
  top: -100px;
  left: -100px;
}

.glow-2 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, #8b5cf6 0%, rgba(139, 92, 246, 0) 70%);
  bottom: -150px;
  right: -150px;
}

.install-card-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 650px;
  padding: 1.5rem;
}

.install-card {
  background: rgba(30, 41, 59, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 20px;
  padding: 2.5rem;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

.card-header {
  text-align: center;
  margin-bottom: 2rem;
}

.logo-wrapper {
  width: 70px;
  height: 70px;
  margin: 0 auto 1rem auto;
  border-radius: 20px;
  background: rgba(59, 130, 246, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.title {
  font-size: 1.85rem;
  font-weight: 800;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, #ffffff 0%, #cbd5e1 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 0.5rem;
}

.subtitle {
  color: #94a3b8;
  font-size: 0.95rem;
}

.stepper-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 2rem;
  padding: 0.75rem 1rem;
  background: rgba(15, 23, 42, 0.5);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.step-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  opacity: 0.5;
  transition: all 0.3s ease;
}

.step-item.active,
.step-item.completed {
  opacity: 1;
}

.step-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.85rem;
}

.step-item.active .step-num {
  background: #3b82f6;
  color: #ffffff;
  box-shadow: 0 0 12px rgba(59, 130, 246, 0.6);
}

.step-item.completed .step-num {
  background: #10b981;
  color: #ffffff;
}

.step-label {
  font-size: 0.88rem;
  font-weight: 600;
}

.step-line {
  flex: 1;
  height: 2px;
  background: rgba(255, 255, 255, 0.1);
  margin: 0 1rem;
  max-width: 60px;
}

.step-line.active {
  background: #3b82f6;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-label {
  font-size: 0.88rem;
  font-weight: 600;
  color: #cbd5e1;
  margin-bottom: 0.4rem;
  display: flex;
  align-items: center;
}

.input-tip {
  font-size: 0.8rem;
  color: #64748b;
  margin-top: 0.4rem;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.shadow-button {
  box-shadow: 0 10px 20px -5px rgba(59, 130, 246, 0.4);
}

@media (max-width: 640px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
