<template>
  <va-modal
    :model-value="isOpen"
    no-outside-dismiss
    no-esc-dismiss
    hide-default-actions
    size="medium"
    @update:model-value="$emit('update:isOpen', $event)"
  >
    <div class="two-factor-modal-container">
      <div class="modal-header-section">
        <div class="header-icon-wrapper">
          <va-icon name="security" size="32px" color="primary" />
        </div>
        <h3 class="modal-title">{{ $t('two_factor_title') }}</h3>
        <p class="modal-subtitle">{{ $t('two_factor_desc') }}</p>
      </div>

      <!-- 유예 기간 안내 배너 -->
      <div v-if="gracePeriodRemainingDays && gracePeriodRemainingDays > 0" class="grace-period-banner mb-3">
        <va-alert color="warning" outline class="w-full">
          <template #icon>
            <va-icon name="warning_amber" />
          </template>
          <div class="d-flex justify-space-between align-center w-full">
            <span>{{ $t('two_factor_grace_banner', { days: gracePeriodRemainingDays }) }}</span>
            <va-button
              preset="secondary"
              size="small"
              color="warning"
              class="ml-2"
              @click="handleSkip"
            >
              {{ $t('two_factor_grace_skip') }}
            </va-button>
          </div>
        </va-alert>
      </div>

      <!-- 인증 수단 탭 -->
      <va-tabs v-model="activeTab" grow class="auth-tabs mb-4">
        <template #tabs>
          <va-tab name="TOTP">
            <va-icon name="phonelink_lock" size="18px" class="mr-1" />
            {{ $t('two_factor_totp_tab') }}
          </va-tab>
          <va-tab name="EMAIL">
            <va-icon name="email" size="18px" class="mr-1" />
            {{ $t('two_factor_email_tab') }}
          </va-tab>
          <va-tab name="BACKUP_CODE">
            <va-icon name="key" size="18px" class="mr-1" />
            {{ $t('two_factor_backup_tab') }}
          </va-tab>
        </template>
      </va-tabs>

      <!-- 에러 메시지 알림 -->
      <va-alert v-if="errorMessage" color="danger" class="mb-3">
        {{ $t(errorMessage) }}
      </va-alert>

      <!-- 이메일 인증 발송 성공 알림 -->
      <va-alert v-if="emailSentMessage" color="info" class="mb-3">
        {{ emailSentMessage }}
      </va-alert>

      <!-- 탭별 입력 내용 -->
      <div class="tab-content">
        <!-- 이메일 탭 안내 -->
        <div v-if="activeTab === 'EMAIL'" class="email-info-box mb-3">
          <div class="text-sm text-secondary mb-2">
            <span>{{ maskedEmail || username }}</span>
          </div>
          <va-button
            preset="primary"
            size="small"
            :loading="isSendingEmail"
            @click="handleSendEmailOtp"
          >
            {{ isEmailSent ? $t('two_factor_resend_email_btn') : $t('two_factor_send_email_btn') }}
          </va-button>
        </div>

        <!-- 백업 코드 탭 경고 안내 -->
        <div v-if="activeTab === 'BACKUP_CODE'" class="mb-3 text-secondary text-xs">
          {{ $t('two_factor_backup_warning') }}
        </div>

        <!-- 인증 코드 입력 필드 -->
        <va-input
          v-model="verificationCode"
          class="w-full mb-4 code-input"
          :placeholder="activeTab === 'BACKUP_CODE' ? $t('two_factor_backup_placeholder') : $t('two_factor_code_placeholder')"
          :maxlength="activeTab === 'BACKUP_CODE' ? 12 : 6"
          autocomplete="one-time-code"
          @keyup.enter="handleVerify"
        />

        <!-- 확인 버튼 -->
        <va-button
          class="w-full luxury-btn"
          size="large"
          :loading="isVerifying"
          :disabled="isVerifying || !verificationCode.trim()"
          @click="handleVerify"
        >
          {{ isVerifying ? $t('two_factor_verifying') : $t('two_factor_verify_btn') }}
        </va-button>
      </div>
    </div>
  </va-modal>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCookie } from '#app'
import { useCustomFetch } from '~/composables/useCustomFetch'

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  },
  username: {
    type: String,
    required: true
  },
  tempToken: {
    type: String,
    required: true
  },
  maskedEmail: {
    type: String,
    default: ''
  },
  gracePeriodRemainingDays: {
    type: Number,
    default: null
  },
  twoFactorType: {
    type: String,
    default: 'TOTP'
  }
})

const emit = defineEmits(['update:isOpen', 'verified', 'skip'])

const { t } = useI18n()
const { customFetch } = useCustomFetch()

const authTokenCookie = useCookie('auth_token')
const refreshTokenCookie = useCookie('refresh_token')

const activeTab = ref(props.twoFactorType === 'EMAIL' ? 'EMAIL' : 'TOTP')
const verificationCode = ref('')
const isVerifying = ref(false)
const isSendingEmail = ref(false)
const isEmailSent = ref(false)
const errorMessage = ref('')
const emailSentMessage = ref('')

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    verificationCode.value = ''
    errorMessage.value = ''
    emailSentMessage.value = ''
    isEmailSent.value = false
    activeTab.value = props.twoFactorType === 'EMAIL' ? 'EMAIL' : 'TOTP'
  }
})

const handleSendEmailOtp = async () => {
  if (isSendingEmail.value) return
  isSendingEmail.value = true
  errorMessage.value = ''
  emailSentMessage.value = ''

  try {
    const resp = await customFetch('/api/auth/2fa/send-email', {
      method: 'POST',
      body: {
        username: props.username,
        tempToken: props.tempToken
      }
    })
    isEmailSent.value = true
    emailSentMessage.value = t('two_factor_email_sent')
  } catch (err) {
    errorMessage.value = 'two_factor_invalid_code'
  } finally {
    isSendingEmail.value = false
  }
}

const handleVerify = async () => {
  if (isVerifying.value || !verificationCode.value.trim()) return
  isVerifying.value = true
  errorMessage.value = ''

  try {
    const resp = await customFetch('/api/auth/2fa/verify', {
      method: 'POST',
      body: {
        username: props.username,
        tempToken: props.tempToken,
        code: verificationCode.value.trim(),
        type: activeTab.value
      }
    })

    if (resp?.token) {
      authTokenCookie.value = resp.token
    }
    if (resp?.refreshToken) {
      refreshTokenCookie.value = resp.refreshToken
    }

    emit('verified', resp)
    emit('update:isOpen', false)
  } catch (err) {
    errorMessage.value = 'two_factor_invalid_code'
  } finally {
    isVerifying.value = false
  }
}

const handleSkip = () => {
  emit('skip')
  emit('update:isOpen', false)
}

defineExpose({
  activeTab,
  verificationCode,
  errorMessage,
  handleVerify,
  handleSendEmailOtp,
  handleSkip
})
</script>

<style scoped>
.two-factor-modal-container {
  padding: 0.5rem 0.25rem;
}
.modal-header-section {
  text-align: center;
  margin-bottom: 1.25rem;
}
.header-icon-wrapper {
  margin-bottom: 0.5rem;
}
.modal-title {
  font-size: 1.25rem;
  font-weight: 700;
  margin-bottom: 0.25rem;
}
.modal-subtitle {
  font-size: 0.875rem;
  color: var(--va-text-secondary);
}
.code-input :deep(input) {
  text-align: center;
  font-size: 1.25rem;
  letter-spacing: 4px;
  font-weight: 600;
}
.email-info-box {
  background: var(--va-background-element);
  border-radius: 8px;
  padding: 0.75rem 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
