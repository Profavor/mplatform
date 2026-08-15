<template>
  <div style="display: flex; flex-direction: column; gap: 1rem;">
    <va-alert v-if="forceMode" color="warning" class="mb-4 text-sm" outline>
      <template #icon><va-icon name="warning" /></template>
      {{ $t('force_password_change_desc') }}
    </va-alert>
    
    <va-input 
      v-model="oldPassword" 
      :label="$t('old_password')" 
      type="password" 
      outline 
      :rules="[v => !!v || t('required_field')]"
    />
    <va-input 
      v-model="newPassword" 
      :label="$t('new_password')" 
      type="password" 
      outline 
      :rules="[v => !!v || t('required_field'), v => (v && v.length >= 8) || t('min_8_chars')]"
    />
    <va-input 
      v-model="confirmPassword" 
      :label="$t('confirm_new_password')" 
      type="password" 
      outline 
      :rules="[v => !!v || t('required_field'), v => v === newPassword || t('passwords_do_not_match')]"
    />
    
    <va-alert v-if="errorMessage" color="danger" class="mb-4 text-sm" outline>
      {{ errorMessage }}
    </va-alert>

    <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem;">
      <va-button v-if="!forceMode" preset="secondary" @click="$emit('cancel')">{{ $t('cancel') }}</va-button>
      <va-button color="primary" @click="handleSubmit" :loading="isSubmitting">{{ $t('change_password') }}</va-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useCookie } from '#app'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'

const { t } = useI18n()

const props = defineProps({
  forceMode: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['success', 'cancel'])

const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const isSubmitting = ref(false)
const errorMessage = ref('')
const userCookie = useCookie('user_data')
const { customFetch } = useCustomFetch()

const handleSubmit = async () => {
  if (!oldPassword.value || !newPassword.value || !confirmPassword.value) {
    errorMessage.value = t('fill_all_fields')
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    errorMessage.value = t('passwords_do_not_match')
    return
  }
  if (newPassword.value.length < 8) {
    errorMessage.value = t('min_8_chars')
    return
  }
  
  isSubmitting.value = true
  errorMessage.value = ''
  
  try {
    await customFetch('/api/users/me/password', {
      method: 'PUT',
      body: {
        oldPassword: oldPassword.value,
        newPassword: newPassword.value
      }
    })
    
    if (userCookie.value) {
      try {
        const u = typeof userCookie.value === 'string' ? JSON.parse(userCookie.value) : userCookie.value
        u.mustChangePassword = false
        userCookie.value = JSON.stringify(u)
      } catch (e) {}
    }
    
    emit('success')
  } catch (e) {
    errorMessage.value = e.response?._data || e.message || t('password_change_failed')
  } finally {
    isSubmitting.value = false
  }
}
</script>
