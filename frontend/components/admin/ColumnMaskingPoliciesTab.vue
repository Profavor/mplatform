<template>
  <div style="display: flex; flex-direction: column; gap: 1.25rem;">
    <!-- Title & Add Policy Button -->
    <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.75rem;">
      <div>
        <h4 style="font-weight: 800; font-size: 1.05rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
          <va-icon name="visibility_off" color="primary" />
          <span>{{ $t('masking_policies_title') }}</span>
        </h4>
        <p style="font-size: 0.84rem; color: var(--va-text-secondary); margin: 0.25rem 0 0 0;">
          {{ $t('masking_policies_desc') }}
        </p>
      </div>

      <div style="display: flex; gap: 0.75rem; align-items: center;">
        <va-select
          v-model="filterDomainId"
          :options="filterDomainOptions"
          value-by="id"
          text-by="label"
          dense
          @update:modelValue="fetchPolicies"
          style="min-width: 160px;"
        />

        <va-button
          v-if="canWrite"
          size="small"
          color="primary"
          icon="add_circle"
          @click="openCreateModal"
          style="font-weight: 700;"
        >
          {{ $t('create_masking_policy') }}
        </va-button>
      </div>
    </div>

    <!-- Policies Table -->
    <div v-if="isLoading" style="text-align: center; padding: 2rem;">
      <va-progress-circle indeterminate color="primary" />
    </div>
    <div v-else-if="policies.length === 0" style="padding: 2rem; text-align: center; background: var(--va-background-primary); border: 1px dashed var(--va-background-border); border-radius: 10px; color: var(--va-text-secondary);">
      <va-icon name="rule" color="secondary" size="large" style="margin-bottom: 0.5rem; display: block;" />
      <span>{{ $t('no_masking_policies') }}</span>
    </div>
    <div v-else style="overflow-x: auto; border: 1px solid var(--va-background-border); border-radius: 10px;">
      <table style="width: 100%; border-collapse: collapse; font-size: 0.88rem; text-align: left;">
        <thead>
          <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('policy_code') }}</th>
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('target_type') }}</th>
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('target_name') }}</th>
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('domain_name') }}</th>
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('field_name') }}</th>
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('masking_action') }}</th>
            <th style="padding: 0.75rem 1rem; font-weight: 700;">{{ $t('timestamp') }}</th>
            <th v-if="canWrite" style="padding: 0.75rem 1rem; font-weight: 700; text-align: center;">{{ $t('delete') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="p in policies"
            :key="p.id"
            style="border-bottom: 1px solid var(--va-background-border);"
          >
            <td style="padding: 0.75rem 1rem; font-weight: 700; font-family: monospace; color: var(--va-primary);">
              {{ p.policyCode }}
            </td>
            <td style="padding: 0.75rem 1rem;">
              <va-badge
                :text="p.targetType === 'ROLE' ? $t('target_type_role') : $t('target_type_department')"
                :color="p.targetType === 'ROLE' ? 'info' : 'warning'"
                outline
                size="small"
              />
            </td>
            <td style="padding: 0.75rem 1rem; font-weight: 600;">
              {{ p.targetName }}
            </td>
            <td style="padding: 0.75rem 1rem;">
              <span v-if="p.domainName">{{ getI18nText(p.domainName) }}</span>
              <va-badge v-else :text="$t('scope_all_nodes')" color="secondary" outline size="small" />
            </td>
            <td style="padding: 0.75rem 1rem; font-family: monospace; font-weight: 600;">
              {{ p.fieldKey }}
            </td>
            <td style="padding: 0.75rem 1rem;">
              <va-badge
                :text="p.maskingAction === 'UNMASK' ? $t('action_unmask') : $t('action_mask')"
                :color="p.maskingAction === 'UNMASK' ? 'success' : 'danger'"
                size="small"
              />
            </td>
            <td style="padding: 0.75rem 1rem; color: var(--va-text-secondary); font-size: 0.82rem;">
              {{ formatDate(p.createdAt) }}
            </td>
            <td v-if="canWrite" style="padding: 0.75rem 1rem; text-align: center;">
              <va-button
                preset="plain"
                icon="delete"
                color="danger"
                size="small"
                @click="confirmDeletePolicy(p)"
              />
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Create Policy Modal -->
    <AppModal
      v-model="showCreateModal"
      :title="$t('create_masking_policy')"
      icon="add_circle"
      size="medium"
      @ok="handleCreatePolicy"
      :ok-text="$t('create')"
      :cancel-text="$t('cancel')"
      :ok-disabled="!isFormValid || isCreating"
    >
      <div style="display: flex; flex-direction: column; gap: 1rem; padding: 0.5rem 0;">
        <va-select
          v-model="createForm.targetType"
          :label="$t('target_type')"
          :options="targetTypeOptions"
          value-by="value"
          text-by="text"
          style="width: 100%;"
        />

        <va-select
          v-if="createForm.targetType === 'ROLE'"
          v-model="createForm.targetId"
          :label="$t('target_name')"
          :options="roleOptions"
          value-by="value"
          text-by="text"
          style="width: 100%;"
        />

        <va-select
          v-else
          v-model="createForm.targetId"
          :label="$t('target_name')"
          :options="departmentOptions"
          value-by="id"
          text-by="name"
          style="width: 100%;"
        />

        <va-select
          v-model="createForm.domainId"
          :label="$t('domain_name')"
          :options="domainOptionsWithAll"
          value-by="id"
          text-by="label"
          style="width: 100%;"
        />

        <va-input
          v-model="createForm.fieldKey"
          :label="$t('field_name')"
          placeholder="e.g. phone, email, resident_number"
          style="width: 100%;"
          required
        />

        <va-select
          v-model="createForm.maskingAction"
          :label="$t('masking_action')"
          :options="actionOptions"
          value-by="value"
          text-by="text"
          style="width: 100%;"
        />
      </div>
    </AppModal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import AppModal from '~/components/common/AppModal.vue'

const props = defineProps({
  canWrite: {
    type: Boolean,
    default: false
  },
  domains: {
    type: Array,
    default: () => []
  },
  departments: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['updated', 'notify'])

const { t, locale } = useI18n()
const token = useCookie('auth_token')

const policies = ref([])
const isLoading = ref(false)
const filterDomainId = ref(null)
const showCreateModal = ref(false)
const isCreating = ref(false)

const createForm = ref({
  targetType: 'ROLE',
  targetId: 'ROLE_USER',
  domainId: null,
  fieldKey: '',
  maskingAction: 'UNMASK'
})

const isFormValid = computed(() => {
  return createForm.value.targetId && createForm.value.fieldKey && createForm.value.fieldKey.trim().length > 0
})

const targetTypeOptions = computed(() => [
  { value: 'ROLE', text: t('target_type_role') },
  { value: 'DEPARTMENT', text: t('target_type_department') }
])

const roleOptions = [
  { value: 'ROLE_ADMIN', text: 'ROLE_ADMIN (관리자)' },
  { value: 'ROLE_MANAGER', text: 'ROLE_MANAGER (매니저)' },
  { value: 'ROLE_OPERATOR', text: 'ROLE_OPERATOR (운영자)' },
  { value: 'ROLE_DATA_STEWARD', text: 'ROLE_DATA_STEWARD (데이터 스튜어드)' },
  { value: 'ROLE_USER', text: 'ROLE_USER (일반 사용자)' }
]

const departmentOptions = computed(() => {
  return props.departments.map(d => ({
    id: d.id,
    name: getI18nText(d.name) || d.id
  }))
})

const actionOptions = computed(() => [
  { value: 'UNMASK', text: t('action_unmask') },
  { value: 'MASK', text: t('action_mask') }
])

const filterDomainOptions = computed(() => {
  return [
    { id: null, label: t('all') },
    ...props.domains.map(d => ({
      id: d.id,
      label: getI18nText(d.name)
    }))
  ]
})

const domainOptionsWithAll = computed(() => {
  return [
    { id: null, label: t('scope_all_nodes') },
    ...props.domains.map(d => ({
      id: d.id,
      label: getI18nText(d.name)
    }))
  ]
})

const getI18nText = (textStr) => {
  if (!textStr) return ''
  try {
    const parsed = typeof textStr === 'object' ? textStr : JSON.parse(textStr)
    if (parsed && typeof parsed === 'object') {
      const loc = (locale?.value || 'ko').toLowerCase()
      return loc.startsWith('en') ? (parsed.en || parsed.ko || '') : (parsed.ko || parsed.en || '')
    }
    return String(textStr)
  } catch {
    return String(textStr)
  }
}

const parseDate = (dateString) => {
  if (!dateString) return null
  let str = String(dateString).trim()
  if (str.includes(' ') && !str.includes('T')) {
    str = str.replace(' ', 'T')
  }
  if (!str.endsWith('Z') && !str.includes('+') && !str.includes('-')) {
    const tz = useCookie('timezone', { default: () => 'Asia/Seoul' }).value
    let serverOffset = '+09:00'
    if (tz === 'UTC' || tz === 'GMT') serverOffset = 'Z'
    else if (tz === 'America/New_York') serverOffset = '-05:00'
    else if (tz === 'Europe/London') serverOffset = '+00:00'
    str += serverOffset
  }
  const d = new Date(str)
  return isNaN(d.getTime()) ? new Date(dateString) : d
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = parseDate(dateString)
  if (!date) return ''
  const tz = useCookie('timezone', { default: () => 'Asia/Seoul' }).value
  const formatted = date.toLocaleString(undefined, { timeZone: tz })
  return formatted.replace(/\s*(GMT|UTC|KST|PST|EST|CET)[-+0-9:]*/gi, '').trim()
}

const fetchPolicies = async () => {
  isLoading.value = true
  try {
    const query = {}
    if (filterDomainId.value) {
      query.domainId = filterDomainId.value
    }
    const res = await $fetch('/api/permissions/masking-policies', {
      headers: { Authorization: `Bearer ${token.value}` },
      query
    })
    policies.value = res || []
  } catch (e) {
    console.error('Failed to fetch masking policies', e)
    policies.value = []
  } finally {
    isLoading.value = false
  }
}

const openCreateModal = () => {
  createForm.value = {
    targetType: 'ROLE',
    targetId: 'ROLE_USER',
    domainId: null,
    fieldKey: '',
    maskingAction: 'UNMASK'
  }
  showCreateModal.value = true
}

const handleCreatePolicy = async () => {
  if (!isFormValid.value) return
  isCreating.value = true
  try {
    await $fetch('/api/permissions/masking-policies', {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` },
      body: {
        targetType: createForm.value.targetType,
        targetId: String(createForm.value.targetId),
        domainId: createForm.value.domainId,
        fieldKey: createForm.value.fieldKey.trim(),
        maskingAction: createForm.value.maskingAction
      }
    })
    showCreateModal.value = false
    emit('notify', { message: t('policy_created_success'), type: 'success' })
    await fetchPolicies()
    emit('updated')
  } catch (e) {
    const msg = e.response?._data?.message || e.message || String(e)
    emit('notify', { message: msg, type: 'error' })
  } finally {
    isCreating.value = false
  }
}

const confirmDeletePolicy = async (policy) => {
  if (!confirm(t('delete_policy_confirm'))) return
  try {
    await $fetch(`/api/permissions/masking-policies/${policy.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    emit('notify', { message: t('policy_deleted_success'), type: 'success' })
    await fetchPolicies()
    emit('updated')
  } catch (e) {
    const msg = e.response?._data?.message || e.message || String(e)
    emit('notify', { message: msg, type: 'error' })
  }
}

onMounted(() => {
  fetchPolicies()
})
</script>
