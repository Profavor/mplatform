<template>
  <va-modal
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    @cancel="$emit('update:modelValue', false)"
    @click-outside="$emit('update:modelValue', false)"
    :title="$t('request_domain_access')"
    size="small"
    hide-default-actions
  >
    <!-- ① 새 도메인 신청 -->
    <h3 class="font-bold mb-2 text-sm text-gray-700">{{ $t('request_new_domain') }}</h3>

    <div v-if="availableDomains.length === 0" class="text-gray-500 mb-4 text-sm">
      {{ $t('no_new_domains_available') }}
    </div>
    <div v-else class="mb-4">
      <!-- Select: 선택하면 바로 칩으로, 입력창은 빈칸 유지 -->
      <va-select
        v-if="selectableOptions.length > 0"
        :model-value="[]"
        @update:model-value="onDomainAdded"
        multiple
        :options="selectableOptions"
        value-by="id"
        text-by="label"
        :placeholder="$t('select_a_domain')"
        no-options-text=""
        class="w-full"
      />
      <div
        v-else
        style="font-size: 0.82rem; color: var(--va-text-secondary); padding: 0.4rem 0;"
      >
        {{ $t('all_domains_selected') || (isEn ? 'All available domains are selected.' : '선택 가능한 모든 도메인이 추가되었습니다.') }}
      </div>

      <!-- 선택된 칩 -->
      <div v-if="selectedDomainToRequest.length > 0" style="display: flex; flex-wrap: wrap; gap: 0.4rem; margin-top: 0.6rem;">
        <va-chip
          v-for="id in selectedDomainToRequest"
          :key="id"
          closeable
          color="primary"
          size="small"
          @click:close="removeSelected(id)"
        >
          {{ getLabelById(id) }}
        </va-chip>
      </div>

      <div style="display: flex; justify-content: flex-end; margin-top: 0.5rem;">
        <va-button
          size="small"
          @click="submitAccessRequest"
          :disabled="!selectedDomainToRequest || selectedDomainToRequest.length === 0 || submitting"
          :loading="submitting"
        >
          {{ $t('submit_request') }}
        </va-button>
      </div>
    </div>

    <va-divider class="my-4" />

    <!-- ② 신청중인 도메인 (날짜별 개별 취소 가능) -->
    <div class="mb-4">
      <h3 class="font-bold mb-2 text-sm text-gray-700">
        {{ $t('pending_requests') || (isEn ? 'Pending Requests' : '신청중인 도메인') }}
      </h3>
      <div v-if="pendingRequests.length === 0" class="text-gray-500 text-sm italic">
        {{ $t('no_pending_requests') || (isEn ? 'No pending requests.' : '신청중인 도메인이 없습니다.') }}
      </div>
      <div v-else style="display: flex; flex-direction: column; gap: 0.4rem;">
        <div
          v-for="req in pendingRequests"
          :key="req.id"
          style="display: flex; align-items: center; justify-content: space-between; background: var(--va-background-element); border-radius: 6px; padding: 0.45rem 0.75rem;"
        >
          <div>
            <div style="font-size: 0.85rem; font-weight: 600;">{{ req.domainName }}</div>
            <div v-if="req.requestedAt" style="font-size: 0.72rem; color: var(--va-text-secondary); margin-top: 1px;">
              {{ formatDate(req.requestedAt) }}
            </div>
          </div>
          <div style="display: flex; align-items: center; gap: 0.5rem;">
            <va-chip color="warning" size="small" style="font-size: 0.72rem;">
              {{ $t('status_pending') || (isEn ? 'Pending' : '검토중') }}
            </va-chip>
            <va-button
              preset="secondary"
              color="danger"
              size="small"
              style="font-size: 0.75rem; padding: 0 6px; height: 24px;"
              :loading="cancelingId === req.id"
              @click="cancelAccessRequest(req.id)"
            >
              {{ $t('cancel_request') || (isEn ? 'Cancel' : '신청 취소') }}
            </va-button>
          </div>
        </div>
      </div>
    </div>

    <va-divider class="my-4" />

    <!-- ③ 승인된 도메인 -->
    <div class="mb-4">
      <h3 class="font-bold mb-2 text-sm text-gray-700">{{ $t('my_granted_domains') }}</h3>
      <div v-if="domainList.length === 0" class="text-gray-500 text-sm italic">{{ $t('no_granted_domains') }}</div>
      <div v-else style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
        <va-chip v-for="d in domainList" :key="d.id" color="success" size="small" outline>
          {{ getDomainName(d.name) }}
        </va-chip>
      </div>
    </div>

    <!-- 중복 신청 경고 -->
    <va-alert
      v-if="duplicateWarning"
      color="warning"
      icon="warning"
      closeable
      style="margin-top: 0.5rem; font-size: 0.82rem;"
      @update:modelValue="duplicateWarning = false"
    >
      {{ $t('duplicate_request_warning') || (isEn ? 'Some domains are already pending approval and were skipped.' : '이미 신청중인 도메인은 중복 신청이 제외되었습니다.') }}
    </va-alert>

    <div style="display: flex; justify-content: flex-end; margin-top: 1.5rem;">
      <va-button preset="secondary" @click="$emit('update:modelValue', false)">
        {{ $t('close') || (isEn ? 'Close' : '닫기') }}
      </va-button>
    </div>
  </va-modal>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useCookie } from '#app'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue'])

const { t } = useI18n()
const tokenCookie = useCookie('auth_token')
const currentLocale = useCookie('locale', { default: () => 'ko' })
const isEn = computed(() => currentLocale.value === 'en')

const isOpen = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const availableDomains = ref([])  // 신청 가능한 전체 도메인
const domainList = ref([])        // 승인된 내 도메인
const pendingRequests = ref([])   // 현재 신청중인 도메인
const selectedDomainToRequest = ref([])
const submitting = ref(false)
const cancelingId = ref(null)
const duplicateWarning = ref(false)

// 신청중인 domainId 집합 (중복 방지용)
const pendingDomainIds = computed(() => new Set(pendingRequests.value.map(r => r.domainId)))

// 이미 신청 중이거나 선택된 항목 제외한 옵션
const selectableOptions = computed(() =>
  availableDomains.value.filter(d =>
    !selectedDomainToRequest.value.includes(d.id) &&
    !pendingDomainIds.value.has(d.id)
  )
)

const getLabelById = (id) => {
  const found = availableDomains.value.find(d => d.id === id)
  return found ? found.label : id
}

// 드롭다운 선택 → 칩으로 추가 (이미 신청중이면 경고)
const onDomainAdded = (vals) => {
  if (!vals || vals.length === 0) return
  let hasDuplicate = false
  vals.forEach(id => {
    if (pendingDomainIds.value.has(id)) {
      hasDuplicate = true
      return
    }
    if (!selectedDomainToRequest.value.includes(id)) {
      selectedDomainToRequest.value.push(id)
    }
  })
  if (hasDuplicate) duplicateWarning.value = true
}

const removeSelected = (id) => {
  selectedDomainToRequest.value = selectedDomainToRequest.value.filter(v => v !== id)
}

const parseDate = (dateString) => {
  if (!dateString) return null
  let str = String(dateString).trim()
  if (str.includes(' ') && !str.includes('T')) str = str.replace(' ', 'T')
  if (!str.endsWith('Z') && !str.includes('+') && str.lastIndexOf('-') < 8) {
    const tz = useCookie('timezone', { default: () => 'Asia/Seoul' }).value
    let offset = '+09:00'
    if (tz === 'UTC' || tz === 'GMT') offset = 'Z'
    else if (tz === 'America/New_York') offset = '-05:00'
    else if (tz === 'Europe/London') offset = '+00:00'
    str += offset
  }
  const d = new Date(str)
  return isNaN(d.getTime()) ? new Date(dateString) : d
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = parseDate(dateString)
  if (!date) return ''
  const tz = useCookie('timezone', { default: () => 'Asia/Seoul' }).value
  return date.toLocaleString(undefined, {
    timeZone: tz,
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  }).replace(/\s*(GMT|UTC|KST|PST|EST|CET)[-+0-9:]*/gi, '').trim()
}

const getDomainName = (nameObj) => {
  if (!nameObj) return 'Unknown'
  if (typeof nameObj === 'string') {
    try {
      const parsed = JSON.parse(nameObj)
      return parsed[currentLocale.value] || parsed.ko || parsed.en || 'Unknown'
    } catch {
      return nameObj
    }
  }
  return nameObj[currentLocale.value] || nameObj.ko || nameObj.en || 'Unknown'
}

const loadDomains = async () => {
  try {
    const headers = { Authorization: `Bearer ${tokenCookie.value}` }
    const [availResult, domainResult, pendingResult] = await Promise.allSettled([
      $fetch('/api/permissions/domains/available', { headers }),
      $fetch('/api/domains', { headers }),
      $fetch('/api/permissions/requests/pending', { headers })
    ])

    availableDomains.value = availResult.status === 'fulfilled' && Array.isArray(availResult.value)
      ? availResult.value.map(d => ({ id: d.id, label: getDomainName(d.name) }))
      : []

    domainList.value = domainResult.status === 'fulfilled' && Array.isArray(domainResult.value)
      ? domainResult.value
      : []

    if (pendingResult.status === 'fulfilled') {
      const raw = pendingResult.value
      const list = Array.isArray(raw) ? raw : (raw?.content || raw?.data || [])
      pendingRequests.value = list.map(r => ({
        id: r.id,
        domainId: r.domainId || r.domain?.id,
        domainName: getDomainName(r.domainName || r.domain?.name) || r.domainId || '-',
        requestedAt: r.requestedAt || r.createdAt || r.created_at || null
      }))
    } else {
      pendingRequests.value = []
    }
  } catch (e) {
    console.error('Error fetching domains for request:', e)
  }
}

watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    selectedDomainToRequest.value = []
    duplicateWarning.value = false
    loadDomains()
  }
})

const submitAccessRequest = async () => {
  if (!selectedDomainToRequest.value || selectedDomainToRequest.value.length === 0) return

  // 프론트 중복 방어: 신청중인 도메인 필터링
  const toSubmit = selectedDomainToRequest.value.filter(id => !pendingDomainIds.value.has(id))
  if (toSubmit.length === 0) {
    duplicateWarning.value = true
    return
  }
  if (toSubmit.length < selectedDomainToRequest.value.length) {
    duplicateWarning.value = true
  }

  submitting.value = true
  try {
    const headers = { Authorization: `Bearer ${tokenCookie.value}` }
    await Promise.all(toSubmit.map(domainId =>
      $fetch('/api/permissions/requests', {
        method: 'POST',
        headers,
        body: { domainId }
      })
    ))
    isOpen.value = false
    selectedDomainToRequest.value = []
    await loadDomains()
  } catch (e) {
    console.error('Error submitting requests:', e)
  } finally {
    submitting.value = false
  }
}

const cancelAccessRequest = async (requestId) => {
  if (!requestId) return
  cancelingId.value = requestId
  try {
    const headers = { Authorization: `Bearer ${tokenCookie.value}` }
    await $fetch(`/api/permissions/requests/${requestId}`, {
      method: 'DELETE',
      headers
    })
    await loadDomains()
  } catch (e) {
    console.error('Error canceling request:', e)
  } finally {
    cancelingId.value = null
  }
}
</script>
