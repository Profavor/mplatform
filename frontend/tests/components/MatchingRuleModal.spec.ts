import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import MatchingRuleModal from '../../components/admin/MatchingRuleModal.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, params?: any) => {
      if (params && typeof params === 'object') {
        let res = key
        Object.keys(params).forEach(k => {
          res = res.replace(`{${k}}`, params[k])
        })
        return res
      }
      return key
    }
  })
}))

describe('MatchingRuleModal.vue (TDD Component Test)', () => {
  it('모달 렌더링 및 props 바인딩 검증', async () => {
    const wrapper = mount(MatchingRuleModal, {
      props: {
        modelValue: true,
        isEditMode: false,
        isSaving: false,
        form: {
          ruleName: '고객 일치 규칙',
          matchType: 'EXACT',
          selectedFields: ['name', 'phone'],
          targetFieldKeysInput: '',
          similarityThreshold: 0.85,
          isActive: true
        },
        matchTypeOptions: [
          { text: 'EXACT', value: 'EXACT' },
          { text: 'FUZZY', value: 'FUZZY' }
        ],
        domainFieldOptions: [
          { text: 'Name', value: 'name' },
          { text: 'Phone', value: 'phone' }
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
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.find('.va-modal-stub').exists()).toBe(true)
  })

  it('저장 버튼 클릭 시 save 이벤트 방출', async () => {
    const wrapper = mount(MatchingRuleModal, {
      props: {
        modelValue: true,
        isEditMode: false,
        isSaving: false,
        form: {
          ruleName: '새 매칭 규칙',
          matchType: 'EXACT',
          selectedFields: ['name'],
          targetFieldKeysInput: '',
          similarityThreshold: 0.85,
          isActive: true
        },
        matchTypeOptions: [],
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
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    const buttons = wrapper.findAll('.va-btn-stub')
    // 취소, 저장 버튼
    expect(buttons.length).toBeGreaterThanOrEqual(2)
    const saveBtn = buttons[buttons.length - 1]
    await saveBtn.trigger('click')

    expect(wrapper.emitted('save')).toBeTruthy()
  })

  it('취소 버튼 클릭 시 close 또는 update:modelValue 이벤트 방출', async () => {
    const wrapper = mount(MatchingRuleModal, {
      props: {
        modelValue: true,
        isEditMode: true,
        isSaving: false,
        form: {
          ruleName: '기존 규칙',
          matchType: 'FUZZY',
          selectedFields: [],
          targetFieldKeysInput: 'email',
          similarityThreshold: 0.8,
          isActive: true
        },
        matchTypeOptions: [],
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
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    const buttons = wrapper.findAll('.va-btn-stub')
    const cancelBtn = buttons[buttons.length - 2]
    await cancelBtn.trigger('click')

    expect(wrapper.emitted('update:modelValue') || wrapper.emitted('close')).toBeTruthy()
  })
})
