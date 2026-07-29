<template>
  <div style="flex: 1; display: flex; flex-direction: column; padding: 1rem; overflow-y: auto;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
      <div>
        <h3 style="font-weight: 700; font-size: 1.1rem; color: var(--va-text-primary); margin: 0; display: flex; align-items: center; gap: 0.5rem;">
          <va-icon name="polyline" color="primary" />
          다축 분류체계 축 관리 (Classification Axes)
        </h3>
        <p style="font-size: 0.85rem; color: var(--va-text-secondary); margin: 0.25rem 0 0 0;">
          주 분류체계 외에 조직, 지역, 산업군 등 다차원 보조 분류 축을 도메인별로 관리합니다.
        </p>
      </div>

      <va-button icon="add" color="primary" size="small" @click="openCreateModal" :disabled="!domainId">
        분류 축 추가
      </va-button>
    </div>

    <va-data-table
      :items="axes"
      :columns="columns"
      striped
      hoverable
      no-data-html="등록된 보조 분류 축이 없습니다."
    >
      <template #cell(code)="{ rowData }">
        <span style="font-family: monospace; font-weight: 700;">{{ rowData.code }}</span>
      </template>

      <template #cell(name)="{ rowData }">
        <span style="font-weight: 600;">{{ rowData.name }}</span>
      </template>

      <template #cell(actions)="{ rowData }">
        <div style="display: flex; gap: 0.25rem;">
          <va-button preset="plain" icon="edit" color="primary" size="small" @click="openEditModal(rowData)" />
          <va-button preset="plain" icon="delete" color="danger" size="small" @click="deleteAxis(rowData)" />
        </div>
      </template>
    </va-data-table>

    <!-- Modal Form -->
    <va-modal
      v-model="showModal"
      :title="isEditMode ? '분류 축 수정' : '새 분류 축 추가'"
      hide-default-actions
      size="small"
      no-outside-dismiss
    >
      <div style="padding: 0.5rem 0;">
        <va-input
          v-model="form.code"
          label="축 코드 (Axis Code)"
          placeholder="예: REGION, REGULATION"
          class="mb-3"
          required
        />

        <va-input
          v-model="form.name"
          label="축 이름 (Axis Name)"
          placeholder="예: 지역별 분류 축"
          class="mb-3"
          required
        />

        <va-input
          v-model="form.description"
          label="설명 (Description)"
          placeholder="보조 축 설명"
          class="mb-3"
        />

        <va-input
          v-model.number="form.sortOrder"
          type="number"
          label="정렬 순서 (Sort Order)"
          class="mb-3"
        />
      </div>

      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem;">
          <va-button preset="secondary" @click="showModal = false">취소</va-button>
          <va-button color="primary" @click="saveAxis" :loading="isSaving">저장</va-button>
        </div>
      </template>
    </va-modal>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useToast, useModal } from 'vuestic-ui'
import { useCustomFetch } from '~/composables/useCustomFetch'

const props = defineProps({
  domainId: { type: String, default: '' }
})

const { init } = useToast()
const { confirm } = useModal()
const { customFetch } = useCustomFetch()

const axes = ref([])
const showModal = ref(false)
const isEditMode = ref(false)
const isSaving = ref(false)
const editingAxisId = ref(null)

const form = ref({
  code: '',
  name: '',
  description: '',
  sortOrder: 1
})

const columns = computed(() => [
  { key: 'code', label: '축 코드', width: 140 },
  { key: 'name', label: '축 이름', width: 180 },
  { key: 'description', label: '설명' },
  { key: 'sortOrder', label: '정렬순서', width: 90 },
  { key: 'actions', label: '', width: 80 }
])

const loadAxes = async () => {
  if (!props.domainId) {
    axes.value = []
    return
  }
  try {
    const res = await customFetch(`/api/domains/${props.domainId}/axes`)
    axes.value = res || []
  } catch (e) {
    axes.value = []
  }
}

const openCreateModal = () => {
  isEditMode.value = false
  editingAxisId.value = null
  form.value = {
    code: '',
    name: '',
    description: '',
    sortOrder: axes.value.length + 1
  }
  showModal.value = true
}

const openEditModal = (axis) => {
  isEditMode.value = true
  editingAxisId.value = axis.id
  form.value = {
    code: axis.code,
    name: axis.name,
    description: axis.description || '',
    sortOrder: axis.sortOrder || 1
  }
  showModal.value = true
}

const saveAxis = async () => {
  if (!form.value.code || !form.value.name) {
    init({ message: '코드와 축 이름을 입력해 주세요.', color: 'warning' })
    return
  }

  isSaving.value = true
  try {
    if (isEditMode.value) {
      await customFetch(`/api/domains/${props.domainId}/axes/${editingAxisId.value}`, {
        method: 'PUT',
        body: form.value
      })
      init({ message: '분류 축이 수정되었습니다.', color: 'success' })
    } else {
      await customFetch(`/api/domains/${props.domainId}/axes`, {
        method: 'POST',
        body: form.value
      })
      init({ message: '새 분류 축이 추가되었습니다.', color: 'success' })
    }
    showModal.value = false
    loadAxes()
  } catch (e) {
    init({ message: '분류 축 저장 중 오류가 발생했습니다.', color: 'danger' })
  } finally {
    isSaving.value = false
  }
}

const deleteAxis = async (axis) => {
  const isConfirmed = await confirm({
    title: '분류 축 삭제',
    message: `[${axis.name}] 축을 삭제하시겠습니까?`,
    okText: '삭제',
    cancelText: '취소'
  })
  if (!isConfirmed) return

  try {
    await customFetch(`/api/domains/${props.domainId}/axes/${axis.id}`, {
      method: 'DELETE'
    })
    init({ message: '분류 축이 삭제되었습니다.', color: 'success' })
    loadAxes()
  } catch (e) {
    init({ message: '분류 축 삭제 실패', color: 'danger' })
  }
}

watch(() => props.domainId, (val) => {
  if (val) loadAxes()
}, { immediate: true })

onMounted(() => {
  if (props.domainId) loadAxes()
})
</script>
