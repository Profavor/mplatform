<template>
  <AppModal
    :model-value="modelValue"
    v-model:fullscreen="isFullscreenModal"
    size="large"
    hide-default-actions
    without-transitions
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <template #header>
      <div v-if="selectedRequest" style="display: flex; flex-direction: column; gap: 0.5rem; width: 100%;">
        <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 0.5rem;">
          <div style="display: flex; align-items: center; gap: 0.65rem;">
            <h3 style="margin: 0; font-size: 1.25rem; font-weight: 800; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
              <va-icon name="verified_user" color="primary" />
              {{ t('details') }}
            </h3>
            <va-badge :text="getRequestTypeLabel(selectedRequest.targetType)" :color="getRequestTypeColor(selectedRequest.targetType)" />
            <va-badge :text="selectedRequest.status" :color="selectedRequest.status === 'PENDING' ? 'warning' : (selectedRequest.status === 'APPROVED' ? 'success' : 'danger')" />
          </div>

          <div style="display: flex; align-items: center; gap: 0.75rem; margin-left: auto;">
            <div style="font-size: 0.85rem; color: var(--va-text-secondary); display: flex; align-items: center; gap: 0.75rem;">
              <span v-if="selectedRequest">
                <va-icon name="person" size="small" style="margin-right: 2px;" />
                {{ t('requester') }}: <strong>{{ getRequesterName(selectedRequest) }}</strong>
              </span>
              <span>
                <va-icon name="schedule" size="small" style="margin-right: 2px;" />
                {{ formatDate(selectedRequest.createdAt) }}
              </span>
            </div>
          </div>
        </div>

        <div v-if="selectedRequest.classificationNode" style="display: flex; align-items: center; margin-top: 0.25rem;">
          <span style="font-size: 0.88rem; font-weight: 700; display: inline-flex; align-items: center; padding: 3px 12px; background: rgba(37, 99, 235, 0.08); border-radius: 16px; border: 1px solid rgba(37, 99, 235, 0.15);">
            <span style="color: var(--va-primary);">
              {{ getClassificationName(selectedRequest.classificationNode, 'domainName') }}
            </span>
            <va-icon name="chevron_right" size="small" style="margin: 0 4px; color: var(--va-primary); font-size: 1rem;" />
            <span style="color: var(--va-text-primary);">
              {{ getClassificationName(selectedRequest.classificationNode, 'name') }}
            </span>
          </span>
        </div>
      </div>
    </template>

    <div v-if="selectedRequest" :style="{ padding: '1rem 0 0 0', maxHeight: isFullscreenModal ? 'calc(100vh - 160px)' : '75vh', overflowY: 'auto' }">
      <!-- Shared Approval Details Viewer (Collapsible requestedData Accordion & Approval Steps Timeline) -->
      <ApprovalDetailsViewer v-if="selectedRequest" :request="selectedRequest" />
    </div>

    <template #footer>
      <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
        <div>
          <va-button
            v-if="canCancelApproval"
            preset="primary"
            color="warning"
            icon="cancel_schedule_send"
            :loading="isCancelling"
            @click="showCancelModal = true"
          >
            {{ $t('inbox.cancel_approval') || '상신 취소' }}
          </va-button>
        </div>
        <va-button preset="secondary" @click="$emit('update:modelValue', false)">{{ t('close') }}</va-button>
      </div>

      <!-- Cancel Confirmation Modal (Inside Footer slot with Teleport to body) -->
      <teleport to="body">
        <va-modal
          v-model="showCancelModal"
          :title="$t('inbox.cancel_approval') || '상신 취소'"
          :ok-text="$t('inbox.cancel_approval') || '상신 취소'"
          :cancel-text="$t('inbox.cancel') || '취소'"
          ok-color="warning"
          @ok="handleCancelApproval"
        >
          <div style="display: flex; flex-direction: column; gap: 0.75rem; padding: 0.5rem 0;">
            <p style="margin: 0; font-size: 0.9rem; color: var(--va-text-primary);">
              {{ $t('inbox.cancel_approval_confirm') || '정말 이 결재 요청을 상신 취소하시겠습니까? 미완료된 결재 단계들이 모두 취소 처리됩니다.' }}
            </p>
            <va-textarea
              v-model="cancelReason"
              :label="$t('inbox.cancel_approval_reason') || '취소 사유'"
              :placeholder="$t('inbox.cancel_approval_reason_placeholder') || '취소 사유를 입력하세요 (선택)'"
              rows="3"
              style="width: 100%;"
            />
          </div>
        </va-modal>
      </teleport>
    </template>
  </AppModal>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'vuestic-ui'
import { useApprovalEnricher } from '~/composables/useApprovalEnricher'
import { useUserStore } from '~/stores/useUserStore'
import ApprovalDetailsViewer from '~/components/ApprovalDetailsViewer.vue'
import ModalControls from '~/components/common/ModalControls.vue'
import AppModal from '~/components/common/AppModal.vue'

const { t } = useI18n()
const { init } = useToast()
const { customFetch } = useCustomFetch()
const { getRequestTypeLabel, getRequestTypeColor, getRequesterName, formatDate, getClassificationName } = useApprovalEnricher()

const { hasPermission } = usePermission()
const userDataCookie = useCookie('user_data')
const userCookie = useCookie('user')
const tokenCookie = useCookie('auth_token')

const isFullscreenModal = ref(false)
const showCancelModal = ref(false)
const cancelReason = ref('')
const isCancelling = ref(false)

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  selectedRequest: { type: Object, default: null }
})

const emit = defineEmits(['update:modelValue', 'cancelled'])

const currentLoggedInUser = computed(() => {
  try {
    const raw = userDataCookie.value || userCookie.value
    if (!raw) return null
    return typeof raw === 'string' ? JSON.parse(raw) : raw
  } catch {
    return null
  }
})

const currentUserId = computed(() => {
  const user = currentLoggedInUser.value
  let id = user?.id || user?.userId || user?.uuid || ''
  if (!id && tokenCookie.value) {
    try {
      const payload = JSON.parse(decodeURIComponent(atob(tokenCookie.value.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join('')))
      id = payload.sub || payload.userId || payload.id || ''
    } catch {}
  }
  return id
})

const currentUsername = computed(() => {
  const user = currentLoggedInUser.value
  let name = user?.username || user?.name || ''
  if (!name && tokenCookie.value) {
    try {
      const payload = JSON.parse(decodeURIComponent(atob(tokenCookie.value.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join('')))
      name = payload.username || payload.preferred_username || ''
    } catch {}
  }
  return name
})

const canCancelApproval = computed(() => {
  if (!props.selectedRequest || props.selectedRequest.status !== 'PENDING') return false
  const reqId = String(props.selectedRequest.requesterId || '').toLowerCase()
  const myId = String(currentUserId.value || '').toLowerCase()
  const myUName = String(currentUsername.value || '').toLowerCase()

  // 오직 기안자 본인만 상신 취소 가능
  return Boolean(reqId && (reqId === myId || reqId === myUName))
})

const handleCancelApproval = async () => {
  if (!props.selectedRequest?.id) return
  isCancelling.value = true
  try {
    const updated = await customFetch(`/api/approval-requests/${props.selectedRequest.id}/cancel`, {
      method: 'POST',
      body: { reason: cancelReason.value }
    })
    init({
      message: t('inbox.cancel_approval_success') || '결재 상신이 성공적으로 취소되었습니다.',
      color: 'warning'
    })
    emit('cancelled', updated)
    emit('update:modelValue', false)
  } catch (e) {
    console.error('Failed to cancel approval:', e)
    init({
      message: e?.data?.message || e?.message || t('inbox.cancel_approval_failed') || '결재 상신 취소에 실패했습니다.',
      color: 'danger'
    })
  } finally {
    isCancelling.value = false
    showCancelModal.value = false
  }
}
</script>
