<template>
  <div style="display: flex; flex-direction: column; gap: 1.25rem; padding-bottom: 2rem;">
    <!-- Top Action Bar -->
    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); padding: 1rem 1.25rem; border-radius: 12px; border: 1px solid var(--va-background-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04);">
      <div style="display: flex; align-items: center; gap: 0.75rem;">
        <va-icon name="menu_book" size="large" color="primary" />
        <div>
          <h2 style="font-weight: 700; font-size: 1.35rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
            {{ pageTitle }}
            <va-badge text="System" color="primary" size="small" />
          </h2>
          <span style="font-size: 0.85rem; color: var(--va-text-secondary);">
            {{ $t('menu_management_desc') }}
          </span>
        </div>
      </div>

      <div style="display: flex; gap: 0.75rem; align-items: center;">
        <va-button v-if="hasPermission('admin:write')" preset="primary" color="warning" icon="sync" size="small" @click="syncMenuSeed">
          {{ $t('sync_menu_seed') }}
        </va-button>
        <va-button v-if="hasPermission('admin:write')" preset="primary" color="primary" icon="save_alt" size="small" @click="dumpMenuSeed">
          {{ $t('backup_menu_seed') }}
        </va-button>
        <va-button icon="add" size="small" @click="openAddModal(null)">{{ $t('add_root_menu') }}</va-button>
        <va-button preset="outline" color="primary" icon="refresh" size="small" @click="fetchMenus">
          {{ $t('refresh') }}
        </va-button>
      </div>
    </div>

    <va-card>
      <va-card-content>
        <div class="d-flex" style="gap: 2rem;">
          <!-- Menu Tree -->
          <div style="flex: 1; border-right: 1px solid var(--va-background-border); padding-right: 1rem;">
            <va-tree-view :nodes="treeNodes" @selected="onNodeSelected" expandable>
              <template #content="node">
                <div class="d-flex justify-space-between align-center w-100" style="padding: 0.25rem 0; cursor: pointer;" @click="onNodeSelected(node)">
                  <div style="display: flex; align-items: center; gap: 0.5rem;">
                    <span style="font-weight: 600;" :style="{ opacity: node.raw?.isActive === false ? 0.5 : 1 }">{{ node.label }}</span>
                    <va-badge v-if="node.raw?.isActive === false" :text="$t('inactive_badge')" color="warning" size="small" />
                  </div>
                  <div>
                    <va-button preset="plain" icon="add" size="small" @click.stop="openAddModal(node.id)" />
                    <va-button preset="plain" icon="delete" color="danger" size="small" @click.stop="deleteMenu(node.id)" />
                  </div>
                </div>
              </template>
            </va-tree-view>
          </div>

          <!-- Edit Form (Decoupled Component) -->
          <div style="flex: 2;">
            <MenuEditForm
              :selected-menu="selectedMenu"
              :selected-menu-name-ko="selectedMenuNameKo"
              :selected-menu-name-en="selectedMenuNameEn"
              :selected-menu-roles="selectedMenuRoles"
              :selected-menu-has-children="selectedMenuHasChildren"
              @update:selected-menu-name-ko="val => selectedMenuNameKo = val"
              @update:selected-menu-name-en="val => selectedMenuNameEn = val"
              @update:selected-menu-roles="val => selectedMenuRoles = val"
              @open-icon-picker="openIconPicker('edit')"
              @save="saveMenu"
            />
          </div>
        </div>
      </va-card-content>
    </va-card>

    <!-- Add Modal (Decoupled Component) -->
    <AddMenuModal
      v-model="showAddModal"
      :new-menu="newMenu"
      :new-menu-name-ko="newMenuNameKo"
      :new-menu-name-en="newMenuNameEn"
      :new-menu-roles="newMenuRoles"
      @update:new-menu-name-ko="val => newMenuNameKo = val"
      @update:new-menu-name-en="val => newMenuNameEn = val"
      @update:new-menu-roles="val => newMenuRoles = val"
      @open-icon-picker="openIconPicker('add')"
      @save="addMenu"
    />

    <!-- Icon Picker Modal -->
    <va-modal v-model="showIconPickerModal" :title="$t('select_icon')" hide-default-actions>
      <IconPicker v-model="tempIcon" />
      <div class="d-flex justify-end mt-4" style="gap: 1rem;">
        <va-button preset="plain" color="secondary" @click="showIconPickerModal = false">{{ $t('cancel') }}</va-button>
        <va-button @click="confirmIconSelection">{{ $t('select') }}</va-button>
      </div>
    </va-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMenu } from '~/composables/useMenu'
import { usePageTitle } from '~/composables/usePageTitle'
import { usePermission } from '~/composables/usePermission'
import AddMenuModal from '~/components/admin/AddMenuModal.vue'
import MenuEditForm from '~/components/admin/MenuEditForm.vue'

const { pageTitle } = usePageTitle('menu_management', '메뉴 관리')
import { useToast } from 'vuestic-ui'

const { t, te, locale } = useI18n()
const { menus, fetchMenus, refreshMenus } = useMenu()
const { hasPermission } = usePermission()
const { init } = useToast()

const dumpMenuSeed = async () => {
  if (!window.confirm(t('menu_dump_seed_confirm'))) return
  try {
    const token = useCookie('auth_token')
    await $fetch('/api/menus/dump-seed', {
      method: 'POST',
      headers: token.value ? { Authorization: `Bearer ${token.value}` } : {}
    })
    init({ message: t('menu_dump_seed_success'), color: 'success' })
  } catch (error) {
    init({ message: t('menu_dump_seed_failed'), color: 'danger' })
    console.error(error)
  }
}

const syncMenuSeed = async () => {
  if (!window.confirm(t('menu_sync_seed_confirm'))) return
  try {
    const token = useCookie('auth_token')
    await $fetch('/api/menus/sync-seed', {
      method: 'POST',
      headers: token.value ? { Authorization: `Bearer ${token.value}` } : {}
    })
    init({ message: t('menu_sync_seed_success'), color: 'success' })
    fetchMenus(true, true)
  } catch (error) {
    init({ message: t('menu_sync_seed_failed'), color: 'danger' })
    console.error(error)
  }
}

const token = useCookie('auth_token')
const userCookie = useCookie('user_data')
const currentUser = computed(() => {
  if (userCookie.value) {
    return typeof userCookie.value === 'string' ? JSON.parse(userCookie.value) : userCookie.value
  }
  return null
})

const adminMenus = ref([])

const loadAdminMenus = async () => {
  const result = await fetchMenus(true, true)
  adminMenus.value = result || []
}

const selectedMenu = ref(null)
const showAddModal = ref(false)
const showIconPickerModal = ref(false)
const iconPickerTarget = ref('')
const tempIcon = ref('')

const selectedMenuNameKo = ref('')
const selectedMenuNameEn = ref('')

const newMenuNameKo = ref('')
const newMenuNameEn = ref('')

const newMenu = ref({ name: '', path: '', icon: '', sortOrder: 0, requiredRole: '', parentId: null, isActive: true })

const extractNameParts = (rawName) => {
  if (!rawName) return { ko: '', en: '' }
  try {
    const parsed = typeof rawName === 'object' ? rawName : (String(rawName).trim().startsWith('{') ? JSON.parse(rawName) : null)
    if (parsed && typeof parsed === 'object') {
      return { ko: parsed.ko || '', en: parsed.en || '' }
    }
  } catch (e) {}
  const str = String(rawName).trim()
  return { ko: str, en: str }
}

const parseMenuName = (name) => {
  if (!name) return ''
  const currentLang = (locale?.value || 'ko').toLowerCase().startsWith('en') ? 'en' : 'ko'

  try {
    const parsed = typeof name === 'object' ? name : (String(name).trim().startsWith('{') ? JSON.parse(name) : null)
    if (parsed && typeof parsed === 'object') {
      const val = currentLang === 'en' ? (parsed.en || parsed.ko) : (parsed.ko || parsed.en)
      if (val) return String(val)
    }
  } catch (e) {}

  return String(name).trim()
}

const selectedMenuRoles = computed({
  get: () => {
    if (!selectedMenu.value) return []
    if (Array.isArray(selectedMenu.value.requiredRoles) && selectedMenu.value.requiredRoles.length > 0) {
      return selectedMenu.value.requiredRoles
    }
    if (selectedMenu.value.requiredRole) {
      return selectedMenu.value.requiredRole.split(',').map(r => r.trim()).filter(Boolean)
    }
    return []
  },
  set: (val) => {
    if (selectedMenu.value) {
      const arr = (val || []).map(r => String(r).trim()).filter(Boolean)
      selectedMenu.value.requiredRoles = arr
      selectedMenu.value.requiredRole = arr.join(',')
    }
  }
})

const selectedMenuHasChildren = computed(() => {
  return !!(selectedMenu.value && selectedMenu.value.children && selectedMenu.value.children.length > 0)
})

const newMenuRoles = computed({
  get: () => newMenu.value.requiredRole ? newMenu.value.requiredRole.split(',') : [],
  set: (val) => {
    newMenu.value.requiredRole = (val || []).join(',')
  }
})

const openIconPicker = (target) => {
  iconPickerTarget.value = target
  tempIcon.value = target === 'edit' ? (selectedMenu.value?.icon || '') : (newMenu.value?.icon || '')
  showIconPickerModal.value = true
}

const confirmIconSelection = () => {
  if (iconPickerTarget.value === 'edit' && selectedMenu.value) {
    selectedMenu.value.icon = tempIcon.value
  } else if (iconPickerTarget.value === 'add') {
    newMenu.value.icon = tempIcon.value
  }
  showIconPickerModal.value = false
}

// Convert to va-tree-view format with multilingual parsing
const formatToTreeNodes = (items) => {
  return items.map(item => ({
    id: item.id,
    label: parseMenuName(item.name),
    icon: item.icon,
    children: item.children ? formatToTreeNodes(item.children) : [],
    raw: item
  }))
}

const treeNodes = computed(() => {
  if (!adminMenus.value) return []
  return formatToTreeNodes(adminMenus.value)
})

const onNodeSelected = (node) => {
  if (node && node.raw) {
    selectedMenu.value = JSON.parse(JSON.stringify(node.raw))
    if (selectedMenu.value.isActive === undefined || selectedMenu.value.isActive === null) {
      selectedMenu.value.isActive = true
    }
    const parts = extractNameParts(selectedMenu.value.name)
    selectedMenuNameKo.value = parts.ko
    selectedMenuNameEn.value = parts.en
  }
}

const openAddModal = (parentId) => {
  newMenu.value = { name: '', path: '', icon: '', sortOrder: 0, requiredRole: '', parentId, isActive: true }
  newMenuNameKo.value = ''
  newMenuNameEn.value = ''
  showAddModal.value = true
}

const addMenu = async () => {
  try {
    const payload = {
      ...newMenu.value,
      name: JSON.stringify({
        ko: newMenuNameKo.value || newMenuNameEn.value,
        en: newMenuNameEn.value || newMenuNameKo.value
      })
    }
    await $fetch('/api/menus', {
      method: 'POST',
      headers: token.value ? { Authorization: `Bearer ${token.value}` } : {},
      body: payload
    })
    init({ message: t('creation_success'), color: 'success' })
    await refreshMenus()
    await loadAdminMenus()
  } catch (error) {
    init({ message: 'Failed to add menu', color: 'danger' })
  }
}

const saveMenu = async () => {
  if (!selectedMenu.value) return
  try {
    const payload = {
      ...selectedMenu.value,
      name: JSON.stringify({
        ko: selectedMenuNameKo.value || selectedMenuNameEn.value,
        en: selectedMenuNameEn.value || selectedMenuNameKo.value
      })
    }
    await $fetch(`/api/menus/${selectedMenu.value.id}`, {
      method: 'PUT',
      headers: token.value ? { Authorization: `Bearer ${token.value}` } : {},
      body: payload
    })
    init({ message: t('update_success'), color: 'success' })
    await refreshMenus()
    await loadAdminMenus()
    
    selectedMenu.value.name = payload.name
  } catch (error) {
    init({ message: 'Failed to update menu', color: 'danger' })
  }
}

const deleteMenu = async (id) => {
  try {
    await $fetch(`/api/menus/${id}`, {
      method: 'DELETE',
      headers: token.value ? { Authorization: `Bearer ${token.value}` } : {}
    })
    init({ message: t('delete_success'), color: 'success' })
    if (selectedMenu.value && selectedMenu.value.id === id) {
      selectedMenu.value = null
    }
    await refreshMenus()
    await loadAdminMenus()
  } catch (error) {
    init({ message: 'Failed to delete menu', color: 'danger' })
  }
}

const { initGlobalRoles } = useRoles()

onMounted(async () => {
  await initGlobalRoles()
  await loadAdminMenus()
})
</script>

<style scoped>
.menus-admin-container {
  padding: 20px;
}
.edit-form {
  background: var(--va-background-secondary);
  padding: 1.5rem;
  border-radius: 12px;
  border: 1px solid var(--va-background-border);
}
</style>
