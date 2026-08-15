import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import InstallPage from '../../pages/install.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, fallback?: string) => fallback || key,
    locale: { value: 'ko' }
  })
}))

vi.mock('vuestic-ui', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vuestic-ui')>()
  return {
    ...actual,
    useToast: () => ({
      init: vi.fn()
    })
  }
})

vi.mock('#app', () => ({
  useRouter: () => ({
    push: vi.fn()
  }),
  definePageMeta: vi.fn()
}))

describe('install.vue (System Install Wizard Unit Test)', () => {
  it('설치 폼 데이터에 emailDomain 필드가 포함되어 있고 초기값이 정상 바인딩되는지 검증', () => {
    const wrapper = mount(InstallPage, {
      global: {
        stubs: {
          'va-input': {
            props: ['modelValue', 'placeholder', 'messages'],
            template: '<div class="va-input-stub"><input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" /></div>'
          },
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          },
          'va-icon': true,
          'va-alert': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    const vm = wrapper.vm as any
    expect(vm.form).toBeDefined()
    expect(vm.form.emailDomain).toBe('')

    // Set emailDomain
    vm.form.emailDomain = 'profavor.com'
    expect(vm.form.emailDomain).toBe('profavor.com')
  })
})
