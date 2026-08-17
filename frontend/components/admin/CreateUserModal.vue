<template>
  <AppModal
    :model-value="modelValue"
    :title="t('create_user', '사용자 생성')"
    icon="person_add"
    size="medium"
    hide-default-actions
    @update:model-value="val => emit('update:modelValue', val)"
  >
    <div style="padding: 0.5rem 0; min-width: 400px; display: flex; flex-direction: column; gap: 1.25rem;">
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

      <va-input
        v-model="newUser.email"
        :label="t('label_email', '이메일 주소')"
        :placeholder="selectedOrgEmailDomain ? `${newUser.username || 'user'}@${selectedOrgEmailDomain}` : t('placeholder_email', '예: user@company.com')"
        outline
        :messages="[emailHintMessage]"
      />

      <UserRoleSelect v-model="newUser.role" :label="t('user_role', '사용자 역할')" />

      <va-select
        v-model="newUser.organizationId"
        :options="organizations"
        value-by="id"
        :text-by="getOrgDisplayName"
        :label="t('organization', '소속 조직')"
        clearable
        outline
      />

      <va-select
        v-model="newUser.departmentId"
        :options="departments"
        value-by="id"
        :text-by="getDeptDisplayName"
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
  </AppModal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import UserRoleSelect from '~/components/UserRoleSelect.vue'
import { formatMultilingual } from '~/composables/useMultilingual'
import AppModal from '~/components/common/AppModal.vue'

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

const selectedOrg = computed(() => {
  if (!props.newUser?.organizationId || !props.organizations) return null
  return props.organizations.find((o: any) => o.id === props.newUser.organizationId)
})

const selectedOrgEmailDomain = computed(() => {
  const domain = selectedOrg.value?.emailDomain
  if (domain && typeof domain === 'string' && domain.trim()) {
    return domain.trim().replace(/^@/, '')
  }
  return ''
})

const emailHintMessage = computed(() => {
  if (selectedOrgEmailDomain.value) {
    return t('email_domain_auto_hint', { domain: '@' + selectedOrgEmailDomain.value })
  }
  return t('email_default_fallback_hint')
})

const getOrgDisplayName = (org: any) => {
  if (!org) return ''
  return formatMultilingual(org.displayName) || formatMultilingual(org.name) || org.name || org.id || ''
}

const getDeptDisplayName = (dept: any) => {
  if (!dept) return ''
  return formatMultilingual(dept.name) || dept.name || dept.id || ''
}

const getI18nText = (nameObj: any) => {
  if (!nameObj) return ''
  return formatMultilingual(nameObj) || String(nameObj)
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
