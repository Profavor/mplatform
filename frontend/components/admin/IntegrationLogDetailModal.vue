<template>
  <AppModal
    :model-value="modelValue"
    :title="t('integration_log_detail', '연동 로그 상세')"
    icon="hub"
    size="large"
    hide-default-actions
    @update:model-value="val => emit('update:modelValue', val)"
  >
    <div v-if="log" class="integration-modal-container">
      <div class="modal-body-content">
        <!-- Metric Status Summary Cards -->
        <div class="metrics-grid">
          <div class="metric-card" :class="log.status === 'SUCCESS' ? 'status-success' : 'status-fail'">
            <span class="metric-label">Status</span>
            <div class="flex items-center gap-2 mt-1">
              <span class="status-indicator-dot"></span>
              <span class="metric-value font-bold">{{ log.status }}</span>
            </div>
          </div>

          <div class="metric-card">
            <span class="metric-label">Direction</span>
            <div class="metric-value mt-1">
              <va-badge
                :text="log.direction === 'INBOUND' ? (t('integration.channels.inbound', 'Inbound')) : (t('integration.channels.outbound', 'Outbound'))"
                :color="log.direction === 'INBOUND' ? 'warning' : 'info'"
              />
            </div>
          </div>

          <div class="metric-card">
            <span class="metric-label">Event Type</span>
            <div class="metric-value mt-1 text-primary flex items-center gap-1">
              <va-icon name="event" size="small" />
              <span>{{ log.eventType }}</span>
            </div>
          </div>

          <div class="metric-card">
            <span class="metric-label">Retry Count</span>
            <div class="metric-value mt-1">
              <span class="retry-badge">{{ log.retryCount }} Retry</span>
            </div>
          </div>

          <div class="metric-card">
            <span class="metric-label">Logged At</span>
            <div class="metric-value mt-1 date-text flex items-center gap-1">
              <va-icon name="schedule" size="small" />
              <span>{{ log.createdAt }}</span>
            </div>
          </div>
        </div>

        <!-- Error Message & Stack Trace Section (Shown only on error) -->
        <div v-if="log.errorMessage" class="error-panel">
          <div class="error-panel-header">
            <va-icon name="error" color="danger" size="small" />
            <span>Error Message</span>
          </div>
          <div class="error-message-body">
            {{ log.errorMessage }}
          </div>
        </div>

        <div v-if="log.stackTrace" class="error-panel mt-3">
          <div class="error-panel-header">
            <va-icon name="bug_report" color="danger" size="small" />
            <span>Stack Trace Exception</span>
          </div>
          <div class="stack-trace-terminal">
            <code>{{ log.stackTrace }}</code>
          </div>
        </div>

        <!-- Payload Viewers (Mac Terminal Shell Style - Direction aware) -->
        <div class="payload-section mt-4">
          <!-- First Terminal (Incoming or Outgoing Payload) -->
          <div class="terminal-card">
            <div class="terminal-header">
              <div class="terminal-dots">
                <span class="dot dot-red"></span>
                <span class="dot dot-yellow"></span>
                <span class="dot dot-green"></span>
              </div>
              <span class="terminal-title">
                {{ log.direction === 'INBOUND' ? (t('incoming_payload_title', 'Incoming Raw Payload')) : (t('outgoing_payload_title', 'Outgoing Target Payload')) }}
              </span>
              <button class="copy-btn" @click="copyPayload(log.originalPayload, 'original')">
                <va-icon name="content_copy" size="small" /> {{ copySuccess === 'original' ? 'Copied!' : 'Copy' }}
              </button>
            </div>
            <div class="terminal-body">
              <pre><code>{{ formatJson(log.originalPayload) }}</code></pre>
            </div>
          </div>

          <!-- Second Terminal (Mapped Record or Response Result) -->
          <div class="terminal-card mt-4">
            <div class="terminal-header">
              <div class="terminal-dots">
                <span class="dot dot-red"></span>
                <span class="dot dot-yellow"></span>
                <span class="dot dot-green"></span>
              </div>
              <span class="terminal-title">
                {{ log.direction === 'INBOUND' ? (t('mapped_payload_title', 'Internal Mapped Data')) : (t('response_result_title', 'External Response Payload')) }}
              </span>
              <button class="copy-btn" @click="copyPayload(log.mappedPayload, 'mapped')">
                <va-icon name="content_copy" size="small" /> {{ copySuccess === 'mapped' ? 'Copied!' : 'Copy' }}
              </button>
            </div>
            <div class="terminal-body">
              <pre><code>{{ formatJson(log.mappedPayload) }}</code></pre>
            </div>
          </div>
        </div>
      </div>

      <!-- Modal Footer Actions -->
      <div class="modal-footer-bar">
        <va-button
          v-if="log.status === 'FAIL' && (!hasPermission || hasPermission('integration:write') || hasPermission('integration:*'))"
          color="warning"
          gradient
          icon="replay"
          @click="onRetry"
        >
          {{ t('retry_integration', '재시도') }}
        </va-button>
        <va-button preset="secondary" border-color="secondary" @click="emit('update:modelValue', false)">
          {{ t('close', '닫기') }}
        </va-button>
      </div>
    </div>
  </AppModal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import AppModal from '~/components/common/AppModal.vue'

const { t } = useI18n()

const props = defineProps<{
  modelValue: boolean
  log: any
  hasPermission?: (perm: string) => boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'retry', logId: string): void
}>()

const copySuccess = ref<string | null>(null)

const copyPayload = async (payload: any, type: string) => {
  if (!payload) return
  const text = typeof payload === 'string' ? payload : JSON.stringify(payload, null, 2)
  try {
    await navigator.clipboard.writeText(text)
    copySuccess.value = type
    setTimeout(() => {
      copySuccess.value = null
    }, 2000)
  } catch (err) {
    console.error('Failed to copy: ', err)
  }
}

const formatJson = (val: any) => {
  if (!val) return 'No Payload Content'
  if (typeof val === 'string') {
    try {
      return JSON.stringify(JSON.parse(val), null, 2)
    } catch {
      return val
    }
  }
  return JSON.stringify(val, null, 2)
}

const onRetry = () => {
  if (props.log?.id) {
    emit('retry', props.log.id)
  }
}

defineExpose({
  copyPayload,
  formatJson,
  onRetry,
  copySuccess
})
</script>

<style scoped>
.integration-modal-container {
  display: flex;
  flex-direction: column;
  background: var(--va-background-primary);
  border-radius: 12px;
  overflow: hidden;
}

.modal-header-banner {
  padding: 1.25rem 1.5rem;
  background: var(--va-background-element);
  border-bottom: 1px solid var(--va-background-border);
}

.modal-title-text {
  font-size: 1.15rem;
  font-weight: 700;
  margin: 0;
  color: var(--va-text-primary);
}

.modal-body-content {
  padding: 1.5rem;
  max-height: 70vh;
  overflow-y: auto;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
  gap: 0.75rem;
  margin-bottom: 1.25rem;
}

.metric-card {
  padding: 0.75rem 1rem;
  border-radius: 8px;
  background: var(--va-background-element);
  border: 1px solid var(--va-background-border);
  display: flex;
  flex-direction: column;
}

.metric-label {
  font-size: 0.72rem;
  font-weight: 700;
  text-transform: uppercase;
  color: var(--va-text-secondary);
}

.status-indicator-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--va-primary);
}

.status-success .status-indicator-dot {
  background: #10b981;
}

.status-fail .status-indicator-dot {
  background: #ef4444;
}

.error-panel {
  border-radius: 8px;
  border: 1px solid rgba(239, 68, 68, 0.3);
  background: rgba(239, 68, 68, 0.05);
  overflow: hidden;
}

.error-panel-header {
  padding: 0.5rem 0.75rem;
  font-weight: 700;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.error-message-body {
  padding: 0.75rem;
  font-size: 0.82rem;
  color: #ef4444;
  word-break: break-all;
}

.stack-trace-terminal {
  padding: 0.75rem;
  background: #18181b;
  color: #f87171;
  font-family: 'Fira Code', monospace;
  font-size: 0.75rem;
  max-height: 180px;
  overflow-y: auto;
  white-space: pre-wrap;
}

.terminal-card {
  border-radius: 8px;
  border: 1px solid var(--va-background-border);
  overflow: hidden;
  background: #1e1e24;
}

.terminal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem 0.75rem;
  background: #18181b;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.terminal-dots {
  display: flex;
  gap: 5px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.dot-red { background: #ff5f56; }
.dot-yellow { background: #ffbd2e; }
.dot-green { background: #27c93f; }

.terminal-title {
  font-size: 0.75rem;
  font-weight: 600;
  color: #a1a1aa;
}

.copy-btn {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: #d4d4d8;
  font-size: 0.72rem;
  padding: 2px 8px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.terminal-body {
  padding: 0.75rem;
  color: #e4e4e7;
  font-family: 'Fira Code', monospace;
  font-size: 0.78rem;
  max-height: 220px;
  overflow-y: auto;
}

.modal-footer-bar {
  padding: 1rem 1.5rem;
  background: var(--va-background-element);
  border-top: 1px solid var(--va-background-border);
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}
</style>
