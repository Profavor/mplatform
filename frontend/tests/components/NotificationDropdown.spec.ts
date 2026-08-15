import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import NotificationDropdown from '../../components/notification/NotificationDropdown.vue'

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: (...args: any[]) => mockCustomFetch(...args)
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    te: () => false,
    locale: { value: 'ko' }
  })
}))

vi.mock('~/composables/useTimezoneDate', () => ({
  formatWithTimezone: () => '2026-08-15 12:00:00'
}))

describe('NotificationDropdown.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: []
      }
    })
  })

  it('renders notification dropdown properly', () => {
    const wrapper = mount(NotificationDropdown, {
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          'va-dropdown': {
            template: '<div><slot name="anchor" /><slot /></div>'
          },
          'va-dropdown-content': {
            template: '<div><slot /></div>'
          },
          'va-button': true,
          'va-icon': true,
          'va-badge': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('notifications')
  })
})
