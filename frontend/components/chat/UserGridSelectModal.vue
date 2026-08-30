<template>
  <AppModal
    :model-value="modelValue"
    @update:model-value="val => $emit('update:modelValue', val)"
    :title="title || $t('messenger.selectUsersLabel')"
    icon="group_add"
    size="large"
    hide-default-actions
  >
    <div style="padding: 0.5rem 0; display: flex; flex-direction: column; gap: 1rem; width: 100%;">
      <div style="display: flex; gap: 0.5rem; align-items: center;">
        <va-input
          v-model="userSearchKeyword"
          :placeholder="$t('messenger.searchUserPlaceholder', '사용자명, 역할, 부서 검색...')"
          clearable
          style="flex: 1;"
        >
          <template #prependInner>
            <va-icon name="search" color="primary" />
          </template>
        </va-input>
      </div>

      <div :class="{ 'ag-theme-quartz-dark': isDark }" style="width: 100%; height: 380px; min-height: 200px; border: 1px solid var(--va-background-border); border-radius: 8px; overflow: hidden;">
        <client-only>
          <ag-grid-vue
            v-if="isMounted"
            style="width: 100%; height: 100%;"
            :theme="gridTheme"
            :columnDefs="columnDefs"
            :rowData="filteredUsers"
            :rowSelection="{ mode: 'multiRow', checkboxes: true, headerCheckbox: true }"
            @grid-ready="onGridReady"
            :autoSizeStrategy="{ type: 'fitGridWidth' }"
            :pagination="true"
            :paginationPageSize="10"
            :paginationPageSizeSelector="[5, 10, 20, 50]"
          />
        </client-only>
      </div>

      <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem;">
        <va-button preset="secondary" @click="closeModal">{{ $t('messenger.cancelBtn', '취소') }}</va-button>
        <va-button color="primary" icon="check" @click="confirmSelection">{{ $t('messenger.confirmBtn', '확인') }}</va-button>
      </div>
    </div>
  </AppModal>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { AgGridVue } from 'ag-grid-vue3'
import { useI18n } from 'vue-i18n'
import { useAgGridTheme } from '~/composables/useAgGridTheme'
import { getMultilingualText } from '~/utils/multilingual'
import AppModal from '~/components/common/AppModal.vue'

const props = defineProps<{
  modelValue: boolean
  title?: string
  users: any[]
  initialSelectedIds?: string[]
}>()

const emit = defineEmits(['update:modelValue', 'confirm'])

const { t } = useI18n()
const { gridTheme, isDark } = useAgGridTheme()

const isMounted = ref(false)
const userSearchKeyword = ref('')
let gridApi: any = null

onMounted(() => {
  isMounted.value = true
})

const filteredUsers = computed(() => {
  if (!userSearchKeyword.value) return props.users
  const lower = userSearchKeyword.value.toLowerCase()
  return props.users.filter((u: any) => 
    (u.username && u.username.toLowerCase().includes(lower)) ||
    (u.role && u.role.toLowerCase().includes(lower)) ||
    (u.orgName && u.orgName.toLowerCase().includes(lower)) ||
    (u.deptName && u.deptName.toLowerCase().includes(lower))
  )
})

const columnDefs = computed(() => [
  {
    headerName: '',
    field: 'id',
    width: 50,
    minWidth: 50,
    maxWidth: 50,
    pinned: 'left'
  },
  {
    headerName: t('messenger.usernameCol', '사용자명'),
    field: 'username',
    minWidth: 140,
    cellRenderer: (params: any) => {
      if (!params.data) return ''
      return `<div style="font-weight: 700; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.4rem;">
        <span>👤</span> <span>${params.value || ''}</span>
      </div>`
    }
  },
  {
    headerName: t('messenger.orgCol', '조직'),
    field: 'orgName',
    minWidth: 120,
    cellRenderer: (params: any) => {
      if (!params.data) return ''
      const org = getMultilingualText(params.data.orgName)
      return org ? `<span style="font-size: 0.85rem;">${org}</span>` : `<span style="color: var(--va-text-secondary); font-size: 0.8rem;">-</span>`
    }
  },
  {
    headerName: t('messenger.deptCol', '부서'),
    field: 'deptName',
    minWidth: 120,
    cellRenderer: (params: any) => {
      if (!params.data) return ''
      const dept = getMultilingualText(params.data.deptName)
      return dept ? `<span style="font-size: 0.85rem; font-weight: 700;">${dept}</span>` : `<span style="color: var(--va-text-secondary); font-size: 0.8rem;">-</span>`
    }
  },
  {
    headerName: t('messenger.roleCol', '역할'),
    field: 'role',
    minWidth: 120,
    cellRenderer: (params: any) => {
      if (!params.data) return ''
      return `<span style="background: rgba(37, 99, 235, 0.1); color: #2563eb; padding: 2px 7px; border-radius: 4px; font-size: 0.75rem; font-weight: 700; border: 1px solid rgba(37, 99, 235, 0.2);">${params.value}</span>`
    }
  }
])

const onGridReady = (params: any) => {
  gridApi = params.api
  selectInitialRows()
}

const selectInitialRows = () => {
  if (!gridApi || !props.initialSelectedIds) return
  gridApi.forEachNode((node: any) => {
    if (props.initialSelectedIds!.includes(node.data.id)) {
      node.setSelected(true)
    }
  })
}

watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    userSearchKeyword.value = ''
    setTimeout(() => {
      selectInitialRows()
    }, 100)
  }
})

const closeModal = () => {
  emit('update:modelValue', false)
}

const confirmSelection = () => {
  if (!gridApi) return
  const selectedNodes = gridApi.getSelectedNodes()
  const selectedIds = selectedNodes.map((node: any) => node.data.id)
  emit('confirm', selectedIds)
  closeModal()
}
</script>
