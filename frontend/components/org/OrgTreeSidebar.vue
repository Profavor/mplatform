<template>
  <va-card style="flex: 1; min-width: 320px;">
    <va-card-title style="display: flex; justify-content: space-between; align-items: center;">
      <span>{{ t('org_list') }}</span>
      <div style="display: flex; align-items: center; gap: 0.5rem;">
        <va-badge color="info" :text="String(organizations?.length || 0)" />
        <va-button size="small" color="primary" icon="add" @click="$emit('add-org')">
          {{ t('create_organization') }}
        </va-button>
      </div>
    </va-card-title>
    <va-card-content>
      <div v-if="loading" style="text-align: center; padding: 2rem;">
        <va-progress-circle indeterminate color="primary" />
      </div>
      <div v-else-if="!organizations || organizations.length === 0" style="text-align: center; padding: 2rem; color: #777;">
        {{ t('no_orgs_registered') }}
      </div>
      <div v-else style="display: flex; flex-direction: column; gap: 0.75rem;">
        <div
          v-for="org in organizations"
          :key="org.id"
          @click="$emit('select-org', org)"
          style="padding: 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); cursor: pointer; transition: all 0.2s ease; background: var(--va-background-element);"
          :style="{
            borderColor: selectedId === org.id ? 'var(--va-primary)' : 'var(--va-background-border)',
            boxShadow: selectedId === org.id ? '0 4px 12px rgba(21, 101, 192, 0.15)' : 'none'
          }"
        >
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.35rem;">
            <span style="font-weight: 700; font-size: 1.05rem; color: var(--va-text-primary);">
              {{ getI18nText(org.displayName) || org.name }}
            </span>
            <div style="display: flex; align-items: center; gap: 0.35rem;">
              <va-badge :color="org.isActive !== false ? 'success' : 'danger'" :text="org.isActive !== false ? t('active_status') : t('inactive_status')" size="small" />
              <va-button
                v-if="org.id !== '00000000-0000-0000-0000-000000000001'"
                preset="plain"
                icon="delete"
                color="danger"
                size="small"
                title="조직 삭제"
                @click.stop="$emit('delete-org', org)"
              />
            </div>
          </div>
          <div style="font-size: 0.8rem; color: var(--va-text-secondary); font-family: monospace;">
            ID: {{ org.id }}
          </div>
          <div v-if="org.description" style="font-size: 0.85rem; color: var(--va-text-secondary); margin-top: 0.35rem;">
            {{ getI18nText(org.description) }}
          </div>
        </div>
      </div>
    </va-card-content>
  </va-card>
</template>

<script setup>
import { useI18n } from 'vue-i18n'

const props = defineProps({
  organizations: { type: Array, default: () => [] },
  selectedId: { type: String, default: '' },
  loading: { type: Boolean, default: false }
})

const emit = defineEmits(['select-org', 'add-org', 'delete-org'])

const { t, locale } = useI18n()

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
</script>
