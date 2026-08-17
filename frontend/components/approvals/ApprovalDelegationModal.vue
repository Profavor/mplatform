<template>
  <AppModal
    v-model="show"
    :title="$t('approval_delegation')"
    icon="supervisor_account"
    size="large"
    hide-default-actions
  >
    <div style="padding: 0.5rem 0; display: flex; flex-direction: column; gap: 1.25rem;">
      <va-alert color="info" outline style="margin: 0; font-size: 0.85rem; line-height: 1.5;">
        💡 {{ $t('approval_delegation_desc') }}
      </va-alert>

      <!-- Tabs -->
      <div style="display: flex; border-bottom: 1px solid var(--va-background-border); gap: 1rem;">
        <button
          type="button"
          :style="{
            padding: '0.6rem 1rem',
            border: 'none',
            background: 'transparent',
            cursor: 'pointer',
            fontWeight: activeTab === 'byMe' ? 'bold' : 'normal',
            borderBottom: activeTab === 'byMe' ? '2px solid var(--va-primary)' : 'none',
            color: activeTab === 'byMe' ? 'var(--va-primary)' : 'var(--va-text-secondary)'
          }"
          @click="activeTab = 'byMe'"
        >
          {{ $t('delegated_by_me') }} ({{ delegatedByMe.length }})
        </button>
        <button
          type="button"
          :style="{
            padding: '0.6rem 1rem',
            border: 'none',
            background: 'transparent',
            cursor: 'pointer',
            fontWeight: activeTab === 'toMe' ? 'bold' : 'normal',
            borderBottom: activeTab === 'toMe' ? '2px solid var(--va-primary)' : 'none',
            color: activeTab === 'toMe' ? 'var(--va-primary)' : 'var(--va-text-secondary)'
          }"
          @click="activeTab = 'toMe'"
        >
          {{ $t('delegated_to_me') }} ({{ delegatedToMe.length }})
        </button>
        <button
          type="button"
          :style="{
            padding: '0.6rem 1rem',
            border: 'none',
            background: 'transparent',
            cursor: 'pointer',
            fontWeight: activeTab === 'create' ? 'bold' : 'normal',
            borderBottom: activeTab === 'create' ? '2px solid var(--va-primary)' : 'none',
            color: activeTab === 'create' ? 'var(--va-primary)' : 'var(--va-text-secondary)'
          }"
          @click="activeTab = 'create'"
        >
          + {{ $t('add_delegation') }}
        </button>
      </div>

      <!-- Tab 1: Delegated By Me -->
      <div v-if="activeTab === 'byMe'">
        <div v-if="loading" style="display: flex; justify-content: center; padding: 2rem;">
          <va-progress-circle indeterminate size="medium" />
        </div>
        <div v-else-if="delegatedByMe.length === 0" style="text-align: center; padding: 2.5rem 1rem; color: var(--va-text-secondary);">
          <va-icon name="assignment_ind" size="3rem" style="opacity: 0.35; margin-bottom: 0.5rem;" />
          <div>설정된 결재 위임 내역이 없습니다.</div>
        </div>
        <div v-else style="display: flex; flex-direction: column; gap: 0.75rem; max-height: 360px; overflow-y: auto;">
          <va-card
            v-for="d in delegatedByMe"
            :key="d.id"
            flat
            bordered
            style="border-left: 4px solid var(--va-primary);"
          >
            <va-card-content style="display: flex; justify-content: space-between; align-items: center; padding: 0.85rem 1rem;">
              <div>
                <div style="display: flex; align-items: center; gap: 0.5rem;">
                  <span style="font-weight: 700; font-size: 0.95rem;">대결자: {{ d.delegateeUserName || d.delegateeUserId }}</span>
                  <va-badge :text="d.isActive ? $t('delegation_active') : $t('delegation_expired')" :color="d.isActive ? 'success' : 'secondary'" size="small" />
                </div>
                <div style="font-size: 0.82rem; color: var(--va-text-secondary); margin-top: 0.35rem;">
                  <b>{{ $t('delegation_period') }}:</b> {{ formatDate(d.startDate) }} ~ {{ formatDate(d.endDate) }}
                </div>
                <div v-if="d.reason" style="font-size: 0.82rem; color: var(--va-text-primary); margin-top: 0.25rem;">
                  <b>{{ $t('delegation_reason') }}:</b> {{ d.reason }}
                </div>
              </div>
              <va-button
                v-if="d.isActive"
                preset="secondary"
                color="danger"
                size="small"
                @click="revokeDelegation(d.id)"
              >
                {{ $t('revoke_delegation') }}
              </va-button>
            </va-card-content>
          </va-card>
        </div>
      </div>

      <!-- Tab 2: Delegated To Me -->
      <div v-else-if="activeTab === 'toMe'">
        <div v-if="loading" style="display: flex; justify-content: center; padding: 2rem;">
          <va-progress-circle indeterminate size="medium" />
        </div>
        <div v-else-if="delegatedToMe.length === 0" style="text-align: center; padding: 2.5rem 1rem; color: var(--va-text-secondary);">
          <va-icon name="how_to_reg" size="3rem" style="opacity: 0.35; margin-bottom: 0.5rem;" />
          <div>나에게 위임된 결재 권한이 없습니다.</div>
        </div>
        <div v-else style="display: flex; flex-direction: column; gap: 0.75rem; max-height: 360px; overflow-y: auto;">
          <va-card
            v-for="d in delegatedToMe"
            :key="d.id"
            flat
            bordered
            style="border-left: 4px solid var(--va-success);"
          >
            <va-card-content style="padding: 0.85rem 1rem;">
              <div style="display: flex; align-items: center; gap: 0.5rem;">
                <span style="font-weight: 700; font-size: 0.95rem;">위임자: {{ d.delegatorUserName || d.delegatorUserId }}</span>
                <va-badge :text="d.isActive ? $t('delegation_active') : $t('delegation_expired')" :color="d.isActive ? 'success' : 'secondary'" size="small" />
              </div>
              <div style="font-size: 0.82rem; color: var(--va-text-secondary); margin-top: 0.35rem;">
                <b>{{ $t('delegation_period') }}:</b> {{ formatDate(d.startDate) }} ~ {{ formatDate(d.endDate) }}
              </div>
              <div v-if="d.reason" style="font-size: 0.82rem; color: var(--va-text-primary); margin-top: 0.25rem;">
                <b>{{ $t('delegation_reason') }}:</b> {{ d.reason }}
              </div>
            </va-card-content>
          </va-card>
        </div>
      </div>

      <!-- Tab 3: Create Delegation -->
      <div v-else-if="activeTab === 'create'" style="display: flex; flex-direction: column; gap: 1rem;">
        <div>
          <label style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">
            {{ $t('delegatee') }} <span style="color: var(--va-danger);">*</span>
          </label>
          <va-select
            v-model="form.delegateeUserId"
            :options="userOptions"
            value-by="id"
            text-by="username"
            placeholder="대결자를 선택하세요"
          />
        </div>

        <div style="display: flex; gap: 1rem;">
          <div style="flex: 1;">
            <label style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">
              위임 시작일시 <span style="color: var(--va-danger);">*</span>
            </label>
            <va-input v-model="form.startDate" type="datetime-local" />
          </div>
          <div style="flex: 1;">
            <label style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">
              위임 종료일시 <span style="color: var(--va-danger);">*</span>
            </label>
            <va-input v-model="form.endDate" type="datetime-local" />
          </div>
        </div>

        <div>
          <label style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">
            {{ $t('delegation_reason') }}
          </label>
          <va-input
            v-model="form.reason"
            type="textarea"
            :autosize="true"
            :min-rows="2"
            placeholder="위임 사유 (예: 연차 휴가, 해외 출장)"
          />
        </div>

        <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem;">
          <va-button preset="secondary" @click="activeTab = 'byMe'">{{ $t('cancel') }}</va-button>
          <va-button
            color="primary"
            icon="how_to_reg"
            :disabled="!form.delegateeUserId || !form.startDate || !form.endDate || submitting"
            :loading="submitting"
            @click="submitDelegation"
          >
            {{ $t('add_delegation') }}
          </va-button>
        </div>
      </div>

      <div v-if="activeTab !== 'create'" style="display: flex; justify-content: flex-end; margin-top: 0.5rem;">
        <va-button preset="secondary" @click="show = false">{{ $t('close') }}</va-button>
      </div>
    </div>
  </AppModal>
</template>

<script setup lang="ts">
import { ref, computed, watch, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'vuestic-ui'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { formatWithTimezone } from '~/composables/useTimezoneDate'
import { useCookie } from '#app'
import AppModal from '~/components/common/AppModal.vue'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const { t } = useI18n()
const toast = useToast()
const tzCookie = useCookie('user-timezone')

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const activeTab = ref<'byMe' | 'toMe' | 'create'>('byMe')
const loading = ref(false)
const submitting = ref(false)

const delegatedByMe = ref<any[]>([])
const delegatedToMe = ref<any[]>([])
const userOptions = ref<any[]>([])

const form = reactive({
  delegateeUserId: '',
  startDate: '',
  endDate: '',
  reason: ''
})

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return formatWithTimezone(dateStr, tzCookie.value || 'Asia/Seoul')
}

const fetchDelegations = async () => {
  loading.value = true
  try {
    const res = await useCustomFetch('/approvals/delegations/my')
    if (res.data?.value) {
      delegatedByMe.value = res.data.value.delegatedByMe || []
      delegatedToMe.value = res.data.value.delegatedToMe || []
    }
  } catch (e) {
    console.error('Failed to fetch delegations', e)
  } finally {
    loading.value = false
  }
}

const fetchUsers = async () => {
  try {
    const res = await useCustomFetch('/admin/users')
    if (res.data?.value) {
      userOptions.value = Array.isArray(res.data.value) ? res.data.value : (res.data.value.content || [])
    }
  } catch (e) {
    console.error('Failed to fetch users', e)
  }
}

const submitDelegation = async () => {
  if (!form.delegateeUserId || !form.startDate || !form.endDate) return
  submitting.value = true
  try {
    const res = await useCustomFetch('/approvals/delegations', {
      method: 'POST',
      body: {
        delegateeUserId: form.delegateeUserId,
        startDate: form.startDate.includes('T') ? form.startDate + ':00' : form.startDate,
        endDate: form.endDate.includes('T') ? form.endDate + ':00' : form.endDate,
        reason: form.reason
      }
    })
    if (res.data?.value || res.status?.value === 'success') {
      toast.init({
        message: t('delegation_success'),
        color: 'success'
      })
      form.delegateeUserId = ''
      form.startDate = ''
      form.endDate = ''
      form.reason = ''
      activeTab.value = 'byMe'
      await fetchDelegations()
    }
  } catch (err: any) {
    toast.init({
      message: err.message || 'Error occurred while creating delegation',
      color: 'danger'
    })
  } finally {
    submitting.value = false
  }
}

const revokeDelegation = async (id: string) => {
  try {
    await useCustomFetch(`/approvals/delegations/${id}`, {
      method: 'DELETE'
    })
    toast.init({
      message: t('revoke_success'),
      color: 'success'
    })
    await fetchDelegations()
  } catch (e: any) {
    toast.init({
      message: e.message || 'Failed to revoke delegation',
      color: 'danger'
    })
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    fetchDelegations()
    fetchUsers()
  }
})
</script>
