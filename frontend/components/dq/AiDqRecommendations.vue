<template>
  <va-card class="premium-card">
    <va-card-title class="card-header-title ai-header">
      <va-icon name="auto_awesome" size="small" color="warning" class="mr-2" />
      AI 추천 데이터 품질 규칙
      <va-spacer />
      <va-button preset="secondary" size="small" @click="fetchRecommendations" :loading="loading">
        <va-icon name="refresh" />
      </va-button>
    </va-card-title>
    
    <va-card-content class="card-content-area">
      <va-inner-loading :loading="loading">
        <div v-if="recommendations.length > 0" class="recommendations-list">
          <div v-for="rec in recommendations" :key="rec.id" class="recommendation-item">
            <div class="rec-header">
              <span class="font-bold text-primary">{{ rec.fieldName }}</span>
              <va-badge :text="rec.recommendedRuleType" color="info" size="small" />
              <va-spacer />
              <span class="confidence" :class="getConfidenceClass(rec.confidenceScore)">
                {{ rec.confidenceScore }}% 일치율
              </span>
            </div>
            
            <p class="rec-reason mt-2 mb-3">
              {{ rec.reason }}
            </p>
            
            <div v-if="rec.suggestedParameter" class="rec-param mb-3">
              <span class="text-xs text-secondary">제안 설정값: </span>
              <code class="param-code">{{ rec.suggestedParameter }}</code>
            </div>
            
            <div style="display: flex; justify-content: flex-end; gap: 0.5rem;">
              <va-button preset="plain" color="danger" size="small" @click="removeRecommendation(rec.id)">
                무시
              </va-button>
              <va-button color="primary" size="small" @click="applyRecommendation(rec)">
                규칙 등록
              </va-button>
            </div>
          </div>
        </div>
        
        <div v-else-if="!loading" class="empty-state">
          <va-icon name="check_circle_outline" size="large" color="success" />
          <p class="mt-2 text-secondary">현재 추천할 새로운 규칙이 없습니다.</p>
        </div>
      </va-inner-loading>
    </va-card-content>
  </va-card>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useCookie } from '#app'

const props = defineProps({
  domainId: {
    type: String,
    required: false
  }
})

const emit = defineEmits(['apply-rule'])

const recommendations = ref([])
const loading = ref(false)
const tokenCookie = useCookie('auth_token')

const fetchRecommendations = async () => {
  if (!props.domainId) {
    recommendations.value = []
    return
  }
  
  loading.value = true
  try {
    const res = await $fetch(`/api/v1/dq/recommendations/${props.domainId}`, {
      headers: { Authorization: `Bearer ${tokenCookie.value}` }
    })
    recommendations.value = res || []
  } catch (e) {
    console.error('Failed to fetch AI recommendations', e)
    recommendations.value = []
  } finally {
    loading.value = false
  }
}

const getConfidenceClass = (score) => {
  if (score >= 90) return 'text-success'
  if (score >= 70) return 'text-warning'
  return 'text-secondary'
}

const removeRecommendation = (id) => {
  recommendations.value = recommendations.value.filter(r => r.id !== id)
}

const applyRecommendation = (rec) => {
  emit('apply-rule', rec)
  removeRecommendation(rec.id)
}

watch(() => props.domainId, () => {
  fetchRecommendations()
})

onMounted(() => {
  fetchRecommendations()
})
</script>

<style scoped>
.premium-card {
  border-radius: 16px;
  box-shadow: 0 10px 30px -10px rgba(0,0,0,0.08);
  border: 1px solid var(--va-background-border);
  height: 100%;
}

.ai-header {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.1) 0%, rgba(234, 179, 8, 0.05) 100%);
  border-bottom: 1px solid rgba(245, 158, 11, 0.2);
  display: flex;
  align-items: center;
}

.card-content-area {
  padding: 1.25rem;
  height: 380px;
  overflow-y: auto;
}

.recommendation-item {
  background: var(--va-background-primary);
  border: 1px solid var(--va-background-border);
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1rem;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.recommendation-item:hover {
  border-color: var(--va-warning);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.1);
}

.rec-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.confidence {
  font-size: 0.8rem;
  font-weight: 700;
}

.rec-reason {
  font-size: 0.9rem;
  color: var(--va-text-secondary);
  line-height: 1.4;
}

.param-code {
  background: var(--va-background-element);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
  color: var(--va-primary);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}
</style>
