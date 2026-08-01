<template>
  <div style="flex: 1; display: flex; flex-direction: column; padding: 1rem; overflow-y: auto; gap: 1.5rem;">
    <!-- Top Section: Axes Definition List -->
    <va-card preset="outlined" style="border-radius: 8px;">
      <va-card-title style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <h3 style="font-weight: 700; font-size: 1.1rem; color: var(--va-text-primary); margin: 0; display: flex; align-items: center; gap: 0.5rem;">
            <va-icon name="polyline" color="primary" />
            {{ $t('axis.management_title') }}
          </h3>
          <p style="font-size: 0.85rem; color: var(--va-text-secondary); margin: 0.25rem 0 0 0;">
            {{ $t('axis.management_desc') }}
          </p>
        </div>

        <va-button icon="add" color="primary" size="small" @click="openCreateAxisModal" :disabled="!domainId">
          {{ $t('axis.add_axis') }}
        </va-button>
      </va-card-title>

      <va-card-content>
        <va-data-table
          :items="axes"
          :columns="columns"
          striped
          hoverable
          :no-data-html="$t('axis.no_axes')"
        >
          <template #cell(code)="{ rowData }">
            <span style="font-family: monospace; font-weight: 700;">{{ rowData.code || rowData.axisCode }}</span>
          </template>

          <template #cell(name)="{ rowData }">
            <span style="font-weight: 600;">{{ formatAxisName(rowData.name) }}</span>
          </template>

          <template #cell(actions)="{ rowData }">
            <div style="display: flex; gap: 0.25rem; align-items: center;">
              <va-button
                size="small"
                :preset="selectedAxis?.id === rowData.id ? 'solid' : 'outlined'"
                color="primary"
                icon="account_tree"
                @click="selectAxisForTree(rowData)"
              >
                {{ $t('axis.tree_manage') }}
              </va-button>
              <va-button preset="plain" icon="edit" color="primary" size="small" @click="openEditAxisModal(rowData)" />
              <va-button preset="plain" icon="delete" color="danger" size="small" @click="deleteAxis(rowData)" />
            </div>
          </template>
        </va-data-table>
      </va-card-content>
    </va-card>

    <!-- Bottom Section: Selected Axis Tree Management Panel -->
    <va-card v-if="selectedAxis" preset="outlined" style="border-radius: 8px; border-top: 3px solid var(--va-primary);">
      <va-card-title style="display: flex; justify-content: space-between; align-items: center;">
        <div style="display: flex; align-items: center; gap: 0.5rem;">
          <va-icon name="account_tree" color="primary" />
          <span style="font-weight: 700; font-size: 1rem; color: var(--va-text-primary);">
            [{{ formatAxisName(selectedAxis.name) }}] {{ $t('axis.node_management_title') }}
          </span>
          <va-badge :text="`${$t('axis.code_badge')}: ${selectedAxis.axisCode || selectedAxis.code}`" color="primary" outline />
        </div>

        <div style="display: flex; gap: 0.5rem;">
          <va-button icon="add" size="small" color="primary" @click="openCreateNodeModal(null)">
            {{ $t('axis.add_root_node') }}
          </va-button>
          <va-button preset="secondary" size="small" icon="refresh" @click="loadAxisNodes">
            {{ $t('axis.refresh') }}
          </va-button>
        </div>
      </va-card-title>

      <va-card-content>
        <div v-if="isTreeLoading" style="padding: 1.5rem; text-align: center; color: var(--va-text-secondary);">
          <va-progress-circle size="small" indeterminate style="margin-right: 0.5rem;" />
          {{ $t('axis.loading_tree') }}
        </div>

        <div v-else-if="!axisTreeNodes || axisTreeNodes.length === 0" style="padding: 2rem; text-align: center; background: var(--va-background-element); border-radius: 8px;">
          <va-icon name="info" size="large" color="info" class="mb-2" />
          <p style="font-size: 0.9rem; color: var(--va-text-secondary); margin: 0; white-space: pre-line;">
            {{ $t('axis.no_nodes_desc') }}
          </p>
        </div>

        <div v-else class="tree-container" style="display: flex; flex-direction: column; gap: 0.25rem;">
          <div v-for="node in axisTreeNodes" :key="node.id" class="tree-node-item">
            <!-- Recursive Node Renderer Component -->
            <AxisTreeNodeRow
              :node="node"
              :depth="0"
              @add-child="openCreateNodeModal"
              @edit-node="openEditNodeModal"
              @delete-node="deleteAxisNode"
            />
          </div>
        </div>
      </va-card-content>
    </va-card>

    <!-- Modal 1: Axis Modal Form -->
    <va-modal
      v-model="showAxisModal"
      :title="isEditAxisMode ? $t('axis.edit_axis') : $t('axis.add_axis')"
      hide-default-actions
      size="small"
      no-outside-dismiss
    >
      <form @submit.prevent="saveAxis" style="padding: 0.5rem 0;">
        <va-input
          v-model="axisForm.code"
          :label="$t('axis.axis_code_label')"
          placeholder="예: REGION, PLANT, JOB"
          class="mb-3"
          required
        />
        <va-input
          v-model="axisForm.name"
          :label="$t('axis.axis_name_label')"
          placeholder="예: 플랜트 축"
          class="mb-3"
          required
        />
        <va-input
          v-model="axisForm.description"
          :label="$t('axis.description')"
          placeholder="보조 축 설명"
          class="mb-3"
        />
        <va-input
          v-model.number="axisForm.sortOrder"
          type="number"
          :label="$t('axis.sort_order')"
          class="mb-3"
        />

        <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1.5rem;">
          <va-button preset="secondary" type="button" @click="showAxisModal = false">{{ $t('btn_cancel') }}</va-button>
          <va-button color="primary" type="submit" :loading="isAxisSaving">{{ $t('btn_submit') }}</va-button>
        </div>
      </form>
    </va-modal>

    <!-- Modal 2: Axis Node Modal Form -->
    <va-modal
      v-model="showNodeModal"
      :title="isEditNodeMode ? $t('axis.edit_node') : (targetParentNode ? `[${targetParentNode.label}] ${$t('axis.add_child_node')}` : `[${selectedAxis ? formatAxisName(selectedAxis.name) : ''}] ${$t('axis.add_root_node')}`)"
      hide-default-actions
      size="small"
      no-outside-dismiss
    >
      <form @submit.prevent="saveAxisNode" style="padding: 0.5rem 0;">
        <div style="display: flex; gap: 0.5rem;" class="mb-3">
          <va-input v-model="nodeForm.nameKo" :label="$t('axis.node_name_ko')" style="flex: 1;" required />
          <va-input v-model="nodeForm.nameEn" :label="$t('axis.node_name_en')" style="flex: 1;" />
        </div>
        <va-input
          v-model.number="nodeForm.order"
          type="number"
          :label="$t('axis.sort_order')"
          class="mb-3"
        />
        
        <div class="mb-4">
          <label style="font-weight: bold; margin-bottom: 0.5rem; display: block; font-size: 0.9rem; color: var(--va-text-primary);">{{ $t('axis.node_icon') }}</label>
          <div style="display: flex; align-items: center; gap: 1rem;">
            <va-icon :name="nodeForm.icon || 'article'" size="large" color="primary" />
            <va-button size="small" preset="secondary" border-color="primary" type="button" @click="openIconPicker">{{ $t('axis.select_icon') }}</va-button>
          </div>
        </div>

        <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1.5rem;">
          <va-button preset="secondary" type="button" @click="showNodeModal = false">{{ $t('btn_cancel') }}</va-button>
          <va-button color="primary" type="submit" :loading="isNodeSaving">{{ $t('btn_submit') }}</va-button>
        </div>
      </form>
    </va-modal>

    <!-- Modal 3: Icon Picker Modal Form -->
    <va-modal v-model="showIconPickerModal" :title="$t('axis.select_icon')" size="medium" hide-default-actions>
      <IconPicker v-model="tempIcon" />
      <div style="display: flex; justify-content: flex-end; margin-top: 1rem; gap: 0.5rem;">
        <va-button preset="secondary" type="button" @click="showIconPickerModal = false">{{ $t('btn_cancel') }}</va-button>
        <va-button color="primary" type="button" @click="applyIcon">{{ $t('btn_submit') }}</va-button>
      </div>
    </va-modal>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, h, defineComponent } from 'vue'
import { VaIcon, useToast, useModal } from 'vuestic-ui'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'
import IconPicker from '~/components/IconPicker.vue'

const props = defineProps({
  domainId: { type: String, default: '' }
})

const { t } = useI18n()
const { init } = useToast()
const { confirm } = useModal()
const { customFetch } = useCustomFetch()

// Axes State
const axes = ref([])
const selectedAxis = ref(null)
const showAxisModal = ref(false)
const isEditAxisMode = ref(false)
const isAxisSaving = ref(false)
const editingAxisId = ref(null)

const axisForm = ref({
  code: '',
  name: '',
  description: '',
  sortOrder: 1
})

// Axis Nodes Tree State
const axisTreeNodes = ref([])
const isTreeLoading = ref(false)
const showNodeModal = ref(false)
const isEditNodeMode = ref(false)
const isNodeSaving = ref(false)
const targetParentNode = ref(null)
const editingNodeId = ref(null)

const nodeForm = ref({
  nameKo: '',
  nameEn: '',
  order: 1,
  icon: ''
})

// Icon Picker State
const showIconPickerModal = ref(false)
const tempIcon = ref('')

const openIconPicker = () => {
  tempIcon.value = nodeForm.value.icon || 'article'
  showIconPickerModal.value = true
}

const applyIcon = () => {
  nodeForm.value.icon = tempIcon.value
  showIconPickerModal.value = false
}

// Columns
const columns = computed(() => [
  { key: 'code', label: t('axis.code'), width: 140 },
  { key: 'name', label: t('axis.name'), width: 180 },
  { key: 'description', label: t('axis.description') },
  { key: 'sortOrder', label: t('axis.sort_order'), width: 90 },
  { key: 'actions', label: t('axis.actions'), width: 180 }
])

const formatAxisName = (name) => {
  if (!name) return ''
  if (typeof name === 'string') return name
  if (typeof name === 'object') {
    return name.ko || name.en || Object.values(name)[0] || ''
  }
  return String(name)
}

// Inline Sub-component for Tree Node Row rendering
const AxisTreeNodeRow = defineComponent({
  name: 'AxisTreeNodeRow',
  props: {
    node: { type: Object, required: true },
    depth: { type: Number, default: 0 }
  },
  emits: ['add-child', 'edit-node', 'delete-node'],
  setup(props, { emit }) {
    const { t } = useI18n()
    return () => {
      const n = props.node
      const paddingLeft = `${props.depth * 1.5 + 0.5}rem`
      const nameStr = typeof n.name === 'object' && n.name !== null ? (n.name.ko || n.name.en || Object.values(n.name)[0]) : (n.name || 'Unnamed')

      return h('div', { style: { display: 'flex', flexDirection: 'column', gap: '0.25rem' } }, [
        h('div', {
          style: {
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '0.5rem 0.75rem',
            paddingLeft,
            background: 'var(--va-background-element)',
            borderRadius: '6px',
            border: '1px solid var(--va-background-border)'
          }
        }, [
          h('div', { style: { display: 'flex', alignItems: 'center', gap: '0.5rem' } }, [
            h('span', { style: { color: 'var(--va-primary)', fontWeight: 'bold' } }, props.depth > 0 ? '└' : '•'),
            h(VaIcon, { name: n.icon ? n.icon : 'article', size: 'small', color: 'primary' }),
            h('span', { style: { fontWeight: '600', fontSize: '0.9rem' } }, nameStr)
          ]),
          h('div', { style: { display: 'flex', gap: '0.25rem' } }, [
            h('button', {
              class: 'va-button va-button--preset-plain va-button--size-small color-primary',
              style: { cursor: 'pointer', border: 'none', background: 'transparent', color: 'var(--va-primary)', fontWeight: '600', fontSize: '0.8rem' },
              onClick: () => emit('add-child', n)
            }, t('axis.add_child_node')),
            h('button', {
              class: 'va-button va-button--preset-plain va-button--size-small color-primary',
              style: { cursor: 'pointer', border: 'none', background: 'transparent', color: 'var(--va-primary)' },
              onClick: () => emit('edit-node', n)
            }, t('axis.edit_node')),
            h('button', {
              class: 'va-button va-button--preset-plain va-button--size-small color-danger',
              style: { cursor: 'pointer', border: 'none', background: 'transparent', color: 'var(--va-danger)' },
              onClick: () => emit('delete-node', n)
            }, t('axis.delete_node'))
          ])
        ]),
        n.children && n.children.length > 0
          ? n.children.map(child => h(AxisTreeNodeRow, {
              key: child.id,
              node: child,
              depth: props.depth + 1,
              onAddChild: (target) => emit('add-child', target),
              onEditNode: (target) => emit('edit-node', target),
              onDeleteNode: (target) => emit('delete-node', target)
            }))
          : null
      ])
    }
  }
})

// Load Axes
const loadAxes = async () => {
  if (!props.domainId) {
    axes.value = []
    selectedAxis.value = null
    axisTreeNodes.value = []
    return
  }
  try {
    const res = await customFetch(`/api/domains/${props.domainId}/axes`)
    axes.value = res || []
    if (axes.value.length > 0 && !selectedAxis.value) {
      selectAxisForTree(axes.value[0])
    }
  } catch (e) {
    console.error('[loadAxes] Error:', e)
    axes.value = []
  }
}

// Select Axis & Load Node Tree
const selectAxisForTree = (axis) => {
  selectedAxis.value = axis
  loadAxisNodes()
}

const loadAxisNodes = async () => {
  if (!props.domainId || !selectedAxis.value) return
  isTreeLoading.value = true
  try {
    const res = await customFetch(`/api/domains/${props.domainId}/nodes/tree?axisId=${selectedAxis.value.id}`)
    axisTreeNodes.value = res || []
  } catch (e) {
    console.error('[loadAxisNodes] Error:', e)
    axisTreeNodes.value = []
  } finally {
    isTreeLoading.value = false
  }
}

// Axis Actions
const openCreateAxisModal = () => {
  isEditAxisMode.value = false
  editingAxisId.value = null
  isAxisSaving.value = false
  axisForm.value = {
    code: '',
    name: '',
    description: '',
    sortOrder: axes.value.length + 1
  }
  showAxisModal.value = true
}

const openEditAxisModal = (axis) => {
  isEditAxisMode.value = true
  editingAxisId.value = axis.id
  isAxisSaving.value = false
  axisForm.value = {
    code: axis.code || axis.axisCode || '',
    name: formatAxisName(axis.name),
    description: axis.description || '',
    sortOrder: axis.sortOrder || 1
  }
  showAxisModal.value = true
}

const saveAxis = async () => {
  if (!props.domainId) {
    init({ message: t('axis.invalid_domain'), color: 'warning' })
    return
  }

  const codeStr = (axisForm.value.code || '').trim()
  const nameStr = (axisForm.value.name || '').trim()

  if (!codeStr || !nameStr) {
    init({ message: t('axis.enter_code_name'), color: 'warning' })
    return
  }

  isAxisSaving.value = true
  try {
    const payload = {
      axisCode: codeStr,
      code: codeStr,
      name: { ko: nameStr, en: nameStr },
      description: axisForm.value.description || '',
      sortOrder: Number(axisForm.value.sortOrder) || 1
    }
    if (isEditAxisMode.value && editingAxisId.value) {
      await customFetch(`/api/domains/${props.domainId}/axes/${editingAxisId.value}`, {
        method: 'PUT',
        body: payload
      })
      init({ message: t('axis.axis_updated'), color: 'success' })
    } else {
      const created = await customFetch(`/api/domains/${props.domainId}/axes`, {
        method: 'POST',
        body: payload
      })
      init({ message: t('axis.axis_added'), color: 'success' })
      if (created) selectAxisForTree(created)
    }
    showAxisModal.value = false
    await loadAxes()
  } catch (e) {
    console.error('[saveAxis] Error:', e)
    init({ message: `${t('axis.axis_updated')} Error: ${e?.message || e}`, color: 'danger' })
  } finally {
    isAxisSaving.value = false
  }
}

const deleteAxis = async (axis) => {
  const isConfirmed = await confirm({
    title: t('axis.delete_node'),
    message: `[${formatAxisName(axis.name)}] ${t('axis.delete_axis_confirm')}`,
    okText: t('axis.delete_node'),
    cancelText: t('btn_cancel')
  })
  if (!isConfirmed) return

  try {
    await customFetch(`/api/domains/${props.domainId}/axes/${axis.id}`, {
      method: 'DELETE'
    })
    init({ message: t('axis.axis_deleted'), color: 'success' })
    if (selectedAxis.value?.id === axis.id) {
      selectedAxis.value = null
      axisTreeNodes.value = []
    }
    await loadAxes()
  } catch (e) {
    console.error('[deleteAxis] Error:', e)
    init({ message: t('axis.axis_deleted'), color: 'danger' })
  }
}

// Axis Node Actions
const openCreateNodeModal = (parent = null) => {
  if (!selectedAxis.value) return
  isEditNodeMode.value = false
  targetParentNode.value = parent
  editingNodeId.value = null
  isNodeSaving.value = false
  nodeForm.value = {
    nameKo: '',
    nameEn: '',
    order: 1,
    icon: ''
  }
  showNodeModal.value = true
}

const openEditNodeModal = (node) => {
  isEditNodeMode.value = true
  targetParentNode.value = null
  editingNodeId.value = node.id
  isNodeSaving.value = false
  const nameMap = typeof node.name === 'object' && node.name !== null ? node.name : { ko: String(node.name || '') }
  nodeForm.value = {
    nameKo: nameMap.ko || '',
    nameEn: nameMap.en || '',
    order: node.order || 1,
    icon: node.icon || ''
  }
  showNodeModal.value = true
}

const saveAxisNode = async () => {
  if (!props.domainId) {
    init({ message: t('axis.invalid_domain'), color: 'warning' })
    return
  }

  if (!selectedAxis.value || !selectedAxis.value.id) {
    init({ message: t('axis.invalid_domain'), color: 'warning' })
    return
  }

  const nameKoStr = (nodeForm.value.nameKo || '').trim()
  if (!nameKoStr) {
    init({ message: t('axis.enter_code_name'), color: 'warning' })
    return
  }

  isNodeSaving.value = true
  try {
    const payload = {
      name: { ko: nameKoStr, en: (nodeForm.value.nameEn || nameKoStr).trim() },
      axisId: selectedAxis.value.id,
      parentId: targetParentNode.value ? targetParentNode.value.id : null,
      order: Number(nodeForm.value.order) || 1,
      icon: nodeForm.value.icon || ''
    }

    if (isEditNodeMode.value && editingNodeId.value) {
      await customFetch(`/api/domains/${props.domainId}/nodes/${editingNodeId.value}`, {
        method: 'PUT',
        body: payload
      })
      init({ message: t('axis.node_updated'), color: 'success' })
    } else {
      await customFetch(`/api/domains/${props.domainId}/nodes`, {
        method: 'POST',
        body: payload
      })
      init({ message: t('axis.node_added'), color: 'success' })
    }
    showNodeModal.value = false
    await loadAxisNodes()
  } catch (e) {
    console.error('[saveAxisNode] Error:', e)
    init({ message: `${t('axis.node_updated')} Error: ${e?.message || e}`, color: 'danger' })
  } finally {
    isNodeSaving.value = false
  }
}

const deleteAxisNode = async (node) => {
  const nodeName = typeof node.name === 'object' ? (node.name.ko || node.name.en) : node.name
  const isConfirmed = await confirm({
    title: t('axis.delete_node'),
    message: `[${nodeName}] ${t('axis.delete_node_confirm')}`,
    okText: t('axis.delete_node'),
    cancelText: t('btn_cancel')
  })
  if (!isConfirmed) return

  try {
    await customFetch(`/api/domains/${props.domainId}/nodes/${node.id}`, {
      method: 'DELETE'
    })
    init({ message: t('axis.node_deleted'), color: 'success' })
    await loadAxisNodes()
  } catch (e) {
    console.error('[deleteAxisNode] Error:', e)
    init({ message: t('axis.node_deleted'), color: 'danger' })
  }
}

watch(() => props.domainId, (val) => {
  if (val) loadAxes()
}, { immediate: true })

onMounted(() => {
  if (props.domainId) loadAxes()
})
</script>
