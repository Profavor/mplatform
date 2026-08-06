<template>
  <va-card v-if="organization" style="flex: 2; min-width: 480px;">
    <va-card-title style="display: flex; align-items: center; justify-content: space-between;">
      <div style="display: flex; align-items: center; gap: 0.5rem;">
        <va-icon name="apartment" color="primary" />
        <span>{{ getI18nText(organization.displayName) || organization.name }}</span>
      </div>
      <span style="font-size: 0.8rem; color: var(--va-text-secondary); font-weight: normal;">
        {{ t('system_org_info') }}
      </span>
    </va-card-title>
    
    <va-card-content>
      <va-tabs v-model="activeTab" style="margin-bottom: 1.25rem;">
        <template #tabs>
          <va-tab name="info">{{ t('basic_info') }}</va-tab>
          <va-tab name="depts">{{ t('dept_team_management') }}</va-tab>
          <va-tab name="roles">{{ t('rbac_role_management') }}</va-tab>
        </template>
      </va-tabs>

      <!-- Tab 1: Basic Info -->
      <div v-if="activeTab === 'info'" style="display: flex; flex-direction: column; gap: 1.25rem;">
        <div class="row" style="display: flex; flex-wrap: wrap; gap: 1rem; align-items: flex-start;">
          <div style="flex: 2; min-width: 300px;">
            <MultilingualInput
              v-model:ko="editOrgForm.displayNameKo"
              v-model:en="editOrgForm.displayNameEn"
              :label="t('org_display_name')"
            />
          </div>
          <div style="flex: 1; min-width: 150px; max-width: 240px;">
            <va-input
              v-model="editOrgForm.name"
              :label="t('org_sys_code')"
              readonly
              class="readonly-sys-code"
              style="width: 100%;"
            />
          </div>
        </div>
        <div style="display: flex; gap: 1rem; align-items: center;">
          <div>
            <label style="display: block; font-size: 0.85rem; font-weight: 700; color: var(--va-text-primary); margin-bottom: 0.5rem;">
              {{ getLabel('org_icon', '조직 아이콘') }}
            </label>
            <div style="display: flex; align-items: center; gap: 1rem; background: var(--va-background-element); padding: 0.5rem 0.75rem; border-radius: 8px; border: 1px solid var(--va-background-border);">
              <va-icon :name="editOrgForm.icon || 'corporate_fare'" color="primary" size="medium" />
              <va-button preset="primary" outline icon="palette" size="small" @click="$emit('open-icon-picker', 'org')">
                {{ getLabel('select_icon', '아이콘 선택') }}
              </va-button>
            </div>
          </div>
        </div>
        <div>
          <MultilingualInput
            v-model:ko="editOrgForm.descriptionKo"
            v-model:en="editOrgForm.descriptionEn"
            :label="t('org_description')"
            is-textarea
            :min-rows="2"
          />
        </div>
        <div style="display: flex; justify-content: flex-end; gap: 0.75rem;">
          <va-button
            v-if="organization && organization.id !== '00000000-0000-0000-0000-000000000001'"
            color="danger"
            preset="secondary"
            icon="delete"
            @click="$emit('delete-org', organization)"
          >
            {{ getLabel('delete_organization', '조직 삭제') }}
          </va-button>
          <va-button color="success" icon="save" @click="handleSave">
            {{ t('save_changes') }}
          </va-button>
        </div>
      </div>

      <!-- Tab 2: Departments & Teams Hierarchy Tree -->
      <div v-else-if="activeTab === 'depts'" style="display: flex; flex-direction: column; gap: 1.25rem;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div>
            <h4 style="margin: 0; font-weight: 700; color: var(--va-text-primary);">
              {{ getLabel('dept_structure', '소속 부서 및 조직 계층 구조 (Tree View)') }}
            </h4>
            <p style="margin: 0.25rem 0 0 0; font-size: 0.82rem; color: var(--va-text-secondary);">
              {{ getLabel('dept_structure_desc', '조직 - 상위 부서 - 하위 부서 N단계 계층 구조') }}
            </p>
          </div>
          <div style="display: flex; gap: 0.5rem;">
            <va-button size="small" preset="secondary" icon="add" @click="$emit('add-dept', null)">
              + {{ getLabel('add_root_dept', '최상위 부서 추가') }}
            </va-button>
          </div>
        </div>

        <div v-if="!departments || departments.length === 0" style="padding: 2.5rem; text-align: center; color: #777; background: var(--va-background-secondary); border-radius: 8px; border: 1px solid var(--va-background-border);">
          <va-icon name="account_tree" size="large" color="secondary" style="margin-bottom: 0.5rem;" />
          <div>{{ t('no_depts_added') }}</div>
        </div>

        <!-- Root Tree View Container -->
        <div v-else style="border: 1px solid var(--va-background-border); border-radius: 10px; padding: 1.25rem; background: var(--va-background-element);">
          <!-- Organization Root Node -->
          <div style="display: flex; align-items: center; justify-content: space-between; padding: 0.75rem 1.25rem; border-radius: 8px; background: var(--va-background-secondary); border: 1.5px solid var(--va-primary); box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);">
            <div style="display: flex; align-items: center; gap: 0.65rem;">
              <va-icon name="corporate_fare" color="primary" size="medium" />
              <span style="font-weight: 800; font-size: 1.05rem; color: var(--va-text-primary);">
                {{ getI18nText(organization.displayName) || organization.name }}
              </span>
              <va-chip size="small" color="primary">{{ getLabel('organization', '조직') }}</va-chip>
            </div>
            <va-button size="small" color="primary" preset="secondary" icon="add" @click="$emit('add-dept', null)">
              + {{ getLabel('add_root_dept', '최상위 부서 추가') }}
            </va-button>
          </div>

          <!-- Sub-Department Tree Branches -->
          <div style="margin-top: 0.5rem;">
            <OrgTreeItem
              v-for="dept in rootDepartments"
              :key="dept.id"
              :node="dept"
              @add-subdept="$emit('add-dept', $event)"
              @edit-dept="$emit('edit-dept', $event)"
              @delete-dept="$emit('delete-dept', $event)"
              @manage-members="$emit('manage-members', $event)"
            />
          </div>
        </div>
      </div>

      <!-- Tab 3: RBAC Roles -->
      <div v-else-if="activeTab === 'roles'" style="display: flex; flex-direction: column; gap: 1.25rem;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <h4 style="margin: 0; font-weight: 700; color: var(--va-text-primary);">
            {{ t('system_custom_roles') }}
          </h4>
          <div style="display: flex; gap: 0.5rem; align-items: center;">

            <va-button size="small" preset="secondary" icon="add" @click="$emit('add-role')">
              + {{ t('add_role') }}
            </va-button>
          </div>
        </div>

        <div style="display: flex; flex-direction: column; gap: 0.85rem;">
          <div v-for="role in roles" :key="role.id" style="border: 1px solid var(--va-background-border); border-radius: 8px; padding: 1rem; background: var(--va-background-secondary);">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
              <div style="display: flex; align-items: center; gap: 0.55rem;">
                <va-badge :color="role.isSystemRole ? 'primary' : 'warning'" :text="role.name" />
                <span style="font-weight: 700; font-size: 0.95rem; color: var(--va-text-primary);">{{ getI18nText(role.displayName) || role.name }}</span>
                <span v-if="role.isSystemRole" style="font-size: 0.72rem; color: var(--va-primary); background: rgba(37,99,235,0.1); padding: 2px 6px; border-radius: 4px; font-weight: 700;">SYSTEM</span>
              </div>
              <div style="display: flex; align-items: center; gap: 0.4rem;">
                <va-button size="small" preset="secondary" icon="edit" @click="$emit('edit-role', role)" />
                <va-button size="small" preset="plain" icon="delete" color="danger" @click="$emit('delete-role', role)" />
              </div>
            </div>
            <div style="font-size: 0.82rem; color: var(--va-text-secondary); margin-bottom: 0.6rem;" v-if="role.description">{{ getI18nText(role.description) }}</div>
            <div style="display: flex; gap: 0.4rem; flex-wrap: wrap;">
              <va-chip v-for="perm in (role.permissions || [])" :key="perm" size="small" color="success" outline>
                {{ perm }}
              </va-chip>
            </div>
          </div>
        </div>
      </div>
    </va-card-content>
  </va-card>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import MultilingualInput from '~/components/MultilingualInput.vue'
import OrgTreeItem from '~/components/OrgTreeItem.vue'

const props = defineProps({
  organization: { type: Object, default: null },
  departments: { type: Array, default: () => [] },
  rootDepartments: { type: Array, default: () => [] },
  roles: { type: Array, default: () => [] }
})

const emit = defineEmits([
  'save-org',
  'delete-org',
  'open-icon-picker',
  'add-dept',
  'edit-dept',
  'delete-dept',
  'manage-members',
  'add-role',
  'edit-role',
  'delete-role',
  'export-roles',
  'import-roles'
])

const { t, locale } = useI18n()

const activeTab = ref('info')
const editOrgForm = ref({
  name: '',
  displayNameKo: '',
  displayNameEn: '',
  descriptionKo: '',
  descriptionEn: '',
  icon: 'corporate_fare'
})

const getLabel = (key, fallback) => {
  const res = t(key)
  return (!res || res === key) ? fallback : res
}

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

const parseMultilingualField = (rawVal) => {
  if (!rawVal) return { ko: '', en: '' }
  if (typeof rawVal === 'object') {
    return { ko: rawVal.ko || '', en: rawVal.en || '' }
  }
  try {
    const parsed = JSON.parse(rawVal)
    if (parsed && typeof parsed === 'object' && ('ko' in parsed || 'en' in parsed)) {
      return { ko: parsed.ko || '', en: parsed.en || '' }
    }
  } catch (e) {}
  return { ko: String(rawVal), en: String(rawVal) }
}

watch(
  () => props.organization,
  (org) => {
    if (!org) return
    const parsedDn = parseMultilingualField(org.displayName || org.name)
    const parsedDesc = parseMultilingualField(org.description)

    editOrgForm.value = {
      name: org.name || '',
      displayNameKo: parsedDn.ko,
      displayNameEn: parsedDn.en,
      descriptionKo: parsedDesc.ko,
      descriptionEn: parsedDesc.en,
      icon: org.icon || 'corporate_fare'
    }
  },
  { immediate: true }
)

const handleSave = () => {
  emit('save-org', editOrgForm.value)
}
</script>

<style scoped>
.readonly-sys-code :deep(.va-input-wrapper),
.readonly-sys-code :deep(input) {
  background-color: var(--va-background-element) !important;
  color: var(--va-text-primary) !important;
  opacity: 0.85;
}
</style>
