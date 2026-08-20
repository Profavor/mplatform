<template>
  <div class="modal-window-controls" @click.stop>
    <!-- Maximize / Restore Toggle Button -->
    <button
      v-if="showMaximize"
      type="button"
      class="modal-control-btn btn-maximize"
      :title="fullscreen ? t('modal_restore') : t('modal_maximize')"
      :aria-label="fullscreen ? t('modal_restore') : t('modal_maximize')"
      @click.stop="toggleFullscreen"
    >
      <va-icon
        :name="fullscreen ? 'fullscreen_exit' : 'fullscreen'"
        size="20px"
        :color="color || 'textPrimary'"
      />
    </button>

    <!-- Close Button -->
    <button
      v-if="showClose"
      type="button"
      class="modal-control-btn btn-close"
      :title="t('btn_close')"
      :aria-label="t('btn_close')"
      @click.stop="emitClose"
    >
      <va-icon
        name="close"
        size="20px"
        :color="color || 'textPrimary'"
      />
    </button>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = withDefaults(
  defineProps<{
    fullscreen?: boolean
    showMaximize?: boolean
    showClose?: boolean
    color?: string
  }>(),
  {
    fullscreen: false,
    showMaximize: true,
    showClose: true,
    color: ''
  }
)

const emit = defineEmits<{
  (e: 'update:fullscreen', val: boolean): void
  (e: 'close'): void
  (e: 'cancel'): void
}>()

const toggleFullscreen = () => {
  emit('update:fullscreen', !props.fullscreen)
}

const emitClose = () => {
  emit('close')
  emit('cancel')
}
</script>

<style scoped>
.modal-window-controls {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-left: 0.5rem;
  user-select: none;
  vertical-align: middle;
}

.modal-control-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  margin: 0;
  background: var(--va-background-element, rgba(125, 125, 125, 0.08));
  border: 1px solid var(--va-background-border, rgba(125, 125, 125, 0.2));
  border-radius: 6px;
  cursor: pointer;
  opacity: 0.85;
  transition: all 0.15s ease;
  outline: none;
  color: var(--va-text-primary, #333333);
}

.modal-control-btn:hover {
  opacity: 1;
  background-color: var(--va-background-secondary, rgba(125, 125, 125, 0.16));
  border-color: var(--va-primary, #2563eb);
  color: var(--va-primary, #2563eb);
  transform: translateY(-1px);
}

.modal-control-btn:active {
  transform: scale(0.94);
}

.modal-control-btn:focus-visible {
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.5);
}
</style>
