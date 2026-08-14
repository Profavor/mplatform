<template>
  <div style="margin-left: 1.25rem; border-left: 2px dashed var(--va-background-border); padding-left: 1rem; margin-top: 0.75rem; position: relative;">
    <!-- Department Node Header -->
    <div style="display: flex; align-items: center; justify-content: space-between; padding: 0.65rem 1rem; border-radius: 8px; background: var(--va-background-secondary); border: 1px solid var(--va-background-border); transition: all 0.2s ease;">
      <div style="display: flex; align-items: center; gap: 0.5rem; flex: 1;">
        <va-icon
          v-if="hasChildren"
          :name="isOpen ? 'expand_more' : 'chevron_right'"
          style="cursor: pointer;"
          @click="isOpen = !isOpen"
        />
        <va-icon :name="node.icon || 'folder'" color="primary" size="small" />

        <span style="font-weight: 700; font-size: 0.95rem; color: var(--va-text-primary);">
          {{ getI18nText(node.name) }}
        </span>
        <va-chip size="small" color="info" outline>{{ t('dept') }}</va-chip>
        <va-badge v-for="r in nodeRoles" :key="r" :text="r" color="warning" size="small" style="font-weight: bold; margin-left: 0.2rem;" />
        <span v-if="node.description" style="font-size: 0.8rem; color: var(--va-text-secondary); margin-left: 0.5rem;">
          {{ getI18nText(node.description) }}
        </span>
      </div>

      <!-- Actions -->
      <div style="display: flex; gap: 0.4rem; align-items: center;">
        <va-button size="small" preset="secondary" color="info" icon="people" @click="$emit('manage-members', node)">
          {{ t('manage_members') }}
          <va-badge v-if="node.memberCount" :text="String(node.memberCount)" color="primary" size="small" style="margin-left: 0.25rem;" />
        </va-button>
        <va-button size="small" preset="secondary" icon="add" @click="$emit('add-subdept', node.id)">
          + {{ t('add_subdept') }}
        </va-button>
        <va-button size="small" preset="secondary" color="warning" icon="edit" @click="$emit('edit-dept', node)">
          {{ t('edit') }}
        </va-button>
        <va-button size="small" preset="secondary" color="danger" icon="delete" @click="$emit('delete-dept', node)">
          {{ t('delete') }}
        </va-button>
      </div>
    </div>

    <!-- Children Nodes -->
    <div v-if="isOpen" style="display: flex; flex-direction: column;">
      <!-- Recursive Sub-Departments -->
      <OrgTreeItem
        v-for="subDept in (node.subDepts || [])"
        :key="subDept.id"
        :node="subDept"
        @add-subdept="$emit('add-subdept', $event)"
        @edit-dept="$emit('edit-dept', $event)"
        @delete-dept="$emit('delete-dept', $event)"
        @manage-members="$emit('manage-members', $event)"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()

const props = defineProps({
  node: { type: Object, required: true }
})

defineEmits(['add-subdept', 'edit-dept', 'delete-dept', 'manage-members'])

const isOpen = ref(true)

const getI18nText = (textStr) => {
  if (!textStr) return ''
  try {
    const parsed = typeof textStr === 'object' ? textStr : JSON.parse(textStr)
    if (parsed && typeof parsed === 'object') {
      const loc = (locale?.value || 'ko').toLowerCase()
      return loc.startsWith('en') ? (parsed.en || parsed.ko || '') : (parsed.ko || parsed.en || '')
    }
    return String(textStr)
  } catch (e) {
    return textStr
  }
}

const hasChildren = computed(() => {
  return (props.node.subDepts && props.node.subDepts.length > 0)
})

const nodeRoles = computed(() => {
  if (!props.node || !props.node.role) return []
  if (Array.isArray(props.node.role)) return props.node.role
  return String(props.node.role).split(',').map(r => r.trim()).filter(Boolean)
})
</script>
