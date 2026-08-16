<template>
  <div class="custom-tree-node">
    <div class="node-wrapper" :class="{ 'selected-node': node.id === selectedNode?.id }" @click="onNodeSelected(node)">
      <!-- Indentation -->
      <div :style="{ width: `${level * 24}px` }"></div>
      
      <!-- Expand Icon -->
      <div class="expand-icon" @click.stop="toggleExpand" v-if="hasChildren">
        <va-icon :name="isExpanded ? 'keyboard_arrow_down' : 'keyboard_arrow_right'" size="small" />
      </div>
      <div class="expand-icon-placeholder" v-else></div>

      <!-- Type Icon -->
      <va-icon 
        :name="node.icon ? node.icon : (node.isDomain ? 'folder' : 'article')" 
        size="small" 
        class="type-icon" 
        :color="node.isDomain ? 'primary' : 'secondary'"
      />

      <!-- Label -->
      <span class="node-label">
        {{ node.label }}
      </span>

      <!-- Action Buttons -->
      <div class="node-actions" v-if="showEdit">
        <va-button class="action-btn" icon="edit" size="small" @click.stop="handleNodeEdit(node)" preset="plain" :color="node.id === selectedNode?.id ? '#ffffff' : 'primary'" />
        <va-button v-if="!node.isDomain" class="action-btn delete-btn" icon="delete" size="small" @click.stop="handleNodeDelete(node)" preset="plain" :color="node.id === selectedNode?.id ? '#ffffff' : 'danger'" />
      </div>
    </div>

    <!-- Children -->
    <transition name="tree-slide">
      <div class="children-wrapper" v-if="hasChildren" v-show="isExpanded">
        <SchemaTreeNode 
          v-for="child in node.children" 
          :key="child.id" 
          :node="child" 
          :level="level + 1"
          :selectedNode="selectedNode" 
          :showEdit="showEdit"
          @select="onNodeSelected" 
          @edit="handleNodeEdit" 
          @delete="handleNodeDelete"
        />
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  node: Object,
  selectedNode: Object,
  level: {
    type: Number,
    default: 0
  },
  showEdit: {
    type: Boolean,
    default: true
  }
})
const emit = defineEmits(['select', 'edit', 'delete'])

const isExpanded = ref(props.node.expanded !== false)

const hasChildren = computed(() => props.node.children && props.node.children.length > 0)

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
}

const onNodeSelected = (n) => emit('select', n)
const handleNodeEdit = (n) => emit('edit', n)
const handleNodeDelete = (n) => emit('delete', n)
</script>

<style scoped>
.custom-tree-node {
  width: 100%;
}
.node-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  margin: 4px 0;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: 1px solid transparent;
  background-color: transparent;
  color: var(--va-text-primary);
}
.node-wrapper:hover {
  background-color: var(--va-background-element);
  border-color: var(--va-background-border);
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  transform: translateX(4px);
}
.node-wrapper.selected-node {
  background: linear-gradient(135deg, var(--va-primary) 0%, var(--va-info) 100%);
  color: white !important;
  box-shadow: 0 4px 12px rgba(21, 78, 193, 0.3);
  border-color: transparent;
}
.expand-icon {
  width: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: transform 0.2s ease;
}
.expand-icon:hover {
  transform: scale(1.2);
}
.expand-icon-placeholder {
  width: 24px;
}
.type-icon {
  margin-right: 8px;
  opacity: 0.9;
}
.selected-node .type-icon,
.selected-node :deep(.type-icon),
.selected-node :deep(.type-icon i) {
  color: #ffffff !important;
}
.node-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding-right: 60px;
  font-weight: 500;
  font-size: 0.95rem;
  letter-spacing: 0.3px;
}
.selected-node .node-label {
  font-weight: 700;
}
.node-wrapper .node-actions {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  opacity: 0;
  visibility: hidden;
  display: flex;
  align-items: center;
  gap: 2px;
  transition: opacity 0.2s ease, visibility 0.2s ease, transform 0.2s ease;
  z-index: 10;
}
.node-wrapper:hover .node-actions {
  opacity: 1;
  visibility: visible;
  transform: translateY(-50%) scale(1.05);
}
.node-actions :deep(.va-button) {
  padding: 2px !important;
}
.tree-slide-enter-active,
.tree-slide-leave-active {
  transition: all 0.3s ease-in-out;
  max-height: 1000px;
  overflow: hidden;
}
.tree-slide-enter-from,
.tree-slide-leave-to {
  max-height: 0;
  opacity: 0;
}
</style>
