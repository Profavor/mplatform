<template>
  <va-card outlined class="specialized-widget material-widget">
    <va-card-title class="widget-header">
      <div class="header-left">
        <va-icon name="inventory" color="info" size="small" class="mr-2" />
        <span class="widget-title">{{ $t('specialized_widget_material') }}</span>
      </div>
      <va-chip size="small" color="info" preset="outline">
        {{ baseUom || 'UOM' }}
      </va-chip>
    </va-card-title>
    <va-card-content class="widget-content">
      <div class="material-name-row">
        <span class="material-name">{{ materialName }}</span>
        <span v-if="materialCode" class="mat-code-badge">{{ materialCode }}</span>
      </div>

      <div class="spec-text" v-if="specification">
        {{ specification }}
      </div>

      <div class="meta-grid">
        <div class="meta-card" v-if="safetyStock !== null">
          <span class="meta-card-label">{{ $t('safety_stock_level') }}</span>
          <div class="meta-card-value-row">
            <va-icon name="shield" size="small" color="warning" class="mr-1" />
            <span class="meta-card-value">{{ safetyStock }} {{ baseUom }}</span>
          </div>
        </div>

        <div class="meta-card" v-if="storageLocation">
          <span class="meta-card-label">{{ $t('storage_location') }}</span>
          <div class="meta-card-value-row">
            <va-icon name="place" size="small" color="primary" class="mr-1" />
            <span class="meta-card-value">{{ storageLocation }}</span>
          </div>
        </div>
      </div>
    </va-card-content>
  </va-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  recordData: Record<string, any>
  domain?: Record<string, any>
}>()

const materialCode = computed(() => props.recordData?.material_code || '')
const materialName = computed(() => props.recordData?.material_name || '-')
const baseUom = computed(() => props.recordData?.base_uom || '')
const specification = computed(() => props.recordData?.specification || '')
const safetyStock = computed(() => props.recordData?.safety_stock ?? null)
const storageLocation = computed(() => props.recordData?.storage_location || '')
</script>

<style scoped>
.specialized-widget {
  border-radius: 10px;
  background: var(--va-background-primary);
  margin-bottom: 1rem;
}
.widget-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid var(--va-background-border);
}
.header-left {
  display: flex;
  align-items: center;
}
.widget-title {
  font-weight: 700;
  font-size: 0.95rem;
  color: var(--va-text-primary);
}
.widget-content {
  padding: 1rem;
}
.material-name-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.35rem;
}
.material-name {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--va-text-primary);
}
.mat-code-badge {
  font-size: 0.75rem;
  padding: 2px 6px;
  border-radius: 4px;
  background: var(--va-background-element);
  color: var(--va-text-secondary);
  font-family: monospace;
}
.spec-text {
  font-size: 0.85rem;
  color: var(--va-text-secondary);
  margin-bottom: 0.75rem;
}
.meta-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.75rem;
}
.meta-card {
  background: var(--va-background-element);
  padding: 0.65rem 0.75rem;
  border-radius: 8px;
}
.meta-card-label {
  font-size: 0.75rem;
  color: var(--va-text-secondary);
  display: block;
  margin-bottom: 0.2rem;
}
.meta-card-value-row {
  display: flex;
  align-items: center;
}
.meta-card-value {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--va-text-primary);
}
</style>
