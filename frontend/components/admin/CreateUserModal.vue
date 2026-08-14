<template>
  <va-modal
    :model-value="modelValue"
    :title="t('create_user', '사용자 생성')"
    hide-default-actions
    @update:model-value="val => emit('update:modelValue', val)"
  >
    <div style="padding: 1rem; min-width: 400px; display: flex; flex-direction: column; gap: 1.25rem;">
      <va-input
        v-model="newUser.username"
        :label="t('label_username', '사용자 아이디')"
        outline
        @update:model-value="emit('username-changed')"
        :success="isUsernameChecked && checkedUsername === newUser.username"
      >
        <template #appendInner>
          <va-button
            size="small"
            color="primary"
            preset="secondary"
            :loading="isCheckingUsername"
            :disabled="!newUser.username || (isUsernameChecked && checkedUsername === newUser.username)"
            style="white-space: nowrap;"
            @click="onCheckDuplicate"
          >
            {{ t('check_duplicate', '중복 확인') }}
          </va-button>
        </template>
      </va-input>

      <UserRoleSelect v-model="newUser.role" :label="t('user_role', '사용자 역할')" />

      <va-select
        v-model="newUser.organizationId"
        :options="organizations"
        value-by="id"
        :text-by="o => getI18nText(o.displayName) || o.name"
        :label="t('organization', '소속 조직')"
        clearable
        outline
      />

      <va-select
        v-model="newUser.departmentId"
        :options="departments"
        value-by="id"
        text-by="name"
        :label="t('department', '소속 부서')"
        clearable
        outline
        :disabled="!newUser.organizationId"
      />
      
      <div style="display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1rem;">
        <va-button preset="secondary" @click="emit('update:modelValue', false)">
          {{ t('cancel', '취소') }}
        </va-button>
        <va-button color="success" :loading="isCreatingUser" @click="onCreate">
          {{ t('create', '생성') }}
        </va-button>
      </div>
    </div>
  </va-modal>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import UserRoleSelect from '~/components/UserRoleSelect.vue'

const { t, locale } = useI18n()

const props = defineProps<{
  modelValue: boolean
  newUser: any
  organizations: any[]
  departments: any[]
  isUsernameChecked: boolean
  checkedUsername: string
  isCheckingUsername: boolean
  isCreatingUser: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'username-changed'): void
  (e: 'check-username'): void
  (e: 'create'): void
}>()

const getI18nText = (nameObj: any) => {
  if (!nameObj) return ''
  if (typeof nameObj === 'object') {
    const current = locale.value || 'ko'
    return nameObj[current] || nameObj.ko || nameObj.en || ''
  }
  return String(nameObj)
}

const onCheckDuplicate = () => {
  emit('check-username')
}

const onCreate = () => {
  emit('create')
}

defineExpose({
  onCheckDuplicate,
  onCreate
})
</script>
