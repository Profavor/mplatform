<template>
  <AppModal
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="$t('domain_ref_modal.title')"
    icon="link"
    hide-default-actions
    size="large"
  >
    <div style="height: 60vh; width: 100%; display: flex; flex-direction: column; gap: 0.75rem;">
      <!-- Search & Filter Bar -->
      <div style="display: flex; justify-content: space-between; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
        <div style="display: flex; align-items: center; gap: 0.5rem; flex: 1; max-width: 420px;">
          <va-input
            v-model="searchQuery"
            :placeholder="$t('domain_ref_modal.search_placeholder')"
            size="small"
            clearable
            style="flex: 1;"
            @keydown.enter="onSearch"
          >
            <template #prependInner>
              <va-icon name="search" color="secondary" size="small" />
            </template>
          </va-input>
          <va-button size="small" color="primary" icon="search" @click="onSearch">
            {{ $t('domain_ref_modal.search_btn') }}
          </va-button>
          <va-button size="small" preset="secondary" icon="restart_alt" @click="onReset">
            {{ $t('domain_ref_modal.reset_btn') }}
          </va-button>
        </div>

        <div style="display: flex; align-items: center; gap: 0.5rem;">
          <va-badge
            v-if="totalRecordsCount !== null"
            :text="$t('domain_ref_modal.total_count', { count: totalRecordsCount })"
            color="primary"
            size="small"
          />
        </div>
      </div>

      <div style="color: var(--va-text-secondary); font-size: 0.85rem;">
        {{ $t('domain_ref_modal.guide') }}
      </div>

      <!-- AG Grid with Infinite Server-Side Pagination -->
      <div :class="{ 'ag-theme-quartz-dark': isDark }" style="flex: 1; width: 100%; min-height: 0;">
        <AgGridVue
          style="width: 100%; height: 100%;"
          :theme="gridTheme"
          :autoSizeStrategy="autoSizeStrategy"
          :columnDefs="domainRefColDefs"
          rowModelType="infinite"
          :pagination="true"
          :paginationPageSize="20"
          :cacheBlockSize="20"
          :defaultColDef="{ sortable: true, resizable: true }"
          :rowSelection="{ mode: 'singleRow' }"
          @grid-ready="onGridReady"
          @rowDoubleClicked="$emit('row-double-clicked', $event)"
        />
      </div>
    </div>

    <div style="display: flex; justify-content: flex-end; margin-top: 1rem;">
      <va-button preset="secondary" @click="$emit('update:modelValue', false)">
        {{ $t('cancel') }}
      </va-button>
    </div>
  </AppModal>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { AgGridVue } from 'ag-grid-vue3'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'
import AppModal from '~/components/common/AppModal.vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  isDark: {
    type: Boolean,
    default: false
  },
  gridTheme: {
    type: [Object, String],
    default: null
  },
  autoSizeStrategy: {
    type: Object,
    default: () => ({ type: 'fitGridWidth' })
  },
  domainRefColDefs: {
    type: Array,
    default: () => []
  },
  targetDomainId: {
    type: String,
    default: null
  },
  idFieldKey: {
    type: String,
    default: null
  },
  nameFieldKey: {
    type: String,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'row-double-clicked'])

const { t } = useI18n()
const { customFetch } = useCustomFetch()

const gridApi = ref(null)
const searchQuery = ref('')
const totalRecordsCount = ref(null)

const createServerDatasource = () => {
  return {
    getRows: async (params) => {
      if (!props.targetDomainId) {
        params.successCallback([], 0)
        totalRecordsCount.value = 0
        return
      }

      try {
        const pageSize = 20
        const page = Math.floor(params.startRow / pageSize)
        let url = `/api/records/domain/${props.targetDomainId}?page=${page}&size=${pageSize}`

        if (searchQuery.value && searchQuery.value.trim()) {
          const trimmed = searchQuery.value.trim()
          const keys = []
          if (props.idFieldKey) keys.push(props.idFieldKey)
          if (props.nameFieldKey && props.nameFieldKey !== props.idFieldKey) keys.push(props.nameFieldKey)

          if (keys.length > 0) {
            url += `&search_multi_keys=${encodeURIComponent(keys.join(','))}&search_multi_val=${encodeURIComponent(trimmed)}`
          } else {
            url += `&search_multi_val=${encodeURIComponent(trimmed)}`
          }
        }

        if (params.sortModel && params.sortModel.length > 0) {
          const sm = params.sortModel[0]
          let fieldName = sm.colId
          if (fieldName.startsWith('data.')) {
            fieldName = fieldName.substring(5)
          }
          url += `&sortField=${encodeURIComponent(fieldName)}&sortOrder=${encodeURIComponent(sm.sort.toUpperCase())}`
        }

        const res = await customFetch(url)
        const content = Array.isArray(res) ? res : (res?.content || [])
        const totalElements = res?.totalElements !== undefined ? res.totalElements : content.length
        totalRecordsCount.value = totalElements

        const rows = content.map((r) => ({
          id: r.id,
          data: typeof r.data === 'string' ? JSON.parse(r.data) : (r.data || {})
        }))

        params.successCallback(rows, totalElements)
      } catch (err) {
        console.error('Failed to load reference records page:', err)
        params.failCallback()
      }
    }
  }
}

const onGridReady = (params) => {
  gridApi.value = params.api
  params.api.setGridOption('datasource', createServerDatasource())
}

const refreshGridData = () => {
  if (gridApi.value) {
    gridApi.value.setGridOption('datasource', createServerDatasource())
  }
}

const onSearch = () => {
  refreshGridData()
}

const onReset = () => {
  searchQuery.value = ''
  refreshGridData()
}

watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      searchQuery.value = ''
      totalRecordsCount.value = null
      nextTick(() => {
        refreshGridData()
      })
    }
  }
)

watch(
  () => props.targetDomainId,
  () => {
    if (props.modelValue) {
      refreshGridData()
    }
  }
)
</script>
