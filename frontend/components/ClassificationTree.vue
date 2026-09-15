<template>
  <div class="classification-tree-root" style="display: flex; flex-direction: column; height: 100%; min-height: 0; width: 100%; overflow: hidden;">
    <!-- Axis Selection Dropdown Header -->
    <div v-if="!hideAxisSelect" style="padding: 0.5rem; border-bottom: 1px solid var(--va-background-border); background: var(--va-background-element); flex: 0 0 auto;">
      <va-select
        v-model="selectedAxisId"
        :options="axisOptions"
        value-by="value"
        text-by="text"
        :placeholder="$t('axis.select_axis')"
        size="small"
        style="width: 100%;"
        @update:model-value="onAxisChanged"
      >
        <template #prependInner>
          <va-icon name="polyline" size="small" color="primary" />
        </template>
      </va-select>
    </div>

    <div class="tree-scroll-container custom-scrollbar" style="flex: 1 1 auto; overflow-y: auto; overflow-x: hidden; min-height: 0; height: 100%; width: 100%;">
      <div v-if="!treeNodes || treeNodes.length === 0" style="padding: 2rem; text-align: center; color: var(--va-text-secondary, #666);">
        {{ emptyMessage }}
      </div>
      <div v-else class="va-tree" style="width: 100%;">
        <SchemaTreeNode 
          v-for="domain in treeNodes" 
          :key="domain.id" 
          :node="domain" 
          :selectedNode="selectedNode" 
          :showEdit="showEdit"
          @select="onNodeSelected" 
          @edit="handleNodeEdit" 
          @delete="handleNodeDelete"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useCookie } from '#app'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'

const props = defineProps({
  selectedNode: {
    type: Object,
    default: null
  },
  showEdit: {
    type: Boolean,
    default: false
  },
  emptyMessage: {
    type: String,
    default: ''
  },
  hideAxisSelect: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select', 'edit', 'delete', 'loaded'])

const { t } = useI18n()
const { customFetch } = useCustomFetch()
const currentLocale = useCookie('locale', { default: () => 'ko' })
const treeNodes = ref([])

const selectedAxisId = ref('__primary__') // '__primary__' = 주 분류체계 기본값
const axisOptions = ref([
  { value: '__primary__', text: `${t('axis.primary_tree')}` }
])

const parseName = (nameObj) => {
  if (!nameObj) return { ko: 'Unknown' };
  if (typeof nameObj === 'object') return nameObj;
  try {
    const parsed = JSON.parse(nameObj);
    if (typeof parsed === 'object' && parsed !== null) {
      return parsed;
    }
    return { ko: nameObj };
  } catch (e) {
    return { ko: nameObj };
  }
}

// In-memory cache for batch-trees (TTL 60s)
let _batchCache: { data: any; timestamp: number } | null = null
const CACHE_TTL = 60_000

const loadTree = async () => {
  try {
    // Use batch-trees API: 1 request instead of 37+
    const isPrimary = !selectedAxisId.value || selectedAxisId.value === '__primary__'
    
    let batchData: any[]
    const now = Date.now()
    if (isPrimary && _batchCache && (now - _batchCache.timestamp) < CACHE_TTL) {
      batchData = _batchCache.data
    } else if (isPrimary) {
      batchData = await customFetch('/api/domains/batch-trees').catch(() => [])
      if (Array.isArray(batchData) && batchData.length > 0) {
        _batchCache = { data: batchData, timestamp: now }
      }
    } else {
      // Non-primary axis: fallback to per-domain calls
      const domains = await customFetch('/api/domains').catch(() => [])
      if (!domains || !Array.isArray(domains)) {
        emit('loaded', [])
        return
      }
      batchData = []
      for (const d of domains) {
        const nodes = await customFetch(`/api/domains/${d.id}/nodes/tree?axisId=${selectedAxisId.value}`).catch(() => [])
        batchData.push({ domain: d, axes: [], tree: Array.isArray(nodes) ? nodes : [] })
      }
    }

    if (!batchData || !Array.isArray(batchData)) {
      emit('loaded', [])
      return
    }

    // Build axis options from batch data
    const opts = [{ value: '__primary__', text: `${t('axis.primary_tree')}` }]
    for (const item of batchData) {
      if (Array.isArray(item.axes)) {
        item.axes.forEach((axis: any) => {
          const axisName = typeof axis.name === 'object' && axis.name !== null
            ? (axis.name[currentLocale.value] || axis.name.ko || axis.name.en || Object.values(axis.name)[0])
            : (axis.name || 'Axis')
          const code = axis.axisCode || axis.code || ''
          opts.push({
            value: axis.id || '',
            text: code ? `[${code}] ${axisName}` : axisName
          })
        })
      }
    }
    axisOptions.value = opts

    // Build tree from batch data
    const formatNode = (n: any, domainId: string) => {
      const pName = parseName(n.name);
      return {
        id: n.id,
        label: pName?.[currentLocale.value] || pName?.ko || pName?.en || 'Unknown',
        domainId: domainId,
        axisId: n.axisId || null,
        isDomain: false,
        icon: n.icon || null,
        children: n.children ? n.children.map((c: any) => formatNode(c, domainId)) : [],
        originalNameMap: pName,
        originalData: n
      };
    };

    const builtTree = batchData.map((item: any) => {
      const d = item.domain
      const dName = parseName(d.name)
      return {
        id: d.id,
        label: (dName?.[currentLocale.value] || dName?.ko || dName?.en || 'Unknown') + (isPrimary ? ' (Domain)' : ''),
        domainId: d.id,
        isDomain: true,
        icon: d.icon || null,
        expanded: true,
        children: Array.isArray(item.tree) ? item.tree.map((n: any) => formatNode(n, d.id)) : [],
        originalNameMap: dName,
        originalData: d
      }
    })

    treeNodes.value = builtTree
    emit('loaded', builtTree)
  } catch (error: any) {
    console.error('Failed to load tree:', error.message || error)
    emit('loaded', [])
  }
}

const onAxisChanged = () => {
  loadTree()
}

watch(currentLocale, () => {
  const updateLabel = (nodes) => {
    nodes.forEach(n => {
      if (n.originalNameMap) {
        n.label = n.originalNameMap[currentLocale.value] || n.originalNameMap.ko || n.originalNameMap.en || 'Unknown';
        if (n.isDomain && !selectedAxisId.value) n.label += ' (Domain)';
      }
      if (n.children && n.children.length > 0) {
        updateLabel(n.children);
      }
    })
  }
  updateLabel(treeNodes.value)
})

const onNodeSelected = (node) => {
  emit('select', node)
}

const handleNodeEdit = (node) => {
  emit('edit', node)
}

const handleNodeDelete = (node) => {
  emit('delete', node)
}

onMounted(() => {
  loadTree()
})

defineExpose({
  loadTree
})
</script>

<style scoped>
.classification-tree-root {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  width: 100%;
  overflow: hidden;
}

.tree-scroll-container {
  flex: 1 1 auto;
  overflow-y: auto;
  overflow-x: hidden;
  min-height: 0;
  height: 100%;
  width: 100%;
  overscroll-behavior: contain;
}

/* Custom Scrollbar for Tree */
.custom-scrollbar {
  scrollbar-width: thin;
  scrollbar-color: rgba(21, 78, 193, 0.4) transparent;
}
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background-color: rgba(21, 78, 193, 0.35);
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background-color: rgba(21, 78, 193, 0.65);
}
</style>
