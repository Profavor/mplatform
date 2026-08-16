import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import NodeModal from '../../components/schema/NodeModal.vue'

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

describe('NodeModal.vue', () => {
  it('renders edit mode with delete button and emits delete event', async () => {
    const selectedNode = { id: 'node-1', label: '정규직' }
    const wrapper = mount(NodeModal, {
      props: {
        modelValue: true,
        isEditMode: true,
        newNode: {
          name: { ko: '정규직', en: 'Regular' },
          order: 1,
          icon: 'article'
        },
        selectedNode
      },
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          'va-modal': {
            template: '<div><h1>{{ title }}</h1><slot /><slot name="footer" /></div>',
            props: ['title']
          },
          'va-input': true,
          'va-icon': true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.text()).toContain('edit_node')
    expect(wrapper.text()).toContain('delete_node')

    const buttons = wrapper.findAll('button')
    const deleteBtn = buttons.find(b => b.text().includes('delete_node'))
    expect(deleteBtn).toBeDefined()
    await deleteBtn?.trigger('click')

    expect(wrapper.emitted('delete')).toBeTruthy()
    expect(wrapper.emitted('delete')?.[0][0]).toEqual(selectedNode)
  })

  it('renders create mode without delete button', () => {
    const wrapper = mount(NodeModal, {
      props: {
        modelValue: true,
        isEditMode: false,
        newNode: {
          name: { ko: '', en: '' },
          order: 0,
          icon: ''
        },
        selectedNode: { id: 'domain-1', label: '임직원' }
      },
      global: {
        mocks: {
          $t: (k: string, p?: any) => k
        },
        stubs: {
          'va-modal': {
            template: '<div><h1>{{ title }}</h1><slot /><slot name="footer" /></div>',
            props: ['title']
          },
          'va-input': true,
          'va-icon': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.text()).not.toContain('delete_node')
  })
})
