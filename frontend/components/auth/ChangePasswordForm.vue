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
      :rules="[v => !!v || '필수 항목입니다']"
    />
    <va-input 
      v-model="newPassword" 
      :label="$t('new_password')" 
      type="password" 
      outline 
      :rules="[v => !!v || '필수 항목입니다', v => v.length >= 8 || '8자 이상 입력해주세요']"
    />
    <va-input 
      v-model="confirmPassword" 
      :label="$t('confirm_new_password')" 
      type="password" 
      outline 
      :rules="[v => !!v || '필수 항목입니다', v => v === newPassword || '비밀번호가 일치하지 않습니다']"
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
const token = useCookie('auth_token')
const userCookie = useCookie('user_data')

const handleSubmit = async () => {
  if (!oldPassword.value || !newPassword.value || !confirmPassword.value) {
    errorMessage.value = '모든 필드를 입력해주세요.'
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    errorMessage.value = '새 비밀번호가 일치하지 않습니다.'
    return
  }
  if (newPassword.value.length < 8) {
    errorMessage.value = '새 비밀번호는 8자 이상이어야 합니다.'
    return
  }
  
  isSubmitting.value = true
  errorMessage.value = ''
  
  try {
    await $fetch('/api/users/me/password', {
      method: 'PUT',
      headers: { Authorization: `Bearer ${token.value}` },
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
    errorMessage.value = e.response?._data || e.message || '비밀번호 변경에 실패했습니다.'
  } finally {
    isSubmitting.value = false
  }
}
</script>
