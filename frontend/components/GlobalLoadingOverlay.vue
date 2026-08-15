<template>
  <Teleport to="body">
    <Transition name="smooth-fade">
      <div v-if="isLoading" class="glass-overlay" role="dialog" aria-modal="true" aria-label="Loading">
        <div class="loading-content">
          <va-progress-circle indeterminate color="primary" size="large" />
          <span class="loading-text">{{ displayText }}</span>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { isLoading, loadingText } = useLoading()

let translate = (k) => '데이터 처리 중입니다...'
try {
  const i18n = useI18n()
  if (i18n?.t) {
    translate = (k) => i18n.t(k)
  }
} catch {
  // test environment fallback
}

const displayText = computed(() => {
  if (loadingText.value) return loadingText.value
  return translate('common_loading')
})
</script>



<style scoped>
.glass-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(15, 23, 42, 0.18);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  z-index: 99999;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: auto;
  will-change: opacity;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.25rem;
  background: rgba(255, 255, 255, 0.94);
  padding: 2.25rem 3.25rem;
  border-radius: 20px;
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.7);
  transform: translateZ(0);
}

.loading-text {
  font-size: 1.05rem;
  font-weight: 600;
  color: #1e293b;
  letter-spacing: -0.01em;
}

.smooth-fade-enter-active,
.smooth-fade-leave-active {
  transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.smooth-fade-enter-from,
.smooth-fade-leave-to {
  opacity: 0;
}
</style>
