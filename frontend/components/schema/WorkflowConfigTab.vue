<template>
  <div style="flex: 1; display: flex; flex-direction: column; position: relative; min-height: 0;">
    <div style="flex: 1; overflow-y: auto; padding: 1rem; min-height: 0;">
      
      <!-- Tab Selection for Actions -->
      <div style="display: flex; gap: 0.5rem; margin-bottom: 1rem; border-bottom: 2px solid var(--va-background-border); padding-bottom: 0.5rem;">
        <va-button
          v-for="act in ['CREATE', 'UPDATE', 'DELETE']"
          :key="act"
          :preset="activeAction === act ? 'primary' : 'secondary'"
          size="small"
          @click="activeAction = act"
        >
          {{ act }} 액션 워크플로우
        </va-button>
      </div>

      <div style="padding: 1rem; border: 1px solid var(--va-background-border); border-radius: 8px; background: var(--va-background-element); margin-bottom: 1.5rem;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
          <h3 style="font-weight: 700; font-size: 1.05rem; color: var(--va-text-primary); margin: 0; display: flex; align-items: center; gap: 0.5rem;">
            <va-icon name="account_tree" color="primary" />
            {{ activeAction }} 다단계 결재선 설정
          </h3>
          <va-button size="small" preset="secondary" icon="add" @click="$emit('addStep', activeAction)">
            결재 단계 추가
          </va-button>
        </div>

        <div v-if="!workflowConfigs[activeAction]?.steps || workflowConfigs[activeAction]?.steps.length === 0" style="color: var(--va-text-secondary); font-size: 0.85rem; padding: 1.5rem; text-align: center; background: var(--va-background-secondary); border-radius: 6px;">
          설정된 결재 단계가 없습니다. "결재 단계 추가" 버튼을 눌러 승인 라인을 구성하세요.
        </div>

        <div
          v-for="(step, sIdx) in workflowConfigs[activeAction]?.steps"
          :key="step.id || sIdx"
          style="border: 1px solid var(--va-background-border); padding: 0.75rem; border-radius: 6px; margin-bottom: 0.75rem; background: var(--va-background-primary); box-shadow: 0 1px 3px rgba(0,0,0,0.03);"
        >
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; border-bottom: 1px dashed var(--va-background-border); padding-bottom: 0.35rem;">
            <div style="display: flex; align-items: center; gap: 0.5rem;">
              <va-badge :text="'Step ' + (sIdx + 1)" color="primary" />
              <input
                type="text"
                v-model="step.stepName"
                placeholder="단계명 (예: 1차 부서장 승인)"
                style="font-size: 0.85rem; padding: 0.2rem 0.5rem; border: 1px solid var(--va-background-border); border-radius: 4px; background: var(--va-background-element); color: var(--va-text-primary);"
              />
            </div>
            <div style="display: flex; gap: 0.25rem;">
              <va-button
                size="small"
                preset="secondary"
                icon="arrow_upward"
                @click="$emit('moveStepUp', activeAction, sIdx)"
                :disabled="sIdx === 0"
                style="padding: 0; min-width: 26px; height: 26px;"
              />
              <va-button
                size="small"
                preset="secondary"
                icon="arrow_downward"
                @click="$emit('moveStepDown', activeAction, sIdx)"
                :disabled="sIdx === (workflowConfigs[activeAction]?.steps.length - 1)"
                style="padding: 0; min-width: 26px; height: 26px;"
              />
              <va-button
                size="small"
                color="danger"
                preset="secondary"
                icon="delete"
                @click="$emit('removeStep', activeAction, sIdx)"
                style="padding: 0; min-width: 26px; height: 26px;"
              />
            </div>
          </div>

          <div
            v-for="(u, uIdx) in step.users"
            :key="uIdx"
            style="display: flex; gap: 0.5rem; align-items: center; margin-bottom: 0.35rem;"
          >
            <va-select style="width: 140px;" v-model="u.assigneeType" :options="[{value:'USER',text:'사용자'},{value:'ROLE',text:'역할(Role)'}]" value-by="value" text-by="text" placeholder="대상 유형" />
            <va-select v-if="u.assigneeType !== 'ROLE'" style="flex: 2;" v-model="u.assigneeId" :options="userOptions" value-by="value" text-by="text" placeholder="승인 사용자 선택" />
            <va-select v-else style="flex: 2;" v-model="u.assigneeRole" :options="roleOptions" value-by="value" text-by="text" placeholder="승인 역할 선택" />
            
            <va-button
              size="small"
              color="danger"
              preset="secondary"
              icon="close"
              @click="$emit('removeUserFromStep', activeAction, sIdx, uIdx)"
              :disabled="step.users.length === 1"
              style="padding: 0; min-width: 24px; height: 24px;"
            />
          </div>

          <div style="text-align: right; margin-top: 0.25rem;">
            <va-button size="small" preset="plain" icon="person_add" @click="$emit('addUserToStep', activeAction, sIdx)">
              승인자 추가
            </va-button>
          </div>
        </div>
      </div>

    </div>

    <!-- Footer Save Bar -->
    <div style="padding: 1rem; border-top: 1px solid var(--va-background-border); background: var(--va-background-element); display: flex; justify-content: flex-end;">
      <va-button color="primary" icon="save" :loading="isSavingWorkflows" @click="$emit('save')">
        워크플로우 설정 저장
      </va-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

defineProps<{
  workflowConfigs: Record<string, any>
  userOptions: Array<{ value: string; text: string }>
  roleOptions?: Array<{ value: string; text: string }>
  isSavingWorkflows: boolean
}>()

const activeAction = ref('CREATE')

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
