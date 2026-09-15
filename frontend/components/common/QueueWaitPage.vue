<template>
  <Teleport to="body">
    <Transition name="queue-fade">
      <div v-if="visible" class="queue-overlay" @click.self="handleOverlayClick">
        <div class="queue-card">
          <!-- Traffic cone icon -->
          <div class="queue-icon">🚦</div>

          <h2 class="queue-title">{{ $t('common.queueTitle', '접속자가 많아 잠시 대기 중입니다') }}</h2>

          <p class="queue-subtitle">
            {{ $t('common.queueSubtitle', '현재 서버에 많은 요청이 집중되고 있습니다. 자동으로 재시도됩니다.') }}
          </p>

          <!-- Countdown Ring -->
          <div class="queue-countdown-wrapper">
            <svg class="countdown-ring" viewBox="0 0 120 120">
              <circle class="ring-bg" cx="60" cy="60" r="52" />
              <circle
                class="ring-progress"
                cx="60" cy="60" r="52"
                :style="{ strokeDashoffset: dashOffset }"
              />
            </svg>
            <div class="countdown-text">
              <span class="countdown-number">{{ countdown }}</span>
              <span class="countdown-label">{{ $t('common.seconds', '초') }}</span>
            </div>
          </div>

          <p class="queue-retry-info">
            {{ $t('common.retryAttempt', '재시도 횟수') }}: {{ retryCount }} / {{ maxRetries }}
          </p>

          <!-- Manual retry button -->
          <button class="queue-retry-btn" @click="manualRetry" :disabled="isRetrying">
            <span v-if="isRetrying" class="retry-spinner" />
            {{ isRetrying
              ? $t('common.retrying', '재시도 중...')
              : $t('common.retryNow', '지금 다시 시도')
            }}
          </button>

          <!-- Give up message after max retries -->
          <p v-if="retryCount >= maxRetries" class="queue-exhausted">
            ⚠️ {{ $t('common.queueExhausted', '잠시 후 다시 접속해 주세요.') }}
          </p>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from 'vue'

const props = withDefaults(defineProps<{
  visible: boolean
  retryAfterSeconds?: number
  maxRetries?: number
}>(), {
  retryAfterSeconds: 5,
  maxRetries: 5
})

const emit = defineEmits<{
  (e: 'retry'): void
  (e: 'close'): void
}>()

const countdown = ref(props.retryAfterSeconds)
const retryCount = ref(0)
const isRetrying = ref(false)
let countdownTimer: ReturnType<typeof setInterval> | null = null

const circumference = 2 * Math.PI * 52
const dashOffset = computed(() => {
  const progress = countdown.value / props.retryAfterSeconds
  return circumference * progress
})

const startCountdown = () => {
  stopCountdown()
  countdown.value = props.retryAfterSeconds
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      stopCountdown()
      autoRetry()
    }
  }, 1000)
}

const stopCountdown = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

const autoRetry = () => {
  if (retryCount.value >= props.maxRetries) {
    return
  }
  retryCount.value++
  isRetrying.value = true
  emit('retry')
  // Reset retrying state after a short delay (parent will close or restart)
  setTimeout(() => {
    isRetrying.value = false
  }, 2000)
}

const manualRetry = () => {
  stopCountdown()
  retryCount.value++
  isRetrying.value = true
  emit('retry')
  setTimeout(() => {
    isRetrying.value = false
  }, 2000)
}

const handleOverlayClick = () => {
  // Don't close on overlay click when queue is active
}

watch(() => props.visible, (newVal) => {
  if (newVal) {
    startCountdown()
  } else {
    stopCountdown()
    retryCount.value = 0
  }
}, { immediate: true })

onUnmounted(() => {
  stopCountdown()
})
</script>

<style scoped>
.queue-overlay {
  position: fixed;
  inset: 0;
  z-index: 99999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
}

.queue-card {
  background: #fff;
  border-radius: 20px;
  padding: 40px 48px;
  max-width: 420px;
  width: 90%;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: queue-slide-up 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.queue-icon {
  font-size: 56px;
  margin-bottom: 16px;
}

.queue-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px;
}

.queue-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0 0 24px;
  line-height: 1.5;
}

.queue-countdown-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto 20px;
}

.countdown-ring {
  width: 120px;
  height: 120px;
  transform: rotate(-90deg);
}

.ring-bg {
  fill: none;
  stroke: #e8eaed;
  stroke-width: 8;
}

.ring-progress {
  fill: none;
  stroke: #1a73e8;
  stroke-width: 8;
  stroke-linecap: round;
  stroke-dasharray: 326.73;
  transition: stroke-dashoffset 1s linear;
}

.countdown-text {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.countdown-number {
  font-size: 36px;
  font-weight: 700;
  color: #1a73e8;
  line-height: 1;
}

.countdown-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.queue-retry-info {
  font-size: 12px;
  color: #999;
  margin: 0 0 16px;
}

.queue-retry-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 32px;
  border: none;
  border-radius: 8px;
  background: #1a73e8;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.queue-retry-btn:hover:not(:disabled) {
  background: #1557b0;
  transform: translateY(-1px);
}

.queue-retry-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.retry-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

.queue-exhausted {
  margin-top: 16px;
  font-size: 13px;
  color: #e67700;
  font-weight: 500;
}

/* Animations */
@keyframes queue-slide-up {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.queue-fade-enter-active,
.queue-fade-leave-active {
  transition: opacity 0.3s;
}
.queue-fade-enter-from,
.queue-fade-leave-to {
  opacity: 0;
}

/* Dark mode */
.dark-mode .queue-card {
  background: #1e1e2e;
}
.dark-mode .queue-title {
  color: #e0e0e0;
}
.dark-mode .queue-subtitle {
  color: #aaa;
}
</style>
