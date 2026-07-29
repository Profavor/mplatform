<template>
  <va-modal
    :modelValue="show"
    @update:modelValue="$emit('close')"
    size="large"
    hide-default-actions
    no-outside-dismiss
    class="survivorship-rules-modal"
  >
    <template #header>
      <div class="modal-header bg-gradient-to-r from-purple-600 to-indigo-600 text-white p-4 rounded-t-lg flex justify-between items-center">
        <h2 class="text-xl font-bold m-0">{{ $t('survivorship.title') }}</h2>
        <va-button preset="plain" icon="close" color="white" @click="$emit('close')" />
      </div>
    </template>

    <div class="modal-body p-4 max-h-[70vh] overflow-y-auto">
      <div class="flex justify-between items-center mb-4">
        <p class="text-sm text-gray-600">{{ $t('survivorship.description') }}</p>
        <va-button icon="add" color="success" size="small" @click="addRule">
          {{ $t('survivorship.add_rule') }}
        </va-button>
      </div>

      <va-data-table
        :items="rules"
        :columns="columns"
        striped
        hoverable
      >
        <template #cell(fieldKey)="{ rowData }">
          <va-select
            v-if="fieldOptions.length > 0"
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

        <template #cell(actions)="{ rowData, rowIndex }">
          <va-button
            preset="plain"
            icon="delete"
            color="danger"
            size="small"
            @click="removeRule(rowIndex)"
          />
        </template>
      </va-data-table>
    </div>

    <template #footer>
      <div class="flex justify-end gap-3 p-4 border-t">
        <va-button preset="secondary" color="gray" @click="$emit('close')">
          {{ $t('common.cancel') }}
        </va-button>
        <va-button color="primary" @click="saveRules" :loading="isSaving">
          {{ $t('common.save') }}
        </va-button>
      </div>
    </template>
  </va-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'vuestic-ui'
import { useCustomFetch } from '~/composables/useCustomFetch'

const props = defineProps<{
  show: boolean
  domainId: string
}>()

const emit = defineEmits(['close', 'saved'])
const { t } = useI18n()
const { init } = useToast()
const { customFetch } = useCustomFetch()

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

const loadRules = async () => {
  if (!props.domainId) return
  try {
    const [rulesRes, fieldsRes] = await Promise.all([
      customFetch(`/api/records/domains/${props.domainId}/survivorship-rules`),
      customFetch(`/api/domains/${props.domainId}/fields`)
    ])
    rules.value = rulesRes || []
    domainFields.value = fieldsRes || []
  } catch (e) {
    rules.value = []
    domainFields.value = []
  }
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
  isSaving.value = true
  try {
    await customFetch(`/api/records/domains/${props.domainId}/survivorship-rules`, {
      method: 'PUT',
      body: rules.value
    })
    init({ message: t('survivorship.save_success') || '병합 생존 규칙이 저장되었습니다.', color: 'success' })
    emit('saved')
    emit('close')
  } catch (e) {
    init({ message: t('survivorship.save_fail') || '생존 규칙 저장 중 오류가 발생했습니다.', color: 'danger' })
  } finally {
    isSaving.value = false
  }
}

watch(() => props.show, (val) => {
  if (val) loadRules()
})

onMounted(() => {
  if (props.show) loadRules()
})
</script>

<style scoped>
.modal-header {
  margin: -1.25rem -1.25rem 1rem -1.25rem;
  border-radius: 0.5rem 0.5rem 0 0;
}
</style>
