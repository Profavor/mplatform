import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import InboxPage from '../../pages/inbox.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key: string) => key })
}))

vi.mock('#app', () => ({
  useHead: vi.fn(),
  useCookie: () => ({ value: 'token' })
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn(),
    back: vi.fn()
  })
}))

describe('Inbox Page', () => {
  it('renders InboxModal correctly', () => {
    const wrapper = mount(InboxPage, {
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          InboxModal: {
            props: ['modelValue'],
            template: '<div class="inbox-modal-stub" :data-open="modelValue" />'
          }
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    const modalStub = wrapper.find('.inbox-modal-stub')
    expect(modalStub.exists()).toBe(true)
    expect(modalStub.attributes('data-open')).toBe('true')
  })
})
