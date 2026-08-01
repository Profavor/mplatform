<template>
  <div style="display: flex; flex-direction: column; gap: 1.25rem; padding-bottom: 2rem; width: 100%;">
    <!-- Top Action Bar -->
    <div style="display: flex; justify-content: space-between; align-items: center; background: var(--va-background-primary); padding: 1rem 1.25rem; border-radius: 12px; border: 1px solid var(--va-background-border); box-shadow: 0 2px 8px rgba(0,0,0,0.04);">
      <div style="display: flex; align-items: center; gap: 0.75rem;">
        <va-icon name="published_with_changes" size="large" color="primary" />
        <div>
          <h2 style="font-weight: 700; font-size: 1.35rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.5rem;">
            {{ $t('survivorship.title') || '서바이버십 규칙 관리' }}
            <va-badge text="Golden Record" color="primary" size="small" />
          </h2>
          <span style="font-size: 0.85rem; color: var(--va-text-secondary);">
            {{ $t('survivorship.description') || '중복 레코드 병합 시 골든 레코드를 생성하기 위한 필드별 생존 우선순위 및 충돌 해결 전략을 설정합니다.' }}
          </span>
        </div>
      </div>

      <div style="display: flex; gap: 0.75rem; align-items: center;">
        <va-select
          v-model="selectedDomainId"
          :options="domainOptions"
          value-by="value"
          text-by="text"
          :placeholder="$t('survivorship.select_domain_placeholder') || '도메인을 선택하세요'"
          style="min-width: 220px;"
          dense
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
              {{ $t('survivorship.save_settings') || '설정 저장' }}
            </va-button>
      </div>
    </div>

    <!-- Stats & Tip Banner (Shown when domain selected) -->
    <div v-if="selectedDomainId" style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 1rem;">
      <va-card class="stat-card">
        <va-card-content class="stat-content">
          <va-icon name="rule" color="primary" size="32px" />
          <div>
            <div class="stat-value">{{ rules.length }}</div>
            <div class="stat-label">{{ $t('survivorship.stat_registered_rules') || '등록된 서바이버십 규칙' }}</div>
          </div>
        </va-card-content>
      </va-card>

      <va-card class="stat-card">
        <va-card-content class="stat-content">
          <va-icon name="category" color="info" size="32px" />
          <div>
            <div class="stat-value">{{ domainFields.length }}</div>
            <div class="stat-label">{{ $t('survivorship.stat_available_fields') || '사용 가능한 도메인 필드' }}</div>
          </div>
        </va-card-content>
      </va-card>

      <va-card class="stat-card">
        <va-card-content class="stat-content">
          <va-icon name="verified" color="success" size="32px" />
          <div>
            <div class="stat-value">{{ currentDomainName }}</div>
            <div class="stat-label">{{ $t('survivorship.stat_current_domain') || '현재 선택된 도메인' }}</div>
          </div>
        </va-card-content>
      </va-card>
    </div>

    <!-- Info Guidance Banner -->
    <va-alert color="info" outline>
      <template #icon>
        <va-icon name="info" />
      </template>
      <div>
        <strong>💡 {{ $t('survivorship.guide_title') || '서바이버십(Survivorship) 전략 가이드' }}</strong>:
        <span>{{ $t('survivorship.guide_text') || 'SOURCE_PRIORITY(원천 소스 시스템 우선), MOST_RECENT(최신 수정 일시 기준), MOST_COMPLETE(가장 긴 완전 데이터 기준) 중 도메인별 최적의 병합 규칙을 구성하세요.' }}</span>
      </div>
    </va-alert>

    <!-- Rules Table Card -->
    <va-card style="flex: 1; display: flex; flex-direction: column; overflow: hidden;">
      <va-card-title class="flex justify-between items-center">
        <div class="flex items-center gap-2 font-bold text-lg">
          <va-icon name="list_alt" color="primary" />
          <span>{{ $t('survivorship.rule_list') || '규칙 목록' }}</span>
          <va-chip v-if="selectedDomainId" size="small" color="primary">{{ rules.length }}{{ $t('survivorship.items_count') || '개 항목' }}</va-chip>
        </div>
        <va-button preset="secondary" icon="refresh" size="small" :disabled="!selectedDomainId" @click="fetchData">
          {{ $t('survivorship.refresh') || '새로고침' }}
        </va-button>
      </va-card-title>

      <va-card-content style="padding: 0;">
        <va-data-table
          :items="rules"
          :columns="columns"
          striped
          hoverable
          class="rules-table"
          style="width: 100%;"
        >
          <!-- Priority Rank Badge -->
          <template #cell(priority)="{ rowData, rowIndex }">
            <div class="flex items-center gap-2">
              <span class="priority-pill">#{{ rowData.priority || (rowIndex + 1) }}</span>
              <va-input
                v-model.number="rowData.priority"
                type="number"
                dense
                style="width: 70px;"
              />
            </div>
          </template>

          <!-- Field Select -->
          <template #cell(fieldKey)="{ rowData }">
            <va-select
              v-if="domainFields.length > 0"
              v-model="rowData.fieldKey"
              :options="fieldOptions"
              value-by="value"
              text-by="text"
              dense
              class="w-full"
            />
            <va-input
              v-else
              v-model="rowData.fieldKey"
              placeholder="e.g. name, phone"
              dense
            />
          </template>

          <!-- Strategy Select with Badges -->
          <template #cell(strategy)="{ rowData }">
            <div class="flex items-center gap-2">
              <va-badge
                :color="getStrategyColor(rowData.strategy)"
                :text="getStrategyLabel(rowData.strategy)"
              />
              <va-select
                v-model="rowData.strategy"
                :options="strategyOptions"
                value-by="value"
                text-by="text"
                dense
                style="min-width: 220px;"
              />
            </div>
          </template>

          <!-- Actions -->
          <template #cell(actions)="{ rowIndex }">
            <va-button
              preset="plain"
              icon="delete"
              color="danger"
              size="small"
              title="삭제"
              @click="removeRule(rowIndex)"
            />
          </template>

          <!-- Custom Empty State -->
          <template #bodyAppend v-if="rules.length === 0">
            <tr>
              <td colspan="4" class="text-center py-8">
                <div class="empty-state">
                  <va-icon name="route" size="48px" color="secondary" class="mb-2" />
                  <p class="text-lg font-bold text-gray-600 mb-1">
                    {{ selectedDomainId ? ($t('survivorship.empty_no_rules') || '등록된 서바이버십 규칙이 없습니다.') : ($t('survivorship.empty_no_domain') || '상단에서 도메인을 먼저 선택해 주세요.') }}
                  </p>
                  <p class="text-sm text-gray-400 mb-4">
                    {{ selectedDomainId ? ($t('survivorship.empty_sub_rules') || '우측 상단의 "+ 규칙 추가" 버튼을 클릭하여 새로운 병합 규칙을 추가할 수 있습니다.') : ($t('survivorship.empty_sub_domain') || '도메인을 선택하면 해당 도메인의 스키마 필드 및 설정된 규칙 목록이 표시됩니다.') }}
                  </p>
                  <va-button v-if="selectedDomainId" color="success" icon="add" size="small" @click="addRule">
                    {{ $t('survivorship.add_first_rule') || '첫 번째 규칙 추가하기' }}
                  </va-button>
                </div>
              </td>
            </tr>
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

const { t } = useI18n()
const { init } = useToast()
const { customFetch } = useCustomFetch()

const domainStore = useDomain()
const selectedDomainId = ref<string | null>(null)
const domainOptions = computed(() => domainStore.domainOptions.value)

const rules = ref<any[]>([])
const domainFields = ref<any[]>([])
const isSaving = ref(false)

const currentDomainName = computed(() => {
  if (!selectedDomainId.value) return '-'
  return domainStore.getDomainName(selectedDomainId.value) || '-'
})

const strategyOptions = computed(() => [
  { label: t('survivorship.source_priority') || 'SOURCE_PRIORITY (원천 소스 우선)', text: t('survivorship.source_priority') || 'SOURCE_PRIORITY (원천 소스 우선)', value: 'SOURCE_PRIORITY' },
  { label: t('survivorship.most_recent') || 'MOST_RECENT (최신 수정 시각)', text: t('survivorship.most_recent') || 'MOST_RECENT (최신 수정 시각)', value: 'MOST_RECENT' },
  { label: t('survivorship.most_complete') || 'MOST_COMPLETE (최고 완전성/길이)', text: t('survivorship.most_complete') || 'MOST_COMPLETE (최고 완전성/길이)', value: 'MOST_COMPLETE' }
])

const getStrategyColor = (strategy: string) => {
  switch (strategy) {
    case 'SOURCE_PRIORITY': return 'primary'
    case 'MOST_RECENT': return 'warning'
    case 'MOST_COMPLETE': return 'success'
    default: return 'secondary'
  }
}

const getStrategyLabel = (strategy: string) => {
  switch (strategy) {
    case 'SOURCE_PRIORITY': return 'SOURCE'
    case 'MOST_RECENT': return 'RECENT'
    case 'MOST_COMPLETE': return 'COMPLETE'
    default: return strategy || 'DEFAULT'
  }
}

const fieldOptions = computed(() => {
  return domainFields.value.map((f: any) => {
    let pName = f.key || ''
    if (f.name) {
      if (typeof f.name === 'object') {
        pName = f.name.ko || f.name.en || f.key
      } else if (typeof f.name === 'string') {
        try {
          const parsed = JSON.parse(f.name)
          pName = parsed.ko || parsed.en || f.name
        } catch (e) {
          pName = f.name
        }
      }
    }
    const displayLabel = `${pName} (${f.key})`
    return {
      label: displayLabel,
      text: displayLabel,
      value: f.key
    }
  })
})

const columns = computed(() => [
  { key: 'priority', label: t('survivorship.priority') || '우선순위', width: '130px' },
  { key: 'fieldKey', label: t('survivorship.field_key') || '필드 (Field Key)', width: '30%' },
  { key: 'strategy', label: t('survivorship.strategy') || '생존 전략 (Strategy)' },
  { key: 'actions', label: '', width: '60px' }
])

const loadDomains = async () => {
  try {
    await domainStore.fetchDomains()
  } catch (e) {
    console.error('Failed to load domains', e)
    init({ message: t('survivorship.load_domains_fail') || '도메인 목록을 불러오지 못했습니다.', color: 'danger' })
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
    const payload = rules.value.map(r => ({
      fieldKey: r.fieldKey,
      strategy: r.strategy,
      priority: Number(r.priority) || 1
    }))
    await customFetch(`/api/records/domains/${selectedDomainId.value}/survivorship-rules`, {
      method: 'PUT',
      body: payload
    })
    init({ message: t('survivorship.save_success') || '병합 생존 규칙이 저장되었습니다.', color: 'success' })
    await fetchData()
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
.survivorship-page {
  width: 100%;
  height: 100%;
  padding: 1.25rem 1.5rem;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.header-card {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
}

.header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.header-title-box {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.icon-avatar {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  background: rgba(21, 78, 193, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 800;
  margin: 0;
  color: var(--va-text-primary);
  display: flex;
  align-items: center;
}

.page-subtitle {
  font-size: 0.88rem;
  color: var(--va-text-secondary);
  margin: 0.25rem 0 0 0;
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.domain-select {
  min-width: 260px;
}

.stat-card {
  border-radius: 10px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
}

.stat-value {
  font-size: 1.35rem;
  font-weight: 800;
  color: var(--va-text-primary);
}

.stat-label {
  font-size: 0.8rem;
  color: var(--va-text-secondary);
}

.priority-pill {
  display: inline-block;
  padding: 2px 8px;
  background: var(--va-background-element, #e2e8f0);
  border-radius: 12px;
  font-size: 0.78rem;
  font-weight: 700;
  color: var(--va-text-primary);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2.5rem 1rem;
}
</style>
