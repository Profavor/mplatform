<template>
  <va-modal
    :model-value="modelValue"
    :title="modalData.id ? t('edit_workflow_title', '워크플로우 수정') : t('create_workflow_title', '새 워크플로우 등록')"
    size="large"
    style="--va-modal-max-width: 960px;"
    :ok-text="t('save', '저장')"
    :cancel-text="t('cancel', '취소')"
    @update:model-value="val => emit('update:modelValue', val)"
    @ok="onSave"
  >
    <div style="display: flex; flex-direction: column; gap: 1.25rem; max-height: 70vh; overflow-y: auto; overflow-x: hidden; padding: 0 4px;">
      
      <!-- SECTION 1: Basic Info -->
      <div style="background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 1rem; display: flex; flex-direction: column; gap: 0.85rem;">
        <h4 style="font-weight: 700; font-size: 0.95rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.4rem;">
          <va-icon name="info" color="primary" size="small" />
          {{ t('basic_info', '기본 정보') }}
        </h4>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.85rem;">
          <!-- Form Name KO -->
          <div>
            <label style="font-size: 0.78rem; font-weight: 700; color: var(--va-text-secondary); display: block; margin-bottom: 0.25rem;">
              {{ t('form_name_ko', '워크플로우명 (한국어)') }}
            </label>
            <va-input v-model="modalData.nameKo" :placeholder="t('form_name_ko_placeholder', '예: 마스터 데이터 생성 결재')" style="width: 100%;" dense>
              <template #prependInner>
                <span style="font-size: 0.75rem; color: var(--va-text-secondary); font-weight: 600; margin-right: 0.4rem; border-right: 1px solid var(--va-background-border); padding-right: 0.4rem; white-space: nowrap;">
                  {{ locale === 'en' ? 'Korean' : '한국어' }}
                </span>
              </template>
            </va-input>
          </div>

          <!-- Form Name EN -->
          <div>
            <label style="font-size: 0.78rem; font-weight: 700; color: var(--va-text-secondary); display: block; margin-bottom: 0.25rem;">
              {{ t('form_name_en', '워크플로우명 (영어)') }}
            </label>
            <va-input v-model="modalData.nameEn" :placeholder="t('form_name_en_placeholder', '예: Master Data Creation Approval')" style="width: 100%;" dense>
              <template #prependInner>
                <span style="font-size: 0.75rem; color: var(--va-text-secondary); font-weight: 600; margin-right: 0.4rem; border-right: 1px solid var(--va-background-border); padding-right: 0.4rem; white-space: nowrap;">
                  {{ locale === 'en' ? 'English' : '영어' }}
                </span>
              </template>
            </va-input>
          </div>
        </div>

        <!-- Description -->
        <div>
          <label style="font-size: 0.78rem; font-weight: 700; color: var(--va-text-secondary); display: block; margin-bottom: 0.25rem;">
            {{ t('form_description', '설명') }}
          </label>
          <va-input v-model="modalData.description" :placeholder="t('form_description_placeholder', '워크플로우 목적 및 용도를 입력하세요')" style="width: 100%;" dense />
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 0.85rem; align-items: end;">
          <!-- Action Type -->
          <div>
            <label style="font-size: 0.78rem; font-weight: 700; color: var(--va-text-secondary); display: block; margin-bottom: 0.25rem;">
              {{ t('action_type_col', '수행 액션') }} *
            </label>
            <va-select
              v-model="modalData.actionType"
              :options="actionTypeOptions"
              value-by="value"
              text-by="text"
              style="width: 100%;"
              dense
            />
          </div>

          <!-- Scope Level -->
          <div>
            <label style="font-size: 0.78rem; font-weight: 700; color: var(--va-text-secondary); display: block; margin-bottom: 0.25rem;">
              {{ t('scope_level', '적용 스코프') }}
            </label>
            <va-select
              v-model="modalData.scopeLevel"
              :options="scopeLevelOptions"
              value-by="value"
              text-by="text"
              style="width: 100%;"
              dense
              @update:model-value="emit('scope-changed', $event)"
            />
          </div>

          <!-- Domain Select -->
          <div>
            <label style="font-size: 0.78rem; font-weight: 700; color: var(--va-text-secondary); display: block; margin-bottom: 0.25rem;">
              {{ t('target_domain', '대상 도메인') }}
            </label>
            <va-select
              v-model="modalData.domainId"
              :options="domainOptions"
              value-by="value"
              text-by="text"
              :placeholder="t('select_domain_placeholder', '도메인을 선택하세요')"
              style="width: 100%;"
              dense
              @update:model-value="emit('domain-changed', $event)"
            />
          </div>
        </div>

        <!-- Classification Node Select (When Scope is NODE) -->
        <div v-if="modalData.scopeLevel === 'NODE'">
          <label style="font-size: 0.78rem; font-weight: 700; color: var(--va-text-secondary); display: block; margin-bottom: 0.25rem;">
            {{ t('target_node', '대상 분류 노드') }}
          </label>
          <va-select
            v-model="modalData.nodeId"
            :options="modalNodeOptions"
            value-by="value"
            text-by="text"
            :placeholder="t('select_node_placeholder', '분류 노드를 선택하세요')"
            style="width: 100%;"
            dense
          />
        </div>

        <!-- Options Checkboxes -->
        <div style="display: flex; gap: 1.5rem; align-items: center; margin-top: 0.25rem;">
          <va-checkbox v-model="modalData.isDefault" :label="t('set_default_workflow_desc', '기본 워크플로우로 지정')" />
          <va-checkbox v-model="modalData.isActive" :label="t('is_active_status', '활성화 상태')" />
        </div>
      </div>

      <!-- SECTION 2: Permissions & Field Rules -->
      <div style="background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 1rem; display: flex; flex-direction: column; gap: 0.85rem;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <h4 style="font-weight: 700; font-size: 0.95rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.4rem;">
            <va-icon name="assignment_ind" color="primary" size="small" />
            {{ modalData.actionType === 'SCHEMA_CHANGE' ? t('initiator_rules_title', '기안 대상 규칙') : t('permissions_rules_title', '기안 권한 및 필드 제어 규칙') }}
          </h4>
          <va-button size="small" color="primary" icon="add" preset="secondary" @click="addPermissionRule">
            {{ t('add_rule', '규칙 추가') }}
          </va-button>
        </div>

        <div v-if="modalData.permissions.length === 0" style="font-size: 0.8rem; color: var(--va-text-secondary); text-align: center; padding: 0.5rem;">
          {{ modalData.actionType === 'SCHEMA_CHANGE' ? t('no_rules_schema_change', '모든 권한자 또는 기본 정책으로 동작합니다.') : t('no_rules_default', '등록된 규칙이 없습니다. 전체 필드가 편집 가능합니다.') }}
        </div>

        <div
          v-for="(rule, rIdx) in modalData.permissions"
          :key="rIdx"
          style="border: 1px solid var(--va-background-border); border-radius: 6px; padding: 0.85rem; background: var(--va-background-primary); display: flex; flex-direction: column; gap: 0.6rem;"
        >
          <!-- Target Selection Row -->
          <div style="display: flex; justify-content: space-between; align-items: center; gap: 0.5rem;">
            <div style="display: flex; gap: 0.5rem; align-items: center; flex: 1;">
              <va-select style="width: 140px;" v-model="rule.targetType" :options="permissionTargetTypeOptions" value-by="value" text-by="text" dense />
              <va-select v-if="rule.targetType === 'USER'" style="flex: 1; max-width: 320px;" v-model="rule.targetId" :options="userOptions" value-by="value" text-by="text" :placeholder="t('select_user', '사용자 선택')" dense />
              <va-select v-else style="flex: 1; max-width: 320px;" v-model="rule.targetRole" :options="roleOptions" value-by="value" text-by="text" :placeholder="t('select_role', '역할 선택')" dense />
            </div>
            <va-button size="small" color="danger" preset="plain" icon="delete" @click="modalData.permissions.splice(rIdx, 1)">
              {{ t('delete', '삭제') }}
            </va-button>
          </div>

          <!-- Field Controls (Only for Data Record Actions: CREATE, UPDATE, DELETE) -->
          <div v-if="modalData.actionType !== 'SCHEMA_CHANGE'" style="display: flex; flex-direction: column; gap: 0.75rem;">
            <div>
              <label style="font-size: 0.78rem; font-weight: 700; color: var(--va-primary); display: block; margin-bottom: 0.3rem;">
                {{ t('editable_fields_title', '편집 가능 필드 지정') }}
              </label>
              
              <!-- Selected Editable Chips List -->
              <div v-if="rule.editableFields && rule.editableFields.length > 0" style="display: flex; flex-wrap: wrap; gap: 0.35rem; margin-bottom: 0.4rem; padding: 0.4rem; background: var(--va-background-element); border-radius: 6px; border: 1px solid var(--va-background-border);">
                <va-chip
                  v-for="fKey in rule.editableFields"
                  :key="fKey"
                  size="small"
                  color="primary"
                  style="font-weight: 600; font-size: 0.78rem; padding: 3px 8px; display: inline-flex; align-items: center;"
                >
                  <span>{{ getFieldOptionLabel(fKey) }}</span>
                  <va-icon
                    name="close"
                    size="14px"
                    style="margin-left: 6px; cursor: pointer; opacity: 0.85;"
                    :title="t('delete', '삭제')"
                    @click.stop="onRemoveEditableField(rule, fKey)"
                  />
                </va-chip>
              </div>

              <va-select
                v-model="rule._tempEditable"
                :options="getRemainingFieldOptions(rule.editableFields)"
                value-by="value"
                text-by="text"
                :placeholder="t('add_editable_field_placeholder', '+ 편집 가능 필드 추가')"
                style="width: 100%;"
                dense
                @update:model-value="onAddEditableField(rule, $event)"
              />
            </div>

            <div>
              <label style="font-size: 0.78rem; font-weight: 700; color: #dc2626; display: block; margin-bottom: 0.3rem;">
                {{ t('hidden_fields_title', '숨김 필드 지정') }}
              </label>

              <!-- Selected Hidden Chips List -->
              <div v-if="rule.hiddenFields && rule.hiddenFields.length > 0" style="display: flex; flex-wrap: wrap; gap: 0.35rem; margin-bottom: 0.4rem; padding: 0.4rem; background: var(--va-background-element); border-radius: 6px; border: 1px solid var(--va-background-border);">
                <va-chip
                  v-for="fKey in rule.hiddenFields"
                  :key="fKey"
                  size="small"
                  color="danger"
                  style="font-weight: 600; font-size: 0.78rem; padding: 3px 8px; display: inline-flex; align-items: center;"
                >
                  <span>{{ getFieldOptionLabel(fKey) }}</span>
                  <va-icon
                    name="close"
                    size="14px"
                    style="margin-left: 6px; cursor: pointer; opacity: 0.85;"
                    :title="t('delete', '삭제')"
                    @click.stop="onRemoveHiddenField(rule, fKey)"
                  />
                </va-chip>
              </div>

              <va-select
                v-model="rule._tempHidden"
                :options="getRemainingFieldOptions(rule.hiddenFields)"
                value-by="value"
                text-by="text"
                :placeholder="t('add_hidden_field_placeholder', '+ 숨김 필드 추가')"
                style="width: 100%;"
                dense
                @update:model-value="onAddHiddenField(rule, $event)"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- SECTION 3: Approval Line Timeline -->
      <div style="background: var(--va-background-element); border: 1px solid var(--va-background-border); border-radius: 8px; padding: 1rem; display: flex; flex-direction: column; gap: 0.85rem;">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <h4 style="font-weight: 700; font-size: 0.95rem; margin: 0; color: var(--va-text-primary); display: flex; align-items: center; gap: 0.4rem;">
            <va-icon name="linear_scale" color="primary" size="small" />
            {{ t('approval_line_title', '결재선 단계 설정') }}
          </h4>
          <va-button size="small" color="primary" icon="add" preset="secondary" @click="addApprovalStep">
            {{ t('add_approval_step', '단계 추가') }}
          </va-button>
        </div>

        <div v-if="modalData.steps.length === 0" style="font-size: 0.8rem; color: var(--va-text-secondary); text-align: center; padding: 0.5rem;">
          {{ t('no_approval_steps', '설정된 결재 단계가 없습니다. 단계를 추가하세요.') }}
        </div>

        <!-- Step Timeline Cards -->
        <div
          v-for="(step, sIdx) in modalData.steps"
          :key="sIdx"
          style="border: 1px solid var(--va-background-border); border-radius: 6px; padding: 0.85rem; background: var(--va-background-primary); display: flex; flex-direction: column; gap: 0.5rem;"
        >
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span style="font-weight: 700; font-size: 0.85rem; color: var(--va-primary);">
              STEP {{ sIdx + 1 }}
            </span>
            <va-button size="small" color="danger" preset="plain" icon="delete" @click="modalData.steps.splice(sIdx, 1)">
              {{ t('delete', '삭제') }}
            </va-button>
          </div>

          <div style="display: grid; grid-template-columns: 1fr 1fr 90px 1.4fr 90px; gap: 0.4rem; align-items: center; width: 100%; min-width: 0; box-sizing: border-box;">
            <va-input v-model="step.stepNameKo" :placeholder="t('step_name_ko_placeholder', '스텝명 (한국어)')" dense style="min-width: 0;">
              <template #prependInner>
                <span style="font-size: 0.75rem; color: var(--va-text-secondary); font-weight: 600; margin-right: 0.3rem; border-right: 1px solid var(--va-background-border); padding-right: 0.3rem; white-space: nowrap;">
                  {{ locale === 'en' ? 'Korean' : '한국어' }}
                </span>
              </template>
            </va-input>
            <va-input v-model="step.stepNameEn" :placeholder="t('step_name_en_placeholder', 'Step Name (EN)')" dense style="min-width: 0;">
              <template #prependInner>
                <span style="font-size: 0.75rem; color: var(--va-text-secondary); font-weight: 600; margin-right: 0.3rem; border-right: 1px solid var(--va-background-border); padding-right: 0.3rem; white-space: nowrap;">
                  {{ locale === 'en' ? 'English' : '영어' }}
                </span>
              </template>
            </va-input>
            <va-select v-model="step.assigneeType" :options="stepAssigneeTypeOptions" value-by="value" text-by="text" dense style="min-width: 0;" />
            <va-select v-if="step.assigneeType === 'USER'" v-model="step.assigneeId" :options="userOptions" value-by="value" text-by="text" :placeholder="t('select_approver_user', '승인자 선택')" dense style="min-width: 0;" />
            <va-select v-else v-model="step.assigneeRole" :options="roleOptions" value-by="value" text-by="text" :placeholder="t('select_approver_role', '역할 선택')" dense style="min-width: 0;" />
            <va-select v-model="step.stepType" :options="stepTypeOptions" value-by="value" text-by="text" dense style="min-width: 0;" />
          </div>
        </div>
      </div>

    </div>
  </va-modal>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t, locale } = useI18n()

interface OptionItem {
  text: string
  value: any
}

const props = defineProps<{
  modelValue: boolean
  modalData: any
  actionTypeOptions: OptionItem[]
  scopeLevelOptions: OptionItem[]
  domainOptions: OptionItem[]
  modalNodeOptions: OptionItem[]
  permissionTargetTypeOptions: OptionItem[]
  stepAssigneeTypeOptions: OptionItem[]
  stepTypeOptions: OptionItem[]
  userOptions: OptionItem[]
  roleOptions: OptionItem[]
  domainFieldOptions: OptionItem[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'save'): void
  (e: 'scope-changed', value: any): void
  (e: 'domain-changed', value: any): void
}>()

const addPermissionRule = () => {
  props.modalData.permissions.push({
    targetType: 'ROLE',
    targetRole: props.roleOptions[0]?.value || 'ROLE_USER',
    targetId: null,
    editableFields: [],
    hiddenFields: [],
    _tempEditable: null,
    _tempHidden: null
  })
}

const addApprovalStep = () => {
  props.modalData.steps.push({
    stepNameKo: `단계 ${props.modalData.steps.length + 1}`,
    stepNameEn: `Step ${props.modalData.steps.length + 1}`,
    assigneeType: 'ROLE',
    assigneeRole: props.roleOptions[0]?.value || 'ROLE_ADMIN',
    assigneeId: null,
    stepType: 'APPROVAL'
  })
}

const onSave = () => {
  emit('save')
}

const getFieldOptionLabel = (fieldKey: string) => {
  const found = props.domainFieldOptions?.find(opt => opt.value === fieldKey)
  return found ? found.text : fieldKey
}

const getRemainingFieldOptions = (alreadySelectedList: string[] = []) => {
  if (!props.domainFieldOptions) return []
  return props.domainFieldOptions.filter(opt => !alreadySelectedList.includes(opt.value))
}

const onAddEditableField = (rule: any, key: any) => {
  if (!key) return
  if (!rule.editableFields) rule.editableFields = []
  if (!rule.editableFields.includes(key)) {
    rule.editableFields.push(key)
  }
  rule._tempEditable = null
}

const onRemoveEditableField = (rule: any, key: any) => {
  if (!rule.editableFields) return
  rule.editableFields = rule.editableFields.filter((k: string) => k !== key)
}

const onAddHiddenField = (rule: any, key: any) => {
  if (!key) return
  if (!rule.hiddenFields) rule.hiddenFields = []
  if (!rule.hiddenFields.includes(key)) {
    rule.hiddenFields.push(key)
  }
  rule._tempHidden = null
}

const onRemoveHiddenField = (rule: any, key: any) => {
  if (!rule.hiddenFields) return
  rule.hiddenFields = rule.hiddenFields.filter((k: string) => k !== key)
}

defineExpose({
  addPermissionRule,
  addApprovalStep,
  onSave
})
</script>
