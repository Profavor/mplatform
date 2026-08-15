<template>
  <va-modal
    v-model="show"
    :title="$t('workspace_widgets')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🎛️ {{ $t('workspace_widgets_desc') }}
      </va-alert>

      <va-inner-loading :loading="loading">
        <div style="display: flex; flex-direction: column; gap: 0.75rem;">
          <div style="font-weight: 700; font-size: 0.85rem;">{{ $t('widget_gallery') }}</div>

          <div style="max-height: 300px; overflow-y: auto; display: flex; flex-direction: column; gap: 0.5rem;">
            <div
              v-for="w in widgets"
              :key="w.widgetId"
              style="padding: 0.75rem 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-card); display: flex; justify-content: space-between; align-items: center;"
            >
              <div style="display: flex; flex-direction: column; gap: 0.2rem;">
                <div style="display: flex; align-items: center; gap: 0.5rem;">
                  <span style="font-weight: 700; font-size: 0.85rem;">{{ w.title }}</span>
                  <va-badge
                    :text="w.category"
                    color="info"
                    size="small"
                  />
                </div>
                <span style="font-size: 0.78rem; color: var(--va-text-secondary);">
                  {{ w.description }}
                </span>
              </div>
              <va-switch v-model="w.enabled" size="small" />
            </div>
          </div>
        </div>
      </va-inner-loading>

      <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem;">
        <va-button preset="secondary" @click="show = false">
          {{ $t('close') }}
        </va-button>
        <va-button color="primary" :loading="saving" @click="saveWidgets">
          {{ $t('save_layout') }}
        </va-button>
      </div>
    </div>
  </va-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const widgets = ref<any[]>([])
const loading = ref(false)
const saving = ref(false)

const loadWidgets = async () => {
  loading.value = true
  try {
    const res = await useCustomFetch('/workspace/widgets')
    if (res.data?.value) {
      widgets.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to load workspace widgets', e)
  } finally {
    loading.value = false
  }
}

const saveWidgets = async () => {
  saving.value = true
  try {
    const res = await useCustomFetch('/workspace/widgets', {
      method: 'POST',
      body: {
        widgets: widgets.value
      }
    })
    if (res.data?.value) {
      show.value = false
    }
  } catch (e: any) {
    console.error('Failed to save widgets', e)
  } finally {
    saving.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) loadWidgets()
})
</script>
