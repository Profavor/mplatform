<template>
  <div style="flex: 1; display: flex; flex-direction: column; position: relative; min-height: 0;">
    <div style="flex: 1; overflow-y: auto; padding: 1rem; min-height: 0;">
      <div
        v-for="action in ['CREATE', 'UPDATE', 'DELETE']"
        :key="action"
        style="margin-bottom: 1rem; padding: 0.75rem; border: 1px solid var(--va-background-border); border-radius: 6px; background: var(--va-background-element);"
      >
        <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--va-background-border); margin-bottom: 0.75rem; padding-bottom: 0.25rem;">
          <h3 style="font-weight: 600; font-size: 1.1rem; color: var(--va-text-primary); margin: 0;">{{ action }} Action</h3>
        </div>

        <div style="display: flex; gap: 1rem; align-items: flex-start;">
          <div style="flex: 2;">
            <h4 style="font-size: 0.9rem; margin-bottom: 0.25rem; color: var(--va-text-secondary);">결재선</h4>
            <div v-if="workflowConfigs[action]?.steps.length === 0" style="color: var(--va-text-secondary); font-size: 0.85rem; padding: 0.5rem 0;">
              결재 단계를 추가해주세요.
            </div>

            <div
              v-for="(step, sIdx) in workflowConfigs[action]?.steps"
              :key="step.id || sIdx"
              style="border: 1px solid var(--va-background-border); padding: 0.5rem; border-radius: 4px; margin-bottom: 0.5rem; background: var(--va-background-primary); box-shadow: 0 1px 2px rgba(0,0,0,0.02);"
            >
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; border-bottom: 1px dashed var(--va-background-border); padding-bottom: 0.25rem;">
                <span style="font-size: 0.85rem; font-weight: bold; color: var(--va-text-secondary);">Step {{ sIdx + 1 }}</span>
                <div style="display: flex; gap: 0.25rem;">
                  <va-button
                    size="small"
                    preset="secondary"
                    icon="arrow_upward"
                    @click="$emit('moveStepUp', action, sIdx)"
                    :disabled="sIdx === 0"
                    style="padding: 0; min-width: 24px; height: 24px;"
                  />
                  <va-button
                    size="small"
                    preset="secondary"
                    icon="arrow_downward"
                    @click="$emit('moveStepDown', action, sIdx)"
                    :disabled="sIdx === (workflowConfigs[action]?.steps.length - 1)"
                    style="padding: 0; min-width: 24px; height: 24px;"
                  />
                  <va-button
                    size="small"
                    color="danger"
                    preset="secondary"
                    icon="delete"
                    @click="$emit('removeStep', action, sIdx)"
                    style="padding: 0; min-width: 24px; height: 24px;"
                  />
                </div>
              </div>

              <div
                v-for="(u, uIdx) in step.users"
                :key="uIdx"
                style="display: flex; gap: 0.5rem; align-items: center; margin-bottom: 0.25rem;"
              >
                <va-select style="flex: 1;" v-model="u.stepType" :options="['CONSENSUS', 'APPROVAL']" placeholder="유형" />
                <va-select style="flex: 2;" v-model="u.assigneeId" :options="userOptions" value-by="value" text-by="text" placeholder="사용자 선택" />
                <va-button
                  size="small"
                  color="danger"
                  preset="secondary"
                  icon="close"
                  @click="$emit('removeUserFromStep', action, sIdx, uIdx)"
                  :disabled="step.users.length === 1"
                  style="padding: 0; min-width: 24px; height: 24px;"
                />
              </div>

              <div style="text-align: right; margin-top: 0.25rem;">
                <va-button size="small" preset="plain" icon="person_add" @click="$emit('addUserToStep', action, sIdx)">
                  승인자 추가
                </va-button>
              </div>
            </div>

            <va-button size="small" preset="secondary" icon="add" @click="$emit('addStep', action)" style="margin-top: 0.25rem;">
              단계 추가
            </va-button>
          </div>
        </div>
      </div>
    </div>

    <div style="padding: 1rem; border-top: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: flex-end;">
      <va-button color="primary" icon="save" :loading="isSavingWorkflows" @click="$emit('save')">
        워크플로우 설정 저장
      </va-button>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  workflowConfigs: Record<string, any>
  userOptions: Array<{ value: string; text: string }>
  isSavingWorkflows: boolean
}>()

defineEmits([
  'addStep',
  'removeStep',
  'moveStepUp',
  'moveStepDown',
  'addUserToStep',
  'removeUserFromStep',
  'save',
])
</script>
