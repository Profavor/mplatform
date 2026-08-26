<template>
  <div class="dynamic-layout-container" :style="gridContainerStyle">
    <template v-for="widget in validWidgets" :key="widget.id">
      <div
        class="layout-widget-item"
        :style="getWidgetStyle(widget)"
        :class="[
          'widget-type-' + (widget.type || '').toLowerCase(),
          { 'is-highlighted': widget.options?.highlight },
          { 'is-single-row': widget.h === 1 }
        ]"
      >
        <!-- ============================================== -->
        <!-- 1. SINGLE-ROW COMPACT MODE (h === 1)           -->
        <!-- ============================================== -->
        <template v-if="widget.h === 1">
          <div class="widget-single-row-content">
            <div class="single-row-left">
              <va-icon :name="getWidgetIcon(widget.type)" size="14px" color="primary" class="mr-1" />
              <span class="single-row-label">
                {{ getWidgetTitle(widget) }}
                <span v-if="isFieldRequired(widget)" class="required-star">*</span>
              </span>
            </div>

            <div class="single-row-center">
              <!-- BOOLEAN -->
              <div v-if="getFieldType(widget) === 'BOOLEAN'" class="single-row-bool">
                <va-switch
                  :model-value="!!record[widget.fieldKey]"
                  @update:model-value="(val) => setFieldValue(widget.fieldKey, val)"
                  :readonly="!isEditing || widget.options?.readOnly"
                  size="small"
                />
                <span class="sample-val-text">{{ record[widget.fieldKey] ? $t('yes') : $t('no') }}</span>
              </div>

              <!-- MULTILINGUAL -->
              <div v-else-if="getFieldType(widget) === 'MULTILINGUAL'" class="single-row-multi">
                <span class="sample-chip-tag">KO</span><span class="sample-val-text">{{ getMultilingualValue(record[widget.fieldKey], 'ko') || '-' }}</span>
                <span class="sample-chip-tag ml-2">EN</span><span class="sample-val-text">{{ getMultilingualValue(record[widget.fieldKey], 'en') || '-' }}</span>
              </div>

              <!-- FILE -->
              <div v-else-if="getFieldType(widget) === 'FILE'" class="single-row-file">
                <template v-if="getFilesList(record[widget.fieldKey]).length > 0">
                  <va-chip
                    v-for="(fObj, fIdx) in getFilesList(record[widget.fieldKey])"
                    :key="fIdx"
                    size="small"
                    outline
                    icon="download"
                    color="primary"
                    style="cursor: pointer; padding: 0 6px; font-size: 0.78rem;"
                  >
                    {{ fObj.name || fObj }}
                  </va-chip>
                </template>
                <span v-else class="sample-val-text text-secondary">{{ $t('no_file_data') }}</span>
              </div>

              <!-- SECTION -->
              <div v-else-if="widget.type === 'SECTION'" class="single-row-section">
                <strong>{{ getWidgetTitle(widget) }}</strong>
              </div>

              <!-- DIVIDER -->
              <div v-else-if="widget.type === 'DIVIDER'" class="w-full">
                <hr class="canvas-divider-hr" />
              </div>

              <!-- STANDARD VALUE / INPUT -->
              <div v-else class="single-row-val-box">
                <va-input
                  v-if="['TEXT', 'NUMBER', 'INTEGER', 'DECIMAL', 'FLOAT', 'DATE', 'DATETIME', 'EMAIL', 'PHONE'].includes(getFieldType(widget))"
                  :model-value="getFieldType(widget) === 'DATE' ? formatDateValue(record[widget.fieldKey]) : record[widget.fieldKey]"
                  @update:model-value="(val) => setFieldValue(widget.fieldKey, val)"
                  :type="getFieldType(widget) === 'DATE' ? (isEditing ? 'date' : 'text') : (['NUMBER', 'INTEGER', 'DECIMAL', 'FLOAT'].includes(getFieldType(widget)) ? 'number' : 'text')"
                  class="single-row-input"
                  :readonly="!isEditing || widget.options?.readOnly"
                />
                <va-select
                  v-else-if="['SELECT', 'MULTI_SELECT', 'CODE', 'ENUM'].includes(getFieldType(widget))"
                  :model-value="record[widget.fieldKey]"
                  @update:model-value="(val) => setFieldValue(widget.fieldKey, val)"
                  :options="getFieldOptions(getFieldDefinition(widget.fieldKey))"
                  :multiple="getFieldType(widget) === 'MULTI_SELECT' || getFieldDefinition(widget.fieldKey)?.isMultiValue"
                  class="single-row-input"
                  :readonly="!isEditing || widget.options?.readOnly"
                />
                <div v-else class="sample-val-text">
                  {{ record[widget.fieldKey] ?? '-' }}
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- ============================================== -->
        <!-- 2. MULTI-ROW RICH MODE (h >= 2)                -->
        <!-- ============================================== -->
        <template v-else>
          <!-- 1. IMAGE WIDGET -->
          <div v-if="widget.type === 'IMAGE' || isFieldType(widget, 'IMAGE')" class="widget-image-box">
            <div class="widget-box-header">
              <span class="widget-box-title">
                <va-icon name="image" size="14px" color="primary" class="mr-1" />
                {{ getWidgetTitle(widget) }}
                <span v-if="isFieldRequired(widget)" class="required-star">*</span>
              </span>
            </div>
            <div class="widget-image-content" :style="{ height: getImageContentHeight(widget) }">
              <ImageUploader
                v-if="isEditing"
                v-model="record[widget.fieldKey]"
                :multiple="getFieldDefinition(widget.fieldKey)?.isMultiValue"
                :readonly="!isEditing || widget.options?.readOnly"
              />
              <div v-else class="image-view-wrapper">
                <template v-if="record[widget.fieldKey]">
                  <img
                    :src="getImageUrl(record[widget.fieldKey])"
                    :alt="getWidgetTitle(widget) || 'Record Image'"
                    class="preview-img"
                    :style="{ objectFit: widget.options?.objectFit || 'cover' }"
                    @click="openImageLightbox(record[widget.fieldKey], getWidgetTitle(widget))"
                  />
                  <div class="image-zoom-overlay" @click="openImageLightbox(record[widget.fieldKey], getWidgetTitle(widget))">
                    <va-icon name="zoom_in" size="small" color="#fff" />
                    <span>{{ $t('click_to_zoom') }}</span>
                  </div>
                </template>
                <div v-else class="empty-image-placeholder">
                  <va-icon name="image_not_supported" size="large" color="secondary" />
                  <span class="empty-text">{{ $t('no_image_data') }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 2. RICH TEXT / TIPTAP HTML EDITOR WIDGET -->
          <div v-else-if="widget.type === 'EDITOR' || isFieldType(widget, 'HTML') || isFieldType(widget, 'HTML_TEXT') || isFieldType(widget, 'RICHTEXT')" class="widget-editor-box">
            <div class="widget-box-header">
              <span class="widget-box-title">
                <va-icon name="edit_note" size="14px" color="primary" class="mr-1" />
                {{ getWidgetTitle(widget) }}
                <span v-if="isFieldRequired(widget)" class="required-star">*</span>
              </span>
            </div>
            <div class="widget-editor-content" :style="{ height: getEditorContentHeight(widget) }">
              <HtmlEditor
                v-model="record[widget.fieldKey]"
                :readonly="!isEditing || widget.options?.readOnly"
                :placeholder="getWidgetTitle(widget)"
              />
            </div>
          </div>

          <!-- 3. SECTION CARD WIDGET -->
          <div v-else-if="widget.type === 'SECTION'" class="widget-section-box">
            <div class="section-title-bar">
              <va-icon name="folder_open" color="primary" size="small" class="mr-1" />
              <h4 class="section-heading">{{ getWidgetTitle(widget) }}</h4>
            </div>
          </div>

          <!-- 4. NOTICE CALLOUT WIDGET -->
          <div v-else-if="widget.type === 'CALLOUT'" class="widget-callout-box" :class="'callout-' + (widget.options?.theme || 'info')">
            <va-icon :name="widget.options?.icon || 'info'" size="small" class="mr-2" />
            <span class="callout-text">{{ getWidgetTitle(widget) }}</span>
          </div>

          <!-- 5. DIVIDER WIDGET -->
          <div v-else-if="widget.type === 'DIVIDER'" class="widget-divider-box">
            <hr class="custom-divider" />
          </div>

          <!-- 6. STANDARD FIELD / SPECIALIZED FORM WIDGETS -->
          <div v-else class="widget-field-box">
            <div class="widget-box-header">
              <span class="widget-box-title">
                <va-icon :name="getWidgetIcon(widget.type)" size="14px" color="primary" class="mr-1" />
                {{ getWidgetTitle(widget) }}
                <span v-if="isFieldRequired(widget)" class="required-star">*</span>
              </span>
            </div>

            <div class="field-control-wrapper">
              <!-- (1) DATE Input -->
              <va-input
                v-if="getFieldType(widget) === 'DATE'"
                :model-value="formatDateValue(record[widget.fieldKey])"
                @update:model-value="(val) => setFieldValue(widget.fieldKey, val)"
                :type="isEditing ? 'date' : 'text'"
                class="w-full"
                :readonly="!isEditing || widget.options?.readOnly"
                :placeholder="$t('date_placeholder')"
              >
                <template #prependInner>
                  <va-icon name="calendar_today" size="small" color="primary" />
                </template>
              </va-input>

              <!-- (2) DATETIME Input -->
              <va-input
                v-else-if="getFieldType(widget) === 'DATETIME'"
                :model-value="record[widget.fieldKey]"
                @update:model-value="(val) => setFieldValue(widget.fieldKey, val)"
                :type="isEditing ? 'datetime-local' : 'text'"
                class="w-full"
                :readonly="!isEditing || widget.options?.readOnly"
              >
                <template #prependInner>
                  <va-icon name="schedule" size="small" color="primary" />
                </template>
              </va-input>

              <!-- (3) NUMBER / INTEGER / DECIMAL / FLOAT -->
              <va-input
                v-else-if="['NUMBER', 'INTEGER', 'DECIMAL', 'FLOAT'].includes(getFieldType(widget))"
                :model-value="record[widget.fieldKey]"
                @update:model-value="(val) => setFieldValue(widget.fieldKey, val)"
                type="number"
                class="w-full"
                :readonly="!isEditing || widget.options?.readOnly"
              >
                <template #prependInner>
                  <va-icon name="pin" size="small" color="primary" />
                </template>
              </va-input>

              <!-- (4) SELECT / MULTI_SELECT / CODE / ENUM -->
              <va-select
                v-else-if="['SELECT', 'MULTI_SELECT', 'CODE', 'ENUM'].includes(getFieldType(widget))"
                :model-value="record[widget.fieldKey]"
                @update:model-value="(val) => setFieldValue(widget.fieldKey, val)"
                :options="getFieldOptions(getFieldDefinition(widget.fieldKey))"
                :multiple="getFieldType(widget) === 'MULTI_SELECT' || getFieldDefinition(widget.fieldKey)?.isMultiValue"
                class="w-full"
                :readonly="!isEditing || widget.options?.readOnly"
                clearable
              />

              <!-- (5) BOOLEAN Switch -->
              <div v-else-if="getFieldType(widget) === 'BOOLEAN'" class="boolean-control-wrapper">
                <va-switch
                  :model-value="!!record[widget.fieldKey]"
                  @update:model-value="(val) => setFieldValue(widget.fieldKey, val)"
                  :readonly="!isEditing || widget.options?.readOnly"
                  size="small"
                />
                <span class="boolean-label">{{ record[widget.fieldKey] ? $t('yes') : $t('no') }}</span>
              </div>

              <!-- (6) DOMAIN_REF / DOMAIN_REFERENCE -->
              <div v-else-if="['DOMAIN_REF', 'DOMAIN_REFERENCE'].includes(getFieldType(widget))" class="domain-ref-display w-full">
                <va-input
                  :model-value="formatDomainRef(record[widget.fieldKey])"
                  readonly
                  class="w-full"
                >
                  <template #appendInner v-if="isEditing">
                    <va-button size="small" preset="plain" icon="search" @click="$emit('openDomainRef', getFieldDefinition(widget.fieldKey))" />
                  </template>
                </va-input>
              </div>

              <!-- (7) MULTILINGUAL Input (KO / EN) -->
              <div v-else-if="getFieldType(widget) === 'MULTILINGUAL'" class="multilingual-control-box w-full">
                <div style="display: flex; gap: 0.5rem; width: 100%;">
                  <va-input
                    :model-value="getMultilingualValue(record[widget.fieldKey], 'ko')"
                    @update:model-value="(val) => setMultilingualValue(widget.fieldKey, 'ko', val)"
                    :readonly="!isEditing || widget.options?.readOnly"
                    style="flex: 1;"
                    size="small"
                  >
                    <template #prependInner><span class="lang-tag">KO</span></template>
                  </va-input>
                  <va-input
                    :model-value="getMultilingualValue(record[widget.fieldKey], 'en')"
                    @update:model-value="(val) => setMultilingualValue(widget.fieldKey, 'en', val)"
                    :readonly="!isEditing || widget.options?.readOnly"
                    style="flex: 1;"
                    size="small"
                  >
                    <template #prependInner><span class="lang-tag">EN</span></template>
                  </va-input>
                </div>
              </div>

              <!-- (8) FILE Upload & List -->
              <div v-else-if="getFieldType(widget) === 'FILE'" class="file-control-box w-full">
                <div v-if="!isEditing" class="file-chips-list">
                  <template v-if="getFilesList(record[widget.fieldKey]).length > 0">
                    <va-chip
                      v-for="(fObj, fIdx) in getFilesList(record[widget.fieldKey])"
                      :key="fIdx"
                      size="small"
                      outline
                      icon="download"
                      color="primary"
                      style="cursor: pointer;"
                    >
                      {{ fObj.name || fObj }}
                    </va-chip>
                  </template>
                  <span v-else class="empty-text">{{ $t('no_file_data') }}</span>
                </div>
                <div v-else class="file-uploader-box">
                  <va-file-upload
                    :model-value="[]"
                    @update:model-value="handleFilesUpload(widget.fieldKey, $event)"
                    dropzone
                    class="w-full"
                  />
                </div>
              </div>

              <!-- (9) TABLE / JSON SUBTABLE GRID -->
              <div v-else-if="isTableField(widget)" class="subtable-container w-full">
                <div class="subtable-toolbar">
                  <div class="subtable-title">
                    <va-badge :text="getTableRows(record[widget.fieldKey]).length + ' ' + $t('rows')" color="info" size="small" />
                  </div>
                  <div v-if="isEditing && !widget.options?.readOnly" class="subtable-actions">
                    <va-button size="small" icon="add" @click="addTableRow(widget.fieldKey, getTableColumns(getFieldDefinition(widget.fieldKey)))">
                      {{ $t('add_row') }}
                    </va-button>
                    <va-button
                      v-if="getTableRows(record[widget.fieldKey]).length > 0"
                      preset="secondary"
                      color="danger"
                      size="small"
                      icon="delete_sweep"
                      @click="clearTableRows(widget.fieldKey)"
                    >
                      {{ $t('clear_all_rows') }}
                    </va-button>
                  </div>
                </div>

                <!-- Empty State -->
                <div v-if="getTableRows(record[widget.fieldKey]).length === 0" class="empty-subtable-prompt">
                  <va-icon name="table_rows" size="medium" color="secondary" />
                  <span>{{ $t('empty_table_data') }}</span>
                </div>

                <!-- Table Content -->
                <div v-else class="subtable-scroll-wrapper">
                  <table class="rendered-subtable">
                    <thead>
                      <tr>
                        <th style="width: 32px; text-align: center;">#</th>
                        <th v-for="col in getTableColumns(getFieldDefinition(widget.fieldKey), record[widget.fieldKey])" :key="col.key">
                          {{ getTranslatedColName(col.name) || col.key }}
                        </th>
                        <th v-if="isEditing && !widget.options?.readOnly" style="width: 40px;"></th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(row, rIdx) in getTableRows(record[widget.fieldKey])" :key="rIdx">
                        <td style="text-align: center; color: var(--va-text-secondary); font-size: 0.75rem;">{{ rIdx + 1 }}</td>
                        <td v-for="col in getTableColumns(getFieldDefinition(widget.fieldKey), record[widget.fieldKey])" :key="col.key">
                          <va-input
                            v-if="isEditing && !widget.options?.readOnly"
                            v-model="row[col.key]"
                            size="small"
                            class="subtable-cell-input"
                          />
                          <span v-else class="subtable-cell-text">{{ row[col.key] ?? '-' }}</span>
                        </td>
                        <td v-if="isEditing && !widget.options?.readOnly" style="text-align: center;">
                          <va-button preset="plain" color="danger" icon="delete" size="small" @click="deleteTableRow(widget.fieldKey, rIdx)" />
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              <!-- (10) LONG_TEXT / TEXTAREA -->
              <va-textarea
                v-else-if="['LONG_TEXT', 'TEXTAREA'].includes(getFieldType(widget))"
                :model-value="record[widget.fieldKey]"
                @update:model-value="(val) => setFieldValue(widget.fieldKey, val)"
                class="w-full"
                :readonly="!isEditing || widget.options?.readOnly"
                :min-rows="2"
                :max-rows="6"
              />

              <!-- (11) CALCULATED (Read-only styled) -->
              <va-input
                v-else-if="getFieldType(widget) === 'CALCULATED'"
                :model-value="record[widget.fieldKey]"
                readonly
                class="w-full calculated-input"
              >
                <template #prependInner>
                  <va-icon name="functions" size="small" color="primary" />
                </template>
              </va-input>

              <!-- (12) DEFAULT TEXT -->
              <va-input
                v-else
                :model-value="record[widget.fieldKey]"
                @update:model-value="(val) => setFieldValue(widget.fieldKey, val)"
                type="text"
                class="w-full"
                :readonly="!isEditing || widget.options?.readOnly"
              />
            </div>
          </div>
        </template>
      </div>
    </template>

    <!-- Image Lightbox Modal for Zoom/Pan -->
    <ImageLightboxModal
      v-model="showLightbox"
      :images="lightboxImages"
      :initial-index="lightboxIndex"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import HtmlEditor from '~/components/common/HtmlEditor.vue'
import ImageUploader from '~/components/common/ImageUploader.vue'
import ImageLightboxModal from '~/components/common/ImageLightboxModal.vue'

const props = defineProps({
  layoutConfig: {
    type: Object,
    default: () => ({ cols: 12, rowHeight: 44, widgets: [] })
  },
  fields: {
    type: Array,
    default: () => []
  },
  record: {
    type: Object,
    default: () => ({})
  },
  isEditing: {
    type: Boolean,
    default: false
  },
  selectedDomainInfo: {
    type: Object,
    default: null
  },
  domainReferences: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:record', 'openDomainRef', 'requestDecrypt'])

const { t, locale } = useI18n()

const showLightbox = ref(false)
const lightboxImages = ref<{ url: string; name?: string }[]>([])
const lightboxIndex = ref(0)

const parseImagesList = (val: any): { url: string; name?: string }[] => {
  if (!val) return []
  if (Array.isArray(val)) {
    return val.map((item, idx) => {
      if (typeof item === 'string') return { url: getImageUrl(item), name: `Image ${idx + 1}` }
      return {
        url: getImageUrl(item.url || item.path || item.downloadUrl || item.filePath || item.id),
        name: item.name || item.fileName || `Image ${idx + 1}`
      }
    }).filter(i => !!i.url)
  }
  if (typeof val === 'object') {
    const u = val.url || val.path || val.downloadUrl || val.filePath || val.id
    if (u) {
      return [{
        url: getImageUrl(u),
        name: val.name || val.fileName || 'Image'
      }]
    }
  }
  if (typeof val === 'string') {
    const trimmed = val.trim()
    if (trimmed.startsWith('[') || trimmed.startsWith('{')) {
      try {
        const parsed = JSON.parse(trimmed)
        return parseImagesList(parsed)
      } catch (e) {}
    }
    return [{ url: getImageUrl(val), name: 'Image' }]
  }
  return []
}

const openImageLightbox = (val: any, title: string = 'Image') => {
  const list = parseImagesList(val)
  if (list.length === 0) return
  if (title && list.length === 1) {
    list[0].name = title
  }
  lightboxImages.value = list
  lightboxIndex.value = 0
  showLightbox.value = true
}

const getWidgetIcon = (type: string) => {
  switch (type) {
    case 'IMAGE': return 'image'
    case 'EDITOR': return 'edit_note'
    case 'SECTION': return 'folder_open'
    case 'CALLOUT': return 'info'
    case 'DIVIDER': return 'horizontal_rule'
    case 'TABLE':
    case 'JSON': return 'table_rows'
    default: return 'widgets'
  }
}

const cols = computed(() => props.layoutConfig?.cols || 12)
const rowHeight = computed(() => props.layoutConfig?.rowHeight || 42)

const validWidgets = computed(() => {
  if (!props.layoutConfig || !Array.isArray(props.layoutConfig.widgets)) return []
  return props.layoutConfig.widgets
})

const gridContainerStyle = computed(() => ({
  display: 'grid',
  gridTemplateColumns: `repeat(${cols.value}, minmax(0, 1fr))`,
  gridAutoRows: `${rowHeight.value}px`,
  gap: '8px',
  width: '100%',
  boxSizing: 'border-box' as const
}))

const getWidgetStyle = (widget: any) => {
  const x = Math.max(0, Number(widget.x) || 0)
  const y = Math.max(0, Number(widget.y) || 0)
  const w = Math.min(cols.value, Math.max(1, Number(widget.w) || 1))
  const h = Math.max(1, Number(widget.h) || 1)

  return {
    gridColumn: `${x + 1} / span ${w}`,
    gridRow: `${y + 1} / span ${h}`,
    minHeight: `${h * rowHeight.value}px`
  }
}

const getImageContentHeight = (widget: any) => {
  const h = Math.max(1, Number(widget.h) || 4)
  return `${h * rowHeight.value - 32}px`
}

const getEditorContentHeight = (widget: any) => {
  const h = Math.max(1, Number(widget.h) || 7)
  return `${h * rowHeight.value - 36}px`
}

const getFieldDefinition = (fieldKey: string) => {
  return props.fields.find((f: any) => f.key === fieldKey)
}

const getFieldType = (widget: any) => {
  const field: any = getFieldDefinition(widget.fieldKey)
  if (field && field.type) return field.type
  return widget.type || 'TEXT'
}

const isFieldType = (widget: any, type: string) => {
  return getFieldType(widget) === type
}

const getWidgetTitle = (widget: any) => {
  if (widget.title && typeof widget.title === 'object') {
    return widget.title[locale.value] || widget.title.ko || widget.title.en || widget.fieldKey || ''
  }
  if (widget.title && typeof widget.title === 'string') {
    return widget.title
  }
  if (widget.fieldKey) {
    const field: any = getFieldDefinition(widget.fieldKey)
    if (field && field.name) {
      if (typeof field.name === 'object') {
        return field.name[locale.value] || field.name.ko || field.name.en || widget.fieldKey
      }
      return field.name
    }
    return widget.fieldKey
  }
  return ''
}

const isFieldRequired = (widget: any) => {
  if (widget.options?.required) return true
  const field: any = getFieldDefinition(widget.fieldKey)
  return field?.required || false
}

const getImageUrl = (val: any): string => {
  if (!val) return ''
  if (typeof val === 'object') {
    if (val.url) return getImageUrl(val.url)
    if (val.downloadUrl) return getImageUrl(val.downloadUrl)
    if (val.filePath) return `/api/files/download/${encodeURIComponent(val.filePath)}`
    if (val.id) return `/api/files/download/${encodeURIComponent(val.id)}`
  }
  if (typeof val === 'string') {
    const trimmed = val.trim()
    if (trimmed.startsWith('{') || trimmed.startsWith('[')) {
      try {
        const parsed = JSON.parse(trimmed)
        if (Array.isArray(parsed) && parsed.length > 0) return getImageUrl(parsed[0])
        if (typeof parsed === 'object') return getImageUrl(parsed)
      } catch (e) {}
    }
    if (trimmed.startsWith('http') || trimmed.startsWith('data:') || trimmed.startsWith('/')) {
      return trimmed
    }
    return `/api/files/download/${encodeURIComponent(trimmed)}`
  }
  return String(val)
}

const formatDateValue = (val: any) => {
  if (!val) return ''
  if (typeof val === 'string' && val.includes('T')) {
    return val.split('T')[0]
  }
  return String(val)
}

const setFieldValue = (fieldKey: string, val: any) => {
  props.record[fieldKey] = val
}

const getMultilingualValue = (obj: any, lang: string) => {
  if (!obj || typeof obj !== 'object') return ''
  return obj[lang] || ''
}

const setMultilingualValue = (fieldKey: string, lang: string, val: string) => {
  if (!props.record[fieldKey] || typeof props.record[fieldKey] !== 'object') {
    props.record[fieldKey] = { ko: '', en: '' }
  }
  props.record[fieldKey][lang] = val
}

const getFieldOptions = (field: any) => {
  if (!field || !field.options) return []
  if (Array.isArray(field.options)) return field.options
  try {
    const parsed = JSON.parse(field.options)
    if (Array.isArray(parsed)) return parsed
  } catch (e) {
    return field.options.split(',').map((s: string) => ({ text: s.trim(), value: s.trim() }))
  }
  return []
}

const formatDomainRef = (val: any) => {
  if (!val) return ''
  if (typeof val === 'object') {
    return val.name || val.label || val.id || ''
  }
  return String(val)
}

const getFilesList = (val: any) => {
  if (!val) return []
  if (Array.isArray(val)) return val
  try {
    const parsed = JSON.parse(val)
    if (Array.isArray(parsed)) return parsed
  } catch (e) {
    return [val]
  }
  return [val]
}

const handleFilesUpload = (fieldKey: string, files: any) => {
  props.record[fieldKey] = files
}

// Table / JSON Subtable Helpers
const getTableColumns = (field: any, rows?: any) => {
  if (field && field.options) {
    try {
      const opts = typeof field.options === 'string' ? JSON.parse(field.options) : field.options
      if (opts && opts.tableSchema && Array.isArray(opts.tableSchema.columns) && opts.tableSchema.columns.length > 0) {
        return opts.tableSchema.columns
      }
      if (opts && Array.isArray(opts.columns) && opts.columns.length > 0) {
        return opts.columns
      }
    } catch (e) {}
  }
  const parsedRows = getTableRows(rows)
  if (Array.isArray(parsedRows) && parsedRows.length > 0 && typeof parsedRows[0] === 'object' && parsedRows[0] !== null) {
    return Object.keys(parsedRows[0]).map((k) => ({
      key: k,
      name: { ko: k, en: k }
    }))
  }
  return [
    { key: 'col1', name: { ko: '항목 1', en: 'Item 1' } },
    { key: 'col2', name: { ko: '항목 2', en: 'Item 2' } }
  ]
}

const getTableRows = (val: any) => {
  if (!val) return []
  if (Array.isArray(val)) return val
  try {
    const parsed = JSON.parse(val)
    if (Array.isArray(parsed)) return parsed
  } catch (e) {}
  return []
}

const isTableField = (widget: any) => {
  const type = getFieldType(widget)
  const field: any = getFieldDefinition(widget.fieldKey)
  return type === 'TABLE' || type === 'JSON' || field?.isTable || (field?.options && String(field.options).includes('tableSchema'))
}

const getTranslatedColName = (name: any) => {
  if (!name) return ''
  if (typeof name === 'object') {
    return name[locale.value] || name.ko || name.en || ''
  }
  return String(name)
}

const getColSelectOptions = (options: any) => {
  if (!options || !Array.isArray(options)) return []
  return options.map((opt: any) => {
    if (typeof opt === 'object' && opt !== null) {
      const key = opt.key || opt.value || ''
      const label = (opt.label && typeof opt.label === 'object') ? (opt.label[locale.value] || opt.label.ko || opt.label.en) : (opt.label || opt.text || key)
      return { text: label, value: key }
    }
    if (typeof opt === 'string' && opt.includes(':')) {
      const parts = opt.split(':').map((s: string) => s.trim())
      const key = parts[0]
      const label = locale.value === 'en' ? (parts[2] || parts[1] || key) : (parts[1] || key)
      return { text: label, value: key }
    }
    return { text: String(opt), value: opt }
  })
}

const getColSelectDisplayValue = (options: any, val: any) => {
  if (val === undefined || val === null || val === '') return '-'
  const opts = getColSelectOptions(options)
  const found = opts.find((o: any) => o.value === val)
  return found ? found.text : String(val)
}

const addTableRow = (fieldKey: string, columns: any[]) => {
  if (!Array.isArray(props.record[fieldKey])) {
    props.record[fieldKey] = []
  }
  const newRow: Record<string, any> = {}
  columns.forEach((c: any) => {
    newRow[c.key] = ''
  })
  props.record[fieldKey].push(newRow)
}

const deleteTableRow = (fieldKey: string, index: number) => {
  if (Array.isArray(props.record[fieldKey])) {
    props.record[fieldKey].splice(index, 1)
  }
}

const clearTableRows = (fieldKey: string) => {
  props.record[fieldKey] = []
}
</script>


<style scoped>
.dynamic-layout-container {
  padding: 8px;
  background: var(--va-background-secondary, #0f172a);
  border-radius: 8px;
  min-height: 200px;
  box-sizing: border-box;
}

.layout-widget-item {
  background: var(--va-background-element, #141b2d);
  border: 1px solid var(--va-background-border, #334155);
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  position: relative;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  box-sizing: border-box;
  overflow: hidden;
  padding: 0;
}

.layout-widget-item.is-highlighted {
  border-color: var(--va-primary, #154ec1);
  box-shadow: 0 0 0 2px rgba(21, 78, 193, 0.4);
}

/* 1. Single-Row Compact Mode (h === 1) */
.layout-widget-item.is-single-row {
  justify-content: center;
}

.widget-single-row-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 100%;
  padding: 0 10px;
  gap: 8px;
  box-sizing: border-box;
}

.single-row-left {
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
  flex-shrink: 0;
}

.single-row-label {
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--va-text-secondary, #94a3b8);
}

.single-row-center {
  flex: 1;
  display: flex;
  align-items: center;
  overflow: hidden;
  min-width: 0;
  justify-content: flex-end;
}

.single-row-val-box {
  display: flex;
  align-items: center;
  width: 100%;
}

.single-row-input {
  width: 100%;
  --va-input-font-size: 0.85rem;
}

.sample-val-text {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--va-text-primary, #f8fafc);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sample-chip-tag {
  background: var(--va-background-border, #334155);
  color: #94a3b8;
  font-size: 0.65rem;
  font-weight: 800;
  padding: 1px 4px;
  border-radius: 3px;
  margin-right: 4px;
}

.single-row-bool, .single-row-multi, .single-row-file {
  display: flex;
  align-items: center;
  gap: 6px;
}

.single-row-section {
  color: var(--va-primary, #154ec1);
  font-size: 0.88rem;
  font-weight: 700;
}

.canvas-divider-hr {
  width: 100%;
  border: none;
  border-top: 1px solid var(--va-background-border, #334155);
  margin: 0;
}

/* 2. Multi-Row Mode (h >= 2) */
.widget-box-header {
  display: flex;
  align-items: center;
  padding: 5px 10px;
  background: rgba(0, 0, 0, 0.2);
  border-bottom: 1px solid var(--va-background-border, #334155);
  flex-shrink: 0;
}

.widget-box-title {
  font-size: 0.78rem;
  font-weight: 700;
  color: var(--va-text-secondary, #94a3b8);
  display: flex;
  align-items: center;
  text-transform: uppercase;
}

.required-star {
  color: var(--va-danger, #ef4444);
  margin-left: 2px;
  font-weight: bold;
}

.field-control-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 6px 10px;
  box-sizing: border-box;
  min-height: 0;
  width: 100%;
}

.widget-field-box {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
}

/* Image Widget */
.widget-image-box {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
}

.widget-image-content {
  flex: 1;
  display: flex;
  position: relative;
  overflow: hidden;
  width: 100%;
  background: rgba(0, 0, 0, 0.15);
}

.image-view-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.preview-img {
  width: 100%;
  height: 100%;
  display: block;
}

.image-zoom-overlay {
  position: absolute;
  bottom: 6px;
  right: 6px;
  background: rgba(0, 0, 0, 0.65);
  backdrop-filter: blur(4px);
  color: #fff;
  font-size: 0.72rem;
  padding: 3px 8px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.image-view-wrapper:hover .image-zoom-overlay {
  opacity: 1;
}

.empty-image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: var(--va-text-secondary, #94a3b8);
}

.empty-text {
  font-size: 0.78rem;
  margin-top: 4px;
}

/* Editor Widget */
.widget-editor-box {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
}

.widget-editor-content {
  flex: 1;
  overflow: hidden;
  padding: 4px 8px;
}

/* Section Widget */
.widget-section-box {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 10px;
}

.section-title-bar {
  display: flex;
  align-items: center;
  border-bottom: 2px solid var(--va-primary, #154ec1);
  width: 100%;
  padding-bottom: 2px;
}

.section-heading {
  margin: 0;
  font-size: 0.92rem;
  font-weight: 800;
  color: var(--va-text-primary, #f8fafc);
}

/* Callout Widget */
.widget-callout-box {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
}

.callout-info {
  background: rgba(21, 78, 193, 0.15);
  color: var(--va-primary, #3b82f6);
  border-left: 4px solid var(--va-primary, #3b82f6);
}

.callout-warning {
  background: rgba(245, 158, 11, 0.15);
  color: #f59e0b;
  border-left: 4px solid #f59e0b;
}

.callout-danger {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
  border-left: 4px solid #ef4444;
}

/* Subtable */
.subtable-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.subtable-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.subtable-scroll-wrapper {
  flex: 1;
  overflow: auto;
  border: 1px solid var(--va-background-border, #334155);
  border-radius: 4px;
}

.rendered-subtable {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.82rem;
}

.rendered-subtable th {
  background: var(--va-background-element, #1e2640);
  color: var(--va-text-secondary, #94a3b8);
  font-weight: 700;
  padding: 5px 8px;
  border-bottom: 1px solid var(--va-background-border, #334155);
  text-align: left;
}

.rendered-subtable td {
  padding: 4px 8px;
  border-bottom: 1px solid var(--va-background-border, #334155);
  color: var(--va-text-primary, #f8fafc);
}

.empty-subtable-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
  color: var(--va-text-secondary, #94a3b8);
  font-size: 0.82rem;
}
</style>
