<template>
  <div class="chat-message-input-container" style="padding: 0.75rem; border-top: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; flex-direction: column; gap: 0.5rem;">
    <!-- Attachment Previews -->
    <div v-if="pendingFiles.length > 0" style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
      <div
        v-for="(f, idx) in pendingFiles"
        :key="idx"
        style="display: flex; align-items: center; gap: 4px; padding: 2px 8px; border-radius: 4px; background: var(--va-background-primary); border: 1px solid var(--va-background-border); font-size: 0.8rem;"
      >
        <va-icon name="attach_file" size="14px" />
        <span>{{ f.name }}</span>
        <va-icon name="close" size="14px" style="cursor: pointer;" @click="removePendingFile(idx)" />
      </div>
    </div>

    <!-- Input Bar -->
    <div style="display: flex; align-items: flex-end; gap: 0.5rem;">
      <input
        ref="fileInputRef"
        type="file"
        multiple
        style="display: none;"
        @change="handleFileSelected"
      />
      <va-button
        preset="plain"
        size="small"
        icon="attach_file"
        color="secondary"
        @click="triggerFileInput"
      />

      <textarea
        ref="textareaRef"
        v-model="inputText"
        rows="1"
        :placeholder="t('messenger.inputPlaceholder')"
        style="flex: 1; resize: none; border-radius: 8px; border: 1px solid var(--va-background-border); padding: 8px 12px; font-size: 0.9rem; font-family: inherit; outline: none; max-height: 120px; overflow-y: auto; background: var(--va-background-primary); color: var(--va-text-primary);"
        @keydown.enter.prevent="handleEnterKey"
        @input="autoGrow"
      />

      <va-button
        color="primary"
        size="small"
        icon="send"
        :loading="loading"
        :disabled="!inputText.trim() && pendingFiles.length === 0"
        @click="submitMessage"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'send', payload: { content: string; files: File[] }): void
}>()

const { t } = useI18n()
const inputText = ref('')
const pendingFiles = ref<File[]>([])
const fileInputRef = ref<HTMLInputElement | null>(null)
const textareaRef = ref<HTMLTextAreaElement | null>(null)

const triggerFileInput = () => {
  fileInputRef.value?.click()
}

const handleFileSelected = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files) {
    pendingFiles.value.push(...Array.from(target.files))
  }
}

const removePendingFile = (idx: number) => {
  pendingFiles.value.splice(idx, 1)
}

const autoGrow = () => {
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
    textareaRef.value.style.height = `${Math.min(textareaRef.value.scrollHeight, 120)}px`
  }
}

const handleEnterKey = (event: KeyboardEvent) => {
  if (event.shiftKey) {
    inputText.value += '\n'
    nextTick(autoGrow)
  } else {
    submitMessage()
  }
}

const submitMessage = () => {
  const text = inputText.value.trim()
  if (!text && pendingFiles.value.length === 0) return

  emit('send', {
    content: text,
    files: [...pendingFiles.value]
  })

  inputText.value = ''
  pendingFiles.value = []
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
  }
}
</script>
