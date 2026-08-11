<template>
  <div class="profiling-container">
    <va-inner-loading :loading="loading">
      <div v-if="stats && stats.length > 0" class="stats-grid">
        <va-card v-for="stat in stats" :key="stat.fieldName" class="stat-card">
          <va-card-title class="stat-header">
            <va-icon name="analytics" color="primary" class="mr-2"/>
            {{ stat.fieldName }}
          </va-card-title>
          <va-card-content>
            <div class="stat-row">
              <span class="stat-label">Total Records:</span>
              <span class="stat-value font-bold">{{ stat.totalCount }}</span>
            </div>
            
            <div class="stat-row mt-2">
              <span class="stat-label">Null Ratio:</span>
              <span class="stat-value font-bold" :class="getNullColorClass(stat.nullRatio)">
                {{ (stat.nullRatio * 100).toFixed(1) }}% ({{stat.nullCount}})
              </span>
            </div>
            <va-progress-bar 
              :model-value="stat.nullRatio * 100" 
              :color="getNullColor(stat.nullRatio)" 
              size="small" 
              class="mt-1 mb-2" 
            />

            <div class="stat-row">
              <span class="stat-label">Cardinality:</span>
              <span class="stat-value font-bold text-primary">{{ stat.cardinality }} unique</span>
            </div>

            <!-- Top Values -->
            <div class="top-values-container mt-3" v-if="stat.topValues && Object.keys(stat.topValues).length > 0">
              <div class="stat-label mb-1" style="font-size: 0.8rem;">Top Values:</div>
              <div v-for="(count, val) in stat.topValues" :key="val" class="top-value-row">
                <span class="top-value-name" :title="val">{{ val }}</span>
                <span class="top-value-count">{{ count }}</span>
              </div>
            </div>
          </va-card-content>
        </va-card>
      </div>
      <div v-else-if="!loading" class="empty-state">
        <va-icon name="info" size="large" color="secondary" />
        <p class="mt-2 text-secondary">No data available for profiling yet.</p>
      </div>
    </va-inner-loading>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useCookie } from '#app'

const props = defineProps({
  domainId: {
    type: String,
    required: true
  }
})

const stats = ref([])
const loading = ref(false)
const tokenCookie = useCookie('auth_token')

const fetchProfiling = async () => {
  if (!props.domainId) return
  loading.value = true
  try {
    const res = await $fetch(`/api/v1/domains/${props.domainId}/profiling`, {
      headers: { Authorization: `Bearer ${tokenCookie.value}` }
    })
    stats.value = res || []
  } catch (e) {
    console.error('Failed to fetch profiling data', e)
    stats.value = []
  } finally {
    loading.value = false
  }
}

const getNullColorClass = (ratio) => {
  if (ratio > 0.5) return 'text-danger'
  if (ratio > 0.1) return 'text-warning'
  return 'text-success'
}

const getNullColor = (ratio) => {
  if (ratio > 0.5) return 'danger'
  if (ratio > 0.1) return 'warning'
  return 'success'
}

watch(() => props.domainId, () => {
  fetchProfiling()
})

onMounted(() => {
  fetchProfiling()
})
</script>

<style scoped>
.profiling-container {
  padding: 1rem;
  height: 100%;
  overflow-y: auto;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

.stat-card {
  border-radius: 12px;
  border: 1px solid var(--va-background-border);
  box-shadow: 0 4px 10px rgba(0,0,0,0.03);
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(0,0,0,0.06);
}

.stat-header {
  font-size: 1rem;
  font-weight: 700;
  border-bottom: 1px solid var(--va-background-border);
  padding: 0.75rem 1rem;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
}

.stat-label {
  color: var(--va-text-secondary);
  font-weight: 500;
}

.top-values-container {
  background: var(--va-background-element);
  border-radius: 8px;
  padding: 0.5rem;
}

.top-value-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  padding: 2px 0;
}

.top-value-name {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 180px;
  color: var(--va-text-primary);
}

.top-value-count {
  font-weight: 600;
  color: var(--va-text-secondary);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
}
</style>
