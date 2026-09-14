<template>
  <AppModal
    v-model="show"
    :title="$t('semantic_ontology')"
    icon="hub"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🕸️ {{ $t('semantic_ontology_desc') }}
      </va-alert>

      <!-- Search Section -->
      <div style="display: flex; flex-direction: column; gap: 0.5rem;">
        <div style="display: flex; gap: 0.5rem; align-items: center;">
          <va-input
            v-model="searchKeyword"
            :placeholder="$t('ontology_search_placeholder')"
            style="flex: 1;"
            @keydown.enter="searchGraph"
          />
          <va-button size="small" color="primary" icon="search" @click="searchGraph">
            {{ $t('search') }}
          </va-button>
          <va-button preset="secondary" size="small" icon="refresh" @click="resetSearch">
            {{ $t('reset') }}
          </va-button>
        </div>

        <!-- Quick Search Tags (Dynamic from DB Domains) -->
        <div v-if="dynamicKeywords.length > 0" style="display: flex; gap: 0.35rem; align-items: center; flex-wrap: wrap;">
          <span style="font-size: 0.75rem; color: var(--va-text-secondary);">{{ $t('recommended_search') }}:</span>
          <va-chip
            v-for="kw in dynamicKeywords"
            :key="kw"
            size="small"
            outline
            color="primary"
            style="cursor: pointer; font-size: 0.72rem;"
            @click="quickSearch(kw)"
          >
            {{ kw }}
          </va-chip>
        </div>
      </div>

      <va-inner-loading :loading="loading">
        <div v-if="graphData" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Summary Banner -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); font-size: 0.85rem; font-weight: 600; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.5rem;">
            <span>{{ graphSummary }}</span>
            <va-badge
              :text="badgeSummaryText"
              color="info"
              size="small"
            />
          </div>

          <!-- Empty State when 0 nodes found -->
          <div
            v-if="!graphData.nodes || graphData.nodes.length === 0"
            style="padding: 2.5rem 1rem; text-align: center; border: 1px dashed var(--va-background-border); border-radius: 8px; background: var(--va-background-element); display: flex; flex-direction: column; align-items: center; gap: 0.75rem;"
          >
            <va-icon name="search_off" size="large" color="secondary" />
            <div style="font-size: 0.9rem; font-weight: 600; color: var(--va-text-primary);">
              {{ $t('ontology_empty_title', { keyword: searchKeyword }) }}
            </div>
            <div style="font-size: 0.78rem; color: var(--va-text-secondary);">
              {{ $t('ontology_empty_sub') }}
            </div>
            <va-button size="small" color="primary" @click="resetSearch">
              {{ $t('view_all_knowledge_graph') }}
            </va-button>
          </div>

          <!-- Nodes & Relations Grid -->
          <div v-else class="ontology-grid">
            <!-- Nodes List -->
            <div style="max-height: 280px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px; padding: 0.5rem; display: flex; flex-direction: column; gap: 0.4rem;">
              <div style="font-weight: 700; font-size: 0.82rem; margin-bottom: 0.2rem; display: flex; justify-content: space-between;">
                <span>{{ $t('ontology_nodes') }}</span>
                <span style="color: var(--va-primary);">{{ graphData.nodes.length }}</span>
              </div>
              <div
                v-for="n in graphData.nodes"
                :key="n.id"
                style="padding: 0.5rem 0.75rem; border-radius: 6px; border: 1px solid var(--va-background-border); background: var(--va-background-card); display: flex; justify-content: space-between; align-items: center; cursor: pointer; transition: background 0.2s;"
                @click="onNodeClick(n)"
              >
                <div>
                  <div style="font-weight: 700; font-size: 0.82rem;">{{ formatNodeLabel(n.label || n.name) }}</div>
                  <div style="font-size: 0.7rem; color: var(--va-text-secondary); font-family: monospace;">{{ n.id }}</div>
                </div>
                <va-badge :text="n.domainCode || n.id" color="primary" size="small" />
              </div>
            </div>

            <!-- Edges/Relations List -->
            <div style="max-height: 280px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px; padding: 0.5rem; display: flex; flex-direction: column; gap: 0.4rem;">
              <div style="font-weight: 700; font-size: 0.82rem; margin-bottom: 0.2rem; display: flex; justify-content: space-between;">
                <span>{{ $t('ontology_edges') }}</span>
                <span style="color: var(--va-primary);">{{ graphData.edges.length }}</span>
              </div>
              <div
                v-for="(e, idx) in graphData.edges"
                :key="idx"
                style="padding: 0.5rem 0.75rem; border-radius: 6px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center; font-size: 0.78rem;"
              >
                <div style="display: flex; align-items: center; gap: 0.4rem; flex-wrap: wrap;">
                  <span style="font-weight: 700;">{{ getNodeLabel(e.sourceId || e.source) }}</span>
                  <span style="color: var(--va-primary); font-family: monospace; font-size: 0.75rem;">──[{{ e.relationType || e.relation }}]──▶</span>
                  <span style="font-weight: 700;">{{ getNodeLabel(e.targetId || e.target) }}</span>
                </div>
                <va-badge :text="($t('weight') || '가중치') + ' ' + (e.weight ?? 1.0)" color="info" size="small" />
              </div>
            </div>
          </div>
        </div>
      </va-inner-loading>

      <div style="display: flex; justify-content: flex-end; margin-top: 0.5rem;">
        <va-button preset="secondary" @click="show = false">
          {{ $t('close') }}
        </va-button>
      </div>
    </div>
  </AppModal>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'
import AppModal from '~/components/common/AppModal.vue'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t, locale } = useI18n()
const { customFetch } = useCustomFetch()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const graphData = ref<any>(null)
const allNodes = ref<any[]>([])
const searchKeyword = ref('')
const loading = ref(false)

const formatNodeLabel = (val: any): string => {
  if (!val) return ''
  if (typeof val === 'object') {
    const curLocale = (locale.value || 'ko').startsWith('en') ? 'en' : 'ko'
    return val[curLocale] || val.ko || val.en || Object.values(val)[0] || ''
  }
  if (typeof val === 'string') {
    const trimmed = val.trim()
    if (trimmed.startsWith('{') && trimmed.endsWith('}')) {
      try {
        const parsed = JSON.parse(trimmed)
        const curLocale = (locale.value || 'ko').startsWith('en') ? 'en' : 'ko'
        return parsed[curLocale] || parsed.ko || parsed.en || Object.values(parsed)[0] || val
      } catch {
        return val
      }
    }
    return val
  }
  return String(val)
}

const dynamicKeywords = computed(() => {
  const list: string[] = []
  allNodes.value.forEach((n: any) => {
    const label = formatNodeLabel(n.label || n.name)
    if (label && !list.includes(label)) list.push(label)
  })
  return list.slice(0, 8)
})

const getNodeLabel = (nodeId: string) => {
  if (!graphData.value?.nodes) return nodeId
  const n = graphData.value.nodes.find((item: any) => item.id === nodeId || item.domainCode === nodeId)
  return n ? formatNodeLabel(n.label || n.name) : nodeId
}

const badgeSummaryText = computed(() => {
  const nodesCount = graphData.value?.nodes?.length || 0
  const edgesCount = graphData.value?.edges?.length || 0
  const isEn = (locale.value || '').startsWith('en')
  if (isEn) {
    return `${nodesCount} Nodes / ${edgesCount} Relations`
  }
  return `노드 ${nodesCount}개 / 관계 ${edgesCount}개`
})

const graphSummary = computed(() => {
  if (!graphData.value) return ''
  const nodesCount = graphData.value?.nodes?.length || 0
  const edgesCount = graphData.value?.edges?.length || 0
  const isEn = (locale.value || '').startsWith('en')
  if (isEn) {
    if (searchKeyword.value.trim()) {
      return `'${searchKeyword.value.trim()}' Ontology Knowledge Graph (${nodesCount} domains, ${edgesCount} relations)`
    }
    return `Enterprise Semantic Ontology Knowledge Graph (${nodesCount} domains, ${edgesCount} relations)`
  }
  return graphData.value.summary || `전사 시맨틱 온톨로지 지식 그래프 (${nodesCount}개 도메인, ${edgesCount}개 관계)`
})

const onNodeClick = (n: any) => {
  const kw = formatNodeLabel(n.label || n.name)
  searchKeyword.value = kw
  loadGraph(kw)
}

const loadGraph = async (keyword?: string) => {
  loading.value = true
  try {
    const url = keyword && keyword.trim() ? `/api/ontology/search?keyword=${encodeURIComponent(keyword.trim())}` : '/api/ontology/graph'
    const res = await customFetch(url)
    if (res) {
      graphData.value = res
      if (!keyword && res.nodes) {
        allNodes.value = res.nodes
      }
    }
  } catch (e: any) {
    console.error('Failed to load ontology graph', e)
  } finally {
    loading.value = false
  }
}

const searchGraph = () => {
  loadGraph(searchKeyword.value)
}

const quickSearch = (kw: string) => {
  searchKeyword.value = kw
  loadGraph(kw)
}

const resetSearch = () => {
  searchKeyword.value = ''
  loadGraph()
}

watch(() => props.modelValue, (val) => {
  if (val) {
    searchKeyword.value = ''
    loadGraph()
  }
})

onMounted(() => {
  if (props.modelValue) {
    loadGraph()
  }
})
</script>

<style scoped>
.ontology-grid {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 1rem;
}

@media (max-width: 640px) {
  .ontology-grid {
    grid-template-columns: 1fr;
  }
}
</style>

