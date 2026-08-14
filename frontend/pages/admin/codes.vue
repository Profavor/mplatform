<template>
  <div style="display: flex; flex-direction: column; gap: 1.5rem; padding-bottom: 2rem; height: 100%; min-height: 85vh;">
    <!-- Premium Top Action Bar -->
    <div style="display: flex; justify-content: space-between; align-items: center; padding: 1.5rem 2rem; border-radius: 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.06); background: linear-gradient(135deg, var(--va-background-primary) 0%, var(--va-background-element) 100%); border: 1px solid var(--va-background-border); position: relative; overflow: hidden;">
      <div style="position: absolute; top: -50px; right: -50px; width: 150px; height: 150px; background: var(--va-primary); opacity: 0.05; border-radius: 50%; filter: blur(30px);"></div>
      <div style="position: absolute; bottom: -50px; left: 10%; width: 200px; height: 200px; background: var(--va-info); opacity: 0.05; border-radius: 50%; filter: blur(40px);"></div>

      <div style="display: flex; align-items: center; gap: 1rem; position: relative; z-index: 1;">
        <div style="background: var(--va-background-element); padding: 0.75rem; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05);">
          <va-icon name="library_books" size="28px" color="primary" />
        </div>
        <div>
          <h2 style="font-weight: 800; font-size: 1.5rem; margin: 0 0 0.25rem 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem; letter-spacing: -0.5px;">
            {{ t('code_management.title') }}
            <va-badge text="System Core" color="info" size="small" style="font-weight: 600; letter-spacing: 0.5px;" />
          </h2>
          <span style="font-size: 0.9rem; color: var(--va-text-secondary); opacity: 0.85;">
            {{ t('code_management.desc') }}
          </span>
        </div>
      </div>

      <div style="display: flex; gap: 0.75rem; align-items: center; position: relative; z-index: 1;">
        <va-button preset="secondary" color="success" icon="sync" size="medium" @click="syncCodeSeed" :loading="isSyncing" style="font-weight: 600; border-radius: 8px;">
          {{ t('code_management.sync_codes') }}
        </va-button>
        <va-button preset="secondary" color="warning" icon="save" size="medium" @click="dumpCodeSeed" :loading="isDumping" style="font-weight: 600; border-radius: 8px;">
          {{ t('code_management.dump_codes') }}
        </va-button>
        <div style="width: 1px; height: 24px; background: var(--va-background-border); margin: 0 0.5rem;"></div>
        <va-button preset="outline" color="primary" icon="refresh" size="medium" @click="refreshGrid" style="border-radius: 8px;">
          {{ t('refresh') }}
        </va-button>
      </div>
    </div>

    <!-- Main Content Layout -->
    <div style="display: flex; gap: 1.5rem; flex-grow: 1;">
      
      <!-- Left Panel: Code Groups -->
      <va-card style="flex: 1; border-radius: 16px; box-shadow: 0 4px 20px rgba(0,0,0,0.03); border: 1px solid var(--va-background-border); display: flex; flex-direction: column; overflow: hidden; transition: all 0.3s ease;" class="hover-elevate">
        <div style="padding: 1.25rem 1.5rem; border-bottom: 1px solid var(--va-background-border); background: var(--va-background-primary); display: flex; justify-content: space-between; align-items: center;">
          <h3 style="font-weight: 700; font-size: 1.1rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center;">
            <va-icon name="folder_special" size="small" class="mr-2" color="secondary"/>
            {{ t('code_management.code_groups') }}
          </h3>
          <va-button icon="add" size="small" color="primary" style="border-radius: 6px; font-weight: 600;" @click="openGroupModal()">
            {{ t('code_management.add') }}
          </va-button>
        </div>
        
        <div style="padding: 0.75rem 1.5rem; border-bottom: 1px solid var(--va-background-border); background: var(--va-background-primary); display: flex; gap: 0.5rem; align-items: center;">
            <va-input 
              v-model="searchKeyword" 
              placeholder="Search by Code or Name..." 
              class="w-full" 
              clearable 
              @clear="onSearch" 
              @keyup.enter="onSearch"
              style="flex-grow: 1;"
            >
              <template #prependInner>
                <va-icon name="search" color="secondary" size="small" />
              </template>
            </va-input>
            <va-button color="primary" icon="search" @click="onSearch" />
          </div>

        <div style="flex-grow: 1; position: relative;" :class="{ 'ag-theme-quartz-dark': isDark }">
          <ag-grid-vue
            style="width: 100%; height: 100%;"
            :theme="gridTheme"
            :columnDefs="groupColumnDefs"
            :rowModelType="'serverSide'"
            :pagination="true"
            :paginationPageSize="20"
            :cacheBlockSize="20"
            @grid-ready="onGridReady"
            @rowClicked="onRowClicked"
            @cellClicked="onGroupCellClicked"
            :rowSelection="{ mode: 'singleRow', headerCheckbox: false }"
            :getRowId="(params) => params.data.id"
            :suppressCellFocus="true"
          >
          </ag-grid-vue>
        </div>
      </va-card>

      <!-- Right Panel: Code Details -->
      <va-card style="flex: 1.5; border-radius: 16px; box-shadow: 0 4px 20px rgba(0,0,0,0.03); border: 1px solid var(--va-background-border); display: flex; flex-direction: column; overflow: hidden; transition: all 0.3s ease;" class="hover-elevate">
        <div style="padding: 1.25rem 1.5rem; border-bottom: 1px solid var(--va-background-border); background: var(--va-background-primary); display: flex; justify-content: space-between; align-items: center;">
          <h3 style="font-weight: 700; font-size: 1.1rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center;">
            <va-icon name="list_alt" size="small" class="mr-2" color="secondary"/>
            {{ t('code_management.code_details') }}
            <va-badge v-if="selectedGroup" :text="selectedGroup.groupCode" color="primary" size="small" style="margin-left: 0.75rem; font-weight: 700;" />
          </h3>
          <va-button icon="add" size="small" color="primary" style="border-radius: 6px; font-weight: 600;" :disabled="!selectedGroup" @click="openDetailModal()">
            {{ t('code_management.add') }}
          </va-button>
        </div>
        
        <div style="flex-grow: 1; overflow: auto; background: var(--va-background-primary); position: relative;">
          <!-- Empty State -->
          <div v-if="!selectedGroup" style="position: absolute; top: 0; left: 0; right: 0; bottom: 0; display: flex; flex-direction: column; justify-content: center; align-items: center; background: rgba(0,0,0,0.01);">
            <div style="width: 80px; height: 80px; border-radius: 50%; background: var(--va-background-element); display: flex; justify-content: center; align-items: center; margin-bottom: 1.5rem; box-shadow: 0 8px 24px rgba(0,0,0,0.05);">
              <va-icon name="touch_app" size="36px" color="secondary" style="opacity: 0.7;" />
            </div>
            <h4 style="font-size: 1.2rem; font-weight: 700; color: var(--va-text-primary); margin: 0 0 0.5rem 0;">No Group Selected</h4>
            <p style="color: var(--va-text-secondary); font-size: 0.95rem; text-align: center; max-width: 300px; line-height: 1.5;">
              {{ t('code_management.select_group_msg') }}
            </p>
          </div>
          
          <!-- Data Table -->
          <div v-else style="position: absolute; top: 0; left: 0; right: 0; bottom: 0;" class="code-management-grid" :class="{ 'ag-theme-quartz-dark': isDark }">
            <ag-grid-vue
              style="width: 100%; height: 100%;"
              :theme="gridTheme"
              :columnDefs="detailColumnDefs"
              :rowData="details"
              :rowModelType="'clientSide'"
              :pagination="false"
              @cellClicked="onDetailCellClicked"
              :getRowId="(params) => params.data.id"
              :suppressCellFocus="true"
              :suppressRowHoverHighlight="true"
            >
            </ag-grid-vue>
          </div>
        </div>
      </va-card>
    </div>

    <!-- Modals (Decoupled Components) -->
    <CodeGroupModal
      v-model="showGroupModal"
      :group-form="groupForm"
      :editing-group="editingGroup"
      @save="saveGroup"
    />

    <CodeDetailModal
      v-model="showDetailModal"
      :detail-form="detailForm"
      :editing-detail="editingDetail"
      @save="saveDetail"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, h } from 'vue'
import { usePageTitle } from '~/composables/usePageTitle'
import { useI18n } from 'vue-i18n'
import { useToast, useColors } from 'vuestic-ui'
import { useCookie } from '#app'
import { AgGridVue } from 'ag-grid-vue3'
import { useCodeStore } from '~/stores/useCodeStore'
import CodeGroupModal from '~/components/admin/CodeGroupModal.vue'
import CodeDetailModal from '~/components/admin/CodeDetailModal.vue'
import 'vue3-emoji-picker/css'
import 'ag-grid-enterprise'

const { t } = useI18n()
usePageTitle('code_management.title')

const { init } = useToast()
const { currentPresetName } = useColors()
const isDark = computed(() => currentPresetName.value === 'dark')
const token = useCookie('auth_token')
const locale = useCookie('locale', { default: () => 'ko' })
const codeStore = useCodeStore()
const { gridTheme } = useAgGridTheme()

const getLocaleName = (nameObj) => {
  if (!nameObj) return ''
  if (typeof nameObj === 'string') return nameObj
  return nameObj[locale.value] || nameObj.ko || nameObj.en || ''
}

const details = ref([])
const selectedGroup = ref(null)
const isSyncing = ref(false)
const isDumping = ref(false)
const gridApi = ref(null)

const groupColumnDefs = computed(() => [
    { field: 'groupCode', headerName: t('code_management.group_code'), sortable: true, flex: 1 },
    { field: 'name', headerName: t('code_management.name'), valueGetter: (params) => getLocaleName(params.data?.name), flex: 1 },
    { 
      field: 'isActive', 
      headerName: t('code_management.status'),
      width: 100,
      cellRenderer: (params) => {
        if (params.value === undefined) return ''
        return params.value 
          ? '<span style="background: var(--va-success); color: white; padding: 2px 8px; border-radius: 12px; font-size: 0.75rem; font-weight: bold;">Active</span>' 
          : '<span style="background: var(--va-secondary); color: white; padding: 2px 8px; border-radius: 12px; font-size: 0.75rem; font-weight: bold;">Inactive</span>'
      }
    },
    { 
      field: 'actions', 
      headerName: '', 
      sortable: false,
      width: 80,
      cellRenderer: () => {
        return `<div style="display: flex; gap: 0.2rem; justify-content: flex-end; opacity: 0.8; height: 100%; align-items: center;">
                  <button class="ag-action-btn edit-btn" style="border:none; background:transparent; cursor:pointer; color: var(--va-info); display: flex; align-items: center;"><span class="material-icons" style="font-size: 1.1rem;">edit</span></button>
                  <button class="ag-action-btn del-btn" style="border:none; background:transparent; cursor:pointer; color: var(--va-danger); display: flex; align-items: center;"><span class="material-icons" style="font-size: 1.1rem;">delete</span></button>
                </div>`
      }
    }
  ])

  const onGroupCellClicked = (params) => {
    if (params.colDef.field === 'actions') {
      const event = params.event;
      if (event.target.closest('.edit-btn')) {
        openGroupModal(params.data);
      } else if (event.target.closest('.del-btn')) {
        confirmDeleteGroup(params.data);
      }
    }
  }

  const searchKeyword = ref('')
  
  const onSearch = () => {
    if (gridApi.value) {
      gridApi.value.refreshServerSide()
    }
  }

  const detailColumnDefs = computed(() => [
    { field: 'detailCode', headerName: t('code_management.detail_code'), sortable: true, flex: 1 },
    { field: 'name', headerName: t('code_management.name'), valueGetter: (params) => getLocaleName(params.data?.name), flex: 1 },
    { field: 'sortOrder', headerName: t('code_management.sort_order'), sortable: true, width: 120 },
    { 
      field: 'isActive', 
      headerName: t('code_management.status'),
      width: 120,
      cellRenderer: (params) => {
        if (params.value === undefined) return ''
        return params.value 
          ? '<span style="background: var(--va-success); color: white; padding: 2px 8px; border-radius: 12px; font-size: 0.75rem; font-weight: bold;">Active</span>' 
          : '<span style="background: var(--va-secondary); color: white; padding: 2px 8px; border-radius: 12px; font-size: 0.75rem; font-weight: bold;">Inactive</span>'
      }
    },
    { 
      field: 'actions', 
      headerName: t('code_management.manage'), 
      sortable: false,
      width: 100,
      cellRenderer: () => {
        return `<div style="display: flex; gap: 0.5rem; justify-content: flex-end; opacity: 0.8; height: 100%; align-items: center;">
                  <button class="ag-action-btn edit-btn" style="border:none; background:transparent; cursor:pointer; color: var(--va-info); display: flex; align-items: center;"><span class="material-icons" style="font-size: 1.2rem;">edit</span></button>
                  <button class="ag-action-btn del-btn" style="border:none; background:transparent; cursor:pointer; color: var(--va-danger); display: flex; align-items: center;"><span class="material-icons" style="font-size: 1.2rem;">delete</span></button>
                </div>`
      }
    }
  ])

  const onDetailCellClicked = (params) => {
    if (params.colDef.field === 'actions') {
      const event = params.event;
      if (event.target.closest('.edit-btn')) {
        openDetailModal(params.data);
      } else if (event.target.closest('.del-btn')) {
        confirmDeleteDetail(params.data);
      }
    }
  }

const onGridReady = (params) => {
  gridApi.value = params.api
  const datasource = {
    getRows: async (rowParams) => {
      try {
        const page = Math.floor(rowParams.request.startRow / 20)
        let sortStr = 'groupCode,asc'
        if (rowParams.request.sortModel && rowParams.request.sortModel.length > 0) {
          const s = rowParams.request.sortModel[0]
          sortStr = `${s.colId},${s.sort}`
        }

        let url = `/api/code-groups/page?page=${page}&size=20&sort=${sortStr}`
        if (searchKeyword.value) {
          url += `&keyword=${encodeURIComponent(searchKeyword.value)}`
        }

        const data = await $fetch(url, {
          headers: { Authorization: `Bearer ${token.value}` }
        })
        
        rowParams.success({
          rowData: data.content,
          rowCount: data.totalElements
        })
      } catch (e) {
        init({ message: t('code_management.load_failed'), color: 'danger' })
        rowParams.fail()
      }
    }
  }
  params.api.setGridOption('serverSideDatasource', datasource)
}

const refreshGrid = () => {
  if (gridApi.value) {
    gridApi.value.refreshServerSide()
  }
}

const onRowClicked = (event) => {
  selectedGroup.value = event.data
  loadDetails(event.data.id)
}

const loadDetails = async (groupId) => {
  try {
    details.value = await $fetch(`/api/code-groups/${groupId}/details`, {
      headers: { Authorization: `Bearer ${token.value}` }
    })
  } catch (e) {
    init({ message: t('code_management.load_failed'), color: 'danger' })
  }
}

const onEmojiSelect = (e, formObj, key) => {
  if (!formObj[key]) formObj[key] = ''
  formObj[key] += e.i
}

const showGroupModal = ref(false)
const editingGroup = ref(null)
const groupForm = ref({ groupCode: '', nameKo: '', nameEn: '', descKo: '', descEn: '', isActive: true })

const openGroupModal = (group = null) => {
  editingGroup.value = group
  if (group) {
    groupForm.value = {
      groupCode: group.groupCode,
      nameKo: group.name?.ko || '',
      nameEn: group.name?.en || '',
      descKo: group.description?.ko || '',
      descEn: group.description?.en || '',
      isActive: group.isActive
    }
  } else {
    groupForm.value = { groupCode: '', nameKo: '', nameEn: '', descKo: '', descEn: '', isActive: true }
  }
  showGroupModal.value = true
}

const saveGroup = async () => {
  const payload = {
    groupCode: groupForm.value.groupCode,
    name: { ko: groupForm.value.nameKo, en: groupForm.value.nameEn },
    description: { ko: groupForm.value.descKo, en: groupForm.value.descEn },
    isActive: groupForm.value.isActive
  }

  try {
    if (editingGroup.value) {
      await $fetch(`/api/code-groups/${editingGroup.value.id}`, {
        method: 'PUT',
        headers: { Authorization: `Bearer ${token.value}` },
        body: payload
      })
      init({ message: t('code_management.save_success'), color: 'success' })
    } else {
      await $fetch(`/api/code-groups`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token.value}` },
        body: payload
      })
      init({ message: t('code_management.save_success'), color: 'success' })
    }
    codeStore.invalidateCache()
    showGroupModal.value = false
    refreshGrid()
  } catch (e) {
    init({ message: t('code_management.save_failed'), color: 'danger' })
  }
}

const confirmDeleteGroup = async (group) => {
  if (!group) return
  const msg = t('code_management.confirm_delete_group').replace('{code}', group.groupCode)
  if (!confirm(msg)) return
  try {
    await $fetch(`/api/code-groups/${group.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    init({ message: t('code_management.delete_success'), color: 'success' })
    codeStore.invalidateCache()
    if (selectedGroup.value?.id === group.id) {
      selectedGroup.value = null
      details.value = []
    }
    refreshGrid()
  } catch (e) {
    init({ message: t('code_management.delete_failed'), color: 'danger' })
  }
}

const showDetailModal = ref(false)
const editingDetail = ref(null)
const detailForm = ref({ detailCode: '', nameKo: '', nameEn: '', sortOrder: 0, isActive: true })

const openDetailModal = (detail = null) => {
  editingDetail.value = detail
  if (detail) {
    detailForm.value = {
      detailCode: detail.detailCode,
      nameKo: detail.name?.ko || '',
      nameEn: detail.name?.en || '',
      sortOrder: detail.sortOrder || 0,
      isActive: detail.isActive
    }
  } else {
    detailForm.value = { detailCode: '', nameKo: '', nameEn: '', sortOrder: details.value.length + 1, isActive: true }
  }
  showDetailModal.value = true
}

const saveDetail = async () => {
  if (!selectedGroup.value) return
  const payload = {
    detailCode: detailForm.value.detailCode,
    name: { ko: detailForm.value.nameKo, en: detailForm.value.nameEn },
    sortOrder: detailForm.value.sortOrder,
    isActive: detailForm.value.isActive
  }

  try {
    if (editingDetail.value) {
      await $fetch(`/api/code-groups/details/${editingDetail.value.id}`, {
        method: 'PUT',
        headers: { Authorization: `Bearer ${token.value}` },
        body: payload
      })
      init({ message: t('code_management.save_success'), color: 'success' })
    } else {
      await $fetch(`/api/code-groups/${selectedGroup.value.id}/details`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token.value}` },
        body: payload
      })
      init({ message: t('code_management.save_success'), color: 'success' })
    }
    codeStore.invalidateCache()
    showDetailModal.value = false
    loadDetails(selectedGroup.value.id)
  } catch (e) {
    init({ message: t('code_management.save_failed'), color: 'danger' })
  }
}

const confirmDeleteDetail = async (detail) => {
  const msg = t('code_management.confirm_delete_detail').replace('{code}', detail.detailCode)
  if (!confirm(msg)) return
  try {
    await $fetch(`/api/code-groups/details/${detail.id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    init({ message: t('code_management.delete_success'), color: 'success' })
    codeStore.invalidateCache()
    loadDetails(selectedGroup.value.id)
  } catch (e) {
    init({ message: t('code_management.delete_failed'), color: 'danger' })
  }
}

const dumpCodeSeed = async () => {
  isDumping.value = true
  try {
    await $fetch('/api/code-groups/dump-seed', {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    init({ message: '백업이 완료되었습니다. (default_codes.json)', color: 'success' })
  } catch (e) {
    init({ message: '백업 중 오류가 발생했습니다.', color: 'danger' })
  } finally {
    isDumping.value = false
  }
}

const syncCodeSeed = async () => {
  isSyncing.value = true
  try {
    await $fetch('/api/code-groups/sync-seed', {
      method: 'POST',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    init({ message: '코드 동기화가 완료되었습니다.', color: 'success' })
    codeStore.invalidateCache()
    refreshGrid()
  } catch (e) {
    init({ message: '코드 동기화 중 오류가 발생했습니다.', color: 'danger' })
  } finally {
    isSyncing.value = false
  }
}

onMounted(() => {
  // initial load handled by AG-Grid gridReady
})
</script>

<style scoped>
.hover-elevate {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.hover-elevate:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0,0,0,0.08) !important;
}

.premium-table {
  --va-data-table-max-height: calc(100vh - 350px);
  width: 100%;
}

:deep(.va-data-table__table-th) {
  font-weight: 700 !important;
  color: var(--va-text-secondary) !important;
  text-transform: uppercase;
  font-size: 0.8rem;
  letter-spacing: 0.5px;
  background-color: var(--va-background-primary) !important;
  border-bottom: 2px solid var(--va-background-border) !important;
  padding: 1rem !important;
}

:deep(.va-data-table__table-td) {
  padding: 1rem !important;
  border-bottom: 1px solid var(--va-background-border);
  transition: background-color 0.2s ease;
}

:deep(.va-data-table__table-tr:hover .va-data-table__table-td) {
  background-color: var(--va-background-element);
}

.status-badge {
  font-weight: 700;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: var(--va-background-border);
  border-radius: 10px;
}
::-webkit-scrollbar-thumb:hover {
  background: var(--va-text-secondary);
}

:deep(.ag-row.ag-row-selected) {
  border-left: 4px solid var(--va-primary) !important;
}
</style>
