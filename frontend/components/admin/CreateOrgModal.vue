<template>
  <va-modal
    :model-value="modelValue"
    @update:model-value="val => emit('update:modelValue', val)"
    :title="t('create_new_org', '신규 조직(Tenant) 생성')"
    hide-default-actions
    size="small"
    :prevent-click-outside="true"
    :no-outside-dismiss="true"
  >
    <div class="org-modal-body">
      <va-input
        :model-value="form.name"
        @update:model-value="val => form.name = val"
        :label="t('org_code_placeholder', '조직 고유 코드 (예: ORG_HQ)')"
        required
      />
      <div>
        <div class="field-label-header">
          {{ t('org_display_name_placeholder', '조직 표시명 (다국어)') }}
        </div>
        <div class="multilingual-row">
          <va-input
            :model-value="form.displayNameKo"
            @update:model-value="val => form.displayNameKo = val"
            class="flex-1-min-0"
            required
          >
            <template #prependInner>
              <span class="lang-tag">Korean</span>
            </template>
          </va-input>
          <va-input
            :model-value="form.displayNameEn"
            @update:model-value="val => form.displayNameEn = val"
            class="flex-1-min-0"
            required
          >
            <template #prependInner>
              <span class="lang-tag">English</span>
            </template>
          </va-input>
        </div>
      </div>
      <div>
        <div class="field-label-header">
          {{ t('org_description', '조직 설명 (다국어)') }}
        </div>
        <div class="multilingual-row">
          <va-textarea
            :model-value="form.descriptionKo"
            @update:model-value="val => form.descriptionKo = val"
            class="flex-1-min-0"
            :min-rows="2"
          >
            <template #prependInner>
              <span class="lang-tag mt-textarea">Korean</span>
            </template>
          </va-textarea>
          <va-textarea
            :model-value="form.descriptionEn"
            @update:model-value="val => form.descriptionEn = val"
            class="flex-1-min-0"
            :min-rows="2"
          >
            <template #prependInner>
              <span class="lang-tag mt-textarea">English</span>
            </template>
          </va-textarea>
        </div>
      </div>
      <div>
        <div class="field-label-header">
          {{ t('email_domain', '이메일 도메인') }}
        </div>
        <va-input
          :model-value="form.emailDomain"
          @update:model-value="val => form.emailDomain = val"
          :placeholder="t('placeholder_email_domain', '예: company.com')"
          :messages="[t('org_email_domain_desc', '조직의 기본 이메일 도메인을 설정합니다.')]"
        />
      </div>
      <div class="modal-footer">
        <va-button preset="secondary" @click="onCancel">
          {{ t('cancel', '취소') }}
        </va-button>
        <va-button color="primary" @click="onSave">
          {{ t('create_organization', '조직 생성') }}
        </va-button>
      </div>
    </div>
  </va-modal>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const props = defineProps<{
  modelValue: boolean
  form: {
    name: string
    displayNameKo: string
    displayNameEn: string
    descriptionKo: string
    descriptionEn: string
    emailDomain?: string
  }
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'save'): void
}>()

const onCancel = () => {
  emit('update:modelValue', false)
}

const onSave = () => {
  emit('save')
}

defineExpose({
  onCancel,
  onSave
})
</script>

<style scoped>
.org-modal-body {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.field-label-header {
  font-size: 0.6rem;
  font-weight: 700;
  color: var(--va-primary);
  margin-bottom: 0.25rem;
  text-transform: uppercase;
  letter-spacing: 0.4px;
}

.multilingual-row {
  display: flex;
  gap: 0.5rem;
  flex-direction: row;
  min-width: 0;
}

.flex-1-min-0 {
  flex: 1;
  min-width: 0;
}

.lang-tag {
  font-size: 0.75rem;
  color: #888;
  font-weight: 600;
  margin-right: 0.5rem;
  border-right: 1px solid #ddd;
  padding-right: 0.5rem;
  white-space: nowrap;
}

.mt-textarea {
  margin-top: 0.25rem;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 1rem;
}
</style>
