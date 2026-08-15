<template>
  <va-modal
    v-model="show"
    :title="$t('business_glossary')"
    size="large"
    hide-default-actions
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; padding: 0.5rem;">
      <va-alert color="primary" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        📖 {{ $t('business_glossary_desc') }}
      </va-alert>

      <!-- Search & Recommendation Bar -->
      <div style="display: flex; gap: 0.5rem; align-items: center;">
        <va-input
          v-model="searchKeyword"
          placeholder="용어명, 약어, 코드 검색 또는 추천"
          style="flex: 1;"
          @update:model-value="handleSearch"
        >
          <template #prependInner>
            <va-icon name="search" color="secondary" />
          </template>
        </va-input>

        <va-button
          color="primary"
          icon="add"
          size="small"
          @click="showCreateForm = !showCreateForm"
        >
          {{ $t('add_term') }}
        </va-button>
      </div>

      <!-- Create Form Inline Collapsible -->
      <div
        v-if="showCreateForm"
        style="padding: 1rem; border-radius: 8px; border: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; flex-direction: column; gap: 0.75rem;"
      >
        <div style="font-weight: 700; font-size: 0.9rem;">
          ➕ 신규 표준 용어 등록
        </div>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem;">
          <va-input v-model="form.termNameKo" label="용어명 (한글)" placeholder="예: 사업자등록번호" />
          <va-input v-model="form.termNameEn" label="용어명 (영문)" placeholder="예: Business Registration No" />
          <va-input v-model="form.termCode" label="표준 용어 코드" placeholder="예: BIZ_NO" />
          <va-input v-model="form.abbreviation" label="표준 약어" placeholder="예: BRN" />
          <va-input v-model="form.synonyms" label="동의어/유의어 (콤마 구분)" placeholder="예: 사업자번호,등록번호" />
          <va-select
            v-model="form.sensitivityLevel"
            :options="['GENERAL', 'SENSITIVE', 'CRITICAL']"
            label="보안/민감도 등급"
          />
        </div>
        <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.25rem;">
          <va-button preset="secondary" size="small" @click="showCreateForm = false">
            {{ $t('cancel') }}
          </va-button>
          <va-button color="success" size="small" :loading="creating" @click="submitTerm">
            {{ $t('save') }}
          </va-button>
        </div>
      </div>

      <!-- Term Table -->
      <va-inner-loading :loading="loading">
        <div style="max-height: 300px; overflow-y: auto; border: 1px solid var(--va-background-border); border-radius: 8px;">
          <table style="width: 100%; border-collapse: collapse; font-size: 0.82rem; text-align: left;">
            <thead>
              <tr style="background: var(--va-background-element); border-bottom: 1px solid var(--va-background-border);">
                <th style="padding: 0.5rem 0.75rem;">{{ $t('term_code') }}</th>
                <th style="padding: 0.5rem 0.75rem;">{{ $t('term_name') }}</th>
                <th style="padding: 0.5rem 0.75rem;">{{ $t('abbreviation') }}</th>
                <th style="padding: 0.5rem 0.75rem;">{{ $t('synonyms') }}</th>
                <th style="padding: 0.5rem 0.75rem; width: 100px;">{{ $t('sensitivity_level') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="t in filteredTerms" :key="t.id" style="border-bottom: 1px solid var(--va-background-border);">
                <td style="padding: 0.5rem 0.75rem; font-weight: 700; color: var(--va-primary);">{{ t.termCode }}</td>
                <td style="padding: 0.5rem 0.75rem;">{{ formatTermName(t.termName) }}</td>
                <td style="padding: 0.5rem 0.75rem;">{{ t.abbreviation || '-' }}</td>
                <td style="padding: 0.5rem 0.75rem; color: var(--va-text-secondary);">{{ t.synonyms || '-' }}</td>
                <td style="padding: 0.5rem 0.75rem;">
                  <va-badge
                    :text="t.sensitivityLevel"
                    :color="getSensitivityColor(t.sensitivityLevel)"
                    size="small"
                  />
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
import { formatMultilingual } from '~/composables/useMultilingual'

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

const terms = ref<any[]>([])
const searchKeyword = ref('')
const loading = ref(false)
const creating = ref(false)
const showCreateForm = ref(false)

const form = reactive({
  termNameKo: '',
  termNameEn: '',
  termCode: '',
  abbreviation: '',
  synonyms: '',
  sensitivityLevel: 'GENERAL'
})

const formatTermName = (nameObj: any) => {
  if (!nameObj) return ''
  return formatMultilingual(nameObj)
}

const getSensitivityColor = (level: string) => {
  switch (level) {
    case 'CRITICAL': return 'danger'
    case 'SENSITIVE': return 'warning'
    default: return 'info'
  }
}

const filteredTerms = computed(() => {
  if (!searchKeyword.value.trim()) return terms.value
  const q = searchKeyword.value.toLowerCase()
  return terms.value.filter(item =>
    item.termCode?.toLowerCase().includes(q) ||
    item.termName?.ko?.toLowerCase().includes(q) ||
    item.termName?.en?.toLowerCase().includes(q) ||
    item.abbreviation?.toLowerCase().includes(q) ||
    item.synonyms?.toLowerCase().includes(q)
  )
})

const fetchTerms = async () => {
  loading.value = true
  try {
    const url = props.domainId ? `/business-terms?domainId=${props.domainId}` : '/business-terms'
    const res = await useCustomFetch(url)
    if (res.data?.value) {
      terms.value = res.data.value
    }
  } catch (e: any) {
    console.error('Failed to fetch terms', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // Client-side filtering via computed
}

const submitTerm = async () => {
  if (!form.termCode || !form.termNameKo) return
  creating.value = true
  try {
    const res = await useCustomFetch('/business-terms', {
      method: 'POST',
      body: {
        termCode: form.termCode,
        termName: { ko: form.termNameKo, en: form.termNameEn },
        abbreviation: form.abbreviation,
        synonyms: form.synonyms,
        sensitivityLevel: form.sensitivityLevel,
        domainId: props.domainId
      }
    })
    if (res.data?.value) {
      terms.value.unshift(res.data.value)
      showCreateForm.value = false
      form.termCode = ''
      form.termNameKo = ''
      form.termNameEn = ''
      form.abbreviation = ''
      form.synonyms = ''
    }
  } catch (e: any) {
    console.error('Failed to create term', e)
  } finally {
    creating.value = false
  }
}

watch(() => props.modelValue, (val) => {
  if (val) fetchTerms()
})
</script>
