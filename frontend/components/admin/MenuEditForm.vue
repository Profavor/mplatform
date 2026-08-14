<template>
  <div>
    <div v-if="selectedMenu" class="edit-form">
      <h3 class="mb-4">{{ t('edit_menu', '메뉴 수정') }}</h3>
      
      <!-- Multilingual Name Input Component -->
      <MultilingualInput
        :ko="selectedMenuNameKo"
        :en="selectedMenuNameEn"
        @update:ko="val => emit('update:selectedMenuNameKo', val)"
        @update:en="val => emit('update:selectedMenuNameEn', val)"
        :label="t('name', '메뉴명')"
        required
      />

      <va-input
        v-model="selectedMenu.path"
        :label="t('path', '라우트 경로')"
        class="mb-4 w-100"
      />
      
      <div class="mb-4">
        <label style="font-size: 0.8rem; color: var(--va-text-primary); margin-bottom: 0.5rem; display: block;">
          {{ t('menu_icon', '메뉴 아이콘') }}
        </label>
        <div class="d-flex align-center" style="gap: 1rem;">
          <va-icon :name="selectedMenu.icon || 'help_outline'" size="large" color="primary" />
          <va-button preset="secondary" border-color="primary" @click="onOpenIconPicker">
            {{ t('select_icon', '아이콘 선택') }}
          </va-button>
        </div>
      </div>

      <!-- Active Status Switch -->
      <div class="mb-4">
        <va-switch
          v-model="selectedMenu.isActive"
          :label="t('is_active_label', '사용 여부 (Active)')"
          color="success"
        />
      </div>
      
      <div class="mb-4 w-100">
        <UserRoleSelect
          :model-value="selectedMenuRoles"
          @update:model-value="val => emit('update:selectedMenuRoles', val)"
          :label="t('required_roles', '접근 허용 역할')"
          class="w-100"
          multiple
          clearable
          include-role-prefix
          :disabled="selectedMenuHasChildren"
        />
        <div v-if="selectedMenuHasChildren" style="font-size: 0.78rem; color: #c2410c; margin-top: 0.35rem; display: flex; align-items: center; gap: 0.25rem; font-weight: 600;">
          <va-icon name="info" size="small" color="warning" />
          <span>{{ t('menu_children_role_union_notice', '하위 메뉴가 존재하여 역할 권한이 자동 상속/결합됩니다.') }}</span>
        </div>
      </div>
      
      <div class="d-flex justify-end mt-4">
        <va-button @click="onSave">{{ t('save_changes', '변경사항 저장') }}</va-button>
      </div>
    </div>
    <div v-else class="text-center mt-5" style="color: var(--va-secondary); padding: 3rem 0;">
      <va-icon name="info" size="large" color="secondary" class="mb-2" />
      <div>{{ t('select_menu_prompt', '좌측 트리에서 수정할 메뉴를 선택해주세요.') }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import MultilingualInput from '~/components/MultilingualInput.vue'
import UserRoleSelect from '~/components/UserRoleSelect.vue'

const { t } = useI18n()

const props = defineProps<{
  selectedMenu: any
  selectedMenuNameKo: string
  selectedMenuNameEn: string
  selectedMenuRoles: any[]
  selectedMenuHasChildren: boolean
}>()

const emit = defineEmits<{
  (e: 'update:selectedMenuNameKo', val: string): void
  (e: 'update:selectedMenuNameEn', val: string): void
  (e: 'update:selectedMenuRoles', val: any[]): void
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
