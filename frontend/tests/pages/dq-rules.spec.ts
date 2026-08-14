import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DqRulesPage from '../../pages/admin/dq-rules.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    locale: { value: 'ko' }
  })
}))

vi.mock('~/composables/usePageTitle', () => ({
  usePageTitle: () => ({
    pageTitle: { value: 'DQ 검칙 관리' }
  })
}))

vi.mock('~/composables/usePermission', () => ({
  usePermission: () => ({
    hasPermission: () => true
  })
}))

vi.mock('vuestic-ui', async (importOriginal) => {
  const actual: any = await importOriginal()
  return {
    ...actual,
    useToast: () => ({ init: vi.fn() })
  }
})

vi.mock('#app', () => ({
  useCookie: () => ({ value: 'test-token' })
}))

describe('pages/admin/dq-rules.vue (TDD)', () => {
  it('DQ 검칙 관리 화면 렌더링 검증', async () => {
    const wrapper = mount(DqRulesPage, {
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-icon': true,
          'va-badge': true,
          'va-button': true,
          'va-card': true,
          'va-card-title': true,
          'va-card-content': true,
          'va-select': true,
          'va-input': true,
          'va-modal': true,
          'va-switch': true,
          ClassificationTree: true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })
})
