import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DqRuleModal from '../../components/admin/DqRuleModal.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('DqRuleModal.vue (TDD Component Test)', () => {
  const createMockFormData = () => ({
    ruleType: 'REGEX',
    severity: 'ERROR',
    params: '^[0-9]+$',
    message: '숫자만 입력 가능합니다.',
    sortOrder: 1,
    isActive: true
  })

  const mockRuleTypeOptions = ['REGEX', 'RANGE', 'NOT_NULL']
  const mockSeverityOptions = ['ERROR', 'WARNING', 'INFO']

  it('DQ 규칙 모달 기본 렌더링 및 폼 바인딩 검증', () => {
    const formData = createMockFormData()
    const wrapper = mount(DqRuleModal, {
      props: {
        modelValue: true,
        isEdit: false,
        formData: formData,
        ruleTypeOptions: mockRuleTypeOptions,
        severityOptions: mockSeverityOptions
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /></div>'
          },
          'va-select': true,
          'va-input': true,
          'va-checkbox': true
        }
      }
    })

    expect(wrapper.find('.va-modal-stub').exists()).toBe(true)
  })

  it('저장 및 취소 이벤트 방출 검증', async () => {
    const formData = createMockFormData()
    const wrapper = mount(DqRuleModal, {
      props: {
        modelValue: true,
        isEdit: true,
        formData: formData,
        ruleTypeOptions: mockRuleTypeOptions,
        severityOptions: mockSeverityOptions
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /></div>'
          },
          'va-select': true,
          'va-input': true,
          'va-checkbox': true
        }
      }
    })

    wrapper.vm.onSave()
    expect(wrapper.emitted('save')).toBeTruthy()

    wrapper.vm.onCancel()
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0]).toEqual([false])
  })
})
