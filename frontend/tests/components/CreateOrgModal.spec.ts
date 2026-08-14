import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import CreateOrgModal from '../../components/admin/CreateOrgModal.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('CreateOrgModal.vue (TDD Component Test)', () => {
  it('신규 조직 생성 모달 렌더링 및 저장 이벤트 방출 검증', async () => {
    const form = {
      name: 'CORP_01',
      displayNameKo: '테스트법인',
      displayNameEn: 'Test Corp',
      descriptionKo: '설명',
      descriptionEn: 'Desc'
    }

    const wrapper = mount(CreateOrgModal, {
      props: {
        modelValue: true,
        form: form
      },
      global: {
        stubs: {
          'va-modal': {
            props: ['title'],
            template: '<div class="va-modal-stub"><h3>{{ title }}</h3><slot name="default" /><slot /></div>'
          },
          'va-input': {
            template: '<div class="va-input-stub"><slot name="prependInner" /></div>'
          },
          'va-textarea': {
            template: '<div class="va-textarea-stub"><slot name="prependInner" /></div>'
          },
          'va-button': {
            template: '<button class="va-button-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.text()).toContain('create_new_org')

    wrapper.vm.onSave()
    expect(wrapper.emitted('save')).toBeTruthy()
  })

  it('취소 버튼 클릭 시 모달 닫기 이벤트 방출 검증', async () => {
    const wrapper = mount(CreateOrgModal, {
      props: {
        modelValue: true,
        form: { name: '', displayNameKo: '', displayNameEn: '', descriptionKo: '', descriptionEn: '' }
      },
      global: {
        stubs: {
          'va-modal': {
            props: ['title'],
            template: '<div class="va-modal-stub"><h3>{{ title }}</h3><slot name="default" /><slot /></div>'
          },
          'va-input': true,
          'va-textarea': true,
          'va-button': true
        }
      }
    })

    wrapper.vm.onCancel()
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![0]).toEqual([false])
  })
})
