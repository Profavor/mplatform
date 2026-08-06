<template>
  <va-card style="width: 100%; margin-bottom: 0.75rem;">
    <va-card-content style="padding: 0.75rem 1rem;">
      <div style="display: flex; gap: 0.75rem; align-items: center; flex-wrap: wrap;">
        <div style="font-weight: bold; min-width: 80px;">{{ $t('search_filters') }}</div>
        
        <div v-for="(filter, index) in searchFilters" :key="index" style="display: flex; gap: 0.5rem; align-items: center;">
          <va-select
            v-model="filter.fieldKey"
            :options="fieldOptions"
            value-by="value"
            text-by="text"
            style="width: 160px;"
            dense
          />
          <va-select
            v-model="filter.operator"
            :options="operatorOptions"
            value-by="value"
            text-by="text"
            style="width: 130px;"
            dense
          />
          <va-input
            v-model="filter.value"
            :placeholder="$t('search_keyword')"
            style="width: 180px;"
            dense
            @keyup.enter="$emit('apply')"
          />
          <va-button
            preset="plain"
            icon="close"
            color="danger"
            size="small"
            @click="$emit('removeFilter', index)"
          />
        </div>

        <va-button preset="secondary" icon="add" size="small" @click="$emit('addFilter')">
          {{ $t('add_filter') }}
        </va-button>
        
        <div style="margin-left: auto; display: flex; gap: 0.5rem;">
          <va-button preset="secondary" icon="restart_alt" @click="$emit('clear')">
            {{ $t('reset') }}
          </va-button>
          <va-button color="primary" icon="search" @click="$emit('apply')">
            {{ $t('search') }}
          </va-button>
        </div>
      </div>
    </va-card-content>
  </va-card>
</template>

<script setup lang="ts">
defineProps<{
  searchFilters: Array<{ fieldKey: string; operator: string; value: string }>
  fieldOptions: Array<{ value: string; text: string }>
  operatorOptions: Array<{ value: string; text: string }>
}>()

defineEmits(['addFilter', 'removeFilter', 'clear', 'apply'])
</script>

<style scoped>
:deep(.va-input-wrapper__container) {
  min-height: 32px;
}
</style>
