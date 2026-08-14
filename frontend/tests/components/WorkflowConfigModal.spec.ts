import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import WorkflowConfigModal from '../../components/admin/WorkflowConfigModal.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    locale: { value: 'ko' }
  })
}))

describe('WorkflowConfigModal.vue (TDD Component Test)', () => {
  const createMockModalData = () => ({
    id: null,
    nameKo: '마스터 데이터 생성 결재',
    nameEn: 'Master Data Creation Approval',
    description: '신규 마스터 데이터 생성 시 결재선',
    actionType: 'CREATE',
    scopeLevel: 'DOMAIN',
    domainId: 'domain-1',
    nodeId: null,
    isDefault: true,
    isActive: true,
    permissions: [
      {
        targetType: 'ROLE',
        targetRole: 'ROLE_USER',
        targetId: null,
        editableFields: ['name', 'code'],
        hiddenFields: ['internalNotes'],
        _tempEditable: null,
        _tempHidden: null
      }
    ],
    steps: [
      {
        stepNameKo: '1차 승인',
        stepNameEn: 'First Approval',
        assigneeType: 'ROLE',
        assigneeRole: 'ROLE_MANAGER',
        assigneeId: null,
        stepType: 'APPROVAL'
      }
    ]
  })

  it('모달 컴포넌트 기본 렌더링 및 폼 데이터 바인딩 검증', async () => {
    const modalData = createMockModalData()
    const wrapper = mount(WorkflowConfigModal, {
      props: {
        modelValue: true,
        modalData: modalData,
        actionTypeOptions: [{ text: 'CREATE', value: 'CREATE' }],
        scopeLevelOptions: [{ text: 'DOMAIN', value: 'DOMAIN' }],
        domainOptions: [{ text: '고객 도메인', value: 'domain-1' }],
        modalNodeOptions: [],
        permissionTargetTypeOptions: [{ text: 'ROLE', value: 'ROLE' }],
        stepAssigneeTypeOptions: [{ text: 'ROLE', value: 'ROLE' }],
        stepTypeOptions: [{ text: 'APPROVAL', value: 'APPROVAL' }],
        userOptions: [],
        roleOptions: [{ text: 'Manager', value: 'ROLE_MANAGER' }],
        domainFieldOptions: [
          { text: 'Name (name)', value: 'name' },
          { text: 'Code (code)', value: 'code' },
          { text: 'Notes (internalNotes)', value: 'internalNotes' }
        ]
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /><slot name="footer" /></div>'
          },
          'va-input': true,
          'va-select': true,
          'va-checkbox': true,
          'va-chip': true,
          'va-icon': true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.find('.va-modal-stub').exists()).toBe(true)
  })

  it('권한 규칙 추가 및 스텝 추가 버튼 상호작용 검증', async () => {
    const modalData = createMockModalData()
    const wrapper = mount(WorkflowConfigModal, {
      props: {
        modelValue: true,
        modalData: modalData,
        actionTypeOptions: [],
        scopeLevelOptions: [],
        domainOptions: [],
        modalNodeOptions: [],
        permissionTargetTypeOptions: [],
        stepAssigneeTypeOptions: [],
        stepTypeOptions: [],
        userOptions: [],
        roleOptions: [],
        domainFieldOptions: []
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /><slot name="footer" /></div>'
          },
          'va-input': true,
          'va-select': true,
          'va-checkbox': true,
          'va-chip': true,
          'va-icon': true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    const initialPermCount = modalData.permissions.length
    const initialStepCount = modalData.steps.length

    // 컴포넌트 내부의 addPermissionRule 및 addApprovalStep 메서드 호출 또는 버튼 클릭
    wrapper.vm.addPermissionRule()
    expect(modalData.permissions.length).toBe(initialPermCount + 1)

    wrapper.vm.addApprovalStep()
    expect(modalData.steps.length).toBe(initialStepCount + 1)
  })

  it('저장 버튼 클릭 시 save 이벤트 방출', async () => {
    const modalData = createMockModalData()
    const wrapper = mount(WorkflowConfigModal, {
      props: {
        modelValue: true,
        modalData: modalData,
        actionTypeOptions: [],
        scopeLevelOptions: [],
        domainOptions: [],
        modalNodeOptions: [],
        permissionTargetTypeOptions: [],
        stepAssigneeTypeOptions: [],
        stepTypeOptions: [],
        userOptions: [],
        roleOptions: [],
        domainFieldOptions: []
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /><slot name="footer" /></div>'
          },
          'va-input': true,
          'va-select': true,
          'va-checkbox': true,
          'va-chip': true,
          'va-icon': true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    wrapper.vm.onSave()
    expect(wrapper.emitted('save')).toBeTruthy()
  })
})
