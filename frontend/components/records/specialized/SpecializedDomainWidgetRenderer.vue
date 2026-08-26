<template>
  <div v-if="isSpecialized && activeWidgetComponent" class="specialized-widget-container">
    <component
      :is="activeWidgetComponent"
      :recordData="recordData"
      :domain="domain"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import CustomerSummaryWidget from './CustomerSummaryWidget.vue'
import VendorBusinessWidget from './VendorBusinessWidget.vue'
import ProductCatalogWidget from './ProductCatalogWidget.vue'
import MaterialInventoryWidget from './MaterialInventoryWidget.vue'
import EmployeeProfileWidget from './EmployeeProfileWidget.vue'
import StockInfoWidget from './StockInfoWidget.vue'

const props = defineProps<{
  domain?: Record<string, any> | null
  recordData: Record<string, any>
}>()

const isSpecialized = computed(() => {
  if (!props.domain) return false
  return props.domain.domainType === 'SPECIALIZED' || !!props.domain.specializedCategory
})

const activeWidgetComponent = computed(() => {
  const cat = (props.domain?.specializedCategory || '').toUpperCase()
  switch (cat) {
    case 'CUSTOMER':
      return CustomerSummaryWidget
    case 'VENDOR':
      return VendorBusinessWidget
    case 'PRODUCT':
      return ProductCatalogWidget
    case 'MATERIAL':
      return MaterialInventoryWidget
    case 'EMPLOYEE':
      return EmployeeProfileWidget
    case 'STOCK':
      return StockInfoWidget
    default:
      return null
  }
})
</script>

<style scoped>
.specialized-widget-container {
  margin-bottom: 0.75rem;
}
</style>
