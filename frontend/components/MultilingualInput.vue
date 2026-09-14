<template>
  <div class="multilingual-input-container mb-4">
    <!-- Top Small Primary Label -->
    <div v-if="label" style="font-size: 0.6rem; font-weight: 700; color: var(--va-primary); margin-bottom: 0.25rem; text-transform: uppercase; letter-spacing: 0.4px;">
      {{ label }} <span v-if="required" style="color: var(--va-danger);">*</span>
    </div>

    <!-- Dual Language Inputs (Korean & English) -->
    <div style="display: flex; gap: 0.5rem; flex-direction: row; min-width: 0;">
      <!-- Textarea Mode -->
      <template v-if="isTextarea">
        <va-textarea
          :model-value="koValue"
          :placeholder="placeholderKo || '한국어 입력'"
          :min-rows="minRows || 2"
          style="flex: 1; min-width: 0;"
          @update:modelValue="onKoInput"
        >
          <template #prependInner>
            <span class="lang-prefix">Korean</span>
          </template>
        </va-textarea>
        <va-textarea
          :model-value="enValue"
          :placeholder="placeholderEn || 'English Input'"
          :min-rows="minRows || 2"
          style="flex: 1; min-width: 0;"
          @update:modelValue="onEnInput"
        >
          <template #prependInner>
            <span class="lang-prefix">English</span>
          </template>
        </va-textarea>
      </template>

      <!-- Standard Input Mode -->
      <template v-else>
        <va-input
          :model-value="koValue"
          :placeholder="placeholderKo || '한국어 입력'"
          style="flex: 1; min-width: 0;"
          @update:modelValue="onKoInput"
        >
          <template #prependInner>
            <span class="lang-prefix">Korean</span>
          </template>
        </va-input>
        <va-input
          :model-value="enValue"
          :placeholder="placeholderEn || 'English Input'"
          style="flex: 1; min-width: 0;"
          @update:modelValue="onEnInput"
        >
          <template #prependInner>
            <span class="lang-prefix">English</span>
          </template>
        </va-input>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  ko: {
    type: String,
    default: ''
  },
  en: {
    type: String,
    default: ''
  },
  label: {
    type: String,
    default: ''
  },
  required: {
    type: Boolean,
    default: false
  },
  isTextarea: {
    type: Boolean,
    default: false
  },
  minRows: {
    type: Number,
    default: 2
  },
  placeholderKo: {
    type: String,
    default: ''
  },
  placeholderEn: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:ko', 'update:en'])

const koValue = computed(() => props.ko || '')
const enValue = computed(() => props.en || '')

const onKoInput = (val) => {
  emit('update:ko', val || '')
}

const onEnInput = (val) => {
  emit('update:en', val || '')
}
</script>

<style scoped>
.lang-prefix {
  font-size: 0.75rem;
  color: #888;
  font-weight: 600;
  margin-right: 0.5rem;
  border-right: 1px solid #ddd;
  padding-right: 0.5rem;
  white-space: nowrap;
}
</style>
