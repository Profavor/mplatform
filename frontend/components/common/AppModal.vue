<template>
  <va-modal
    :model-value="modelValue"
    :title="title"
    :fullscreen="isFullscreen"
    :size="isFullscreen ? 'large' : size"
    :close-button="false"
    :hide-default-actions="hideDefaultActions"
    :ok-text="okText"
    :cancel-text="cancelText"
    :no-padding="noPadding"
    :z-index="zIndex"
    :no-outside-dismiss="noOutsideDismiss"
    :prevent-click-outside="preventClickOutside"
    class="app-modal-wrapper"
    without-transitions
    @update:model-value="val => emit('update:modelValue', val)"
    @ok="emit('ok')"
    @cancel="emit('cancel')"
  >
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
      :style="{
        maxHeight: isFullscreen ? 'calc(100vh - 160px)' : (maxHeight || '70vh'),
        height: isFullscreen ? 'calc(100vh - 160px)' : undefined
      }"
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
import { ref, watch } from 'vue'
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
    preventClickOutside?: boolean
    maxHeight?: string
    zIndex?: number
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
    preventClickOutside: true
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'update:fullscreen', val: boolean): void
  (e: 'ok'): void
  (e: 'cancel'): void
  (e: 'close'): void
}>()

const isFullscreen = ref(props.fullscreen)

watch(
  () => props.fullscreen,
  (val) => {
    isFullscreen.value = val
  }
)

watch(isFullscreen, (val) => {
  emit('update:fullscreen', val)
})

watch(
  () => props.modelValue,
  (val) => {
    if (!val) {
      isFullscreen.value = false
    }
  }
)

const handleClose = () => {
  emit('update:modelValue', false)
  emit('close')
}
</script>

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
</style>
