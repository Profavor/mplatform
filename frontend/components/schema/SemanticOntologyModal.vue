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
            placeholder="노드 명칭, 도메인 코드 또는 관계 유형 검색 (예: 고객, PROD, PURCHASED)"
            style="flex: 1;"
            @keydown.enter="searchGraph"
          />
          <va-button size="small" color="primary" icon="search" @click="searchGraph">
            검색
          </va-button>
          <va-button preset="secondary" size="small" icon="refresh" @click="resetSearch">
            초기화
          </va-button>
        </div>

        <!-- Quick Search Tags (Dynamic from DB Domains) -->
        <div v-if="dynamicKeywords.length > 0" style="display: flex; gap: 0.35rem; align-items: center; flex-wrap: wrap;">
          <span style="font-size: 0.75rem; color: var(--va-text-secondary);">추천 검색:</span>
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
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); font-size: 0.85rem; font-weight: 600; display: flex; justify-content: space-between; align-items: center;">
            <span>{{ graphData.summary }}</span>
            <va-badge
              :text="'노드 ' + (graphData.nodes?.length || 0) + '개 / 관계 ' + (graphData.edges?.length || 0) + '개'"
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
              '{{ searchKeyword }}' 검색어와 일치하는 시맨틱 온톨로지 노드가 없습니다.
            </div>
            <div style="font-size: 0.78rem; color: var(--va-text-secondary);">
              도메인 명칭(고객, 제품, 주문 등)이나 코드(DOM-CUST 등)로 다시 검색해 보세요.
            </div>
            <va-button size="small" color="primary" @click="resetSearch">
              전체 지식 그래프 보기
            </va-button>
          </div>

          <!-- Nodes & Relations Grid -->
          <div v-else style="display: grid; grid-template-columns: 1fr 1.2fr; gap: 1rem;">
            <!-- Nodes List -->
            <div style="max-height: 250px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px; padding: 0.5rem; display: flex; flex-direction: column; gap: 0.4rem;">
              <div style="font-weight: 700; font-size: 0.82rem; margin-bottom: 0.2rem; display: flex; justify-content: space-between;">
                <span>{{ $t('ontology_nodes') }}</span>
                <span style="color: var(--va-primary);">{{ graphData.nodes.length }}개</span>
              </div>
              <div
                v-for="n in graphData.nodes"
                :key="n.id"
                style="padding: 0.5rem 0.75rem; border-radius: 6px; border: 1px solid var(--va-background-border); background: var(--va-background-card); display: flex; justify-content: space-between; align-items: center; cursor: pointer; transition: background 0.2s;"
                @click="searchKeyword = n.label; searchGraph()"
              >
                <div>
                  <div style="font-weight: 700; font-size: 0.82rem;">{{ n.label }}</div>
                  <div style="font-size: 0.7rem; color: var(--va-text-secondary); font-family: monospace;">{{ n.id }}</div>
                </div>
                <va-badge :text="n.domainCode" color="primary" size="small" />
              </div>
            </div>

            <!-- Edges/Relations List -->
            <div style="max-height: 250px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px; padding: 0.5rem; display: flex; flex-direction: column; gap: 0.4rem;">
              <div style="font-weight: 700; font-size: 0.82rem; margin-bottom: 0.2rem; display: flex; justify-content: space-between;">
                <span>{{ $t('ontology_edges') }}</span>
                <span style="color: var(--va-primary);">{{ graphData.edges.length }}개</span>
              </div>
              <div
                v-for="(e, idx) in graphData.edges"
                :key="idx"
                style="padding: 0.5rem 0.75rem; border-radius: 6px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center; font-size: 0.78rem;"
              >
                <div style="display: flex; align-items: center; gap: 0.4rem;">
                  <span style="font-weight: 700;">{{ getNodeLabel(e.sourceId) }}</span>
                  <span style="color: var(--va-primary); font-family: monospace; font-size: 0.75rem;">──[{{ e.relationType }}]──▶</span>
                  <span style="font-weight: 700;">{{ getNodeLabel(e.targetId) }}</span>
                </div>
                <va-badge :text="'가중치 ' + e.weight" color="info" size="small" />
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

const { t } = useI18n()
const { customFetch } = useCustomFetch()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const graphData = ref<any>(null)
const allNodes = ref<any[]>([])
const searchKeyword = ref('')
const loading = ref(false)

const dynamicKeywords = computed(() => {
  const list: string[] = []
  allNodes.value.forEach((n: any) => {
    if (n.label && !list.includes(n.label)) list.push(n.label)
  })
  return list.slice(0, 8)
})

const getNodeLabel = (nodeId: string) => {
  if (!graphData.value?.nodes) return nodeId
  const n = graphData.value.nodes.find((item: any) => item.id === nodeId)
  return n ? n.label : nodeId
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
