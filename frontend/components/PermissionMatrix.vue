<template>
  <div class="permission-matrix-container">
    <div v-if="editable" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; padding-bottom: 0.5rem; border-bottom: 1px solid var(--va-background-border);">
      <span style="font-size: 0.95rem; font-weight: 700; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.4rem;">
        <va-icon name="admin_panel_settings" color="primary" />
        {{ getLabel('perm_master_title', '세부 권한 마스터 그룹 목록') }}
      </span>
      <va-button color="primary" size="small" icon="add" @click="$emit('add-group')">
        + {{ getLabel('add_new_group_btn', '신규 그룹 추가') }}
      </va-button>
    </div>

    <div v-else style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
      <span style="font-size: 0.88rem; font-weight: 700; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.35rem;">
        <va-icon name="admin_panel_settings" color="primary" size="small" />
        {{ getLabel('permissions_matrix_title', '부여할 세부 권한 그룹 목록 (Permissions Matrix)') }}
      </span>
      <span style="font-size: 0.78rem; color: var(--va-text-secondary); font-weight: 600;">
        {{ getLabel('selected_count', '선택됨') }}: <b style="color: var(--va-primary);">{{ selectedPerms.length }}</b>개
      </span>
    </div>

    <!-- Dynamic Permission Groups -->
    <div style="display: flex; flex-direction: column; gap: 1rem;">
      <div v-for="group in groups" :key="group.id" class="perm-category-group">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.4rem;">
          <div class="perm-category-title" :style="{ color: group.color || '#3b82f6', display: 'flex', alignItems: 'center', gap: '0.4rem' }">
            <span style="font-weight: 700; font-size: 0.9rem;">
              {{ group.icon }} {{ currentLocale === 'en' ? (group.titleEn || (group.titleKey ? getLabel(group.titleKey, group.title) : group.title)) : (group.titleKey ? getLabel(group.titleKey, group.title) : group.title) }} ({{ group.code ? group.code.toUpperCase() : '' }})
            </span>
            <template v-if="editable">
              <va-icon name="edit" size="14px" style="cursor: pointer; opacity: 0.65;" title="그룹 수정" @click.stop="$emit('edit-group', group)" />
              <va-icon name="delete" size="14px" color="danger" style="cursor: pointer; opacity: 0.65;" title="그룹 삭제" @click.stop="$emit('delete-group', group)" />
            </template>
          </div>

          <va-button v-if="editable" preset="primary" outline size="small" icon="add" style="font-size: 0.75rem; font-weight: 700;" @click="$emit('add-perm', group)">
            + {{ getLabel('add_perm_btn', '권한 추가') }}
          </va-button>
        </div>

        <div class="perm-chips-container">
          <div
            v-for="p in group.permissions"
            :key="p.value"
            class="perm-chip-item"
            :class="[group.chipClass || '', { active: isSelected(p.value) }]"
            @click="togglePerm(p.value)"
          >
            <span class="chip-check" v-if="isSelected(p.value)">✓</span>
            <span>{{ currentLocale === 'en' ? (p.labelEn || (p.labelKey ? getLabel(p.labelKey, p.label) : p.label)) : (p.labelKey ? getLabel(p.labelKey, p.label) : p.label) }} ({{ p.value }})</span>
            
            <span v-if="editable" style="display: inline-flex; align-items: center; gap: 3px; margin-left: 6px; opacity: 0.8;" class="chip-actions">
              <va-icon name="edit" size="12px" title="권한 수정" @click.stop="$emit('edit-perm', { group, perm: p })" />
              <va-icon name="close" size="12px" title="권한 삭제" @click.stop="$emit('delete-perm', { group, perm: p })" />
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  groups: {
    type: Array,
    default: () => []
  },
  editable: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'update:modelValue',
  'add-group',
  'edit-group',
  'delete-group',
  'add-perm',
  'edit-perm',
  'delete-perm'
])

const { t, locale } = useI18n()

const currentLocale = computed(() => {
  const loc = (locale?.value || 'ko').toLowerCase()
  return loc.startsWith('en') ? 'en' : 'ko'
})

const getLabel = (key, fallback) => {
  if (!key) return fallback
  const res = t(key)
  return (!res || res === key) ? fallback : res
}

const selectedPerms = computed(() => Array.isArray(props.modelValue) ? props.modelValue : [])

const isSelected = (val) => selectedPerms.value.includes(val)

const togglePerm = (val) => {
  const current = [...selectedPerms.value]
  const idx = current.indexOf(val)
  if (idx > -1) {
    current.splice(idx, 1)
  } else {
    current.push(val)
  }
  emit('update:modelValue', current)
}
</script>

<style scoped>
.permission-matrix-container {
  width: 100%;
}

.perm-category-group {
  background: var(--va-background-element);
  border: 1px solid var(--va-background-border);
  border-radius: 10px;
  padding: 0.85rem 1rem;
}

.perm-chips-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
  margin-top: 0.25rem;
}

.perm-chip-item {
  padding: 4px 10px;
  border-radius: 16px;
  font-size: 0.78rem;
  font-weight: 700;
  cursor: pointer;
  border: 1px solid var(--va-background-border);
  background: var(--va-background-secondary);
  color: var(--va-text-secondary);
  transition: all 0.2s ease;
  user-select: none;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

.perm-chip-item:hover {
  border-color: var(--va-primary);
  color: var(--va-primary);
  transform: translateY(-1px);
}

.perm-chip-item.active {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: white;
  border-color: transparent;
  box-shadow: 0 2px 6px rgba(37, 99, 235, 0.3);
}

.perm-chip-item.green.active {
  background: linear-gradient(135deg, #10b981, #059669);
  box-shadow: 0 2px 6px rgba(16, 185, 129, 0.3);
}

.perm-chip-item.purple.active {
  background: linear-gradient(135deg, #8b5cf6, #6d28d9);
  box-shadow: 0 2px 6px rgba(139, 92, 246, 0.3);
}

.perm-chip-item.amber.active {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  box-shadow: 0 2px 6px rgba(245, 158, 11, 0.3);
}

.perm-chip-item.cyan.active {
  background: linear-gradient(135deg, #06b6d4, #0891b2);
  box-shadow: 0 2px 6px rgba(6, 182, 212, 0.3);
}

.chip-check {
  font-weight: 900;
  font-size: 0.75rem;
}
</style>
