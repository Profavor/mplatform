import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import SchemaTreeNode from '../../components/SchemaTreeNode.vue'

describe('SchemaTreeNode.vue', () => {
  it('renders classification node and emits delete event when delete button is clicked', async () => {
    const wrapper = mount(SchemaTreeNode, {
      props: {
        node: {
          id: 'node-123',
          label: '정규직',
          isDomain: false,
          children: []
        },
        selectedNode: null,
        showEdit: true
      },
      global: {
        stubs: {
          'va-icon': true,
          'va-button': {
            template: '<button class="va-button-stub" @click="$emit(\'click\', $event)"><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.text()).toContain('정규직')

    const deleteBtn = wrapper.find('.delete-btn')
    expect(deleteBtn.exists()).toBe(true)
    await deleteBtn.trigger('click')

    expect(wrapper.emitted('delete')).toBeTruthy()
    expect(wrapper.emitted('delete')?.[0][0]).toEqual(expect.objectContaining({
      id: 'node-123',
      label: '정규직'
    }))
  })

  it('renders delete button for domain root node and emits delete event', async () => {
    const wrapper = mount(SchemaTreeNode, {
      props: {
        node: {
          id: 'domain-123',
          label: '임직원 (Domain)',
          isDomain: true,
          children: []
        },
        selectedNode: null,
        showEdit: true
      },
      global: {
        stubs: {
          'va-icon': true,
          'va-button': {
            template: '<button class="va-button-stub" @click="$emit(\'click\', $event)"><slot /></button>'
          }
        }
      }
    })

    const deleteBtn = wrapper.find('.delete-btn')
    expect(deleteBtn.exists()).toBe(true)
    await deleteBtn.trigger('click')

    expect(wrapper.emitted('delete')).toBeTruthy()
    expect(wrapper.emitted('delete')?.[0][0]).toEqual(expect.objectContaining({
      id: 'domain-123',
      label: '임직원 (Domain)',
      isDomain: true
    }))
  })
})
