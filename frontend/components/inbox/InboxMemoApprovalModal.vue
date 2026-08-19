<template>
  <AppModal
    :model-value="modelValue"
    @update:model-value="val => emit('update:modelValue', val)"
    size="large"
    :title="$t('inbox.memo_approval_title')"
    icon="rate_review"
    custom-class="inbox-compose-modal-wrapper"
    :show-maximize="true"
    :show-close="true"
    hide-default-actions
  >
    <div class="memo-approval-form">
      <!-- Title Input -->
      <div class="form-row">
        <va-input
          v-model="formData.title"
          :placeholder="$t('inbox.subject_placeholder')"
          class="title-input"
        >
          <template #prependInner>
            <span class="field-label-tag">{{ $t('inbox.subject') }}</span>
          </template>
        </va-input>
      </div>

      <!-- Approval Route Designer Section -->
      <div class="form-row">
        <ApprovalRouteBuilder
          v-model="routeData"
        />
      </div>

      <!-- Attachments Section -->
      <div class="form-row">
        <InboxAttachmentUploader ref="uploaderRef" v-model="attachedFiles" />
      </div>

      <!-- Rich Text Body Editor (HtmlEditor) -->
      <div class="form-row editor-section">
        <div class="editor-label-row">
          <span class="field-label-tag">{{ $t('inbox.memo_content') }}</span>
        </div>
        <HtmlEditor v-model="formData.content" :placeholder="$t('inbox.body_placeholder')" />
      </div>

      <!-- Optional Drafter Comment Input -->
      <div class="form-row">
        <va-input
          v-model="formData.comment"
          :placeholder="$t('inbox.approval_comment')"
          class="comment-input"
        >
          <template #prependInner>
            <span class="field-label-tag">{{ $t('inbox.approval_comment') }}</span>
          </template>
        </va-input>
      </div>
    </div>

    <template #footer>
      <div class="modal-footer-actions">
        <va-button
          preset="secondary"
          :disabled="isSubmitting"
          @click="emit('update:modelValue', false)"
        >
          {{ $t('inbox.cancel') }}
        </va-button>
        <va-button
          color="primary"
          icon="send"
          :loading="isSubmitting"
          :disabled="!isFormValid || isSubmitting"
          @click="submitApproval"
        >
          {{ $t('inbox.submit_approval') }}
        </va-button>
      </div>
    </template>
  </AppModal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'
import AppModal from '~/components/common/AppModal.vue'
import HtmlEditor from '~/components/common/HtmlEditor.vue'
import InboxAttachmentUploader, { type AttachedFileItem } from './InboxAttachmentUploader.vue'
import ApprovalRouteBuilder, { type RouteStepItem } from './ApprovalRouteBuilder.vue'
import { useToast } from 'vuestic-ui'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'submitted'): void
}>()

const { t } = useI18n()
const { customFetch } = useCustomFetch()
const { init } = useToast()

const uploaderRef = ref<InstanceType<typeof InboxAttachmentUploader> | null>(null)
const attachedFiles = ref<AttachedFileItem[]>([])
const isSubmitting = ref(false)

const formData = ref({
  title: '',
  content: '',
  comment: ''
})

const routeData = ref<{
  steps: RouteStepItem[]
  observerIds: string[]
}>({
  steps: [],
  observerIds: []
})

const isFormValid = computed(() => {
  const hasTitle = formData.value.title && formData.value.title.trim().length > 0
  const hasContent = formData.value.content && formData.value.content.trim().length > 0
  const validSteps = (routeData.value.steps || []).filter(s => s.assigneeId && s.assigneeId.trim().length > 0)
  return hasTitle && hasContent && validSteps.length > 0
})

watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    formData.value = {
      title: '',
      content: '',
      comment: ''
    }
    attachedFiles.value = []
  }
})

const submitApproval = async () => {
  if (isSubmitting.value) return

  const cleanTitle = formData.value.title?.trim()
  if (!cleanTitle) {
    init({ message: t('inbox.subject_required'), color: 'warning' })
    return
  }

  const cleanContent = formData.value.content?.trim()
  if (!cleanContent) {
    init({ message: t('inbox.body_placeholder'), color: 'warning' })
    return
  }

  const validSteps = (routeData.value.steps || []).filter(s => s.assigneeId && s.assigneeId.trim().length > 0)
  if (validSteps.length === 0) {
    init({ message: t('inbox.approval_line_empty'), color: 'warning' })
    return
  }

  isSubmitting.value = true
  try {
    // 1. Upload attachments if present
    let uploadedAttachments: Array<{ fileName: string; fileSize: number; downloadUrl: string }> = []
    if (uploaderRef.value && attachedFiles.value.length > 0) {
      const uploadRes = await uploaderRef.value.uploadAll()
      if (!uploadRes.success) {
        init({ message: t('inbox.upload_failed'), color: 'danger' })
        isSubmitting.value = false
        return
      }
      if (uploadRes.urls && uploadRes.urls.length > 0) {
        uploadedAttachments = uploadRes.urls.map(u => {
          const name = decodeURIComponent(u.split('name=')[1] || 'attachment')
          return {
            fileName: name,
            fileSize: 0,
            downloadUrl: u
          }
        })
      }
    }

    // 2. Build payload for POST /api/approval-requests/memo
    const payload = {
      title: cleanTitle,
      content: cleanContent,
      comment: formData.value.comment?.trim() || '',
      steps: validSteps.map(s => ({
        stepOrder: s.stepOrder,
        stepType: s.stepType,
        assigneeId: s.assigneeId,
        assigneeRole: s.assigneeRole || ''
      })),
      observerIds: (routeData.value.observerIds || []).filter(Boolean),
      attachments: uploadedAttachments
    }

    await customFetch('/api/approval-requests/memo', {
      method: 'POST',
      body: payload
    })

    init({ message: t('inbox.submit_approval_success'), color: 'success' })
    emit('submitted')
    emit('update:modelValue', false)
  } catch (e: any) {
    console.error('Failed to submit memo approval:', e)
    init({ message: e?.data?.message || e?.message || t('inbox.submit_approval_failed'), color: 'danger' })
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.memo-approval-form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 0.25rem 0;
  max-height: 75vh;
  overflow-y: auto;
}

.form-row {
  width: 100%;
}

.title-input,
.comment-input {
  width: 100%;
}

.field-label-tag {
  font-size: 0.82rem;
  font-weight: 700;
  color: var(--va-text-secondary, #94a3b8);
  margin-right: 0.5rem;
  user-select: none;
}

.editor-section {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.editor-label-row {
  display: flex;
  align-items: center;
}

.modal-footer-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
  align-items: center;
}
</style>
