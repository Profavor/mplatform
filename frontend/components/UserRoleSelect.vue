<template>
  <div class="user-role-select-wrapper" style="display: flex; flex-direction: column; gap: 0.4rem; width: 100%;">
    <!-- Single Select Dropdown -->
    <va-select
      v-model="tempSelectedRole"
      :options="formattedOptions"
      :label="label"
      :placeholder="placeholder || t('select_role_to_add')"
      :clearable="clearable"
      :disabled="disabled"
      :size="size"
      value-by="value"
      text-by="text"
      @update:modelValue="onSelectRole"
    />

    <!-- Selected Role Chips List -->
    <div v-if="selectedRoleList.length > 0" class="selected-role-chips" style="display: flex; flex-wrap: wrap; gap: 0.35rem; margin-top: 0.25rem;">
      <va-chip
        v-for="roleCode in selectedRoleList"
        :key="roleCode"
        size="small"
        :color="getRoleColor(roleCode)"
        style="font-weight: 600; font-size: 0.78rem; padding: 3px 8px; display: inline-flex; align-items: center;"
      >
        <span>{{ formatRoleText(roleCode, false, locale) }}</span>
        <va-icon
          name="close"
          size="14px"
          style="margin-left: 6px; cursor: pointer; opacity: 0.85;"
          title="역할 해제"
          @click.stop="removeRole(roleCode)"
        />
      </va-chip>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoles } from '~/composables/useRoles'

const props = defineProps({
  modelValue: {
    type: [String, Array],
    default: null
  },
  multiple: {
    type: Boolean,
    default: false
  },
  orgId: {
    type: String,
    default: null
  },
  label: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: ''
  },
  clearable: {
    type: Boolean,
    default: true
  },
  disabled: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'medium'
  },
  includeRolePrefix: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const { t, locale } = useI18n()

const { fetchRolesForOrg, formatRoleText, getRoleColor, getUserOrgId, initGlobalRoles } = useRoles()

const localRoleList = ref([])
const tempSelectedRole = ref(null)

const loadRoles = async () => {
  await initGlobalRoles()
  const targetOrgId = props.orgId || getUserOrgId()
  localRoleList.value = await fetchRolesForOrg(targetOrgId)
}

onMounted(async () => {
  await loadRoles()
})

watch(() => props.orgId, async (newOrgId) => {
  localRoleList.value = await fetchRolesForOrg(newOrgId || getUserOrgId(), true)
})

const formattedOptions = computed(() => {
  if (!localRoleList.value || localRoleList.value.length === 0) return []

  const result = []
  const seenCodes = new Set()
  const seenTexts = new Set()

  localRoleList.value.forEach(role => {
    if (!role || !role.name) return
    const code = role.name
    const normCode = code.startsWith('ROLE_') ? code.replace('ROLE_', '') : code
    const textStr = formatRoleText(code, false, locale.value)

    if (!seenCodes.has(code) && !seenCodes.has(normCode) && !seenTexts.has(textStr)) {
      seenCodes.add(code)
      seenCodes.add(normCode)
      seenTexts.add(textStr)
      result.push({
        value: code,
        text: textStr
      })
    }
  })

  return result
})

const selectedRoleList = computed(() => {
  if (!props.modelValue) return []
  if (Array.isArray(props.modelValue)) {
    return props.modelValue.map(r => String(r).trim()).filter(Boolean)
  }
  if (typeof props.modelValue === 'string') {
    return props.modelValue.split(',').map(r => r.trim()).filter(Boolean)
  }
  return []
})

const onSelectRole = (newVal) => {
  if (!newVal) return

  const currentList = Array.isArray(props.modelValue)
    ? [...props.modelValue]
    : (typeof props.modelValue === 'string' && props.modelValue ? props.modelValue.split(',').map(r => r.trim()) : [])

  const normTarget = newVal.replace('ROLE_', '')
  const exists = currentList.some(r => r === newVal || r.replace('ROLE_', '') === normTarget)

  if (!exists) {
    currentList.push(newVal)
    const finalVal = Array.isArray(props.modelValue) ? currentList : currentList.join(', ')
    emit('update:modelValue', finalVal)
    emit('change', finalVal)
  }

  nextTick(() => {
    tempSelectedRole.value = null
  })
}

const removeRole = (roleCodeToRemove) => {
  const normTarget = roleCodeToRemove.replace('ROLE_', '')
  const currentList = Array.isArray(props.modelValue)
    ? [...props.modelValue]
    : (typeof props.modelValue === 'string' && props.modelValue ? props.modelValue.split(',').map(r => r.trim()) : [])

  const newList = currentList.filter(r => r !== roleCodeToRemove && r.replace('ROLE_', '') !== normTarget)

  const finalVal = Array.isArray(props.modelValue) ? newList : newList.join(', ')
  emit('update:modelValue', finalVal)
  emit('change', finalVal)
}
</script>
