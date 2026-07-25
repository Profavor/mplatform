<template>
  <div style="display: flex; flex-direction: column; height: 100%; min-height: 0;">
    <h1 style="font-size: 2rem; font-weight: bold; margin-bottom: 1.5rem; flex: 0 0 auto;">{{ $t('domain_schema_title') || 'Domain Schema Management' }}</h1>
    
    <div class="schema-layout" style="flex: 1; min-height: 0;">
      <!-- Tree Column -->
      <div class="schema-tree-column">
        <va-card>
          <va-card-title>
            {{ $t('classification_tree') || 'Classification Tree' }}
          </va-card-title>
          <va-card-content>
            <div class="schema-tree-wrapper">
              <ClassificationTree
                ref="treeRef"
                :selectedNode="selectedNode"
                :showEdit="true"
                emptyMessage="분류체계 트리가 없습니다. 하단의 Domain 버튼을 눌러 새 도메인을 생성해주세요."
                @select="onNodeSelected"
                @edit="handleNodeEdit"
                @loaded="onTreeLoaded"
              />
            </div>
            <div style="display: flex; gap: 0.75rem; margin-top: 1.5rem; padding: 0 0.5rem;">
              <va-button v-if="isAdmin || hasPermission('domain:write') || hasPermission('domain:*')" style="flex: 1; border-radius: 8px; box-shadow: 0 2px 6px rgba(21,78,193,0.15);" icon="create_new_folder" @click="openDomainModal()" color="primary">Domain</va-button>
              <va-button v-if="hasPermission('node:write') || hasPermission('node:*')" style="flex: 1; border-radius: 8px; box-shadow: 0 2px 6px rgba(21,78,193,0.15);" icon="note_add" @click="openNodeModal()" :disabled="!selectedNode" color="primary" :preset="selectedNode ? 'primary' : 'secondary'">Node</va-button>
            </div>
            <div v-if="!isAdmin" style="margin-top: 0.75rem; padding: 0 0.5rem;">
              <va-button preset="secondary" style="width: 100%;" @click="showRequestAccessModal = true">Request Domain Access</va-button>
            </div>
            <div style="margin-top: 0.75rem; padding: 0 0.5rem;">
              <va-button style="width: 100%; border-radius: 8px; font-weight: 600;" color="info" icon="tune" @click="openSectorGroupModal" :disabled="!treeNodes || treeNodes.length === 0" preset="secondary">{{ $t('manage_sectors_groups') || 'Manage Sectors & Groups' }}</va-button>
            </div>
          </va-card-content>
        </va-card>
      </div>
      
      <!-- Detail Column -->
      <div class="schema-detail-column">
        <va-card v-if="selectedNode" style="flex: 1; display: flex; flex-direction: column; min-height: 0;">
          <va-card-title>
            <va-tabs v-model="activeTab" style="width: 100%;">
              <template #tabs>
                <va-tab>{{ $t('tab_fields') || 'Fields' }}</va-tab>
                <va-tab>{{ $t('tab_workflows') || 'Workflows' }}</va-tab>
                <va-tab>{{ $t('schema_history.title') || 'Schema History' }}</va-tab>
              </template>
            </va-tabs>
          </va-card-title>
          <va-card-content style="flex: 1; display: flex; flex-direction: column; min-height: 0; padding: 0;">
            <!-- Fields Tab -->
            <div v-show="activeTab === 0" style="flex: 1; display: flex; flex-direction: column; min-height: 0; padding: 1rem;">
              <div class="schema-grid-wrapper">
                <ag-grid-vue
                  style="width: 100%; height: 100%;"
                  :theme="gridTheme"
                  :autoSizeStrategy="autoSizeStrategy"
                  :columnDefs="columnDefs"
                  :rowHeight="42"
                  :rowSelection="{ mode: 'singleRow' }"
                  rowModelType="infinite"
                  :pagination="true"
                  :paginationPageSize="20"
                  :cacheBlockSize="20"
                  @grid-ready="onGridReady"
                  @column-resized="onColumnChanged"
                  @column-moved="onColumnChanged"
                  @rowDoubleClicked="onRowDoubleClicked"
                />
              </div>
              
              <div style="display: flex; justify-content: flex-end; margin-top: 1rem;">
                <va-button v-if="hasPermission('field:write') || hasPermission('field:*')" icon="add" @click="openFieldModal(null)">Add Field</va-button>
              </div>
            </div>

            <WorkflowConfigTab
              v-show="activeTab === 1"
              :workflowConfigs="workflowConfigs"
              :userOptions="userOptions"
              :isSavingWorkflows="isSavingWorkflows"
              @addStep="addStep"
              @removeStep="removeStep"
              @moveStepUp="moveStepUp"
              @moveStepDown="moveStepDown"
              @addUserToStep="addUserToStep"
              @removeUserFromStep="removeUserFromStep"
              @save="saveWorkflowConfigs"
            />

            <!-- Schema History Tab -->
            <div v-show="activeTab === 2" style="flex: 1; display: flex; flex-direction: column; min-height: 0; padding: 1rem; overflow-y: auto;">
              <SchemaHistoryTab :domain-id="selectedNode?.domainId || selectedNode?.id || (selectedNode?.type === 'domain' ? selectedNode?.id : null)" />
            </div>
          </va-card-content>
        </va-card>
        <va-card v-else>
          <va-card-content style="text-align: center; padding: 3rem; color: #666;">
            Select a Classification Node from the tree to view or add fields.
          </va-card-content>
        </va-card>
      </div>
    </div>

    <!-- Domain Modal -->
    <va-modal v-model="showDomainModal" :title="isEditMode ? 'Edit Domain' : 'Create New Domain'" :ok-text="isEditMode ? 'Save' : 'Create'" cancel-text="Cancel" @ok="saveDomain" :prevent-click-outside="true">
      <div style="display: flex; gap: 1rem;">
        <va-input v-model="newDomain.name.ko" label="Domain Name (KO)" class="mb-4" style="flex: 1; min-width: 0;" />
        <va-input v-model="newDomain.name.en" label="Domain Name (EN)" class="mb-4" style="flex: 1; min-width: 0;" />
      </div>
      <div style="display: flex; gap: 1rem;">
        <va-input v-model="newDomain.description.ko" label="Description (KO)" class="mb-4" style="flex: 1; min-width: 0;" />
        <va-input v-model="newDomain.description.en" label="Description (EN)" class="mb-4" style="flex: 1; min-width: 0;" />
      </div>
      <div class="mb-4">
        <label style="font-weight: bold; margin-bottom: 0.5rem; display: block; font-size: 0.9rem; color: var(--va-text-primary);">Domain Icon</label>
        <div style="display: flex; align-items: center; gap: 1rem;">
          <va-icon :name="newDomain.icon || 'folder'" size="large" color="primary" />
          <va-button size="small" preset="secondary" border-color="primary" @click="openIconPicker(true)">Select Icon</va-button>
        </div>
      </div>
      <va-input v-model="newDomain.numberingPattern" label="Numbering Pattern" placeholder="e.g. ITEM-{YYYY}-{SEQ:5} (Leave empty for manual)" class="mb-4 w-full" />
      <va-input v-model="newDomain.sortOrder" type="number" label="Sort Order" class="mb-4 w-full" />
      <va-switch v-model="newDomain.autoDqScanEnabled" label="Auto Data Quality Scan (매일 새벽 2시 자동 정기 검사)" class="mb-4 w-full" color="primary" />
      
      <!-- Field Mappings (Only show in Edit mode because we need fields) -->
      <div v-if="isEditMode" style="margin-top: 1rem; border-top: 1px solid #eee; padding-top: 1rem;">
        <div style="margin-bottom: 0.5rem; font-weight: bold; font-size: 0.9rem; color: #666;">Domain Field Mappings (Required)</div>
        <va-select
          v-model="newDomain.identifierFieldId"
          :options="domainFieldOptions"
          value-by="value"
          text-by="text"
          label="Identifier Field (ID)*"
          class="mb-4 w-full"
          :error="mappingError.id"
          :error-messages="['Required']"
        />
        <va-select
          v-model="newDomain.displayNameFieldId"
          :options="domainFieldOptions.filter(o => o.type === 'MULTILINGUAL')"
          value-by="value"
          text-by="text"
          label="Display Name Field*"
          class="mb-4 w-full"
          :error="mappingError.name"
          :error-messages="['Required']"
        />
        <va-select
          v-model="newDomain.descriptionFieldId"
          :options="domainFieldOptions"
          value-by="value"
          text-by="text"
          label="Description Field (Optional)"
          class="mb-4 w-full"
          clearable
        />
      </div>
    </va-modal>

    <!-- Node Modal -->
    <va-modal v-model="showNodeModal" :title="isEditMode ? `Edit Node` : `Add Node to ${selectedNode?.label}`" :ok-text="isEditMode ? 'Save' : 'Create'" cancel-text="Cancel" @ok="saveNode">
      <div style="display: flex; gap: 1rem;">
        <va-input v-model="newNode.name.ko" label="Node Name (KO)" class="mb-4" style="flex: 1; min-width: 0;" />
        <va-input v-model="newNode.name.en" label="Node Name (EN)" class="mb-4" style="flex: 1; min-width: 0;" />
      </div>
      <va-input v-model="newNode.order" type="number" label="Order" class="mb-4 w-full" />
      <div class="mb-4">
        <label style="font-weight: bold; margin-bottom: 0.5rem; display: block; font-size: 0.9rem; color: var(--va-text-primary);">Node Icon</label>
        <div style="display: flex; align-items: center; gap: 1rem;">
          <va-icon :name="newNode.icon || 'article'" size="large" color="primary" />
          <va-button size="small" preset="secondary" border-color="primary" @click="openIconPicker(false)">Select Icon</va-button>
        </div>
      </div>
    </va-modal>

    <!-- Field Modal -->
    <va-modal v-model="showFieldModal" :title="isEditMode ? `Edit Field` : `Add Field to ${selectedNode?.label}`" hide-default-actions size="large">
      <div style="display: flex; gap: 1rem;">
        <va-input v-model="newField.name.ko" label="Field Name (KO)" class="mb-4" style="flex: 1; min-width: 0;" />
        <va-input v-model="newField.name.en" label="Field Name (EN)" class="mb-4" style="flex: 1; min-width: 0;" />
      </div>
      <va-select 
        v-model="newField.fieldGroupId" 
        :options="groupOptions" 
        value-by="value"
        label="Group (Sector is mapped automatically)" 
        class="mb-4 w-full" 
      />
      <div style="display: flex; gap: 1rem;">
        <va-input v-model="newField.key" label="Field Key" class="mb-4" style="flex: 1; min-width: 0;" />
        <va-input v-model="newField.order" type="number" label="Sort Order" class="mb-4" style="flex: 1; min-width: 0;" />
      </div>
      
      <va-select v-model="newField.type" :options="fieldTypes" label="Field Type" class="mb-4 w-full" />
        
        <va-select 
          v-if="newField.type === 'DOMAIN_REFERENCE'" 
          v-model="newField.targetDomainId" 
          :options="domainOptions" 
          value-by="value"
          label="Target Domain" 
          class="mb-4 w-full" 
        />
      
      <div v-if="['SELECT', 'MULTI_SELECT'].includes(newField.type)" class="mb-4 w-full" style="border: 1px solid #ccc; padding: 1rem; border-radius: 8px;">
        <label style="font-weight: bold; margin-bottom: 0.5rem; display: block;">{{ t('options_settings') }}</label>
        
        <div style="margin-bottom: 0.5rem; display: flex; gap: 0.5rem; justify-content: flex-end;">
          <va-button size="small" icon="add" @click="addGridOption">{{ t('add_option') }}</va-button>
          <va-button size="small" icon="remove" color="danger" @click="removeSelectedGridOption" :outline="isDark">{{ t('remove_selected') }}</va-button>
        </div>
        
        <ag-grid-vue
          style="width: 100%; height: 250px;"
          :theme="gridTheme"
          :autoSizeStrategy="autoSizeStrategy"
          :columnDefs="optionsColumnDefs"
          :rowData="newFieldOptionsList"
          :defaultColDef="optionsDefaultColDef"
          :rowSelection="{ mode: 'singleRow' }"
          @grid-ready="onOptionsGridReady"
        />
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
            <span style="flex: 1;">{{ t('searchable_1') }}</span>
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
            <span style="flex: 1;">Highlight (강조)</span>
          </div>
        </div>
      </div>

      <!-- Conditional Field Control Section (조건부 연동 설정) -->
      <div style="margin-top: 1.25rem; padding: 1rem; background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
          <div style="font-size: 0.75rem; font-weight: 700; color: var(--va-primary); text-transform: uppercase; letter-spacing: 0.5px; display: flex; align-items: center; gap: 4px;">
            <va-icon name="bolt" size="small" color="primary" />
            Conditional Field Control (조건부 연동 설정)
          </div>
          <va-checkbox v-model="newField.conditionEnabled" label="연동 사용 (Enable)" />
        </div>

        <div v-if="newField.conditionEnabled" style="display: flex; flex-direction: column; gap: 0.75rem; padding-top: 0.25rem;">
          <!-- Mode Tabs -->
          <div style="display: flex; gap: 0.5rem; align-items: center; font-size: 0.85rem;">
            <span style="font-weight: 600; color: var(--va-text-secondary); width: 80px;">설정 방식:</span>
            <va-button-toggle
              v-model="newField.conditionMode"
              size="small"
              :options="[
                { label: '드롭다운 선택 (GUI)', value: 'GUI' },
                { label: '표현식 직접 입력 (Expression)', value: 'EXPRESSION' }
              ]"
            />
          </div>

          <!-- Action Selector (Multi-select) -->
          <div style="display: flex; gap: 0.5rem; align-items: center;">
            <span style="font-weight: 600; font-size: 0.85rem; color: var(--va-text-secondary); width: 80px;">제어 동작:</span>
            <va-select
              v-model="newField.conditionAction"
              style="flex: 1;"
              multiple
              :options="[
                { label: '👁️ 조건 충족 시만 노출 (SHOW)', value: 'SHOW' },
                { label: '✨ 조건 충족 시 하이라이트 (HIGHLIGHT)', value: 'HIGHLIGHT' },
                { label: '🔒 조건 충족 시 필수값 지정 (REQUIRE)', value: 'REQUIRE' },
                { label: '📖 조건 충족 시 읽기 전용 (READ_ONLY)', value: 'READ_ONLY' },
                { label: '🚫 조건 충족 시 수정 금지 (DISABLE)', value: 'DISABLE' }
              ]"
              value-by="value"
              text-by="label"
            />
          </div>

          <!-- Mode 1: GUI Builder -->
          <div v-if="newField.conditionMode === 'GUI'" style="display: grid; grid-template-columns: 2fr 1.5fr 2fr; gap: 0.5rem; align-items: center;">
            <va-select
              v-model="newField.dependsOnFieldKey"
              label="기준 필드 (Depends On)"
              :options="availableConditionFields"
              value-by="value"
              text-by="text"
              clearable
            />
            <va-select
              v-model="newField.conditionOperator"
              label="연산자"
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
        <va-button preset="secondary" @click="showFieldModal = false">Cancel</va-button>
        <va-button v-if="hasPermission('field:write') || hasPermission('field:*')" @click="saveField">{{ isEditMode ? 'Save' : 'Create' }}</va-button>
      </div>
    </va-modal>

    <!-- Sector & Group Manager Modal -->
    <va-modal v-model="showSectorGroupModal" title="Manage Sectors & Groups" hide-default-actions size="large">
      <va-tabs v-model="sgActiveTab" style="width: 100%; margin-bottom: 1.5rem;">
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
              <va-button size="small" @click="addSectorRow">Add Row</va-button>
              <va-button size="small" color="danger" @click="deleteSelectedSector" :outline="isDark">Delete Selected</va-button>
            </div>
          </div>
          <div style="flex: 1; width: 100%;">
            <AgGridVue
              style="width: 100%; height: 100%;"
              :theme="gridTheme"
              :autoSizeStrategy="autoSizeStrategy"
              :columnDefs="sectorColumnDefs"
              :rowData="domainSectors"
              :defaultColDef="sgDefaultColDef"
              @grid-ready="onSectorGridReady"
              @rowValueChanged="onSectorRowSaved"
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
              <va-button size="small" @click="addGroupRow">Add Row</va-button>
              <va-button size="small" color="danger" @click="deleteSelectedGroup" :outline="isDark">Delete Selected</va-button>
            </div>
          </div>
          <div style="flex: 1; width: 100%;">
            <AgGridVue
              style="width: 100%; height: 100%;"
              :theme="gridTheme"
              :autoSizeStrategy="autoSizeStrategy"
              :columnDefs="groupColumnDefs"
              :rowData="domainGroups"
              :defaultColDef="sgDefaultColDef"
              @grid-ready="onGroupGridReady"
              @rowValueChanged="onGroupRowSaved"
              editType="fullRow"
              :rowSelection="{ mode: 'singleRow' }"
            />
          </div>
        </div>
      </div>
      <div style="display: flex; justify-content: space-between; margin-top: 1rem;">
        <span style="font-size: 0.85em; color: #888;">* Double-click a row to edit. Press Enter to save.</span>
        <va-button @click="showSectorGroupModal = false">Close</va-button>
      </div>
    </va-modal>

    <!-- Icon Picker Modal -->
    <va-modal v-model="showIconPickerModal" title="Select Icon" size="medium" hide-default-actions>
      <IconPicker v-model="tempIcon" />
      <div style="display: flex; justify-content: flex-end; margin-top: 1rem; gap: 0.5rem;">
        <va-button preset="secondary" @click="showIconPickerModal = false">Cancel</va-button>
        <va-button @click="applyIcon">Confirm</va-button>
      </div>
    </va-modal>

    <!-- Request Access Modal -->
    <DomainAccessRequestModal v-model="showRequestAccessModal" />

    <!-- DQ Rule Editor Modal -->
    <DqRuleEditor
      v-model="showDqRuleEditor"
      :fieldId="dqTargetFieldId"
      :fieldName="dqTargetFieldName"
    />
    <!-- System Notification Modal -->
    <va-modal
      v-model="showErrorAlertModal"
      :title="errorAlertTitle || $t('system_notification')"
      hide-default-actions
      size="small"
      :prevent-click-outside="true"
      :no-outside-dismiss="true"
    >
      <div style="padding: 1.25rem 0; text-align: center;">
        <div
          v-if="errorAlertType === 'success'"
          style="width: 60px; height: 60px; border-radius: 50%; background: rgba(30, 203, 114, 0.12); color: #15803d; display: flex; align-items: center; justify-content: center; margin: 0 auto 1.25rem auto;"
        >
          <va-icon name="check_circle" size="2.5rem" color="success" />
        </div>
        <div
          v-else-if="errorAlertType === 'warning'"
          style="width: 60px; height: 60px; border-radius: 50%; background: rgba(232, 139, 36, 0.12); color: #c2410c; display: flex; align-items: center; justify-content: center; margin: 0 auto 1.25rem auto;"
        >
          <va-icon name="warning" size="2.5rem" color="warning" />
        </div>
        <div
          v-else
          style="width: 60px; height: 60px; border-radius: 50%; background: rgba(229, 57, 53, 0.12); color: #b91c1c; display: flex; align-items: center; justify-content: center; margin: 0 auto 1.25rem auto;"
        >
          <va-icon name="error" size="2.5rem" color="danger" />
        </div>

        <h3
          style="margin: 0 0 0.75rem 0; font-weight: 700; font-size: 1.25rem;"
          :style="{
            color: errorAlertType === 'success' ? '#15803d' : (errorAlertType === 'warning' ? '#c2410c' : '#b91c1c')
          }"
        >
          {{ errorAlertHeader || $t('system_notification') }}
        </h3>

        <div style="background: var(--va-background-secondary); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 1rem 1.25rem; text-align: left; font-size: 0.92rem; color: var(--va-text-primary); max-height: 200px; overflow-y: auto; margin-bottom: 1.5rem; word-break: break-word; white-space: pre-wrap;">
          {{ errorAlertMessage }}
        </div>

        <div style="display: flex; justify-content: center;">
          <va-button
            :color="errorAlertType === 'success' ? 'success' : (errorAlertType === 'warning' ? 'warning' : 'primary')"
            preset="solid"
            style="min-width: 120px;"
            @click="showErrorAlertModal = false"
          >
            {{ $t('close') || '확인' }}
          </va-button>
        </div>
      </div>
    </va-modal>
  </div>
</template>

<script setup>
const colors = useColors()
const isDark = computed(() => colors.currentPresetName.value === 'dark')
import { usePermission } from '~/composables/usePermission'

const { t } = useI18n()
const { currentPresetName } = useColors()
const { hasPermission } = usePermission()
import { ref, computed, onMounted, watch } from 'vue'
import { useCookie, useState } from '#app'

import { AgGridVue } from 'ag-grid-vue3'
import SchemaHistoryTab from '~/components/schema/SchemaHistoryTab.vue'

const { gridTheme, autoSizeStrategy } = useAgGridTheme()

const showErrorAlertModal = ref(false)
const errorAlertTitle = ref('')
const errorAlertHeader = ref('')
const errorAlertMessage = ref('')
const errorAlertType = ref('success')

const showCustomAlert = (msg, header = '', title = '', type = 'success') => {
  errorAlertMessage.value = msg
  errorAlertHeader.value = header
  errorAlertTitle.value = title
  errorAlertType.value = type
  showErrorAlertModal.value = true
}

const currentLocale = useCookie('locale', { default: () => 'ko' })
const token = useCookie('auth_token', { default: () => '' })
const userCookie = useCookie('user_data')
const getAuthHeaders = () => token.value ? { Authorization: `Bearer ${token.value}` } : {}

const activeTab = ref(0)
const sgActiveTab = ref(0)
const workflowConfigs = ref({
  CREATE: { steps: [], observerIds: [] },
  UPDATE: { steps: [], observerIds: [] },
  DELETE: { steps: [], observerIds: [] }
})
const userOptions = ref([])
const unitOptions = ref([])
const treeNodes = ref([])
const selectedNode = ref(null)
const fields = ref([])

const createFieldsDatasource = () => {
  return {
    getRows: async (params) => {
      if (!selectedNode.value) {
        params.successCallback([], 0);
        return;
      }
      
      const size = params.endRow - params.startRow;
      const page = Math.floor(params.startRow / size);
      
      try {
        const endpoint = selectedNode.value.isDomain 
          ? `/api/domains/${selectedNode.value.id}/fields/page?page=${page}&size=${size}`
          : `/api/nodes/${selectedNode.value.id}/fields/effective/page?page=${page}&size=${size}`;
          
        const pageData = await $fetch(endpoint, { headers: getAuthHeaders() });
        params.successCallback(pageData.content, pageData.totalElements);
      } catch (e) {
        console.error('Failed to load fields page:', e);
        params.failCallback();
      }
    }
  };
};

const currentUser = computed(() => {
  if (userCookie.value) {
    return typeof userCookie.value === 'string' ? JSON.parse(userCookie.value) : userCookie.value
  }
  return null
})

const isAdmin = computed(() => {
  const r = currentUser.value?.role
  if (!r) return false
  const roles = Array.isArray(r) ? r : String(r).split(',').map(item => item.trim()).filter(Boolean)
  return roles.some(role => role === 'ROLE_ADMIN' || role === 'ORG_ADMIN')
})

const fetchFields = () => {
  if (fieldsGridApi.value) {
    fieldsGridApi.value.setGridOption('datasource', createFieldsDatasource());
  }
}

const fieldsGridApi = ref(null)
const gridApi = ref(null)

const showDomainModal = ref(false)
const showNodeModal = ref(false)
const showFieldModal = ref(false)
const showSectorGroupModal = ref(false)
const showIconPickerModal = ref(false)
const tempIcon = ref('')
const isPickingForDomain = ref(true)

const openIconPicker = (forDomain) => {
  isPickingForDomain.value = forDomain
  tempIcon.value = forDomain ? newDomain.value.icon : newNode.value.icon
  showIconPickerModal.value = true
}

const applyIcon = () => {
  if (isPickingForDomain.value) {
    newDomain.value.icon = tempIcon.value
  } else {
    newNode.value.icon = tempIcon.value
  }
  showIconPickerModal.value = false
}

const showRequestAccessModal = ref(false)

// DQ Rule Editor
const showDqRuleEditor = ref(false)
const dqTargetFieldId = ref(null)
const dqTargetFieldName = ref('')
const openDqRuleEditor = (fieldData) => {
  if (!fieldData) return
  dqTargetFieldId.value = fieldData.id
  const nameObj = typeof fieldData.name === 'string' ? JSON.parse(fieldData.name || '{}') : fieldData.name
  dqTargetFieldName.value = nameObj?.[currentLocale.value] || nameObj?.ko || nameObj?.en || fieldData.key || 'Field'
  showDqRuleEditor.value = true
}

const isEditMode = ref(false)
const editingId = ref(null)

const newDomain = ref({ 
  name: { ko: '', en: '' }, 
  description: { ko: '', en: '' }, 
  identifierFieldId: null, 
  displayNameFieldId: null, 
  descriptionFieldId: null,
  icon: '',
  sortOrder: 0,
  autoDqScanEnabled: false
})
const domainFieldOptions = ref([])
const mappingError = ref({ id: false, name: false })

const newNode = ref({ name: { ko: '', en: '' }, order: 1, icon: '' })
const newField = ref({ 
  name: { ko: '', en: '' }, 
  fieldGroupId: null,
  key: '', 
  type: 'TEXT', 
  options: '', 
  formula: '',
  targetDomainId: null,
  required: false, 
  isMultiValue: false, 
  isSearchable: true, 
  isEncrypted: false,
  isReadOnly: false,
  isImmutable: false,
  isHidden: false,
  isHighlighted: false,
  unit: '',
  order: 0
})

watch(() => newField.value.key, (newVal) => {
  if (newVal) {
    const sanitized = newVal.toUpperCase().replace(/[^A-Z_]/g, '');
    if (newVal !== sanitized) {
      newField.value.key = sanitized;
    }
  }
})

const newFieldOptionsList = ref([])
const optionsGridApi = ref(null)

const optionsDefaultColDef = {
  flex: 1,
  editable: true,
  sortable: true,
  resizable: true,
}

const optionsColumnDefs = [
  { 
    headerName: 'Key', 
    field: 'key',
    valueSetter: params => {
      params.data.key = String(params.newValue || '').toUpperCase().replace(/[^A-Z_]/g, '');
      return true;
    },
    cellStyle: params => {
      if (!params.value || String(params.value).trim() === '') {
        return { backgroundColor: '#fff3cd' } // Soft yellow warning
      }
      return null
    }
  },
  { headerName: 'Label (KO)', field: 'label.ko', valueSetter: params => { params.data.label.ko = params.newValue; return true; } },
  { headerName: 'Label (EN)', field: 'label.en', valueSetter: params => { params.data.label.en = params.newValue; return true; } },
  { headerName: 'Order', field: 'order', valueParser: params => Number(params.newValue) || 0 }
]

const sgDefaultColDef = { flex: 1, sortable: true, resizable: true }

const sectorColumnDefs = [
  { field: 'name.ko', headerName: 'Name (KO)', editable: true },
  { field: 'name.en', headerName: 'Name (EN)', editable: true },
  { field: 'sortOrder', headerName: 'Order', width: 100, flex: 0, editable: true, valueParser: p => Number(p.newValue) || 0 }
]

const groupColumnDefs = [
  { 
    headerName: 'Sector',
    editable: true,
    cellEditor: 'agSelectCellEditor',
    cellEditorParams: params => ({ values: domainSectors.value.map(s => s.id) }),
    valueGetter: p => p.data.sector?.id,
    valueSetter: p => {
      if (!p.data.sector) p.data.sector = {}
      p.data.sector.id = p.newValue
      return true
    },
    valueFormatter: params => {
      const s = domainSectors.value.find(sec => sec.id === params.value)
      return s ? (s.name?.ko || s.name?.en) : ''
    }
  },
  { field: 'name.ko', headerName: 'Name (KO)', editable: true },
  { field: 'name.en', headerName: 'Name (EN)', editable: true },
  { field: 'sortOrder', headerName: 'Order', width: 100, flex: 0, editable: true, valueParser: p => Number(p.newValue) || 0 },
  { 
    field: 'isDefaultOpen', 
    headerName: 'Default Open', 
    editable: true,
    cellEditor: 'agSelectCellEditor',
    cellEditorParams: { values: [true, false] },
    valueFormatter: p => p.value !== false ? 'O' : 'X',
    width: 140,
    flex: 0 
  }
]

const onOptionsGridReady = (params) => {
  optionsGridApi.value = params.api
}

const addGridOption = () => {
  newFieldOptionsList.value.push({ key: '', label: { ko: '', en: '' }, order: 0 })
  if (optionsGridApi.value) {
    optionsGridApi.value.setRowData(newFieldOptionsList.value)
  }
}

const removeSelectedGridOption = () => {
  if (!optionsGridApi.value) return
  const selectedNodes = optionsGridApi.value.getSelectedNodes()
  if (selectedNodes.length > 0) {
    const selectedData = selectedNodes.map(node => node.data)
    newFieldOptionsList.value = newFieldOptionsList.value.filter(opt => !selectedData.includes(opt))
    optionsGridApi.value.setRowData(newFieldOptionsList.value)
  }
}

const domainSectors = ref([])
const domainGroups = ref([])

const sectorGridApi = ref(null)
const groupGridApi = ref(null)

const onSectorGridReady = (params) => sectorGridApi.value = params.api
const onGroupGridReady = (params) => groupGridApi.value = params.api

const sectorOptions = computed(() => {
  return domainSectors.value.map(s => ({
    text: s.name?.[currentLocale.value] || s.name?.ko || s.name?.en || 'Unknown',
    value: s.id
  }))
})

const groupOptions = computed(() => {
  return domainGroups.value.map(g => {
    const sName = g.sector?.name?.[currentLocale.value] || g.sector?.name?.ko || g.sector?.name?.en || 'Unknown Sector';
    const gName = g.name?.[currentLocale.value] || g.name?.ko || g.name?.en || 'Unknown Group';
    return {
      text: `[${sName}] ${gName}`,
      value: g.id
    }
  })
})

const fieldTypes = ['TEXT', 'NUMBER', 'DATE', 'BOOLEAN', 'SELECT', 'DECIMAL', 'FLOAT', 'INTEGER', 'DOMAIN_REFERENCE', 'MULTI_SELECT', 'TIME', 'HTML_TEXT', 'CHECKBOX', 'CALCULATED', 'MULTILINGUAL', 'FILE']

const domainOptions = computed(() => {
  return treeNodes.value.filter(n => n.isDomain).map(d => ({
    value: d.id,
    text: d.label || d.id
  }))
})

const columnDefs = computed(() => [
  { 
    headerName: 'Name', 
    field: 'name', 
    sortable: true,
    flex: 1,
    valueGetter: (params) => {
      if (!params.data) return '';
      const pName = typeof params.data.name === 'string' ? JSON.parse(params.data.name || '{}') : params.data.name;
      return pName?.[currentLocale.value] || pName?.ko || pName?.en || 'Unknown';
    }
  },
  { 
    headerName: 'Sector', 
    field: 'fieldGroup', 
    sortable: true,
    width: 150,
    valueGetter: (params) => {
      if (!params.data || !params.data.fieldGroup || !params.data.fieldGroup.sector) return '';
      const sName = params.data.fieldGroup.sector.name;
      return sName?.[currentLocale.value] || sName?.ko || sName?.en || '';
    }
  },
  { 
    headerName: 'Group', 
    field: 'fieldGroup', 
    sortable: true,
    width: 150,
    valueGetter: (params) => {
      if (!params.data || !params.data.fieldGroup) return '';
      const gName = params.data.fieldGroup.name;
      return gName?.[currentLocale.value] || gName?.ko || gName?.en || '';
    }
  },
  { headerName: 'Key', field: 'key', sortable: true, flex: 1 },
  { headerName: 'Order', field: 'order', sortable: true, width: 90 },
  { headerName: 'Grid Width', field: 'gridWidth', sortable: true, width: 120 },
  { headerName: 'AG-Grid Width', field: 'tableColumnWidth', sortable: true, width: 130 },
  { headerName: 'Type', field: 'type', sortable: true, width: 150 },
  { 
    headerName: 'Required', 
    field: 'required', 
    sortable: true,
    width: 120,
    cellRenderer: (params) => {
      if (!params || params.value === undefined) return '';
      const span = document.createElement('span');
      span.style.padding = '2px 6px';
      span.style.borderRadius = '4px';
      span.style.color = 'white';
      span.style.fontSize = '12px';
      span.style.fontWeight = 'bold';
      if (params.value) {
        span.style.background = '#e42222';
        span.innerText = 'Yes';
      } else {
        span.style.background = '#2c82e0';
        span.innerText = 'No';
      }
      return span;
    }
  },
  { 
    headerName: 'Actions', 
    field: 'id',
    width: 180,
    minWidth: 180,
    cellRenderer: (params) => {
      if (!params || !params.data) return '';
      const container = document.createElement('div');
      container.style.display = 'flex';
      container.style.gap = '8px';
      container.style.alignItems = 'center';
      container.style.height = '100%';

      if (params.data.domainId && !selectedNode.value.isDomain) {
        const span = document.createElement('span');
        span.style.color = '#666';
        span.style.fontStyle = 'italic';
        span.innerText = 'Inherited';
        container.appendChild(span);
      } else {
        const editBtn = document.createElement('span');
        editBtn.style.cursor = 'pointer';
        editBtn.style.color = '#2c82e0';
        editBtn.style.fontWeight = '600';
        editBtn.style.fontSize = '12px';
        editBtn.style.lineHeight = '1';
        editBtn.style.padding = '3px 8px';
        editBtn.style.borderRadius = '4px';
        editBtn.style.border = '1px solid #2c82e0';
        editBtn.style.display = 'inline-flex';
        editBtn.style.alignItems = 'center';
        editBtn.style.boxSizing = 'border-box';
        editBtn.innerText = 'Edit';
        editBtn.addEventListener('click', () => openFieldModal(params.data));
        container.appendChild(editBtn);
      }

      // DQ Rules button — always visible
      const dqBtn = document.createElement('span');
      dqBtn.style.cursor = 'pointer';
      dqBtn.style.color = '#e88b24';
      dqBtn.style.fontWeight = '600';
      dqBtn.style.fontSize = '12px';
      dqBtn.style.lineHeight = '1';
      dqBtn.style.padding = '3px 6px';
      dqBtn.style.borderRadius = '4px';
      dqBtn.style.border = '1px solid #e88b24';
      dqBtn.style.display = 'inline-flex';
      dqBtn.style.alignItems = 'center';
      dqBtn.style.boxSizing = 'border-box';
      dqBtn.innerText = 'DQ';
      dqBtn.addEventListener('click', () => openDqRuleEditor(params.data));
      container.appendChild(dqBtn);

      return container;
    }
  }
])

const onRowDoubleClicked = (params) => {
  if (params.data.domainId && !selectedNode.value.isDomain) return;
  openFieldModal(params.data)
}

const { saveState: saveSchemaGridState, restoreState: restoreSchemaGridState } = useGridState('schema_fields_grid')

const onGridReady = (params) => {
  fieldsGridApi.value = params.api
  gridApi.value = params.api
  restoreSchemaGridState(params.api)
  fetchFields()
}

const onColumnChanged = () => {
  if (fieldsGridApi.value) {
    saveSchemaGridState(fieldsGridApi.value)
  }
}

const treeRef = ref(null)

const onTreeLoaded = (nodes) => {
  treeNodes.value = nodes
}

const loadTree = async () => {
  if (treeRef.value) {
    await treeRef.value.loadTree()
  }
}

onMounted(async () => {
  if (userCookie.value?.role === 'USER') {
    useRouter().push('/')
    return
  }
  loadTree()
  try {
    const users = await $fetch('/api/auth/users', { headers: getAuthHeaders() }).catch(() => [])
    userOptions.value = users.map(u => ({ text: u.username, value: u.uuid }))
    
    unitOptions.value = [
      'kg', 'g', 'mg', 't', 'lb', 'oz', 
      'm', 'cm', 'mm', 'km', 'in', 'ft', 'yd', 'mi', 
      'm²', 'cm²', 'km²', 'ha', 
      'L', 'mL', 'm³', 'cm³', 
      's', 'min', 'h', 'd', 
      '℃', '℉', 'K', 
      'Pa', 'kPa', 'MPa', 'bar', 'atm', 'psi', 
      'N', 'kN', 'J', 'kJ', 'cal', 'kcal', 
      'W', 'kW', 'MW', 
      'V', 'A', 'Ω', 'Hz', 'kHz', 'MHz', 'GHz', 
      'B', 'KB', 'MB', 'GB', 'TB', 
      '%', '‰', 'ppm', 
      '원', '$', '€', '¥', '£', 
      'EA', 'SET', 'BOX', 'ROLL', 'SHEET', 'PCS'
    ]
  } catch (e) {
    console.error('Failed to load metadata:', e)
  }
})


const addStep = (action) => {
  workflowConfigs.value[action].steps.push({
    id: Date.now(),
    users: [{ stepType: 'APPROVAL', assigneeId: null }]
  })
}

const removeStep = (action, sIdx) => {
  workflowConfigs.value[action].steps.splice(sIdx, 1)
}

const moveStepUp = (action, sIdx) => {
  if (sIdx > 0) {
    const arr = workflowConfigs.value[action].steps
    const temp = arr[sIdx]
    arr[sIdx] = arr[sIdx - 1]
    arr[sIdx - 1] = temp
  }
}

const moveStepDown = (action, sIdx) => {
  const arr = workflowConfigs.value[action].steps
  if (sIdx < arr.length - 1) {
    const temp = arr[sIdx]
    arr[sIdx] = arr[sIdx + 1]
    arr[sIdx + 1] = temp
  }
}

const addUserToStep = (action, sIdx) => {
  workflowConfigs.value[action].steps[sIdx].users.push({ stepType: 'CONSENSUS', assigneeId: null })
}

const removeUserFromStep = (action, sIdx, uIdx) => {
  workflowConfigs.value[action].steps[sIdx].users.splice(uIdx, 1)
}

const saveWorkflowConfigs = async () => {
  if (!selectedNode.value) return
  try {
    const payloads = Object.keys(workflowConfigs.value).map(action => {
      const conf = workflowConfigs.value[action]
      const flatSteps = []
      conf.steps.forEach((step, sIdx) => {
        step.users.forEach(u => {
          if (u.assigneeId && u.stepType) {
            flatSteps.push({
              stepType: u.stepType,
              assigneeId: u.assigneeId,
              stepOrder: sIdx + 1
            })
          }
        })
      })
      
      const stepsConfig = JSON.stringify({
          steps: flatSteps,
          observerIds: conf.observerIds || []
      })
      
      return {
        actionType: action,
        stepsConfig: stepsConfig
      }
    })
    
    const url = selectedNode.value.isDomain 
      ? `/api/workflow-configs/domain/${selectedNode.value.id}`
      : `/api/workflow-configs/node/${selectedNode.value.id}`
      
    await $fetch(url, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: payloads
    })
    showCustomAlert('Workflow configurations saved successfully.', 'Save Success', 'Notification', 'success')
  } catch (e) {
    console.error('Failed to save workflows', e)
    showCustomAlert('Failed to save workflows.', 'Save Failed', 'Error', 'error')
  }
}

watch(currentLocale, () => {
  if (gridApi.value) {
    gridApi.value.refreshCells({ force: true })
  }
})

const onNodeSelected = async (nodes) => {
  const node = Array.isArray(nodes) ? nodes[0] : nodes
  selectedNode.value = node || null
  
  if (!node) {
    fields.value = []
    domainSectors.value = []
    domainGroups.value = []
    return
  }
  
  const dId = node.domainId
  try {
    const wfUrl = node.isDomain ? `/api/workflow-configs/domain/${node.id}` : `/api/workflow-configs/node/${node.id}`
    const fieldUrl = node.isDomain ? `/api/domains/${node.id}/fields` : `/api/nodes/${node.id}/fields/effective`
    const [sData, gData, wfData, fData] = await Promise.all([
      $fetch(`/api/domains/${dId}/sectors`, { headers: getAuthHeaders() }),
      $fetch(`/api/domains/${dId}/groups`, { headers: getAuthHeaders() }),
      $fetch(wfUrl, { headers: getAuthHeaders() }).catch(() => []),
      $fetch(fieldUrl, { headers: getAuthHeaders() }).catch(() => [])
    ])
    domainSectors.value = sData
    domainGroups.value = gData
    fields.value = fData || []
    
    workflowConfigs.value = {
      CREATE: { steps: [], observerIds: [] },
      UPDATE: { steps: [], observerIds: [] },
      DELETE: { steps: [], observerIds: [] }
    }
    for (const wf of wfData) {
      if (workflowConfigs.value[wf.actionType]) {
         try {
             const parsed = wf.stepsConfig ? JSON.parse(wf.stepsConfig) : { steps: [], observerIds: [] }
             
             // Convert flat steps back into grouped UI steps
             const flatSteps = parsed.steps || []
             const grouped = []
             let currentOrder = -1
             let currentStep = null
             
             for (const fs of flatSteps) {
                 if (fs.stepOrder !== currentOrder) {
                     currentOrder = fs.stepOrder
                     currentStep = { id: Date.now() + Math.random(), users: [] }
                     grouped.push(currentStep)
                 }
                 currentStep.users.push({
                     stepType: fs.stepType,
                     assigneeId: fs.assigneeId
                 })
             }
             
             workflowConfigs.value[wf.actionType].steps = grouped
             workflowConfigs.value[wf.actionType].observerIds = parsed.observerIds || []
         } catch(e) {
             console.error('Failed to parse stepsConfig', e)
         }
      }
    } // end for wf
  } catch(e) {
    console.error('Failed to load node data', e)
  }
  
  fetchFields();
}

const handleNodeEdit = async (node) => {
  const targetNode = node || selectedNode.value
  if (!targetNode) return
  isEditMode.value = true
  if (targetNode.isDomain) {
    try {
      const dFields = await $fetch(`/api/domains/${targetNode.id}/fields`, { headers: getAuthHeaders() })
      domainFieldOptions.value = dFields.map(f => {
        const pName = typeof f.name === 'string' ? JSON.parse(f.name || '{}') : (f.name || {})
        return {
          value: f.id,
          text: pName[currentLocale.value] || pName.ko || pName.en || f.key || 'Unknown',
          type: f.type
        }
      })
    } catch (e) {
      domainFieldOptions.value = []
    }
    const rawDomain = targetNode.originalData || {}
    const pDesc = typeof rawDomain.description === 'string' ? JSON.parse(rawDomain.description || '{}') : (rawDomain.description || {})
    newDomain.value = { 
      ...targetNode, 
      name: { ...(targetNode.originalNameMap || {ko:'', en:''}) },
      description: { ko: pDesc.ko || '', en: pDesc.en || '' },
      identifierFieldId: rawDomain.identifierFieldId,
      displayNameFieldId: rawDomain.displayNameFieldId,
      descriptionFieldId: rawDomain.descriptionFieldId,
      icon: rawDomain.icon || '',
      sortOrder: rawDomain.sortOrder || 0,
      numberingPattern: rawDomain.numberingPattern || '',
      autoDqScanEnabled: rawDomain.autoDqScanEnabled || false
    }
    showDomainModal.value = true
  } else {
    const rawNode = targetNode.originalData || {}
    newNode.value = { 
      ...targetNode, 
      name: { ...(targetNode.originalNameMap || {ko:'', en:''}) },
      order: rawNode.order || 0,
      icon: rawNode.icon || ''
    }
    showNodeModal.value = true
  }
}

const openDomainModal = () => {
  isEditMode.value = false
  newDomain.value = { name: {ko:'', en:''}, description: {ko:'', en:''}, identifierFieldId: null, displayNameFieldId: null, descriptionFieldId: null, icon: '', sortOrder: 0, numberingPattern: '', autoDqScanEnabled: false }
  showDomainModal.value = true
}

const openNodeModal = () => {
  if (!selectedNode.value) return
  isEditMode.value = false
  newNode.value = { name: {ko:'', en:''}, order: 0, icon: '' }
  showNodeModal.value = true
}

const getTranslatedName = (nameObj) => {
  if (!nameObj) return ''
  if (typeof nameObj === 'string') return nameObj
  return nameObj[currentLocale.value] || nameObj['ko'] || nameObj['en'] || Object.values(nameObj)[0] || ''
}

const availableConditionFields = computed(() => {
  return (fields.value || [])
    .filter(f => !isEditMode.value || f.id !== editingId.value)
    .map(f => ({
      value: f.key,
      text: `${getTranslatedName(f.name)} (${f.key})`
    }))
})

const resetConditionFields = () => {
  newField.value.conditionEnabled = false
  newField.value.conditionMode = 'GUI'
  newField.value.conditionAction = ['SHOW']
  newField.value.dependsOnFieldKey = ''
  newField.value.conditionOperator = 'EQUALS'
  newField.value.conditionValue = ''
  newField.value.conditionExpression = ''
}

const openFieldModal = async (rowData = null) => {
  if (selectedNode.value && (!fields.value || fields.value.length === 0)) {
    try {
      const fieldUrl = selectedNode.value.isDomain
        ? `/api/domains/${selectedNode.value.id}/fields`
        : `/api/nodes/${selectedNode.value.id}/fields/effective`
      fields.value = await $fetch(fieldUrl, { headers: getAuthHeaders() }).catch(() => [])
    } catch (e) {
      console.error('Failed to load fields for modal:', e)
    }
  }

  if (rowData) {
    isEditMode.value = true
    editingId.value = rowData.id
    newField.value = { 
      ...rowData, 
      name: { ...rowData.name }, 
      type: (rowData.type === 'STRING' || !rowData.type) ? 'TEXT' : rowData.type,
      formula: rowData.formula || '', 
      unit: rowData.unit || '',
      fieldGroupId: rowData.fieldGroup?.id || null,
      gridWidth: rowData.gridWidth || null,
      tableColumnWidth: rowData.tableColumnWidth || null
    }
    if (['SELECT', 'MULTI_SELECT'].includes(rowData.type)) {
      try {
        newFieldOptionsList.value = JSON.parse(rowData.options || '[]')
      } catch (e) { newFieldOptionsList.value = [] }
    } else {
      newFieldOptionsList.value = []
    }
    // Parse targetDomainId, formula, conditionRule from options
    if (rowData.options) {
      try {
        const opts = typeof rowData.options === 'string' ? JSON.parse(rowData.options) : rowData.options
        if (opts.targetDomainId) newField.value.targetDomainId = opts.targetDomainId
        if (opts.formula) newField.value.formula = opts.formula
        if (opts.conditionRule) {
          const cond = opts.conditionRule
          newField.value.conditionEnabled = cond.enabled !== false
          newField.value.conditionMode = cond.expression ? 'EXPRESSION' : 'GUI'
          let acts = ['SHOW']
          if (cond.action) {
            acts = Array.isArray(cond.action) ? cond.action : [cond.action]
          }
          newField.value.conditionAction = acts
          newField.value.dependsOnFieldKey = cond.dependsOnFieldKey || ''
          newField.value.conditionOperator = cond.operator || 'EQUALS'
          newField.value.conditionValue = cond.value || ''
          newField.value.conditionExpression = cond.expression || ''
        } else {
          resetConditionFields()
        }
      } catch (e) { resetConditionFields() }
    } else {
      resetConditionFields()
    }
  } else {
    isEditMode.value = false
    editingId.value = null
    newField.value = { 
      name: {ko:'', en:''}, key: '', type: 'TEXT', required: false, order: 0, 
      fieldGroupId: null, targetDomainId: null, isMultiValue: false, isSearchable: true, 
      isEncrypted: false, isReadOnly: false, isImmutable: false, isHidden: false, isHighlighted: false, 
      formula: '', unit: '', gridWidth: null, tableColumnWidth: null 
    }
    resetConditionFields()
    newFieldOptionsList.value = []
  }
  showFieldModal.value = true
}
const saveDomain = async () => {
  try {
    const url = isEditMode.value ? `/api/domains/${newDomain.value.id}` : `/api/domains`
    await $fetch(url, {
      method: isEditMode.value ? 'PUT' : 'POST',
      headers: getAuthHeaders(),
      body: {
        name: newDomain.value.name,
        description: newDomain.value.description,
        identifierFieldId: newDomain.value.identifierFieldId,
        displayNameFieldId: newDomain.value.displayNameFieldId,
        descriptionFieldId: newDomain.value.descriptionFieldId,
        icon: newDomain.value.icon,
        sortOrder: newDomain.value.sortOrder,
        numberingPattern: newDomain.value.numberingPattern,
        autoDqScanEnabled: newDomain.value.autoDqScanEnabled
      }
    })
    showDomainModal.value = false
    await loadTree()
  } catch (e) {
    showCustomAlert(t('error_saving_domain'), 'Domain Save Error', 'Error', 'error')
  }
}

const saveNode = async () => {
  if (!selectedNode.value) return
  
  try {
    const domainId = selectedNode.value.isDomain ? selectedNode.value.id : selectedNode.value.domainId
    const targetId = isEditMode.value ? newNode.value.id : domainId
    const url = isEditMode.value ? `/api/domains/${newNode.value.domainId}/nodes/${targetId}` : `/api/domains/${domainId}/nodes`
    const parentId = isEditMode.value ? undefined : (selectedNode.value.isDomain ? null : selectedNode.value.id)

    await $fetch(url, {
      method: isEditMode.value ? 'PUT' : 'POST',
      headers: getAuthHeaders(),
      body: {
        name: newNode.value.name,
        order: newNode.value.order,
        icon: newNode.value.icon,
        parentId: parentId
      }
    })
    showNodeModal.value = false
    await loadTree()
  } catch (e) {
    showCustomAlert('Error saving node', 'Save Error', 'Error', 'error')
  }
}

const saveField = async () => {
  const duplicate = fields.value.find(f => f.key === newField.value.key && (!isEditMode.value || f.id !== editingId.value))
  if (duplicate) {
    showCustomAlert(t('field_key_already_exists_newfield_value_key'), 'Key Duplicate', 'Warning', 'warning')
    return
  }
  
  let existingOptsObj = {}
  if (['SELECT', 'MULTI_SELECT'].includes(newField.value.type)) {
    const hasEmptyKey = newFieldOptionsList.value.some(opt => !opt.key || String(opt.key).trim() === '')
    if (hasEmptyKey) {
      showCustomAlert(t('enter_key_all_options'), 'Input Missing', 'Warning', 'warning')
      return
    }
    existingOptsObj = { optionsList: newFieldOptionsList.value }
  } else if (newField.value.type === 'DOMAIN_REFERENCE') {
    if (!newField.value.targetDomainId) {
      showCustomAlert(t('please_select_a_target_domain'), 'Domain Missing', 'Warning', 'warning')
      return
    }
    existingOptsObj = { targetDomainId: newField.value.targetDomainId }
  } else if (newField.value.type === 'CALCULATED') {
    if (!newField.value.formula || String(newField.value.formula).trim() === '') {
      showCustomAlert(t('enter_formula'), 'Formula Missing', 'Warning', 'warning')
      return
    }
    try {
      const testFormula = newField.value.formula.replace(/\${[^}]+}/g, '1')
      const ROUND = (val, dec=0) => Number(Math.round(val+'e'+dec)+'e-'+dec);
      const fn = new Function('ROUND', 'ABS', 'CEIL', 'FLOOR', `return ${testFormula};`)
      fn(ROUND, Math.abs, Math.ceil, Math.floor)
    } catch (e) {
      showCustomAlert(t('syntax_error_in_formula_e_message'), 'Formula Syntax Error', 'Error', 'error')
      return
    }
    existingOptsObj = { formula: newField.value.formula.trim() }
  } else if (newField.value.options) {
    try {
      existingOptsObj = typeof newField.value.options === 'string' ? JSON.parse(newField.value.options) : newField.value.options
    } catch(e){}
  }

  if (typeof existingOptsObj !== 'object' || Array.isArray(existingOptsObj)) {
    existingOptsObj = { rawOptions: existingOptsObj }
  }

  if (newField.value.conditionEnabled) {
    let acts = newField.value.conditionAction || ['SHOW']
    if (!Array.isArray(acts)) acts = [acts]
    existingOptsObj.conditionRule = {
      enabled: true,
      action: acts,
      expression: newField.value.conditionMode === 'EXPRESSION' ? newField.value.conditionExpression : '',
      dependsOnFieldKey: newField.value.conditionMode === 'GUI' ? newField.value.dependsOnFieldKey : '',
      operator: newField.value.conditionMode === 'GUI' ? newField.value.conditionOperator : '',
      value: newField.value.conditionMode === 'GUI' ? newField.value.conditionValue : ''
    }
  } else {
    delete existingOptsObj.conditionRule
  }

  newField.value.options = JSON.stringify(existingOptsObj)
  
  try {
    let url = ''
    if (selectedNode.value.isDomain) {
      url = isEditMode.value
        ? `/api/domains/${selectedNode.value.id}/fields/${editingId.value}`
        : `/api/domains/${selectedNode.value.id}/fields`
    } else {
      url = isEditMode.value
        ? `/api/nodes/${selectedNode.value.id}/fields/${editingId.value}`
        : `/api/nodes/${selectedNode.value.id}/fields`
    }
    const method = isEditMode.value ? 'PUT' : 'POST'

    const payload = {
      name: newField.value.name,
      fieldGroupId: newField.value.fieldGroupId || newField.value.fieldGroup?.id || null,
      key: newField.value.key,
      type: newField.value.type,
      unit: newField.value.unit,
      options: newField.value.options,
      required: newField.value.required,
      defaultValue: newField.value.defaultValue,
      order: newField.value.order,
      gridWidth: newField.value.gridWidth ? Number(newField.value.gridWidth) : null,
      tableColumnWidth: newField.value.tableColumnWidth ? Number(newField.value.tableColumnWidth) : null,
      isMultiValue: newField.value.isMultiValue,
      isTable: newField.value.isTable,
      isEncrypted: newField.value.isEncrypted,
      isSearchable: newField.value.isSearchable,
      isReadOnly: newField.value.isReadOnly,
      isImmutable: newField.value.isImmutable,
      isHidden: newField.value.isHidden,
      isHighlighted: newField.value.isHighlighted
    }

    await $fetch(url, {
      method,
      headers: { Authorization: `Bearer ${token.value}` },
      body: payload
    })
    newField.value = { 
      name: { ko: '', en: '' }, 
      fieldGroupId: null,
      key: '', type: 'TEXT', options: '', required: false, isMultiValue: false, isSearchable: true, isEncrypted: false, isReadOnly: false, isImmutable: false, isHidden: false, isHighlighted: false, order: 0 
    }
    showFieldModal.value = false
    await onNodeSelected(selectedNode.value)
  } catch (error) {
    showCustomAlert('Error saving field', 'Save Error', 'Error', 'error')
  }
}

const openSectorGroupModal = async () => {
  if (!selectedNode.value && treeNodes.value && treeNodes.value.length > 0) {
    await onNodeSelected(treeNodes.value[0])
  }
  showSectorGroupModal.value = true
}

const addSectorRow = () => {
  domainSectors.value = [...domainSectors.value, { name: { ko: '', en: '' }, sortOrder: 0 }]
}

const addGroupRow = () => {
  domainGroups.value = [...domainGroups.value, { sector: { id: null }, name: { ko: '', en: '' }, sortOrder: 0, isDefaultOpen: true }]
}

const onSectorRowSaved = async (event) => {
  const data = event.data
  const dId = selectedNode.value.domainId
  if (!data.name?.ko && !data.name?.en) {
    showCustomAlert('Sector name is required.', 'Input Missing', 'Warning', 'warning')
    domainSectors.value = await $fetch(`/api/domains/${dId}/sectors`, { headers: getAuthHeaders() })
    return
  }
  try {
    if (data.id) {
      await $fetch(`/api/domains/${dId}/sectors/${data.id}`, { method: 'PUT', headers: getAuthHeaders(), body: data })
    } else {
      await $fetch(`/api/domains/${dId}/sectors`, { method: 'POST', headers: getAuthHeaders(), body: data })
    }
    domainSectors.value = await $fetch(`/api/domains/${dId}/sectors`, { headers: getAuthHeaders() })
  } catch(e) { showCustomAlert('Error saving sector', 'Save Error', 'Error', 'error') }
}

const onGroupRowSaved = async (event) => {
  const data = event.data
  const dId = selectedNode.value.domainId
  if (!data.name?.ko && !data.name?.en) {
    showCustomAlert('Group name is required.', 'Input Missing', 'Warning', 'warning')
    domainGroups.value = await $fetch(`/api/domains/${dId}/groups`, { headers: getAuthHeaders() })
    return
  }
  if (!data.sector?.id) {
    showCustomAlert('Sector is required.', 'Input Missing', 'Warning', 'warning')
    domainGroups.value = await $fetch(`/api/domains/${dId}/groups`, { headers: getAuthHeaders() })
    return
  }
  
  const payload = {
    sectorId: data.sector.id,
    name: data.name,
    sortOrder: data.sortOrder,
    isDefaultOpen: data.isDefaultOpen
  }
  
  try {
    if (data.id) {
      await $fetch(`/api/domains/${dId}/groups/${data.id}`, { method: 'PUT', headers: getAuthHeaders(), body: payload })
    } else {
      await $fetch(`/api/domains/${dId}/groups`, { method: 'POST', headers: getAuthHeaders(), body: payload })
    }
    domainGroups.value = await $fetch(`/api/domains/${dId}/groups`, { headers: getAuthHeaders() })
  } catch(e) { showCustomAlert('Error saving group', 'Save Error', 'Error', 'error') }
}

const deleteSelectedSector = async () => {
  if (!sectorGridApi.value) return
  const selected = sectorGridApi.value.getSelectedNodes()
  if (selected.length === 0) return
  const id = selected[0].data.id
  const dId = selectedNode.value.domainId
  if (!id) {
    domainSectors.value = await $fetch(`/api/domains/${dId}/sectors`, { headers: getAuthHeaders() })
    return
  }
  await deleteSector(id)
}

const deleteSelectedGroup = async () => {
  if (!groupGridApi.value) return
  const selected = groupGridApi.value.getSelectedNodes()
  if (selected.length === 0) return
  const id = selected[0].data.id
  const dId = selectedNode.value.domainId
  if (!id) {
    domainGroups.value = await $fetch(`/api/domains/${dId}/groups`, { headers: getAuthHeaders() })
    return
  }
  await deleteGroup(id)
}

const deleteGroup = async (id) => {
  const dId = selectedNode.value.domainId
  try {
    await $fetch(`/api/domains/${dId}/groups/${id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token.value}` }
    })
    domainGroups.value = await $fetch(`/api/domains/${dId}/groups`, { headers: { Authorization: `Bearer ${token.value}` } })
    cancelEditGroup()
  } catch (e) { showCustomAlert('Error deleting group.', 'Delete Error', 'Error', 'error') }
}
</script>

<style scoped>
.schema-layout {
  display: flex;
  gap: 0.5rem;
  width: 100%;
  height: 100%;
  min-height: 0;
}
.schema-tree-column {
  width: 300px;
  min-width: 300px;
  max-width: 300px;
  overflow: hidden;
}
.schema-tree-wrapper {
  max-height: 400px;
  overflow-y: auto;
  overflow-x: hidden;
  margin-bottom: 1rem;
}
.schema-detail-column {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.schema-grid-wrapper {
  flex: 1;
  width: 100%;
  min-height: 0;
}

@media (max-width: 768px) {
  .schema-layout {
    flex-direction: column;
  }
  .schema-tree-column {
    width: 100%;
    max-width: 100%;
    min-width: 100%;
  }
  .schema-tree-wrapper {
    max-height: 250px;
  }
  .schema-detail-column {
    width: 100%;
    max-width: 100%;
    padding: 0.25rem 0;
  }
  .schema-grid-wrapper {
    height: 400px;
  }
}
</style>

<style scoped>
.mb-4 { margin-bottom: 1rem; }
.w-full { width: 100%; }

/* Tree Container */
:deep(.va-tree) {
  overflow-x: hidden;
}

.option-pill {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.45rem 0.65rem;
  border: 1px solid var(--va-background-border);
  border-radius: 8px;
  background: var(--va-background-card, #ffffff);
  cursor: pointer;
  user-select: none;
  transition: all 0.2s ease;
  font-size: 0.8rem;
  font-weight: 500;
}
.option-pill:hover {
  border-color: #2c82e0;
  background: var(--va-background-element, #f4f6f9);
}
.option-pill.active {
  border-color: #2c82e0;
  background: rgba(44, 130, 224, 0.08);
}
</style>
