import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DomainModal from '../../../components/schema/DomainModal.vue'
import { ref } from 'vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    locale: ref('ko')
  })
}))

vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: vi.fn().mockResolvedValue([])
  })
}))

describe('DomainModal Component (TDD)', () => {
  const dummyFieldOptions = [
    { value: 'f1', text: '상품명 (ITEM_NAME)', type: 'TEXT' },
    { value: 'f2', text: '상품코드 (ITEM_CODE)', type: 'STRING' },
    { value: 'f3', text: '다국어명 (NAME_I18N)', type: 'MULTILINGUAL' },
    { value: 'f4', text: '자동번호 (SEQ)', type: 'AUTO_INCREMENT' },
    { value: 'f5', text: '상품설명 (DESC)', type: 'MULTILINGUAL_HTML' },
    { value: 'f6', text: '이미지 (PHOTO)', type: 'IMAGE' },
    { value: 'f7', text: '기타특수필드 (SPECIAL)', type: 'CUSTOM_TYPE' }
  ]

  it('displayNameFieldOptions는 MULTILINGUAL뿐만 아니라 TEXT, STRING 및 기존 매핑 필드를 포함해야 함', () => {
    const wrapper = mount(DomainModal, {
      props: {
        modelValue: true,
        isEditMode: true,
        newDomain: {
          id: 'dom-1',
          name: { ko: '상품', en: 'Item' },
          description: { ko: '', en: '' },
          displayNameFieldId: 'f7',
          identifierFieldId: 'f4'
        },
        domainFieldOptions: dummyFieldOptions
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          AppModal: { template: '<div><slot /><slot name="footer" /></div>' },
          'va-tabs': true,
          'va-tab': true,
          'va-input': true,
          'va-switch': true,
          'va-select': true,
          'va-button': { template: '<button><slot /></button>' },
          'va-icon': true,
          'va-badge': true
        }
      }
    })

    const vm = wrapper.vm as any
    const displayOptions = vm.displayNameFieldOptions
    const displayOptionValues = displayOptions.map((o: any) => o.value)

    expect(displayOptionValues).toContain('f1')
    expect(displayOptionValues).toContain('f2')
    expect(displayOptionValues).toContain('f3')
    expect(displayOptionValues).toContain('f7')
  })

  it('identifierFieldOptions는 TEXT, STRING, AUTO_INCREMENT, NUMBER, KEY, UUID 및 기존 매핑 필드를 포함해야 함', () => {
    const wrapper = mount(DomainModal, {
      props: {
        modelValue: true,
        isEditMode: true,
        newDomain: {
          id: 'dom-1',
          name: { ko: '상품', en: 'Item' },
          identifierFieldId: 'f4',
          displayNameFieldId: 'f1'
        },
        domainFieldOptions: dummyFieldOptions
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          AppModal: { template: '<div><slot /><slot name="footer" /></div>' },
          'va-tabs': true,
          'va-tab': true,
          'va-input': true,
          'va-switch': true,
          'va-select': true,
          'va-button': { template: '<button><slot /></button>' },
          'va-icon': true,
          'va-badge': true
        }
      }
    })

    const vm = wrapper.vm as any
    const idOptions = vm.identifierFieldOptions
    const idOptionValues = idOptions.map((o: any) => o.value)

    expect(idOptionValues).toContain('f1')
    expect(idOptionValues).toContain('f2')
    expect(idOptionValues).toContain('f4')
    expect(idOptionValues).not.toContain('f6')
  })
})
