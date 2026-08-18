<template>
  <div class="message-list-container">
    <div class="message-list-toolbar">
      <va-input v-model="localKeyword" :placeholder="$t('inbox.search_users')" class="search-input" clearable @update:modelValue="onSearch">
        <template #prependInner>
          <va-icon name="search" />
        </template>
      </va-input>
      <div class="bulk-actions" v-if="selectedRows.length > 0">
        <va-button preset="secondary" size="small" icon="drafts" @click="markRead">{{ $t('inbox.mark_read') }}</va-button>
        <va-button preset="secondary" size="small" icon="archive" @click="archive">{{ $t('inbox.move_to_archive') }}</va-button>
        <va-button preset="secondary" size="small" color="danger" icon="delete" @click="trash">{{ $t('inbox.move_to_trash') }}</va-button>
      </div>
    </div>
    
    <div class="grid-wrapper" :class="{ 'ag-theme-quartz-dark': isDark, 'ag-theme-quartz': !isDark }">
      <AgGridVue
        style="width: 100%; height: 100%;"
        :gridOptions="gridOptions"
        @grid-ready="onGridReady"
        @row-clicked="onRowClicked"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { AgGridVue } from 'ag-grid-vue3'
import { useI18n } from 'vue-i18n'
import { useInbox } from '~/composables/useInbox'
import { useUserStore } from '~/stores/useUserStore'
import { useColors } from 'vuestic-ui'
import { useTimezoneDate } from '~/composables/useTimezoneDate'
import type { GridOptions, GridReadyEvent, IServerSideDatasource } from 'ag-grid-community'

const props = defineProps<{
  folder: string
  searchKeyword: string
}>()

const emit = defineEmits(['select-message', 'refresh', 'update:searchKeyword'])
const { t } = useI18n()
const { fetchMessages, customFetch } = useInbox()
const userStore = useUserStore()
const { currentPresetName } = useColors()
const { formatWithTimezone } = useTimezoneDate()

const isDark = computed(() => currentPresetName.value === 'dark')

const localKeyword = ref(props.searchKeyword)
let gridApi: any = null
const selectedRows = ref<any[]>([])

const onSearch = () => {
  emit('update:searchKeyword', localKeyword.value)
  if (gridApi) gridApi.refreshServerSide()
}

const handleRealtimeInboxRefresh = () => {
  if (gridApi) {
    gridApi.refreshServerSide()
  }
}

watch(() => props.folder, () => {
  if (gridApi) gridApi.refreshServerSide()
})

onMounted(() => {
  if (typeof window !== 'undefined') {
    window.addEventListener('inbox-refresh-counts', handleRealtimeInboxRefresh)
    window.addEventListener('inbox-message-received', handleRealtimeInboxRefresh)
  }
})

onUnmounted(() => {
  if (typeof window !== 'undefined') {
    window.removeEventListener('inbox-refresh-counts', handleRealtimeInboxRefresh)
    window.removeEventListener('inbox-message-received', handleRealtimeInboxRefresh)
  }
})

const onRowClicked = (event: any) => {
  emit('select-message', event.data)
}

const onGridReady = (params: GridReadyEvent) => {
  gridApi = params.api
  const dataSource: IServerSideDatasource = {
    getRows: async (params) => {
      try {
        const page = Math.floor((params.request.startRow || 0) / 50)
        const size = 50
        const res: any = await fetchMessages(props.folder, page, size, localKeyword.value)
        const rowData = res?.content || res?.data?.value?.content || res?.data?.content || (Array.isArray(res) ? res : [])
        const totalCount = res?.totalElements ?? res?.data?.value?.totalElements ?? res?.data?.totalElements ?? rowData.length
        params.success({ rowData, rowCount: totalCount })
      } catch (e) {
        console.error('Failed to load inbox messages', e)
        params.success({ rowData: [], rowCount: 0 })
      }
    }
  }
  gridApi.setGridOption('serverSideDatasource', dataSource)
}

const markRead = async () => {
  const ids = selectedRows.value.map(r => r.id)
  if (!ids.length) return
  await customFetch('/inbox/messages/read', { method: 'PUT', body: { messageIds: ids } })
  if (gridApi) gridApi.refreshServerSide()
  emit('refresh')
}

const archive = async () => {
  const ids = selectedRows.value.map(r => r.id)
  if (!ids.length) return
  await customFetch('/inbox/messages/move', { method: 'PUT', body: { messageIds: ids, targetFolder: 'ARCHIVE' } })
  if (gridApi) gridApi.refreshServerSide()
  emit('refresh')
}

const trash = async () => {
  const ids = selectedRows.value.map(r => r.id)
  if (!ids.length) return
  await customFetch('/inbox/messages/move', { method: 'PUT', body: { messageIds: ids, targetFolder: 'TRASH' } })
  if (gridApi) gridApi.refreshServerSide()
  emit('refresh')
}

const gridOptions = computed<GridOptions>(() => {
  return {
    rowModelType: 'serverSide',
    cacheBlockSize: 50,
    getRowId: (params: any) => {
      if (params.data?.recipientId) return String(params.data.recipientId)
      if (params.data?.id) return String(params.data.id) + (params.data?.folder ? '_' + params.data.folder : '')
      return String(Math.random())
    },
    rowSelection: {
      mode: 'multiRow',
      checkboxes: true,
      headerCheckbox: true,
      enableClickSelection: false
    },
    onSelectionChanged: () => {
      selectedRows.value = gridApi?.getSelectedRows() || []
    },
    columnDefs: [
      { 
        field: 'isStarred', headerName: '', width: 50, pinned: 'left',
        cellRenderer: (params: any) => {
          return params.value ? '★' : '☆'
        },
        onCellClicked: async (params: any) => {
          const msg = params.data
          if (!msg) return
          await customFetch(`/inbox/messages/${msg.id}/star`, { method: 'PUT', body: { isStarred: !msg.isStarred } })
          if (gridApi) gridApi.refreshServerSide()
          emit('refresh')
        }
      },
      {
        field: 'sender', headerName: t('inbox.sender'), width: 150,
        valueGetter: (params: any) => {
          const msg = params.data
          if (!msg) return ''
          if (props.folder === 'SENT' || props.folder === 'DRAFT') {
            return msg.toRecipients?.map((r: any) => r.name || r.email).join(', ') || ''
          }
          return userStore.getUserName(msg.senderId, msg.senderName) || msg.senderEmail
        },
        cellStyle: (params: any) => params.data && !params.data.isRead ? { fontWeight: 'bold' } : {}
      },
      {
        field: 'subject', headerName: t('inbox.subject'), flex: 1,
        cellStyle: (params: any) => params.data && !params.data.isRead ? { fontWeight: 'bold' } : {}
      },
      {
        field: 'importance', headerName: t('inbox.importance'), width: 100,
        cellRenderer: (params: any) => {
          if (params.value === 'URGENT') return '🔴'
          if (params.value === 'HIGH') return '🟠'
          return ''
        }
      },
      {
        field: 'hasAttachments', headerName: '📎', width: 60,
        cellRenderer: (params: any) => params.value ? '📎' : ''
      },
      {
        field: 'createdAt', headerName: t('inbox.date'), width: 180,
        valueFormatter: (params: any) => {
          if (!params.value) return ''
          return formatWithTimezone(params.value)
        }
      }
    ]
  }
})
</script>

<style scoped>
.message-list-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--va-background-primary);
}
.message-list-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid var(--va-background-border);
  gap: 1rem;
}
.search-input {
  max-width: 300px;
}
.bulk-actions {
  display: flex;
  gap: 0.5rem;
}
.grid-wrapper {
  flex-grow: 1;
  width: 100%;
}
</style>
