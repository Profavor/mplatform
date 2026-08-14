<template>
  <va-modal
    :model-value="modelValue"
    :title="t('add_menu', '메뉴 추가')"
    :ok-text="t('save', '저장')"
    @ok="onSave"
    @cancel="emit('update:modelValue', false)"
    @update:model-value="val => emit('update:modelValue', val)"
  >
    <!-- Multilingual Name Input Component -->
    <MultilingualInput
      :ko="newMenuNameKo"
      :en="newMenuNameEn"
      @update:ko="val => emit('update:newMenuNameKo', val)"
      @update:en="val => emit('update:newMenuNameEn', val)"
      :label="t('name', '메뉴명')"
      required
    />

    <va-input
      v-model="newMenu.path"
      :label="t('path', '라우트 경로')"
      class="mb-4 w-100"
    />
    
    <div class="mb-4">
      <label style="font-size: 0.8rem; color: var(--va-text-primary); margin-bottom: 0.5rem; display: block;">
        {{ t('menu_icon', '메뉴 아이콘') }}
      </label>
      <div class="d-flex align-center" style="gap: 1rem;">
        <va-icon :name="newMenu.icon || 'help_outline'" size="large" color="primary" />
        <va-button preset="secondary" border-color="primary" @click="onOpenIconPicker">
          {{ t('select_icon', '아이콘 선택') }}
        </va-button>
      </div>
    </div>
    
    <va-input
      v-model="newMenu.sortOrder"
      :label="t('sort_order', '정렬 순서')"
      type="number"
      class="mb-4 w-100"
    />
    
    <!-- Active Status Switch in Modal -->
    <div class="mb-4">
      <va-switch
        v-model="newMenu.isActive"
        :label="t('is_active_label', '사용 여부 (Active)')"
        color="success"
      />
    </div>

    <UserRoleSelect
      :model-value="newMenuRoles"
      @update:model-value="val => emit('update:newMenuRoles', val)"
      :label="t('required_roles', '접근 허용 역할')"
      class="mb-4 w-100"
      multiple
      clearable
      include-role-prefix
    />
  </va-modal>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import MultilingualInput from '~/components/MultilingualInput.vue'
import UserRoleSelect from '~/components/UserRoleSelect.vue'

const { t } = useI18n()

const props = defineProps<{
  modelValue: boolean
  newMenu: any
  newMenuNameKo: string
  newMenuNameEn: string
  newMenuRoles: any[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'update:newMenuNameKo', val: string): void
  (e: 'update:newMenuNameEn', val: string): void
  (e: 'update:newMenuRoles', val: any[]): void
  (e: 'open-icon-picker'): void
  (e: 'save'): void
}>()

const onOpenIconPicker = () => {
  emit('open-icon-picker')
}

const onSave = () => {
  emit('save')
}

defineExpose({
  onOpenIconPicker,
  onSave
})
</script>
