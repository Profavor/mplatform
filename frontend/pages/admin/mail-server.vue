<template>
  <div style="display: flex; flex-direction: column; gap: 1.25rem; padding-bottom: 2rem;">
    <!-- Top Action Bar -->
    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); padding: 1rem 1.25rem; border-radius: 12px; border: 1px solid var(--va-background-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04);">
      <div style="display: flex; align-items: center; gap: 0.75rem;">
        <va-icon name="dns" size="large" color="primary" />
        <div>
          <h2 style="font-weight: 700; font-size: 1.35rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
            {{ $t('inbox.mail_server') }}
          </h2>
        </div>
      </div>
    </div>

    <!-- Status Section -->
    <va-card>
      <va-card-title>
        <div style="display: flex; align-items: center; gap: 0.5rem; font-size: 1.05rem;">
          <va-icon name="speed" color="primary" />
          <span>{{ $t('inbox.server_status') }}</span>
        </div>
      </va-card-title>
      <va-card-content>
        <div style="display: flex; gap: 2rem; align-items: center;">
          <div style="display: flex; align-items: center; gap: 0.5rem;">
            <div :style="{ width: '12px', height: '12px', borderRadius: '50%', backgroundColor: isServerConnected ? 'var(--va-success)' : 'var(--va-danger)' }"></div>
            <span style="font-weight: 600;">{{ isServerConnected ? 'Connected' : 'Disconnected' }}</span>
          </div>
          <div v-if="serverStatus.domain" style="display: flex; gap: 0.5rem;">
            <span style="color: var(--va-text-secondary);">Domain:</span>
            <span style="font-weight: 600;">{{ serverStatus.domain }}</span>
          </div>
        </div>
      </va-card-content>
    </va-card>

    <!-- Accounts Section -->
    <va-card style="flex: 1; display: flex; flex-direction: column;">
      <va-card-title style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--va-background-border); padding-bottom: 0.85rem;">
        <div style="display: flex; align-items: center; gap: 0.5rem; color: var(--va-text-primary); font-weight: 700; font-size: 1.05rem;">
          <va-icon name="email" color="primary" />
          <span>{{ $t('inbox.mail_accounts') }}</span>
        </div>
        <div style="display: flex; gap: 0.5rem;">
          <va-button preset="solid" color="success" icon="add" size="small" @click="showCreateModal = true">
            {{ $t('inbox.create_account') }}
          </va-button>
          <va-button preset="outline" color="primary" icon="sync" size="small" @click="confirmSync">
            {{ $t('inbox.sync_accounts') }}
          </va-button>
        </div>
      </va-card-title>
      <va-card-content style="flex: 1; padding: 0;">
        <div :class="isDark ? 'ag-theme-quartz-dark' : 'ag-theme-quartz'" style="width: 100%; height: 500px;">
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

    <!-- Create Modal -->
    <va-modal v-model="showCreateModal" :title="$t('inbox.create_account')" size="small">
      <div style="display: flex; flex-direction: column; gap: 1rem; padding: 1rem 0;">
        <va-input v-model="newAccount.email" label="Email" placeholder="username" required>
          <template #appendInner>
            <span style="color: var(--va-text-secondary);">@mplatform.com</span>
          </template>
        </va-input>
        <va-input v-model="newAccount.password" type="password" label="Password" required />
      </div>
      <template #footer>
        <va-button preset="plain" color="secondary" @click="showCreateModal = false">{{ $t('cancel', 'Cancel') }}</va-button>
        <va-button color="primary" @click="createAccount" :loading="isCreating">{{ $t('save', 'Save') }}</va-button>
      </template>
    </va-modal>

    <!-- Change Password Modal -->
    <va-modal v-model="showPasswordModal" :title="$t('inbox.change_password')" size="small">
      <div style="display: flex; flex-direction: column; gap: 1rem; padding: 1rem 0;">
        <div style="font-weight: 600; margin-bottom: 0.5rem;">{{ selectedEmail }}</div>
        <va-input v-model="newPassword" type="password" label="New Password" required />
      </div>
      <template #footer>
        <va-button preset="plain" color="secondary" @click="showPasswordModal = false">{{ $t('cancel', 'Cancel') }}</va-button>
        <va-button color="primary" @click="changePassword" :loading="isChangingPassword">{{ $t('save', 'Save') }}</va-button>
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

const { t } = useI18n()
const { customFetch } = useCustomFetch()
const { gridTheme, autoSizeStrategy, isDark } = useAgGridTheme()
const { init: initToast } = useToast()
const { confirm } = useModal()
const userStore = useUserStore()

useHead({ title: t('inbox.mail_server') })

const isServerConnected = ref(false)
const serverStatus = ref<any>({})

const showCreateModal = ref(false)
const isCreating = ref(false)
const newAccount = ref({ email: '', password: '' })

const showPasswordModal = ref(false)
const isChangingPassword = ref(false)
const selectedEmail = ref('')
const newPassword = ref('')

let gridApi: any = null

onMounted(async () => {
  await userStore.fetchUserMap()
  fetchServerStatus()
})

const fetchServerStatus = async () => {
  try {
    const res = await customFetch('/api/admin/mail/status')
    if (res) {
      serverStatus.value = res
      isServerConnected.value = res.status === 'ok' || res.connected === true
      if (!serverStatus.value.domain) {
        serverStatus.value.domain = 'mplatform.com'
      }
    }
  } catch (e) {
    isServerConnected.value = false
    serverStatus.value.domain = 'mplatform.com'
  }
}

const defaultColDef = {
  sortable: true,
  resizable: true,
  minWidth: 120
}

const formatBytes = (bytes: number) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const columnDefs = computed(() => [
  { field: 'email', headerName: 'Email', width: 250 },
  {
    field: 'userId',
    headerName: 'User Name',
    width: 200,
    valueGetter: (params: any) => {
      const id = params.data?.userId
      return id ? userStore.getUserName(id) : '-'
    }
  },
  {
    field: 'active',
    headerName: 'Status',
    width: 120,
    cellRenderer: (params: any) => {
      const isActive = params.value
      const color = isActive ? 'success' : 'danger'
      const text = isActive ? 'Active' : 'Inactive'
      return `<span style="padding: 3px 10px; border-radius: 12px; font-size: 0.75rem; font-weight: bold; background-color: var(--va-${color}); color: white;">${text}</span>`
    }
  },
  {
    field: 'quotaUsed',
    headerName: t('inbox.quota_used'),
    width: 150,
    valueFormatter: (params: any) => formatBytes(params.value)
  },
  {
    field: 'quotaLimit',
    headerName: t('inbox.quota_limit'),
    width: 150,
    valueFormatter: (params: any) => formatBytes(params.value)
  },
  {
    headerName: 'Actions',
    width: 250,
    sortable: false,
    cellRenderer: (params: any) => {
      if (!params.data) return ''
      return `
        <div style="display: flex; gap: 8px; align-items: center; height: 100%;">
          <button class="va-button va-button--small va-button--outline va-button--primary" data-action="password" data-email="${params.data.email}">
            ${t('inbox.change_password')}
          </button>
          <button class="va-button va-button--small va-button--outline va-button--danger" data-action="delete" data-email="${params.data.email}">
            ${t('inbox.delete_account')}
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
      const res = await customFetch(`/api/admin/mail/accounts?page=${page}&size=${size}`)
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

  // Event delegation for action buttons
  params.api.getGridOption('onCellClicked', (e: any) => {
    if (!e.event?.target) return
    const target = e.event.target as HTMLElement
    const btn = target.closest('button[data-action]')
    if (!btn) return
    
    const action = btn.getAttribute('data-action')
    const email = btn.getAttribute('data-email')
    
    if (action === 'password' && email) {
      selectedEmail.value = email
      newPassword.value = ''
      showPasswordModal.value = true
    } else if (action === 'delete' && email) {
      confirmDelete(email)
    }
  })
}

// Add native event listener for cell clicks to handle inner HTML buttons since AG-Grid Vue3 wrapper can sometimes be tricky
const setupCellClickDelegate = () => {
  if (typeof document === 'undefined') return
  document.addEventListener('click', (e) => {
    const target = e.target as HTMLElement
    const btn = target.closest('button[data-action]')
    if (!btn) return
    
    // Ensure it's from our grid
    if (!btn.closest('.ag-theme-quartz') && !btn.closest('.ag-theme-quartz-dark')) return
    
    e.preventDefault()
    e.stopPropagation()
    
    const action = btn.getAttribute('data-action')
    const email = btn.getAttribute('data-email')
    
    if (action === 'password' && email) {
      selectedEmail.value = email
      newPassword.value = ''
      showPasswordModal.value = true
    } else if (action === 'delete' && email) {
      confirmDelete(email)
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

const createAccount = async () => {
  if (!newAccount.value.email || !newAccount.value.password) {
    initToast({ message: 'Email and password are required', color: 'warning' })
    return
  }

  isCreating.value = true
  try {
    const fullEmail = newAccount.value.email.includes('@') 
      ? newAccount.value.email 
      : `${newAccount.value.email}@mplatform.com`
      
    await customFetch('/api/admin/mail/accounts', {
      method: 'POST',
      body: { email: fullEmail, password: newAccount.value.password }
    })
    initToast({ message: 'Account created successfully', color: 'success' })
    showCreateModal.value = false
    newAccount.value = { email: '', password: '' }
    refreshGrid()
  } catch (e: any) {
    initToast({ message: e.message || 'Failed to create account', color: 'danger' })
  } finally {
    isCreating.value = false
  }
}

const changePassword = async () => {
  if (!newPassword.value) {
    initToast({ message: 'Password is required', color: 'warning' })
    return
  }

  isChangingPassword.value = true
  try {
    await customFetch(`/api/admin/mail/accounts/${encodeURIComponent(selectedEmail.value)}/password`, {
      method: 'PUT',
      body: { password: newPassword.value }
    })
    initToast({ message: 'Password updated successfully', color: 'success' })
    showPasswordModal.value = false
  } catch (e: any) {
    initToast({ message: e.message || 'Failed to update password', color: 'danger' })
  } finally {
    isChangingPassword.value = false
  }
}

const confirmDelete = async (email: string) => {
  const result = await confirm({
    title: t('inbox.delete_account'),
    message: `Are you sure you want to delete the account ${email}?`,
    okText: t('delete', 'Delete'),
    cancelText: t('cancel', 'Cancel')
  })

  if (result) {
    try {
      await customFetch(`/api/admin/mail/accounts/${encodeURIComponent(email)}`, {
        method: 'DELETE'
      })
      initToast({ message: 'Account deleted successfully', color: 'success' })
      refreshGrid()
    } catch (e: any) {
      initToast({ message: e.message || 'Failed to delete account', color: 'danger' })
    }
  }
}

const confirmSync = async () => {
  const result = await confirm({
    title: t('inbox.sync_accounts'),
    message: t('inbox.sync_accounts_confirm'),
    okText: t('inbox.sync_accounts'),
    cancelText: t('cancel', 'Cancel')
  })

  if (result) {
    try {
      await customFetch('/api/admin/mail/accounts/sync', { method: 'POST' })
      initToast({ message: 'Accounts synchronized successfully', color: 'success' })
      refreshGrid()
    } catch (e: any) {
      initToast({ message: e.message || 'Failed to sync accounts', color: 'danger' })
    }
  }
}
</script>

<style scoped>
.va-button {
  font-weight: 600;
}
</style>
