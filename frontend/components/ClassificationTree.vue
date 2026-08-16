<template>
  <div class="schema-tree-wrapper" style="display: flex; flex-direction: column; height: 100%;">
    <!-- Axis Selection Dropdown Header -->
    <div v-if="!hideAxisSelect" style="padding: 0.5rem; border-bottom: 1px solid var(--va-background-border); background: var(--va-background-element);">
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

    <div style="flex: 1; overflow-y: auto;">
      <div v-if="!treeNodes || treeNodes.length === 0" style="padding: 2rem; text-align: center; color: #666;">
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

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useCookie } from '#app'
import { useI18n } from 'vue-i18n'

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
const token = useCookie('auth_token')
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

const loadAxisOptions = async (domains) => {
  const opts = [{ value: '__primary__', text: `${t('axis.primary_tree')}` }]
  for (const d of domains) {
    try {
      const axes = await $fetch(`/api/domains/${d.id}/axes`, {
        headers: { Authorization: `Bearer ${token.value}` }
      })
      if (Array.isArray(axes)) {
        axes.forEach(axis => {
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
    } catch (e) {
      console.error('Failed to load axes for domain:', d.id, e)
    }
  }
  axisOptions.value = opts
}

const loadTree = async () => {
  if (!token.value) return;
  try {
    const domains = await $fetch('/api/domains', {
      headers: { Authorization: `Bearer ${token.value}` }
    })
    
    // Load Axes options once when domains are fetched
    await loadAxisOptions(domains)

    const builtTree = []
    for (const d of domains) {
      const isPrimary = !selectedAxisId.value || selectedAxisId.value === '__primary__'
      const url = isPrimary
        ? `/api/domains/${d.id}/nodes/tree`
        : `/api/domains/${d.id}/nodes/tree?axisId=${selectedAxisId.value}`

      const nodes = await $fetch(url, {
        headers: { Authorization: `Bearer ${token.value}` }
      })
      
      const formatNode = (n) => {
        const pName = parseName(n.name);
        return {
          id: n.id,
          label: pName?.[currentLocale.value] || pName?.ko || pName?.en || 'Unknown',
          domainId: d.id,
          axisId: n.axisId || null,
          isDomain: false,
          icon: n.icon || null,
          children: n.children ? n.children.map(formatNode) : [],
          originalNameMap: pName,
          originalData: n
        };
      };
      
      const dName = parseName(d.name);
      builtTree.push({
        id: d.id,
        label: (dName?.[currentLocale.value] || dName?.ko || dName?.en || 'Unknown') + (selectedAxisId.value && selectedAxisId.value !== '__primary__' ? '' : ' (Domain)'),
        domainId: d.id,
        isDomain: true,
        icon: d.icon || null,
        expanded: true,
        children: nodes.map(formatNode),
        originalNameMap: dName,
        originalData: d
      })
    }
    treeNodes.value = builtTree
    emit('loaded', builtTree)
  } catch (error) {
    console.error('Failed to load tree:', error.message || error)
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
.schema-tree-wrapper {
  flex: 1;
  overflow-y: auto;
}
</style>
