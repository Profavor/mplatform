<template>
  <va-modal
    v-model="show"
    :title="$t('domain_snapshot')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        💾 {{ $t('domain_snapshot_desc') }}
      </va-alert>

      <!-- Action Bar -->
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <span style="font-weight: 700; font-size: 0.9rem;">
          📦 저장된 스냅샷: {{ snapshots.length }}개
        </span>
        <va-button
          color="primary"
          icon="add"
          size="small"
          @click="showCreateForm = !showCreateForm"
        >
          {{ $t('create_snapshot') }}
        </va-button>
      </div>

      <!-- Create Form Collapsible -->
      <div
        v-if="showCreateForm"
        style="padding: 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; flex-direction: column; gap: 0.75rem;"
      >
        <div style="font-weight: 700; font-size: 0.88rem;">
          ➕ 신규 도메인 시점 스냅샷 생성
        </div>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem;">
          <va-input v-model="form.snapshotName" :label="$t('snapshot_name')" placeholder="예: 2026 3분기 정기 백업" />
          <va-input v-model="form.versionTag" :label="$t('version_tag')" placeholder="예: v1.5-prod" />
        </div>
        <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.25rem;">
          <va-button preset="secondary" size="small" @click="showCreateForm = false">
            {{ $t('cancel') }}
          </va-button>
          <va-button color="success" size="small" :loading="creating" @click="submitSnapshot">
            {{ $t('save') }}
          </va-button>
        </div>
      </div>

      <!-- Snapshots Table -->
      <va-inner-loading :loading="loading">
        <div style="max-height: 280px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
          <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
            <thead>
              <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                <th style="padding: 0.5rem 0.75rem;">{{ $t('snapshot_name') }}</th>
                <th style="padding: 0.5rem 0.75rem; width: 90px;">{{ $t('version_tag') }}</th>
                <th style="padding: 0.5rem 0.75rem; width: 80px;">보관 건수</th>
                <th style="padding: 0.5rem 0.75rem; width: 90px;">생성자</th>
                <th style="padding: 0.5rem 0.75rem; width: 130px;">생성 일시</th>
                <th style="padding: 0.5rem 0.75rem; width: 100px; text-align: center;">복원 액션</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="s in snapshots"
                :key="s.snapshotId"
                style="border-bottom: 1px solid var(--va-background-border);"
              >
                <td style="padding: 0.5rem 0.75rem; font-weight: 600;">{{ s.snapshotName }}</td>
                <td style="padding: 0.5rem 0.75rem;">
                  <va-badge :text="s.versionTag" color="primary" size="small" />
                </td>
                <td style="padding: 0.5rem 0.75rem;">{{ s.recordCount }}건</td>
                <td style="padding: 0.5rem 0.75rem; color: var(--va-text-secondary);">{{ s.createdBy }}</td>
                <td style="padding: 0.5rem 0.75rem; color: var(--va-text-secondary); font-size: 0.75rem;">
                  {{ formatDate(s.createdAt) }}
                </td>
                <td style="padding: 0.5rem 0.75rem; text-align: center;">
                  <va-button
                    color="warning"
                    size="small"
                    preset="secondary"
                    :loading="restoringId === s.snapshotId"
                    @click="handleRestore(s)"
                  >
                    {{ $t('restore_snapshot') }}
                  </va-button>
                </td>
              </tr>
              <tr v-if="snapshots.length === 0">
                <td colspan="6" style="text-align: center; padding: 2rem; color: var(--va-text-secondary);">
                  {{ $t('no_snapshots') }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </va-inner-loading>

      <div style="display: flex; justify-content: flex-end; margin-top: 0.5rem;">
        <va-button preset="secondary" @click="show = false">
          {{ $t('close') }}
        </va-button>
      </div>
    </div>
  </va-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { formatWithTimezone } from '~/composables/useTimezoneDate'

const props = defineProps<{
  modelValue: boolean
  domainId?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const snapshots = ref<any[]>([])
const loading = ref(false)
const creating = ref(false)
const showCreateForm = ref(false)
const restoringId = ref<string | null>(null)

const form = reactive({
  snapshotName: '',
  versionTag: ''
})

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return formatWithTimezone(dateStr, 'YYYY-MM-DD HH:mm')
}

const fetchSnapshots = async () => {
  if (!props.domainId) return
  loading.value = true
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/snapshots`)
    if (res.data?.value) {
      snapshots.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to fetch domain snapshots', e)
  } finally {
    loading.value = false
  }
}

const submitSnapshot = async () => {
  if (!props.domainId || !form.snapshotName) return
  creating.value = true
  try {
    const res = await useCustomFetch(`/domains/${props.domainId}/snapshots`, {
      method: 'POST',
      body: {
        snapshotName: form.snapshotName,
        versionTag: form.versionTag || 'v1.0'
      }
    })
    if (res.data?.value) {
      snapshots.value.unshift(res.data.value)
      showCreateForm.value = false
      form.snapshotName = ''
      form.versionTag = ''
    }
  } catch (e: any) {
    console.error('Failed to create snapshot', e)
  } finally {
    creating.value = false
  }
}

const handleRestore = async (s: any) => {
  const confirmed = window.confirm(t('confirm_restore', { name: s.snapshotName, tag: s.versionTag }))
  if (!confirmed) return

  restoringId.value = s.snapshotId
  try {
    await useCustomFetch(`/domains/snapshots/${s.snapshotId}/restore`, {
      method: 'POST'
    })
    alert('스냅샷 복원이 성공적으로 완료되었습니다.')
  } catch (e: any) {
    console.error('Failed to restore snapshot', e)
  } finally {
    restoringId.value = null
  }
}

watch(() => props.modelValue, (val) => {
  if (val) fetchSnapshots()
})
</script>
