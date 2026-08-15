<template>
  <va-modal
    v-model="show"
    :title="$t('bulk_import')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="info" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        💡 {{ $t('bulk_import_desc') }}
      </va-alert>

      <!-- File Select -->
      <div>
        <label style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">
          {{ $t('select_file') }}
        </label>
        <input
          type="file"
          accept=".csv,.json"
          :disabled="uploading"
          @change="handleFileSelected"
          style="display: block; width: 100%; padding: 0.5rem; border: 1px dashed var(--va-background-border); border-radius: 8px; background: var(--va-background-element);"
        />
      </div>

      <!-- File Summary -->
      <div v-if="parsedRows.length > 0" style="display: flex; justify-content: space-between; align-items: center; padding: 0.75rem 1rem; background: var(--va-background-element); border-radius: 8px; border: 1px solid var(--va-background-border);">
        <div>
          <div style="font-weight: 700; font-size: 0.95rem;">{{ selectedFileName }}</div>
          <div style="font-size: 0.82rem; color: var(--va-text-secondary); margin-top: 0.2rem;">
            총 <b>{{ parsedRows.length }}</b>개 행(Row) 감지됨
          </div>
        </div>
        <va-button
          color="primary"
          icon="upload_file"
          :loading="uploading"
          :disabled="uploading"
          @click="startImport"
        >
          {{ $t('start_upload') }}
        </va-button>
      </div>

      <!-- Progress / Result -->
      <div v-if="jobResult" style="display: flex; flex-direction: column; gap: 0.75rem;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-weight: 700; font-size: 0.95rem;">
            진행 상태:
            <va-badge
              :text="jobResult.status"
              :color="jobResult.status === 'COMPLETED' ? 'success' : 'warning'"
              size="small"
            />
          </span>
          <span style="font-size: 0.85rem; color: var(--va-text-secondary);">
            성공: <b style="color: var(--va-success);">{{ jobResult.successCount }}</b>건 /
            실패: <b style="color: var(--va-danger);">{{ jobResult.errorCount }}</b>건
          </span>
        </div>

        <va-progress-bar
          :model-value="jobResult.progressPercentage || 100"
          :color="jobResult.errorCount > 0 ? 'warning' : 'success'"
          size="medium"
          show-percent
        />

        <!-- Error Details List -->
        <div v-if="jobResult.errorDetails && jobResult.errorDetails.length > 0" style="margin-top: 0.5rem;">
          <div style="font-weight: 600; font-size: 0.85rem; color: var(--va-danger); margin-bottom: 0.35rem;">
            ⚠️ {{ $t('error_details') }} ({{ jobResult.errorDetails.length }}건):
          </div>
          <div style="max-height: 160px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
            <table style="width: 100%; border-collapse: collapse; font-size: 0.8rem; text-align: left;">
              <thead>
                <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                  <th style="padding: 0.4rem 0.6rem; width: 60px;">{{ $t('row_number') }}</th>
                  <th style="padding: 0.4rem 0.6rem; width: 100px;">식별자</th>
                  <th style="padding: 0.4rem 0.6rem;">{{ $t('error_reason') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="err in jobResult.errorDetails" :key="err.rowNumber" style="border-bottom: 1px solid var(--va-background-border);">
                  <td style="padding: 0.35rem 0.6rem; font-weight: bold;">{{ err.rowNumber }}</td>
                  <td style="padding: 0.35rem 0.6rem;">{{ err.recordKey || '-' }}</td>
                  <td style="padding: 0.35rem 0.6rem; color: var(--va-danger);">{{ err.errorMessage }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem;">
        <va-button preset="secondary" @click="show = false">
          {{ $t('close') }}
        </va-button>
      </div>
    </div>
  </va-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'vuestic-ui'
import { useCustomFetch } from '~/composables/useCustomFetch'

const props = defineProps<{
  modelValue: boolean
  domainId?: string
  nodeId?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'imported', result: any): void
}>()

const { t } = useI18n()
const toast = useToast()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const uploading = ref(false)
const selectedFileName = ref('')
const parsedRows = ref<any[]>([])
const jobResult = ref<any>(null)

const handleFileSelected = (event: any) => {
  const file = event.target.files?.[0]
  if (!file) return

  selectedFileName.value = file.name
  jobResult.value = null

  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const text = e.target?.result as string
      if (file.name.endsWith('.json')) {
        const json = JSON.parse(text)
        parsedRows.value = Array.isArray(json) ? json : [json]
      } else {
        // Simple CSV Parser
        const lines = text.split(/\r?\n/).filter(l => l.trim().length > 0)
        if (lines.length > 1) {
          const headers = lines[0].split(',').map(h => h.trim().replace(/^["']|["']$/g, ''))
          const rows: any[] = []
          for (let i = 1; i < lines.length; i++) {
            const cols = lines[i].split(',').map(c => c.trim().replace(/^["']|["']$/g, ''))
            const rowObj: any = {}
            headers.forEach((h, idx) => {
              rowObj[h] = cols[idx] || ''
            })
            rows.push(rowObj)
          }
          parsedRows.value = rows
        }
      }
    } catch (err) {
      toast.init({
        message: '파일 파싱 중 오류가 발생했습니다.',
        color: 'danger'
      })
    }
  }
  reader.readAsText(file)
}

const startImport = async () => {
  if (!props.domainId || parsedRows.value.length === 0) return
  uploading.value = true
  try {
    const res = await useCustomFetch('/records/bulk-import/jobs', {
      method: 'POST',
      body: {
        domainId: props.domainId,
        nodeId: props.nodeId || null,
        fileName: selectedFileName.value,
        rows: parsedRows.value
      }
    })

    if (res.data?.value) {
      jobResult.value = res.data.value
      toast.init({
        message: t('bulk_import_success', {
          success: res.data.value.successCount,
          errors: res.data.value.errorCount
        }),
        color: res.data.value.errorCount > 0 ? 'warning' : 'success'
      })
      emit('imported', res.data.value)
    }
  } catch (e: any) {
    toast.init({
      message: '일괄 업로드 실패: ' + (e.message || ''),
      color: 'danger'
    })
  } finally {
    uploading.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    parsedRows.value = []
    selectedFileName.value = ''
    jobResult.value = null
  }
})
</script>
