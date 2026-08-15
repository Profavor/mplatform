import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ApprovalSteps from '../../components/ApprovalSteps.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, params?: any) => {
      if (params && params.name) return `${key}:${params.name}`
      if (params && params.time) return `${key}:${params.time}`
      return key
    },
    te: () => false,
    locale: { value: 'ko' }
  })
}))

vi.mock('~/composables/useTimezoneDate', () => ({
  formatWithTimezone: (d: any) => '2026-08-15 12:00:00'
}))

describe('ApprovalSteps.vue', () => {
  it('renders escalated badge and SLA due time when escalated', () => {
    const mockRequest = {
      steps: [
        {
          id: 's1',
          stepOrder: 1,
          stepType: 'APPROVAL',
          assigneeId: 'admin',
          status: 'PENDING',
          isEscalated: true,
          escalatedFromUserId: 'user-origin',
          slaDueAt: '2026-08-15T12:00:00'
        }
      ]
    }

    const wrapper = mount(ApprovalSteps, {
      props: {
        request: mockRequest
      },
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          'va-badge': {
            template: '<span><slot />{{ text }}</span>',
            props: ['text']
          },
          'va-icon': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('sla_escalated_badge:user-origin')
    expect(wrapper.text()).toContain('sla_due:2026-08-15 12:00:00')
  })
})
