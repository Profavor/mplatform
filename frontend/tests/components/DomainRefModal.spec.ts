import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DomainRefModal from '../../components/records/DomainRefModal.vue'

const mockCustomFetch = vi.fn().mockResolvedValue({
  content: [
    { id: 'REC-001', data: '{"EP_NO":"0000001","EP_NAME":"인치국"}' }
  ],
  totalElements: 1
})

vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: mockCustomFetch
  })
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, params?: any) => {
      if (params) {
        let res = key
        for (const [k, v] of Object.entries(params)) {
          res += `_${k}:${v}`
        }
        return res
      }
      return key
    }
  })
}))

describe('DomainRefModal.vue - Server-Side Pagination & Search Filter', () => {
  it('renders modal with search bar and guide message', () => {
    const wrapper = mount(DomainRefModal, {
      props: {
        modelValue: true,
        targetDomainId: 'domain-123',
        idFieldKey: 'EP_NO',
        nameFieldKey: 'EP_NAME',
        domainRefColDefs: [
          { headerName: '사번', field: 'data.EP_NO' },
          { headerName: '이름', field: 'data.EP_NAME' }
        ]
      },
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><h1>{{ title }}</h1><slot /></div>',
            props: ['title']
          },
          'va-input': {
            template: '<input class="va-input-stub" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue', 'placeholder']
          },
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          },
          'va-badge': {
            template: '<span class="va-badge-stub">{{ text }}</span>',
            props: ['text']
          },
          'va-icon': true,
          'AgGridVue': {
            template: '<div class="ag-grid-stub"></div>'
          }
        }
      }
    })

    expect(wrapper.text()).toContain('domain_ref_modal.title')
    expect(wrapper.text()).toContain('domain_ref_modal.guide')
    expect(wrapper.find('.va-input-stub').exists()).toBe(true)
  })

  it('updates search query and clears on reset', async () => {
    const wrapper = mount(DomainRefModal, {
      props: {
        modelValue: true,
        targetDomainId: 'domain-123',
        idFieldKey: 'EP_NO',
        nameFieldKey: 'EP_NAME'
      },
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /></div>'
          },
          'va-input': {
            template: '<input class="va-input-stub" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue']
          },
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          },
          'va-badge': true,
          'va-icon': true,
          'AgGridVue': true
        }
      }
    })

    const input = wrapper.find('.va-input-stub')
    await input.setValue('인치국')
    expect((wrapper.vm as any).searchQuery).toBe('인치국')

    ;(wrapper.vm as any).onReset()
    expect((wrapper.vm as any).searchQuery).toBe('')
  })
})
