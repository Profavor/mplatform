<template>
  <va-modal
    v-model="show"
    :title="$t('semantic_ontology')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🕸️ {{ $t('semantic_ontology_desc') }}
      </va-alert>

      <!-- Search Section -->
      <div style="display: flex; gap: 0.5rem; align-items: center;">
        <va-input
          v-model="searchKeyword"
          :placeholder="$t('search_ontology')"
          style="flex: 1;"
          @keyup.enter="searchGraph"
        />
        <va-button size="small" color="primary" @click="searchGraph">
          검색
        </va-button>
        <va-button preset="secondary" size="small" @click="resetSearch">
          초기화
        </va-button>
      </div>

      <va-inner-loading :loading="loading">
        <div v-if="graphData" style="display: flex; flex-direction: column; gap: 1rem;">
          <!-- Summary Banner -->
          <div style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); font-size: 0.85rem; font-weight: 600;">
            {{ graphData.summary }}
          </div>

          <!-- Nodes & Relations Grid -->
          <div style="display: grid; grid-template-columns: 1fr 1.2fr; gap: 1rem;">
            <!-- Nodes List -->
            <div style="max-height: 250px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px; padding: 0.5rem; display: flex; flex-direction: column; gap: 0.4rem;">
              <div style="font-weight: 700; font-size: 0.82rem; margin-bottom: 0.2rem;">{{ $t('ontology_nodes') }} ({{ graphData.nodes.length }})</div>
              <div
                v-for="n in graphData.nodes"
                :key="n.id"
                style="padding: 0.4rem 0.6rem; border-radius: 6px; border: 1px solid var(--va-background-border); background: var(--va-background-card); display: flex; justify-content: space-between; align-items: center;"
              >
                <div style="font-weight: 600; font-size: 0.8rem;">{{ n.label }}</div>
                <va-badge :text="n.domainCode" color="info" size="small" />
              </div>
            </div>

            <!-- Edges/Relations List -->
            <div style="max-height: 250px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px; padding: 0.5rem; display: flex; flex-direction: column; gap: 0.4rem;">
              <div style="font-weight: 700; font-size: 0.82rem; margin-bottom: 0.2rem;">{{ $t('ontology_edges') }} ({{ graphData.edges.length }})</div>
              <div
                v-for="(e, idx) in graphData.edges"
                :key="idx"
                style="padding: 0.4rem 0.6rem; border-radius: 6px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: space-between; align-items: center; font-size: 0.78rem;"
              >
                <div style="display: flex; align-items: center; gap: 0.4rem;">
                  <span style="font-weight: 700;">{{ getNodeLabel(e.sourceId) }}</span>
                  <span style="color: var(--va-primary);">──[{{ e.relationType }}]──▶</span>
                  <span style="font-weight: 700;">{{ getNodeLabel(e.targetId) }}</span>
                </div>
                <span style="font-size: 0.72rem; color: var(--va-text-secondary);">가중치: {{ e.weight }}</span>
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
  </va-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const graphData = ref<any>(null)
const searchKeyword = ref('')
const loading = ref(false)

const getNodeLabel = (nodeId: string) => {
  if (!graphData.value?.nodes) return nodeId
  const n = graphData.value.nodes.find((item: any) => item.id === nodeId)
  return n ? n.label : nodeId
}

const loadGraph = async (keyword?: string) => {
  loading.value = true
  try {
    const url = keyword ? `/ontology/search?keyword=${encodeURIComponent(keyword)}` : '/ontology/graph'
    const res = await useCustomFetch(url)
    if (res.data?.value) {
      graphData.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to load ontology graph', e)
  } finally {
    loading.value = false
  }
}

const searchGraph = () => {
  loadGraph(searchKeyword.value.trim())
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
</script>
