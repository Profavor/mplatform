<template>
  <div class="recipient-picker-wrapper">
    <div
      class="recipient-tag-box"
      :class="{ 'is-focused': isInputFocused, 'has-chips': modelValue.length > 0 }"
      @click="focusInput"
    >
      <!-- Label Badge -->
      <span class="recipient-label-tag">{{ label }}</span>

      <!-- Chips and Inline Input Container -->
      <div class="recipient-chips-container">
        <!-- Selected Chips -->
        <div
          v-for="item in selectedItems"
          :key="item.value"
          class="recipient-chip"
        >
          <va-icon name="person" size="14px" class="chip-icon" />
          <span class="chip-label">{{ item.text }}</span>
          <button
            type="button"
            class="chip-close-btn"
            :title="$t('inbox.delete')"
            @click.stop="removeItem(item.value)"
          >
            <va-icon name="close" size="12px" />
          </button>
        </div>

        <!-- Inline Typing / Search Input -->
        <input
          ref="inputRef"
          v-model="searchKeyword"
          type="text"
          :placeholder="modelValue.length === 0 ? (placeholder || $t('inbox.search_users')) : ''"
          class="recipient-inline-input"
          @focus="onFocus"
          @blur="onBlur"
          @keydown.enter.prevent="onEnterKey"
          @keydown.backspace="onBackspaceKey"
          @keydown.down.prevent="navigateDropdown(1)"
          @keydown.up.prevent="navigateDropdown(-1)"
        />
      </div>

      <!-- Right Action Tools -->
      <div class="recipient-actions-box" @click.stop>
        <slot name="extra-actions" />
        <va-button
          preset="secondary"
          size="small"
          icon="person_search"
          class="address-book-btn"
          :title="$t('inbox.address_book')"
          @click="openUserSelectModal"
        >
          {{ $t('inbox.search_users_btn') }}
        </va-button>
      </div>
    </div>

    <!-- Autocomplete Suggestions Dropdown -->
    <div
      v-if="isDropdownOpen && (dropdownOptions.length > 0 || isDirectEmailInput)"
      class="recipient-dropdown-menu"
      @mousedown.prevent
    >
      <div
        v-if="isDirectEmailInput && !dropdownOptions.some(o => o.value === searchKeyword.trim())"
        class="dropdown-item direct-email-item"
        :class="{ 'is-active': activeDropdownIndex === 0 }"
        @click="selectOption({ value: searchKeyword.trim(), text: searchKeyword.trim() })"
      >
        <va-icon name="mail" size="16px" color="primary" />
        <div class="dropdown-item-info">
          <span class="dropdown-main-text">{{ searchKeyword.trim() }}</span>
          <span class="dropdown-sub-text">{{ $t('inbox.add_external_email') }}</span>
        </div>
      </div>

      <div
        v-for="(option, idx) in dropdownOptions"
        :key="option.value"
        class="dropdown-item"
        :class="{ 'is-active': activeDropdownIndex === (isDirectEmailInput ? idx + 1 : idx) }"
        @click="selectOption(option)"
      >
        <va-icon name="account_circle" size="18px" color="secondary" />
        <div class="dropdown-item-info">
          <span class="dropdown-main-text">{{ option.username }}</span>
          <span class="dropdown-sub-text">{{ option.email }} <span v-if="option.role">· {{ option.role }}</span></span>
        </div>
      </div>
    </div>

    <!-- User Grid Select Modal (AG-Grid Multi-select) -->
    <UserGridSelectModal
      v-model="showUserModal"
      :title="`${label} - ${$t('inbox.address_book')}`"
      :users="allUsers"
      :initial-selected-ids="selectedUserIds"
      @confirm="onUserModalConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { useUserStore } from '~/stores/useUserStore'
import UserGridSelectModal from '~/components/chat/UserGridSelectModal.vue'

const props = defineProps<{
  modelValue: string[]
  label: string
  placeholder?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: string[]): void
}>()

const { t } = useI18n()
const { customFetch } = useCustomFetch()
const userStore = useUserStore()

const inputRef = ref<HTMLInputElement | null>(null)
const searchKeyword = ref('')
const isInputFocused = ref(false)
const isDropdownOpen = ref(false)
const activeDropdownIndex = ref(0)
const allUsers = ref<any[]>([])
const showUserModal = ref(false)

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

const focusInput = () => {
  inputRef.value?.focus()
}

const onFocus = () => {
  isInputFocused.value = true
  isDropdownOpen.value = true
}

const onBlur = () => {
  isInputFocused.value = false
  setTimeout(() => {
    isDropdownOpen.value = false
  }, 150)
}

const isDirectEmailInput = computed(() => {
  const kw = searchKeyword.value.trim()
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(kw)
})

const dropdownOptions = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase()
  const alreadySelected = new Set(props.modelValue)

  return allUsers.value
    .filter(u => {
      const val = u.id || u.email
      if (alreadySelected.has(val) || alreadySelected.has(u.email) || alreadySelected.has(u.id)) {
        return false
      }
      if (!kw) return true
      const username = (u.username || '').toLowerCase()
      const email = (u.email || '').toLowerCase()
      const department = (u.department || '').toLowerCase()
      const role = (u.role || '').toLowerCase()
      return username.includes(kw) || email.includes(kw) || department.includes(kw) || role.includes(kw)
    })
    .slice(0, 10)
    .map(u => ({
      value: u.id || u.email,
      username: u.username || u.email,
      email: u.email,
      role: u.role || '',
      text: u.username ? `${u.username} (${u.email})` : u.email
    }))
})

const selectedItems = computed(() => {
  return props.modelValue.map(val => {
    const foundUser = allUsers.value.find(u => u.id === val || u.email === val)
    if (foundUser) {
      return {
        value: foundUser.id || foundUser.email,
        text: foundUser.username ? `${foundUser.username} (${foundUser.email})` : foundUser.email
      }
    }
    const isEmail = val.includes('@')
    return {
      value: val,
      text: isEmail ? val : userStore.getUserName(val, val)
    }
  })
})

const selectedUserIds = computed(() => {
  return props.modelValue.map(val => {
    const found = allUsers.value.find(u => u.id === val || u.email === val)
    return found ? found.id : val
  })
})

const selectOption = (opt: { value: string; text?: string }) => {
  if (!opt.value) return
  if (!props.modelValue.includes(opt.value)) {
    emit('update:modelValue', [...props.modelValue, opt.value])
  }
  searchKeyword.value = ''
  activeDropdownIndex.value = 0
  inputRef.value?.focus()
}

const removeItem = (valToRemove: string) => {
  emit('update:modelValue', props.modelValue.filter(v => v !== valToRemove))
  inputRef.value?.focus()
}

const onEnterKey = () => {
  if (isDirectEmailInput.value && activeDropdownIndex.value === 0 && !dropdownOptions.value.some(o => o.value === searchKeyword.value.trim())) {
    selectOption({ value: searchKeyword.value.trim() })
    return
  }

  const adjustedIndex = isDirectEmailInput.value ? activeDropdownIndex.value - 1 : activeDropdownIndex.value
  if (adjustedIndex >= 0 && adjustedIndex < dropdownOptions.value.length) {
    selectOption(dropdownOptions.value[adjustedIndex])
  } else if (isDirectEmailInput.value) {
    selectOption({ value: searchKeyword.value.trim() })
  }
}

const onBackspaceKey = () => {
  if (!searchKeyword.value && props.modelValue.length > 0) {
    const lastItem = props.modelValue[props.modelValue.length - 1]
    removeItem(lastItem)
  }
}

const navigateDropdown = (step: number) => {
  const max = dropdownOptions.value.length + (isDirectEmailInput.value ? 1 : 0)
  if (max === 0) return
  activeDropdownIndex.value = (activeDropdownIndex.value + step + max) % max
}

const openUserSelectModal = async () => {
  await loadAllUsers()
  showUserModal.value = true
}

const onUserModalConfirm = (selectedIds: string[]) => {
  const externalEmails = props.modelValue.filter(v => v.includes('@') && !allUsers.value.some(u => u.email === v || u.id === v))
  const merged = Array.from(new Set([...selectedIds, ...externalEmails]))
  emit('update:modelValue', merged)
}
</script>

<style scoped>
.recipient-picker-wrapper {
  position: relative;
  width: 100%;
  margin-bottom: 0.75rem;
}

.recipient-tag-box {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.4rem;
  padding: 0.35rem 0.6rem;
  min-height: 42px;
  background: var(--va-background-element, rgba(255, 255, 255, 0.04));
  border: 1px solid var(--va-background-border, rgba(255, 255, 255, 0.15));
  border-radius: 6px;
  cursor: text;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.recipient-tag-box.is-focused {
  border-color: var(--va-primary, #3b82f6);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

.recipient-label-tag {
  font-size: 0.82rem;
  font-weight: 700;
  color: var(--va-text-secondary, #94a3b8);
  min-width: 32px;
  user-select: none;
}

.recipient-chips-container {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.35rem;
  flex: 1;
  min-width: 180px;
}

.recipient-chip {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.2rem 0.55rem;
  border-radius: 14px;
  background: var(--va-primary, #2563eb);
  color: #ffffff;
  font-size: 0.78rem;
  font-weight: 500;
  line-height: 1.2;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  user-select: none;
  animation: chipFadeIn 0.15s ease-out;
}

@keyframes chipFadeIn {
  from { opacity: 0; transform: scale(0.92); }
  to { opacity: 1; transform: scale(1); }
}

.chip-icon {
  opacity: 0.85;
}

.chip-label {
  max-width: 240px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chip-close-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.25);
  border: none;
  border-radius: 50%;
  width: 15px;
  height: 15px;
  cursor: pointer;
  color: #ffffff;
  padding: 0;
  transition: background 0.15s ease;
}

.chip-close-btn:hover {
  background: rgba(255, 255, 255, 0.45);
}

.recipient-inline-input {
  flex: 1;
  min-width: 120px;
  border: none;
  outline: none;
  background: transparent;
  color: var(--va-text-primary, #f8fafc);
  font-size: 0.85rem;
  padding: 0.2rem 0.25rem;
}

.recipient-inline-input::placeholder {
  color: var(--va-text-secondary, #64748b);
  font-size: 0.82rem;
}

.recipient-actions-box {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  margin-left: auto;
}

.address-book-btn {
  font-size: 0.78rem !important;
  padding: 0.25rem 0.5rem !important;
  height: 28px !important;
}

/* Dropdown Autocomplete Menu */
.recipient-dropdown-menu {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  max-height: 220px;
  overflow-y: auto;
  background: var(--va-background-secondary, #1e293b);
  border: 1px solid var(--va-background-border, rgba(255, 255, 255, 0.15));
  border-radius: 8px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.4);
  z-index: 1000;
  padding: 0.35rem 0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.5rem 0.85rem;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.dropdown-item:hover,
.dropdown-item.is-active {
  background: var(--va-background-element, rgba(255, 255, 255, 0.08));
}

.dropdown-item-info {
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
  overflow: hidden;
}

.dropdown-main-text {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--va-text-primary, #f8fafc);
}

.dropdown-sub-text {
  font-size: 0.74rem;
  color: var(--va-text-secondary, #94a3b8);
}

.direct-email-item {
  border-bottom: 1px solid var(--va-background-border, rgba(255, 255, 255, 0.1));
}
</style>
