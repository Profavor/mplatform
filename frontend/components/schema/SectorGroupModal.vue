<template>
  <va-modal :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)" title="Manage Sectors & Groups" hide-default-actions size="large">
    <va-tabs :model-value="sgActiveTab" @update:model-value="$emit('update:sgActiveTab', $event)" style="width: 100%; margin-bottom: 1.5rem;">
      <template #tabs>
        <va-tab>Sectors</va-tab>
        <va-tab>Groups</va-tab>
      </template>
    </va-tabs>

    <div style="height: 480px; padding-bottom: 1rem;">
      <!-- Sectors Tab Content -->
      <div v-show="sgActiveTab === 0" style="height: 100%; display: flex; flex-direction: column;">
        <div style="display:flex; justify-content:space-between; margin-bottom: 1rem; align-items: center;">
          <h3 style="font-weight:bold; margin: 0;">Sectors</h3>
          <div style="display:flex; gap: 0.5rem;">
            <va-button size="small" icon="add" @click="$emit('add-sector-row')">행 추가</va-button>
            <va-button size="small" color="primary" icon="save" @click="$emit('save-all-sectors')">저장</va-button>
            <va-button size="small" color="danger" icon="delete" @click="$emit('delete-selected-sector')" :outline="isDark">선택 삭제</va-button>
          </div>
        </div>
        <div :class="{ 'ag-theme-quartz-dark': isDark }" style="flex: 1; width: 100%;">
          <AgGridVue
            style="width: 100%; height: 100%;"
            :theme="gridTheme"
            :autoSizeStrategy="autoSizeStrategy"
            :columnDefs="sectorColumnDefs"
            :rowData="domainSectors"
            :defaultColDef="sgDefaultColDef"
            @grid-ready="$emit('sector-grid-ready', $event)"
            editType="fullRow"
            :rowSelection="{ mode: 'singleRow' }"
          />
        </div>
      </div>

      <!-- Groups Tab Content -->
      <div v-show="sgActiveTab === 1" style="height: 100%; display: flex; flex-direction: column;">
        <div style="display:flex; justify-content:space-between; margin-bottom: 1rem; align-items: center;">
          <h3 style="font-weight:bold; margin: 0;">Groups</h3>
          <div style="display:flex; gap: 0.5rem;">
            <va-button size="small" icon="add" @click="$emit('add-group-row')">행 추가</va-button>
            <va-button size="small" color="primary" icon="save" @click="$emit('save-all-groups')">저장</va-button>
            <va-button size="small" color="danger" icon="delete" @click="$emit('delete-selected-group')" :outline="isDark">선택 삭제</va-button>
          </div>
        </div>
        <div :class="{ 'ag-theme-quartz-dark': isDark }" style="flex: 1; width: 100%;">
          <AgGridVue
            style="width: 100%; height: 100%;"
            :theme="gridTheme"
            :autoSizeStrategy="autoSizeStrategy"
            :columnDefs="groupColumnDefs"
            :rowData="domainGroups"
            :defaultColDef="sgDefaultColDef"
            @grid-ready="$emit('group-grid-ready', $event)"
            editType="fullRow"
            :rowSelection="{ mode: 'singleRow' }"
          />
        </div>
      </div>
    </div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 1rem; border-top: 1px solid #eee; padding-top: 0.75rem;">
      <span style="font-size: 0.85em; color: #666;">* 셀 수정 후 상단 또는 하단의 '저장' 버튼을 클릭하여 변경사항을 반영하세요.</span>
      <div style="display: flex; gap: 0.5rem;">
        <va-button color="primary" icon="save" @click="$emit('save-sector-group-changes')">저장</va-button>
        <va-button preset="secondary" @click="$emit('update:modelValue', false)">닫기</va-button>
      </div>
    </div>
  </va-modal>
</template>

<script setup>
import { AgGridVue } from 'ag-grid-vue3'

defineProps({
  modelValue: { type: Boolean, default: false },
  sgActiveTab: { type: Number, default: 0 },
  isDark: { type: Boolean, default: false },
  gridTheme: { type: [Object, String], default: null },
  autoSizeStrategy: { type: Object, default: () => ({ type: 'fitGridWidth' }) },
  sectorColumnDefs: { type: Array, default: () => [] },
  domainSectors: { type: Array, default: () => [] },
  groupColumnDefs: { type: Array, default: () => [] },
  domainGroups: { type: Array, default: () => [] },
  sgDefaultColDef: { type: Object, default: () => ({}) }
})

defineEmits([
  'update:modelValue',
  'update:sgActiveTab',
  'add-sector-row',
  'save-all-sectors',
  'delete-selected-sector',
  'sector-grid-ready',
  'add-group-row',
  'save-all-groups',
  'delete-selected-group',
  'group-grid-ready',
  'save-sector-group-changes'
])
</script>
