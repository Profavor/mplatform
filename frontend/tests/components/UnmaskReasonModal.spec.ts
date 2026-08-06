import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import UnmaskReasonModal from '../../components/UnmaskReasonModal.vue'

// Mock vue-i18n
vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    locale: { value: 'ko' }
  })
}))

describe('UnmaskReasonModal.vue', () => {
  it('renders correctly when show is true', async () => {
    const wrapper = mount(UnmaskReasonModal, {
      props: {
        modelValue: true
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-modal': true,
          'va-input': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })

  it('emits confirm with trimmed reason when submit is called', async () => {
    const wrapper = mount(UnmaskReasonModal, {
      props: {
        modelValue: true
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-modal': {
            template: '<div><slot /><slot name="footer" /></div>'
          },
          'va-input': true,
          'va-button': {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    // Set reason
    const vm = wrapper.vm as any
    vm.reason = '  업무 처리 목적  '
    
    // Call submit directly since we stubbed components
    vm.submit()
    await wrapper.vm.$nextTick()

    expect(wrapper.emitted('confirm')).toBeTruthy()
    expect(wrapper.emitted('confirm')![0]).toEqual(['업무 처리 목적'])
    
    // Should update modelValue to false to close modal
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')![wrapper.emitted('update:modelValue')!.length - 1]).toEqual([false])
  })

  it('does not emit confirm if reason is empty', async () => {
    const wrapper = mount(UnmaskReasonModal, {
      props: {
        modelValue: true
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-modal': true,
          'va-input': true,
          'va-button': true
        }
      }
    })

    const vm = wrapper.vm as any
    vm.reason = '   '
    vm.submit()

    expect(wrapper.emitted('confirm')).toBeFalsy()
  })

  it('emits cancel and clears reason when cancel is called', async () => {
    const wrapper = mount(UnmaskReasonModal, {
      props: {
        modelValue: true
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-modal': true,
          'va-input': true,
          'va-button': true
        }
      }
    })

    const vm = wrapper.vm as any
    vm.reason = '일부 작성'
    vm.cancel()
    await wrapper.vm.$nextTick()

    expect(vm.reason).toBe('')
    expect(wrapper.emitted('cancel')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
  })
})
