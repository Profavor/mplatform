<template>
  <AppModal
    v-model="show"
    :title="$t('bulk_reclassify')"
    icon="drive_file_move"
    size="medium"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem 0;">
      <va-alert color="info" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🔀 {{ $t('bulk_reclassify_desc', { count: selectedRecordIds.length }) }}
      </va-alert>

      <!-- Target Node Select -->
      <div>
        <label style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">
          {{ $t('target_node') }} <span style="color: var(--va-danger);">*</span>
        </label>
        <va-select
          v-model="targetNodeId"
          :options="nodeOptions"
          value-by="id"
          text-by="displayName"
          :placeholder="$t('select_target_node_placeholder')"
        />
      </div>

      <!-- Reclassify Reason -->
      <div>
        <label style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">
          {{ $t('reclassify_reason') }}
        </label>
        <va-input
          v-model="reason"
          type="textarea"
          :autosize="true"
          :min-rows="2"
          :placeholder="$t('reclassify_reason_placeholder')"
        />
      </div>

      <!-- Footer Buttons -->
      <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem;">
        <va-button preset="secondary" @click="show = false">
          {{ $t('cancel') }}
        </va-button>
        <va-button
          color="primary"
          icon="drive_file_move"
          :disabled="!targetNodeId || selectedRecordIds.length === 0 || submitting"
          :loading="submitting"
          @click="submitReclassify"
        >
          {{ $t('bulk_reclassify') }}
        </va-button>
      </div>
    </div>
  </AppModal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'vuestic-ui'
import { useCustomFetch } from '~/composables/useCustomFetch'
import AppModal from '~/components/common/AppModal.vue'

const props = defineProps<{
  modelValue: boolean
  selectedRecordIds: string[]
  domainId?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'reclassified', result: any): void
}>()

const { t, locale } = useI18n()
const toast = useToast()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const targetNodeId = ref<string>('')
const reason = ref<string>('')
const submitting = ref(false)
const nodeOptions = ref<any[]>([])

const fetchNodes = async () => {
  if (!props.domainId) return
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/classification-tree`)
    if (res.data?.value) {
      const flattened: any[] = []
      const flatten = (nodes: any[], depth = 0) => {
        for (const node of nodes) {
          const prefix = '— '.repeat(depth)
          let nodeName = node.name
          if (typeof node.name === 'object' && node.name !== null) {
            nodeName = node.name[locale.value] || node.name['ko'] || node.name['en'] || Object.values(node.name)[0]
          }
          flattened.push({
            id: node.id,
            displayName: prefix + (nodeName || node.id)
          })
          if (node.children && node.children.length > 0) {
            flatten(node.children, depth + 1)
          }
        }
      }
      flatten(Array.isArray(res.data.value) ? res.data.value : [res.data.value])
      nodeOptions.value = flattened
    }
  } catch (e) {
    console.error('Failed to fetch classification nodes', e)
  }
}

const submitReclassify = async () => {
  if (!targetNodeId.value || props.selectedRecordIds.length === 0) return
  submitting.value = true
  try {
    const res = await useCustomFetch('/records/bulk-reclassify', {
      method: 'POST',
      body: {
        recordIds: props.selectedRecordIds,
        targetNodeId: targetNodeId.value,
        reason: reason.value
      }
    })

    const data = res.data?.value
    if (data) {
      if (data.failureCount === 0) {
        toast.init({
          message: t('reclassify_success', { count: data.successCount }),
          color: 'success'
        })
      } else {
        toast.init({
          message: t('reclassify_partial_failed', { success: data.successCount, failure: data.failureCount }),
          color: 'warning'
        })
      }
      emit('reclassified', data)
      show.value = false
    }
  } catch (err: any) {
    toast.init({
      message: err.message || 'Failed to reclassify records',
      color: 'danger'
    })
  } finally {
    submitting.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    targetNodeId.value = ''
    reason.value = ''
    fetchNodes()
  }
}, { immediate: true })
</script>
