<template>
  <div class="records-grid-wrapper" style="width: 100%; height: 100%; min-height: 400px;">
    <ag-grid-vue
      style="width: 100%; height: 100%;"
      :theme="gridTheme"
      :autoSizeStrategy="autoSizeStrategy"
      :columnDefs="columnDefs"
      :defaultColDef="defaultColDef"
      rowModelType="infinite"
      :cacheBlockSize="20"
      :rowSelection="{ mode: 'singleRow', headerCheckbox: false }"
      :pagination="true"
      :paginationPageSize="20"
      :paginationPageSizeSelector="[10, 20, 50]"
      @grid-ready="onGridReady"
      @column-resized="onColumnChanged"
      @column-moved="onColumnChanged"
      @row-double-clicked="$emit('rowDoubleClicked', $event)"
      @cell-double-clicked="$emit('cellDoubleClicked', $event)"
    />
  </div>
</template>

<script setup lang="ts">
import { AgGridVue } from 'ag-grid-vue3'
import { useGridState } from '~/composables/useGridState'

const props = withDefaults(defineProps<{
  gridTheme?: any
  autoSizeStrategy?: any
  columnDefs?: any[]
  defaultColDef?: any
  gridKey?: string
}>(), {
  gridTheme: null,
  autoSizeStrategy: null,
  columnDefs: () => [],
  defaultColDef: () => ({}),
  gridKey: 'records_grid'
})

const emit = defineEmits(['gridReady', 'rowDoubleClicked', 'cellDoubleClicked'])
const { saveState, restoreState } = useGridState(props.gridKey || 'records_grid')
let gridApiInstance: any = null

const onGridReady = (params: any) => {
  gridApiInstance = params.api
  restoreState(gridApiInstance)
  emit('gridReady', params)
}

const onColumnChanged = () => {
  if (gridApiInstance) {
    saveState(gridApiInstance)
  }
}
</script>

<style scoped>
.records-grid-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
</style>
