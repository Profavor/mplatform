<template>
  <AppModal
    v-model="modalVisible"
    :title="modalTitle"
    icon="edit_note"
    hide-default-actions
    v-model:fullscreen="isFullscreenModal"
    size="large"
    class="custom-record-modal"
  >
    <div :style="{ maxHeight: isFullscreenModal ? 'calc(100vh - 180px)' : '65vh', overflowY: 'auto', overflowX: 'hidden', padding: '1rem 0.5rem', boxSizing: 'border-box', width: '100%' }">
      <div
        v-if="!hasWorkflow"
        style="margin-bottom: 1rem; padding: 0.5rem; background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; border-radius: 4px; text-align: center; font-weight: bold;"
      >
        {{ isEdit 
          ? 'This classification node does not have an UPDATE workflow configured. You cannot save records.' 
          : 'This classification node does not have a CREATE workflow configured. You cannot save records.' 
        }}
      </div>

      <!-- Workflow Action & Permission Rule Banner with Combo Box Selector -->
      <div v-if="hasWorkflow" style="margin-bottom: 1rem; padding: 0.75rem 1rem; background: var(--va-background-element); border: 1px solid var(--va-primary); border-radius: 8px; display: flex; align-items: center; justify-content: space-between; gap: 1rem; flex-wrap: wrap;">
        <div style="display: flex; align-items: center; gap: 0.6rem;">
          <va-icon name="assignment_turned_in" color="primary" size="medium" />
          <div>
            <div style="font-size: 0.88rem; font-weight: 700; color: var(--va-text-primary);">
              {{ isEdit ? '✏️ 정보 변경 (UPDATE) 워크플로우 서식' : '🆕 신규 등록 (CREATE) 워크플로우 서식' }}
            </div>
            <div style="font-size: 0.78rem; color: var(--va-text-secondary);">
              선택한 워크플로우 서식의 필드 권한(작성 허용 / 읽기 전용 / 숨김)에 따라 UI가 동적으로 구성됩니다.
            </div>
          </div>
        </div>

        <div style="display: flex; align-items: center; gap: 0.5rem; flex: 1; max-width: 580px;">
          <span style="font-size: 0.78rem; font-weight: 700; color: var(--va-text-secondary); white-space: nowrap;">
            적용 워크플로우:
          </span>
          <va-select
            v-model="selectedWorkflowId"
            :options="availableWorkflowOptions"
            value-by="value"
            text-by="text"
            style="flex: 1; min-width: 340px; max-width: 500px;"
            dense
            @update:model-value="onWorkflowSelected"
          />
        </div>
      </div>

      <!-- Sector Tabs -->
      <va-tabs v-model="activeSectorTab" style="margin-bottom: 1rem;">
        <template #tabs>
          <va-tab v-for="sector in groupedFieldsArray" :key="sector.key" :name="sector.key">
            {{ sector.label }}
          </va-tab>
        </template>
      </va-tabs>

      <!-- Sector Content -->
      <div v-for="(sector, idx) in groupedFieldsArray" :key="sector.key" v-show="activeSectorTab === sector.key || (!activeSectorTab && idx === 0) || activeSectorTab === idx">

        <va-accordion multiple style="width: 100%;" class="mb-4">
          <va-collapse
            v-for="group in sector.groups"
            :key="group.key"
            :header="group.label"
            v-model="group.isOpen"
            solid
            color="background-element"
            style="margin-bottom: 0.5rem;"
          >
            <div style="padding: 0.5rem 1rem; overflow: visible; box-sizing: border-box;">
              <div class="row" style="row-gap: 1.25rem; margin: 0 -0.5rem; display: flex; flex-wrap: wrap;">
                <template v-for="field in group.fields" :key="field.id">
                  <div
                    v-if="evalConditionRule(field, localRecord).show"
                    :class="['flex', 'xs' + (field.gridWidth || 12)]"
                    :data-field-key="field.key"
                    style="padding: 0 0.5rem; min-width: 0; margin-bottom: 0.5rem;"
                  >
                    <div style="display: flex; flex-direction: column; gap: 0.25rem; width: 100%; box-sizing: border-box; min-width: 0; --va-input-font-size: 0.9rem;">
                      <!-- External Label -->
                      <span :style="{ fontSize: '0.75rem', color: evalConditionRule(field, localRecord).highlight ? 'var(--va-primary)' : 'var(--va-text-secondary)', fontWeight: evalConditionRule(field, localRecord).highlight ? '800' : '600', textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: '4px', minHeight: '18px', lineHeight: '18px' }">
                        <va-icon v-if="evalConditionRule(field, localRecord).highlight" name="star" size="small" color="primary" />
                        {{ getTranslatedName(field.name) }}{{ evalConditionRule(field, localRecord).required ? ' *' : '' }}{{ field.type === 'CALCULATED' ? ' (계산됨)' : '' }}
                        <va-popover v-if="hasHint(field.hint)" :message="getTranslatedName(field.hint)" trigger="hover" placement="top">
                          <va-icon name="info" size="small" color="info" style="cursor: help; margin-left: 2px;" />
                        </va-popover>
                      </span>

                      <!-- Text / Number / Date / Email / Phone -->
                      <va-input
                        v-if="['TEXT', 'NUMBER', 'DECIMAL', 'FLOAT', 'INTEGER', 'DATE', 'EMAIL', 'PHONE'].includes(field.type)"
                        :model-value="localRecord[field.key]"
                        @update:model-value="(val) => handleMaskedInput(field, val)"
                        :type="field.type === 'DATE' ? (focusedDateFields[field.key] || localRecord[field.key] ? 'date' : 'text') : (['NUMBER', 'DECIMAL', 'FLOAT', 'INTEGER'].includes(field.type) ? 'number' : (field.type === 'EMAIL' ? 'email' : (field.type === 'PHONE' ? 'tel' : 'text')))"
                        :readonly="evalConditionRule(field, localRecord).readOnly"
                        :disabled="isAutoNumberingField(field) || evalConditionRule(field, localRecord).disabled"
                        :rules="getFieldRules(field)"
                        :lang="locale === 'en' ? 'en-US' : 'ko-KR'"
                        :placeholder="isAutoNumberingField(field) ? (locale === 'en' ? 'Auto-generated on final approval' : '자동 채번됩니다 (최종 승인 시)') : (field.type === 'DATE' ? (locale === 'en' ? 'YYYY-MM-DD' : '연도-월-일') : (field.type === 'EMAIL' ? 'example@domain.com' : ''))"
                        class="w-full"
                        @focus="focusedDateFields[field.key] = true"
                        @blur="focusedDateFields[field.key] = false"
                      />
                      <!-- Date Range -->
                      <div v-else-if="field.type === 'DATE_RANGE'" class="w-full" style="display: flex; gap: 0.5rem; flex-direction: row; align-items: center; min-width: 0;">
                        <va-input
                          :model-value="(localRecord[field.key] || '').split('~')[0] || ''"
                          @update:model-value="(val) => { const arr = (localRecord[field.key] || '').split('~'); arr[0] = val; localRecord[field.key] = arr.join('~'); if (arr.length === 1) localRecord[field.key] += '~'; }"
                          :type="focusedDateFields[field.key + '_start'] || (localRecord[field.key] || '').split('~')[0] ? 'date' : 'text'"
                          :readonly="evalConditionRule(field, localRecord).readOnly"
                          :disabled="evalConditionRule(field, localRecord).disabled"
                          :lang="locale === 'en' ? 'en-US' : 'ko-KR'"
                          :placeholder="locale === 'en' ? 'Start Date' : '시작일'"
                          style="flex: 1; min-width: 0;"
                          @focus="focusedDateFields[field.key + '_start'] = true"
                          @blur="focusedDateFields[field.key + '_start'] = false"
                        />
                        <span style="font-weight: bold; color: var(--va-text-secondary);">~</span>
                        <va-input
                          :model-value="(localRecord[field.key] || '').split('~')[1] || ''"
                          @update:model-value="(val) => { const arr = (localRecord[field.key] || '').split('~'); arr[0] = arr[0] || ''; arr[1] = val; localRecord[field.key] = arr.join('~'); }"
                          :type="focusedDateFields[field.key + '_end'] || (localRecord[field.key] || '').split('~')[1] ? 'date' : 'text'"
                          :readonly="evalConditionRule(field, localRecord).readOnly"
                          :disabled="evalConditionRule(field, localRecord).disabled"
                          :lang="locale === 'en' ? 'en-US' : 'ko-KR'"
                          :placeholder="locale === 'en' ? 'End Date' : '종료일'"
                          style="flex: 1; min-width: 0;"
                          @focus="focusedDateFields[field.key + '_end'] = true"
                          @blur="focusedDateFields[field.key + '_end'] = false"
                        />
                      </div>

                      <!-- Multilingual -->
                      <div v-else-if="field.type === 'MULTILINGUAL'" class="w-full" style="display: flex; gap: 0.5rem; flex-direction: row; min-width: 0;">
                        <va-input v-model="localRecord[field.key].ko" style="flex: 1; min-width: 0;" class="slim-multilingual-input" :readonly="evalConditionRule(field, localRecord).readOnly">
                          <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">{{ locale === 'en' ? 'Korean' : '한국어' }}</span></template>
                        </va-input>
                        <va-input v-model="localRecord[field.key].en" style="flex: 1; min-width: 0;" class="slim-multilingual-input" :readonly="evalConditionRule(field, localRecord).readOnly">
                          <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">{{ locale === 'en' ? 'English' : '영어' }}</span></template>
                        </va-input>
                      </div>

                      <!-- Calculated -->
                      <va-input
                        v-else-if="field.type === 'CALCULATED'"
                        v-model="localRecord[field.key]"
                        readonly
                        class="w-full"
                        style="background-color: #f4f6f8;"
                      />

                      <!-- HTML Editor (Rich Text) -->
                      <div v-else-if="field.type === 'HTML_TEXT' || field.type === 'HTML'" class="w-full">
                        <HtmlEditor
                          v-model="localRecord[field.key]"
                          :readonly="evalConditionRule(field, localRecord).readOnly"
                          :disabled="evalConditionRule(field, localRecord).disabled"
                          :placeholder="getTranslatedName(field.hint) || $t('editor_placeholder')"
                        />
                      </div>

                      <!-- Select -->
                      <va-select
                        v-else-if="['SELECT', 'MULTI_SELECT', 'CODE'].includes(field.type)"
                        v-model="localRecord[field.key]"
                        :options="parseOptions(field.options)"
                        :multiple="field.type === 'MULTI_SELECT' || field.isMultiValue"
                        value-by="value"
                        text-by="text"
                        class="w-full"
                        :readonly="evalConditionRule(field, localRecord).readOnly"
                      />

                      <!-- Domain Reference -->
                      <div v-else-if="field.type === 'DOMAIN_REFERENCE'" class="w-full" style="display: flex; gap: 0.5rem; align-items: center;">
                        <va-input
                          :model-value="getDomainRefDisplayName(field.key, localRecord[field.key])"
                          readonly
                          style="flex: 1;"
                        />
                        <va-button icon="search" @click="openDomainRefPicker(field.key)" />
                      </div>

                      <!-- Checkbox / Boolean -->
                      <va-checkbox
                        v-else-if="field.type === 'BOOLEAN'"
                        v-model="localRecord[field.key]"
                        class="w-full"
                        :readonly="evalConditionRule(field, localRecord).readOnly"
                      />

                      <!-- Image Uploader & Carousel Gallery -->
                      <div v-else-if="field.type === 'IMAGE'" class="w-full">
                        <ImageUploader
                          v-model="localRecord[field.key]"
                          :multiple="field.isMultiValue"
                          :readonly="evalConditionRule(field, localRecord).readOnly"
                          :disabled="evalConditionRule(field, localRecord).disabled"
                        />
                      </div>

                      <!-- File Upload -->
                      <div v-else-if="field.type === 'FILE'" class="w-full">
                        <va-file-upload
                          :model-value="[]"
                          @update:model-value="handleFilesAdded(field, $event)"
                          :type="field.isMultiValue ? 'list' : 'single'"
                          dropzone
                          class="w-full file-upload-wrapper"
                        >
                          <div style="display: flex; flex-direction: row; align-items: center; gap: 1rem; padding: 0.5rem; justify-content: center; width: 100%;">
                            <span style="font-size: 0.9rem; color: #666;">{{ $t('file_upload_dropzone') }}</span>
                            <va-button size="small">{{ $t('file_upload_button') }}</va-button>
                          </div>
                        </va-file-upload>
                        <transition-group name="flip-list" tag="div" v-if="getFilesList(localRecord[field.key]).length > 0" class="custom-file-list" @dragover.prevent>
                          <div
                            v-for="(fileObj, i) in getFilesList(localRecord[field.key])"
                            :key="fileObj.url || fileObj.name || i"
                            class="custom-file-item"
                            :draggable="field.isMultiValue"
                            @dragstart="onDragStart($event, i, localRecord[field.key])"
                            @dragenter.prevent="onDragEnter($event, i, localRecord[field.key])"
                            @dragover.prevent
                            @drop.prevent="onDrop($event, i, localRecord[field.key])"
                            @dragend="onDragEnd($event)"
                            :style="field.isMultiValue ? 'cursor: grab;' : ''"
                          >
                            <div class="custom-file-info" style="display: flex; align-items: center;">
                              <va-icon v-if="field.isMultiValue" name="drag_indicator" style="color: #666; margin-right: 8px; cursor: grab;" />
                              {{ fileObj.name || extractFilename(fileObj.url || fileObj) }}
                            </div>
                            <div class="custom-file-actions">
                              <va-icon name="delete" style="cursor: pointer; color: #E53935;" @click="removeFile(field.key, i)" />
                            </div>
                          </div>
                        </transition-group>
                      </div>

                      <!-- JSON Table / Sub-Schema Grid -->
                      <div v-else-if="field.type === 'JSON'" class="w-full">
                        <!-- Sub-table with schema definition -->
                        <div v-if="getTableColumns(field).length > 0" style="border: 1px solid var(--va-background-border); border-radius: 8px; overflow: hidden; background: var(--va-background-element);">
                          <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.5rem 0.75rem; background: var(--va-background-secondary); border-bottom: 1px solid var(--va-background-border);">
                            <span style="font-size: 0.8rem; font-weight: 700; color: var(--va-text-primary);">
                              {{ $t('total_rows_count', { count: (localRecord[field.key] || []).length }) }}
                            </span>
                            <div style="display: flex; gap: 0.5rem;">
                              <va-button size="small" icon="add" @click="addTableRow(field.key, getTableColumns(field))" :disabled="evalConditionRule(field, localRecord).disabled || evalConditionRule(field, localRecord).readOnly">
                                {{ $t('add_row') }}
                              </va-button>
                              <va-button v-if="(localRecord[field.key] || []).length > 0" preset="secondary" color="danger" size="small" @click="clearTableRows(field.key)" :disabled="evalConditionRule(field, localRecord).disabled || evalConditionRule(field, localRecord).readOnly">
                                {{ $t('clear_all_rows') }}
                              </va-button>
                            </div>
                          </div>

                          <div v-if="!localRecord[field.key] || localRecord[field.key].length === 0" style="padding: 1.5rem; text-align: center; font-size: 0.85rem; color: var(--va-text-secondary);">
                            {{ $t('empty_table_data') }}
                          </div>

                          <div v-else style="overflow-x: auto; max-height: 350px;">
                            <table style="width: 100%; border-collapse: collapse; font-size: 0.85rem;">
                              <thead>
                                <tr style="background: var(--va-background-secondary); border-bottom: 1px solid var(--va-background-border);">
                                  <th style="padding: 0.5rem; width: 40px; text-align: center; color: var(--va-text-secondary);">#</th>
                                  <th 
                                    v-for="col in getTableColumns(field)" 
                                    :key="col.key" 
                                    :style="{ padding: '0.5rem 0.75rem', textAlign: 'left', minWidth: (col.width || 120) + 'px', color: 'var(--va-text-primary)', fontWeight: 600 }"
                                  >
                                    {{ getTranslatedColName(col.name) }}
                                    <span v-if="col.required" style="color: var(--va-danger);">*</span>
                                  </th>
                                  <th style="padding: 0.5rem; width: 50px; text-align: center;"></th>
                                </tr>
                              </thead>
                              <tbody>
                                <tr 
                                  v-for="(row, rIdx) in localRecord[field.key]" 
                                  :key="rIdx"
                                  style="border-bottom: 1px solid var(--va-background-border);"
                                >
                                  <td style="padding: 0.5rem; text-align: center; color: var(--va-text-secondary); font-size: 0.75rem;">
                                    {{ rIdx + 1 }}
                                  </td>
                                  <td 
                                    v-for="col in getTableColumns(field)" 
                                    :key="col.key" 
                                    style="padding: 0.35rem 0.5rem;"
                                  >
                                    <!-- SELECT in subtable -->
                                    <va-select
                                      v-if="col.type === 'SELECT'"
                                      v-model="row[col.key]"
                                      :options="getColSelectOptions(col.options)"
                                      value-by="value"
                                      text-by="text"
                                      dense
                                      class="w-full"
                                      :readonly="evalConditionRule(field, localRecord).readOnly"
                                      :disabled="evalConditionRule(field, localRecord).disabled"
                                    />
                                    <!-- DATE in subtable -->
                                    <va-input
                                      v-else-if="col.type === 'DATE'"
                                      v-model="row[col.key]"
                                      type="date"
                                      dense
                                      class="w-full"
                                      :readonly="evalConditionRule(field, localRecord).readOnly"
                                      :disabled="evalConditionRule(field, localRecord).disabled"
                                    />
                                    <!-- NUMBER in subtable -->
                                    <va-input
                                      v-else-if="col.type === 'NUMBER'"
                                      v-model.number="row[col.key]"
                                      type="number"
                                      dense
                                      class="w-full"
                                      :readonly="evalConditionRule(field, localRecord).readOnly"
                                      :disabled="evalConditionRule(field, localRecord).disabled"
                                    />
                                    <!-- BOOLEAN in subtable -->
                                    <div v-else-if="col.type === 'BOOLEAN'" style="display: flex; justify-content: center;">
                                      <va-checkbox
                                        v-model="row[col.key]"
                                        :readonly="evalConditionRule(field, localRecord).readOnly"
                                        :disabled="evalConditionRule(field, localRecord).disabled"
                                      />
                                    </div>
                                    <!-- Default TEXT in subtable -->
                                    <va-input
                                      v-else
                                      v-model="row[col.key]"
                                      dense
                                      class="w-full"
                                      :readonly="evalConditionRule(field, localRecord).readOnly"
                                      :disabled="evalConditionRule(field, localRecord).disabled"
                                    />
                                  </td>
                                  <td style="padding: 0.35rem; text-align: center;">
                                    <va-button 
                                      preset="plain" 
                                      icon="delete" 
                                      color="danger" 
                                      size="small" 
                                      @click="deleteTableRow(field.key, rIdx)"
                                      :disabled="evalConditionRule(field, localRecord).disabled || evalConditionRule(field, localRecord).readOnly"
                                    />
                                  </td>
                                </tr>
                              </tbody>
                            </table>
                          </div>
                        </div>
                        <!-- Generic JSON Textarea fallback -->
                        <div v-else>
                          <va-textarea
                            :model-value="typeof localRecord[field.key] === 'object' ? JSON.stringify(localRecord[field.key], null, 2) : localRecord[field.key]"
                            @update:model-value="(val) => { try { localRecord[field.key] = JSON.parse(val) } catch(e) { localRecord[field.key] = val } }"
                            :min-rows="3"
                            style="font-family: monospace; font-size: 0.85rem;"
                            class="w-full"
                            placeholder="{}"
                          />
                        </div>
                      </div>
                    </div>
                  </div>
                </template>
              </div>
            </div>
          </va-collapse>
        </va-accordion>
      </div>

      <!-- Secondary Axes Section -->

      <div v-if="axesList.length > 0" style="margin-bottom: 1rem; margin-top: 1rem;">
        <va-accordion multiple style="width: 100%;">
          <va-collapse
            :header="$t('axis.select_nodes_for_axis')"
            solid
            color="background-element"
            v-model="secondaryAxesOpen"
          >
            <div style="padding: 1rem; display: flex; flex-direction: column; gap: 1rem;">
              <div v-for="axis in axesList" :key="axis.id">
                <label style="font-weight: 700; font-size: 0.85rem; color: var(--va-text-secondary); display: block; margin-bottom: 0.5rem;">
                  {{ axis.name?.ko || axis.name?.en || axis.name || 'Axis' }}
                </label>
                <va-select
                  v-model="localSecondaryNodeSelections[axis.id]"
                  :options="getNodesForAxis(axis.id)"
                  value-by="id"
                  text-by="label"
                  multiple
                  searchable
                  :placeholder="getNodesForAxis(axis.id).length > 0 ? $t('axis.select_nodes_for_axis') : $t('axis.no_nodes_registered')"
                  :disabled="getNodesForAxis(axis.id).length === 0"
                />
              </div>
            </div>
          </va-collapse>
        </va-accordion>
      </div>
    </div>
    <div style="display: flex; justify-content: flex-end; margin-top: 1rem; gap: 0.5rem;">
      <va-button
        color="success"
        :disabled="!hasWorkflow"
        @click="handleSave"
      >
        {{ isEdit ? 'Save' : 'Create & Submit for Approval' }}
      </va-button>
      <va-button preset="secondary" @click="handleClose">Cancel</va-button>
    </div>
  </AppModal>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useCookie } from '#app'
import { useI18n } from 'vue-i18n'
import { useToast } from 'vuestic-ui'
import { useCustomFetch } from '~/composables/useCustomFetch'
import HtmlEditor from '~/components/common/HtmlEditor.vue'
import ImageUploader from '~/components/common/ImageUploader.vue'
import AppModal from '~/components/common/AppModal.vue'
import { parseOptions } from '~/utils/optionParser'

const { t } = useI18n()
const { init: notifyToast } = useToast()
const { customFetch } = useCustomFetch()


const props = defineProps({
  show: { type: Boolean, default: false },
  isEdit: { type: Boolean, default: false },
  record: { type: Object, default: () => ({}) },
  fields: { type: Array, default: () => [] },
  nodeLabel: { type: String, default: '' },
  hasWorkflow: { type: Boolean, default: true },
  selectedDomainInfo: { type: Object, default: null },
  domainReferences: { type: Object, default: () => ({}) },
  workflowPermission: { type: Object, default: () => ({}) },
  availableWorkflows: { type: Array, default: () => [] }
})

const emit = defineEmits(['close', 'save', 'openDomainRef', 'update:show', 'selectWorkflow'])

const localeCookie = useCookie('locale', { default: () => 'ko' })
const locale = computed(() => localeCookie.value || 'ko')

const modalVisible = computed({
  get: () => props.show,
  set: (val) => {
    emit('update:show', val)
    if (!val) emit('close')
  }
})

const isFullscreenModal = ref(false)

watch(() => props.show, (val) => {
  if (!val) {
    isFullscreenModal.value = false
  }
})

const selectedWorkflowId = ref('')

const secondaryAxesOpen = ref([false])
const axesList = ref([])
const allNodes = ref([])
const localSecondaryNodeSelections = ref({})


const availableWorkflowOptions = computed(() => {
  if (!props.availableWorkflows || props.availableWorkflows.length === 0) {
    const perm = props.workflowPermission || {}
    let rName = ''
    if (perm.ruleName) {
      if (typeof perm.ruleName === 'object') {
        rName = perm.ruleName[locale.value] || perm.ruleName.ko || perm.ruleName.en || ''
      } else if (typeof perm.ruleName === 'string') {
        rName = perm.ruleName
      }
    }
    const labelText = props.isEdit 
      ? (rName ? `✏️ ${rName} (UPDATE)` : '✏️ 기본 승인 서식 (등록된 서식 없음)')
      : (rName ? `🆕 ${rName} (CREATE)` : '🆕 기본 승인 서식 (등록된 서식 없음)')
    return [{ value: 'DEFAULT', text: labelText }]
  }

  return props.availableWorkflows.map(wf => {
    let nameText = ''
    if (wf.name) {
      try {
        const parsed = typeof wf.name === 'string' ? JSON.parse(wf.name) : wf.name
        nameText = parsed[locale.value] || parsed.ko || parsed.en || String(wf.name)
      } catch(e) {
        nameText = String(wf.name)
      }
    }
    if (!nameText) nameText = props.isEdit ? '정보 변경 서식' : '신규 등록 서식'
    const icon = props.isEdit ? '✏️' : '🆕'
    const defaultSuffix = wf.isDefault ? ' (기본)' : ''
    return {
      value: wf.id,
      text: `${icon} ${nameText}${defaultSuffix}`
    }
  })
})

watch(
  () => props.availableWorkflows,
  (newVal) => {
    if (newVal && newVal.length > 0) {
      const def = newVal.find(w => w.isDefault) || newVal[0]
      if (def && def.id) {
        selectedWorkflowId.value = def.id
      }
    } else {
      selectedWorkflowId.value = 'DEFAULT'
    }
  },
  { immediate: true, deep: true }
)

const onWorkflowSelected = (wfId) => {
  if (wfId && wfId !== 'DEFAULT') {
    emit('selectWorkflow', wfId)
  }
}

const modalTitle = computed(() => {
  if (props.isEdit) {
    return props.nodeLabel ? `Edit Record - ${props.nodeLabel}` : 'Edit Record'
  }
  return props.nodeLabel ? `Create Record in ${props.nodeLabel}` : 'Create Record'
})

const activeSectorTab = ref('')
const focusedDateFields = ref({})
const localRecord = ref({})

const handleMaskedInput = (field, val) => {
  localRecord.value[field.key] = val
  nextTick(() => {
    localRecord.value[field.key] = formatMaskedInput(val, field.maskingPattern)
  })
}

const openDomainRefPicker = (fieldKey) => {
  emit('openDomainRef', {
    fieldKey,
    isCreate: !props.isEdit,
    currentData: { ...localRecord.value }
  })
}

const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/

const getFieldRules = (field) => {
  const rules = []
  if (field && field.type === 'EMAIL') {
    rules.push((val) => {
      if (!val || String(val).trim() === '') return true
      return emailRegex.test(String(val).trim()) || (t('invalid_email_format') || (locale.value === 'ko' ? '올바른 이메일 형식을 입력해주세요.' : 'Please enter a valid email address.'))
    })
  }
  return rules
}

const populateRecordWithFields = (rawRecord) => {
  const result = rawRecord ? { ...rawRecord } : {}
  if (props.fields && rawRecord) {
    const rawKeys = Object.keys(rawRecord)
    props.fields.forEach(f => {
      if (f.key && result[f.key] === undefined) {
        const matchKey = rawKeys.find(k => k.toUpperCase() === f.key.toUpperCase())
        if (matchKey && result[matchKey] !== undefined) {
          result[f.key] = result[matchKey]
        }
      }
    })
  }
  return result
}

watch(
  () => props.show,
  (isOpen) => {
    if (isOpen) {
      localRecord.value = populateRecordWithFields(props.record)
      if (props.fields) {
        props.fields.forEach(f => {
          if (f.type === 'JSON') {
            const val = localRecord.value[f.key]
            if (typeof val === 'string') {
              try {
                localRecord.value[f.key] = JSON.parse(val)
              } catch (e) {
                localRecord.value[f.key] = []
              }
            } else if (!val) {
              localRecord.value[f.key] = []
            }
          }
        })
      }
    }
  }
)

watch(
  () => props.record,
  (newVal) => {
    if (!newVal) {
      if (!props.show) localRecord.value = {}
      return
    }
    const populated = populateRecordWithFields(newVal)
    if (props.show && Object.keys(localRecord.value).length > 0) {
      localRecord.value = { ...localRecord.value, ...populated }
    } else {
      localRecord.value = { ...populated }
    }
    // Parse JSON fields if they come as string or null
    if (props.fields) {
      props.fields.forEach(f => {
        if (f.type === 'JSON') {
          const val = localRecord.value[f.key]
          if (typeof val === 'string') {
            try {
              localRecord.value[f.key] = JSON.parse(val)
            } catch (e) {
              localRecord.value[f.key] = []
            }
          } else if (!val) {
            localRecord.value[f.key] = []
          }
        }
      })
    }
    // Reset secondary node selections when record changes
    localSecondaryNodeSelections.value = {}
    if (newVal && newVal._secondaryNodeIds) {
       // if we pass down secondary node IDs from parent
       const ids = newVal._secondaryNodeIds
       allNodes.value.forEach(node => {
          if (node.axisId && ids.includes(node.id)) {
             if (!localSecondaryNodeSelections.value[node.axisId]) {
               localSecondaryNodeSelections.value[node.axisId] = []
             }
             localSecondaryNodeSelections.value[node.axisId].push(node.id)
          }
       })
    }
  },
  { immediate: true, deep: true }
)

const getTableColumns = (field) => {
  if (!field || !field.options) return []
  try {
    const opts = typeof field.options === 'string' ? JSON.parse(field.options) : field.options
    if (opts && opts.tableSchema && Array.isArray(opts.tableSchema.columns)) {
      return opts.tableSchema.columns
    }
  } catch (e) {}
  return []
}

const getTranslatedColName = (name) => {
  if (!name) return ''
  if (typeof name === 'object') {
    return name[locale.value] || name.ko || name.en || ''
  }
  return String(name)
}

const getColSelectOptions = (options) => {
  if (!options || !Array.isArray(options)) return []
  return options.map(opt => {
    if (typeof opt === 'object' && opt !== null) {
      const key = opt.key || opt.value || ''
      let label = ''
      if (opt.label) {
        if (typeof opt.label === 'object') {
          label = opt.label[locale.value] || opt.label.ko || opt.label.en || key
        } else {
          label = String(opt.label)
        }
      } else {
        label = opt.text || key
      }
      return { text: label, value: key }
    }
    if (typeof opt === 'string' && opt.includes(':')) {
      const parts = opt.split(':').map(s => s.trim())
      const key = parts[0]
      const ko = parts[1] || key
      const en = parts[2] || ko
      const label = locale.value === 'en' ? en : ko
      return { text: label, value: key }
    }
    return { text: opt, value: opt }
  })
}

const addTableRow = (fieldKey, columns) => {
  if (!Array.isArray(localRecord.value[fieldKey])) {
    localRecord.value[fieldKey] = []
  }
  const newRow = {}
  columns.forEach(col => {
    newRow[col.key] = col.type === 'BOOLEAN' ? false : (col.type === 'NUMBER' ? null : '')
  })
  localRecord.value[fieldKey].push(newRow)
}

const deleteTableRow = (fieldKey, idx) => {
  if (Array.isArray(localRecord.value[fieldKey])) {
    localRecord.value[fieldKey].splice(idx, 1)
  }
}

const clearTableRows = (fieldKey) => {
  localRecord.value[fieldKey] = []
}

const handleClose = () => {
  emit('close')
}

const handleSave = () => {
  // Validate EMAIL fields
  if (props.fields) {
    for (const f of props.fields) {
      if (f.type === 'EMAIL') {
        const val = localRecord.value[f.key]
        if (val && typeof val === 'string' && val.trim() !== '' && !val.includes('*') && !val.startsWith('vault:') && !val.startsWith('ENC(') && !emailRegex.test(val.trim())) {
          notifyToast({
            message: `${getTranslatedName(f.name)}: ${t('invalid_email_format') || (locale.value === 'ko' ? '올바른 이메일 형식을 입력해주세요.' : 'Please enter a valid email address.')}`,
            color: 'danger'
          })
          return
        }
      }
    }
  }

  // Flatten secondary nodes
  const secNodes = []
  Object.values(localSecondaryNodeSelections.value).forEach(arr => {
    if (Array.isArray(arr)) {
      secNodes.push(...arr)
    }
  })

  emit('save', {
    isEdit: props.isEdit,
    record: localRecord.value,
    secondaryNodes: secNodes,
    workflowConfigId: selectedWorkflowId.value && selectedWorkflowId.value !== 'DEFAULT' ? selectedWorkflowId.value : null
  })
}

const isAutoNumberingField = (field) => {
  if (!props.selectedDomainInfo || !field) return false
  return (
    field.id === props.selectedDomainInfo.identifierFieldId &&
    props.selectedDomainInfo.numberingPattern &&
    props.selectedDomainInfo.numberingPattern.trim() !== ''
  )
}

const parseName = (nameObj) => {
  if (!nameObj) return null
  if (typeof nameObj === 'string') {
    try { return JSON.parse(nameObj) } catch (e) { return null }
  }
  return nameObj
}

const getTranslatedName = (nameObj) => {
  const pName = parseName(nameObj)
  return pName?.[locale.value] || pName?.ko || pName?.en || ''
}

const hasHint = (hintObj) => {
  const parsed = parseName(hintObj)
  return !!(parsed && (parsed.ko || parsed.en))
}

const groupedFieldsArray = computed(() => {
  const map = new Map()
  const sortedFields = [...(props.fields || [])].sort((a, b) => (a.order || 0) - (b.order || 0))

  sortedFields.forEach((f) => {
    const sObj = f.fieldGroup?.sector || f.sector || null
    const gObj = f.fieldGroup || f.group || null

    const sName = getTranslatedName(sObj?.name) || (typeof sObj === 'string' ? sObj : '') || t('common.general') || (locale.value === 'ko' ? '일반' : 'General')
    const sKey = sObj?.id || sObj?.key || (sObj?.name ? (typeof sObj.name === 'string' ? sObj.name : sObj.name.ko || sObj.name.en) : 'default')
    const sOrder = sObj?.sortOrder ?? sObj?.order ?? 0

    const gName = getTranslatedName(gObj?.name) || (typeof gObj === 'string' ? gObj : '') || t('common.fields') || (locale.value === 'ko' ? '기본 필드' : 'Fields')
    const gKey = gObj?.id || gObj?.key || (gObj?.name ? (typeof gObj.name === 'string' ? gObj.name : gObj.name.ko || gObj.name.en) : 'default')
    const gOrder = gObj?.sortOrder ?? gObj?.order ?? 0

    if (!map.has(sKey)) {
      map.set(sKey, { key: sKey, label: sName, order: sOrder, groups: new Map() })
    }
    const sectorObj = map.get(sKey)

    if (!sectorObj.groups.has(gKey)) {
      sectorObj.groups.set(gKey, { key: gKey, label: gName, order: gOrder, fields: [], isOpen: gObj?.isDefaultOpen ?? true })
    }
    sectorObj.groups.get(gKey).fields.push(f)
  })

  const sectors = Array.from(map.values())
  sectors.sort((a, b) => a.order - b.order)

  const filteredSectors = []
  sectors.forEach((s) => {
    const groups = Array.from(s.groups.values())
    groups.sort((a, b) => a.order - b.order)

    const visibleGroups = groups.map(g => {
      const visibleFields = g.fields.filter(f => evalConditionRule(f, localRecord.value).show)
      return { ...g, fields: visibleFields }
    }).filter(g => g.fields.length > 0)

    if (visibleGroups.length > 0) {
      filteredSectors.push({ key: s.key, label: s.label, groups: visibleGroups })
    }
  })

  return filteredSectors
})

watch(
  groupedFieldsArray,
  (arr) => {
    if (arr && arr.length > 0) {
      if (!arr.some((s) => s.key === activeSectorTab.value)) {
        activeSectorTab.value = arr[0].key
      }
    }
  },
  { immediate: true }
)



const domainRefResolvedCache = ref({})
const resolvingRefMap = {}

const resolveDomainRefAsync = async (fieldKey, recordId) => {
  if (resolvingRefMap[recordId]) return
  resolvingRefMap[recordId] = true

  try {
    const recRes = await customFetch(`/api/records/${recordId}`)
    if (recRes && recRes.data) {
      const data = typeof recRes.data === 'string' ? JSON.parse(recRes.data) : recRes.data
      const refInfo = props.domainReferences?.[fieldKey]
      const idFieldId = refInfo?.domainInfo?.identifierFieldId
      const dFieldId = refInfo?.domainInfo?.displayNameFieldId || idFieldId
      const idF = refInfo?.fields?.find((x) => x.id === idFieldId)
      const nameF = refInfo?.fields?.find((x) => x.id === dFieldId)

      const extractVal = (d, key) => {
        if (!d || !key) return null
        const v = d[key]
        if (v && typeof v === 'object') return v[locale.value] || v.ko || v.en || JSON.stringify(v)
        return v ? String(v) : null
      }

      let idStr = extractVal(data, idF?.key)
      let nameStr = extractVal(data, nameF?.key)

      if (!idStr) idStr = data.EP_NO || data.id || data.code
      if (!nameStr) nameStr = data.EP_NAME || data.name || data.title

      let res = ''
      if (idStr && nameStr && idStr !== nameStr) res = `[${idStr}] ${nameStr}`
      else if (nameStr) res = nameStr
      else if (idStr) res = `[${idStr}]`
      else res = recordId

      domainRefResolvedCache.value[recordId] = res
    }
  } catch (e) {
    console.error('Failed to resolve domain reference display name:', e)
  }
}

const getDomainRefDisplayName = (fieldKey, recordId) => {
  if (!recordId) return ''
  if (domainRefResolvedCache.value[recordId]) {
    return domainRefResolvedCache.value[recordId]
  }

  const refInfo = props.domainReferences?.[fieldKey]
  if (!refInfo) return recordId

  const recList = Array.isArray(refInfo.records) ? refInfo.records : (refInfo.records?.content || [])
  const record = recList.find((r) => r.id === recordId)
  if (record) {
    const data = typeof record.data === 'string' ? JSON.parse(record.data) : record.data
    const idFieldId = refInfo.domainInfo?.identifierFieldId
    let dFieldId = refInfo.domainInfo?.displayNameFieldId || idFieldId
    const idF = refInfo.fields?.find((x) => x.id === idFieldId)
    const nameF = refInfo.fields?.find((x) => x.id === dFieldId)

    const extractVal = (d, key) => {
      if (!d || !key) return null
      const v = d[key]
      if (v && typeof v === 'object') return v[locale.value] || v.ko || v.en || JSON.stringify(v)
      return v ? String(v) : null
    }

    const idStr = extractVal(data, idF?.key)
    const nameStr = extractVal(data, nameF?.key)

    let res = ''
    if (idStr && nameStr && idStr !== nameStr) res = `[${idStr}] ${nameStr}`
    else if (nameStr) res = nameStr
    else if (idStr) res = `[${idStr}]`
    else res = recordId

    domainRefResolvedCache.value[recordId] = res
    return res
  }

  if (typeof recordId === 'string' && (recordId.length >= 32 || recordId.includes('-'))) {
    resolveDomainRefAsync(fieldKey, recordId)
  }

  return recordId
}

function evaluateConditionExpression(expr, formData) {
  if (!expr || !expr.trim() || !formData) return false
  try {
    const replaced = expr.replace(/#{([a-zA-Z0-9_]+)}/g, (_, key) => {
      const val = formData[key]
      if (val === undefined || val === null) return 'null'
      if (typeof val === 'number' || typeof val === 'boolean') return String(val)
      if (typeof val === 'object') return JSON.stringify(JSON.stringify(val))
      return JSON.stringify(String(val))
    })
    const fn = new Function(`return Boolean(${replaced});`)
    return fn()
  } catch (e) {
    return false
  }
}

// Fetch Axes and Nodes (Per-axis independent trees)
const fetchAxesAndNodes = async () => {
  if (!props.selectedDomainInfo?.id) return
  try {
    const domainId = props.selectedDomainInfo.id
    const axesRes = await customFetch(`/api/domains/${domainId}/axes`)
    axesList.value = axesRes || []
    
    const flat = []
    const flattenTree = (nodes, depth = 0) => {
      if (!nodes) return
      nodes.forEach(n => {
        const indent = depth > 0 ? '└ '.repeat(depth) : ''
        const nameStr = typeof n.name === 'object' && n.name !== null ? (n.name.ko || n.name.en || Object.values(n.name)[0] || 'Unnamed') : (n.name || 'Unnamed')
        flat.push({
          id: n.id,
          axisId: n.axisId,
          depth,
          rawName: nameStr,
          label: `${indent}${nameStr}`
        })
        if (n.children && n.children.length > 0) {
          flattenTree(n.children, depth + 1)
        }
      })
    }

    // Fetch tree for primary domain nodes + each secondary axis
    const treePromises = [
      customFetch(`/api/domains/${domainId}/nodes/tree`),
      ...axesList.value.map(a => customFetch(`/api/domains/${domainId}/nodes/tree?axisId=${a.id}`))
    ]
    const treeResults = await Promise.all(treePromises)
    treeResults.forEach(tree => flattenTree(tree))
    allNodes.value = flat

    // Initialize selections if editing
    if (localRecord.value && localRecord.value._secondaryNodeIds) {
      const ids = localRecord.value._secondaryNodeIds
      const selections = {}
      allNodes.value.forEach(node => {
        if (node.axisId && ids.includes(node.id)) {
          if (!selections[node.axisId]) selections[node.axisId] = []
          selections[node.axisId].push(node.id)
        }
      })
      localSecondaryNodeSelections.value = selections
    }
  } catch (e) {
    console.error('Failed to fetch axes/nodes for secondary mapping', e)
  }
}

const getNodesForAxis = (axisId) => {
  return allNodes.value
    .filter(n => n.axisId === axisId)
    .map(n => ({
      id: n.id,
      label: n.label
    }))
}

watch(() => props.show, (val) => {
  if (val) {
    fetchAxesAndNodes()
  } else {
    axesList.value = []
    allNodes.value = []
    localSecondaryNodeSelections.value = {}
  }
})

function evalConditionRule(field, formData) {
  const defaultRes = {
    show: true,
    highlight: field?.isHighlighted || false,
    required: field?.required || false,
    readOnly: field?.isReadOnly || false,
    disabled: false
  }

  // Apply workflow permission rules if available
  const perm = props.workflowPermission || {}
  const hiddenFields = perm.hiddenFields || []
  const readOnlyFields = perm.readOnlyFields || []
  const editableFields = perm.editableFields || []

  const isFieldInList = (f, list) => {
    if (!f || !list || !Array.isArray(list) || list.length === 0) return false
    const fKeyUpper = String(f.key || '').toUpperCase()
    const fId = String(f.id || '')
    return list.some(item => {
      const itemStr = String(item).trim()
      return itemStr.toUpperCase() === fKeyUpper || (fId && itemStr === fId)
    })
  }

  if (isFieldInList(field, hiddenFields)) {
    return { show: false, highlight: false, required: false, readOnly: true, disabled: true }
  }

  if (isFieldInList(field, readOnlyFields)) {
    defaultRes.readOnly = true
  }

  if (editableFields.length > 0 && !isFieldInList(field, editableFields)) {
    if (!props.isEdit) {
      // In CREATE mode, fields not in editableFields are hidden completely
      return { show: false, highlight: false, required: false, readOnly: true, disabled: true }
    } else {
      defaultRes.readOnly = true
    }
  }

  if (!field || !field.options || !formData) return defaultRes

  try {
    const opts = typeof field.options === 'string' ? JSON.parse(field.options) : field.options
    const rule = opts.conditionRule
    if (!rule || rule.enabled === false) return defaultRes

    let actions = ['SHOW']
    if (rule.action) {
      actions = Array.isArray(rule.action) ? rule.action : [rule.action]
    }
    let isMatch = false

    if (rule.expression && String(rule.expression).trim() !== '') {
      isMatch = evaluateConditionExpression(rule.expression, formData)
    } else if (rule.dependsOnFieldKey) {
      const targetVal = String(formData[rule.dependsOnFieldKey] ?? '').trim()
      const condVal = String(rule.value ?? '').trim()
      const op = rule.operator || 'EQUALS'

      if (op === 'EQUALS' || op === '==') isMatch = targetVal.toLowerCase() === condVal.toLowerCase()
      else if (op === 'NOT_EQUALS' || op === '!=') isMatch = targetVal.toLowerCase() !== condVal.toLowerCase()
      else if (op === 'CONTAINS') isMatch = targetVal.toLowerCase().includes(condVal.toLowerCase())
      else if (op === 'NOT_EMPTY') isMatch = targetVal.length > 0
      else if (op === 'EMPTY') isMatch = targetVal.length === 0
      else if (op === 'GREATER_THAN' || op === '>') isMatch = Number(targetVal) > Number(condVal)
      else if (op === 'GREATER_THAN_OR_EQUAL' || op === '>=') isMatch = Number(targetVal) >= Number(condVal)
      else if (op === 'LESS_THAN' || op === '<') isMatch = Number(targetVal) < Number(condVal)
      else if (op === 'LESS_THAN_OR_EQUAL' || op === '<=') isMatch = Number(targetVal) <= Number(condVal)
    } else {
      return defaultRes
    }

    let show = true
    if (actions.includes('SHOW')) show = isMatch

    let highlight = field?.isHighlighted || false
    if (actions.includes('HIGHLIGHT')) highlight = isMatch ? true : highlight

    let required = field?.required || false
    if (actions.includes('REQUIRE')) required = isMatch ? true : required

    let readOnly = field?.isReadOnly || false
    if (actions.includes('READ_ONLY')) readOnly = isMatch ? true : readOnly

    let disabled = false
    if (actions.includes('DISABLE') || actions.includes('EDIT_FORBIDDEN')) disabled = isMatch ? true : disabled

    return { show, highlight, required, readOnly, disabled }
  } catch (e) {}

  return defaultRes
}

const extractFilename = (input) => {
  if (!input) return ''
  if (typeof input === 'object') {
    if (input.name && input.name !== 'Download') return input.name
    if (input.originalName) return input.originalName
    if (input.url) input = input.url
    else return ''
  }
  let str = String(input).trim()
  if (!str || str === '-' || str === '[]' || str === '{}' || str === 'null' || str === 'undefined') return ''
  
  try {
    if (str.startsWith('{') || str.startsWith('[')) {
      const parsed = JSON.parse(str)
      if (Array.isArray(parsed) && parsed.length > 0) return extractFilename(parsed[0])
      if (typeof parsed === 'object' && (parsed.name || parsed.originalName)) return parsed.name || parsed.originalName
    }
  } catch (e) {}

  try {
    if (str.includes('?name=')) return decodeURIComponent(str.split('?name=')[1].split('&')[0])
    if (str.includes('?filename=')) return decodeURIComponent(str.split('?filename=')[1].split('&')[0])
    const fname = decodeURIComponent(str.split('/').pop().split('?')[0])
    if (fname && fname !== '-' && fname !== 'null') return fname
  } catch (e) {}
  
  return str
}

let draggedItemIndex = null
let currentArrayRef = null

const onDragStart = (event, index, arr) => {
  draggedItemIndex = index
  currentArrayRef = arr
  if (event.dataTransfer) event.dataTransfer.effectAllowed = 'move'
  setTimeout(() => {
    if (event.target && event.target.style) event.target.style.opacity = '0.5'
  }, 0)
}

const onDragEnter = (event, index, arr) => {
  if (draggedItemIndex === null || currentArrayRef !== arr) return
  if (draggedItemIndex === index) return
  const temp = arr[draggedItemIndex]
  arr.splice(draggedItemIndex, 1)
  arr.splice(index, 0, temp)
  draggedItemIndex = index
}

const onDrop = () => {}

const onDragEnd = (event) => {
  if (event.target && event.target.style) event.target.style.opacity = '1'
  draggedItemIndex = null
  currentArrayRef = null
}

const getFilesList = (v) => {
  if (!v) return []
  if (Array.isArray(v)) {
    return v.filter(Boolean)
  }
  if (v instanceof File) {
    return [v]
  }
  if (typeof v === 'string') {
    const trimmed = v.trim()
    if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
      try {
        const parsed = JSON.parse(trimmed)
        return Array.isArray(parsed) ? parsed.filter(Boolean) : []
      } catch (e) {
        return [v]
      }
    }
    return [v]
  }
  if (typeof v === 'object' && v !== null) {
    return [v]
  }
  return []
}

const handleFilesAdded = (field, newFiles) => {
  if (!newFiles) return
  const currentList = getFilesList(localRecord.value[field.key])
  const incoming = Array.isArray(newFiles) ? newFiles : [newFiles]
  const validIncoming = incoming.filter(Boolean)
  
  if (field.isMultiValue) {
    localRecord.value[field.key] = [...currentList, ...validIncoming]
  } else {
    localRecord.value[field.key] = validIncoming.length > 0 ? [validIncoming[validIncoming.length - 1]] : []
  }
}

const removeFile = (fieldKey, index) => {
  const currentList = getFilesList(localRecord.value[fieldKey])
  currentList.splice(index, 1)
  localRecord.value[fieldKey] = [...currentList]
}
</script>

<style scoped>
.mb-4 { margin-bottom: 1rem; }
.w-full { width: 100%; }

.file-upload-wrapper :deep(.va-file-upload) {
  width: 100% !important;
  max-width: 100% !important;
}
.file-upload-wrapper :deep(.va-file-upload-dropzone) {
  box-sizing: border-box !important;
  padding: 0.5rem 1rem !important;
  min-height: 60px !important;
}
.file-upload-wrapper :deep(.va-file-upload-dropzone__content) {
  width: 100% !important;
  box-sizing: border-box !important;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 1rem;
}
.file-upload-wrapper :deep(.va-file-upload-list) {
  display: none !important;
}
.custom-file-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-top: 0.5rem;
}
.custom-file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 1rem;
  background-color: var(--va-background-element);
  border-radius: 0.5rem;
  font-size: 0.9rem;
}
.custom-file-info {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.custom-file-actions {
  display: flex;
  gap: 0.25rem;
  align-items: center;
}
.flip-list-move {
  transition: transform 0.3s cubic-bezier(0.25, 0.8, 0.5, 1);
}

.slim-multilingual-input :deep(.va-input-wrapper__container) {
  align-items: center;
  margin: 0;
}
.slim-multilingual-input :deep(.va-input-wrapper__field) {
  align-items: center;
}

.custom-modal-header-wrapper {
  margin: 0 !important;
  width: 100%;
}

.custom-record-modal :deep(.va-modal__header) {
  padding: 0.75rem 1.25rem 0.5rem 1.25rem !important;
  margin: 0 !important;
  display: flex !important;
  align-items: center !important;
  min-height: 38px !important;
}

.custom-record-modal :deep(.va-modal__close) {
  top: 0.75rem !important;
  right: 1.25rem !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.custom-record-modal :deep(.va-modal__message) {
  padding-top: 0.25rem !important;
}
</style>





