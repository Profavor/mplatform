<template>
  <div class="approval-route-builder">
    <div class="route-header">
      <div class="header-title-group">
        <va-icon name="alt_route" size="small" color="primary" />
        <span class="header-title">{{ $t('inbox.approval_route_setting') }}</span>
      </div>
      <va-button
        preset="secondary"
        size="small"
        icon="add"
        @click="addStep"
      >
        + {{ $t('inbox.add_step') }}
      </va-button>
    </div>

    <!-- Approval Route Steps Timeline / List -->
    <div class="route-steps-container">
      <!-- Fixed Step 0: Drafter (Current User) -->
      <div class="route-step-card drafter-card">
        <div class="step-badge-area">
          <va-badge :text="$t('inbox.drafter')" color="info" />
        </div>
        <div class="step-info-area">
          <div class="step-user-info">
            <va-avatar size="small" color="info">{{ drafterInitials }}</va-avatar>
            <span class="user-name">{{ drafterName }}</span>
            <span class="user-meta" v-if="drafterDepartment">{{ drafterDepartment }}</span>
          </div>
        </div>
        <div class="step-actions-area">
          <va-chip size="small" flat color="info">{{ $t('inbox.drafter') }}</va-chip>
        </div>
      </div>

      <!-- Arrow Divider -->
      <div class="step-flow-arrow" v-if="steps.length > 0">
        <va-icon name="arrow_downward" size="18px" color="secondary" />
      </div>

      <!-- Configurable Step Groups (Grouped by stepOrder for Parallel Approval/Agreement) -->
      <div
        v-for="(group, groupIndex) in stepGroups"
        :key="group.stepOrder"
        class="step-group-block"
      >
        <div class="group-header">
          <span class="group-order-tag">{{ $t('inbox.step_order_label', { order: group.stepOrder }) }}</span>
          <span v-if="group.items.length > 1" class="parallel-indicator">
            <va-icon name="call_split" size="14px" color="warning" />
            {{ group.items[0].stepType === 'CONSENSUS' ? $t('inbox.parallel_consensus') : $t('inbox.parallel_approval') }}
          </span>
          <div class="group-header-actions">
            <!-- Parallel Add -->
            <va-button
              preset="secondary"
              size="small"
              icon="add"
              class="mini-action-btn"
              :title="$t('inbox.add_parallel_step')"
              @click="addParallelStep(group.stepOrder)"
            >
              {{ $t('inbox.add_parallel_step') }}
            </va-button>
            <!-- Move Up -->
            <va-button
              preset="secondary"
              size="small"
              icon="arrow_upward"
              class="mini-action-btn"
              :disabled="groupIndex === 0"
              :title="$t('inbox.move_up')"
              @click="moveGroup(groupIndex, -1)"
            />
            <!-- Move Down -->
            <va-button
              preset="secondary"
              size="small"
              icon="arrow_downward"
              class="mini-action-btn"
              :disabled="groupIndex === stepGroups.length - 1"
              :title="$t('inbox.move_down')"
              @click="moveGroup(groupIndex, 1)"
            />
          </div>
        </div>

        <!-- Group Items (Parallel Steps in this order) -->
        <div class="group-items-list">
          <div
            v-for="(step, itemIndex) in group.items"
            :key="step.id"
            class="route-step-card"
          >
            <!-- Type Selector: APPROVAL vs CONSENSUS -->
            <div class="step-type-selector">
              <va-select
                v-model="step.stepType"
                :options="stepTypeOptions"
                value-by="value"
                text-by="text"
                dense
                class="type-select"
                @update:model-value="onStepChanged"
              />
            </div>

            <!-- Assignee Selection (Dropdown with search / address book modal) -->
            <div class="step-assignee-selector">
              <va-select
                v-model="step.assigneeId"
                :options="userOptions"
                value-by="value"
                text-by="text"
                searchable
                dense
                :placeholder="$t('inbox.search_users')"
                class="assignee-select"
                @update:model-value="onStepChanged"
              >
                <template #appendInner>
                  <va-button
                    preset="plain"
                    size="small"
                    icon="person_search"
                    class="address-btn"
                    :title="$t('inbox.address_book')"
                    @click.stop="openUserSelectForStep(step)"
                  />
                </template>
              </va-select>
            </div>

            <!-- Step Delete Action -->
            <div class="step-item-actions">
              <va-button
                preset="secondary"
                size="small"
                color="danger"
                icon="delete"
                class="mini-del-btn"
                :title="$t('inbox.delete_step')"
                @click="removeStep(step.id)"
              />
            </div>
          </div>
        </div>

        <!-- Arrow Divider between groups -->
        <div class="step-flow-arrow" v-if="groupIndex < stepGroups.length - 1">
          <va-icon name="arrow_downward" size="18px" color="secondary" />
        </div>
      </div>
    </div>

    <!-- Observers (Notification) Section -->
    <div class="observers-section">
      <div class="observers-header">
        <div class="observers-title-group">
          <va-icon name="notifications" size="small" color="secondary" />
          <span class="observers-title">{{ $t('inbox.observers') }}</span>
          <span class="observers-hint">{{ $t('inbox.observers_desc') }}</span>
        </div>
      </div>
      <InboxRecipientPicker
        v-model="observersList"
        :label="$t('inbox.observers')"
        :placeholder="$t('inbox.search_users')"
        @update:model-value="onObserversChanged"
      />
    </div>

    <!-- User Modal for Single Pick -->
    <UserGridSelectModal
      v-model="showUserModal"
      :title="`${$t('inbox.approval_line')} - ${$t('inbox.address_book')}`"
      :users="allUsers"
      :initial-selected-ids="modalTargetStep?.assigneeId ? [modalTargetStep.assigneeId] : []"
      :single-select="true"
      @confirm="onSingleUserSelected"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthUser } from '~/composables/useAuthUser'
import { useUserStore } from '~/stores/useUserStore'
import { useCustomFetch } from '~/composables/useCustomFetch'
import InboxRecipientPicker from '~/components/inbox/InboxRecipientPicker.vue'
import UserGridSelectModal from '~/components/chat/UserGridSelectModal.vue'

export interface RouteStepItem {
  id: string
  stepOrder: number
  stepType: 'APPROVAL' | 'CONSENSUS'
  assigneeId: string
  assigneeRole?: string
}

const props = defineProps<{
  modelValue?: {
    steps: RouteStepItem[]
    observerIds: string[]
  }
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: { steps: RouteStepItem[]; observerIds: string[] }): void
  (e: 'change', val: { steps: RouteStepItem[]; observerIds: string[] }): void
}>()

const { t } = useI18n()
const authUserStore = useAuthUser()
const userStore = useUserStore()
const { customFetch } = useCustomFetch()

const allUsers = ref<any[]>([])
const showUserModal = ref(false)
const modalTargetStep = ref<RouteStepItem | null>(null)

const drafterId = computed(() => authUserStore?.currentUserId || authUserStore?.currentUser?.id || '')
const drafterName = computed(() => authUserStore?.currentUser?.username || authUserStore?.currentUser?.name || drafterId.value)
const drafterDepartment = computed(() => authUserStore?.currentUser?.department || '')
const drafterInitials = computed(() => (drafterName.value ? drafterName.value.substring(0, 2).toUpperCase() : 'ME'))

const steps = ref<RouteStepItem[]>([])
const observersList = ref<string[]>([])

const stepTypeOptions = computed(() => [
  { value: 'APPROVAL', text: t('inbox.type_approval') },
  { value: 'CONSENSUS', text: t('inbox.type_consensus') }
])

const userOptions = computed(() => {
  return allUsers.value.map(u => ({
    value: u.id,
    text: u.username ? `${u.username} (${u.email || u.id})` : (u.email || u.id)
  }))
})

// Group steps by stepOrder for parallel visualization
const stepGroups = computed(() => {
  const map = new Map<number, RouteStepItem[]>()
  for (const s of steps.value) {
    const list = map.get(s.stepOrder) || []
    list.push(s)
    map.set(s.stepOrder, list)
  }
  const sortedOrders = Array.from(map.keys()).sort((a, b) => a - b)
  return sortedOrders.map(order => ({
    stepOrder: order,
    items: map.get(order) || []
  }))
})

const loadAllUsers = async () => {
  if (allUsers.value.length === 0) {
    try {
      const res: any = await customFetch('/api/users')
      if (res && Array.isArray(res)) {
        allUsers.value = res
      } else if (res && res.data && Array.isArray(res.data)) {
        allUsers.value = res.data
      }
    } catch (e) {
      console.debug('Failed to fetch users:', e)
    }
  }
}

onMounted(() => {
  loadAllUsers()
})

watch(() => props.modelValue, (newVal) => {
  if (newVal && newVal.steps && newVal.steps.length > 0) {
    if (JSON.stringify(newVal.steps) !== JSON.stringify(steps.value)) {
      steps.value = JSON.parse(JSON.stringify(newVal.steps))
    }
    if (newVal.observerIds && JSON.stringify(newVal.observerIds) !== JSON.stringify(observersList.value)) {
      observersList.value = JSON.parse(JSON.stringify(newVal.observerIds))
    }
  } else if (!props.modelValue || !props.modelValue.steps || props.modelValue.steps.length === 0) {
    if (steps.value.length === 0) {
      steps.value = [
        {
          id: 'step_' + Date.now() + '_1',
          stepOrder: 1,
          stepType: 'APPROVAL',
          assigneeId: ''
        }
      ]
      observersList.value = []
    }
  }
}, { immediate: true })

const emitUpdate = () => {
  const payload = {
    steps: steps.value,
    observerIds: observersList.value
  }
  emit('update:modelValue', payload)
  emit('change', payload)
}

const onStepChanged = () => {
  emitUpdate()
}

const onObserversChanged = () => {
  emitUpdate()
}

const addStep = () => {
  const maxOrder = steps.value.reduce((max, s) => Math.max(max, s.stepOrder), 0)
  steps.value.push({
    id: 'step_' + Date.now() + '_' + Math.random().toString(36).substring(2, 6),
    stepOrder: maxOrder + 1,
    stepType: 'APPROVAL',
    assigneeId: ''
  })
  emitUpdate()
}

const addParallelStep = (order: number) => {
  steps.value.push({
    id: 'step_' + Date.now() + '_' + Math.random().toString(36).substring(2, 6),
    stepOrder: order,
    stepType: 'APPROVAL',
    assigneeId: ''
  })
  emitUpdate()
}

const removeStep = (stepId: string) => {
  steps.value = steps.value.filter(s => s.id !== stepId)
  reindexStepOrders()
  emitUpdate()
}

const moveGroup = (groupIndex: number, direction: number) => {
  const targetIndex = groupIndex + direction
  if (targetIndex < 0 || targetIndex >= stepGroups.value.length) return

  const currentOrder = stepGroups.value[groupIndex].stepOrder
  const targetOrder = stepGroups.value[targetIndex].stepOrder

  for (const s of steps.value) {
    if (s.stepOrder === currentOrder) {
      s.stepOrder = targetOrder
    } else if (s.stepOrder === targetOrder) {
      s.stepOrder = currentOrder
    }
  }
  reindexStepOrders()
  emitUpdate()
}

const reindexStepOrders = () => {
  const uniqueOrders = Array.from(new Set(steps.value.map(s => s.stepOrder))).sort((a, b) => a - b)
  const orderMap = new Map<number, number>()
  uniqueOrders.forEach((oldOrder, idx) => {
    orderMap.set(oldOrder, idx + 1)
  })

  for (const s of steps.value) {
    s.stepOrder = orderMap.get(s.stepOrder) || s.stepOrder
  }
}

const openUserSelectForStep = async (step: RouteStepItem) => {
  await loadAllUsers()
  modalTargetStep.value = step
  showUserModal.value = true
}

const onSingleUserSelected = (selectedIds: string[]) => {
  if (modalTargetStep.value && selectedIds.length > 0) {
    modalTargetStep.value.assigneeId = selectedIds[0]
    emitUpdate()
  }
  showUserModal.value = false
}
</script>

<style scoped>
.approval-route-builder {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  background: var(--va-background-element, #f8fafc);
  border: 1px solid var(--va-background-border, #e2e8f0);
  border-radius: 8px;
  padding: 0.85rem 1rem;
}

:global([data-vuestic-preset="dark"]) .approval-route-builder,
:global(.va-theme-dark) .approval-route-builder {
  background: #1e293b !important;
  border-color: #334155 !important;
}

.route-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--va-background-border, #e2e8f0);
  padding-bottom: 0.5rem;
}

.header-title-group {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.header-title {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--va-text-primary);
}

.route-steps-container {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.step-flow-arrow {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: -2px 0;
  opacity: 0.6;
}

.route-step-card {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  background: var(--va-background-primary, #ffffff);
  border: 1px solid var(--va-background-border, #e2e8f0);
  border-radius: 6px;
  padding: 0.45rem 0.75rem;
  transition: all 0.2s ease;
}

:global([data-vuestic-preset="dark"]) .route-step-card,
:global(.va-theme-dark) .route-step-card {
  background: #0f172a !important;
  border-color: #334155 !important;
}

.drafter-card {
  border-left: 3px solid var(--va-info, #0284c7);
}

.step-badge-area {
  flex-shrink: 0;
}

.step-info-area {
  flex: 1;
  display: flex;
  align-items: center;
}

.step-user-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.user-name {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--va-text-primary);
}

.user-meta {
  font-size: 0.75rem;
  color: var(--va-text-secondary, #64748b);
}

.step-group-block {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  background: rgba(255, 255, 255, 0.5);
  border: 1px dashed var(--va-background-border, #cbd5e1);
  border-radius: 6px;
  padding: 0.45rem 0.6rem;
}

:global([data-vuestic-preset="dark"]) .step-group-block,
:global(.va-theme-dark) .step-group-block {
  background: rgba(15, 23, 42, 0.4) !important;
  border-color: #334155 !important;
}

.group-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.group-order-tag {
  font-size: 0.78rem;
  font-weight: 700;
  color: var(--va-primary, #2563eb);
}

.parallel-indicator {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--va-warning, #d97706);
}

.group-header-actions {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  margin-left: auto;
}

.group-items-list {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.step-type-selector {
  width: 100px;
  flex-shrink: 0;
}

.step-assignee-selector {
  flex: 1;
}

.address-btn {
  padding: 0 4px !important;
}

.mini-action-btn {
  padding: 2px 4px !important;
  font-size: 0.75rem !important;
  height: 22px !important;
}

.mini-del-btn {
  padding: 2px !important;
  height: 24px !important;
  min-width: 24px !important;
}

.observers-section {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  margin-top: 0.25rem;
  padding-top: 0.5rem;
  border-top: 1px solid var(--va-background-border, #e2e8f0);
}

.observers-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.observers-title-group {
  display: flex;
  align-items: center;
  gap: 0.35rem;
}

.observers-title {
  font-size: 0.82rem;
  font-weight: 700;
  color: var(--va-text-primary);
}

.observers-hint {
  font-size: 0.75rem;
  color: var(--va-text-secondary, #64748b);
  margin-left: 0.25rem;
}
</style>
