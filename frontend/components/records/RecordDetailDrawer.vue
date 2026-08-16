<template>
  <va-modal
    v-model="modalVisible"
    hide-default-actions
    size="large"
    :prevent-click-outside="true"
    :no-outside-dismiss="true"
    class="custom-record-modal"
  >
    <template #header>
      <div class="custom-modal-header-wrapper" style="display: flex; align-items: center; justify-content: space-between; width: 100%; min-height: 32px; gap: 0.75rem; flex-wrap: wrap;">
        <!-- Left: Icon & Main Title & Name -->
        <div style="display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap;">
          <h3 style="margin: 0; padding: 0; font-size: 1.05rem; font-weight: 800; color: var(--va-text-primary); text-transform: uppercase; display: inline-flex; align-items: center; gap: 0.4rem; line-height: 1.2;">
            <va-icon :name="isSnapshotMode ? 'history' : 'badge'" color="primary" size="22px" />
            <span>{{ isSnapshotMode ? 'RECORD SNAPSHOT' : 'RECORD DETAILS' }}</span>
          </h3>
          
          <span v-if="headerRecordTitle" style="font-size: 1.15rem; font-weight: 800; color: var(--va-primary); border-left: 2px solid var(--va-background-border); padding-left: 0.6rem; margin-left: 0.2rem;">
            {{ headerRecordTitle }}
          </span>

        </div>

        <!-- Right: Chips & Badges -->
        <div style="display: flex; align-items: center; gap: 0.4rem; flex-wrap: wrap; margin-right: 2.2rem;">

          <!-- ID / 사번 칩 -->
          <va-chip
            v-if="headerRecordId"
            size="small"
            color="primary"
            square
            outline
            style="font-weight: 700; font-family: monospace; margin-top: 4px;"
          >
            {{ headerRecordId }}
          </va-chip>


          <!-- 분류 노드 칩 (괄호 정제됨) -->
          <va-chip
            v-if="cleanedNodeLabel"
            size="small"
            color="info"
            square
            style="font-weight: 600; margin-top: 4px;"
          >
            {{ cleanedNodeLabel }}
          </va-chip>


          <!-- 상태 뱃지 -->
          <va-chip
            v-if="recordStatus"
            :color="recordStatus === 'ACTIVE' ? 'success' : (recordStatus === 'PENDING_APPROVAL' ? 'warning' : 'danger')"
            size="small"
            square
            style="font-weight: 800; margin-top: 4px;"
          >
            {{ recordStatus === 'PENDING_APPROVAL' ? (t('pending_approval')) : (recordStatus === 'ACTIVE' ? (t('active_status')) : (t('deleted_status'))) }}
          </va-chip>
        </div>
      </div>
    </template>

    <div style="height: 60vh; min-height: 520px; max-height: 65vh; overflow: hidden; padding: 1rem; box-sizing: border-box; width: 100%; display: flex; flex-direction: column;">






      <!-- Status Banners -->
      <div v-if="isSnapshotMode" style="margin-bottom: 1rem; padding: 0.5rem; background-color: #fff3cd; color: #856404; border: 1px solid #ffeeba; border-radius: 4px; text-align: center; font-weight: bold;">
        {{ t('snapshot_viewing_notice') }}
      </div>
      <div v-if="hasPendingUpdate" style="margin-bottom: 1rem; padding: 0.5rem; background-color: #fff3cd; color: #856404; border: 1px solid #ffeeba; border-radius: 4px; text-align: center; font-weight: bold;">
        {{ t('pending_approval_notice') }}
      </div>
      <div v-if="isEditing && !hasUpdateWorkflow" style="margin-bottom: 1rem; padding: 0.5rem; background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; border-radius: 4px; text-align: center; font-weight: bold;">
        This classification node does not have an UPDATE workflow configured. You cannot save records.
      </div>

      <!-- Main Drawer Tabs: Modern Segmented Pill Control -->
      <div class="main-segmented-tabs" style="display: flex; gap: 4px; padding: 4px; background: rgba(0, 0, 0, 0.04); border-radius: 8px; margin-bottom: 1rem; border: 1px solid rgba(0,0,0,0.08);">
        <button
          type="button"
          :class="['segmented-tab-btn', { active: activeMainTab === 'details' }]"
          @click="activeMainTab = 'details'"
          style="flex: 1; border: none; padding: 8px; cursor: pointer; border-radius: 6px; font-weight: 600; display: flex; align-items: center; justify-content: center; background: transparent; transition: all 0.2s;"
        >
          <span style="margin-right: 6px;">📄</span>
          <span>{{ t('details_info') || (i18nLocale === 'en' ? 'Details' : '상세 정보') }}</span>
        </button>
        <button
          v-if="!isSnapshotMode"
          type="button"
          :class="['segmented-tab-btn', { active: activeMainTab === 'secondary' }]"
          @click="activeMainTab = 'secondary'"
          style="flex: 1; border: none; padding: 8px; cursor: pointer; border-radius: 6px; font-weight: 600; display: flex; align-items: center; justify-content: center; background: transparent; transition: all 0.2s;"
        >
          <span style="margin-right: 6px;">🌿</span>
          <span>{{ t('secondary_nodes_tab') || (i18nLocale === 'en' ? 'Secondary Nodes' : '다축/보조 노드') }}</span>
          <span class="tab-badge" style="margin-left: 6px; background: rgba(0,0,0,0.1); padding: 2px 6px; border-radius: 10px; font-size: 0.75rem;">{{ secondaryNodes?.length || 0 }}</span>
        </button>
        <button
          v-if="!isSnapshotMode"
          type="button"
          :class="['segmented-tab-btn', { active: activeMainTab === 'history' }]"
          @click="activeMainTab = 'history'"
          style="flex: 1; border: none; padding: 8px; cursor: pointer; border-radius: 6px; font-weight: 600; display: flex; align-items: center; justify-content: center; background: transparent; transition: all 0.2s;"
        >
          <span style="margin-right: 6px;">📜</span>
          <span>{{ t('change_history_tab') || (i18nLocale === 'en' ? 'Change History' : '변경 이력') }}</span>
          <span class="tab-badge" style="margin-left: 6px; background: rgba(0,0,0,0.1); padding: 2px 6px; border-radius: 10px; font-size: 0.75rem;">{{ history?.length || 0 }}</span>
        </button>
      </div>

      <!-- Details Tab Content -->
      <div v-show="activeMainTab === 'details'" style="overflow-y: auto; flex: 1; padding-right: 4px;">

        <!-- Sector Sub-Tabs: Clean Underline / Small Chip style -->
        <va-tabs v-model="activeSectorTab" style="margin-bottom: 1rem;" grow>
          <template #tabs>
            <va-tab v-for="(sector, idx) in groupedFieldsArray" :key="sector.key" :name="idx">
              {{ sector.label }}
            </va-tab>
          </template>
        </va-tabs>

        <!-- Sector Content -->
        <div v-for="(sector, idx) in groupedFieldsArray" :key="sector.key" v-show="activeSectorTab === idx">
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
                        <span :style="{ fontSize: '0.75rem', color: evalConditionRule(field, localRecord).highlight ? 'var(--va-primary)' : 'var(--va-text-secondary)', fontWeight: evalConditionRule(field, localRecord).highlight ? '800' : '600', textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: '4px', minHeight: '18px', lineHeight: '18px' }">
                          <va-icon v-if="evalConditionRule(field, localRecord).highlight" name="star" size="small" color="primary" />
                          {{ getTranslatedName(field.name) }}{{ evalConditionRule(field, localRecord).required ? ' *' : '' }}{{ field.type === 'CALCULATED' ? ' (계산됨)' : '' }}
                          <va-popover v-if="hasHint(field.hint)" :message="getTranslatedName(field.hint)" trigger="hover" placement="top">
                            <va-icon name="info" size="small" color="info" style="cursor: help; margin-left: 2px;" />
                          </va-popover>
                          <!-- Decrypt Button for Details -->
                          <span v-if="(field.isEncrypted || field.encryptionType) && (localRecord[field.key] !== undefined && localRecord[field.key] !== null && localRecord[field.key] !== '')" style="margin-left:auto; display:inline-flex; align-items:center; gap:4px; font-size:0.75rem; color:#888;">
                            <va-icon name="lock" size="small" />
                            <template v-if="!decryptedValues[field.key]">
                              <span style="cursor:pointer; text-decoration:underline; color:var(--va-primary);" @click.stop="requestDecryptRecordField(field.key)">
                                {{ t('view_original') }}
                              </span>
                            </template>
                            <template v-else>
                              <span style="cursor:pointer; text-decoration:underline; color:var(--va-primary);" @click.stop="hideDecryptedField(field.key)">
                                {{ t('hide_original') }}
                              </span>
                              <span v-if="decryptRemainingTime[field.key]" style="margin-left:4px; font-variant-numeric: tabular-nums;">
                                (00:{{ String(decryptRemainingTime[field.key]).padStart(2, '0') }})
                              </span>
                            </template>
                            <va-icon v-if="decryptingFields[field.key]" name="sync" size="small" spin />
                          </span>
                        </span>

                        <va-input
                          v-if="['TEXT', 'NUMBER', 'DECIMAL', 'FLOAT', 'INTEGER', 'DATE'].includes(field.type)"
                          :model-value="decryptedValues[field.key] || localRecord[field.key]"
                          @update:model-value="(val) => handleMaskedInput(field, val)"
                          :type="field.type === 'DATE' ? (focusedDateFields['edit_' + field.key] || localRecord[field.key] ? 'date' : 'text') : (['NUMBER', 'DECIMAL', 'FLOAT', 'INTEGER'].includes(field.type) ? 'number' : 'text')"
                          class="w-full"
                          :readonly="!isEditing || evalConditionRule(field, localRecord).readOnly"
                          :disabled="isEditing && (isAutoNumberingField(field) || evalConditionRule(field, localRecord).disabled)"
                          :lang="locale === 'en' ? 'en-US' : 'ko-KR'"
                          :placeholder="isEditing && isAutoNumberingField(field) ? (locale === 'en' ? 'Auto-generated on final approval' : '자동 채번됩니다 (최종 승인 시)') : (field.type === 'DATE' ? (locale === 'en' ? 'YYYY-MM-DD' : '연도-월-일') : '')"
                          @focus="focusedDateFields['edit_' + field.key] = true"
                          @blur="focusedDateFields['edit_' + field.key] = false"
                        />

                        <div v-else-if="field.type === 'DOMAIN_REFERENCE'" class="w-full" style="display: flex; gap: 0.5rem; align-items: center;">
                          <va-input
                            :model-value="getDomainRefDisplayName(field.key, localRecord[field.key])"
                            readonly
                            style="flex: 1;"
                          />
                          <va-button v-if="isEditing" icon="search" @click="openDomainRefPicker(field.key)" />
                        </div>

                        <!-- Multilingual Edit -->
                        <div v-else-if="field.type === 'MULTILINGUAL'" class="w-full" style="display: flex; gap: 0.5rem; flex-direction: row; min-width: 0;">
                          <va-input v-model="localRecord[field.key].ko" style="flex: 1; min-width: 0;" :readonly="!isEditing" class="slim-multilingual-input">
                            <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">{{ locale === 'en' ? 'Korean' : '한국어' }}</span></template>
                          </va-input>
                          <va-input v-model="localRecord[field.key].en" style="flex: 1; min-width: 0;" :readonly="!isEditing" class="slim-multilingual-input">
                            <template #prependInner><span style="font-size: 0.75rem; color: #888; font-weight: 600; margin-right: 0.5rem; border-right: 1px solid #ddd; padding-right: 0.5rem; white-space: nowrap;">{{ locale === 'en' ? 'English' : '영어' }}</span></template>
                          </va-input>
                        </div>

                        <va-input
                          v-else-if="field.type === 'CALCULATED'"
                          v-model="localRecord[field.key]"
                          readonly
                          class="w-full"
                          style="background-color: #f4f6f8;"
                        />

                        <va-select
                          v-else-if="['SELECT', 'MULTI_SELECT'].includes(field.type)"
                          v-model="localRecord[field.key]"
                          :options="parseOptions(field.options)"
                          :multiple="field.type === 'MULTI_SELECT' || field.isMultiValue"
                          value-by="value"
                          class="w-full"
                          :readonly="!isEditing"
                        />

                        <va-checkbox
                          v-else-if="field.type === 'BOOLEAN'"
                          v-model="localRecord[field.key]"
                          class="w-full"
                          :readonly="!isEditing"
                        />

                        <div v-else-if="field.type === 'FILE'" class="w-full">
                          <div v-if="!isEditing" style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
                            <template v-if="localRecord[field.key] && localRecord[field.key].length > 0">
                              <va-chip
                                v-for="(fileObj, i) in localRecord[field.key]"
                                :key="i"
                                :href="fileObj.url || fileObj"
                                target="_blank"
                                outline
                                icon="download"
                                color="primary"
                                style="cursor: pointer;"
                              >
                                {{ fileObj.name || extractFilename(fileObj.url || fileObj) }}
                              </va-chip>
                            </template>
                            <span v-else>-</span>
                          </div>
                          <va-file-upload v-else v-model="localRecord[field.key]" :type="field.isMultiValue ? 'list' : 'single'" dropzone class="w-full file-upload-wrapper">
                            <div style="display: flex; flex-direction: row; align-items: center; gap: 1rem; padding: 0.5rem; justify-content: center; width: 100%;">
                              <span style="font-size: 0.9rem; color: #666;">{{ $t('file_upload_dropzone') }}</span>
                              <va-button size="small">{{ $t('file_upload_button') }}</va-button>
                            </div>
                          </va-file-upload>
                          <transition-group name="flip-list" tag="div" v-if="isEditing && localRecord[field.key] && localRecord[field.key].length > 0" class="custom-file-list" @dragover.prevent>
                            <div
                              v-for="(fileObj, i) in localRecord[field.key]"
                              :key="fileObj.url || fileObj.name"
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
                                <va-icon name="delete" style="cursor: pointer; color: #E53935;" @click="removeFile(localRecord[field.key], i)" />
                              </div>
                            </div>
                          </transition-group>
                        </div>
                        <div v-else-if="field.type === 'DATE_RANGE'" class="w-full" style="display: flex; gap: 0.5rem; flex-direction: row; align-items: center; min-width: 0;">
                          <template v-if="!isEditing">
                            <va-input
                              :model-value="(decryptedValues[field.key] || localRecord[field.key] || '').replace('~', ' ~ ')"
                              readonly
                              class="w-full"
                            />
                          </template>
                          <template v-else>
                            <va-input
                              :model-value="(localRecord[field.key] || '').split('~')[0] || ''"
                              @update:model-value="(val) => { const arr = (localRecord[field.key] || '').split('~'); arr[0] = val; localRecord[field.key] = arr.join('~'); if (arr.length === 1) localRecord[field.key] += '~'; }"
                              :type="focusedDateFields['edit_' + field.key + '_start'] || (localRecord[field.key] || '').split('~')[0] ? 'date' : 'text'"
                              :readonly="evalConditionRule(field, localRecord).readOnly"
                              :disabled="evalConditionRule(field, localRecord).disabled"
                              :lang="locale === 'en' ? 'en-US' : 'ko-KR'"
                              :placeholder="locale === 'en' ? 'Start Date' : '시작일'"
                              style="flex: 1; min-width: 0;"
                              @focus="focusedDateFields['edit_' + field.key + '_start'] = true"
                              @blur="focusedDateFields['edit_' + field.key + '_start'] = false"
                            />
                            <span style="font-weight: bold; color: var(--va-text-secondary);">~</span>
                            <va-input
                              :model-value="(localRecord[field.key] || '').split('~')[1] || ''"
                              @update:model-value="(val) => { const arr = (localRecord[field.key] || '').split('~'); arr[0] = arr[0] || ''; arr[1] = val; localRecord[field.key] = arr.join('~'); }"
                              :type="focusedDateFields['edit_' + field.key + '_end'] || (localRecord[field.key] || '').split('~')[1] ? 'date' : 'text'"
                              :readonly="evalConditionRule(field, localRecord).readOnly"
                              :disabled="evalConditionRule(field, localRecord).disabled"
                              :lang="locale === 'en' ? 'en-US' : 'ko-KR'"
                              :placeholder="locale === 'en' ? 'End Date' : '종료일'"
                              style="flex: 1; min-width: 0;"
                              @focus="focusedDateFields['edit_' + field.key + '_end'] = true"
                              @blur="focusedDateFields['edit_' + field.key + '_end'] = false"
                            />
                          </template>
                        </div>

                        <!-- JSON Sub-Table / Table Schema -->
                        <div v-else-if="field.type === 'JSON'" class="w-full">
                          <div v-if="getTableColumns(field).length > 0" style="border: 1px solid var(--va-background-border); border-radius: 8px; overflow: hidden; background: var(--va-background-element);">
                            <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.5rem 0.75rem; background: var(--va-background-secondary); border-bottom: 1px solid var(--va-background-border);">
                              <span style="font-size: 0.8rem; font-weight: 700; color: var(--va-text-primary);">
                                {{ $t('total_rows_count', { count: (localRecord[field.key] || []).length }) }}
                              </span>
                              <div v-if="isEditing" style="display: flex; gap: 0.5rem;">
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
                                    <th v-if="isEditing" style="padding: 0.5rem; width: 50px; text-align: center;"></th>
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
                                      <template v-if="!isEditing">
                                        <span v-if="col.type === 'SELECT'">{{ getColSelectDisplayValue(col.options, row[col.key]) }}</span>
                                        <span v-else-if="col.type === 'BOOLEAN'">{{ row[col.key] ? 'O' : 'X' }}</span>
                                        <span v-else>{{ row[col.key] || '-' }}</span>
                                      </template>
                                      <template v-else>
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
                                      </template>
                                    </td>
                                    <td v-if="isEditing" style="padding: 0.35rem; text-align: center;">
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
                              :readonly="!isEditing"
                              :min-rows="3"
                              style="font-family: monospace; font-size: 0.85rem;"
                              class="w-full"
                              placeholder="{}"
                            />
                          </div>
                        </div>
                        <va-input
                          v-else
                          :model-value="decryptedValues[field.key] || localRecord[field.key]"
                          @update:model-value="(val) => { localRecord[field.key] = val }"
                          type="text"
                          class="w-full"
                          :readonly="!isEditing"
                        />
                      </div>
                    </div>
                  </template>
                </div>
              </div>
            </va-collapse>
          </va-accordion>
        </div>
      </div>

      <!-- Secondary Nodes Tab Content -->
      <div v-show="activeMainTab === 'secondary'" style="flex: 1; overflow-y: auto; padding: 0.5rem;">
        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem;">
          <div>
            <h4 style="font-weight: 700; font-size: 1rem; color: var(--va-text-primary); margin: 0 0 0.25rem 0;">
              {{ $t('axis.secondary_mapping_title') }}
            </h4>
            <p style="font-size: 0.85rem; color: var(--va-text-secondary); margin: 0;">
              {{ $t('axis.secondary_mapping_desc') }}
            </p>
          </div>

          <va-button
            v-if="canWrite"
            icon="edit"
            color="primary"
            size="small"
            @click="openSecondaryNodeAssignModal"
          >
            {{ $t('axis.assign_secondary_nodes') }}
          </va-button>
        </div>

        <div v-if="loadingSecondaryNodes" style="text-align: center; padding: 2rem;">
          <va-progress-circle indeterminate color="primary" />
        </div>
        <div v-else-if="secondaryNodes.length === 0" style="padding: 2rem; text-align: center; color: var(--va-text-secondary); background: var(--va-background-element); border-radius: 6px;">
          {{ $t('axis.no_secondary_nodes') }}
        </div>
        <div v-else style="display: flex; flex-direction: column; gap: 0.5rem;">
          <div v-for="sec in secondaryNodes" :key="sec.nodeId" style="display: flex; justify-content: space-between; align-items: center; padding: 0.75rem; border: 1px solid var(--va-background-border); border-radius: 6px; background: var(--va-background-primary);">
            <div>
              <div style="font-weight: 700; font-size: 0.95rem;">{{ parseMultilingualName(sec.nodeName) || sec.nodeCode }}</div>
              <div style="font-size: 0.8rem; color: var(--va-text-secondary);">{{ $t('axis.axis_label') }}: {{ parseMultilingualName(sec.axisName) || sec.axisCode || '-' }}</div>
            </div>
            <va-chip color="info" size="small" outline>{{ sec.nodeCode || sec.nodeId }}</va-chip>
          </div>
        </div>
      </div>

      <!-- History Tab Content (AG-Grid with Pagination) -->
      <div v-show="activeMainTab === 'history'" style="height: 100%; width: 100%; display: flex; flex-direction: column; flex: 1; overflow-y: auto; padding-right: 8px;">
        <div v-if="!history || history.length === 0" style="text-align: center; color: #777; padding: 2rem;">
          {{ t('audit_no_history') }}
        </div>
        <div v-else style="padding: 1rem 0.5rem;">
          <va-timeline vertical>
            <va-timeline-item
              v-for="(log, idx) in history"
              :key="log.id || idx"
              :color="getHistoryTimelineColor(log.changeType)"
              active
            >
              <template #before>
                <div style="font-size: 0.85rem; font-weight: 600; color: var(--va-secondary); margin-bottom: 0.25rem;">
                  {{ formatDate(log.changedAt) }}
                </div>
                <div
                  v-if="log.changedByName"
                  style="font-size: 0.8rem; font-weight: 700; color: var(--va-text-primary); display: inline-flex; align-items: center; gap: 0.3rem; cursor: pointer; transition: all 0.2s ease-in-out; padding: 0.15rem 0.45rem; border-radius: 6px; background: var(--va-background-element); border: 1px solid var(--va-background-border);"
                  class="user-profile-trigger"
                  :title="t('view_user_profile')"
                  @click="openUserProfileModal(log)"
                >
                  <va-icon name="account_circle" size="small" color="primary" />
                  <span>{{ log.changedByName }}</span>
                </div>
              </template>
              <template #after>
                <va-card outlined style="margin-bottom: 1.5rem; width: 100%;">
                  <va-card-title style="display: flex; justify-content: space-between; align-items: center; padding: 0.75rem 1rem; border-bottom: 1px solid var(--va-background-border);">
                    <div style="display: flex; align-items: center; gap: 0.5rem;">
                      <va-badge :color="getHistoryTimelineColor(log.changeType)" :text="log.changeType === 'PENDING_APPROVAL' ? (t('pending_approval')) : log.changeType" />
                    </div>
                    <div style="display: flex; align-items: center; gap: 0.5rem;">
                      <va-button
                        v-if="log.newData || log.previousData"
                        preset="secondary"
                        size="small"
                        icon="history"
                        @click="$emit('viewSnapshot', log.newData || log.previousData, log.id)"
                      >
                        {{ t('view_snapshot') }}
                      </va-button>
                      <va-button
                        v-if="log.approvalRequestId"
                        preset="secondary"
                        size="small"
                        icon="fact_check"
                        @click="$emit('viewApprovalHistory', log)"
                      >
                        {{ t('approval_history_btn') }}
                      </va-button>
                      <va-button
                        v-else-if="log.sourceSystem"
                        preset="secondary"
                        size="small"
                        icon="sync"
                        @click="$emit('viewIntegrationHistory', log)"
                      >
                        {{ t('integration_history_btn') }}
                      </va-button>
                    </div>
                  </va-card-title>
                  <va-card-content v-if="log.changeType === 'UPDATE' && log.previousData && log.newData" style="padding: 1rem; background: var(--va-background-secondary);">
                    <!-- Inline Diff Rendering -->
                    <div style="display: flex; flex-direction: column; gap: 0.75rem;">
                      <div v-for="fieldKey in getChangedKeys(log.previousData, log.newData)" :key="fieldKey" style="display: flex; flex-direction: column; gap: 0.35rem; font-size: 0.85rem;">
                        <div style="display: flex; align-items: center; gap: 0.5rem;">
                          <span style="font-weight: 700; color: var(--va-text-primary); min-width: 100px;">
                            {{ getFieldLabelByKey(fieldKey) }}:
                          </span>
                          <!-- If NOT JSON Type -->
                          <div v-if="getFieldByKey(fieldKey)?.type !== 'JSON'" style="display: flex; align-items: center; gap: 0.5rem; flex: 1; flex-wrap: wrap;">
                            <span style="text-decoration: line-through; color: var(--va-danger); background: rgba(229, 57, 53, 0.1); padding: 0.1rem 0.4rem; border-radius: 4px;">
                              {{ decryptedValues[log.id + '_' + fieldKey] || formatDiffValue(fieldKey, safeParseJson(log.previousData)[fieldKey]) }}
                            </span>
                            <va-icon name="arrow_forward" size="small" color="secondary" />
                            <span style="color: var(--va-success); background: rgba(30, 203, 114, 0.1); padding: 0.1rem 0.4rem; border-radius: 4px; font-weight: 600;">
                              {{ decryptedValues[log.id + '_' + fieldKey] || formatDiffValue(fieldKey, safeParseJson(log.newData)[fieldKey]) }}
                            </span>
                            <!-- Decrypt Button for History -->
                            <span v-if="getFieldByKey(fieldKey)?.isEncrypted" style="margin-left:8px; display:inline-flex; align-items:center; gap:4px; font-size:0.75rem; color:#888;">
                              <va-icon name="lock" size="small" />
                              <template v-if="!decryptedValues[log.id + '_' + fieldKey]">
                                <span style="cursor:pointer; text-decoration:underline; color:var(--va-primary);" @click.stop="requestDecryptHistoryField(log.id, fieldKey)">
                                  {{ t('view_original') }}
                                </span>
                              </template>
                              <template v-else>
                                <span style="cursor:pointer; text-decoration:underline; color:var(--va-primary);" @click.stop="hideDecryptedField(log.id + '_' + fieldKey)">
                                  {{ t('hide_original') }}
                                </span>
                                <span v-if="decryptRemainingTime[log.id + '_' + fieldKey]" style="margin-left:4px; font-variant-numeric: tabular-nums;">
                                  (00:{{ String(decryptRemainingTime[log.id + '_' + fieldKey]).padStart(2, '0') }})
                                </span>
                              </template>
                              <va-icon v-if="decryptingFields[log.id + '_' + fieldKey]" name="sync" size="small" spin />
                            </span>
                          </div>
                        </div>

                        <!-- If JSON Type: Render Sub-Table Diff -->
                        <div v-if="getFieldByKey(fieldKey)?.type === 'JSON'" style="margin-top: 0.25rem;">
                          <div v-if="getTableRows(safeParseJson(log.newData)[fieldKey]).length > 0" style="border: 1px solid var(--va-background-border); border-radius: 6px; overflow: hidden; background: var(--va-background-element);">
                            <table style="width: 100%; border-collapse: collapse; font-size: 0.8rem;">
                              <thead>
                                <tr style="background: var(--va-background-secondary); border-bottom: 1px solid var(--va-background-border);">
                                  <th style="padding: 0.4rem 0.5rem; width: 35px; text-align: center; color: var(--va-text-secondary);">#</th>
                                  <th v-for="col in getTableColumns(getFieldByKey(fieldKey))" :key="col.key" style="padding: 0.4rem 0.6rem; text-align: left; color: var(--va-text-primary); font-weight: 600;">
                                    {{ getTranslatedColName(col.name) }}
                                  </th>
                                </tr>
                              </thead>
                              <tbody>
                                <tr v-for="(row, rIdx) in getTableRows(safeParseJson(log.newData)[fieldKey])" :key="rIdx" style="border-bottom: 1px solid var(--va-background-border);">
                                  <td style="padding: 0.4rem 0.5rem; text-align: center; color: var(--va-text-secondary); font-size: 0.75rem;">{{ rIdx + 1 }}</td>
                                  <td v-for="col in getTableColumns(getFieldByKey(fieldKey))" :key="col.key" style="padding: 0.4rem 0.6rem; color: var(--va-text-primary);">
                                    {{ formatTableCellVal(row[col.key], col) }}
                                  </td>
                                </tr>
                              </tbody>
                            </table>
                          </div>
                          <div v-else style="color: var(--va-text-secondary); font-style: italic; padding: 0.25rem 0.5rem;">
                            {{ t('none') || '(없음)' }}
                          </div>
                        </div>
                      </div>
                    </div>
                  </va-card-content>
                  <va-card-content v-else-if="log.changeType === 'CREATE'" style="padding: 1rem; color: var(--va-success); font-weight: 600;">
                    <va-icon name="add_circle_outline" class="mr-2" /> {{ t('initial_created') }}
                  </va-card-content>
                  <va-card-content v-else-if="log.changeType === 'DELETE'" style="padding: 1rem; color: var(--va-danger); font-weight: 600;">
                    <va-icon name="remove_circle_outline" class="mr-2" /> {{ t('deleted_status') }}
                  </va-card-content>
                </va-card>
              </template>
            </va-timeline-item>
          </va-timeline>
        </div>
      </div>




    </div>

    <!-- Action Buttons -->
    <div style="display: flex; justify-content: flex-end; margin-top: 1rem; gap: 0.5rem;">
      <va-button
        v-if="activeMainTab === 'details' && !isEditing && recordStatus === 'MERGED' && canWrite"
        color="warning"
        icon="call_split"
        @click="showUnmergePreview = true"
      >
        {{ t('unmerge_btn') }}
      </va-button>

      <va-button
        v-if="activeMainTab === 'details' && !isEditing && !isSnapshotMode && canDelete"
        color="danger"
        @click="$emit('delete')"
      >
        {{ t('btn_delete') }}
      </va-button>
      <va-button
        v-if="activeMainTab === 'details' && !isEditing && !isSnapshotMode && canWrite"
        color="warning"
        @click="isEditing = true"
      >
        {{ t('btn_edit') }}
      </va-button>
      <va-button
        v-if="activeMainTab === 'details' && isEditing && !isSnapshotMode && canWrite"
        color="success"
        :disabled="!hasUpdateWorkflow"
        @click="$emit('save', localRecord)"
      >
        {{ t('btn_save') }}
      </va-button>
      <va-button @click="handleClose">{{ t('btn_close') }}</va-button>
    </div>
  </va-modal>


  <UnmaskReasonModal
    v-model="showUnmaskReasonModal"
    @confirm="executePendingDecrypt"
  />

  <!-- Reusable User Profile Modal Component -->
  <UserProfileModal
    v-model="showUserProfileModal"
    :user-profile="selectedUserProfile"
  />

  <UnmergePreviewModal
    v-model="showUnmergePreview"
    :master-record="localRecord"
    :source-records="subRecords"
    @confirm="$emit('unmerge', localRecord)"
  />

  <!-- Modal: Assign Secondary Classification Nodes -->
  <va-modal
    v-model="showAssignSecondaryModal"
    :title="$t('axis.assign_modal_title')"
    size="small"
    hide-default-actions
    no-outside-dismiss
  >
    <div style="padding: 0.5rem 0;">
      <div v-if="loadingAssignAxes" style="text-align: center; padding: 2rem;">
        <va-progress-circle indeterminate color="primary" />
      </div>
      <div v-else-if="assignAxesList.length === 0" style="text-align: center; padding: 1.5rem; color: var(--va-text-secondary);">
        {{ $t('axis.no_axes') }}
      </div>
      <div v-else style="display: flex; flex-direction: column; gap: 1rem;">
        <div v-for="axis in assignAxesList" :key="axis.id">
          <label style="font-weight: 700; font-size: 0.85rem; color: var(--va-text-secondary); display: block; margin-bottom: 0.5rem;">
            {{ formatAxisName(axis.name) }} ({{ axis.axisCode || axis.code }})
          </label>
          <va-select
            v-model="selectedSecondaryNodesMap[axis.id]"
            :options="getNodesForAssignAxis(axis.id)"
            value-by="id"
            text-by="text"
            searchable
            clearable
            :placeholder="getNodesForAssignAxis(axis.id).length > 0 ? $t('axis.select_nodes_placeholder') : $t('axis.no_nodes_registered')"
            :disabled="getNodesForAssignAxis(axis.id).length === 0"
          />
        </div>
      </div>

      <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1.5rem;">
        <va-button preset="secondary" type="button" @click="showAssignSecondaryModal = false">{{ $t('btn_cancel') }}</va-button>
        <va-button color="primary" type="button" :loading="savingSecondaryNodes" @click="saveSecondaryNodesAssignment">{{ $t('vuestic.save') }}</va-button>
      </div>
    </div>
  </va-modal>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { AgGridVue } from 'ag-grid-vue3'
import { useToast } from 'vuestic-ui'
import { useAgGridTheme } from '~/composables/useAgGridTheme'
import UnmaskReasonModal from '../UnmaskReasonModal.vue'
import UnmergePreviewModal from './UnmergePreviewModal.vue'

const { gridTheme } = useAgGridTheme()

const showUnmergePreview = ref(false)
const showUnmaskReasonModal = ref(false)

const pendingDecryptAction = ref(null)

const executePendingDecrypt = async (reason) => {
  if (!pendingDecryptAction.value) return
  const action = pendingDecryptAction.value
  if (action.type === 'record') {
    await executeDecryptRecordField(action.fieldKey, reason)
  } else if (action.type === 'history') {
    await executeDecryptHistoryField(action.historyId, action.fieldKey, reason)
  }
}

const createUnifiedBtn = (label, colorHex, onClick) => {
  const btn = document.createElement('button');
  btn.style.cssText = `
    display: inline-flex;
    align-items: center;
    justify-content: center;
    height: 24px;
    padding: 0 8px;
    font-size: 11px;
    font-weight: 600;
    line-height: 1;
    border: 1px solid ${colorHex};
    color: ${colorHex};
    background: ${colorHex}15;
    border-radius: 4px;
    cursor: pointer;
    white-space: nowrap;
    box-sizing: border-box;
    transition: all 0.15s ease-in-out;
  `;
  btn.innerText = label;
  btn.onclick = onClick;
  return btn;
};


const { t, locale: i18nLocale } = useI18n()

const showUserProfileModal = ref(false)
const selectedUserProfile = ref(null)

const openUserProfileModal = (log) => {
  if (!log) return;
  if (log.changedUserProfile) {
    selectedUserProfile.value = log.changedUserProfile;
  } else {
    selectedUserProfile.value = {
      id: log.changedBy,
      username: log.changedByName || log.changedBy || 'Unknown User',
      role: 'USER',
      organizationName: null,
      departmentName: null,
      teamName: null,
      timezone: null
    };
  }
  showUserProfileModal.value = true;
};

const getHistoryTimelineColor = (type) => {
  if (type === 'PENDING_APPROVAL') return 'warning';
  if (type === 'CREATE') return 'success';
  if (type === 'DELETE') return 'danger';
  return 'primary';
};

const safeParseJson = (val) => {
  if (!val) return {};
  if (typeof val === 'object') return val;
  try { return JSON.parse(val); } catch (e) { return {}; }
};

const getChangedKeys = (prev, curr) => {
  if (!prev || !curr) return [];
  const prevObj = safeParseJson(prev);
  const currObj = safeParseJson(curr);
  const keys = new Set([...Object.keys(prevObj), ...Object.keys(currObj)]);
  const changed = [];
  for (const k of keys) {
    if (k === 'id' || k === 'createdAt' || k === 'updatedAt' || k === 'createdBy' || k === 'updatedBy' || k === 'domainId' || k === 'status') continue;
    if (k.startsWith('_idx_')) continue;
    if (JSON.stringify(prevObj[k]) !== JSON.stringify(currObj[k])) {
      changed.push(k);
    }
  }
  return changed;
};

const getTableRows = (val) => {
  if (!val) return []
  if (Array.isArray(val)) return val
  if (typeof val === 'string') {
    try {
      const parsed = JSON.parse(val)
      return Array.isArray(parsed) ? parsed : []
    } catch (e) {
      return []
    }
  }
  return []
}

const formatTableCellVal = (val, col) => {
  if (val === null || val === undefined || val === '') return '-'
  if (col && col.type === 'SELECT' && col.options) {
    let opts = []
    if (typeof col.options === 'string') {
      try { opts = JSON.parse(col.options) } catch (e) {}
    } else if (Array.isArray(col.options)) {
      opts = col.options
    }
    const found = opts.find((o) => (o.value || o.key || o.code) === val)
    if (found) return found.label || found.name || val
  }
  if (typeof val === 'object') {
    return val[locale.value] || val.ko || val.en || JSON.stringify(val)
  }
  return String(val)
}

const formatDiffValue = (key, val) => {
  if (val === null || val === undefined) return '(null)';
  const f = props.fields?.find(f => f.key === key || String(f.id) === String(key) || (f.key && String(f.key).toLowerCase() === String(key).toLowerCase()));
  if (f && f.type === 'DOMAIN_REFERENCE') {
    return getDomainRefDisplayName(key, val);
  }
  if (f && f.options) {
    let opts = [];
    if (typeof f.options === 'string') {
      try {
        const parsed = JSON.parse(f.options);
        opts = Array.isArray(parsed) ? parsed : [];
      } catch(e) {}
    } else if (Array.isArray(f.options)) {
      opts = f.options;
    }
    if (Array.isArray(opts) && opts.length > 0) {
      const matchedOpt = opts.find(o => o && (String(o.value) === String(val) || String(o.code) === String(val) || String(o.id) === String(val)));
      if (matchedOpt) {
        return matchedOpt.label || matchedOpt.name || String(val);
      }
    }
  }
  if (f && f.type === 'DATE_RANGE' && typeof val === 'string') {
    return val.replace('~', ' ~ ');
  }
  if (typeof val === 'object') return JSON.stringify(val);
  return String(val);
};

const getFieldLabelByKey = (key) => {
  if (!key) return '';
  const f = props.fields?.find(f => f.key === key || String(f.id) === String(key) || (f.key && String(f.key).toLowerCase() === String(key).toLowerCase()));
  return f ? getTranslatedName(f.name) : key;
};

const getFieldByKey = (key) => {
  if (!key) return null;
  return props.fields?.find(f => f.key === key || String(f.id) === String(key) || (f.key && String(f.key).toLowerCase() === String(key).toLowerCase()));
};

const historyGridColumnDefs = computed(() => {
  // Touch i18nLocale value for reactive updates on locale change
  const _dummy = i18nLocale.value
  return [
    {
      field: 'changedAt',
      headerName: t('date_time') || (i18nLocale.value === 'en' ? 'Date & Time' : '일시'),
      sortable: true,
      width: 175,
      valueFormatter: (params) => formatDate(params.value)
    },
    {
      field: 'changedBy',
      headerName: t('processed_by') || (i18nLocale.value === 'en' ? 'Processed By' : '처리자'),
      width: 110,
      valueFormatter: (params) => getUserName(params.value, params.data?.changedBy)
    },
    {
      field: 'changeType',
      headerName: t('change_type') || (i18nLocale.value === 'en' ? 'Type' : '유형'),
      width: 115,
      cellRenderer: (params) => {
        if (!params || !params.value) return '';
        const val = params.value;
        const span = document.createElement('span');
        span.style.cssText = 'display: inline-flex; align-items: center; justify-content: center; height: 22px; padding: 0 6px; border-radius: 4px; font-weight: 700; font-size: 11px; color: white; box-sizing: border-box;';

        if (val === 'PENDING_APPROVAL') {
          span.style.background = '#e6a23c';
          span.innerText = t('pending_approval');
        } else if (val === 'CREATE') {
          span.style.background = '#1ebc72';
          span.innerText = 'CREATE';
        } else if (val === 'DELETE') {
          span.style.background = '#e53935';
          span.innerText = 'DELETE';
        } else {
          span.style.background = '#2c82e0';
          span.innerText = val;
        }
        return span;
      }
    },
    {
      field: 'diff',
      headerName: t('change_details'),
      width: 150,
      cellRenderer: (params) => {
        if (!params || !params.data) return '';
        const row = params.data;
        const div = document.createElement('div');
        div.style.cssText = 'display: flex; align-items: center; height: 100%; gap: 0.35rem;';

        if (row.changeType === 'PENDING_APPROVAL' && row.rawRequest) {
          const btn = createUnifiedBtn(t('view_changes'), '#2c82e0', () => emit('viewDiffDetails', row.rawRequest.changes, row.rawRequest.targetType, true));
          div.appendChild(btn);
        } else if (row.changeType === 'UPDATE') {
          const btn = createUnifiedBtn(t('view_changes'), '#2c82e0', () => emit('viewDiffDetails', row.previousData, row.newData, false));
          div.appendChild(btn);
        } else if (row.changeType === 'CREATE') {
          div.innerHTML = `<span style="display: inline-flex; align-items: center; justify-content: center; height: 22px; background: #1ebc72; color: white; padding: 0 5px; border-radius: 4px; font-size: 11px; font-weight: 700;">CREATE</span> <span style="font-size: 11px; font-weight: 700; color: #15803d; margin-left: 4px; display: inline-flex; align-items: center; height: 100%;">${t('initial_created')}</span>`;
        } else if (row.changeType === 'DELETE') {
          div.innerHTML = `<span style="display: inline-flex; align-items: center; justify-content: center; height: 22px; background: #e53935; color: white; padding: 0 5px; border-radius: 4px; font-size: 11px; font-weight: 700;">DELETE</span> <span style="font-size: 11px; font-weight: 700; color: #b91c1c; margin-left: 4px; display: inline-flex; align-items: center; height: 100%;">${t('deleted_status')}</span>`;
        }
        return div;
      }
    },
    {
      field: 'actions',
      headerName: t('actions'),
      flex: 1,
      minWidth: 280,
      cellRenderer: (params) => {
        if (!params || !params.data) return '';
        const row = params.data;
        const container = document.createElement('div');
        container.style.cssText = 'display: flex; align-items: center; height: 100%; gap: 0.35rem;';

        if (row.changeType === 'CREATE') {
          container.appendChild(createUnifiedBtn(t('view_snapshot'), '#0284c7', () => emit('viewSnapshot', row.newData, row.id)));
          if (row.approvalRequestId) {
            container.appendChild(createUnifiedBtn(t('approval_history_btn'), '#6b7280', () => emit('viewApprovalHistory', row)));
          } else if (row.sourceSystem) {
            container.appendChild(createUnifiedBtn(t('integration_history_btn'), '#0284c7', () => emit('viewIntegrationHistory', row)));
          }
        } else if (row.changeType === 'DELETE') {
          container.appendChild(createUnifiedBtn(t('last_snapshot'), '#d97706', () => emit('viewSnapshot', row.previousData, row.id)));
          if (row.approvalRequestId) {
            container.appendChild(createUnifiedBtn(t('approval_history_btn'), '#6b7280', () => emit('viewApprovalHistory', row)));
          } else if (row.sourceSystem) {
            container.appendChild(createUnifiedBtn(t('integration_history_btn'), '#0284c7', () => emit('viewIntegrationHistory', row)));
          }
        } else if (row.changeType === 'UPDATE') {
          container.appendChild(createUnifiedBtn(t('prev_snapshot'), '#d97706', () => emit('viewSnapshot', row.previousData, row.id)));
          container.appendChild(createUnifiedBtn(t('next_snapshot'), '#0284c7', () => emit('viewSnapshot', row.newData, row.id)));
          if (row.approvalRequestId) {
            container.appendChild(createUnifiedBtn(t('approval_history_btn'), '#6b7280', () => emit('viewApprovalHistory', row)));
          } else if (row.sourceSystem) {
            container.appendChild(createUnifiedBtn(t('integration_history_btn'), '#0284c7', () => emit('viewIntegrationHistory', row)));
          }
        } else if (row.changeType === 'PENDING_APPROVAL') {
          container.appendChild(createUnifiedBtn(t('approval_monitoring'), '#d97706', () => emit('viewApprovalHistory', row)));
        }
        return container;
      }
    }
  ]
})



const props = defineProps({
  show: { type: Boolean, default: false },
  record: { type: Object, default: () => ({}) },
  fields: { type: Array, default: () => [] },
  history: { type: Array, default: () => [] },
  nodeLabel: { type: String, default: '' },
  isSnapshotMode: { type: Boolean, default: false },
  snapshotId: { type: String, default: null },
  hasPendingUpdate: { type: Boolean, default: false },
  isEditingRecord: { type: Boolean, default: false },
  hasUpdateWorkflow: { type: Boolean, default: true },
  canDelete: { type: Boolean, default: true },
  canWrite: { type: Boolean, default: true },
  canReadHistory: { type: Boolean, default: true },
  selectedDomainInfo: { type: Object, default: null },
  domainReferences: { type: Object, default: () => ({}) },
  userList: { type: Array, default: () => [] }
})

const emit = defineEmits([
  'close',
  'save',
  'delete',
  'unmerge',
  'openHistory',
  'openDomainRef',
  'viewDiffDetails',
  'viewSnapshot',
  'viewApprovalHistory',
  'viewIntegrationHistory',
  'secondaryNodesUpdated',
  'update:show'
])

const localeCookie = useCookie('locale', { default: () => 'ko' })
const locale = computed(() => localeCookie.value || 'ko')

const modalVisible = computed({
  get: () => props.show,
  set: (val) => {
    emit('update:show', val)
    if (!val) emit('close')
  }
})

const isEditing = ref(props.isEditingRecord)
watch(
  () => props.isEditingRecord,
  (val) => {
    isEditing.value = val
  }
)

const activeMainTab = ref('details')
const activeSectorTab = ref(0)
const focusedDateFields = ref({})
const localRecord = ref({})

const handleMaskedInput = (field, val) => {
  localRecord.value[field.key] = val
  nextTick(() => {
    localRecord.value[field.key] = formatMaskedInput(val, field.maskingPattern)
  })
}

const decryptedValues = ref({})
const decryptingFields = ref({})
const decryptTimers = ref({})
const decryptRemainingTime = ref({})
const decryptIntervals = ref({})

const requestDecryptRecordField = (fieldKey) => {
  pendingDecryptAction.value = { type: 'record', fieldKey }
  showUnmaskReasonModal.value = true
}

const executeDecryptRecordField = async (fieldKey, reason) => {
  let recId = null
  let endpoint = ''
  
  if (props.isSnapshotMode && props.snapshotId) {
    recId = props.snapshotId
    endpoint = `/api/sensitive-data/history/${recId}/decrypt`
  } else {
    recId = props.record?.id || localRecord.value?.id
    if (!recId) return
    endpoint = `/api/sensitive-data/record/${recId}/decrypt`
  }
  if (!recId) return

  decryptingFields.value[fieldKey] = true
  try {
    const token = useCookie('auth_token').value
    const res = await $fetch(endpoint, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` },
      body: { fieldKeys: [fieldKey], accessReason: reason }
    })
    if (res && res[fieldKey]) {
      decryptedValues.value[fieldKey] = res[fieldKey]
      
      if (decryptTimers.value[fieldKey]) clearTimeout(decryptTimers.value[fieldKey])
      if (decryptIntervals.value[fieldKey]) clearInterval(decryptIntervals.value[fieldKey])
      
      decryptRemainingTime.value[fieldKey] = 30
      
      decryptIntervals.value[fieldKey] = setInterval(() => {
        if (decryptRemainingTime.value[fieldKey] > 0) {
          decryptRemainingTime.value[fieldKey]--
        }
      }, 1000)

      decryptTimers.value[fieldKey] = setTimeout(() => {
        hideDecryptedField(fieldKey)
      }, 30000)
    }
  } catch (e) {
    console.error('Failed to decrypt field:', e)
    useToast().init({ message: t('decrypt_failed'), color: 'danger' })
  } finally {
    decryptingFields.value[fieldKey] = false
  }
}

const requestDecryptHistoryField = (historyId, fieldKey) => {
  pendingDecryptAction.value = { type: 'history', historyId, fieldKey }
  showUnmaskReasonModal.value = true
}

const executeDecryptHistoryField = async (historyId, fieldKey, reason) => {
  const key = `${historyId}_${fieldKey}`
  decryptingFields.value[key] = true
  try {
    const token = useCookie('auth_token').value
    const res = await $fetch(`/api/sensitive-data/history/${historyId}/decrypt`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${token}` },
      body: { fieldKeys: [fieldKey], accessReason: reason }
    })
    if (res && res[fieldKey]) {
      decryptedValues.value[key] = res[fieldKey]
      
      if (decryptTimers.value[key]) clearTimeout(decryptTimers.value[key])
      if (decryptIntervals.value[key]) clearInterval(decryptIntervals.value[key])
      
      decryptRemainingTime.value[key] = 30
      
      decryptIntervals.value[key] = setInterval(() => {
        if (decryptRemainingTime.value[key] > 0) {
          decryptRemainingTime.value[key]--
        }
      }, 1000)

      decryptTimers.value[key] = setTimeout(() => {
        hideDecryptedField(key)
      }, 30000)
    }
  } catch (e) {
    console.error('Failed to decrypt history field:', e)
    useToast().init({ message: t('decrypt_failed'), color: 'danger' })
  } finally {
    decryptingFields.value[key] = false
  }
}

const hideDecryptedField = (key) => {
  if (decryptTimers.value[key]) {
    clearTimeout(decryptTimers.value[key])
    delete decryptTimers.value[key]
  }
  if (decryptIntervals.value[key]) {
    clearInterval(decryptIntervals.value[key])
    delete decryptIntervals.value[key]
  }
  delete decryptRemainingTime.value[key]
  delete decryptedValues.value[key]
}

const secondaryNodes = ref([])
const loadingSecondaryNodes = ref(false)

const loadSecondaryNodes = async () => {
  const recId = props.record?.id || localRecord.value?.id
  if (!recId) return
  loadingSecondaryNodes.value = true
  try {
    const token = useCookie('auth_token').value
    const res = await $fetch(`/api/records/${recId}/secondary-nodes`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    secondaryNodes.value = res || []
  } catch (e) {
    secondaryNodes.value = []
  } finally {
    loadingSecondaryNodes.value = false
  }
}

// Assignment Modal Logic
const showAssignSecondaryModal = ref(false)
const loadingAssignAxes = ref(false)
const savingSecondaryNodes = ref(false)
const assignAxesList = ref([])
const assignAxisNodesMap = ref({})
const selectedSecondaryNodesMap = ref({})

const formatAxisName = (name) => {
  if (!name) return ''
  if (typeof name === 'string') {
    try {
      const parsed = JSON.parse(name)
      if (parsed && typeof parsed === 'object') return parsed[locale.value] || parsed.ko || parsed.en || Object.values(parsed)[0] || ''
    } catch {}
    return name
  }
  if (typeof name === 'object') return name[locale.value] || name.ko || name.en || Object.values(name)[0] || ''
  return String(name)
}

// nodeName, axisName 등 다국어 JSON 문자열 또는 객체를 현재 로케일로 파싱
const parseMultilingualName = (name) => {
  return formatAxisName(name)
}

const openSecondaryNodeAssignModal = async () => {
  const domainId = props.selectedDomainInfo?.id || localRecord.value?.domainId || props.record?.domainId
  if (!domainId) return

  showAssignSecondaryModal.value = true
  loadingAssignAxes.value = true

  try {
    const token = useCookie('auth_token').value
    const axes = await $fetch(`/api/domains/${domainId}/axes`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    assignAxesList.value = axes || []

    const nodesMap = {}
    const selectionsMap = {}

    const assignedIds = (secondaryNodes.value || []).map(n => n.nodeId)

    for (const axis of assignAxesList.value) {
      const tree = await $fetch(`/api/domains/${domainId}/nodes/tree?axisId=${axis.id}`, {
        headers: { Authorization: `Bearer ${token}` }
      })
      const flatList = []
      const flattenTree = (nodes) => {
        if (!Array.isArray(nodes)) return
        nodes.forEach(n => {
          const nameObj = typeof n.name === 'object' ? n.name : { ko: String(n.name || '') }
          const label = nameObj[locale.value] || nameObj.ko || nameObj.en || 'Unknown'
          flatList.push({ id: n.id, text: label })
          if (n.children && n.children.length > 0) flattenTree(n.children)
        })
      }
      flattenTree(tree || [])
      nodesMap[axis.id] = flatList

      // 단일 선택: 이미 할당된 노드 중 이 축에 해당하는 첫 번째 nodeId 사용
      const alreadyAssigned = flatList.find(n => assignedIds.includes(n.id))
      selectionsMap[axis.id] = alreadyAssigned ? alreadyAssigned.id : null
    }

    assignAxisNodesMap.value = nodesMap
    selectedSecondaryNodesMap.value = selectionsMap
  } catch (e) {
    console.error('Failed to load axes for record assignment', e)
  } finally {
    loadingAssignAxes.value = false
  }
}

const getNodesForAssignAxis = (axisId) => {
  return assignAxisNodesMap.value[axisId] || []
}

const saveSecondaryNodesAssignment = async () => {
  const recId = props.record?.id || localRecord.value?.id
  if (!recId) {
    useToast().init({ message: t('axis.secondary_nodes_save_failed') + ': Record ID missing', color: 'danger' })
    return
  }

  if (!props.canWrite) {
    useToast().init({ message: t('no_permission'), color: 'warning' })
    return
  }

  savingSecondaryNodes.value = true
  try {
    const token = useCookie('auth_token').value
    const allNodeIds = []
    Object.values(selectedSecondaryNodesMap.value).forEach(val => {
      if (val && typeof val === 'string') allNodeIds.push(val)
    })

    console.log('[SecondaryNode] Saving nodeIds:', allNodeIds, 'for record:', recId)

    await $fetch(`/api/records/${recId}/secondary-nodes`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: { nodeIds: [...new Set(allNodeIds)].filter(id => id && typeof id === 'string') }
    })

    useToast().init({ message: t('axis.secondary_nodes_saved'), color: 'success' })
    showAssignSecondaryModal.value = false
    await loadSecondaryNodes()
    emit('secondaryNodesUpdated')
  } catch (e) {
    console.error('Failed to save secondary nodes assignment', e)
    useToast().init({ message: t('axis.secondary_nodes_save_failed') + ': ' + (e?.data?.message || e?.message || String(e)), color: 'danger' })
  } finally {
    savingSecondaryNodes.value = false
  }
}

const openDomainRefPicker = (fieldKey) => {
  emit('openDomainRef', {
    fieldKey,
    isCreate: false,
    currentData: { ...localRecord.value }
  })
}

watch(() => props.show, (val) => {
  if (val) {
    localRecord.value = props.record ? { ...props.record } : {}
    loadSecondaryNodes()
  }
})

watch(
  () => props.record,
  (newVal) => {
    if (!newVal) {
      if (!props.show) localRecord.value = {}
      return
    }
    if (props.show && Object.keys(localRecord.value).length > 0) {
      localRecord.value = { ...localRecord.value, ...newVal }
    } else {
      localRecord.value = { ...newVal }
    }
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

const getColSelectDisplayValue = (options, value) => {
  if (value === undefined || value === null || value === '') return '-'
  const parsed = getColSelectOptions(options)
  const match = parsed.find(o => String(o.value) === String(value))
  return match ? match.text : value
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

const cleanedNodeLabel = computed(() => {
  if (!props.nodeLabel) return ''
  return props.nodeLabel.replace(/\s*\([^)]*\)/g, '').trim() || props.nodeLabel.trim()
})

const headerRecordName = computed(() => {
  const rec = localRecord.value || {}
  let nameStr = ''
  if (rec.name) {
    if (typeof rec.name === 'object') {
      nameStr = rec.name[locale.value] || rec.name.ko || rec.name.en || ''
    } else {
      nameStr = String(rec.name)
    }
  }
  if (!nameStr) {
    nameStr = rec.name_ko || rec.name_en || rec.title || rec.label || ''
  }
  return nameStr
})

const headerRecordId = computed(() => {
  const rec = localRecord.value || {}

  // 1. Identifier Field from Selected Domain
  if (props.selectedDomainInfo?.identifierFieldId && props.fields?.length > 0) {
    const targetField = props.fields.find(f => f.id === props.selectedDomainInfo.identifierFieldId)
    if (targetField && rec[targetField.key] !== undefined && rec[targetField.key] !== null) {
      return String(rec[targetField.key])
    }
  }

  // 2. Identifier fields by naming convention
  if (props.fields?.length > 0) {
    const idField = props.fields.find(f => 
      f.key.toLowerCase().includes('empno') || 
      f.key.toLowerCase().includes('code') || 
      f.key.toLowerCase().includes('number') || 
      f.key === 'id' || 
      (typeof f.name === 'object' && (f.name.ko?.includes('사번') || f.name.ko?.includes('코드') || f.name.ko?.includes('번호')))
    )
    if (idField && rec[idField.key] !== undefined && rec[idField.key] !== null) {
      return String(rec[idField.key])
    }
  }

  // 3. Fallback record object keys
  let idStr = rec.empNo || rec.employeeNo || rec.emp_no || rec.code || rec.id || ''
  if (!idStr) {
    for (const [k, v] of Object.entries(rec)) {
      if (v !== undefined && v !== null && typeof v !== 'object' && (k.toLowerCase().includes('no') || k.toLowerCase().includes('code') || k.includes('사번') || k.includes('코드'))) {
        idStr = String(v)
        break
      }
    }
  }

  if (!idStr && props.record?.id) {
    idStr = String(props.record.id).substring(0, 8) + '...'
  } else if (typeof idStr === 'string' && idStr.length > 15 && !rec.empNo && !rec.employeeNo) {
    idStr = idStr.substring(0, 8) + '...'
  }

  return idStr ? String(idStr) : ''
})


const headerRecordTitle = computed(() => {
  const name = headerRecordName.value
  const id = headerRecordId.value
  if (name && id) {
    return `${name} (${id})`
  }
  return name || id || ''
})


const recordStatus = computed(() => {
  return localRecord.value?.status || props.record?.status || ''
})

watch(
  () => [props.show, props.isSnapshotMode],
  ([val, snapshot]) => {
    if (val) {
      if (snapshot) {
        activeMainTab.value = 'details'
      } else {
        emit('openHistory')
      }
    }
  },
  { immediate: true }
)


const handleClose = () => {
  emit('close')
}


const historyColumns = computed(() => [
  { key: 'changedAt', label: t('date_time'), sortable: true },
  { key: 'changedBy', label: t('processed_by') },
  { key: 'changeType', label: t('change_type') },
  { key: 'diff', label: t('change_details') },
  { key: 'actions', label: t('actions') }
])

const isUuid = (val) => typeof val === 'string' && /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/.test(val)

const getUserName = (uuid, nameFallback) => {
  const target = uuid || nameFallback
  if (!target) return ''
  const u = props.userList?.find((user) => user.uuid === target || user.id === target || user.username === target)
  if (u) return u.username || u.name || ''
  if (isUuid(target)) return ''
  return target
}

const parseDate = (dateString) => {
  if (!dateString) return null
  let str = String(dateString).trim()
  if (/^\d+$/.test(str)) {
    return new Date(parseInt(str, 10))
  }
  if (!str.endsWith('Z') && !str.includes('+') && !/[-+]\d{2}:\d{2}$/.test(str)) {
    if (str.includes(' ') && !str.includes('T')) {
      str = str.replace(' ', 'T')
    }
    const serverOffset = useCookie('server_offset', { default: () => '+09:00' }).value
    str += serverOffset
  }
  const d = new Date(str)
  return isNaN(d.getTime()) ? new Date(dateString) : d
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = parseDate(dateString)
  if (!date) return ''
  const tz = useCookie('timezone', { default: () => 'Asia/Seoul' }).value || 'Asia/Seoul'
  const currentLoc = (i18nLocale?.value || locale.value) === 'en' ? 'en-US' : 'ko-KR'
  const formatted = date.toLocaleString(currentLoc, {
    timeZone: tz,
    year: 'numeric',
    month: 'numeric',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric',
    second: 'numeric',
    hour12: true
  })
  return formatted.replace(/\s*(GMT|UTC|KST|PST|EST|CET)[-+0-9:]*/gi, '').trim()
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
    const sObj = f.fieldGroup?.sector
    const gObj = f.fieldGroup

    const sName = getTranslatedName(sObj?.name) || (locale.value === 'ko' ? '일반' : 'General')
    const sKey = sObj?.id || 'default'
    const sOrder = sObj?.sortOrder || 0

    const gName = getTranslatedName(gObj?.name) || (locale.value === 'ko' ? '기본 필드' : 'Fields')
    const gKey = gObj?.id || 'default'
    const gOrder = gObj?.sortOrder || 0

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

  return sectors.map((s) => {
    const groups = Array.from(s.groups.values())
    groups.sort((a, b) => a.order - b.order)
    return { key: s.key, label: s.label, groups: groups }
  })
})

const parseOptions = (opts) => {
  if (!opts) return []
  if (typeof opts === 'string') {
    if (opts.trim().startsWith('[')) {
      try {
        const parsed = JSON.parse(opts)
        const mapped = parsed.map((o) => {
          if (typeof o === 'string') return { text: o, value: o, order: 0 }
          return {
            value: o.key,
            text: o.label?.[locale.value] || o.label?.ko || o.label?.en || o.key,
            order: o.order || 0
          }
        })
        return mapped.sort((a, b) => a.order - b.order)
      } catch (e) {}
    }
    return opts.split(',').map((s) => {
      const val = s.trim()
      return { text: val, value: val }
    })
  }
  return opts
}

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

const evaluateConditionExpression = (expr, formData) => {
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

const evalConditionRule = (field, formData) => {
  const defaultRes = {
    show: true,
    highlight: field?.isHighlighted || false,
    required: field?.required || false,
    readOnly: field?.isReadOnly || false,
    disabled: false
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

const removeFile = (arr, index) => {
  if (!arr || !Array.isArray(arr)) return
  arr.splice(index, 1)
}
</script>

<style scoped>
.mb-4 { margin-bottom: 1rem; }
.w-full { width: 100%; }

.main-segmented-tabs {
  background: rgba(0, 0, 0, 0.05);
}

:deep(.va-modal--dark) .main-segmented-tabs,
.main-segmented-tabs.dark {
  background: rgba(255, 255, 255, 0.08) !important;
  border-color: rgba(255, 255, 255, 0.12) !important;
}

.segmented-tab-btn {
  color: var(--va-secondary, #6b7280);
  font-size: 0.875rem;
}

.segmented-tab-btn:hover {
  background: rgba(0, 0, 0, 0.04) !important;
  color: var(--va-primary, #2c82e0);
}

.segmented-tab-btn.active {
  background: var(--va-primary, #2c82e0) !important;
  color: #ffffff !important;
  box-shadow: 0 2px 8px rgba(44, 130, 224, 0.35);
}

.segmented-tab-btn.active .tab-badge {
  background: rgba(255, 255, 255, 0.25) !important;
  color: #ffffff !important;
}



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
</style>
