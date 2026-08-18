<template>
  <AppModal
    :model-value="modelValue"
    @update:model-value="val => emit('update:modelValue', val)"
    size="large"
    :title="modalTitle"
    icon="edit_note"
    custom-class="inbox-compose-modal-wrapper"
    :show-maximize="true"
    :show-close="true"
    hide-default-actions
  >
    <div class="compose-form">
<!-- To Recipient Picker -->
      <InboxRecipientPicker
        v-model="formData.toRecipients"
        :label="$t('inbox.recipient_to')"
      >
        <template #extra-actions>
          <va-button
            v-if="!showCcBcc"
            preset="secondary"
            size="small"
            icon="group_add"
            class="cc-bcc-btn"
            @click="showCcBcc = true"
          >
            + {{ $t('inbox.add_recipient') }}
          </va-button>
        </template>
      </InboxRecipientPicker>
      
      <!-- CC & BCC Pickers when toggled -->
      <div v-if="showCcBcc" class="cc-bcc-fields">
        <InboxRecipientPicker
          v-model="formData.ccRecipients"
          :label="$t('inbox.recipient_cc')"
        />
        <InboxRecipientPicker
          v-model="formData.bccRecipients"
          :label="$t('inbox.recipient_bcc')"
        />
      </div>

      <!-- Subject & Importance Row -->
      <div class="subject-importance-row">
        <va-input
          v-model="formData.subject"
          :placeholder="$t('inbox.subject_placeholder')"
          class="subject-input"
        >
          <template #prependInner>
            <span class="field-label-tag">{{ $t('inbox.subject') }}</span>
          </template>
        </va-input>
        
        <va-select
          v-model="formData.importance"
          :options="importanceOptions"
          value-by="value"
          text-by="text"
          class="importance-select"
        >
          <template #prependInner>
            <span class="field-label-tag">{{ $t('inbox.importance') }}</span>
          </template>
        </va-select>
      </div>

      <!-- Modern Interactive Attachment Manager (Directly below recipient & subject) -->
      <div class="attachments-area">
        <InboxAttachmentUploader ref="uploaderRef" v-model="attachedFiles" />
      </div>

      <!-- Rich Text Body Editor -->
      <div class="editor-container">
        <HtmlEditor v-model="formData.body" :placeholder="$t('inbox.body_placeholder')" />
      </div>
    </div>

    <template #footer>
      <div class="compose-footer">
        <va-button preset="secondary" :disabled="isSubmitting" @click="emit('update:modelValue', false)">{{ $t('inbox.cancel') }}</va-button>
        <va-button preset="outline" color="secondary" :loading="isSubmitting" :disabled="isSubmitting" @click="saveDraft">{{ $t('inbox.save_draft') }}</va-button>
        <va-button color="primary" :loading="isSubmitting" :disabled="!isFormValid || isSubmitting" @click="send">{{ $t('inbox.send') }}</va-button>
      </div>
    </template>
  </AppModal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { InboxMessage, InboxMessageRequest } from '~/composables/useInbox'
import { useInbox } from '~/composables/useInbox'
import { useAuthUser } from '~/composables/useAuthUser'
import AppModal from '~/components/common/AppModal.vue'
import HtmlEditor from '~/components/common/HtmlEditor.vue'
import InboxRecipientPicker from './InboxRecipientPicker.vue'
import InboxAttachmentUploader, { type AttachedFileItem } from './InboxAttachmentUploader.vue'
import { useToast } from 'vuestic-ui'

const props = defineProps<{
  modelValue: boolean
  mode: 'compose' | 'reply' | 'replyAll' | 'forward'
  originalMessage: InboxMessage | null
}>()

const emit = defineEmits(['update:modelValue', 'sent', 'drafted'])
const { t } = useI18n()
const { sendMessage, saveDraft: apiSaveDraft } = useInbox()
const { user } = useAuthUser()
const { init } = useToast()

const showCcBcc = ref(false)
const uploaderRef = ref<InstanceType<typeof InboxAttachmentUploader> | null>(null)
const attachedFiles = ref<AttachedFileItem[]>([])
const isSubmitting = ref(false)

const formData = ref<{
  subject: string
  body: string
  importance: string
  toRecipients: string[]
  ccRecipients: string[]
  bccRecipients: string[]
  parentMessageId?: string
}>({
  subject: '',
  body: '',
  importance: 'NORMAL',
  toRecipients: [],
  ccRecipients: [],
  bccRecipients: []
})

const importanceOptions = computed(() => [
  { value: 'NORMAL', text: t('inbox.importance_normal') },
  { value: 'HIGH', text: t('inbox.importance_high') },
  { value: 'URGENT', text: t('inbox.importance_urgent') }
])

const modalTitle = computed(() => {
  if (props.mode === 'reply') return t('inbox.reply')
  if (props.mode === 'replyAll') return t('inbox.reply_all')
  if (props.mode === 'forward') return t('inbox.forward')
  return t('inbox.compose_title')
})

const isFormValid = computed(() => {
  const cleanTo = (formData.value.toRecipients || []).filter(r => r && String(r).trim().length > 0)
  return cleanTo.length > 0 && formData.value.subject && formData.value.subject.trim().length > 0
})

watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    showCcBcc.value = false
    attachedFiles.value = []
    if (props.mode === 'compose' || !props.originalMessage) {
      formData.value = {
        subject: '',
        body: '',
        importance: 'NORMAL',
        toRecipients: [],
        ccRecipients: [],
        bccRecipients: []
      }
    } else {
      const msg = props.originalMessage
      const myId = user.value?.id
      
      let to: string[] = []
      let cc: string[] = []
      let subj = msg.subject || ''
      let body = `<br/><br/><hr/><blockquote>${msg.body || ''}</blockquote>`
      
      if (props.mode === 'reply') {
        to = [msg.senderId || msg.senderEmail]
        subj = subj.startsWith('Re:') ? subj : `Re: ${subj}`
      } else if (props.mode === 'replyAll') {
        to = [msg.senderId || msg.senderEmail, ...msg.toRecipients.map(r => r.userId || r.email).filter(id => id !== myId)]
        cc = [...msg.ccRecipients.map(r => r.userId || r.email).filter(id => id !== myId)]
        subj = subj.startsWith('Re:') ? subj : `Re: ${subj}`
        if (cc.length > 0) showCcBcc.value = true
      } else if (props.mode === 'forward') {
        subj = subj.startsWith('Fwd:') ? subj : `Fwd: ${subj}`
      }
      
      formData.value = {
        subject: subj,
        body: body,
        importance: msg.importance || 'NORMAL',
        toRecipients: to,
        ccRecipients: cc,
        bccRecipients: [],
        parentMessageId: props.mode !== 'forward' ? msg.id : undefined
      }
    }
  }
})

const send = async () => {
  if (isSubmitting.value) return

  const cleanTo = (formData.value.toRecipients || []).map(r => typeof r === 'string' ? r.trim() : (r?.id || r?.username || r?.email || '')).filter(Boolean)
  if (cleanTo.length === 0) {
    init({ message: t('inbox.recipient_required', '수신자를 1명 이상 입력하거나 선택해주세요.'), color: 'warning' })
    return
  }

  const cleanSubject = formData.value.subject ? formData.value.subject.trim() : ''
  if (!cleanSubject) {
    init({ message: t('inbox.subject_required', '메시지 제목을 입력해주세요.'), color: 'warning' })
    return
  }

  isSubmitting.value = true
  try {
    // Upload all attached files now at send time!
    let attachmentUrls: string[] = []
    if (uploaderRef.value && attachedFiles.value.length > 0) {
      const uploadRes = await uploaderRef.value.uploadAll()
      if (!uploadRes.success) {
        init({ message: t('inbox.upload_failed', '일부 파일 업로드에 실패하였습니다. 다시 확인해주세요.'), color: 'danger' })
        isSubmitting.value = false
        return
      }
      attachmentUrls = uploadRes.urls
    }

    let finalBody = formData.value.body || ''
    if (attachmentUrls.length > 0) {
      const linksHtml = attachmentUrls.map(u => {
        const name = decodeURIComponent(u.split('name=')[1] || 'attachment')
        return `<p><a href="${u}" target="_blank" download>📎 ${name}</a></p>`
      }).join('')
      // Store links in HTML body for external email delivery, but cleaned for internal UI
      finalBody += `<br/><hr/><h4>${t('inbox.attachments', '첨부파일')}</h4>${linksHtml}`
    }

    const cleanCc = (formData.value.ccRecipients || []).map(r => typeof r === 'string' ? r.trim() : (r?.id || r?.username || r?.email || '')).filter(Boolean)
    const cleanBcc = (formData.value.bccRecipients || []).map(r => typeof r === 'string' ? r.trim() : (r?.id || r?.username || r?.email || '')).filter(Boolean)

    const req: InboxMessageRequest = {
      subject: cleanSubject,
      body: finalBody,
      importance: formData.value.importance || 'NORMAL',
      toRecipients: cleanTo,
      ccRecipients: cleanCc,
      bccRecipients: cleanBcc,
      parentMessageId: formData.value.parentMessageId || undefined,
      attachmentIds: [],
      isDraft: false
    }
    await sendMessage(req)
    init({ message: t('inbox.message_sent'), color: 'success' })
    emit('sent')
    emit('update:modelValue', false)
  } catch (e: any) {
    console.error('Failed to send message:', e)
    init({ message: e?.data?.message || e?.message || t('inbox.send_failed', '메시지 전송에 실패하였습니다.'), color: 'danger' })
  } finally {
    isSubmitting.value = false
  }
}

const saveDraft = async () => {
  if (isSubmitting.value) return
  isSubmitting.value = true
  try {
    let attachmentUrls: string[] = []
    if (uploaderRef.value && attachedFiles.value.length > 0) {
      const uploadRes = await uploaderRef.value.uploadAll()
      if (uploadRes.urls) {
        attachmentUrls = uploadRes.urls
      }
    }

    let finalBody = formData.value.body || ''
    if (attachmentUrls.length > 0) {
      const linksHtml = attachmentUrls.map(u => {
        const name = decodeURIComponent(u.split('name=')[1] || 'attachment')
        return `<p><a href="${u}" target="_blank" download>📎 ${name}</a></p>`
      }).join('')
      finalBody += `<br/><hr/><h4>${t('inbox.attachments', '첨부파일')}</h4>${linksHtml}`
    }

    const cleanTo = (formData.value.toRecipients || []).map(r => typeof r === 'string' ? r.trim() : (r?.id || r?.username || r?.email || '')).filter(Boolean)
    const cleanCc = (formData.value.ccRecipients || []).map(r => typeof r === 'string' ? r.trim() : (r?.id || r?.username || r?.email || '')).filter(Boolean)
    const cleanBcc = (formData.value.bccRecipients || []).map(r => typeof r === 'string' ? r.trim() : (r?.id || r?.username || r?.email || '')).filter(Boolean)

    const req: InboxMessageRequest = {
      subject: formData.value.subject?.trim() || t('inbox.no_subject', '(제목 없음)'),
      body: finalBody,
      importance: formData.value.importance || 'NORMAL',
      toRecipients: cleanTo,
      ccRecipients: cleanCc,
      bccRecipients: cleanBcc,
      parentMessageId: formData.value.parentMessageId || undefined,
      attachmentIds: [],
      isDraft: true
    }
    await apiSaveDraft(req)
    init({ message: t('inbox.draft_saved'), color: 'info' })
    emit('drafted')
    emit('update:modelValue', false)
  } catch (e: any) {
    console.error('Failed to save draft:', e)
    init({ message: e?.data?.message || e?.message || t('inbox.draft_failed', '임시저장에 실패하였습니다.'), color: 'danger' })
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.compose-form {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 0.25rem 0;
}

.cc-bcc-btn {
  font-size: 0.78rem !important;
  padding: 0.25rem 0.5rem !important;
  height: 28px !important;
}

.cc-bcc-fields {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  animation: fadeIn 0.15s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}

.subject-importance-row {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  width: 100%;
}

.subject-input {
  flex: 1;
}

.importance-select {
  width: 160px;
  flex-shrink: 0;
}

.field-label-tag {
  font-size: 0.82rem;
  font-weight: 700;
  color: var(--va-text-secondary, #94a3b8);
  margin-right: 0.5rem;
  user-select: none;
}

.editor-container {
  min-height: 280px;
  margin-top: 0.25rem;
}

.attachments-area {
  margin-top: 0.25rem;
}

.compose-footer {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
  align-items: center;
}
</style>
