<template>
  <va-modal
    ref="vaModalRef"
    :model-value="modelValue"
    :title="title"
    :fullscreen="isFullscreen"
    :size="isFullscreen ? 'large' : size"
    :close-button="false"
    :hide-default-actions="hideDefaultActions"
    :ok-text="okText"
    :cancel-text="cancelText"
    :no-padding="noPadding"
    :zIndex="zIndex"
    :no-outside-dismiss="noOutsideDismiss"
    :class="[
      'app-modal-wrapper',
      customClass,
      {
        'is-resizable': resizable && !isFullscreen,
        'has-custom-width': !!customWidth && !isFullscreen,
        'has-custom-height': !!customHeight && !isFullscreen
      }
    ]"
    :style="modalCustomStyle"
    @update:model-value="val => emit('update:modelValue', val)"
    @ok="emit('ok')"
    @cancel="emit('cancel')"
  >
    <!-- 8-Directional Resize Handles (Corners & Edges) -->
    <template v-if="resizable && !isFullscreen && modelValue">
      <div class="modal-resize-handle handle-top" @mousedown="onStartResize($event, 'n')"></div>
      <div class="modal-resize-handle handle-bottom" @mousedown="onStartResize($event, 's')"></div>
      <div class="modal-resize-handle handle-left" @mousedown="onStartResize($event, 'w')"></div>
      <div class="modal-resize-handle handle-right" @mousedown="onStartResize($event, 'e')"></div>
      <div class="modal-resize-handle handle-top-left" @mousedown="onStartResize($event, 'nw')"></div>
      <div class="modal-resize-handle handle-top-right" @mousedown="onStartResize($event, 'ne')"></div>
      <div class="modal-resize-handle handle-bottom-left" @mousedown="onStartResize($event, 'sw')"></div>
      <div
        class="modal-resize-handle handle-bottom-right"
        @mousedown="onStartResize($event, 'se')"
        @dblclick="resetModalSize"
        :title="resizeTooltip"
      >
        <div class="corner-grip-lines"></div>
      </div>
    </template>

    <!-- Header Slot / Default Header -->
    <template #header>
      <div class="app-modal-header">
        <slot name="header">
          <div class="app-modal-title-wrap">
            <va-icon v-if="icon" :name="icon" color="primary" size="20px" />
            <h3 class="app-modal-title">{{ title }}</h3>
          </div>
        </slot>

        <!-- Right Side Modal Controls with Header Actions -->
        <div class="app-modal-controls-wrap">
          <slot name="header-actions" />
          <ModalControls
            v-if="!hideControls"
            v-model:fullscreen="isFullscreen"
            :show-maximize="showMaximize"
            :show-close="showClose"
            @close="handleClose"
          />
        </div>
      </div>
    </template>

    <!-- Modal Body Content -->
    <div
      class="app-modal-body"
      :style="bodyStyle"
    >
      <slot />
    </div>

    <!-- Footer Slot -->
    <template v-if="$slots.footer" #footer>
      <slot name="footer" />
    </template>
  </va-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import ModalControls from './ModalControls.vue'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    title?: string
    icon?: string
    size?: 'small' | 'medium' | 'large'
    fullscreen?: boolean
    hideDefaultActions?: boolean
    hideControls?: boolean
    showMaximize?: boolean
    showClose?: boolean
    okText?: string
    cancelText?: string
    noPadding?: boolean
    noOutsideDismiss?: boolean
    maxHeight?: string
    zIndex?: number
    customClass?: string
    resizable?: boolean
    minWidth?: number
    minHeight?: number
  }>(),
  {
    title: '',
    icon: '',
    size: 'large',
    fullscreen: false,
    hideDefaultActions: false,
    hideControls: false,
    showMaximize: true,
    showClose: true,
    noPadding: false,
    noOutsideDismiss: true,
    resizable: true,
    minWidth: 380,
    minHeight: 240
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'update:fullscreen', val: boolean): void
  (e: 'ok'): void
  (e: 'cancel'): void
  (e: 'close'): void
  (e: 'resize', size: { width: number; height: number }): void
}>()

let tFunc: (key: string, def?: any) => string = (k, def) => (typeof def === 'string' ? def : k)
try {
  const i18n = useI18n()
  if (i18n && typeof i18n.t === 'function') {
    tFunc = i18n.t
  }
} catch (e) {
  // Safe fallback if useI18n is not in injection scope
}

const resizeTooltip = computed(() => {
  return tFunc('inbox.drag_to_resize', '드래그하여 크기 조절 (더블클릭 시 초기화)')
})

const isFullscreen = ref(props.fullscreen)
const customWidth = ref<number | null>(null)
const customHeight = ref<number | null>(null)
const isResizing = ref(false)

const modalCustomStyle = computed(() => {
  const styles: Record<string, string> = {}
  if (customWidth.value && !isFullscreen.value) {
    styles['--app-modal-custom-width'] = `${customWidth.value}px`
  }
  if (customHeight.value && !isFullscreen.value) {
    styles['--app-modal-custom-height'] = `${customHeight.value}px`
  }
  if (props.zIndex) {
    styles['z-index'] = String(props.zIndex)
    styles['--va-modal-z-index'] = String(props.zIndex)
  }
  return styles
})

const bodyStyle = computed(() => {
  if (isFullscreen.value) {
    return {
      maxHeight: 'calc(100vh - 120px)',
      height: 'calc(100vh - 120px)'
    }
  }
  if (customHeight.value) {
    return {
      maxHeight: `calc(${customHeight.value}px - 110px)`,
      height: `calc(${customHeight.value}px - 110px)`
    }
  }
  return {
    maxHeight: props.maxHeight || '85vh',
    height: undefined
  }
})

watch(
  () => props.fullscreen,
  (val) => {
    isFullscreen.value = val
  }
)

watch(isFullscreen, (val) => {
  emit('update:fullscreen', val)
  if (val) {
    customWidth.value = null
    customHeight.value = null
  }
  if (typeof window !== 'undefined') {
    setTimeout(() => {
      window.dispatchEvent(new Event('resize'))
    }, 50)
  }
})

watch(
  () => props.modelValue,
  (val) => {
    if (!val) {
      isFullscreen.value = false
      customWidth.value = null
      customHeight.value = null
    }
  }
)

const resetModalSize = () => {
  customWidth.value = null
  customHeight.value = null
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new Event('resize'))
  }
}

const onStartResize = (e: MouseEvent, direction: string) => {
  if (!props.resizable || isFullscreen.value) return
  e.preventDefault()
  e.stopPropagation()

  isResizing.value = true

  const handleEl = e.target as HTMLElement
  const dialogEl = (handleEl.closest('.va-modal__dialog') || handleEl.closest('.app-modal-wrapper') || handleEl.parentElement) as HTMLElement

  if (!dialogEl) return

  const rect = dialogEl.getBoundingClientRect()
  const startWidth = rect.width
  const startHeight = rect.height
  const startX = e.clientX
  const startY = e.clientY

  const cursorMap: Record<string, string> = {
    n: 'ns-resize',
    s: 'ns-resize',
    e: 'ew-resize',
    w: 'ew-resize',
    nw: 'nwse-resize',
    se: 'nwse-resize',
    ne: 'nesw-resize',
    sw: 'nesw-resize'
  }

  if (typeof document !== 'undefined') {
    document.body.style.cursor = cursorMap[direction] || 'nwse-resize'
    document.body.style.userSelect = 'none'
  }

  const onMouseMove = (moveEvent: MouseEvent) => {
    if (!isResizing.value) return

    const deltaX = moveEvent.clientX - startX
    const deltaY = moveEvent.clientY - startY

    let newWidth = startWidth
    let newHeight = startHeight

    if (direction.includes('e')) {
      newWidth = startWidth + deltaX
    } else if (direction.includes('w')) {
      newWidth = startWidth - deltaX
    }

    if (direction.includes('s')) {
      newHeight = startHeight + deltaY
    } else if (direction.includes('n')) {
      newHeight = startHeight - deltaY
    }

    const minW = props.minWidth || 380
    const maxW = typeof window !== 'undefined' ? window.innerWidth * 0.98 : 1920
    const minH = props.minHeight || 240
    const maxH = typeof window !== 'undefined' ? window.innerHeight * 0.98 : 1080

    if (direction.includes('e') || direction.includes('w')) {
      customWidth.value = Math.round(Math.min(Math.max(newWidth, minW), maxW))
    }
    if (direction.includes('s') || direction.includes('n')) {
      customHeight.value = Math.round(Math.min(Math.max(newHeight, minH), maxH))
    }

    emit('resize', {
      width: customWidth.value || startWidth,
      height: customHeight.value || startHeight
    })

    if (typeof window !== 'undefined') {
      window.dispatchEvent(new Event('resize'))
    }
  }

  const onMouseUp = () => {
    isResizing.value = false
    if (typeof document !== 'undefined') {
      document.body.style.cursor = ''
      document.body.style.userSelect = ''
    }
    window.removeEventListener('mousemove', onMouseMove)
    window.removeEventListener('mouseup', onMouseUp)

    if (typeof window !== 'undefined') {
      window.dispatchEvent(new Event('resize'))
    }
  }

  window.addEventListener('mousemove', onMouseMove)
  window.addEventListener('mouseup', onMouseUp)
}

const handleClose = () => {
  emit('update:modelValue', false)
  emit('close')
}
</script>

<style>
/* Resizable styling applied to Vuestic Modal Dialog ONLY when custom size is explicitly applied by dragging */
.app-modal-wrapper.has-custom-width:not(.va-modal--fullscreen) .va-modal__dialog {
  width: var(--app-modal-custom-width) !important;
  max-width: calc(100vw - 32px) !important;
}

.app-modal-wrapper.has-custom-height:not(.va-modal--fullscreen) .va-modal__dialog {
  height: var(--app-modal-custom-height) !important;
  max-height: calc(100vh - 32px) !important;
}

.app-modal-wrapper.is-resizable:not(.va-modal--fullscreen) .va-modal__dialog {
  position: relative !important;
}
</style>

<style scoped>
.app-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  min-height: 28px;
  gap: 0.75rem;
}

.app-modal-title-wrap {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  min-width: 0;
  flex: 1;
}

.app-modal-title {
  margin: 0;
  padding: 0;
  font-size: 1.05rem;
  font-weight: 800;
  color: var(--va-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-modal-controls-wrap {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-left: auto;
  flex-shrink: 0;
}

.app-modal-body {
  width: 100%;
  box-sizing: border-box;
  overflow-y: auto;
  overflow-x: hidden;
}

:deep(.va-modal__close),
:deep(.va-modal__close-button) {
  display: none !important;
}

/* =========================================================================
   8-Directional Resize Handles
   ========================================================================= */
.modal-resize-handle {
  position: absolute;
  z-index: 100;
  box-sizing: border-box;
}

.handle-top {
  top: -4px;
  left: 10px;
  right: 10px;
  height: 8px;
  cursor: ns-resize;
}

.handle-bottom {
  bottom: -4px;
  left: 10px;
  right: 10px;
  height: 8px;
  cursor: ns-resize;
}

.handle-left {
  left: -4px;
  top: 10px;
  bottom: 10px;
  width: 8px;
  cursor: ew-resize;
}

.handle-right {
  right: -4px;
  top: 10px;
  bottom: 10px;
  width: 8px;
  cursor: ew-resize;
}

.handle-top-left {
  top: -6px;
  left: -6px;
  width: 16px;
  height: 16px;
  cursor: nwse-resize;
}

.handle-top-right {
  top: -6px;
  right: -6px;
  width: 16px;
  height: 16px;
  cursor: nesw-resize;
}

.handle-bottom-left {
  bottom: -6px;
  left: -6px;
  width: 16px;
  height: 16px;
  cursor: nesw-resize;
}

.handle-bottom-right {
  bottom: 0px;
  right: 0px;
  width: 18px;
  height: 18px;
  cursor: nwse-resize;
  display: flex;
  align-items: flex-end;
  justify-content: flex-end;
  padding: 3px;
  user-select: none;
}

.corner-grip-lines {
  width: 10px;
  height: 10px;
  background: linear-gradient(
    135deg,
    transparent 0%,
    transparent 40%,
    var(--va-secondary, #94a3b8) 40%,
    var(--va-secondary, #94a3b8) 55%,
    transparent 55%,
    transparent 70%,
    var(--va-secondary, #94a3b8) 70%,
    var(--va-secondary, #94a3b8) 85%,
    transparent 85%
  );
  opacity: 0.6;
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.handle-bottom-right:hover .corner-grip-lines {
  opacity: 1;
  transform: scale(1.15);
}
</style>
