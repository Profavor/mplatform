<template>
  <va-modal
    :model-value="isOpen"
    size="medium"
    hide-default-actions
    @update:model-value="$emit('update:isOpen', $event)"
  >
    <div class="two-factor-setup-modal">
      <div class="modal-header-section mb-3">
        <div class="d-flex align-center justify-center gap-2 mb-1">
          <va-icon name="enhanced_encryption" size="32px" color="primary" />
          <h3 class="modal-title mb-0">{{ $t('two_factor_setup_title') }}</h3>
        </div>
        <div class="d-flex justify-center mt-2">
          <va-badge
            :color="isAlreadyEnabled ? 'success' : 'secondary'"
            :text="isAlreadyEnabled ? $t('two_factor_enabled_badge') : $t('two_factor_disabled_badge')"
          />
        </div>
      </div>

      <!-- 에러 알림 -->
      <va-alert v-if="errorMessage" color="danger" class="mb-3">
        {{ $t(errorMessage) }}
      </va-alert>

      <!-- 복사 완료 알림 -->
      <va-alert v-if="copySuccessMessage" color="success" class="mb-3">
        {{ copySuccessMessage }}
      </va-alert>

      <!-- 이미 활성화된 상태에서의 비활성화 뷰 -->
      <div v-if="isAlreadyEnabled && step === 1" class="already-enabled-section text-center py-3">
        <p class="text-secondary mb-4">{{ $t('two_factor_desc') }}</p>
        <va-button
          color="danger"
          preset="secondary"
          :loading="isDisabling"
          @click="handleDisable"
        >
          {{ $t('two_factor_disable_btn') }}
        </va-button>
      </div>

      <!-- 신규 활성화 단계 1 & 2 -->
      <div v-else-if="step <= 2" class="setup-steps-section">
        <!-- Step 1: QR 코드 스캔 -->
        <div class="step-box mb-3">
          <div class="step-title font-bold text-sm mb-2">{{ $t('two_factor_setup_step1') }}</div>
          <div v-if="isLoadingSetup" class="d-flex justify-center py-4">
            <va-progress-circle indeterminate size="24px" color="primary" />
          </div>
          <div v-else class="qr-code-wrapper text-center my-2">
            <img v-if="qrCodeUrl" :src="qrCodeUrl" alt="2FA QR Code" class="qr-image" />
            <div class="manual-key-box mt-2">
              <span class="text-xs text-secondary">{{ $t('two_factor_setup_step1_manual') }}</span>
              <code class="secret-key-display">{{ secret }}</code>
            </div>
          </div>
        </div>

        <!-- Step 2: 코드 검증 -->
        <div class="step-box mb-3">
          <div class="step-title font-bold text-sm mb-2">{{ $t('two_factor_setup_step2') }}</div>
          <va-input
            v-model="verificationCode"
            class="w-full mb-3 code-input"
            :placeholder="$t('two_factor_code_placeholder')"
            maxlength="6"
            autocomplete="one-time-code"
            @keyup.enter="handleEnable"
          />
          <va-button
            class="w-full luxury-btn"
            size="large"
            :loading="isEnabling"
            :disabled="isEnabling || !verificationCode.trim()"
            @click="handleEnable"
          >
            {{ $t('two_factor_enable_btn') }}
          </va-button>
        </div>
      </div>

      <!-- Step 3: 백업 코드 표시 및 저장 안내 -->
      <div v-else-if="step === 3" class="backup-codes-section py-2">
        <div class="step-title font-bold text-sm mb-2">{{ $t('two_factor_setup_step3') }}</div>
        <va-alert color="warning" outline class="mb-3 text-xs">
          {{ $t('two_factor_backup_warning') }}
        </va-alert>

        <div class="backup-codes-grid mb-3">
          <div v-for="(code, idx) in backupCodes" :key="idx" class="backup-code-item">
            <code>{{ code }}</code>
          </div>
        </div>

        <div class="d-flex gap-2">
          <va-button
            preset="secondary"
            class="flex-1"
            icon="content_copy"
            @click="copyBackupCodes"
          >
            {{ $t('two_factor_backup_copy_btn') }}
          </va-button>
          <va-button
            class="flex-1"
            color="primary"
            @click="handleFinish"
          >
            {{ $t('btn_login') || '확인' }}
          </va-button>
        </div>
      </div>
    </div>
  </va-modal>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
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
  isAlreadyEnabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:isOpen', 'enabled', 'disabled'])

const { t } = useI18n()
const { customFetch } = useCustomFetch()

const step = ref(1)
const secret = ref('')
const otpAuthUrl = ref('')
const qrCodeUrl = ref('')
const verificationCode = ref('')
const backupCodes = ref([])
const isLoadingSetup = ref(false)
const isEnabling = ref(false)
const isDisabling = ref(false)
const errorMessage = ref('')
const copySuccessMessage = ref('')

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    step.value = 1
    verificationCode.value = ''
    backupCodes.value = []
    errorMessage.value = ''
    copySuccessMessage.value = ''
    if (!props.isAlreadyEnabled) {
      loadSetupData()
    }
  }
})

const loadSetupData = async () => {
  isLoadingSetup.value = true
  errorMessage.value = ''
  try {
    const resp = await customFetch('/api/auth/2fa/setup', {
      method: 'POST',
      params: { username: props.username }
    })
    secret.value = resp.secret || ''
    otpAuthUrl.value = resp.otpAuthUrl || ''
    qrCodeUrl.value = resp.qrCodeUrl || ''
  } catch (err) {
    errorMessage.value = 'two_factor_invalid_code'
  } finally {
    isLoadingSetup.value = false
  }
}

const handleEnable = async () => {
  if (isEnabling.value || !verificationCode.value.trim()) return
  isEnabling.value = true
  errorMessage.value = ''

  try {
    const resp = await customFetch('/api/auth/2fa/enable', {
      method: 'POST',
      params: { username: props.username },
      body: {
        secret: secret.value,
        code: verificationCode.value.trim(),
        type: 'TOTP'
      }
    })

    backupCodes.value = resp.backupCodes || []
    step.value = 3
    emit('enabled')
  } catch (err) {
    errorMessage.value = 'two_factor_invalid_code'
  } finally {
    isEnabling.value = false
  }
}

const handleDisable = async () => {
  if (isDisabling.value) return
  isDisabling.value = true
  errorMessage.value = ''

  try {
    await customFetch('/api/auth/2fa/disable', {
      method: 'POST',
      params: { username: props.username }
    })
    emit('disabled')
    emit('update:isOpen', false)
  } catch (err) {
    errorMessage.value = 'two_factor_invalid_code'
  } finally {
    isDisabling.value = false
  }
}

const copyBackupCodes = async () => {
  if (!backupCodes.value || backupCodes.value.length === 0) return
  const text = backupCodes.value.join('\n')
  try {
    if (typeof navigator !== 'undefined' && navigator.clipboard) {
      await navigator.clipboard.writeText(text)
    }
    copySuccessMessage.value = t('two_factor_backup_copied')
    setTimeout(() => { copySuccessMessage.value = '' }, 3000)
  } catch (e) {
    // fallback
  }
}

const handleFinish = () => {
  emit('update:isOpen', false)
}

defineExpose({
  step,
  secret,
  qrCodeUrl,
  verificationCode,
  backupCodes,
  loadSetupData,
  handleEnable,
  handleDisable,
  copyBackupCodes
})
</script>

<style scoped>
.two-factor-setup-modal {
  padding: 0.5rem 0.25rem;
}
.modal-header-section {
  text-align: center;
}
.modal-title {
  font-size: 1.25rem;
  font-weight: 700;
}
.qr-image {
  width: 160px;
  height: 160px;
  margin: 0 auto;
  border-radius: 8px;
  border: 1px solid var(--va-background-border);
}
.manual-key-box {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}
.secret-key-display {
  background: var(--va-background-element);
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-family: monospace;
  font-weight: 700;
  letter-spacing: 2px;
}
.backup-codes-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.5rem;
  background: var(--va-background-element);
  padding: 1rem;
  border-radius: 8px;
}
.backup-code-item {
  text-align: center;
  font-family: monospace;
  font-size: 1rem;
  font-weight: 700;
  letter-spacing: 1px;
}
.code-input :deep(input) {
  text-align: center;
  font-size: 1.25rem;
  letter-spacing: 4px;
  font-weight: 600;
}
</style>
