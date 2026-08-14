<template>
  <va-modal :model-value="modelValue" hide-default-actions size="small" no-padding @update:model-value="val => emit('update:modelValue', val)">
    <template #header>
      <div style="padding: 1.25rem 1.5rem; border-bottom: 1px solid var(--va-background-border); display: flex; align-items: center; gap: 0.75rem;">
        <div style="background: var(--va-primary); padding: 0.5rem; border-radius: 8px;">
          <va-icon :name="editingGroup ? 'edit' : 'add_circle'" color="white" size="small" />
        </div>
        <h3 style="margin: 0; font-size: 1.15rem; font-weight: 700;">
          {{ editingGroup ? t('code_management.edit_group') : t('code_management.add_group') }}
        </h3>
      </div>
    </template>
    <div style="padding: 1.5rem; display: flex; flex-direction: column; gap: 1.25rem; overflow-x: hidden; box-sizing: border-box; width: 100%;">
      <va-input v-model="groupForm.groupCode" :label="t('code_management.group_code')" :disabled="!!editingGroup" outline />
      <div style="display: flex; gap: 1rem;">
        <va-input v-model="groupForm.nameKo" :label="t('code_management.name_ko')" outline style="flex: 1;">
          <template #appendInner>
            <va-dropdown :close-on-content-click="false" trigger="click" placement="bottom-end">
              <template #anchor>
                <va-icon name="sentiment_satisfied_alt" size="small" style="cursor: pointer" />
              </template>
              <ClientOnly>
                <EmojiPicker :native="true" @select="(e: any) => onEmojiSelect(e, 'nameKo')" />
              </ClientOnly>
            </va-dropdown>
          </template>
        </va-input>
        <va-input v-model="groupForm.nameEn" :label="t('code_management.name_en')" outline style="flex: 1;">
          <template #appendInner>
            <va-dropdown :close-on-content-click="false" trigger="click" placement="bottom-end">
              <template #anchor>
                <va-icon name="sentiment_satisfied_alt" size="small" style="cursor: pointer" />
              </template>
              <ClientOnly>
                <EmojiPicker :native="true" @select="(e: any) => onEmojiSelect(e, 'nameEn')" />
              </ClientOnly>
            </va-dropdown>
          </template>
        </va-input>
      </div>
      <div style="display: flex; gap: 1rem;">
        <va-input v-model="groupForm.descKo" :label="t('code_management.desc_ko')" outline style="flex: 1;" />
        <va-input v-model="groupForm.descEn" :label="t('code_management.desc_en')" outline style="flex: 1;" />
      </div>
      <div style="background: var(--va-background-element); padding: 1rem; border-radius: 8px; display: flex; align-items: center; justify-content: space-between;">
        <span style="font-weight: 600; font-size: 0.9rem;">{{ t('code_management.active') }}</span>
        <va-switch v-model="groupForm.isActive" size="small" />
      </div>
    </div>
    <template #footer>
      <div style="padding: 1rem 1.5rem; border-top: 1px solid var(--va-background-border); display: flex; justify-content: flex-end; gap: 0.75rem; background: var(--va-background-element);">
        <va-button preset="secondary" color="secondary" @click="emit('update:modelValue', false)">{{ t('code_management.cancel') }}</va-button>
        <va-button @click="onSave">{{ t('code_management.save') }}</va-button>
      </div>
    </template>
  </va-modal>
</template>

<script setup lang="ts">
import { defineAsyncComponent } from 'vue'
import { useI18n } from 'vue-i18n'

const EmojiPicker = defineAsyncComponent(() => import('vue3-emoji-picker'))

const { t } = useI18n()

const props = defineProps<{
  modelValue: boolean
  groupForm: any
  editingGroup: any
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'save'): void
}>()

const onEmojiSelect = (emoji: any, field: string) => {
  if (emoji && emoji.i) {
    props.groupForm[field] = (props.groupForm[field] || '') + emoji.i
  }
}

const onSave = () => {
  emit('save')
}

defineExpose({
  onSave
})
</script>
