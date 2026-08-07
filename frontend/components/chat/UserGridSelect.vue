<template>
  <div style="display: flex; flex-direction: column; gap: 0.5rem; width: 100%;">
    <div style="display: flex; gap: 0.5rem; align-items: center;">
      <va-input
        v-model="userSearchKeyword"
        :placeholder="$t('messenger.searchUserPlaceholder', '사용자명 또는 역할 검색...')"
        clearable
        style="flex: 1;"
      >
        <template #prependInner>
          <va-icon name="search" color="primary" />
        </template>
      </va-input>
    </div>

    <div :class="{ 'ag-theme-quartz-dark': isDark }" style="width: 100%; height: 280px; border: 1px solid var(--va-background-border); border-radius: 8px; overflow: hidden;">
      <client-only>
        <ag-grid-vue
          v-if="isMounted"
          style="width: 100%; height: 100%;"
          :theme="gridTheme"
          :columnDefs="columnDefs"
          :rowData="filteredUsers"
          :rowSelection="{ mode: 'multiRow', checkboxes: true, headerCheckbox: true }"
          @grid-ready="onGridReady"
          @selection-changed="onSelectionChanged"
          :autoSizeStrategy="{ type: 'fitGridWidth' }"
          :pagination="true"
          :paginationPageSize="10"
          :paginationPageSizeSelector="[5, 10, 20]"
        />
      </client-only>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { AgGridVue } from 'ag-grid-vue3'
import { useI18n } from 'vue-i18n'
import { useAgGridTheme } from '~/composables/useAgGridTheme'

const props = defineProps<{
  users: any[]
  modelValue: string[] // selected user IDs
}>()

const emit = defineEmits(['update:modelValue'])

const { t } = useI18n()
const { gridTheme, isDark } = useAgGridTheme()

const isMounted = ref(false)
const userSearchKeyword = ref('')
let gridApi: any = null
let isUpdatingFromProps = false

onMounted(() => {
  isMounted.value = true
})

const filteredUsers = computed(() => {
  if (!userSearchKeyword.value) return props.users
  const lower = userSearchKeyword.value.toLowerCase()
  return props.users.filter((u: any) => 
    (u.username && u.username.toLowerCase().includes(lower)) ||
    (u.role && u.role.toLowerCase().includes(lower))
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
    headerName: t('messenger.roleCol', '역할'),
    field: 'role',
    minWidth: 140,
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
  if (!gridApi || !props.modelValue) return
  isUpdatingFromProps = true
  gridApi.forEachNode((node: any) => {
    if (props.modelValue.includes(node.data.id)) {
      node.setSelected(true)
    } else {
      node.setSelected(false)
    }
  })
  isUpdatingFromProps = false
}

watch(() => props.modelValue, () => {
  if (!isUpdatingFromProps) {
    selectInitialRows()
  }
}, { deep: true })

const onSelectionChanged = () => {
  if (!gridApi || isUpdatingFromProps) return
  const selectedNodes = gridApi.getSelectedNodes()
  const selectedIds = selectedNodes.map((node: any) => node.data.id)
  isUpdatingFromProps = true
  emit('update:modelValue', selectedIds)
  isUpdatingFromProps = false
}
</script>
