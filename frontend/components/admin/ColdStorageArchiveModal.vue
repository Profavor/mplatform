<template>
  <va-modal
    v-model="show"
    :title="$t('cold_storage')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        🧊 {{ $t('cold_storage_desc') }}
      </va-alert>

      <!-- Action & Creation Bar -->
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <span style="font-weight: 700; font-size: 0.85rem;">동결 아카이브 패키지 ({{ archives.length }})</span>
        <va-button color="success" size="small" :loading="creating" @click="createArchive">
          + {{ $t('create_archive') }}
        </va-button>
      </div>

      <!-- DR Simulation Result Alert -->
      <div v-if="drResult" style="padding: 0.75rem 1rem; border-radius: 8px; background: rgba(32, 191, 107, 0.1); border: 1px solid var(--va-success); font-size: 0.82rem; font-weight: 600;">
        {{ drResult.message }} (복원 소요 시간: {{ drResult.drDurationMs }}ms)
      </div>

      <!-- Archive Table -->
      <va-inner-loading :loading="loading">
        <div v-if="archives?.length > 0" style="max-height: 280px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
          <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
            <thead>
              <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                <th style="padding: 0.5rem 0.75rem;">패키지 ID</th>
                <th style="padding: 0.5rem 0.75rem;">{{ $t('archive_name') }}</th>
                <th style="padding: 0.5rem 0.75rem;">규모 / 압축률</th>
                <th style="padding: 0.5rem 0.75rem; width: 110px; text-align: center;">{{ $t('simulate_dr') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="a in archives"
                :key="a.archiveId"
                style="border-bottom: 1px solid var(--va-background-border);"
              >
                <td style="padding: 0.5rem 0.75rem; font-weight: 700; font-family: monospace; font-size: 0.78rem;">
                  {{ a.archiveId }}
                </td>
                <td style="padding: 0.5rem 0.75rem;">
                  <div style="font-weight: 600;">{{ a.archiveName }}</div>
                  <div style="font-size: 0.72rem; color: var(--va-text-secondary); font-family: monospace;">SHA-256: {{ a.checksumSha256.substring(0, 16) }}...</div>
                </td>
                <td style="padding: 0.5rem 0.75rem;">
                  <div>{{ a.domainCount }}개 도메인, {{ a.recordCount.toLocaleString() }}건</div>
                  <div style="font-size: 0.72rem; color: var(--va-primary);">{{ a.compressionRatio }}</div>
                </td>
                <td style="padding: 0.5rem 0.75rem; text-align: center;">
                  <va-button
                    color="warning"
                    size="small"
                    preset="secondary"
                    :loading="simulatingId === a.archiveId"
                    @click="simulateDr(a.archiveId)"
                  >
                    {{ $t('simulate_dr') }}
                  </va-button>
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

const archives = ref<any[]>([])
const drResult = ref<any>(null)
const loading = ref(false)
const creating = ref(false)
const simulatingId = ref<string | null>(null)

const loadArchives = async () => {
  loading.value = true
  try {
    const res = await useCustomFetch('/system/archives')
    if (res.data?.value) {
      archives.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to load archives', e)
  } finally {
    loading.value = false
  }
}

const createArchive = async () => {
  creating.value = true
  try {
    const res = await useCustomFetch('/system/archives', {
      method: 'POST',
      body: {
        archiveName: '전사 마스터 데이터 원클릭 동결 아카이브',
        encrypt: true
      }
    })
    if (res.data?.value) {
      await loadArchives()
    }
  } catch (e: any) {
    console.error('Failed to create archive', e)
  } finally {
    creating.value = false
  }
}

const simulateDr = async (archiveId: string) => {
  simulatingId.value = archiveId
  drResult.value = null
  try {
    const res = await useCustomFetch(`/system/archives/${archiveId}/dr-simulate`, {
      method: 'POST'
    })
    if (res.data?.value) {
      drResult.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to simulate DR', e)
  } finally {
    simulatingId.value = null
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    drResult.value = null
    loadArchives()
  }
})
</script>
