<template>
  <va-card class="mb-4" style="background-color: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px;">
    <va-card-content style="padding: 1.25rem;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; padding-bottom: 0.5rem; border-bottom: 1px dashed var(--va-background-border);">
        <div style="display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.5rem;">
          <va-icon name="tune" color="primary" size="18px" />
          <span style="font-weight: 700; font-size: 0.9rem; color: var(--va-primary);">{{ t('advanced_search_condition') }}</span>
        </div>
        <va-badge v-if="activeFilterCount > 0" :text="t('applied_filters_count', { count: activeFilterCount })" color="primary" />
      </div>

      <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 1.25rem; align-items: start;">
        <div v-for="field in searchableFields" :key="field.id" style="display: flex; flex-direction: column; gap: 0.4rem;">
          <span style="font-size: 0.75rem; color: var(--va-text-secondary); font-weight: 600; text-transform: uppercase;">
            {{ formatFieldName(field.name) }}
          </span>
          
          <va-select
            v-if="['SELECT', 'MULTI_SELECT'].includes(field.type)"
            :model-value="draftFilters[field.key]"
            :options="parseOptions(field.options)"
            value-by="value"
            :placeholder="t('op.select_option')"
            clearable
            class="w-full"
            @update:model-value="updateDraftFilter(field.key, $event)"
          />
          <va-select
            v-else-if="field.type === 'BOOLEAN'"
            :model-value="draftFilters[field.key]"
            :options="['true', 'false']"
            :placeholder="t('op.select_option')"
            clearable
            class="w-full"
            @update:model-value="updateDraftFilter(field.key, $event)"
          />
          <div v-else-if="['NUMBER', 'DECIMAL', 'FLOAT', 'INTEGER'].includes(field.type)" style="display: flex; flex-direction: column; gap: 0.4rem; width: 100%;">
            <va-input
              :model-value="draftFilters[field.key]"
              type="number"
              :placeholder="t('op.enter_number')"
              clearable
              class="w-full"
              @update:model-value="updateDraftFilter(field.key, $event)"
              @keydown="$emit('filterKeydown', $event)"
            >
              <template #prependInner>
                <select 
                  :value="draftFiltersOp[field.key] || 'EQ'" 
                  @change="updateDraftFilterOp(field.key, ($event.target as HTMLSelectElement).value)"
                  @click.stop
                  @mousedown.stop
                  style="border: none; outline: none; background: transparent; font-weight: bold; color: var(--va-primary); cursor: pointer; padding-right: 0.2rem; margin-right: 0.5rem; border-right: 1px solid var(--va-background-border); font-size: 0.85rem;"
                >
                  <option value="EQ">=</option>
                  <option value="GT">&gt;</option>
                  <option value="GTE">&gt;=</option>
                  <option value="LT">&lt;</option>
                  <option value="LTE">&lt;=</option>
                  <option value="BETWEEN">{{ t('op.range') }}</option>
                </select>
              </template>
            </va-input>
            <va-input
              v-if="draftFiltersOp[field.key] === 'BETWEEN'"
              :model-value="draftFiltersMax[field.key]"
              type="number"
              :placeholder="t('op.max_value')"
              clearable
              class="w-full"
              @update:model-value="updateDraftFilterMax(field.key, $event)"
              @keydown="$emit('filterKeydown', $event)"
            >
              <template #prependInner>
                <span style="font-weight: bold; color: #666; margin-right: 0.5rem; border-right: 1px solid #ccc; padding-right: 0.5rem; font-size: 0.8rem;">~ {{ t('op.below') }}</span>
              </template>
            </va-input>
          </div>
          <va-input
            v-else
            :model-value="draftFilters[field.key]"
            :placeholder="t('op.enter_keyword')"
            clearable
            class="w-full"
            @update:model-value="updateDraftFilter(field.key, $event)"
            @keydown="$emit('filterKeydown', $event)"
          >
            <template #prependInner>
              <select 
                :value="draftFiltersOp[field.key] || 'EQ'" 
                @change="updateDraftFilterOp(field.key, ($event.target as HTMLSelectElement).value)"
                @click.stop
                @mousedown.stop
                style="border: none; outline: none; background: transparent; font-weight: bold; color: var(--va-primary); cursor: pointer; padding-right: 0.2rem; margin-right: 0.5rem; border-right: 1px solid var(--va-background-border); font-size: 0.85rem;"
              >
                <option value="EQ">{{ t('op.eq') }}</option>
                <option value="CONTAINS">{{ t('op.contains') }}</option>
                <option value="STARTS_WITH">{{ t('op.starts_with') }}</option>
                <option value="ENDS_WITH">{{ t('op.ends_with') }}</option>
              </select>
            </template>
          </va-input>
        </div>
      </div>
    </va-card-content>
  </va-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { formatMultilingual } from '~/composables/useMultilingual'

const props = defineProps<{
  searchableFields: any[]
  draftFilters: Record<string, any>
  draftFiltersOp: Record<string, string>
  draftFiltersMax: Record<string, any>
  activeFilters: Record<string, any>
}>()

const emit = defineEmits<{
  (e: 'update:draftFilters', val: Record<string, any>): void
  (e: 'update:draftFiltersOp', val: Record<string, string>): void
  (e: 'update:draftFiltersMax', val: Record<string, any>): void
  (e: 'filterKeydown', event: KeyboardEvent): void
}>()

const { t } = useI18n()

const activeFilterCount = computed(() => {
  return Object.keys(props.activeFilters).filter(k => props.activeFilters[k]).length
})

const formatFieldName = (nameObj: any) => {
  if (!nameObj) return ''
  return formatMultilingual(nameObj)
}

const parseOptions = (optionsStr: any) => {
  if (!optionsStr) return []
  if (Array.isArray(optionsStr)) return optionsStr
  try {
    return JSON.parse(optionsStr)
  } catch {
    return []
  }
}

const updateDraftFilter = (key: string, val: any) => {
  emit('update:draftFilters', { ...props.draftFilters, [key]: val })
}

const updateDraftFilterOp = (key: string, op: string) => {
  emit('update:draftFiltersOp', { ...props.draftFiltersOp, [key]: op })
}

const updateDraftFilterMax = (key: string, val: any) => {
  emit('update:draftFiltersMax', { ...props.draftFiltersMax, [key]: val })
}
</script>
