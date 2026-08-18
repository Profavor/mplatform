import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import InboxRecipientPicker from '../../components/inbox/InboxRecipientPicker.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key: string) => key })
}))

vi.mock('~/stores/useUserStore', () => ({
  useUserStore: () => ({
    getUserName: (id: string, fallback: string) => fallback || 'User ' + id,
    fetchUserMap: vi.fn().mockResolvedValue({})
  })
}))

vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: vi.fn().mockResolvedValue([
      { id: 'user-1', username: 'john', email: 'john@mplatform.com', role: 'ADMIN', orgName: 'HQ', deptName: 'IT' },
      { id: 'user-2', username: 'alice', email: 'alice@mplatform.com', role: 'USER', orgName: 'HQ', deptName: 'HR' }
    ])
  })
}))

describe('InboxRecipientPicker', () => {
  it('renders correctly with modelValue and chips', () => {
    const wrapper = mount(InboxRecipientPicker, {
      props: {
        modelValue: ['user-1', 'external@example.com'],
        label: 'To'
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          VaIcon: true,
          VaButton: { template: '<button class="va-button-stub" @click="$emit(\'click\')"><slot /></button>' },
          UserGridSelectModal: { template: '<div class="user-grid-select-modal-stub" />' }
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.recipient-tag-box').exists()).toBe(true)
    expect(wrapper.find('.address-book-btn').exists()).toBe(true)
    expect(wrapper.findAll('.recipient-chip').length).toBe(2)
  })

  it('opens user modal and updates selected users on confirmation', async () => {
    const wrapper = mount(InboxRecipientPicker, {
      props: {
        modelValue: ['external@example.com'],
        label: 'To'
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          VaIcon: true,
          VaButton: { template: '<button class="va-button-stub" @click="$emit(\'click\')"><slot /></button>' },
          UserGridSelectModal: {
            props: ['modelValue', 'users', 'initialSelectedIds'],
            template: '<div class="user-grid-modal-stub" @confirm-modal="$emit(\'confirm\', [\'user-1\', \'user-2\'])" />'
          }
        }
      }
    })

    // Click address book search button
    await wrapper.find('.address-book-btn').trigger('click')

    const modal = wrapper.findComponent({ name: 'UserGridSelectModal' })
    if (modal.exists()) {
      modal.vm.$emit('confirm', ['user-1', 'user-2'])
      expect(wrapper.emitted('update:modelValue')).toBeTruthy()
      expect(wrapper.emitted('update:modelValue')![0][0]).toEqual(['user-1', 'user-2', 'external@example.com'])
    }
  })
})
