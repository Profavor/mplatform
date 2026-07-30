<template>
  <div class="survivorship-container p-4">
    <!-- Header -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
      <div>
        <h1 style="font-size: 1.75rem; font-weight: 800; color: var(--va-text-primary); margin: 0; display: flex; align-items: center; gap: 0.5rem;">
          <va-icon name="alt_route" color="primary" size="32px" />
          <span>{{ $t('survivorship.title') || '생존 규칙 관리 (Survivorship Rules)' }}</span>
        </h1>
        <p style="color: var(--va-text-secondary); margin: 0.25rem 0 0 0; font-size: 0.9rem;">
          {{ $t('survivorship.description') || '중복 데이터(Duplicates)를 병합하여 골든 레코드를 생성할 때의 충돌 해결 전략을 정의합니다.' }}
        </p>
      </div>
      <div style="display: flex; gap: 0.75rem; align-items: center;">
        <va-select
          v-model="selectedDomainId"
          :options="domainOptions"
          value-by="value"
          label-by="label"
          placeholder="도메인 선택 (Select Domain)"
          style="min-width: 250px;"
          @update:modelValue="onDomainChange"
        />
        <va-button
          color="success"
          icon="add"
          :disabled="!selectedDomainId"
          @click="addRule"
        >
          {{ $t('survivorship.add_rule') || '규칙 추가' }}
        </va-button>
        <va-button
          color="primary"
          icon="save"
          :disabled="!selectedDomainId || isSaving"
          :loading="isSaving"
          @click="saveRules"
        >
          {{ $t('common.save') || '저장' }}
        </va-button>
      </div>
    </div>

    <!-- Rules Data Table -->
    <va-card>
      <va-card-title>
        <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
          <span>생존 규칙 목록 (총 {{ rules.length }}건)</span>
          <va-button preset="secondary" icon="refresh" size="small" @click="fetchData">새로고침</va-button>
        </div>
      </va-card-title>
      <va-card-content>
        <va-data-table
          :items="rules"
          :columns="columns"
          striped
          hoverable
          :no-data-html="selectedDomainId ? '등록된 생존 규칙이 없습니다. 규칙 추가 버튼을 눌러주세요.' : '상단에서 도메인을 선택해주세요.'"
        >
          <template #cell(fieldKey)="{ rowData }">
            <va-select
              v-if="domainFields.length > 0"
              v-model="rowData.fieldKey"
              :options="fieldOptions"
              value-by="value"
              dense
            />
            <va-input
              v-else
              v-model="rowData.fieldKey"
              placeholder="e.g. name, phone"
              dense
            />
          </template>

          <template #cell(strategy)="{ rowData }">
            <va-select
              v-model="rowData.strategy"
              :options="strategyOptions"
              value-by="value"
              dense
            />
          </template>

          <template #cell(priority)="{ rowData }">
            <va-input
              v-model.number="rowData.priority"
              type="number"
              placeholder="1"
              dense
              style="width: 80px;"
            />
          </template>

          <template #cell(actions)="{ rowIndex }">
            <va-button
              preset="plain"
              icon="delete"
              color="danger"
              size="small"
              @click="removeRule(rowIndex)"
            />
          </template>
        </va-data-table>
      </va-card-content>
    </va-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'vuestic-ui'
import { useCustomFetch } from '~/composables/useCustomFetch'

definePageMeta({
  layout: 'admin'
})

const { t } = useI18n()
const { init } = useToast()
const { customFetch } = useCustomFetch()

const selectedDomainId = ref<string | null>(null)
const domainOptions = ref<{label: string, value: string}[]>([])

const rules = ref<any[]>([])
const domainFields = ref<any[]>([])
const isSaving = ref(false)

const strategyOptions = computed(() => [
  { label: 'SOURCE_PRIORITY (소스 우선순위)', value: 'SOURCE_PRIORITY' },
  { label: 'MOST_RECENT (최신 수정 시각)', value: 'MOST_RECENT' },
  { label: 'MOST_COMPLETE (최고 완전성/길이)', value: 'MOST_COMPLETE' }
])

const fieldOptions = computed(() => {
  return domainFields.value.map((f: any) => ({
    label: `${f.name} (${f.key})`,
    value: f.key
  }))
})

const columns = computed(() => [
  { key: 'fieldKey', label: t('survivorship.field_key') || '필드 (Field Key)' },
  { key: 'strategy', label: t('survivorship.strategy') || '생존 전략 (Strategy)' },
  { key: 'priority', label: t('survivorship.priority') || '우선순위', width: 100 },
  { key: 'actions', label: '', width: 80 }
])

const loadDomains = async () => {
  try {
    const res = await customFetch('/api/domains')
    domainOptions.value = (res || []).map((d: any) => ({
      label: d.name ? (d.name.ko || d.name.en || d.name) : 'Unknown',
      value: d.id
    }))
  } catch (e) {
    console.error('Failed to load domains', e)
    init({ message: '도메인 목록을 불러오지 못했습니다.', color: 'danger' })
  }
}

const fetchData = async () => {
  if (!selectedDomainId.value) return
  try {
    const [rulesRes, fieldsRes] = await Promise.all([
      customFetch(`/api/records/domains/${selectedDomainId.value}/survivorship-rules`),
      customFetch(`/api/domains/${selectedDomainId.value}/fields`)
    ])
    rules.value = rulesRes || []
    domainFields.value = fieldsRes || []
  } catch (e) {
    rules.value = []
    domainFields.value = []
  }
}

const onDomainChange = () => {
  rules.value = []
  domainFields.value = []
  fetchData()
}

const addRule = () => {
  rules.value.push({
    fieldKey: domainFields.value[0]?.key || '',
    strategy: 'SOURCE_PRIORITY',
    priority: rules.value.length + 1
  })
}

const removeRule = (index: number) => {
  rules.value.splice(index, 1)
}

const saveRules = async () => {
  if (!selectedDomainId.value) return
  isSaving.value = true
  try {
    await customFetch(`/api/records/domains/${selectedDomainId.value}/survivorship-rules`, {
      method: 'PUT',
      body: rules.value
    })
    init({ message: t('survivorship.save_success') || '병합 생존 규칙이 저장되었습니다.', color: 'success' })
  } catch (e) {
    init({ message: t('survivorship.save_fail') || '생존 규칙 저장 중 오류가 발생했습니다.', color: 'danger' })
  } finally {
    isSaving.value = false
  }
}

onMounted(() => {
  loadDomains()
})
</script>

<style scoped>
.survivorship-container {
  max-width: 1200px;
  margin: 0 auto;
}
</style>
