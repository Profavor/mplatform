<template>
  <va-modal
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    title="Select Reference Record"
    hide-default-actions
    size="large"
    :prevent-click-outside="true"
    :no-outside-dismiss="true"
  >
    <div style="height: 50vh; width: 100%; display: flex; flex-direction: column;">
      <div style="margin-bottom: 1rem; color: #666; font-size: 0.9rem;">
        원하시는 레코드를 목록에서 더블 클릭하여 선택해 주세요.
      </div>
      <div :class="{ 'ag-theme-quartz-dark': isDark }" style="flex: 1; width: 100%;">
        <AgGridVue
          style="width: 100%; height: 100%;"
          :theme="gridTheme"
          :autoSizeStrategy="autoSizeStrategy"
          :columnDefs="domainRefColDefs"
          :rowData="domainRefRowData"
          :defaultColDef="{ sortable: true, filter: true, resizable: true }"
          :rowSelection="{ mode: 'singleRow' }"
          @rowDoubleClicked="$emit('row-double-clicked', $event)"
        />
      </div>
    </div>
    <div style="display: flex; justify-content: flex-end; margin-top: 1rem;">
      <va-button @click="$emit('update:modelValue', false)">Cancel</va-button>
    </div>
  </va-modal>
</template>

<script setup>
import { AgGridVue } from 'ag-grid-vue3'

defineProps({
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
  domainRefRowData: {
    type: Array,
    default: () => []
  }
})

defineEmits(['update:modelValue', 'row-double-clicked'])
</script>
