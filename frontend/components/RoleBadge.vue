<template>
  <span class="role-badge-wrapper" style="display: inline-flex; gap: 0.35rem; flex-wrap: wrap; align-items: center;">
    <va-chip
      v-for="role in roleListInput"
      :key="role"
      :color="getRoleColor(role)"
      size="small"
      class="mr-1"
      style="font-weight: 700; letter-spacing: 0.2px; font-size: 0.78rem;"
    >
      <va-icon name="verified_user" size="small" style="margin-right: 4px;" />
      {{ formatRoleText(role, hideCode) }}
    </va-chip>
  </span>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoles } from '~/composables/useRoles'

const props = defineProps({
  value: {
    type: [String, Array],
    default: () => []
  },
  orgId: {
    type: String,
    default: null
  },
  hideCode: {
    type: Boolean,
    default: false
  }
})

const { fetchRolesForOrg, formatRoleText, getRoleColor, getUserOrgId } = useRoles()

onMounted(() => {
  fetchRolesForOrg(props.orgId || getUserOrgId())
})

const roleListInput = computed(() => {
  if (!props.value) return []
  if (Array.isArray(props.value)) {
    return props.value.filter(Boolean)
  }
  if (typeof props.value === 'string') {
    return props.value.split(',').map(r => r.trim()).filter(Boolean)
  }
  return []
})
</script>
