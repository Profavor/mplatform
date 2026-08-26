<template>
  <AppModal
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="isEditMode ? $t('edit_domain') : $t('create_new_domain')"
    icon="domain"
    hide-default-actions
    size="large"
  >
    <!-- Creation Mode Tabs: General Domain vs Specialized Templates -->
    <div v-if="!isEditMode" class="domain-modal-tabs mb-4">
      <va-tabs v-model="activeTab" color="primary">
        <template #tabs>
          <va-tab name="general">
            <va-icon name="create_new_folder" class="mr-2" size="small" />
            {{ $t('general_domain') }}
          </va-tab>
          <va-tab name="specialized">
            <va-icon name="auto_awesome" class="mr-2" size="small" />
            {{ $t('specialized_templates') }}
          </va-tab>
        </template>
      </va-tabs>
    </div>

    <!-- TAB 1: General Domain Form -->
    <div v-if="isEditMode || activeTab === 'general'" style="padding: 0.5rem 0;">
      <div style="display: flex; gap: 1rem;">
        <va-input v-model="newDomain.name.ko" :label="`${$t('domain_name')} (KO)`" class="mb-4" style="flex: 1; min-width: 0;" />
        <va-input v-model="newDomain.name.en" :label="`${$t('domain_name')} (EN)`" class="mb-4" style="flex: 1; min-width: 0;" />
      </div>
      <div style="display: flex; gap: 1rem;">
        <va-input v-model="newDomain.description.ko" :label="`${$t('description')} (KO)`" class="mb-4" style="flex: 1; min-width: 0;" />
        <va-input v-model="newDomain.description.en" :label="`${$t('description')} (EN)`" class="mb-4" style="flex: 1; min-width: 0;" />
      </div>
      <div class="mb-4">
        <label style="font-weight: bold; margin-bottom: 0.5rem; display: block; font-size: 0.9rem; color: var(--va-text-primary);">
          {{ $t('domain_icon') }}
        </label>
        <div style="display: flex; align-items: center; gap: 1rem;">
          <va-icon :name="newDomain.icon || 'folder'" size="large" color="primary" />
          <va-button size="small" preset="secondary" border-color="primary" @click="$emit('open-icon-picker', true)">
            {{ $t('select_icon') }}
          </va-button>
        </div>
      </div>
      <va-input
        v-model="newDomain.numberingPattern"
        :label="$t('pattern_preview')"
        placeholder="e.g. ITEM-{YYYY}-{SEQ:5}"
        class="mb-4 w-full"
      />
      <va-input v-model="newDomain.sortOrder" type="number" :label="$t('node_order')" class="mb-4 w-full" />
      <va-switch v-model="newDomain.autoDqScanEnabled" :label="$t('auto_dq_scan_label')" class="mb-4 w-full" color="primary" />
      
      <!-- Field Mappings (Edit mode) -->
      <div v-if="isEditMode" style="margin-top: 1rem; border-top: 1px solid var(--va-background-border); padding-top: 1rem;">
        <div style="margin-bottom: 0.75rem; font-weight: bold; font-size: 0.9rem; color: var(--va-text-secondary);">
          {{ $t('domain_field_mappings') }}
        </div>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 0.75rem;">
          <va-select
            v-model="newDomain.identifierFieldId"
            :options="domainFieldOptions.filter(o => o.type === 'TEXT')"
            value-by="value"
            text-by="text"
            :label="`${$t('id_attribute')}*`"
            class="w-full"
            :error="mappingError?.id"
            :error-messages="[$t('required')]"
          />
          <va-select
            v-model="newDomain.displayNameFieldId"
            :options="domainFieldOptions.filter(o => o.type === 'MULTILINGUAL' || o.type === 'MULTILINGUAL_TEXT' || o.type === 'TEXT')"
            value-by="value"
            text-by="text"
            :label="`${$t('name_attribute')}*`"
            class="w-full"
            :error="mappingError?.name"
            :error-messages="[$t('required')]"
          />
          <va-select
            v-model="newDomain.descriptionFieldId"
            :options="domainFieldOptions.filter(o => o.type === 'MULTILINGUAL' || o.type === 'MULTILINGUAL_TEXT' || o.type === 'TEXT')"
            value-by="value"
            text-by="text"
            :label="$t('description')"
            class="w-full"
            clearable
          />
          <va-select
            v-model="newDomain.imageFieldId"
            :options="domainFieldOptions.filter(o => o.type === 'IMAGE' || o.type === 'IMAGE_FILE')"
            value-by="value"
            text-by="text"
            label="Image"
            class="w-full"
            clearable
          />
        </div>
      </div>
    </div>

    <!-- TAB 2: Specialized Domain Templates -->
    <div v-else-if="activeTab === 'specialized'" class="specialized-templates-view">
      <div class="template-guide-box mb-4">
        <va-icon name="info" color="info" size="small" class="mr-2" />
        <span class="guide-text">{{ $t('specialized_templates_desc') }}</span>
      </div>

      <!-- 6 Template Cards Grid -->
      <div class="templates-grid mb-4">
        <div
          v-for="tpl in templateList"
          :key="tpl.category"
          class="template-card"
          :class="{ selected: selectedTemplate?.category === tpl.category }"
          @click="selectTemplate(tpl)"
        >
          <div class="template-card-header">
            <div class="tpl-icon-box">
              <va-icon :name="tpl.icon" size="24px" color="primary" />
            </div>
            <va-badge :text="getCategoryLabel(tpl.category)" color="primary" size="small" />
          </div>
          <div class="template-card-title">{{ getTranslatedText(tpl.name) }}</div>
          <div class="template-card-desc">{{ getTranslatedText(tpl.description) }}</div>
          <div class="template-card-footer">
            <span class="footer-chip">
              <va-icon name="format_list_bulleted" size="14px" class="mr-1" />
              {{ $t('fields_count', { count: tpl.fields?.length || 0 }) }}
            </span>
            <span class="footer-chip">
              <va-icon name="verified" size="14px" class="mr-1" />
              {{ $t('dq_rules_count', { count: tpl.dqRules?.length || 0 }) }}
            </span>
          </div>
        </div>
      </div>

      <!-- Selected Template Customization & Preview Panel -->
      <div v-if="selectedTemplate" class="template-custom-panel">
        <div class="panel-header">
          <va-icon :name="selectedTemplate.icon" color="primary" size="small" class="mr-2" />
          <span class="panel-title">{{ getTranslatedText(selectedTemplate.name) }} - {{ $t('template_provisioning') }}</span>
        </div>

        <div class="panel-body">
          <div style="display: flex; gap: 1rem;" class="mb-3">
            <va-input v-model="tplForm.name.ko" :label="`${$t('domain_name')} (KO)`" style="flex: 1;" />
            <va-input v-model="tplForm.name.en" :label="`${$t('domain_name')} (EN)`" style="flex: 1;" />
          </div>
          <div style="display: flex; gap: 1rem;" class="mb-3">
            <va-input v-model="tplForm.description.ko" :label="`${$t('description')} (KO)`" style="flex: 1;" />
            <va-input v-model="tplForm.description.en" :label="`${$t('description')} (EN)`" style="flex: 1;" />
          </div>
          <va-input
            v-model="tplForm.numberingPattern"
            :label="$t('pattern_preview')"
            class="mb-3 w-full"
          />

          <!-- Standard Fields Chips -->
          <div class="fields-preview-box">
            <span class="box-label">{{ $t('fields_count', { count: selectedTemplate.fields?.length || 0 }) }}:</span>
            <div class="fields-chips-list">
              <va-chip
                v-for="f in selectedTemplate.fields"
                :key="f.key"
                size="small"
                preset="outline"
                :color="f.key === selectedTemplate.identifierFieldKey ? 'primary' : 'secondary'"
              >
                {{ getTranslatedText(f.name) }} ({{ f.key }})
              </va-chip>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer Actions -->
    <template #footer>
      <div style="display: flex; justify-content: flex-end; gap: 0.5rem;">
        <va-button preset="secondary" @click="$emit('update:modelValue', false)">
          {{ $t('btn_close') }}
        </va-button>

        <va-button
          v-if="isEditMode || activeTab === 'general'"
          color="primary"
          @click="$emit('save')"
        >
          {{ isEditMode ? $t('btn_save') : $t('create') }}
        </va-button>

        <va-button
          v-else-if="activeTab === 'specialized'"
          color="primary"
          icon="auto_awesome"
          :disabled="!selectedTemplate"
          :loading="provisioning"
          @click="submitProvision"
        >
          {{ $t('btn_create_from_template') }}
        </va-button>
      </div>
    </template>
  </AppModal>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import AppModal from '~/components/common/AppModal.vue'
import { useCustomFetch } from '~/composables/useCustomFetch'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  isEditMode: {
    type: Boolean,
    default: false
  },
  newDomain: {
    type: Object,
    required: true
  },
  domainFieldOptions: {
    type: Array,
    default: () => []
  },
  mappingError: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'save', 'open-icon-picker', 'provision-success'])

const { t, locale } = useI18n()
const { customFetch } = useCustomFetch()

const activeTab = ref('general')
const templateList = ref([])
const selectedTemplate = ref(null)
const provisioning = ref(false)

const tplForm = ref({
  category: '',
  name: { ko: '', en: '' },
  description: { ko: '', en: '' },
  numberingPattern: '',
  icon: ''
})

const getTranslatedText = (val) => {
  if (!val) return ''
  if (typeof val === 'string') return val
  return val[locale.value] || val['ko'] || val['en'] || Object.values(val)[0] || ''
}

const getCategoryLabel = (category) => {
  const map = {
    CUSTOMER: 'category_customer',
    VENDOR: 'category_vendor',
    PRODUCT: 'category_product',
    MATERIAL: 'category_material',
    EMPLOYEE: 'category_employee',
    STOCK: 'category_stock'
  }
  const key = map[category] || category
  return t(key)
}

const fetchTemplates = async () => {
  try {
    const res = await customFetch('/api/domains/specialized-templates')
    if (Array.isArray(res)) {
      templateList.value = res
      if (res.length > 0 && !selectedTemplate.value) {
        selectTemplate(res[0])
      }
    }
  } catch (err) {
    console.error('Failed to fetch specialized templates:', err)
  }
}

const selectTemplate = (tpl) => {
  selectedTemplate.value = tpl
  tplForm.value = {
    category: tpl.category,
    name: { ko: tpl.name?.ko || '', en: tpl.name?.en || '' },
    description: { ko: tpl.description?.ko || '', en: tpl.description?.en || '' },
    numberingPattern: tpl.numberingPattern || '',
    icon: tpl.icon || ''
  }
}

const submitProvision = async () => {
  if (!selectedTemplate.value) return
  provisioning.value = true
  try {
    const res = await customFetch('/api/domains/specialized-provision', {
      method: 'POST',
      body: tplForm.value
    })
    emit('update:modelValue', false)
    emit('provision-success', res)
  } catch (err) {
    console.error('Provisioning failed:', err)
  } finally {
    provisioning.value = false
  }
}

watch(() => props.modelValue, (newVal) => {
  if (newVal && !props.isEditMode) {
    activeTab.value = 'general'
    if (templateList.value.length === 0) {
      fetchTemplates()
    }
  }
})

onMounted(() => {
  if (!props.isEditMode) {
    fetchTemplates()
  }
})
</script>

<style scoped>
.domain-modal-tabs {
  border-bottom: 1px solid var(--va-background-border);
}
.template-guide-box {
  display: flex;
  align-items: center;
  padding: 0.75rem 1rem;
  background: var(--va-background-element);
  border-radius: 8px;
  font-size: 0.85rem;
  color: var(--va-text-secondary);
}
.templates-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 0.75rem;
  max-height: 280px;
  overflow-y: auto;
  padding: 2px;
}
.template-card {
  border: 1px solid var(--va-background-border);
  border-radius: 10px;
  padding: 0.75rem 1rem;
  background: var(--va-background-primary);
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
}
.template-card:hover {
  border-color: var(--va-primary);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}
.template-card.selected {
  border-color: var(--va-primary);
  background: rgba(var(--va-primary-rgb, 21, 78, 193), 0.05);
  box-shadow: 0 0 0 2px var(--va-primary);
}
.template-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}
.tpl-icon-box {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: var(--va-background-element);
  display: flex;
  align-items: center;
  justify-content: center;
}
.template-card-title {
  font-weight: 700;
  font-size: 0.95rem;
  color: var(--va-text-primary);
  margin-bottom: 0.25rem;
}
.template-card-desc {
  font-size: 0.75rem;
  color: var(--va-text-secondary);
  line-height: 1.3;
  margin-bottom: 0.75rem;
  flex: 1;
}
.template-card-footer {
  display: flex;
  gap: 0.5rem;
  font-size: 0.75rem;
  color: var(--va-text-secondary);
  border-top: 1px dashed var(--va-background-border);
  padding-top: 0.5rem;
}
.footer-chip {
  display: flex;
  align-items: center;
}
.template-custom-panel {
  border: 1px solid var(--va-background-border);
  border-radius: 8px;
  padding: 1rem;
  background: var(--va-background-primary);
}
.panel-header {
  display: flex;
  align-items: center;
  font-weight: 700;
  font-size: 0.95rem;
  margin-bottom: 0.75rem;
  color: var(--va-text-primary);
}
.fields-preview-box {
  padding: 0.75rem;
  background: var(--va-background-element);
  border-radius: 8px;
}
.box-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--va-text-secondary);
  display: block;
  margin-bottom: 0.5rem;
}
.fields-chips-list {
  display: flex;
  gap: 0.35rem;
  flex-wrap: wrap;
}
</style>
