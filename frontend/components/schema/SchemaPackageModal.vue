<template>
  <va-modal
    v-model="show"
    :title="$t('schema_package')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <!-- Tabs -->
      <div style="display: flex; border-bottom: 1px solid var(--va-background-border); gap: 1rem;">
        <button
          type="button"
          :style="{
            padding: '0.6rem 1rem',
            border: 'none',
            background: 'transparent',
            cursor: 'pointer',
            fontWeight: activeTab === 'export' ? 'bold' : 'normal',
            borderBottom: activeTab === 'export' ? '2px solid var(--va-primary)' : 'none',
            color: activeTab === 'export' ? 'var(--va-primary)' : 'var(--va-text-secondary)'
          }"
          @click="activeTab = 'export'"
        >
          📤 {{ $t('export_package') }}
        </button>
        <button
          type="button"
          :style="{
            padding: '0.6rem 1rem',
            border: 'none',
            background: 'transparent',
            cursor: 'pointer',
            fontWeight: activeTab === 'import' ? 'bold' : 'normal',
            borderBottom: activeTab === 'import' ? '2px solid var(--va-primary)' : 'none',
            color: activeTab === 'import' ? 'var(--va-primary)' : 'var(--va-text-secondary)'
          }"
          @click="activeTab = 'import'"
        >
          📥 {{ $t('import_package') }}
        </button>
      </div>

      <!-- Tab 1: Export -->
      <div v-if="activeTab === 'export'" style="display: flex; flex-direction: column; gap: 1rem;">
        <va-alert color="info" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
          💡 {{ $t('package_export_desc') }}
        </va-alert>

        <va-card flat bordered style="padding: 1rem;">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <div>
              <div style="font-weight: 700; font-size: 1.1rem; color: var(--va-text-primary);">
                {{ domainName || $t('domain') }}
              </div>
              <div style="font-size: 0.85rem; color: var(--va-text-secondary); margin-top: 0.25rem;">
                분류 노드, 필드 정의, DQ 검칙, 중복 매칭 룰, 결재 서식 전체가 패키징됩니다.
              </div>
            </div>
            <va-button
              color="primary"
              icon="download"
              :loading="exporting"
              @click="downloadPackage"
            >
              {{ $t('export_download_json') }}
            </va-button>
          </div>
        </va-card>

        <div v-if="previewJson" style="display: flex; flex-direction: column; gap: 0.5rem;">
          <div style="font-weight: 600; font-size: 0.85rem; color: var(--va-text-secondary);">
            패키지 JSON 미리보기:
          </div>
          <pre style="background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 1rem; font-size: 0.8rem; max-height: 250px; overflow: auto;">{{ previewJson }}</pre>
        </div>
      </div>

      <!-- Tab 2: Import -->
      <div v-else-if="activeTab === 'import'" style="display: flex; flex-direction: column; gap: 1rem;">
        <va-alert color="warning" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
          ⚠️ {{ $t('package_import_desc') }}
        </va-alert>

        <div>
          <label style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">
            JSON 패키지 파일 선택 (.json)
          </label>
          <input
            type="file"
            accept=".json"
            @change="handleFileSelected"
            style="display: block; width: 100%; padding: 0.5rem; border: 1px dashed var(--va-background-border); border-radius: 8px; background: var(--va-background-element);"
          />
        </div>

        <div style="display: flex; align-items: center; gap: 0.5rem;">
          <va-checkbox v-model="overwrite" :label="$t('overwrite_existing')" />
        </div>

        <div v-if="importPreview" style="display: flex; flex-direction: column; gap: 0.5rem;">
          <div style="font-weight: 600; font-size: 0.85rem; color: var(--va-text-secondary);">
            불러온 패키지 정보:
          </div>
          <va-card flat bordered style="padding: 0.75rem 1rem; background: var(--va-background-element);">
            <div><b>도메인명:</b> {{ importPreview.domain?.name?.ko || importPreview.domain?.name?.en || '-' }}</div>
            <div style="font-size: 0.82rem; color: var(--va-text-secondary); margin-top: 0.25rem;">
              분류 노드 {{ importPreview.nodes?.length || 0 }}개 · 필드 {{ importPreview.fields?.length || 0 }}개 · DQ 룰 {{ importPreview.dqRules?.length || 0 }}개
            </div>
          </va-card>
        </div>

        <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem;">
          <va-button preset="secondary" @click="show = false">
            {{ $t('cancel') }}
          </va-button>
          <va-button
            color="primary"
            icon="upload"
            :disabled="!importPackageData || importing"
            :loading="importing"
            @click="submitImport"
          >
            {{ $t('import_upload_json') }}
          </va-button>
        </div>
      </div>

      <div v-if="activeTab === 'export'" style="display: flex; justify-content: flex-end; margin-top: 0.5rem;">
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
  domainName?: string
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

const activeTab = ref<'export' | 'import'>('export')
const exporting = ref(false)
const importing = ref(false)
const overwrite = ref(false)
const previewJson = ref<string>('')
const importPackageData = ref<any>(null)
const importPreview = ref<any>(null)

const downloadPackage = async () => {
  if (!props.domainId) return
  exporting.value = true
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/package/export`)
    if (res.data?.value) {
      const dataStr = JSON.stringify(res.data.value, null, 2)
      previewJson.value = dataStr

      // Trigger Browser Download
      const blob = new Blob([dataStr], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `domain_package_${props.domainId}_${new Date().toISOString().slice(0, 10)}.json`
      a.click()
      URL.revokeObjectURL(url)

      toast.init({
        message: '도메인 패키지 다운로드가 완료되었습니다.',
        color: 'success'
      })
    }
  } catch (e: any) {
    toast.init({
      message: e.message || 'Export failed',
      color: 'danger'
    })
  } finally {
    exporting.value = false
  }
}

const handleFileSelected = (event: any) => {
  const file = event.target.files?.[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = (e) => {
    try {
      const json = JSON.parse(e.target?.result as string)
      importPackageData.value = json
      importPreview.value = json
    } catch (err) {
      toast.init({
        message: '올바른 JSON 패키지 파일이 아닙니다.',
        color: 'danger'
      })
    }
  }
  reader.readAsText(file)
}

const submitImport = async () => {
  if (!importPackageData.value) return
  importing.value = true
  try {
    const res = await useCustomFetch(`/domains/package/import?overwrite=${overwrite.value}`, {
      method: 'POST',
      body: importPackageData.value
    })

    const data = res.data?.value
    if (data && data.success) {
      toast.init({
        message: t('import_success', { nodes: data.nodeCount, fields: data.fieldCount }),
        color: 'success'
      })
      emit('imported', data)
      show.value = false
    }
  } catch (err: any) {
    toast.init({
      message: err.message || t('import_failed'),
      color: 'danger'
    })
  } finally {
    importing.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    previewJson.value = ''
    importPackageData.value = null
    importPreview.value = null
    overwrite.value = false
  }
})
</script>
