<template>
  <va-modal
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="isEditMode ? t('edit_field') : t('add_field_to_node', { name: formatNodeTitle(selectedNode) })"
    hide-default-actions
    size="large"
  >
    <va-alert v-if="isCurrentFieldPendingApproval" color="warning" class="mb-4">
      ⚠️ {{ t('pending_field_approval_warning') }}
    </va-alert>
    <div style="display: flex; gap: 1rem;">
      <va-input v-model="newField.name.ko" :label="t('field_name_ko')" class="mb-4" style="flex: 1; min-width: 0;" />
      <va-input v-model="newField.name.en" :label="t('field_name_en')" class="mb-4" style="flex: 1; min-width: 0;" />
    </div>
    <div style="display: flex; gap: 1rem;">
      <va-input v-model="newField.hint.ko" :label="t('field_hint_ko')" class="mb-4" style="flex: 1; min-width: 0;" />
      <va-input v-model="newField.hint.en" :label="t('field_hint_en')" class="mb-4" style="flex: 1; min-width: 0;" />
    </div>
    
    <div style="display: flex; gap: 1rem; align-items: center;" class="mb-4">
      <va-select 
        v-model="newField.targetNodeId" 
        :options="availableClassificationNodes" 
        value-by="value"
        :label="t('belonging_node_domain')" 
        style="flex: 1; min-width: 0;"
        @update:model-value="$emit('target-node-selected', $event)"
      />
      <div style="display: flex; align-items: center; gap: 0.5rem; margin-top: 1.2rem;">
        <va-checkbox v-model="newField.isDomainField" :label="t('domain_common_field')" @update:model-value="$emit('is-domain-field-checked', $event)" />
      </div>
    </div>

    <va-select 
      v-model="newField.fieldGroupId" 
      :options="groupOptions" 
      value-by="value"
      :label="t('group_sector_mapped')" 
      class="mb-4 w-full" 
    />
    <div style="display: flex; gap: 1rem;">
      <va-input v-model="newField.key" :label="t('field_key')" class="mb-4" style="flex: 1; min-width: 0;" />
      <va-input v-model="newField.order" type="number" :label="t('sort_order')" class="mb-4" style="flex: 1; min-width: 0;" />
    </div>
    
    <va-select v-model="newField.type" :options="fieldTypes" value-by="value" :label="t('field_type')" class="mb-4 w-full" />

    <va-select
      v-if="newField.isEncrypted"
      v-model="newField.maskingPattern"
      :options="maskingPatternOptions"
      value-by="value"
      :label="t('masking_pattern')"
      class="mb-4 w-full"
    />
      
    <va-select 
      v-if="newField.type === 'DOMAIN_REFERENCE'" 
      v-model="newField.targetDomainId" 
      :options="domainOptions" 
      value-by="value"
      :label="t('target_domain')" 
      class="mb-4 w-full" 
    />
    
    <div v-if="['SELECT', 'MULTI_SELECT'].includes(newField.type)" class="mb-4 w-full" style="border: 1px solid #ccc; padding: 1rem; border-radius: 8px;">
      <label style="font-weight: bold; margin-bottom: 0.5rem; display: block;">{{ t('options_settings') }}</label>
      
      <div style="margin-bottom: 0.5rem; display: flex; gap: 0.5rem; justify-content: flex-end;">
        <va-button size="small" icon="add" @click="$emit('add-grid-option')">{{ t('add_option') }}</va-button>
        <va-button size="small" icon="remove" color="danger" @click="$emit('remove-selected-grid-option')" :outline="isDark">{{ t('remove_selected') }}</va-button>
      </div>
      
      <div :class="{ 'ag-theme-quartz-dark': isDark }" style="width: 100%; height: 250px;">
        <ag-grid-vue
          style="width: 100%; height: 100%;"
          :theme="gridTheme"
          :autoSizeStrategy="autoSizeStrategy"
          :columnDefs="optionsColumnDefs"
          :rowData="newFieldOptionsList"
          :defaultColDef="optionsDefaultColDef"
          :rowSelection="{ mode: 'singleRow' }"
          @grid-ready="$emit('options-grid-ready', $event)"
        />
      </div>
    </div>
    
    <div v-else-if="newField.type === 'CALCULATED'" class="mb-4 w-full" style="border: 1px solid #ccc; padding: 1rem; border-radius: 8px;">
      <label style="font-weight: bold; margin-bottom: 0.5rem; display: block;">{{ t('formula_settings') }}</label>
      <va-textarea
        v-model="newField.formula"
        :placeholder="t('e_g_abs_key_a_key_b_2_100')"
        class="w-full mb-2"
        :min-rows="3"
        style="font-family: monospace;"
      />
      <va-alert color="info" dense class="w-full" style="font-size: 0.85rem;">
        <strong>{{ t('formula_guide') }}</strong><br/>
        - <strong>필드 참조</strong>: <code>${필드_KEY}</code> 형식으로 입력하세요. (예: <code>${PRICE}</code>)<br/>
        - <strong>기본 연산</strong>: <code>+</code> (더하기), <code>-</code> (빼기), <code>*</code> (곱하기), <code>/</code> (나누기)<br/>
        - <strong>수학 함수</strong>:<br/>
          &nbsp;&nbsp;• <code>ROUND(값, 자리수)</code> : 반올림(예: <code>ROUND(${PRICE}, 2)</code>)<br/>
          &nbsp;&nbsp;• <code>CEIL(값)</code> : 올림<br/>
          &nbsp;&nbsp;• <code>FLOOR(값)</code> : 내림<br/>
          &nbsp;&nbsp;• <code>ABS(값)</code> : 절대값<br/>
        <span style="color: #d9534f; font-weight: bold;">주의:</span> 참조하는 필드는 반드시 숫자(NUMBER, DECIMAL, FLOAT, INTEGER)이거나 다른 계산 필드(CALCULATED)여야 합니다.
      </va-alert>
    </div>

    <div v-if="['DATE', 'DATE_RANGE'].includes(newField.type)" class="mb-4 w-full">
      <va-select
        v-model="newField.dateFormat"
        :options="['YYYY-MM-DD', 'MM/DD/YYYY', 'DD/MM/YYYY']"
        :label="t('date_format')"
        class="w-full"
        clearable
        allow-create="unique"
        :no-options-text="t('can_input_directly')"
        placeholder="YYYY-MM-DD"
      />
    </div>

    <div v-if="['NUMBER', 'DECIMAL', 'FLOAT', 'INTEGER', 'CALCULATED'].includes(newField.type)" class="mb-4 w-full">
      <va-select
        v-model="newField.unit"
        :options="unitOptions"
        label="Unit (단위)"
        class="w-full"
        clearable
        allow-create="unique"
        :no-options-text="'직접 입력 가능'"
      />
    </div>

    <div style="display: flex; gap: 1rem; margin-top: 1rem; margin-bottom: 0.5rem; flex-wrap: wrap;">
      <va-input v-model="newField.gridWidth" type="number" label="Form Grid Width (1-12)" class="w-full" style="max-width: 170px;" placeholder="Auto" clearable />
      <va-input v-model="newField.tableColumnWidth" type="number" label="AG-Grid Width (px)" class="w-full" style="max-width: 170px;" placeholder="Auto" clearable />
    </div>
    <div style="margin-top: 1.25rem; padding: 1rem; background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px;">
      <div style="font-size: 0.75rem; font-weight: 700; color: var(--va-secondary); text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 0.75rem;">
        Field Attributes & Controls (필드 속성)
      </div>
      <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 0.6rem;">
        <div class="option-pill" :class="{ active: newField.required }" @click="newField.required = !newField.required">
          <va-checkbox v-model="newField.required" @click.stop />
          <va-icon name="star" size="small" :color="newField.required ? 'danger' : 'secondary'" />
          <span style="flex: 1;">{{ t('required') }}</span>
        </div>

        <div class="option-pill" :class="{ active: newField.isMultiValue }" @click="newField.isMultiValue = !newField.isMultiValue">
          <va-checkbox v-model="newField.isMultiValue" @click.stop />
          <va-icon name="dataset" size="small" :color="newField.isMultiValue ? 'primary' : 'secondary'" />
          <span style="flex: 1;">{{ t('multi_value') }}</span>
        </div>

        <div class="option-pill" :class="{ active: newField.isSearchable }" @click="newField.isSearchable = !newField.isSearchable">
          <va-checkbox v-model="newField.isSearchable" @click.stop />
          <va-icon name="search" size="small" :color="newField.isSearchable ? 'primary' : 'secondary'" />
          <span style="flex: 1;">{{ t('searchable') }}</span>
        </div>

        <div class="option-pill" :class="{ active: newField.isEncrypted }" @click="newField.isEncrypted = !newField.isEncrypted">
          <va-checkbox v-model="newField.isEncrypted" @click.stop />
          <va-icon name="lock" size="small" :color="newField.isEncrypted ? 'warning' : 'secondary'" />
          <span style="flex: 1;">{{ t('encrypted') }}</span>
        </div>

        <div class="option-pill" :class="{ active: newField.isReadOnly }" @click="newField.isReadOnly = !newField.isReadOnly">
          <va-checkbox v-model="newField.isReadOnly" @click.stop />
          <va-icon name="visibility_off" size="small" :color="newField.isReadOnly ? 'info' : 'secondary'" />
          <span style="flex: 1;">{{ t('read_only') }}</span>
        </div>

        <div class="option-pill" :class="{ active: newField.isImmutable }" @click="newField.isImmutable = !newField.isImmutable">
          <va-checkbox v-model="newField.isImmutable" @click.stop />
          <va-icon name="edit_off" size="small" :color="newField.isImmutable ? 'danger' : 'secondary'" />
          <span style="flex: 1;">{{ t('immutable') }}</span>
        </div>

        <div class="option-pill" :class="{ active: newField.isHidden }" @click="newField.isHidden = !newField.isHidden">
          <va-checkbox v-model="newField.isHidden" @click.stop />
          <va-icon name="hide_source" size="small" :color="newField.isHidden ? 'secondary' : 'secondary'" />
          <span style="flex: 1;">{{ t('hidden') }}</span>
        </div>

        <div class="option-pill" :class="{ active: newField.isHighlighted }" @click="newField.isHighlighted = !newField.isHighlighted">
          <va-checkbox v-model="newField.isHighlighted" @click.stop />
          <va-icon name="auto_awesome" size="small" :color="newField.isHighlighted ? 'warning' : 'secondary'" />
          <span style="flex: 1;">{{ t('highlight') }}</span>
        </div>
      </div>
    </div>

    <!-- Conditional Field Control Section (조건부 연동 설정) -->
    <div style="margin-top: 1.25rem; padding: 1rem; background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
        <div style="font-size: 0.75rem; font-weight: 700; color: var(--va-primary); text-transform: uppercase; letter-spacing: 0.5px; display: flex; align-items: center; gap: 4px;">
          <va-icon name="bolt" size="small" color="primary" />
          {{ t('conditional_field_control') }}
        </div>
        <va-checkbox v-model="newField.conditionEnabled" :label="t('enable_condition')" />
      </div>

      <div v-if="newField.conditionEnabled" style="display: flex; flex-direction: column; gap: 0.75rem; padding-top: 0.25rem;">
        <!-- Mode Tabs -->
        <div style="display: flex; gap: 0.5rem; align-items: center; font-size: 0.85rem;">
          <span style="font-weight: 600; color: var(--va-text-secondary); width: 80px;">{{ t('condition_mode') }}</span>
          <va-button-toggle
            v-model="newField.conditionMode"
            size="small"
            :options="[
              { label: t('gui_mode'), value: 'GUI' },
              { label: t('expression_mode'), value: 'EXPRESSION' }
            ]"
          />
        </div>

        <!-- Action Selector (Multi-select) -->
        <div style="display: flex; gap: 0.5rem; align-items: center;">
          <span style="font-weight: 600; font-size: 0.85rem; color: var(--va-text-secondary); width: 80px;">{{ t('control_action') }}</span>
          <va-select
            v-model="newField.conditionAction"
            style="flex: 1;"
            multiple
            :options="[
              { label: t('action_show'), value: 'SHOW' },
              { label: t('action_highlight'), value: 'HIGHLIGHT' },
              { label: t('action_require'), value: 'REQUIRE' },
              { label: t('action_read_only'), value: 'READ_ONLY' },
              { label: t('action_disable'), value: 'DISABLE' }
            ]"
            value-by="value"
            text-by="label"
          />
        </div>

        <!-- Mode 1: GUI Builder -->
        <div v-if="newField.conditionMode === 'GUI'" style="display: grid; grid-template-columns: 2fr 1.5fr 2fr; gap: 0.5rem; align-items: center;">
          <va-select
            v-model="newField.dependsOnFieldKey"
            :label="t('depends_on')"
            :options="availableConditionFields"
            value-by="value"
            text-by="text"
            clearable
          />
          <va-select
            v-model="newField.conditionOperator"
            :label="t('operator')"
            :options="[
              { label: '== (일치)', value: 'EQUALS' },
              { label: '!= (불일치)', value: 'NOT_EQUALS' },
              { label: '> (초과)', value: 'GREATER_THAN' },
              { label: '>= (이상)', value: 'GREATER_THAN_OR_EQUAL' },
              { label: '< (미만)', value: 'LESS_THAN' },
              { label: '<= (이하)', value: 'LESS_THAN_OR_EQUAL' },
              { label: 'Contains (포함)', value: 'CONTAINS' },
              { label: 'Is Not Empty (값 존재)', value: 'NOT_EMPTY' },
              { label: 'Is Empty (값 없음)', value: 'EMPTY' }
            ]"
            value-by="value"
            text-by="label"
          />
          <va-input
            v-model="newField.conditionValue"
            label="비교 기준값"
            placeholder="예: KOSPI 또는 1"
          />
        </div>

        <!-- Mode 2: Expression Mode -->
        <div v-else style="display: flex; flex-direction: column; gap: 0.4rem;">
          <va-input
            v-model="newField.conditionExpression"
            label="표현식 수식 입력"
            placeholder="예: #{market} == 'KOSPI' && #{per} > 1"
            class="w-full"
          />
          <div style="font-size: 0.75rem; color: var(--va-text-secondary); background: rgba(0,0,0,0.03); padding: 0.4rem 0.6rem; border-radius: 4px;">
            💡 <b>작성 팁:</b> <code>#{field_key}</code> 형태로 변수를 사용하세요. 예: <code>#{market} == 'KOSPI'</code>, <code>#{per} > 1</code>, <code>#{market} == 'KOSPI' || #{market} == 'KOSDAQ'</code>
          </div>
        </div>
      </div>
    </div>

    <div style="display: flex; justify-content: flex-end; gap: 1rem; margin-top: 1.5rem;">
      <va-button preset="secondary" @click="$emit('update:modelValue', false)">{{ t('cancel') }}</va-button>
      <va-button v-if="canEdit" :disabled="isCurrentFieldPendingApproval" @click="$emit('save')">{{ isEditMode ? t('save') : t('create') }}</va-button>
    </div>
  </va-modal>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import { AgGridVue } from 'ag-grid-vue3'
import { formatMultilingual } from '~/composables/useMultilingual'

const { t } = useI18n()

const formatNodeTitle = (node) => {
  if (!node) return ''
  if (node.name) return formatMultilingual(node.name)
  return node.label || ''
}

defineProps({
  modelValue: { type: Boolean, default: false },
  isEditMode: { type: Boolean, default: false },
  selectedNode: { type: Object, default: null },
  isCurrentFieldPendingApproval: { type: Boolean, default: false },
  newField: { type: Object, required: true },
  availableClassificationNodes: { type: Array, default: () => [] },
  groupOptions: { type: Array, default: () => [] },
  fieldTypes: { type: Array, default: () => [] },
  maskingPatternOptions: { type: Array, default: () => [] },
  domainOptions: { type: Array, default: () => [] },
  unitOptions: { type: Array, default: () => [] },
  isDark: { type: Boolean, default: false },
  gridTheme: { type: [Object, String], default: null },
  autoSizeStrategy: { type: Object, default: () => ({ type: 'fitGridWidth' }) },
  optionsColumnDefs: { type: Array, default: () => [] },
  newFieldOptionsList: { type: Array, default: () => [] },
  optionsDefaultColDef: { type: Object, default: () => ({}) },
  availableConditionFields: { type: Array, default: () => [] },
  canEdit: { type: Boolean, default: true }
})

defineEmits([
  'update:modelValue',
  'target-node-selected',
  'is-domain-field-checked',
  'add-grid-option',
  'remove-selected-grid-option',
  'options-grid-ready',
  'save'
])
</script>

<style scoped>
.option-pill {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  background: var(--va-background-secondary);
  border: 1px solid var(--va-background-border);
  font-size: 0.85rem;
  color: var(--va-text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}
.option-pill:hover {
  border-color: var(--va-primary);
  background: rgba(21, 78, 193, 0.05);
}
.option-pill.active {
  background: rgba(21, 78, 193, 0.1);
  border-color: var(--va-primary);
  color: var(--va-text-primary);
  font-weight: 600;
}
</style>
