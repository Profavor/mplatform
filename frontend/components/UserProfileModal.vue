<template>
  <AppModal
    :model-value="modelValue"
    :title="t('user_profile_title')"
    icon="badge"
    size="small"
    hide-default-actions
    class="user-profile-modal"
    @update:model-value="$emit('update:modelValue', $event)"
  >

    <div v-if="userProfile" style="padding: 1rem 0; display: flex; flex-direction: column; gap: 1rem;">
      <!-- Profile Header Card -->
      <div style="display: flex; align-items: center; gap: 1.25rem; padding: 1.2rem; background: var(--va-background-element); border-radius: 12px; border: 1px solid var(--va-background-border); box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);">
        <va-avatar color="primary" size="large" style="font-weight: 900; font-size: 1.4rem; min-width: 56px; min-height: 56px; box-shadow: 0 4px 10px rgba(44, 130, 224, 0.25);">
          {{ (userProfile.username || 'U')[0].toUpperCase() }}
        </va-avatar>
        <div style="flex: 1;">
          <div style="font-size: 1.45rem; font-weight: 900; color: var(--va-text-primary); letter-spacing: -0.4px; line-height: 1.25;">
            {{ userProfile.username || '-' }}
          </div>
          <div style="font-size: 0.85rem; color: var(--va-text-secondary); margin-top: 0.4rem; display: flex; align-items: center; gap: 0.6rem; flex-wrap: wrap;">
            <RoleBadge :value="userProfile.role || 'USER'" :hide-code="true" />
            <span v-if="userProfile.timezone" style="display: inline-flex; align-items: center; gap: 3px; font-weight: 600;">
              🌐 {{ userProfile.timezone }}
            </span>
          </div>
        </div>
      </div>

      <!-- Organization Info Section -->
      <div style="display: flex; flex-direction: column; gap: 0.75rem; border: 1px solid var(--va-background-border); border-radius: 8px; padding: 1rem; background: var(--va-background-secondary);">
        <div style="font-size: 0.88rem; font-weight: 800; color: var(--va-primary); display: flex; align-items: center; gap: 0.4rem;">
          <va-icon name="corporate_fare" size="small" color="primary" />
          <span>{{ t('org_info_title') }}</span>
        </div>
        <div style="display: grid; grid-template-columns: minmax(165px, max-content) 1fr; row-gap: 0.6rem; font-size: 0.88rem; align-items: center;">
          <template v-if="userProfile.organizationName && formatLocalizedText(userProfile.organizationName) !== '-'">
            <span style="color: var(--va-text-secondary); font-weight: 600;">🏢 {{ t('company_org') }}:</span>
            <span style="font-weight: 700; color: var(--va-text-primary);">{{ formatLocalizedText(userProfile.organizationName) }}</span>
          </template>

          <template v-if="userProfile.departmentName && formatLocalizedText(userProfile.departmentName) !== '-'">
            <span style="color: var(--va-text-secondary); font-weight: 600;">🏬 {{ t('department') }}:</span>
            <span style="font-weight: 700; color: var(--va-text-primary);">{{ formatLocalizedText(userProfile.departmentName) }}</span>
          </template>

          <template v-if="userProfile.teamName && formatLocalizedText(userProfile.teamName) !== '-'">
            <span style="color: var(--va-text-secondary); font-weight: 600;">👥 {{ t('team') }}:</span>
            <span style="font-weight: 700; color: var(--va-text-primary);">{{ formatLocalizedText(userProfile.teamName) }}</span>
          </template>

          <template v-if="userProfile.assignedAt">
            <span style="color: var(--va-text-secondary); font-weight: 600;">📅 {{ t('assigned_at') }}:</span>
            <span style="font-weight: 700; color: var(--va-text-primary);">{{ formatDate(userProfile.assignedAt) }}</span>
          </template>
        </div>
      </div>

      <!-- Security & 2FA Section -->
      <div style="display: flex; flex-direction: column; gap: 0.75rem; border: 1px solid var(--va-background-border); border-radius: 8px; padding: 1rem; background: var(--va-background-secondary);">
        <div style="display: flex; align-items: center; justify-content: space-between;">
          <div style="font-size: 0.88rem; font-weight: 800; color: var(--va-primary); display: flex; align-items: center; gap: 0.4rem;">
            <va-icon name="security" size="small" color="primary" />
            <span>{{ t('two_factor_title') }}</span>
          </div>
          <va-badge
            :color="is2FaEnabled ? 'success' : 'secondary'"
            :text="is2FaEnabled ? t('two_factor_enabled_badge') : t('two_factor_disabled_badge')"
          />
        </div>
        <div style="display: flex; align-items: center; justify-content: space-between; gap: 0.5rem;">
          <span style="font-size: 0.82rem; color: var(--va-text-secondary);">{{ t('two_factor_desc') }}</span>
          <va-button size="small" preset="secondary" @click="is2FaModalOpen = true">
            {{ t('two_factor_setup_title') }}
          </va-button>
        </div>
      </div>
    </div>

    <!-- 2FA Setup / Management Modal -->
    <TwoFactorSetupModal
      v-model:is-open="is2FaModalOpen"
      :username="userProfile?.username || ''"
      :is-already-enabled="is2FaEnabled"
      @enabled="handle2FaStatusChange"
      @disabled="handle2FaStatusChange"
    />

    <template #footer>
      <div style="display: flex; justify-content: flex-end; width: 100%; margin-top: 0.5rem;">
        <va-button color="secondary" preset="solid" @click="$emit('update:modelValue', false)">
          {{ t('close') }}
        </va-button>
      </div>
    </template>
  </AppModal>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import AppModal from '~/components/common/AppModal.vue'
import TwoFactorSetupModal from '~/components/auth/TwoFactorSetupModal.vue'
import { useCustomFetch } from '~/composables/useCustomFetch'

const { t, locale } = useI18n()
const { customFetch } = useCustomFetch()

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  userProfile: { type: Object, default: null }
})

defineEmits(['update:modelValue'])

const is2FaModalOpen = ref(false)
const is2FaEnabled = ref(false)

const load2FaStatus = async () => {
  if (!props.userProfile?.username) return
  try {
    const resp = await customFetch('/api/auth/2fa/status', {
      params: { username: props.userProfile.username }
    })
    is2FaEnabled.value = Boolean(resp?.twoFactorEnabled)
  } catch (e) {
    // ignore
  }
}

watch(() => props.modelValue, (isOpen) => {
  if (isOpen) {
    load2FaStatus()
  }
})

const handle2FaStatusChange = () => {
  load2FaStatus()
}

const formatLocalizedText = (val) => {
  if (!val) return '-'
  let obj = val
  if (typeof val === 'string') {
    const trimmed = val.trim()
    if (trimmed.startsWith('{') && trimmed.endsWith('}')) {
      try {
        obj = JSON.parse(trimmed)
      } catch (e) {
        return val
      }
    } else {
      return val
    }
  }
  if (typeof obj === 'object' && obj !== null) {
    const currentLang = locale.value || 'ko'
    return obj[currentLang] || obj['ko'] || obj['en'] || Object.values(obj)[0] || '-'
  }
  return String(val)
}

const formatDate = (dateVal) => {
  if (!dateVal) return '-'
  let str = String(dateVal).trim()
  const tz = useCookie('timezone', { default: () => 'Asia/Seoul' }).value || 'Asia/Seoul'
  const currentLoc = (locale?.value || 'ko') === 'en' ? 'en-US' : 'ko-KR'

  if (/^\d+$/.test(str)) {
    const d = new Date(parseInt(str, 10))
    return d.toLocaleString(currentLoc, {
      timeZone: tz,
      year: 'numeric',
      month: 'numeric',
      day: 'numeric',
      hour: 'numeric',
      minute: 'numeric',
      second: 'numeric',
      hour12: true
    })
  }
  if (!str.endsWith('Z') && !str.includes('+') && !/[-+]\d{2}:\d{2}$/.test(str)) {
    if (str.includes(' ') && !str.includes('T')) {
      str = str.replace(' ', 'T')
    }
    const serverOffset = useCookie('server_offset', { default: () => '+09:00' }).value
    str += serverOffset
  }
  const d = new Date(str)
  if (isNaN(d.getTime())) return String(dateVal)
  return d.toLocaleString(currentLoc, {
    timeZone: tz,
    year: 'numeric',
    month: 'numeric',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric',
    second: 'numeric',
    hour12: true
  })
}
</script>
