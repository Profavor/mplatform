<template>
  <div style="display: flex; flex-direction: column; gap: 1.25rem; padding-bottom: 2rem;">
    <!-- Top Action Bar -->
    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); padding: 1rem 1.25rem; border-radius: 12px; border: 1px solid var(--va-background-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04);">
      <div style="display: flex; align-items: center; gap: 0.75rem;">
        <va-icon name="mark_email_read" size="large" color="primary" />
        <div>
          <h2 style="font-weight: 700; font-size: 1.35rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
            {{ $t('inbox.mailing_lists') }}
          </h2>
        </div>
      </div>
      <div style="display: flex; gap: 0.75rem; align-items: center;">
        <va-button preset="solid" color="success" icon="add" size="small" @click="openCreateModal" style="font-weight: 700;">
          {{ $t('inbox.create_mailing_list') }}
        </va-button>
        <va-button preset="outline" color="primary" icon="sync" size="small" @click="syncAliases">
          Sync Postfix Aliases
        </va-button>
      </div>
    </div>

    <!-- Mailing Lists Grid -->
    <va-card style="flex: 1; display: flex; flex-direction: column;">
      <va-card-title style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--va-background-border); padding-bottom: 0.85rem;">
        <div style="display: flex; align-items: center; gap: 0.5rem; color: var(--va-text-primary); font-weight: 700; font-size: 1.05rem;">
          <va-icon name="list_alt" color="primary" />
          <span>{{ $t('inbox.mailing_lists') }}</span>
        </div>
      </va-card-title>
      <va-card-content style="flex: 1; padding: 0;">
        <div :class="isDark ? 'ag-theme-quartz-dark' : 'ag-theme-quartz'" style="width: 100%; height: 600px;">
          <AgGridVue
            class="ag-theme-quartz"
            :style="{ height: '100%', width: '100%' }"
            :columnDefs="columnDefs"
            :defaultColDef="defaultColDef"
            rowModelType="infinite"
            :cacheBlockSize="20"
            :rowHeight="48"
            :headerHeight="48"
            :gridOptions="gridTheme"
            :autoSizeStrategy="autoSizeStrategy"
            @grid-ready="onGridReady"
          />
        </div>
      </va-card-content>
    </va-card>

    <!-- Create/Edit Modal -->
    <va-modal v-model="showModal" :title="isEditing ? $t('inbox.edit_mailing_list') : $t('inbox.create_mailing_list')" size="medium">
      <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 1rem 0;">
        <va-input v-model="formData.groupName" label="Group Name" placeholder="e.g. Development Team" required />
        <va-input v-model="formData.email" :label="$t('inbox.group_email')" placeholder="dev-team" required>
          <template #appendInner>
            <span style="color: var(--va-text-secondary);">@mplatform.com</span>
          </template>
        </va-input>
        
        <!-- Description -->
        <div style="display: flex; flex-direction: column; gap: 0.5rem;">
          <label style="font-size: 0.85rem; font-weight: 600; color: var(--va-text-primary);">Description</label>
          <va-input v-model="formData.description.ko" placeholder="설명 (한국어)" />
          <va-input v-model="formData.description.en" placeholder="Description (English)" />
        </div>

        <!-- Members Section -->
        <div style="display: flex; flex-direction: column; gap: 1rem; background: var(--va-background-element); padding: 1rem; border-radius: 8px; border: 1px solid var(--va-background-border);">
          <div style="font-weight: 600; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
            <va-icon name="people" color="primary" size="small" />
            Members
          </div>
          
          <InboxRecipientPicker 
            v-model="formData.members" 
            :label="$t('inbox.search_users')" 
          />
          
          <div style="display: flex; gap: 0.5rem; align-items: flex-end;">
            <va-input v-model="externalEmailInput" :label="$t('inbox.add_external_email')" placeholder="user@example.com" style="flex: 1;" @keyup.enter="addExternalEmail" />
            <va-button color="primary" preset="outline" icon="add" @click="addExternalEmail">Add</va-button>
          </div>
          
          <!-- Member List with Remove Buttons -->
          <div v-if="formData.members.length > 0" style="display: flex; flex-direction: column; gap: 0.5rem; margin-top: 0.5rem;">
            <label style="font-size: 0.8rem; font-weight: 600; color: var(--va-text-secondary);">Selected Members ({{ formData.members.length }})</label>
            <div style="display: flex; flex-wrap: wrap; gap: 0.5rem;">
              <va-chip
                v-for="member in formData.members"
                :key="member"
                closeable
                @click:close="removeMember(member)"
                size="small"
                color="info"
                outline
              >
                {{ resolveMemberName(member) }}
              </va-chip>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <va-button preset="plain" color="secondary" @click="showModal = false">{{ $t('cancel', 'Cancel') }}</va-button>
        <va-button color="primary" @click="saveMailingList" :loading="isSaving">{{ $t('save', 'Save') }}</va-button>
      </template>
    </va-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast, useModal } from 'vuestic-ui'
import { AgGridVue } from 'ag-grid-vue3'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { useAgGridTheme } from '~/composables/useAgGridTheme'
import { useUserStore } from '~/stores/useUserStore'
import { useHead } from '#imports'
import InboxRecipientPicker from '~/components/inbox/InboxRecipientPicker.vue'

const { t, locale } = useI18n()
const { customFetch } = useCustomFetch()
const { gridTheme, autoSizeStrategy, isDark } = useAgGridTheme()
const { init: initToast } = useToast()
const { confirm } = useModal()
const userStore = useUserStore()

useHead({ title: t('inbox.mailing_lists') })

onMounted(async () => {
  await userStore.fetchUserMap()
})

const showModal = ref(false)
const isEditing = ref(false)
const isSaving = ref(false)
const editingId = ref<string | null>(null)
const externalEmailInput = ref('')

const formData = ref({
  groupName: '',
  email: '',
  description: { ko: '', en: '' },
  members: [] as string[]
})

let gridApi: any = null

const defaultColDef = {
  sortable: true,
  resizable: true,
  minWidth: 120
}

const columnDefs = computed(() => [
  { field: 'groupName', headerName: 'Group Name', width: 200 },
  { field: 'email', headerName: 'Email Address', width: 250 },
  {
    field: 'members',
    headerName: 'Members',
    width: 150,
    valueFormatter: (params: any) => {
      const arr = params.value || []
      return t('inbox.member_count', { count: arr.length })
    }
  },
  {
    field: 'active',
    headerName: 'Status',
    width: 120,
    cellRenderer: (params: any) => {
      const isActive = params.value !== false
      const color = isActive ? 'success' : 'danger'
      const text = isActive ? 'Active' : 'Inactive'
      return `<span style="padding: 3px 10px; border-radius: 12px; font-size: 0.75rem; font-weight: bold; background-color: var(--va-${color}); color: white;">${text}</span>`
    }
  },
  {
    headerName: 'Actions',
    width: 200,
    sortable: false,
    cellRenderer: (params: any) => {
      if (!params.data) return ''
      return `
        <div style="display: flex; gap: 8px; align-items: center; height: 100%;">
          <button class="va-button va-button--small va-button--outline va-button--primary" data-action="edit" data-id="${params.data.id}">
            Edit
          </button>
          <button class="va-button va-button--small va-button--outline va-button--danger" data-action="delete" data-id="${params.data.id}">
            Delete
          </button>
        </div>
      `
    }
  }
])

const createDatasource = () => ({
  getRows: async (params: any) => {
    const startRow = params.startRow || 0
    const endRow = params.endRow || 20
    const size = Math.max(endRow - startRow, 1)
    const page = Math.floor(startRow / size)

    try {
      const res = await customFetch(`/api/admin/mailing-lists?page=${page}&size=${size}`)
      const content = res?.content || res?.data || (Array.isArray(res) ? res : [])
      const total = res?.totalElements ?? res?.total ?? content.length

      params.successCallback(content, total)
    } catch (e) {
      console.error(e)
      params.failCallback()
    }
  }
})

const onGridReady = (params: any) => {
  gridApi = params.api
  gridApi.setGridOption('datasource', createDatasource())
}

const setupCellClickDelegate = () => {
  if (typeof document === 'undefined') return
  document.addEventListener('click', async (e) => {
    const target = e.target as HTMLElement
    const btn = target.closest('button[data-action]')
    if (!btn) return
    
    if (!btn.closest('.ag-theme-quartz') && !btn.closest('.ag-theme-quartz-dark')) return
    
    e.preventDefault()
    e.stopPropagation()
    
    const action = btn.getAttribute('data-action')
    const id = btn.getAttribute('data-id')
    
    if (!id) return

    if (action === 'edit') {
      await openEditModal(id)
    } else if (action === 'delete') {
      await confirmDelete(id)
    }
  })
}

onMounted(() => {
  setupCellClickDelegate()
})

const refreshGrid = () => {
  if (gridApi) {
    gridApi.setGridOption('datasource', createDatasource())
  }
}

const resolveMemberName = (idOrEmail: string) => {
  if (idOrEmail.includes('@')) return idOrEmail
  return userStore.getUserName(idOrEmail)
}

const addExternalEmail = () => {
  const val = externalEmailInput.value.trim()
  if (!val) return
  if (!val.includes('@')) {
    initToast({ message: 'Please enter a valid email address', color: 'warning' })
    return
  }
  if (!formData.value.members.includes(val)) {
    formData.value.members.push(val)
  }
  externalEmailInput.value = ''
}

const removeMember = (member: string) => {
  formData.value.members = formData.value.members.filter(m => m !== member)
}

const openCreateModal = () => {
  isEditing.value = false
  editingId.value = null
  formData.value = {
    groupName: '',
    email: '',
    description: { ko: '', en: '' },
    members: []
  }
  externalEmailInput.value = ''
  showModal.value = true
}

const openEditModal = async (id: string) => {
  try {
    const data = await customFetch(`/api/admin/mailing-lists/${id}`)
    if (data) {
      isEditing.value = true
      editingId.value = id
      
      const rawEmail = data.email || ''
      const emailPrefix = rawEmail.includes('@') ? rawEmail.split('@')[0] : rawEmail

      formData.value = {
        groupName: data.groupName || '',
        email: emailPrefix,
        description: data.description || { ko: '', en: '' },
        members: [...(data.members || [])]
      }
      externalEmailInput.value = ''
      showModal.value = true
    }
  } catch (e: any) {
    initToast({ message: 'Failed to load mailing list details', color: 'danger' })
  }
}

const saveMailingList = async () => {
  if (!formData.value.groupName || !formData.value.email) {
    initToast({ message: 'Group Name and Email are required', color: 'warning' })
    return
  }

  isSaving.value = true
  try {
    const fullEmail = `${formData.value.email}@mplatform.com`
    const payload = {
      ...formData.value,
      email: fullEmail
    }

    if (isEditing.value && editingId.value) {
      await customFetch(`/api/admin/mailing-lists/${editingId.value}`, {
        method: 'PUT',
        body: payload
      })
      initToast({ message: 'Mailing list updated successfully', color: 'success' })
    } else {
      await customFetch('/api/admin/mailing-lists', {
        method: 'POST',
        body: payload
      })
      initToast({ message: 'Mailing list created successfully', color: 'success' })
    }
    
    showModal.value = false
    refreshGrid()
  } catch (e: any) {
    initToast({ message: e.message || 'Failed to save mailing list', color: 'danger' })
  } finally {
    isSaving.value = false
  }
}

const confirmDelete = async (id: string) => {
  const result = await confirm({
    title: 'Delete Mailing List',
    message: 'Are you sure you want to delete this mailing list?',
    okText: t('delete', 'Delete'),
    cancelText: t('cancel', 'Cancel')
  })

  if (result) {
    try {
      await customFetch(`/api/admin/mailing-lists/${id}`, { method: 'DELETE' })
      initToast({ message: 'Mailing list deleted successfully', color: 'success' })
      refreshGrid()
    } catch (e: any) {
      initToast({ message: e.message || 'Failed to delete mailing list', color: 'danger' })
    }
  }
}

const syncAliases = async () => {
  try {
    await customFetch('/api/admin/mailing-lists/sync-aliases', { method: 'POST' })
    initToast({ message: 'Postfix aliases synchronized successfully', color: 'success' })
  } catch (e: any) {
    initToast({ message: e.message || 'Failed to sync aliases', color: 'danger' })
  }
}
</script>

<style scoped>
.va-button {
  font-weight: 600;
}
</style>
